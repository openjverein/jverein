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

import de.jost_net.JVerein.rmi.Lastschrift;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Lastschrift.
 */
public class LastschriftDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Lastschrift) && !(context instanceof Lastschrift[])))
    {
      throw new ApplicationException("Keine Lastschrift ausgewählt");
    }
    try
    {
      Lastschrift[] la = null;
      if (context instanceof Lastschrift)
      {
        la = new Lastschrift[1];
        la[0] = (Lastschrift) context;
      }
      else if (context instanceof Lastschrift[])
      {
        la = (Lastschrift[]) context;
      }
      if (la == null)
      {
        return;
      }
      if (la.length == 0)
      {
        return;
      }
      if (la[0].isNewObject())
      {
        return;
      }
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Lastschrift" + (la.length > 1 ? "en" : "") + " löschen");
      d.setText("Wollen Sie diese Lastschrift" + (la.length > 1 ? "en" : "")
          + " wirklich löschen?");
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Löschen der Lastschrift", e);
        return;
      }
      int count = 0;
      for (Lastschrift l : la)
      {
        l.delete();
        count++;
      }
      if (count > 0)
      {
        GUI.getStatusBar().setSuccessText(String.format(
            "%d Lastschrift" + (count != 1 ? "en" : "") + " gelöscht.", count));
      }
      else
      {
        GUI.getStatusBar().setErrorText("Keine Lastschrift gelöscht");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen der Lastschrift.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
