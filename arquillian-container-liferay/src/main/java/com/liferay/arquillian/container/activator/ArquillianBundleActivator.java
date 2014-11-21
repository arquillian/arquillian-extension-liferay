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

package com.liferay.arquillian.container.activator;

import java.lang.management.ManagementFactory;

import java.util.ArrayList;
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
	}

	public void stop(BundleContext context) throws Exception {

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

	// Provide logging

	private static final Logger log = Logger.getLogger(
		ArquillianBundleActivator.class.getName());

	private JMXTestRunner testRunner;

}