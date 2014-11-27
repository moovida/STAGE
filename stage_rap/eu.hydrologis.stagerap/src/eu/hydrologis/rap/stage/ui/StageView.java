/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.hydrologis.rap.stage.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.apache.batik.css.parser.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.rap.stage.StageSessionPluginSingleton;
import eu.hydrologis.rap.stage.core.ModuleDescription;
import eu.hydrologis.rap.stage.core.ModuleDescription.Status;
import eu.hydrologis.rap.stage.core.StageModulesManager;
import eu.hydrologis.rap.stage.core.ScriptHandler;
import eu.hydrologis.rap.stage.utils.ImageCache;
import eu.hydrologis.rap.stage.utils.StageConstants;
import eu.hydrologis.rap.stage.utils.ViewerFolder;
import eu.hydrologis.rap.stage.utils.ViewerModule;
import eu.hydrologis.rap.stage.widgets.ModuleGui;

/**
 * The database view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class StageView {

    private static final String CHARACTER_ENCODING = "Character Encoding";
    private static final String DEBUG_INFO = "Debug info";
    private static final String MEMORY_MB = "Memory [MB]";
    private static final String PROCESS_SETTINGS = "Process settings";
    private static final String LOAD_EXPERIMENTAL_MODULES = "Load experimental modules";
    private static final String NO_MODULE_SELECTED = "No module selected";
    private static final String MODULES = "Modules";
    public static final String SPATIAL_TOOLBOX = "Spatial Toolbox...";
    public static final String LOADING_MODULES_FROM_LIBRARIES = "Loading modules from libraries...";

    public static final String ID = "eu.udig.omsbox.view.OmsBoxView"; //$NON-NLS-1$

    private Composite modulesGuiComposite;
    private StackLayout modulesGuiStackLayout;
    private TreeViewer modulesViewer;

    private ModuleGui currentSelectedModuleGui;
    private String filterTextString;

    private HashMap<String, Control> module2GuiMap = new HashMap<String, Control>();
    private Display display;

    public StageView() {

    }

    public void createPartControl( Display display, Composite parent ) throws IOException {
        this.display = display;
        module2GuiMap.clear();

        Composite mainComposite = new Composite(parent, SWT.None);
        GridLayout mainLayout = new GridLayout(3, true);
        mainLayout.marginWidth = 25;
        mainLayout.marginHeight = 25;
        mainComposite.setLayout(mainLayout);
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        Group modulesListGroup = new Group(mainComposite, SWT.NONE);
        modulesListGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        modulesListGroup.setLayout(new GridLayout(2, false));
        modulesListGroup.setText(MODULES);

        addTextFilter(modulesListGroup);
        modulesViewer = createTreeViewer(modulesListGroup);
        List<ViewerFolder> viewerFolders = new ArrayList<ViewerFolder>(); // ViewerFolder.hashmap2ViewerFolders(availableModules);
        modulesViewer.setInput(viewerFolders);
        addFilterButtons(modulesListGroup);

        Group parametersTabsGroup = new Group(mainComposite, SWT.NONE);
        GridData modulesGuiCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        modulesGuiCompositeGD.horizontalSpan = 2;
        modulesGuiCompositeGD.verticalSpan = 2;
        parametersTabsGroup.setLayoutData(modulesGuiCompositeGD);
        parametersTabsGroup.setLayout(new GridLayout(1, false));
        parametersTabsGroup.setText("Parameters");

        modulesGuiComposite = new Composite(parametersTabsGroup, SWT.BORDER);
        modulesGuiStackLayout = new StackLayout();
        modulesGuiStackLayout.marginWidth = 15;
        modulesGuiStackLayout.marginHeight = 15;
        modulesGuiComposite.setLayout(modulesGuiStackLayout);
        modulesGuiComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label noModuleLabel = new Label(modulesGuiComposite, SWT.NONE);
        noModuleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        noModuleLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
        noModuleLabel.setText("<span style='font:bold 26px Arial;'>" + NO_MODULE_SELECTED + "</span>");

        modulesGuiStackLayout.topControl = noModuleLabel;

        addQuickSettings(mainComposite);
        try {
            relayout(false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addTextFilter( Composite modulesComposite ) {

        Label filterLabel = new Label(modulesComposite, SWT.NONE);
        filterLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        filterLabel.setText("Filter");

        final Text filterText = new Text(modulesComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterText.addModifyListener(new ModifyListener(){

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
            public Object[] getElements( Object inputElement ) {
                return getChildren(inputElement);
            }

            public Object[] getChildren( Object parentElement ) {
                if (parentElement instanceof List< ? >) {
                    List< ? > list = (List< ? >) parentElement;
                    Object[] array = list.toArray();
                    return array;
                }
                if (parentElement instanceof ViewerFolder) {
                    ViewerFolder folder = (ViewerFolder) parentElement;
                    List<ViewerFolder> subFolders = folder.getSubFolders();
                    List<ViewerModule> modules = folder.getModules();
                    List<Object> allObjs = new ArrayList<Object>();
                    allObjs.addAll(subFolders);
                    allObjs.addAll(modules);

                    return allObjs.toArray();
                }
                return new Object[0];
            }

            public Object getParent( Object element ) {
                if (element instanceof ViewerFolder) {
                    ViewerFolder folder = (ViewerFolder) element;
                    return folder.getParentFolder();
                }
                if (element instanceof ViewerModule) {
                    ViewerModule module = (ViewerModule) element;
                    return module.getParentFolder();
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
            public Image getImage( Object element ) {
                if (element instanceof ViewerFolder) {
                    return ImageCache.getInstance().getImage(display, ImageCache.CATEGORY);
                }
                if (element instanceof ViewerModule) {
                    ModuleDescription md = ((ViewerModule) element).getModuleDescription();
                    Status status = md.getStatus();
                    if (status == Status.experimental) {
                        return ImageCache.getInstance().getImage(display, ImageCache.MODULEEXP);
                    } else {
                        return ImageCache.getInstance().getImage(display, ImageCache.MODULE);
                    }
                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof ViewerFolder) {
                    ViewerFolder categoryFolder = (ViewerFolder) element;
                    String name = categoryFolder.getName();
                    name = name.replaceFirst("/", "");
                    return name;
                }
                if (element instanceof ViewerModule) {
                    ModuleDescription module = ((ViewerModule) element).getModuleDescription();
                    return module.getName().replaceAll("\\_\\_", ".");
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

                if (selectedItem instanceof ViewerModule) {
                    ModuleDescription currentSelectedModule = ((ViewerModule) selectedItem).getModuleDescription();
                    currentSelectedModuleGui = new ModuleGui(currentSelectedModule);

                    Control control = currentSelectedModuleGui.makeGui(modulesGuiComposite, false);

                    // Label dummyLabel = new Label(modulesGuiComposite,
                    // SWT.NONE);
                    // dummyLabel.setLayoutData(new
                    // GridData(SWT.BEGINNING, SWT.CENTER, false,
                    // false));
                    // dummyLabel.setText(currentSelectedModule.toString());

                    modulesGuiStackLayout.topControl = control;
                    modulesGuiComposite.layout(true);
                } else {
                    putUnselected();
                }
            }

        });

        return modulesViewer;
    }

    private void addFilterButtons( Composite modulesComposite ) {
        Button filterActive = new Button(modulesComposite, SWT.CHECK);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = 2;
        filterActive.setLayoutData(gd);
        filterActive.setText(LOAD_EXPERIMENTAL_MODULES);
        final ExperimentalFilter activeFilter = new ExperimentalFilter();
        // modulesViewer.addFilter(activeFilter);
        filterActive.setSelection(true);
        filterActive.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent event ) {
                if (((Button) event.widget).getSelection())
                    modulesViewer.removeFilter(activeFilter);
                else
                    modulesViewer.addFilter(activeFilter);
            }
        });
    }

    private void addQuickSettings( Composite modulesListComposite ) throws IOException {
        Group quickSettingsGroup = new Group(modulesListComposite, SWT.NONE);
        quickSettingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        quickSettingsGroup.setLayout(new GridLayout(2, true));
        quickSettingsGroup.setText(PROCESS_SETTINGS);

        Label heapLabel = new Label(quickSettingsGroup, SWT.NONE);
        heapLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        heapLabel.setText(MEMORY_MB);
        final Combo heapCombo = new Combo(quickSettingsGroup, SWT.DROP_DOWN);
        heapCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        heapCombo.setItems(StageConstants.HEAPLEVELS);
        int savedHeapLevel = StageSessionPluginSingleton.getInstance().retrieveSavedHeap();
        for( int i = 0; i < StageConstants.HEAPLEVELS.length; i++ ) {
            if (StageConstants.HEAPLEVELS[i].equals(String.valueOf(savedHeapLevel))) {
                heapCombo.select(i);
                break;
            }
        }
        heapCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String item = heapCombo.getText();
                try {
                    StageSessionPluginSingleton.getInstance().saveHeap(Integer.parseInt(item));
                } catch (NumberFormatException | IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        heapCombo.addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent e ) {
                String item = heapCombo.getText();
                try {
                    Integer.parseInt(item);
                } catch (Exception ex) {
                    return;
                }
                if (item.length() > 0) {
                    try {
                        StageSessionPluginSingleton.getInstance().saveHeap(Integer.parseInt(item));
                    } catch (NumberFormatException | IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });

        Label logLabel = new Label(quickSettingsGroup, SWT.NONE);
        logLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        logLabel.setText(DEBUG_INFO);
        final Combo logCombo = new Combo(quickSettingsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        logCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        logCombo.setItems(StageConstants.LOGLEVELS_GUI);
        String savedLogLevel = StageSessionPluginSingleton.getInstance().retrieveSavedLogLevel();
        for( int i = 0; i < StageConstants.LOGLEVELS_GUI.length; i++ ) {
            if (StageConstants.LOGLEVELS_GUI[i].equals(savedLogLevel)) {
                logCombo.select(i);
                break;
            }
        }
        logCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String item = logCombo.getText();
                try {
                    StageSessionPluginSingleton.getInstance().saveLogLevel(item);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        Label chartsetLabel = new Label(quickSettingsGroup, SWT.NONE);
        chartsetLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        chartsetLabel.setText(CHARACTER_ENCODING);

        SortedMap<String, Charset> charsetMap = Charset.availableCharsets();
        ArrayList<String> charsetList = new ArrayList<String>();
        charsetList.add("");
        for( Entry<String, Charset> charset : charsetMap.entrySet() ) {
            charsetList.add(charset.getKey());
        }
        String[] charsetArray = charsetList.toArray(new String[0]);
        final Combo encodingCombo = new Combo(quickSettingsGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        encodingCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        encodingCombo.setItems(charsetArray);
        String savedEncoding = StageSessionPluginSingleton.getInstance().retrieveSavedEncoding();
        for( int i = 0; i < charsetArray.length; i++ ) {
            if (charsetArray[i].equals(savedEncoding)) {
                encodingCombo.select(i);
                break;
            }
        }
        encodingCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                String item = encodingCombo.getText();
                try {
                    StageSessionPluginSingleton.getInstance().saveEncoding(item);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    private static class ExperimentalFilter extends ViewerFilter {
        public boolean select( Viewer arg0, Object arg1, Object arg2 ) {
            if (arg2 instanceof ViewerFolder) {
                return true;
            }
            if (arg2 instanceof ViewerModule) {
                ModuleDescription md = ((ViewerModule) arg2).getModuleDescription();
                if (md.getStatus() == ModuleDescription.Status.experimental) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Resfresh the viewer.
     * @param filterText 
     * 
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public void relayout( final boolean expandAll, final String filterText  ) throws InvocationTargetException, InterruptedException {

        // IRunnableWithProgress operation1 = new IRunnableWithProgress(){
        //
        // }
        // IRunnableWithProgress operation = new IRunnableWithProgress() {
        // public void run(IProgressMonitor pm)
        // throws InvocationTargetException, InterruptedException {
        // pm.beginTask(LOADING_MODULES_FROM_LIBRARIES,
        // IProgressMonitor.UNKNOWN);
        TreeMap<String, List<ModuleDescription>> availableModules = StageModulesManager.getInstance().browseModules(false);
        final List<ViewerFolder> viewerFolders = ViewerFolder.hashmap2ViewerFolders(availableModules, filterText);

        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                modulesViewer.setInput(viewerFolders);
                if (expandAll)
                    modulesViewer.expandAll();
            }
        });
        // pm.done();
        // }
        // };
        //
        // IWorkbench wb = PlatformUI.getWorkbench();
        // IProgressService ps = wb.getProgressService();
        // ps.busyCursorWhile(operation);
    }

    /**
     * Put the properties view to an label that defines no selection.
     */
    public void putUnselected() {
        Label l = new Label(modulesGuiComposite, SWT.SHADOW_ETCHED_IN);
        l.setText(NO_MODULE_SELECTED);
        modulesGuiStackLayout.topControl = l;
        modulesGuiComposite.layout(true);
    }

    /**
     * Runs the module currently selected in the gui.
     * 
     * @throws Exception
     */
    public void runSelectedModule() throws Exception {
        ScriptHandler handler = new ScriptHandler();
        if (currentSelectedModuleGui == null) {
            return;
        }
        String script = handler.genereateScript(currentSelectedModuleGui, display.getActiveShell());
        if (script == null) {
            return;
        }

        String scriptID = currentSelectedModuleGui.getModuleDescription().getName() + " "
                + StageConstants.dateTimeFormatterYYYYMMDDHHMMSS.format(new Date());
        handler.runModule(scriptID, script);
    }

    /**
     * Generates the script for the module currently selected in the gui.
     * 
     * @return the generated script.
     * @throws Exception
     */
    public String generateScriptForSelectedModule() throws Exception {
        ScriptHandler handler = new ScriptHandler();
        if (currentSelectedModuleGui == null) {
            return null;
        }
        String script = handler.genereateScript(currentSelectedModuleGui, display.getActiveShell());
        return script;
    }
}
