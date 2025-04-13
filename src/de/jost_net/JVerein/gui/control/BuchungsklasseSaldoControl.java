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
import de.jost_net.JVerein.io.BuchungsklassesaldoCSV;
import de.jost_net.JVerein.io.BuchungsklassesaldoPDF;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
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
   * Anzahl Buchungen
   */
  public static final String ANZAHL = "anzahl";

  /**
   * true wenn Steuern verwendet werden sollen
   */
  protected boolean mitSteuer;

  private TablePart saldoList;

  protected boolean mitUmbuchung;

  public BuchungsklasseSaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    mitSteuer = Einstellungen.getEinstellung().getOptiert();
    mitUmbuchung = true;
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
      saldoList.addColumn("Buchungsklasse", GRUPPE, null,
          false);
      saldoList.addColumn("Buchungsart", BUCHUNGSART);
      saldoList.addColumn("Einnahmen", EINNAHMEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Ausgaben", AUSGABEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Umbuchungen", UMBUCHUNGEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Anzahl", ANZAHL);
      saldoList.setRememberColWidths(true);
      saldoList.removeFeature(FeatureSummary.class);
      return saldoList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler aufgetreten %s", e.getMessage()));
    }
  }

  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    String klasseAlt = null;

    // Summen der Buchungsklasse
    Double einnahmenSumme = 0d;
    Double ausgabenSumme = 0d;
    Double umbuchungenSumme = 0d;

    // Summen aller Buchungsklassen
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
      Integer art = ((Number) o.getAttribute(ARTBUCHUNGSART)).intValue();
      Double summe = ((Number) o.getAttribute(SUMME)).doubleValue();

      // Wenn es "einnahmen" oder "ausgaben" spalten gibt, nehmen wir die Werte
      // direkt.
      Double einnahmen = o.getAttribute(EINNAHMEN) == null ? null
          : ((Number) o.getAttribute(EINNAHMEN)).doubleValue();
      Double ausgaben = o.getAttribute(AUSGABEN) == null ? null
          : ((Number) o.getAttribute(AUSGABEN)).doubleValue();

      // Vor neuer Klasse Saldo der letzten anzeigen.
      if (!klasse.equals(klasseAlt) && klasseAlt != null)
      {
        PseudoDBObject saldo = new PseudoDBObject();
        saldo.setAttribute(ART, ART_SALDOFOOTER);
        saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
        saldo.setAttribute(EINNAHMEN, einnahmenSumme);
        saldo.setAttribute(AUSGABEN, ausgabenSumme);
        saldo.setAttribute(UMBUCHUNGEN, umbuchungenSumme);
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
          umbuchungen = summe;
          umbuchungenSumme += umbuchungen;
          umbuchungenGesamt += umbuchungen;
          o.setAttribute(UMBUCHUNGEN, umbuchungen);
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
      zeilen.add(o);
    }

    // Am Ende noch Saldo der letzten Klasse.
    // (Nur wenn auch Buchungsklassen existieren)
    if (klasseAlt != null)
    {
      PseudoDBObject saldo = new PseudoDBObject();
      saldo.setAttribute(ART, ART_SALDOFOOTER);
      saldo.setAttribute(GRUPPE, "Saldo " + klasseAlt);
      saldo.setAttribute(EINNAHMEN, einnahmenSumme);
      saldo.setAttribute(AUSGABEN, ausgabenSumme);
      saldo.setAttribute(UMBUCHUNGEN, umbuchungenSumme);
      zeilen.add(saldo);

      PseudoDBObject saldogv = new PseudoDBObject();
      saldogv.setAttribute(ART, ART_SALDOGEWINNVERLUST);
      saldogv.setAttribute(GRUPPE, "Gewinn/Verlust " + klasseAlt);
      saldogv.setAttribute(EINNAHMEN,
          einnahmenSumme + ausgabenSumme + umbuchungenSumme);
      zeilen.add(saldogv);
    }

    PseudoDBObject saldo = new PseudoDBObject();
    saldo.setAttribute(ART, ART_GESAMTSALDOFOOTER);
    saldo.setAttribute(GRUPPE, "Gesamt Saldo");
    saldo.setAttribute(EINNAHMEN, einnahmenGesamt);
    saldo.setAttribute(AUSGABEN, ausgabenGesamt);
    saldo.setAttribute(UMBUCHUNGEN, umbuchungenGesamt);
    zeilen.add(saldo);

    PseudoDBObject saldogv = new PseudoDBObject();
    saldogv.setAttribute(ART, ART_GESAMTGEWINNVERLUST);
    saldogv.setAttribute(GRUPPE, "Gesamt Gewinn/Verlust");
    saldogv.setAttribute(EINNAHMEN,
        einnahmenGesamt + ausgabenGesamt + umbuchungenGesamt);
    zeilen.add(saldogv);

    // Ggf. die Anzahl nicht zugeordneter Buchungen anzeigen.
    // (Geht nicht mit im oberen Query, da MySQL und H2 kein FULL JOIN
    // unterstützen)
    ExtendedDBIterator<PseudoDBObject> anzahlIt = new ExtendedDBIterator<>(
        "buchung");
    anzahlIt.addColumn("count(*) AS anzahl");
    anzahlIt.addFilter("buchungsart IS NULL");
    anzahlIt.addFilter("datum >= ?", getDatumvon().getDate());
    anzahlIt.addFilter("datum <= ?", getDatumbis().getDate());

    PseudoDBObject oAnz = anzahlIt.next();
    Integer anzahl = oAnz.getAttribute("anzahl") == null ? 0
        : ((Number) oAnz.getAttribute("anzahl")).intValue();
    if (anzahl > 0)
    {
      PseudoDBObject ohneBuchungsart = new PseudoDBObject();
      ohneBuchungsart.setAttribute(ART,
          AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN);
      ohneBuchungsart.setAttribute(GRUPPE, "Anzahl Buchungen ohne Buchungsart");
      ohneBuchungsart.setAttribute(ANZAHL, anzahl);
      zeilen.add(ohneBuchungsart);
    }
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
    final boolean unterdrueckung = Einstellungen.getEinstellung()
        .getUnterdrueckungOhneBuchung();

    final boolean klasseInBuchung = Einstellungen.getEinstellung()
        .getBuchungsklasseInBuchung();

    final boolean steuerInBuchung = Einstellungen.getEinstellung()
        .getSteuerInBuchung();

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "buchungsart");
    it.addColumn("buchungsklasse.bezeichnung as " + BUCHUNGSKLASSE);
    it.addColumn("buchungsart.bezeichnung as " + BUCHUNGSART);
    it.addColumn("buchungsart.art as " + ARTBUCHUNGSART);
    it.addColumn("COUNT(buchung.id) as " + ANZAHL);

    if (mitSteuer)
    {
      // Nettobetrag berechnen und steuerbetrag der Steuerbuchungsart
      // hinzurechnen
      it.addColumn(
          "COALESCE(SUM(CAST(buchung.betrag * 100 / (100 + COALESCE(steuer.satz,0)) AS DECIMAL(10,2))),0)"
              + " + COALESCE(st.steuerbetrag,0) AS " + SUMME);
    }
    else
    {
      it.addColumn("COALESCE(SUM(buchung.betrag),0) AS " + SUMME);
    }

    it.leftJoin("buchung",
        "buchung.buchungsart = buchungsart.id AND datum >= ? AND datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());
    it.leftJoin("konto", "buchung.konto = konto.id and konto.kontoart < ?",
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
      it.addHaving("ABS(" + SUMME + ") >= 0.01");
    }
    it.setOrder(
        "Order by -buchungsklasse.nummer DESC, -buchungsart.nummer DESC ");

    // Für die Steuerbträge auf der Steuerbuchungsart machen wir ein Subselect
    if (mitSteuer)
    {
      String subselect = "(SELECT buchungsart.id, "
          + " SUM(CAST(buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2))) AS steuerbetrag "
          + " FROM buchung"
          + " JOIN konto on buchung.konto = konto.id and konto.kontoart < ? ";

      // Wenn die Steuer in der Buchung steht, können wir sie direkt nehmen,
      // sonst müssen wir den Umweg über die Buchungsart nehmen.
      if (steuerInBuchung)
      {
        subselect += " JOIN steuer ON steuer.id = buchung.steuer ";
      }
      else
      {
        subselect += " JOIN buchungsart AS buchungbuchungsart ON buchung.buchungsart = buchungbuchungsart.id "
            + " JOIN steuer ON steuer.id = buchungbuchungsart.steuer ";
      }
      subselect += " JOIN buchungsart ON steuer.buchungsart = buchungsart.id "
          + " WHERE datum >= ? and datum <= ? "
          + " GROUP BY buchungsart.id) AS st ";
      it.leftJoin(subselect, "st.id = buchungsart.id ", Kontoart.LIMIT.getKey(),
          getDatumvon().getDate(), getDatumbis().getDate());
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
        return new BuchungsklassesaldoPDF(mitUmbuchung);
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }
}
