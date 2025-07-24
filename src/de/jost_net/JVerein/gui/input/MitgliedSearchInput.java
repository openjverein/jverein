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

import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.logging.Logger;

public class MitgliedSearchInput extends SearchInput
{
  public MitgliedSearchInput()
  {
    super();
  }

  @Override
  @SuppressWarnings("rawtypes")
  public List startSearch(String text)
  {
    try
    {
      DBIterator result = Einstellungen.getDBService()
          .createList(Mitglied.class);

      if (text != null)
      {
        text = "%" + text.toUpperCase() + "%";
        result.addFilter("(UPPER(name) like ? or UPPER(vorname) like ?)",
            new Object[] { text, text });
      }
      result.setOrder("order by name, vorname");

      return result != null ? PseudoIterator.asList(result) : null;

    }
    catch (Exception e)
    {
      Logger.error("Unable to load mitglied list", e);
      return null;
    }
  }

}
