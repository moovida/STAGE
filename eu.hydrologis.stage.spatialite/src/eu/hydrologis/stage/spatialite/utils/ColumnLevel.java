/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.spatialite.utils;

import org.jgrasstools.gears.spatialite.ForeignKey;
import org.jgrasstools.gears.spatialite.SpatialiteGeometryColumns;

public class ColumnLevel {
    public TableLevel parent;

    public String columnName;
    public String columnType;
    public boolean isPK = false;
    public SpatialiteGeometryColumns geomColumn;

    /**
     * if not null, it describes the table(colname) it references as foreign key.
     */
    public String references;

    public String[] tableColsFromFK() {
        String tmpReferences = references.replaceFirst("->", "").trim();
        String[] split = tmpReferences.split("\\(|\\)");
        String refTable = split[0];
        String refColumn = split[1];
        return new String[]{refTable, refColumn};
    }

    public void setFkReferences( ForeignKey fKey ) {
        references = " -> " + fKey.table + "(" + fKey.to + ")";
    }

}
