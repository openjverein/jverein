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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Lehrgangsart
 */
public class LehrgangsartDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null || !(context instanceof Lehrgangsart))
    {
      throw new ApplicationException("Keine Lehrgangsart ausgew�hlt");
    }
    try
    {
      Lehrgangsart l = (Lehrgangsart) context;
      if (l.isNewObject())
      {
        return;
      }
      DBIterator<Lehrgang> it = Einstellungen.getDBService()
          .createList(Lehrgang.class);
      it.addFilter("lehrgangsart = ?", new Object[] { l.getID() });
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(String.format(
            "Lehrgangsart '%s' kann nicht gel�scht werden. Es existieren Lehrg�nge dieser Art.",
            l.getBezeichnung()));
      }
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Lehrgangsart l�schen");
      d.setText("Wollen Sie diese Lehrgangsart wirklich l�schen?");

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
        Logger.error("Fehler beim L�schen einer Lehrgangsart", e);
        return;
      }
      l.delete();
      GUI.getStatusBar().setSuccessText("Lehrgangsart gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen einer Lehrgangsart";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
