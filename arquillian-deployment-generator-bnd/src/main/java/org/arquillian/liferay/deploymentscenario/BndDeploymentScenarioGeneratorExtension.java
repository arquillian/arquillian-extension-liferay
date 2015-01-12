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

package org.arquillian.liferay.deploymentscenario;

import org.arquillian.liferay.processor.NoOpArchiveApplicationProcessor;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Carlos Sierra Andrés
 */
public class BndDeploymentScenarioGeneratorExtension
	implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.service(
			DeploymentScenarioGenerator.class,
			BndDeploymentScenarioGenerator.class);

		if (Validate.classExists(
				"org.jboss.arquillian.container.osgi." +
					"OSGiApplicationArchiveProcessor")) {

			Class<ApplicationArchiveProcessor>
				osgiApplicationArchiveProcessorClass = null;

			try {
				osgiApplicationArchiveProcessorClass =
					(Class<ApplicationArchiveProcessor>)Class.forName(
						"org.jboss.arquillian.container.osgi." +
							"OSGiApplicationArchiveProcessor");

				builder.override(
					ApplicationArchiveProcessor.class,
					osgiApplicationArchiveProcessorClass,
					NoOpArchiveApplicationProcessor.class);
			}
			catch (ClassNotFoundException e) {
				//Ignored
			}
		}
	}

}