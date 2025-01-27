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
import de.jost_net.JVerein.gui.view.LesefeldUebersichtView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;

public class LesefelddefinitionenAction implements Action
{
  private Mitglied selectedMitglied;

  public LesefelddefinitionenAction(Mitglied mitglied)
  {
    selectedMitglied = mitglied;
  }

  @Override
  public void handleAction(Object context)
  {
    if (selectedMitglied == null )
    {
      try
      {
        DBIterator<Mitglied> it = Einstellungen.getDBService().createList(Mitglied.class);
        it.setOrder("order by name, vorname");
        if (it.hasNext())
        {
          selectedMitglied = it.next();
        }
      }
      catch (RemoteException e)
      {
        // Dann lassen wir die null
      }
    }

    GUI.startView(LesefeldUebersichtView.class.getName(), selectedMitglied);
  }
}
