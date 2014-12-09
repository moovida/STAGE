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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabItem;
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
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.gears.utils.images.ImageGenerator;

import com.vividsolutions.jts.geom.Envelope;

import eu.hydrologis.rap.stage.utilsrap.DownloadUtils;
import eu.hydrologis.rap.stage.utilsrap.FileSelectionDialog;
import eu.hydrologis.rap.stage.workspace.StageWorkspace;
import eu.hydrologis.rap.stage.workspace.User;

/**
 * The stage viewer view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class StageSimpleViewerView {

    private final static String SERVICE_HANDLER = "imageServiceHandler";
    private final static String IMAGE_KEY = "imageKey";
    private Browser browser;

    public void createStageSimpleViewerTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(1, true));

        Group fileSelectionGroup = new Group(mainComposite, SWT.NONE);
        fileSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileSelectionGroup.setLayout(new GridLayout(2, false));
        fileSelectionGroup.setText("Simple Viewer File Selection");

        Button button = new Button(fileSelectionGroup, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
        button.setText("Select Map File");
        // button.setToolTipText("Download a file");
        final Shell parentShell = fileSelectionGroup.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                    FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, userFolder, null, null);
                    int returnCode = fileDialog.open();
                    if (returnCode == SWT.CANCEL) {
                        return;
                    }

                    File selectedFile = fileDialog.getSelectedFile();
                    if (selectedFile != null && selectedFile.exists() && !selectedFile.isDirectory()) {
                        setImage(browser, selectedFile);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        Group viewerGroup = new Group(mainComposite, SWT.NONE);
        viewerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewerGroup.setLayout(new GridLayout(1, false));
        viewerGroup.setText("Viewer");

        // register the service handler
        try {
            RWT.getServiceManager().registerServiceHandler(SERVICE_HANDLER, new ImageServiceHandler());
        } catch (Exception e1) {
            // TODO
        }
        browser = new Browser(viewerGroup, SWT.NONE);;
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        stageTab.setControl(mainComposite);
    }

    private void setImage( Browser browser, File imageFile ) throws Exception {
        // create the image
        BufferedImage image = createImage(imageFile);
        // store the image in the UISession for the service handler
        RWT.getUISession().setAttribute(IMAGE_KEY, image);
        // create the HTML with a single <img> tag.
        browser.setText(createHtml(IMAGE_KEY));
    }

    private BufferedImage createImage( File imageFile ) throws Exception {
        String name = imageFile.getName();

        final ImageGenerator imgGen = new ImageGenerator(null);
        if (name.endsWith("asc") || name.endsWith("tiff")) {
            imgGen.addCoveragePath(imageFile.getAbsolutePath());
        } else {
            imgGen.addFeaturePath(imageFile.getAbsolutePath(), null);
        }
        ReferencedEnvelope maxEnv = imgGen.setLayers();
        BufferedImage image = imgGen.getImageWithCheck(maxEnv, 800, 600, 0.0, null);
        return image;
    }

    private String createHtml( String key ) {
        StringBuffer html = new StringBuffer();
        html.append("<img src=\"");
        html.append(createImageUrl(key));
        html.append("\"/>");
        return html.toString();
    }

    private String createImageUrl( String key ) {
        StringBuffer url = new StringBuffer();
        url.append(RWT.getServiceManager().getServiceHandlerUrl(SERVICE_HANDLER));
        url.append("&imageId=");
        url.append(key);
        url.append("&nocache=");
        url.append(System.currentTimeMillis());
        return url.toString();
    }

}
