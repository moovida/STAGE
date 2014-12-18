/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.stage.utils;

import eu.hydrologis.rap.stage.core.ModuleDescription;

/**
 * A modules wrapper.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ViewerModule {
    private final ModuleDescription moduleDescription;

    private ViewerFolder parentFolder;

    public ViewerModule( ModuleDescription moduleDescription ) {
        this.moduleDescription = moduleDescription;
    }
    
    public ModuleDescription getModuleDescription() {
        return moduleDescription;
    }

    public void setParentFolder( ViewerFolder parentFolder ) {
        this.parentFolder = parentFolder;
    }

    public ViewerFolder getParentFolder() {
        return parentFolder;
    }
}
