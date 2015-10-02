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

		ServiceReference<CompanyLocalService>
			companyLocalServiceServiceReference = context.getServiceReference(
				CompanyLocalService.class);

		if (companyLocalServiceServiceReference != null) {
			installPortletServlet.setCompanyLocalService(
				context.getService(companyLocalServiceServiceReference));
		}

		ServiceReference<GroupLocalService> groupLocalServiceServiceReference =
			context.getServiceReference(GroupLocalService.class);

		if (groupLocalServiceServiceReference != null) {
			installPortletServlet.setGroupLocalService(
				context.getService(groupLocalServiceServiceReference));
		}

		ServiceReference<LayoutLocalService>
			layoutLocalServiceServiceReference = context.getServiceReference(
				LayoutLocalService.class);

		if (layoutLocalServiceServiceReference != null) {
			installPortletServlet.setLayoutLocalService(
				context.getService(layoutLocalServiceServiceReference));
		}

		ServiceReference<PortletPreferencesLocalService>
			portletPreferencesLocalServiceServiceReference =
				context.getServiceReference(
					PortletPreferencesLocalService.class);

		if (portletPreferencesLocalServiceServiceReference != null) {
			installPortletServlet.setPortletPreferencesLocalService(
				context.getService(
					portletPreferencesLocalServiceServiceReference));
		}

		ServiceReference<UserLocalService> userLocalServiceServiceReference =
			context.getServiceReference(UserLocalService.class);

		if (userLocalServiceServiceReference != null) {
			installPortletServlet.setUserLocalService(
				context.getService(userLocalServiceServiceReference));
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
		context.ungetService(_servletServiceRegistration.getReference());
	}

	private ServiceRegistration<Servlet> _servletServiceRegistration;

}