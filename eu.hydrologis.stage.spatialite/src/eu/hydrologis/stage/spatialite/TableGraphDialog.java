package eu.hydrologis.stage.spatialite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utilsrap.DownloadUtils;

public class TableGraphDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    // private static final String CANCEL = "Cancel";
    private final String title;

    public static final String SESSION_USER_KEY = "SESSION_USER";
    private String graphUrl;
    private String json;

    private Browser tablesGraphBrowser;

    public TableGraphDialog( Shell parent, String title, String json ) {
        super(parent);
        this.title = title;
        this.json = json;

        JsResources.ensureJavaScriptResources();
        graphUrl = JsResources.ensureGraphHtmlResource();
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
        composite.setLayout(new GridLayout(2, false));

        try {
            Button downloadButton = new Button(composite, SWT.PUSH);
            downloadButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
            downloadButton.setText("Download svg");
            downloadButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    Object evaluate = tablesGraphBrowser.evaluate("return getSvg()");
                    if (evaluate != null) {
                        byte[] bytes = evaluate.toString().getBytes();
                        try {
                            String fileName = "tables_graph.svg";
                            new DownloadUtils().sendDownload(getParentShell(), fileName, bytes);
                        } catch (IOException e1) {
                            StageLogger.logError(TableGraphDialog.this, e1);
                        }
                    }
                }
            });

            final Button zoomToggleButton = new Button(composite, SWT.CHECK);
            zoomToggleButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
            zoomToggleButton.setText("Toggle zoom behaviour");
            zoomToggleButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    if (zoomToggleButton.getSelection()) {
                        tablesGraphBrowser.evaluate("enableZoom();");
                    } else {
                        tablesGraphBrowser.evaluate("disableZoom();");
                    }
                }
            });

            tablesGraphBrowser = new Browser(composite, SWT.NONE);
            GridData tablesGraphBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
            tablesGraphBrowserGD.horizontalSpan = 2;
            tablesGraphBrowser.setLayoutData(tablesGraphBrowserGD);
            tablesGraphBrowser.setUrl(graphUrl);
            tablesGraphBrowser.addProgressListener(new ProgressListener(){
                public void completed( ProgressEvent event ) {
                    new BrowserFunction(tablesGraphBrowser, "getData"){
                        @Override
                        public Object function( Object[] arguments ) {
                            return json;
                        }
                    };
                    tablesGraphBrowser.evaluate("loadScript();");
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
