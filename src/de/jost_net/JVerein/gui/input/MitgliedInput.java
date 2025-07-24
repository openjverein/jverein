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
package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.SelectInput;

public class MitgliedInput
{

  public AbstractInput getMitgliedInput(AbstractInput mitgliedInput,
      Mitglied mitglied, int auswahl) throws RemoteException
  {
    switch (auswahl)
    {
      case AbstractInputAuswahl.ComboBox:
        // Hole alle Mitglieder aus Datenbank um sie später anzuzeigen.
        DBIterator<Mitglied> it = Einstellungen.getDBService()
            .createList(Mitglied.class);
        it.setOrder("order by name, vorname");
        ArrayList<Mitglied> mitgliederList = new ArrayList<>();
        while (it.hasNext())
        {
          mitgliederList.add(it.next());
        }
        // Das erste Mitglied wird ausgewählt wenn nichts übergeben
        Mitglied selectedMitglied = mitglied;
        if (selectedMitglied == null && !mitgliederList.isEmpty())
        {
          selectedMitglied = mitgliederList.get(0);
        }
        mitgliedInput = new SelectInput(mitgliederList.toArray(),
            selectedMitglied);
        break;
      case AbstractInputAuswahl.SearchInput:
      default:
        mitgliedInput = new MitgliedSearchInput();
        ((MitgliedSearchInput) mitgliedInput)
            .setSearchString("Zum Suchen tippen");
        mitgliedInput.setValue(mitglied);
    }
    return mitgliedInput;
  }

}
