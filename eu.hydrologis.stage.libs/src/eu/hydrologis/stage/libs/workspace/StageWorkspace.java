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

import eu.hydrologis.stage.libs.log.StageLogger;

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
     * Name of the string to use in scripts to refer to the remote data folder.
     *  
     * <p>This will be substituted at runtime.
     */
    public static final String STAGE_DATA_FOLDER_SUBSTITUTION_NAME = "@df";

    private static final String COULD_NOT_CREATE_DATA_FOLDER = "Could not create data folder in workspace.";
    private static final String COULD_NOT_CREATE_GEOPAP_FOLDER = "Could not create geopaparazzi folder in workspace.";
    private static final String COULD_NOT_CREATE_SCRIPTS_FOLDER = "Could not create scripts folder in workspace.";
    private static final String COULD_NOT_CREATE_USER_FOLDER = "Could not create user folder in workspace.";
    private static final String NO_WORKSPACE_DEFINED = "No workspace defined for the current installation.";

    /**
     * The java -D commandline property that defines the workspace.
     */
    private static final String STAGE_WORKSPACE_JAVA_PROPERTIES_KEY = "stage.workspace";
    private static final String STAGE_DATAFOLDER_JAVA_PROPERTIES_KEY = "stage.datafolder";
    private static final String STAGE_SCRIPTSFOLDER_JAVA_PROPERTIES_KEY = "stage.scriptsfolder";
    private static final String STAGE_GEOPAPARAZZIFOLDER_JAVA_PROPERTIES_KEY = "stage.geopaparazzifolder";

    private static StageWorkspace stageWorkspace;
    private File stageWorkspaceFolder;
    private File customDataFolder;
    private File customScriptsFolder;
    private File customGeopaparazziFolder;

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

        String dataFolderPath = System.getProperty(STAGE_DATAFOLDER_JAVA_PROPERTIES_KEY);
        if (dataFolderPath != null && new File(dataFolderPath).exists()) {
            customDataFolder = new File(dataFolderPath);
            StageLogger.logInfo(this, "Custom data folder in use: " + customDataFolder);
        }
        String scriptsFolderPath = System.getProperty(STAGE_SCRIPTSFOLDER_JAVA_PROPERTIES_KEY);
        if (scriptsFolderPath != null && new File(scriptsFolderPath).exists()) {
            customScriptsFolder = new File(scriptsFolderPath);
            StageLogger.logInfo(this, "Custom scripts folder in use: " + customScriptsFolder);
        }
        String geopaparazziFolderPath = System.getProperty(STAGE_GEOPAPARAZZIFOLDER_JAVA_PROPERTIES_KEY);
        if (geopaparazziFolderPath != null && new File(geopaparazziFolderPath).exists()) {
            customGeopaparazziFolder = new File(geopaparazziFolderPath);
            StageLogger.logInfo(this, "Custom geopaparazzi folder in use: " + customGeopaparazziFolder);
        }
        stageWorkspaceFolder = new File(stageWorkspacepath);
    }

    private File getUserFolder( String user ) {
        File userFolder = new File(stageWorkspaceFolder, user);
        if (!userFolder.exists()) {
            if (!userFolder.mkdirs()) {
                throw new RuntimeException(COULD_NOT_CREATE_USER_FOLDER);
            }
        }
        return userFolder;
    }

    public File getScriptsFolder( String user ) {
        if (customScriptsFolder != null) {
            return customScriptsFolder;
        }
        File userFolder = getUserFolder(user);
        File scriptsFolder = new File(userFolder, SCRIPTS_FOLDERNAME);
        if (!scriptsFolder.exists() && !scriptsFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_SCRIPTS_FOLDER);
        }
        return scriptsFolder;
    }

    public File getDataFolder( String user ) {
        if (customDataFolder != null) {
            return customDataFolder;
        }
        File userFolder = getUserFolder(user);
        File dataFolder = new File(userFolder, DATA_FOLDERNAME);
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_DATA_FOLDER);
        }
        return dataFolder;
    }

    public File getGeopaparazziFolder( String user ) {
        if (customGeopaparazziFolder != null) {
            return customGeopaparazziFolder;
        }
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
        dataFolderPath = checkBackSlash(dataFolderPath);
        String filePath = currentFile.getAbsolutePath();
        filePath = checkBackSlash(filePath);

        String relativePath = filePath.replaceFirst(dataFolderPath, "");
        if (relativePath.startsWith("/") && !new File(relativePath).exists()) {
            relativePath = relativePath.substring(1);
        }
        return relativePath;
    }

    /**
     * Substitute the datafolder in a string.
     * 
     * @param stringToCheck
     * @return
     */
    public static String substituteDataFolder( String stringToCheck ) {
        File dataFolder = getInstance().getDataFolder(User.getCurrentUserName());

        String dataFolderPath = dataFolder.getAbsolutePath();
        dataFolderPath = checkBackSlash(dataFolderPath);

        String removed = stringToCheck.replaceAll(dataFolderPath, STAGE_DATA_FOLDER_SUBSTITUTION_NAME);
        return removed;
    }

    private static String checkBackSlash( String path ) {
        if (path != null) {
            path = path.replaceAll("\\\\", "/");
        }
        return path;
    }

    public static String makeSafe( String path ) {
        path = path.replace('\\', '/');
        return path;
    }

    /**
     * Add to the relative file path the data folder. 
     * 
     * @param relativePath the relative path.
     * @param isOutFile <code>true</code>, if the path is supposed to be an outfile, in which
     *          case the file doesn't exist, but the folder has to. 
     * @return the complete absolute path.
     */
    public static File makeRelativeDataPathToFile( String relativePath, boolean isOutFile ) {
        int firstSlash = relativePath.indexOf('/');
        if (firstSlash > 2 || firstSlash == -1) {
            // it is assumed to be relative
            File dataFolder = getInstance().getDataFolder(User.getCurrentUserName());
            File file = new File(dataFolder, relativePath);
            return file;
        } else {
            File absolutePathFile = new File(relativePath);
            // if (!isOutFile) {
            // if (absolutePathFile.exists()) {
            // // it is an absolute path to an existing file, leave it as is
            // return absolutePathFile;
            // }
            // } else {
            // File parentFile = absolutePathFile.getParentFile();
            // if (parentFile != null && parentFile.exists()) {
            // // it is an absolute path to a file to be created,
            // // leave it if the folder exists
            // return absolutePathFile;
            // }
            // }
            return absolutePathFile;
        }
    }
}
