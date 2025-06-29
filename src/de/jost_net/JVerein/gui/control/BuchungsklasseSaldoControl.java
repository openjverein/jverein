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
import de.jost_net.JVerein.gui.formatter.SaldoFormatter;
import de.jost_net.JVerein.gui.parts.SaldoListTablePart;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.BuchungsklassesaldoCSV;
import de.jost_net.JVerein.io.BuchungsklassesaldoPDF;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.StatusBuchungsart;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class BuchungsklasseSaldoControl extends AbstractSaldoControl
{
  /**
   * Die Art der Buchung: Einnahme (0), Ausgabe (1), Umbuchung (2)
   */
  protected static final String ARTBUCHUNGSART = "art";

  /**
   * Die Summe, bei optiernenden Vereinen die Nettosumme + Steuern auf der
   * Steuerbuchungsart
   */
  protected static final String SUMME = "summe";

  /**
   * true wenn Steuern verwendet werden sollen. Default wie in Einstellungen
   * "Optiert" gesetzt.
   */
  protected boolean mitSteuer;

  /**
   * Soll "Buchungen ohne Buchungsart" mit ausgegeben werden? Default true
   */
  protected boolean mitOhneBuchungsart = true;

  /**
   * Die Bezeichnung der Gruppen-Spalte: "Buchungsklasse", "Projekt". Default
   * "Buchungsklasse"
   */
  protected String gruppenBezeichnung = "Buchnugsklasse";

  private SaldoListTablePart saldoList;

  /**
   * Sollen Umbuchungen mit augegeben werden? Default true.
   */
  protected boolean mitUmbuchung = true;

  public BuchungsklasseSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    mitSteuer = (Boolean) Einstellungen.getEinstellung(Property.OPTIERT);
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
      saldoList = new SaldoListTablePart(getList(), null)
      {
        // Sortieren verhindern
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn(gruppenBezeichnung, GRUPPE, null,
          false);
      saldoList.addColumn("Buchungsart", BUCHUNGSART);
      saldoList.addColumn("Einnahmen", EINNAHMEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Ausgaben", AUSGABEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      if (mitUmbuchung)
      {
        saldoList.addColumn("Umbuchungen", UMBUCHUNGEN,
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
            Column.ALIGN_RIGHT);
      }
      saldoList.addColumn("Anzahl", ANZAHL);
      saldoList.setMulti(true);
      saldoList.setRememberColWidths(true);
      saldoList.setRememberOrder(true);
      saldoList.setRememberState(true);
      saldoList.addFeature(new FeatureSummary());
      saldoList.setFormatter(new SaldoFormatter());

      return saldoList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
  }

  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    String klasseAlt = null;

    // Summen der Buchungsklasse/Projekt
    Double einnahmenSumme = 0d;
    Double ausgabenSumme = 0d;
    Double umbuchungenSumme = 0d;

    // Summen aller Buchungsklassen/Projekte
    Double einnahmenGesamt = 0d;
    Double ausgabenGesamt = 0d;
    Double umbuchungenGesamt = 0d;

    while (it.hasNext())
    {
      PseudoDBObject o = it.next();

      String klasse = (String) o.getAttribute(BUCHUNGSKLASSE);
      if (klasse == null)
      {
        klasse = "Nicht zugeordnet";
      }
      // Die Art der Buchungsart: Einnahme, Ausgabe, Umbuchung
      Integer art = o.getInteger(ARTBUCHUNGSART);
      Double summe = o.getDouble(SUMME);

      // Wenn es "einnahmen" oder "ausgaben" spalten gibt, nehmen wir die Werte
      // direkt.
      Double einnahmen = o.getDouble(EINNAHMEN);
      Double ausgaben = o.getDouble(AUSGABEN);

      // Vor neuer Klasse Saldo der letzten anzeigen.
      if (!klasse.equals(klasseAlt) && klasseAlt != null)
      {
        PseudoDBObject saldo = new PseudoDBObject();
        saldo.setAttribute(ART, ART_SALDOFOOTER);
        saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
        if (Math.abs(einnahmenSumme) >= 0.01d)
        {
          saldo.setAttribute(EINNAHMEN, einnahmenSumme);
        }
        if (Math.abs(ausgabenSumme) >= 0.01d)
        {
          saldo.setAttribute(AUSGABEN, ausgabenSumme);
        }
        if (Math.abs(umbuchungenSumme) >= 0.01d)
        {
          saldo.setAttribute(UMBUCHUNGEN, umbuchungenSumme);
        }
        zeilen.add(saldo);

        PseudoDBObject saldogv = new PseudoDBObject();
        saldogv.setAttribute(ART, ART_SALDOGEWINNVERLUST);
        saldogv.setAttribute(GRUPPE, "Gewinn/Verlust " + klasseAlt);
        saldogv.setAttribute(EINNAHMEN,
            einnahmenSumme + ausgabenSumme + umbuchungenSumme);
        zeilen.add(saldogv);

        einnahmenSumme = 0d;
        ausgabenSumme = 0d;
        umbuchungenSumme = 0d;
      }

      Double umbuchungen = 0d;
      switch (art)
      {
        case ArtBuchungsart.EINNAHME:
          if (einnahmen == null)
          {
            einnahmen = summe;
            o.setAttribute(EINNAHMEN, einnahmen);
          }
          einnahmenSumme += einnahmen;
          einnahmenGesamt += einnahmen;
          break;
        case ArtBuchungsart.AUSGABE:
          if (ausgaben == null)
          {
            ausgaben = summe;
            o.setAttribute(AUSGABEN, ausgaben);
          }
          ausgabenSumme += ausgaben;
          ausgabenGesamt += ausgaben;
          break;
        case ArtBuchungsart.UMBUCHUNG:
          if (einnahmen == null && ausgaben == null)
          {
            umbuchungen = summe;
            umbuchungenSumme += umbuchungen;
            umbuchungenGesamt += umbuchungen;
            o.setAttribute(UMBUCHUNGEN, umbuchungen);
          }
          else
          {
            einnahmenSumme += einnahmen;
            einnahmenGesamt += einnahmen;
            ausgabenSumme += ausgaben;
            ausgabenGesamt += ausgaben;
          }
          break;
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
      // Die Detailzeile wie sie aus dem iterator kommt azeigen.
      o.setAttribute(ART, ART_DETAIL);

      // Anzahl null blenden wir aus
      if (o.getInteger(ANZAHL).equals(0))
      {
        o.setAttribute(ANZAHL, null);
      }
      zeilen.add(o);
    }

    // Am Ende noch Saldo der letzten Klasse.
    // (Nur wenn auch Buchungsklassen existieren)
    if (klasseAlt != null)
    {
      PseudoDBObject saldo = new PseudoDBObject();
      saldo.setAttribute(ART, ART_SALDOFOOTER);
      saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
      if (Math.abs(einnahmenSumme) >= 0.01d)
      {
        saldo.setAttribute(EINNAHMEN, einnahmenSumme);
      }
      if (Math.abs(ausgabenSumme) >= 0.01d)
      {
        saldo.setAttribute(AUSGABEN, ausgabenSumme);
      }
      if (Math.abs(umbuchungenSumme) >= 0.01d)
      {
        saldo.setAttribute(UMBUCHUNGEN, umbuchungenSumme);
      }
      zeilen.add(saldo);

      PseudoDBObject saldogv = new PseudoDBObject();
      saldogv.setAttribute(ART, ART_SALDOGEWINNVERLUST);
      saldogv.setAttribute(GRUPPE, "Gewinn/Verlust " + klasseAlt);
      saldogv.setAttribute(EINNAHMEN,
          einnahmenSumme + ausgabenSumme + umbuchungenSumme);
      zeilen.add(saldogv);
    }

    PseudoDBObject o = new PseudoDBObject();
    o.setAttribute(ART, ART_LEERZEILE);
    zeilen.add(o);

    PseudoDBObject saldo = new PseudoDBObject();
    saldo.setAttribute(ART, ART_GESAMTSALDOFOOTER);
    saldo.setAttribute(GRUPPE, "Gesamt Saldo");
    saldo.setAttribute(EINNAHMEN, einnahmenGesamt);
    saldo.setAttribute(AUSGABEN, ausgabenGesamt);
    saldo.setAttribute(UMBUCHUNGEN, umbuchungenGesamt);
    zeilen.add(saldo);

    Double summeOhneBuchungsart = 0d;
    if (mitOhneBuchungsart)
    {
      // Ggf. die Anzahl und Summe nicht zugeordneter Buchungen anzeigen.
      // (Geht nicht mit im oberen Query, da MySQL und H2 kein FULL JOIN
      // unterstützen)
      ExtendedDBIterator<PseudoDBObject> ohneBaIt = new ExtendedDBIterator<>(
          "buchung");
      ohneBaIt.addColumn("count(*) AS anzahl");
      ohneBaIt.addColumn("sum(buchung.betrag) AS summe");
      ohneBaIt.addFilter("buchungsart IS NULL");
      ohneBaIt.addFilter("datum >= ?", getDatumvon().getDate());
      ohneBaIt.addFilter("datum <= ?", getDatumbis().getDate());

      PseudoDBObject oAnz = ohneBaIt.next();
      Integer anzahl = oAnz.getAttribute("anzahl") == null ? 0
          : oAnz.getInteger("anzahl");
      summeOhneBuchungsart = oAnz.getAttribute("summe") == null ? 0
          : oAnz.getDouble("summe");
      if (anzahl > 0)
      {
        PseudoDBObject ohneBuchungsart = new PseudoDBObject();
        ohneBuchungsart.setAttribute(ART,
            AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN);
        ohneBuchungsart.setAttribute(GRUPPE,
            "Saldo Buchungen ohne Buchungsart");
        ohneBuchungsart.setAttribute(EINNAHMEN, summeOhneBuchungsart);
        ohneBuchungsart.setAttribute(ANZAHL, anzahl);
        zeilen.add(ohneBuchungsart);
      }
    }

    PseudoDBObject saldogv = new PseudoDBObject();
    saldogv.setAttribute(ART, ART_GESAMTGEWINNVERLUST);
    saldogv.setAttribute(GRUPPE, "Gesamt Gewinn/Verlust");
    saldogv.setAttribute(EINNAHMEN,
        einnahmenGesamt + ausgabenGesamt + umbuchungenGesamt
            + summeOhneBuchungsart);
    zeilen.add(saldogv);

    return zeilen;
  }

  /**
   * Holt den Iterator, auf dessen Basis die Salodliste erstellt wird.
   * 
   * @return der Iterator
   * @throws RemoteException
   */
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    final boolean unterdrueckung = (Boolean) Einstellungen.getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG);

    final boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);

    final boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "buchungsart");
    it.addColumn("buchungsklasse.bezeichnung as " + BUCHUNGSKLASSE);
    it.addColumn("buchungsart.bezeichnung as " + BUCHUNGSART);
    it.addColumn("buchungsart.art as " + ARTBUCHUNGSART);
    it.addColumn("COUNT(buchung.id) as " + ANZAHL);
    it.addColumn("buchungsart.status");

    if (mitSteuer)
    {
      // Nettobetrag berechnen und steuerbetrag der Steuerbuchungsart
      // hinzurechnen
      it.addColumn(
          "COALESCE(SUM(CAST(buchung.betrag * 100 / (100 + "
              // Anlagenkonto immer Bruttobeträge.
              // Alte Steuerbuchungen mit dependencyid lassen wir bestehen ohne
              // Netto zu berehnen.
              + "CASE WHEN konto.kontoart = ? OR buchung.dependencyid > -1 THEN 0 ELSE COALESCE(steuer.satz,0) END"
              + ") AS DECIMAL(10,2))),0) + COALESCE(SUM(st.steuerbetrag),0) AS "
              + SUMME,
          Kontoart.ANLAGE.getKey());
    }
    else
    {
      it.addColumn("COALESCE(SUM(buchung.betrag),0) AS " + SUMME);
    }

    it.leftJoin("buchung",
        "buchung.buchungsart = buchungsart.id AND datum >= ? AND datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());
    it.leftJoin("konto", "buchung.konto = konto.id");
    it.addFilter("konto.kontoart is null OR konto.kontoart < ?",
        Kontoart.LIMIT.getKey());
    if (mitSteuer)
    {
      if (steuerInBuchung)
      {
        it.leftJoin("steuer", "steuer.id = buchung.steuer");
      }
      else
      {
        it.leftJoin("steuer", "steuer.id = buchungsart.steuer");
      }
    }
    if (klasseInBuchung)
    {
      it.leftJoin("buchungsklasse",
          "buchungsklasse.id = buchung.buchungsklasse");
      it.addGroupBy("buchung.buchungsklasse");
    }
    else
    {
      it.leftJoin("buchungsklasse",
          "buchungsklasse.id = buchungsart.buchungsklasse ");
      it.addGroupBy("buchungsart.buchungsklasse");
    }
    it.addGroupBy("buchungsart.id");
    // Ggf. Buchungsarten ausblenden
    if (unterdrueckung)
    {
      it.addHaving("anzahl > 0 OR abs(" + SUMME + ") >= 0.01");
    }
    else
    {
      it.addHaving(
          "anzahl > 0 OR abs(" + SUMME + ") >= 0.01 OR buchungsart.status != ?",
          StatusBuchungsart.INACTIVE);
    }
    it.setOrder(
        "Order by -buchungsklasse.nummer DESC, -buchungsart.nummer DESC ");

    // Für die Steuerbträge auf der Steuerbuchungsart machen wir ein Subselect
    if (mitSteuer)
    {
      String subselect = "(SELECT steuer.buchungsart, "
          + " SUM(CAST(buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2))) AS steuerbetrag, "
          + "buchung.projekt "
          + " FROM buchung"
          // Keine Steuer bei Anlagekonten
          + " JOIN konto on buchung.konto = konto.id and konto.kontoart < ? and konto.kontoart != ?";

      // Wenn die Steuer in der Buchung steht, können wir sie direkt nehmen,
      // sonst müssen wir den Umweg über die Buchungsart nehmen.
      if (steuerInBuchung)
      {
        subselect += " JOIN steuer ON steuer.id = buchung.steuer ";
      }
      else
      {
        subselect += " JOIN buchungsart ON buchung.buchungsart = buchungsart.id "
            + " JOIN steuer ON steuer.id = buchungsart.steuer ";
      }
      subselect += " WHERE datum >= ? and datum <= ? "
          // Keine Steuer bei alten Steuerbuchungen mit dependencyid
          + " AND (buchung.dependencyid is null or  buchung.dependencyid = -1)"
          + " GROUP BY steuer.buchungsart, buchung.projekt) AS st ";
      it.leftJoin(subselect, "st.buchungsart = buchungsart.id ",
          Kontoart.LIMIT.getKey(),
          Kontoart.ANLAGE.getKey(), getDatumvon().getDate(),
          getDatumbis().getDate());
    }
    return it;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return "Buchungsklassen-Saldo";
  }

  @Override
  protected ISaldoExport getAuswertung(String type) throws ApplicationException
  {
    switch (type)
    {
      case AuswertungCSV:
        return new BuchungsklassesaldoCSV(mitUmbuchung);
      case AuswertungPDF:
        return new BuchungsklassesaldoPDF(mitUmbuchung, getAuswertungTitle());
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }
}
