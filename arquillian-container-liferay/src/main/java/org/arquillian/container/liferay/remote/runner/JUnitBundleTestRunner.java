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

package org.arquillian.container.liferay.remote.runner;

import org.jboss.arquillian.junit.container.JUnitTestRunner;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * @author Cristina González Castellano
 */
public class JUnitBundleTestRunner extends JUnitTestRunner {

	@Override
	public TestResult execute(Class<?> testClass, String methodName) {
		Thread currentThread = Thread.currentThread();

		ClassLoader ctxLoader = currentThread.getContextClassLoader();

		try {
			//Make sure we run in the context of the arquillian-bundle

			// class loader

			currentThread.setContextClassLoader(getClass().getClassLoader());
			return super.execute(testClass, methodName);
		}
		finally {
			currentThread.setContextClassLoader(ctxLoader);
		}
	}

}