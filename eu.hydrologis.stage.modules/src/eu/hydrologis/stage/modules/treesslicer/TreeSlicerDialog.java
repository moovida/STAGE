package eu.hydrologis.stage.modules.treesslicer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.rap.rwt.RWT;
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

public class TreeSlicerDialog extends Dialog {

    public static final String SESSION_USER_KEY = "SESSION_USER";

    private static final long serialVersionUID = 1L;

    private String htmlUrl;

    // private static final String CANCEL = "Cancel";

    public TreeSlicerDialog( Shell parent ) {
        super(parent);
        JsResources.ensureJavaScriptResources();
        htmlUrl = JsResources.ensureHtmlResources();
        // htmlUrl = JsResources.registerIfMissing("trees_info.html");
    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        // if (title != null) {
        // shell.setText(title);
        // }
        
        shell.setMaximized(true);
    }

    @SuppressWarnings("serial")
    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        try {
            final Browser treeSlicerBrowser = new Browser(composite, SWT.NONE);
            treeSlicerBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            // treeSlicerBrowser.setUrl(RWT.getRequest().getContextPath() + "trees_info.html");
            treeSlicerBrowser.setUrl(htmlUrl);
            treeSlicerBrowser.addProgressListener(new ProgressListener(){
                public void completed( ProgressEvent event ) {
                    new BrowserFunction(treeSlicerBrowser, "getPlotFiles"){
                        @Override
                        public Object function( Object[] arguments ) {
                            return "[{\"name\": \"plot1.json\"}, {\"name\": \"plot2.json\"}]";
                        }
                    };
                    treeSlicerBrowser.evaluate("loadScript();");
                }
                public void changed( ProgressEvent event ) {
                }
            });

            // String treeHtml = getTreeHtml();
            // treeSlicerBrowser.setText(treeHtml);

            // new GetSelectedPlotFilesFunction(treeSlicerBrowser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return composite;
    }

    public static String getTreeHtml() throws Exception {
        InputStream inputStream = JsResources.class.getClassLoader().getResourceAsStream("js/trees_info.html");

        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while( (line = reader.readLine()) != null ) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        return "";
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
