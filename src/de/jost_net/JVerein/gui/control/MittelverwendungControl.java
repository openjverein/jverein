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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.MittelverwendungDialog;
import de.jost_net.JVerein.gui.formatter.SaldoFormatter;
import de.jost_net.JVerein.io.ISaldoExport;
import de.jost_net.JVerein.io.MittelverwendungExportCSV;
import de.jost_net.JVerein.io.MittelverwendungExportPDF;
import de.jost_net.JVerein.keys.Anlagenzweck;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class MittelverwendungControl extends AbstractSaldoControl
{
  private TablePart zuflussList;

  private TablePart saldoList;

  public final static int FLOW_REPORT = 0;

  public final static int SALDO_REPORT = 1;

  private int selectedTab = FLOW_REPORT;

  // Spalten des PseudoDBObjects
  public static final String SUMME = "summe";

  private static final String KONTOART = "kontoart";

  public static final String BEZEICHNUNG = "bezeichnung";

  public static final String KOMMENTAR = "kommentar";

  public static final String BETRAG = "betragfeld";

  private static final String ZWECK = "zweck";

  public static final String NR = "nr";

  private static final String ANZAHL = "anzahl";

  /**
   * Einrückung der Spalten
   */
  private static String BLANKS = "          ";

  private Button configButton;

  private Date editDatumvon = null;

  private String jaId = null;

  private String vorJaId = null;

  private double rueckstandVorjahrNeu;

  private double zwanghafteWeitergabeNeu;

  public MittelverwendungControl(AbstractView view) throws RemoteException
  {
    super(view);
  }

  /**
   * Der Button zum Setzen der Startwerte
   * 
   * @return Button
   * @throws RemoteException
   * @throws ApplicationException
   */
  public Button getConfigButton() throws RemoteException, ApplicationException
  {
    configButton = new Button("Startwerte setzen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          Jahresabschluss ja = (Jahresabschluss) Einstellungen.getDBService()
              .createObject(Jahresabschluss.class, jaId);
          Double rueckstand = null;
          Double weitergabe = null;
          if (vorJaId != null)
          {
            Jahresabschluss vorJa = (Jahresabschluss) Einstellungen
                .getDBService().createObject(Jahresabschluss.class, vorJaId);
            if (vorJa.getVerwendungsrueckstand() != null
                && vorJa.getZwanghafteWeitergabe() != null)
            {
              // Werte des Jahres berechnen
              getMittelverwendungFlowList(ja.getVon(), ja.getBis());
              rueckstand = rueckstandVorjahrNeu;
              weitergabe = zwanghafteWeitergabeNeu;
            }
          }
          MittelverwendungDialog dialog = new MittelverwendungDialog(rueckstand,
              weitergabe, ja.getName());
          if (!dialog.open())
          {
            return;
          }
          if (ja.isNewObject())
          {
            ja.setVon(editDatumvon);
            Calendar cal = Calendar.getInstance();
            cal.setTime(editDatumvon);
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            ja.setBis(cal.getTime());
            ja.setDatum(new Date());
            ja.setName(dialog.getName());
          }
          ja.setVerwendungsrueckstand(dialog.getVerwendungsrueckstand());
          ja.setZwanghafteWeitergabe(dialog.getZwanghafteWeitergabe());
          ja.store();

          // Listen neu laden
          reloadList();

          // Button aktualiseren um Werte zu setzen
          updateConfigButton();
        }
        catch (OperationCanceledException ignore)
        {
          throw new OperationCanceledException();
        }
        catch (Exception e)
        {
          throw new ApplicationException(e);
        }
      }
    }, null, false, "text-x-generic.png");
    updateConfigButton();
    return configButton;
  }

  /**
   * Den Button enabble oder disable; <code>jaId</code>, <code>vorJaId</code>
   * und <code>editDatumvon</code> setzen
   * 
   * @throws ApplicationException
   */
  @SuppressWarnings("unchecked")
  private void updateConfigButton() throws ApplicationException
  {
    try
    {
      DBService service = Einstellungen.getDBService();
      DBIterator<Jahresabschluss> abschluesse = service
          .createList(Jahresabschluss.class);
      abschluesse.setOrder("ORDER BY von desc");
      List<Jahresabschluss> jahresabschluesse = PseudoIterator
          .asList(abschluesse);

      Date abschlussvon = DateUtils.addYears(getDatumvon().getDate(), -1);

      // Es gibt noch keinen Jahresabschluss, dann wird er erzeugt
      // Oder es ist der Mittelverwendungsreport des ersten Jahressabschlusses
      // Dann muss einer vorher erzeugt werden
      if (jahresabschluesse.size() == 0
          || jahresabschluesse.get(jahresabschluesse.size() - 1).getVon()
              .equals(getDatumvon().getDate()))
      {
        configButton.setEnabled(true);
        editDatumvon = abschlussvon;
        jaId = null;
        vorJaId = null;
        return;
      }

      // Der aktuelle Mittelverwendungsreport ist mehr als 1 Jahr nach dem
      // letzten Jahresabschluss oder vor dem ersten Jahresabschluss
      if (abschlussvon.after(jahresabschluesse.get(0).getVon())
          || getDatumvon().getDate().before(
              jahresabschluesse.get(jahresabschluesse.size() - 1).getVon()))
      {
        configButton.setEnabled(false);
        return;
      }

      // Der aktuelle Mittelverwendungsreport ist ein Jahr nach dem letzten
      // Jahresabschluss, wir erlauben das Rücksetzen z.B. wegen
      // Unterschreiten der 45.000 EUR Einnahmen Grenze
      if (abschlussvon.equals(jahresabschluesse.get(0).getVon()))
      {
        configButton.setEnabled(true);
        editDatumvon = abschlussvon;
        jaId = jahresabschluesse.get(0).getID();
        if (jahresabschluesse.size() == 1)
        {
          vorJaId = null;
        }
        else
        {
          vorJaId = jahresabschluesse.get(1).getID();
        }
        return;
      }

      int i = 0;
      for (Jahresabschluss ja : jahresabschluesse)
      {
        if (ja.getVon().equals(abschlussvon))
        {
          if (ja.getVerwendungsrueckstand() == null
              || ja.getZwanghafteWeitergabe() == null)
          {
            configButton.setEnabled(true);
            editDatumvon = abschlussvon;
            jaId = ja.getID();
            if (i == jahresabschluesse.size() - 1)
            {
              vorJaId = null;
            }
            else
            {
              vorJaId = jahresabschluesse.get(i + 1).getID();
            }
            return;
          }
        }
        i++;
      }
      configButton.setEnabled(false);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("fehler");
    }
  }

  @Override
  public TablePart getSaldoList() throws ApplicationException
  {
    switch (selectedTab)
    {
      case FLOW_REPORT:
        return getMittelverwendungFlowTable();
      case SALDO_REPORT:
        return getMittelverwendungSaldoTable();
      default:
        return null;
    }

  }

  /**
   * Den TablePart für die Saldobasierte Mittelverwendung holen
   * 
   * @return TablePart
   * @throws ApplicationException
   */
  public TablePart getMittelverwendungSaldoTable() throws ApplicationException
  {
    try
    {
      if (saldoList != null)
      {
        return saldoList;
      }
      saldoList = new TablePart(getMittelverwendungSaldoList(), null)
      {
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      saldoList.addColumn("Art", GRUPPE);
      saldoList.addColumn("Konto", BEZEICHNUNG);
      saldoList.addColumn("Betrag", BETRAG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Summe", SUMME,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      saldoList.addColumn("Kommentar", KOMMENTAR);
      saldoList.setRememberColWidths(true);
      saldoList.setRememberOrder(true);
      saldoList.removeFeature(FeatureSummary.class);
      saldoList.setFormatter(new SaldoFormatter());
      return saldoList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten " + e.getMessage());
    }
  }

  /**
   * Den TablePart für die Zuflussbsiere Mittelverwendung holen
   * 
   * @return TablePart
   * @throws ApplicationException
   */
  public TablePart getMittelverwendungFlowTable() throws ApplicationException
  {
    try
    {
      if (zuflussList != null)
      {
        return zuflussList;
      }
      zuflussList = new TablePart(getMittelverwendungFlowList(
          getDatumvon().getDate(), getDatumbis().getDate()), null)
      {
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };
      zuflussList.addColumn("Nr", NR);
      zuflussList.addColumn("Mittel", GRUPPE);
      zuflussList.addColumn("Betrag", BETRAG,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      zuflussList.addColumn("Summe", SUMME,
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_RIGHT);
      // Dummy Spalte, damit Summe nicht am rechten Rand klebt
      zuflussList.addColumn(" ", " ",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT), false,
          Column.ALIGN_LEFT);
      zuflussList.setRememberColWidths(true);
      zuflussList.setRememberOrder(true);
      zuflussList.removeFeature(FeatureSummary.class);
      return zuflussList;
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler aufgetreten" + e.getMessage());
    }
  }

  @Override
  public ArrayList<PseudoDBObject> getList() throws RemoteException
  {
    switch (selectedTab)
    {
      case FLOW_REPORT:
        return getMittelverwendungFlowList(getDatumvon().getDate(),
            getDatumbis().getDate());
      case SALDO_REPORT:
        return getMittelverwendungSaldoList();
      default:
        return null;
    }
  }

  /**
   * Die Liste für die Zuflussbasierte Mittelverwendung holen
   * 
   * @param datumvon
   * @param datumbis
   * @return Die Liste der Einträge
   * @throws RemoteException
   */
  public ArrayList<PseudoDBObject> getMittelverwendungFlowList(Date datumvon,
      Date datumbis) throws RemoteException
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(datumvon);
    int aktuellesGJ = cal.get(Calendar.YEAR);
    int letztesGJ = aktuellesGJ - 1;
    int vorletztesGJ = aktuellesGJ - 2;
    cal.add(Calendar.DAY_OF_MONTH, -1);
    Date endeLetztesGJ = cal.getTime();

    Integer pos = 1;

    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    // Schritt 1: Berechnung des Verwendungsrückstand(+)/-überhang(-)
    // am Ende des letzten GJ
    // Vorhandene Geldmittel zum Ende des letzten GJ sind zu verwenden
    // Vorhandene zweckfremde Anlagen sind zu verwenden
    // Nicht der zeitnahen Mittelverwendung unterliegende Mittel (Rücklagen)
    // zum Ende des letzten GJ können abgezogen werden
    ExtendedDBIterator<PseudoDBObject> anfangsbestandIt = new ExtendedDBIterator<>(
        "anfangsbestand");
    anfangsbestandIt.addColumn(
        "SUM(COALESCE(CASE WHEN konto.kontoart > ? THEN -1 ELSE 1 END * anfangsbestand.betrag,0)) as "
            + BETRAG,
        Kontoart.LIMIT.getKey());
    anfangsbestandIt.join("konto", "anfangsbestand.konto = konto.id");
    anfangsbestandIt.addFilter("anfangsbestand.datum = ?", datumvon);
    anfangsbestandIt.addFilter(
        "konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?) OR (konto.kontoart > ? AND konto.kontoart < ?)",
        Kontoart.GELD.getKey(), Kontoart.ANLAGE.getKey(),
        Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(), Kontoart.LIMIT.getKey(),
        Kontoart.LIMIT_RUECKLAGE.getKey());

    PseudoDBObject anfangsbestand = anfangsbestandIt.next();
    anfangsbestand.setAttribute(NR, pos++);
    anfangsbestand.setAttribute(ART, ART_DETAIL);
    anfangsbestand.setAttribute(GRUPPE,
        "Verwendungsrückstand(+)/-überhang(-) zu Beginn des aktuellen GJ "
            + aktuellesGJ);
    zeilen.add(anfangsbestand);

    Double vorhandeneMittel = anfangsbestand.getDouble(BETRAG) != null
        ? anfangsbestand.getDouble(BETRAG)
        : 0d;

    // Der in dem Rückstand enthaltene Rückstand aus dem vorletzten Jahr
    Double rueckstandVorVorjahr = 0d;
    Double zwanghafteWeitergabeVorjahr = 0d;
    DBIterator<Jahresabschluss> jahresabschluesse = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    jahresabschluesse.addFilter("bis = ?", endeLetztesGJ);
    if (jahresabschluesse != null && jahresabschluesse.hasNext())
    {
      Jahresabschluss abschluss = jahresabschluesse.next();
      rueckstandVorVorjahr = abschluss.getVerwendungsrueckstand();
      zwanghafteWeitergabeVorjahr = abschluss.getZwanghafteWeitergabe();
    }

    PseudoDBObject ja = new PseudoDBObject();
    ja.setAttribute(NR, pos++);
    ja.setAttribute(ART, ART_DETAIL);
    ja.setAttribute(BETRAG, rueckstandVorVorjahr);
    ja.setAttribute(GRUPPE, BLANKS
        + "- Darin enthaltener Rest des Verwendungsrückstand aus dem vorletzten GJ "
        + vorletztesGJ);
    zeilen.add(ja);

    PseudoDBObject jaRueck = new PseudoDBObject();
    jaRueck.setAttribute(NR, pos++);
    jaRueck.setAttribute(ART, ART_DETAIL);
    jaRueck.setAttribute(BETRAG, zwanghafteWeitergabeVorjahr);
    jaRueck.setAttribute(GRUPPE, BLANKS
        + "- Überfällige zwanghafte satzungsgemäße Weitergabe von Mitteln aus den letzten GJ "
        + letztesGJ);
    zeilen.add(jaRueck);

    // Schritt 2: Mittel Zufluss
    // Summe Zuflüsse durch Umbuchung
    // Auszahlung aus Fremdkapital z.B. Darlehen,
    // Rückbuchung von zweckgebundenen Anlagen
    // Summe aller Zuflüsse bei Geldkonten und Anlagen (=Sachspenden)
    ExtendedDBIterator<PseudoDBObject> flussIt = new ExtendedDBIterator<>(
        "buchung");

    flussIt.join("konto", "buchung.konto = konto.id");
    flussIt.join("buchungsart", "buchung.buchungsart = buchungsart.id");

    flussIt.addColumn("SUM(COALESCE("
        // Geld und zweckfremde Anlagen EINNAMEN +
        + "CASE WHEN buchungsart.art = ? AND "
        + "(konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)) THEN buchung.betrag "
        // Schulden und gebundene Anlagen UMBUCHUNG < 0 -
        + "WHEN buchungsart.art = ? AND buchung.betrag < 0 AND "
        + "(konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)) THEN -buchung.betrag "
        + "ELSE 0 END,0)) as " + EINNAHMEN, ArtBuchungsart.EINNAHME,
        Kontoart.GELD.getKey(), Kontoart.ANLAGE.getKey(),
        Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(), ArtBuchungsart.UMBUCHUNG,
        Kontoart.SCHULDEN.getKey(), Kontoart.ANLAGE.getKey(),
        Anlagenzweck.NUTZUNGSGEBUNDEN.getKey());

    // Summe aller Abflüsse bei Geldkonten
    // Summe der Abflüsse bei Umbuchung
    // Tilgung Verbindlichkeiten z.B. Darlehen,
    // Erwerb zweckgebundener Anlagen
    flussIt.addColumn("SUM(COALESCE("
        // Geld und zweckfremde Anlagen AUSGABEN -
        + "CASE WHEN buchungsart.art = ? AND "
        + "(konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)) THEN -buchung.betrag "
        // Schulden und gebundene Anlagen UMBUCHUNG > 0 +
        + "WHEN buchungsart.art = ? AND buchung.betrag > 0 AND "
        + "(konto.kontoart = ? OR (konto.kontoart = ? AND konto.zweck = ?)) THEN buchung.betrag "
        + "ELSE 0 END,0)) as " + AUSGABEN, ArtBuchungsart.AUSGABE,
        Kontoart.GELD.getKey(), Kontoart.ANLAGE.getKey(),
        Anlagenzweck.ZWECKFREMD_EINGESETZT.getKey(), ArtBuchungsart.UMBUCHUNG,
        Kontoart.SCHULDEN.getKey(), Kontoart.ANLAGE.getKey(),
        Anlagenzweck.NUTZUNGSGEBUNDEN.getKey());

    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERTPFLICHT))
    {
      // Die Steuer bei Veräußerung von Anlagevermögen mit Steuer
      // Steuer Einnahmen bei Umbuchungen > 0 auf dem Geldkonto
      flussIt.addColumn("SUM(COALESCE("
          + "CASE WHEN buchungsart.art = ? AND konto.kontoart = ? AND buchung.betrag > 0 "
          + "THEN CAST(-buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2)) ELSE 0 END "
          + ",0)) AS steuereinnahme", ArtBuchungsart.UMBUCHUNG,
          Kontoart.GELD.getKey());

      // Die Steuer bei Kauf von Anlagevermögen mit Steuer
      // Steuer Ausgabe bei Umbuchungen < 0 auf dem Geldkonto
      flussIt.addColumn("SUM(COALESCE("
          + "CASE WHEN buchungsart.art = ? AND konto.kontoart = ? AND buchung.betrag < 0 "
          + "THEN CAST(-buchung.betrag * steuer.satz/100 / (1 + steuer.satz/100) AS DECIMAL(10,2)) ELSE 0 END "
          + ",0)) AS steuerausgabe", ArtBuchungsart.UMBUCHUNG,
          Kontoart.GELD.getKey());

      // Je nach Einstellung Steuer an Buchungsart oder Buchung hängen
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        flussIt.leftJoin("steuer", "steuer.id = buchung.steuer");
      }
      else
      {
        flussIt.leftJoin("steuer", "steuer.id = buchungsart.steuer");
      }
    }

    flussIt.addFilter("datum >= ?", datumvon);
    flussIt.addFilter("datum <= ?", datumbis);

    flussIt.addFilter("konto.kontoart < ?", Kontoart.LIMIT.getKey());

    PseudoDBObject fluss = flussIt.next();
    Double zufuehrung = fluss.getDouble(EINNAHMEN) != null
        ? fluss.getDouble(EINNAHMEN)
        : 0d;
    Double verwendung = fluss.getDouble(AUSGABEN) != null
        ? fluss.getDouble(AUSGABEN)
        : 0d;

    // ggf. Steuern hinzurechnen
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERTPFLICHT))
    {
      if (fluss.getDouble("steuereinnahme") != null)
      {
        zufuehrung += fluss.getDouble("steuereinnahme");
      }
      if (fluss.getDouble("steuerausgabe") != null)
      {
        verwendung += fluss.getDouble("steuerausgabe");
      }
    }

    PseudoDBObject zufluss = new PseudoDBObject();
    zufluss.setAttribute(NR, pos++);
    zufluss.setAttribute(ART, ART_DETAIL);
    zufluss.setAttribute(GRUPPE,
        "Insgesamt im aktuellen GJ zugeflossene Mittel");
    zufluss.setAttribute(BETRAG, zufuehrung);
    zeilen.add(zufluss);

    PseudoDBObject zuVerwenden = new PseudoDBObject();
    zuVerwenden.setAttribute(NR, pos++);
    zuVerwenden.setAttribute(ART, ART_SALDOFOOTER);
    zuVerwenden.setAttribute(GRUPPE,
        BLANKS + "Zu verwendende Mittel im aktuellen GJ und nächstem GJ");
    zuVerwenden.setAttribute(SUMME, zufuehrung + vorhandeneMittel);
    zeilen.add(zuVerwenden);

    PseudoDBObject leerZeile = new PseudoDBObject();
    leerZeile.setAttribute(ART, ART_LEERZEILE);
    zeilen.add(leerZeile);

    PseudoDBObject abfluss = new PseudoDBObject();
    abfluss.setAttribute(NR, pos++);
    abfluss.setAttribute(ART, ART_DETAIL);
    abfluss.setAttribute(GRUPPE, "Im aktuellen GJ verwendete Mittel");
    abfluss.setAttribute(BETRAG, verwendung);
    zeilen.add(abfluss);

    // Rücklagen
    ExtendedDBIterator<PseudoDBObject> ruecklageIt = new ExtendedDBIterator<>(
        "buchung");
    ruecklageIt.addColumn("SUM(buchung.betrag) as " + BETRAG);
    ruecklageIt.addColumn("konto.kontoart as " + KONTOART);
    ruecklageIt.addColumn("buchungsart.art as " + ART);

    ruecklageIt.join("konto", "buchung.konto = konto.id");
    ruecklageIt.join("buchungsart", "buchung.buchungsart = buchungsart.id");

    ruecklageIt.addFilter("buchung.datum >= ?", datumvon);
    ruecklageIt.addFilter("buchung.datum <= ?", datumbis);
    ruecklageIt.addFilter("konto.kontoart > ?", Kontoart.LIMIT.getKey());
    ruecklageIt.addFilter("konto.kontoart < ?",
        Kontoart.LIMIT_RUECKLAGE.getKey());

    ruecklageIt.addGroupBy("konto.kontoart");
    ruecklageIt.addGroupBy("buchungsart.art");

    ruecklageIt.setOrder("ORDER BY konto.kontoart,buchungsart.art");

    if ((Boolean) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG))
    {
      ruecklageIt.addHaving("abs(" + BETRAG + ") >= 0.01");
    }

    Double summeZuRuecklagen = 0d;
    Double summeEntRuecklagen = 0d;
    while (ruecklageIt.hasNext())
    {
      PseudoDBObject o = ruecklageIt.next();

      switch (o.getInteger(ART))
      {
        case ArtBuchungsart.EINNAHME:
          summeZuRuecklagen += o.getDouble(BETRAG);
          o.setAttribute(NR, pos++);
          o.setAttribute(ART, ART_DETAIL);
          o.setAttribute(GRUPPE, "Zuführung "
              + Kontoart.getByKey(o.getInteger(KONTOART)).getText());
          zeilen.add(o);
          break;
        case ArtBuchungsart.AUSGABE:
          summeEntRuecklagen -= o.getDouble(BETRAG);
          o.setAttribute(NR, pos++);
          o.setAttribute(ART, ART_DETAIL);
          o.setAttribute(GRUPPE, "Entnahme "
              + Kontoart.getByKey(o.getInteger(KONTOART)).getText());
          zeilen.add(o);
          break;
        case ArtBuchungsart.UMBUCHUNG:
          break;
      }
    }
    PseudoDBObject gesamtVerwendet = new PseudoDBObject();
    gesamtVerwendet.setAttribute(NR, pos++);
    gesamtVerwendet.setAttribute(ART, ART_SALDOFOOTER);
    gesamtVerwendet.setAttribute(GRUPPE,
        BLANKS + "Insgesamt im aktuellen GJ verwendete Mittel");
    gesamtVerwendet.setAttribute(SUMME,
        verwendung + summeZuRuecklagen - summeEntRuecklagen);
    zeilen.add(gesamtVerwendet);

    zeilen.add(leerZeile);

    PseudoDBObject rueckstand = new PseudoDBObject();
    rueckstand.setAttribute(NR, pos++);
    rueckstand.setAttribute(ART, ART_SALDOFOOTER);
    rueckstand.setAttribute(GRUPPE,
        "Verwendungsrückstand(+)/-überhang(-) zum Ende des aktuellen GJ "
            + aktuellesGJ);
    rueckstand.setAttribute(SUMME, zufuehrung + vorhandeneMittel - verwendung
        - summeZuRuecklagen + summeEntRuecklagen);
    zeilen.add(rueckstand);

    // Berechnung der Mittelverwendung
    rueckstandVorVorjahr = (rueckstandVorVorjahr == null) ? 0.0
        : rueckstandVorVorjahr;
    zwanghafteWeitergabeVorjahr = (zwanghafteWeitergabeVorjahr == null) ? 0.0
        : zwanghafteWeitergabeVorjahr;

    Double ausgaben = Math.max(verwendung - summeEntRuecklagen, 0);
    Double rueckstandVorjahr = Math.max(
        vorhandeneMittel - rueckstandVorVorjahr - zwanghafteWeitergabeVorjahr,
        0);
    zwanghafteWeitergabeNeu = 0.0;
    rueckstandVorjahrNeu = 0.0; // Rest aus Rückstand Vorjahr
    // Der Rückstand aus dem vorletzten Jahr muss ganz aufgebraucht werden,
    // ansonsten unterliegt der Restbetrag der zwanghaften satzungsgemäßen
    // Weitergabe von Mitteln

    if (zwanghafteWeitergabeVorjahr > ausgaben)
    {
      zwanghafteWeitergabeNeu = rueckstandVorVorjahr;
      rueckstandVorjahrNeu = rueckstandVorjahr;
    }
    else if (rueckstandVorVorjahr + zwanghafteWeitergabeVorjahr > ausgaben)
    {
      zwanghafteWeitergabeNeu = rueckstandVorVorjahr - ausgaben
          + zwanghafteWeitergabeVorjahr;
      rueckstandVorjahrNeu = rueckstandVorjahr;
    }
    else
    {
      rueckstandVorjahrNeu = Math.max(vorhandeneMittel - ausgaben, 0);
    }
    PseudoDBObject rueckstandLetztesGJ = new PseudoDBObject();
    rueckstandLetztesGJ.setAttribute(NR, pos++);
    rueckstandLetztesGJ.setAttribute(ART, ART_DETAIL);
    rueckstandLetztesGJ.setAttribute(GRUPPE,
        BLANKS + "- Darin enthaltener Verwendungsrückstand aus dem letzten GJ "
            + letztesGJ);
    rueckstandLetztesGJ.setAttribute(BETRAG, rueckstandVorjahrNeu);
    zeilen.add(rueckstandLetztesGJ);

    PseudoDBObject ueberfaellig = new PseudoDBObject();
    ueberfaellig.setAttribute(NR, pos++);
    ueberfaellig.setAttribute(ART, ART_DETAIL);
    ueberfaellig.setAttribute(GRUPPE, BLANKS
        + "- Überfällige zwanghafte satzungsgemäße Weitergabe von Mitteln");
    ueberfaellig.setAttribute(BETRAG, zwanghafteWeitergabeNeu);
    zeilen.add(ueberfaellig);

    // Buchungen ohne Buchungsart
    ExtendedDBIterator<PseudoDBObject> ohneBuchungsartIt = new ExtendedDBIterator<>(
        "buchung");
    ohneBuchungsartIt.addColumn("count(*) as " + ANZAHL);
    ohneBuchungsartIt.addColumn("SUM(buchung.betrag) as " + SUMME);
    ohneBuchungsartIt.addColumn("konto.kontoart as " + KONTOART);

    ohneBuchungsartIt.join("konto", "buchung.konto = konto.id");

    ohneBuchungsartIt.addFilter("buchung.datum >= ?", datumvon);
    ohneBuchungsartIt.addFilter("buchung.datum <= ?", datumbis);
    ohneBuchungsartIt.addFilter("buchung.buchungsart is null");

    ohneBuchungsartIt.addGroupBy("konto.kontoart > " + Kontoart.LIMIT.getKey());
    ohneBuchungsartIt.addGroupBy("konto.kontoart");
    ohneBuchungsartIt
        .setOrder("ORDER BY konto.kontoart > " + Kontoart.LIMIT.getKey());

    if (ohneBuchungsartIt.hasNext())
    {
      // Leerzeile
      zeilen.add(leerZeile);
    }

    while (ohneBuchungsartIt.hasNext())
    {
      PseudoDBObject o = ohneBuchungsartIt.next();

      if (o.getInteger(KONTOART) < Kontoart.LIMIT.getKey())
      {
        o.setAttribute(NR, pos++);
        o.setAttribute(ART, ART_SALDOFOOTER);
        o.setAttribute(GRUPPE,
            "Buchungen ohne Buchungsart: " + o.getInteger(ANZAHL));
        zeilen.add(o);
      }
      else
      {
        o.setAttribute(NR, pos++);
        o.setAttribute(ART, ART_SALDOFOOTER);
        o.setAttribute(GRUPPE,
            "Buchungen ohne Buchungsart bei Rücklagen und Vermögen: "
                + o.getInteger(ANZAHL));
        zeilen.add(o);
      }
    }
    return zeilen;
  }

  /**
   * Die Liste für die Saldobasierte Mittelverwendung holen
   * 
   * @return Die Liste der Einträge
   * @throws RemoteException
   */
  public ArrayList<PseudoDBObject> getMittelverwendungSaldoList()
      throws RemoteException
  {
    ExtendedDBIterator<PseudoDBObject> it = getIterator();
    ArrayList<PseudoDBObject> zeilen = new ArrayList<>();

    Boolean anlagenSummenkonto = (Boolean) Einstellungen
        .getEinstellung(Property.SUMMENANLAGENKONTO);

    Integer kontoartAlt = null;
    String kontoklasseAlt = null;

    Double summeKontoart = 0d;
    Double summeAnlageklasse = 0d;

    Double summeAnlagenGebunden = 0d;
    Double summeFremdkapital = 0d;

    Double summeVermoegen = 0d;
    Double summeFreieMittel = 0d;

    // Wir fügen dem iterator noch einen Dummy Eintrag eines Kontos über dem
    // Limit hinzu, so wird sichergestellt, dass die Summen auch immer angezeigt
    // werden, ohne den Code doppelt schreiben müssen.
    @SuppressWarnings("unchecked")
    List<PseudoDBObject> list = PseudoIterator.asList(it);
    PseudoDBObject dummy = new PseudoDBObject();
    dummy.setAttribute(ART, ART_LEERZEILE);
    dummy.setAttribute(KONTOART, Kontoart.RUECKLAGE_SONSTIG.getKey());
    dummy.setAttribute(BUCHUNGSKLASSE, "");
    list.add(dummy);

    for (PseudoDBObject o : list)
    {
      Integer kontoart = o.getInteger(KONTOART);
      String kontoklasse = (String) o.getAttribute(BUCHUNGSKLASSE);
      if (kontoklasse == null)
      {
        kontoklasse = "Nicht zugeordnet";
      }

      // Bei bei Konten unter dem Limit vor neuer Kontoart Summe ausgeben.
      if (!kontoart.equals(kontoartAlt) && kontoartAlt != null
          && kontoartAlt < Kontoart.LIMIT.getKey())
      {
        PseudoDBObject saldo = new PseudoDBObject();
        saldo.setAttribute(ART, ART_SALDOFOOTER);
        saldo.setAttribute(GRUPPE,
            "Summe " + Kontoart.getByKey(kontoartAlt).getTextVermoegen());
        saldo.setAttribute(SUMME, summeKontoart);
        zeilen.add(saldo);

        summeVermoegen += summeKontoart;
        summeKontoart = 0d;
      }

      // Nach allen Kontoarten unter dem Limit Summe ausgeben
      if (kontoartAlt != null && kontoartAlt < Kontoart.LIMIT.getKey()
          && kontoart > Kontoart.LIMIT.getKey())
      {
        PseudoDBObject gesamtSaldo = new PseudoDBObject();
        gesamtSaldo.setAttribute(ART, ART_SALDOFOOTER);
        gesamtSaldo.setAttribute(GRUPPE, "Gesamtvermögen");
        gesamtSaldo.setAttribute(SUMME, summeVermoegen);
        zeilen.add(gesamtSaldo);

        PseudoDBObject leer = new PseudoDBObject();
        leer.setAttribute(ART, ART_LEERZEILE);
        zeilen.add(leer);

        // Head "Verwendete Mittel" und bekannte Werte der Konten unter dem
        // Limit ausgeben.
        PseudoDBObject headMittel = new PseudoDBObject();
        headMittel.setAttribute(ART, ART_HEADER);
        headMittel.setAttribute(GRUPPE,
            "Liste der zeitnahen Verwendung entzogenen Mittel:");
        zeilen.add(headMittel);

        if (Math.abs(summeAnlagenGebunden) >= 0.01d)
        {
          PseudoDBObject mittel = new PseudoDBObject();
          mittel.setAttribute(ART, ART_SALDOFOOTER);
          mittel.setAttribute(GRUPPE, "Nutzungsgebundenes Anlagevermögen");
          mittel.setAttribute(SUMME, summeAnlagenGebunden);
          zeilen.add(mittel);
        }
        if (Math.abs(summeFremdkapital) >= 0.01d)
        {
          PseudoDBObject mittel = new PseudoDBObject();
          mittel.setAttribute(ART, ART_SALDOFOOTER);
          mittel.setAttribute(GRUPPE, "Fremdkapital");
          mittel.setAttribute(SUMME, summeFremdkapital);
          zeilen.add(mittel);
        }
        // Diese Summen gehören auch zu den freien Mitteln.
        summeFreieMittel += summeAnlagenGebunden + summeFremdkapital;
      }

      // Bei bei Konten über dem Limit vor neuer Anlagenklasse Summe ausgeben.
      if (!kontoklasse.equals(kontoklasseAlt) && kontoklasseAlt != null
          && kontoart > Kontoart.LIMIT.getKey())
      {
        PseudoDBObject saldo = new PseudoDBObject();
        saldo.setAttribute(ART, ART_SALDOFOOTER);
        saldo.setAttribute(GRUPPE, "Summe Rücklagen " + kontoklasseAlt);
        saldo.setAttribute(SUMME, summeAnlageklasse);
        zeilen.add(saldo);

        summeFreieMittel += summeAnlageklasse;
        summeAnlageklasse = 0d;
      }
      // Dummy Eintrag ignorieren
      if (o.getInteger(ART) != null && o.getInteger(ART) == ART_LEERZEILE)
      {
        continue;
      }

      // Bei Konten unter dem Limit bei neuer Kontoart Kopfzeile anzeigen.
      if ((!kontoart.equals(kontoartAlt) || kontoartAlt == null)
          && kontoart < Kontoart.LIMIT.getKey())
      {
        // Bei AnlagenSummenKonto keinen Head anzeigen
        if (!anlagenSummenkonto || kontoart != Kontoart.ANLAGE.getKey())
        {
          // Als erstes Header Vermögen anzeigen
          if (kontoartAlt == null)
          {
            PseudoDBObject head = new PseudoDBObject();
            head.setAttribute(ART, ART_HEADER);
            head.setAttribute(GRUPPE, "Liste an Vermögen:");
            zeilen.add(head);
          }

          PseudoDBObject kontoarthead = new PseudoDBObject();
          kontoarthead.setAttribute(ART, ART_HEADER);
          kontoarthead.setAttribute(GRUPPE,
              Kontoart.getByKey(kontoart).getTextVermoegen());
          zeilen.add(kontoarthead);
        }
        kontoartAlt = kontoart;
      }
      // Bei Konten über dem Limit nach Klasse gruppieren.
      if (!kontoklasse.equals(kontoklasseAlt)
          && kontoart > Kontoart.LIMIT.getKey())
      {
        PseudoDBObject kontoarthead = new PseudoDBObject();
        kontoarthead.setAttribute(ART, ART_HEADER);
        kontoarthead.setAttribute(GRUPPE, "Rücklagen " + kontoklasse);
        zeilen.add(kontoarthead);
        kontoklasseAlt = kontoklasse;
        kontoartAlt = kontoart;
        summeAnlageklasse = 0d;
      }

      // Das Konto anzeigen
      // Bei AnlagenSummenKonto nicht anzeigen
      if (!anlagenSummenkonto || kontoart != Kontoart.ANLAGE.getKey())
      {
        // Vom Kommentar nur die erste Zeile
        String kommentar = (String) o.getAttribute(KOMMENTAR);
        if (kommentar != null && kommentar.contains("\n"))
        {
          kommentar = kommentar.substring(0, kommentar.indexOf("\n"));
          o.setAttribute(KOMMENTAR, kommentar);
        }
        o.setAttribute(ART, ART_DETAIL);
        zeilen.add(o);
      }

      summeKontoart += o.getDouble(BETRAG);
      summeAnlageklasse += o.getDouble(BETRAG);

      // Summen berechnen
      if (kontoart == Kontoart.ANLAGE.getKey() && o.getInteger(ZWECK) != null
          && o.getInteger(ZWECK) == Anlagenzweck.NUTZUNGSGEBUNDEN.getKey())
      {
        summeAnlagenGebunden += o.getDouble(BETRAG);
      }
      else if (kontoart == Kontoart.SCHULDEN.getKey())
      {
        summeFremdkapital += o.getDouble(BETRAG);
      }
    }

    // Gesamt-Salden anzeigen
    PseudoDBObject gesamtSaldo = new PseudoDBObject();
    gesamtSaldo.setAttribute(ART, ART_SALDOFOOTER);
    gesamtSaldo.setAttribute(GRUPPE,
        "Summe der zeitnahen Verwendung entzogenen Mittel");
    gesamtSaldo.setAttribute(SUMME, summeFreieMittel);
    zeilen.add(gesamtSaldo);

    PseudoDBObject leer = new PseudoDBObject();
    leer.setAttribute(ART, ART_LEERZEILE);
    zeilen.add(leer);

    PseudoDBObject rueckstand = new PseudoDBObject();
    rueckstand.setAttribute(ART, ART_SALDOFOOTER);
    rueckstand.setAttribute(GRUPPE,
        "Verwendungsrückstand(+)/-überhang(-) zum Ende des GJ");
    rueckstand.setAttribute(SUMME, summeVermoegen - summeFreieMittel);
    zeilen.add(rueckstand);

    return zeilen;
  }

  /**
   * Holt den Iterator, auf dessen Basis die SaldoListe erstellt wird.
   * 
   * @return der Iterator
   * @throws RemoteException
   */
  protected ExtendedDBIterator<PseudoDBObject> getIterator()
      throws RemoteException
  {
    final boolean unterdrueckung = (Boolean) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG);

    ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>("konto");
    it.addColumn("konto.kontoart as " + KONTOART);
    it.addColumn("konto.bezeichnung as " + BEZEICHNUNG);
    // Nur die erste Zeile des Kommentars
    it.addColumn("konto.kommentar as " + KOMMENTAR);
    it.addColumn("buchungsklasse.bezeichnung as " + BUCHUNGSKLASSE);
    it.addColumn("konto.zweck as " + ZWECK);
    it.addColumn(
        "COALESCE(anfangsbestand.betrag,0) + COALESCE(sum(buchung.betrag),0) as "
            + BETRAG);

    // join Anfangsbestand und Buchung für Summe.
    it.leftJoin("anfangsbestand",
        "anfangsbestand.konto = konto.id AND anfangsbestand.datum = ?",
        getDatumvon().getDate());
    it.leftJoin("buchung",
        "buchung.konto = konto.id AND buchung.datum >= ? AND buchung.datum <= ?",
        getDatumvon().getDate(), getDatumbis().getDate());
    it.leftJoin("buchungsklasse", "buchungsklasse.id = konto.anlagenklasse");

    // Nur aktive Konten
    it.addFilter("konto.aufloesung IS NULL OR konto.aufloesung >= ?",
        getDatumvon().getDate());
    it.addFilter("konto.eroeffnung IS NULL OR konto.eroeffnung <= ?",
        getDatumbis().getDate());
    it.addFilter("konto.kontoart < ?", Kontoart.LIMIT_RUECKLAGE.getKey());

    // nach Konto gruppieren
    it.addGroupBy("konto.id");
    it.addGroupBy("konto.kontoart");
    it.addGroupBy("konto.bezeichnung");
    it.addGroupBy("konto.kommentar");
    it.addGroupBy("konto.zweck");
    it.addGroupBy("buchungsklasse.bezeichnung");
    it.addGroupBy("anfangsbestand.betrag");

    // nach Kontenart und Anlagenklasse sortieren
    it.setOrder("ORDER BY konto.kontoart, konto.anlagenklasse, konto.nummer");

    // Ggf. Konten ausblenden
    if (unterdrueckung)
    {
      it.addHaving("abs(" + BETRAG + ") >= 0.01");
    }
    return it;
  }

  // Überschrieben um beide TableParts neu zu laden.
  @Override
  public void reloadList() throws ApplicationException
  {
    try
    {
      // Daten in Einstellungen Speichern
      if (getDatumvon().getDate() != null)
      {
        settings.setAttribute("von",
            new JVDateFormatTTMMJJJJ().format(getDatumvon().getDate()));
        settings.setAttribute("bis",
            new JVDateFormatTTMMJJJJ().format(getDatumbis().getDate()));
      }

      ArrayList<PseudoDBObject> zeile = getMittelverwendungFlowList(
          getDatumvon().getDate(), getDatumbis().getDate());
      getMittelverwendungFlowTable().removeAll();
      for (PseudoDBObject sz : zeile)
      {
        getMittelverwendungFlowTable().addItem(sz);
      }

      zeile = getMittelverwendungSaldoList();
      getMittelverwendungSaldoTable().removeAll();
      for (PseudoDBObject sz : zeile)
      {
        getMittelverwendungSaldoTable().addItem(sz);
      }
      updateConfigButton();
    }
    catch (RemoteException re)
    {
      throw new ApplicationException("Fehler bei neu laden der Liste.");
    }
  }

  /**
   * Setzt den ausgewählten TAB
   * 
   * @param tab
   */
  public void setSelectedTab(int tab)
  {
    selectedTab = tab;
  }

  @Override
  protected String getDateiname()
  {
    if (selectedTab == 0)
    {
      return VorlageUtil
          .getName(VorlageTyp.MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME, this);
    }
    else
    {
      return VorlageUtil
          .getName(VorlageTyp.MITTELVERWENDUNGSREPORT_SALDO_DATEINAME, this);
    }
  }

  @Override
  protected ISaldoExport getAuswertung(String type) throws ApplicationException
  {
    switch (type)
    {
      case AuswertungCSV:
        return new MittelverwendungExportCSV(selectedTab);
      case AuswertungPDF:
        return new MittelverwendungExportPDF(selectedTab);
      default:
        throw new ApplicationException("Ausgabetyp nicht implementiert");
    }
  }

  /**
   * Liefert den Rückstand des VOrjahres
   * 
   * @return
   */
  public Double getRueckstandVorjahrNeu()
  {
    return rueckstandVorjahrNeu;
  }

  /**
   * Liefert die Zwandhafte Weitergabe
   * 
   * @return
   */
  public Double getZwanghafteWeitergabeNeu()
  {
    return zwanghafteWeitergabeNeu;
  }

}
