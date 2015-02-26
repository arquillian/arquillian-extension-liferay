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

package org.arquillian.container.liferay.remote.deploy.processor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cristina González
 */
public class BundleActivators {

	public BundleActivators() throws IOException {
		_bundleActivators = new ArrayList<>();
	}

	public BundleActivators(InputStream is) throws IOException {
		_bundleActivators = _getBundleActivatorFromInputStream(is);
	}

	public ByteArrayOutputStream getBundleActivatorAsOutputStream()
		throws IOException {

		String bundleActivatorsString = "";

		for (String bundleActivator : _bundleActivators) {
			bundleActivatorsString += bundleActivator + "\n";
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		outputStream.write(bundleActivatorsString.getBytes());

		return outputStream;
	}

	public List<String> getBundleActivators() {
		return _bundleActivators;
	}

	private List<String> _getBundleActivatorFromInputStream(InputStream is)
		throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder out = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			_bundleActivators.add(line);
		}

		reader.close();

		return _bundleActivators;
	}

	private final List<String> _bundleActivators;

}