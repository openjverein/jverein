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
import de.jost_net.JVerein.gui.view.SollbuchungPositionView;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
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
    Mitgliedskonto sollbuchung = null;
    SollbuchungPosition position = null;

    if (context != null && (context instanceof Mitgliedskonto))
    {
      sollbuchung = (Mitgliedskonto) context;
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
      throw new ApplicationException("Keine Sollbuchung ausgewählt");
    }

    GUI.startView(SollbuchungPositionView.class.getName(), position);
  }
}
