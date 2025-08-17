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
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.SpbAdressaufbereitung;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungNeuAction implements Action
{
  private Spendenbescheinigung spb = null;

  private ArrayList<Buchung> sachspenden = new ArrayList<>();

  private int anzahlGeldspenden = 0;

  private int anzahlSachspenden = 0;

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      spb = (Spendenbescheinigung) Einstellungen.getDBService()
          .createObject(Spendenbescheinigung.class, null);
      spb.setBescheinigungsdatum(new Date());

      if (context != null && (context instanceof Mitglied))
      {
        Mitglied m = (Mitglied) context;
        SpbAdressaufbereitung.adressaufbereitung(m, spb);
        handleMitglied(m);
        if (anzahlGeldspenden > 0 && anzahlSachspenden == 0)
        {
          // Eine Geldspende also weiter machen und anzeigen
        }
        else if (anzahlGeldspenden == 0 && anzahlSachspenden == 1)
        {
          // Eine Sachspende also generieren und anzeigen
          spb = (Spendenbescheinigung) Einstellungen.getDBService()
              .createObject(Spendenbescheinigung.class, null);
          spb.setBescheinigungsdatum(new Date());
          generiereSpendenbescheinigung(sachspenden.get(0));
        }
        else
        {
          String text = "";
          if (anzahlGeldspenden > 0)
          {
            // Geldspendenbescheinigung speichern
            spb.store();
            text = "Eine Geldspendenbescheinigung, ";
          }
          for (Buchung b : sachspenden)
          {
            spb = (Spendenbescheinigung) Einstellungen.getDBService()
                .createObject(Spendenbescheinigung.class, null);
            spb.setBescheinigungsdatum(new Date());
            generiereSpendenbescheinigung(b);
            spb.store();
          }
          text = text + anzahlSachspenden
              + "Sachspendenbescheinigungen generiert.";
          GUI.getStatusBar().setSuccessText(text);
          return;
        }
      }
      else if (context instanceof MitgliedskontoNode
          && ((MitgliedskontoNode) context).getType() == MitgliedskontoNode.IST)
      {
        // Istbuchung in Mitgliedskonto ausgewählt
        MitgliedskontoNode mkn = (MitgliedskontoNode) context;
        // Buchung eintragen
        Buchung b = Einstellungen.getDBService().createObject(Buchung.class,
            mkn.getID());
        generiereSpendenbescheinigung(b);
      }
      else if (context instanceof Buchung)
      {
        Buchung b = (Buchung) context;
        generiereSpendenbescheinigung(b);
      }
      else
      {
        throw new ApplicationException(
            "Kein Mitglied oder keine Buchung ausgewählt!");
      }

      GUI.startView(SpendenbescheinigungDetailView.class.getName(), spb);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(
          "Fehler bei der Erstellung der Spendenbescheinigung");
    }
  }

  private void handleMitglied(Mitglied mg)
      throws RemoteException, ApplicationException
  {
    /* Ermitteln der Buchungen zu der neuen Spendenbescheinigung */
    Date minDatum = Calendar.getInstance().getTime();
    Date maxDatum = Calendar.getInstance().getTime();
    final DBService service = Einstellungen.getDBService();
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Buchung> list = new ArrayList<Buchung>();
        while (rs.next())
        {
          list.add(
              (Buchung) service.createObject(Buchung.class, rs.getString(1)));
        }
        return list;
      }
    };
    String sql = "SELECT buchung.id  FROM buchung "
        + "  JOIN buchungsart ON buchung.buchungsart = buchungsart.id "
        + "  JOIN " + Sollbuchung.TABLE_NAME + " ON " + Buchung.T_SOLLBUCHUNG
        + " = " + Sollbuchung.TABLE_NAME_ID
        + " WHERE buchungsart.spende = true " + "  AND " + Sollbuchung.T_ZAHLER
        + " = ? " + "  AND buchung.spendenbescheinigung IS NULL " + "  AND "
        + Buchung.T_SOLLBUCHUNG + " IS NOT NULL " + "ORDER BY buchung.datum";
    @SuppressWarnings("unchecked")
    ArrayList<Buchung> buchungen = (ArrayList<Buchung>) Einstellungen
        .getDBService().execute(sql, new Object[] { mg.getID() }, rs);

    if (buchungen.isEmpty())
    {
      throw new ApplicationException(
          "Es wurden keine relevanten Buchungen gefunden");
    }

    for (Buchung bu : buchungen)
    {
      // Bei Sachspende
      if (bu.getBezeichnungSachzuwendung() != null
          && !bu.getBezeichnungSachzuwendung().isEmpty())
      {
        sachspenden.add(bu);
        anzahlSachspenden++;
      }
      else
      {
        if (minDatum.after(bu.getDatum()))
        {
          minDatum = bu.getDatum();
        }
        if (maxDatum.before(bu.getDatum()))
        {
          maxDatum = bu.getDatum();
        }
        spb.addBuchung(bu);
        anzahlGeldspenden++;
      }
    }
    spb.setSpendedatum(minDatum);
    spb.setSpendenart(Spendenart.GELDSPENDE);
    spb.setBezeichnungSachzuwendung("");
    spb.setHerkunftSpende(HerkunftSpende.KEINEANGABEN);
    spb.setUnterlagenWertermittlung(false);

    // Nur Geldspenden
    if (anzahlGeldspenden == buchungen.size())
    {
      double minbetrag = (Double) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGMINBETRAG);
      if (spb.getBetrag() < minbetrag)
      {
        throw new ApplicationException(
            String.format(
                "Der Betrag der Spendenbescheinigung ist unter %s Euro. "
                    + "Siehe Einstellungen->Spendenbescheinigungen.",
                minbetrag));
      }
    }
  }

  private void generiereSpendenbescheinigung(Buchung b)
      throws ApplicationException, RemoteException
  {
    if (b.getBuchungsart() == null || !b.getBuchungsart().getSpende())
    {
      throw new ApplicationException(
          "Die Buchung hat keine Buchungsart die als Spende deklariert ist!");
    }
    if (b.getSpendenbescheinigung() != null)
    {
      throw new ApplicationException(
          "Die Buchung ist bereits auf einer Spendenbescheinigung eingetragen!");
    }
    if (b.getSollbuchung() != null)
    {
      // Zahler aus Sollbuchung lesen
      Mitglied zahler = b.getSollbuchung().getZahler();
      if (zahler != null)
      {
        SpbAdressaufbereitung.adressaufbereitung(zahler, spb);
      }
    }
    spb.setBuchung(b);
    spb.setSpendedatum(b.getDatum());
    // Bei Sachspende
    if (b.getBezeichnungSachzuwendung() != null
        && !b.getBezeichnungSachzuwendung().isEmpty())
    {
      spb.setSpendenart(Spendenart.SACHSPENDE);
      spb.setBezeichnungSachzuwendung(b.getBezeichnungSachzuwendung());
      spb.setHerkunftSpende(b.getHerkunftSpende());
      spb.setUnterlagenWertermittlung(b.getUnterlagenWertermittlung());
    }
    else
    {
      spb.setSpendenart(Spendenart.GELDSPENDE);
    }
  }

}
