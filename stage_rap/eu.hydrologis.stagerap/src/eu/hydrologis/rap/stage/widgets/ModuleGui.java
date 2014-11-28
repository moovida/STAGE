/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.rap.stage.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import eu.hydrologis.rap.stage.StageSessionPluginSingleton;
import eu.hydrologis.rap.stage.core.FieldData;
import eu.hydrologis.rap.stage.core.ModuleDescription;
import eu.hydrologis.rap.stage.utils.FileUtilities;
import eu.hydrologis.rap.stage.utils.StageConstants;
import eu.hydrologis.rap.stage.utils.StageUtils;

/**
 * A class representing the main tabbed component gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ModuleGui {
    private final ModuleDescription mainModuleDescription;

    private ModuleGuiFactory formGuiFactory = new ModuleGuiFactory();

    private List<ModuleGuiElement> modulesOuputGuiList;

    private List<ModuleGuiElement> modulesInputGuiList;

    private boolean hideComplex;

    public ModuleGui( ModuleDescription mainModuleDescription ) {
        this.mainModuleDescription = mainModuleDescription;
    }

    @SuppressWarnings("nls")
    public Control makeGui( Composite parent, boolean hideComplex ) {
        this.hideComplex = hideComplex;

        modulesInputGuiList = new ArrayList<ModuleGuiElement>();
        modulesOuputGuiList = new ArrayList<ModuleGuiElement>();

        // parent has FillLayout
        // create the tab folder
        final CTabFolder folder = new CTabFolder(parent, SWT.BOTTOM);
        folder.setUnselectedCloseVisible(false);
        folder.setLayout(new FillLayout());
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // for every Tab object create a tab
        String layoutConstraint = "insets 20 20 20 20, fillx";

        makeInputTab(folder, layoutConstraint);
        makeOutputTab(folder, layoutConstraint);
        makeDescription(folder, layoutConstraint);

        return folder;
    }

    private void makeDescription( final CTabFolder folder, String layoutConstraint ) {
        // the tabitem
        CTabItem tab = new CTabItem(folder, SWT.NONE);
        tab.setText("description");

        try {
            Browser browser = new Browser(folder, SWT.NONE);
            GridData layoutData = new GridData(GridData.FILL_BOTH);
            browser.setLayoutData(layoutData);

            String className = mainModuleDescription.getClassName();
            String moduleDocumentationPath = StageUtils.getModuleDocumentationPath(className);

            File docFile = new File(moduleDocumentationPath);
            if (docFile.exists()) {
                String html = FileUtilities.readFile(moduleDocumentationPath);
                browser.setText(html);
            } else {
                browser.setText(moduleDocumentationPath);
            }

            // browser.setUrl("file:" + moduleDocumentationPath);
            tab.setControl(browser);
        } catch (SWTError e) {
            e.printStackTrace();

            Label problemLabel = new Label(folder, SWT.NONE);
            problemLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            problemLabel.setText("An error occurred while loading the documentation.");
            tab.setControl(problemLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeInputTab( final CTabFolder folder, String layoutConstraint ) {
        List<FieldData> inputsList = mainModuleDescription.getInputsList();
        if (inputsList.size() == 0) {
            return;
        }
        if (hideComplex) {
            // if all are complex we do not want te tab
            boolean oneNotComplex = false;
            for( FieldData fieldData : inputsList ) {
                if (fieldData.isSimpleType()) {
                    oneNotComplex = true;
                }
            }
            if (!oneNotComplex) {
                return;
            }
        }

        // the tabitem
        CTabItem tab = new CTabItem(folder, SWT.NONE);
        tab.setText("inputs");
        folder.setSelection(tab);

        // we want the content to scroll
        final ScrolledComposite scroller = new ScrolledComposite(folder, SWT.V_SCROLL | SWT.H_SCROLL);
        scroller.setLayout(new FillLayout());

        // the actual content of the tab
        Composite tabComposite = new Composite(scroller, SWT.NONE);
        layoutConstraint = layoutConstraint + ", gapy 15";
        tabComposite.setLayout(new MigLayout(layoutConstraint, ""));

        // which goes as content to the scrolled composite
        scroller.setContent(tabComposite);
        scroller.setExpandVertical(true);
        scroller.setExpandHorizontal(true);

        inputsList = checkOutputAtEnd(inputsList);

        // the scroller gets the control of the tab item
        tab.setControl(scroller);

        int[] row = new int[]{0};
        for( int j = 0; j < inputsList.size(); j++ ) {
            FieldData inputData = inputsList.get(j);
            if (hideComplex && !inputData.isSimpleType() && !StageUtils.isFieldExceptional(inputData)) {
                continue;
            }

            // remove region related widgets, if the user chose to not have them.
            if (inputData.isProcessingRegionRelated() && StageSessionPluginSingleton.getInstance().doIgnoreProcessingRegion()) {
                continue;
            }

            List<ModuleGuiElement> inputList = formGuiFactory.createInputGui(inputData, row);
            for( ModuleGuiElement moduleGuiElement : inputList ) {
                moduleGuiElement.makeGui(tabComposite);
            }

            modulesInputGuiList.addAll(inputList);

            row[0] = row[0] + 1;
        }

        Point size = folder.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        scroller.setMinHeight(size.y);
        scroller.setMinWidth(size.x);

        addDrop(tabComposite);

    }

    private List<FieldData> checkOutputAtEnd( List<FieldData> inputsList ) {
        List<FieldData> tmpInputsList = new ArrayList<FieldData>();
        List<FieldData> tmpOutputsList = new ArrayList<FieldData>();

        for( int i = 0; i < inputsList.size(); i++ ) {
            FieldData fieldData = inputsList.get(i);
            String guiHints = fieldData.guiHints;

            if (guiHints != null
                    && (guiHints.contains(StageConstants.FILEOUT_UI_HINT) || guiHints.contains(StageConstants.FOLDEROUT_UI_HINT))) {
                tmpOutputsList.add(fieldData);
            } else {
                tmpInputsList.add(fieldData);
            }
        }

        inputsList.clear();
        inputsList.addAll(tmpInputsList);
        inputsList.addAll(tmpOutputsList);
        return inputsList;
    }

    private void makeOutputTab( final CTabFolder folder, String layoutConstraint ) {
        List<FieldData> outputsList = mainModuleDescription.getOutputsList();
        if (outputsList.size() == 0) {
            return;
        }

        // if all are complex we do not want te tab
        boolean atLeastOneIsSimple = false;
        boolean atLeastOneIsSimpleArray = false;
        boolean atLeastOneIsComplex = false;
        for( FieldData fieldData : outputsList ) {
            if (fieldData.isSimpleType()) {
                atLeastOneIsSimple = true;
            } else if (fieldData.isSimpleArrayType()) {
                atLeastOneIsSimpleArray = true;
            } else {
                atLeastOneIsComplex = true;
            }
        }

        /*
         * if we hide the complex and there i sno simple, 
         * do not show the tab.
         */
        if (hideComplex && !atLeastOneIsSimple && atLeastOneIsComplex) {
            return;
        }
        /*
         * if we have only simple ones or simple arrays 
         * do not show the tab.
         */
        if ((atLeastOneIsSimple || atLeastOneIsSimpleArray) && !atLeastOneIsComplex) {
            return;
        }

        // the tabitem
        CTabItem tab = new CTabItem(folder, SWT.NONE);
        tab.setText("outputs");

        // we want the content to scroll
        final ScrolledComposite scroller = new ScrolledComposite(folder, SWT.V_SCROLL);
        scroller.setLayout(new FillLayout());

        // the actual content of the tab
        Composite tabComposite = new Composite(scroller, SWT.NONE);
        tabComposite.setLayout(new MigLayout(layoutConstraint, ""));

        // which goes as content to the scrolled composite
        scroller.setContent(tabComposite);
        scroller.setExpandVertical(true);
        scroller.setExpandHorizontal(true);

        // the scroller gets the control of the tab item
        tab.setControl(scroller);

        int[] row = new int[]{0};
        for( int j = 0; j < outputsList.size(); j++ ) {
            FieldData outputData = outputsList.get(j);
            if (hideComplex && !outputData.isSimpleType()) {
                continue;
            }

            // remove region related widgets, if the user chose to not have them.
            if (outputData.isProcessingRegionRelated() && StageSessionPluginSingleton.getInstance().doIgnoreProcessingRegion()) {
                continue;
            }

            List<ModuleGuiElement> ouputList = formGuiFactory.createOutputGui(outputData, row);
            for( ModuleGuiElement moduleGuiElement : ouputList ) {
                moduleGuiElement.makeGui(tabComposite);
            }
            modulesOuputGuiList.addAll(ouputList);
            row[0] = row[0] + 1;
        }

        Point size = folder.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        scroller.setMinHeight(size.y);
        scroller.setMinWidth(size.x);
    }
    public ModuleDescription getModuleDescription() {
        return mainModuleDescription;
    }

    public List<ModuleGuiElement> getModulesInputGuiList() {
        return modulesInputGuiList;
    }

    public List<ModuleGuiElement> getModulesOuputGuiList() {
        return modulesOuputGuiList;
    }

    private void addDrop( Composite tabComposite ) {
        int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
        DropTarget target = new DropTarget(tabComposite, operations);

        // final TextTransfer textTransfer = TextTransfer.getInstance();
        final FileTransfer fileTransfer = FileTransfer.getInstance();
        final URLTransfer urlTransfer = URLTransfer.getInstance();
        // final UDigByteAndLocalTransfer omsboxTransfer = UDigByteAndLocalTransfer.getInstance();
        Transfer[] types = new Transfer[]{fileTransfer, urlTransfer};// ,
                                                                     // omsboxTransfer};
        target.setTransfer(types);
        target.addDropListener(new DropTargetListener(){
            public void drop( DropTargetEvent event ) {
                // if (textTransfer.isSupportedType(event.currentDataType)) {
                // String text = (String) event.data;
                // System.out.println(text);
                // }
                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    String[] files = (String[]) event.data;
                    if (files.length > 0) {
                        File file = new File(files[0]);
                        if (file.exists()) {
                            setProcessingRegionFromAsciiGrid(file);
                        }
                    }
                }
                if (urlTransfer.isSupportedType(event.currentDataType)) {
                    Object data2 = event.data;
                    System.out.println(data2);
                }
            }
            private void setProcessingRegionFromAsciiGrid( File file ) {
                if (file.getName().toLowerCase().endsWith(".asc")) {
                    // let's try
                    EsriAsciiProcessingRegion r = new EsriAsciiProcessingRegion(file);
                    double[] regionInfo = r.getRegionInfo();
                    if (regionInfo != null) {
                        for( ModuleGuiElement guiElement : modulesInputGuiList ) {
                            if (guiElement instanceof GuiTextField) {
                                GuiTextField textField = (GuiTextField) guiElement;
                                if (textField.isProcessing()) {
                                    textField.setRegion(regionInfo);
                                }
                            }
                        }
                    }
                }
            }
            public void dragEnter( DropTargetEvent event ) {
            }
            public void dragLeave( DropTargetEvent event ) {
            }
            public void dragOperationChanged( DropTargetEvent event ) {
            }
            public void dragOver( DropTargetEvent event ) {
            }
            public void dropAccept( DropTargetEvent event ) {
            }
        });

    }

}
