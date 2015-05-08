/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules;

import static eu.hydrologis.stage.modules.utils.SpatialToolboxConstants.LIBS_MAIN_FOLDER_NAME;
import static eu.hydrologis.stage.modules.utils.SpatialToolboxConstants.LIBS_SUBFOLDER_NAME;
import static eu.hydrologis.stage.modules.utils.SpatialToolboxConstants.MODULES_SUBFOLDER_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.SettingStore;

import eu.hydrologis.stage.libs.StageLibsActivator;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.modules.utils.SpatialToolboxConstants;

public class SpatialToolboxSessionPluginSingleton {

    public static final String JARPATH_SPLITTER = "@@@"; //$NON-NLS-1$
    public static final String STAGE_LOADED_JARS_KEY = "STAGE_LOADED_JARS_KEY"; //$NON-NLS-1$
    public static final String STAGE_LOG_KEY = "STAGE_LOG_KEY"; //$NON-NLS-1$
    public static final String STAGE_ENCODING = "STAGE_ENCODING"; //$NON-NLS-1$
    public static final String STAGE_RAM_KEY = "STAGE_RAM_KEY"; //$NON-NLS-1$

    private File installationFolder;
    private File configFolder;
    private File jreFolder;
    private boolean doIgnoreProcessingRegion = true;

    private HashMap<String, Process> runningProcessesMap = new HashMap<String, Process>();

    private SpatialToolboxSessionPluginSingleton() {
        installationFolder = new File("."); // TODO make this good

        configFolder = new File(installationFolder, "stageconfig");
        jreFolder = new File(installationFolder, "jre");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }

    public static SpatialToolboxSessionPluginSingleton getInstance() {
        return SingletonUtil.getSessionInstance(SpatialToolboxSessionPluginSingleton.class);
    }

    public File getJreFolders() {
        return jreFolder;
    }

    public String[] retrieveSavedJars() {
        ApplicationContext context = RWT.getApplicationContext();
        Object loadedJarsObj = context.getAttribute(STAGE_LOADED_JARS_KEY);
        if (loadedJarsObj instanceof String) {
            String loadedJars = (String) loadedJarsObj;
            if (loadedJars.length() > 0) {
                String[] jarSplits = loadedJars.split(JARPATH_SPLITTER);
                // filter out only existing jars
                List<String> existingList = new ArrayList<String>();
                for( String jarPath : jarSplits ) {
                    File f = new File(jarPath);
                    if (f.exists()) {
                        existingList.add(jarPath);
                    }
                }

                if (existingList.size() == 0) {
                    return new String[0];
                }

                String[] existingJarsArray = existingList.toArray(new String[existingList.size()]);
                return existingJarsArray;
            }
        }
        return new String[0];
    }

    /**
     * Save a list of jar paths to the preferences.
     * 
     * @param jarsList
     *            the list of jars to save.
     */
    public void saveJars( List<String> jarsList ) {
        ApplicationContext context = RWT.getApplicationContext();
        StringBuilder sb = new StringBuilder();
        for( String jarPath : jarsList ) {
            sb.append(JARPATH_SPLITTER);
            sb.append(jarPath);
        }
        String jarsPathPref = sb.toString().replaceFirst(JARPATH_SPLITTER, ""); //$NON-NLS-1$
        context.setAttribute(STAGE_LOADED_JARS_KEY, jarsPathPref);
    }

    public int retrieveSavedHeap() throws IOException {
        // return 2000;
        int savedRam = 2000;

        SettingStore settingStore = RWT.getSettingStore();
        Object savedRamObj = settingStore.getAttribute(STAGE_RAM_KEY);
        if (savedRamObj instanceof String) {
            String savedRamStr = (String) savedRamObj;
            try {
                savedRam = Integer.parseInt(savedRamStr);
                if (savedRam <= 0) {
                    savedRam = 2000;
                    saveHeap(savedRam);
                }
            } catch (NumberFormatException e) {
                // ignore and return default
                saveHeap(savedRam);
            }
        }
        return savedRam;
    }

    public void saveHeap( int heapMB ) throws IOException {
        SettingStore settingStore = RWT.getSettingStore();
        settingStore.setAttribute(STAGE_RAM_KEY, String.valueOf(heapMB));
    }

    public String retrieveSavedLogLevel() throws IOException {
        SettingStore settingStore = RWT.getSettingStore();
        String savedLogLevel = settingStore.getAttribute(STAGE_LOG_KEY);
        if (savedLogLevel == null || savedLogLevel.length() == 0) {
            savedLogLevel = "OFF";
            saveLogLevel(savedLogLevel);
        }
        return savedLogLevel;
    }

    public void saveLogLevel( String item ) throws IOException {
        SettingStore settingStore = RWT.getSettingStore();
        settingStore.setAttribute(STAGE_LOG_KEY, item);
    }

    public String retrieveSavedEncoding() throws IOException {
        SettingStore settingStore = RWT.getSettingStore();
        String savedEncodingLevel = settingStore.getAttribute(STAGE_ENCODING);
        if (savedEncodingLevel == null || savedEncodingLevel.length() == 0) {
            savedEncodingLevel = "";
            saveEncoding(savedEncodingLevel);
        }
        return savedEncodingLevel;
    }

    public void saveEncoding( String item ) throws IOException {
        SettingStore settingStore = RWT.getSettingStore();
        settingStore.setAttribute(STAGE_ENCODING, item);
    }

    public String getClasspathJars() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(".");
            sb.append(File.pathSeparator);

            String sysClassPath = System.getProperty("java.class.path");
            if (sysClassPath != null && sysClassPath.length() > 0 && !sysClassPath.equals("null")) {
                addPath(sysClassPath, sb);
                sb.append(File.pathSeparator);
            }

            // add jars in the libs folder
            File libsFolder = StageLibsActivator.getGeotoolsLibsFolder();
            if (libsFolder != null) {
                sb.append(File.pathSeparator);
                addPath(libsFolder.getAbsolutePath() + File.separator + "*", sb);
            }
            // add jars in the modules folder
            File modulesFolder = StageLibsActivator.getModulesLibsFolder();
            if (modulesFolder != null) {
                sb.append(File.pathSeparator);
                addPath(modulesFolder.getAbsolutePath() + File.separator + "*", sb);
            }

            // add loaded jars
            String[] retrieveSavedJars = retrieveSavedJars();
            for( String file : retrieveSavedJars ) {
                sb.append(File.pathSeparator);
                addPath(file, sb);
            }

            return sb.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void addPath( String path, StringBuilder sb ) throws IOException {
//        sb.append("\"").append(path).append("\"");
         sb.append(path);
    }

    public void addProcess( Process process, String id ) {
        Process tmp = runningProcessesMap.get(id);
        if (tmp != null) {
            tmp.destroy();
        }
        runningProcessesMap.put(id, process);
    }

    public void killProcess( String id ) {
        Process process = runningProcessesMap.get(id);
        if (process != null) {
            runningProcessesMap.remove(id);
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // also cleanup if needed
        Set<Entry<String, Process>> entrySet = runningProcessesMap.entrySet();
        for( Entry<String, Process> entry : entrySet ) {
            if (entry.getValue() == null) {
                String key = entry.getKey();
                if (key != null)
                    runningProcessesMap.remove(key);
            }
        }
    }

    public HashMap<String, Process> getRunningProcessesMap() {
        return runningProcessesMap;
    }

    public void cleanProcess( String id ) {
        runningProcessesMap.remove(id);
    }

    public File getConfigurationsFolder() {
        return configFolder;
    }

    public void setLastFolderChosen( String absolutePath ) {
        // TODO Auto-generated method stub

    }

    public String getWorkingFolder() {
        SettingStore settingStore = RWT.getSettingStore();
        String workingFolder = settingStore.getAttribute(SpatialToolboxConstants.WORKINGFOLDER);
        if (workingFolder == null || workingFolder.length() == 0) {
            return null;
        }
        return workingFolder + "/";
    }

    public void setWorkingFolder( String path ) throws IOException {
        SettingStore settingStore = RWT.getSettingStore();
        settingStore.setAttribute(SpatialToolboxConstants.WORKINGFOLDER, path);
    }

    public boolean doIgnoreProcessingRegion() {
        return doIgnoreProcessingRegion;
    }

    public void setDoIgnoreProcessingRegion( boolean doIgnoreProcessingRegion ) {
        this.doIgnoreProcessingRegion = doIgnoreProcessingRegion;
    }

}