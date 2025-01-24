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
import de.jost_net.JVerein.gui.control.MittelverwendungControl;
import de.jost_net.JVerein.io.MittelverwendungZeile;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class MittelverwendungList extends TablePart
{

  private TablePart saldoList;

  private Date datumvon = null;

  private Date datumbis = null;

  private int tab = 0;

  private static double LIMIT = 0.005;

  private static String NULL = " ";

  public MittelverwendungList(Action action, Date datumvon, Date datumbis,
      int tab)
  {
    super(action);
    this.datumvon = datumvon;
    this.datumbis = datumbis;
    this.tab = tab;
  }

  public Part getSaldoList() throws ApplicationException
  {
    ArrayList<MittelverwendungZeile> zeilen = null;
    try
    {
      zeilen = getInfo();

      if (saldoList == null)
      {
        saldoList = new TablePart(zeilen, null)
        {
          @Override
          protected void orderBy(int index)
          {
            return;
          }
        };
        saldoList.addColumn("Nr", "position");
        saldoList.addColumn("Mittel", "bezeichnung");
        saldoList.addColumn("Betrag", "betrag",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Summe", "summe",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_LEFT);
        saldoList.addColumn("Kommentar", "kommentar");
        saldoList.setRememberColWidths(true);
        saldoList.setRememberOrder(true);
        saldoList.removeFeature(FeatureSummary.class);
      }
      else
      {
        saldoList.removeAll();
        for (MittelverwendungZeile sz : zeilen)
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

  ResultSetExtractor rsmap = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws SQLException
    {
      HashMap<Long, String> map = new HashMap<>();
      while (rs.next())
      {
        map.put(rs.getLong(1), rs.getString(2));
      }
      return map;
    }
  };

  ResultSetExtractor rsmapa = new ResultSetExtractor()
  {
    @Override
    public Object extract(ResultSet rs) throws SQLException
    {
      HashMap<Long, String[]> map = new HashMap<>();
      while (rs.next())
      {
        map.put(rs.getLong(1),
            new String[] { rs.getString(2), rs.getString(3) });
      }
      return map;
    }
  };

  public ArrayList<MittelverwendungZeile> getInfo() throws RemoteException
  {
    switch (tab)
    {
      case MittelverwendungControl.FLOW_REPORT:
        return getFlowReport();
      case MittelverwendungControl.SALDO_REPORT:
        return getSaldoReport();
    }
    return new ArrayList<MittelverwendungZeile>();
  }

  public ArrayList<MittelverwendungZeile> getFlowReport() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    String sql;
    ArrayList<MittelverwendungZeile> zeilen = new ArrayList<>();
    String bezeichnung = "";
    Integer pos = 1;

    // Schritt 1: Berechnung des Verwendungsrückstand(+)/-überhang(-)
    // am Ende des letzten GJ
    // Vorhandene Geldmittel zum Ende des letzten GJ sind zu verwenden
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? ";
    Double vorhandeneMittel = (Double) service.execute(sql,
        new Object[] { datumvon, Kontoart.GELD.getKey() }, rsd);

    // Vorhandene zweckfremde Anlagen sind zu verwenden
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? "
        + " AND konto.zweck = ?";
    vorhandeneMittel += (Double) service.execute(sql, new Object[] { datumvon,
        Kontoart.ANLAGE.getKey(), Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey() },
        rsd);

    // Nicht der zeitnahen Mittelverwendung unterliegende Mittel (Rücklagen)
    // zum Ende des letzten GJ können abgezogen werden
    sql = "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id" + " AND konto.kontoart >= ?"
        + " AND konto.kontoart <= ?";
    vorhandeneMittel -= (Double) service.execute(sql,
        new Object[] { datumvon, Kontoart.RUECKLAGE_ZWECK_GEBUNDEN.getKey(),
            Kontoart.RUECKLAGE_SONSTIG.getKey() },
        rsd);

    bezeichnung = "Verwendungsrückstand(+)/-überhang(-) am Ende des letzten GJ";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        vorhandeneMittel, null, NULL);

    // Schritt 2: Mittel Zufluss
    // Summe aller Zuflüsse bei Geldkonten und Anlagen (=Sachspenden)
    sql = getSummenKontenSql();
    Double zufuehrung = (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.GELD.getKey(),
            Kontoart.ANLAGE.getKey(), ArtBuchungsart.EINNAHME },
        rsd);
    // Summe Zuflüsse durch Umbuchung
    // Auszahlung aus Verbindlichkeiten z.B. Darlehen,
    // Rückbuchung von zweckgebundenen Anlagen
    sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0";
    zufuehrung -= (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
            Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(),
            ArtBuchungsart.UMBUCHUNG },
        rsd);

    bezeichnung = "Insgesamt im GJ zugeflossene Mittel";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        zufuehrung, null, NULL);
    bezeichnung = "          Zu verwendende Mittel im GJ und nächstem GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung, vorhandeneMittel, NULL);

    // Schritt 3: Mittel Abfluss
    // Summe aller Abflüsse bei Geldkonten
    sql = getSummenKontoSql();
    Double verwendung = (Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.GELD.getKey(), ArtBuchungsart.AUSGABE }, rsd);
    // Summe aller Abflüsse bei nicht nutzungsgebundenen Anlagen
    sql = getSummenKontoZweckSql();
    verwendung += (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
            Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(),
            ArtBuchungsart.AUSGABE },
        rsd);
    // Summe der Abflüsse bei Umbuchung
    // Tilgung Verbindlichkeiten z.B. Darlehen,
    // Erwerb zweckgebundener Anlagen
    sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0";
    verwendung -= (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
            Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(),
            ArtBuchungsart.UMBUCHUNG },
        rsd);

    bezeichnung = "Im GJ verwendete Mittel";
    addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
        verwendung, NULL);

    // Rücklagen
    Double summeZuRuecklagen = 0.0;
    Double summeEntRuecklagen = 0.0;
    sql = getSummenRuecklagenSql();
    for (int i = Kontoart.RUECKLAGE_ZWECK_GEBUNDEN
        .getKey(); i <= Kontoart.RUECKLAGE_SONSTIG.getKey(); i++)
    {
      Double zuRuecklagen = (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, i, ArtBuchungsart.EINNAHME }, rsd);
      summeZuRuecklagen += zuRuecklagen;
      if (Math.abs(zuRuecklagen) > LIMIT
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        bezeichnung = "Zuführung " + Kontoart.getByKey(i).getText();
        addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung,
            null, -zuRuecklagen, NULL);
      }
      Double entRuecklagen = (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, i, ArtBuchungsart.AUSGABE }, rsd);
      summeEntRuecklagen += entRuecklagen;
      if (Math.abs(entRuecklagen) > LIMIT
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        bezeichnung = "Entnahme " + Kontoart.getByKey(i).getText();
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
            -entRuecklagen, null, NULL);
      }
    }

    bezeichnung = "          Verwendungsrückstand(+)/-überhang(-) zum Ende des GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung + vorhandeneMittel + verwendung - summeZuRuecklagen,
        -summeEntRuecklagen, NULL);

    // Leerzeile am Ende wegen Scrollbar
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, null,
        null, null, null, NULL));
    return zeilen;
  }

  public ArrayList<MittelverwendungZeile> getSaldoReport()
      throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    String sql;
    ArrayList<MittelverwendungZeile> zeilen = new ArrayList<>();
    Integer pos = 1;
    Double summeVermoegen = 0.0;
    String bezeichnung = "";
    // Anlagevermögen
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, pos++,
        "Anlagenvermögen", null, null, NULL));
    if (Einstellungen.getEinstellung().getSummenAnlagenkonto())
    {
      sql = getAnfangsbestandKontoartSql();
      Double anlagenStand = (Double) service.execute(sql,
          new Object[] { datumvon, Kontoart.ANLAGE.getKey(), datumvon }, rsd);
      sql = getSummenBetragKontoartSql();
      anlagenStand += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey() }, rsd);
      bezeichnung = "          Summe Anlagenkonten";
      addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
          anlagenStand, 0.0, NULL);
      summeVermoegen += anlagenStand;
    }
    else
    {
      sql = "SELECT id, bezeichnung, kommentar FROM konto WHERE konto.kontoart = ?";
      @SuppressWarnings("unchecked")
      HashMap<Long, String[]> map0 = (HashMap<Long, String[]>) service
          .execute(sql, new Object[] { Kontoart.ANLAGE.getKey() }, rsmapa);
      for (Long kontoId : map0.keySet())
      {
        sql = getAnfangsbestandKontoSql();
        Double kontoStand = (Double) service.execute(sql,
            new Object[] { datumvon, kontoId, datumvon }, rsd);
        sql = getSummenBetragKontoSql();
        kontoStand += (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, kontoId }, rsd);
        if (Math.abs(kontoStand) > LIMIT)
        {
          String kommentar = map0.get(kontoId)[1];
          if (kommentar != null && !kommentar.isEmpty())
          {
            kommentar = kommentar.split("\n")[0];
          }
          addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++,
              map0.get(kontoId)[0], kontoStand, null, kommentar);
          summeVermoegen += kontoStand;
        }
      }
      bezeichnung = "          Summe Anlagenkonten";
      addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
          summeVermoegen, 0.0, NULL);
    }

    // Geldkonten
    Double summeGeld = 0.0;
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, pos++,
        "Geldvermögen", null, null, NULL));
    sql = "SELECT id, bezeichnung, kommentar FROM konto WHERE konto.kontoart = ?";
    @SuppressWarnings("unchecked")
    HashMap<Long, String[]> map1 = (HashMap<Long, String[]>) service
        .execute(sql, new Object[] { Kontoart.GELD.getKey() }, rsmapa);
    for (Long kontoId : map1.keySet())
    {
      sql = getAnfangsbestandKontoSql();
      Double kontoStand = (Double) service.execute(sql,
          new Object[] { datumvon, kontoId, datumvon }, rsd);
      sql = getSummenBetragKontoSql();
      kontoStand += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, kontoId }, rsd);
      if (Math.abs(kontoStand) > LIMIT)
      {
        String kommentar = map1.get(kontoId)[1];
        if (kommentar != null && !kommentar.isEmpty())
        {
          kommentar = kommentar.split("\n")[0];
        }
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++,
            map1.get(kontoId)[0], kontoStand, null, kommentar);
        summeGeld += kontoStand;
      }
    }
    bezeichnung = "          Summe Geldkonten";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, summeGeld,
        0.0, NULL);
    summeVermoegen += summeGeld;

    // Verbindlichkeitskonten
    Double summeSchulden = 0.0;
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, pos++,
        "Darlehen, Kredite etc.", null, null, NULL));
    sql = "SELECT id, bezeichnung, kommentar FROM konto WHERE konto.kontoart = ?";
    @SuppressWarnings("unchecked")
    HashMap<Long, String[]> map2 = (HashMap<Long, String[]>) service.execute(
        sql, new Object[] { Kontoart.SCHULDEN.getKey() }, rsmapa);
    for (Long kontoId : map2.keySet())
    {
      sql = getAnfangsbestandKontoSql();
      Double kontoStand = (Double) service.execute(sql,
          new Object[] { datumvon, kontoId, datumvon }, rsd);
      sql = getSummenBetragKontoSql();
      kontoStand += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, kontoId }, rsd);
      if (Math.abs(kontoStand) > LIMIT)
      {
        String kommentar = map2.get(kontoId)[1];
        if (kommentar != null && !kommentar.isEmpty())
        {
          kommentar = kommentar.split("\n")[0];
        }
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++,
            map2.get(kontoId)[0], kontoStand, null, kommentar);
        summeSchulden += kontoStand;
      }
    }
    bezeichnung = "          Summe Darlehen, Kredite etc.";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        summeSchulden, 0.0, NULL);
    summeVermoegen += summeSchulden;
    bezeichnung = "          Gesamtvermögen";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        summeVermoegen, 0.0, NULL);
    // Leerzeile
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.LEERZEILE, null,
        null, null, null, NULL));

    // Mittelverwendung
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, pos++,
        "Mittelverwendung", null, null, NULL));
    // Nutzungsgebundenes Anlagevermögen
    sql = getAnfangsbestandKontoartZweckSql();
    Double anlagenStand = (Double) service.execute(sql, new Object[] { datumvon,
        Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), datumvon },
        rsd);
    sql = getSummenBetragKontoartZweckSql();
    anlagenStand += (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
            Anlagenzweck.NUTZUNGSGEBUNDEN.getKey() },
        rsd);
    bezeichnung = "          Nutzungsgebundenes Anlagenvermögen";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, 0.0,
        -anlagenStand, NULL);
    summeVermoegen -= anlagenStand;
    // Verbindlichkeitskonten
    bezeichnung = "          Summe Darlehen, Kredite etc.";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, 0.0,
        -summeSchulden, NULL);
    summeVermoegen -= summeSchulden;
    // Rücklagen, Vermögen nicht zugeordnet
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, pos++,
        "Nicht zugeordnete Rücklagen", null, null, NULL));
    Double summeRuecklagen = 0.0;
    sql = "SELECT id, bezeichnung, kommentar FROM konto"
        + " WHERE konto.kontoart >= ?" + " AND konto.kontoart <= ?"
        + " AND konto.anlagenklasse IS NULL";
    @SuppressWarnings("unchecked")
    HashMap<Long, String[]> map3 = (HashMap<Long, String[]>) service
        .execute(sql, new Object[] { Kontoart.RUECKLAGE_ZWECK_GEBUNDEN.getKey(),
            Kontoart.RUECKLAGE_SONSTIG.getKey() }, rsmapa);
    for (Long kontoId : map3.keySet())
    {
      sql = getAnfangsbestandKontoSql();
      Double ruecklagen = (Double) service.execute(sql,
          new Object[] { datumvon, kontoId, datumvon }, rsd);
      sql = getSummenBetragKontoSql();
      ruecklagen += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, kontoId }, rsd);
      if (Math.abs(ruecklagen) > LIMIT)
      {
        String kommentar = map3.get(kontoId)[1];
        if (kommentar != null && !kommentar.isEmpty())
        {
          kommentar = kommentar.split("\n")[0];
        }
        addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++,
            map3.get(kontoId)[0], null, -ruecklagen, kommentar);
        summeRuecklagen += ruecklagen;
      }
    }
    if (Math.abs(summeRuecklagen) > LIMIT)
    {
      bezeichnung = "          Summe nicht zugeordneter Rücklagen";
      addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, 0.0,
          -summeRuecklagen, NULL);
      summeVermoegen -= summeRuecklagen;
    }
    else
    {
      zeilen.remove(zeilen.size() - 1);
    }

    // Rücklagen, Vermögen den Buchungsklassen zugeordnet
    sql = "SELECT buchungsklasse.id, buchungsklasse.bezeichnung FROM buchungsklasse"
        + " ORDER BY nummer";
    @SuppressWarnings("unchecked")
    HashMap<Long, String> buchungsklassen = (HashMap<Long, String>) service
        .execute(sql, new Object[] {}, rsmap);
    for (Long buchungsklasseId : buchungsklassen.keySet())
    {
      zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED,
          pos++, buchungsklassen.get(buchungsklasseId), null, null, NULL));
      summeRuecklagen = 0.0;
      sql = "SELECT id, bezeichnung, kommentar FROM konto"
          + " WHERE konto.kontoart >= ?" + " AND konto.kontoart <= ?"
          + " AND konto.anlagenklasse = ?";
      @SuppressWarnings("unchecked")
      HashMap<Long, String[]> map4 = (HashMap<Long, String[]>) service
          .execute(sql,
              new Object[] { Kontoart.RUECKLAGE_ZWECK_GEBUNDEN.getKey(),
                  Kontoart.RUECKLAGE_SONSTIG.getKey(), buchungsklasseId },
              rsmapa);
      for (Long kontoId : map4.keySet())
      {
        sql = getAnfangsbestandKontoSql();
        Double ruecklagen = (Double) service.execute(sql,
            new Object[] { datumvon, kontoId, datumvon }, rsd);
        sql = getSummenBetragKontoSql();
        ruecklagen += (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, kontoId }, rsd);
        if (Math.abs(ruecklagen) > LIMIT)
        {
          String kommentar = map4.get(kontoId)[1];
          if (kommentar != null && !kommentar.isEmpty())
          {
            kommentar = kommentar.split("\n")[0];
          }
          addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++,
              map4.get(kontoId)[0], null, -ruecklagen, kommentar);
          summeRuecklagen += ruecklagen;
        }
      }
      if (Math.abs(summeRuecklagen) > LIMIT)
      {
        bezeichnung = "          Summe Rücklagen/Vermögen "
            + buchungsklassen.get(buchungsklasseId);
        addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, 0.0,
            -summeRuecklagen, NULL);
        summeVermoegen -= summeRuecklagen;
      }
      else
      {
        zeilen.remove(zeilen.size() - 1);
      }
    }
    // Leerzeile
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.LEERZEILE, null,
        null, null, null, NULL));
    bezeichnung = "Verwendungsrückstand(+)/-überhang(-) zum Ende des GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung, 0.0,
        summeVermoegen, NULL);
    // Leerzeile undefined - nicht drucken in PDF und CSV
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, null,
        null, null, null, NULL));
    return zeilen;
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

  private String getSummenKontoSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenKontenSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND (konto.kontoart = ? OR konto.kontoart = ?)"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenKontoZweckSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ? AND konto.zweck = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenUmbuchungSql() throws RemoteException
  {
    String sql = "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND (konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?))"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
    return sql;
  }

  private String getSummenRuecklagenSql()
  {
    return "SELECT sum(buchung.betrag) FROM buchung, konto, buchungsart"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?"
        + " AND buchung.buchungsart = buchungsart.id"
        + " AND buchungsart.art = ?";
  }

  private String getAnfangsbestandKontoartSql() throws RemoteException
  {
    return "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? "
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)";
  }

  private String getAnfangsbestandKontoartZweckSql() throws RemoteException
  {
    return "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? "
        + " AND konto.zweck = ?"
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)";
  }

  private String getSummenBetragKontoartSql() throws RemoteException
  {
    return "SELECT sum(buchung.betrag) FROM buchung, konto"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?";
  }

  private String getSummenBetragKontoartZweckSql() throws RemoteException
  {
    return "SELECT sum(buchung.betrag) FROM buchung, konto"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = konto.id"
        + " AND konto.kontoart = ?" + " AND konto.zweck = ?";
  }

  private String getAnfangsbestandKontoSql() throws RemoteException
  {
    return "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?" + " AND anfangsbestand.konto = ?"
        + " AND anfangsbestand.konto = konto.id "
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)";
  }

  private String getSummenBetragKontoSql() throws RemoteException
  {
    return "SELECT sum(buchung.betrag) FROM buchung"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = ?";
  }

  private void addZeile(ArrayList<MittelverwendungZeile> zeilen, int status,
      Integer position, String bezeichnung, Double einnahme, Double ausgabe,
      String kommentar) throws RemoteException
  {
    if (einnahme != null && einnahme == -0.0)
    {
      einnahme = 0.0;
    }
    if (ausgabe != null && ausgabe == -0.0)
    {
      ausgabe = 0.0;
    }
    switch (status)
    {
      case MittelverwendungZeile.EINNAHME:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            einnahme, null, kommentar));
        break;
      case MittelverwendungZeile.AUSGABE:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            ausgabe, null, kommentar));
        break;
      case MittelverwendungZeile.SUMME:
        zeilen.add(new MittelverwendungZeile(status, position, bezeichnung,
            null, einnahme + ausgabe, kommentar));
        break;
    }
  }

}
