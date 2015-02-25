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

package org.arquillian.container.liferay.remote.deploy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.arquillian.container.liferay.remote.activator.ArquillianBundleActivator;
import org.arquillian.container.liferay.remote.enricher.Inject;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;

/**
 * @author Cristina González Castellano
 */
public class OSGiDeploymentPackager implements DeploymentPackager {

	public Archive<?> generateDeployment(
		TestDeployment testDeployment,
		Collection<ProtocolArchiveProcessor> processors) {

		Archive<?> bundleArchive = testDeployment.getApplicationArchive();

		return handleArchive(
			bundleArchive, testDeployment.getAuxiliaryArchives());
	}

	public class ManifestConfig {

		public ManifestConfig(
			List<String> activators, OSGiManifestBuilder builder,
			List<String> classPaths, List<String> exports,
			List<String> imports) {

			_activators = activators;
			_builder = builder;
			_classPaths = classPaths;
			_exports = exports;
			_imports = imports;
		}

		public List<String> getActivators() {
			return _activators;
		}

		public OSGiManifestBuilder getBuilder() {
			return _builder;
		}

		public List<String> getClassPaths() {
			return _classPaths;
		}

		public List<String> getExports() {
			return _exports;
		}

		public List<String> getImports() {
			return _imports;
		}

		private final List<String> _activators;
		private final OSGiManifestBuilder _builder;
		private final List<String> _classPaths;
		private final List<String> _exports;
		private final List<String> _imports;

	}

	private void addArquillianDependencies(JavaArchive javaArchive)
		throws Exception {

		addDependencyToArchive(
			javaArchive, "org.jboss.arquillian.protocol",
			"arquillian-protocol-jmx", "1.1.7.Final");
	}

	private void addBundleClasspath(ManifestConfig manifestConfig) {
		String bundleClassPath = ".";

		for (String classPath : manifestConfig.getClassPaths()) {
			bundleClassPath = bundleClassPath + "," + classPath;
		}

		manifestConfig.getBuilder().addBundleClasspath(bundleClassPath);
	}

	private void addDependencyToArchive(
			JavaArchive javaArchive, String groupId, String artifactId,
			String version)
		throws Exception {

		String filespec = groupId + ":" + artifactId + ":jar:" + version;

		MavenResolverSystem resolver = Maven.resolver();

		MavenStrategyStage mavenStrategyStage = resolver.resolve(filespec);

		MavenFormatStage mavenFormatStage =
			mavenStrategyStage.withoutTransitivity();

		File[] resolved = mavenFormatStage.asFile();

		if (resolved == null || resolved.length == 0)
			throw new BundleException(
				"Cannot obtain maven artifact: " + filespec);

		if (resolved.length > 1)
			throw new BundleException(
				"Multiple maven artifacts for: " + filespec);

		String path = "lib/" + resolved[0].getName();

		javaArchive.addAsResource(new FileAsset(resolved[0]), path);

		Manifest manifest = getManifest(javaArchive);

		Attributes mainAttributes = manifest.getMainAttributes();

		String bundleClasspath = mainAttributes.getValue("Bundle-ClassPath");

		if ((bundleClasspath == null) || bundleClasspath.isEmpty()) {
			bundleClasspath = ".," + path;
		}
		else {
			bundleClasspath += "," + path;
		}

		mainAttributes.putValue("Bundle-ClassPath", bundleClasspath);

		replaceManifest(javaArchive, manifest);
	}

	private void addManifestToArchive(
			JavaArchive javaArchive, ManifestConfig manifestConfig)
		throws IOException {

		List<String> filteredImports = new ArrayList<>();

		for (String importValue : manifestConfig.getImports()) {
			if (!importValue.contains("org.jboss.arquillian") &&
				!importValue.contains("junit") &&
				!importValue.contains(Inject.class.getPackage().getName())) {

				filteredImports.add(importValue);
			}
		}

		OSGiManifestBuilder builder = manifestConfig.getBuilder();

		builder.addImportPackages(
			(String[])filteredImports.toArray(
				new String[filteredImports.size()]));

		List<String> exports = manifestConfig.getExports();

		builder.addExportPackages(
			(String[])exports.toArray(new String[exports.size()]));

		addBundleClasspath(manifestConfig);

		builder.addBundleActivator(ArquillianBundleActivator.class);
		javaArchive.addClass(ArquillianBundleActivator.class);

		Manifest manifest = builder.getManifest();

		ByteArrayOutputStream manifestOutputStream =
			new ByteArrayOutputStream();

		manifest.write(manifestOutputStream);

		ByteArrayAsset manifestAsset = new ByteArrayAsset(
			manifestOutputStream.toByteArray());

		javaArchive.delete(JarFile.MANIFEST_NAME);

		javaArchive.add(manifestAsset, JarFile.MANIFEST_NAME);

		String activatorsString = "";

		for (String activator : manifestConfig.getActivators()) {
			activatorsString += activator + "\n";
		}

		manifestOutputStream = new ByteArrayOutputStream();

		manifestOutputStream.write(activatorsString.getBytes());

		manifestAsset = new ByteArrayAsset(manifestOutputStream.toByteArray());

		javaArchive.add(manifestAsset, _ACTIVATORS_FILE);
	}

	private void addOsgiImports(JavaArchive javaArchive) throws IOException {
		Manifest manifest = getManifest(javaArchive);

		Attributes mainAttributes = manifest.getMainAttributes();

		String imports = mainAttributes.getValue("Import-Package");

		String extensionsImports =
			"org.osgi.framework" + "," + "javax.management" + "," +
			"javax.management.*" + "," + "javax.naming.*" + ","  +
			"org.osgi.service.packageadmin" + "," +
			"org.osgi.service.startlevel" + "," + "org.osgi.util.tracker";

		if ((imports == null) || imports.isEmpty()) {
			imports = extensionsImports;
		}
		else {
			imports += "," + extensionsImports;
		}

		mainAttributes.putValue("Import-Package", imports);

		replaceManifest(javaArchive, manifest);
	}

	private Manifest getManifest(JavaArchive javaArchive) throws IOException {
		Node manifestNode = javaArchive.get(JarFile.MANIFEST_NAME);

		Asset manifestAsset = manifestNode.getAsset();

		return new Manifest(manifestAsset.openStream());
	}

	private ManifestConfig getManifestConfig(JavaArchive javaArchive)
		throws Exception {

		Manifest manifest = getManifest(javaArchive);

		List<String> imports = new ArrayList<>();
		List<String> exports = new ArrayList<>();
		List<String> activators = new ArrayList<>();
		List<String> classPaths = new ArrayList<>();

		OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();

		Attributes mainAttributes = manifest.getMainAttributes();

		for (Map.Entry<Object, Object> entry : mainAttributes.entrySet()) {
			String key = entry.getKey().toString();
			String value = (String)entry.getValue();

			if (key.equals("Manifest-Version")) {
				continue;
			}

			if (key.equals("Bundle-Activator")) {
				activators.add(value);
				continue;
			}

			if (key.equals("Bundle-ClassPath")) {
				classPaths.add(value);
				continue;
			}

			if (key.equals("Import-Package")) {
				String[] importsValue = value.split(",");

				for (String importValue : importsValue) {
					imports.add(importValue);
				}

				continue;
			}

			if (key.equals("Export-Package")) {
				exports.addAll(Arrays.asList(value.split(",")));
				continue;
			}

			builder.addManifestHeader(key, value);
		}

		return new ManifestConfig(
			activators, builder, classPaths, exports, imports);
	}

	private Archive<?> handleArchive(
		Archive<?> archive, Collection<Archive<?>> auxiliaryArchives) {

		try {
			validateBundleArchive(archive);

			JavaArchive javaArchive = (JavaArchive)archive;

			addOsgiImports(javaArchive);

			addArquillianDependencies(javaArchive);

			ManifestConfig manifestConfig = getManifestConfig(javaArchive);

			handleAuxiliaryArchives(
				javaArchive, manifestConfig, auxiliaryArchives);

			addManifestToArchive(javaArchive, manifestConfig);

			return javaArchive;
		}
		catch (RuntimeException rte) {
			throw rte;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(
				"Not a valid OSGi bundle: " + archive, ex);
		}
	}

	private void handleAuxiliaryArchives(
			JavaArchive javaArchive, ManifestConfig manifestConfig,
			Collection<Archive<?>> auxiliaryArchives)
		throws IOException {

		for (Archive auxiliaryArchive : auxiliaryArchives) {
			Map<ArchivePath, Node> remoteLoadableExtensionMap =
				auxiliaryArchive.getContent(
					Filters.include(_REMOTE_LOADABLE_EXTENSION_FILE));

			Collection<Node> remoteLoadableExtensions =
				remoteLoadableExtensionMap.values();

			if (remoteLoadableExtensions.size() > 1) {
				throw new RuntimeException(
					"The archive " + auxiliaryArchive.getName() +
						" contains more than one RemoteLoadableExtension file");
			}

			if (remoteLoadableExtensions.size() == 1) {
				Iterator<Node> remoteLoadableExtensionsIterator =
					remoteLoadableExtensions.iterator();

				Node remoteLoadableExtensionsNext =
					remoteLoadableExtensionsIterator.next();

				javaArchive.add(
					remoteLoadableExtensionsNext.getAsset(),
					_REMOTE_LOADABLE_EXTENSION_FILE);
			}

			ZipExporter auxiliaryArchiveZipExporter = auxiliaryArchive.as(
				ZipExporter.class);

			InputStream auxiliaryArchiveInputStream =
				auxiliaryArchiveZipExporter.exportAsInputStream();

			ByteArrayAsset byteArrayAsset = new ByteArrayAsset(
				auxiliaryArchiveInputStream);

			String path = "extension/" + auxiliaryArchive.getName();

			javaArchive.addAsResource(byteArrayAsset, path);

			manifestConfig.getClassPaths().add(path);

			Node manifestFile = auxiliaryArchive.get(JarFile.MANIFEST_NAME);

			if (manifestFile != null) {
				Asset ManifestFileAsset = manifestFile.getAsset();

				Manifest auxiliaryArchiveManifest = new Manifest(
					ManifestFileAsset.openStream());

				if (OSGiManifestBuilder.isValidBundleManifest(
						auxiliaryArchiveManifest)) {

					Attributes mainAttributes =
						auxiliaryArchiveManifest.getMainAttributes();

					String value = mainAttributes.getValue("Import-package");

					if (value != null) {
						String[] importsValue = value.split(",");

						for (String importValue : importsValue) {
							manifestConfig.getImports().add(importValue);
						}
					}

					String bundleActivator = mainAttributes.getValue(
						"Bundle-Activator");

					if (bundleActivator != null) {
						manifestConfig.getActivators().add(bundleActivator);
					}
				}
			}
		}
	}

	private void replaceManifest(Archive archive, Manifest manifest )
		throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		manifest.write(baos);

		ByteArrayAsset byteArrayAsset = new ByteArrayAsset(baos.toByteArray());

		archive.delete(JarFile.MANIFEST_NAME);

		archive.add(byteArrayAsset, JarFile.MANIFEST_NAME);
	}

	private void validateBundleArchive(Archive<?> archive) throws Exception {
		Manifest manifest = null;

		Node node = archive.get(JarFile.MANIFEST_NAME);

		if (node != null) {
			manifest = new Manifest(node.getAsset().openStream());
		}

		OSGiManifestBuilder.validateBundleManifest(manifest);
	}

	private static final String _ACTIVATORS_FILE =
		"/META-INF/services/" + BundleActivator.class.getCanonicalName();

	private static final String _REMOTE_LOADABLE_EXTENSION_FILE =
		"/META-INF/services/" +
		RemoteLoadableExtension.class.getCanonicalName();

}