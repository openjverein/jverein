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
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
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

public class SpendenbescheinigungAction implements Action
{
  private int spendenart = Spendenart.SACHSPENDE;
  
  private Spendenbescheinigung spb = null;
  
  public SpendenbescheinigungAction(int spendenart)
  {
    this.spendenart = spendenart;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (context instanceof Spendenbescheinigung)
      {
        spb = (Spendenbescheinigung) context;
      }
      else
      {
        spb = (Spendenbescheinigung) Einstellungen.getDBService()
            .createObject(Spendenbescheinigung.class, null);
        spb.setSpendenart(spendenart);
        spb.setAutocreate(Boolean.FALSE);
        spb.setErsatzAufwendungen(false);
        spb.setBescheinigungsdatum(new Date());

        if (context != null && (context instanceof Mitglied))
        {
          Mitglied m = (Mitglied) context;
          SpbAdressaufbereitung.adressaufbereitung(m, spb);
          if (spendenart == Spendenart.GELDSPENDE)
          {
            handleMitglied(m);
          }
        }
        else if (context instanceof MitgliedskontoNode)
        {
          MitgliedskontoNode mkn = (MitgliedskontoNode) context;

          // Istbuchung in Mitgliedskonto ausgewählt
          if (mkn.getType() == MitgliedskontoNode.IST)
          {
            // Buchung eintragen
            Object o = Einstellungen.getDBService().createObject(Buchung.class,
                mkn.getID());
            if (o != null)
            {
              Buchung b = (Buchung) o;
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
              spb.setAutocreate(Boolean.TRUE);
            }
          }
          // Mitglied in Mitgliedskonto ausgewählt
          else if (mkn.getType() == MitgliedskontoNode.MITGLIED)
          {
            if (mkn.getMitglied() != null)
            {
              // Mitglied aus Mitgliedskonto lesen
              Mitglied m = mkn.getMitglied();
              SpbAdressaufbereitung.adressaufbereitung(m, spb);
            }
            if (spendenart == Spendenart.GELDSPENDE)
            {
              handleMitglied(spb.getMitglied());
            }
          }
        }
        else if (context instanceof Buchung)
        {
          Buchung b = (Buchung) context;
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
          spb.setAutocreate(Boolean.TRUE);
        }
        else
        {
          spb.setSpendenart(Spendenart.SACHSPENDE);
          spb.setAutocreate(Boolean.FALSE);
          Object o = GUI.getCurrentView().getCurrentObject();
          if (o != null && o instanceof Spendenbescheinigung)
          {
            Spendenbescheinigung von = (Spendenbescheinigung) o;
            spb.setZeile1(von.getZeile1());
            spb.setZeile2(von.getZeile2());
            spb.setZeile3(von.getZeile3());
            spb.setZeile4(von.getZeile4());
            spb.setZeile5(von.getZeile5());
            spb.setZeile6(von.getZeile6());
            spb.setZeile7(von.getZeile7());
          }
        }
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

  private void handleMitglied(Mitglied mg) throws RemoteException, ApplicationException
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
        + " WHERE buchungsart.spende = true "
        + "  AND " + Sollbuchung.T_ZAHLER + " = ? "
        + "  AND buchung.spendenbescheinigung IS NULL "
        + "  AND " + Buchung.T_SOLLBUCHUNG + " IS NOT NULL "
        + "ORDER BY buchung.datum";
    @SuppressWarnings("unchecked")
    ArrayList<Buchung> buchungen = (ArrayList<Buchung>) Einstellungen.getDBService()
        .execute(sql, new Object[] { mg.getID() }, rs);
    
    if (buchungen.isEmpty())
    {
      throw new ApplicationException(
          "Es wurden keine relevanten Buchungen gefunden");
    }
    
    for (Buchung bu: buchungen)
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
    }
    spb.setSpendedatum(minDatum);
    spb.setAutocreate(Boolean.TRUE);
    
    double minbetrag = Einstellungen.getEinstellung().getSpendenbescheinigungminbetrag();
    if (spb.getBetrag() < minbetrag)
    {
      throw new ApplicationException(
          String.format("Der Betrag der Spendenbescheinigung ist unter %s Euro. "
              + "Siehe Einstellungen->Spendenbescheinigungen.", minbetrag));
    }
  }

}
