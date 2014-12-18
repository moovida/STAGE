/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.geopapbrowser.functions;

import java.io.File;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

import eu.hydrologis.rap.stage.utilsrap.ImageDialog;

public class OpenImageFunction extends BrowserFunction {
    private Browser browser;
    private File databaseFile;
    public OpenImageFunction( Browser browser, String name, File databaseFile ) {
        super(browser, name);
        this.browser = browser;
        this.databaseFile = databaseFile;
    }
    @Override
    public Object function( Object[] arguments ) {

        String imageId = arguments[0].toString();
        String imageName = arguments[1].toString();

        long id = (long) Double.parseDouble(imageId);
        ImageDialog imageDialog = new ImageDialog(browser.getShell(), "Image: " + imageName, databaseFile, id, imageName);
        imageDialog.open();

        // String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new
        // Date(image.getTs()));
        // String pic = "L.marker([" + image.getLat() + ", " + image.getLon() //
        // + "], {icon: photoIcon}).addTo(map).bindPopup(\"" //
        // + "<b>Image:</b> " + image.getName() + "<br/>" //
        // + "<b>Timestamp:</b> " + dateTimeString + "<br/>" //
        // + "<b>Azimuth:</b> " + (int) image.getAzim() + " deg<br/>" //
        // + "<b>Altim:</b> " + (int) image.getAltim() + " m<br/>" //
        // + "\");";
        // MessageDialog.openInformation(browser.getShell(), "Image", "Image shown" + imageId);
        return null;
    }
}