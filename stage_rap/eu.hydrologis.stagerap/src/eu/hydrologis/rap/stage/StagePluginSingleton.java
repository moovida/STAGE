package eu.hydrologis.rap.stage;

import java.io.File;

import org.eclipse.rap.rwt.SingletonUtil;

public class StagePluginSingleton {
	private StagePluginSingleton() {
	}

	public static StagePluginSingleton getInstance() {
		return SingletonUtil.getSessionInstance(StagePluginSingleton.class);
	}

	public String[] retrieveSavedJars() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getModulesFolders() {
		// TODO Auto-generated method stub
		return null;
	}

	public void log(String string) {
		// TODO Auto-generated method stub
		
	}

	public File getLibsFolders() {
		// TODO Auto-generated method stub
		return null;
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