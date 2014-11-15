package eu.hydrologis.jconsole;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use
    // them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.

    public ApplicationActionBarAdvisor( IActionBarConfigurer configurer ) {
        super(configurer);
    }

    protected void fillStatusLine( IStatusLineManager statusLine ) {
        // statusLine.add(new StatusLineContributor());
    }

    // class StatusLineContributor extends ContributionItem {
    // @Override
    // public void fill( Composite parent ) {
    // Composite statusLine = parent;
    //
    // StatusLineLayoutData sepLD = new StatusLineLayoutData();
    // sepLD.heightHint = -1;
    // Label sep = new Label(parent, SWT.SEPARATOR);
    // sep.setLayoutData(sepLD);
    //
    // CLabel logLevelLabel = new CLabel(statusLine, SWT.SHADOW_NONE);
    // StatusLineLayoutData logLevelLD = new StatusLineLayoutData();
    // logLevelLD.widthHint = -1;
    // logLevelLabel.setLayoutData(logLevelLD);
    // logLevelLabel.setText("Log level");
    //
    // final String[] logLevels = {"OFF", "CONFIG", "ALL"};
    // final Combo loggerCombo = new Combo(statusLine, SWT.DROP_DOWN | SWT.READ_ONLY);
    // loggerCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    // loggerCombo.setItems(logLevels);
    // selectLog(logLevels, loggerCombo);
    //
    // StatusLineLayoutData logComboLD = new StatusLineLayoutData();
    // logComboLD.widthHint = -1;
    // loggerCombo.setLayoutData(logComboLD);
    // loggerCombo.addSelectionListener(new SelectionAdapter(){
    // public void widgetSelected( SelectionEvent e ) {
    // int selectionIndex = loggerCombo.getSelectionIndex();
    // eu.udig.jconsole.JConsoleEditorPlugin.getDefault().setLoggerLevel(logLevels[selectionIndex]);
    // }
    // });
    // loggerCombo.addControlListener(new ControlAdapter(){
    // public void controlMoved( ControlEvent e ) {
    // super.controlMoved(e);
    // // something odd happens with the combo, workaround it here
    // if (logLevels != null && loggerCombo != null) {
    // selectLog(logLevels, loggerCombo);
    // }
    // }
    // });
    //
    // // RAM
    // sepLD = new StatusLineLayoutData();
    // sepLD.heightHint = -1;
    // sep = new Label(parent, SWT.SEPARATOR);
    // sep.setLayoutData(sepLD);
    //
    // CLabel ramLabel = new CLabel(statusLine, SWT.SHADOW_NONE);
    // StatusLineLayoutData ramLD = new StatusLineLayoutData();
    // ramLD.widthHint = -1;
    // ramLabel.setLayoutData(ramLD);
    // ramLabel.setText("Process memory [MB]");
    //
    // final String[] ramLevels = {"64", "100", "200", "480", "900", "1900", "2900", "3900", "5900",
    // "10000"};
    // final Combo ramCombo = new Combo(statusLine, SWT.DROP_DOWN | SWT.READ_ONLY);
    // ramCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    // ramCombo.setItems(ramLevels);
    // selectRam(ramLevels, ramCombo);
    //
    // StatusLineLayoutData ramComboLD = new StatusLineLayoutData();
    // ramComboLD.widthHint = -1;
    // ramCombo.setLayoutData(ramComboLD);
    // ramCombo.addSelectionListener(new SelectionAdapter(){
    // public void widgetSelected( SelectionEvent e ) {
    // int selectionIndex = ramCombo.getSelectionIndex();
    // eu.udig.jconsole.JConsoleEditorPlugin.getDefault().setRam(ramLevels[selectionIndex]);
    // }
    // });
    // ramCombo.addControlListener(new ControlAdapter(){
    // public void controlMoved( ControlEvent e ) {
    // super.controlMoved(e);
    // // something odd happens with the combo, workaround it here
    // if (ramLevels != null && ramCombo != null) {
    // selectRam(ramLevels, ramCombo);
    // }
    // }
    // });
    // }
    //
    // private void selectLog( final String[] logLevels, final Combo loggerCombo ) {
    // String loggerLevel = eu.udig.jconsole.JConsoleEditorPlugin.getDefault().getLoggerLevel();
    // for( int i = 0; i < logLevels.length; i++ ) {
    // if (logLevels[i].equals(loggerLevel)) {
    // loggerCombo.select(i);
    // break;
    // }
    // }
    // }
    //
    // private void selectRam( final String[] ramLevels, final Combo ramCombo ) {
    // String ramLevel = eu.udig.jconsole.JConsoleEditorPlugin.getDefault().getRam();
    // for( int i = 0; i < ramLevels.length; i++ ) {
    // if (ramLevels[i].equals(ramLevel)) {
    // ramCombo.select(i);
    // break;
    // }
    // }
    // }
    // }
}
