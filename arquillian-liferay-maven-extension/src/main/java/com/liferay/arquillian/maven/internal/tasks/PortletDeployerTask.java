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

import java.util.HashMap;
import java.util.Map;

import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.impl.maven.task.MavenWorkingSessionTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public enum PortletDeployerTask
	implements MavenWorkingSessionTask<MavenWorkingSession> {

	INSTANCE;

	/**
	 * (non-Javadoc)
	 * @see
	 * org.jboss.shrinkwrap.resolver.impl.maven.task.MavenWorkingSessionTask
	 * #execute(org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession)
	 */
	@Override
	public MavenWorkingSession execute(MavenWorkingSession session) {
		if (_log.isDebugEnabled()) {
			_log.debug("Building Portlet Archive");
		}

		Map<String, Object> args = new HashMap<>();
		args.put(
			"deployerClassName",
			"com.liferay.portal.tools.deploy.PortletDeployer");

		final ParsedPomFile pomFile = session.getParsedPomFile();

		LiferayPluginConfiguration configuration =
			new LiferayPluginConfiguration(pomFile);

		String customPortletXml = String.valueOf(
			configuration.isCustomPortletXml());

		String tldPath = configuration.getAppServerTldPortalDir();

		System.setProperty("deployer.aui.taglib.dtd", tldPath + "/aui.tld");

		System.setProperty(
			"deployer.custom.portlet.xml", String.valueOf(customPortletXml));
		System.setProperty(
			"deployer.portlet.taglib.dtd", tldPath + "/liferay-portlet.tld");
		System.setProperty(
			"deployer.portlet-ext.taglib.dtd",
			tldPath + "/liferay-portlet-ext.tld");
		System.setProperty(
			"deployer.security.taglib.dtd", tldPath + "/liferay-security.tld");
		System.setProperty(
			"deployer.theme.taglib.dtd", tldPath + "/liferay-theme.tld");
		System.setProperty(
			"deployer.ui.taglib.dtd", tldPath + "/liferay-ui.tld");
		System.setProperty(
			"deployer.util.taglib.dtd", tldPath + "/liferay-util.tld");

		File appServerLibPortalDir = new File(
			configuration.getAppServerLibPortalDir());

		String libPath = appServerLibPortalDir.getAbsolutePath();

		String[] jars = {
			libPath + "/util-bridges.jar", libPath + "/util-java.jar",
			libPath + "/util-taglib.jar"
		};

		args.put("jars", jars);

		ExecuteDeployerTask.INSTANCE.execute(session, configuration, args);

		return session;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		PortletDeployerTask.class);

}