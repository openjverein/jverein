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
package de.jost_net.JVerein.gui.util;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.AfaMode;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.internal.parts.BackgroundTaskMonitor;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AfaUtil
{

  public AfaUtil(final Geschaeftsjahr aktuellesGJ,
      final Jahresabschluss abschluss)
  {
    BackgroundTaskMonitor monitor = new BackgroundTaskMonitor();
    try
    {
      monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
      monitor.setPercentComplete(0);
      monitor.setStatusText("Generiere Abschreibungen");

      int anzahlBuchungen = 0;
      DBService service;
      Calendar calendar = Calendar.getInstance();
      // Aktuelles Geschäftsjahr bestimmen
      calendar.setTime(aktuellesGJ.getBeginnGeschaeftsjahr());
      int ersterMonatAktuellesGJ = calendar.get(Calendar.MONTH);
      // AfA Buchungen zu Ende des aktuellen GJ
      Date afaBuchungDatum = aktuellesGJ.getEndeGeschaeftsjahr();

      service = Einstellungen.getDBService();
      DBIterator<Konto> kontenIt = service.createList(Konto.class);
      kontenIt.addFilter("kontoart = ?",
          new Object[] { Kontoart.ANLAGE.getKey() });
      kontenIt.addFilter("(eroeffnung IS NULL OR eroeffnung <= ?)",
          new Object[] { new java.sql.Date(
              aktuellesGJ.getEndeGeschaeftsjahr().getTime()) });
      kontenIt.addFilter("(aufloesung IS NULL OR aufloesung >= ?)",
          new Object[] { new java.sql.Date(
              aktuellesGJ.getBeginnGeschaeftsjahr().getTime()) });
      while (kontenIt.hasNext())
      {
        Konto konto = kontenIt.next();
        if (konto.getAfaMode() == null)
        {
          monitor.setStatusText(
              "Konto " + konto.getNummer() + ": Afa Mode nicht gesetzt");
          continue;
        }
        if (konto.getAfaMode() != null && konto.getAfaMode() == AfaMode.MANUELL)
        {
          monitor.setStatusText("Konto " + konto.getNummer()
              + ": Afa Mode ist manuell, keine automatische Generierung");
          continue;
        }
        switch (konto.getAfaMode())
        {
          case AfaMode.ANGEPASST:
            anzahlBuchungen += doAbschreibungAngepasst(konto, aktuellesGJ,
                ersterMonatAktuellesGJ, afaBuchungDatum, abschluss, monitor);
            break;
          case AfaMode.AUTO:
            anzahlBuchungen += doAbschreibungAuto(konto, aktuellesGJ,
                ersterMonatAktuellesGJ, afaBuchungDatum, abschluss, monitor);
            break;
        }
      }
      monitor.setPercentComplete(100);
      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setStatusText(
          String.format("Anzahl generierter Buchungen: %d", anzahlBuchungen));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    catch (OperationCanceledException oce)
    {
      if (monitor != null)
      {
        monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
        monitor.setPercentComplete(100);
        String msg = oce.getMessage();
        monitor.setStatusText(msg != null ? msg
            : Application.getI18n().tr("Vorgang abgebrochen"));
      }
    }
    catch (ApplicationException ae)
    {
      if (monitor != null)
      {
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
        monitor.setPercentComplete(100);
        monitor.setStatusText(ae.getMessage());
      }
    }
  }

  private int doAbschreibungAngepasst(Konto konto, Geschaeftsjahr aktuellesGJ,
      int ersterMonatAktuellesGJ, Date afaBuchungDatum,
      Jahresabschluss abschluss, ProgressMonitor monitor)
      throws RemoteException, ParseException, ApplicationException
  {
    if (checkKonto(konto, monitor))
      return 0;
    if (checkAfa(konto, monitor))
      return 0;
    int anschaffungsJahr;
    int monatAnschaffung;
    Calendar calendar = Calendar.getInstance();
    Geschaeftsjahr anschaffungGJ = new Geschaeftsjahr(konto.getAnschaffung());
    anschaffungsJahr = anschaffungGJ.getBeginnGeschaeftsjahrjahr();
    calendar.setTime(konto.getAnschaffung());
    monatAnschaffung = calendar.get(Calendar.MONTH);
    int aktuellesJahr = aktuellesGJ.getBeginnGeschaeftsjahrjahr();
    // Check ob ausserhalb des Abschreibungszeitraums
    if (aktuellesJahr < anschaffungsJahr
        || aktuellesJahr > anschaffungsJahr + konto.getNutzungsdauer())
      return 0;
    // Check ob Anschaffung im ersten Monat des GJ, dann keine Restabschreibung
    // Wenn Nutzungsdauer 0 dann direktabschreibung
    if ((aktuellesJahr == anschaffungsJahr + konto.getNutzungsdauer()
        && ersterMonatAktuellesGJ == monatAnschaffung)
        && konto.getNutzungsdauer() != 0)
      return 0;

    int monate = getMonths(konto.getAnschaffung(),
        anschaffungGJ.getEndeGeschaeftsjahr());

    Buchung buchung = (Buchung) Einstellungen.getDBService()
        .createObject(Buchung.class, null);
    double buchwert = getBuchwert(konto, anschaffungsJahr, aktuellesJahr,
        aktuellesGJ, monitor);
    double restbuchwert = konto.getAfaRestwert();
    double betrag = 0d;

    // GWGs voll abschreiben
    if (aktuellesJahr == anschaffungsJahr && konto.getNutzungsdauer() == 0)
    {
      if (buchwert <= 0d)
        return 0; // bereits abgeschrieben
      betrag = konto.getBetrag();
      if (betrag > buchwert)
        betrag = buchwert;
      buchung.setBetrag(-betrag);
      buchung.setZweck("GWG-Abschreibung");
    }

    if (konto.getNutzungsdauer() > 0)
    {
      if (buchwert <= konto.getAfaRestwert())
        return 0; // bereits abgeschrieben

      if (aktuellesJahr == anschaffungsJahr)
      {
        betrag = konto.getAfaStart();
        if ((buchwert - restbuchwert) < betrag)
          betrag = buchwert - restbuchwert;
        buchung.setZweck("Anteilige Abschreibung für " + monate + " Monate");
      }
      else
      {
        betrag = konto.getAfaDauer();
        if (betrag < buchwert - restbuchwert)
        {
          buchung.setZweck("Abschreibung");
        }
        else
        {
          betrag = buchwert - restbuchwert;
          buchung.setZweck("Restwertbuchung");
        }
      }
    }
    buchung.setKonto(konto);
    buchung.setName((String) Einstellungen.getEinstellung(Property.NAME));
    buchung.setDatum(afaBuchungDatum);
    buchung.setBetrag(-betrag);
    buchung.setBuchungsartId(konto.getAfaartId());
    buchung.setAbschluss(abschluss);
    if (abschluss == null)
      buchung.store();
    else
      buchung.updateForced();
    monitor
        .setStatusText("Konto " + konto.getNummer() + ": AfA Buchung erzeugt");
    return 1;
  }

  private int doAbschreibungAuto(Konto konto, Geschaeftsjahr aktuellesGJ,
      int ersterMonatAktuellesGJ, Date afaBuchungDatum,
      Jahresabschluss abschluss, ProgressMonitor monitor)
      throws RemoteException, ParseException, ApplicationException
  {
    if (checkKonto(konto, monitor))
      return 0;
    int anschaffungsJahr;
    int monatAnschaffung;
    Calendar calendar = Calendar.getInstance();
    Geschaeftsjahr anschaffungGJ = new Geschaeftsjahr(konto.getAnschaffung());
    anschaffungsJahr = anschaffungGJ.getBeginnGeschaeftsjahrjahr();
    calendar.setTime(konto.getAnschaffung());
    monatAnschaffung = calendar.get(Calendar.MONTH);
    int aktuellesJahr = aktuellesGJ.getBeginnGeschaeftsjahrjahr();
    // Check ob ausserhalb des Abschreibungszeitraums
    if (aktuellesJahr < anschaffungsJahr
        || aktuellesJahr > anschaffungsJahr + konto.getNutzungsdauer())
      return 0;
    // Check ob Anschaffung im ersten Monaz des GJ, dann keine Restabschreibung
    // Wenn Nutzungsdauer 0 dann Direktabschreibung
    if ((aktuellesJahr == anschaffungsJahr + konto.getNutzungsdauer()
        && ersterMonatAktuellesGJ == monatAnschaffung)
        && konto.getNutzungsdauer() != 0)
      return 0;

    double buchwert = getBuchwert(konto, anschaffungsJahr, aktuellesJahr,
        aktuellesGJ, monitor);
    double restbuchswert = konto.getAfaRestwert();
    double betrag = 0d;
    String zweck = "Abschreibung";

    // Datum des Nutzungsende ermitteln
    final Calendar cal = Calendar.getInstance();
    cal.setTime(konto.getAnschaffung());
    cal.add(Calendar.YEAR, konto.getNutzungsdauer());
    final Date nutzungsende = cal.getTime();

    // GWGs voll abschreiben
    if (konto.getNutzungsdauer() == 0)
    {
      if (buchwert <= 0d)
        return 0; // bereits abgeschrieben
      zweck = "GWG-Abschreibung";
      betrag = konto.getBetrag();
      if (betrag > buchwert)
        betrag = buchwert;
    }
    else
    {
      if (buchwert <= restbuchswert)
        return 0; // bereits abgeschrieben

      // Im Anschaffungsjahr haben wir die volle Restlaufzeit.
      int restnutzungsdauer = 0;
      if (aktuellesJahr == anschaffungsJahr)
      {
        restnutzungsdauer = Math
            .max(getMonths(konto.getAnschaffung(), nutzungsende) - 1, 0);
        // Ein Monat abziehen, weil der letzte nicht mitzaehlt
      }
      else
      {
        restnutzungsdauer = Math.max(
            getMonths(aktuellesGJ.getBeginnGeschaeftsjahr(), nutzungsende) - 1,
            0);
        // Ein Monat abziehen, weil der letzte nicht mitzaehlt
      }

      double abbetrag = (buchwert - restbuchswert) / (restnutzungsdauer / 12d);
      betrag = Math.ceil(abbetrag);

      // Anteilig abschreiben, wenn wir uns im Anschaffungsjahr befinden
      if (aktuellesJahr == anschaffungsJahr)
      {
        int months = getMonths(konto.getAnschaffung(),
            aktuellesGJ.getEndeGeschaeftsjahr());
        zweck = "Anteilige Abschreibung für " + months + " Monate";
        betrag = Math.ceil((abbetrag / 12d) * months);
        double kosten = konto.getBetrag();
        // Nachkommastellen der Anschaffungskosten addieren, das ergiebt einen
        // geraden Betrag für den neuen Buchwert
        betrag = betrag + (kosten - (int) kosten);
      }

      // Abzuschreibender Betrag >= Restwert -> Restwertbuchung
      if (abbetrag >= buchwert - restbuchswert)
      {
        zweck = "Restwertbuchung";
        betrag = buchwert - restbuchswert;
      }
    }

    Buchung buchung = (Buchung) Einstellungen.getDBService()
        .createObject(Buchung.class, null);
    buchung.setKonto(konto);
    buchung.setName((String) Einstellungen.getEinstellung(Property.NAME));
    buchung.setZweck(zweck);
    buchung.setDatum(afaBuchungDatum);
    buchung.setBuchungsartId(konto.getAfaartId());
    buchung.setBetrag(-betrag);
    buchung.setAbschluss(abschluss);
    if (abschluss == null)
      buchung.store();
    else
      buchung.updateForced();
    monitor
        .setStatusText("Konto " + konto.getNummer() + ": AfA Buchung erzeugt");
    return 1;
  }

  private double getBuchwert(Konto konto, int anschaffungsJahr,
      int aktuellesJahr, Geschaeftsjahr aktuellesGJ, ProgressMonitor monitor)
      throws RemoteException
  {
    double buchwert = 0d;

    if (anschaffungsJahr == aktuellesJahr)
    {
      buchwert = konto.getBetrag();
    }
    else
    {
      DBIterator<Anfangsbestand> anfangsbestand = Einstellungen.getDBService()
          .createList(Anfangsbestand.class);
      anfangsbestand.addFilter("konto = ?", new Object[] { konto.getID() });
      anfangsbestand.addFilter("datum = ?",
          new Object[] { aktuellesGJ.getBeginnGeschaeftsjahr() });
      if (!anfangsbestand.hasNext())
      {
        monitor.setStatusText("Konto " + konto.getNummer() + ": "
            + "Für das Konto existiert kein Anfangsbestand für das Geschäftsjahr");
        return 0d;
      }
      else
      {
        buchwert = ((Anfangsbestand) anfangsbestand.next()).getBetrag();
      }
    }

    // Jetzt noch AfA Buchungen des aktuellen GJ addieren
    DBIterator<Buchung> buchungsIt = Einstellungen.getDBService()
        .createList(Buchung.class);
    buchungsIt.addFilter("konto = ?", konto.getID());
    buchungsIt.join("buchungsart");
    buchungsIt.addFilter("buchungsart.id = buchung.buchungsart");
    buchungsIt.addFilter("buchungsart.abschreibung = ?", true);
    buchungsIt.addFilter("datum >= ?", aktuellesGJ.getBeginnGeschaeftsjahr());
    while (buchungsIt.hasNext())
    {
      Buchung afa = (Buchung) buchungsIt.next();
      buchwert += afa.getBetrag();
    }

    return buchwert;
  }

  private boolean checkKonto(Konto konto, ProgressMonitor monitor)
      throws RemoteException
  {
    boolean fehler = false;
    if (konto.getBetrag() == null)
    {
      monitor.setStatusText(
          "Konto " + konto.getNummer() + ": Anlagenwert nicht gesetzt");
      fehler = true;
    }
    if (konto.getAnschaffung() == null)
    {
      monitor.setStatusText(
          "Konto " + konto.getNummer() + ": Anschaffungsdatum nicht gesetzt");
      fehler = true;
    }
    if (konto.getNutzungsdauer() == null)
    {
      monitor.setStatusText(
          "Konto " + konto.getNummer() + ": Nutzungsdauer nicht gesetzt");
      fehler = true;
    }
    if (konto.getAfaRestwert() == null)
    {
      monitor.setStatusText(
          "Konto " + konto.getNummer() + ": Anlagen Restwert nicht gesetzt");
      fehler = true;
    }
    return fehler;
  }

  private boolean checkAfa(Konto konto, ProgressMonitor monitor)
      throws RemoteException
  {
    boolean fehler = false;
    if (konto.getAfaStart() == null)
    {
      monitor.setStatusText(
          "Konto " + konto.getNummer() + ": Afa Erstes Jahr nicht gesetzt");
      fehler = true;
    }
    if (konto.getAfaDauer() == null)
    {
      monitor.setStatusText(
          "Konto " + konto.getNummer() + ": Afa Folgejahr nicht gesetzt");
      fehler = true;
    }
    return fehler;
  }

  /**
   * Liefert die Anzahl der Monate von einem zum anderen Datum. Die Funktion
   * liefert immer ganze Monate incl. des Monats aus "from". Von Juli bis
   * Dezember sind also 6 Monate - egal, welche Tage des Monats. Die Funktion
   * wird fuer die jahresanteilige Abschreibung verwendet.
   * 
   * @param from
   *          Start-Datum.
   * @param end
   *          End-Datum.
   * @return die Anzahl der Monate.
   */
  private int getMonths(final Date from, final Date end)
  {
    if (from == null || end == null)
      return 0;

    int count = 0;
    final Calendar cal = Calendar.getInstance();
    cal.setTime(from);
    while (count < 1000) // Groessere Zeitraeume waren Unsinn.
    {
      Date test = cal.getTime();
      if (test.after(end))
        return count;

      cal.add(Calendar.MONTH, 1);
      count++;
    }

    return count;
  }

}
