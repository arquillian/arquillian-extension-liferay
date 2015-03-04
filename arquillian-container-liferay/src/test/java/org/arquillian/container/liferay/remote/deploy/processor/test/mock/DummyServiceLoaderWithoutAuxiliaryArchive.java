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

package org.arquillian.container.liferay.remote.deploy.processor.test.mock;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * @author Cristina González
 */
public class DummyServiceLoaderWithoutAuxiliaryArchive
	implements ServiceLoader {

	@Override
	public <T> Collection<T> all(Class<T> aClass) {
		return new ArrayList<>();
	}

	@Override
	public <T> T onlyOne(Class<T> aClass) {
		return null;
	}

	@Override
	public <T> T onlyOne(Class<T> aClass, Class<? extends T> aClass1) {
		return null;
	}

}