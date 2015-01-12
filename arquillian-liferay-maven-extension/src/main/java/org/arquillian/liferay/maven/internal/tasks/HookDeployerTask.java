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

package org.arquillian.liferay.maven.internal.tasks;

import java.io.File;

import java.util.HashMap;

import org.arquillian.liferay.maven.internal.LiferayPluginConfiguration;

import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.impl.maven.task.MavenWorkingSessionTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public enum HookDeployerTask
    implements MavenWorkingSessionTask<MavenWorkingSession> {

    INSTANCE;

    /*
     * (non-Javadoc)
     * @see
     * org.jboss.shrinkwrap.resolver.impl.maven.task.MavenWorkingSessionTask
     * #execute(org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession)
     */
    @Override
    public MavenWorkingSession execute(MavenWorkingSession session) {

        final Logger log =
            LoggerFactory.getLogger(HookDeployerTask.class);

        log.debug("Building Hook Archive");

        HashMap<String, Object> args = new HashMap<>();
        args.put(
            "deployerClassName", "com.liferay.portal.tools.deploy.HookDeployer");

        final ParsedPomFile pomFile = session.getParsedPomFile();

        LiferayPluginConfiguration configuration =
            new LiferayPluginConfiguration(pomFile);

        File appServerLibPortalDir =
            new File(configuration.getAppServerLibPortalDir());

        String libPath = appServerLibPortalDir.getAbsolutePath();

        String[] jars = {
            libPath + "/util-java.jar"
        };
        args.put("jars", jars);

        ExecuteDeployerTask.INSTANCE.execute(session, configuration, args);

        return session;
    }

}
