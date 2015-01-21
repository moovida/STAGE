/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.geopaparazzi.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.workspace.LoginChecker;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * Geopaparazzi projects upload servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class UploadGeopaparazziProjectServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        String[] userPwd = StageUtils.getUserPwdWithBasicAuthentication(authHeader);
        if (userPwd == null || !LoginChecker.isLoginOk(userPwd[0], userPwd[1])) {
            throw new ServletException("No permission!");
        }

        File geopaparazziFolder = StageWorkspace.getInstance().getGeopaparazziFolder(userPwd[0]);

        Object projectNameObj = request.getParameter("name");
        if (projectNameObj == null) {
            throw new ServletException("No project name provided.");
        }

        ServletInputStream inputStream = request.getInputStream();

        try (FileOutputStream out = new FileOutputStream(new File(geopaparazziFolder, projectNameObj.toString()))) {
            int read = 0;
            final byte[] bytes = new byte[1024];
            while( (read = inputStream.read(bytes)) != -1 ) {
                out.write(bytes, 0, read);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

}
