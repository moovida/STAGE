/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.stage.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import oms3.Access;
import oms3.ComponentAccess;
import oms3.annotations.Description;
import oms3.annotations.Label;
import oms3.annotations.Name;
import oms3.annotations.Range;
import oms3.annotations.Status;
import oms3.annotations.UI;
import oms3.annotations.Unit;
import oms3.util.Components;
import eu.hydrologis.rap.log.StageLogger;
import eu.hydrologis.rap.stage.StageSessionPluginSingleton;
import eu.hydrologis.rap.stage.utils.AnnotationUtilities;
import eu.hydrologis.rap.stage.utils.ResourceFinder;
import eu.hydrologis.rap.stage.utils.StageConstants;
import eu.hydrologis.rap.stage.utils.StageUtils;

/**
 * Singleton in which the modules discovery and load/unload occurrs.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class StageModulesManager {

    private static StageModulesManager modulesManager;

    private List<String> loadedJarsList = new ArrayList<String>();
    private TreeMap<String, List<ModuleDescription>> modulesMap;
    private List<ModuleDescription> gridReaders = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> rasterReaders = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> rasterWriters = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> featureReaders = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> featureWriters = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> hashMapReaders = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> hashMapWriters = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> listReaders = new ArrayList<ModuleDescription>();
    private List<ModuleDescription> listWriters = new ArrayList<ModuleDescription>();

    private URLClassLoader jarClassloader;

    private StageModulesManager() {
        getModulesJars(false);
    }

    public List<String> getModulesJars( boolean onlyModules ) {
        List<String> jarsPathList = new ArrayList<String>();
        // add jars from preferences
        String[] retrieveSavedJars = StageSessionPluginSingleton.getInstance().retrieveSavedJars();
        for( String jar : retrieveSavedJars ) {
            addJar(jar);
            jarsPathList.add(jar);
        }

        /*
         * load modules
         */
        File modulesFolder = StageSessionPluginSingleton.getInstance().getModulesFolders();
        if (modulesFolder != null) {
            StageSessionPluginSingleton.getInstance().log("Searching module libraries in: " + modulesFolder.getAbsolutePath());
            File[] extraJars = modulesFolder.listFiles(new FilenameFilter(){
                public boolean accept( File dir, String name ) {
                    return name.endsWith(".jar");
                }
            });
            for( File extraJar : extraJars ) {
                addJar(extraJar.getAbsolutePath());
                jarsPathList.add(extraJar.getAbsolutePath());
            }
        }
        /*
         * load libs
         */
        if (!onlyModules) {
            File libsFolder = StageSessionPluginSingleton.getInstance().getLibsFolders();
            if (libsFolder != null) {
                StageSessionPluginSingleton.getInstance().log("Searching libs in: " + libsFolder.getAbsolutePath());
                File[] extraJars = libsFolder.listFiles(new FilenameFilter(){
                    public boolean accept( File dir, String name ) {
                        return name.endsWith(".jar");
                    }
                });
                for( File extraJar : extraJars ) {
                    addJar(extraJar.getAbsolutePath());
                    jarsPathList.add(extraJar.getAbsolutePath());
                }
            }
        }

        return jarsPathList;
    }

    public synchronized static StageModulesManager getInstance() {
        if (modulesManager == null) {
            modulesManager = new StageModulesManager();
        }
        return modulesManager;
    }

    /**
     * Add a jar to the jars list.
     * 
     * @param newJar
     *            the path to the new jar to add.
     */
    public void addJar( String newJar ) {
        if (!loadedJarsList.contains(newJar)) {
            loadedJarsList.add(newJar);
        }
    }

    /**
     * Remove a jar from the jars list.
     * 
     * @param removeJar
     *            the jar to remove.
     */
    public void removeJar( String removeJar ) {
        if (loadedJarsList.contains(removeJar)) {
            loadedJarsList.remove(removeJar);
        }
    }

    /**
     * Remove all jars from the cache list.
     */
    public void clearJars() {
        loadedJarsList.clear();
    }

    /**
     * Browses the loaded jars searching for executable modules.
     * 
     * @param rescan
     *            whther to scan the jars again. Isn't considered the first
     *            time.
     * @return the list of modules that are executable.
     * @throws IOException
     */
    public TreeMap<String, List<ModuleDescription>> browseModules( boolean rescan ) {
        try {
            if (modulesMap == null) {
                modulesMap = new TreeMap<String, List<ModuleDescription>>();
            } else {
                if (rescan) {
                    modulesMap.clear();
                    gridReaders.clear();
                    rasterReaders.clear();
                    rasterWriters.clear();
                    featureReaders.clear();
                    featureWriters.clear();
                    hashMapReaders.clear();
                    hashMapWriters.clear();
                    listWriters.clear();
                    listReaders.clear();
                } else {
                    if (modulesMap.size() > 0) {
                        return modulesMap;
                    }
                }
            }

            scanForModules();

        } catch (Exception e) {
            e.printStackTrace();
            modulesMap = new TreeMap<String, List<ModuleDescription>>();
        }

        Set<Entry<String, List<ModuleDescription>>> entrySet = modulesMap.entrySet();
        for( Entry<String, List<ModuleDescription>> entry : entrySet ) {
            Collections.sort(entry.getValue(), new ModuleDescription.ModuleDescriptionNameComparator());
        }

        return modulesMap;
    }

    public List<ModuleDescription> getFeatureReaders() {
        return cloneList(featureReaders);
    }

    public List<ModuleDescription> getFeatureWriters() {
        return cloneList(featureWriters);
    }

    public List<ModuleDescription> getGridReaders() {
        return cloneList(gridReaders);
    }

    public List<ModuleDescription> getRasterReaders() {
        return cloneList(rasterReaders);
    }

    public List<ModuleDescription> getRasterWriters() {
        return cloneList(rasterWriters);
    }

    public List<ModuleDescription> getHashMapReaders() {
        return cloneList(hashMapReaders);
    }

    public List<ModuleDescription> getHashMapWriters() {
        return cloneList(hashMapWriters);
    }

    public List<ModuleDescription> getListReaders() {
        return cloneList(listReaders);
    }

    public List<ModuleDescription> getListWriters() {
        return cloneList(listWriters);
    }

    public List<ModuleDescription> cloneList( List<ModuleDescription> list ) {
        List<ModuleDescription> copy = new ArrayList<ModuleDescription>();
        for( ModuleDescription moduleDescription : list ) {
            copy.add(moduleDescription.makeCopy());
        }
        return copy;
    }

    private void scanForModules() throws Exception {

        if (loadedJarsList.size() == 0) {
            return;
        }

        // add jai/imageio
        File jreFolder = StageSessionPluginSingleton.getInstance().getJreFolders();
        File extLibsFolder = new File(jreFolder.getAbsolutePath() + File.separator + "lib" + File.separator + "ext");
        if (extLibsFolder.exists()) {
            StageLogger.logDebug("ADDING JAI JARS FROM JRE");
            File[] jaiJarFiles = extLibsFolder.listFiles(new FilenameFilter(){
                public boolean accept( File dir, String name ) {
                    return name.contains("jai") || name.contains("jiio");
                }
            });
            for( File jaiJarFile : jaiJarFiles ) {
                String path = jaiJarFile.getAbsolutePath();
                if (!loadedJarsList.contains(path)) {
                    loadedJarsList.add(path);
                    StageLogger.logDebug("--> " + path);
                }
            }
        }

        List<URL> urlList = new ArrayList<URL>();
        StageLogger.logDebug("ADDED TO URL CLASSLOADER:");
        for( int i = 0; i < loadedJarsList.size(); i++ ) {
            String jarPath = loadedJarsList.get(i);
            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                continue;
            }
            urlList.add(jarFile.toURI().toURL());
            StageLogger.logDebug("--> " + jarPath);
        }
        URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
        List<Class< ? >> classesList = new ArrayList<Class< ? >>();

        jarClassloader = new URLClassLoader(urls, this.getClass().getClassLoader());

        StageLogger.logDebug("LOAD MODULES:");
        for( URL url : urlList ) {
            ResourceFinder finder = new ResourceFinder("META-INF/", url);
            Map<String, Properties> servicesList = finder.mapAllProperties("services");
            Properties jgtProperties = servicesList.get("org.jgrasstools.gears.libs.modules.JGTModel");
            if (jgtProperties == null) {
                continue;
            }
            Set<Entry<Object, Object>> entrySet = jgtProperties.entrySet();
            for( Entry<Object, Object> entry : entrySet ) {
                String className = entry.getKey().toString();
                // Avoid loading of oms modules, load only the file based
                // modules
                String simpleName = className;
                int lastDot = simpleName.lastIndexOf(".");
                if (lastDot != -1) {
                    simpleName = simpleName.substring(lastDot + 1);
                }
                if (simpleName.startsWith("Oms")) {
                    continue;
                }

                Class< ? > possibleModulesClass = null;
                try {
                    possibleModulesClass = Class.forName(className, true, jarClassloader);
                } catch (Exception e) {
                    // ignore and try to gather as much as possible
                }
                if (possibleModulesClass != null) {
                    // extract only the ones properly annotated
                    Name name = possibleModulesClass.getAnnotation(Name.class);
                    if (name != null) {
                        classesList.add(possibleModulesClass);
                        StageLogger.logDebug("--> " + className);
                    }
                }
            }
        }

        if (classesList.size() == 0) {
            // try the old and slow way
            try {
                System.out.println("URLS TO LOAD:");
                for( URL url : urls ) {
                    System.out.println(url.toExternalForm());
                }
                List<Class< ? >> allComponents = new ArrayList<Class< ? >>();
                allComponents = Components.getComponentClasses(jarClassloader, urls);
                classesList.addAll(allComponents);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        // clean up html docs in config area, it will be redone
        StageUtils.cleanModuleDocumentation();

        for( Class< ? > moduleClass : classesList ) {
            try {
                String simpleName = moduleClass.getSimpleName();

                UI uiHints = moduleClass.getAnnotation(UI.class);
                if (uiHints != null) {
                    String uiHintStr = uiHints.value();
                    if (uiHintStr.contains(StageConstants.HIDE_UI_HINT)) {
                        continue;
                    }
                }

                Label category = moduleClass.getAnnotation(Label.class);
                String categoryStr = StageConstants.CATEGORY_OTHERS;
                if (category != null && categoryStr.trim().length() > 1) {
                    categoryStr = category.value();
                }

                Description description = moduleClass.getAnnotation(Description.class);
                String descrStr = null;
                if (description != null) {
                    descrStr = description.value();
                }
                Status status = moduleClass.getAnnotation(Status.class);

                ModuleDescription module = new ModuleDescription(moduleClass, categoryStr, descrStr, status);

                Object newInstance = null;
                try {
                    newInstance = moduleClass.newInstance();
                } catch (Throwable e) {
                    // ignore module
                    continue;
                }
                try {
                    // generate the html docs
                    String className = module.getClassName();
                    StageUtils.generateModuleDocumentation(className);
                } catch (Exception e) {
                    // ignore doc if it breaks
                }

                ComponentAccess cA = new ComponentAccess(newInstance);

                Collection<Access> inputs = cA.inputs();
                for( Access access : inputs ) {
                    addInput(access, module);
                }

                Collection<Access> outputs = cA.outputs();
                for( Access access : outputs ) {
                    addOutput(access, module);
                }

                if (categoryStr.equals(StageConstants.GRIDGEOMETRYREADER)) {
                    gridReaders.add(module);
                } else if (categoryStr.equals(StageConstants.RASTERREADER)) {
                    rasterReaders.add(module);
                } else if (categoryStr.equals(StageConstants.RASTERWRITER)) {
                    rasterWriters.add(module);
                } else if (categoryStr.equals(StageConstants.VECTORREADER)) {
                    featureReaders.add(module);
                } else if (categoryStr.equals(StageConstants.VECTORWRITER)) {
                    featureWriters.add(module);
                } else if (categoryStr.equals(StageConstants.GENERICREADER)) {
                    // ignore for now
                } else if (categoryStr.equals(StageConstants.GENERICWRITER)) {
                    // ignore for now
                } else if (categoryStr.equals(StageConstants.HASHMAP_READER)) {
                    hashMapReaders.add(module);
                } else if (categoryStr.equals(StageConstants.HASHMAP_WRITER)) {
                    hashMapWriters.add(module);
                } else if (categoryStr.equals(StageConstants.LIST_READER)) {
                    listReaders.add(module);
                } else if (categoryStr.equals(StageConstants.LIST_WRITER)) {
                    listWriters.add(module);
                } else {
                    List<ModuleDescription> modulesList4Category = modulesMap.get(categoryStr);
                    if (modulesList4Category == null) {
                        modulesList4Category = new ArrayList<ModuleDescription>();
                        modulesMap.put(categoryStr, modulesList4Category);
                    }
                    modulesList4Category.add(module);
                }

            } catch (NoClassDefFoundError e) {
                if (moduleClass != null)
                    System.out.println("ERROR IN: " + moduleClass.getCanonicalName());
                e.printStackTrace();
            }
        }

    }

    private void addInput( Access access, ModuleDescription module ) throws Exception {
        Field field = access.getField();
        Description descriptionAnn = field.getAnnotation(Description.class);
        String descriptionStr = "No description available";
        if (descriptionAnn != null) {
            descriptionStr = AnnotationUtilities.getLocalizedDescription(descriptionAnn);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(descriptionStr);

        Unit unitAnn = field.getAnnotation(Unit.class);
        if (unitAnn != null) {
            sb.append(" [");
            sb.append(unitAnn.value());
            sb.append("]");
        }
        Range rangeAnn = field.getAnnotation(Range.class);
        if (rangeAnn != null) {
            sb.append(" [");
            sb.append(rangeAnn.min());
            sb.append(" ,");
            sb.append(rangeAnn.max());
            sb.append("]");
        }
        descriptionStr = sb.toString();

        String fieldName = field.getName();
        Class< ? > fieldClass = field.getType();
        Object fieldValue = access.getFieldValue();

        String defaultValue = ""; //$NON-NLS-1$
        if (fieldValue != null) {
            defaultValue = fieldValue.toString();
        }

        UI uiHintAnn = field.getAnnotation(UI.class);
        String uiHint = null;
        if (uiHintAnn != null) {
            uiHint = uiHintAnn.value();
        }

        module.addInput(fieldName, fieldClass.getCanonicalName(), descriptionStr, defaultValue, uiHint);
    }

    private void addOutput( Access access, ModuleDescription module ) throws Exception {
        Field field = access.getField();
        Description descriptionAnn = field.getAnnotation(Description.class);
        String descriptionStr = "No description available";
        if (descriptionAnn != null) {
            descriptionStr = AnnotationUtilities.getLocalizedDescription(descriptionAnn);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(descriptionStr);

        Unit unitAnn = field.getAnnotation(Unit.class);
        if (unitAnn != null) {
            sb.append(" [");
            sb.append(unitAnn.value());
            sb.append("]");
        }
        Range rangeAnn = field.getAnnotation(Range.class);
        if (rangeAnn != null) {
            sb.append(" [");
            sb.append(rangeAnn.min());
            sb.append(" ,");
            sb.append(rangeAnn.max());
            sb.append("]");
        }
        descriptionStr = sb.toString();

        String fieldName = field.getName();
        Class< ? > fieldClass = field.getType();
        Object fieldValue = access.getFieldValue();

        String defaultValue = ""; //$NON-NLS-1$
        if (fieldValue != null) {
            defaultValue = fieldValue.toString();
        }

        UI uiHintAnn = field.getAnnotation(UI.class);
        String uiHint = null;
        if (uiHintAnn != null) {
            uiHint = uiHintAnn.value();
        }

        module.addOutput(fieldName, fieldClass.getCanonicalName(), descriptionStr, defaultValue, uiHint);
    }

    /**
     * Get a class from the loaded modules.
     * 
     * @param className
     *            full class name.
     * @return the class for the given name.
     * @throws ClassNotFoundException
     */
    public Class< ? > getModulesClass( String className ) throws ClassNotFoundException {
        Class< ? > moduleClass = Class.forName(className, true, jarClassloader);
        return moduleClass;
    }

    public InputStream getResourceAsStream( String fullName ) throws IOException {
        URL resource = jarClassloader.getResource(fullName);
        InputStream resourceAsStream = resource.openStream();
        // InputStream resourceAsStream =
        // jarClassloader.getResourceAsStream(fullName);
        return resourceAsStream;
    }

}
