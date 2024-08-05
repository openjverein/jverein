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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragAbrechnungslauf;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * L�schen eines Abrechnungslaufes
 */
public class AbrechnungslaufDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Abrechnungslauf))
    {
      throw new ApplicationException("Keinen Abrechnungslauf ausgew�hlt");
    }
    try
    {
      Abrechnungslauf abrl = (Abrechnungslauf) context;
      if (abrl.isNewObject())
      {
        return;
      }

      // Pr�fe, ob der Abrechnungslauf als abgeschlossen gekennzeichnet ist.
      // In diesem Fall darf er nicht gel�scht werden!
      if (abrl.getAbgeschlossen())
      {
        throw new ApplicationException(
            "Abgeschlossene Abrechnungsl�ufe k�nnen nicht gel�scht werden!");
      }
      
      // Check ob einer der Buchungen des Abrechnungslaufs
      // eine Spendenbescheinigung zugeordnet ist
      final DBService service = Einstellungen.getDBService();
      String sql = "SELECT DISTINCT buchung.id from buchung "
          + "WHERE (abrechnungslauf = ? and spendenbescheinigung IS NOT NULL) ";
      boolean spendenbescheinigung = (boolean) service.execute(sql,
          new Object[] { abrl.getID() }, new ResultSetExtractor()
      {
        @Override
        public Object extract(ResultSet rs)
            throws RemoteException, SQLException
        {
          if (rs.next())
          {
            return true;
          }
          return false;
        }
      });

      String text = "";
      if (!spendenbescheinigung)
      {
        text = "Wollen Sie diesen Abrechnungslauf wirklich l�schen?";
      }
      else
      {
        text = "Der Abrechnungslauf enth�lt Buchungen denen eine "
            + "Spendenbescheinigung zugeordnet ist.\n"
            + "Sie k�nnen nur zusammen gel�scht werden.\n"
            + "Abrechnungslauf und Spendenbescheinigungen l�schen?";
      }

      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle(String.format("Abrechnungslauf %s l�schen", abrl.getID()));
      d.setText(text);

      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim L�schen eines Abrechnungslaufes", e);
        return;
      }
      
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("abrechnungslauf = ?", new Object[] { abrl.getID() });
      while (it.hasNext())
      {
        Buchung bu = it.next();
        Buchung b = (Buchung) Einstellungen.getDBService()
            .createObject(Buchung.class, bu.getID());
        Jahresabschluss ja = b.getJahresabschluss();
        if (ja != null)
        {
          throw new ApplicationException(
              String.format("Buchung wurde bereits am %s von %s abgeschlossen.",
                  new Object[] {
                      new JVDateFormatTTMMJJJJ().format(ja.getDatum()),
                      ja.getName() }));
        }
        b.setMitgliedskontoID(null);
        b.store();
        if (b.getSpendenbescheinigung() != null)
          b.getSpendenbescheinigung().delete();
        b.delete();
      }
      it = Einstellungen.getDBService().createList(Mitgliedskonto.class);
      it.addFilter("abrechnungslauf = ?", new Object[] { abrl.getID() });
      while (it.hasNext())
      {
        Mitgliedskonto mkt = (Mitgliedskonto) it.next();
        DBIterator<Buchung> it2 = Einstellungen.getDBService()
            .createList(Buchung.class);
        it2.addFilter("mitgliedskonto = ?", new Object[] { mkt.getID() });
        while (it2.hasNext())
        {
          Buchung bu = it2.next();
          Buchung b = (Buchung) Einstellungen.getDBService()
              .createObject(Buchung.class, bu.getID());
          b.setMitgliedskontoID(null);
          b.store();
        }
        Mitgliedskonto mk = (Mitgliedskonto) Einstellungen.getDBService()
            .createObject(Mitgliedskonto.class, mkt.getID());
        mk.delete();
      }
      it = Einstellungen.getDBService()
          .createList(ZusatzbetragAbrechnungslauf.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      while (it.hasNext())
      {
        ZusatzbetragAbrechnungslauf za = (ZusatzbetragAbrechnungslauf) it
            .next();
        Zusatzbetrag z = (Zusatzbetrag) Einstellungen.getDBService()
            .createObject(Zusatzbetrag.class, za.getZusatzbetrag().getID());
        z.vorherigeFaelligkeit();
        z.setAusfuehrung(za.getLetzteAusfuehrung());
        z.store();
      }
      it = Einstellungen.getDBService().createList(Lastschrift.class);
      it.addFilter("abrechnungslauf = ?", abrl.getID());
      while (it.hasNext())
      {
        Lastschrift la = (Lastschrift) it.next();
        Kursteilnehmer kt = la.getKursteilnehmer();
        if (kt != null)
        {
          kt.resetAbbudatum();
          kt.store();
        }
      }
      abrl.delete();
      GUI.getStatusBar().setSuccessText("Abrechnungslauf gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen eines Abrechnungslaufes";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
