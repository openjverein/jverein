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
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.io.UmsatzsteuerSaldoCSV;
import de.jost_net.JVerein.io.UmsatzsteuerSaldoPDF;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.util.ApplicationException;

public class UmsatzsteuerSaldoControl extends AbstractSaldoControl
{
  /**
   * Die Summe
   */
  public static final String SUMME = "summe";

  public static final String STEUER = "steuer";

  public static final String STEUERBETRAG = "steuerbetrag";

  private static final String ARTSTEUERBUCHUNGSART = "artsteuerbuchungsart";

  private TablePart saldoList;

  public UmsatzsteuerSaldoControl(AbstractView view) throws RemoteException
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
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn("Steuerart", GRUPPE, null, false, Column.ALIGN_RIGHT);
      saldoList.addColumn("Steuer Name", STEUER);
      saldoList.addColumn("Bemessungsgrundlage", SUMME,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Steuerbetrag", STEUERBETRAG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Anzahl", ANZAHL);
      saldoList.setRememberColWidths(true);
      saldoList.removeFeature(FeatureSummary.class);
      saldoList.setFormatter(new SaldoFormatter());
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
    return saldoList;
  }

  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    String artAlt = null;

    // Summen der Steuerart
    Double steuerSumme = 0d;

    // Summen aller Steuern
    Double steuerGesamt = 0d;

    while (it.hasNext())
    {
      PseudoDBObject o = it.next();

      String art = null;
      Integer steuerArt = o.getInteger(ARTSTEUERBUCHUNGSART);
      if (steuerArt == ArtBuchungsart.EINNAHME || steuerArt == null)
      {
        art = "Umsatzsteuer";
      }
      else if (steuerArt == ArtBuchungsart.AUSGABE)
      {
        art = "Vorsteuer";
      }
      else
      {
        // Steuerfreie Ausgaben geben wir nicht mit aus.
        // Umbuchungen darf es gar nicht geben
        continue;
      }

      // Vor neuer Art Summe der letzten anzeigen.
      if (!art.equals(artAlt) && artAlt != null)
      {
        PseudoDBObject summe = new PseudoDBObject();
        summe.setAttribute(ART, ART_SALDOFOOTER);
        summe.setAttribute(GRUPPE, "Summe " + artAlt);
        summe.setAttribute(STEUERBETRAG, steuerSumme);
        zeilen.add(summe);

        steuerSumme = 0d;
      }

      // Bei neuer Art Kopfzeile anzeigen.
      if (!art.equals(artAlt))
      {
        PseudoDBObject head = new PseudoDBObject();
        head.setAttribute(ART, ART_HEADER);
        head.setAttribute(GRUPPE, art);
        zeilen.add(head);
        artAlt = art;
      }
      // Die Detailzeile wie sie aus dem iterator kommt anzeigen.
      o.setAttribute(ART, ART_DETAIL);
      if (o.getAttribute(STEUER) == null)
      {
        o.setAttribute(STEUER, "Steuerfreie Umsätze");
      }

      zeilen.add(o);
      steuerSumme += o.getDouble(STEUERBETRAG);
      steuerGesamt += o.getDouble(STEUERBETRAG);
    }

    // Am Ende noch Saldo der letzten Art.
    if (artAlt != null)
    {
      PseudoDBObject summe = new PseudoDBObject();
      summe.setAttribute(ART, ART_SALDOFOOTER);
      summe.setAttribute(GRUPPE, "Summe " + artAlt);
      summe.setAttribute(STEUERBETRAG, steuerSumme);
      zeilen.add(summe);
    }

    PseudoDBObject o = new PseudoDBObject();
    o.setAttribute(ART, ART_LEERZEILE);
    zeilen.add(o);

    PseudoDBObject saldo = new PseudoDBObject();
    saldo.setAttribute(ART, ART_GESAMTSALDOFOOTER);
    saldo.setAttribute(GRUPPE,
        "Verbleibende Umsatzsteuer/Verbleibender Überschuss ");
    saldo.setAttribute(STEUERBETRAG, steuerGesamt);
    zeilen.add(saldo);

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
    final boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
        "buchungsart");
    it.addColumn("steuer.name as " + STEUER);
    it.addColumn("steuerbuchungsart.art as " + ARTSTEUERBUCHUNGSART);
    it.addColumn("COUNT(buchung.id) as " + ANZAHL);

    // Bemessungsgrundlage (Netto) berechnen
    it.addColumn(
        // Alte Steuerbuchungen mit dependencyid direkt nehmen. Auch bei
        // Anlagebuchungen.
        "COALESCE(SUM(CASE WHEN buchung.dependencyid > -1 OR konto.kontoart = ? then buchung.betrag ELSE "
            + "CAST(buchung.betrag*100/(100+COALESCE(steuer.satz,0)) AS DECIMAL(10,2)) END),0) AS "
            + SUMME,
        Kontoart.ANLAGE.getKey());

    // Steuer berechnen.
    it.addColumn("COALESCE(SUM("
        // Alte Steuerbuchungen mit dependencyid keine Steuer berechnen.
        // Anlagekonten keine Steuer.
        + "CASE WHEN buchung.dependencyid > -1 OR konto.kontoart = ? THEN 0 ELSE "
        + "CAST(steuer.satz/100 * buchung.betrag*100/(100+COALESCE(steuer.satz,0)) AS DECIMAL(10,2)) END "
        // Alte Steuer hinzurechnen
        + "+ COALESCE(buchung_steuer_alt.betrag,0)" + "),0) AS " + STEUERBETRAG,
        Kontoart.ANLAGE.getKey());

    it.join("buchung",
        "buchung.buchungsart = buchungsart.id AND buchung.datum >= ? AND buchung.datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());

    // Die Steuer-Splitbuchung mit der gleichen dependecy-ID anhängen um die
    // alte Steuer zu ermitteln.
    it.leftJoin("buchung as buchung_steuer_alt",
        "buchung_steuer_alt.splitid = buchung.splitid AND buchung_steuer_alt.dependencyid = buchung.dependencyid"
            + " AND buchung_steuer_alt.dependencyid > -1 AND ABS(buchung_steuer_alt.betrag) < ABS(buchung.betrag)");
    it.addFilter(
        "buchung.dependencyid is null or buchung.dependencyid = -1 OR buchung_steuer_alt.betrag IS NOT NULL");

    it.leftJoin("konto", "buchung.konto = konto.id");
    it.addFilter("konto.kontoart is null OR konto.kontoart < ?",
        Kontoart.LIMIT.getKey());
    // Von Anlagekonten nur Einnahmen (Sachspenden)
    it.addFilter("konto.kontoart != ? OR buchungsart.art = ?",
        Kontoart.ANLAGE.getKey(), ArtBuchungsart.EINNAHME);

    // Steuerfrei Buchungen nur Einnahmen
    it.addFilter("steuerbuchungsart.art IS NOT NULL OR buchungsart.art = ?",
        ArtBuchungsart.EINNAHME);

    if (steuerInBuchung)
    {
      it.leftJoin("steuer", "steuer.id = buchung.steuer");
    }
    else
    {
      it.leftJoin("steuer", "steuer.id = buchungsart.steuer");
    }
    it.leftJoin("buchungsart as steuerbuchungsart",
        "steuer.buchungsart = steuerbuchungsart.id");

    it.addGroupBy("steuer.id");
    it.addGroupBy("steuerbuchungsart.art");
    it.addGroupBy("steuer.name");

    it.setOrder("ORDER BY steuerbuchungsart.art, steuer.id");

    return it;
  }

  @Override
  protected String getAuswertungTitle()
  {
    return VorlageUtil.getName(VorlageTyp.UMSATZSTEUER_VORANMELDUNG_TITEL,
        this);
  }

  @Override
  protected String getAuswertungSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.UMSATZSTEUER_VORANMELDUNG_SUBTITEL,
        this);
  }

  @Override
  protected String getDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.UMSATZSTEUER_VORANMELDUNG_DATEINAME,
        this);
  }

  @Override
  protected ISaldoExport getAuswertung(String type) throws ApplicationException
  {
    switch (type)
    {
      case AuswertungCSV:
        return new UmsatzsteuerSaldoCSV();
      case AuswertungPDF:
        return new UmsatzsteuerSaldoPDF();
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }

}
