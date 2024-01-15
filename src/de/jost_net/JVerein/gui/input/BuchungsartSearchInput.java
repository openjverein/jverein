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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.logging.Logger;

/**
 * Google like live search of BuchungsArt. Matching Buchungsarten, will be shown
 * up while typing. Implements startSearch from SearchInput. Searches case
 * insensitive in "Bezeichnung" and "Nummer" from the Buchungsart Table.
 * 
 * @author Thomas Laubrock
 * 
 */
public class BuchungsartSearchInput extends SearchInput
{
  
  private int unterdrueckunglaenge = 0;
  
  @Override
  @SuppressWarnings("rawtypes")
  public List startSearch(String text)
  {
    try
    {
      DBIterator result = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      if (text != null)
      {
        text = "%" + text.toUpperCase() + "%";
        result.addFilter("(UPPER(bezeichnung) like ? or nummer like ?)",
            new Object[] { text, text });
      }
      unterdrueckunglaenge = Einstellungen.getEinstellung().getUnterdrueckungLaenge();
      if (unterdrueckunglaenge > 0 )
      {
        List<Buchungsart> buas = new ArrayList<Buchungsart>();
        Buchungsart bua;
        BuchungQuery query;
        Calendar cal = Calendar.getInstance();
        Date db = cal.getTime();
        cal.add(Calendar.MONTH, -unterdrueckunglaenge);
        Date dv = cal.getTime();
        while (result.hasNext())
        {
          bua = (Buchungsart) result.next();
          query = new BuchungQuery(dv, db, null, bua, null, "", "", null);
          if (query.get().isEmpty())
          {
            continue;
          }
          buas.add(bua);
        }
        return buas;
      } else 
      {
        return PseudoIterator.asList(result);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load project list", e);
      return null;
    }
  }

}
