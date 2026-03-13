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
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.GutschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.control.GutschriftControl;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.VorlageUtil;
import de.jost_net.OBanToo.SEPA.Basislastschrift.MandatSequence;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Gutschrift extends SEPASupport
{
  private final Double LIMIT = 0.005;

  private static final String SKIP = "====> Überspringe ";

  private static final String MARKER = "-> ";

  private ArrayList<Lastschrift> ueberweisungen = new ArrayList<>();

  private GutschriftParam params = null;

  private int erstellt = 0;

  private int skip = 0;

  private Konto konto = null;

  private double summe = 0d;

  private Abrechnungslauf abrl = null;

  private File file = null;

  private Settings settings = null;

  private JVereinDBService service;

  public Gutschrift(GutschriftControl gcontrol) throws Exception
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    service = Einstellungen.getDBService();
    this.params = gcontrol.getParams();

    if (params.getDatum() == null || params.getVerwendungszweck() == null
        || params.getVerwendungszweck().isEmpty()
        || (params.isRechnungErzeugen() && (params.getFormular() == null
            || params.getRechnungsDatum() == null))
        || (params.isFixerBetragAbrechnen() && (params.getFixerBetrag() == null
            || params.getFixerBetrag() < LIMIT)))
    {
      throw new ApplicationException("Eingabeparameter fehlerhaft!");
    }

    konto = getKonto();

    // Datei für SEPA Ausgabe holen
    if (params.getAusgabe() == UeberweisungAusgabe.SEPA_DATEI)
    {
      file = getFile();
      if (file == null)
      {
        throw new ApplicationException("Keine Datei ausgewählt!");
      }
    }

    BackgroundTask t = new BackgroundTask()
    {
      private boolean interrupted = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          DBTransaction.starten();
          monitor.setStatusText("Starte die Generierung der Gutschriften");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);

          // Abrechnungslauf erzeugen damit man die Lastschrift speichern kann
          // was
          // wegen der Map nötig ist. Es hat auch noch den Vorteil, dass man ihn
          // löschen kann und damit alle generierten Sollbuchungen und Buchungen
          abrl = (Abrechnungslauf) Einstellungen.getDBService()
              .createObject(Abrechnungslauf.class, null);
          abrl.setDatum(new Date());
          abrl.setModus(Abrechnungsmodi.GUTSCHRIFT);
          abrl.setFaelligkeit(params.getDatum());
          abrl.setStichtag(params.getDatum());
          abrl.setZahlungsgrund(params.getVerwendungszweck());
          abrl.setAbgeschlossen(false);
          abrl.store();

          for (IGutschriftProvider provider : gcontrol.getProviderArray())
          {
            if (isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
              monitor.setStatusText("Generierung abgebrochen.");
              monitor.setPercentComplete(100);
              throw new OperationCanceledException();
            }

            monitor.setPercentComplete(
                100 * (erstellt + skip) / gcontrol.getProviderArray().length);

            String statustext = "";
            String name = "";

            statustext = provider.getObjektName() + " mit Nr. "
                + provider.getID();

            String bug = gcontrol.doChecks(provider, params, null);
            if (bug != null)
            {
              skip++;
              monitor.setStatusText(SKIP + statustext + " " + bug);
              continue;
            }

            if (provider instanceof Lastschrift
                && provider.getGutschriftZahler() == null)
            {
              name = Adressaufbereitung.getNameVorname((IAdresse) provider);
            }
            else if (provider.getGutschriftZahler() != null)
            {
              name = Adressaufbereitung
                  .getNameVorname(provider.getGutschriftZahler());
            }

            // Beträge bestimmen
            // Überweisungsbetrag ist maximal der bereits eingezahlte Betrag
            // bzw. Providerbetrag
            double ueberweisungsbetrag = Math.min(provider.getBetrag(),
                provider.getIstSumme());
            // Offenbetrag ist der Fehlbetrag des aktuellen Providers
            double offenbetrag = Math
                .max(provider.getBetrag() - provider.getIstSumme(), 0);
            // Ausgleichbetrag ist der Betrag der beim Provider mit einer
            // Buchung ausgeglichen werden muss damit dieser dann nicht
            // mehr als Fehlbetrag gelistet wird
            double ausgleichsbetrag = offenbetrag;
            if (params.isFixerBetragAbrechnen())
            {
              // Der Überweisungsbetrag wird mit offenen Beträgen verrechnet. Es
              // wird nur etwas überwiesen wenn der fixe Betrag den offenen
              // Betrag überschreitet
              ueberweisungsbetrag = Math
                  .max(params.getFixerBetrag() - offenbetrag, 0);
              // Es muss der Betrag beim Provider ausgeglichen werden der mit
              // dem fixen Betrag verrechnet wurde. Dieser gilt ja durch die
              // Gutschrift als bezahlt
              ausgleichsbetrag = params.getFixerBetrag() - ueberweisungsbetrag;
            }

            monitor.setStatusText("Generiere Gutschrift für " + statustext
                + " und Zahler " + name + ".");

            // Sollbuchung, Buchungen und Lastschriften erzeugen
            Buchung buchung = generiereGutschrift(provider, ueberweisungsbetrag,
                ausgleichsbetrag, name, monitor);
            if (ausgleichsbetrag > LIMIT)
            {
              sollbuchungenAusgleich(provider, ausgleichsbetrag,
                  ueberweisungsbetrag, buchung, monitor);
            }
            erstellt++;
          }

          if (summe > LIMIT)
          {
            // Gegenbuchung erstellen
            getBuchung(summe, "JVerein", "Gegenbuchung", "", "").store();
            monitor.setStatusText("Gegenbuchung erzeugt");

            // Überweisung erstellen
            // Wenn keine Datei ausgewählt wurde, dann wird keine generiert
            Ueberweisung ueberweisung = new Ueberweisung(null);
            ueberweisung.write(ueberweisungen, file, params.getDatum(),
                params.getAusgabe(), null);
            if (params.getAusgabe() == UeberweisungAusgabe.HIBISCUS)
            {
              monitor.setStatusText("SEPA Auftrag an Hibiscus übergeben");
            }
            else
            {
              // Dateiausgabe
              monitor.setStatusText("SEPA Datei erzeugt");
            }

          }

          monitor.setPercentComplete(100);
          if (skip > 0)
          {
            monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          }
          else
          {
            monitor.setStatus(ProgressMonitor.STATUS_DONE);
          }

          if (erstellt == 0)
          {
            monitor.setStatusText("Keine Gutschrift erstellt: "
                + (skip > 0 ? skip + " übersprungen." : ""));
          }
          else
          {
            GUI.getCurrentView().reload();
            monitor.setStatusText(erstellt + " Gutschrift(en) erstellt"
                + (skip > 0 ? ", " + skip + " übersprungen." : "."));
          }
          DBTransaction.commit();
        }
        catch (OperationCanceledException oe)
        {
          DBTransaction.rollback();
          throw oe;
        }
        catch (ApplicationException ae)
        {
          DBTransaction.rollback();
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
        catch (Exception e)
        {
          DBTransaction.rollback();
          Logger.error("Fehler beim Gutschrift erstellen", e);
          GUI.getStatusBar().setErrorText(
              "Fehler beim Gutschrift erstellen: " + e.getMessage());
          throw new ApplicationException("Fehler beim Gutschrift erstellen", e);
        }
      }

      @Override
      public void interrupt()
      {
        interrupted = true;
      }

      @Override
      public boolean isInterrupted()
      {
        return interrupted;
      }

    };
    Application.getController().start(t);
  }

  /**
   * Die Methode generiert die Überweisung, Sollbuchung, Buchung, Rechnung und
   * Buchungsdokument.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param ueberweisungsbetrag
   *          Der Betrag der überwiesen werden soll.
   * @param ausgleichsbetrag
   *          Der Betrag der beim Provider mit einer Buchung ausgeglichen werden
   *          muss, damit dieser dann nicht mehr als Fehlbetrag gelistet wird.
   * @param name
   *          Der Name der Person für die die Gutschrift estellt wird.
   * @param monitor
   *          Monitor für die Meldungsausgabe.
   */
  private Buchung generiereGutschrift(IGutschriftProvider prov,
      double ueberweisungsbetrag, double ausgleichsbetrag, String name,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    String zweck = params.getVerwendungszweck();
    Rechnung rechnung = null;
    Sollbuchung sollbuchung = null;
    Lastschrift ueberweisung = null;
    Buchung buchung = null;

    // Überweisungen erzeugen
    ueberweisung = generiereUeberweisung(prov, zweck, ueberweisungsbetrag);
    if (ueberweisungsbetrag > LIMIT)
    {
      ueberweisungen.add(ueberweisung);
      summe += ueberweisungsbetrag;
      monitor.setStatusText(MARKER + "Überweisung erzeugt");
    }

    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = new GutschriftMap().getMap(ueberweisung, map);
    if (ueberweisung.getMitglied() != null)
    {
      boolean ohneLesefelder = !zweck.contains(Einstellungen.LESEFELD_PRE);
      map = new MitgliedMap().getMap(ueberweisung.getMitglied(), map,
          ohneLesefelder);
    }
    try
    {
      zweck = VelocityTool.eval(map, params.getVerwendungszweck());
      if (zweck.length() >= 140)
      {
        zweck = zweck.substring(0, 136) + "...";
      }
      ueberweisung.setVerwendungszweck(zweck);
    }
    catch (IOException e)
    {
      Logger.error("Fehler bei der Aufbereitung der Variablen", e);
    }

    double betrag = 0;
    if (params.isFixerBetragAbrechnen())
    {
      betrag = -params.getFixerBetrag();
    }
    else
    {
      betrag = -prov.getBetrag();
    }
    // Sollbuchung nur wenn Mitglied und Zahler vorhanden, z.B. nicht bei
    // Kursteilnehmer
    if (prov.getMitglied() != null && prov.getGutschriftZahler() != null)
    {
      sollbuchung = generiereSollbuchung(prov, betrag, zweck);
      monitor.setStatusText(MARKER + "Sollbuchung erzeugt");
    }

    // Rechnung erzeugen
    if (params.isRechnungErzeugen() && sollbuchung != null
        && (Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      rechnung = generiereRechnung(prov, ueberweisungsbetrag, sollbuchung);
      monitor.setStatusText(MARKER + "Rechnung erzeugt");
      if (params.getRechnungsText() != null
          && params.getRechnungsText().trim().length() > 0)
      {
        zweck = params.getRechnungsText();
        Map<String, Object> rmap = new AllgemeineMap().getMap(null);
        rmap = new GutschriftMap().getMap(ueberweisung, rmap);
        boolean ohneLesefelder = !zweck.contains(Einstellungen.LESEFELD_PRE);
        rmap = new MitgliedMap().getMap(sollbuchung.getZahler(), rmap,
            ohneLesefelder);
        rmap = new RechnungMap().getMap(rechnung, rmap);
        try
        {
          zweck = VelocityTool.eval(rmap, zweck);
          if (zweck.length() >= 140)
          {
            zweck = zweck.substring(0, 136) + "...";
          }
        }
        catch (IOException e)
        {
          Logger.error("Fehler bei der Aufbereitung der Variablen", e);
        }
        sollbuchung.setZweck1(zweck);
      }
      sollbuchung.setRechnung(rechnung);
      sollbuchung.updateForced();

      rechnung.setRechnungstext(zweck);
      rechnung.store();
    }

    // Buchung erzeugen
    String art = "";
    // Wenn voll gezahlt ist gibt es keinen Ausgleich, dann ist das hier die
    // Überweisung
    if (ausgleichsbetrag <= LIMIT)
    {
      art = "Überweisung";
    }
    buchung = generiereBuchung(prov, betrag, name, zweck, sollbuchung, art);
    monitor.setStatusText(MARKER + "Buchung erzeugt");

    // Buchungsdokument erzeugen
    if (params.isRechnungsDokumentSpeichern() && rechnung != null
        && buchung != null)
    {
      generiereBuchungsdokument(prov, buchung, rechnung);
      monitor.setStatusText(MARKER + "Buchungsdokument erzeugt");
    }
    return (buchung);
  }

  /**
   * Die Methode generiert die Überweisung. Es wurde für eine Überweisung keine
   * neu Klasse eingeführt sondern die Klasse für Lastschrift wieder verwendet.
   * Lastschrift hält die Daten für die Überweisung.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param zweck
   *          Der Verwendungszweck in der Überweisung.
   * @param betrag
   *          Der Betrag der überwiesen werden soll.
   */
  private Lastschrift generiereUeberweisung(IGutschriftProvider prov,
      String zweck, double betrag) throws RemoteException, ApplicationException
  {
    ILastschrift ila;
    // Überweisungen erstellen
    // Lastschrift wird als Datenklasse für die Überweisung benutzt
    Lastschrift ls = (Lastschrift) service.createObject(Lastschrift.class,
        null);
    if (prov.getGutschriftZahler() == null)
    {
      // Dann muss es eine Lastschrift sein bei dem kein Mitglied gesetzt ist
      Kursteilnehmer k = ((Lastschrift) prov).getKursteilnehmer();
      ls.setKursteilnehmer(Integer.parseInt(k.getID()));
      ila = (Lastschrift) prov;
    }
    else
    {
      Mitglied m = prov.getGutschriftZahler();
      ls.setMitglied(Integer.parseInt(m.getID()));
      ila = m;
    }

    ls.setEmail(ila.getEmail());
    ls.setBic(ila.getBic());
    ls.setIban(ila.getIban());
    ls.setMandatDatum(ila.getMandatDatum());
    ls.setMandatID(ila.getMandatID());
    ls.setVerwendungszweck(zweck);
    ls.setBetrag(betrag);
    ls.setAbrechnungslauf(Integer.valueOf(abrl.getID()));
    // Wird bei Überweisung nicht gebraucht aber wegen der Map implementierung
    // gesetzt
    ls.setMandatSequence(MandatSequence.RCUR.getTxt());
    ls.set(ila);

    return ls;
  }

  /**
   * Die Methode generiert die Sollbuchung. Ist der Provider eine Rechnung,
   * Sollbuchung oder Lastschrift, wird bei nicht fixem Betrag der negative
   * Soll-Betrag des Providers als Betrag in die Sollbuchung aufgenommen. Bei
   * fixem Betrag ist es der negative fixe Betrag.
   * 
   * Bei nicht fixem Betrag und Rechnung oder Sollbuchung werden die
   * Sollbuchungspositionen mit ihrem negativen Betrag in die Sollbuchung für
   * die Gutschrift übernommen.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param zweck
   *          Der Verwendungszweck in der Überweisung.
   * @param betrag
   *          Der Betrag der überwiesen werden soll.
   */
  private Sollbuchung generiereSollbuchung(IGutschriftProvider prov,
      double betrag, String zweck) throws RemoteException, ApplicationException
  {
    Sollbuchung sollbuchung = null;
    // Sollbuchung mit negativem bereits bezahltem Betrag
    sollbuchung = (Sollbuchung) service.createObject(Sollbuchung.class, null);
    sollbuchung.setBetrag(betrag);
    sollbuchung.setDatum(params.getDatum());
    sollbuchung.setMitglied(prov.getMitglied());
    sollbuchung.setZahler(prov.getGutschriftZahler());
    sollbuchung.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
    sollbuchung.setZweck1(zweck);
    sollbuchung.setAbrechnungslauf(abrl);
    sollbuchung.store();

    // Sollbuchungspositionen erstellen
    // Mitglied hat immer fixen Betrag
    if (params.isFixerBetragAbrechnen() || prov instanceof Lastschrift)
    {
      SollbuchungPosition sbp = (SollbuchungPosition) service
          .createObject(SollbuchungPosition.class, null);
      sbp.setBetrag(betrag);
      sbp.setBuchungsartId(params.getBuchungsart() != null
          ? Long.valueOf(params.getBuchungsart().getID())
          : null);
      sbp.setBuchungsklasseId(params.getBuchungsklasse() != null
          ? Long.valueOf(params.getBuchungsklasse().getID())
          : null);
      sbp.setSteuer(params.getSteuer());
      sbp.setDatum(params.getDatum());
      sbp.setZweck(zweck);
      sbp.setSollbuchung(sollbuchung.getID());
      sbp.store();
    }
    else
    {
      // Bei Rechnung und Sollbuchung nehmen wir die negativen Positionen
      List<SollbuchungPosition> positionen = prov.getSollbuchungPositionList();
      for (SollbuchungPosition sp : positionen)
      {
        SollbuchungPosition sbp = (SollbuchungPosition) service
            .createObject(SollbuchungPosition.class, null);
        sbp.setBetrag(-sp.getBetrag());
        sbp.setBuchungsartId(sp.getBuchungsartId());
        sbp.setBuchungsklasseId(sp.getBuchungsklasseId());
        sbp.setSteuer(sp.getSteuer());
        sbp.setDatum(params.getDatum());
        sbp.setZweck(sp.getZweck());
        sbp.setSollbuchung(sollbuchung.getID());
        sbp.store();
      }
    }
    return sollbuchung;
  }

  /**
   * Die Methode generiert die Rechnung für die Gutschrift. Falls die Gutschrift
   * für eine bestehende Rechnung ist, oder für eine Sollbuchung, der eine
   * Rechnung zugeordnet ist, wird die bestehende Rechnung in das Referenz
   * Attribut der neuen Rechnung eingetragen. Damit kann im Formular für die
   * Rechnung die bestehende Rechnung (Rechnungsnummer) referenziert werden.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param ueberweisungsbetrag
   *          Der Betrag der bei der Gutschrift überwiesen wird. Dieser wird im
   *          Attribut Erstattungsbetrag der Rechnung gespeichert. Er kann dann
   *          im Formular für die Rechnung verwendet werden.
   * @param sollbuchung
   *          Sollbuchung für die eine Rechnung erzeugt werden soll.
   */
  private Rechnung generiereRechnung(IGutschriftProvider prov,
      double ueberweisungsbetrag, Sollbuchung sollbuchung)
      throws RemoteException, ApplicationException
  {
    Rechnung rechnung = null;

    rechnung = (Rechnung) service.createObject(Rechnung.class, null);
    rechnung.setFormular(params.getFormular());
    rechnung.setDatum(params.getRechnungsDatum());
    rechnung.fill(sollbuchung);
    rechnung.setKommentar(params.getRechnungsKommentar());
    rechnung.setErstattungsbetrag(ueberweisungsbetrag);
    if (prov instanceof Rechnung)
    {
      rechnung.setReferenzrechnungID(Long.valueOf(prov.getID()));
    }
    else if (prov instanceof Sollbuchung)
    {
      Sollbuchung sollb = (Sollbuchung) prov;
      if (sollb.getRechnung() != null)
      {
        rechnung
            .setReferenzrechnungID(Long.valueOf(sollb.getRechnung().getID()));
      }
    }
    rechnung.store();
    return rechnung;
  }

  /**
   * Die Methode generiert eine Buchung für die Gutschrift. Bei einem fixen
   * Betrag werden Buchungsart, Buchungsklasse und Steuer aus den eingegebenen
   * Daten des Gutschrift Dialog genommen. Bei Sollbuchungen und Rechnungen
   * werden die Werte aus der ersten Sollbuchungsposition genommen. Bei
   * Lastschriften werden diese Werte nicht gesetzt, da keine Information
   * darüber verfügbar ist.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param betrag
   *          Der Betrag der Buchung.
   * @param name
   *          Der Name der Person für die die Gutschrift estellt wird.
   * @param zweck
   *          Der Verwendungszweck in der Buchung.
   * @param sollbuchung
   *          Sollbuchung an die die Buchung zugeordnet wird.
   * @param art
   *          Buchunsart für die Buchung. Bei Erstattungsbuchungen wird hier
   *          "Überweisung" eingetragen
   */
  private Buchung generiereBuchung(IGutschriftProvider prov, double betrag,
      String name, String zweck, Sollbuchung sollbuchung, String art)
      throws RemoteException, ApplicationException
  {
    // Buchung erzeugen
    Buchung buchung = getBuchung(betrag, name, zweck, getIBAN(prov), art);
    if (params.isFixerBetragAbrechnen() || prov instanceof Lastschrift)
    {
      buchung.setBuchungsartId(params.getBuchungsart() != null
          ? Long.valueOf(params.getBuchungsart().getID())
          : null);
      buchung.setBuchungsklasseId(params.getBuchungsklasse() != null
          ? Long.valueOf(params.getBuchungsklasse().getID())
          : null);
      buchung.setSteuer(params.getSteuer());
    }
    else
    {
      // Sollbuchung oder Rechnung
      List<SollbuchungPosition> positionen = prov.getSollbuchungPositionList();
      if (positionen != null && positionen.size() > 0)
      {
        SollbuchungPosition pos = positionen.get(0);
        buchung.setBuchungsartId(pos.getBuchungsartId());
        buchung.setBuchungsklasseId(pos.getBuchungsklasseId());
        buchung.setSteuer(pos.getSteuer());
      }
    }

    // Nicht splitten bei fixen Betrag und Lastschrift (nur einer Position
    // vorhanden)
    if (!(params.isFixerBetragAbrechnen() || prov instanceof Lastschrift))
    {
      buchung.store();
      SplitbuchungsContainer.autoSplit(buchung, sollbuchung, false);
    }
    else
    {
      buchung.setSollbuchung(sollbuchung);
      buchung.store();
    }
    return buchung;
  }

  /**
   * Die Methode generiert für eine Buchung die Rechnung als Buchungsdokument.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll. Es wird benutzt um die MitgliedMap für den Zahler zu
   *          erzeugen.
   * @param buchung
   *          Die Buchung bei der die Rechnung hinterlegt werden soll.
   * @param rechnung
   *          Die Rechnung die bei der Buchung hinterlegt werden soll.
   */
  private void generiereBuchungsdokument(IGutschriftProvider prov,
      Buchung buchung, Rechnung rechnung)
      throws RemoteException, ApplicationException
  {
    Map<String, Object> rmap = new AllgemeineMap().getMap(null);
    rmap = new MitgliedMap().getMap(prov.getGutschriftZahler(), rmap);
    rmap = new RechnungMap().getMap(rechnung, rmap);
    storeBuchungsDokument(rechnung, buchung, params.getDatum(), rmap);
  }

  /**
   * Die Methode ruft den Buchungsausgleich für die jeweilige bestehende
   * Sollbuchung auf. Bei Gesamtrechnungen wird dies für jede Sollbuchung der
   * Rechnung gemacht. Bei fixem Betrag werden keine Gesamtrechnungen
   * unterstützt.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param ueberweisungsbetrag
   *          Der Betrag der überwiesen werden soll.
   * @param ausgleichsbetrag
   *          Der Betrag der beim Provider mit einer Buchung ausgeglichen werden
   *          muss, damit dieser dann nicht mehr als Fehlbetrag gelistet wird.
   * @param buchung
   *          Die Buchung für die Gutschrift Sollbuchung. Ihre ID wird beim
   *          Verwendungszweck der Ausgleichsbuchung verwendet um sie zu
   *          referenzieren.
   * @param monitor
   *          Monitor für die Meldungsausgabe.
   */
  private void sollbuchungenAusgleich(IGutschriftProvider provider,
      double ausgleichsbetrag, double ueberweisungsbetrag, Buchung buchung,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    // Bei fixem Betrag mit offenem Betrag verrechnen
    if (params.isFixerBetragAbrechnen())
    {
      Sollbuchung sollbFix = null;
      if (provider instanceof Sollbuchung)
      {
        sollbFix = (Sollbuchung) provider;
      }
      else if (provider instanceof Rechnung)
      {
        if (((Rechnung) provider).getSollbuchungList().size() == 1)
        {
          sollbFix = ((Rechnung) provider).getSollbuchungList().get(0);
        }
        else
        {
          throw new ApplicationException(
              "Gesamtrechnung wird nicht unterstützt!");
        }
      }
      // Sollbuchung ausgleichen
      sollbuchungAusgleich(provider, sollbFix, ausgleichsbetrag,
          ueberweisungsbetrag, buchung, monitor);
    }
    else
    {
      // Sollbuchungen ausgleichen wenn etwas offen ist
      if (provider instanceof Sollbuchung)
      {
        Sollbuchung sollb = (Sollbuchung) provider;
        sollbuchungAusgleich(provider, sollb, ausgleichsbetrag,
            ueberweisungsbetrag, buchung, monitor);
      }
      else if (provider instanceof Rechnung)
      {
        double ausgleich = 0;
        for (Sollbuchung sollb : ((Rechnung) provider).getSollbuchungList())
        {
          // Auch bei ausgleich == 0 ausgleichen wegen der Erstattungsbuchung
          // die erzeugt werden soll. Ein negativer Ausgleich kann nicht
          // vorkommen weil die Überzahlung in diesem Fall durch die Checks
          // unterbunden wird
          if (sollb.getIstSumme() - sollb.getBetrag() > LIMIT)
          {
            throw new ApplicationException(
                "Überzahlung der sollbuchung wird bei Gesamtrechnung nicht unterstützt!");
          }
          ausgleich = Math.max(sollb.getBetrag() - sollb.getIstSumme(), 0.0);
          sollbuchungAusgleich(provider, sollb, ausgleich, ueberweisungsbetrag,
              buchung, monitor);
        }
      }
    }
  }

  /**
   * Die Methode führt den Buchungsausgleich durch.
   * 
   * Bei fixem Betrag: Falls es keine Überweisung gibt und der offene Betrag für
   * die Positionen mit der entsprechenden Buchungsart, Buchungsklasse und
   * Steuer noch größer als der Erstattungsbetrag ist, wird einfach der Wert des
   * Ausgleichsbetrags als Buchung erzeugt. Im anderen Fall wird der
   * Ausgleichsbetrag auch auf andere Sollbuchungspositionen verteilt und
   * notfalls auch die Erstattungsbuchung erzeugt, falls etwas überwiesen wurde.
   * Mit dem Ausgleichsbetrag wird als erstes die Sollbuchungsposition mit der
   * Buchungsart, Buchungsklasse und Steuer ausgeglichen die dem fixen Betrag
   * entspricht.
   * 
   * Bei nicht fixem Betrag: Falls noch nichts eingezahlt wurde werden alle
   * Sollbuchungspositionen ausgeglichen.Falls bereits teilweise etwas
   * eingezahlt wurde (keine Vollzahlung oder Überzahlung, da hier keine
   * Ausgleichsbuchungen erzeugt werden) werden auch alle Sollbuchungspositionen
   * ausgeglichen und die bereits eingezahlten Buchungen als erstattet gebucht.
   * Damit werden alle bisherigen Einzahlungen ausgeglichen.
   * 
   * @param prov
   *          Das selektierte Objekt (Provider) für das eine Gutschrift erzeugt
   *          werden soll.
   * @param sollb
   *          Die bestehende Sollbuchung die ausgeglichen werden soll.
   * @param ueberweisungsbetrag
   *          Der Betrag der überwiesen werden soll.
   * @param ausgleichsbetrag
   *          Der Betrag der beim Provider mit einer Buchung ausgeglichen werden
   *          muss, damit dieser dann nicht mehr als Fehlbetrag gelistet wird.
   * @param buchung
   *          Die Buchung für die Gutschrift Sollbuchung. Ihre ID wird beim
   *          Verwendungszweck der Ausgleichsbuchung verwendet um sie zu
   *          referenzieren.
   * @param monitor
   *          Monitor für die Meldungsausgabe.
   */
  private void sollbuchungAusgleich(IGutschriftProvider prov, Sollbuchung sollb,
      double ausgleichsbetrag, double ueberweisungsbetrag, Buchung buchung,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    boolean steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);
    boolean klasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);

    // Fixer Betrag und Sollbuchung
    if (params.isFixerBetragAbrechnen())
    {
      HashMap<String, Double> posMap = new HashMap<>();
      HashMap<String, String> posZweckMap = new HashMap<>();
      SplitbuchungsContainer.positionenAbgleichen(sollb, posMap, posZweckMap,
          false);
      // Key in der Form BuchungsartId-BuchungsklasseId#SteuerId (Steuer nur
      // wenn steuerInBuchung gesetzt ist)
      Long baId = Long.valueOf(params.getBuchungsart().getID());
      Long bkId = params.getBuchungsklasse() != null
          ? Long.valueOf(params.getBuchungsklasse().getID())
          : null;
      String key = baId + "-" + (klasseInBuchung && bkId != null ? bkId : "")
          + "#";
      if (steuerInBuchung)
      {
        key += (params.getSteuer() != null ? params.getSteuer().getID() : "");
      }

      // Prüfen ob genügend offener Betrag existiert
      Double posOffenBetrag = posMap.getOrDefault(key, 0d);
      if (posOffenBetrag - ausgleichsbetrag > -LIMIT
          && ueberweisungsbetrag <= LIMIT)
      {
        // Ausgleichsbuchung ohne splitten da keine Überweisung vorhanden ist
        generiereBuchung(prov, ausgleichsbetrag, "JVerein",
            "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), sollb,
            "");
      }
      else
      {
        // Es ist mehr auszugleichen als die Position hat. Darum verteilen wir
        // den Ausgleichsbetrag auf andere Posten
        Iterator<Entry<String, Double>> iterator = posMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue()).iterator();

        // Hauptbuchung erzeugen
        Buchung bu = getBuchung(ausgleichsbetrag, "JVerein",
            "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), null,
            "");
        initBuchung(bu, null, key);
        bu.store();
        bu.setSplitTyp(SplitbuchungTyp.HAUPT);
        SplitbuchungsContainer.init(bu);
        Long splitId = Long.valueOf(bu.getID());

        // Falls etwas erstattet wird, dann Erstattungsbuchung erzeugen
        if (ueberweisungsbetrag > LIMIT)
        {
          Buchung buch = getBuchung(-ueberweisungsbetrag, "JVerein",
              "Erstattung für Gutschrift Nr. " + buchung.getID(), null,
              "Überweisung");
          buch.setBuchungsartId(params.getBuchungsart() != null
              ? Long.valueOf(params.getBuchungsart().getID())
              : null);
          buch.setBuchungsklasseId(params.getBuchungsklasse() != null
              ? Long.valueOf(params.getBuchungsklasse().getID())
              : null);
          buch.setSteuer(params.getSteuer());
          buch.setSollbuchung(sollb);
          buch.setSplitTyp(SplitbuchungTyp.SPLIT);
          buch.setSplitId(splitId);
          SplitbuchungsContainer.add(buch);
        }

        // Der Ausgleichsbetrag wird um den Überweisungsbetrag erhöht wegen der
        // Erstattungsbuchung
        double ausgleichen = Math.min(posOffenBetrag, ausgleichsbetrag)
            + ueberweisungsbetrag;
        if (ausgleichen > LIMIT)
        {
          Buchung buch1 = getBuchung(ausgleichen, "JVerein",
              "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), null,
              "");
          initBuchung(buch1, null, key);
          buch1.setSollbuchung(sollb);
          buch1.setSplitTyp(SplitbuchungTyp.SPLIT);
          buch1.setSplitId(splitId);
          SplitbuchungsContainer.add(buch1);
        }
        double restbetrag = ausgleichsbetrag
            - Math.min(posOffenBetrag, ausgleichsbetrag);

        if (restbetrag > LIMIT)
        {
          while (iterator.hasNext())
          {
            Entry<String, Double> entry = iterator.next();
            if (entry.getKey().equals(key))
            {
              continue;
            }
            if (entry.getValue() > LIMIT)
            {
              double ausgleich = Math.min(entry.getValue(), restbetrag);
              Buchung rbuch = getBuchung(ausgleich, "JVerein",
                  "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(),
                  null, "");
              initBuchung(rbuch, entry, null);
              rbuch.setSollbuchung(sollb);
              rbuch.setSplitTyp(SplitbuchungTyp.SPLIT);
              rbuch.setSplitId(splitId);
              SplitbuchungsContainer.add(rbuch);
              restbetrag = restbetrag - ausgleich;
              if (restbetrag <= LIMIT)
              {
                break;
              }
            }
          }
        }
        SplitbuchungsContainer.store();
      }
    }
    else if (prov.getIstSumme() < LIMIT)
    {
      // Es werden keine Einzahlungen erstattet, da nehmen wir den autosplit
      generiereBuchung(prov, ausgleichsbetrag, "JVerein",
          "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), sollb, "");
    }
    else
    {
      Buchung bu = getBuchung(ausgleichsbetrag, "JVerein",
          "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), null, "");

      List<SollbuchungPosition> positionen = sollb.getSollbuchungPositionList();
      SollbuchungPosition pos = positionen.get(0);
      bu.setBuchungsartId(pos.getBuchungsartId());
      bu.setBuchungsklasseId(pos.getBuchungsklasseId());
      bu.setSteuer(pos.getSteuer());
      bu.store();
      bu.setSplitTyp(SplitbuchungTyp.HAUPT);
      SplitbuchungsContainer.init(bu);
      Long splitId = Long.valueOf(bu.getID());

      // Sollbuchungsposten ausgleichen und Positionen mit gleicher Buchungsart,
      // Buchungsklasse und Steuer zusammen fassen
      HashMap<String, Double> posMap = new HashMap<>();
      HashMap<String, String> posZweckMap = new HashMap<>();
      SplitbuchungsContainer.positionenZusammenfassen(sollb, posMap,
          posZweckMap);
      Iterator<Entry<String, Double>> iterator = posMap.entrySet().stream()
          .sorted(Map.Entry.comparingByValue()).iterator();
      while (iterator.hasNext())
      {
        Entry<String, Double> entry = iterator.next();
        Buchung buch = getBuchung(entry.getValue(), "JVerein",
            "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), null,
            "");
        initBuchung(buch, entry, null);
        buch.setSollbuchung(sollb);
        buch.setSplitTyp(SplitbuchungTyp.SPLIT);
        buch.setSplitId(splitId);
        SplitbuchungsContainer.add(buch);
      }

      // Einzahlungen erstatten
      for (Buchung b : sollb.getBuchungList())
      {
        Buchung buch = getBuchung(-b.getBetrag(), "JVerein",
            "Erstattung für Gutschrift Nr. " + buchung.getID(), null,
            "Überweisung");
        buch.setBuchungsartId(b.getBuchungsartId());
        buch.setBuchungsklasseId(b.getBuchungsklasseId());
        buch.setSteuer(b.getSteuer());
        buch.setSollbuchung(sollb);
        buch.setSplitTyp(SplitbuchungTyp.SPLIT);
        buch.setSplitId(splitId);
        SplitbuchungsContainer.add(buch);
      }
      SplitbuchungsContainer.store();
    }
    monitor.setStatusText(MARKER + "Ausgleichsbuchung erzeugt");
  }

  private void initBuchung(Buchung bu, Entry<String, Double> entry, String key)
      throws NumberFormatException, RemoteException
  {
    String theKey = key;
    if (key == null)
    {
      theKey = entry.getKey();
    }
    String buchungsart = theKey.substring(0, theKey.indexOf("-"));
    bu.setBuchungsartId(Long.parseLong(buchungsart));
    String tmpKey = theKey.substring(theKey.indexOf("-") + 1);
    String buchungsklasse = tmpKey.substring(0, tmpKey.indexOf("#"));
    String steuer = tmpKey.substring(tmpKey.indexOf("#") + 1);
    if (buchungsklasse.length() > 0)
    {
      bu.setBuchungsklasseId(Long.parseLong(buchungsklasse));
    }
    if (steuer.length() > 0)
    {
      bu.setSteuerId(Long.parseLong(steuer));
    }
  }

  private File getFile() throws Exception
  {
    File file = null;
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("SEPA-Ausgabedatei wählen.");
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(
        VorlageUtil.getName(VorlageTyp.GUTSCHRIFT_DATEINAME) + ".xml");
    fd.setFilterExtensions(new String[] { "*.xml" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return null;
    }
    if (!s.toLowerCase().endsWith(".xml"))
    {
      s = s + ".xml";
    }
    file = new File(s);
    settings.setAttribute("lastdir", file.getParent());
    return file;
  }

  private Buchung getBuchung(double betrag, String name, String zweck,
      String iban, String art) throws RemoteException, ApplicationException
  {
    Buchung buchung = (Buchung) service.createObject(Buchung.class, null);
    buchung.setBetrag(betrag);
    buchung.setDatum(params.getDatum());
    buchung.setKonto(konto);
    buchung.setName(name);
    buchung.setZweck(zweck);
    buchung.setIban(iban);
    buchung.setVerzicht(false);
    buchung.setArt(art);
    buchung.setBezeichnungSachzuwendung("");
    buchung.setHerkunftSpende(HerkunftSpende.KEINEANGABEN);
    buchung.setUnterlagenWertermittlung(false);
    buchung.setGeprueft(false);
    buchung.setAbrechnungslauf(abrl);
    return buchung;
  }

  private String getIBAN(IGutschriftProvider prov) throws RemoteException
  {
    if (prov.getGutschriftZahler() == null)
    {
      // Dann muss es eine Lastschrift sein
      return ((Lastschrift) prov).getIban();
    }
    else
    {
      return prov.getGutschriftZahler().getIban();
    }
  }
}
