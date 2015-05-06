/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.treeslicesviewer;

import java.io.File;
import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.stage.libs.utilsrap.FileSelectionDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

/**
 * The stage viewer view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class TreeSlicesViewerView {

    private File selectedFile = null;

    public void createStageSimpleViewerTab( Display display, Composite parent ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.None);
        GridLayout mainLayout = new GridLayout(1, true);
        mainComposite.setLayout(mainLayout);
        mainLayout.marginBottom = 30;
        mainLayout.marginTop = 30;

        mainComposite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Group fileSelectionGroup = new Group(mainComposite, SWT.NONE);
        fileSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout filesSelLayout = new GridLayout(1, true);
        fileSelectionGroup.setLayout(filesSelLayout);
        filesSelLayout.marginBottom = 30;
        filesSelLayout.marginTop = 30;
        filesSelLayout.marginLeft = 10;
        filesSelLayout.marginRight = 10;
        fileSelectionGroup.setText("Tree Slices Folder Selection");

        selectedFile = null;
        Button button = new Button(fileSelectionGroup, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        button.setText("Select folder");

        Group selectedFileGroup = new Group(fileSelectionGroup, SWT.NONE);
        selectedFileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        selectedFileGroup.setLayout(new GridLayout(1, false));
        selectedFileGroup.setText("Selected Folder");

        final Label label = new Label(selectedFileGroup, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        label.setText("");

        final Shell parentShell = fileSelectionGroup.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                    FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, userFolder, null, null, null);
                    int returnCode = fileDialog.open();
                    if (returnCode == SWT.CANCEL) {
                        selectedFile = null;
                        label.setText("");
                        return;
                    }

                    selectedFile = fileDialog.getSelectedFile();
                    label.setText(selectedFile.getName());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        final Button drawButton = new Button(fileSelectionGroup, SWT.PUSH);
        drawButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        drawButton.setText("View Trees");
        drawButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                TreeSlicerDialog d = new TreeSlicerDialog(drawButton.getShell(), selectedFile);
                d.open();
            }
        });

    }

}
