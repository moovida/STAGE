package eu.hydrologis.stage.spatialite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.sun.media.imageioimpl.plugins.jpeg2000.ComponentMappingBox;

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
        composite.setLayout(new GridLayout(1, false));

        try {

            Composite buttonsComposite = new Composite(composite, SWT.NONE);
            buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
            GridLayout buttonsCompositeLayout = new GridLayout(8, false);
            buttonsCompositeLayout.marginWidth = 5;
            buttonsComposite.setLayout(buttonsCompositeLayout);

            Button downloadButton = new Button(buttonsComposite, SWT.PUSH);
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

            Button resetZoomButton = new Button(buttonsComposite, SWT.PUSH);
            resetZoomButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
            resetZoomButton.setText("Reset Zoom");
            resetZoomButton.addSelectionListener(new SelectionAdapter(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    tablesGraphBrowser.evaluate("resetZoom()");
                }
            });

//            final Button zoomToggleButton = new Button(buttonsComposite, SWT.CHECK | SWT.BORDER);
//            GridData zoomToggleButtonGD = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
//            zoomToggleButtonGD.horizontalIndent = 5;
//            zoomToggleButton.setLayoutData(zoomToggleButtonGD);
//            zoomToggleButton.setText("Toggle Pan");
//            zoomToggleButton.addSelectionListener(new SelectionAdapter(){
//                @Override
//                public void widgetSelected( SelectionEvent e ) {
//                    if (zoomToggleButton.getSelection()) {
//                        tablesGraphBrowser.evaluate("enableZoom();");
//                    } else {
//                        tablesGraphBrowser.evaluate("disableZoom();");
//                    }
//                }
//            });

            Label scaleLabel = new Label(buttonsComposite, SWT.NONE);
            scaleLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            scaleLabel.setText("Scale");

            List<String> scalesList = new ArrayList<>();
            for( int i = 1; i <= 10; i++ ) {
                double scale = i / 10.0;
                scalesList.add("" + scale);
            }
            for( int i = 2; i < 10; i++ ) {
                scalesList.add("" + i);
            }

            final Combo scalesCombo = new Combo(buttonsComposite, SWT.DROP_DOWN);
            scalesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            scalesCombo.setItems(scalesList.toArray(new String[0]));
            scalesCombo.addSelectionListener(new SelectionListener(){
                @Override
                public void widgetSelected( SelectionEvent e ) {
                    int selectionIndex = scalesCombo.getSelectionIndex();
                    String selectedScale = scalesCombo.getItem(selectionIndex);
                    tablesGraphBrowser.evaluate("setScale(" + selectedScale + ");");
                }

                @Override
                public void widgetDefaultSelected( SelectionEvent e ) {
                }
            });
            scalesCombo.select(9);

            tablesGraphBrowser = new Browser(composite, SWT.NONE);
            GridData tablesGraphBrowserGD = new GridData(SWT.FILL, SWT.FILL, true, true);
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
