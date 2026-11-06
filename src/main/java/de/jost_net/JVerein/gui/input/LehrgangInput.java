/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;

public class LehrgangInput
{

  public Input getLehrgangInput(Input lehrgangInput, Lehrgang lehrgang,
      int auswahl) throws RemoteException
  {
    switch (auswahl)
    {
      case AbstractInputAuswahl.ComboBox:
        // Hole alle Lehrgänge aus Datenbank um sie später anzuzeigen.
        DBIterator<Lehrgang> it = Einstellungen.getDBService()
            .createList(Lehrgang.class);
        it.setOrder("order by bezeichnung");
        ArrayList<Lehrgang> lehrgangList = new ArrayList<>();
        while (it.hasNext())
        {
          lehrgangList.add(it.next());
        }
        // Der erste Lehrgang wird ausgewählt wenn nichts übergeben
        Lehrgang selectedLehrgang = lehrgang;
        if (selectedLehrgang == null && !lehrgangList.isEmpty())
        {
          selectedLehrgang = lehrgangList.get(0);
        }
        lehrgangInput = new SelectInput(lehrgangList.toArray(),
            selectedLehrgang);
        break;
      case AbstractInputAuswahl.SearchInput:
      default:
        lehrgangInput = new LehrgangSearchInput();
        ((LehrgangSearchInput) lehrgangInput)
            .setSearchString("Zum Suchen tippen");
        lehrgangInput.setValue(lehrgang);
    }
    return lehrgangInput;
  }

}
