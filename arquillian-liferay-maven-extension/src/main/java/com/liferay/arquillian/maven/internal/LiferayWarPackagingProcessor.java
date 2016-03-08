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

import com.liferay.arquillian.maven.internal.tasks.HookDeployerTask;
import com.liferay.arquillian.maven.internal.tasks.PortletDeployerTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;

import org.codehaus.plexus.util.DirectoryScanner;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporterException;
import org.jboss.shrinkwrap.resolver.api.maven.pom.ParsedPomFile;
import org.jboss.shrinkwrap.resolver.api.maven.pom.Resource;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.MavenResolutionStrategy;
import org.jboss.shrinkwrap.resolver.impl.maven.archive.packaging.AbstractCompilingProcessor;
import org.jboss.shrinkwrap.resolver.impl.maven.archive.packaging.ArchiveFilteringUtils;
import org.jboss.shrinkwrap.resolver.impl.maven.archive.plugins.WarPluginConfiguration;
import org.jboss.shrinkwrap.resolver.impl.maven.task.AddAllDeclaredDependenciesTask;
import org.jboss.shrinkwrap.resolver.impl.maven.util.Validate;
import org.jboss.shrinkwrap.resolver.spi.maven.archive.packaging.PackagingProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public class LiferayWarPackagingProcessor
	extends AbstractCompilingProcessor<WebArchive>
	implements PackagingProcessor<WebArchive> {

	/**
	 * (non-Javadoc)
	 * @see
	 * org.jboss.shrinkwrap.resolver.spi.maven.archive.packaging.PackagingProcessor
	 * #configure(org.jboss.shrinkwrap.api.Archive,
	 * org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession)
	 */
	@Override
	public LiferayWarPackagingProcessor configure(
		Archive<?> originalArchive, MavenWorkingSession session) {

		super.configure(session);

		archive = ShrinkWrap.create(WebArchive.class);

		return this;
	}

	/**
	 * (non-Javadoc)
	 * @see
	 * org.jboss.shrinkwrap.resolver.spi.maven.archive.packaging.PackagingProcessor
	 * #getResultingArchive()
	 */
	@Override
	public WebArchive getResultingArchive() {
		log.trace("Resulting Archive:" + archive.toString(Formatters.VERBOSE));

		return archive;
	}

	/**
	 * (non-Javadoc)
	 * @see
	 * org.jboss.shrinkwrap.resolver.spi.maven.archive.packaging.PackagingProcessor
	 * #handles(org.jboss.shrinkwrap.resolver.api.maven.PackagingType)
	 */
	@Override
	public boolean handles(PackagingType packagingType) {
		return PackagingType.WAR.equals(packagingType);
	}

	/**
	 * (non-Javadoc)
	 * @see
	 * org.jboss.shrinkwrap.resolver.spi.maven.archive.packaging.PackagingProcessor
	 * #importBuildOutput(org.jboss.shrinkwrap.resolver.api.maven.strategy.
	 * MavenResolutionStrategy)
	 */
	@Override
	public LiferayWarPackagingProcessor importBuildOutput(
		MavenResolutionStrategy strategy) {

		log.debug("Building Liferay Plugin Archive");

		ParsedPomFile pomFile = session.getParsedPomFile();

		// Compile and add Java classes

		if (Validate.isReadable(pomFile.getSourceDirectory())) {
			compile(
				pomFile.getSourceDirectory(), pomFile.getBuildOutputDirectory(),
				ScopeType.COMPILE, ScopeType.RUNTIME, ScopeType.SYSTEM,
				ScopeType.IMPORT, ScopeType.PROVIDED);
			JavaArchive classes =
				ShrinkWrap.create(ExplodedImporter.class, "webinf_clases.jar").
					importDirectory(
						pomFile.getBuildOutputDirectory()).as(
							JavaArchive.class);

			archive = archive.merge(
				classes, ArchivePaths.create("WEB-INF/classes"));

			// Raise bug with shrink wrap ?Since configure creates the base war
			// in target classes, we need to delete from the archive

			log.trace(
				"Removing temp file: " + pomFile.getFinalName() +
					" form archive");
			archive.delete(
				ArchivePaths.create("WEB-INF/classes", pomFile.getFinalName()));
		}

		// Add Resources

		for (Resource resource : pomFile.getResources()) {
			archive.addAsResource(
				resource.getSource(), resource.getTargetPath());
		}

		// Webapp build

		WarPluginConfiguration warPluginConfiguration =
			new WarPluginConfiguration(pomFile);

		if (Validate.isReadable(
				warPluginConfiguration.getWarSourceDirectory())) {

			WebArchive webapp =
				ShrinkWrap.create(ExplodedImporter.class, "webapp.war").
					importDirectory(
						warPluginConfiguration.getWarSourceDirectory(),
						applyFilter(warPluginConfiguration)).as(
					WebArchive.class);

			archive.merge(webapp);
		}

		// Add manifest

		try {
			Manifest manifest =
				warPluginConfiguration.getArchiveConfiguration().asManifest();

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			manifest.write(bout);

			archive.setManifest(new StringAsset(bout.toString()));
		}
		catch (MavenImporterException e) {
			log.error("Error adding manifest", e);
		}
		catch (IOException e) {
			log.error("Error adding manifest", e);
		}

		// add dependencies

		this.session = AddAllDeclaredDependenciesTask.INSTANCE.execute(session);

		final Collection<MavenResolvedArtifact> artifacts =
			session.resolveDependencies(strategy);

		for (MavenResolvedArtifact artifact : artifacts) {
			archive.addAsLibrary(artifact.asFile());
		}

		// Archive Filtering

		archive = ArchiveFilteringUtils.filterArchiveContent(
			archive, WebArchive.class, warPluginConfiguration.getIncludes(),
			warPluginConfiguration.getExcludes());

		// Liferay Plugin Deployer

		LiferayPluginConfiguration liferayPluginConfiguration =
			new LiferayPluginConfiguration(pomFile);

		// Temp Archive for processing by Liferay deployers

		String baseDirPath = liferayPluginConfiguration.getBaseDir();

		File tempDestFile = new File(baseDirPath, pomFile.getFinalName());

		File baseDir = new File(baseDirPath);

		if (!baseDir.exists()) {
			baseDir.mkdirs();

			log.info("Created dir " + baseDir);
		}

		log.trace("Temp Archive:" + tempDestFile.getName());

		archive.as(ZipExporter.class).exportTo(tempDestFile, true);

		FileUtils.deleteQuietly(new File(pomFile.getFinalName()));

		if ("hook".equals(liferayPluginConfiguration.getPluginType())) {

			// perform hook deployer task

			HookDeployerTask.INSTANCE.execute(session);
		}
		else {

			// default is always portletdeployer

			PortletDeployerTask.INSTANCE.execute(session);
		}

		// Call Liferay Deployer

		LiferayPluginConfiguration configuration =
			new LiferayPluginConfiguration(pomFile);

		File ddPluginArchiveFile = new File(
			configuration.getDestDir(), pomFile.getArtifactId() + ".war");
		archive =
			ShrinkWrap.create(ZipImporter.class, pomFile.getFinalName()).
				importFrom(ddPluginArchiveFile).as(WebArchive.class);

		try {
			FileUtils.forceDelete(ddPluginArchiveFile);

			FileUtils.forceDelete(
				new File(configuration.getBaseDir(), pomFile.getFinalName()));
		}
		catch (IOException e) {

			// nothing to do

		}

		return this;
	}

	private Filter<ArchivePath> applyFilter(
		WarPluginConfiguration warPluginConfiguration) {

		final List<String> filesToIncludes = Arrays.asList(
			includesFiles(
				warPluginConfiguration.getWarSourceDirectory(),
				warPluginConfiguration.getExcludes()));

		return new Filter<ArchivePath>() {

			@Override
			public boolean include(ArchivePath archivePath) {
				final String strFilePath = archivePath.get();

				if (filesToIncludes.contains(strFilePath)) {
					return true;
				}

				for (String fileToInclude : filesToIncludes) {
					if (fileToInclude.startsWith(strFilePath)) {
						return true;
					}
				}

				return false;
			}

		};
	}

	private String[] includesFiles(File baseDir, String[] excludes) {
		DirectoryScanner dirScanner = new DirectoryScanner();
		dirScanner.setBasedir(baseDir);

		if (excludes != null) {
			dirScanner.setExcludes(excludes);
		}

		dirScanner.addDefaultExcludes();
		dirScanner.scan();

		final String[] includedFiles = dirScanner.getIncludedFiles();

		// ? dont know why we need this

		for (int i = 0; i < includedFiles.length; i++) {
			includedFiles[i] =
				"/" + includedFiles[i].replace(File.separator, "/");
		}

		return includedFiles;
	}

	private static final Logger log = LoggerFactory.getLogger(
		LiferayWarPackagingProcessor.class);

	private WebArchive archive;

}