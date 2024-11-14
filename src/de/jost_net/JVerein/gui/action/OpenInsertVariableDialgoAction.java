package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.dialogs.ShowVariablesDialog;
import de.jost_net.JVerein.gui.menu.ShowVariablesMenu;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

public class OpenInsertVariableDialgoAction implements Action {
    @Override
    public void handleAction(Object context) {
        try
        {
            ShowVariablesDialog d = new ShowVariablesDialog(
                    ((LesefeldAuswerter) context).getMap(), false);
            ShowVariablesMenu menu = new ShowVariablesMenu();
            menu.setPrependCopyText("");
            menu.setAppendCopyText("");
            d.setContextMenu(menu);
            d.setDoubleClickAction(menu.getCopyToClipboardAction());
            d.open();
        }
        catch (OperationCanceledException ignored) {}
        catch (Exception e)
        {
            Logger.error("Fehler beim Anzeigen der Variablen.", e);
            GUI.getStatusBar().setErrorText("Fehler beim Anzeigen der Variablen.");
        }
    }
}
