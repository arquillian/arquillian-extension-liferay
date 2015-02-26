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

import aQute.bnd.osgi.Jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.arquillian.container.liferay.remote.activator.ArquillianBundleActivator;
import org.arquillian.container.liferay.remote.deploy.processor.BundleActivatorsManager;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

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
			(JavaArchive)bundleArchive, testDeployment.getAuxiliaryArchives());
	}

	private void addArquillianDependencies(JavaArchive javaArchive)
		throws Exception {

		javaArchive.addPackage(JMXTestRunner.class.getPackage());
	}

	private void addAttributeValueToListAttributeInManifest(
			JavaArchive javaArchive, String attributeName,
			String attributeValue, String startValue)
		throws IOException {

		Manifest manifest = getManifest(javaArchive);

		Attributes mainAttributes = manifest.getMainAttributes();

		String attributeValues = mainAttributes.getValue(attributeName);

		if ((attributeValues == null) ||
			attributeValues.isEmpty()) {

			if ((startValue != null) && !startValue.isEmpty()) {
				startValue = startValue + ",";
			}

			attributeValues = startValue + attributeValue;
		}
		else {
			attributeValues += "," + attributeValue;
		}

		mainAttributes.putValue(attributeName, attributeValues);

		replaceManifest(javaArchive, manifest);
	}

	private void addBundleActivator(
			JavaArchive javaArchive, String bundleActivatorValue)
		throws IOException {

		Node node = javaArchive.get(_ACTIVATORS_FILE);

		BundleActivatorsManager bundleActivatorsManager =
			new BundleActivatorsManager();

		if (node != null) {
			Asset asset = node.getAsset();

			bundleActivatorsManager = new BundleActivatorsManager(
				asset.openStream());
		}

		List<String> bundleActivators =
			bundleActivatorsManager.getBundleActivators();

		bundleActivators.add(bundleActivatorValue);

		ByteArrayOutputStream bundleActivatorAsOutputStream =
			bundleActivatorsManager.getBundleActivatorAsOutputStream();

		ByteArrayAsset byteArrayAsset = new ByteArrayAsset(
			bundleActivatorAsOutputStream.toByteArray());

		javaArchive.delete(_ACTIVATORS_FILE);

		javaArchive.add(byteArrayAsset, _ACTIVATORS_FILE);
	}

	private void addOsgiImports(JavaArchive javaArchive) throws IOException {
		String extensionsImports =
			"org.osgi.framework" + "," + "javax.management" + "," +
			"javax.management.*" + "," + "javax.naming.*" + "," +
			"org.osgi.service.packageadmin" + "," +
			"org.osgi.service.startlevel" + "," + "org.osgi.util.tracker";

		addAttributeValueToListAttributeInManifest(
			javaArchive, "Import-Package", extensionsImports, "");
	}

	private void deleteImportsIncludedInClassPath(
			JavaArchive javaArchive, Collection<Archive<?>> auxiliaryArchives)
		throws IOException {

		try {
			List<String> packages = new ArrayList<>();

			for (Archive auxiliaryArchive : auxiliaryArchives) {
				ZipExporter zipExporter = auxiliaryArchive.as(
					ZipExporter.class);

				InputStream auxiliaryArchiveInputStream =
					zipExporter.exportAsInputStream();

				Jar jar = new Jar(
					auxiliaryArchive.getName(), auxiliaryArchiveInputStream);

				packages.addAll(jar.getPackages());
			}

			Manifest manifest = getManifest(javaArchive);

			Attributes mainAttributes = manifest.getMainAttributes();

			String importString = mainAttributes.getValue("Import-Package");

			mainAttributes.remove(new Attributes.Name("Import-Package"));

			replaceManifest(javaArchive, manifest);

			String[] imports = importString.split(",");

			for (String importValue : imports) {
				if (!packages.contains(importValue)) {
					addAttributeValueToListAttributeInManifest(
						javaArchive, "Import-Package", importValue, "");
				}
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Manifest getManifest(JavaArchive javaArchive) throws IOException {
		Node manifestNode = javaArchive.get(JarFile.MANIFEST_NAME);

		Asset manifestAsset = manifestNode.getAsset();

		return new Manifest(manifestAsset.openStream());
	}

	private Archive<?> handleArchive(
		JavaArchive javaArchive, Collection<Archive<?>> auxiliaryArchives) {

		try {
			validateBundleArchive(javaArchive);

			addOsgiImports(javaArchive);

			addArquillianDependencies(javaArchive);

			handleAuxiliaryArchives(javaArchive, auxiliaryArchives);

			deleteImportsIncludedInClassPath(javaArchive, auxiliaryArchives);

			Manifest manifest = getManifest(javaArchive);

			Attributes mainAttributes = manifest.getMainAttributes();

			mainAttributes.put(
				new Attributes.Name("Bundle-Activator"),
				ArquillianBundleActivator.class.getCanonicalName());

			replaceManifest(javaArchive, manifest);

			javaArchive.addClass(ArquillianBundleActivator.class);

			return javaArchive;
		}
		catch (RuntimeException rte) {
			throw rte;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(
				"Not a valid OSGi bundle: " + javaArchive, ex);
		}
	}

	private void handleAuxiliaryArchives(
			JavaArchive javaArchive, Collection<Archive<?>> auxiliaryArchives)
		throws Exception {

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

			addAttributeValueToListAttributeInManifest(
				javaArchive, "Bundle-ClassPath", path, ".");

			try {
				validateBundleArchive(auxiliaryArchive);

				Manifest auxiliaryArchiveManifest = getManifest(
					(JavaArchive)auxiliaryArchive);

				Attributes mainAttributes =
					auxiliaryArchiveManifest.getMainAttributes();

				String value = mainAttributes.getValue("Import-package");

				if (value != null) {
					String[] importValues = value.split(",");

					for (String importValue : importValues) {
						addAttributeValueToListAttributeInManifest(
							javaArchive, "Import-Package", importValue, "");
					}
				}

				String bundleActivatorValue = mainAttributes.getValue(
					"Bundle-Activator");

				if ((bundleActivatorValue != null) &&
					!bundleActivatorValue.isEmpty()) {

					addBundleActivator(javaArchive, bundleActivatorValue);
				}
			}
			catch (BundleException e) {
				//If this jar is not a bundle, we should not process
				//the manifest

				System.err.println(
					"Skipping " + javaArchive +":" + e.getMessage());
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

		if (manifest != null) {
			OSGiManifestBuilder.validateBundleManifest(manifest);
		}
		else {
			throw new BundleException("can't obtain Manifest");
		}
	}

	private static final String _ACTIVATORS_FILE =
		"/META-INF/services/" + BundleActivator.class.getCanonicalName();

	private static final String _REMOTE_LOADABLE_EXTENSION_FILE =
		"/META-INF/services/" +
		RemoteLoadableExtension.class.getCanonicalName();

}