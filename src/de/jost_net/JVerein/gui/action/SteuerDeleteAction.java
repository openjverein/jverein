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

import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Löschen einer Steuer.
 */
public class SteuerDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Steuer[] steuern = null;
    if (context == null)
    {
      throw new ApplicationException("Keine Steuer ausgewählt");
    }
    else if (context instanceof Steuer)
    {
      steuern = new Steuer[] { (Steuer) context };
    }
    else if (context instanceof Steuer[])
    {
      steuern = (Steuer[]) context;
    }
    else
    {
      return;
    }
    try
    {
      String mehrzahl = steuern.length > 1 ? "n" : "";
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Steuer" + mehrzahl + " löschen");
      d.setText(
          "Wollen Sie diese Steuer" + mehrzahl + " wirklich löschen?");
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Löschen der Steuer", e);
        return;
      }

      for (Steuer s : steuern)
      {
        if (s.isNewObject())
          continue;
        s.delete();
      }
      GUI.getStatusBar()
          .setSuccessText("Steuer" + mehrzahl + " gelöscht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Die Steuer wird bereits benutzt und kann nicht gelöscht werden";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
