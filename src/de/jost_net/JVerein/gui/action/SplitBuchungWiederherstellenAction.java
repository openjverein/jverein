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

import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchung.
 */
public class SplitBuchungWiederherstellenAction implements Action
{
  private BuchungsControl control;

  public SplitBuchungWiederherstellenAction(BuchungsControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Buchung))
    {
      throw new ApplicationException("Keine Buchung ausgew�hlt");
    }
    try
    {
      Buchung bu = (Buchung) context;
      if (bu.getDependencyId() == -1)
      {
        bu.setDelete(false);
      }
      else
      {
        for (Buchung buchung_tmp : SplitbuchungsContainer.get())
        {
          if (buchung_tmp.getDependencyId() == bu.getDependencyId())
          {
            buchung_tmp.setDelete(false);
          }
        }
      }
      control.refreshSplitbuchungen();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Wiederherstellen der Buchung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
