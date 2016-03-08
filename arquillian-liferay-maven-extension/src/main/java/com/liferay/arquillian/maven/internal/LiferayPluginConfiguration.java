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

package com.liferay.arquillian.maven.internal;

import java.io.File;

import java.util.Map;

import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.AbstractPackagingPluginConfiguration;
import org.jboss.shrinkwrap.resolver.impl.maven.util.Validate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public class LiferayPluginConfiguration
	extends AbstractPackagingPluginConfiguration {

	public static final String LIFERAY_PLUGIN_GA =
		"com.liferay.maven.plugins:liferay-maven-plugin";

	/**
	 * @param pomFile
	 */
	public LiferayPluginConfiguration(ParsedPomFile pomFile) {
		super(pomFile);
		Map<String, Object> configValues = pomFile.getPluginConfiguration(
			LIFERAY_PLUGIN_GA);

		if (_log.isTraceEnabled()) {
			_log.trace("Liferay Plugin Configuration:" + configValues);
		}

		appServerLibGlobalDir = (String)configValues.get(
			"appServerLibGlobalDir");

		if (Validate.isNullOrEmpty(appServerLibGlobalDir)) {
			throw new RuntimeException(
				"Please configure 'appServerLibGlobalDir' in maven liferay " +
					"plugin");
		}

		appServerPortalDir = (String)configValues.get("appServerPortalDir");

		if (Validate.isNullOrEmpty(appServerPortalDir)) {
			throw new RuntimeException(
				"Please configure 'appServerPortalDir' in maven liferay " +
					"plugin");
		}

		appServerClassesPortalDir = (String)configValues.get(
			"appServerClassesPortalDir");

		if (Validate.isNullOrEmpty(appServerClassesPortalDir)) {
			appServerClassesPortalDir = appServerPortalDir + "/WEB-INF/classes";
		}

		appServerLibPortalDir = (String)configValues.get(
			"appServerLibPortalDir");

		if (Validate.isNullOrEmpty(appServerLibPortalDir)) {
			appServerLibPortalDir = appServerPortalDir + "/WEB-INF/lib";
		}

		appServerTldPortalDir = (String)configValues.get(
			"appServerTldPortalDir");

		if (Validate.isNullOrEmpty(appServerTldPortalDir)) {
			appServerTldPortalDir = appServerPortalDir + "/WEB-INF/tld";
		}

		liferayVersion = (String)configValues.get("liferayVersion");

		if (Validate.isNullOrEmpty(liferayVersion)) {
			throw new RuntimeException(
				"Please configure 'liferayVersion' in maven liferay plugin");
		}

		pluginType = (String)configValues.get("pluginType");

		autoDeployDir = (String)configValues.get("autoDeployDir");

		appServerDeployDir = (String)configValues.get("appServerDeployDir");

		customPortletXml = Boolean.parseBoolean(
			(String)configValues.get("customPortletXml"));

		baseDir = pomFile.getBuildOutputDirectory().getAbsolutePath();
		destDir = pomFile.getBuildOutputDirectory().getAbsolutePath() + "/..";
		directDeployArchive = new File(
			pomFile.getBuildOutputDirectory(), pomFile.getFinalName());
	}

	/**
	 * @return the appServerClassesPortalDir
	 */
	public String getAppServerClassesPortalDir() {
		return appServerClassesPortalDir;
	}

	/**
	 * @return the appServerDeployDir
	 */
	public String getAppServerDeployDir() {
		return appServerDeployDir;
	}

	/**
	 * @return the appServerLibGlobalDir
	 */
	public String getAppServerLibGlobalDir() {
		return appServerLibGlobalDir;
	}

	/**
	 * @return the appServerLibPortalDir
	 */
	public String getAppServerLibPortalDir() {
		return appServerLibPortalDir;
	}

	/**
	 * @return the appServerPortalDir
	 */
	public String getAppServerPortalDir() {
		return appServerPortalDir;
	}

	/**
	 * @return the appServerTldPortalDir
	 */
	public String getAppServerTldPortalDir() {
		return appServerTldPortalDir;
	}

	public String getAppServerType() {
		return _APP_SERVER_TYPE;
	}

	/**
	 * @return the autoDeployDir
	 */
	public String getAutoDeployDir() {
		return autoDeployDir;
	}

	public String getBaseDir() {
		return baseDir;
	}

	/**
	 * @return the destDir
	 */
	public String getDestDir() {
		return destDir;
	}

	public File getDirectDeployArchive() {
		return directDeployArchive;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.
	 * AbstractPackagingPluginConfiguration#getExcludes()
	 */
	@Override
	public String[] getExcludes() {

		// Nothing here

		return new String[0];
	}

	/**
	 * (non-Javadoc)
	 * @see org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.
	 * AbstractPackagingPluginConfiguration#getIncludes()
	 */
	@Override
	public String[] getIncludes() {

		// Nothing here

		return new String[0];
	}

	/**
	 * @return the liferayVersion
	 */
	public String getLiferayVersion() {
		return liferayVersion;
	}

	/**
	 * (non-Javadoc)
	 * @see org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.
	 * AbstractPackagingPluginConfiguration#getPluginGA()
	 */
	@Override
	public String getPluginGA() {
		return LIFERAY_PLUGIN_GA;
	}

	/**
	 * @return the pluginType
	 */
	public String getPluginType() {
		return pluginType;
	}

	public boolean isCustomPortletXml() {
		return customPortletXml;
	}

	// TODO change

	private static final String _APP_SERVER_TYPE = "tomcat";
	private static final Logger _log = LoggerFactory.getLogger(
		LiferayPluginConfiguration.class);

	private String appServerClassesPortalDir;
	private String appServerDeployDir;
	private String appServerLibGlobalDir;
	private String appServerLibPortalDir;
	private String appServerPortalDir;
	private String appServerTldPortalDir;
	private String autoDeployDir;
	private String baseDir;
	private boolean customPortletXml;
	private String destDir;
	private File directDeployArchive;
	private String liferayVersion;
	private String pluginType;

}