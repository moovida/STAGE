/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Script template utils.
 * 
 * @author hydrologis
 */
public class ScriptTemplatesUtil {

    private static final String INDEX_FILE_NAME = "index.txt";
    private static final String SCRIPT_TEMPLATES_FOLDER = "script-templates/";
    private static List<String> scriptNamesList;

    /**
     * @return the name of the available scripts
     */
    public static List<String> getScriptNames() {
        if (scriptNamesList == null) {
            scriptNamesList = new ArrayList<String>();
            scriptNamesList.add("");

            ArrayList<String> tmpScriptNamesList = new ArrayList<String>();
            ClassLoader classLoader = ScriptTemplatesUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(SCRIPT_TEMPLATES_FOLDER + INDEX_FILE_NAME);
            if (inputStream != null) {
                try (Scanner s = new Scanner(inputStream).useDelimiter("\n")) {
                    while( s.hasNext() ) {
                        String nextString = s.next().trim();
                        if (nextString.length() > 0)
                            tmpScriptNamesList.add(nextString);
                    }

                    Collections.sort(tmpScriptNamesList);
                    scriptNamesList.addAll(tmpScriptNamesList);
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return scriptNamesList;
    }

    /**
     * Get the content of a script by its name.
     * 
     * @param name the name.
     * @return the script content.
     */
    public static String getScriptByName( String name ) {
        ClassLoader classLoader = ScriptTemplatesUtil.class.getClassLoader();

        name = name.trim().replace(' ', '_') + ".groovy";
        InputStream inputStream = classLoader.getResourceAsStream(SCRIPT_TEMPLATES_FOLDER + name);
        if (inputStream != null) {
            try (Scanner scanner = new Scanner(inputStream)) {
                scanner.useDelimiter("\n");
                StringBuilder sb = new StringBuilder();
                while( scanner.hasNext() ) {
                    String nextString = scanner.next();
                    sb.append(nextString).append("\n");
                }
                return sb.toString();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private ScriptTemplatesUtil() {
        // prevent instantiation
    }

}
