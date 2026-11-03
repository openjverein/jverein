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

import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.logging.Logger;

public class LehrgangSearchInput extends SearchInput
{

  @Override
  @SuppressWarnings("rawtypes")
  public List startSearch(String text)
  {
    try
    {
      DBIterator result = Einstellungen.getDBService()
          .createList(Lehrgang.class);

      if (text != null)
      {
        text = "%" + text.toUpperCase() + "%";
        result.addFilter("(UPPER(bezeichnung) like ?)", new Object[] { text });
      }
      result.setOrder("order by bezeichnung");

      return result != null ? PseudoIterator.asList(result) : null;

    }
    catch (Exception e)
    {
      Logger.error("Unable to load lehrgang list", e);
      return null;
    }
  }

}
