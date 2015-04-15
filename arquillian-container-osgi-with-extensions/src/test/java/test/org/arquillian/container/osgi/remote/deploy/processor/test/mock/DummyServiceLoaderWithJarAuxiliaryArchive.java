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

package org.arquillian.container.osgi.remote.deploy.processor.test.mock;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Cristina González
 */
public class DummyServiceLoaderWithJarAuxiliaryArchive
	implements ServiceLoader {

	@Override
	public <T> Collection<T> all(Class<T> aClass) {
		ArrayList<DummyAuxiliaryArchiveAppender> dummyAuxiliaryArchives =
			new ArrayList<>();

		dummyAuxiliaryArchives.add(new DummyAuxiliaryArchiveAppender());

		return (Collection<T>)dummyAuxiliaryArchives;
	}

	@Override
	public <T> T onlyOne(Class<T> aClass) {
		if (aClass.getClass().isInstance(DummyAuxiliaryArchiveAppender.class)) {
			return (T)new DummyAuxiliaryArchiveAppender();
		}

		return null;
	}

	@Override
	public <T> T onlyOne(Class<T> aClass, Class<? extends T> aClass1) {
		if (aClass.getClass().isInstance(DummyAuxiliaryArchiveAppender.class)) {
			return (T)new DummyAuxiliaryArchiveAppender();
		}

		return null;
	}

	private class DummyAuxiliaryArchiveAppender
		implements AuxiliaryArchiveAppender {

		@Override
		public Archive<?> createAuxiliaryArchive() {
			JavaArchive archive = ShrinkWrap.create(
				JavaArchive.class, "dummy-jar.jar");

			archive.addPackage(
				DummyServiceLoaderWithJarAuxiliaryArchive.class.getPackage());

			return archive;
		}

	}

}