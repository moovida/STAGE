/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.libs.workspace;

/**
 * A dummy login check for test purposes.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class LoginChecker {

    public static final String TESTUSER = "testuser";

    private LoginChecker() {
    }

    /**
     * Checks user and pwd.
     * 
     * @param user
     * @param pwd
     * @return <code>true</code>, if login is ok.
     */
    public static boolean isLoginOk( String user, String pwd ) {
        if (user.equals(TESTUSER) && pwd.equals("t")) {
            return true;
        }
        return false;
    }
}
