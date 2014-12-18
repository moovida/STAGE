/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.geopapbrowser;

import java.io.File;

import org.jgrasstools.gears.io.geopaparazzi.geopap4.Image;

/**
 * Project information.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class ProjectInfo {
    File databaseFile;
    String fileName;
    String metadata;

    Image[] images;
}
