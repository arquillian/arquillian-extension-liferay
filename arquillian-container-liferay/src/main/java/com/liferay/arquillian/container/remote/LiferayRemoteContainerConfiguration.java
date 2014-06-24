/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.arquillian.container.remote;

import org.jboss.arquillian.container.osgi.karaf.remote.KarafRemoteContainerConfiguration;
import org.jboss.arquillian.container.spi.ConfigurationException;

/**
 * @author Carlos Sierra Andrés
 */
public class LiferayRemoteContainerConfiguration extends KarafRemoteContainerConfiguration {

	public static final String DEFAULT_JMX_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:8099/jmxrmi";
	public static final String DEFAULT_JMX_USERNAME = "";
	public static final String DEFAULT_JMX_PASSWORD = "";

	@Override
	public void validate() throws ConfigurationException {
		if (jmxServiceURL == null)
			setJmxServiceURL(DEFAULT_JMX_SERVICE_URL);
		if (jmxUsername == null)
			setJmxUsername(DEFAULT_JMX_USERNAME);
		if (jmxPassword == null)
			setJmxPassword(DEFAULT_JMX_PASSWORD);
	}

}
