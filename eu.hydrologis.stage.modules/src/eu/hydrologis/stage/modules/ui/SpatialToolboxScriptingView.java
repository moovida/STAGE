/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.jgrasstools.gears.libs.modules.ModelsSupporter;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utilsrap.FileSelectionDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;
import eu.hydrologis.stage.modules.core.ModuleDescription;
import eu.hydrologis.stage.modules.core.ScriptHandler;
import eu.hydrologis.stage.modules.utils.FileUtilities;
import eu.hydrologis.stage.modules.utils.ScriptTemplatesUtil;
import eu.hydrologis.stage.modules.utils.SpatialToolboxConstants;

/**
 * The stage scripting view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class SpatialToolboxScriptingView {

    private static final String ERROR = "ERROR";
    private static final String WARNING = "WARNING";
    private static final String SCRIPT_EXE_ERROR = "An error occurred while executing the script.";

    private static final String EMPTY_SCRIPT_NAME = "The script name can't be empty.";
    private static final String SCRIPT_IS_EMPTY = "Script is empty";
    private static final String GROOVY = ".groovy";
    private static final String FILE_IS_FOLDER = "The selected file is a folder.";
    private static final String TEMPLATES = "templates";
    private static final String TEMPLATES_TOOLTIP = "Use one of the available templates";
    private static final String SCRIPT_SAVED = "Script saved.";
    private static final String INFORMATION = "Information";
    private static final String COULD_NOT_SAVE_SCRIPT = "Could not save script: ";
    private static final String SCRIPT_NAME_TO_SAVE = "Enter the script name to save to:";
    private static final String SCRIPT_NAME_NOT_VALID = "The script name is not valid!";
    private static final String SAVE = "save";
    private static final String SAVE_TOOLTIP = "Save current script";
    private static final String OPEN = "open";
    private static final String OPEN_TOOLTIP = "Open existing script";
    private static final String RUN = "run";
    private static final String RUN_TOOLTIP = "Run module";
    private static final String COPY_LOG = "copy log";
    private static final String COPY_LOG_TOOLTIP = "Copy selected log into the scripting area";
    private static final String SETNAME = "set name";
    private static final String SETNAME_TOOLTIP = "Set a date based script name";
    private static final String USEMODULE = "use module";
    private static final String USEMODULE_TOOLTIP = "Use module selected in the modules view";
    private static final String USEMODULETEMPLATE = "module template";
    private static final String USEMODULETEMPLATE_TOOLTIP = "Use the selected module's template";
    private static final String SCRIPT_NAME = "Script name";
    private org.eclipse.swt.widgets.List logList;
    private Text scriptTitleText;
    private Text scriptAreaText;

    public void createStageScriptingTab( Display display, Composite parent, CTabItem stageTab,
            final SpatialToolboxModulesView stageModulesView ) throws IOException {
        final Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(3, false));

        final ToolBar toolBar = new ToolBar(mainComposite, SWT.NONE);
        Menu menu = new Menu(toolBar);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("context menu item");
        toolBar.setMenu(menu);

        ToolItem runModuleButton = new ToolItem(toolBar, SWT.PUSH);
        runModuleButton.setText(RUN);
        runModuleButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN));
        runModuleButton.setToolTipText(RUN_TOOLTIP);
        runModuleButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                runScript();
            }
        });

        ToolItem openButton = new ToolItem(toolBar, SWT.PUSH);
        openButton.setText(OPEN);
        openButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.OPEN));
        openButton.setToolTipText(OPEN_TOOLTIP);
        openButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                File scriptsFolder = StageWorkspace.getInstance().getScriptsFolder(User.getCurrentUserName());
                FileSelectionDialog fileDialog = new FileSelectionDialog(mainComposite.getShell(), false, scriptsFolder,
                        new String[]{GROOVY}, new String[]{GROOVY}, null);
                int returnCode = fileDialog.open();
                if (returnCode == SWT.CANCEL) {
                    return;
                }
                File selectedFile = fileDialog.getSelectedFile();
                if (selectedFile != null && selectedFile.exists()) {
                    if (selectedFile.isDirectory()) {
                        MessageDialog.openWarning(mainComposite.getShell(), ERROR, FILE_IS_FOLDER);
                        return;
                    }
                    try {
                        String readFile = FileUtilities.readFile(selectedFile);
                        String name = selectedFile.getName();
                        if (name.endsWith(GROOVY)) {
                            name = name.replaceFirst(GROOVY, "");
                        }
                        scriptTitleText.setText(name);
                        scriptAreaText.setText(readFile);
                    } catch (IOException e1) {
                        StageLogger.logError(this, null, e1);
                        MessageDialog.openWarning(mainComposite.getShell(), ERROR, e1.getLocalizedMessage());
                    }
                }
            }
        });

        ToolItem saveButton = new ToolItem(toolBar, SWT.PUSH);
        saveButton.setText(SAVE);
        saveButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.SAVE));
        saveButton.setToolTipText(SAVE_TOOLTIP);
        saveButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String scriptTitle = scriptTitleText.getText();
                String scriptText = scriptAreaText.getText();

                final IInputValidator val = new IInputValidator(){
                    public String isValid( final String newText ) {
                        File scriptFile = getScriptFile(newText);
                        boolean isFilenameValid = FileUtilities.isFilenameValid(scriptFile);
                        String result = null;
                        if (!isFilenameValid) {
                            result = SCRIPT_NAME_NOT_VALID;
                        }
                        return result;
                    }
                };
                String title = SCRIPT_NAME;
                String mesg = SCRIPT_NAME_TO_SAVE;
                InputDialog inputDialog = new InputDialog(mainComposite.getShell(), title, mesg, scriptTitle, val);
                int returnCode = inputDialog.open();
                if (returnCode == Window.OK) {
                    scriptTitle = inputDialog.getValue();
                    File scriptFile = getScriptFile(scriptTitle);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile))) {
                        writer.write(scriptText);
                        MessageDialog.openInformation(mainComposite.getShell(), INFORMATION, SCRIPT_SAVED);
                    } catch (IOException e1) {
                        StageLogger.logError(this, null, e1);
                        MessageDialog.openWarning(mainComposite.getShell(), ERROR,
                                COULD_NOT_SAVE_SCRIPT + e1.getLocalizedMessage());
                    }
                }

            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        final ToolItem templatesCombo = new ToolItem(toolBar, SWT.DROP_DOWN);
        templatesCombo.setText(TEMPLATES);
        templatesCombo.setToolTipText(TEMPLATES_TOOLTIP);
        templatesCombo.setImage(ImageCache.getInstance().getImage(display, ImageCache.FILE));
        final Menu templatesMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
        List<String> scriptNames = ScriptTemplatesUtil.getScriptNames();
        for( String scriptName : scriptNames ) {
            final MenuItem menuItem = new MenuItem(templatesMenu, SWT.PUSH);
            menuItem.setText(scriptName);
            menuItem.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    String scriptName = menuItem.getText();
                    String script = ScriptTemplatesUtil.getScriptByName(scriptName);

                    String title = scriptTitleText.getText();
                    if (title.trim().length() == 0) {
                        scriptTitleText.setText(scriptName);
                    } else {
                        script = "\n\n" + script;
                    }
                    scriptAreaText.append(script);
                }
            });
        }

        templatesCombo.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( final SelectionEvent event ) {
                if (event.detail == SWT.ARROW) {
                    Point point = toolBar.toDisplay(event.x, event.y);
                    templatesMenu.setLocation(point);
                    templatesMenu.setVisible(true);
                }
            }
        });

        ToolItem setDefaultNameButton = new ToolItem(toolBar, SWT.PUSH);
        setDefaultNameButton.setText(SETNAME);
        setDefaultNameButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.COPY));
        setDefaultNameButton.setToolTipText(SETNAME_TOOLTIP);
        setDefaultNameButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Script_");
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
                    String dateStr = dateFormatter.format(new Date());
                    sb.append(dateStr);
                    scriptTitleText.setText(sb.toString());
                } catch (Exception e1) {
                    StageLogger.logError(this, null, e1);
                    MessageDialog.openError(scriptAreaText.getShell(), ERROR, e1.getLocalizedMessage());
                }
            }
        });

        ToolItem useFromModulesViewButton = new ToolItem(toolBar, SWT.PUSH);
        useFromModulesViewButton.setText(USEMODULE);
        useFromModulesViewButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.MODULE));
        useFromModulesViewButton.setToolTipText(USEMODULE_TOOLTIP);
        useFromModulesViewButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {

                    ModuleDescription currentSelectedModule = stageModulesView.getCurrentSelectedModule();
                    if (currentSelectedModule != null) {
                        String scriptText = stageModulesView.generateScriptForSelectedModule();
                        scriptText = StageWorkspace.substituteDataFolder(scriptText);
                        if (scriptTitleText.getText().trim().length() == 0) {
                            scriptTitleText.setText("Script_" + currentSelectedModule.getName());
                        }

                        boolean ignoreImports = false;
                        String IMPORT = "import";
                        if (scriptAreaText.getText().trim().startsWith(IMPORT)) {
                            ignoreImports = true;
                        }
                        String[] split = scriptText.split("\n");
                        for( String string : split ) {
                            if (ignoreImports && string.trim().startsWith(IMPORT))
                                continue;
                            if (string.trim().startsWith("println"))
                                continue;
                            scriptAreaText.append(string + "\n");
                        }

                    } else {
                        MessageDialog.openWarning(scriptAreaText.getShell(), WARNING, "No module selected.");
                    }
                } catch (Exception e1) {
                    StageLogger.logError(this, null, e1);
                    MessageDialog.openError(scriptAreaText.getShell(), ERROR, e1.getLocalizedMessage());
                }
            }
        });

        ToolItem useTemplateFromModulesViewButton = new ToolItem(toolBar, SWT.PUSH);
        useTemplateFromModulesViewButton.setText(USEMODULETEMPLATE);
        useTemplateFromModulesViewButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.MODULE_TEMPLATE));
        useTemplateFromModulesViewButton.setToolTipText(USEMODULETEMPLATE_TOOLTIP);
        useTemplateFromModulesViewButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {

                    ModuleDescription currentSelectedModule = stageModulesView.getCurrentSelectedModule();
                    if (currentSelectedModule != null) {
                        Object newInstance = currentSelectedModule.getModuleClass().newInstance();
                        String template = ModelsSupporter.generateTemplate(newInstance);

                        if (scriptTitleText.getText().trim().length() == 0) {
                            scriptTitleText.setText("Script_" + currentSelectedModule.getName());
                        }

                        boolean ignoreImports = false;
                        String IMPORT = "import";
                        if (scriptAreaText.getText().trim().startsWith(IMPORT)) {
                            ignoreImports = true;
                        }
                        String[] split = template.split("\n");
                        for( String string : split ) {
                            if (ignoreImports && string.trim().startsWith(IMPORT))
                                continue;
                            scriptAreaText.append(string + "\n");
                        }

                    } else {
                        MessageDialog.openWarning(scriptAreaText.getShell(), WARNING, "No module selected.");
                    }
                } catch (Exception e1) {
                    StageLogger.logError(this, null, e1);
                    MessageDialog.openError(scriptAreaText.getShell(), ERROR, e1.getLocalizedMessage());
                }
            }
        });

        ToolItem copyLogButton = new ToolItem(toolBar, SWT.PUSH);
        copyLogButton.setText(COPY_LOG);
        copyLogButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.COPY));
        copyLogButton.setToolTipText(COPY_LOG_TOOLTIP);
        copyLogButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    String[] selection = logList.getSelection();
                    if (selection.length == 0) {
                        selection = logList.getItems();
                    }
                    for( String string : selection ) {
                        scriptAreaText.append(string + "\n");
                    }

                } catch (Exception e1) {
                    StageLogger.logError(this, null, e1);
                    MessageDialog.openError(scriptAreaText.getShell(), ERROR, e1.getLocalizedMessage());
                }
            }
        });

        SashForm mainScriptingComposite = new SashForm(mainComposite, SWT.HORIZONTAL);
        GridData mainScriptingCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainScriptingCompositeGD.horizontalSpan = 3;
        mainScriptingComposite.setLayoutData(mainScriptingCompositeGD);

        Composite leftComposite = new Composite(mainScriptingComposite, SWT.None);
        GridLayout leftLayout = new GridLayout(2, false);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        leftComposite.setLayout(leftLayout);
        GridData leftGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        leftComposite.setLayoutData(leftGD);

        Label titleLabel = new Label(leftComposite, SWT.NONE);
        titleLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        titleLabel.setText("<big><i>" + SCRIPT_NAME + "</i></big>");
        titleLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        scriptTitleText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        scriptTitleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        scriptTitleText.setText("");

        scriptAreaText = new Text(leftComposite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData scriptAreaTextGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        scriptAreaTextGD.horizontalSpan = 2;
        scriptAreaText.setLayoutData(scriptAreaTextGD);
        scriptAreaText.setText("import org.jgrasstools.modules.*\n");

        logList = new org.eclipse.swt.widgets.List(mainScriptingComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        logList.setLayoutData(new GridData(GridData.FILL_BOTH));
        logList.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        mainScriptingComposite.setWeights(new int[]{1, 1});

        stageTab.setControl(mainComposite);

        /*
         * execution shortcut
         */
        String[] shortcut = new String[]{"ALT+ENTER"};
        display.setData(RWT.ACTIVE_KEYS, shortcut);
        display.setData(RWT.CANCEL_KEYS, shortcut);
        display.addFilter(SWT.KeyDown, new Listener(){
            public void handleEvent( Event event ) {
                runScript();
            }
        });

    }

    private File getScriptFile( String name ) {
        File scriptsFolder = StageWorkspace.getInstance().getScriptsFolder(User.getCurrentUserName());
        File scriptFile = new File(scriptsFolder, name + GROOVY);
        return scriptFile;
    }

    /**
     * Runs the script.
     * 
     * @throws Exception
     */
    private void runScript() {
        try {
            String script = scriptAreaText.getText();
            String name = scriptTitleText.getText();
            String currentUserName = User.getCurrentUserName();
            String dataPath = StageWorkspace.getInstance().getDataFolder(currentUserName).getAbsolutePath();
            dataPath = dataPath.replace('\\', '/');
            script = script.replaceAll(StageWorkspace.STAGE_DATA_FOLDER_SUBSTITUTION_NAME, dataPath);
            if (script.length() == 0) {
                MessageDialog.openWarning(scriptAreaText.getShell(), ERROR, SCRIPT_IS_EMPTY);
                return;
            }
            if (name.length() == 0) {
                MessageDialog.openWarning(scriptAreaText.getShell(), ERROR, EMPTY_SCRIPT_NAME);
                return;
            }
            ScriptHandler scriptHandler = new ScriptHandler();
            String scriptID = name + " " + SpatialToolboxConstants.dateTimeFormatterYYYYMMDDHHMMSS.format(new Date());
            logList.removeAll();
            scriptHandler.runModule(scriptID, script, logList);
        } catch (Exception e1) {
            e1.printStackTrace();
            String msg = "Error running script: " + scriptTitleText.getText() + "\n" + scriptAreaText.getText();
            StageLogger.logError(this, msg, e1);
            MessageDialog.openError(scriptAreaText.getShell(), ERROR, SCRIPT_EXE_ERROR);
        }
    }
}
