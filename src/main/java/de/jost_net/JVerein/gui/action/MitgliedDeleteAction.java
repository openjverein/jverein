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
import java.math.BigDecimal;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;

/**
 * Loeschen eines Mitgliedes.
 */
public class MitgliedDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitglied[] mitglieder = null;
    if (context == null)
    {
      throw new ApplicationException("Kein Mitglied ausgewählt");
    }
    else if(context instanceof Mitglied)
    {
      mitglieder = new Mitglied[] {(Mitglied)context};
    }
    else if(context instanceof Mitglied[])
    {
      mitglieder = (Mitglied[])context;
    }
    else
    {
      return;
    }
    try
    {
      String mehrzahl = mitglieder.length > 1 ? "er" : "";
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Mitglied" + mehrzahl + " löschen");
      d.setPanelText("Mitglied" + mehrzahl + " löschen?");
      d.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
      String text = "Wollen Sie diese" + (mitglieder.length > 1 ? "":"s") + " Mitglied" + mehrzahl + " wirklich löschen?"
          + "\nDies löscht auch alle Mitglied bezogenen Daten wie"
          + "\nz.B. Sollbuchungen, Mails etc."
          + "\nDiese Daten können nicht wieder hergestellt werden!";
      d.setText(text);

      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Löschen des Mitgliedes", e);
        return;
      }
      
      final DBService service = Einstellungen.getDBService();
      DBTransaction.starten();
      for(Mitglied m:mitglieder)
      {
        if (m.isNewObject())
        {
          continue;
        }
        
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
        ArrayList<BigDecimal> ergebnis = (ArrayList<BigDecimal>) service.execute(sql,
            new Object[] { }, rs);
        
        // Alle Mails an das Mitglied löschen wenn nur ein Empfänger vorhanden
        DBIterator<MailEmpfaenger> it = Einstellungen.getDBService()
            .createList(MailEmpfaenger.class);
        it.addFilter("mitglied = ?", m.getID());
        while (it.hasNext())
        {
          Mail ma = ((MailEmpfaenger) it.next()).getMail();
          if (!ergebnis.contains(new BigDecimal(ma.getID())))
          {
            // Die Mail hat keinen weiteren Empfänger also löschen
            ma.delete();
          }
        }
        
        m.delete();
      }
      DBTransaction.commit();
      GUI.getStatusBar().setSuccessText("Mitglied" + mehrzahl + " gelöscht.");
    }
    catch (RemoteException e)
    {
      DBTransaction.rollback();
      String fehler = "Fehler beim Löschen des Mitgliedes";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
