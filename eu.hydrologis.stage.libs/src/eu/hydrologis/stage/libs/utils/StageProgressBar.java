package eu.hydrologis.stage.libs.utils;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class StageProgressBar {

    private ProgressBar progressBar;
    private ServerPushSession serverPush;
    private Composite mainComposite;
    private GridData pBarGridData;
    private Label progressLabel;

    public StageProgressBar( final Composite parent, int style, GridData gridData ) {
        serverPush = new ServerPushSession();

        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setLayoutData(gridData);
        mainComposite.setLayout(new GridLayout(2, false));
        this.mainComposite = mainComposite;
        progressLabel = new Label(mainComposite, SWT.NONE);
        progressLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        progressLabel.setText("");

        progressBar = new ProgressBar(mainComposite, style);
        pBarGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        progressBar.setLayoutData(pBarGridData);

        mainComposite.setVisible(false);
    }

    public void reStyle( int newStyle ) {
        if (progressBar != null)
            progressBar.dispose();
        progressBar = new ProgressBar(mainComposite, newStyle);
        progressBar.setLayoutData(pBarGridData);
    }

    public void start( int max ) {
        progressBar.setMaximum(max);
        progressBar.setState(SWT.NORMAL);
        mainComposite.setVisible(true);
        serverPush.start();
    }

    public void setProgressText( String title ) {
        progressLabel.setText(title);
    }

    public void setSelection( int selection ) {
        if (!progressBar.isDisposed()) {
            progressBar.setSelection(selection);
        }
    }

    public void stop() {
        serverPush.stop();
        progressLabel.setText("");
        mainComposite.setVisible(false);
    }

    public void dispose() {
        progressBar.dispose();
        progressLabel.dispose();
        mainComposite.dispose();
    }

}
