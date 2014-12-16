/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.hydrologis.rap.geopapbrowser;

import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOGS;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOG_DATA;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_GPSLOG_PROPERTIES;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_METADATA;
import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_NOTES;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

import eu.hydrologis.rap.stage.ui.ImageServiceHandler;
import eu.hydrologis.rap.stage.utils.FileUtilities;
import eu.hydrologis.rap.stage.utils.ImageCache;
import eu.hydrologis.rap.stage.utilsrap.ImageUtil;
import eu.hydrologis.rap.stage.workspace.StageWorkspace;
import eu.hydrologis.rap.stage.workspace.User;

/**
 * The geopaparazzi view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageGeopaparazziView {

    private static final String PARAMETERS = "Parameters";
    private static final String EXECUTION_TOOLS = "Execution";
    private static final String CHARACTER_ENCODING = "Encoding";
    private static final String DEBUG_INFO = "Debug info";
    private static final String MEMORY_MB = "Memory [MB]";
    private static final String PROCESS_SETTINGS = "Process settings";
    private static final String LOAD_EXPERIMENTAL_MODULES = "Load experimental modules";
    private static final String NO_MODULE_SELECTED = "No module selected";

    private final static String SERVICE_HANDLER = "imageServiceHandler";
    private final static String IMAGE_KEY = "imageKey";

    private static final String PROJECTS = "Projects";

    private Composite projectViewComposite;
    private TreeViewer modulesViewer;

    private String filterTextString;

    private Display display;

    private static boolean hasDriver = false;
    private List<ProjectInfo> projectInfos;
    private ProjectInfo currentSelectedProject;
    private Browser infoBrowser;

    static {
        try {
            // make sure sqlite driver are there
            Class.forName("org.sqlite.JDBC");
            hasDriver = true;
        } catch (Exception e) {
        }
    }

    public void createGeopaparazziTab( Display display, Composite parent ) throws Exception {
        this.display = display;

        if (!hasDriver) {
            throw new Exception("No SQLite drivers available to read geopaparazzi projects.");
        }

        // register the service handler
        try {
            RWT.getServiceManager().registerServiceHandler(SERVICE_HANDLER, new ImageServiceHandler());
        } catch (Exception e1) {
            // TODO
        }

        File geopaparazziFolder = StageWorkspace.getInstance().getGeopaparazziFolder(User.getCurrentUserName());
        File[] projectFiles = geopaparazziFolder.listFiles(new FilenameFilter(){
            @Override
            public boolean accept( File dir, String name ) {
                return name.endsWith(".gpap");
            }
        });
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

        infoBrowser = new Browser(projectViewComposite, SWT.NONE);
        infoBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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
                    sb.append(key).append(" = ").append(value).append("<br/>");
                } else {
                    try {
                        long ts = Long.parseLong(value);
                        String dateTimeString = TimeUtilities.INSTANCE.TIME_FORMATTER_LOCAL.format(new Date(ts));
                        sb.append(key).append(" = ").append(dateTimeString).append("<br/>");
                    } catch (Exception e) {
                        sb.append(key).append(" = ").append(value).append("<br/>");
                    }
                }
            }

        }
        return sb.toString();
    }

    private void setNoProjectLabel() {
        infoBrowser.setText("<h1>No project selected</h1>");
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

        final Text filterText = new Text(modulesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterText.addModifyListener(new ModifyListener(){
            private static final long serialVersionUID = 1L;

            public void modifyText( ModifyEvent event ) {
                filterTextString = filterText.getText();
                try {
                    if (filterTextString == null || filterTextString.length() == 0) {
                        relayout(false, null);
                    } else {
                        relayout(true, filterTextString);
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
                        String projectTemplate = getProjectTemplate();

                        // substitute the notes info
                        String projectHtmlFile = setData(projectTemplate, currentSelectedProject);

                        String html = FileUtilities.readFile(projectHtmlFile);
                        // infoBrowser.setUrl("file:" + projectHtmlFile);
                        infoBrowser.setText(html);
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
                        setImage(infoBrowser, selectedImage, currentSelectedProject.databaseFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setNoProjectLabel();
                    }
                } else {
                    setNoProjectLabel();
                }
            }

        });

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
        String folderName = name.replaceFirst("\\.gpap", "");
        File projectFolderFile = new File(parentFile, folderName);
        File projectFile = new File(projectFolderFile, "project.html");
        if (projectFolderFile.exists()) {
            return projectFile.getAbsolutePath();
        } else {
            if (!projectFolderFile.mkdirs()) {
                throw new IOException("Unable to create the project data folder.");
            }
        }

        String titleName = currentSelectedProject.fileName;
        titleName = titleName.replace('_', ' ').replaceFirst("\\.gpap", "");
        projectTemplate = projectTemplate.replaceFirst("PROJECTTITLE", "PROJECT: " + titleName);

        projectTemplate = projectTemplate.replaceFirst("PROJECTMETADATA", currentSelectedProject.metadata);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {

            // NOTES

            List<String[]> noteDataList = getNotesText(connection);
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for( String[] noteData : noteDataList ) {
                // [lon, lat, altim, dateTimeString, text, descr]
                String lon = noteData[0];
                String lat = noteData[1];
                String altim = noteData[2];
                String date = noteData[3];
                String text = noteData[4];
                String descr = noteData[5];

                if (index == 0) {
                    sb.append("var firstLL = L.latLng(" + lat + ", " + lon + ");\n");
                    sb.append("var fixBounds = L.latLngBounds(firstLL, firstLL);\n");
                } else {
                    sb.append("fixBounds = fixBounds.extend(L.latLng(" + lat + ", " + lon + "));\n");
                }
                index++;

                String note = "L.marker([" + lat + ", " + lon //
                        + "], {icon: infoIcon}).addTo(map).bindPopup(\"" //
                        + "<b>Text:</b> " + text + "<br/>" //
                        + "<b>Descr:</b> " + descr + "<br/>" //
                        + "<b>Timestamp:</b> " + date + "<br/>" //
                        + "<b>Altim:</b> " + altim + "<br/>" //
                        + "\");";
                sb.append(note).append("\n");
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

        FileUtilities.writeFile(projectTemplate, projectFile);
        return projectFile.getAbsolutePath();
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

    private void setImage( Browser browser, org.jgrasstools.gears.io.geopaparazzi.geopap4.Image image, File dbFile )
            throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
            File newImageFile = File.createTempFile("stage" + new Date().getTime(), image.getName());
            byte[] imageData = DaoImages.getImageData(connection, image.getImageDataId());
            try (OutputStream outStream = new FileOutputStream(newImageFile)) {
                outStream.write(imageData);
            }

            BufferedImage bufferedImage = createImage(newImageFile);
            // store the image in the UISession for the service handler
            RWT.getUISession().setAttribute(IMAGE_KEY, bufferedImage);
            // create the HTML with a single <img> tag.
            browser.setText(createHtml(IMAGE_KEY));
        }
    }

    private BufferedImage createImage( File imageFile ) throws Exception {
        BufferedImage image = ImageIO.read(imageFile);

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int width;
        int height;
        if (imageWidth > imageHeight) {
            width = 800;
            height = imageHeight * width / imageWidth;
        } else {
            height = 800;
            width = height * imageWidth / imageHeight;
        }

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        return resizedImage;
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
