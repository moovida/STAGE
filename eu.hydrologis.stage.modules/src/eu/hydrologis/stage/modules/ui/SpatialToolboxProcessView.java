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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import eu.hydrologis.stage.modules.SpatialToolboxSessionPluginSingleton;

/**
 * The stage file management view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class SpatialToolboxProcessView {

    private HashMap<String, Process> runningProcessesMap;
    private List<Button> buttonsList = new ArrayList<Button>();
    private Group processGroup;

    public void createStageProcessesTab( Composite parent, CTabItem stageTab ) throws IOException {

        runningProcessesMap = SpatialToolboxSessionPluginSingleton.getInstance().getRunningProcessesMap();
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(3, true));

        createControlsColumn(mainComposite);

        stageTab.setControl(mainComposite);
    }

    private Control createControlsColumn( Composite parent ) {

        new Label(parent, SWT.NONE);

        Button refreshButton = new Button(parent, SWT.PUSH);
        refreshButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        refreshButton.setText("Refresh process list");
        refreshButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                refreshProcessList();
            }
        });

        new Label(parent, SWT.NONE);
        new Label(parent, SWT.NONE);

        processGroup = new Group(parent, SWT.NONE);
        processGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        processGroup.setLayout(new GridLayout(1, true));
        processGroup.setText("Push button to kill process");

        refreshProcessList();

        return parent;
    }

    private void refreshProcessList() {
        for( Button button : buttonsList ) {
            button.dispose();
        }
        buttonsList.clear();
        for( String id : runningProcessesMap.keySet() ) {
            final Button killButton = new Button(processGroup, SWT.PUSH);
            killButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            killButton.setText(id);
            killButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    SpatialToolboxSessionPluginSingleton.getInstance().killProcess(killButton.getText());
                    killButton.dispose();
                }
            });
            buttonsList.add(killButton);
        }
    }
}
