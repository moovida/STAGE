//package eu.hydrologis.rap.stage.utils;
//
//import org.eclipse.rap.rwt.RWT;
//import org.eclipse.rap.rwt.service.ServiceHandler;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.browser.Browser;
//import org.eclipse.swt.widgets.Shell;
//
//public class DownloadUtils2 {
//
//	public void downloadFile(Shell shell, String filePath) {
//		final Browser browser = new Browser(shell, SWT.NONE);
//		browser.setSize(0, 0);
//		browser.setUrl(createDownloadUrl(filePath));
//	}
//
//	private String createDownloadUrl(final String fileName) {
//		final StringBuilder url = new StringBuilder();
//		url.append(RWT.getRequest().getContextPath());
//		url.append(RWT.getRequest().getServletPath());
//		url.append("?");
//		url.append(IServiceHandler.REQUEST_PARAM);
//		url.append("=downloadServiceHandler");
//		url.append("&filename=");
//		url.append(fileName);
//		return RWT.getResponse().encodeURL(url.toString());
//	}
//	
//	
//	public class DownloadServiceHandler implements ServiceHandler
//	{
//	  public void service() throws IOException, ServletException
//	  {
//	    final String fileName = RWT.getRequest().getParameter("filename");
//	    final byte[] download = getYourFileContent().getBytes();
//	    // Send the file in the response
//	    final HttpServletResponse response = RWT.getResponse();
//	    response.setContentType("application/octet-stream");
//	    response.setContentLength(download.length);
//	    final String contentDisposition = "attachment; filename=\"" + fileName + "\"";
//	    response.setHeader("Content-Disposition", contentDisposition);
//	    response.getOutputStream().write(download);
//	  }
//	}
//
//}
