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

import java.io.InputStream;

import java.net.URL;

import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author Cristina González
 */
@RunWith(Arquillian.class)
public class BasicFunctionalTest {

	@Deployment
	public static JavaArchive create() {
		final JavaArchive archive = ShrinkWrap.create(
			JavaArchive.class, "bundle" + UUID.randomUUID() + ".jar");

		archive.setManifest(new Asset() {
			@Override
			public InputStream openStream() {
				OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
				builder.addBundleSymbolicName("bundle");
				builder.addBundleManifestVersion(2);
				return builder.openStream();
			}
		});
		return archive;
	}

	@RunAsClient
	@Test
	public void testSignIn(@ArquillianResource URL url) {
		Assert.assertNotNull(url);

		Assert.assertNotNull(browser);

		browser.get(url.toExternalForm());

		Assert.assertNotNull(signIn);
		Assert.assertNotNull(login);
		Assert.assertNotNull(password);

		login.clear();

		login.sendKeys("test@liferay.com");

		password.sendKeys("test");

		signIn.click();

		String bodyText = browser.findElement(By.tagName("body")).getText();

		Assert.assertTrue(
			"SignIn has failed", bodyText.contains("Terms of Use"));
	}

	@Drone
	private WebDriver browser;

	@FindBy(css = "input[id$='_login']")
	private WebElement login;

	@FindBy(css = "input[id$='_password']")
	private WebElement password;

	@FindBy(css = "button[type=submit]")
	private WebElement signIn;

}