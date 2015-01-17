/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.rap.stage.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
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
import org.eclipse.swt.widgets.Text;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.gears.utils.images.ImageGenerator;

import eu.hydrologis.rap.stage.utils.FileUtilities;
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

    private static final int DEFAULT_HEIGHT = 600;
    private static final int DEFAULT_WIDTH = 800;
    private final static String SERVICE_HANDLER = "imageServiceHandler";
    private final static String IMAGE_KEY = "imageKey";
    private Browser browser;
    private File[] selectedFiles = new File[5];
    private Text heightText;
    private Text widthText;

    public void createStageSimpleViewerTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(5, true));

        Group fileSelectionGroup = new Group(mainComposite, SWT.NONE);
        fileSelectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileSelectionGroup.setLayout(new GridLayout(2, false));
        fileSelectionGroup.setText("Parameters");

        Label widthLabel = new Label(fileSelectionGroup, SWT.NONE);
        widthLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        widthLabel.setText("Image width");
        widthText = new Text(fileSelectionGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        widthText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        widthText.setText("" + DEFAULT_WIDTH);

        Label heightLabel = new Label(fileSelectionGroup, SWT.NONE);
        heightLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        heightLabel.setText("Image height");
        heightText = new Text(fileSelectionGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        heightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        heightText.setText("" + DEFAULT_HEIGHT);

        new Label(fileSelectionGroup, SWT.NONE);
        new Label(fileSelectionGroup, SWT.NONE);

        createFileSelectionButtons(fileSelectionGroup);

        Group viewerGroup = new Group(mainComposite, SWT.NONE);
        GridData viewerGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewerGroupGD.horizontalSpan = 4;
        viewerGroup.setLayoutData(viewerGroupGD);
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

    private void createFileSelectionButtons( Group fileSelectionGroup ) {
        for( int i = 0; i < selectedFiles.length; i++ ) {
            Button button = new Button(fileSelectionGroup, SWT.PUSH);
            button.setLayoutData(new GridData(SWT.LEAD, SWT.FILL, false, false));
            button.setText("Map File " + (i + 1));

            final Label label = new Label(fileSelectionGroup, SWT.NONE);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            label.setText("");

            final int index = i;
            final Shell parentShell = fileSelectionGroup.getShell();
            button.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    try {
                        File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                        FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, userFolder, null, null);
                        int returnCode = fileDialog.open();
                        if (returnCode == SWT.CANCEL) {
                            selectedFiles[index] = null;
                            label.setText("");
                            return;
                        }

                        selectedFiles[index] = fileDialog.getSelectedFile();
                        label.setText(selectedFiles[index].getName());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });

        }
        new Label(fileSelectionGroup, SWT.NONE);
        new Label(fileSelectionGroup, SWT.NONE);
        Button drawButton = new Button(fileSelectionGroup, SWT.PUSH);
        drawButton.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
        drawButton.setText("Draw map");
        drawButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    setImage(browser, selectedFiles);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    private void setImage( Browser browser, File[] imageFiles ) throws Exception {
        // create the image
        BufferedImage image = createImage(imageFiles);
        // store the image in the UISession for the service handler
        RWT.getUISession().setAttribute(IMAGE_KEY, image);
        // create the HTML with a single <img> tag.
        browser.setText(createHtml(IMAGE_KEY));
    }

    private BufferedImage createImage( File[] imageFiles ) throws Exception {
        final ImageGenerator imgGen = new ImageGenerator(null);
        for( File imageFile : imageFiles ) {
            if (imageFile == null)
                continue;
            String name = imageFile.getName().toLowerCase();
            if (FileUtilities.isSupportedRaster(name)) {
                imgGen.addCoveragePath(imageFile.getAbsolutePath());
            }
            if (FileUtilities.isSupportedVector(name)) {
                imgGen.addFeaturePath(imageFile.getAbsolutePath(), null);
            }
        }
        ReferencedEnvelope maxEnv = imgGen.setLayers();

        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        try {
            int w = Integer.parseInt(widthText.getText());
            int h = Integer.parseInt(heightText.getText());
            width = w;
            height = h;
        } catch (Exception e) {
        }

        BufferedImage image = imgGen.getImageWithCheck(maxEnv, width, height, 0.0, null);
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
