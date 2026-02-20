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
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class SollbuchungEditAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Sollbuchung sollb = null;
    MitgliedskontoNode mkn = null;

    if (context instanceof Sollbuchung)
    {
      sollb = (Sollbuchung) context;
    }
    else if (context instanceof MitgliedskontoNode)
    {
      mkn = (MitgliedskontoNode) context;
      try
      {
        sollb = (Sollbuchung) Einstellungen.getDBService()
            .createObject(Sollbuchung.class, mkn.getID());
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(
            "Fehler beim Editieren einer Sollbuchung");
      }
    }
    else if ((context instanceof Buchung))
    {
      Buchung buchung = (Buchung) context;
      try
      {
        if (buchung.getSollbuchung() != null)
        {
          sollb = ((Buchung) context).getSollbuchung();
        }
        else
        {
          throw new ApplicationException(
              "Die Buchung ist keiner Sollbuchung zugeordnet");
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler beim Öffnen einer Sollbuchung");
      }
    }
    else
    {
      throw new ApplicationException("Keine Sollbuchung ausgewählt");
    }
    GUI.startView(new SollbuchungDetailView(), sollb);
  }
}
