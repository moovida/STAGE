package eu.hydrologis.jconsole;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import eu.udig.omsbox.view.OmsBoxView;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor( IWorkbenchWindowConfigurer configurer ) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor( IActionBarConfigurer configurer ) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(1000, 600));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
    }

    @Override
    public void postWindowOpen() {

        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OmsBoxView.ID);
        } catch (PartInitException e) {
            e.printStackTrace();
        }

        // File f = null;
        // JavaFileEditorInput jFile = new JavaFileEditorInput(f);
        // try {
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(jFile,
        // JavaEditor.ID);
        // } catch (PartInitException e) {
        // e.printStackTrace();
        // }
    }

}
