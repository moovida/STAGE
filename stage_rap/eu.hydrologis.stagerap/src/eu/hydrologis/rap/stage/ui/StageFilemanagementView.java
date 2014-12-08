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

import java.io.File;
import java.io.IOException;

import org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.rap.stage.utilsrap.DownloadUtils;
import eu.hydrologis.rap.stage.utilsrap.ExampleUtil;
import eu.hydrologis.rap.stage.utilsrap.FileSelectionDialog;
import eu.hydrologis.rap.stage.workspace.StageWorkspace;
import eu.hydrologis.rap.stage.workspace.User;

/**
 * The stage file management view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class StageFilemanagementView {

    private FileUpload fileUpload;
    private Label fileNameLabel;
    private Button uploadButton;
    private ServerPushSession pushSession;

    public void createStageFileManagementTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainComposite.setLayout(new GridLayout(1, true));

        createControlsColumn(mainComposite);

        stageTab.setControl(mainComposite);
    }

    private Control createControlsColumn( Composite parent ) {
        Control fileUploadArea = createFileUploadArea(parent);
        fileUploadArea.setLayoutData(ExampleUtil.createHorzFillData());
        // Control fileDialogArea = createFileDialogArea(column);
        // fileDialogArea.setLayoutData(ExampleUtil.createHorzFillData());
        Control fileDownloadArea = createFileDownloadArea(parent);
        fileDownloadArea.setLayoutData(ExampleUtil.createHorzFillData());
        return parent;
    }

    private Control createFileUploadArea( Composite parent ) {
        Group uploadGroup = new Group(parent, SWT.NONE);
        uploadGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        uploadGroup.setLayout(new GridLayout(2, false));
        uploadGroup.setText("Files Upload");

        fileUpload = new FileUpload(uploadGroup, SWT.NONE);
        fileUpload.setText("Select File");
        fileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fileNameLabel = new Label(uploadGroup, SWT.NONE);
        fileNameLabel.setText("no file selected");
        fileNameLabel.setLayoutData(ExampleUtil.createHorzFillData());
        fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        uploadButton = new Button(uploadGroup, SWT.PUSH);
        uploadButton.setText("Upload");
        uploadButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        new Label(uploadGroup, SWT.NONE);
        fileUpload.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String fileName = fileUpload.getFileName();
                fileNameLabel.setText(fileName == null ? "" : fileName);
            }
        });
        final String url = startUploadReceiver();
        pushSession = new ServerPushSession();
        uploadButton.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                pushSession.start();
                fileUpload.submit(url);
            }
        });
        return uploadGroup;
    }

    private String startUploadReceiver() {
        DiskFileUploadReceiver receiver = new DiskFileUploadReceiver(){
            @Override
            protected File createTargetFile( FileDetails details ) throws IOException {
                String fileName = "upload.tmp";
                if (details != null && details.getFileName() != null) {
                    fileName = details.getFileName();
                }

                File dataFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
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
                addToLog("upload failed: " + event.getException());
            }

            public void uploadFinished( FileUploadEvent event ) {
                for( FileDetails file : event.getFileDetails() ) {
                    addToLog("received: " + file.getFileName());
                }
            }
        });
        return uploadHandler.getUploadUrl();
    }

    private void addToLog( final String message ) {
        System.out.println(message);

        if (pushSession != null)
            pushSession.stop();
        // if (!logText.isDisposed()) {
        // logText.getDisplay().asyncExec(new Runnable(){
        // public void run() {
        // String text = logText.getText();
        // if (INITIAL_TEXT.equals(text)) {
        // text = "";
        // }
        // logText.setText(text + message + "\n");
        // pushSession.stop();
        // }
        // });
        // }
    }

    private Composite createFileDialogArea( Composite parent ) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(ExampleUtil.createGridLayout(2, true, true, true));
        createAddMultiButton(area);
        return area;
    }

    private Composite createFileDownloadArea( Composite parent ) {
        Group downloadGroup = new Group(parent, SWT.NONE);
        downloadGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        downloadGroup.setLayout(new GridLayout(2, false));
        downloadGroup.setText("Files Download");
        // ExampleUtil.createHeading(area, "File download", 2);
        createDownloadButton(downloadGroup);
        return downloadGroup;
    }

    // private void createAddSingleButton( Composite parent ) {
    // Button button = new Button(parent, SWT.PUSH);
    // button.setLayoutData(ExampleUtil.createHorzFillData());
    // button.setText("Single File");
    // button.setToolTipText("Launches file dialog for single file selection.");
    // final Shell parentShell = parent.getShell();
    // button.addSelectionListener(new SelectionAdapter(){
    // @Override
    // public void widgetSelected( SelectionEvent e ) {
    // openFileDialog(parentShell, false);
    // }
    // });
    // }

    private void createAddMultiButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(ExampleUtil.createHorzFillData());
        button.setText("Files Upload");
        button.setToolTipText("Upload one or multiple files.");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                final FileDialog fileDialog = new FileDialog(parentShell, getDialogStyle(true));
                fileDialog.setText(true ? "Upload Files" : "Upload File");
                DialogUtil.open(fileDialog, new DialogCallback(){
                    public void dialogClosed( int returnCode ) {
                        showUploadResults(fileDialog);

                        // FIXME
                        final String url = startUploadReceiver();
                        pushSession = new ServerPushSession();
                        pushSession.start();

                        // dfileUpload.submit(url);
                    }
                });
            }
        });
    }

    private void createDownloadButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
        button.setText("File Download");
        button.setToolTipText("Download a file");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    File userFolder = StageWorkspace.getInstance().getUserFolder(User.getCurrentUserName());
                    FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, userFolder, null, null);
                    int returnCode = fileDialog.open();
                    if (returnCode == SWT.CANCEL) {
                        return;
                    }
                    File selectedFile = fileDialog.getSelectedFile();
                    if (selectedFile != null && selectedFile.exists() && !selectedFile.isDirectory()) {
                        new DownloadUtils().sendDownload(parentShell, selectedFile);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void showUploadResults( FileDialog fileDialog ) {
        String[] selectedFiles = fileDialog.getFileNames();
        for( String fileName : selectedFiles ) {
            addToLog("received: " + new File(fileName).getName());
        }
    }

    // private void createClearButton( Composite parent ) {
    // Button button = new Button(parent, SWT.PUSH);
    // button.setLayoutData(ExampleUtil.createHorzFillData());
    // button.setText("Clear");
    // button.setToolTipText("Clears the results list");
    // button.addSelectionListener(new SelectionAdapter(){
    // @Override
    // public void widgetSelected( SelectionEvent e ) {
    // logText.setText(INITIAL_TEXT);
    // }
    // });
    // }

    private static int getDialogStyle( boolean multi ) {
        int result = SWT.SHELL_TRIM | SWT.APPLICATION_MODAL;
        if (multi) {
            result |= SWT.MULTI;
        }
        return result;
    }

}
