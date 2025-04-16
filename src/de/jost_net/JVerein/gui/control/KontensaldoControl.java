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
import de.jost_net.JVerein.gui.parts.KontensaldoListTablePart;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.io.KontenSaldoCSV;
import de.jost_net.JVerein.io.KontenSaldoPDF;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.KontoImpl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class KontensaldoControl extends AbstractSaldoControl
{

  public static final String KONTOART = "kontoart";

  public static final String KONTO_ID = "konto_id";

  public static final String OHNE_BUCHUNGSART = "ohne_buchungsart";

  public static final String BEMERKUNG = "bemerkung";

  public static final String ANFANGSBESTAND = "anfangsbestand";

  public static final String ENDBESTAND = "endbestand";

  public static final String KONTO_NUMMER = "konto_nummer";

  private KontensaldoListTablePart saldoList;

  protected boolean summensaldo;

  public KontensaldoControl(AbstractView view) throws RemoteException
  {
    super(view);
    summensaldo = Einstellungen.getEinstellung().getSummenAnlagenkonto();
  }

  public TablePart getSaldoList() throws ApplicationException
  {
    try
    {
      if (saldoList != null)
      {
        return saldoList;
      }
      saldoList = new KontensaldoListTablePart(getList(), null)
      {
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn("Kontonummer", KONTO_NUMMER, null, false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Bezeichnung", GRUPPE);
      saldoList.addColumn("Anfangsbestand", ANFANGSBESTAND,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Einnahmen", EINNAHMEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Ausgaben", AUSGABEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Umbuchungen", UMBUCHUNGEN,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Endbestand", ENDBESTAND,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Bemerkung", BEMERKUNG);
      saldoList.setRememberColWidths(true);
      saldoList.setMulti(true);
      saldoList.addFeature(new FeatureSummary());
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
    return saldoList;
  }

  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>("konto");
    boolean mitSteuer = Einstellungen.getEinstellung().getOptiert();
    if (mitSteuer)
    {
      // Bei Umbuchungen vom Geldkonto den Steueranteil nicht bei den
      // Umbuchungen sondern bei den Einnahmen/Ausgaben aufführen.
      // Alte Steuerbuchungen mit dependencyid berücksichtigen wir dabei nicht
      it.addColumn(
          "sum(case when buchungsart.art = ? then buchung.betrag else 0 end "
              + "- case when (dependencyid is null or dependencyid = -1)"
              + " and konto.kontoart = ? and buchungsart.art = ? then "
              + "CAST(buchung.betrag * COALESCE(steuer.satz,0) / (100 + COALESCE(steuer.satz,0))"
              + " AS DECIMAL(10,2)) ELSE 0 END) as "
              + UMBUCHUNGEN,
          ArtBuchungsart.UMBUCHUNG, Kontoart.GELD.getKey(),
          ArtBuchungsart.UMBUCHUNG);

      it.addColumn(
          "sum(case when buchungsart.art = ? then buchung.betrag else 0 end "
              + "+ case when (dependencyid is null or dependencyid = -1)"
              + " and konto.kontoart = ? and buchungsart.art = ? and buchung.betrag > 0 then "
              + "CAST(buchung.betrag * COALESCE(steuer.satz,0) / (100 + COALESCE(steuer.satz,0))"
              + " AS DECIMAL(10,2)) ELSE 0 END) as "
              + EINNAHMEN,
          ArtBuchungsart.EINNAHME, Kontoart.GELD.getKey(),
          ArtBuchungsart.UMBUCHUNG);

      it.addColumn(
          "sum(case when buchungsart.art = ? then buchung.betrag else 0 end"
              + "+ case when (dependencyid is null or dependencyid = -1)"
              + " and konto.kontoart = ? and buchungsart.art = ? and buchung.betrag < 0 then "
              + "CAST(buchung.betrag * COALESCE(steuer.satz,0) / (100 + COALESCE(steuer.satz,0))"
              + " AS DECIMAL(10,2)) ELSE 0 END) as "
              + AUSGABEN,
          ArtBuchungsart.AUSGABE, Kontoart.GELD.getKey(),
          ArtBuchungsart.UMBUCHUNG);
    }
    else
    {
      it.addColumn(
          "sum(case when buchungsart.art = ? then buchung.betrag else 0 end) as "
              + UMBUCHUNGEN,
          ArtBuchungsart.UMBUCHUNG);
      it.addColumn(
          "sum(case when buchungsart.art = ? then buchung.betrag else 0 end) as "
              + EINNAHMEN,
          ArtBuchungsart.EINNAHME);
      it.addColumn(
          "sum(case when buchungsart.art = ? then buchung.betrag else 0 end) as "
              + AUSGABEN,
          ArtBuchungsart.AUSGABE);
    }
    it.addColumn(
        "sum(case when buchungsart.art is null then buchung.betrag else 0 end) as "
            + OHNE_BUCHUNGSART);

    it.addColumn("konto.kontoart AS " + KONTOART);
    it.addColumn("konto.id as " + KONTO_ID);
    it.addColumn("konto.bezeichnung as " + GRUPPE);
    it.addColumn("konto.nummer as " + KONTO_NUMMER);

    it.leftJoin("buchung",
        "buchung.konto = konto.id AND buchung.datum >= ? AND buchung.datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());
    it.leftJoin("buchungsart", "buchung.buchungsart = buchungsart.id");

    if (mitSteuer)
    {
      if (Einstellungen.getEinstellung().getSteuerInBuchung())
      {
        it.leftJoin("steuer", "steuer.id = buchung.steuer");
      }
      else
      {
        it.leftJoin("steuer", "steuer.id = buchungsart.steuer");
      }
    }

    // Nur aktive Konten
    it.addFilter("(konto.aufloesung is null or konto.aufloesung >= ?)",
        getDatumvon().getDate());
    it.addFilter("(konto.eroeffnung is null or konto.eroeffnung <= ?)",
        getDatumbis().getDate());

    it.addGroupBy("konto.id");
    it.addGroupBy("konto.kontoart");

    it.setOrder("ORDER BY konto.bezeichnung");

    return it;
  }

  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();
    ArrayList<PseudoDBObject> zeilenUeberLimit = new ArrayList<>();

    // Die Summen aller Konten
    double summeAnfangsbestand = 0;
    double summeEinnahmen = 0;
    double summeAusgaben = 0;
    double summeUmbuchungen = 0;
    double summeEndbestand = 0;
    double jahressaldo = 0;

    // Summen der Anlagenkonten, falls sie gesammelt ausgegeben werden sollen.
    double summenAnlageAnfangsbestand = 0;
    double summenAnlageEinnahmen = 0;
    double summenAnlageAusgaben = 0;
    double summenAnlageUmbuchungen = 0;
    double summenAnlageEndbestand = 0;

    while (it.hasNext())
    {
      PseudoDBObject o = it.next();
      Integer kontoart = o.getInteger(KONTOART);
      Integer konto = o.getInteger(KONTO_ID);

      Double anfangsbestand = KontoImpl.getSaldo(konto,
          getDatumvon().getDate());

      // Wenn kein Anfangsbestand vorhanden ist, geben wir nur die Bemerkung aus
      if (anfangsbestand == null)
      {
        o.setAttribute(EINNAHMEN, "");
        o.setAttribute(AUSGABEN, "");
        o.setAttribute(UMBUCHUNGEN, "");
        o.setAttribute(BEMERKUNG, "Kein Anfangsbestand vorhanden");
        zeilen.add(o);
      }
      else
      {
        o.setAttribute(ANFANGSBESTAND, anfangsbestand);

        Double einnahmen = o.getDouble(EINNAHMEN);
        Double ausgaben = o.getDouble(AUSGABEN);
        Double umbuchungen = o.getDouble(UMBUCHUNGEN);
        Double ohneBuchungsart = o.getDouble(OHNE_BUCHUNGSART);
        if (ohneBuchungsart != null && Math.abs(ohneBuchungsart) >= 0.01d
            && anfangsbestand != null)
        {
          o.setAttribute(BEMERKUNG,
              "Summe Buchungen ohne Buchungsart: "
                  + new CurrencyFormatter("", Einstellungen.DECIMALFORMAT)
                      .format(ohneBuchungsart)
                  + " ");
        }
        Double endbestand = anfangsbestand + einnahmen + ausgaben + umbuchungen
            + (ohneBuchungsart == null ? 0 : ohneBuchungsart);
        o.setAttribute(ENDBESTAND, endbestand);

        // Die Art des Eintrags ist hier immer "Detail" (Wird für die Summen in
        // der Fußzeile benötigt)
        o.setAttribute(ART, ART_DETAIL);

        // Summen aller Konten ermitteln.
        // Konten über dem Limit nicht mitzählen.
        if (kontoart < Kontoart.LIMIT.getKey())
        {
          summeAnfangsbestand += anfangsbestand;
          summeEinnahmen += einnahmen;
          summeAusgaben += ausgaben;
          summeUmbuchungen += umbuchungen;
          summeEndbestand += endbestand;
          jahressaldo += endbestand - anfangsbestand;
        }

        // Die Summen für das Summen Anlagenkonto bestimmen
        if (summensaldo && kontoart == Kontoart.ANLAGE.getKey())
        {
          summenAnlageAnfangsbestand += anfangsbestand;
          summenAnlageEinnahmen += einnahmen;
          summenAnlageAusgaben += ausgaben;
          summenAnlageUmbuchungen += umbuchungen;
          summenAnlageEndbestand += endbestand;
        }
        // Die Konten über dem Limit gesondert ausgeben
        else if (kontoart > Kontoart.LIMIT.getKey())
        {
          zeilenUeberLimit.add(o);
        }
        else
        {
          zeilen.add(o);
        }
      }
    }

    // Wenn in den Einstellungen eingestellt nur ein Summen Anlagenkonto
    // anzeigen.
    if (summensaldo)
    {
      PseudoDBObject saldo = new PseudoDBObject();
      saldo.setAttribute(GRUPPE, "Summe Anlagenkonten");
      saldo.setAttribute(ANFANGSBESTAND, summenAnlageAnfangsbestand);
      saldo.setAttribute(EINNAHMEN, summenAnlageEinnahmen);
      saldo.setAttribute(AUSGABEN, summenAnlageAusgaben);
      saldo.setAttribute(UMBUCHUNGEN, summenAnlageUmbuchungen);
      saldo.setAttribute(ENDBESTAND, summenAnlageEndbestand);
      saldo.setAttribute(ART, ART_DETAIL);
      zeilen.add(saldo);
    }

    // Summe aller Konten Zeile
    PseudoDBObject summe = new PseudoDBObject();
    summe.setAttribute(GRUPPE, "Summe aller Konten");
    summe.setAttribute(ANFANGSBESTAND, summeAnfangsbestand);
    summe.setAttribute(EINNAHMEN, summeEinnahmen);
    summe.setAttribute(AUSGABEN, summeAusgaben);
    summe.setAttribute(UMBUCHUNGEN, summeUmbuchungen);
    summe.setAttribute(ENDBESTAND, summeEndbestand);
    summe.setAttribute(ART, ART_GESAMTSALDOFOOTER);
    zeilen.add(summe);

    // Überschuss/Verlust Zeile
    PseudoDBObject gv = new PseudoDBObject();
    gv.setAttribute(GRUPPE, "Überschuss/Verlust(-)");
    gv.setAttribute(ENDBESTAND, jahressaldo);
    gv.setAttribute(ART, ART_GESAMTGEWINNVERLUST);
    zeilen.add(gv);

    // Konten ohne Berücksichtigung im Saldo
    if (zeilenUeberLimit.size() > 0)
    {
      // Leerzeile als Trenner
      zeilen.add(new PseudoDBObject());

      // Konten ohne Berücksichtigung im Saldo
      PseudoDBObject ohne = new PseudoDBObject();
      ohne.setAttribute(GRUPPE, "Konten ohne Berücksichtigung im Saldo:");
      zeilen.add(ohne);

      zeilen.addAll(zeilenUeberLimit);
    }
    return zeilen;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return "Kontensaldo";
  }

  @Override
  protected ISaldoExport getAuswertung(String type) throws ApplicationException
  {
    switch (type)
    {
      case AuswertungCSV:
        return new KontenSaldoCSV();
      case AuswertungPDF:
        return new KontenSaldoPDF();
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }
}
