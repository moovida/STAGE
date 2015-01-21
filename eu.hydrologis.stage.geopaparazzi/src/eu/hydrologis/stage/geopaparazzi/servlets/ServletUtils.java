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
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilities for servlets.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ServletUtils {

    /**
     * Create a simple error page.
     * 
     * @param request
     * @param response
     * @param message optional message.
     * @param exception exception to describe.
     * @throws IOException
     */
    public static void throwError( HttpServletRequest request, HttpServletResponse response, String message, Throwable exception )
            throws IOException {
        Throwable throwable = exception;
        if (exception == null) {
            throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        }
        String servletName = (String) request.getAttribute("javax.servlet.error.servlet_name");
        if (servletName == null) {
            servletName = "Unknown";
        }
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        // Set response content type
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String title = "Error/Exception Information";
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";
        out.println(docType + "<html>\n" + "<head><title>" + title + "</title></head>\n" + "<body bgcolor=\"#f0f0f0\">\n");

        if (message != null || throwable != null)
            out.println("<h2>Error information:</h2>");

        if (message != null) {
            out.println("<b>" + message + "</b></br></br>");
        }
        if (throwable != null) {
            out.println("Servlet Name : " + servletName + "</br></br>");
            out.println("Exception Type : " + throwable.getClass().getName() + "</br></br>");
            out.println("The request URI: " + requestUri + "<br><br>");
            out.println("The exception message: " + throwable.getMessage());
        }
        out.println("</body>");
        out.println("</html>");
        out.flush();
        out.close();
    }

}
