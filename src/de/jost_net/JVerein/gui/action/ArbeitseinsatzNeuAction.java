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
import de.jost_net.JVerein.gui.view.ArbeitseinsatzDetailView;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class ArbeitseinsatzNeuAction implements Action
{
  private Mitglied m;

  public ArbeitseinsatzNeuAction(Mitglied m)
  {
    super();
    this.m = m;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Arbeitseinsatz aeins = null;

    try
    {
      aeins = (Arbeitseinsatz) Einstellungen.getDBService()
          .createObject(Arbeitseinsatz.class, null);
      if (m != null)
      {
        if (m.getID() == null)
        {
          throw new ApplicationException(
              "Neues Mitglied bitte erst speichern. Dann können Arbeitseinsätze aufgenommen werden.");
        }
        aeins.setMitglied(Integer.valueOf(m.getID()).intValue());
      }
      else
      {
        throw new ApplicationException("Kein Mitglied ausgewählt");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler bei der Erzeugung eines neuen Arbeitseinsatzes", e);
    }

    GUI.startView(ArbeitseinsatzDetailView.class.getName(), aeins);
  }
}
