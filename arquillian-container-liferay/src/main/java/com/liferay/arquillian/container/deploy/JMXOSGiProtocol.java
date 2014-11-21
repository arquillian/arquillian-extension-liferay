/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.arquillian.container.deploy;

import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.protocol.jmx.AbstractJMXProtocol;

/**
 * JMXOSGiProtocol
 *
 * @author thomas.diesler@jboss.com
 * @since 21-Apr-2011
 */
public class JMXOSGiProtocol extends AbstractJMXProtocol {

	@Override
	public DeploymentPackager getPackager() {
		return new OSGiDeploymentPackager();
	}

	@Override
	public String getProtocolName() {
		return "jmx-osgi";
	}

}