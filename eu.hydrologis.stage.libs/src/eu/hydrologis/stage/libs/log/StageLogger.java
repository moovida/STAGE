/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.libs.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Platform;

/**
 * A simple logger, to be properly implemented.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageLogger {

    private static boolean LOG_INFO = true;
    private static boolean LOG_DEBUG = false;
    private static boolean LOG_ERROR = true;

    private static final String SEP = ":: ";

    private static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void logInfo( Object owner, String msg ) {
        if (LOG_INFO) {
            msg = toMessage(owner, msg);
            System.out.println(msg);
        }
    }

    public static void logDebug( Object owner, String msg ) {
        if (LOG_DEBUG) {
            msg = toMessage(owner, msg);
            System.out.println(msg);
        }
    }

    public static void logError( Object owner, String msg, Throwable e ) {
        if (LOG_ERROR) {
            msg = toMessage(owner, msg);
            System.err.println(msg);
            e.printStackTrace();
        }
    }

    private static String toMessage( Object owner, String msg ) {
        String newMsg = f.format(new Date()) + SEP;
        if (owner instanceof String) {
            newMsg = newMsg + owner + SEP;
        } else {
            newMsg = newMsg + owner.getClass().getSimpleName() + SEP;
        }
        newMsg = newMsg + msg;
        return newMsg;
    }

}
