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

package org.arquillian.liferay.installportlet.activator;

import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.GroupLocalService;
import com.liferay.portal.service.LayoutLocalService;
import com.liferay.portal.service.PortletPreferencesLocalService;
import com.liferay.portal.service.UserLocalService;

import java.util.Dictionary;

import javax.servlet.Servlet;

import org.arquillian.liferay.installportlet.servlet.InstallPortletServlet;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * @author Cristina González Castellano
 */
public class InstallPortletBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		InstallPortletServlet installPortletServlet =
			new InstallPortletServlet();

		_companyLocalServiceServiceReference = context.getServiceReference(
			CompanyLocalService.class);

		if (_companyLocalServiceServiceReference != null) {
			installPortletServlet.setCompanyLocalService(
				context.getService(_companyLocalServiceServiceReference));
		}

		_groupLocalServiceServiceReference = context.getServiceReference(
			GroupLocalService.class);

		if (_groupLocalServiceServiceReference != null) {
			installPortletServlet.setGroupLocalService(
				context.getService(_groupLocalServiceServiceReference));
		}

		_layoutLocalServiceServiceReference = context.getServiceReference(
			LayoutLocalService.class);

		if (_layoutLocalServiceServiceReference != null) {
			installPortletServlet.setLayoutLocalService(
				context.getService(_layoutLocalServiceServiceReference));
		}

		_portletPreferencesLocalServiceServiceReference =
			context.getServiceReference(
				PortletPreferencesLocalService.class);

		if (_portletPreferencesLocalServiceServiceReference != null) {
			installPortletServlet.setPortletPreferencesLocalService(
				context.getService(
					_portletPreferencesLocalServiceServiceReference));
		}

		_userLocalServiceServiceReference = context.getServiceReference(
			UserLocalService.class);

		if (_userLocalServiceServiceReference != null) {
			installPortletServlet.setUserLocalService(
				context.getService(_userLocalServiceServiceReference));
		}

		Dictionary<String, Object> properties = new HashMapDictionary<>();

		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME,
			"Install Portlet Servlet");

		properties.put(
			HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN,
			"/install-portlet-servlet/*");

		_servletServiceRegistration = context.registerService(
			Servlet.class, installPortletServlet, properties);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		context.ungetService(_companyLocalServiceServiceReference);
		context.ungetService(_groupLocalServiceServiceReference);
		context.ungetService(_layoutLocalServiceServiceReference);
		context.ungetService(_portletPreferencesLocalServiceServiceReference);
		context.ungetService(_userLocalServiceServiceReference);

		_servletServiceRegistration.unregister();
	}

	private ServiceReference<CompanyLocalService>
		_companyLocalServiceServiceReference;
	private ServiceReference<GroupLocalService>
		_groupLocalServiceServiceReference;
	private ServiceReference<LayoutLocalService>
		_layoutLocalServiceServiceReference;
	private ServiceReference<PortletPreferencesLocalService>
		_portletPreferencesLocalServiceServiceReference;
	private ServiceReference<UserLocalService>
		_userLocalServiceServiceReference;

	private ServiceRegistration<Servlet> _servletServiceRegistration;

}