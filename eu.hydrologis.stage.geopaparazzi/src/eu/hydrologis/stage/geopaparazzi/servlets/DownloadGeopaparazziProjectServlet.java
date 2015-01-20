/*
 * Stage - Spatial Toolbox And Geoscript Environment 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html).
 */
package eu.hydrologis.stage.geopaparazzi.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.lifecycle.UICallBack;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.swt.widgets.Display;

import eu.hydrologis.stage.libs.utils.StageUtils;
import eu.hydrologis.stage.libs.utilsrap.DownloadUtils.DownloadService;
import eu.hydrologis.stage.libs.workspace.LoginChecker;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * The geopaparazzi project download servlet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class DownloadGeopaparazziProjectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        PrintWriter out = resp.getWriter();
        
        
        HttpSession session = req.getSession();
        
        HttpServletResponse response = RWT.getResponse();
        ApplicationContext applicationContext = RWT.getApplicationContext();
        
        
        String[] userPwd = StageUtils.getUserPwdWithBasicAuthentication(authHeader);
        if (userPwd == null || !LoginChecker.isLoginOk(userPwd[0], userPwd[1])) {
            out.print("<b>No permission!</b>");
            out.flush();
            return;
        }

        Object projectIdObj = req.getParameter("id");
        if (projectIdObj != null) {
            String projectId = (String) projectIdObj;
            File geopaparazziFolder = StageWorkspace.getInstance().getGeopaparazziFolder(userPwd[0]);
            File newGeopaparazziFile = new File(geopaparazziFolder, projectId);

            // servletWay(resp, newGeopaparazziFile);

            rapWay(newGeopaparazziFile);
        } else {
            out.print("<b>ERROR, no project id available!</b>");
            out.flush();
            out.close();
        }

    }

    private void rapWay( final File newGeopaparazziFile ) throws IOException, FileNotFoundException {
        final byte[] data = IOUtils.toByteArray(new FileInputStream(newGeopaparazziFile));


        final Display display = new Display();
        final Runnable bgRunnable = new Runnable(){
            public void run() {
                RWT.getUISession(display).exec(new Runnable(){
                    public void run() {
                        DownloadService service = new DownloadService(data, newGeopaparazziFile.getName());
                        service.register();
                        UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
                        launcher.openURL(service.getURL());
                        // resp.sendRedirect( resp.encodeRedirectURL(service.getURL()) );
                    }
                });
            }
        };
        Thread thread = new Thread(bgRunnable);
        thread.setDaemon(true);
        thread.start();
    }

    private void servletWay( HttpServletResponse resp, File newGeopaparazziFile ) throws IOException, FileNotFoundException {
        resp.setContentType("application/octet-stream");
        resp.setContentLength((int) newGeopaparazziFile.length());
        resp.setHeader("Content-Disposition", "filename=" + newGeopaparazziFile.getName());
        OutputStream outSTream = resp.getOutputStream();
        FileInputStream in = new FileInputStream(newGeopaparazziFile);
        byte[] buffer = new byte[4096];
        int length;
        while( (length = in.read(buffer)) > 0 ) {
            outSTream.write(buffer, 0, length);
            System.out.print("*");
        }
        System.out.println("");
        in.close();
        outSTream.flush();
        outSTream.close();
    }
}