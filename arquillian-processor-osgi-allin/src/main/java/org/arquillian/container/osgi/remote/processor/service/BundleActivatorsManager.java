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

package org.arquillian.container.osgi.remote.processor.service;

import java.io.IOException;

import java.util.List;

import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Cristina González
 */
public interface BundleActivatorsManager {

	public List<String> getBundleActivators(Archive archive, String fileName)
		throws IOException;

	public void replaceBundleActivatorsFile(
			Archive archive, String fileName, List<String> bundleActivators)
		throws IOException;

}