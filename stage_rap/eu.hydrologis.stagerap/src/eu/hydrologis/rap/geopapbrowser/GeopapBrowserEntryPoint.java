package eu.hydrologis.rap.geopapbrowser;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import eu.hydrologis.rap.stage.utilsrap.LoginDialog;

public class GeopapBrowserEntryPoint extends AbstractEntryPoint {

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
            StageGeopaparazziView geopapView = new StageGeopaparazziView();
            geopapView.createGeopaparazziTab(Display.getCurrent(), parent);
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label(parent, SWT.NONE);
            errorLabel.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
            errorLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
            String errorMessage = "An error occurred: " + e.getLocalizedMessage();
            errorLabel.setText("<span style='font:bold 24px Arial;'>" + errorMessage + "</span>");
        }
    }

}
