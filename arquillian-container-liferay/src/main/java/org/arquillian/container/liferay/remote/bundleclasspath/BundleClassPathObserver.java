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

package org.arquillian.container.liferay.remote.bundleclasspath;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * @author Cristina González
 */
public class BundleClassPathObserver {

	public void afterClass(@Observes EventContext<AfterClass> event)
		throws Exception {

		event.proceed();

		currentThread.setContextClassLoader(ctxLoader);
	}

	public void beforeClass(@Observes EventContext<BeforeClass> event)
		throws Exception {

		currentThread = Thread.currentThread();

		ctxLoader = currentThread.getContextClassLoader();

		BeforeClass beforeClassEvent = event.getEvent();

		TestClass testClass = beforeClassEvent.getTestClass();

		Class<?> javaTestClass = testClass.getJavaClass();

		currentThread.setContextClassLoader(javaTestClass.getClassLoader());

		event.proceed();
	}

	private ClassLoader ctxLoader;
	private Thread currentThread = Thread.currentThread();

}