/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.libs.workspace;

import java.io.File;

/**
 * The main workspace class.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class StageWorkspace {
    /**
     * Name of the default geopaparazzi project folder.
     */
    public static final String GEOPAPARAZZI_FOLDERNAME = "geopaparazzi";
    /**
     * Name of the default data folder.
     */
    public static final String DATA_FOLDERNAME = "data";
    /**
     * Name of the default scripts folder.
     */
    public static final String SCRIPTS_FOLDERNAME = "scripts";

    /**
     * Name of the string to use in scripts to refer to the data folder.
     * 
     * <p>This will be substituted at runtime.
     */
    public static final String STAGE_DATA_FOLDER_SUBSTITUTION_NAME = "DATAFOLDER";

    private static final String COULD_NOT_CREATE_DATA_FOLDER = "Could not create data folder in workspace.";
    private static final String COULD_NOT_CREATE_GEOPAP_FOLDER = "Could not create geopaparazzi folder in workspace.";
    private static final String COULD_NOT_CREATE_SCRIPTS_FOLDER = "Could not create scripts folder in workspace.";
    private static final String COULD_NOT_CREATE_USER_FOLDER = "Could not create user folder in workspace.";
    private static final String NO_WORKSPACE_DEFINED = "No workspace defined for the current installation.";
    
    /**
     * The java -D commandline property that defines the workspace.
     */
    private static final String STAGE_WORKSPACE_JAVA_PROPERTIES_KEY = "stage.workspace";

    private static StageWorkspace stageWorkspace;
    private File stageWorkspaceFolder;

    public static StageWorkspace getInstance() {
        if (stageWorkspace == null) {
            stageWorkspace = new StageWorkspace();
        }
        return stageWorkspace;
    }

    private StageWorkspace() {
        String stageWorkspacepath = System.getProperty(STAGE_WORKSPACE_JAVA_PROPERTIES_KEY);
        if (stageWorkspacepath == null || !new File(stageWorkspacepath).exists()) {
            throw new RuntimeException(NO_WORKSPACE_DEFINED);
        }
        stageWorkspaceFolder = new File(stageWorkspacepath);
    }

    public File getStageWorkspaceFolder() {
        return stageWorkspaceFolder;
    }

    public File getUserFolder( String user ) {
        File userFolder = new File(stageWorkspaceFolder, user);
        if (!userFolder.exists()) {
            if (!userFolder.mkdirs()) {
                throw new RuntimeException(COULD_NOT_CREATE_USER_FOLDER);
            }
        }
        return userFolder;
    }

    public File getScriptsFolder( String user ) {
        File userFolder = getUserFolder(user);
        File scriptsFolder = new File(userFolder, SCRIPTS_FOLDERNAME);
        if (!scriptsFolder.exists() && !scriptsFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_SCRIPTS_FOLDER);
        }
        return scriptsFolder;
    }

    public File getDataFolder( String user ) {
        File userFolder = getUserFolder(user);
        File dataFolder = new File(userFolder, DATA_FOLDERNAME);
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_DATA_FOLDER);
        }
        return dataFolder;
    }

    public File getGeopaparazziFolder( String user ) {
        File userFolder = getUserFolder(user);
        File geopaparazziFolder = new File(userFolder, GEOPAPARAZZI_FOLDERNAME);
        if (!geopaparazziFolder.exists() && !geopaparazziFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_GEOPAP_FOLDER);
        }
        return geopaparazziFolder;
    }

    /**
     * Convert a file to its relative to datafolder version.
     * 
     * @param currentFile the file to trim.
     * @return the relative path.
     */
    public static String makeRelativeToDataFolder( File currentFile ) {
        File dataFolder = getInstance().getDataFolder(User.getCurrentUserName());

        String dataFolderPath = dataFolder.getAbsolutePath();
        String filePath = currentFile.getAbsolutePath();

        String relativePath = filePath.replaceFirst(dataFolderPath, "");
        if (relativePath.startsWith("/") && !new File(relativePath).exists()) {
            relativePath = relativePath.substring(1);
        }
        return relativePath;
    }

    /**
     * Add to the relative file path the data folder. 
     * 
     * @param relativePath the relative path.
     * @return the complete absolute path.
     */
    public static File makeRelativeDataPathToFile( String relativePath ) {
        File dataFolder = getInstance().getDataFolder(User.getCurrentUserName());
        File file = new File(dataFolder, relativePath);
        return file;
    }
}
