/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.arquillian.maven.internal.tasks;

import com.liferay.arquillian.maven.internal.LiferayPluginConfiguration;

import java.io.File;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.URLClassLoader;

import java.security.Permission;

import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.impl.maven.util.Validate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public enum ExecuteDeployerTask {

	INSTANCE;

	public WebArchive execute(
		MavenWorkingSession session, LiferayPluginConfiguration configuration,
		Map<String, Object> params) {

		final ParsedPomFile pomFile = session.getParsedPomFile();

		URLClassLoader classLoader = ToolsClasspathTask.INSTANCE.execute(
			session);

		if (classLoader == null) {
			throw new RuntimeException("Error loading classloader");
		}

		System.setProperty(
			"deployer.app.server.type", configuration.getAppServerType());

		System.setProperty("deployer.base.dir", configuration.getBaseDir());

		System.setProperty("deployer.unpack.war", "false");

		System.setProperty("deployer.dest.dir", configuration.getDestDir());

		System.setProperty("deployer.file.pattern", pomFile.getFinalName());

		String deployerClassName = (String)params.get("deployerClassName");

		if (Validate.isNullOrEmpty(deployerClassName)) {
			throw new RuntimeException("Unable to load deployer classname");
		}

		String[] jars = (String[])params.get("jars");

		try {
			initUtils(classLoader);
		}
		catch (Exception e) {
			_log.error(
				"Error executing deployer task :" + deployerClassName, e);
		}

		// START Execute Deployer

		try {
			executeTool(deployerClassName, classLoader, jars);
		}
		catch (Exception e) {
			_log.error(
				"Error executing deployer task :" + deployerClassName, e);
		}

		// END Execute Deployer

		File ddPluginArchiveFile = configuration.getDirectDeployArchive();

		System.setProperty("deployer.file.pattern", pomFile.getFinalName());

		return ShrinkWrap.create(ZipImporter.class, pomFile.getFinalName()).
			importFrom(ddPluginArchiveFile).as(WebArchive.class);
	}

	public static final class SAXReaderUtil {

		public static Document read(File file, boolean validate)
			throws DocumentException {

			SAXReader saxReader = new SAXReader(validate);

			saxReader.setEntityResolver(_entityResolver);

			return saxReader.read(file);
		}

		public static void setEntityResolver(EntityResolver entityResolver) {
			_entityResolver = entityResolver;
		}

		private SAXReaderUtil() {
		}

		private static EntityResolver _entityResolver;

	}

	protected void executeTool(
		String deployerClassName, ClassLoader classLoader,
		String[] args) throws Exception {

		Thread currentThread = Thread.currentThread();

		ClassLoader contextClassLoader = currentThread.getContextClassLoader();

		currentThread.setContextClassLoader(classLoader);

		SecurityManager currentSecurityManager = System.getSecurityManager();

		// Required to prevent premature exit by DBBuilder. See LPS-7524.

		SecurityManager securityManager = new SecurityManager() {

			@Override
			public void checkPermission(Permission permission) {
				//It is not needed to check permissions
			}

			@Override
			public void checkExit(int status) {
				throw new SecurityException();
			}
		};

		System.setSecurityManager(securityManager);

		try {
			System.setProperty(
				"external-properties",
				"com/liferay/portal/tools/dependencies" +
					"/portal-tools.properties");
			System.setProperty(
				"org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.Log4JLogger");

			Class<?> clazz = classLoader.loadClass(deployerClassName);

			Method method = clazz.getMethod("main", String[].class);

			method.invoke(null, (Object)args);
		}
		catch (InvocationTargetException ite) {
			if (!(ite.getCause() instanceof SecurityException)) {
				throw ite;
			}
		}
		finally {
			currentThread.setContextClassLoader(contextClassLoader);

			System.setSecurityManager(currentSecurityManager);
		}
	}

	protected void initUtils(ClassLoader classLoader) throws Exception {
		Class<?> clazz = classLoader.loadClass(
			"com.liferay.portal.util.EntityResolver");

		EntityResolver entityResolver = (EntityResolver)clazz.newInstance();

		SAXReaderUtil.setEntityResolver(entityResolver);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		ExecuteDeployerTask.class);

}