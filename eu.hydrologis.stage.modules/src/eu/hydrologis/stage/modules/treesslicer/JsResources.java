package eu.hydrologis.stage.modules.treesslicer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ResourceManager;

public class JsResources {

    private static final String[][] javascriptFiles = new String[][]{//
    {"libs/jquery.min.js", "jquery.min.js"}, //
            {"libs/d3.min.js", "d3.min.js"}, //
            {"libs/leaflet.css", "leaflet.css"}, //
            {"libs/leaflet.js", "leaflet.js"}, //
            {"libs/hylib.js", "hylib.js"}, //
            {"tree_profiles_scatterchart.js", "tree_profiles_scatterchart.js"}, //

            // images
            {"libs/images/layers.png", "images/layers.png"},//
            {"libs/images/layers-2x.png", "images/layers-2x.png"},//
    };

    private static List<String> toRequireList = new ArrayList<String>();
    private static List<String> toRequireHtmlList = new ArrayList<String>();

    private static final ResourceLoader resourceLoader = new ResourceLoader(){
        public InputStream getResourceAsStream( String resourceName ) throws IOException {
            return JsResources.class.getClassLoader().getResourceAsStream(resourceName);
        }
    };

    public static void ensureJavaScriptResources() {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();

        for( String[] jsFile : javascriptFiles ) {
            try {
                if (!resourceManager.isRegistered(jsFile[0])) {
                    InputStream resourceAsStream = resourceLoader.getResourceAsStream(jsFile[1]);
                    if (resourceAsStream == null) {
                        throw new NullPointerException();
                    }
                    String registered = register(resourceManager, jsFile[0], resourceAsStream);
                    if (!toRequireList.contains(registered))
                        toRequireList.add(registered);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JavaScriptLoader loader = RWT.getClient().getService(JavaScriptLoader.class);
        for( String toRequireStr : toRequireList ) {
            loader.require(toRequireStr);
        }
    }

    public static String ensureHtmlResources() {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();
        try {
            String fileName = "trees_info.html";
            if (!resourceManager.isRegistered(fileName)) {
                InputStream resourceAsStream = resourceLoader.getResourceAsStream(fileName);
                if (resourceAsStream == null) {
                    throw new NullPointerException();
                }
                String registered = register(resourceManager, fileName, resourceAsStream);
                if (!toRequireHtmlList.contains(registered))
                    toRequireHtmlList.add(registered);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        JavaScriptLoader loader = RWT.getClient().getService(JavaScriptLoader.class);
        for( String toRequireStr : toRequireList ) {
            loader.require(toRequireStr);
        }
        return toRequireHtmlList.get(0);
    }

    public static String registerIfMissing( String resource ) {
        ResourceManager resourceManager = RWT.getApplicationContext().getResourceManager();
        try {
            // load html
            String location;
            if (!resourceManager.isRegistered(resource)) {
                InputStream resourceAsStream = resourceLoader.getResourceAsStream(resource);
                if (resourceAsStream == null) {
                    throw new NullPointerException();
                }
                location = register(resourceManager, resource, resourceAsStream);
            } else {
                location = resourceManager.getLocation(resource);
            }
            JavaScriptLoader loader = RWT.getClient().getService(JavaScriptLoader.class);
            loader.require(resource);
            return location;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String register( ResourceManager resourceManager, String registerPath, InputStream inputStream )
            throws IOException {
        String location;
        try {
            resourceManager.register(registerPath, inputStream);
            location = resourceManager.getLocation(registerPath);
        } finally {
            inputStream.close();
        }
        return location;
    }

}
