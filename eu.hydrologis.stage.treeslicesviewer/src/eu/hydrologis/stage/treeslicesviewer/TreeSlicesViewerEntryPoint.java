/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.treeslicesviewer;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utilsrap.LoginDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TreeSlicesViewerEntryPoint extends AbstractEntryPoint implements TreeSlicerConstants {
    private static final String EMPTY = " - ";
    private File selectedFile = null;
    private Shell parentShell;
    private Combo plotCombo;
    private boolean onlyDiameterMajor17 = true;
    private CLabel informationText;
    private String plotHtmlResource;
    private double minHeight;
    private double minDiam;
    private double maxHeight;
    private double maxDiam;
    private JSONObject plotObjectTransfer;
    private HashMap<String, JSONObject> id2TreeJsonMap = new HashMap<String, JSONObject>();
    private Browser mapBrowser;

    private DecimalFormat radiusFormatter = new DecimalFormat("0.0");
    private DecimalFormat llFormatter = new DecimalFormat("0.000000");

    @SuppressWarnings("serial")
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

        JsResources.ensureJavaScriptResources();
        plotHtmlResource = JsResources.ensurePlotHtmlResource();

        parentShell = parent.getShell();

        Composite mainComposite = new Composite(parent, SWT.None);
        GridLayout mainLayout = new GridLayout(8, true);
        mainComposite.setLayout(mainLayout);
        mainLayout.marginBottom = 30;
        mainLayout.marginTop = 30;

        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite leftComposite = new Composite(mainComposite, SWT.NONE);
        GridData leftCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        leftCompositeGD.horizontalSpan = 2;
        leftComposite.setLayoutData(leftCompositeGD);
        leftComposite.setLayout(new GridLayout(3, false));

        Composite rightComposite = new Composite(mainComposite, SWT.NONE);
        GridData rightCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        rightCompositeGD.horizontalSpan = 6;
        rightComposite.setLayoutData(rightCompositeGD);
        rightComposite.setLayout(new GridLayout(2, true));

        Label datasetTitleLabel = new Label(leftComposite, SWT.NONE);
        datasetTitleLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        datasetTitleLabel.setText("Dataset: ");
        final Text datasetNameText = new Text(leftComposite, SWT.BORDER);
        datasetNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        datasetNameText.setText("");
        datasetNameText.setEditable(false);
        Button datasetButton = new Button(leftComposite, SWT.PUSH);
        datasetButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        datasetButton.setText("...");
        datasetButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                    TreeFolderSelectionDialog fileDialog = new TreeFolderSelectionDialog(parentShell, userFolder);
                    int returnCode = fileDialog.open();
                    if (returnCode == SWT.CANCEL) {
                        selectedFile = null;
                        datasetNameText.setText("");
                        return;
                    }

                    selectedFile = fileDialog.getSelectedFile();
                    if (selectedFile != null)
                        datasetNameText.setText(selectedFile.getName());

                    File[] plotFiles = selectedFile.listFiles(new FilenameFilter(){
                        @Override
                        public boolean accept( File dir, String name ) {
                            return name.endsWith(".json");
                        }
                    });
                    Arrays.sort(plotFiles);

                    String[] names = new String[plotFiles.length + 1];
                    names[0] = EMPTY;
                    for( int i = 0; i < plotFiles.length; i++ ) {
                        names[i + 1] = FileUtilities.getNameWithoutExtention(plotFiles[i]);
                    }
                    plotCombo.setItems(names);
                    plotCombo.select(0);

                    clearPlotSelection();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        Label plotLabel = new Label(leftComposite, SWT.NONE);
        plotLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        plotLabel.setText("Plot: ");

        plotCombo = new Combo(leftComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData plotComboGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        plotComboGD.horizontalSpan = 2;
        plotCombo.setLayoutData(plotComboGD);
        plotCombo.setItems(new String[0]);
        plotCombo.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String selectedText = plotCombo.getItem(plotCombo.getSelectionIndex());
                if (selectedText.equals(EMPTY)) {
                    clearPlotSelection();
                } else {
                    selectPlot(selectedText);
                }
            }
        });

        final Button onlyDiametersCheckButton = new Button(leftComposite, SWT.CHECK);
        GridData onlyDiametersCheckButtonGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        onlyDiametersCheckButtonGD.horizontalSpan = 3;
        onlyDiametersCheckButton.setLayoutData(onlyDiametersCheckButtonGD);
        onlyDiametersCheckButton.setText("Only diameters > 17.5cm");
        onlyDiametersCheckButton.setSelection(true);
        onlyDiametersCheckButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                onlyDiameterMajor17 = onlyDiametersCheckButton.getSelection();
                String tf = onlyDiameterMajor17 ? "true" : "false";
                mapBrowser.evaluate("setMajor175(" + tf + ")");
            }
        });

        addMapBrowser(leftComposite);

        Group infoGroup = new Group(leftComposite, SWT.NONE);
        GridData infoGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        infoGroupGD.horizontalSpan = 3;
        infoGroup.setLayoutData(infoGroupGD);
        infoGroup.setLayout(new GridLayout(1, false));

        informationText = new CLabel(infoGroup, SWT.NONE);
        GridData informationTextGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        informationText.setLayoutData(informationTextGD);
        informationText.setText("");
        informationText.setMargins(5, 5, 5, 5);
        informationText.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        Group snGroup = new Group(rightComposite, SWT.NONE);
        snGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        snGroup.setLayout(new GridLayout(1, false));
        snGroup.setText("S->N");

        Group swNeGroup = new Group(rightComposite, SWT.NONE);
        swNeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        swNeGroup.setLayout(new GridLayout(1, false));
        swNeGroup.setText("SW->NE");

        Group weGroup = new Group(rightComposite, SWT.NONE);
        weGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        weGroup.setLayout(new GridLayout(1, false));
        weGroup.setText("W->E");

        Group wnEsGroup = new Group(rightComposite, SWT.NONE);
        wnEsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        wnEsGroup.setLayout(new GridLayout(1, false));
        wnEsGroup.setText("WN->ES");

    }

    @SuppressWarnings("serial")
    private void addMapBrowser( Composite leftComposite ) {
        mapBrowser = new Browser(leftComposite, SWT.NONE | SWT.BORDER);
        GridData mapBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mapBrowserGD.horizontalSpan = 3;
        mapBrowser.setLayoutData(mapBrowserGD);
        mapBrowser.setUrl(plotHtmlResource);
        mapBrowser.addProgressListener(new ProgressListener(){
            public void completed( ProgressEvent event ) {
                new BrowserFunction(mapBrowser, "getPlotData"){
                    @Override
                    public Object function( Object[] arguments ) {
                        if (plotObjectTransfer != null) {
                            return plotObjectTransfer.toString();
                        }
                        return null;
                    }
                };
                new BrowserFunction(mapBrowser, "getTreedataById"){
                    @Override
                    public Object function( Object[] arguments ) {
                        String treeId = arguments[0].toString();
                        JSONObject treeObject = id2TreeJsonMap.get(treeId);
                        if (treeObject != null) {
                            double treeHeight = treeObject.getDouble(JSON_TREE_HEIGHT);
                            double lat = treeObject.getDouble(JSON_TREE_LAT);
                            double lon = treeObject.getDouble(JSON_TREE_LON);

                            StringBuilder plotInfoSB = new StringBuilder();
                            boolean hasDiam = treeObject.has(JSON_TREE_DIAM);
                            boolean hasMatched = treeObject.has(JSON_TREE_ID_MATCHED);
                            boolean isTp = hasDiam && hasMatched;
                            boolean isFn = hasDiam && !hasMatched;
                            boolean isFp = !hasDiam;
                            if (isTp) {
                                plotInfoSB.append("<b>TRUE POSITIVE</b><br/>\n");
                            } else if (isFn) {
                                plotInfoSB.append("<b>FALSE NEGATIVE</b><br/>\n");
                            } else if (isFp) {
                                plotInfoSB.append("<b>FALSE POSITIVE</b><br/>\n");
                            }
                            if (isTp || isFn) {
                                double diam = treeObject.getDouble(JSON_TREE_DIAM);
                                plotInfoSB.append("<br/><b>MAPPED TREE INFORMATION</b><br/>\n");
                                plotInfoSB.append("id = " + treeId + "<br/>");
                                plotInfoSB.append("height [m] = " + radiusFormatter.format(treeHeight) + "<br/>");
                                plotInfoSB.append("diameter [cm] = " + radiusFormatter.format(diam) + "<br/>");
                                plotInfoSB.append("lat  = " + llFormatter.format(lat) + "<br/>");
                                plotInfoSB.append("lon  = " + llFormatter.format(lon) + "<br/>");
                            }
                            if (isTp) {
                                double matchedTreeHeight = treeObject.getDouble(JSON_TREE_HEIGHT_MATCHED);
                                String matchedTreeId = treeObject.getString(JSON_TREE_ID_MATCHED);

                                plotInfoSB.append("<br/><b>EXTRACTED TREE INFORMATION</b><br/>\n");
                                plotInfoSB.append("id = " + matchedTreeId + "<br/>");
                                plotInfoSB.append("height [m] = " + radiusFormatter.format(matchedTreeHeight) + "<br/>");
                            }
                            if (isFp) {
                                plotInfoSB.append("<br/><b>EXTRACTED TREE INFORMATION</b><br/>\n");
                                plotInfoSB.append("id = " + treeId + "<br/>");
                                plotInfoSB.append("height [m] = " + radiusFormatter.format(treeHeight) + "<br/>");
                            }

                            informationText.setText(plotInfoSB.toString());

                            String treeJson = treeObject.toString();
                            return treeJson;
                        }
                        return null;
                    }
                };
                new BrowserFunction(mapBrowser, "getMinHeight"){
                    @Override
                    public Object function( Object[] arguments ) {
                        return minHeight;
                    }
                };
                new BrowserFunction(mapBrowser, "getMaxHeight"){
                    @Override
                    public Object function( Object[] arguments ) {
                        return maxHeight;
                    }
                };
                new BrowserFunction(mapBrowser, "getMinDiam"){
                    @Override
                    public Object function( Object[] arguments ) {
                        return minDiam;
                    }
                };
                new BrowserFunction(mapBrowser, "getMaxDiam"){
                    @Override
                    public Object function( Object[] arguments ) {
                        return maxDiam;
                    }
                };

                Rectangle clientArea = mapBrowser.getClientArea();
                int w = clientArea.width;
                int h = clientArea.height;
                mapBrowser.evaluate("createMap(" + w + ", " + h + ")");
            }
            public void changed( ProgressEvent event ) {
            }
        });

    }

    protected void selectPlot( String selectedText ) {
        File plotFile = new File(selectedFile, selectedText + ".json");

        id2TreeJsonMap.clear();
        if (plotFile.exists()) {
            plotObjectTransfer = new JSONObject();

            try {
                String fileJson = FileUtilities.readFile(plotFile);
                JSONObject plotObject = new JSONObject(fileJson);
                JSONObject plotData = plotObject.getJSONObject(JSON_PLOT_DATA);
                JSONArray plotsArray = plotData.getJSONArray(JSON_PLOTS);
                JSONObject selectedJsonPlot = plotsArray.getJSONObject(0);

                String id = selectedJsonPlot.getString(JSON_PLOT_ID);
                double lon = selectedJsonPlot.getDouble(JSON_PLOT_CENTERLON);
                double lat = selectedJsonPlot.getDouble(JSON_PLOT_CENTERLAT);
                double radius = selectedJsonPlot.getDouble(JSON_PLOT_RADIUS);
                double radiusLL = selectedJsonPlot.getDouble(JSON_PLOT_RADIUSDEG);

                plotObjectTransfer.put(JSON_PLOT_ID, id);
                plotObjectTransfer.put(JSON_PLOT_CENTERLAT, lat);
                plotObjectTransfer.put(JSON_PLOT_CENTERLON, lon);
                plotObjectTransfer.put(JSON_PLOT_RADIUSDEG, radiusLL);

                StringBuilder plotInfoSB = new StringBuilder();
                plotInfoSB.append("<b>PLOT INFORMATION</b><br/>\n");
                plotInfoSB.append("id = " + id + "<br/>");
                plotInfoSB.append("lon = " + llFormatter.format(lon) + "<br/>");
                plotInfoSB.append("lat = " + llFormatter.format(lat) + "<br/>");
                plotInfoSB.append("radius = " + radiusFormatter.format(radius) + " m<br/>");

                int fpCount = 0;
                int fnCount = 0;
                int matchedCount = 0;

                minHeight = Double.POSITIVE_INFINITY;
                minDiam = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
                maxDiam = Double.NEGATIVE_INFINITY;

                JSONArray treesArrayTransfer = new JSONArray();
                int index = 0;
                plotObjectTransfer.put(JSON_PLOT_TREES, treesArrayTransfer);

                JSONArray treesArray = selectedJsonPlot.getJSONArray(JSON_PLOT_TREES);
                for( int i = 0; i < treesArray.length(); i++ ) {
                    JSONObject treeObject = treesArray.getJSONObject(i);
                    String treeId = treeObject.getString(JSON_TREE_ID);

                    id2TreeJsonMap.put(treeId, treeObject);

                    double treeHeight = treeObject.getDouble(JSON_TREE_HEIGHT);
                    minHeight = min(minHeight, treeHeight);
                    maxHeight = max(maxHeight, treeHeight);

                    boolean hasDiam = false;
                    boolean hasMatch = false;
                    double diamLL = 0;
                    double diam = 0;
                    if (treeObject.has(JSON_TREE_DIAM)) {
                        hasDiam = true;
                        diam = treeObject.getDouble(JSON_TREE_DIAM);
                        diamLL = treeObject.getDouble(JSON_TREE_DIAMDEG);
                        minDiam = min(minDiam, diamLL);
                        maxDiam = max(maxDiam, diamLL);
                    }
                    if (treeObject.has(JSON_TREE_ID_MATCHED)) {
                        matchedCount++;
                        hasMatch = true;
                    }
                    if (!hasDiam) {
                        fpCount++;
                    } else {
                        if (!hasMatch) {
                            fnCount++;
                        }
                    }

                    JSONObject treeObjectTransfer = new JSONObject();
                    treesArrayTransfer.put(index++, treeObjectTransfer);
                    treeObjectTransfer.put(JSON_TREE_ID, treeId);
                    treeObjectTransfer.put(JSON_TREE_LAT, treeObject.getDouble(JSON_TREE_LAT));
                    treeObjectTransfer.put(JSON_TREE_LON, treeObject.getDouble(JSON_TREE_LON));
                    treeObjectTransfer.put(JSON_TREE_HEIGHT, treeObject.getDouble(JSON_TREE_HEIGHT));
                    if (hasDiam) {
                        treeObjectTransfer.put(JSON_TREE_DIAMDEG, diamLL);
                        treeObjectTransfer.put(JSON_TREE_DIAM, diam);
                    }
                    if (hasMatch) {
                        treeObjectTransfer.put(JSON_TREE_ID_MATCHED, treeObject.getString(JSON_TREE_ID_MATCHED));
                    }
                }

                plotInfoSB.append("<br/><b>Matching:</b><br/>\n");
                plotInfoSB.append("true positives = " + matchedCount + "<br/>");
                plotInfoSB.append("false negatives = " + fnCount + "<br/>");
                plotInfoSB.append("false positives = " + fpCount + "<br/>");

                informationText.setText(plotInfoSB.toString());

                mapBrowser.evaluate("updateData()");
            } catch (IOException e) {
                StageLogger.logError(this, "Error reading the plot data.", e);
            }
        } else {
            plotObjectTransfer = null;
        }

    }

    // private void getTreeInfo(){
    // String treeId = treeObject.getString(JSON_TREE_ID);
    // double treeLon = treeObject.getDouble(JSON_TREE_LON);
    // double treeLat = treeObject.getDouble(JSON_TREE_LAT);
    // double treeHeight = treeObject.getDouble(JSON_TREE_HEIGHT);
    //
    // if (treeObject.has(JSON_TREE_DIAM)) {
    // double diam = treeObject.getDouble(JSON_TREE_DIAM);
    // double diamLL = treeObject.getDouble(JSON_TREE_DIAMDEG);
    // String type = treeObject.getString(JSON_TREE_TYPE);
    // }
    // if (treeObject.has(JSON_TREE_ID_MATCHED)) {
    // String matchedId = treeObject.getString(JSON_TREE_ID_MATCHED);
    // double matchedLon = treeObject.getDouble(JSON_TREE_LON_MATCHED);
    // double matchedLat = treeObject.getDouble(JSON_TREE_LAT_MATCHED);
    // double matchedHeight = treeObject.getDouble(JSON_TREE_HEIGHT_MATCHED);
    // matchedCount++;
    // }
    // double minHeight = treeObject.getDouble(JSON_TREE_MIN_H);
    // double maxHeight = treeObject.getDouble(JSON_TREE_MAX_H);
    // double minProgressive = treeObject.getDouble(JSON_TREE_MIN_P);
    // double maxProgressive = treeObject.getDouble(JSON_TREE_MAX_P);
    // }

    protected void clearPlotSelection() {
        informationText.setText("");
        mapBrowser.evaluate("clearPlotMap()");
    }
}
