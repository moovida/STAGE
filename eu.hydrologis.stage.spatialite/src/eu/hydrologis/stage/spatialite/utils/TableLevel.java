/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.spatialite.utils;

import java.util.ArrayList;
import java.util.List;

public class TableLevel {
    public DbLevel parent;
    public String tableName;
    public boolean isGeo = false;

    public List<ColumnLevel> columnsList = new ArrayList<ColumnLevel>();
}
