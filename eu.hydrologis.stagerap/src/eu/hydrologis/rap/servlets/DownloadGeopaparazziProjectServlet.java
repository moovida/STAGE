/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.hydrologis.rap.stage.utils.StageUtils;
import eu.hydrologis.rap.stage.workspace.LoginChecker;
import eu.hydrologis.rap.stage.workspace.StageWorkspace;

/**
 * The geopaparazzi project download servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class DownloadGeopaparazziProjectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        PrintWriter out = resp.getWriter();

        String[] userPwd = StageUtils.getUserPwdWithBasicAuthentication(authHeader);
        if (userPwd == null || !LoginChecker.isLoginOk(userPwd[0], userPwd[1])) {
            out.print("<b>No permission!</b>");
            out.flush();
            return;
        }

        Object projectIdObj = req.getParameter("id");
        if (projectIdObj != null) {
            String projectId = (String) projectIdObj;
            File geopaparazziFolder = StageWorkspace.getInstance().getGeopaparazziFolder(userPwd[0]);
            File newGeopaparazziFile = new File(geopaparazziFolder, projectId);

            OutputStream outSTream = resp.getOutputStream();
            FileInputStream in = new FileInputStream(newGeopaparazziFile);
            byte[] buffer = new byte[4096];
            int length;
            while( (length = in.read(buffer)) > 0 ) {
                outSTream.write(buffer, 0, length);
            }
            in.close();
            outSTream.flush();

            // byte[] data = IOUtils.toByteArray(new FileInputStream(newGeopaparazziFile));
            // DownloadService service = new DownloadService(data, newGeopaparazziFile.getName());
            // service.register();
            // resp.sendRedirect( resp.encodeRedirectURL(service.getURL()) );
        } else {
            out.print("<b>ERROR, no project id available!</b>");
            out.flush();
        }

    }
}