/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.libs.utils;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.geotools.data.Base64;

/**
 * Utilities.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageUtils {

    /**
     * Supported file formats in read and write mode.
     */
    public static String[] EXTENTIONS_READ_WRITE = {"asc", "tiff", "tif", "shp"};

    /**
     * Supported vector formats in read and write mode.
     */
    public static String[] VECTOR_EXTENTIONS_READ_WRITE = {"shp"};
    
    /**
     * Supported raster formats in read and write mode.
     */
    public static String[] RASTER_EXTENTIONS_READ_WRITE = {"asc", "tiff", "tif"};
    
    /**
     * General textcolor.
     */
    public static final String TEXTCOLOR = "#505050";

    public static String escapeHTML( String s ) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String[] getUserPwdWithBasicAuthentication( String authHeader ) {
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    String credentials;
                    try {
                        credentials = new String(Base64.decode(st.nextToken()), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                    int p = credentials.indexOf(":");
                    if (p != -1) {
                        String login = credentials.substring(0, p).trim();
                        String password = credentials.substring(p + 1).trim();

                        return new String[]{login, password};
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
