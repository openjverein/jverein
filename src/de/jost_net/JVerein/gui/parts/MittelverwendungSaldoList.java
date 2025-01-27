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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.MittelverwendungZeile;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.Kontoart;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class MittelverwendungSaldoList extends MittelverwendungList
{

  private TablePart saldoList;

  public MittelverwendungSaldoList(Date datumvon, Date datumbis)
  {
    this.datumvon = datumvon;
    this.datumbis = datumbis;
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
        saldoList.addColumn("Art", "art");
        saldoList.addColumn("Konto", "bezeichnung");
        saldoList.addColumn("Betrag", "betrag",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        saldoList.addColumn("Summe", "summe",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
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

  public ArrayList<MittelverwendungZeile> getInfo() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    String sql;
    ArrayList<MittelverwendungZeile> zeilen = new ArrayList<>();
    Double summeVermoegen = 0.0;
    Double summeFreieMittel = 0.0;
    String bezeichnung = "";
    boolean nichtUnterdruecken = !Einstellungen.getEinstellung()
        .getUnterdrueckungOhneBuchung();
    // Überschrift
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null, null,
        null, null, BLANK, "Liste an Vermögen:"));
    // Anlagevermögen
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null, null,
        null, null, BLANK, "Anlagevermögen"));
    if (Einstellungen.getEinstellung().getSummenAnlagenkonto())
    {
      sql = getAnfangsbestandKontoartSql();
      Double anlagenStand = (Double) service.execute(sql, new Object[] {
          datumvon, Kontoart.ANLAGE.getKey(), datumvon, datumbis }, rsd);
      sql = getSummenBetragKontoartSql();
      anlagenStand += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey() }, rsd);
      if (Math.abs(anlagenStand) > LIMIT || nichtUnterdruecken)
      {
        bezeichnung = "Summe Anlagevermögen";
        addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung,
            anlagenStand, 0.0, BLANK);
        summeVermoegen += anlagenStand;
      }
      else
      {
        zeilen.remove(zeilen.size() - 1);
      }
    }
    else
    {
      sql = getAktiveKontenMapSql();
      @SuppressWarnings("unchecked")
      HashMap<Long, String[]> map0 = (HashMap<Long, String[]>) service.execute(
          sql, new Object[] { Kontoart.ANLAGE.getKey(), datumvon, datumbis },
          rsmapa);
      for (Long kontoId : map0.keySet())
      {
        sql = getAnfangsbestandKontoSql();
        Double kontoStand = (Double) service.execute(sql,
            new Object[] { datumvon, kontoId }, rsd);
        sql = getSummenBetragKontoSql();
        kontoStand += (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, kontoId }, rsd);
        if (Math.abs(kontoStand) > LIMIT || nichtUnterdruecken)
        {
          String kommentar = map0.get(kontoId)[1];
          if (kommentar != null && !kommentar.isEmpty())
          {
            kommentar = kommentar.split("\n")[0];
          }
          addZeile(zeilen, MittelverwendungZeile.EINNAHME, null,
              map0.get(kontoId)[0], kontoStand, null, kommentar);
          summeVermoegen += kontoStand;
        }
      }
      if (Math.abs(summeVermoegen) > LIMIT || nichtUnterdruecken)
      {
        bezeichnung = "Summe Anlagevermögen";
        addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung,
            summeVermoegen, 0.0, BLANK);
      }
      else
      {
        zeilen.remove(zeilen.size() - 1);
      }
    }

    // Geldkonten
    Double summeGeld = 0.0;
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null, null,
        null, null, BLANK, "Geldvermögen"));
    sql = getAktiveKontenMapSql();
    @SuppressWarnings("unchecked")
    HashMap<Long, String[]> map1 = (HashMap<Long, String[]>) service.execute(
        sql, new Object[] { Kontoart.GELD.getKey(), datumvon, datumbis },
        rsmapa);
    for (Long kontoId : map1.keySet())
    {
      sql = getAnfangsbestandKontoSql();
      Double kontoStand = (Double) service.execute(sql,
          new Object[] { datumvon, kontoId }, rsd);
      sql = getSummenBetragKontoSql();
      kontoStand += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, kontoId }, rsd);
      if (Math.abs(kontoStand) > LIMIT || nichtUnterdruecken)
      {
        String kommentar = map1.get(kontoId)[1];
        if (kommentar != null && !kommentar.isEmpty())
        {
          kommentar = kommentar.split("\n")[0];
        }
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, null,
            map1.get(kontoId)[0], kontoStand, null, kommentar);
        summeGeld += kontoStand;
      }
    }
    if (Math.abs(summeGeld) > LIMIT || nichtUnterdruecken)
    {
      bezeichnung = "Summe Geldvermögen";
      addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, summeGeld,
          0.0, BLANK);
      summeVermoegen += summeGeld;
    }
    else
    {
      zeilen.remove(zeilen.size() - 1);
    }

    // Fremdkapital
    Double summeSchulden = 0.0;
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null, null,
        null, null, BLANK, "Fremdkapital"));
    sql = getAktiveKontenMapSql();
    @SuppressWarnings("unchecked")
    HashMap<Long, String[]> map2 = (HashMap<Long, String[]>) service.execute(
        sql, new Object[] { Kontoart.SCHULDEN.getKey(), datumvon, datumbis },
        rsmapa);
    for (Long kontoId : map2.keySet())
    {
      sql = getAnfangsbestandKontoSql();
      Double kontoStand = (Double) service.execute(sql,
          new Object[] { datumvon, kontoId }, rsd);
      sql = getSummenBetragKontoSql();
      kontoStand += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, kontoId }, rsd);
      if (Math.abs(kontoStand) > LIMIT || nichtUnterdruecken)
      {
        String kommentar = map2.get(kontoId)[1];
        if (kommentar != null && !kommentar.isEmpty())
        {
          kommentar = kommentar.split("\n")[0];
        }
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, null,
            map2.get(kontoId)[0], kontoStand, null, kommentar);
        summeSchulden += kontoStand;
      }
    }
    if (Math.abs(summeSchulden) > LIMIT || nichtUnterdruecken)
    {
      bezeichnung = "Summe Fremdkapital";
      addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung,
          summeSchulden, 0.0, BLANK);
      summeVermoegen += summeSchulden;
    }
    else
    {
      zeilen.remove(zeilen.size() - 1);
    }

    bezeichnung = "Gesamtvermögen";
    addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung,
        summeVermoegen, 0.0, BLANK);
    // Leerzeile
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.LEERZEILE, null,
        null, null, null, BLANK));

    // Mittelverwendung
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null, null,
        null, null, BLANK,
        "Liste der zeitnahen Verwendung entzogenen Mittel:"));
    // Nutzungsgebundenes Anlagevermögen
    sql = getAnfangsbestandKontoartZweckSql();
    Double anlagenStand = (Double) service
        .execute(sql,
            new Object[] { datumvon, Kontoart.ANLAGE.getKey(),
                Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(), datumvon, datumbis },
            rsd);
    sql = getSummenBetragKontoartZweckSql();
    anlagenStand += (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
            Anlagenzweck.NUTZUNGSGEBUNDEN.getKey() },
        rsd);
    if (Math.abs(anlagenStand) > LIMIT || nichtUnterdruecken)
    {
      bezeichnung = "Nutzungsgebundenes Anlagevermögen";
      addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, 0.0,
          anlagenStand, BLANK);
      summeFreieMittel = anlagenStand;
    }
    // Fremdkapital
    if (Math.abs(summeSchulden) > LIMIT || nichtUnterdruecken)
    {
      bezeichnung = "Fremdkapital";
      addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, 0.0,
          summeSchulden, BLANK);
      summeFreieMittel += summeSchulden;
    }
    // Rücklagen, Vermögen nicht zugeordnet
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null, null,
        null, null, BLANK, "Nicht zugeordnete Rücklagen"));
    Double summeRuecklagen = 0.0;
    sql = getAktiveKontenRangeMapSql() + " AND konto.anlagenklasse IS NULL";
    @SuppressWarnings("unchecked")
    HashMap<Long, String[]> map3 = (HashMap<Long, String[]>) service
        .execute(sql,
            new Object[] { Kontoart.RUECKLAGE_ZWECK_GEBUNDEN.getKey(),
                Kontoart.RUECKLAGE_SONSTIG.getKey(), datumvon, datumbis },
            rsmapa);
    for (Long kontoId : map3.keySet())
    {
      sql = getAnfangsbestandKontoSql();
      Double ruecklagen = (Double) service.execute(sql,
          new Object[] { datumvon, kontoId }, rsd);
      sql = getSummenBetragKontoSql();
      ruecklagen += (Double) service.execute(sql,
          new Object[] { datumvon, datumbis, kontoId }, rsd);
      if (Math.abs(ruecklagen) > LIMIT || nichtUnterdruecken)
      {
        String kommentar = map3.get(kontoId)[1];
        if (kommentar != null && !kommentar.isEmpty())
        {
          kommentar = kommentar.split("\n")[0];
        }
        addZeile(zeilen, MittelverwendungZeile.AUSGABE, null,
            map3.get(kontoId)[0], null, ruecklagen, kommentar);
        summeRuecklagen += ruecklagen;
      }
    }
    if (Math.abs(summeRuecklagen) > LIMIT || nichtUnterdruecken)
    {
      bezeichnung = "Summe nicht zugeordneter Rücklagen";
      addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, 0.0,
          summeRuecklagen, BLANK);
      summeFreieMittel += summeRuecklagen;
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
      zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.ART, null,
          null, null, null, BLANK, buchungsklassen.get(buchungsklasseId)));
      summeRuecklagen = 0.0;
      sql = getAktiveKontenRangeMapSql() + " AND konto.anlagenklasse = ?";
      @SuppressWarnings("unchecked")
      HashMap<Long, String[]> map4 = (HashMap<Long, String[]>) service.execute(
          sql,
          new Object[] { Kontoart.RUECKLAGE_ZWECK_GEBUNDEN.getKey(),
              Kontoart.RUECKLAGE_SONSTIG.getKey(), datumvon, datumbis,
              buchungsklasseId },
          rsmapa);
      for (Long kontoId : map4.keySet())
      {
        sql = getAnfangsbestandKontoSql();
        Double ruecklagen = (Double) service.execute(sql,
            new Object[] { datumvon, kontoId }, rsd);
        sql = getSummenBetragKontoSql();
        ruecklagen += (Double) service.execute(sql,
            new Object[] { datumvon, datumbis, kontoId }, rsd);
        if (Math.abs(ruecklagen) > LIMIT || nichtUnterdruecken)
        {
          String kommentar = map4.get(kontoId)[1];
          if (kommentar != null && !kommentar.isEmpty())
          {
            kommentar = kommentar.split("\n")[0];
          }
          addZeile(zeilen, MittelverwendungZeile.AUSGABE, null,
              map4.get(kontoId)[0], null, ruecklagen, kommentar);
          summeRuecklagen += ruecklagen;
        }
      }
      if (Math.abs(summeRuecklagen) > LIMIT || nichtUnterdruecken)
      {
        bezeichnung = "Summe Rücklagen/Vermögen "
            + buchungsklassen.get(buchungsklasseId);
        addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, 0.0,
            summeRuecklagen, BLANK);
        summeFreieMittel += summeRuecklagen;
      }
      else
      {
        zeilen.remove(zeilen.size() - 1);
      }
    }

    bezeichnung = "Summe der zeitnahen Verwendung entzogenen Mittel";
    addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, 0.0,
        summeFreieMittel, BLANK);

    // Leerzeile
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.LEERZEILE, null,
        null, null, null, BLANK));
    bezeichnung = "Verwendungsrückstand(+)/-überhang(-) zum Ende des GJ";
    addZeile(zeilen, MittelverwendungZeile.ART, null, bezeichnung, 0.0,
        summeVermoegen - summeFreieMittel, BLANK);
    // Leerzeile undefined - nicht drucken in PDF und CSV
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, null,
        null, null, null, BLANK));
    return zeilen;
  }

  // Parameter Kontoart, datumvon, datumbis
  private String getAktiveKontenMapSql() throws RemoteException
  {
    return "SELECT id, bezeichnung, kommentar FROM konto WHERE konto.kontoart = ?"
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)"
        + " AND (konto.eroeffnung IS NULL OR konto.eroeffnung <= ?)";
  }

  // Parameter Kontoart von, Kontoart bis, datumvon, datumbis
  private String getAktiveKontenRangeMapSql() throws RemoteException
  {
    return "SELECT id, bezeichnung, kommentar FROM konto WHERE konto.kontoart >= ?"
        + " AND konto.kontoart <= ?"
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)"
        + " AND (konto.eroeffnung IS NULL OR konto.eroeffnung <= ?)";
  }

  private String getAnfangsbestandKontoartSql() throws RemoteException
  {
    return "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? "
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)"
        + " AND (konto.eroeffnung IS NULL OR konto.eroeffnung <= ?)";
  }

  private String getAnfangsbestandKontoartZweckSql() throws RemoteException
  {
    return "SELECT SUM(anfangsbestand.betrag) FROM anfangsbestand, konto"
        + " WHERE anfangsbestand.datum = ?"
        + " AND anfangsbestand.konto = konto.id " + " AND konto.kontoart = ? "
        + " AND konto.zweck = ?"
        + " AND (konto.aufloesung IS NULL OR konto.aufloesung >= ?)"
        + " AND (konto.eroeffnung IS NULL OR konto.eroeffnung <= ?)";
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
        + " AND anfangsbestand.konto = konto.id ";
  }

  private String getSummenBetragKontoSql() throws RemoteException
  {
    return "SELECT sum(buchung.betrag) FROM buchung"
        + " WHERE datum >= ? AND datum <= ?" + " AND buchung.konto = ?";
  }

}
