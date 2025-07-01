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
package de.jost_net.JVerein.Queries;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;

public class BuchungsKorrekturQuery
{

  private List<Buchung> ergebnis;

  public BuchungsKorrekturQuery()
  {
  }

  @SuppressWarnings("unchecked")
  public List<Buchung> get() throws RemoteException
  {
    final DBService service = Einstellungen.getDBService();

    DBIterator<Jahresabschluss> jahresabschlussIterator = service
        .createList(Jahresabschluss.class);
    jahresabschlussIterator.setOrder("ORDER BY bis DESC");
    Date bis = null;
    if (jahresabschlussIterator.hasNext())
      bis = ((Jahresabschluss) jahresabschlussIterator.next()).getBis();

    DBIterator<Buchung> buchungIterator = service.createList(Buchung.class);
    if (bis != null)
      buchungIterator.addFilter("datum > ?", new java.sql.Date(bis.getTime()));
    Object[] keys = { "%EREF%", "%KREF%", "%MREF%", "%CRED%", "%DBET%",
        "%SVWZ%", "%ABWA%", "%IBAN+%", "%IBAN:%", "%BIC%" };
    buchungIterator
        .addFilter("(upper(zweck) like ? or " + "upper(zweck) like ? or "
            + "upper(zweck) like ? or " + "upper(zweck) like ? or "
            + "upper(zweck) like ? or " + "upper(zweck) like ? or "
            + "upper(zweck) like ? or " + "upper(zweck) like ? or "
            + "upper(zweck) like ? or " + "upper(zweck) like ?)", keys);

    buchungIterator.setOrder("ORDER BY datum");

    this.ergebnis = buchungIterator != null
        ? PseudoIterator.asList(buchungIterator)
        : null;
    return ergebnis;
  }

  public int getSize()
  {
    return ergebnis.size();
  }

}
