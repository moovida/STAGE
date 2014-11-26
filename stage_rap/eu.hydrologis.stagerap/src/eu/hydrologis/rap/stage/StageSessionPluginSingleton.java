package eu.hydrologis.rap.stage;

import java.io.File;

import org.eclipse.rap.rwt.SingletonUtil;

import eu.hydrologis.rap.stage.utils.StageConstants;
import static eu.hydrologis.rap.stage.utils.StageConstants.*;

public class StageSessionPluginSingleton {
	private File modulesFolder;
	private File libsFolder;

	private StageSessionPluginSingleton() {
		File installationFolder = new File(".");
		modulesFolder = new File(installationFolder, LIBS_MAIN_FOLDER_NAME + "/" + MODULES_SUBFOLDER_NAME);
		libsFolder = new File(installationFolder, LIBS_MAIN_FOLDER_NAME + "/" + LIBS_SUBFOLDER_NAME);
	}

	public static StageSessionPluginSingleton getInstance() {
		return SingletonUtil.getSessionInstance(StageSessionPluginSingleton.class);
	}

	public File getModulesFolders() {
		return modulesFolder;
	}

	public File getLibsFolders() {
		return libsFolder;
	}

	public String[] retrieveSavedJars() {
		// TODO Auto-generated method stub
		return null;
	}


	public void log(String string) {
		// TODO Auto-generated method stub
		
	}


	public File getConfigurationsFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getApplicationJava() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClasspathJars() {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveSavedLogLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	public void cleanProcess(String scriptId) {
		// TODO Auto-generated method stub
		
	}

	public int retrieveSavedHeap() {
		// TODO Auto-generated method stub
		return 2000;
	}

	public String retrieveSavedEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addProcess(Process process, String scriptId) {
		// TODO Auto-generated method stub
		
	}

	public void setLastFolderChosen(String absolutePath) {
		// TODO Auto-generated method stub
		
	}

	public String getWorkingFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean doIgnoreProcessingRegion() {
		// TODO Auto-generated method stub
		return false;
	}

	public void saveHeap(int parseInt) {
		// TODO Auto-generated method stub
		
	}

	public void saveLogLevel(String item) {
		// TODO Auto-generated method stub
		
	}

	public void saveEncoding(String item) {
		// TODO Auto-generated method stub
		
	}

}