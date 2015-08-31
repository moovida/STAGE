/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Text;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.gears.libs.modules.JGTProcessingRegion;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utilsrap.FileSelectionDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;
import eu.hydrologis.stage.modules.SpatialToolboxSessionPluginSingleton;
import eu.hydrologis.stage.modules.widgets.EsriAsciiProcessingRegion;

/**
 * The stage file management view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class SpatialToolboxProcessingRegionView {

    private Text nText;
    private Text sText;
    private Text wText;
    private Text eText;
    private Text xresText;
    private Text yresText;
    private Text colsText;
    private Text rowsText;

    public void createStageProcessingRegionTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        mainComposite.setLayout(new GridLayout(2, false));

        JGTProcessingRegion processingRegion = SpatialToolboxSessionPluginSingleton.getInstance().getProcessingRegion();

        Composite leftComposite = new Composite(mainComposite, SWT.NONE);
        leftComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        leftComposite.setLayout(new GridLayout(2, false));

        final Button enableButton = new Button(leftComposite, SWT.CHECK);
        GridData enableButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        enableButtonGD.horizontalSpan = 2;
        enableButton.setLayoutData(enableButtonGD);
        enableButton.setText("Enable processing region for modules");
        enableButton.setSelection(!SpatialToolboxSessionPluginSingleton.getInstance().doIgnoreProcessingRegion());
        enableButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                SpatialToolboxSessionPluginSingleton.getInstance().setDoIgnoreProcessingRegion(!enableButton.getSelection());
            }
        });

        Label nlabel = new Label(leftComposite, SWT.NONE);
        nlabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        nlabel.setText("North");
        nText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        nText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nText.setText("");

        Label slabel = new Label(leftComposite, SWT.NONE);
        slabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        slabel.setText("South");
        sText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        sText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        sText.setText("");

        Label wlabel = new Label(leftComposite, SWT.NONE);
        wlabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        wlabel.setText("West");
        wText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        wText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        wText.setText("");

        Label elabel = new Label(leftComposite, SWT.NONE);
        elabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        elabel.setText("East");
        eText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        eText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        eText.setText("");

        Label xresLabel = new Label(leftComposite, SWT.NONE);
        xresLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        xresLabel.setText("X res");
        xresText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        xresText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        xresText.setText("");

        Label yresLabel = new Label(leftComposite, SWT.NONE);
        yresLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        yresLabel.setText("Y res");
        yresText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        yresText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        yresText.setText("");

        Label colsLabel = new Label(leftComposite, SWT.NONE);
        colsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        colsLabel.setText("Cols");
        colsText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        colsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        colsText.setText("");

        Label rowsLabel = new Label(leftComposite, SWT.NONE);
        rowsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        rowsLabel.setText("Rows");
        rowsText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        rowsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        rowsText.setText("");

        Button recalculateButton = new Button(leftComposite, SWT.PUSH);
        GridData recalculateButtonGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        recalculateButtonGD.horizontalSpan = 2;
        recalculateButton.setLayoutData(recalculateButtonGD);
        recalculateButton.setText("set region");
        recalculateButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                setRegion();
            }
        });

        if (processingRegion != null) {
            setTextRegionValues(processingRegion);
        }

        // RIGHT COMPOSITE
        Composite rightComposite = new Composite(mainComposite, SWT.NONE);
        rightComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        rightComposite.setLayout(new GridLayout(1, false));

        Group fileGroup = new Group(rightComposite, SWT.NONE);
        fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        fileGroup.setLayout(new GridLayout(2, false));
        fileGroup.setText("Set from file");

        final Button browseButton = new Button(fileGroup, SWT.PUSH);
        browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        browseButton.setText("...");
        browseButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                File dataFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                FileSelectionDialog fileDialog = new FileSelectionDialog(browseButton.getShell(), false, dataFolder, null,
                        new String[]{"asc", "shp"}, null);
                int returnCode = fileDialog.open();
                if (returnCode == SWT.CANCEL) {
                    return;
                }
                File selectedFile = fileDialog.getSelectedFile();
                String name = selectedFile.getName();
                try {
                    if (name.toLowerCase().endsWith("asc")) {
                        EsriAsciiProcessingRegion ascReader = new EsriAsciiProcessingRegion(selectedFile);
                        double[] wesnXYCR = ascReader.getRegionInfo();
                        double n = wesnXYCR[3];
                        double s = wesnXYCR[2];
                        double w = wesnXYCR[0];
                        double es = wesnXYCR[1];
                        double xres = wesnXYCR[4];
                        double yres = wesnXYCR[5];
                        JGTProcessingRegion p = new JGTProcessingRegion(w, es, s, n, xres, yres);
                        setTextRegionValues(p);
                    } else if (name.toLowerCase().endsWith("shp")) {
                        FileDataStore store = FileDataStoreFinder.getDataStore(selectedFile);
                        SimpleFeatureSource featureSource = store.getFeatureSource();
                        SimpleFeatureCollection fc = featureSource.getFeatures();
                        ReferencedEnvelope bounds = fc.getBounds();
                        double n = bounds.getMaxY();
                        double s = bounds.getMinY();
                        double w = bounds.getMinX();
                        double es = bounds.getMaxX();
                        double xres = 1;
                        double yres = 1;
                        JGTProcessingRegion p = new JGTProcessingRegion(w, es, s, n, xres, yres);
                        setTextRegionValues(p);
                    }
                } catch (IOException e1) {
                    StageLogger.logError(this, "Error reading file: " + selectedFile, e1);
                    MessageDialog.openError(nText.getShell(), "ERROR", "Could not read region info from file: " + selectedFile);
                }
            }
        });

        stageTab.setControl(mainComposite);
    }
    private void setTextRegionValues( JGTProcessingRegion processingRegion ) {
        nText.setText("" + processingRegion.getNorth());
        sText.setText("" + processingRegion.getSouth());
        wText.setText("" + processingRegion.getWest());
        eText.setText("" + processingRegion.getEast());
        xresText.setText("" + processingRegion.getWEResolution());
        yresText.setText("" + processingRegion.getNSResolution());
        colsText.setText("" + processingRegion.getCols());
        rowsText.setText("" + processingRegion.getRows());
    }

    protected void setRegion() {
        String nStr = nText.getText();
        String sStr = sText.getText();
        String wStr = wText.getText();
        String eStr = eText.getText();
        String xresStr = xresText.getText();
        String yresStr = yresText.getText();
        String colsStr = colsText.getText();
        String rowsStr = rowsText.getText();

        if (hasEmptyOrWrong(nStr, sStr, wStr, eStr, xresStr, yresStr, colsStr, rowsStr)) {
            MessageDialog.openWarning(nText.getShell(), "WARNING",
                    "All fields need to be filled with numbers in order to set the processing region.");
        }

        double n = Double.parseDouble(nStr);
        double s = Double.parseDouble(sStr);
        double w = Double.parseDouble(wStr);
        double e = Double.parseDouble(eStr);
        double xres = Double.parseDouble(xresStr);
        double yres = Double.parseDouble(yresStr);
        // int cols = (int) Double.parseDouble(colsStr);
        // int rows = (int) Double.parseDouble(rowsStr);

        JGTProcessingRegion region = new JGTProcessingRegion(w, e, s, n, xres, yres);
        setTextRegionValues(region);

        SpatialToolboxSessionPluginSingleton.getInstance().setProcessingRegion(region);

        MessageDialog.openInformation(nText.getShell(), "INFO", "Processing region set to:\n" + region.toString());
    }

    private boolean hasEmptyOrWrong( String... strings ) {
        for( String string : strings ) {
            if (string.trim().length() == 0) {
                return true;
            }
            try {
                Double.parseDouble(string);
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }
}
