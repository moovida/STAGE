/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.geopaparazzi.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The geopaparazzi project download servlet redirector.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class DownloadGeopaparazziProjectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse response ) throws ServletException, IOException {
        Object projectIdObj = req.getParameter("id");
        if (projectIdObj != null) {
            String newUrl = "geopapbrowser?servicehandler=stage_gpproject_download&id=" + projectIdObj.toString();
            response.sendRedirect(response.encodeRedirectURL(newUrl));
        } else {
            ServletUtils.throwError(req, response, "No project id provided.", null);
        }

    }

}