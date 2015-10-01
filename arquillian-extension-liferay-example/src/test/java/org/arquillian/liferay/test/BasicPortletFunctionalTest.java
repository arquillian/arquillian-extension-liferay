/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 * <p/>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package org.arquillian.liferay.test;

import com.liferay.portal.kernel.exception.PortalException;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import org.arquillian.container.liferay.remote.enricher.Inject;
import org.arquillian.liferay.installportlet.annotation.InstallPortlet;
import org.arquillian.liferay.sample.service.SampleService;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.osgi.api.BndProjectBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * @author Cristina González
 */
@RunAsClient
@RunWith(Arquillian.class)
public class BasicPortletFunctionalTest {

	@Deployment
	public static JavaArchive create() {
		BndProjectBuilder bndProjectBuilder = ShrinkWrap.create(
			BndProjectBuilder.class);

		bndProjectBuilder.setBndFile(new File("bnd-basic-portlet-test.bnd"));

		bndProjectBuilder.generateManifest(true);

		return bndProjectBuilder.as(JavaArchive.class);
	}

	@Test
	public void testInstallPortlet() throws IOException, PortalException {
		browser.get(_portlerURL.toExternalForm());

		String bodyText = browser.findElement(By.tagName("body")).getText();

		Assert.assertTrue(
			"The portlet is not well deployed",
			bodyText.contains("Sample Portlet is working!"));
	}

	@InstallPortlet(name ="arquillian_sample_portlet")
	private URL _portlerURL;

	@Inject
	private SampleService _sampleService;

	@Drone
	private WebDriver browser;

}