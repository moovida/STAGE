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
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.jgrasstools.gears.spatialite.SpatialiteDb;
import org.jgrasstools.gears.spatialite.TableRecordMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utilsrap.LoginDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

/**
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SpatialiteViewerEntryPoint extends AbstractEntryPoint {
    private static final String RUN_QUERY = "Run query";
    private static final String SQL_EDITOR = "SQL Editor";
    private static final String DATA_VIEWER = "Data viewer";
    private static final String DATABASE_CONNECTIONS = "Database connections";
    private static final String NEW = "new";
    private static final String NEW_TOOLTIP = "create a new spatialite database";
    private static final String OPEN = "open";
    private static final String OPEN_TOOLTIP = "connect to an existing database";

    private static final long serialVersionUID = 1L;
    private static final String EMPTY = " - ";

    private Display display;
    private TreeViewer databaseTreeViewer;

    private SpatialiteDb currentConnectedDatabase;
    private Shell parentShell;
    private TableViewer dataTableViewer;
    private Group resultsetViewerGroup;
    private Text sqlEditorText;

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
        newDatabaseButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.NEW));
        newDatabaseButton.setToolTipText(NEW_TOOLTIP);
        newDatabaseButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                createNewDatabase(parent.getShell());
            }
        });
        ToolItem openDatabaseButton = new ToolItem(toolBar, SWT.PUSH);
        openDatabaseButton.setText(OPEN);
        openDatabaseButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.OPEN));
        openDatabaseButton.setToolTipText(OPEN_TOOLTIP);
        openDatabaseButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                openDatabase(parent.getShell());
            }
        });

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

        Group modulesListGroup = new Group(leftComposite, SWT.NONE);
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
        runQueryButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        runQueryButton.setText(RUN_QUERY);
        runQueryButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN));
        runQueryButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String sqlText = sqlEditorText.getText();
                if (currentConnectedDatabase != null && sqlText.trim().length() > 0) {
                    try {
                        List<String> columnNames = new ArrayList<String>();
                        List<TableRecordMap> tableRecordsMapFromRawSql = currentConnectedDatabase.getTableRecordsMapFromRawSql(
                                sqlText, 1000, columnNames);
                        createTableViewer(resultsetViewerGroup, tableRecordsMapFromRawSql, columnNames);
                    } catch (Exception e1) {
                        String localizedMessage = e1.getLocalizedMessage();
                        MessageDialog.openError(parentShell, "ERROR", "An error occurred: " + localizedMessage);
                    }
                }
            }
        });

        Combo oldQueriesCombo = new Combo(sqlEditorGroup, SWT.DROP_DOWN);
        oldQueriesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        oldQueriesCombo.setItems(new String[0]);

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

    private Action makeRecordViewAction( final Control parent, final IStructuredSelection selection ) {
        return new Action("View geometries", null){
            private static final long serialVersionUID = 1L;

            @Override
            public void run() {
                Iterator selectionIter = selection.iterator();
                List<Geometry> geomsList = new ArrayList<Geometry>();
                while( selectionIter.hasNext() ) {
                    TableRecordMap record = (TableRecordMap) selectionIter.next();
                    geomsList.add(record.geometry);
                    System.out.println(record.geometry.toText());
                }

            }
        };
    }

    private TableViewerColumn createColumn( TableViewer viewer, String name ) {
        TableViewerColumn result = new TableViewerColumn(viewer, SWT.NONE);
        result.setLabelProvider(new RecordLabelProvider(name));
        TableColumn column = result.getColumn();
        column.setText(name);
        column.setWidth(170);
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
                if (parentElement instanceof List< ? >) {
                    List< ? > list = (List< ? >) parentElement;
                    Object[] array = list.toArray();
                    return array;
                }
                if (parentElement instanceof SpatialiteDb) {
                    SpatialiteDb db = (SpatialiteDb) parentElement;
                    try {
                        List<String> tables = db.getUserTables(true);
                        return tables.toArray();
                    } catch (SQLException e) {
                        StageLogger.logError(SpatialiteViewerEntryPoint.this, e);
                    }
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
                if (element instanceof SpatialiteDb) {
                    return ImageCache.getInstance().getImage(display, ImageCache.DATABASE);
                }
                if (element instanceof String) {
                    return ImageCache.getInstance().getImage(display, ImageCache.TABLE);
                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof SpatialiteDb) {
                    SpatialiteDb db = (SpatialiteDb) element;
                    String databasePath = db.getDatabasePath();
                    File dbFile = new File(databasePath);
                    String dbPathRelative = StageWorkspace.makeRelativeToDataFolder(dbFile);
                    return dbPathRelative;
                }
                if (element instanceof String) {
                    return (String) element;
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
                if (selectedItem instanceof String) {
                    String selectedTable = (String) selectedItem;

                    try {
                        List<TableRecordMap> tableRecordsMapList = currentConnectedDatabase.getTableRecordsMapIn(selectedTable,
                                null, true, 20);
                        List<String> tableColumns = currentConnectedDatabase.getTableColumns(selectedTable);
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
     * 
     * @param filterText
     * 
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public void relayout( final boolean expandAll ) throws InvocationTargetException, InterruptedException {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                if (currentConnectedDatabase != null) {
                    databaseTreeViewer.setInput(Arrays.asList(currentConnectedDatabase));
                    if (expandAll)
                        databaseTreeViewer.expandAll();
                }
            }
        });
    }

    private void createNewDatabase( Shell shell ) {

    }

    private void openDatabase( Shell shell ) {
        try {
            closeCurrentDb();
        } catch (Exception e1) {
            StageLogger.logError(this, e1);
        }
        File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
        SpatialiteSelectionDialog fileDialog = new SpatialiteSelectionDialog(parentShell, userFolder);
        int returnCode = fileDialog.open();
        if (returnCode == SWT.OK) {
            File selectedFile = fileDialog.getSelectedFile();
            if (selectedFile != null) {
                try {
                    currentConnectedDatabase = new SpatialiteDb();
                    currentConnectedDatabase.open(selectedFile.getAbsolutePath());
                    relayout(true);
                } catch (Exception e) {
                    currentConnectedDatabase = null;
                    StageLogger.logError(this, e);
                }
            }
        }
    }

    private void closeCurrentDb() throws Exception {
        if (currentConnectedDatabase != null) {
            currentConnectedDatabase.close();
            currentConnectedDatabase = null;
            relayout(true);
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
