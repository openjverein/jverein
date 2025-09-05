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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

import java.math.BigDecimal;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.util.ApplicationException;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;

/**
 * Loeschen eines Mitgliedes.
 */
public class MitgliedDeleteAction extends DeleteAction
{
  @Override
  protected String getText(JVereinDBObject object[])
      throws RemoteException, ApplicationException
  {
    if (object == null || object.length == 0
        || !(object[0] instanceof Mitglied))
    {
      throw new ApplicationException("Kein Mitglied ausgewählt");
    }

    String nicht = " ";
    if (!((Mitglied) object[0]).getMitgliedstyp().getID()
        .equals(Mitgliedstyp.MITGLIED))
    {
      nicht = " Nicht-";
    }
    String mehrzahl = object.length > 1 ? "er" : "";
    return "Wollen Sie diese" + (object.length > 1 ? "" : "s") + nicht
        + "Mitglied" + mehrzahl + " wirklich löschen?"
        + "\nDies löscht auch alle" + nicht + "Mitglieder bezogenen Daten wie"
        + "\nz.B. Sollbuchungen, Mails etc."
        + "\nDiese Daten können nicht wieder hergestellt werden!";
  }

  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Mitglied))
    {
      return;
    }

    final DBService service = Einstellungen.getDBService();
    // Suche Mails mit mehr als einem Empfänger
    String sql = "SELECT mail , count(id) anzahl from mailempfaenger ";
    sql += "group by mailempfaenger.mail ";
    sql += "HAVING anzahl > 1 ";
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<BigDecimal> list = new ArrayList<BigDecimal>();
        while (rs.next())
        {
          list.add(rs.getBigDecimal(1));
        }
        return list;
      }
    };
    @SuppressWarnings("unchecked")
    ArrayList<BigDecimal> ergebnis = (ArrayList<BigDecimal>) service
        .execute(sql, new Object[] {}, rs);

    // Alle Mails an das Mitglied löschen wenn nur ein Empfänger vorhanden
    DBIterator<MailEmpfaenger> it = Einstellungen.getDBService()
        .createList(MailEmpfaenger.class);
    it.addFilter("mitglied = ?", object.getID());
    while (it.hasNext())
    {
      Mail ma = ((MailEmpfaenger) it.next()).getMail();
      if (!ergebnis.contains(new BigDecimal(ma.getID())))
      {
        // Die Mail hat keinen weiteren Empfänger also löschen
        ma.delete();
      }
    }

    object.delete();
  }

  @Override
  protected Image getImage()
  {
    return SWTUtil.getImage("dialog-warning-large.png");
  }
}
