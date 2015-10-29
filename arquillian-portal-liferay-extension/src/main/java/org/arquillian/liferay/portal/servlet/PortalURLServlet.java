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

package org.arquillian.liferay.portal.servlet;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.LayoutTypePortlet;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.GroupLocalService;
import com.liferay.portal.service.LayoutLocalService;
import com.liferay.portal.service.PortletPreferencesLocalService;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalService;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.portlet.PortletPreferences;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Cristina González
 */
public class PortalURLServlet extends HttpServlet {

	public void destroy() {
		if (_layouts != null) {
			for (Layout layout : _layouts) {
				try {
					_layoutLocalService.deleteLayout(
						layout.getPlid(), new ServiceContext());
				}
				catch (PortalException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		String portletId = request.getParameter("portlet-id");

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.println("<h1> Portlet ID: " + portletId + "</h1>");

		Company company = _companyLocalService.getCompanies().get(0);

		Group guestGroup = null;

		if (_layouts == null) {
			_layouts = new ArrayList<>();
		}

		try {
			guestGroup = _groupLocalService.getGroup(
				company.getCompanyId(), "Guest");

			User defaultUser = _userLocalService.getDefaultUser(
				company.getCompanyId());

			UUID uuid = UUID.randomUUID();

			_layout = _layoutLocalService.addLayout(
				defaultUser.getUserId(), guestGroup.getGroupId(), false, 0,
				uuid.toString(), null, null, "portlet", false,
				"/"+uuid.toString(), new ServiceContext());

			_layouts.add(_layout);

			LayoutTypePortlet layoutTypePortlet =
				(LayoutTypePortlet) _layout.getLayoutType();

			layoutTypePortlet.setLayoutTemplateId(
				defaultUser.getUserId(), "1_column");

			String portletIdAdded = layoutTypePortlet.addPortletId(
				defaultUser.getUserId(), portletId, false);

			long ownerId = 0;
			int ownerType = 3;

			PortletPreferences prefs =
				_portletPreferencesLocalService.getPreferences(
					company.getCompanyId(), ownerId, ownerType,
					_layout.getPlid(), portletIdAdded);

			_portletPreferencesLocalService.updatePreferences(
				ownerId, ownerType, _layout.getPlid(), portletIdAdded, prefs);

			_layoutLocalService.updateLayout(
				_layout.getGroupId(), _layout.isPrivateLayout(),
				_layout.getLayoutId(), _layout.getTypeSettings());

			response.sendRedirect("/"+uuid.toString());
		}
		catch (PortalException e) {
			e.printStackTrace(out);
		}
	}

	@Override
	public void init() throws ServletException {
		//There are not init actions for this server
	}

	public void setCompanyLocalService(
		CompanyLocalService companyLocalService) {

		_companyLocalService = companyLocalService;
	}

	public void setGroupLocalService(GroupLocalService groupLocalService) {
		_groupLocalService = groupLocalService;
	}

	public void setLayoutLocalService(LayoutLocalService layoutLocalService) {
		_layoutLocalService = layoutLocalService;
	}

	public void setPortletPreferencesLocalService(
		PortletPreferencesLocalService porletPreferencesLocalService) {

		_portletPreferencesLocalService = porletPreferencesLocalService;
	}

	public void setUserLocalService(UserLocalService userLocalService) {
		_userLocalService = userLocalService;
	}

	private CompanyLocalService _companyLocalService;
	private GroupLocalService _groupLocalService;
	private Layout _layout;
	private LayoutLocalService _layoutLocalService;
	private List<Layout> _layouts;
	private PortletPreferencesLocalService _portletPreferencesLocalService;
	private UserLocalService _userLocalService;

}