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

package org.arquillian.container.liferay.remote.activator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.management.ManagementFactory;

import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.arquillian.protocol.jmx.JMXTestRunner.TestClassLoader;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Cristina González Castellano
 */
public class ArquillianBundleActivator implements BundleActivator {

	public void start(final BundleContext context) throws Exception {
		final TestClassLoader testClassLoader = new TestClassLoader() {

			@Override
			public Class<?> loadTestClass(String className)
				throws ClassNotFoundException {

				return context.getBundle().loadClass(className);
			}
		};

		// Register the JMXTestRunner

		MBeanServer mbeanServer = findOrCreateMBeanServer();

		testRunner = new JMXTestRunner(testClassLoader) {
			@Override
			public byte[] runTestMethod(String className, String methodName) {
				return super.runTestMethod(className, methodName);
			}
		};
		testRunner.registerMBean(mbeanServer);

		// Execute all activators

		Set<BundleActivator> bundleActivators = loadActivators();

		for (BundleActivator bundleActivator : bundleActivators) {
			bundleActivator.start(context);
		}
	}

	public void stop(BundleContext context) throws Exception {

		// Execute all activators

		Set<BundleActivator> bundleActivators = loadActivators();

		for (BundleActivator bundleActivator : bundleActivators) {
			bundleActivator.stop(context);
		}

		// Unregister the JMXTestRunner

		MBeanServer mbeanServer = findOrCreateMBeanServer();
		testRunner.unregisterMBean(mbeanServer);
	}

	private MBeanServer findOrCreateMBeanServer() {
		MBeanServer mbeanServer = null;

		ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(
			null);

		if (serverArr.size() > 1)
			log.warning("Multiple MBeanServer instances: " + serverArr);

		if (serverArr.size() > 0) {
			mbeanServer = serverArr.get(0);
			log.fine("Found MBeanServer: " + mbeanServer.getDefaultDomain());
		}

		if (mbeanServer == null) {
			log.fine("No MBeanServer, create one ...");
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
		}

		return mbeanServer;
	}

	private Set<BundleActivator> loadActivators() {
		String serviceFile =
			_SERVICES + "/" + BundleActivator.class.getCanonicalName();

		LinkedHashSet<BundleActivator> activators = new LinkedHashSet<>();

		try {
			ClassLoader classLoader = getClass().getClassLoader();

			Enumeration<URL> enumeration = classLoader.getResources(
				serviceFile);

			while (enumeration.hasMoreElements()) {
				final URL url = enumeration.nextElement();
				final InputStream is = url.openStream();
				BufferedReader reader = null;

				try {
					reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
					String line = reader.readLine();
					while (null != line) {
						line = skipCommentAndTrim(line);

						if (line.length() > 0) {
							try {
								boolean mustBeVetoed = line.startsWith("!");

								if (mustBeVetoed) {
									line = line.substring(1);
								}

								Class<? extends BundleActivator> activator =
									classLoader.loadClass(line).asSubclass(
										BundleActivator.class);

								activators.add(activator.newInstance());
							}
							catch (ClassCastException e) {
								throw new IllegalStateException(
									"Activator " + line +
										" does not implement expected type " +
										BundleActivator.class.
											getCanonicalName());
							}
						}

						line = reader.readLine();
					}
				}
				finally {
					if (reader != null) {
						reader.close();
					}
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Could not load bundle activators", e);
		}

		return activators;
	}

	private String skipCommentAndTrim(String line) {
		final int comment = line.indexOf('#');

		if (comment > -1)
		{
			line = line.substring(0, comment);
		}

		line = line.trim();
		return line;
	}

	private static final String _SERVICES = "/META-INF/services";

	// Provide logging

	private static final Logger log = Logger.getLogger(
		ArquillianBundleActivator.class.getName());

	private JMXTestRunner testRunner;

}