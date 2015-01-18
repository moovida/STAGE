package eu.hydrologis.stage.libs;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.hydrologis.stage.libs.log.StageLogger;

public class StageLibsActivator implements BundleActivator {

    private static File bundleFile;

    @Override
    public void start( BundleContext context ) throws Exception {
        Bundle bundle = context.getBundle();

        bundleFile = FileLocator.getBundleFile(bundle);
        StageLogger.logInfo(this, "Libs bundle file: " + bundleFile.getAbsolutePath());
    }

    public static File getBundleFile() {
        return bundleFile;
    }

    public static File getGeotoolsLibsFolder() {
        return new File(bundleFile, "libs-geotools");
    }

    public static File getModulesLibsFolder() {
        return new File(bundleFile, "spatialtoolbox");
    }

    @Override
    public void stop( BundleContext context ) throws Exception {
        // TODO Auto-generated method stub

    }

}
