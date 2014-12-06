package eu.hydrologis.rap.stage.workspace;

import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.RWT;

import eu.hydrologis.rap.stage.utilsrap.LoginDialog;

public class User {

    private String name;

    public User( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the name of the current session user.
     */
    public static String getCurrentUserName() {
        HttpSession httpSession = RWT.getUISession().getHttpSession();
        Object attribute = httpSession.getAttribute(LoginDialog.SESSION_USER_KEY);
        if (attribute instanceof String) {
            return (String) attribute;
        }
        throw new IllegalArgumentException("No user defined for the session.");
    }
}
