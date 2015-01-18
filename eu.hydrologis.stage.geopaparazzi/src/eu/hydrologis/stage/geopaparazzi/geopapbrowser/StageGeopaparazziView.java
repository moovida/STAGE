/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.geopaparazzi.geopapbrowser;

import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOGS;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOG_DATA;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOG_PROPERTIES;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_METADATA;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_NOTES;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoImages;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.GpsLogsDataTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.GpsLogsPropertiesTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.GpsLogsTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.MetadataTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.NotesTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TimeUtilities;

import eu.hydrologis.stage.geopaparazzi.geopapbrowser.functions.OpenImageFunction;
import eu.hydrologis.stage.geopaparazzi.geopapbrowser.utils.GeopaparazziImageUtils;
import eu.hydrologis.stage.geopaparazzi.geopapbrowser.utils.GeopaparazziWorkspaceUtilities;
import eu.hydrologis.stage.libs.utils.FileUtilities;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.DownloadUtils;
import eu.hydrologis.stage.libs.utilsrap.ExampleUtil;
import eu.hydrologis.stage.libs.utilsrap.ImageServiceHandler;
import eu.hydrologis.stage.libs.utilsrap.ImageUtil;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

/**
 * The geopaparazzi view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageGeopaparazziView {

    private static final String PARAMETERS = "Parameters";

    private final static String SERVICE_HANDLER = "imageServiceHandler";
    private final static String IMAGE_KEY = "imageKey";

    private static final String PROJECTS = "Projects";
    private static final String INFO = "Info";

    private Composite projectViewComposite;
    private TreeViewer modulesViewer;

    private String filterTextString;

    private Display display;

    private static boolean hasDriver = false;
    private List<ProjectInfo> projectInfos;
    private ProjectInfo currentSelectedProject;
    private Browser dataBrowser;
    private boolean CACHE_HTML_TO_FILE;

    private Browser infoBrowser;

    private FileUpload gpFileUpload;
    private Label gpFileNameLabel;
    private Button gpUploadButton;
    private ServerPushSession gpPushSession;

    static {
        try {
            // make sure sqlite driver are there
            Class.forName("org.sqlite.JDBC");
            hasDriver = true;
        } catch (Exception e) {
        }

        // register the service handler
        try {
            RWT.getServiceManager().registerServiceHandler(SERVICE_HANDLER, new ImageServiceHandler());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void createGeopaparazziTab( Display display, Composite parent ) throws Exception {
        this.display = display;

        if (!hasDriver) {
            throw new Exception("No SQLite drivers available to read geopaparazzi projects.");
        }

        File[] projectFiles = GeopaparazziWorkspaceUtilities.getGeopaparazziProjectFiles(null);
        projectInfos = readProjectInfos(projectFiles);

        SashForm mainComposite = new SashForm(parent, SWT.HORIZONTAL);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite leftComposite = new Composite(mainComposite, SWT.None);
        GridLayout leftLayout = new GridLayout(1, true);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        leftComposite.setLayout(leftLayout);
        GridData leftGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        leftComposite.setLayoutData(leftGD);

        Group projectsListGroup = new Group(leftComposite, SWT.NONE);
        projectsListGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        projectsListGroup.setLayout(new GridLayout(2, false));
        projectsListGroup.setText(PROJECTS);

        addTextFilter(projectsListGroup);
        modulesViewer = createTreeViewer(projectsListGroup);
        // modulesViewer.setInput(projectInfos);
        Tree tree = modulesViewer.getTree();
        tree.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        // tree.setData(RWT.CUSTOM_ITEM_HEIGHT, new Integer(150));

        // THE UPLOAD GROUOP
        Group uploadGroup = new Group(leftComposite, SWT.NONE);
        GridData uploadGroupGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        uploadGroup.setLayoutData(uploadGroupGD);
        uploadGroup.setLayout(new GridLayout(2, false));
        uploadGroup.setText("Project Upload");

        gpFileUpload = new FileUpload(uploadGroup, SWT.NONE);
        gpFileUpload.setText("Select project to upload");
        gpFileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        gpFileNameLabel = new Label(uploadGroup, SWT.NONE);
        gpFileNameLabel.setText("no file selected");
        gpFileNameLabel.setLayoutData(ExampleUtil.createHorzFillData());
        gpFileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        gpUploadButton = new Button(uploadGroup, SWT.PUSH);
        gpUploadButton.setText("Upload file");
        gpUploadButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        new Label(uploadGroup, SWT.NONE);
        gpFileUpload.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String fileName = gpFileUpload.getFileName();
                gpFileNameLabel.setText(fileName == null ? "" : fileName);
            }
        });
        final String url = startGpUploadReceiver();
        gpPushSession = new ServerPushSession();
        gpUploadButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                gpPushSession.start();
                gpFileUpload.submit(url);
            }
        });

        // THE INFO GROUOP
        Group infoGroup = new Group(leftComposite, SWT.NONE);
        GridData infoGroupGD = new GridData(SWT.FILL, SWT.FILL, true, false);
        infoGroupGD.heightHint = 200;
        infoGroup.setLayoutData(infoGroupGD);
        infoGroup.setLayout(new GridLayout(1, false));
        infoGroup.setText(INFO);

        infoBrowser = new Browser(infoGroup, SWT.NONE);
        infoBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        // infoLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        infoBrowser.setText("");

        Group parametersTabsGroup = new Group(mainComposite, SWT.NONE);
        GridData modulesGuiCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        modulesGuiCompositeGD.horizontalSpan = 2;
        modulesGuiCompositeGD.verticalSpan = 2;
        parametersTabsGroup.setLayoutData(modulesGuiCompositeGD);
        parametersTabsGroup.setLayout(new GridLayout(1, false));
        parametersTabsGroup.setText(PARAMETERS);

        projectViewComposite = new Composite(parametersTabsGroup, SWT.BORDER);
        projectViewComposite.setLayout(new GridLayout(1, false));
        projectViewComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        dataBrowser = new Browser(projectViewComposite, SWT.NONE);
        dataBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        setNoProjectLabel();

        mainComposite.setWeights(new int[]{1, 2});
        try {
            relayout(false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<ProjectInfo> readProjectInfos( File[] projectFiles ) throws Exception {
        List<ProjectInfo> infoList = new ArrayList<ProjectInfo>();
        for( File geopapDatabaseFile : projectFiles ) {
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + geopapDatabaseFile.getAbsolutePath())) {
                String projectInfo = getProjectInfo(connection);
                ProjectInfo info = new ProjectInfo();
                info.databaseFile = geopapDatabaseFile;
                info.fileName = geopapDatabaseFile.getName();
                info.metadata = projectInfo;

                List<org.jgrasstools.gears.io.geopaparazzi.geopap4.Image> imagesList = DaoImages.getImagesList(connection);
                info.images = imagesList.toArray(new org.jgrasstools.gears.io.geopaparazzi.geopap4.Image[0]);
                infoList.add(info);
            }
        }
        return infoList;
    }

    private String getProjectInfo( Connection connection ) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            String sql = "select " + MetadataTableFields.COLUMN_KEY.getFieldName() + ", " + //
                    MetadataTableFields.COLUMN_VALUE.getFieldName() + " from " + TABLE_METADATA;

            ResultSet rs = statement.executeQuery(sql);
            while( rs.next() ) {
                String key = rs.getString(MetadataTableFields.COLUMN_KEY.getFieldName());
                String value = rs.getString(MetadataTableFields.COLUMN_VALUE.getFieldName());

                if (!key.endsWith("ts")) {
                    sb.append("<b>").append(key).append(":</b> ").append(StageUtils.escapeHTML(value)).append("<br/>");
                } else {
                    try {
                        long ts = Long.parseLong(value);
                        String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(ts));
                        sb.append("<b>").append(key).append(":</b> ").append(dateTimeString).append("<br/>");
                    } catch (Exception e) {
                        sb.append("<b>").append(key).append(":</b> ").append(StageUtils.escapeHTML(value)).append("<br/>");
                    }
                }
            }

        }
        return sb.toString();
    }

    private void setNoProjectLabel() {
        dataBrowser.setText("<h1>No project selected</h1>");
        // Label noModuleLabel = new Label(projectViewComposite, SWT.NONE);
        // noModuleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        // noModuleLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        // noModuleLabel.setText("<span style='font:bold 26px Arial;'>" + NO_MODULE_SELECTED +
        // "</span>");
        // return noModuleLabel;
    }

    private void addTextFilter( Composite modulesComposite ) {

        Label filterLabel = new Label(modulesComposite, SWT.NONE);
        filterLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        filterLabel.setText("Filter");

        final Text filterText = new Text(modulesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterText.addModifyListener(new ModifyListener(){
            private static final long serialVersionUID = 1L;

            public void modifyText( ModifyEvent event ) {
                filterTextString = filterText.getText();
                try {
                    if (filterTextString == null || filterTextString.length() == 0) {
                        relayout(false, null);
                    } else {
                        relayout(false, filterTextString);
                    }
                } catch (InvocationTargetException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private TreeViewer createTreeViewer( Composite modulesComposite ) {

        // FIXME
        // PatternFilter patternFilter = new PatternFilter();
        // final FilteredTree filter = new FilteredTree(modulesComposite,
        // SWT.SINGLE | SWT.BORDER, patternFilter, true);
        // final TreeViewer modulesViewer = filter.getViewer();
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
                if (parentElement instanceof ProjectInfo) {
                    ProjectInfo info = (ProjectInfo) parentElement;
                    return info.images;
                }
                return new Object[0];
            }

            public Object getParent( Object element ) {
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
                if (element instanceof ProjectInfo) {
                    return ImageCache.getInstance().getImage(display, ImageCache.DATABASE);
                }
                if (element instanceof org.jgrasstools.gears.io.geopaparazzi.geopap4.Image) {
                    return ImageCache.getInstance().getImage(display, ImageCache.DBIMAGE);
                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof ProjectInfo) {
                    ProjectInfo projectInfo = (ProjectInfo) element;
                    String fileName = projectInfo.fileName;
                    fileName = fileName.replace('_', ' ').replaceFirst("\\.gpap", "");
                    String name = "<big>" + fileName + "</big><br/>";
                    return name;
                }
                if (element instanceof org.jgrasstools.gears.io.geopaparazzi.geopap4.Image) {
                    org.jgrasstools.gears.io.geopaparazzi.geopap4.Image image = (org.jgrasstools.gears.io.geopaparazzi.geopap4.Image) element;
                    String imageName = image.getName();
                    return imageName;
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
                if (selectedItem == null) {
                    // unselected, show empty panel
                    setNoProjectLabel();
                    return;
                }

                if (selectedItem instanceof ProjectInfo) {
                    currentSelectedProject = (ProjectInfo) selectedItem;
                    try {
                        /*
                         * set the info view
                         */
                        String titleName = currentSelectedProject.fileName;
                        titleName = titleName.replace('_', ' ').replaceFirst("\\.gpap", "");
                        String text = titleName + "<br/><br/>" + currentSelectedProject.metadata;

                        infoBrowser.setText(text);

                        /*
                         * set the project view
                         */
                        String projectTemplate = getProjectTemplate();
                        // substitute the notes info
                        String projectHtml = setData(projectTemplate, currentSelectedProject);
                        if (CACHE_HTML_TO_FILE) {
                            projectHtml = FileUtilities.readFile(projectHtml);
                        }
                        // infoBrowser.setUrl("file:" + projectHtmlFile);
                        dataBrowser.setText(projectHtml);

                        if (projectHtml.contains("openGpImage")) {
                            new OpenImageFunction(dataBrowser, "openGpImage", currentSelectedProject.databaseFile);
                            // infoBrowser.addProgressListener(new ProgressListener(){
                            // @Override
                            // public void completed( ProgressEvent event ) {
                            // infoBrowser.addLocationListener(new LocationAdapter(){
                            // @Override
                            // public void changed( LocationEvent event ) {
                            // infoBrowser.removeLocationListener(this);
                            // System.out.println("left java function-aware page, so disposed CustomFunction");
                            // openImageFunction.dispose();
                            // }
                            // });
                            // }
                            // @Override
                            // public void changed( ProgressEvent event ) {
                            // }
                            // });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // projectViewStackLayout.topControl = control;
                    // projectViewComposite.layout(true);
                } else if (selectedItem instanceof org.jgrasstools.gears.io.geopaparazzi.geopap4.Image) {
                    org.jgrasstools.gears.io.geopaparazzi.geopap4.Image selectedImage = (org.jgrasstools.gears.io.geopaparazzi.geopap4.Image) selectedItem;
                    for( ProjectInfo projectInfo : projectInfos ) {
                        for( org.jgrasstools.gears.io.geopaparazzi.geopap4.Image tmpImage : projectInfo.images ) {
                            if (tmpImage.equals(selectedImage)) {
                                currentSelectedProject = projectInfo;
                                break;
                            }
                        }
                    }

                    try {
                        String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(selectedImage.getTs()));
                        String picInfo = "<b>Image:</b> " + StageUtils.escapeHTML(selectedImage.getName()) + "<br/>" //
                                + "<b>Timestamp:</b> " + dateTimeString + "<br/>" //
                                + "<b>Azimuth:</b> " + (int) selectedImage.getAzim() + " deg<br/>" //
                                + "<b>Altim:</b> " + (int) selectedImage.getAltim() + " m<br/>";
                        infoBrowser.setText(picInfo);

                        GeopaparazziImageUtils.setImageInBrowser(dataBrowser, selectedImage.getId(), selectedImage.getName(),
                                currentSelectedProject.databaseFile, IMAGE_KEY, SERVICE_HANDLER);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setNoProjectLabel();
                    }
                } else {
                    setNoProjectLabel();
                }
            }

        });

        // final ImageDescriptor exportImageDescr =
        // ImageDescriptor.createFromImage(ImageCache.getInstance().getImage(display,
        // ImageCache.EXPORT));
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

                    if (selectedItem instanceof ProjectInfo) {
                        manager.add(new Action("Download", null){
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void run() {
                                ProjectInfo selectedProject = (ProjectInfo) selectedItem;
                                File dbFile = selectedProject.databaseFile;
                                if (dbFile != null && dbFile.exists() && !dbFile.isDirectory()) {
                                    try {
                                        new DownloadUtils().sendDownload(modulesViewer.getControl().getShell(), dbFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        manager.setRemoveAllWhenShown(true);
        return modulesViewer;
    }
    /**
     * Extract data from the db and add them to the map view.
     * 
     * @param projectTemplate
     * @return
     * @throws Exception 
     */
    private String setData( String projectTemplate, ProjectInfo currentSelectedProject ) throws Exception {
        File dbFile = currentSelectedProject.databaseFile;
        File parentFile = dbFile.getParentFile();
        String name = dbFile.getName();

        File projectFile = null;
        if (CACHE_HTML_TO_FILE) {
            String folderName = name.replaceFirst("\\.gpap", "");
            File projectFolderFile = new File(parentFile, folderName);
            projectFile = new File(projectFolderFile, "project.html");
            if (projectFolderFile.exists()) {
                return projectFile.getAbsolutePath();
            } else {
                if (!projectFolderFile.mkdirs()) {
                    throw new IOException("Unable to create the project data folder.");
                }
            }
        }

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {

            // NOTES

            List<String[]> noteDataList = getNotesText(connection);
            StringBuilder sb = new StringBuilder();
            sb.append("\n\n// GP NOTES\n");
            int index = 0;
            for( String[] noteData : noteDataList ) {
                // [lon, lat, altim, dateTimeString, text, descr]
                String lon = noteData[0];
                String lat = noteData[1];
                String altim = noteData[2];
                String date = noteData[3];
                String text = noteData[4];
                String descr = noteData[5];

                sb.append("var ll" + index + " = L.latLng(" + lat + ", " + lon + ");\n");
                if (index == 0) {
                    sb.append("fixBounds = L.latLngBounds(ll" + index + ", ll" + index + ");\n");
                } else {
                    sb.append("fixBounds = fixBounds.extend(ll" + index + ");\n");
                }

                String note = "L.marker(ll" + index //
                        + ", {icon: infoIcon}).addTo(map).bindPopup(\"" //
                        + "<b>Text:</b> " + text + "<br/>" //
                        + "<b>Descr:</b> " + descr + "<br/>" //
                        + "<b>Timestamp:</b> " + date + "<br/>" //
                        + "<b>Altim:</b> " + altim + "<br/>" //
                        + "\");";
                sb.append(note).append("\n");
                index++;
            }

            /*
             * IMAGES
             */
            sb.append("\n\n// GP IMAGE NOTES\n");
            for( org.jgrasstools.gears.io.geopaparazzi.geopap4.Image image : currentSelectedProject.images ) {
                sb.append("var ll" + index + " = L.latLng(" + image.getLat() + ", " + image.getLon() + ");\n");
                String pic = "var marker" + index + " =L.marker(ll" + index + ", {icon: photoIcon});";
                sb.append(pic).append("\n");
                String function = "marker" + index + ".on('click', function(e){\nopenGpImage(" + //
                        image.getId() + ",'" + image.getName() + "')\n});";
                sb.append(function).append("\n");
                sb.append("marker" + index + ".addTo(map);").append("\n");
                if (index == 0) {
                    sb.append("fixBounds = L.latLngBounds(ll" + index + ", ll" + index + ");\n");
                } else {
                    sb.append("fixBounds = fixBounds.extend(ll" + index + ");\n");
                }
                index++;
            }

            projectTemplate = projectTemplate.replaceFirst("//MARKERS", sb.toString());

            // TRACKS
            StringBuilder sbLogs = new StringBuilder();
            // [51.509, -0.08],
            // [51.503, -0.06],
            // [51.51, -0.047]
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30); // set timeout to 30 sec.

                String sql = "select " + //
                        GpsLogsTableFields.COLUMN_ID.getFieldName() + "," + //
                        GpsLogsTableFields.COLUMN_LOG_STARTTS.getFieldName() + "," + //
                        GpsLogsTableFields.COLUMN_LOG_ENDTS.getFieldName() + "," + //
                        GpsLogsTableFields.COLUMN_LOG_TEXT.getFieldName() + //
                        " from " + TABLE_GPSLOGS; //

                // first get the logs
                ResultSet rs = statement.executeQuery(sql);
                int lineIndex = 0;
                while( rs.next() ) {
                    long id = rs.getLong(1);

                    long startDateTime = rs.getLong(2);
                    String startDateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(startDateTime));
                    long endDateTime = rs.getLong(3);
                    String endDateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(endDateTime));
                    String text = rs.getString(4);

                    sbLogs.append("var polyline" + lineIndex + " = L.polyline([\n");

                    // points
                    String query = "select " //
                            + GpsLogsDataTableFields.COLUMN_DATA_LAT.getFieldName()
                            + ","
                            + GpsLogsDataTableFields.COLUMN_DATA_LON.getFieldName()
                            + ","
                            + GpsLogsDataTableFields.COLUMN_DATA_TS.getFieldName()//
                            + " from " + TABLE_GPSLOG_DATA + " where "
                            + //
                            GpsLogsDataTableFields.COLUMN_LOGID.getFieldName() + " = " + id
                            + " order by "
                            + GpsLogsDataTableFields.COLUMN_DATA_TS.getFieldName();

                    try (Statement newStatement = connection.createStatement()) {
                        newStatement.setQueryTimeout(30);
                        ResultSet result = newStatement.executeQuery(query);

                        while( result.next() ) {
                            double lat = result.getDouble(1);
                            double lon = result.getDouble(2);
                            sbLogs.append("[" + lat + ", " + lon + "],\n");
                        }
                    }

                    sbLogs.append("], {color: '");
                    // color
                    String colorQuery = "select " //
                            + GpsLogsPropertiesTableFields.COLUMN_PROPERTIES_COLOR.getFieldName()
                            + " from "
                            + TABLE_GPSLOG_PROPERTIES + " where " + //
                            GpsLogsPropertiesTableFields.COLUMN_LOGID.getFieldName() + " = " + id;

                    String color = "red";
                    try (Statement newStatement = connection.createStatement()) {
                        newStatement.setQueryTimeout(30);
                        ResultSet result = newStatement.executeQuery(colorQuery);

                        if (result.next()) {
                            color = result.getString(1);
                        }
                        if (color == null || color.length() == 0) {
                            color = "red";
                        }
                    }

                    sbLogs.append(color + "'}).addTo(map);\n");
                    sbLogs.append("polyline" + lineIndex + ".bindPopup(\"" //
                            + "<b>Name:</b> " + text + "<br/>" //
                            + "<b>Start</b>: " + startDateTimeString + "<br/>" //
                            + "<b>End</b>: " + endDateTimeString + "<br/>" //
                            + "\");\n");

                    sbLogs.append("if(!fixBounds){ fixBounds = polyline" + lineIndex + ".getBounds();}\n");
                    sbLogs.append("fixBounds = fixBounds.extend(polyline" + lineIndex + ".getBounds());\n");
                    lineIndex++;
                }

            }

            projectTemplate = projectTemplate.replaceFirst("//LOGS", sbLogs.toString());

            // IMAGES

            // // IMAGETABLE
            //
            // List<org.jgrasstools.gears.io.geopaparazzi.geopap4.Image> imagesList =
            // DaoImages.getImagesList(connection);
            // for( org.jgrasstools.gears.io.geopaparazzi.geopap4.Image image : imagesList ) {
            // File newImageFile = new File(mediaFolderFile, image.getName());
            //
            // byte[] imageData = DaoImages.getImageData(connection, image.getImageDataId());
            //
            // try (OutputStream outStream = new FileOutputStream(newImageFile)) {
            // outStream.write(imageData);
            // }
            //
            // Point point = gf.createPoint(new Coordinate(image.getLon(), image.getLat()));
            // long ts = image.getTs();
            // String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new
            // Date(ts));
            //
            // String imageRelativePath = mediaFolderFile.getName() + "/" + image.getName();
            // Object[] values = new Object[]{point, image.getAltim(), dateTimeString,
            // image.getAzim(), imageRelativePath};
            //
            // SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
            // builder.addAll(values);
            // SimpleFeature feature = builder.buildFeature(null);
            // newCollection.add(feature);
            // pm.worked(1);
            // }

        }

        if (CACHE_HTML_TO_FILE) {
            FileUtilities.writeFile(projectTemplate, projectFile);
            return projectFile.getAbsolutePath();
        } else {
            return projectTemplate;
        }
    }
    /**
     * Resfresh the viewer.
     * 
     * @param filterText
     * 
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public void relayout( final boolean expandAll, final String filterText ) throws InvocationTargetException,
            InterruptedException {
        final List<ProjectInfo> filtered = new ArrayList<ProjectInfo>();
        if (filterText == null) {
            filtered.addAll(projectInfos);
        } else {
            for( ProjectInfo projectInfo : projectInfos ) {
                if (projectInfo.fileName.contains(filterText) || projectInfo.metadata.contains(filterText)) {
                    filtered.add(projectInfo);
                }
            }
        }

        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                modulesViewer.setInput(filtered);
                if (expandAll)
                    modulesViewer.expandAll();
            }
        });
    }

    public static String getProjectTemplate() throws Exception {
        ClassLoader classLoader = ImageUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("resources/project.html");
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while( (line = reader.readLine()) != null ) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * @param connection
     * @return the list of [lon, lat, altim, dateTimeString, text, descr]
     * @throws Exception
     */
    public static List<String[]> getNotesText( Connection connection ) throws Exception {
        String textFN = NotesTableFields.COLUMN_TEXT.getFieldName();
        String descFN = NotesTableFields.COLUMN_DESCRIPTION.getFieldName();
        String tsFN = NotesTableFields.COLUMN_TS.getFieldName();
        String altimFN = NotesTableFields.COLUMN_ALTIM.getFieldName();
        String latFN = NotesTableFields.COLUMN_LAT.getFieldName();
        String lonFN = NotesTableFields.COLUMN_LON.getFieldName();

        String sql = "select " + //
                latFN + "," + //
                lonFN + "," + //
                altimFN + "," + //
                tsFN + "," + //
                textFN + "," + //
                descFN + " from " + //
                TABLE_NOTES;

        List<String[]> notesDescriptionList = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.
            ResultSet rs = statement.executeQuery(sql);
            while( rs.next() ) {
                double lat = rs.getDouble(latFN);
                double lon = rs.getDouble(lonFN);
                double altim = rs.getDouble(altimFN);
                long ts = rs.getLong(tsFN);
                String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(ts));
                String text = rs.getString(textFN);
                String descr = rs.getString(descFN);
                if (descr == null)
                    descr = "";

                if (lat == 0 || lon == 0) {
                    continue;
                }

                notesDescriptionList.add(new String[]{//
                        String.valueOf(lon),//
                                String.valueOf(lat),//
                                String.valueOf(altim),//
                                dateTimeString,//
                                text,//
                                descr//
                        });

            }

        }
        return notesDescriptionList;
    }

    private String startGpUploadReceiver() {
        DiskFileUploadReceiver receiver = new DiskFileUploadReceiver(){
            @Override
            protected File createTargetFile( FileDetails details ) throws IOException {
                String fileName = "upload.tmp";
                if (details != null && details.getFileName() != null) {
                    fileName = details.getFileName();
                }

                File dataFolder = StageWorkspace.getInstance().getGeopaparazziFolder(User.getCurrentUserName());
                File result = new File(dataFolder, fileName);
                result.createNewFile();
                return result;
            }
        };
        FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
        uploadHandler.addUploadListener(new FileUploadListener(){

            public void uploadProgress( FileUploadEvent event ) {
                // handle upload progress
            }

            public void uploadFailed( FileUploadEvent event ) {
                final Exception ex = event.getException();
                display.syncExec(new Runnable(){
                    public void run() {
                        MessageDialog.openWarning(gpFileNameLabel.getShell(), "ERROR",
                                "File upload failed with error: " + ex.getLocalizedMessage());
                    }
                });

                ex.printStackTrace();
            }

            public void uploadFinished( FileUploadEvent event ) {
                if (gpPushSession != null)
                    gpPushSession.stop();
                StringBuilder sb = new StringBuilder();
                for( FileDetails file : event.getFileDetails() ) {
                    sb.append(",").append(file.getFileName());
                }
                final String filesStr = sb.substring(1);
                display.syncExec(new Runnable(){
                    public void run() {
                        File[] projectFiles = GeopaparazziWorkspaceUtilities.getGeopaparazziProjectFiles(null);
                        try {
                            projectInfos = readProjectInfos(projectFiles);
                            relayout(false, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MessageDialog.openInformation(gpFileNameLabel.getShell(), "INFORMATION", "Successfully uploaded: "
                                + filesStr);
                    }
                });
            }
        });
        return uploadHandler.getUploadUrl();
    }

}
