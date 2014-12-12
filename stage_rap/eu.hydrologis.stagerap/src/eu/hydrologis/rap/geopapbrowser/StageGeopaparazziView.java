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

import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_METADATA;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
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
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.MetadataTableFields;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TimeUtilities;

import eu.hydrologis.rap.stage.utils.ImageCache;
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

    private static final String PROJECTS = "Projects";

    private Composite projectViewComposite;
    private StackLayout projectViewStackLayout;
    private TreeViewer modulesViewer;

    private String filterTextString;

    private Display display;

    private static boolean hasDriver = false;
    private List<ProjectInfo> projectInfos;
    private ProjectInfo currentSelectedProject;

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
        tree.setData(RWT.CUSTOM_ITEM_HEIGHT, new Integer(150));

        Group parametersTabsGroup = new Group(mainComposite, SWT.NONE);
        GridData modulesGuiCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        modulesGuiCompositeGD.horizontalSpan = 2;
        modulesGuiCompositeGD.verticalSpan = 2;
        parametersTabsGroup.setLayoutData(modulesGuiCompositeGD);
        parametersTabsGroup.setLayout(new GridLayout(1, false));
        parametersTabsGroup.setText(PARAMETERS);

        projectViewComposite = new Composite(parametersTabsGroup, SWT.BORDER);
        projectViewStackLayout = new StackLayout();
        projectViewStackLayout.marginWidth = 15;
        projectViewStackLayout.marginHeight = 15;
        projectViewComposite.setLayout(projectViewStackLayout);
        projectViewComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label noProjectLabel = getNoProjectLabel();
        projectViewStackLayout.topControl = noProjectLabel;

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
                info.fileName = geopapDatabaseFile.getName();
                info.metadata = projectInfo;
                infoList.add(info);
            }
        }
        return infoList;
    }

    private String getProjectInfo( Connection connection ) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            String sql = "select " + MetadataTableFields.COLUMN_KEY.getFieldName() + ", " + //
                    MetadataTableFields.COLUMN_VALUE.getFieldName() + " from " + TABLE_METADATA;

            StringBuilder sb = new StringBuilder();
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

            return sb.toString();
        }
    }

    private Label getNoProjectLabel() {
        Label noModuleLabel = new Label(projectViewComposite, SWT.NONE);
        noModuleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        noModuleLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        noModuleLabel.setText("<span style='font:bold 26px Arial;'>" + NO_MODULE_SELECTED + "</span>");
        return noModuleLabel;
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
                // if (element instanceof ProjectInfo) {
                // return ImageCache.getInstance().getImage(display, ImageCache.GRID);
                // }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof ProjectInfo) {
                    ProjectInfo projectInfo = (ProjectInfo) element;
                    String fileName = projectInfo.fileName;
                    fileName = fileName.replace('_', ' ').replaceFirst("\\.gpap", "");
                    String name = "<big>" + fileName + "</big><br/>";
                    name = name + "<small>" + projectInfo.metadata + "</small>";
                    return name;
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
                    putUnselected();
                    return;
                }

                if (selectedItem instanceof ProjectInfo) {
                    currentSelectedProject = (ProjectInfo) selectedItem;

                    // projectViewStackLayout.topControl = control;
                    // projectViewComposite.layout(true);
                } else {
                    putUnselected();
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

    /**
     * Put the properties view to an label that defines no selection.
     */
    public void putUnselected() {
        Label noModuleLabel = getNoProjectLabel();
        projectViewStackLayout.topControl = noModuleLabel;
        projectViewComposite.layout(true);
    }

}
