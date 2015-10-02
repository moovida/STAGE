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
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ServiceHandler;

import eu.hydrologis.stage.libs.log.StageLogger;

public class StageApplication implements ApplicationConfiguration {

    public static final String ID = "eu.hydrologis.stage.application.StageApplication";

    private static final String ID_SERVICE_HANDLER = "org.eclipse.rap.ui.serviceHandler";

    @SuppressWarnings("unchecked")
    public void configure( Application application ) {

        Map<String, String> stageProperties = new HashMap<String, String>();
        stageProperties.put(WebClient.PAGE_TITLE, "S.T.A.G.E.");
        stageProperties.put(WebClient.BODY_HTML, readTextFromResource("resources/body.html", "UTF-8"));
        stageProperties.put(WebClient.HEAD_HTML, readTextFromResource("resources/head.html", "UTF-8"));
        stageProperties.put(WebClient.FAVICON, "resources/favicon.png");
        application.addEntryPoint("/", StageEntryPoint.class, stageProperties);

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("eu.hydrologis.stage.entrypoint");

        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            TreeMap<String, IConfigurationElement> elementsToKeep = new TreeMap<>();
            for( IExtension extension : extensions ) {
                IConfigurationElement[] configurationElements = extension.getConfigurationElements();
                for( IConfigurationElement element : configurationElements ) {
                    try {
                        String title = element.getAttribute("title");
                        elementsToKeep.put(title, element);
                    } catch (Exception e) {
                        StageLogger.logError(this, e);
                    }
                }
            }
            for( Entry<String, IConfigurationElement> entry : elementsToKeep.entrySet() ) {
                String title = entry.getKey();
                IConfigurationElement element = entry.getValue();
                String clazz = element.getAttribute("class");
                // String id = element.getAttribute("id");
                String path = element.getAttribute("path");

                if (clazz != null && title != null && path != null && clazz.length() > 0 && title.length() > 0
                        && path.length() > 0) {
                    try {
                        Class< ? extends AbstractEntryPoint> entryPointClass = (Class< ? extends AbstractEntryPoint>) Class
                                .forName(clazz);

                        Map<String, String> jgrasstoolsProperties = new HashMap<String, String>();
                        jgrasstoolsProperties.put(WebClient.PAGE_TITLE, title);

                        // TODO add custom html and icon part
                        jgrasstoolsProperties.put(WebClient.BODY_HTML,
                                readTextFromResource("resources/body_loading.html", "UTF-8"));
                        jgrasstoolsProperties.put(WebClient.HEAD_HTML, readTextFromResource("resources/head.html", "UTF-8"));
                        jgrasstoolsProperties.put(WebClient.FAVICON, "resources/favicon.png");

                        application.addEntryPoint(path, entryPointClass, jgrasstoolsProperties);
                    } catch (Exception e) {
                        StageLogger.logError(this, e);
                    }
                }
            }
        }

        application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
        application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/theme.css");
        application.addResource("resources/favicon.png", createResourceLoader("resources/favicon.png"));
        application.addResource("resources/loading.gif", createResourceLoader("resources/loading.gif"));

        registerCustomServiceHandlers(application);

    }

    private void registerCustomServiceHandlers( Application application ) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(ID_SERVICE_HANDLER);
        IConfigurationElement[] elements = point.getConfigurationElements();
        for( int i = 0; i < elements.length; i++ ) {
            try {
                String id = elements[i].getAttribute("id");
                if (id != null) {
                    Object extObject = elements[i].createExecutableExtension("class");
                    ServiceHandler handler = (ServiceHandler) extObject;
                    application.addServiceHandler(id, handler);
                }
            } catch (final CoreException ce) {
                ce.printStackTrace();
            }

        }

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
