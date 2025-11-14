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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.SaldoFormatter;
import de.jost_net.JVerein.io.AnlagenverzeichnisCSV;
import de.jost_net.JVerein.io.AnlagenverzeichnisPDF;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.keys.BuchungsartAnzeige;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.KontoImpl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class AnlagenlisteControl extends AbstractSaldoControl
{

  public static final String STARTWERT = "startwert";

  public static final String ZUGANG = "zugang";

  public static final String ABSCHREIBUNG = "abschreibung";

  public static final String ABGANG = "abgang";

  public static final String ENDWERT = "endwert";

  public static final String KONTO = "konto";

  public static final String NUTZUNGSDAUER = "nutzungsdauer";

  public static final String ANSCHAFFUNG_DATUM = "anschaffung";

  public static final String BETRAG = "betrag";

  public static final String AFAART = "afaart";

  private static final String KONTO_ID = "konto_id";

  private TablePart saldoList;

  public AnlagenlisteControl(AbstractView view) throws RemoteException
  {
    super(view);
  }

  @Override
  public TablePart getSaldoList() throws ApplicationException
  {
    try
    {
      if (saldoList != null)
      {
        return saldoList;
      }
      saldoList = new TablePart(getList(), null)
      {
        // Sortieren verhindern
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn("Anlagenart", GRUPPE);
      saldoList.addColumn("Bezeichnung", KONTO);
      saldoList.addColumn("Nutzungsdauer", NUTZUNGSDAUER, null, false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Afa Art", AFAART);
      saldoList.addColumn("Anschaffung", ANSCHAFFUNG_DATUM,
          new DateFormatter(new JVDateFormatTTMMJJJJ()), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Anschaffungskosten", BETRAG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Buchwert Start GJ", STARTWERT,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Zugang", ZUGANG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Abschreibung", ABSCHREIBUNG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Abgang", ABGANG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Buchwert Ende GJ", ENDWERT,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      // Dummy Spalte, damit endwert nicht am rechten Rand klebt
      saldoList.addColumn(" ", " ",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_LEFT);
      saldoList.setRememberColWidths(true);
      saldoList.removeFeature(FeatureSummary.class);
      saldoList.setFormatter(new SaldoFormatter());
      return saldoList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler aufgetreten %s", e.getMessage()));
    }
  }

  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>("konto");

    switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTANZEIGE))
    {
      case BuchungsartAnzeige.NUMMER_BEZEICHNUNG:
        it.addColumn(
            "CONCAT(buchungsart.nummer,' - ',buchungsart.bezeichnung) as "
                + BUCHUNGSART);
        it.addColumn(
            "CONCAT(buchungsklasse.nummer,' - ',buchungsklasse.bezeichnung) as "
                + BUCHUNGSKLASSE);
        it.addColumn(
            "CONCAT(afaart.nummer,' - ',afaart.bezeichnung) as " + AFAART);
        break;
      case BuchungsartAnzeige.BEZEICHNUNG_NUMMER:
        it.addColumn(
            "CONCAT(buchungsart.bezeichnung,' (',buchungsart.nummer,')') as "
                + BUCHUNGSART);
        it.addColumn(
            "CONCAT(buchungsklasse.bezeichnung,' (',buchungsklasse.nummer,')') as "
                + BUCHUNGSKLASSE);
        it.addColumn(
            "CONCAT(afaart.bezeichnung,' (',afaart.nummer,')') as " + AFAART);
        break;
      default:
        it.addColumn("buchungsart.bezeichnung as " + BUCHUNGSART);
        it.addColumn("buchungsklasse.bezeichnung as " + BUCHUNGSKLASSE);
        it.addColumn("afaart.bezeichnung as " + AFAART);
        break;
    }
    switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
    {
      case BuchungsartSort.NACH_NUMMER:
        it.setOrder(
            "Order by -buchungsklasse.nummer DESC, -buchungsart.nummer DESC, konto.anschaffung");
        break;
      case BuchungsartSort.NACH_BEZEICHNUNG:
      default:
        it.setOrder(
            "Order by buchungsklasse.bezeichnung is NULL, buchungsklasse.bezeichnung,"
                + " buchungsart.bezeichnung is NULL, buchungsart.bezeichnung, konto.anschaffung");
        break;
    }
    it.addColumn("konto.bezeichnung AS " + KONTO);
    it.addColumn("konto.id AS " + KONTO_ID);
    it.addColumn("konto.nutzungsdauer AS " + NUTZUNGSDAUER);
    it.addColumn("konto.anschaffung AS " + ANSCHAFFUNG_DATUM);
    it.addColumn("konto.betrag AS " + BETRAG);

    it.addColumn(
        "SUM(case when buchungbuchungsart.abschreibung = TRUE then buchung.betrag ELSE 0 END) AS "
            + ABSCHREIBUNG);
    it.addColumn(
        "SUM(case when buchungbuchungsart.abschreibung = FALSE AND buchung.betrag > 0 then buchung.betrag ELSE 0 END) AS "
            + ZUGANG);
    it.addColumn(
        "SUM(case when buchungbuchungsart.abschreibung = FALSE AND buchung.betrag < 0 then buchung.betrag ELSE 0 END) AS "
            + ABGANG);

    it.leftJoin("buchung",
        "konto.id = buchung.konto AND buchung.datum >= ? AND buchung.datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());
    it.leftJoin("buchungsart as buchungbuchungsart",
        "buchungbuchungsart.id = buchung.buchungsart");

    it.join("buchungsart", "buchungsart.id = konto.anlagenart");
    it.leftJoin("buchungsklasse", "buchungsklasse.id = konto.anlagenklasse");
    it.leftJoin("buchungsart as afaart", "afaart.id = konto.afaart");

    it.addFilter("konto.kontoart = ?", Kontoart.ANLAGE.getKey());
    it.addFilter("konto.eroeffnung is null or konto.eroeffnung <= ?",
        getDatumbis().getDate());
    it.addFilter("konto.aufloesung is null or konto.aufloesung >= ?",
        getDatumvon().getDate());

    it.addGroupBy("konto.id");
    it.addGroupBy("konto.anlagenart");
    it.addGroupBy("konto.anlagenklasse");
    it.addGroupBy("konto.bezeichnung");
    it.addGroupBy("konto.nutzungsdauer");
    it.addGroupBy("konto.anschaffung");
    it.addGroupBy("konto.betrag");
    it.addGroupBy("buchungsart.bezeichnung");
    it.addGroupBy("buchungsart.nummer");
    it.addGroupBy("buchungsklasse.bezeichnung");
    it.addGroupBy("buchungsklasse.nummer");
    it.addGroupBy("afaart.bezeichnung");
    it.addGroupBy("afaart.nummer");

    return it;
  }

  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    String klasseAlt = null;
    String artAlt = null;

    // Summen der Buchungsklasse
    Double summeStartwert = 0d;
    Double summeAbschreibung = 0d;
    Double summeZugang = 0d;
    Double summeAbgang = 0d;
    Double summeEndwert = 0d;

    // Gesamtsummen
    Double gesamtStartwert = 0d;
    Double gesamtAbschreibung = 0d;
    Double gesamtZugang = 0d;
    Double gesamtAbgang = 0d;
    Double gesamtEndwert = 0d;

    while (it.hasNext())
    {
      PseudoDBObject o = it.next();

      String klasse = (String) o.getAttribute(BUCHUNGSKLASSE);
      if (klasse == null)
      {
        klasse = "Nicht zugeordnet";
      }

      // Vor neuer Klasse Saldo der letzten anzeigen.
      if (!klasse.equals(klasseAlt) && klasseAlt != null)
      {
        PseudoDBObject saldo = new PseudoDBObject();
        saldo.setAttribute(ART, ART_SALDOFOOTER);
        saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
        saldo.setAttribute(EINNAHMEN, summeStartwert);
        saldo.setAttribute(AUSGABEN, summeZugang);
        saldo.setAttribute(ABSCHREIBUNG, summeAbschreibung);
        saldo.setAttribute(ABGANG, summeAbgang);
        saldo.setAttribute(ENDWERT, summeEndwert);
        zeilen.add(saldo);

        summeStartwert = 0d;
        summeAbschreibung = 0d;
        summeZugang = 0d;
        summeAbgang = 0d;
        summeEndwert = 0d;
      }

      String buchungsart = (String) o.getAttribute(BUCHUNGSART);
      Integer konto = o.getInteger(KONTO_ID);

      Double zugang = o.getDouble(ZUGANG);
      Double abschreibung = o.getDouble(ABSCHREIBUNG);
      Double abgang = o.getDouble(ABGANG);

      Double startwert = KontoImpl.getSaldo(konto, getDatumvon().getDate());
      Double endwert = null;

      // Summen Berechnen

      if (startwert != null)
      {
        gesamtStartwert += startwert;
        summeStartwert += startwert;
        endwert = startwert + zugang + abschreibung + abgang;
      }

      summeAbschreibung += abschreibung;
      gesamtAbschreibung += abschreibung;
      summeZugang += zugang;
      gesamtZugang += zugang;
      summeAbgang += abgang;
      gesamtAbgang += abgang;

      if (endwert != null)
      {
        summeEndwert += endwert;
        gesamtEndwert += endwert;
      }

      // Bei neuer Klasse Kopfzeile anzeigen.
      if (!klasse.equals(klasseAlt))
      {
        PseudoDBObject head = new PseudoDBObject();
        head.setAttribute(ART, ART_HEADER);
        head.setAttribute(GRUPPE, klasse);
        zeilen.add(head);
        klasseAlt = klasse;
      }
      // Bei neuer Buchungsart Kopfzeile anzeigen.
      if (!buchungsart.equals(artAlt))
      {
        PseudoDBObject head = new PseudoDBObject();
        head.setAttribute(ART, ART_HEADER);
        head.setAttribute(GRUPPE, buchungsart);
        zeilen.add(head);
        artAlt = buchungsart;
      }

      // Die Detailzeile wie sie aus dem iterator kommt azeigen.
      o.setAttribute(ART, ART_DETAIL);
      o.setAttribute(STARTWERT, startwert);
      o.setAttribute(ENDWERT, endwert);
      zeilen.add(o);
    }

    // Am Ende noch Saldo der letzten Klasse.
    // (Nur wenn auch Buchungsklassen existieren)
    if (klasseAlt != null)
    {
      PseudoDBObject saldo = new PseudoDBObject();
      saldo.setAttribute(ART, ART_SALDOFOOTER);
      saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
      saldo.setAttribute(STARTWERT, summeStartwert);
      saldo.setAttribute(ZUGANG, summeZugang);
      saldo.setAttribute(ABSCHREIBUNG, summeAbschreibung);
      saldo.setAttribute(ABGANG, summeAbgang);
      saldo.setAttribute(ENDWERT, summeEndwert);
      zeilen.add(saldo);
    }

    PseudoDBObject saldo = new PseudoDBObject();
    saldo.setAttribute(ART, ART_GESAMTSALDOFOOTER);
    saldo.setAttribute(GRUPPE, "Gesamt Saldo");
    saldo.setAttribute(STARTWERT, gesamtStartwert);
    saldo.setAttribute(ZUGANG, gesamtZugang);
    saldo.setAttribute(ABSCHREIBUNG, gesamtAbschreibung);
    saldo.setAttribute(ABGANG, gesamtAbgang);
    saldo.setAttribute(ENDWERT, gesamtEndwert);
    zeilen.add(saldo);

    return zeilen;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return VorlageUtil.getName(VorlageTyp.ANLAGENVERZEICHNIS_TITEL, this);
  }

  @Override
  protected String getAuswertungSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.ANLAGENVERZEICHNIS_SUBTITEL, this);
  }

  @Override
  protected String getDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.ANLAGENVERZEICHNIS_DATEINAME, this);
  }

  @Override
  protected ISaldoExport getAuswertung(String type) throws ApplicationException
  {
    switch (type)
    {
      case AuswertungCSV:
        return new AnlagenverzeichnisCSV();
      case AuswertungPDF:
        return new AnlagenverzeichnisPDF();
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }

}
