package eu.hydrologis.stage.geopaparazzi.geopapbrowser.utils;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.stage.libs.utilsrap.ImageServiceHandler;

public class ImageDialog extends Dialog {

    private final static String SERVICE_HANDLER = "imageServiceHandler4dialog";
    private final static String IMAGE_KEY = "imageKey4dialog";
    static {
        // register the service handler
        try {
            RWT.getServiceManager().registerServiceHandler(SERVICE_HANDLER, new ImageServiceHandler());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static final String SESSION_USER_KEY = "SESSION_USER";

    private static final long serialVersionUID = 1L;

    // private static final String CANCEL = "Cancel";
    private final String title;
    private long imageId;
    private File dbFile;
    private String imageName;

    public ImageDialog( Shell parent, String title, File dbFile, long imageId, String imageName ) {
        super(parent);
        this.title = title;
        this.dbFile = dbFile;
        this.imageId = imageId;
        this.imageName = imageName;

        try {
            RWT.getServiceManager().registerServiceHandler(SERVICE_HANDLER, new ImageServiceHandler());
        } catch (Exception e1) {
            // TODO
        }
    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        Browser imageBrowser = new Browser(composite, SWT.NONE);
        imageBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        try {
            GeopaparazziImageUtils.setImageInBrowser(imageBrowser, imageId, imageName, dbFile, IMAGE_KEY, SERVICE_HANDLER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        // createButton(parent, IDialogConstants.CANCEL_ID, CANCEL, false);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        super.buttonPressed(buttonId);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(800, 800);
    }

    @Override
    public boolean close() {
        return super.close();
    }

}
