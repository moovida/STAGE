/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;

import eu.hydrologis.stage.geopaparazzi.geopapbrowser.GeopapBrowserEntryPoint;
import eu.hydrologis.stage.modules.StageEntryPoint;

public class StageApplication implements ApplicationConfiguration {

    public static final String ID = "eu.hydrologis.stage.application.StageApplication";

    public void configure( Application application ) {

        Map<String, String> stageProperties = new HashMap<String, String>();
        stageProperties.put(WebClient.PAGE_TITLE, "S.T.A.G.E.");
        stageProperties.put(WebClient.BODY_HTML, readTextFromResource("resources/body.html", "UTF-8"));
        stageProperties.put(WebClient.HEAD_HTML, readTextFromResource("resources/head.html", "UTF-8"));
        stageProperties.put(WebClient.FAVICON, "resources/favicon.png");

        application.addEntryPoint("/stage", StageEntryPoint.class, stageProperties);

        Map<String, String> geopapBrowserProperties = new HashMap<String, String>();
        geopapBrowserProperties.put(WebClient.PAGE_TITLE, "Geopaparazzi Browser");
        geopapBrowserProperties.put(WebClient.BODY_HTML, readTextFromResource("resources/body.html", "UTF-8"));
        geopapBrowserProperties.put(WebClient.HEAD_HTML, readTextFromResource("resources/head.html", "UTF-8"));
        geopapBrowserProperties.put(WebClient.FAVICON, "resources/favicon.png");

        application.addEntryPoint("/geopapbrowser", GeopapBrowserEntryPoint.class, geopapBrowserProperties);

        application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
        application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/theme.css");
        application.addResource("resources/favicon.png", createResourceLoader("resources/favicon.png"));
        application.addResource("resources/loading.gif", createResourceLoader("resources/loading.gif"));
    }

    private static ResourceLoader createResourceLoader( final String resourceName ) {
        return new ResourceLoader(){
            public InputStream getResourceAsStream( String resourceName ) throws IOException {
                return getClass().getClassLoader().getResourceAsStream(resourceName);
            }
        };
    }

    private static String readTextFromResource( String resourceName, String charset ) {
        String result;
        try {
            ClassLoader classLoader = StageApplication.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resourceName);
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
                StringBuilder stringBuilder = new StringBuilder();
                String line = reader.readLine();
                while( line != null ) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                    line = reader.readLine();
                }
                result = stringBuilder.toString();
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read text from resource: " + resourceName);
        }
        return result;
    }
}
