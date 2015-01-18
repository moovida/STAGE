/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.libs.log;

/**
 * A simple logger, to be properly implemented.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageLogger {

    private static boolean LOG_INFO = true;
    private static boolean LOG_DEBUG = false;
    private static boolean LOG_ERROR = true;

    public static void logInfo( String msg ) {
        if (LOG_INFO)
            System.out.println(msg);
    }

    public static void logDebug( String msg ) {
        if (LOG_DEBUG)
            System.out.println(msg);
    }

    public static void logError( String msg, Throwable e ) {
        if (LOG_ERROR) {
            System.err.println(msg);
            e.printStackTrace();
        }
    }

}
