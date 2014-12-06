package eu.hydrologis.rap.stage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.rap.addons.fileupload.DiskFileUploadReceiver;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.rap.stage.ui.StageView;
import eu.hydrologis.rap.stage.utilsrap.DownloadUtils;
import eu.hydrologis.rap.stage.utilsrap.ExampleUtil;
import eu.hydrologis.rap.stage.utilsrap.LoginDialog;

public class StageEntryPoint extends AbstractEntryPoint {

    private static final String INITIAL_TEXT = "no files uploaded.";
    private FileUpload fileUpload;
    private Label fileNameLabel;
    private Button uploadButton;
    private Text logText;
    private ServerPushSession pushSession;

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

        StageView view = new StageView();
        try {
            view.createPartControl(Display.getCurrent(), parent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // File file = new File(".");
        // System.out.println(file.getAbsolutePath());

        // parent.setLayout(ExampleUtil.createMainLayout(3));
        // Control controlsColumn = createControlsColumn(parent);
        // controlsColumn.setLayoutData(ExampleUtil.createFillData());
        // Control serverColumn = createLogColumn(parent);
        // serverColumn.setLayoutData(ExampleUtil.createFillData());
        // Control infoColumn = createInfoColumn(parent);
        // infoColumn.setLayoutData(ExampleUtil.createFillData());
    }

    private Control createControlsColumn( Composite parent ) {
        Composite column = new Composite(parent, SWT.NONE);
        column.setLayout(ExampleUtil.createGridLayoutWithoutMargin(1, false));
        Control fileUploadArea = createFileUploadArea(column);
        fileUploadArea.setLayoutData(ExampleUtil.createHorzFillData());
        Control fileDialogArea = createFileDialogArea(column);
        fileDialogArea.setLayoutData(ExampleUtil.createHorzFillData());
        Control fileDownloadArea = createFileDownloadArea(column);
        fileDownloadArea.setLayoutData(ExampleUtil.createHorzFillData());
        return column;
    }

    private Control createLogColumn( Composite parent ) {
        Composite column = new Composite(parent, SWT.NONE);
        column.setLayout(ExampleUtil.createGridLayout(1, false, true, true));
        ExampleUtil.createHeading(column, "Server log", 2);
        logText = new Text(column, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.BORDER);
        logText.setText(INITIAL_TEXT);
        logText.setLayoutData(ExampleUtil.createFillData());

        File f = new File(".");
        File[] listFiles = f.listFiles();
        StringBuilder sb = new StringBuilder();
        for( File file : listFiles ) {
            sb.append(file.getName()).append("\n");
        }
        logText.setText(sb.toString());

        createClearButton(column);
        return column;
    }

    private static Control createInfoColumn( Composite parent ) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(ExampleUtil.createFillData());
        return label;
    }

    private Control createFileUploadArea( Composite parent ) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(ExampleUtil.createGridLayout(2, true, true, true));
        ExampleUtil.createHeading(area, "FileUpload widget", 2);
        fileUpload = new FileUpload(area, SWT.NONE);
        fileUpload.setText("Select File");
        fileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        fileNameLabel = new Label(area, SWT.NONE);
        fileNameLabel.setText("no file selected");
        fileNameLabel.setLayoutData(ExampleUtil.createHorzFillData());
        fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        uploadButton = new Button(area, SWT.PUSH);
        uploadButton.setText("Upload");
        uploadButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        new Label(area, SWT.NONE);
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
        return area;
    }

    private String startUploadReceiver() {
        DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
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
        if (!logText.isDisposed()) {
            logText.getDisplay().asyncExec(new Runnable(){
                public void run() {
                    String text = logText.getText();
                    if (INITIAL_TEXT.equals(text)) {
                        text = "";
                    }
                    logText.setText(text + message + "\n");
                    pushSession.stop();
                }
            });
        }
    }

    private Composite createFileDialogArea( Composite parent ) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(ExampleUtil.createGridLayout(2, true, true, true));
        ExampleUtil.createHeading(area, "FileDialog", 2);
        createAddSingleButton(area);
        new Label(area, SWT.NONE);
        createAddMultiButton(area);
        return area;
    }

    private Composite createFileDownloadArea( Composite parent ) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(ExampleUtil.createGridLayout(2, true, true, true));
        ExampleUtil.createHeading(area, "File download", 2);
        createDownloadButton(area);
        return area;
    }

    private void createDownloadButton( Composite parent ) {
        final File f = new File("/home/hydrologis/src.zip");

        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(ExampleUtil.createHorzFillData());
        button.setText("Download " + f.getName());
        button.setToolTipText("Download a file");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                try {
                    new DownloadUtils().sendDownload(parentShell, f);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }

    private void createAddSingleButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(ExampleUtil.createHorzFillData());
        button.setText("Single File");
        button.setToolTipText("Launches file dialog for single file selection.");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                openFileDialog(parentShell, false);
            }
        });
    }

    private void createAddMultiButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(ExampleUtil.createHorzFillData());
        button.setText("Multiple Files");
        button.setToolTipText("Launches file dialog for multiple file selection.");
        final Shell parentShell = parent.getShell();
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                openFileDialog(parentShell, true);
            }
        });
    }

    private void createClearButton( Composite parent ) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(ExampleUtil.createHorzFillData());
        button.setText("Clear");
        button.setToolTipText("Clears the results list");
        button.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent e ) {
                logText.setText(INITIAL_TEXT);
            }
        });
    }

    private void openFileDialog( Shell parent, boolean multi ) {
        final FileDialog fileDialog = new FileDialog(parent, getDialogStyle(multi));
        fileDialog.setText(multi ? "Upload Multiple Files" : "Upload Single File");
        DialogUtil.open(fileDialog, new DialogCallback(){
            public void dialogClosed( int returnCode ) {
                showUploadResults(fileDialog);
            }
        });
    }

    private void showUploadResults( FileDialog fileDialog ) {
        String[] selectedFiles = fileDialog.getFileNames();
        for( String fileName : selectedFiles ) {
            addToLog("received: " + new File(fileName).getName());
        }
    }

    private static int getDialogStyle( boolean multi ) {
        int result = SWT.SHELL_TRIM | SWT.APPLICATION_MODAL;
        if (multi) {
            result |= SWT.MULTI;
        }
        return result;
    }

    // @Override
    // protected void createContents(final Composite parent) {
    // parent.setLayout(new GridLayout(2, false));
    // Button checkbox = new Button(parent, SWT.CHECK);
    // checkbox.setText("Hello");
    // Button button = new Button(parent, SWT.PUSH);
    // button.setText("World");
    // button.addSelectionListener(new SelectionListener() {
    //
    // /**
    // *
    // */
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public void widgetSelected(SelectionEvent e) {
    // System.out.println();
    // }
    //
    // @Override
    // public void widgetDefaultSelected(SelectionEvent e) {
    // // TODO Auto-generated method stub
    //
    // }
    // });

}
