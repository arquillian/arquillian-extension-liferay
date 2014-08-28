/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.arquillian.container.remote;

import org.jboss.arquillian.container.osgi.karaf.remote.KarafRemoteDeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

import java.io.IOException;
import java.util.jar.Manifest;

/**
 * @author Carlos Sierra Andrés
 */
public class LiferayRemoteDeployableContainer<T extends LiferayRemoteContainerConfiguration>
	extends KarafRemoteDeployableContainer<T> {

    LiferayRemoteContainerConfiguration config;

	@Override
	public Class<T> getConfigurationClass() {
		@SuppressWarnings("uncheked")
		Class<T> clazz = (Class<T>) LiferayRemoteContainerConfiguration.class;
		return clazz;
	}

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        ProtocolMetaData protocolMetaData = super.deploy(archive);

        protocolMetaData.addContext(new HTTPContext(config.getHttpHost(), config.getHttpPort()));

        Node node = archive.get("META-INF/MANIFEST.MF");

        try {
            Manifest manifest = new Manifest(node.getAsset().openStream());

            String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            String version = manifest.getMainAttributes().getValue("Bundle-Version");

            startBundle(symbolicName, version);
        } catch (IOException e) {
            throw new DeploymentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
        }

        return protocolMetaData;
    }

    @Override
    public void setup(T config) {
        this.config = config;

        super.setup(config);
    }
}