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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.hydrologis.rap.stage.workspace.StageWorkspaceUtilities;

/**
 * The geopaparazzi projects download servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class QueryGeopaparazziProjectsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static {
        try {
            // make sure sqlite driver are there
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
        }
    }

    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        // UISession session = RWT.getUISession();
        //
        // session.exec(new Runnable(){
        // public void run() {
        //
        // }
        // });

        Object user = req.getAttribute("user");
        Object pwd = req.getAttribute("password");
        String userString = null;
        if (user != null) {
            userString = user.toString();
        }
        if (user == null) {
            userString = "testuser";
            // throw new ServletException("Error on user!");
        }

        try {
            File[] geopaparazziProjectFiles = StageWorkspaceUtilities.getGeopaparazziProjectFiles(userString);
            List<HashMap<String, String>> projectMetadata = StageWorkspaceUtilities.readProjectMetadata(geopaparazziProjectFiles);

            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append("    {");
            sb.append("        \"status\": \"1\",");
            sb.append("        \"error\": {");
            sb.append("            \"errcode\": \"0\",");
            sb.append("            \"errortype\": \"http... sys...\",");
            sb.append("            \"errmsg\": \"\",");
            sb.append("            \"errdata\": []");
            sb.append("        }");
            sb.append("    },");
            sb.append("    {");
            sb.append("        \"projects\": [");

            for( int i = 0; i < projectMetadata.size(); i++ ) {
                HashMap<String, String> metadataMap = projectMetadata.get(i);
                long fileSize = geopaparazziProjectFiles[i].length();
                if (i > 0)
                    sb.append(",");
                sb.append("            {");
                sb.append("                \"id\": \"" + metadataMap.get("creationts") + "\",");
                sb.append("                \"title\": \"" + metadataMap.get("name") + "\",");
                sb.append("                \"date\": \"" + metadataMap.get("creationts") + "\",");
                sb.append("                \"author\": \"" + metadataMap.get("creationuser") + "\",");
                sb.append("                \"name\": \"" + metadataMap.get("name") + "\",");
                sb.append("                \"size\": \"" + fileSize + "\"");
                sb.append("            }");
            }

            sb.append("        ]");
            sb.append("    }");
            sb.append("]");
            sb.append("");

            out.print(sb.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}