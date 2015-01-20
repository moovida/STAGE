package eu.hydrologis.stage.geopaparazzi.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.rap.rwt.service.ServiceHandler;

import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.DownloadUtils.DownloadService;
import eu.hydrologis.stage.libs.workspace.LoginChecker;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

public class ProjectDownloadServiceHandler implements ServiceHandler {

    public ProjectDownloadServiceHandler() {

    }

    @Override
    public void service( HttpServletRequest req, HttpServletResponse resp ) throws IOException, ServletException {
        String authHeader = req.getHeader("Authorization");
        PrintWriter out = resp.getWriter();

        String[] userPwd = StageUtils.getUserPwdWithBasicAuthentication(authHeader);
        if (userPwd == null || !LoginChecker.isLoginOk(userPwd[0], userPwd[1])) {
            out.print("<b>No permission!</b>");
            out.flush();
            return;
        }

        // userPwd = new String[]{"testuser"};
        Object projectIdObj = req.getParameter("id");
        if (projectIdObj != null) {
            String projectId = (String) projectIdObj;
            File geopaparazziFolder = StageWorkspace.getInstance().getGeopaparazziFolder(userPwd[0]);
            File newGeopaparazziFile = new File(geopaparazziFolder, projectId);

            byte[] data = IOUtils.toByteArray(new FileInputStream(newGeopaparazziFile));
            DownloadService service = new DownloadService(data, newGeopaparazziFile.getName());
            service.register();
            resp.sendRedirect(resp.encodeRedirectURL(service.getURL()));
        } else {
            out.print("<b>ERROR, no project id available!</b>");
            out.flush();
            out.close();
        }

    }

}
