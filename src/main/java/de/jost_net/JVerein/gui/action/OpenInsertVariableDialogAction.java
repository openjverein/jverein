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

import java.util.Map;

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.dialogs.ShowVariablesDialog;
import de.jost_net.JVerein.gui.menu.ShowVariablesMenu;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class OpenInsertVariableDialogAction implements Action
{
  @SuppressWarnings("unchecked")
  @Override
  public void handleAction(Object context)
  {
    String prependCopyText = "";
    try
    {
      ShowVariablesMenu menu = new ShowVariablesMenu();
      Map<String, Object> map;
      if (context instanceof LesefeldAuswerter)
      {
        map = ((LesefeldAuswerter) context).getMap();
      }
      else if (context instanceof Map)
      {
        map = (Map<String, Object>) context;
        prependCopyText = "$";
      }
      else if (context instanceof MailEmpfaenger)
      {
        map = new MitgliedMap().getMap(((MailEmpfaenger) context).getMitglied(),
            new AllgemeineMap().getMap(null));
        prependCopyText = "$";
      }
      else
      {
        throw new ApplicationException("Falscher Kontext");
      }
      ShowVariablesDialog d = new ShowVariablesDialog(map, false,
          prependCopyText, "");
      menu.setAppendCopyText("");
      menu.setPrependCopyText(prependCopyText);
      d.setContextMenu(menu);
      d.setDoubleClickAction(menu.getCopyToClipboardAction());
      d.open();
    }
    catch (OperationCanceledException ignored)
    {
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Anzeigen der Variablen.", e);
      GUI.getStatusBar().setErrorText("Fehler beim Anzeigen der Variablen.");
    }
  }
}
