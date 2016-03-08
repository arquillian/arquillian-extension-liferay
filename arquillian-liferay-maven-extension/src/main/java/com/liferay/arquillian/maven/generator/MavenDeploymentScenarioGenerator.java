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

package com.liferay.arquillian.maven.generator;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.PomlessMavenImporter;
import org.jboss.shrinkwrap.resolver.impl.maven.util.Validate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public class MavenDeploymentScenarioGenerator
	extends AnnotationDeploymentScenarioGenerator {

	/**
	 * (non-Javadoc)
	 * @see org.jboss.arquillian.container.test.impl.client.deployment.
	 * AnnotationDeploymentScenarioGenerator
	 * #generate(org.jboss.arquillian.test.spi.TestClass)
	 */
	@Override
	public List<DeploymentDescription> generate(TestClass testClass) {
		containerDef();

		List<DeploymentDescription> descriptions = super.generate(testClass);

		if (descriptions == null) {
			descriptions = new ArrayList<>();
		}

		log.info("Generating Deployment for Liferay Plugin ");

		DeploymentDescription deploymentDecription =
			createLiferayPluginDeployment();

		if (deploymentDecription != null) {
			descriptions.add(deploymentDecription);
		}

		return descriptions;
	}

	private ContainerDef containerDef() {
		List<ContainerDef> containers = descriptor.get().getContainers();

		ContainerDef defaultContainer = null;

		for (int i = 0; i < containers.size(); i++) {
			defaultContainer = containers.get(i);

			if (defaultContainer.isDefault()) {
				String containerName = defaultContainer.getContainerName();
				Validate.notNullOrEmpty(
					containerName,
					"At least one default container must be defined in " +
						"arquillian.xml");

				if (containerName.contains("tomcat")) {
					System.setProperty("appServerType", "tomcat");
				}
				else if (containerName.contains("jboss")) {
					System.setProperty("appServerType", "jboss");
				}

				return defaultContainer;
			}
		}

		return null;
	}

	private DeploymentDescription createLiferayPluginDeployment() {
		log.debug("Building Liferay Plugin from project pom.xml");

		File pomFile = new File("pom.xml");

		if ((pomFile != null) && pomFile.exists()) {
			log.debug(
				"Loading project from pom file:" + pomFile.getAbsolutePath());

			String globalSettings = System.getProperty(
				"maven.execution.global-settings");

			String userSettings = System.getProperty(
				"maven.execution.user-settings");

			String profilesString = System.getProperty(
				"maven.execution.active-profiles");

			PomlessMavenImporter mavenImporter = ShrinkWrap.create(
				MavenImporter.class);

			if (globalSettings != null) {
				mavenImporter =
					((MavenImporter)mavenImporter).configureFromFile(
						globalSettings);
			}

			if (userSettings != null) {
				mavenImporter =
					((MavenImporter)mavenImporter).configureFromFile(
						userSettings);
			}

			String[] profiles = new String[0];

			if (profilesString != null) {
				profiles = profilesString.split(",");
			}

			WebArchive archive = mavenImporter.loadPomFromFile(
				pomFile, profiles).importBuildOutput().as(WebArchive.class);

			DeploymentDescription deploymentDescription =
				new DeploymentDescription("_DEFAULT", archive);

			deploymentDescription.shouldBeTestable(true);

			return deploymentDescription;
		}

		return null;
	}

	private static final Logger log = LoggerFactory.getLogger(
		MavenDeploymentScenarioGenerator.class);

	@Inject
	private Instance<ArquillianDescriptor> descriptor;

}