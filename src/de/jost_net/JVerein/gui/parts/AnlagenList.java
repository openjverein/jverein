/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.AnlagenlisteZeile;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class AnlagenList extends TablePart implements Part
{

	private TablePart saldoList;

	private Date datumvon = null;

	private Date datumbis = null;

  public AnlagenList(Action action, Date datumvon, Date datumbis)
  {
    super(action);
    this.datumvon = datumvon;
    this.datumbis = datumbis;
  }

  public Part getSaldoList() throws ApplicationException
  {
    ArrayList<AnlagenlisteZeile> zeile = null;
    try
    {
      zeile = getInfo();

      if (saldoList == null)
      {
        saldoList = new TablePart(zeile, null)
        {
          @Override
          protected void orderBy(int index)
          {
            return;
          }
        };
        saldoList.addColumn("Anlagenart", "anlagenart");
        saldoList.addColumn("Bezeichnung", "bezeichnung");
        saldoList.addColumn("Nutzungsdauer", "nutzungsdauer",
            null, false, Column.ALIGN_RIGHT);
        saldoList.addColumn("Afa Art", "afaartbezeichnung");
        saldoList.addColumn("Anschaffung", "anschaffung",
            new DateFormatter(new JVDateFormatTTMMJJJJ()), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Anschaffungskosten", "kosten",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Buchwert Start", "startwert",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Abschreibung", "abschreibung",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Buchwert Ende", "endwert",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.setRememberColWidths(true);
        saldoList.removeFeature(FeatureSummary.class);
      }
      else
      {
        saldoList.removeAll();
        for (AnlagenlisteZeile sz : zeile)
        {
          saldoList.addItem(sz);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten" + e.getMessage());
    }
    return saldoList;
  }

  public ArrayList<AnlagenlisteZeile> getInfo() throws RemoteException
  {
    ArrayList<AnlagenlisteZeile> zeile = new ArrayList<>();
    Buchungsklasse buchungsklasse = null;
    Buchungsart buchungsart = null;
    Konto konto = null;
    Double startwert = null;
    Double abschreibung = null;
    Double endwert = null;
    Double suBukStartwert = null;
    Double suBukAbschreibung = null;
    Double suBukEndwert = null;
    Double suStartwert = null;
    Double suAbschreibung = null;
    Double suEndwert = null;


    ResultSetExtractor rsi = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        if (!rs.next())
        {
          return Integer.valueOf(0);
        }
        return Integer.valueOf(rs.getInt(1));
      }
    };

    DBService service = Einstellungen.getDBService();
    DBIterator<Buchungsklasse> buchungsklassenIt = service
        .createList(Buchungsklasse.class);
    buchungsklassenIt.setOrder("ORDER BY nummer");
    while (buchungsklassenIt.hasNext())
    {
      buchungsklasse = (Buchungsklasse) buchungsklassenIt.next();
      zeile.add(new AnlagenlisteZeile(AnlagenlisteZeile.HEADER,
          buchungsklasse));
      DBIterator<Buchungsart> buchungsartenIt = service
          .createList(Buchungsart.class);
      buchungsartenIt.addFilter("buchungsklasse = ?",
          new Object[] { buchungsklasse.getID() });
      buchungsartenIt.setOrder("order by nummer");
      
      suBukStartwert = null;
      suBukAbschreibung = null;
      suBukEndwert = null;
      boolean ausgabe = false;
      
      while (buchungsartenIt.hasNext())
      {
        buchungsart = (Buchungsart) buchungsartenIt.next();
        String sqlc = "select count(*) from konto "
            + "where anlagenkonto = TRUE "
            + "and (aufloesung IS NULL OR aufloesung >= ?) "
            + "and (eroeffnung IS NULL OR eroeffnung <= ?)  "
            + "and anlagenklasse = ? "
            + "and anlagenart = ? ";
        int anz = (Integer) service.execute(sqlc,
            new Object[] { datumvon, datumbis, buchungsklasse.getID(), 
                buchungsart.getID() }, rsi);
        if (anz == 0)
        {
          continue;
        }
        ausgabe = true;
        zeile.add(new AnlagenlisteZeile(AnlagenlisteZeile.HEADER2,
            buchungsart));
        
        DBIterator<Konto> kontenIt = service
            .createList(Konto.class);
        kontenIt.addFilter("anlagenkonto = TRUE");
        kontenIt.addFilter("anlagenklasse = ?",
            new Object[] { buchungsklasse.getID() });
        kontenIt.addFilter("anlagenart = ?",
            new Object[] { buchungsart.getID() });
        kontenIt.addFilter("(eroeffnung IS NULL OR eroeffnung <= ?)",
            new Object[] { new java.sql.Date(datumbis.getTime())  });
        kontenIt.addFilter("(aufloesung IS NULL OR aufloesung >= ?)",
            new Object[] { new java.sql.Date(datumvon.getTime())  });
        
        while (kontenIt.hasNext())
        {
          konto = (Konto) kontenIt.next();
          DBIterator<Anfangsbestand> anfangsbestandIt = service
              .createList(Anfangsbestand.class);
          anfangsbestandIt.addFilter("konto = ?",
              new Object[] { konto.getID() });
          anfangsbestandIt.addFilter("datum = ?",
              new Object[] { new java.sql.Date(datumvon.getTime()) });
          startwert = null;
          if (anfangsbestandIt.hasNext() )
          {
            startwert = ((Anfangsbestand) anfangsbestandIt.next()).getBetrag();
            if (suBukStartwert == null)
              suBukStartwert = startwert;
            else
              suBukStartwert += startwert;
          }
          
          Calendar cal = Calendar.getInstance();
          cal.setTime(datumbis);
          cal.add(Calendar.DATE, 1);
          anfangsbestandIt = service.createList(Anfangsbestand.class);
          anfangsbestandIt.addFilter("konto = ?",
              new Object[] { konto.getID() });
          anfangsbestandIt.addFilter("datum = ?",
              new Object[] { new java.sql.Date(cal.getTimeInMillis()) });
          anfangsbestandIt.setOrder("ORDER BY datum");
          endwert = null;
          if (anfangsbestandIt.hasNext() )
          {
            endwert = ((Anfangsbestand) anfangsbestandIt.next()).getBetrag();
            if (suBukEndwert == null)
              suBukEndwert = endwert;
            else
              suBukEndwert += endwert;
          }
          
          DBIterator<Buchung> buchungenIt = service
              .createList(Buchung.class);
          buchungenIt.addFilter("konto = ?",
              new Object[] { konto.getID() });
          buchungenIt.addFilter("buchungsart = ?",
              new Object[] { konto.getAfaartId() });
          buchungenIt.addFilter("datum <= ?",
              new Object[] { new java.sql.Date(datumbis.getTime()) });
          buchungenIt.addFilter("datum >= ?",
              new Object[] { new java.sql.Date(datumvon.getTime()) });
          
          abschreibung = null;
          while (buchungenIt.hasNext())
          {
            if (abschreibung == null)
              abschreibung = ((Buchung) buchungenIt.next()).getBetrag();
            else
              abschreibung += ((Buchung) buchungenIt.next()).getBetrag();
          }
          if (abschreibung != null)
          {
            if (suBukAbschreibung == null)
              suBukAbschreibung = abschreibung;
            else
              suBukAbschreibung += abschreibung;
          }

          zeile.add(new AnlagenlisteZeile(AnlagenlisteZeile.DETAIL,
              konto.getBezeichnung(), konto.getNutzungsdauer(),
              konto.getEroeffnung(), konto.getAfaart(), konto.getBetrag(),
              startwert, abschreibung, endwert));
        }
      }

      if (!ausgabe
          && Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        zeile.remove(zeile.size() - 1);
        continue;
      }
      
      if (suBukStartwert != null)
      {
        if (suStartwert == null)
          suStartwert = suBukStartwert;
        else
          suStartwert += suBukStartwert;
      }
      if (suBukAbschreibung != null)
      {
        if (suAbschreibung == null)
          suAbschreibung = suBukAbschreibung;
        else
          suAbschreibung += suBukAbschreibung;
      }
      if (suBukEndwert != null)
      {
        if (suEndwert == null)
          suEndwert = suBukEndwert;
        else
          suEndwert += suBukEndwert;
      }
      zeile.add(
          new AnlagenlisteZeile(AnlagenlisteZeile.SALDOFOOTER,
              "Saldo" + " " + buchungsklasse.getBezeichnung(), 
              suBukStartwert, suBukAbschreibung, suBukEndwert));
    }
    
    zeile.add(new AnlagenlisteZeile(
        AnlagenlisteZeile.GESAMTSALDOFOOTER, "Saldo Gesamt",
        suStartwert, suAbschreibung, suEndwert));

    // Leerzeile am Ende wegen Scrollbar
    zeile.add(new AnlagenlisteZeile(AnlagenlisteZeile.UNDEFINED, ""));
    return zeile;
  }

  public void setDatumvon(Date datumvon)
  {
    this.datumvon = datumvon;
  }

  public void setDatumbis(Date datumbis)
  {
    this.datumbis = datumbis;
  }

  @Override
  public void removeAll()
  {
    saldoList.removeAll();
  }

  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }

}