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
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.MittelverwendungZeile;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class MittelverwendungFlowList extends MittelverwendungList
{

  private TablePart flowList;

  private int aktuellesGJ;

  private int letztesGJ;

  private int vorletztesGJ;

  private Date endeLetztesGJ;

  private Double zwanghafteWeitergabeNeu;

  private Double rueckstandVorjahrNeu;

  public MittelverwendungFlowList(Date datumvon, Date datumbis)
  {
    this.datumvon = datumvon;
    this.datumbis = datumbis;
    updateDatum();
  }

  private void updateDatum()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(datumvon);
    aktuellesGJ = cal.get(Calendar.YEAR);
    letztesGJ = aktuellesGJ - 1;
    vorletztesGJ = aktuellesGJ - 2;
    cal.add(Calendar.DAY_OF_MONTH, -1);
    endeLetztesGJ = cal.getTime();
  }

  public Part getFlowList() throws ApplicationException
  {
    ArrayList<MittelverwendungZeile> zeilen = null;
    try
    {
      zeilen = getInfo();

      if (flowList == null)
      {
        flowList = new TablePart(zeilen, null)
        {
          @Override
          protected void orderBy(int index)
          {
            return;
          }
        };
        flowList.addColumn("Nr", "position");
        flowList.addColumn("Mittel", "bezeichnung");
        flowList.addColumn("Betrag", "betrag",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        flowList.addColumn("Summe", "summe",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
        // Dummy Spalte, damit Summe nicht am rechten Rand klebt
        flowList.addColumn(" ", " ",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_LEFT);
        flowList.setRememberColWidths(true);
        flowList.setRememberOrder(true);
        flowList.removeFeature(FeatureSummary.class);
      }
      else
      {
        flowList.removeAll();
        for (MittelverwendungZeile sz : zeilen)
        {
          flowList.addItem(sz);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten" + e.getMessage());
    }
    return flowList;
  }

  public ArrayList<MittelverwendungZeile> getInfo() throws RemoteException
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

    bezeichnung = "Verwendungsrückstand(+)/-überhang(-) am Ende des letzten GJ "
        + letztesGJ;
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        vorhandeneMittel, null, BLANK);

    // Der in dem Rückstand enthaltene Rückstand aus dem vorletzten Jahr
    Double rueckstandVorVorjahr = null;
    Double zwanghafteWeitergabeVorjahr = null;
    DBIterator<Jahresabschluss> jahresabschluesse = service
        .createList(Jahresabschluss.class);
    jahresabschluesse.addFilter("bis = ?", endeLetztesGJ);
    if (jahresabschluesse != null && jahresabschluesse.hasNext())
    {
      Jahresabschluss abschluss = jahresabschluesse.next();
      rueckstandVorVorjahr = abschluss.getVerwendungsrueckstand();
      zwanghafteWeitergabeVorjahr = abschluss.getZwanghafteWeitergabe();
    }
    bezeichnung = BLANKS
        + "- Darin enthaltener Rest des Verwendungsrückstand aus dem vorletzten GJ "
        + vorletztesGJ;
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        rueckstandVorVorjahr, null, BLANK);
    bezeichnung = BLANKS
        + "- Darin enthaltene zwanghafte satzungsgemäße Weitergabe von Mitteln";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        zwanghafteWeitergabeVorjahr, null, BLANK);

    // Schritt 2: Mittel Zufluss
    // Summe aller Zuflüsse bei Geldkonten und Anlagen (=Sachspenden)
    sql = getSummenKontoSql();
    Double zufuehrung = (Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.GELD.getKey(), ArtBuchungsart.EINNAHME }, rsd);
    // Summe aller Abflüsse bei nicht nutzungsgebundenen Anlagen
    sql = getSummenKontoZweckSql();
    zufuehrung += (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
            Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(),
            ArtBuchungsart.EINNAHME },
        rsd);
    // Summe Zuflüsse durch Umbuchung
    // Auszahlung aus Fremdkapital z.B. Darlehen,
    // Rückbuchung von zweckgebundenen Anlagen
    sql = getSummenUmbuchungSql() + " AND buchung.betrag < 0";
    zufuehrung -= (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
            Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(),
            ArtBuchungsart.UMBUCHUNG },
        rsd);

    bezeichnung = "Insgesamt im aktuellen GJ zugeflossene Mittel";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        zufuehrung, null, BLANK);
    bezeichnung = BLANKS
        + "Zu verwendende Mittel im aktuellen GJ und nächstem GJ";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung, vorhandeneMittel, BLANK);

    // Leerzeile
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.LEERZEILE, null,
        null, null, null, BLANK));

    // Schritt 3: Mittel Abfluss
    // Summe aller Abflüsse bei Geldkonten
    sql = getSummenKontoSql();
    Double verwendung = -(Double) service.execute(sql, new Object[] { datumvon,
        datumbis, Kontoart.GELD.getKey(), ArtBuchungsart.AUSGABE }, rsd);
    // Summe aller Abflüsse bei nicht nutzungsgebundenen Anlagen
    sql = getSummenKontoZweckSql();
    verwendung -= (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.ANLAGE.getKey(),
            Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(),
            ArtBuchungsart.AUSGABE },
        rsd);
    // Summe der Abflüsse bei Umbuchung
    // Tilgung Verbindlichkeiten z.B. Darlehen,
    // Erwerb zweckgebundener Anlagen
    sql = getSummenUmbuchungSql() + " AND buchung.betrag > 0";
    verwendung += (Double) service.execute(sql,
        new Object[] { datumvon, datumbis, Kontoart.SCHULDEN.getKey(),
            Kontoart.ANLAGE.getKey(), Anlagenzweck.NUTZUNGSGEBUNDEN.getKey(),
            ArtBuchungsart.UMBUCHUNG },
        rsd);

    bezeichnung = "Im aktuellen GJ verwendete Mittel";
    addZeile(zeilen, MittelverwendungZeile.AUSGABE, pos++, bezeichnung, null,
        verwendung, BLANK);

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
            null, zuRuecklagen, BLANK);
      }
      Double entRuecklagen = -(Double) service.execute(sql,
          new Object[] { datumvon, datumbis, i, ArtBuchungsart.AUSGABE }, rsd);
      summeEntRuecklagen += entRuecklagen;
      if (Math.abs(entRuecklagen) > LIMIT
          || !Einstellungen.getEinstellung().getUnterdrueckungOhneBuchung())
      {
        bezeichnung = "Entnahme " + Kontoart.getByKey(i).getText();
        addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
            -entRuecklagen, null, BLANK);
      }
    }

    bezeichnung = BLANKS
        + "Insgesamt im aktuellen GJ verwendete Mittel";
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        verwendung + summeZuRuecklagen, -summeEntRuecklagen, BLANK);

    // Leerzeile
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.LEERZEILE, null,
        null, null, null, BLANK));

    bezeichnung = "Verwendungsrückstand(+)/-überhang(-) zum Ende des aktuellen GJ "
        + aktuellesGJ;
    addZeile(zeilen, MittelverwendungZeile.SUMME, pos++, bezeichnung,
        zufuehrung + vorhandeneMittel - verwendung - summeZuRuecklagen,
        summeEntRuecklagen, BLANK);

    // Berechnung der Mittelverwendung
    rueckstandVorVorjahr = (rueckstandVorVorjahr == null) ? 0.0
        : rueckstandVorVorjahr;
    zwanghafteWeitergabeVorjahr = (zwanghafteWeitergabeVorjahr == null) ? 0.0
        : zwanghafteWeitergabeVorjahr;
    Double ausgaben = Math.max(verwendung - summeEntRuecklagen, 0);
    Double rueckstandVorjahr = vorhandeneMittel - rueckstandVorVorjahr
        - zwanghafteWeitergabeVorjahr;
    zwanghafteWeitergabeNeu = 0.0;
    rueckstandVorjahrNeu = 0.0; // Rest aus Rückstand Vorjahr
    // Der Rückstand aus dem vorletzten Jahr muss ganz aufgebraucht werden,
    // ansonsten unterliegt der Restbetrag der zwanghaften satzungsgemäßen
    // Weitergabe von Mitteln

    if (rueckstandVorVorjahr > ausgaben)
    {
      zwanghafteWeitergabeNeu = rueckstandVorVorjahr - ausgaben;
      rueckstandVorjahrNeu = rueckstandVorjahr;
    }
    else
    {
      rueckstandVorjahrNeu = Math
          .max(vorhandeneMittel - ausgaben - zwanghafteWeitergabeVorjahr, 0);
    }
    bezeichnung = BLANKS
        + "- Darin enthaltener Verwendungsrückstand aus dem letzten GJ "
        + letztesGJ;
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        rueckstandVorjahrNeu, null, BLANK);
    bezeichnung = BLANKS
        + "- Darin enthaltene zwanghafte satzungsgemäße Weitergabe von Mitteln";
    addZeile(zeilen, MittelverwendungZeile.EINNAHME, pos++, bezeichnung,
        zwanghafteWeitergabeNeu, null, BLANK);

    // Leerzeile am Ende wegen Scrollbar
    zeilen.add(new MittelverwendungZeile(MittelverwendungZeile.UNDEFINED, null,
        null, null, null, BLANK));
    return zeilen;
  }

  public void setDatumvon(Date datumvon)
  {
    this.datumvon = datumvon;
    updateDatum();
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

  public Double getZwanghafteWeitergabeNeu()
  {
    return zwanghafteWeitergabeNeu;
  }

  public Double getRueckstandVorjahrNeu()
  {
    return rueckstandVorjahrNeu;
  }
}
