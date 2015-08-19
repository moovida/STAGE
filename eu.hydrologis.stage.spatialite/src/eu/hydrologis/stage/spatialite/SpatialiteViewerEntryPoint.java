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
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.geotools.factory.Hints;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.jgrasstools.gears.spatialite.ForeignKey;
import org.jgrasstools.gears.spatialite.QueryResult;
import org.jgrasstools.gears.spatialite.SpatialiteDb;
import org.jgrasstools.gears.spatialite.SpatialiteGeometryColumns;
import org.jgrasstools.gears.spatialite.SpatialiteGeometryType;
import org.jgrasstools.gears.spatialite.SpatialiteImportUtils;
import org.jgrasstools.gears.spatialite.SpatialiteTableNames;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utils.StageProgressBar;
import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.FileSelectionDialog;
import eu.hydrologis.stage.libs.utilsrap.LoginDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;
import eu.hydrologis.stage.spatialite.utils.ColumnLevel;
import eu.hydrologis.stage.spatialite.utils.DbLevel;
import eu.hydrologis.stage.spatialite.utils.SqlTemplates;
import eu.hydrologis.stage.spatialite.utils.TableLevel;
import eu.hydrologis.stage.spatialite.utils.TypeLevel;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SpatialiteViewerEntryPoint extends AbstractEntryPoint {
    private static final String SHAPEFILE_IMPORT = "import shapefile in selected table";
    private static final String SHAPEFILE_CCREATE_FROM_SCHEMA = "create table from shapefile schema";
    private static final String SHAPEFILE_TOOLTIP = "tools to deal with shapefiles";
    private static final String SHAPEFILE = "shapefile";
    private static final String SQL_TEMPLATES_TOOLTIP = "create a query based on a template";
    private static final String SQL_TEMPLATES = "sql templates";
    private static final String SQL_HISTORY_TOOLTIP = "select queries from the history";
    private static final String SQL_HISTORY = "sql history";
    private static final String DISCONNECT_TOOLTIP = "disconnect from current database";
    private static final String DISCONNECT = "disconnect";
    private static final String RUN_QUERY = "run query";
    private static final String RUN_QUERY_TOOLTIP = "run the query in the SQL Editor";
    private static final String RUN_QUERY_TO_FILE_TOOLTIP = "run the query in the SQL Editor and store result in file";
    private static final String SQL_EDITOR = "SQL Editor";
    private static final String DATA_VIEWER = "Data viewer";
    private static final String DATABASE_CONNECTIONS = "Database connection";
    private static final String NEW = "new";
    private static final String NEW_TOOLTIP = "create a new spatialite database";
    private static final String CONNECT = "connect";
    private static final String CONNECT_TOOLTIP = "connect to an existing spatialite database";

    private static final long serialVersionUID = 1L;

    private Display display;
    private TreeViewer databaseTreeViewer;

    private SpatialiteDb currentConnectedDatabase;
    private TableLevel currentSelectedTable;
    private ColumnLevel currentSelectedColumn;

    private Shell parentShell;
    private TableViewer dataTableViewer;
    private Group resultsetViewerGroup;
    private Text sqlEditorText;

    private List<String> oldSqlCommands = new ArrayList<String>();
    private Menu sqlHistoryMenu;
    private Group modulesListGroup;
    private StageProgressBar generalProgressBar;
    private Label messageLabel;
    private DbLevel currentDbLevel;

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

        parentShell = parent.getShell();
        display = parent.getDisplay();

        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, true));

        final ToolBar toolBar = new ToolBar(composite, SWT.FLAT);
        Menu menu = new Menu(toolBar);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("context menu item");
        toolBar.setMenu(menu);

        ToolItem newDatabaseButton = new ToolItem(toolBar, SWT.PUSH);
        newDatabaseButton.setText(NEW);
        newDatabaseButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.NEW_DATABASE));
        newDatabaseButton.setToolTipText(NEW_TOOLTIP);
        newDatabaseButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                createNewDatabase(parent.getShell());
            }
        });
        ToolItem connectDatabaseButton = new ToolItem(toolBar, SWT.PUSH);
        connectDatabaseButton.setText(CONNECT);
        connectDatabaseButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.CONNECT));
        connectDatabaseButton.setToolTipText(CONNECT_TOOLTIP);
        connectDatabaseButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                openDatabase(parent.getShell());
            }
        });

        ToolItem disconnectDatabaseButton = new ToolItem(toolBar, SWT.PUSH);
        disconnectDatabaseButton.setText(DISCONNECT);
        disconnectDatabaseButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.DISCONNECT));
        disconnectDatabaseButton.setToolTipText(DISCONNECT_TOOLTIP);
        disconnectDatabaseButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    closeCurrentDb();
                } catch (Exception e1) {
                    StageLogger.logError(this, e1);
                }
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        try {
            addHistoryCombo(toolBar);
            addTemplateCombo(toolBar);
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        new ToolItem(toolBar, SWT.SEPARATOR);

        try {
            addShapefileCombo(toolBar);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        new ToolItem(toolBar, SWT.SEPARATOR);

        SashForm mainComposite = new SashForm(composite, SWT.HORIZONTAL);
        GridData mainCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainCompositeGD.horizontalSpan = 2;
        mainComposite.setLayoutData(mainCompositeGD);

        Composite leftComposite = new Composite(mainComposite, SWT.None);
        GridLayout leftLayout = new GridLayout(1, true);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        leftComposite.setLayout(leftLayout);
        GridData leftGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        leftComposite.setLayoutData(leftGD);

        modulesListGroup = new Group(leftComposite, SWT.NONE);
        modulesListGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        modulesListGroup.setLayout(new GridLayout(2, false));
        modulesListGroup.setText(DATABASE_CONNECTIONS);

        databaseTreeViewer = createTreeViewer(modulesListGroup);
        databaseTreeViewer.setInput(null);

        Composite rightComposite = new Composite(mainComposite, SWT.None);
        GridLayout rightLayout = new GridLayout(1, true);
        rightLayout.marginWidth = 0;
        rightLayout.marginHeight = 0;
        rightComposite.setLayout(rightLayout);
        GridData rightGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        rightComposite.setLayoutData(rightGD);

        Group sqlEditorGroup = new Group(rightComposite, SWT.NONE);
        GridData sqlEditorGroupGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        sqlEditorGroup.setLayoutData(sqlEditorGroupGD);
        sqlEditorGroup.setLayout(new GridLayout(2, false));
        sqlEditorGroup.setText(SQL_EDITOR);

        Button runQueryButton = new Button(sqlEditorGroup, SWT.PUSH);
        runQueryButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        // runQueryButton.setText(RUN_QUERY);
        runQueryButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN));
        runQueryButton.setToolTipText(RUN_QUERY_TOOLTIP);
        runQueryButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                runQuery();
            }
        });

        sqlEditorText = new Text(sqlEditorGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData sqlEditorTextGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        sqlEditorTextGD.heightHint = 200;
        // sqlEditorTextGD.horizontalSpan = 2;
        sqlEditorTextGD.verticalSpan = 3;
        sqlEditorText.setLayoutData(sqlEditorTextGD);
        sqlEditorText.setText("");
        addDropTarget(sqlEditorText);

        Button runQueryToFileButton = new Button(sqlEditorGroup, SWT.PUSH);
        runQueryToFileButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        // runQueryButton.setText(RUN_QUERY);
        runQueryToFileButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN_TO_FILE));
        runQueryToFileButton.setToolTipText(RUN_QUERY_TO_FILE_TOOLTIP);
        runQueryToFileButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                runQueryToFile();
            }
        });

        Button clearSqlEditorButton = new Button(sqlEditorGroup, SWT.PUSH);
        clearSqlEditorButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        clearSqlEditorButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.TRASH));
        // clearSqlEditorButton.setText("");
        clearSqlEditorButton.setToolTipText("clear SQL editor");
        clearSqlEditorButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                sqlEditorText.setText("");
            }
        });

        messageLabel = new Label(rightComposite, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        messageLabel.setText("");

        resultsetViewerGroup = new Group(rightComposite, SWT.NONE);
        GridData resultsetViewerGroupGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        resultsetViewerGroup.setLayoutData(resultsetViewerGroupGD);
        resultsetViewerGroup.setLayout(new GridLayout(1, false));
        resultsetViewerGroup.setText(DATA_VIEWER);

        mainComposite.setWeights(new int[]{1, 3});

        GridData progressBarGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        progressBarGD.horizontalSpan = 2;
        generalProgressBar = new StageProgressBar(composite, SWT.HORIZONTAL | SWT.INDETERMINATE, progressBarGD);

    }

    private void addDropTarget( final Text text ) {
        DropTarget dropTarget = new DropTarget(text, DND.DROP_MOVE);
        dropTarget.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        dropTarget.addDropListener(new DropTargetListener(){
            private static final long serialVersionUID = 1L;
            public void dragEnter( final DropTargetEvent event ) {
            }

            public void dragLeave( final DropTargetEvent event ) {
            }

            public void dragOperationChanged( final DropTargetEvent event ) {
            }

            public void dragOver( final DropTargetEvent event ) {
            }

            public void drop( final DropTargetEvent event ) {
                String string = null;
                if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
                    string = (String) event.data;
                }
                if (string != null) {
                    int caretPosition = text.getCaretPosition();
                    String textStr = text.getText();
                    if (textStr.length() == 0 || textStr.length() == caretPosition) {
                        text.append(string);
                    } else {
                        String pre = textStr.substring(0, caretPosition);
                        String post = textStr.substring(caretPosition);
                        text.setText(pre + string + post);
                    }

                }
            }
            public void dropAccept( final DropTargetEvent event ) {
            }
        });

    }

    private void addShapefileCombo( final ToolBar toolBar ) throws IOException {
        final ToolItem shpCombo = new ToolItem(toolBar, SWT.DROP_DOWN);
        shpCombo.setText(SHAPEFILE);
        shpCombo.setToolTipText(SHAPEFILE_TOOLTIP);
        shpCombo.setImage(ImageCache.getInstance().getImage(display, ImageCache.VECTOR));
        shpCombo.setWidth(150);
        final Menu shpMenu = new Menu(toolBar.getShell(), SWT.POP_UP);

        final MenuItem tableFromShpMenuItem = new MenuItem(shpMenu, SWT.PUSH);
        tableFromShpMenuItem.setText(SHAPEFILE_CCREATE_FROM_SCHEMA);
        tableFromShpMenuItem.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;
            @Override
            public void widgetSelected( SelectionEvent e ) {
                File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, false, userFolder, null,
                        StageUtils.VECTOR_EXTENTIONS_READ_WRITE, null);
                int returnCode = fileDialog.open();
                if (returnCode == SWT.CANCEL) {
                    return;
                }
                final File selectedFile = fileDialog.getSelectedFile();
                if (selectedFile != null) {
                    try {
                        SpatialiteImportUtils.createTableFromShp(currentConnectedDatabase, selectedFile);
                        DbLevel levels = gatherDatabaseLevels(currentConnectedDatabase);
                        relayout(levels, true);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        final MenuItem importShpMenuItem = new MenuItem(shpMenu, SWT.PUSH);
        importShpMenuItem.setText(SHAPEFILE_IMPORT);
        importShpMenuItem.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;
            @Override
            public void widgetSelected( SelectionEvent e ) {
                if (currentSelectedTable == null) {
                    MessageDialog.openError(parentShell, "ERROR",
                            "To import a shapefile, the destination table needs to be selected.");
                    return;
                }

                File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, false, userFolder, null,
                        StageUtils.VECTOR_EXTENTIONS_READ_WRITE, null);
                int returnCode = fileDialog.open();
                if (returnCode != SWT.OK) {
                    return;
                }
                final File selectedFile = fileDialog.getSelectedFile();
                if (selectedFile != null) {
                    generalProgressBar.setProgressText("Importing shapefile...");
                    generalProgressBar.start(0);
                    new Thread(new Runnable(){
                        public void run() {
                            parentShell.getDisplay().syncExec(new Runnable(){
                                public void run() {
                                    try {
                                        try {
                                            SpatialiteImportUtils.importShapefile(currentConnectedDatabase, selectedFile,
                                                    currentSelectedTable.tableName, -1);
                                        } catch (Exception e1) {
                                            StageLogger.logError(this, e1);
                                            MessageDialog.openError(parentShell, "ERROR",
                                                    "Could not import shapefile: " + e1.getLocalizedMessage());
                                        }
                                    } finally {
                                        generalProgressBar.stop();
                                    }
                                }
                            });
                        }
                    }).start();

                }
            }
        });

        shpCombo.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( final SelectionEvent event ) {
                if (event.detail == SWT.ARROW) {
                    Point point = toolBar.toDisplay(event.x, event.y);
                    shpMenu.setLocation(point);
                    shpMenu.setVisible(true);
                }
            }
        });
    }
    private void addTemplateCombo( final ToolBar toolBar ) throws IOException {
        final ToolItem templatesCombo = new ToolItem(toolBar, SWT.DROP_DOWN);
        templatesCombo.setText(SQL_TEMPLATES);
        templatesCombo.setToolTipText(SQL_TEMPLATES_TOOLTIP);
        templatesCombo.setImage(ImageCache.getInstance().getImage(display, ImageCache.TEMPLATE));
        templatesCombo.setWidth(150);
        final Menu templatesMenu = new Menu(toolBar.getShell(), SWT.POP_UP);

        for( String templateName : SqlTemplates.templatesMap.keySet() ) {
            final MenuItem menuItem = new MenuItem(templatesMenu, SWT.PUSH);
            menuItem.setText(templateName);
            menuItem.addSelectionListener(new SelectionAdapter(){
                private static final long serialVersionUID = 1L;

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    String templateName = menuItem.getText();
                    String query = SqlTemplates.templatesMap.get(templateName);
                    String text = sqlEditorText.getText();
                    if (text.trim().length() != 0) {
                        text += "\n";
                    }
                    text += query;
                    sqlEditorText.setText(text);
                }
            });
        }

        templatesCombo.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( final SelectionEvent event ) {
                if (event.detail == SWT.ARROW) {
                    Point point = toolBar.toDisplay(event.x, event.y);
                    templatesMenu.setLocation(point);
                    templatesMenu.setVisible(true);
                }
            }
        });
    }

    private void addHistoryCombo( final ToolBar toolBar ) throws IOException {
        final ToolItem historyCombo = new ToolItem(toolBar, SWT.DROP_DOWN);
        historyCombo.setToolTipText(SQL_HISTORY_TOOLTIP);
        historyCombo.setImage(ImageCache.getInstance().getImage(display, ImageCache.HISTORY_DB));
        historyCombo.setWidth(150);
        sqlHistoryMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
        historyCombo.setText(SQL_HISTORY);

        updateHistoryCombo();

        historyCombo.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( final SelectionEvent event ) {
                if (event.detail == SWT.ARROW) {
                    Point point = toolBar.toDisplay(event.x, event.y);
                    sqlHistoryMenu.setLocation(point);
                    sqlHistoryMenu.setVisible(true);
                }
            }
        });
    }

    private void updateHistoryCombo() {
        MenuItem[] items = sqlHistoryMenu.getItems();
        for( MenuItem menuItem : items ) {
            menuItem.dispose();
        }

        for( String sqlCommand : oldSqlCommands ) {
            final MenuItem menuItem = new MenuItem(sqlHistoryMenu, SWT.PUSH);
            menuItem.setText(sqlCommand);
            menuItem.addSelectionListener(new SelectionAdapter(){
                private static final long serialVersionUID = 1L;

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    String query = menuItem.getText();
                    addTextToQueryEditor(query);
                }
            });
        }
    }

    private void createTableViewer( Composite parent, QueryResult queryResult ) throws Exception {
        if (dataTableViewer != null)
            dataTableViewer.getControl().dispose();
        messageLabel.setText("");

        dataTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        dataTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        for( int i = 0; i < queryResult.names.size(); i++ ) {
            String name = queryResult.names.get(i);
            createColumn(dataTableViewer, name, i);
        }

        dataTableViewer.getTable().setHeaderVisible(true);
        dataTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataTableViewer.getTable().setLayoutData(tableData);

        dataTableViewer.setInput(queryResult.data);

        // add right click menu
        MenuManager manager = new MenuManager();
        dataTableViewer.getControl().setMenu(manager.createContextMenu(dataTableViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                if (dataTableViewer.getSelection() instanceof IStructuredSelection) {
                    final IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                    if (selection.isEmpty()) {
                        return;
                    }

                    manager.add(new Action("Quick View Geometries", null){
                        @Override
                        public void run() {
                            try {
                                if (currentSelectedColumn == null || currentSelectedColumn.geomColumn == null) {
                                    MessageDialog.openInformation(parentShell, "INFO",
                                            "Select in the tables view the column of the geoetry represented.");
                                    return;
                                }

                                int epsg = currentSelectedColumn.geomColumn.srid;

                                CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:" + epsg);

                                Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
                                CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
                                CoordinateReferenceSystem targetCRS = factory.createCoordinateReferenceSystem("EPSG:4326");

                                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

                                SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
                                b.setName("geojson");
                                b.setCRS(targetCRS);
                                b.add("geometry", Geometry.class);

                                b.add("cat", String.class);

                                SimpleFeatureType type = b.buildFeatureType();
                                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);

                                DefaultFeatureCollection fc = new DefaultFeatureCollection();
                                List<Geometry> geomsList = new ArrayList<>();
                                List<Object[]> selectedData = selection.toList();
                                int index = 0;
                                for( Object[] objects : selectedData ) {
                                    for( int i = 0; i < objects.length; i++ ) {
                                        Object object1 = objects[i];
                                        if (object1 instanceof Geometry) {
                                            Geometry geom = (Geometry) object1;
                                            Geometry targetGeometry = JTS.transform(geom, transform);
                                            geomsList.add(targetGeometry);

                                            String catValue = "" + index;
                                            if (i + 1 < objects.length) {
                                                if (objects[i + 1] != null)
                                                    catValue = objects[i + 1].toString();
                                            }
                                            Object[] values = new Object[]{targetGeometry, catValue};
                                            index++;
                                            builder.addAll(values);
                                            SimpleFeature feature = builder.buildFeature(null);
                                            fc.add(feature);
                                        }
                                    }
                                }

                                FeatureJSON fjson = new FeatureJSON();
                                StringWriter writer = new StringWriter();
                                fjson.writeFeatureCollection(fc, writer);
                                String geojson = writer.toString();

                                // System.out.println(geojson);

                                QuickGeometryViewDialog d = new QuickGeometryViewDialog(parentShell, "Table Graph", geojson);
                                d.open();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    // Action[] multiTableActions = makeMultiTablesAction(selectedTables);
                    // for( Action action : multiTableActions ) {
                    // manager.add(action);
                    // }
                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        parent.layout();
    }

    private String getGeometryFieldName() throws SQLException {
        String geomFieldName = SpatialiteDb.defaultGeomFieldName;
        if (currentSelectedTable != null) {
            SpatialiteGeometryColumns geometryColumns = currentConnectedDatabase
                    .getGeometryColumnsForTable(currentSelectedTable.tableName);
            if (geometryColumns != null) {
                geomFieldName = geometryColumns.f_geometry_column;
            }
        }
        return geomFieldName;
    }

    private TableViewerColumn createColumn( TableViewer viewer, String name, int dataIndex ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new RecordLabelProvider(dataIndex));
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setToolTipText(name);
        column.setWidth(100);
        column.setMoveable(true);
        // column.addSelectionListener( new SelectionAdapter() {
        // @Override
        // public void widgetSelected( SelectionEvent event ) {
        // int sortDirection = updateSortDirection( ( TableColumn )event.widget );
        // sort( viewer, COL_FIRST_NAME, sortDirection == SWT.DOWN );
        // }
        // } );
        return result;
    }

    private TreeViewer createTreeViewer( Composite modulesComposite ) {
        final TreeViewer modulesViewer = new TreeViewer(modulesComposite);

        Control control = modulesViewer.getControl();
        GridData controlGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        controlGD.horizontalSpan = 2;
        control.setLayoutData(controlGD);
        modulesViewer.setContentProvider(new ITreeContentProvider(){
            private static final long serialVersionUID = 1L;

            public Object[] getElements( Object inputElement ) {
                return getChildren(inputElement);
            }

            public Object[] getChildren( Object parentElement ) {
                if (parentElement instanceof DbLevel) {
                    DbLevel dbLevel = (DbLevel) parentElement;
                    return dbLevel.typesList.toArray();
                }
                if (parentElement instanceof TypeLevel) {
                    TypeLevel typeLevel = (TypeLevel) parentElement;
                    return typeLevel.tablesList.toArray();
                }
                if (parentElement instanceof TableLevel) {
                    TableLevel tableLevel = (TableLevel) parentElement;
                    return tableLevel.columnsList.toArray();
                }
                return new Object[0];
            }

            public Object getParent( Object element ) {
                if (element instanceof String) {
                    return currentConnectedDatabase;
                }
                return null;
            }

            public boolean hasChildren( Object element ) {
                return getChildren(element).length > 0;
            }

            public void dispose() {
            }

            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }

        });

        modulesViewer.setLabelProvider(new LabelProvider(){
            private static final long serialVersionUID = 1L;

            public Image getImage( Object element ) {
                if (element instanceof DbLevel) {
                    return ImageCache.getInstance().getImage(display, ImageCache.DATABASE);
                }
                if (element instanceof TypeLevel) {
                    return ImageCache.getInstance().getImage(display, ImageCache.TABLE_FOLDER);
                }
                if (element instanceof TableLevel) {
                    TableLevel tableLevel = (TableLevel) element;
                    if (tableLevel.isGeo) {
                        return ImageCache.getInstance().getImage(display, ImageCache.TABLE_SPATIAL);
                    } else {
                        return ImageCache.getInstance().getImage(display, ImageCache.TABLE);
                    }
                }
                if (element instanceof ColumnLevel) {
                    ColumnLevel columnLevel = (ColumnLevel) element;
                    if (columnLevel.isPK) {
                        return ImageCache.getInstance().getImage(display, ImageCache.TABLE_COLUMN_PRIMARYKEY);
                    } else if (columnLevel.references != null) {
                        return ImageCache.getInstance().getImage(display, ImageCache.TABLE_COLUMN_INDEX);
                    } else if (columnLevel.geomColumn != null) {
                        SpatialiteGeometryType gType = SpatialiteGeometryType.forValue(columnLevel.geomColumn.geometry_type);
                        switch( gType ) {
                        case POINT_XY:
                        case POINT_XYM:
                        case POINT_XYZ:
                        case POINT_XYZM:
                            return ImageCache.getInstance().getImage(display, ImageCache.GEOM_POINT);
                        case LINESTRING_XY:
                        case LINESTRING_XYM:
                        case LINESTRING_XYZ:
                        case LINESTRING_XYZM:
                            return ImageCache.getInstance().getImage(display, ImageCache.GEOM_LINE);
                        case POLYGON_XY:
                        case POLYGON_XYM:
                        case POLYGON_XYZ:
                        case POLYGON_XYZM:
                            return ImageCache.getInstance().getImage(display, ImageCache.GEOM_POLYGON);
                        default:
                            return ImageCache.getInstance().getImage(display, ImageCache.TABLE_COLUMN);
                        }
                    } else {
                        return ImageCache.getInstance().getImage(display, ImageCache.TABLE_COLUMN);
                    }
                }

                return null;
            }

            public String getText( Object element ) {
                if (element instanceof DbLevel) {
                    DbLevel dbLevel = (DbLevel) element;
                    return dbLevel.dbName;
                }
                if (element instanceof TypeLevel) {
                    TypeLevel typeLevel = (TypeLevel) element;
                    return typeLevel.typeName;
                }
                if (element instanceof TableLevel) {
                    TableLevel tableLevel = (TableLevel) element;
                    return tableLevel.tableName;
                }
                if (element instanceof ColumnLevel) {
                    ColumnLevel columnLevel = (ColumnLevel) element;
                    SpatialiteGeometryColumns gc = columnLevel.geomColumn;
                    if (gc == null) {
                        String col = columnLevel.columnName + " (" + columnLevel.columnType + ")";
                        if (columnLevel.references != null) {
                            col += " " + columnLevel.references;
                        }
                        return col;
                    } else {
                        String gType = SpatialiteGeometryType.forValue(gc.geometry_type).getDescription();
                        boolean indexEnabled = gc.spatial_index_enabled == 1 ? true : false;
                        return columnLevel.columnName + "[" + gType + ",EPSG:" + gc.srid + ",idx:" + indexEnabled + "]";
                    }
                }
                return ""; //$NON-NLS-1$
            }
        });

        modulesViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {
                if (!(event.getSelection() instanceof IStructuredSelection)) {
                    return;
                }
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();

                currentSelectedTable = null;
                currentSelectedColumn = null;

                Object selectedItem = sel.getFirstElement();
                if (selectedItem instanceof TableLevel) {
                    currentSelectedTable = (TableLevel) selectedItem;

                    try {
                        QueryResult queryResult = currentConnectedDatabase.getTableRecordsMapIn(currentSelectedTable.tableName,
                                null, true, 20);
                        createTableViewer(resultsetViewerGroup, queryResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (selectedItem instanceof ColumnLevel) {
                    currentSelectedColumn = (ColumnLevel) selectedItem;
                }
            }

        });

        Tree tree = modulesViewer.getTree();
        DragSource dragSource = new DragSource(tree, DND.DROP_MOVE);
        dragSource.setTransfer(new Transfer[]{TextTransfer.getInstance()});
        dragSource.addDragListener(new DragSourceListener(){
            private static final long serialVersionUID = 1L;
            private String dragDataText;

            public void dragFinished( final DragSourceEvent event ) {
                dragDataText = "";
                if (event.detail == DND.DROP_MOVE) {
                    if (currentSelectedTable != null) {
                        dragDataText = currentSelectedTable.tableName;
                    } else if (currentSelectedColumn != null) {
                        dragDataText = currentSelectedColumn.columnName;
                    }
                }
            }

            public void dragSetData( final DragSourceEvent event ) {
                if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                    event.data = dragDataText;
                }
            }

            public void dragStart( final DragSourceEvent event ) {
                if (currentSelectedTable != null) {
                    dragDataText = currentSelectedTable.tableName;
                    event.doit = true;
                } else if (currentSelectedColumn != null) {
                    dragDataText = currentSelectedColumn.columnName;
                    event.doit = true;
                } else {
                    event.doit = false;
                }
            }
        });

        // add right click menu
        MenuManager manager = new MenuManager();
        modulesViewer.getControl().setMenu(manager.createContextMenu(modulesViewer.getControl()));
        manager.addMenuListener(new IMenuListener(){
            private static final long serialVersionUID = 1L;

            @Override
            public void menuAboutToShow( IMenuManager manager ) {
                if (modulesViewer.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) modulesViewer.getSelection();
                    final Object selectedItem = selection.getFirstElement();
                    if (selectedItem == null || selection.isEmpty()) {
                        return;
                    }

                    if (selection.size() == 1) {
                        if (selectedItem instanceof TableLevel) {
                            TableLevel selectedTable = (TableLevel) selectedItem;
                            Action[] tableActions = makeTableAction(selectedTable);
                            for( Action action : tableActions ) {
                                manager.add(action);
                            }
                        } else if (selectedItem instanceof ColumnLevel) {
                            ColumnLevel selectedColumn = (ColumnLevel) selectedItem;
                            if (selectedColumn.references != null) {
                                Action[] columnActions = makeFKColumnAction(selectedColumn);
                                for( Action action : columnActions ) {
                                    manager.add(action);
                                }
                            }
                        }
                    } else {
                        if (selectedItem instanceof TableLevel) {
                            List<TableLevel> selectedTables = selection.toList();
                            Action[] multiTableActions = makeMultiTablesAction(selectedTables);
                            for( Action action : multiTableActions ) {
                                manager.add(action);
                            }
                        }
                    }
                }
            }

        });
        manager.setRemoveAllWhenShown(true);

        return modulesViewer;
    }

    /**
     * Resfresh the viewer.
     * @param dbLevel 
     * 
     * @param filterText
     * 
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public void relayout( final DbLevel dbLevel, final boolean expandAll )
            throws InvocationTargetException, InterruptedException {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                databaseTreeViewer.setInput(dbLevel);
                if (expandAll)
                    databaseTreeViewer.expandToLevel(2);
            }
        });
    }

    private void addQueryToHistoryCombo( String sqlText ) {
        if (oldSqlCommands.contains(sqlText)) {
            return;
        }
        oldSqlCommands.add(0, sqlText);
        if (oldSqlCommands.size() > 20) {
            oldSqlCommands.remove(20);
        }
        updateHistoryCombo();
    }

    private void createNewDatabase( final Shell shell ) {
        try {
            closeCurrentDb();
        } catch (Exception e1) {
            StageLogger.logError(this, e1);
        }
        File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
        FileSelectionDialog fileDialog = new FileSelectionDialog(shell, true, userFolder, null, null, null);
        int returnCode = fileDialog.open();
        if (returnCode == SWT.CANCEL) {
            return;
        }
        final File selectedFile = fileDialog.getSelectedFile();
        if (selectedFile != null) {
            generalProgressBar.setProgressText("Creating new spatialite database (this might take a few minutes)...");
            generalProgressBar.start(0);
            new Thread(new Runnable(){
                public void run() {
                    shell.getDisplay().syncExec(new Runnable(){
                        public void run() {
                            try {
                                currentConnectedDatabase = new SpatialiteDb();
                                currentConnectedDatabase.open(selectedFile.getAbsolutePath());
                                currentConnectedDatabase.initSpatialMetadata(null);

                                DbLevel dbLevel = gatherDatabaseLevels(currentConnectedDatabase);
                                modulesListGroup.setText(dbLevel.dbName);

                                relayout(dbLevel, false);
                            } catch (Exception e) {
                                currentConnectedDatabase = null;
                                StageLogger.logError(SpatialiteViewerEntryPoint.this, e);
                            } finally {
                                generalProgressBar.stop();
                            }
                        }
                    });
                }
            }).start();
        }
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
                                    currentConnectedDatabase = new SpatialiteDb();
                                    currentConnectedDatabase.open(selectedFile.getAbsolutePath());

                                    DbLevel dbLevel = gatherDatabaseLevels(currentConnectedDatabase);
                                    modulesListGroup.setText(dbLevel.dbName);

                                    relayout(dbLevel, true);
                                } catch (Exception e) {
                                    currentConnectedDatabase = null;
                                    StageLogger.logError(SpatialiteViewerEntryPoint.this, e);
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

    private DbLevel gatherDatabaseLevels( SpatialiteDb db ) throws SQLException {
        currentDbLevel = new DbLevel();
        String databasePath = db.getDatabasePath();
        File dbFile = new File(databasePath);
        String dbPathRelative = StageWorkspace.makeRelativeToDataFolder(dbFile);
        currentDbLevel.dbName = dbPathRelative;

        HashMap<String, List<String>> currentDatabaseTablesMap = db.getTablesMap(true);
        for( String typeName : SpatialiteTableNames.ALL_TYPES_LIST ) {
            TypeLevel typeLevel = new TypeLevel();
            typeLevel.typeName = typeName;
            List<String> tablesList = currentDatabaseTablesMap.get(typeName);
            for( String tableName : tablesList ) {
                TableLevel tableLevel = new TableLevel();
                tableLevel.parent = currentDbLevel;
                tableLevel.tableName = tableName;

                SpatialiteGeometryColumns geometryColumns = db.getGeometryColumnsForTable(tableName);
                List<ForeignKey> foreignKeys = new ArrayList<>();
                try {
                    foreignKeys = db.getForeignKeys(tableName);
                } catch (Exception e) {
                }
                tableLevel.isGeo = geometryColumns != null;
                List<String[]> tableInfo = db.getTableColumns(tableName);
                for( String[] columnInfo : tableInfo ) {
                    ColumnLevel columnLevel = new ColumnLevel();
                    columnLevel.parent = tableLevel;
                    String columnName = columnInfo[0];
                    String columnType = columnInfo[1];
                    String columnPk = columnInfo[2];
                    columnLevel.columnName = columnName;
                    columnLevel.columnType = columnType;
                    columnLevel.isPK = columnPk.equals("1") ? true : false;
                    if (geometryColumns != null && columnName.equals(geometryColumns.f_geometry_column)) {
                        columnLevel.geomColumn = geometryColumns;
                    }
                    for( ForeignKey fKey : foreignKeys ) {
                        if (fKey.from.equals(columnName)) {
                            columnLevel.setFkReferences(fKey);
                        }
                    }
                    tableLevel.columnsList.add(columnLevel);
                }
                typeLevel.tablesList.add(tableLevel);
            }
            currentDbLevel.typesList.add(typeLevel);
        }
        return currentDbLevel;
    }

    private void closeCurrentDb() throws Exception {
        if (currentConnectedDatabase != null) {
            currentConnectedDatabase.close();
            currentConnectedDatabase = null;
            relayout(null, false);
        }
        if (modulesListGroup != null)
            modulesListGroup.setText(DATABASE_CONNECTIONS);
        if (dataTableViewer != null)
            dataTableViewer.getTable().dispose();
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

    private class RecordLabelProvider extends ColumnLabelProvider {
        private static final long serialVersionUID = 1L;
        private int dataIndex;
        public RecordLabelProvider( int dataIndex ) {
            this.dataIndex = dataIndex;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof Object[]) {
                Object[] data = (Object[]) element;
                Object obj = data[dataIndex];
                if (obj != null) {
                    return obj.toString();
                } else {
                    return "NULL";
                }
            }
            return "";
        }
    }

    private void addTextToQueryEditor( String newText ) {
        String text = sqlEditorText.getText();
        if (text.trim().length() != 0) {
            text += "\n";
        }
        text += newText;
        sqlEditorText.setText(text);
    }

    private void runQuery() {
        String sqlText = sqlEditorText.getText();
        sqlText = sqlText.trim();
        if (currentConnectedDatabase != null && sqlText.length() > 0) {
            try {
                int limit = -1;

                if (sqlText.toLowerCase().startsWith("select") || sqlText.toLowerCase().startsWith("pragma")) {
                    limit = 5000;
                    QueryResult queryResult = currentConnectedDatabase.getTableRecordsMapFromRawSql(sqlText, limit);
                    createTableViewer(resultsetViewerGroup, queryResult);

                    int size = queryResult.data.size();
                    String msg = "Records: " + size;
                    if (size == limit) {
                        msg += " (table output limited to " + limit + " records)";
                    }
                    messageLabel.setText(msg);
                } else {
                    int resultCode = currentConnectedDatabase.executeInsertUpdateDeleteSql(sqlText);
                    QueryResult dummyQueryResult = new QueryResult();
                    dummyQueryResult.names.add("Result = " + resultCode);
                    createTableViewer(resultsetViewerGroup, dummyQueryResult);
                }

                addQueryToHistoryCombo(sqlText);

            } catch (Exception e1) {
                String localizedMessage = e1.getLocalizedMessage();
                MessageDialog.openError(parentShell, "ERROR", "An error occurred: " + localizedMessage);
            }
        }
    }

    private void runQueryToFile() {

        final String sqlText = sqlEditorText.getText().trim();
        if (currentConnectedDatabase != null && sqlText.length() > 0) {
            try {

                if (sqlText.toLowerCase().startsWith("select") || sqlText.toLowerCase().startsWith("pragma")) {
                    File dataFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                    FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, true, dataFolder, null,
                            new String[]{"csv"}, null);
                    int returnCode = fileDialog.open();
                    if (returnCode == SWT.CANCEL) {
                        return;
                    }
                    final File selectedFile = fileDialog.getSelectedFile();
                    if (selectedFile != null) {
                        generalProgressBar.setProgressText("Running query...");
                        generalProgressBar.start(0);
                        new Thread(new Runnable(){
                            public void run() {
                                parentShell.getDisplay().syncExec(new Runnable(){
                                    public void run() {
                                        try {
                                            currentConnectedDatabase.runRawSqlToCsv(sqlText, selectedFile, true, ";");
                                            messageLabel.setText("Data written to: " + selectedFile.getName());
                                            addQueryToHistoryCombo(sqlText);
                                        } catch (Exception e) {
                                            StageLogger.logError(SpatialiteViewerEntryPoint.this, e);
                                        } finally {
                                            generalProgressBar.stop();
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                } else {
                    MessageDialog.openWarning(parentShell, "WARNING",
                            "Writing to files is allowed only for SELECT statements and PRAGMAs.");
                    return;
                }
            } catch (Exception e1) {
                String localizedMessage = e1.getLocalizedMessage();
                MessageDialog.openError(parentShell, "ERROR", "An error occurred: " + localizedMessage);
            }
        }
    }

    @SuppressWarnings("serial")
    private Action[] makeTableAction( final TableLevel selectedTable ) {
        boolean hasFK = false;
        for( ColumnLevel col : selectedTable.columnsList ) {
            if (col.references != null) {
                hasFK = true;
                break;
            }
        }

        Action[] actions = new Action[hasFK ? 3 : 2];
        actions[0] = new Action("Create select statement", null){
            @Override
            public void run() {

                try {
                    String tableName = selectedTable.tableName;
                    String letter = tableName.substring(0, 1);
                    List<String[]> tableColumns = currentConnectedDatabase.getTableColumns(tableName);
                    SpatialiteGeometryColumns geometryColumns = currentConnectedDatabase.getGeometryColumnsForTable(tableName);
                    String query = "SELECT ";
                    for( int i = 0; i < tableColumns.size(); i++ ) {
                        if (i > 0)
                            query += ",";
                        String colName = tableColumns.get(i)[0];
                        if (geometryColumns != null && colName.equals(geometryColumns.f_geometry_column)) {
                            colName = "ST_AsBinary(" + letter + "." + colName + ") as " + colName;
                            query += colName;
                        } else {
                            query += letter + "." + colName;
                        }
                    }
                    query += " FROM " + tableName + " " + letter;
                    addTextToQueryEditor(query);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        };
        actions[1] = new Action("Count table records", null){
            @Override
            public void run() {
                try {
                    String tableName = selectedTable.tableName;
                    long count = currentConnectedDatabase.getCount(tableName);
                    MessageDialog.openInformation(parentShell, "INFO", "Count: " + count);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        };
        if (hasFK) {
            actions[2] = new Action("Show Foreign Keys Diagram", null){
                @Override
                public void run() {

                    try {
                        HashMap<String, TableLevel> name2LevelMap = new HashMap<>();
                        List<TypeLevel> typesList = currentDbLevel.typesList;
                        for( TypeLevel typeLevel : typesList ) {
                            if (typeLevel.typeName.equals(SpatialiteTableNames.USERDATA)) {
                                List<TableLevel> tablesList = typeLevel.tablesList;
                                for( TableLevel tableLevel : tablesList ) {
                                    name2LevelMap.put(tableLevel.tableName, tableLevel);
                                }
                            }
                        }

                        List<TableLevel> tablesInvolved = new ArrayList<>();
                        tablesInvolved.add(selectedTable);
                        for( ColumnLevel col : selectedTable.columnsList ) {
                            if (col.references != null) {
                                String[] tableColsFromFK = col.tableColsFromFK();
                                TableLevel tableLevel = name2LevelMap.get(tableColsFromFK[0]);
                                if (tableLevel != null && !tablesInvolved.contains(tableLevel)) {
                                    tablesInvolved.add(tableLevel);
                                }
                            }
                        }

                        int tablesNum = tablesInvolved.size();

                        int gridCols = 4;
                        int gridRows = tablesNum / gridCols + 1;

                        int indent = 10;
                        int tableWidth = 350;
                        int tableHeight = 300;

                        JSONArray root = new JSONArray();
                        int tabesIndex = 0;
                        int runningX = 0;
                        int runningY = 10;
                        for( int gridRow = 0; gridRow < gridRows; gridRow++ ) {
                            runningX = indent;
                            for( int gridCol = 0; gridCol < gridCols; gridCol++ ) {
                                if (tabesIndex == tablesNum) {
                                    break;
                                }

                                JSONObject tableJson = new JSONObject();
                                root.put(tableJson);
                                TableLevel curTable = tablesInvolved.get(tabesIndex);
                                int id = tabesIndex;
                                tabesIndex++;

                                tableJson.put("id", id);
                                tableJson.put("x", runningX);
                                tableJson.put("y", runningY);

                                String fromTable = curTable.tableName;
                                tableJson.put("name", fromTable);
                                JSONArray fieldsArray = new JSONArray();
                                tableJson.put("fields", fieldsArray);
                                List<ColumnLevel> cols = curTable.columnsList;

                                for( ColumnLevel col : cols ) {
                                    JSONObject colObject = new JSONObject();
                                    fieldsArray.put(colObject);
                                    colObject.put("fname", col.columnName);
                                    if (col.references != null) {
                                        String[] tableColsFromFK = col.tableColsFromFK();
                                        String toTable = tableColsFromFK[0];
                                        String toColumn = tableColsFromFK[1];
                                        colObject.put("fk_name", toTable);
                                        colObject.put("fk_field", toColumn);
                                    }
                                }

                                runningX += indent + tableWidth;
                            }
                            runningY += tableHeight + indent;
                        }
                        String json = root.toString();
                        // String string = root.toString(2);
                        TableGraphDialog d = new TableGraphDialog(parentShell, "Table Graph", json);
                        d.open();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
        }
        return actions;
    }

    @SuppressWarnings("serial")
    private Action[] makeMultiTablesAction( final List<TableLevel> selectedTables ) {
        Action[] actions = {//
                new Action("Create Tables Diagram", null){
                    @Override
                    public void run() {
                        try {
                            int tablesNum = selectedTables.size();

                            int gridCols = 4;
                            int gridRows = tablesNum / gridCols + 1;

                            int indent = 10;
                            int tableWidth = 350;
                            int tableHeight = 300;

                            JSONArray root = new JSONArray();
                            int tabesIndex = 0;
                            int runningX = 0;
                            int runningY = 10;
                            for( int gridRow = 0; gridRow < gridRows; gridRow++ ) {
                                runningX = indent;
                                for( int gridCol = 0; gridCol < gridCols; gridCol++ ) {
                                    if (tabesIndex == tablesNum) {
                                        break;
                                    }

                                    JSONObject tableJson = new JSONObject();
                                    root.put(tableJson);
                                    TableLevel curTable = selectedTables.get(tabesIndex);
                                    int id = tabesIndex;
                                    tabesIndex++;

                                    tableJson.put("id", id);
                                    tableJson.put("x", runningX);
                                    tableJson.put("y", runningY);

                                    String fromTable = curTable.tableName;
                                    tableJson.put("name", fromTable);
                                    JSONArray fieldsArray = new JSONArray();
                                    tableJson.put("fields", fieldsArray);
                                    List<ColumnLevel> cols = curTable.columnsList;

                                    List<ColumnLevel> sortedCols = new ArrayList<ColumnLevel>();
                                    sortedCols.addAll(cols);
                                    // Collections.sort(sortedCols, new Comparator<ColumnLevel>(){
                                    // public int compare( ColumnLevel o1, ColumnLevel o2 ) {
                                    // return o1.columnName.compareToIgnoreCase(o2.columnName);
                                    // }
                                    // });

                                    for( ColumnLevel col : sortedCols ) {
                                        JSONObject colObject = new JSONObject();
                                        fieldsArray.put(colObject);
                                        colObject.put("fname", col.columnName + " (" + col.columnType + ")");
                                        if (col.references != null) {
                                            String[] tableColsFromFK = col.tableColsFromFK();
                                            String toTable = tableColsFromFK[0];
                                            String toColumn = tableColsFromFK[1];
                                            colObject.put("fk_name", toTable);
                                            colObject.put("fk_field", toColumn);
                                        }
                                    }

                                    runningX += indent + tableWidth;
                                }
                                runningY += tableHeight + indent;
                            }
                            // String string = root.toString(2);
                            
                            String json = root.toString();
                            TableGraphDialog d = new TableGraphDialog(parentShell, "Table Graph", json);
                            d.open();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                },//
        };
        return actions;
    }
    @SuppressWarnings("serial")
    private Action[] makeFKColumnAction( final ColumnLevel selectedColumn ) {
        Action[] actions = {//
                new Action("Create combined select statement", null){
                    @Override
                    public void run() {

                        String[] tableColsFromFK = selectedColumn.tableColsFromFK();
                        String refTable = tableColsFromFK[0];
                        String refColumn = tableColsFromFK[1];

                        String tableName = selectedColumn.parent.tableName;
                        String query = "SELECT t1.*, t2.* FROM " + tableName + " t1, " + refTable + " t2" + "\nWHERE t1."
                                + selectedColumn.columnName + "=t2." + refColumn;
                        addTextToQueryEditor(query);

                    }
                }, //
                new Action("Quick view other table", null){
                    @Override
                    public void run() {
                        try {
                            String[] tableColsFromFK = selectedColumn.tableColsFromFK();
                            String refTable = tableColsFromFK[0];
                            QueryResult queryResult = currentConnectedDatabase.getTableRecordsMapIn(refTable, null, true, 20);
                            createTableViewer(resultsetViewerGroup, queryResult);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, //
        };
        return actions;
    }
}
