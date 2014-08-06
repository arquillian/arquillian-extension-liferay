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

package com.liferay.maven.arquillain.internal;

import java.util.Map;

import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.AbstractPackagingPluginConfiguration;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public class LiferayPluginConfiguration
    extends AbstractPackagingPluginConfiguration {

    public static final String LIFERAY_PLUGIN_GA =
        "com.liferay.maven.plugins:liferay-maven-plugin";

    private String appServerClassesPortalDir;
    private String appServerLibGlobalDir;
    private String appServerPortalDir;
    private String appServerLibPortalDir;
    private String appServerTldPortalDir;
    private String liferayVersion;
    private String pluginType;
    private String autoDeployDir;
    private String appServerDeployDir;
    private String destDir;
    private String baseDir;
    private String filePattern;

    /**
     * @param pomFile
     */
    protected LiferayPluginConfiguration(ParsedPomFile pomFile) {
        super(pomFile);
        Map<String, Object> configValues =
            pomFile.getPluginConfiguration(LIFERAY_PLUGIN_GA);
        appServerClassesPortalDir =
            (String)configValues.get("appServerClassesPortalDir");
        appServerLibGlobalDir =
            (String)configValues.get("appServerLibGlobalDir");
        appServerPortalDir =
            (String)configValues.get("appServerPortalDir");
        appServerLibPortalDir =
            (String)configValues.get("appServerLibPortalDir");
        appServerTldPortalDir =
            (String)configValues.get("appServerTldPortalDir");
        liferayVersion =
            (String)configValues.get("liferayVersion");
        pluginType =
            (String)configValues.get("pluginType");
        autoDeployDir =
            (String)configValues.get("autoDeployDir");
        appServerDeployDir =
            (String)configValues.get("appServerDeployDir");
        baseDir = pomFile.getBuildOutputDirectory().getAbsolutePath();
        destDir = pomFile.getBuildOutputDirectory() + "/tmp";
        filePattern = pomFile.getFinalName();

    }

    /**
     * @return the appServerClassesPortalDir
     */
    public String getAppServerClassesPortalDir() {
        return appServerClassesPortalDir;
    }

    /**
     * @return the appServerLibGlobalDir
     */
    public String getAppServerLibGlobalDir() {
        return appServerLibGlobalDir;
    }

    /**
     * @return the appServerPortalDir
     */
    public String getAppServerPortalDir() {
        return appServerPortalDir;
    }

    /**
     * @return the appServerLibPortalDir
     */
    public String getAppServerLibPortalDir() {
        return appServerLibPortalDir;
    }

    /**
     * @return the appServerTldPortalDir
     */
    public String getAppServerTldPortalDir() {
        return appServerTldPortalDir;
    }

    /**
     * @return the liferayVersion
     */
    public String getLiferayVersion() {
        return liferayVersion;
    }

    /**
     * @return the pluginType
     */
    public String getPluginType() {
        return pluginType;
    }

    /**
     * @return the autoDeployDir
     */
    public String getAutoDeployDir() {
        return autoDeployDir;
    }

    /**
     * @return the appServerDeployDir
     */
    public String getAppServerDeployDir() {
        return appServerDeployDir;
    }

    /**
     * @return the destDir
     */
    public String getDestDir() {
        return destDir;
    }

    /**
     * @return the baseDir
     */
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * @return the filePattern
     */
    public String getFilePattern() {
        return filePattern;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.
     * AbstractPackagingPluginConfiguration#getPluginGA()
     */
    @Override
    public String getPluginGA() {
        return LIFERAY_PLUGIN_GA;
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.
     * AbstractPackagingPluginConfiguration#getIncludes()
     */
    @Override
    public String[] getIncludes() {
        // Nothing here
        return new String[0];
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.
     * AbstractPackagingPluginConfiguration#getExcludes()
     */
    @Override
    public String[] getExcludes() {
        // Nothing here
        return new String[0];
    }

}
