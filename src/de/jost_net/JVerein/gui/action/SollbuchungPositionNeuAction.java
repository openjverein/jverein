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
import de.jost_net.JVerein.gui.view.SollbuchungPositionDetailView;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SollbuchungPositionNeuAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Sollbuchung sollbuchung = null;
    SollbuchungPosition position = null;

    if (context != null && (context instanceof Sollbuchung))
    {
      sollbuchung = (Sollbuchung) context;
      try
      {
        if (sollbuchung.isNewObject())
        {
          throw new ApplicationException(
              "Vor dem Anlegen der Sollbuchungsposition muss die Sollbuchung gespeichert werden!");
        }
        position = (SollbuchungPosition) Einstellungen.getDBService()
            .createObject(SollbuchungPosition.class, null);
        position.setSollbuchung(sollbuchung.getID());
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException(
            "Fehler bei der Erzeugung einer neuen Sollbuchungsposition", e);
      }
    }
    else
    {
      throw new ApplicationException("Keine Sollbuchung ausgew�hlt");
    }

    // Wenn CurrentObject und View von aktueller und n�chster View gleich
    // sind, wird die aktuelle View nicht in die History aufgenommen. Dadurch
    // f�hrt der Zur�ckbutton auch bei "Speichern und neu" zur Liste
    // zur�ck.
    if (GUI.getCurrentView().getClass()
        .equals(SollbuchungPositionDetailView.class))
    {
      GUI.getCurrentView().setCurrentObject(position);
    }
    GUI.startView(SollbuchungPositionDetailView.class.getName(), position);
  }
}
