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
import java.util.Date;
import java.util.HashMap;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.BuchungsklasseSaldoZeile;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class AbstractSaldoList extends TablePart
{

  protected TablePart saldoList;

  protected Date datumvon = null;

  protected Date datumbis = null;

  protected boolean umbuchung;

  protected static double LIMIT = 0.005;

  protected Double einnahmen;

  protected Double ausgaben;

  protected Double umbuchungen;

  protected Double suBukEinnahmen = Double.valueOf(0);

  protected Double suBukAusgaben = Double.valueOf(0);

  protected Double suBukUmbuchungen = Double.valueOf(0);

  protected Double suEinnahmen = Double.valueOf(0);

  protected Double suAusgaben = Double.valueOf(0);

  protected Double suUmbuchungen = Double.valueOf(0);

  protected HashMap<Double, Double> suNetto = new HashMap<Double, Double>();

  protected HashMap<Double, Double> suSteuer = new HashMap<Double, Double>();

  protected HashMap<Double, Double> suBukNetto = new HashMap<Double, Double>();

  protected HashMap<Double, Double> suBukSteuer = new HashMap<Double, Double>();

  protected HashMap<String, Double> suBukSteuersatz = new HashMap<String, Double>();

  protected DBService service;

  ResultSetExtractor rsd = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws SQLException
    {
      if (!rs.next())
      {
        return Double.valueOf(0);
      }
      return Double.valueOf(rs.getDouble(1));
    }
  };

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

  public AbstractSaldoList(Action action, Date datumvon, Date datumbis,
      boolean umbuchung)
  {
    super(action);
    this.datumvon = datumvon;
    this.datumbis = datumbis;
    this.umbuchung = umbuchung;
  }

  public Part getSaldoList() throws ApplicationException
  {
    ArrayList<BuchungsklasseSaldoZeile> zeile = null;
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
        saldoList.addColumn("Buchungsklasse", "buchungsklassenbezeichnung",
            null, false);
        saldoList.addColumn("Buchungsart", "buchungsartbezeichnung");
        saldoList.addColumn("Einnahmen", "einnahmen",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Ausgaben", "ausgaben",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        if (umbuchung)
        {
        saldoList.addColumn("Umbuchungen", "umbuchungen",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        }
        saldoList.addColumn("Anzahl", "anzahlbuchungen");
        saldoList.setRememberColWidths(true);
        saldoList.removeFeature(FeatureSummary.class);
      }
      else
      {
        saldoList.removeAll();
        for (BuchungsklasseSaldoZeile sz : zeile)
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

  public ArrayList<BuchungsklasseSaldoZeile> getInfo() throws RemoteException
  {
    ArrayList<BuchungsklasseSaldoZeile> zeile = new ArrayList<>();
    Buchungsklasse buchungsklasse = null;
    suBukEinnahmen = Double.valueOf(0);
    suBukAusgaben = Double.valueOf(0);
    suBukUmbuchungen = Double.valueOf(0);
    suEinnahmen = Double.valueOf(0);
    suAusgaben = Double.valueOf(0);
    suUmbuchungen = Double.valueOf(0);
    suNetto = new HashMap<Double, Double>();
    suSteuer = new HashMap<Double, Double>();
    suBukNetto = new HashMap<Double, Double>();
    suBukSteuer = new HashMap<Double, Double>();
    suBukSteuersatz = new HashMap<String, Double>();

    service = Einstellungen.getDBService();
    DBIterator<Buchungsklasse> buchungsklassenIt = service
        .createList(Buchungsklasse.class);
    buchungsklassenIt.setOrder("ORDER BY nummer");
    while (buchungsklassenIt.hasNext())
    {
      buchungsklasse = (Buchungsklasse) buchungsklassenIt.next();
      zeile.add(new BuchungsklasseSaldoZeile(
          BuchungsklasseSaldoZeile.HEADER, buchungsklasse));
      createBuchungsklasse(buchungsklasse, zeile);
    }
    
    // Buchungen ohne Buchungsklasse
    Buchungsklasse b = (Buchungsklasse) service
        .createObject(Buchungsklasse.class, null);
    b.setBezeichnung("Nicht zugeordnet");
    zeile.add(
        new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.HEADER, b));
    createBuchungsklasse(null, zeile);

    zeile.add(
        new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.GESAMTSALDOFOOTER,
            "Saldo aller Buchungsklassen ", suEinnahmen, suAusgaben,
            suUmbuchungen));

    // Buchungen ohne Buchungsart
    String sqlOhneBuchungsart = "SELECT sum(buchung.betrag) FROM buchung, konto "
        + "WHERE datum >= ? AND datum <= ? AND buchung.konto = konto.id "
        + "AND konto.kontoart < ? AND buchung.buchungsart is null";
    Double ohneBuchungsart = (Double) service.execute(sqlOhneBuchungsart,
        new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey() }, rsd);

    if (ohneBuchungsart >= 0.01d || ohneBuchungsart <= -0.01d)
    {
      zeile.add(new BuchungsklasseSaldoZeile(
          BuchungsklasseSaldoZeile.GESAMTGEWINNVERLUST,
          "Saldo Buchungen ohne Buchungsart ", ohneBuchungsart));
    }

    zeile.add(new BuchungsklasseSaldoZeile(
        BuchungsklasseSaldoZeile.GESAMTGEWINNVERLUST, "Gesamtsaldo ",
        suEinnahmen + suAusgaben + suUmbuchungen + ohneBuchungsart));

    // Gesamtübersicht Steuern ausgeben
    getSteuerUebersicht(zeile);

    // Anzahl Buchungen ohne Buchungsart
    String sql = "SELECT count(*) FROM buchung, konto "
        + "WHERE datum >= ? AND datum <= ? AND buchung.konto = konto.id "
        + "AND konto.kontoart < ? AND buchung.buchungsart is null";

    Integer anzahl = (Integer) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey() }, rsi);
    if (anzahl > 0)
    {
      zeile.add(new BuchungsklasseSaldoZeile(
          BuchungsklasseSaldoZeile.NICHTZUGEORDNETEBUCHUNGEN,
          "Anzahl Buchungen ohne Buchungsart", anzahl));
    }
    
    // Leerzeile am Ende wegen Scrollbar
    zeile.add(new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.UNDEFINED, ""));
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

  protected void createBuchungsklasse(Buchungsklasse buchungsklasse,
      ArrayList<BuchungsklasseSaldoZeile> zeile) throws RemoteException 
  {
    // In abgeleiteter Klasse implementieren
  }

  protected void getSteuerUebersicht(ArrayList<BuchungsklasseSaldoZeile> zeile)
  {
    // In abgeleiteter Klasse implementieren
  }

  protected int getAnzahlBuchungen(String buchungsklasseId,
      Buchungsart buchungsart) throws RemoteException
  {
    String sqlc = null;
    int anz = 0;
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      sqlc = "select count(*) from buchung, buchungsart, konto "
          + "where datum >= ? and datum <= ?  "
          + "and buchung.buchungsart = buchungsart.id "
          + "and buchungsart.id = ? " + "and buchung.konto = konto.id "
          + "and konto.kontoart < ? ";
      anz = (Integer) service.execute(sqlc, new Object[] { datumvon, datumbis,
          buchungsart.getID(), Kontoart.LIMIT.getKey() }, rsi);
    }
    else
    {
      if (buchungsklasseId != null)
      {
        // Buchungen der Buchungsklasse
        sqlc = "select count(*) from buchung, buchungsart, konto "
            + "where datum >= ? and datum <= ?  "
            + "and buchung.buchungsart = buchungsart.id "
            + "and buchungsart.id = ? " + "and buchung.buchungsklasse = ? "
            + "and buchung.konto = konto.id " + "and konto.kontoart < ? ";
        anz = (Integer) service.execute(sqlc, new Object[] { datumvon, datumbis,
            buchungsart.getID(), buchungsklasseId, Kontoart.LIMIT.getKey() },
            rsi);
      }
      else
      {
        // Buchungen ohne Buchungsklasse
        sqlc = "select count(*) from buchung, buchungsart, konto "
            + "where datum >= ? and datum <= ?  "
            + "and buchung.buchungsart = buchungsart.id "
            + "and buchungsart.id = ? " + "and buchung.buchungsklasse is null "
            + "and buchung.konto = konto.id " + "and konto.kontoart < ? ";
        anz = (Integer) service.execute(sqlc, new Object[] { datumvon, datumbis,
            buchungsart.getID(), Kontoart.LIMIT.getKey() }, rsi);
      }
    }
    return anz;
  }

}
