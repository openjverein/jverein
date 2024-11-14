/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/

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
