/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.treesslicer;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public class GetSelectedPlotFilesFunction extends BrowserFunction {
    public GetSelectedPlotFilesFunction( Browser browser ) {
        super(browser, "getPlotFiles");
    }
    @Override
    public Object function( Object[] arguments ) {
        return new String[]{"plot1.json", "plot2.json"};
    }
}