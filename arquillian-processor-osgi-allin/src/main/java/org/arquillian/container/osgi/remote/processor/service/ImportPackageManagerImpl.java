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

package org.arquillian.container.osgi.remote.processor.service;

import aQute.bnd.osgi.Jar;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * @author Cristina González
 */
public class ImportPackageManagerImpl implements ImportPackageManager {

	public Manifest cleanRepeatedImports(
			Manifest manifest, Collection<Archive<?>> auxiliaryArchives)
		throws IOException {

		List<String> auxiliaryArchivesPackages = getAuxiliaryArchivesPackages(
			auxiliaryArchives);

		Attributes mainAttributes = manifest.getMainAttributes();

		String importPackages = mainAttributes.getValue("Import-Package");

		mainAttributes.remove(new Attributes.Name("Import-Package"));

		Map<String, Set<String>> importsWithDirectivesMap =
			toImportsWithDirectivesMap(importPackages);

		List<String> resultImports = new ArrayList<>();

		for (String importValue : importsWithDirectivesMap.keySet()) {
			if (auxiliaryArchivesPackages.contains(importValue)) {
				continue;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(importValue);

			for (String directive : importsWithDirectivesMap.get(importValue)) {
				sb.append(";");
				sb.append(directive);
			}

			resultImports.add(sb.toString());
		}

		ManifestManager manifestManager = _manifestManagerInstance.get();

		manifest = manifestManager.putAttributeValue(
			manifest, "Import-Package", resultImports.toArray(
				new String[resultImports.size()]));

		return manifest;
	}

	private List<String> getAuxiliaryArchivesPackages(
			Collection<Archive<?>> auxiliaryArchives)
		throws IOException {

		List<String> packages = new ArrayList<>();

		for (Archive auxiliaryArchive : auxiliaryArchives) {
			ZipExporter zipExporter = auxiliaryArchive.as(ZipExporter.class);

			InputStream auxiliaryArchiveInputStream =
				zipExporter.exportAsInputStream();

			Jar jar = new Jar(
				auxiliaryArchive.getName(), auxiliaryArchiveInputStream);

			packages.addAll(jar.getPackages());
		}

		return packages;
	}

	private Map<String, Set<String>> toImportsWithDirectivesMap(
		String importsInManifest) {

		List<String> packageNamesWithDirectives = Arrays.asList(
			importsInManifest.split(","));

		Map<String, Set<String>> packagesNameToDirectives = new HashMap<>();

		for (String packageNameWithDirectives : packageNamesWithDirectives) {
			LinkedList<String> packageNameAndDirectives = new LinkedList<>();

			Collections.addAll(
				packageNameAndDirectives, packageNameWithDirectives.split(";"));

			String packageName = packageNameAndDirectives.pop();

			Set<String> currentDirectives = packagesNameToDirectives.get(
				packageName);

			if (currentDirectives == null) {
				currentDirectives = new HashSet<>();
			}

			currentDirectives.addAll(packageNameAndDirectives);

			packagesNameToDirectives.put(packageName, currentDirectives);
		}

		return packagesNameToDirectives;
	}

	@Inject
	private Instance<ManifestManager> _manifestManagerInstance;

}