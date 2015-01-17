/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.stage.workspace;

import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_METADATA;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.MetadataTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TimeUtilities;

/**
 * Workspace utils.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class StageWorkspaceUtilities {

    public static File[] getGeopaparazziProjectFiles( String user ) {
        if (user == null) {
            user = User.getCurrentUserName();
        }
        File geopaparazziFolder = StageWorkspace.getInstance().getGeopaparazziFolder(user);
        File[] projectFiles = geopaparazziFolder.listFiles(new FilenameFilter(){
            @Override
            public boolean accept( File dir, String name ) {
                return name.endsWith(".gpap");
            }
        });
        Arrays.sort(projectFiles, Collections.reverseOrder());
        return projectFiles;
    }

    public static List<HashMap<String, String>> readProjectMetadata( File[] projectFiles ) throws Exception {
        List<HashMap<String, String>> infoList = new ArrayList<HashMap<String, String>>();
        for( File geopapDatabaseFile : projectFiles ) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + geopapDatabaseFile.getAbsolutePath())) {
                HashMap<String, String> projectInfo = getProjectMetadata(connection);
                infoList.add(projectInfo);
            }
        }
        return infoList;
    }

    private static HashMap<String, String> getProjectMetadata( Connection connection ) throws Exception {
        HashMap<String, String> infoMap = new HashMap<String, String>();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            String sql = "select " + MetadataTableFields.COLUMN_KEY.getFieldName() + ", " + //
                    MetadataTableFields.COLUMN_VALUE.getFieldName() + " from " + TABLE_METADATA;

            ResultSet rs = statement.executeQuery(sql);
            while( rs.next() ) {
                String key = rs.getString(MetadataTableFields.COLUMN_KEY.getFieldName());
                String value = rs.getString(MetadataTableFields.COLUMN_VALUE.getFieldName());

                if (!key.endsWith("ts")) {
                    infoMap.put(key, value);
                } else {
                    try {
                        long ts = Long.parseLong(value);
                        String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(ts));
                        infoMap.put(key, dateTimeString);
                    } catch (Exception e) {
                        infoMap.put(key, value);
                    }
                }
            }

        }
        return infoMap;
    }

}
