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
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.jgrasstools.gears.spatialite.SpatialiteDb;
import org.jgrasstools.gears.spatialite.SpatialiteTableNames;
import org.jgrasstools.gears.spatialite.TableRecordMap;

import com.vividsolutions.jts.geom.Geometry;

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
    private Shell parentShell;
    private TableViewer dataTableViewer;
    private Group resultsetViewerGroup;
    private Text sqlEditorText;

    private List<String> oldSqlCommands = new ArrayList<String>();
    private Menu sqlHistoryMenu;
    private Group modulesListGroup;
    private StageProgressBar generalProgressBar;

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

        // TODO fix this
        System.setProperty("java.io.tmpdir", "D:/TMP/");

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

        ToolItem runQueryButton = new ToolItem(toolBar, SWT.PUSH);
        runQueryButton.setText(RUN_QUERY);
        runQueryButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN));
        runQueryButton.setToolTipText(RUN_QUERY_TOOLTIP);
        runQueryButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                String sqlText = sqlEditorText.getText();
                if (currentConnectedDatabase != null && sqlText.trim().length() > 0) {
                    try {
                        List<String> columnNames = new ArrayList<String>();
                        List<TableRecordMap> tableRecordsMapFromRawSql = currentConnectedDatabase.getTableRecordsMapFromRawSql(
                                sqlText, 1000, columnNames);
                        createTableViewer(resultsetViewerGroup, tableRecordsMapFromRawSql, columnNames);

                        addQueryToHistoryCombo(sqlText);

                    } catch (Exception e1) {
                        String localizedMessage = e1.getLocalizedMessage();
                        MessageDialog.openError(parentShell, "ERROR", "An error occurred: " + localizedMessage);
                    }
                }
            }
        });

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
        sqlEditorGroup.setLayout(new GridLayout(1, false));
        sqlEditorGroup.setText(SQL_EDITOR);

        sqlEditorText = new Text(sqlEditorGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData sqlEditorTextGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        sqlEditorTextGD.heightHint = 100;
        sqlEditorTextGD.horizontalSpan = 2;
        sqlEditorText.setLayoutData(sqlEditorTextGD);
        sqlEditorText.setText("");

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
                        currentConnectedDatabase.createTableFromShp(selectedFile);
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
                // TODO create
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
                    String text = sqlEditorText.getText();
                    if (text.trim().length() != 0) {
                        text += "\n";
                    }
                    text += query;
                    sqlEditorText.setText(text);
                }
            });
        }
    }

    private void createTableViewer( Composite parent, List<TableRecordMap> tableRecordsMapList, List<String> tableColumns )
            throws Exception {
        if (dataTableViewer != null)
            dataTableViewer.getControl().dispose();
        dataTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        dataTableViewer.setContentProvider(ArrayContentProvider.getInstance());

        String geomFieldName = currentConnectedDatabase.getGeomFieldName();
        if (tableRecordsMapList.size() > 0) {
            TableRecordMap tableRecordMap = tableRecordsMapList.get(0);
            if (tableRecordMap.geometry != null) {
                createColumn(dataTableViewer, geomFieldName);
            }
            for( String key : tableRecordMap.data.keySet() ) {
                createColumn(dataTableViewer, key);
            }
        } else {
            // supply a header anyways
            if (tableColumns.contains(geomFieldName)) {
                createColumn(dataTableViewer, geomFieldName);
            }
            for( String column : tableColumns ) {
                if (!column.equals(geomFieldName))
                    createColumn(dataTableViewer, column);
            }
        }

        dataTableViewer.getTable().setHeaderVisible(true);
        dataTableViewer.getTable().setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataTableViewer.getTable().setLayoutData(tableData);

        dataTableViewer.setInput(tableRecordsMapList);
        parent.layout();
    }

    private TableViewerColumn createColumn( TableViewer viewer, String name ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new RecordLabelProvider(name));
        TableColumn column = result.getColumn();
        column.setText(name);
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
                    return columnLevel.columnName;
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

                Object selectedItem = sel.getFirstElement();
                if (selectedItem instanceof TableLevel) {
                    TableLevel selectedTable = (TableLevel) selectedItem;

                    try {
                        List<TableRecordMap> tableRecordsMapList = currentConnectedDatabase.getTableRecordsMapIn(
                                selectedTable.tableName, null, true, 20);
                        List<String> tableColumns = currentConnectedDatabase.getTableColumns(selectedTable.tableName);
                        createTableViewer(resultsetViewerGroup, tableRecordsMapList, tableColumns);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

        });

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
    public void relayout( final DbLevel dbLevel, final boolean expandAll ) throws InvocationTargetException, InterruptedException {
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
        DbLevel dbLevel = new DbLevel();
        String databasePath = db.getDatabasePath();
        File dbFile = new File(databasePath);
        String dbPathRelative = StageWorkspace.makeRelativeToDataFolder(dbFile);
        dbLevel.dbName = dbPathRelative;

        HashMap<String, List<String>> currentDatabaseTablesMap = db.getTablesMap(true);
        for( String typeName : SpatialiteTableNames.ALL_TYPES_LIST ) {
            TypeLevel typeLevel = new TypeLevel();
            typeLevel.typeName = typeName;
            List<String> tablesList = currentDatabaseTablesMap.get(typeName);
            for( String tableName : tablesList ) {
                TableLevel tableLevel = new TableLevel();
                tableLevel.tableName = tableName;
                tableLevel.isGeo = db.isTableGeometric(tableName);
                List<String> tableColumns = db.getTableColumns(tableName);
                for( String coumnName : tableColumns ) {
                    ColumnLevel columnLevel = new ColumnLevel();
                    columnLevel.columnName = coumnName;
                    tableLevel.columnsList.add(columnLevel);
                }
                typeLevel.tablesList.add(tableLevel);
            }
            dbLevel.typesList.add(typeLevel);
        }
        return dbLevel;
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
        private String columnName;
        public RecordLabelProvider( String columnName ) {
            this.columnName = columnName;
        }
        @Override
        public String getText( Object element ) {
            if (element instanceof TableRecordMap) {
                TableRecordMap tableRecordMap = (TableRecordMap) element;
                if (columnName.equals(currentConnectedDatabase.getGeomFieldName())) {
                    Geometry geometry = tableRecordMap.geometry;
                    if (geometry == null)
                        return "";
                    return geometry.toText();
                }
                return tableRecordMap.data.get(columnName).toString();
            }
            return " - ";
        }
    }
}
