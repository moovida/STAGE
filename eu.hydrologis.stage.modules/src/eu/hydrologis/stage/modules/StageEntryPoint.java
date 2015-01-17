/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import eu.hydrologis.stage.libs.utilsrap.LoginDialog;
import eu.hydrologis.stage.modules.ui.StageView;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class StageEntryPoint extends AbstractEntryPoint {

    @Override
    protected void createContents( final Composite parent ) {
        // Login screen
        if (!LoginDialog.checkUserLogin(getShell())) {
            Label errorLabel = new Label(parent, SWT.NONE);
            errorLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
            errorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
            String errorMessage = "No permission to proceed.<br/>Please check your password or contact your administrator.";
            errorLabel.setText("<span style='font:bold 24px Arial;'>" + errorMessage + "</span>");
            return;
        }

        try {
            StageView view = new StageView();
            view.createPartControl(Display.getCurrent(), parent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // File file = new File(".");
        // System.out.println(file.getAbsolutePath());

        // parent.setLayout(ExampleUtil.createMainLayout(3));
        // Control controlsColumn = createControlsColumn(parent);
        // controlsColumn.setLayoutData(ExampleUtil.createFillData());
        // Control serverColumn = createLogColumn(parent);
        // serverColumn.setLayoutData(ExampleUtil.createFillData());
        // Control infoColumn = createInfoColumn(parent);
        // infoColumn.setLayoutData(ExampleUtil.createFillData());
    }

    // private Control createLogColumn( Composite parent ) {
    // Composite column = new Composite(parent, SWT.NONE);
    // column.setLayout(ExampleUtil.createGridLayout(1, false, true, true));
    // ExampleUtil.createHeading(column, "Server log", 2);
    // logText = new Text(column, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.BORDER);
    // logText.setText(INITIAL_TEXT);
    // logText.setLayoutData(ExampleUtil.createFillData());
    //
    // File f = new File(".");
    // File[] listFiles = f.listFiles();
    // StringBuilder sb = new StringBuilder();
    // for( File file : listFiles ) {
    // sb.append(file.getName()).append("\n");
    // }
    // logText.setText(sb.toString());
    //
    // createClearButton(column);
    // return column;
    // }
    //
    // private static Control createInfoColumn( Composite parent ) {
    // Label label = new Label(parent, SWT.NONE);
    // label.setLayoutData(ExampleUtil.createFillData());
    // return label;
    // }

}
