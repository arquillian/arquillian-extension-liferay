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

import java.io.File;
import java.io.IOException;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
public class LiferayHookMavenImporterTestCase extends LiferayPluginTestCase {

	@BeforeClass
	public static void setupPortal() throws IOException {
		if (!setup) {
			setupPortalMinimal();
		}
	}

	@Before
	public void cleanTarget() {
		new File("src/it/demo-hook/target").delete();
	}

	@Test
	public void importWar() {

		// When

		final WebArchive archive = doImport("src/it/demo-hook/pom.xml");

		// Then

		assertNotNull(
			archive.get(ArchivePaths.create("/WEB-INF/lib", "util-java.jar")));
		assertNotNull(
			archive.get(
				ArchivePaths.create("/WEB-INF/lib", "commons-logging.jar")));
		assertNotNull(
			archive.get(
				ArchivePaths.create("/WEB-INF/lib", "log4j-extras.jar")));
		assertNotNull(
			archive.get(ArchivePaths.create("/WEB-INF", "liferay-hook.xml")));
		assertNotNull(
			archive.get(
				ArchivePaths.create("/WEB-INF/classes", "log4j.properties")));
	}

	private WebArchive doImport(String pomFile) {
		try {

			// When

			WebArchive archive =
				ShrinkWrap.create(MavenImporter.class).loadPomFromFile(pomFile).
					importBuildOutput().as(WebArchive.class);

			return archive;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}