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

package com.liferay.arquillian.maven.importer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;

import org.junit.AfterClass;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public class LiferayPluginTestCase {

	@AfterClass
	public static void cleanup() {
		plexusContainer.dispose();
	}

	protected static void setupPortalMinimal() {
		System.setProperty("liferay.version", LIFERAY_VERSION);

		System.setProperty("liferay.auto.deploy.dir", PORTAL_AUTO_DEPLOY_DIR);

		System.setProperty(
			"liferay.app.server.deploy.dir", PORTAL_SERVER_DEPLOY_DIR);

		System.setProperty(
			"liferay.app.server.lib.global.dir", PORTAL_SERVER_LIB_GLOBAL_DIR);

		System.setProperty("liferay.app.server.portal.dir", SERVER_PORTAL_DIR);

		try {
			ArchiverManager archiverManager = plexusContainer.lookup(
				ArchiverManager.class);

			assertNotNull(archiverManager);

			FileUtils.forceMkdir(new File(PORTAL_AUTO_DEPLOY_DIR));
			FileUtils.forceMkdir(new File(PORTAL_SERVER_DEPLOY_DIR));
			FileUtils.forceMkdir(new File(PORTAL_SERVER_LIB_GLOBAL_DIR));
			FileUtils.forceMkdir(new File(SERVER_PORTAL_DIR));

			final MavenResolverSystem mavenResolverSystem =
				Maven.configureResolver().fromClassloaderResource(
					"settings.xml");

			File[] dependencies =
				mavenResolverSystem.loadPomFromClassLoaderResource(
					"liferay-setup.xml").importRuntimeAndTestDependencies().
					resolve().withoutTransitivity().asFile();

			File warFile = null;

			for (File file : dependencies) {
				String fileName = file.getName();
				String fileExtension = FilenameUtils.getExtension(fileName);

				if (fileExtension.equalsIgnoreCase("jar")) {
					FileUtils.copyFile(
						file, new File(PORTAL_SERVER_LIB_GLOBAL_DIR,
						file.getName()));
				}
				else if (fileExtension.equalsIgnoreCase("war") &&
						 fileName.contains("portal-web")) {

					warFile = file;
				}
			}

			assertNotNull(warFile);

			// extract portal war

			UnArchiver unArchiver = archiverManager.getUnArchiver(warFile);
			unArchiver.setDestDirectory(new File(SERVER_PORTAL_DIR));
			unArchiver.setSourceFile(warFile);
			unArchiver.setOverwrite(false);
			unArchiver.extract();
			setup = true;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 */
	protected static final String LIFERAY_VERSION = "6.2.1";

	protected static final String PORTAL_AUTO_DEPLOY_DIR =
		"target/lportal/deploy";

	protected static final String PORTAL_SERVER_DEPLOY_DIR =
		"target/lportal/webapps";

	protected static final String PORTAL_SERVER_LIB_GLOBAL_DIR =
		"target/lportal/lib/ext";

	protected static final String SERVER_PORTAL_DIR =
		"target/lportal/webapps/ROOT";

	protected static PlexusContainer plexusContainer;
	protected static boolean setup;

	static {
		try {
			plexusContainer = new DefaultPlexusContainer();
		}
		catch (PlexusContainerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}