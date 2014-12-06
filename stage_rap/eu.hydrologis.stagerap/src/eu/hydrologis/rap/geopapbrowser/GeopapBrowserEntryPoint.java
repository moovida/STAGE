package eu.hydrologis.rap.geopapbrowser;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class GeopapBrowserEntryPoint extends AbstractEntryPoint {



	@Override
	protected void createContents(final Composite parent) {
	    HttpServletRequest request = RWT.getRequest();
	    String var1 = request.getParameter( "var1" );
	    String var2 = request.getParameter( "var2" );
	    
	    Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        label.setText(var1 + "/" + var2);
        
	    System.out.println("Passed");
	}

}
