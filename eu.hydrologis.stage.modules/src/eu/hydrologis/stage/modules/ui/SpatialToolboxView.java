/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.ui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * The main stage view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SpatialToolboxView {

    public static final String SPATIAL_TOOLBOX = "Spatial Toolbox...";
    public static final String LOADING_MODULES_FROM_LIBRARIES = "Loading modules from libraries...";

    public static final String ID = "eu.hydrologis.stage.modules.ui.StageView"; //$NON-NLS-1$

    public void createPartControl( Display display, Composite parent ) throws IOException {
        final CTabFolder mainStageCFolder = new CTabFolder(parent, SWT.TOP);
        mainStageCFolder.setUnselectedCloseVisible(false);
        mainStageCFolder.setLayout(new FillLayout());
        mainStageCFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem stageTab = new CTabItem(mainStageCFolder, SWT.NONE);
        stageTab.setText("STAGE");
        mainStageCFolder.setSelection(stageTab);
        SpatialToolboxModulesView stageModulesView = new SpatialToolboxModulesView();
        stageModulesView.createStageModulesTab(display, mainStageCFolder, stageTab);

        CTabItem scriptingTab = new CTabItem(mainStageCFolder, SWT.NONE);
        scriptingTab.setText("Geo-scripting");
        SpatialToolboxScriptingView stageScriptingView = new SpatialToolboxScriptingView();
        stageScriptingView.createStageScriptingTab(display, mainStageCFolder, scriptingTab);

        CTabItem fileManagementTab = new CTabItem(mainStageCFolder, SWT.NONE);
        fileManagementTab.setText("File Management");
        SpatialToolboxFilemanagementView stageFileManagementView = new SpatialToolboxFilemanagementView();
        stageFileManagementView.createStageFileManagementTab(display, mainStageCFolder, fileManagementTab);

        CTabItem viewerTab = new CTabItem(mainStageCFolder, SWT.NONE);
        viewerTab.setText("Simple Viewer");
        SpatialToolboxSimpleViewerView stageViewerView = new SpatialToolboxSimpleViewerView();
        stageViewerView.createStageSimpleViewerTab(display, mainStageCFolder, viewerTab);

        CTabItem processTab = new CTabItem(mainStageCFolder, SWT.NONE);
        processTab.setText("Processes");
        SpatialToolboxProcessView processView = new SpatialToolboxProcessView();
        processView.createStageProcessesTab(mainStageCFolder, processTab);

    }
}
