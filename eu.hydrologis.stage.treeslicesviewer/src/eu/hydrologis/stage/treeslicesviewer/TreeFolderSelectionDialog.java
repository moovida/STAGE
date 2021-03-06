package eu.hydrologis.stage.treeslicesviewer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
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

import eu.hydrologis.stage.libs.utils.ImageCache;
import eu.hydrologis.stage.libs.workspace.StageWorkspace;

/**
 * A folder browser dialog.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class TreeFolderSelectionDialog extends Dialog {

    private static final String FILTER = "Filter";
    private static final String SELECT_FILE = "Select folder";
    private static final int BUTTON_WIDTH = 60;
    private static final int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;

    private File selectedFile;
    private Shell parent;
    private Button okButton;
    private TreeViewer fileFolderViewer;
    private File parentFile;
    private Text filterText;

    public TreeFolderSelectionDialog( Shell parent, File parentFile ) {
        this(parent, SWT.APPLICATION_MODAL, parentFile);
    }

    /**
     * Constructor.
     * 
     * @param parent the parent shell.
     * @param style the style to use.
     * @param parentFile the root folder to start from.
     * @param extentionsToHide the array of extensions to hide in the browser (i.e. 
     *          they are trimmed away from the end of the name).
     * @param allowedExtentions the array of allowed extensions to show.
     * @param foldernamesToHide the array of folder names to hide in the browser (i.e. 
     */
    public TreeFolderSelectionDialog( Shell parent, int style, File parentFile ) {
        super(parent, style);
        this.parent = parent;
        this.parentFile = parentFile;
        checkSubclass();
        setText(SELECT_FILE);
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
                    TreeFolderSelectionDialog.this.selectedFile = null;
                }
            }
        });
        shell.setLayout(new GridLayout(1, false));
    }

    private void createControls() {
        createMainArea();
        createButtons();
        okButton.setEnabled(false);
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
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterText.setText("");
        filterText.addModifyListener(new ModifyListener(){
            public void modifyText( ModifyEvent event ) {
                relayout(true);
            }
        });

        createTableViewer(mainComposite);

        relayout(false);

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
                                if (file.isDirectory()) {
                                    // allowed if the filter fits
                                    String text = filterText.getText();
                                    if (text.length() > 0) {
                                        if (!name.toLowerCase().contains(text.toLowerCase())) {
                                            return false;
                                        }
                                    }
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                        });

                        Arrays.sort(listFiles);
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
                    if (isTreesFolder(file)) {
                        return ImageCache.getInstance().getImage(getDisplay(), ImageCache.GLOBE);
                    } else {
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
                    if (isTreesFolder(selectedFile)) {
                        enableOk = true;
                    }
                }
                if (okButton != null) {
                    okButton.setEnabled(enableOk);
                }
            }

        });

        return fileFolderViewer;
    }

    private boolean isTreesFolder( File file ) {
        if (file.isDirectory()) {
            final File[] jsonFile = new File[1];
            final File[] folderFile = new File[1];
            file.listFiles(new FileFilter(){
                @Override
                public boolean accept( File file ) {
                    if (file.getName().endsWith("json")) {
                        jsonFile[0] = file;
                    } else if (file.isDirectory()) {
                        folderFile[0] = file;
                    }
                    return false;
                }
            });
            if (folderFile[0] == null && jsonFile[0] != null && isPlotsFile(jsonFile[0])) {
                return true;
            }
        }
        return false;
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
        okButton = createButton(composite, SWT.getMessage("SWT_OK"), SWT.OK);
        shell.setDefaultButton(okButton);
        createButton(composite, SWT.getMessage("SWT_Cancel"), SWT.CANCEL);
        okButton.forceFocus();
        okButton.setEnabled(false);
    }

    public static boolean isPlotsFile( File file ) {
        try {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] bArray = new byte[10];
                int read = fis.read(bArray);
                if (read == 10) {
                    String str = new String(bArray);
                    return str.equals("{\"plotData");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void configureShell() {
        shell.setText(SELECT_FILE);
        Rectangle parentSize = parent.getBounds();
        Point prefSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        shell.setSize(prefSize);
        int locationX = (parentSize.width - prefSize.x) / 2 + parentSize.x;
        int locationY = (parentSize.height - prefSize.y) / 2 + parentSize.y;
        shell.setLocation(new Point(locationX, locationY));
        shell.pack();
    }

    private Button createButton( Composite parent, String text, final int buttonId ) {
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
                TreeFolderSelectionDialog.this.returnCode = buttonId;
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

}
