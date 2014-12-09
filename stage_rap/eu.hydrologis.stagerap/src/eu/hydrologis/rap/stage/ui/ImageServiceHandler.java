package eu.hydrologis.rap.stage.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ServiceHandler;

public class ImageServiceHandler implements ServiceHandler {
    public void service( HttpServletRequest request, HttpServletResponse response ) throws IOException, ServletException {
        String id = request.getParameter("imageId");
        BufferedImage image = (BufferedImage) RWT.getUISession().getAttribute(id);
        response.setContentType("image/png");
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "png", out);
    }
}