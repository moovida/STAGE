/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.spatialite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.gears.io.las.spatialite.LasCell;
import org.jgrasstools.gears.io.las.spatialite.LasCellsTable;
import org.jgrasstools.gears.io.las.spatialite.LasLevel;
import org.jgrasstools.gears.io.las.spatialite.LasLevelsTable;
import org.jgrasstools.gears.io.las.spatialite.LasSource;
import org.jgrasstools.gears.io.las.spatialite.LasSourcesTable;
import org.jgrasstools.gears.spatialite.SpatialiteDb;
import org.jgrasstools.gears.spatialite.SpatialiteGeometryColumns;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utils.StageProgressBar;
import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.LoginDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class LidarViewerEntryPoint extends AbstractEntryPoint {

    private static final long serialVersionUID = 1L;

    private Display display;

    private SpatialiteDb currentConnectedDatabase;

    private Shell parentShell;

    private StageProgressBar generalProgressBar;

    private Browser mapBrowser;

    private Text databaseNameText;

    private String lidarMapUrl;

    private LidarViewerProgressListener lidarViewerProgressListener;

    /**
     * 0 elev, 1 intens
     */
    private int dataType = 0;
    /**
     * 0 automatic, 1 overview, 2 points
     */
    private int viewType = 0;

    private Text loadedElementsNumText;

    private CoordinateReferenceSystem databaseCrs;

    private int maxLevel = 0;
    private double maxLevelResolution;
    private double maxLevelFactor;

    private Combo viewTypeCombo;

    private int pointsLimit = 50000;
    private int cellsLimit = 1000;

    private CoordinateReferenceSystem leafletCRS = DefaultGeographicCRS.WGS84;

    private boolean isInfoToolOn;
    private boolean isSourcesView = false;

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

        parentShell = parent.getShell();
        display = parent.getDisplay();

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));

        Composite leftComposite = new Composite(composite, SWT.None);
        GridLayout leftLayout = new GridLayout(3, false);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        leftComposite.setLayout(leftLayout);
        GridData leftGD = new GridData(GridData.FILL, GridData.FILL, false, true);
        leftComposite.setLayoutData(leftGD);

        Label connectedDbLabel = new Label(leftComposite, SWT.NONE);
        connectedDbLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        connectedDbLabel.setText("Database: ");

        databaseNameText = new Text(leftComposite, SWT.BORDER);
        databaseNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        databaseNameText.setText("                          ");
        databaseNameText.setEditable(false);

        Button connectDatabaseButton = new Button(leftComposite, SWT.PUSH);
        // connectDatabaseButton.setText("");
        connectDatabaseButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.CONNECT));
        connectDatabaseButton.setToolTipText("Select database to connect to.");
        connectDatabaseButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                openDatabase(parent.getShell());
            }
        });

        Label viewTypeLabel = new Label(leftComposite, SWT.NONE);
        viewTypeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        viewTypeLabel.setText("Visualization type: ");

        viewTypeCombo = new Combo(leftComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData viewTypeComboGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        viewTypeComboGD.horizontalSpan = 2;
        viewTypeComboGD.widthHint = 200;
        viewTypeCombo.setLayoutData(viewTypeComboGD);
        viewTypeCombo.setItems(new String[]{"automatic", "overview", "points"});
        viewTypeCombo.select(0);
        viewTypeCombo.addSelectionListener(new SelectionListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                parentShell.getDisplay().asyncExec(new Runnable(){
                    public void run() {
                        try {
                            // generalProgressBar.setProgressText("Load data...");
                            // generalProgressBar.start(0);
                            viewType = viewTypeCombo.getSelectionIndex();
                            mapBrowser.evaluate("checkData(false, 1);");
                        } catch (Exception e) {
                            StageLogger.logError(LidarViewerEntryPoint.this, e);
                            // } finally {
                            // generalProgressBar.stop();
                        }
                    }
                });

            }

            public void widgetDefaultSelected( SelectionEvent e ) {
            }
        });

        Label dataTypeLabel = new Label(leftComposite, SWT.NONE);
        dataTypeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        dataTypeLabel.setText("Data type: ");

        final Combo dataTypeCombo = new Combo(leftComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData dataTypeComboGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        dataTypeComboGD.horizontalSpan = 2;
        dataTypeCombo.setLayoutData(dataTypeComboGD);
        dataTypeCombo.setItems(new String[]{"elevation", "intensity"});
        dataTypeCombo.select(0);
        dataTypeCombo.addSelectionListener(new SelectionListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                parentShell.getDisplay().asyncExec(new Runnable(){
                    public void run() {
                        try {
                            // generalProgressBar.setProgressText("Load data...");
                            // generalProgressBar.start(0);
                            dataType = dataTypeCombo.getSelectionIndex();
                            if (dataType == 0) {
                                StageUtils.blockWhileOtherScriptIsBusy(mapBrowser);
                                mapBrowser.evaluate("checkData(false, 1);");
                            } else {
                                StageUtils.blockWhileOtherScriptIsBusy(mapBrowser);
                                mapBrowser.evaluate("checkData(false, 2);");
                            }
                        } catch (Exception e) {
                            StageLogger.logError(LidarViewerEntryPoint.this, e);
                            // } finally {
                            // generalProgressBar.stop();
                        }
                    }
                });

            }

            public void widgetDefaultSelected( SelectionEvent e ) {
            }
        });

        Label pointsLimitLabel = new Label(leftComposite, SWT.NONE);
        pointsLimitLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        pointsLimitLabel.setText("Points limit: ");

        final Text pointsLimitText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData pointsLimitTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        pointsLimitTextGD.horizontalSpan = 2;
        pointsLimitText.setLayoutData(pointsLimitTextGD);
        pointsLimitText.setText("" + pointsLimit);
        pointsLimitText.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText( ModifyEvent event ) {
                String limitStr = pointsLimitText.getText();
                try {
                    pointsLimit = (int) Double.parseDouble(limitStr);
                } catch (NumberFormatException e) {
                    pointsLimit = 50000;
                }
            }
        });

        Label cellsLimitLabel = new Label(leftComposite, SWT.NONE);
        cellsLimitLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        cellsLimitLabel.setText("Cells limit: ");

        final Text cellsLimitText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData cellsLimitTextGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
        cellsLimitTextGD.horizontalSpan = 2;
        cellsLimitText.setLayoutData(cellsLimitTextGD);
        cellsLimitText.setText("" + cellsLimit);
        cellsLimitText.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText( ModifyEvent event ) {
                String limitStr = cellsLimitText.getText();
                try {
                    cellsLimit = (int) Double.parseDouble(limitStr);
                } catch (NumberFormatException e) {
                    cellsLimit = 1000;
                }
            }
        });

        Label loadedElementsLabel = new Label(leftComposite, SWT.NONE);
        loadedElementsLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        loadedElementsLabel.setText("Loaded elements: ");
        loadedElementsNumText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        GridData loadedElementsNumLabelGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        loadedElementsNumLabelGD.horizontalSpan = 2;
        loadedElementsNumText.setLayoutData(loadedElementsNumLabelGD);
        loadedElementsNumText.setText(" - ");
        loadedElementsNumText.setEditable(false);

        Label spacerLabel = new Label(leftComposite, SWT.NONE);
        GridData spacerLabelGD = new GridData(SWT.FILL, SWT.FILL, false, true);
        spacerLabelGD.horizontalSpan = 3;
        spacerLabel.setLayoutData(spacerLabelGD);
        spacerLabel.setText("");

        createButtonsComposite(leftComposite);

        Composite rightComposite = new Composite(composite, SWT.None);
        GridLayout rightLayout = new GridLayout(1, true);
        rightLayout.marginWidth = 0;
        rightLayout.marginHeight = 0;
        rightComposite.setLayout(rightLayout);
        GridData rightGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        rightComposite.setLayoutData(rightGD);

        try {
            mapBrowser = new Browser(rightComposite, SWT.NONE);
            GridData mapBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            // mapBrowserGD.horizontalIndent = 3;
            // mapBrowserGD.verticalIndent = 3;
            mapBrowser.setLayoutData(mapBrowserGD);

            JsResources.ensureJavaScriptResources();
            lidarMapUrl = JsResources.ensureLidarmapHtmlResource();

            lidarViewerProgressListener = new LidarViewerProgressListener();

        } catch (Exception e) {
            e.printStackTrace();
        }

        GridData progressBarGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        progressBarGD.horizontalSpan = 2;
        generalProgressBar = new StageProgressBar(composite, SWT.HORIZONTAL | SWT.INDETERMINATE, progressBarGD);

    }

    private void createButtonsComposite( Composite leftComposite ) {
        Composite buttonsComposite = new Composite(leftComposite, SWT.NONE);
        buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        buttonsComposite.setLayout(new GridLayout(5, false));

        Button zoomToAllButton = new Button(buttonsComposite, SWT.PUSH);
        zoomToAllButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        zoomToAllButton.setToolTipText("Zoom to the complete data extend.");
        zoomToAllButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.ZOOM_TO_ALL));
        zoomToAllButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                mapBrowser.evaluate("zoomToData();");
            }
        });

        final Button infoToolButton = new Button(buttonsComposite, SWT.PUSH);
        infoToolButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        infoToolButton.setToolTipText("Enable info tool");
        infoToolButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.INFOTOOL_ON));
        infoToolButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                isInfoToolOn = !isInfoToolOn;
                mapBrowser.evaluate("enableInfo(" + isInfoToolOn + ");");
                if (isInfoToolOn) {
                    infoToolButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.INFOTOOL_OFF));
                } else {
                    infoToolButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.INFOTOOL_ON));
                }
            }
        });
    }

    private void openDatabase( final Shell shell ) {
        try {
            closeCurrentDb();
        } catch (Exception e1) {
            StageLogger.logError(this, e1);
        }
        File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
        SpatialiteSelectionDialog fileDialog = new SpatialiteSelectionDialog(parentShell, userFolder);
        final int returnCode = fileDialog.open();
        if (returnCode == SWT.OK) {
            final File selectedFile = fileDialog.getSelectedFile();
            if (selectedFile != null) {
                generalProgressBar.setProgressText("Connect to existing database");
                generalProgressBar.start(0);
                new Thread(new Runnable(){
                    public void run() {
                        shell.getDisplay().syncExec(new Runnable(){

                            public void run() {
                                try {

                                    closeCurrentDb();

                                    currentConnectedDatabase = new SpatialiteDb();
                                    currentConnectedDatabase.open(selectedFile.getAbsolutePath());

                                    // TODO check lidar db
                                    if (!LasSourcesTable.isLasDatabase(currentConnectedDatabase)) {
                                        MessageDialog.openWarning(shell, "WARNING",
                                                "The selected database is not a LiDAR database readable by STAGE.");
                                        return;
                                    }

                                    List<LasSource> lasSources = LasSourcesTable.getLasSources(currentConnectedDatabase);
                                    for( LasSource lasSource : lasSources ) {
                                        if (lasSource.levels > maxLevel) {
                                            maxLevel = lasSource.levels;
                                            maxLevelResolution = lasSource.resolution;
                                            maxLevelFactor = lasSource.levelFactor;
                                        }
                                    }
                                    SpatialiteGeometryColumns geometryColumns = currentConnectedDatabase
                                            .getGeometryColumnsForTable(LasSourcesTable.TABLENAME);
                                    databaseCrs = CRS.decode("EPSG:" + geometryColumns.srid);

                                    databaseNameText.setText(selectedFile.getName());

                                    mapBrowser.setUrl(lidarMapUrl);
                                    mapBrowser.addProgressListener(lidarViewerProgressListener);
                                    // DbLevel dbLevel =
                                    // gatherDatabaseLevels(currentConnectedDatabase);
                                } catch (Exception e) {
                                    currentConnectedDatabase = null;
                                    StageLogger.logError(LidarViewerEntryPoint.this, e);
                                } finally {
                                    generalProgressBar.stop();
                                }
                            }
                        });
                    }
                }).start();
            } else {
                generalProgressBar.stop();
            }
        } else {
            generalProgressBar.stop();
        }

    }

    private void closeCurrentDb() throws Exception {
        if (currentConnectedDatabase != null) {
            currentConnectedDatabase.close();
            currentConnectedDatabase = null;
        }
    }

    public void selected( boolean selected ) {
        // /*
        // * execution shortcut
        // */
        // if (selected) {
        // runKeyListener = new Listener(){
        // private static final long serialVersionUID = 1L;
        //
        // public void handleEvent( Event event ) {
        // createNewDatabase(parent.getShell());
        // }
        // };
        // String[] shortcut = new String[]{"ALT+ENTER"};
        // display.setData(RWT.ACTIVE_KEYS, shortcut);
        // display.setData(RWT.CANCEL_KEYS, shortcut);
        // display.addFilter(SWT.KeyDown, runKeyListener);
        // } else {
        // if (runKeyListener != null)
        // display.removeFilter(SWT.KeyDown, runKeyListener);
        // }
    }

    private class LidarViewerProgressListener implements ProgressListener {
        private static final long serialVersionUID = 1L;
        public void completed( ProgressEvent event ) {
            new BrowserFunction(mapBrowser, "getJsonData"){
                @Override
                public Object function( Object[] arguments ) {
                    int type = (int) getDouble(arguments[0]); // 1,
                    double south = getDouble(arguments[1]); // s,
                    double north = getDouble(arguments[2]); // n,
                    double west = getDouble(arguments[3]); // w,
                    double east = getDouble(arguments[4]); // e,
                    int zoom = (int) getDouble(arguments[5]); // zoom

                    if (StageLogger.LOG_DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("type=").append(type).append("\n");
                        sb.append("south=").append(south).append("\n");
                        sb.append("north=").append(north).append("\n");
                        sb.append("west=").append(west).append("\n");
                        sb.append("east=").append(east).append("\n");
                        sb.append("zoom=").append(zoom).append("\n***************************************");
                        StageLogger.logInfo(this, sb.toString());
                    }
                    Envelope llEnv = new Envelope(west, east, south, north);

                    try {
                        if (viewType == 1) {
                            isSourcesView = true;
                            return getSources(llEnv);
                        } else if (viewType == 2) {
                            isSourcesView = false;
                            return getPoints(llEnv);
                        } else {
                            MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);
                            Envelope searchEnv = JTS.transform(llEnv, ll2CRSTransform);
                            int countX = (int) (searchEnv.getWidth() / maxLevelResolution);
                            int countY = (int) (searchEnv.getHeight() / maxLevelResolution);
                            int tilesNumEsteem = countX * countY;
                            if (tilesNumEsteem >= 0 && tilesNumEsteem < cellsLimit) {
                                if (StageLogger.LOG_INFO)
                                    StageLogger.logInfo(this, "At zoom: " + zoom + " and bounds: " + searchEnv
                                            + " render cells (ca." + tilesNumEsteem + ")");
                                isSourcesView = false;
                                return getCells(llEnv);
                            } else {
                                for( int i = 1; i <= maxLevel; i++ ) {
                                    double res = maxLevelResolution * i * maxLevelFactor;
                                    countX = (int) (searchEnv.getWidth() / res);
                                    countY = (int) (searchEnv.getHeight() / res);
                                    tilesNumEsteem = countX * countY;
                                    if (tilesNumEsteem >= 0 && tilesNumEsteem < cellsLimit) {
                                        if (StageLogger.LOG_INFO)
                                            StageLogger.logInfo(this, "At zoom: " + zoom + " and bounds: " + searchEnv
                                                    + " render level: " + i + " (ca." + tilesNumEsteem + ")");
                                        isSourcesView = false;
                                        return getLevels(llEnv, i);
                                    }
                                }
                                if (StageLogger.LOG_INFO)
                                    StageLogger.logInfo(this, "At zoom: " + zoom + " and bounds: " + searchEnv
                                            + " render sources (ca." + tilesNumEsteem + ")");
                                isSourcesView = true;
                                return getSources(llEnv);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }

            };
            new BrowserFunction(mapBrowser, "getInfoData"){
                @Override
                public Object function( Object[] arguments ) {
                    double lon = getDouble(arguments[0]);
                    double lat = getDouble(arguments[1]);
                    String msg = "";

                    try {
                        MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);
                        Coordinate llCord = new Coordinate(lon, lat);
                        Coordinate crsCoord = JTS.transform(llCord, null, ll2CRSTransform);
                        Envelope env = new Envelope(llCord);
                        env.expandBy(0.000001);
                        Polygon searchPolygonLL = GeometryUtilities.createPolygonFromEnvelope(env);
                        Geometry searchPolygonLLCRS = JTS.transform(searchPolygonLL, ll2CRSTransform);
                        List<LasCell> lasCells = LasCellsTable.getLasCells(currentConnectedDatabase, searchPolygonLLCRS, true,
                                true, true, true, true);
                        if (lasCells.size() == 0) {
                            msg = "<p>No data found here.</p>";
                        } else if (isSourcesView == true) {
                            List<LasSource> sources = LasSourcesTable.getLasSources(currentConnectedDatabase);
                            for( LasSource lasSource : sources ) {
                                if (lasSource.polygon.intersects(GeometryUtilities.gf().createPoint(crsCoord))) {
                                    msg = "<p>source: " + lasSource.name;
                                    msg += "<br/>min elevation: " + lasSource.minElev;
                                    msg += "<br/>max elevation: " + lasSource.maxElev;
                                    msg += "<br/>cell resolution: " + lasSource.resolution;
                                    msg += "</p>";
                                    return msg;
                                }
                            }
                        } else {
                            LasCell lasCell = lasCells.get(0);
                            double[][] cellPositions = LasCellsTable.getCellPositions(lasCell);
                            short[][] cellIntens = LasCellsTable.getCellIntensityClass(lasCell);
                            double minDist = Double.POSITIVE_INFINITY;
                            double z = 0;
                            short intens = 0;
                            short classif = 0;
                            for( int i = 0; i < cellPositions.length; i++ ) {
                                double[] xyz = cellPositions[i];

                                Coordinate c = new Coordinate(xyz[0], xyz[1]);
                                double dist = c.distance(crsCoord);
                                if (dist < minDist) {
                                    minDist = dist;
                                    z = xyz[2];
                                    intens = cellIntens[i][0];
                                    classif = cellIntens[i][1];
                                }
                            }
                            msg = "<p>elevation: " + z;
                            msg += "<br/>intensity: " + intens;
                            msg += "<br/>classification: " + classif;
                            msg += "</p>";
                            return msg;
                        }
                    } catch (Exception e) {
                        StageLogger.logError(this, e);
                        msg = "<p>An error occurred while retrieving the data.</p>";
                    }
                    return msg;

                }

            };
            new BrowserFunction(mapBrowser, "getProfileChart"){
                @Override
                public Object function( Object[] arguments ) {
                    try {
                        Object[] object = (Object[]) arguments[0];

                        Coordinate[] coordsLL = new Coordinate[object.length / 2];
                        int index = 0;
                        for( int i = 0; i < object.length; i = i + 2 ) {
                            Coordinate c = new Coordinate((double) object[i + 1], (double) object[i]);
                            coordsLL[index++] = c;
                        }

                        MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);

                        GeometryFactory gf = GeometryUtilities.gf();
                        LineString profileLineLL = gf.createLineString(coordsLL);

                        LineString profileLineCrs = (LineString) JTS.transform(profileLineLL, ll2CRSTransform);
                        Geometry lineBuffer = profileLineCrs.buffer(0.2);

                        PreparedGeometry preparedLineBuffer = PreparedGeometryFactory.prepare(lineBuffer);

                        Coordinate c1 = profileLineCrs.getStartPoint().getCoordinate();
                        // Coordinate c2 = profileLineCrs.getEndPoint().getCoordinate();

                        List<double[]> progressiveElevList = new ArrayList<>();
                        List<LasCell> lasCells = LasCellsTable.getLasCells(currentConnectedDatabase, profileLineCrs, true, false,
                                false, false, false);
                        for( LasCell lasCell : lasCells ) {
                            double[][] cellPositions = LasCellsTable.getCellPositions(lasCell);
                            for( int i = 0; i < cellPositions.length; i++ ) {
                                Coordinate c = new Coordinate(cellPositions[i][0], cellPositions[i][1]);
                                Point p = gf.createPoint(c);
                                if (!preparedLineBuffer.intersects(p)) {
                                    continue;
                                }
                                progressiveElevList.add(new double[]{c1.distance(c), cellPositions[i][2]});
                            }
                        }

                        Object[] data = new Object[progressiveElevList.size()*2];
                        index = 0;
                        for( int i = 0; i < progressiveElevList.size(); i++ ) {
                            double[] ds = progressiveElevList.get(i);
                            data[index++] = ds[0];
                            data[index++] = ds[1];
                        }
                        return data;
                    } catch (Exception e) {
                        StageLogger.logError(this, e);
                    }
                    return null;
                }
            };
            mapBrowser.evaluate("loadScript();");
        }
        public void changed( ProgressEvent event ) {
        }
    }

    private Object getSources( Envelope env ) throws Exception {
        CoordinateReferenceSystem leafletCRS = DefaultGeographicCRS.WGS84;// CRS.decode("EPSG:4326");
        MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);
        MathTransform crs2llTransform = CRS.findMathTransform(databaseCrs, leafletCRS);
        Envelope searchEnv = JTS.transform(env, ll2CRSTransform);

        List<LasSource> lasSources = LasSourcesTable.getLasSources(currentConnectedDatabase);
        if (lasSources.size() == 0) {
            return null;
        }

        JSONObject rootObj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        rootObj.put("data", dataArray);

        Envelope bounds = null;
        int index = 0;
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;
        for( LasSource lasSource : lasSources ) {
            Envelope geomEnvCRS = lasSource.polygon.getEnvelopeInternal();
            if (!geomEnvCRS.intersects(searchEnv)) {
                continue;
            }

            Envelope geomEnvLL = JTS.transform(geomEnvCRS, crs2llTransform);

            if (bounds == null) {
                bounds = new Envelope(geomEnvLL);
            } else {
                bounds.expandToInclude(geomEnvLL);
            }

            minValue = Math.min(minValue, r(lasSource.maxElev));
            maxValue = Math.max(maxValue, r(lasSource.maxElev));
            Coordinate[] coordinates = lasSource.polygon.getCoordinates();
            JSONObject dataObj = new JSONObject();
            dataArray.put(index++, dataObj);
            Coordinate newCoord = new Coordinate();
            for( int i = 0; i < coordinates.length; i++ ) {
                JTS.transform(coordinates[i], newCoord, crs2llTransform);

                dataObj.put("x" + (i + 1), newCoord.x);
                dataObj.put("y" + (i + 1), newCoord.y);
            }

            dataObj.put("l", lasSource.name);
            dataObj.put("v", r(lasSource.maxElev));
        }
        if (bounds == null) {
            return null;
        }

        rootObj.put("xmin", bounds.getMinX());
        rootObj.put("xmax", bounds.getMaxX());
        rootObj.put("ymin", bounds.getMinY());
        rootObj.put("ymax", bounds.getMaxY());
        rootObj.put("vmin", minValue);
        rootObj.put("vmax", maxValue);

        loadedElementsNumText.setText("" + index);

        return rootObj.toString();
    }

    private Object getCells( Envelope env ) throws Exception {

        MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);
        MathTransform crs2llTransform = CRS.findMathTransform(databaseCrs, leafletCRS);

        Polygon searchPolygonLL = GeometryUtilities.createPolygonFromEnvelope(env);
        Geometry searchPolygonLLCRS = JTS.transform(searchPolygonLL, ll2CRSTransform);

        long t1 = System.currentTimeMillis();
        List<LasCell> lasCells = LasCellsTable.getLasCells(currentConnectedDatabase, searchPolygonLLCRS, true, true, false, false,
                false);
        int size = lasCells.size();
        long t2 = System.currentTimeMillis();
        System.out.println("time: " + (t2 - t1) + " size = " + size);

        if (size == 0) {
            return null;
        }

        loadedElementsNumText.setText("" + size);

        JSONObject rootObj = cells2Json(crs2llTransform, lasCells);
        return rootObj.toString();
    }

    private JSONObject cells2Json( MathTransform crs2llTransform, List<LasCell> lasCells ) throws TransformException {
        JSONObject rootObj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        rootObj.put("data", dataArray);
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;
        int index = 0;
        for( LasCell lasCell : lasCells ) {
            Coordinate[] coordinates = lasCell.polygon.getCoordinates();
            JSONObject dataObj = new JSONObject();
            dataArray.put(index++, dataObj);
            Coordinate newCoord = new Coordinate();
            for( int i = 0; i < coordinates.length; i++ ) {
                JTS.transform(coordinates[i], newCoord, crs2llTransform);

                dataObj.put("x" + (i + 1), newCoord.x);
                dataObj.put("y" + (i + 1), newCoord.y);
            }
            if (dataType == 0) {
                minValue = Math.min(minValue, r(lasCell.avgElev));
                maxValue = Math.max(maxValue, r(lasCell.avgElev));
                // dataObj.put("l", lasCell.avgElev);
                dataObj.put("v", r(lasCell.avgElev));
            } else {
                minValue = Math.min(minValue, r(lasCell.avgIntensity));
                maxValue = Math.max(maxValue, r(lasCell.avgIntensity));
                dataObj.put("v", r(lasCell.avgIntensity));
            }
        }
        rootObj.put("vmin", minValue);
        rootObj.put("vmax", maxValue);
        return rootObj;
    }

    private Object getLevels( Envelope env, int level )
            throws NoSuchAuthorityCodeException, FactoryException, TransformException, Exception, IOException {

        MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);
        MathTransform crs2llTransform = CRS.findMathTransform(databaseCrs, leafletCRS);

        JSONObject rootObj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        rootObj.put("data", dataArray);

        Polygon searchPolygonLL = GeometryUtilities.createPolygonFromEnvelope(env);
        Geometry searchPolygonLLCRS = JTS.transform(searchPolygonLL, ll2CRSTransform);

        long t1 = System.currentTimeMillis();
        List<LasLevel> lasLevels = LasLevelsTable.getLasLevels(currentConnectedDatabase, level, searchPolygonLLCRS);
        int size = lasLevels.size();
        long t2 = System.currentTimeMillis();
        System.out.println("time: " + (t2 - t1) + " size = " + size);

        if (size == 0) {
            return null;
        }

        loadedElementsNumText.setText("" + size);

        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;
        int index = 0;
        for( LasLevel laslevel : lasLevels ) {
            Coordinate[] coordinates = laslevel.polygon.getCoordinates();
            JSONObject dataObj = new JSONObject();
            dataArray.put(index++, dataObj);
            Coordinate newCoord = new Coordinate();
            for( int i = 0; i < coordinates.length; i++ ) {
                JTS.transform(coordinates[i], newCoord, crs2llTransform);

                dataObj.put("x" + (i + 1), newCoord.x);
                dataObj.put("y" + (i + 1), newCoord.y);
            }
            if (dataType == 0) {
                minValue = Math.min(minValue, r(laslevel.avgElev));
                maxValue = Math.max(maxValue, r(laslevel.avgElev));
                // dataObj.put("l", lasCell.avgElev);
                dataObj.put("v", r(laslevel.avgElev));
            } else {
                minValue = Math.min(minValue, r(laslevel.avgIntensity));
                maxValue = Math.max(maxValue, r(laslevel.avgIntensity));
                dataObj.put("v", r(laslevel.avgIntensity));
            }
        }
        rootObj.put("vmin", minValue);
        rootObj.put("vmax", maxValue);
        return rootObj.toString();
    }

    private Object getPoints( Envelope env )
            throws NoSuchAuthorityCodeException, FactoryException, TransformException, Exception, IOException {

        MathTransform ll2CRSTransform = CRS.findMathTransform(leafletCRS, databaseCrs);
        MathTransform crs2llTransform = CRS.findMathTransform(databaseCrs, leafletCRS);

        JSONObject rootObj = new JSONObject();
        JSONArray dataArray = new JSONArray();
        rootObj.put("data", dataArray);

        Polygon searchPolygonLL = GeometryUtilities.createPolygonFromEnvelope(env);
        Geometry searchPolygonLLCRS = JTS.transform(searchPolygonLL, ll2CRSTransform);

        long t1 = System.currentTimeMillis();
        List<LasCell> lasCells = LasCellsTable.getLasCells(currentConnectedDatabase, searchPolygonLLCRS, true, true, false, false,
                false);
        int size = lasCells.size();
        if (size == 0) {
            return null;
        }

        int count = 0;
        for( LasCell lasCell : lasCells ) {
            count += lasCell.pointsCount;
        }
        long t2 = System.currentTimeMillis();
        System.out.println("time: " + (t2 - t1) + " exploded size = " + count);
        if (count > pointsLimit) {
            loadedElementsNumText.setText("Wont't render: " + count);
            viewTypeCombo.select(0);
            viewType = 0;
            return cells2Json(crs2llTransform, lasCells).toString();
        }
        loadedElementsNumText.setText("" + count);

        int index = 0;
        if (dataType == 0) {
            double minValue = Double.POSITIVE_INFINITY;
            double maxValue = Double.NEGATIVE_INFINITY;
            for( LasCell lasCell : lasCells ) {
                double[][] cellData = LasCellsTable.getCellPositions(lasCell);
                Coordinate newCoord = new Coordinate();
                for( int i = 0; i < cellData.length; i++ ) {
                    JSONObject dataObj = new JSONObject();
                    dataArray.put(index++, dataObj);
                    double[] xyz = cellData[i];

                    minValue = Math.min(minValue, r(xyz[2]));
                    maxValue = Math.max(maxValue, r(xyz[2]));
                    JTS.transform(new Coordinate(xyz[0], xyz[1]), newCoord, crs2llTransform);

                    dataObj.put("x", newCoord.x);
                    dataObj.put("y", newCoord.y);
                    // dataObj.put("l", lasCell.avgElev);
                    dataObj.put("v", r(xyz[2]));
                }
            }
            rootObj.put("vmin", minValue);
            rootObj.put("vmax", maxValue);
        } else {
            short minValue = 30000;
            short maxValue = -1;
            for( LasCell lasCell : lasCells ) {
                double[][] cellPosition = LasCellsTable.getCellPositions(lasCell);
                short[][] cellIntens = LasCellsTable.getCellIntensityClass(lasCell);
                Coordinate newCoord = new Coordinate();
                for( int i = 0; i < cellPosition.length; i++ ) {
                    JSONObject dataObj = new JSONObject();
                    dataArray.put(index++, dataObj);
                    double[] xyz = cellPosition[i];
                    short[] intens = cellIntens[i];

                    minValue = (short) Math.min(minValue, r(intens[0]));
                    maxValue = (short) Math.max(maxValue, r(intens[0]));
                    JTS.transform(new Coordinate(xyz[0], xyz[1]), newCoord, crs2llTransform);

                    dataObj.put("x", newCoord.x);
                    dataObj.put("y", newCoord.y);
                    // dataObj.put("l", lasCell.avgElev);
                    dataObj.put("v", r(intens[0]));
                }
            }
            rootObj.put("vmin", minValue);
            rootObj.put("vmax", maxValue);
        }

        // FileUtilities.writeFile(rootObj.toString(2),
        // new
        // File("/home/hydrologis/development/STAGE-git/eu.hydrologis.stage.spatialite/js/wegl.json"));

        return rootObj.toString();
    }

    private double r( double num ) {
        return Math.round(num * 10.0) / 10.0;
    }

    private double getDouble( Object object ) {
        if (object instanceof Number) {
            Number num = (Number) object;
            return num.doubleValue();
        }
        throw new RuntimeException("Not a number...");
    }

}
