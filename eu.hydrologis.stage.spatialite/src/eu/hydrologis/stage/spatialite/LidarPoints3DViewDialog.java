package eu.hydrologis.stage.spatialite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class LidarPoints3DViewDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    // private static final String CANCEL = "Cancel";
    private final String title;

    public static final String SESSION_USER_KEY = "SESSION_USER";
    private String threejsPointUrl;

    private Browser threejsPointBrowser;

    private Object[] xyzArray;

    private boolean useColors;

    public LidarPoints3DViewDialog( Shell parent, String title, Object[] xyzArray, boolean useColors ) {
        super(parent);
        this.title = title;
        this.xyzArray = xyzArray;
        this.useColors = useColors;
    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }

        // shell.setMaximized(true);
    }

    @SuppressWarnings("serial")
    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        try {
            threejsPointBrowser = new Browser(composite, SWT.NONE);
            GridData tablesGraphBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            threejsPointBrowser.setLayoutData(tablesGraphBrowserGD);

            JsResources.ensureJavaScriptResources();
            threejsPointUrl = JsResources.ensureThreejsPointsHtmlResource();
            threejsPointBrowser.setUrl(threejsPointUrl);
            threejsPointBrowser.addProgressListener(new ProgressListener(){
                public void completed( ProgressEvent event ) {
                    new BrowserFunction(threejsPointBrowser, "getData"){
                        @Override
                        public Object function( Object[] arguments ) {
                            return xyzArray;
                        }
                    };
                    threejsPointBrowser.evaluate("loadScript(" + useColors + ");");
                }
                public void changed( ProgressEvent event ) {
                }
            });
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
        return new Point(1000, 800);
    }

    @Override
    public boolean close() {
        return super.close();
    }

}
