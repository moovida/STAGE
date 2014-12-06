package eu.hydrologis.rap.stage.workspace;

import java.io.File;

public class StageWorkspace {
    public static String STAGE_DATA_FOLDER_SUBSTITUTION_NAME = "DATAFOLDER";

    private static final String COULD_NOT_CREATE_DATA_FOLDER = "Could not create data folder in workspace.";
    private static final String COULD_NOT_CREATE_SCRIPTS_FOLDER = "Could not create scripts folder in workspace.";
    private static final String COULD_NOT_CREATE_USER_FOLDER = "Could not create user folder in workspace.";
    private static final String NO_WORKSPACE_DEFINED = "No workspace defined for the current installation.";
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
        File scriptsFolder = new File(userFolder, "scripts");
        if (!scriptsFolder.exists() && !scriptsFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_SCRIPTS_FOLDER);
        }
        return scriptsFolder;
    }

    public File getDataFolder( String user ) {
        File userFolder = getUserFolder(user);
        File dataFolder = new File(userFolder, "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException(COULD_NOT_CREATE_DATA_FOLDER);
        }
        return dataFolder;
    }
}
