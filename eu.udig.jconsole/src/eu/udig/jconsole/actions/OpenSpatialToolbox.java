/*
 * uDig - User Friendly Desktop Internet GIS client
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.udig.jconsole.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import eu.udig.omsbox.view.OmsBoxView;

/**
 * Action to open the spatial toolbox view.
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class OpenSpatialToolbox implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;

    public void dispose() {
    }

    public void init( IWorkbenchWindow window ) {
        this.window = window;
    }

    public void run( IAction action ) {
        IWorkbench wb = PlatformUI.getWorkbench();
        IProgressService ps = wb.getProgressService();
        try {
            ps.busyCursorWhile(new IRunnableWithProgress(){
                public void run( IProgressMonitor pm ) {
                    Display.getDefault().syncExec(new Runnable(){
                        public void run() {
                            try {
                                window.getActivePage().showView(OmsBoxView.ID);
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void selectionChanged( IAction action, ISelection selection ) {
    }

}
