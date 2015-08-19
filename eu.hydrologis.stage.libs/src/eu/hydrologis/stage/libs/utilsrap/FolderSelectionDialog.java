package eu.hydrologis.stage.libs.utilsrap;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.hydrologis.stage.libs.log.StageLogger;
import eu.hydrologis.stage.libs.utils.FileUtilities;
import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * A folder browser dialog.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class FolderSelectionDialog extends Dialog implements ModifyListener {

    private static final String FILTER = "Filter";
    private static final String NEW_FOLDER_NAME = "New folder name";
    private static final String SELECT_FOLDER = "Select folder";
    private static final int BUTTON_WIDTH = 60;
    private static final int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;

    private File selectedFile;
    private Shell parent;
    private Button okButton;
    private TreeViewer fileFolderViewer;
    private File parentFile;
    private Text filterText;
    private String[] foldernamesToHide;
    private boolean save;
    private Text folderNameText;

    public FolderSelectionDialog( Shell parent, boolean save, File parentFile, String[] foldernamesToHide ) {
        this(parent, save, SWT.APPLICATION_MODAL, parentFile, foldernamesToHide);
    }

    /**
     * Constructor.
     * 
     * @param parent the parent shell.
     * @param save it <code>true</code>, save mode is used instead of open mode.
     * @param style the style to use.
     * @param parentFile the root folder to start from.
     * @param foldernamesToHide the array of folder names to hide in the browser (i.e. 
     */
    public FolderSelectionDialog( Shell parent, boolean save, int style, File parentFile, String[] foldernamesToHide ) {
        super(parent, style);
        this.parent = parent;
        this.save = save;
        this.parentFile = parentFile;
        selectedFile = parentFile;
        this.foldernamesToHide = foldernamesToHide;
        checkSubclass();
        setText(SELECT_FOLDER);
    }

    public int open() {
        checkOperationMode();
        prepareOpen();
        runEventLoop(shell);
        return returnCode;
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Getter for the relative file path.
     * 
     * @return the file path relative to the user data space.
     */
    public String getSelectedFileRelativePath() {
        if (selectedFile != null) {
            String relativePath = StageWorkspace.makeRelativeToDataFolder(selectedFile);
            return relativePath;
        }
        return null;
    }

    @Override
    protected void prepareOpen() {
        createShell();
        createControls();
        configureShell();
    }

    private void createShell() {
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.addShellListener(new ShellAdapter(){
            @Override
            public void shellClosed( ShellEvent event ) {
                if (returnCode == SWT.CANCEL) {
                    FolderSelectionDialog.this.selectedFile = null;
                }
            }
        });
        shell.setLayout(new GridLayout(1, false));
    }

    private void createControls() {
        createMainArea();
        createButtons();
    }

    private void createMainArea() {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridData mainCompositeGD = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        mainCompositeGD.widthHint = 600;
        mainCompositeGD.heightHint = 400;
        mainComposite.setLayoutData(mainCompositeGD);
        mainComposite.setLayout(new GridLayout(2, false));

        Label filterLabel = new Label(mainComposite, SWT.NONE);
        GridData filterLabelGD = new GridData(SWT.FILL, SWT.FILL, false, false);
        filterLabelGD.widthHint = SWT.DEFAULT;
        filterLabelGD.heightHint = SWT.DEFAULT;
        filterLabel.setLayoutData(filterLabelGD);
        filterLabel.setText(FILTER);

        filterText = new Text(mainComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        filterText.setText("");
        filterText.addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent event ) {
                relayout(true);
            }
        });

        createTableViewer(mainComposite);

        if (save) {
            Label folderNameLabel = new Label(mainComposite, SWT.NONE);
            GridData folderNameLabelGD = new GridData(SWT.FILL, SWT.FILL, false, false);
            folderNameLabelGD.widthHint = SWT.DEFAULT;
            folderNameLabelGD.heightHint = SWT.DEFAULT;
            folderNameLabel.setLayoutData(folderNameLabelGD);
            folderNameLabel.setText(NEW_FOLDER_NAME);
            folderNameText = new Text(mainComposite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            folderNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            folderNameText.setText("");
            folderNameText.addModifyListener(this);
        }
        relayout(false);

    }

    private void makeSafe() {
        String text = folderNameText.getText();
        boolean changed = false;
        if (text.contains("\\.\\.")) {
            // no dots allowed
            text = text.replaceAll("\\.\\.", "");
            changed = true;
        }
        if (text.indexOf('\\') != -1) {
            text = FileUtilities.checkBackSlash(text);
            changed = true;
        }
        if (changed) {
            folderNameText.removeModifyListener(this);
            folderNameText.setText(text);
            folderNameText.addModifyListener(this);
        }
    }

    private TreeViewer createTableViewer( Composite parentComposite ) {
        fileFolderViewer = new TreeViewer(parentComposite);

        Control control = fileFolderViewer.getControl();
        GridData controlGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        controlGD.horizontalSpan = 2;
        control.setLayoutData(controlGD);
        fileFolderViewer.setContentProvider(new ITreeContentProvider(){
            private static final long serialVersionUID = 1L;

            public Object[] getElements( Object inputElement ) {
                return getChildren(inputElement);
            }

            public Object[] getChildren( Object parentElement ) {
                if (parentElement instanceof File) {
                    File parentFile = (File) parentElement;
                    if (parentFile.isDirectory()) {
                        File[] listFiles = parentFile.listFiles(new FileFilter(){
                            @Override
                            public boolean accept( File file ) {
                                String name = file.getName();
                                if (name.startsWith(".")) {
                                    return false;
                                }
                                // allow folders
                                if (!file.isDirectory()) {
                                    return false;
                                } else {
                                    if (foldernamesToHide != null) {
                                        for( String ext : foldernamesToHide ) {
                                            if (name.endsWith(ext)) {
                                                return false;
                                            }
                                        }
                                    }
                                    // allowed if the filter fits
                                    String text = filterText.getText();
                                    if (text.length() > 0) {
                                        if (!name.toLowerCase().contains(text.toLowerCase())) {
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                            }
                        });
                        if (StageLogger.LOG_DEBUG) {
                            StageLogger.logDebug(this, "getChildren of: " + parentElement);
                        }
                        if (listFiles != null) {
                            Arrays.sort(listFiles);
                        } else {
                            listFiles = new File[0];
                        }
                        return listFiles;
                    }
                }
                return new Object[0];
            }

            public Object getParent( Object element ) {
                if (element instanceof File) {
                    File file = (File) element;
                    return file.getParent();
                }
                return null;
            }

            public boolean hasChildren( Object element ) {
                return getChildren(element).length > 0;
            }

            public void dispose() {
            }

            public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
            }

        });

        fileFolderViewer.setLabelProvider(new LabelProvider(){
            private static final long serialVersionUID = 1L;

            public Image getImage( Object element ) {
                if (element instanceof File) {
                    File file = (File) element;
                    if (file.isDirectory()) {
                        return ImageCache.getInstance().getImage(getDisplay(), ImageCache.FOLDER);
                    }
                }
                return null;
            }

            public String getText( Object element ) {
                if (element instanceof File) {
                    File file = (File) element;
                    String name = file.getName();
                    return name;
                }
                return ""; //$NON-NLS-1$
            }
        });

        fileFolderViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged( SelectionChangedEvent event ) {
                if (!(event.getSelection() instanceof IStructuredSelection)) {
                    return;
                }
                IStructuredSelection sel = (IStructuredSelection) event.getSelection();

                Object selectedItem = sel.getFirstElement();
                if (selectedItem == null) {
                    return;
                }

                boolean enableOk = false;
                if (selectedItem instanceof File) {
                    selectedFile = (File) selectedItem;
                    enableOk = true;
                }
            }

        });

        return fileFolderViewer;
    }

    public void relayout( final boolean expandAll ) {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                fileFolderViewer.setInput(parentFile);
                if (expandAll)
                    fileFolderViewer.expandAll();
            }
        });
    }

    private void createButtons() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayout(new GridLayout(0, true));
        GridData gridData = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        composite.setLayoutData(gridData);
        okButton = createOkButton(composite);
        shell.setDefaultButton(okButton);
        createCancelButton(composite);
        okButton.forceFocus();
    }

    private void configureShell() {
        shell.setText(SELECT_FOLDER);
        Rectangle parentSize = parent.getBounds();
        Point prefSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        shell.setSize(prefSize);
        int locationX = (parentSize.width - prefSize.x) / 2 + parentSize.x;
        int locationY = (parentSize.height - prefSize.y) / 2 + parentSize.y;
        shell.setLocation(new Point(locationX, locationY));
        shell.pack();
    }

    private Button createOkButton( Composite parent ) {
        String text = SWT.getMessage("SWT_OK");
        final int buttonId = SWT.OK;
        ((GridLayout) parent.getLayout()).numColumns++;
        Button result = new Button(parent, SWT.PUSH);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = convertHorizontalDLUsToPixels(shell, BUTTON_WIDTH);
        Point minSize = result.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        result.setLayoutData(data);
        result.setText(text);
        result.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent event ) {
                if (save) {
                    String newFolderName = folderNameText.getText().trim();
                    if (newFolderName.length() > 0) {
                        File newFolderFile = new File(selectedFile, newFolderName);
                        selectedFile = newFolderFile;
                        if (!selectedFile.exists()) {
                            selectedFile.mkdirs();
                        }
                    }
                }
                FolderSelectionDialog.this.returnCode = buttonId;
                shell.close();
            }
        });
        return result;
    }

    private Button createCancelButton( Composite parent ) {
        String text = SWT.getMessage("SWT_Cancel");
        final int buttonId = SWT.CANCEL;
        ((GridLayout) parent.getLayout()).numColumns++;
        Button result = new Button(parent, SWT.PUSH);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = convertHorizontalDLUsToPixels(shell, BUTTON_WIDTH);
        Point minSize = result.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        result.setLayoutData(data);
        result.setText(text);
        result.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected( SelectionEvent event ) {
                FolderSelectionDialog.this.returnCode = buttonId;
                shell.close();
            }
        });
        return result;
    }

    private static int convertHorizontalDLUsToPixels( Control control, int dlus ) {
        Font dialogFont = control.getFont();
        float charWidth = TextSizeUtil.getAvgCharWidth(dialogFont);
        float width = charWidth * dlus + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2;
        return (int) (width / HORIZONTAL_DIALOG_UNIT_PER_CHAR);
    }

    private Display getDisplay() {
        return parent.getDisplay();
    }

    @Override
    public void modifyText( ModifyEvent event ) {
        makeSafe();
    }

}
