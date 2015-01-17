/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package eu.hydrologis.stage.libs.utilsrap;

import javax.servlet.http.HttpSession;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.stage.libs.workspace.LoginChecker;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;
import eu.hydrologis.stage.libs.workspace.User;

public class LoginDialog extends Dialog {

    public static final String SESSION_USER_KEY = "SESSION_USER";

    private static final long serialVersionUID = 1L;

    private static final String LOGINMESSAGE = "Please sign in with your username and password:";
    private static final String LOGIN = "Login";
    private static final String CANCEL = "Cancel";
    private static final String PASSWORD = "Password:";
    private static final String USERNAME = "Username:";

    private static final int LOGIN_ID = IDialogConstants.CLIENT_ID + 1;
    private Text userText;
    private Text passText;
    private Label mesgLabel;
    private final String title;
    private final String message;
    private String username;
    private String password;

    public LoginDialog( Shell parent, String title, String message ) {
        super(parent);
        this.title = title;
        this.message = message;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername( String username ) {
        this.username = username;
    }

    public String getUsername() {
        return username;
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
        composite.setLayout(new GridLayout(2, false));
        mesgLabel = new Label(composite, SWT.NONE);
        GridData messageData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        messageData.horizontalSpan = 2;
        mesgLabel.setLayoutData(messageData);
        Label userLabel = new Label(composite, SWT.NONE);
        userLabel.setText(USERNAME);
        userText = new Text(composite, SWT.BORDER);
        userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Label passLabel = new Label(composite, SWT.NONE);
        passLabel.setText(PASSWORD);
        passText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        passText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        initilizeDialogArea();
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton(parent, IDialogConstants.CANCEL_ID, CANCEL, false);
        createButton(parent, LOGIN_ID, LOGIN, true);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        if (buttonId == LOGIN_ID) {
            username = userText.getText();
            password = passText.getText();
            setReturnCode(OK);
            close();
        } else {
            password = null;
        }
        super.buttonPressed(buttonId);
    }

    private void initilizeDialogArea() {
        if (message != null) {
            mesgLabel.setText(message);
        }
        if (username != null) {
            userText.setText(username);
        }
        userText.setFocus();
    }

    /**
     * Show login screen and check pwd.
     * 
     * <p>Dummy implementation.
     * 
     * @param shell
     * @return the {@link User} or <code>null</code>.
     */
    public static boolean checkUserLogin( Shell shell ) {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        Object attribute = httpSession.getAttribute(SESSION_USER_KEY);
        if (attribute instanceof String) {
            return true;
        } else {
            String message = LOGINMESSAGE;
            final LoginDialog loginDialog = new LoginDialog(shell, LOGIN, message);
            loginDialog.setUsername(LoginChecker.TESTUSER);
            int returnCode = loginDialog.open();
            if (returnCode == Window.OK) {
                String username = loginDialog.getUsername();
                String password = loginDialog.getPassword();
                if (LoginChecker.isLoginOk(username, password)) {
                    httpSession.setAttribute(SESSION_USER_KEY, username);

                    try {
                        StageWorkspace.getInstance().getDataFolder(username);
                        StageWorkspace.getInstance().getScriptsFolder(username);
                        StageWorkspace.getInstance().getGeopaparazziFolder(username);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageDialogUtil.openError(shell, "Error", "An error occurred while trying to access the workspace.",
                                null);
                    }
                    return true;
                }
            }
            return false;
        }
    }

}
