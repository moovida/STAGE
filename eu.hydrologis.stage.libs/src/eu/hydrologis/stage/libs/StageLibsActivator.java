package eu.hydrologis.stage.libs;

import java.io.File;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.hydrologis.stage.libs.log.StageLogger;

public class StageLibsActivator implements BundleActivator {

    private static File bundleFile;

    /**
     * The java -D commandline property that defines the workspace.
     */
    private static final String STAGE_JAVAEXEC_JAVA_PROPERTIES_KEY = "stage.javaexec";

    private static File geotoolsLibsFolder;

    private static File spatialToolboxFolder;

    private static File stageJavaExecFile;

    @Override
    public void start( BundleContext context ) throws Exception {
        Bundle bundle = context.getBundle();

        bundleFile = FileLocator.getBundleFile(bundle);

        StageLogger.logInfo(this, "Libs bundle file: " + bundleFile.getAbsolutePath());

        geotoolsLibsFolder = new File(bundleFile, "libs-geotools");
        spatialToolboxFolder = new File(bundleFile, "spatialtoolbox");

        String stageJavaExecPath = System.getProperty(STAGE_JAVAEXEC_JAVA_PROPERTIES_KEY);
        boolean javaMissing = false;
        if (stageJavaExecPath != null) {
            stageJavaExecFile = new File(stageJavaExecPath);
            if (stageJavaExecFile.exists()) {
                StageLogger.logInfo(this, "Java executable: " + stageJavaExecFile.getAbsolutePath());
            } else {
                javaMissing = true;
            }
        } else {
            javaMissing = true;
        }
        if (javaMissing)
            throw new RuntimeException("No java executable for STAGE modules execution found.");

    }

    public static File getBundleFile() {
        return bundleFile;
    }

    public static File getGeotoolsLibsFolder() {
        return geotoolsLibsFolder;
    }

    public static File getModulesLibsFolder() {
        return spatialToolboxFolder;
    }

    public static File getStageJavaExec() {
        return stageJavaExecFile;
    }

    @Override
    public void stop( BundleContext context ) throws Exception {
        // TODO Auto-generated method stub

    }

}
