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
import de.jost_net.JVerein.gui.control.GutschriftBugsControl;
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
  private static final String ERROR = "===== Fehler ===== ";

  private static final String SKIP = "====> Überspringe ";

  private static final String MARKER = "-> ";

  private ArrayList<Lastschrift> lastschriften = new ArrayList<>();

  private GutschriftParam params = null;

  private int erstellt = 0;

  private int skip = 0;

  private int error1 = 0;

  private int error2 = 0;

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
            || params.getFixerBetrag() < 0.005d)))
    {
      throw new ApplicationException("Eingabeparameter fehlerhaft!");
    }

    konto = getKonto();

    // Abrechnungslauf erzeugen damit man die Lastschrift speichern kann was
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

    // Datei für SEPA Ausgabe holen
    if (params.getAusgabe() == UeberweisungAusgabe.SEPA_DATEI)
    {
      file = getFile();
    }

    BackgroundTask t = new BackgroundTask()
    {
      private boolean interrupted = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        monitor.setStatusText("Starte die Generierung der Gutschriften");
        monitor.setStatus(ProgressMonitor.STATUS_RUNNING);

        for (IGutschriftProvider provider : gcontrol.getProviderArray())
        {
          if (isInterrupted())
          {
            monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
            monitor.setStatusText("Generierung abgebrochen.");
            monitor.setPercentComplete(100);
            throw new OperationCanceledException();
          }

          monitor.setPercentComplete(100 * (erstellt + skip + error1)
              / gcontrol.getProviderArray().length);

          String statustext = "";
          String name = "";
          try
          {
            DBTransaction.starten();
            statustext = provider.getObjektName() + " mit Nr. "
                + provider.getID();

            String bug = GutschriftBugsControl.doChecks(provider, params, null);
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
            double ueberweisungsbetrag = provider.getIstSumme() > 0.005d
                ? provider.getIstSumme()
                : 0;
            double tmp = provider.getBetrag() - provider.getIstSumme();
            double offenbetrag = tmp > 0.005d ? tmp : 0;
            double ausgleichsbetrag = offenbetrag;
            if (params.isFixerBetragAbrechnen())
            {
              tmp = params.getFixerBetrag() - offenbetrag;
              ueberweisungsbetrag = tmp > 0.005d ? tmp : 0;
              tmp = params.getFixerBetrag() - ueberweisungsbetrag;
              ausgleichsbetrag = tmp > 0.005d ? tmp : 0;
            }

            monitor.setStatusText("Generiere Gutschrift für " + statustext
                + " und Zahler " + name + ".");

            // Sollbuchung, Buchungen und Lastschriften erzeugen
            Buchung buchung = generiereGutschrift(provider, ueberweisungsbetrag,
                name, monitor);
            sollbuchungenAusgleich(provider, ausgleichsbetrag, buchung,
                monitor);
            DBTransaction.commit();
            erstellt++;
          }
          catch (ApplicationException ae)
          {
            DBTransaction.rollback();
            error1++;
            String text = ERROR + statustext + ": " + ae.getMessage();
            monitor.setStatusText(text);
            continue;
          }
          catch (Exception e)
          {
            DBTransaction.rollback();
            error1++;
            String text = ERROR + statustext
                + ": Fehler beim Datenbank Zugriff!";
            monitor.setStatusText(text);
            Logger.error(text, e);
            continue;
          }
        }

        // Gegenbuchung erstellen
        if (summe > 0)
        {
          try
          {
            // Gegenbuchung
            getBuchung(summe, "JVerein", "Gegenbuchung", "").store();
            monitor.setStatusText("Gegenbuchung erzeugt");
          }
          catch (ApplicationException ae)
          {
            error2++;
            monitor.setStatusText(ERROR + ae.getMessage());
          }
          catch (Exception e)
          {
            error2++;
            String text = "Fehler beim Generieren der Gegenbuchung!";
            monitor.setStatusText(ERROR + text);
            Logger.error(text, e);
          }
        }

        // Überweisung erstellen
        if (summe > 0)
        {
          try
          {
            // Wenn keine Datei ausgewählt wurde, dann wird keine generiert
            Ueberweisung ueberweisung = new Ueberweisung(null);
            if (params.getAusgabe() == UeberweisungAusgabe.HIBISCUS)
            {
              ueberweisung.write(lastschriften, file, params.getDatum(),
                  params.getAusgabe(), null);
              monitor.setStatusText("SEPA Auftrag an Hibiscus übergeben");
            }
            else if (file != null)
            {
              // Dateiausgabe
              ueberweisung.write(lastschriften, file, params.getDatum(),
                  params.getAusgabe(), null);
              monitor.setStatusText("SEPA Datei erzeugt");
            }
          }
          catch (ApplicationException ae)
          {
            error2++;
            monitor.setStatusText(ERROR + ae.getMessage());
          }
          catch (Exception e)
          {
            error2++;
            String text = "Fehler bei der SEPA Ausgabe!";
            monitor.setStatusText(ERROR + text);
            Logger.error(text, e);
          }
        }

        monitor.setPercentComplete(100);
        if (skip + error1 + error2 > 0)
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
              + (skip > 0 ? skip + " übersprungen." : "")
              + (error1 > 0 ? " " + error1 + " fehlerhaft." : ""));
        }
        else
        {
          GUI.getCurrentView().reload();
          monitor.setStatusText(erstellt + " Gutschrift(en) erstellt"
              + (skip > 0 ? ", " + skip + " übersprungen." : ".")
              + (error1 > 0 ? " " + error1 + " Gutschriften fehlerhaft." : "")
              + (error2 > 0
                  ? " " + error2 + " Fehler bei Gegenbuchung und SEPA Ausgabe."
                  : ""));
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

  private Buchung generiereGutschrift(IGutschriftProvider prov,
      double ueberweisungsbetrag, String name, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    String zweck = params.getVerwendungszweck();
    Rechnung rechnung = null;
    Sollbuchung sollbuchung = null;
    Lastschrift ls = null;
    Buchung buchung = null;

    ls = generiereLastschrift(prov, zweck, ueberweisungsbetrag, monitor);
    if (ueberweisungsbetrag > 0)
    {
      lastschriften.add(ls);
      summe += ueberweisungsbetrag;
      monitor.setStatusText(MARKER + "Überweisung erzeugt");
    }

    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = new GutschriftMap().getMap(ls, map);
    try
    {
      zweck = VelocityTool.eval(map, params.getVerwendungszweck());
      if (zweck.length() >= 140)
      {
        zweck = zweck.substring(0, 136) + "...";
      }
      ls.setVerwendungszweck(zweck);
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
    sollbuchung = generiereSollbuchung(prov, betrag, zweck, monitor);
    if (params.isRechnungErzeugen() && sollbuchung != null
        && (Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      rechnung = generiereRechnung(sollbuchung, monitor);
      if (params.getRechnungsText().trim().length() > 0)
      {
        zweck = params.getRechnungsText();
        boolean ohneLesefelder = !zweck.contains(Einstellungen.LESEFELD_PRE);
        new AllgemeineMap().getMap(null);
        map = new MitgliedMap().getMap((Mitglied) sollbuchung.getZahler(), map,
            ohneLesefelder);
        map = new RechnungMap().getMap(rechnung, map);
        try
        {
          zweck = VelocityTool.eval(map, zweck);
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
    }
    buchung = generiereBuchung(prov, betrag, name, zweck, sollbuchung);
    monitor.setStatusText(MARKER + "Buchung erzeugt");
    generiereBuchungsdokument(prov, buchung, rechnung, monitor);
    return (buchung);
  }

  private Lastschrift generiereLastschrift(IGutschriftProvider prov,
      String zweck, double betrag, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    // Lastschrift für Überweisungen erstellen
    // Das ist eine Hilfsklasse um die bestehende Klasse für Überweisungen
    // verwenden zu können
    Lastschrift ls = null;
    if (prov.getGutschriftZahler() == null)
    {
      // Dann muss es eine Lastschrift sein
      ls = getLastschriftVonLastschrift((Lastschrift) prov, zweck, betrag);
    }
    else
    {
      ls = getLastschriftVonMitglied(prov.getGutschriftZahler(), zweck, betrag);
    }
    return ls;
  }

  private Sollbuchung generiereSollbuchung(IGutschriftProvider prov,
      double betrag, String zweck, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    Sollbuchung sollbuchung = null;
    // Sollbuchung nur wenn Mitglied und Zahler vorhanden, z.B. nicht bei
    // Kursteilnehmer
    if (prov.getMitglied() != null && prov.getGutschriftZahler() != null)
    {
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
        List<SollbuchungPosition> positionen = prov
            .getSollbuchungPositionList();
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
      monitor.setStatusText(MARKER + "Sollbuchung erzeugt");
    }
    return sollbuchung;
  }

  private Rechnung generiereRechnung(Sollbuchung sollbuchung,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    Rechnung rechnung = null;

    rechnung = (Rechnung) service.createObject(Rechnung.class, null);
    rechnung.setFormular(params.getFormular());
    rechnung.setDatum(params.getRechnungsDatum());
    rechnung.fill(sollbuchung);
    rechnung.store();
    monitor.setStatusText(MARKER + "Rechnung erzeugt");
    return rechnung;
  }

  private Buchung generiereBuchung(IGutschriftProvider prov, double betrag,
      String name, String zweck, Sollbuchung sollbuchung)
      throws RemoteException, ApplicationException
  {
    // Buchung erzeugen
    Buchung buchung = getBuchung(betrag, name, zweck, getIBAN(prov));
    if (params.isFixerBetragAbrechnen() || prov instanceof Lastschrift)
    {
      // Ist auch bei Mitgliedern
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

    // Nicht splitten bei nur einer Position oder kompletter Erstattung
    if (!(params.isFixerBetragAbrechnen() || prov instanceof Lastschrift
        || prov.getIstSumme() < 0.005d))
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

  private void generiereBuchungsdokument(IGutschriftProvider prov,
      Buchung buchung, Rechnung rechnung, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    // Buchungsdokument erzeugen
    if (params.isRechnungsDokumentSpeichern() && rechnung != null
        && buchung != null)
    {
      Map<String, Object> rmap = new AllgemeineMap().getMap(null);
      rmap = new MitgliedMap().getMap(prov.getGutschriftZahler(), rmap);
      rmap = new RechnungMap().getMap(rechnung, rmap);
      storeBuchungsDokument(rechnung, buchung, params.getDatum(), rmap);
      monitor.setStatusText(MARKER + "Buchungsdokument erzeugt");
    }
  }

  private void sollbuchungenAusgleich(IGutschriftProvider provider,
      double ausgleichsbetrag, Buchung buchung, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    if (ausgleichsbetrag > 0)
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
          // Gesamtrechnung wird nicht unterstützt
          sollbFix = ((Rechnung) provider).getSollbuchungList().get(0);
        }
        // Sollbuchung ausgleichen
        sollbuchungAusgleich(provider, sollbFix, ausgleichsbetrag, buchung,
            monitor);
      }
      else
      {
        // Sollbuchungen ausgleichen wenn etwas offen ist
        if (provider instanceof Sollbuchung)
        {
          Sollbuchung sollb = (Sollbuchung) provider;
          sollbuchungAusgleich(provider, sollb, ausgleichsbetrag, buchung,
              monitor);
        }
        else if (provider instanceof Rechnung)
        {
          double ausgleich = 0;
          for (Sollbuchung sollb : ((Rechnung) provider).getSollbuchungList())
          {
            double tmp = sollb.getBetrag() - sollb.getIstSumme();
            ausgleich = tmp > 0.005d ? tmp : 0;
            if (ausgleich > 0)
            {
              sollbuchungAusgleich(provider, sollb, ausgleich, buchung,
                  monitor);
            }
          }
        }
      }
    }
  }

  private void sollbuchungAusgleich(IGutschriftProvider prov, Sollbuchung sollb,
      double ausgleichsbetrag, Buchung buchung, ProgressMonitor monitor)
      throws RemoteException, ApplicationException
  {
    // Fixer Betrag und Sollbuchung
    if (params.isFixerBetragAbrechnen())
    {
      // Ausgleichsbuchung ohne splitten
      generiereBuchung(prov, ausgleichsbetrag, "JVerein",
          "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), sollb);
    }
    else if (prov.getIstSumme() < 0.005d)
    {
      // Ausgleichsbuchung ohne splitten
      generiereBuchung(prov, ausgleichsbetrag, "JVerein",
          "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), sollb);
    }
    else
    {
      // Fehlende Positionen berechnen
      HashMap<String, Double> posMap = new HashMap<>();
      HashMap<String, String> posZweckMap = new HashMap<>();
      SplitbuchungsContainer.positionenAbgleichen(sollb, posMap, posZweckMap,
          false);
      Buchung bu = getBuchung(ausgleichsbetrag, "JVerein",
          "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), null);

      Iterator<Entry<String, Double>> iterator = posMap.entrySet().stream()
          .sorted(Map.Entry.comparingByValue()).iterator();

      if (posMap.size() == 1)
      {
        initBuchung(bu, iterator.next());
        bu.setSollbuchung(sollb);
        bu.store();
      }
      else if (posMap.size() > 1)
      {
        boolean first = true;
        while (iterator.hasNext())
        {
          Entry<String, Double> entry = iterator.next();
          if (first)
          {
            initBuchung(bu, entry);
            bu.store();
            bu.setSplitTyp(SplitbuchungTyp.HAUPT);
            SplitbuchungsContainer.init(bu);
            first = false;
          }
          Buchung buch = getBuchung(entry.getValue(), "JVerein",
              "Buchungsausgleich für Gutschrift Nr. " + buchung.getID(), null);
          initBuchung(buch, entry);
          buch.setSollbuchung(sollb);
          buch.setSplitTyp(SplitbuchungTyp.SPLIT);
          SplitbuchungsContainer.add(buch);
        }
        SplitbuchungsContainer.store();
      }
    }
    monitor.setStatusText(MARKER + "Ausgleichsbuchung erzeugt");
  }

  private void initBuchung(Buchung bu, Entry<String, Double> entry)
      throws NumberFormatException, RemoteException
  {
    String buchungsart = entry.getKey().substring(0,
        entry.getKey().indexOf("-"));
    bu.setBuchungsartId(Long.parseLong(buchungsart));
    String tmpKey = entry.getKey().substring(entry.getKey().indexOf("-") + 1);
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
      String iban) throws RemoteException, ApplicationException
  {
    Buchung buchung = (Buchung) service.createObject(Buchung.class, null);
    buchung.setBetrag(betrag);
    buchung.setDatum(params.getDatum());
    buchung.setKonto(konto);
    buchung.setName(name);
    buchung.setZweck(zweck);
    buchung.setIban(iban);
    buchung.setVerzicht(false);
    buchung.setArt("Überweisung");
    buchung.setBezeichnungSachzuwendung("");
    buchung.setHerkunftSpende(HerkunftSpende.KEINEANGABEN);
    buchung.setUnterlagenWertermittlung(false);
    buchung.setGeprueft(false);
    buchung.setAbrechnungslauf(abrl);
    return buchung;
  }

  private Lastschrift getLastschriftVonMitglied(Mitglied m, String zweck,
      double betrag) throws RemoteException, ApplicationException
  {
    Lastschrift ls = (Lastschrift) service.createObject(Lastschrift.class,
        null);
    ls.setMitglied(Integer.parseInt(m.getID()));
    ls.setEmail(m.getEmail());
    ls.setBIC(m.getBic());
    ls.setIBAN(m.getIban());
    ls.setMandatDatum(m.getMandatDatum());
    ls.setMandatID(m.getMandatID());
    ls.setVerwendungszweck(zweck);
    ls.setBetrag(betrag);
    ls.setAbrechnungslauf(Integer.valueOf(abrl.getID()));
    // Wird bei Überweisung nicht gebraucht aber wegen der Map implementierung
    // gesetzt
    ls.setMandatSequence(MandatSequence.RCUR.getTxt());
    ls.set(m);
    return ls;
  }

  private Lastschrift getLastschriftVonLastschrift(Lastschrift la, String zweck,
      double betrag) throws RemoteException, ApplicationException
  {
    Lastschrift ls = (Lastschrift) service.createObject(Lastschrift.class,
        null);
    Mitglied m = la.getMitglied();
    Kursteilnehmer k = la.getKursteilnehmer();
    if (m != null)
    {
      ls.setMitglied(Integer.parseInt(m.getID()));
    }
    else if (k != null)
    {
      ls.setKursteilnehmer(Integer.parseInt(k.getID()));
    }
    else
    {
      throw new ApplicationException(
          "Lastschrift hat kein Mitglied und keinen Kursteilnehmer!");
    }
    ls.setEmail(la.getEmail());
    ls.setBIC(la.getBIC());
    ls.setIBAN(la.getIBAN());
    ls.setMandatDatum(la.getMandatDatum());
    ls.setMandatID(la.getMandatID());
    ls.setVerwendungszweck(zweck);
    ls.setBetrag(betrag);
    ls.setAbrechnungslauf(Integer.valueOf(abrl.getID()));
    // Wird bei Überweisung nicht gebraucht aber wegen der Map implementierung
    // gesetzt
    ls.setMandatSequence(MandatSequence.RCUR.getTxt());
    ls.set(la);
    return ls;
  }

  private String getIBAN(IGutschriftProvider prov) throws RemoteException
  {
    if (prov.getGutschriftZahler() == null)
    {
      // Dann muss es eine Lastschrift sein
      return ((Lastschrift) prov).getIBAN();
    }
    else
    {
      return prov.getGutschriftZahler().getIban();
    }
  }
}
