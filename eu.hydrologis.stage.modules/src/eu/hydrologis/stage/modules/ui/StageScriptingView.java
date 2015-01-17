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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.utilsrap.FileSelectionDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;
import eu.hydrologis.stage.modules.core.ScriptHandler;
import eu.hydrologis.stage.modules.utils.FileUtilities;
import eu.hydrologis.stage.modules.utils.ScriptTemplatesUtil;
import eu.hydrologis.stage.modules.utils.StageConstants;

/**
 * The stage scripting view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class StageScriptingView {

    private static final String EMPTY_SCRIPT_NAME = "The script name can't be empty.";
    private static final String SCRIPT_IS_EMPTY = "Script is empty";
    private static final String GROOVY = ".groovy";
    private static final String FILE_IS_FOLDER = "The selected file is a folder.";
    private static final String TEMPLATES = "Templates";
    private static final String ERROR = "ERROR";
    private static final String SCRIPT_SAVED = "Script saved.";
    private static final String INFORMATION = "Information";
    private static final String COULD_NOT_SAVE_SCRIPT = "Could not save script: ";
    private static final String SCRIPT_NAME_TO_SAVE = "Enter the script name to save to:";
    private static final String SCRIPT_NAME_NOT_VALID = "The script name is not valid!";
    private static final String SAVE_CURRENT_SCRIPT = "Save current script";
    private static final String OPEN_EXISTING_SCRIPT = "Open existing script";
    private static final String FILE = "File";
    private static final String RUN_MODULE = "Run module";
    private static final String EXECUTION = "Execution";
    private static final String SCRIPT_NAME = "Script name";
    private org.eclipse.swt.widgets.List logList;
    private Text scriptTitleText;
    private Text scriptAreaText;

    public void createStageScriptingTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(3, false));

        addRunGroup(display, mainComposite);

        addFileGroup(display, mainComposite);

        addTemplatesGroup(display, mainComposite);

        SashForm mainScriptingComposite = new SashForm(mainComposite, SWT.HORIZONTAL);
        GridData mainScriptingCompositeGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainScriptingCompositeGD.horizontalSpan = 3;
        mainScriptingComposite.setLayoutData(mainScriptingCompositeGD);

        Composite leftComposite = new Composite(mainScriptingComposite, SWT.None);
        GridLayout leftLayout = new GridLayout(2, true);
        leftLayout.marginWidth = 0;
        leftLayout.marginHeight = 0;
        leftComposite.setLayout(leftLayout);
        GridData leftGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        leftComposite.setLayoutData(leftGD);

        Label titleLabel = new Label(leftComposite, SWT.NONE);
        titleLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        titleLabel.setText(SCRIPT_NAME);

        scriptTitleText = new Text(leftComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        scriptTitleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        scriptTitleText.setText("");

        scriptAreaText = new Text(leftComposite, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        GridData scriptAreaTextGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        scriptAreaTextGD.horizontalSpan = 2;
        scriptAreaText.setLayoutData(scriptAreaTextGD);
        scriptAreaText.setText("");

        logList = new org.eclipse.swt.widgets.List(mainScriptingComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        logList.setLayoutData(new GridData(GridData.FILL_BOTH));
        logList.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

        mainScriptingComposite.setWeights(new int[]{1, 1});

        stageTab.setControl(mainComposite);
    }

    private void addRunGroup( Display display, Composite mainComposite ) {
        Group execGroup = new Group(mainComposite, SWT.NONE);
        execGroup.setLayoutData(new GridData(SWT.LEAD, SWT.FILL, false, false));
        execGroup.setLayout(new GridLayout(1, true));
        execGroup.setText(EXECUTION);

        Button runModuleButton = new Button(execGroup, SWT.PUSH);
        runModuleButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
        // runModuleButton.setText("Run module");
        runModuleButton.setToolTipText(RUN_MODULE);
        runModuleButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.RUN));
        runModuleButton.addSelectionListener(new SelectionAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    String script = scriptAreaText.getText();
                    String name = scriptTitleText.getText();
                    runScript(name, script);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void addFileGroup( Display display, Composite mainComposite ) {
        Group fileGroup = new Group(mainComposite, SWT.NONE);
        fileGroup.setLayoutData(new GridData(SWT.LEAD, SWT.FILL, false, false));
        fileGroup.setLayout(new GridLayout(2, true));
        fileGroup.setText(FILE);

        final Button openButton = new Button(fileGroup, SWT.PUSH);
        openButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        openButton.setToolTipText(OPEN_EXISTING_SCRIPT);
        openButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.OPEN));
        openButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                File scriptsFolder = StageWorkspace.getInstance().getScriptsFolder(User.getCurrentUserName());
                FileSelectionDialog fileDialog = new FileSelectionDialog(openButton.getShell(), scriptsFolder,
                        new String[]{GROOVY}, new String[]{GROOVY});
                int returnCode = fileDialog.open();
                if (returnCode == SWT.CANCEL) {
                    return;
                }
                File selectedFile = fileDialog.getSelectedFile();
                if (selectedFile != null && selectedFile.exists()) {
                    if (selectedFile.isDirectory()) {
                        MessageDialog.openWarning(openButton.getShell(), ERROR, FILE_IS_FOLDER);
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
                        e1.printStackTrace();
                        MessageDialog.openWarning(openButton.getShell(), ERROR, e1.getLocalizedMessage());
                    }
                }
            }
        });

        final Button saveButton = new Button(fileGroup, SWT.PUSH);
        saveButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        saveButton.setToolTipText(SAVE_CURRENT_SCRIPT);
        saveButton.setImage(ImageCache.getInstance().getImage(display, ImageCache.SAVE));
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
                InputDialog inputDialog = new InputDialog(saveButton.getShell(), title, mesg, scriptTitle, val);
                int returnCode = inputDialog.open();
                if (returnCode == Window.OK) {
                    scriptTitle = inputDialog.getValue();
                    File scriptFile = getScriptFile(scriptTitle);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile))) {
                        writer.write(scriptText);
                        MessageDialog.openInformation(saveButton.getShell(), INFORMATION, SCRIPT_SAVED);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        MessageDialog.openWarning(saveButton.getShell(), ERROR, COULD_NOT_SAVE_SCRIPT + e1.getLocalizedMessage());
                    }
                }

            }
        });
    }

    private File getScriptFile( String name ) {
        File scriptsFolder = StageWorkspace.getInstance().getScriptsFolder(User.getCurrentUserName());
        File scriptFile = new File(scriptsFolder, name + GROOVY);
        return scriptFile;
    }

    private void addTemplatesGroup( Display display, Composite mainComposite ) {
        Group templatesGroup = new Group(mainComposite, SWT.NONE);
        templatesGroup.setLayoutData(new GridData(SWT.LEAD, SWT.FILL, false, false));
        templatesGroup.setLayout(new GridLayout(1, true));
        templatesGroup.setText(TEMPLATES);

        String[] templates = new String[]{""};
        List<String> scriptNames = ScriptTemplatesUtil.getScriptNames();
        if (scriptNames.size() > 0) {
            templates = scriptNames.toArray(new String[0]);
        }
        final Combo templatesCombo = new Combo(templatesGroup, SWT.DROP_DOWN);
        templatesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        templatesCombo.setItems(templates);
        templatesCombo.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                int selectionIndex = templatesCombo.getSelectionIndex();
                if (selectionIndex != -1) {
                    String scriptName = templatesCombo.getItem(selectionIndex);
                    String script = ScriptTemplatesUtil.getScriptByName(scriptName);

                    String title = scriptTitleText.getText();
                    if (title.trim().length() == 0) {
                        scriptTitleText.setText(scriptName);
                    } else {
                        script = "\n\n" + script;
                    }
                    scriptAreaText.append(script);
                }
            }
        });

    }

    /**
     * Runs the script.
     * 
     * @throws Exception
     */
    public void runScript( String name, String script ) throws Exception {
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
        String scriptID = name + " " + StageConstants.dateTimeFormatterYYYYMMDDHHMMSS.format(new Date());
        logList.removeAll();
        scriptHandler.runModule(scriptID, script, logList);
    }
}
