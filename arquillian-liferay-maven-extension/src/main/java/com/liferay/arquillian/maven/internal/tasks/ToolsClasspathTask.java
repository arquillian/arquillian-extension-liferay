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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FileUtils;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptAllStrategy;
import org.jboss.shrinkwrap.resolver.impl.maven.task.MavenWorkingSessionTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public enum ToolsClasspathTask
	implements MavenWorkingSessionTask<URLClassLoader> {

	INSTANCE;

	/**
	 * (non-Javadoc)
	 * @see
	 * org.jboss.shrinkwrap.resolver.impl.maven.task.MavenWorkingSessionTask
	 * #execute(org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession)
	 */
	@Override
	public URLClassLoader execute(MavenWorkingSession session) {
		final ParsedPomFile pomFile = session.getParsedPomFile();

		LiferayPluginConfiguration configuration =
			new LiferayPluginConfiguration(pomFile);

		System.setProperty("liferayVersion", configuration.getLiferayVersion());

		File appServerLibGlobalDir = new File(
			configuration.getAppServerLibGlobalDir());

		File appServerLibPortalDir = new File(
			configuration.getAppServerLibPortalDir());

		List<URI> liferayToolArchives = new ArrayList<>();

		if ((appServerLibGlobalDir != null) && appServerLibGlobalDir.exists()) {

			// app server global libraries

			Collection<File> appServerLibs =
				FileUtils.listFiles(appServerLibGlobalDir, new String[] {
					"jar"
				}, true);

			for (File file : appServerLibs) {
				liferayToolArchives.add(file.toURI());
			}

			// All Liferay Portal Lib jars

			Collection<File> liferayPortalLibs =
				FileUtils.listFiles(appServerLibPortalDir, new String[] {
					"jar"
				}, true);

			for (File file : liferayPortalLibs) {
				liferayToolArchives.add(file.toURI());
			}

			// Util jars

			File[] utilJars = Maven.resolver().loadPomFromClassLoaderResource(
				"liferay-tool-deps.xml").importCompileAndRuntimeDependencies().
				resolve().using(AcceptAllStrategy.INSTANCE).asFile();

			for (int i = 0; i < utilJars.length; i++) {
				liferayToolArchives.add(utilJars[i].toURI());
			}
		}

		if (_log.isTraceEnabled()) {
			_log.trace(
				"Jars count in Tools classpath Archive:" +
					liferayToolArchives.size());
		}

		List<URL> classpathUrls = new ArrayList<>();

		try {
			if (!liferayToolArchives.isEmpty()) {
				ListIterator<URI> toolsJarItr =
					liferayToolArchives.listIterator();
				while (toolsJarItr.hasNext()) {
					URI jarURI = toolsJarItr.next();
					classpathUrls.add(jarURI.toURL());
				}
			}
		}
		catch (MalformedURLException e) {
			_log.error("Error building Tools classpath", e);
		}

		return new URLClassLoader(
			classpathUrls.toArray(new URL[classpathUrls.size()]), null);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		ToolsClasspathTask.class);

}