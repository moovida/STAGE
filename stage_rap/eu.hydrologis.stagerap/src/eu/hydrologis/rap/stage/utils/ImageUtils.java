/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.stage.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.imageio.ImageIO;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.browser.Browser;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoImages;

/**
 * Utilities for image handling.
 * 
 * @author hydrologis
 *
 */
public class ImageUtils {

    /**
     * Set an image as browser content.
     * 
     * @param browser the browser to use.
     * @param imageId the id of the image to show.
     * @param imageName the name of the image to show.
     * @param dbFile the db to use.
     * @param IMAGE_KEY the image key.
     * @param SERVICE_HANDLER the service handler id.
     * @throws Exception
     */
    public static void setImageInBrowser( Browser browser, long imageId, String imageName, File dbFile, String IMAGE_KEY,
            String SERVICE_HANDLER ) throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
            byte[] imageData = DaoImages.getImageData(connection, imageId);
            InputStream imageStream = null;
            try {
                imageStream = new ByteArrayInputStream(imageData);
                BufferedImage bufferedImage = createImage(imageStream);
                // store the image in the UISession for the service handler
                RWT.getUISession().setAttribute(IMAGE_KEY, bufferedImage);
                // create the HTML with a single <img> tag.
                browser.setText(createHtml(IMAGE_KEY, SERVICE_HANDLER));
                // newImageFile.delete();
            } catch (Exception e) {
                // File newImageFile = File.createTempFile("stage" + new Date().getTime(),
                // imageName);
                // try (OutputStream outStream = new FileOutputStream(newImageFile)) {
                // outStream.write(imageData);
                // }
                e.printStackTrace();
            }

        }
    }
    private static BufferedImage createImage( InputStream inputStream ) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int width;
        int height;
        if (imageWidth > imageHeight) {
            width = 800;
            height = imageHeight * width / imageWidth;
        } else {
            height = 800;
            width = height * imageWidth / imageHeight;
        }

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
    }

    private static String createHtml( String key, String SERVICE_HANDLER ) {
        StringBuffer html = new StringBuffer();
        html.append("<div id=\"wrapper\" style=\"width:100%; text-align:center\">\n");
        html.append("<img src=\"");
        html.append(createImageUrl(key, SERVICE_HANDLER));
        html.append("\"/>");
        html.append("</div>");
        return html.toString();
    }

    private static String createImageUrl( String key, String SERVICE_HANDLER ) {
        StringBuffer url = new StringBuffer();
        url.append(RWT.getServiceManager().getServiceHandlerUrl(SERVICE_HANDLER));
        url.append("&imageId=");
        url.append(key);
        url.append("&nocache=");
        url.append(System.currentTimeMillis());
        return url.toString();
    }
}
