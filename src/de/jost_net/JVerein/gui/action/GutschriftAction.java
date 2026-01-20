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
package de.jost_net.JVerein.gui.action;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.dialogs.GutschriftDialog;
import de.jost_net.JVerein.io.SEPASupport;
import de.jost_net.JVerein.io.Ueberweisung;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.VorlageUtil;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class GutschriftAction extends SEPASupport implements Action
{
  private ArrayList<Lastschrift> lastschriften = new ArrayList<>();

  private Formular formular;

  private Date datum;

  private String verwendungszweck;

  private UeberweisungAusgabe ausgabe;

  private boolean buchungErzeugen;

  private boolean rechnungErzeugen;

  private boolean rechnungsDokumentSpeichern;

  private int erstellt = 0;

  private int skip = 0;

  private Konto konto = null;

  private Double summe = 0d;

  private Abrechnungslauf abrl = null;

  private Settings settings = null;

  public GutschriftAction()
  {
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    IGutschriftProvider[] providerArray = null;
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }

    if (context instanceof Abrechnungslauf)
    {
      Abrechnungslauf lauf = (Abrechnungslauf) context;
      try
      {
        DBService service = Einstellungen.getDBService();
        DBIterator<Sollbuchung> sollbIt = service.createList(Sollbuchung.class);
        sollbIt.addFilter(Sollbuchung.ABRECHNUNGSLAUF + " = ?",
            Integer.valueOf(lauf.getID()));
        sollbIt.setOrder("ORDER BY " + Sollbuchung.MITGLIED);
        if (sollbIt.size() == 0)
        {
          throw new ApplicationException(
              "Der Abrechnungslauf enthält keine Sollbuchungen");
        }
        providerArray = new IGutschriftProvider[sollbIt.size()];
        PseudoIterator.asList(sollbIt).toArray(providerArray);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler Abrechnungslauf Auswertung", e);
      }
    }
    else if (context instanceof IGutschriftProvider)
    {
      providerArray = new IGutschriftProvider[] {
          (IGutschriftProvider) context };
    }
    else if (context instanceof IGutschriftProvider[])
    {
      providerArray = (IGutschriftProvider[]) context;
    }
    else
    {
      throw new ApplicationException(
          "Keine Sollbuchung, Rechnung oder Abrechnungslauf ausgewählt");
    }

    try
    {
      GutschriftDialog dialog = new GutschriftDialog();
      if (!dialog.open())
      {
        return;
      }
      formular = dialog.getFormular();
      datum = dialog.getDatum();
      verwendungszweck = dialog.getZweck();
      ausgabe = dialog.getAusgabe();
      buchungErzeugen = dialog.getBuchungErzeugen();
      rechnungErzeugen = dialog.getRechnungErzeugen();
      rechnungsDokumentSpeichern = dialog.getRechnungsDokumentSpeichern();
      if (datum == null || verwendungszweck == null
          || verwendungszweck.isEmpty()
          || (formular == null && rechnungErzeugen))
      {
        return;
      }

      if (buchungErzeugen)
      {
        konto = getKonto();
      }

      // Abrechnungslauf erzeugen damit man die Lastschrift speichern kann was
      // wegen der Map nötig ist. Es hat auch noch den Vorteil, dass man ihn
      // löschen kann und damit alle generierten Sollbuchungen und Buchungen
      abrl = (Abrechnungslauf) Einstellungen.getDBService()
          .createObject(Abrechnungslauf.class, null);
      abrl.setDatum(new Date());
      abrl.setModus(Abrechnungsmodi.GUTSCHRIFT);
      abrl.setFaelligkeit(datum);
      abrl.setStichtag(datum);
      abrl.setZahlungsgrund(verwendungszweck);
      abrl.setAbgeschlossen(false);
      abrl.store();

      for (IGutschriftProvider provider : providerArray)
      {
        // Keine Gutschrift bei Erstattungen und keiner Einzahlung
        // Ohne IBAN keine Überweisung möglich
        String iban = provider.getZahler().getIban();
        if (provider.getBetrag() < 0.005d || provider.getIstSumme() < 0.005d
            || iban == null || iban.isEmpty())
        {
          skip++;
          continue;
        }

        // Sollbuchung, Buchungen und Lastschriften erzeugen
        generiereSollbuchung(provider);
      }

      // Überweisung und Gegebbuchung erstellen
      if (erstellt > 0)
      {
        generiereUeberweisungen();
        if (buchungErzeugen)
        {
          // Gegenbuchung
          getBuchung(summe, null, "", null);
        }
      }

      // Temoräre Lastschriften löschen
      for (Lastschrift la : lastschriften)
      {
        la.delete();
      }

      if (erstellt == 0)
      {
        GUI.getStatusBar().setErrorText(
            "Keine Gutschrift erstellt: Entweder kein Erstattungsbetrag oder keine IBAN vorhanden.");
      }
      else
      {
        GUI.getCurrentView().reload();
        GUI.getStatusBar().setSuccessText(erstellt + " Gutschrift(en) erstellt"
            + (skip > 0 ? ", " + skip + " vorhandene übersprungen." : "."));
      }
    }
    catch (OperationCanceledException ignore)
    {

    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim erstellen der Gutschrift";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
      return;
    }
  }

  private void generiereSollbuchung(IGutschriftProvider prov)
      throws RemoteException, ApplicationException
  {
    Double betrag = prov.getIstSumme();
    String zweck = verwendungszweck;

    // Lastschrift für Überweisungen erstellen
    // Das ist eine Hilfsklasse um die bestehende Klasse für Überweisungen
    // verwenden zu können
    Mitglied m = prov.getZahler();
    Lastschrift ls = (Lastschrift) Einstellungen.getDBService()
        .createObject(Lastschrift.class, null);
    ls.setAbrechnungslauf(Integer.valueOf(abrl.getID()));
    ls.setMitglied(Integer.parseInt(m.getID()));
    ls.setPersonenart(m.getPersonenart());
    ls.setAnrede(m.getAnrede());
    ls.setTitel(m.getTitel());
    ls.setName(m.getName());
    ls.setVorname(m.getVorname());
    ls.setStrasse(m.getStrasse());
    ls.setAdressierungszusatz(m.getAdressierungszusatz());
    ls.setPlz(m.getPlz());
    ls.setOrt(m.getOrt());
    ls.setStaat(m.getStaatCode());
    ls.setEmail(m.getEmail());
    ls.setGeschlecht(m.getGeschlecht());
    ls.setVerwendungszweck(zweck);
    ls.setBetrag(betrag);
    ls.setBIC(m.getBic());
    ls.setIBAN(m.getIban());
    ls.setMandatDatum(m.getMandatDatum());
    ls.setMandatID(m.getMandatID());
    ls.store();
    lastschriften.add(ls);

    Map<String, Object> map = new AllgemeineMap().getMap(null);
    map = new LastschriftMap().getMap(ls, map);
    try
    {
      zweck = VelocityTool.eval(map, verwendungszweck);
      if (zweck.length() >= 140)
      {
        zweck = zweck.substring(0, 136) + "...";
      }
      ls.setVerwendungszweck(zweck);
      ls.store();
    }
    catch (IOException e)
    {
      Logger.error("Fehler bei der Aufbereitung der Variablen", e);
    }

    // Sollbuchung mit negativem bereits bezahltem Betrag
    Sollbuchung sollbuchung = (Sollbuchung) Einstellungen.getDBService()
        .createObject(Sollbuchung.class, null);
    sollbuchung.setBetrag(-betrag);
    sollbuchung.setDatum(datum);
    sollbuchung.setMitglied(prov.getMitglied());
    sollbuchung.setZahler(prov.getZahler());
    sollbuchung.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
    sollbuchung.setZweck1(zweck);
    sollbuchung.setAbrechnungslauf(abrl);
    sollbuchung.store();

    // Sollbuchungsposition ertellen
    SollbuchungPosition sbp = (SollbuchungPosition) Einstellungen.getDBService()
        .createObject(SollbuchungPosition.class, null);
    sbp.setBetrag(-betrag);
    ArrayList<SollbuchungPosition> positionenList = prov
        .getSollbuchungPositionList();
    if (positionenList != null && positionenList.size() > 0)
    {
      sbp.setBuchungsartId(positionenList.get(0).getBuchungsartId());
      sbp.setBuchungsklasseId(positionenList.get(0).getBuchungsklasseId());
      sbp.setSteuer(positionenList.get(0).getSteuer());
    }
    sbp.setDatum(datum);
    sbp.setZweck(zweck);
    sbp.setSollbuchung(sollbuchung.getID());
    sbp.store();

    // Rechnung erzeugen
    Rechnung rechnung = null;
    if (rechnungErzeugen)
    {
      rechnung = (Rechnung) Einstellungen.getDBService()
          .createObject(Rechnung.class, null);

      rechnung.setFormular(formular);
      rechnung.setDatum(datum);
      rechnung.fill(sollbuchung);
      rechnung.store();

      sollbuchung.setRechnung(rechnung);
      sollbuchung.updateForced();
    }

    // Buchung erzeugen
    if (buchungErzeugen)
    {
      Buchung buchung = getBuchung(-betrag, prov.getZahler(), zweck,
          positionenList);
      buchung.setSollbuchung(sollbuchung);
      buchung.store();

      if (rechnung != null && rechnungsDokumentSpeichern)
      {
        Map<String, Object> rmap = new AllgemeineMap().getMap(null);
        rmap = new MitgliedMap().getMap(prov.getZahler(), rmap);
        rmap = new RechnungMap().getMap(rechnung, rmap);
        storeBuchungsDokument(rechnung, buchung, datum, rmap);
      }
    }

    summe += betrag;
    erstellt++;
  }

  private void generiereUeberweisungen() throws Exception
  {

    File file = null;
    if (ausgabe == UeberweisungAusgabe.SEPA_DATEI)
    {
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
        return;
      }
      if (!s.toLowerCase().endsWith(".xml"))
      {
        s = s + ".xml";
      }
      file = new File(s);
      settings.setAttribute("lastdir", file.getParent());
    }
    Ueberweisung ueberweisung = new Ueberweisung(null);
    ueberweisung.write(lastschriften, file, datum, ausgabe, verwendungszweck);
  }

  private Buchung getBuchung(Double betrag, Mitglied m, String zweck,
      ArrayList<SollbuchungPosition> positionenList)
      throws RemoteException, ApplicationException
  {
    Buchung buchung = (Buchung) Einstellungen.getDBService()
        .createObject(Buchung.class, null);
    buchung.setBetrag(betrag);
    buchung.setDatum(datum);
    buchung.setKonto(konto);
    buchung
        .setName(m != null ? Adressaufbereitung.getNameVorname(m) : "JVerein");
    buchung.setZweck(m == null ? "Gegenbuchung" : zweck);
    buchung.setIban("");
    buchung.setVerzicht(false);
    buchung.setArt("Überweisung");
    buchung.setBezeichnungSachzuwendung("");
    buchung.setHerkunftSpende(HerkunftSpende.KEINEANGABEN);
    buchung.setUnterlagenWertermittlung(false);
    buchung.setGeprueft(false);
    buchung.setAbrechnungslauf(abrl);
    if (positionenList != null && positionenList.size() > 0)
    {
      buchung.setBuchungsartId(positionenList.get(0).getBuchungsartId());
      buchung.setBuchungsklasseId(positionenList.get(0).getBuchungsklasseId());
      buchung.setSteuer(positionenList.get(0).getSteuer());
    }
    buchung.store();
    return buchung;
  }
}
