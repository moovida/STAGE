/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.modules.ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.stage.libs.utilsrap.DownloadUtils;
import eu.hydrologis.stage.libs.utilsrap.ExampleUtil;
import eu.hydrologis.stage.libs.utilsrap.FileSelectionDialog;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

/**
 * The stage file management view.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class SpatialToolboxFilemanagementView {

    private FileUpload fileUpload;
    private Label fileNameLabel;
    private Button uploadButton;
    private ServerPushSession pushSession;
    private Display display;

    public void createStageFileManagementTab( Display display, Composite parent, CTabItem stageTab ) throws IOException {

        this.display = display;
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
        fileUpload.setText("Select file to upload");
        fileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fileNameLabel = new Label(uploadGroup, SWT.NONE);
        fileNameLabel.setText("no file selected");
        fileNameLabel.setLayoutData(ExampleUtil.createHorzFillData());
        fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        uploadButton = new Button(uploadGroup, SWT.PUSH);
        uploadButton.setText("Upload file");
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
                final Exception ex = event.getException();
                display.syncExec(new Runnable(){
                    public void run() {
                        MessageDialog.openWarning(fileNameLabel.getShell(), "ERROR",
                                "File upload failed with error: " + ex.getLocalizedMessage());
                    }
                });

                ex.printStackTrace();
            }

            public void uploadFinished( FileUploadEvent event ) {
                if (pushSession != null)
                    pushSession.stop();
                StringBuilder sb = new StringBuilder();
                for( FileDetails file : event.getFileDetails() ) {
                    sb.append(",").append(file.getFileName());
                }
                final String filesStr = sb.substring(1);
                display.syncExec(new Runnable(){
                    public void run() {
                        MessageDialog.openInformation(fileNameLabel.getShell(), "INFORMATION", "Successfully uploaded: "
                                + filesStr);
                    }
                });
            }
        });
        return uploadHandler.getUploadUrl();
    }

    private Composite createFileDownloadArea( Composite parent ) {
        Group downloadGroup = new Group(parent, SWT.NONE);
        downloadGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        downloadGroup.setLayout(new GridLayout(2, true));
        downloadGroup.setText("Files Download");
        createDataDownloadButton(downloadGroup);
        createScriptDownloadButton(downloadGroup);
        return downloadGroup;
    }

    private void createDataDownloadButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        button.setText("Data");
        button.setToolTipText("Download data files");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    File userFolder = StageWorkspace.getInstance().getDataFolder(User.getCurrentUserName());
                    FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, false, userFolder, null, null,
                            new String[]{StageWorkspace.GEOPAPARAZZI_FOLDERNAME});
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

    private void createScriptDownloadButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        button.setText("Scripts");
        button.setToolTipText("Download a script");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    File userFolder = StageWorkspace.getInstance().getScriptsFolder(User.getCurrentUserName());
                    FileSelectionDialog fileDialog = new FileSelectionDialog(parentShell, false, userFolder, null, null,
                            new String[]{StageWorkspace.GEOPAPARAZZI_FOLDERNAME});
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

}
