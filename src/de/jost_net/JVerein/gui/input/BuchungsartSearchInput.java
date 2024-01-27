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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
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

      unterdrueckunglaenge = Einstellungen.getEinstellung().getUnterdrueckungLaenge();
      if (unterdrueckunglaenge > 0 )
      {
        final DBService service = Einstellungen.getDBService();
        Calendar cal = Calendar.getInstance();
        Date db = cal.getTime();
        cal.add(Calendar.MONTH, - unterdrueckunglaenge);
        Date dv = cal.getTime();
        String sql = "SELECT buchungsart.* from buchungsart, buchung ";
        sql += "WHERE buchung.buchungsart = buchungsart.id ";
        sql += "AND buchung.datum >= ? AND buchung.datum <= ? ";
        if (text != null)
        {
          text = "%" + text.toUpperCase() + "%";
          sql += "AND (UPPER(buchungsart.bezeichnung) like ? or buchungsart.nummer like ?) ";
        }
        sql += "ORDER BY nummer";
        Logger.debug(sql);
        ResultSetExtractor rs = new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs) throws RemoteException, SQLException
          {
            ArrayList<Buchungsart> list = new ArrayList<Buchungsart>();
            while (rs.next())
            {
              list.add(
                (Buchungsart) service.createObject(Buchungsart.class, rs.getString(1)));
            }
            return list;
          }
        };

        ArrayList<Buchungsart> ergebnis;
        if (text != null)
        {
          @SuppressWarnings("unchecked")
          ArrayList<Buchungsart> result = (ArrayList<Buchungsart>) service.execute(sql,
              new Object[] { dv, db, text, text }, rs);
          ergebnis = result;
        }
        else
        {
          @SuppressWarnings("unchecked")
          ArrayList<Buchungsart> result = (ArrayList<Buchungsart>) service.execute(sql,
              new Object[] { dv, db }, rs);
          ergebnis = result;
        }
        int size = ergebnis.size();
        Buchungsart bua;
        for (int i = 0; i < size; i++)
        {
          bua = ergebnis.get(i);
          for (int j = i + 1; j < size; j++)
          {
            if (bua.getNummer() == ergebnis.get(j).getNummer())
            {
              ergebnis.remove(j);
              j--;
              size--;
            }
          }
        }
        return ergebnis;
      }
      else 
      {
        DBIterator result = Einstellungen.getDBService()
            .createList(Buchungsart.class);
        if (text != null)
        {
          text = "%" + text.toUpperCase() + "%";
          result.addFilter("(UPPER(bezeichnung) like ? or nummer like ?)",
              new Object[] { text, text });
        }
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
