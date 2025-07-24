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

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchungsart.
 */
public class BuchungsartDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Buchungsart[] buchungsarten = null;
    if (context == null)
    {
      throw new ApplicationException("Keine Buchungsart ausgewählt");
    }
    else if (context instanceof Buchungsart)
    {
      buchungsarten = new Buchungsart[] { (Buchungsart) context };
    }
    else if (context instanceof Buchungsart[])
    {
      buchungsarten = (Buchungsart[]) context;
    }
    else
    {
      return;
    }
    try
    {
      String mehrzahl = buchungsarten.length > 1 ? "en" : "";
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Buchungsart" + mehrzahl + " löschen");
      d.setText(
          "Wollen Sie diese Buchungsart" + mehrzahl + " wirklich löschen?");
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Löschen der Buchungsart", e);
        return;
      }

      for (Buchungsart b : buchungsarten)
      {
        if (b.isNewObject())
          continue;
        b.delete();
      }
      GUI.getStatusBar()
          .setSuccessText("Buchungsart" + mehrzahl + " gelöscht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Die Buchungsart wird bereits benutzt und kann nicht gelöscht werden";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
