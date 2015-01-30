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

package org.arquillian.container.liferay.remote;

import org.jboss.arquillian.container.osgi.karaf.remote.KarafRemoteContainerConfiguration;
import org.jboss.arquillian.container.spi.ConfigurationException;

/**
 * @author Carlos Sierra Andrés
 */
public class LiferayRemoteContainerConfiguration extends KarafRemoteContainerConfiguration {

	public static final String DEFAULT_JMX_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:8099/jmxrmi";
	public static final String DEFAULT_JMX_USERNAME = "";
	public static final String DEFAULT_JMX_PASSWORD = "";

    public static final String DEFAULT_HTTP_HOST = "localhost";
    public static final int DEFAULT_HTTP_PORT = 8080;

    private String httpHost;
    private Integer httpPort;

    @Override
    public boolean isAutostartBundle() {
        return true;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    @Override
	public void validate() throws ConfigurationException {
        if (httpHost == null)
            setHttpHost(DEFAULT_HTTP_HOST);
        if (httpPort == null)
            setHttpPort(DEFAULT_HTTP_PORT);
		if (jmxServiceURL == null)
			setJmxServiceURL(DEFAULT_JMX_SERVICE_URL);
		if (jmxUsername == null)
			setJmxUsername(DEFAULT_JMX_USERNAME);
		if (jmxPassword == null)
			setJmxPassword(DEFAULT_JMX_PASSWORD);
	}

}
