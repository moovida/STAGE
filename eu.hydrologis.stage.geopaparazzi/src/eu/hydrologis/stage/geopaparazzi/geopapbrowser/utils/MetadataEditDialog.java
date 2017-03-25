package eu.hydrologis.stage.geopaparazzi.geopapbrowser.utils;

import static org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.TABLE_METADATA;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.compat.IJGTStatement;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.MetadataTableFields;

import eu.hydrologis.stage.geopaparazzi.geopapbrowser.ProjectInfo;

public class MetadataEditDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    // private static final String CANCEL = "Cancel";
    private final String title;
    private File dbFile;

    private static final String OK = "Ok";
    private static final String CANCEL = "Cancel";

    private LinkedHashMap<String, String> metadataMap;
    private List<Text> textWidgets = new ArrayList<Text>();

    private ProjectInfo selectedProject;

    public MetadataEditDialog( Shell parent, String title, ProjectInfo selectedProject ) throws Exception {
        super(parent);
        this.title = title;
        this.selectedProject = selectedProject;
        dbFile = selectedProject.databaseFile;

        textWidgets.clear();

        metadataMap = new LinkedHashMap<>();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath())) {
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30); // set timeout to 30 sec.

                String sql = "select " + MetadataTableFields.COLUMN_KEY.getFieldName() + ", " + //
                        MetadataTableFields.COLUMN_VALUE.getFieldName() + " from " + TABLE_METADATA;

                ResultSet rs = statement.executeQuery(sql);
                while( rs.next() ) {
                    String key = rs.getString(MetadataTableFields.COLUMN_KEY.getFieldName());
                    String value = rs.getString(MetadataTableFields.COLUMN_VALUE.getFieldName());
                    if (!key.endsWith("ts")) {
                        // timestamps can't be changed
                        metadataMap.put(key, value);
                    }
                }

            }

        }

    }

    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea( Composite parent ) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        final ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setExpandHorizontal(true);

        final Composite main = new Composite(scrolledComposite, SWT.NONE);
        main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        main.setLayout(new GridLayout(1, false));

        Set<Entry<String, String>> entrySet = metadataMap.entrySet();
        for( Entry<String, String> entry : entrySet ) {
            String key = entry.getKey();
            String value = entry.getValue();

            Group group = new Group(main, SWT.NONE);
            group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            group.setLayout(new GridLayout(1, false));
            group.setText(key);

            int lines = value.length() / 30;

            Text text;
            GridData textGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
            if (lines <= 1) {
                text = new Text(group, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
            } else {
                text = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER);
                textGD.verticalSpan = lines;
            }
            text.setLayoutData(textGD);
            text.setText(value);
            text.setData(key);

            textWidgets.add(text);
        }

        scrolledComposite.setContent(main);
        Point point = main.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        main.setSize(point);
        scrolledComposite.setMinSize(point);
        scrolledComposite.addControlListener(new ControlAdapter(){
            private static final long serialVersionUID = 1L;

            @Override
            public void controlResized( ControlEvent e ) {
                Rectangle r = scrolledComposite.getClientArea();
                scrolledComposite.setMinSize(main.computeSize(r.width, SWT.DEFAULT));
            }
        });

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar( Composite parent ) {
        createButton(parent, IDialogConstants.CANCEL_ID, CANCEL, false);
        createButton(parent, IDialogConstants.OK_ID, OK, false);
    }

    @Override
    protected void buttonPressed( int buttonId ) {
        if (buttonId == IDialogConstants.OK_ID) {
            // save data
            try {
                try (SqliteDb db = new SqliteDb()) {
                    db.open(dbFile.getAbsolutePath());
                    IJGTConnection connection = db.getConnection();
                    try (IJGTStatement statement = connection.createStatement()) {
                        statement.setQueryTimeout(30); // set timeout to 30 sec.

                        for( Text text : textWidgets ) {
                            String key = text.getData().toString();
                            String textData = text.getText();

                            String query = "update " + TABLE_METADATA + " set value='" + textData + "'  where key='" + key + "';";

                            statement.executeUpdate(query);
                        }
                    }
                    String projectInfo = GeopaparazziWorkspaceUtilities.getProjectInfo(connection);
                    selectedProject.metadata = projectInfo;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.buttonPressed(buttonId);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 600);
    }

    @Override
    public boolean close() {
        return super.close();
    }

}
