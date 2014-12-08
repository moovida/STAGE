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

        try {
            StageView view = new StageView();
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

    // private Control createLogColumn( Composite parent ) {
    // Composite column = new Composite(parent, SWT.NONE);
    // column.setLayout(ExampleUtil.createGridLayout(1, false, true, true));
    // ExampleUtil.createHeading(column, "Server log", 2);
    // logText = new Text(column, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.BORDER);
    // logText.setText(INITIAL_TEXT);
    // logText.setLayoutData(ExampleUtil.createFillData());
    //
    // File f = new File(".");
    // File[] listFiles = f.listFiles();
    // StringBuilder sb = new StringBuilder();
    // for( File file : listFiles ) {
    // sb.append(file.getName()).append("\n");
    // }
    // logText.setText(sb.toString());
    //
    // createClearButton(column);
    // return column;
    // }
    //
    // private static Control createInfoColumn( Composite parent ) {
    // Label label = new Label(parent, SWT.NONE);
    // label.setLayoutData(ExampleUtil.createFillData());
    // return label;
    // }

}
