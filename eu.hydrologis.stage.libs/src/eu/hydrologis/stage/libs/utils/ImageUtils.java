/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.libs.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.eclipse.rap.rwt.RWT;

/**
 * Utilities for image handling.
 * 
 * @author hydrologis
 *
 */
public class ImageUtils {

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
