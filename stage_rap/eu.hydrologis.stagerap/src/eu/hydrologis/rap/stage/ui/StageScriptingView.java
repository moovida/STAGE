/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.hydrologis.rap.stage.ui;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.rap.stage.utils.ImageCache;

/**
 * The stage scripting view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageScriptingView {

    private org.eclipse.swt.widgets.List logList;

    public void createStageScriptingTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(3, true));

        addRunGroup(display, mainComposite);

        addFileGroup(display, mainComposite);

        addTemplatesGroup(display, mainComposite);

        SashForm mainScriptingComposite = new SashForm(mainComposite, SWT.HORIZONTAL);
        GridData mainScriptingCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainScriptingCompositeGD.horizontalSpan = 3;
        mainScriptingComposite.setLayoutData(mainScriptingCompositeGD);

        Composite leftComposite = new Composite(mainScriptingComposite, SWT.None);
        GridLayout leftLayout = new GridLayout(2, true);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        leftComposite.setLayout(leftLayout);
        GridData leftGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        leftComposite.setLayoutData(leftGD);

        Label titleLabel = new Label(leftComposite, SWT.NONE);
        titleLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        titleLabel.setText("Script name");

        Text scriptTitleText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        scriptTitleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        scriptTitleText.setText("");

        Text scriptAreaText = new Text(leftComposite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData scriptAreaTextGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        scriptAreaTextGD.horizontalSpan = 2;
        scriptAreaText.setLayoutData(scriptAreaTextGD);
        scriptAreaText.setText("");

        logList = new org.eclipse.swt.widgets.List(mainScriptingComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        logList.setLayoutData(new GridData(GridData.FILL_BOTH));
        logList.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        mainScriptingComposite.setWeights(new int[]{1, 1});

        stageTab.setControl(mainComposite);
    }

    private void addRunGroup( Display display, Composite mainComposite ) {
        Group execGroup = new Group(mainComposite, SWT.NONE);
        execGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        execGroup.setLayout(new GridLayout(4, true));
        execGroup.setText("Execution");

        Button runModuleButton = new Button(execGroup, SWT.PUSH);
        runModuleButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        // runModuleButton.setText("Run module");
        runModuleButton.setToolTipText("Run module");
        runModuleButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN));
        runModuleButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    // runSelectedModule();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    private void addFileGroup( Display display, Composite mainComposite ) {
        Group fileGroup = new Group(mainComposite, SWT.NONE);
        fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileGroup.setLayout(new GridLayout(4, true));
        fileGroup.setText("File");

        Button openButton = new Button(fileGroup, SWT.PUSH);
        openButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        openButton.setToolTipText("Open existing script");
        openButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.OPEN));
        // openButton.addSelectionListener(new SelectionAdapter(){
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // public void widgetSelected( SelectionEvent e ) {
        // try {
        // runSelectedModule();
        // } catch (Exception e1) {
        // e1.printStackTrace();
        // }
        // }
        // });

        Button saveButton = new Button(fileGroup, SWT.PUSH);
        saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        saveButton.setToolTipText("Save current script");
        saveButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.SAVE));
    }

    private void addTemplatesGroup( Display display, Composite mainComposite ) {
        Group templatesGroup = new Group(mainComposite, SWT.NONE);
        templatesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        templatesGroup.setLayout(new GridLayout(1, true));
        templatesGroup.setText("Templates");

        Combo templatesCombo = new Combo(templatesGroup, SWT.DROP_DOWN);
        templatesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templatesCombo.setItems(new String[]{"template1", "template2"});
    }

}
