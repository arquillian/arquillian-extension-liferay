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

package org.arquillian.container.liferay.remote;

import com.liferay.arquillian.container.deploy.JMXOSGiProtocol;
import com.liferay.arquillian.container.enricher.LiferayEnricherAuxiliaryAppender;
import com.liferay.arquillian.container.wait.LiferayWaitForServiceAuxiliaryAppender;

import org.jboss.arquillian.container.osgi.karaf.remote.KarafRemoteDeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Carlos Sierra Andrés
 */
public class LiferayRemoteContainerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.override(
			DeployableContainer.class, KarafRemoteDeployableContainer.class,
			LiferayRemoteDeployableContainer.class);

		builder.override(
			Protocol.class,
			org.jboss.arquillian.protocol.osgi.JMXOSGiProtocol.class,
			JMXOSGiProtocol.class);

		builder.service(
			AuxiliaryArchiveAppender.class,
			LiferayEnricherAuxiliaryAppender.class);

		builder.service(
			AuxiliaryArchiveAppender.class,
			LiferayWaitForServiceAuxiliaryAppender.class);
	}

}
