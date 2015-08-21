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

public class QuickGeometryViewDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    // private static final String CANCEL = "Cancel";
    private final String title;

    public static final String SESSION_USER_KEY = "SESSION_USER";
    private String quickViewUrl;
    private String geoJson;

    private Browser quickViewBrowser;

    public QuickGeometryViewDialog( Shell parent, String title, String geoJson ) {
        super(parent);
        this.title = title;
        this.geoJson = geoJson;

    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }

        shell.setMaximized(true);
    }

    @SuppressWarnings("serial")
    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        try {
            quickViewBrowser = new Browser(composite, SWT.NONE);
            GridData tablesGraphBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            quickViewBrowser.setLayoutData(tablesGraphBrowserGD);
            
            JsResources.ensureJavaScriptResources();
            quickViewUrl = JsResources.ensureQuickmapHtmlResource();
            quickViewBrowser.setUrl(quickViewUrl);
            quickViewBrowser.addProgressListener(new ProgressListener(){
                public void completed( ProgressEvent event ) {
                    new BrowserFunction(quickViewBrowser, "getJsonData"){
                        @Override
                        public Object function( Object[] arguments ) {
                            return geoJson;
                        }
                    };
                    quickViewBrowser.evaluate("loadScript();");
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
