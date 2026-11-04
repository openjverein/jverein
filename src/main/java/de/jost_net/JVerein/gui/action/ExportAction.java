/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung für Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2014 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify 
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.control.AbstractListeControl;
import de.jost_net.JVerein.gui.dialogs.ExportDialog;
import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ExportAction implements Action
{

  private String help;

  public ExportAction(String help)
  {
    this.help = help;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      Object[] objArray = null;
      if (context instanceof AbstractListeControl control)
      {

        IJVereinPart tablePart = control.getTablePart();

        if (tablePart == null)
        {
          throw new ApplicationException(
              "Für diesen Bereich gibt es keine exportierbaren Daten.");
        }

        objArray = tablePart.getItems().toArray();
      }
      else if (context instanceof Object[] objs)
      {
        objArray = objs;
      }
      else if (context != null)
      {
        objArray = new Object[] { context };
      }

      if (objArray == null || objArray.length == 0)
      {
        throw new ApplicationException("Keine Daten zum Exportieren vorhanden");
      }
      ExportDialog d = new ExportDialog(objArray, context.getClass(), help,
          context);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Export", e);
      GUI.getStatusBar().setErrorText("Fehler beim Exportieren");
    }
  }
}
