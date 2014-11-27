/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.hydrologis.rap.stage.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oms3.CLI;
import eu.hydrologis.rap.stage.StageSessionPluginSingleton;
import eu.hydrologis.rap.stage.utils.FileUtilities;
import eu.hydrologis.rap.stage.utils.StageConstants;

/**
 * Executor of OMS scripts.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class StageScriptExecutor {

    private String classPath;

    private boolean isRunning = false;

    List<IProcessListener> listeners = new ArrayList<IProcessListener>();

    private String javaFile;

    public StageScriptExecutor() throws Exception {
        /*
         * get java exec
         */
        javaFile = StageSessionPluginSingleton.getInstance().getApplicationJava();
        if (!javaFile.equals("java")) {
            javaFile = "\"" + javaFile + "\"";
        }
        /*
         * get libraries
         */
        String classpathJars = StageSessionPluginSingleton.getInstance().getClasspathJars();
        classPath = classpathJars;
    }

    /**
     * Execute an OMS script.
     * 
     * @param script the script file or the script string.
     * @param internalStream
     * @param errorStream
     * @param loggerLevelGui the log level as presented in the GUI, can be OFF|ON. This is not the OMS logger level, which 
     *                              in stead has to be picked from the {@link StageConstants#LOGLEVELS_MAP}.
     * @param ramLevel the heap size to use in megabytes.
     * @param encoding 
     * @return the process.
     * @throws Exception
     */
    public Process exec( String script, final String loggerLevelGui, String ramLevel, String encoding ) throws Exception {
        File scriptFile = new File(script);
        if (!scriptFile.exists()) {
            // if the file doesn't exist, it is a script, let's put it into a file
            scriptFile = File.createTempFile("omsbox_script_", ".oms");
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(scriptFile));
                bw.write(script);
            } finally {
                bw.close();
            }

        } else {
            // it is a script in a file, read it to log it
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            try {
                br = new BufferedReader(new FileReader(scriptFile));
                String line = null;
                while( (line = br.readLine()) != null ) {
                    sb.append(line).append("\n");
                }
            } finally {
                br.close();
            }
            script = sb.toString();
        }

        // tmp folder
        String tempdir = System.getProperty("java.io.tmpdir");
        File omsTmp = new File(tempdir + File.separator + "oms");
        if (!omsTmp.exists())
            omsTmp.mkdirs();

        List<String> arguments = new ArrayList<String>();
        arguments.add(javaFile);

        // ram usage
        String ramExpr = "-Xmx" + ramLevel + "m";
        arguments.add(ramExpr);

        if (encoding != null && encoding.length() > 0) {
            encoding = "-Dfile.encoding=" + encoding;
            arguments.add(encoding);
        }

        // modules jars
        List<String> modulesJars = StageModulesManager.getInstance().getModulesJars(true);
        StringBuilder sb = new StringBuilder();
        for( String moduleJar : modulesJars ) {
            sb.append(File.pathSeparator).append(moduleJar);
        }
        String modulesJarsString = sb.toString().replaceFirst(File.pathSeparator, "");
        String resourcesFlag = "-Doms.sim.resources=\"" + modulesJarsString + "\"";
        arguments.add(resourcesFlag);

        // all the arguments
        arguments.add("-cp");
        arguments.add(classPath);
        arguments.add(CLI.class.getCanonicalName());
        arguments.add("-r ");
        arguments.add("\"" + scriptFile.getAbsolutePath() + "\"");

        String homeDir = System.getProperty("java.io.tmpdir");
        File homeFile = new File(homeDir);
        StringBuilder runSb = new StringBuilder();
        for( String arg : arguments ) {
            runSb.append(arg).append(" ");
        }

        String[] args;
        // FIXME find OS
        // if (Platform.getOS().equals(Platform.OS_WIN32)) {
        // File tmpRunFile = new File(homeFile, "udig_spatialtoolbox.bat");
        // FileUtilities.writeFile("@echo off\n" + runSb.toString(), tmpRunFile);
        // args = new String[]{"cmd", "/c", tmpRunFile.getAbsolutePath()};
        // } else {
        File tmpRunFile = new File(homeFile, "udig_spatialtoolbox.sh");
        FileUtilities.writeFile(runSb.toString(), tmpRunFile);
        args = new String[]{"sh", tmpRunFile.getAbsolutePath()};
        // }

        // {javaFile, ramExpr, resourcesFlag, "-cp", classPath,
        // CLI.class.getCanonicalName(), "-r",
        // scriptFile.getAbsolutePath()};

        ProcessBuilder processBuilder = new ProcessBuilder(args);
        // work in home
        // processBuilder.directory(homeFile);

        // environment
        Map<String, String> environment = processBuilder.environment();
        // environment.put("CLASSPATH", classPath);

        final Process process = processBuilder.start();
        printMessage("Process started: " + StageConstants.dateTimeFormatterYYYYMMDDHHMMSS.format(new Date()));
        printMessage("");

        // command launched
        if (loggerLevelGui.equals(StageConstants.LOGLEVEL_GUI_ON)) {
            StringBuilder tmpBuilder = new StringBuilder();
            tmpBuilder.append("------------------------------>8----------------------------\n");
            tmpBuilder.append("Launching command: \n");
            tmpBuilder.append("------------------\n");
            List<String> command = processBuilder.command();
            for( String arg : command ) {
                tmpBuilder.append(arg);
                tmpBuilder.append(" ");
            }
            tmpBuilder.append("\n");
            tmpBuilder.append("(you can run the above from command line, customizing the content)\n");
            tmpBuilder.append("----------------------------------->8---------------------------------\n");
            tmpBuilder.append("\n");
            // script run
            tmpBuilder.append("Script run: \n");
            tmpBuilder.append("-----------\n");
            tmpBuilder.append(script);
            tmpBuilder.append("\n");
            tmpBuilder.append("------------------------------>8----------------------------\n");
            tmpBuilder.append("\n");
            // environment used
            tmpBuilder.append("Environment used: \n");
            tmpBuilder.append("-----------------\n");

            Set<Entry<String, String>> entrySet = environment.entrySet();
            for( Entry<String, String> entry : entrySet ) {
                tmpBuilder.append(entry.getKey());
                tmpBuilder.append(" =\t");
                tmpBuilder.append(entry.getValue()).append("\n");
            }
            tmpBuilder.append("------------------------------>8----------------------------\n");
            tmpBuilder.append("");
            printMessage(tmpBuilder.toString());
        }
        printMessage("");
        isRunning = true;

        new Thread(){
            public void run() {
                BufferedReader br = null;
                try {
                    InputStream is = process.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);
                    String line;
                    while( (line = br.readLine()) != null ) {
                        printMessage(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    printException(loggerLevelGui, e);
                } finally {
                    if (br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    isRunning = false;
                    updateListenersForModuleStop();
                }
                printMessage("\nProcess finished: " + StageConstants.dateTimeFormatterYYYYMMDDHHMMSS.format(new Date()));
            }

        }.start();

        new Thread(){
            public void run() {
                BufferedReader br = null;
                try {
                    InputStream is = process.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);
                    String line;
                    while( (line = br.readLine()) != null ) {
                        /*
                         * remove of ugly recurring geotools warnings. Not nice, but 
                         * at least users do not get confused. 
                         */
                        if (ConsoleMessageFilter.doRemove(line)) {
                            continue;
                        }
                        printError(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    printException(loggerLevelGui, e);
                } finally {
                    if (br != null)
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            };
        }.start();

        return process;
    }
    public boolean isRunning() {
        return isRunning;
    }

    public void addProcessListener( IProcessListener listener ) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeProcessListener( IProcessListener listener ) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void printMessage( String message ) {
        for( IProcessListener listener : listeners ) {
            listener.onMessage(message, false);
        }
    }

    private void printError( String error ) {
        for( IProcessListener listener : listeners ) {
            listener.onMessage(error, true);
        }
    }

    private void updateListenersForModuleStop() {
        for( IProcessListener listener : listeners ) {
            listener.onProcessStopped();
        }
    }

    private void printException( final String loggerLevelGui, Exception e ) {
        if (loggerLevelGui.equals(StageConstants.LOGLEVEL_GUI_ON)) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            printError(sw.toString());
        } else {
            printError(e.getLocalizedMessage());
        }
    };
}
