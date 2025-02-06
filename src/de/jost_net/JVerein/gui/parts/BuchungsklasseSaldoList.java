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
import de.jost_net.JVerein.keys.ArtBuchungsart;
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

public class BuchungsklasseSaldoList extends TablePart implements Part
{

	private TablePart saldoList;

	private Date datumvon = null;

	private Date datumbis = null;

  protected static double LIMIT = 0.005;

  Double einnahmen;
  Double ausgaben;
  Double umbuchungen;
  Double suBukEinnahmen = Double.valueOf(0);
  Double suBukAusgaben = Double.valueOf(0);
  Double suBukUmbuchungen = Double.valueOf(0);
  Double suEinnahmen = Double.valueOf(0);
  Double suAusgaben = Double.valueOf(0);
  Double suUmbuchungen = Double.valueOf(0);
  HashMap<Double, Double> suNetto = new HashMap<Double, Double>();
  HashMap<Double, Double> suSteuer = new HashMap<Double, Double>();
  HashMap<Double, Double> suBukNetto = new HashMap<Double, Double>();
  HashMap<Double, Double> suBukSteuer = new HashMap<Double, Double>();
  HashMap<String, Double> suBukSteuersatz = new HashMap<String, Double>();
  DBService service;

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

  public BuchungsklasseSaldoList(Action action, Date datumvon, Date datumbis)
  {
    super(action);
    this.datumvon = datumvon;
    this.datumbis = datumbis;
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
        saldoList.addColumn("Umbuchungen", "umbuchungen",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
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
            "Gesamtsaldo" + " ", suEinnahmen, suAusgaben, suUmbuchungen));
    zeile.add(new BuchungsklasseSaldoZeile(
        BuchungsklasseSaldoZeile.GESAMTGEWINNVERLUST, "Gesamt Gewinn/Verlust ",
        suEinnahmen + suAusgaben + suUmbuchungen));

    // Gesamtübersicht Steuern ausgeben
    Boolean first_row = true;
    for (Double steuersatz : suNetto.keySet())
    {
      String string_steuersatz = String.format("%.2f", Math.abs(steuersatz))
          + "% ";
      if (steuersatz > 0.0)
      {
        string_steuersatz += " MwSt.";
      }
      else
      {
        string_steuersatz += " VSt.";
      }
      if (first_row)
      {
        zeile.add(
            new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.STEUERHEADER,
                "Gesamtübersicht Steuern", string_steuersatz,
                suNetto.get(steuersatz), suSteuer.get(steuersatz)));
        first_row = false;
      }
      else
      {
        zeile.add(new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.STEUER,
            "", string_steuersatz, suNetto.get(steuersatz),
            suSteuer.get(steuersatz)));
      }
    }

    // Buchungen ohne Buchungsart
    String sql = "select count(*) from buchung " + "where datum >= ? and datum <= ?  "
        + "and buchung.buchungsart is null";

    Integer anzahl = (Integer) service.execute(sql,
        new Object[] { datumvon, datumbis }, rsi);
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

  private void createBuchungsklasse(Buchungsklasse buchungsklasse, 
      ArrayList<BuchungsklasseSaldoZeile> zeile) throws RemoteException 
  {
    Buchungsart buchungsart = null;
    String buchungsklasseId = null;
    String bezeichnung = "Nicht zugeordnet";
    if (buchungsklasse != null)
    {
      buchungsklasseId = buchungsklasse.getID();
      bezeichnung = buchungsklasse.getBezeichnung();
    }

    suBukSteuersatz.clear();
    DBIterator<Buchungsart> buchungsartenSteuerIt = service
        .createList(Buchungsart.class);
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      if (buchungsklasseId != null)
        buchungsartenSteuerIt.addFilter("buchungsklasse = ?", buchungsklasseId );
      else
        buchungsartenSteuerIt.addFilter("buchungsklasse is null");
    }
    buchungsartenSteuerIt.addFilter("steuersatz <> 0");
    while (buchungsartenSteuerIt.hasNext())
    {
      buchungsart = (Buchungsart) buchungsartenSteuerIt.next();
      Buchungsart steuer_buchungsart = buchungsart.getSteuerBuchungsart();
      if (steuer_buchungsart != null)
      {
        String steuer_buchungsart_id = steuer_buchungsart.getID();
        if (buchungsart.getArt() == ArtBuchungsart.EINNAHME)
        {
          suBukSteuersatz.put(steuer_buchungsart_id,
              buchungsart.getSteuersatz());
        }
        else if (buchungsart.getArt() == ArtBuchungsart.AUSGABE)
        {
          suBukSteuersatz.put(steuer_buchungsart_id,
              -buchungsart.getSteuersatz());
        }
      }
    }
    
    DBIterator<Buchungsart> buchungsartenIt = service
        .createList(Buchungsart.class);
    if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      if (buchungsklasseId != null)
        buchungsartenIt.addFilter("buchungsklasse = ?", buchungsklasseId );
      else
        buchungsartenIt.addFilter("buchungsklasse is null");
    }
    buchungsartenIt.setOrder("order by nummer");
    suBukEinnahmen = Double.valueOf(0);
    suBukAusgaben = Double.valueOf(0);
    suBukUmbuchungen = Double.valueOf(0);
    suBukNetto.clear();
    suBukSteuer.clear();

    while (buchungsartenIt.hasNext())
    {
      buchungsart = (Buchungsart) buchungsartenIt.next();
      String sqlc = null;
      int anz = 0;
      if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
      {
        sqlc = "select count(*) from buchung, buchungsart, konto "
            + "where datum >= ? and datum <= ?  "
            + "and buchung.buchungsart = buchungsart.id "
            + "and buchungsart.id = ? "
            + "and buchung.konto = konto.id "
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
              + "and buchungsart.id = ? "
              + "and buchung.buchungsklasse = ? "
              + "and buchung.konto = konto.id "
              + "and konto.kontoart < ? ";
          anz = (Integer) service.execute(sqlc,
              new Object[] { datumvon, datumbis, buchungsart.getID(), 
                  buchungsklasseId, Kontoart.LIMIT.getKey() }, rsi);
        }
        else
        {
          // Buchungen ohne Buchungsklasse
          sqlc = "select count(*) from buchung, buchungsart, konto "
              + "where datum >= ? and datum <= ?  "
              + "and buchung.buchungsart = buchungsart.id "
              + "and buchungsart.id = ? "
              + "and buchung.buchungsklasse is null "
              + "and buchung.konto = konto.id "
              + "and konto.kontoart < ? ";
          anz = (Integer) service.execute(sqlc, new Object[] { datumvon,
              datumbis, buchungsart.getID(), Kontoart.LIMIT.getKey() }, rsi);
        }
      }

      if (anz == 0)
      {
        continue;
      }
      String sql = null;
      if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
      {
        // Buchungsklasse steht in Buchungsart
        sql = "select sum(buchung.betrag) from buchung, konto, buchungsart "
            + "where datum >= ? and datum <= ?  "
            + "and buchung.konto = konto.id "
            + "and konto.kontoart < ? "
            + "and buchung.buchungsart = buchungsart.id "
            + "and buchungsart.id = ? " + "and buchungsart.art = ?";
        einnahmen = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(), 0 }, rsd);
        suBukEinnahmen += einnahmen;
        ausgaben = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(), 1 }, rsd);
        suBukAusgaben += ausgaben;
        umbuchungen = (Double) service.execute(sql, new Object[] { datumvon,
            datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(), 2 }, rsd);
        suBukUmbuchungen += umbuchungen;
      }
      else
      {
        // Buchungsklasse steht in Buchung
        if (buchungsklasseId != null)
        {
          // Buchungen der Buchungsklasse
          sql = "select sum(buchung.betrag) from buchung, konto, buchungsart "
              + "where datum >= ? and datum <= ?  "
              + "and buchung.konto = konto.id "
              + "and konto.kontoart < ? "
              + "and buchung.buchungsart = buchungsart.id "
              + "and buchungsart.id = ? " + "and buchungsart.art = ? "
              + "and buchung.buchungsklasse = ? ";
          einnahmen = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey(),
                  buchungsart.getID(), 0, buchungsklasseId },
              rsd);
          suBukEinnahmen += einnahmen;
          ausgaben = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey(),
                  buchungsart.getID(), 1, buchungsklasseId },
              rsd);
          suBukAusgaben += ausgaben;
          umbuchungen = (Double) service.execute(sql,
              new Object[] { datumvon, datumbis, Kontoart.LIMIT.getKey(),
                  buchungsart.getID(), 2, buchungsklasseId },
              rsd);
          suBukUmbuchungen += umbuchungen;
        }
        else
        {
          // Buchungen ohne Buchungsklasse
          sql = "select sum(buchung.betrag) from buchung, konto, buchungsart "
              + "where datum >= ? and datum <= ?  "
              + "and buchung.konto = konto.id "
              + "and konto.kontoart < ? "
              + "and buchung.buchungsart = buchungsart.id "
              + "and buchungsart.id = ? " + "and buchungsart.art = ? "
              + "and buchung.buchungsklasse is null ";
          einnahmen = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(), 0 },
              rsd);
          suBukEinnahmen += einnahmen;
          ausgaben = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(), 1 },
              rsd);
          suBukAusgaben += ausgaben;
          umbuchungen = (Double) service.execute(sql, new Object[] { datumvon,
              datumbis, Kontoart.LIMIT.getKey(), buchungsart.getID(), 2 },
              rsd);
          suBukUmbuchungen += umbuchungen;
        }
      }

      if (buchungsart.getSteuersatz() > 0.0)
      {
        Double steuersatz = buchungsart.getSteuersatz();
        Double val = 0.0;
        if (buchungsart.getArt() == ArtBuchungsart.EINNAHME)
        {
          val = einnahmen;
        }
        else if (buchungsart.getArt() == ArtBuchungsart.AUSGABE)
        {
          steuersatz = -steuersatz;
          val = ausgaben;
        }
        if (!suBukNetto.containsKey(steuersatz))
        {
          suBukNetto.put(steuersatz, 0.0);
        }
        suBukNetto.put(steuersatz, suBukNetto.get(steuersatz) + val);
      }
      else if (suBukSteuersatz.containsKey(buchungsart.getID()))
      {
        Double steuersatz = suBukSteuersatz.get(buchungsart.getID());
        if (!suBukSteuer.containsKey(steuersatz))
        {
          suBukSteuer.put(steuersatz, 0.0);
        }
        suBukSteuer.put(steuersatz,
            suBukSteuer.get(steuersatz) + einnahmen + ausgaben + umbuchungen);
      }
      if (Math.abs(einnahmen) >= LIMIT || Math.abs(ausgaben) >= LIMIT
          || Math.abs(umbuchungen) >= LIMIT
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        zeile.add(new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.DETAIL,
            buchungsart, einnahmen, ausgaben, umbuchungen));
      }
    }
    suEinnahmen += suBukEinnahmen;
    suAusgaben += suBukAusgaben;
    suUmbuchungen += suBukUmbuchungen;
    if (Math.abs(suBukEinnahmen) < LIMIT && Math.abs(suBukAusgaben) < LIMIT
        && Math.abs(suBukUmbuchungen) < LIMIT
        && Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
    {
      zeile.remove(zeile.size() - 1);
      return;
    }

    zeile.add(
        new BuchungsklasseSaldoZeile(BuchungsklasseSaldoZeile.SALDOFOOTER,
            "Saldo" + " " + bezeichnung, suBukEinnahmen,
            suBukAusgaben, suBukUmbuchungen));
    zeile.add(new BuchungsklasseSaldoZeile(
        BuchungsklasseSaldoZeile.SALDOGEWINNVERLUST,
        "Gewinn/Verlust" + " " + bezeichnung,
        suBukEinnahmen + suBukAusgaben + suBukUmbuchungen));

    // Buchungsklasse Übersicht Steuern ausgeben
    Boolean first_row = true;
    for (Double steuersatz : suBukNetto.keySet())
    {
      String string_steuersatz = String.format("%.2f", Math.abs(steuersatz))
          + "% ";
      if (steuersatz > 0.0)
      {
        string_steuersatz += " MwSt.";
      }
      else
      {
        string_steuersatz += " VSt.";
      }
      if (!suBukSteuer.containsKey(steuersatz))
      {
        suBukSteuer.put(steuersatz, 0.0);
      }
      if (first_row)
      {
        zeile.add(new BuchungsklasseSaldoZeile(
            BuchungsklasseSaldoZeile.STEUERHEADER,
            "Steuern " + bezeichnung, string_steuersatz,
            suBukNetto.get(steuersatz), suBukSteuer.get(steuersatz)));
        first_row = false;
      }
      else
      {
        zeile.add(new BuchungsklasseSaldoZeile(
            BuchungsklasseSaldoZeile.STEUER, "", string_steuersatz,
            suBukNetto.get(steuersatz), suBukSteuer.get(steuersatz)));
      }

      // Werte für Gesamtübersicht addieren
      if (!suNetto.containsKey(steuersatz))
      {
        suNetto.put(steuersatz, 0.0);
        suSteuer.put(steuersatz, 0.0);
      }
      suNetto.put(steuersatz,
          suNetto.get(steuersatz) + suBukNetto.get(steuersatz));
      suSteuer.put(steuersatz,
          suSteuer.get(steuersatz) + suBukSteuer.get(steuersatz));

    }
  }
}
