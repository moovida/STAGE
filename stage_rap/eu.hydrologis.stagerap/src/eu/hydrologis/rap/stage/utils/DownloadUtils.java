package eu.hydrologis.rap.stage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DownloadUtils {
	public boolean sendDownload(Shell parentShell, File file) throws FileNotFoundException,
			IOException {
		byte[] data = IOUtils.toByteArray(new FileInputStream(file));
		DownloadService service = new DownloadService(data, file.getName());
		service.register();

		// UrlLauncher launcher = RWT.getClient().getService(UrlLauncher.class);
		// launcher.openURL(service.getURL());

		final Browser browser = new Browser(parentShell, SWT.NONE);
		browser.setSize(0, 0);
		browser.setUrl(service.getURL());
		return true;
	}

	private static final class DownloadService implements ServiceHandler {

		private final byte[] data;
		private final String filename;
		private String id;

		public DownloadService(byte[] data, String filename) {
			this.data = data;
			this.filename = filename;
			this.id = calculateId();
		}

		public String getURL() {
			return RWT.getServiceManager().getServiceHandlerUrl(getId());
		}

		private String getId() {
			return id;
		}

		private String calculateId() {
			return String.valueOf(System.currentTimeMillis()) + data.length;
		}

		public boolean register() {
			try {
				RWT.getServiceManager().registerServiceHandler(getId(), this);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		private boolean unregister() {
			try {
				RWT.getServiceManager().unregisterServiceHandler(getId());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public void service(HttpServletRequest request,
				HttpServletResponse response) throws IOException,
				ServletException {
			try {
				response.setContentType("application/octet-stream");
				response.setContentLength(data.length);
				response.setHeader("Content-Disposition",
						"attachment; filename=\"" + filename + "\"");
				response.getOutputStream().write(data);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				unregister();
			}
		}

	}

}
