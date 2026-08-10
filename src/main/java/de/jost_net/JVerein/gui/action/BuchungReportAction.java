/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import de.jost_net.JVerein.gui.dialogs.BuchungReportExportDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungReportAction implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Buchung[] buchungen = null;
    if (context instanceof Buchung)
    {
      buchungen = new Buchung[1];
      buchungen[0] = (Buchung) context;
    }
    else if (context instanceof Buchung[])
    {
      buchungen = (Buchung[]) context;
    }
    if (buchungen == null || buchungen.length == 0)
    {
      throw new ApplicationException("Keine Buchung ausgewählt!");
    }

    try
    {
      BuchungReportExportDialog d = new BuchungReportExportDialog(
          BuchungReportExportDialog.POSITION_CENTER, buchungen);
      d.open();
    }
    catch (OperationCanceledException | ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar()
          .setErrorText("Fehler beim Generieren des Buchungreports");
    }
  }
}
