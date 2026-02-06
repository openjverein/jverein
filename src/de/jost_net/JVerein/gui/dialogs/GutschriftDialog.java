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

package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.GutschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.gui.control.GutschriftControl;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.gui.view.GutschriftBugsView;
import de.jost_net.JVerein.io.Gutschrift;
import de.jost_net.JVerein.io.GutschriftParam;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class GutschriftDialog extends AbstractDialog<GutschriftParam>
{
  private boolean isNewDialog;

  private GutschriftParam params = null;

  private LabelInput status = null;

  private CheckboxInput rechnungErzeugenInput;

  private CheckboxInput rechnungsDokumentSpeichernInput;

  private FormularInput formularInput;

  private TextInput rechnungsTextInput;

  private DateInput rechnungsDatumInput;

  private AbstractInput buchungsartInput;

  private SelectInput buchungsklasseInput;

  private SelectInput steuerInput;

  private boolean EinstellungRechnungAnzeigen = false;

  private boolean EinstellungBuchungsklasseInBuchung = false;

  private boolean EinstellungSteuerInBuchung = false;

  private boolean EinstellungSpeicherungAnzeigen = false;

  private Settings settings = null;

  final AbrechnungSEPAControl scontrol = new AbrechnungSEPAControl(null);

  private GutschriftControl gcontrol;

  public GutschriftDialog(IGutschriftProvider[] providerArray,
      boolean isMitglied, boolean isNewDialog)
  {
    super(SWT.CENTER);
    setTitle("Gutschrift erstellen");
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.gcontrol = new GutschriftControl(providerArray, isMitglied);
    this.isNewDialog = isNewDialog;
    setSize(950, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    EinstellungRechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    EinstellungSpeicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    EinstellungBuchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    EinstellungSteuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(getStatus());
    ColumnLayout cl = new ColumnLayout(group.getComposite(), 2);
    SimpleContainer left = new SimpleContainer(cl.getComposite(), false, 2);
    SimpleContainer right = new SimpleContainer(cl.getComposite(), false, 2);
    ColumnLayout cl2 = new ColumnLayout(group.getComposite(), 1);
    SimpleContainer below = new SimpleContainer(cl2.getComposite(), false, 2);

    left.addHeadline("Überweisung");
    left.addLabelPair("Ausgabe", gcontrol.getAusgabeInput());
    left.addLabelPair("Ausführungsdatum", gcontrol.getDatumInput());
    if (isNewDialog)
    {
      gcontrol.getDatumInput().setValue(new Date());
    }
    left.addLabelPair("Verwendungszweck", gcontrol.getZweckInput());

    // Fixen Betrag erstatten
    right.addHeadline("Fixer Betrag");
    right.addLabelPair("Fixen Betrag erstatten",
        gcontrol.getFixerBetragAbrechnenInput());
    right.addLabelPair("Erstattungsbetrag", gcontrol.getFixerBetragInput());
    buchungsartInput = gcontrol.getBuchungsartInput();
    right.addLabelPair("Buchungsart", buchungsartInput);
    if (EinstellungBuchungsklasseInBuchung)
    {
      buchungsklasseInput = gcontrol.getBuchungsklasseInput();
      right.addLabelPair("Buchungsklasse", buchungsklasseInput);
    }
    if (EinstellungSteuerInBuchung)
    {
      steuerInput = gcontrol.getSteuerInput();
      right.addLabelPair("Steuer", steuerInput);
    }

    // Nur anzeigen wenn Rechnungen aktiviert sind
    if (EinstellungRechnungAnzeigen)
    {
      // Settings hier speichern damit sie nicht mit Anrechnungslauf vermischt
      // werden
      below.addHeadline("Rechnung");
      rechnungErzeugenInput = scontrol.getRechnung();
      rechnungErzeugenInput
          .setValue(settings.getBoolean("rechnungErzeugen", false));
      below.addLabelPair("Rechnung zur Gutschrift erzeugen",
          rechnungErzeugenInput);
      if (EinstellungSpeicherungAnzeigen)
      {
        rechnungsDokumentSpeichernInput = scontrol
            .getRechnungsdokumentSpeichern();
        rechnungsDokumentSpeichernInput
            .setValue(settings.getBoolean("rechnungsDokumentSpeichern", false));
        below.addLabelPair("Rechnung als Buchungsdokument speichern",
            rechnungsDokumentSpeichernInput);
      }
      formularInput = scontrol.getRechnungFormular();
      Formular f = null;
      try
      {
        String id = settings.getString("formular", "");
        if (id != null && !id.isEmpty())
        {
          f = (Formular) Einstellungen.getDBService()
              .createObject(Formular.class, id);
        }
      }
      catch (Exception ex)
      {
        // Nicht gefunden, dann null
      }
      formularInput.setValue(f);
      below.addLabelPair("Erstattungsformular", formularInput);
      rechnungsTextInput = scontrol.getRechnungstext();
      rechnungsTextInput.setValue(settings.getString("rechnungstext", ""));
      below.addLabelPair("Rechnungstext", rechnungsTextInput);
      rechnungsDatumInput = scontrol.getRechnungsdatum();
      if (!isNewDialog)
      {
        rechnungsDatumInput.setValue(gcontrol.getDatum("rechnungsdatum"));
      }
      below.addLabelPair("Rechnungsdatum", rechnungsDatumInput);
    }

    // Buttons
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.GUTSCHRIFT, false, "question-circle.png");

    Map<String, Object> map = GutschriftMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);
    buttons.addButton("Verwendungszweck Variablen anzeigen",
        new InsertVariableDialogAction(map), null, false, "bookmark.png");

    if (EinstellungRechnungAnzeigen)
    {
      Map<String, Object> rmap = new AllgemeineMap().getMap(null);
      rmap = MitgliedMap.getDummyMap(rmap);
      rmap = RechnungMap.getDummyMap(rmap);
      buttons.addButton("Rechnungstext Variablen anzeigen",
          new InsertVariableDialogAction(rmap), null, false, "bookmark.png");
    }

    buttons.addButton("Fehler/Warnungen/Hinweise", context -> {
      storeValues();
      saveSettings();
      gcontrol.saveSettings();
      close();
      GUI.startView(GutschriftBugsView.class, gcontrol);
    }, null, false, "bug.png");

    buttons.addButton("Gutschriften erstellen", context -> {
      try
      {
        if (!checkInput())
        {
          return;
        }
        storeValues();
        saveSettings();
        gcontrol.saveSettings();
        new Gutschrift(gcontrol);
        close();
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
      catch (Exception e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
        Logger.error("Fehler", e);
      }
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> {
      close();
    }, null, false, "process-stop.png");

    buttons.paint(parent);
  }

  private LabelInput getStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new LabelInput("");
    return status;
  }

  private void storeValues()
  {
    params = new GutschriftParam();
    params.setAusgabe(
        (UeberweisungAusgabe) gcontrol.getAusgabeInput().getValue());
    params.setDatum((Date) gcontrol.getDatumInput().getValue());
    params.setVerwendungszweck((String) gcontrol.getZweckInput().getValue());

    // Rechnung
    if (EinstellungRechnungAnzeigen)
    {
      params.setRechnungErzeugen((boolean) rechnungErzeugenInput.getValue());
      params.setFormular((Formular) formularInput.getValue());
      if (rechnungsDokumentSpeichernInput != null)
      {
        params.setRechnungsDokumentSpeichern(
            (boolean) rechnungsDokumentSpeichernInput.getValue());
      }
      params.setRechnungsText((String) rechnungsTextInput.getValue());
      params.setRechnungsDatum((Date) rechnungsDatumInput.getValue());
    }

    // Fixer Betrag
    boolean fixerBetragAbrechnen = (boolean) gcontrol
        .getFixerBetragAbrechnenInput().getValue();
    if (fixerBetragAbrechnen)
    {
      params.setFixerBetragAbrechnen(fixerBetragAbrechnen);
      params.setFixerBetrag((Double) gcontrol.getFixerBetragInput().getValue());
      params.setBuchungsart((Buchungsart) buchungsartInput.getValue());
      if (buchungsklasseInput != null)
      {
        params
            .setBuchungsklasse((Buchungsklasse) buchungsklasseInput.getValue());
      }
      if (steuerInput != null)
      {
        params.setSteuer((Steuer) steuerInput.getValue());
      }
    }
    gcontrol.setParams(params);
  }

  private boolean checkInput()
  {
    try
    {
      if (gcontrol.getZweckInput().getValue() == null
          || ((String) gcontrol.getZweckInput().getValue()).isEmpty())
      {
        status.setValue("Bitte Verwendungszweck eingeben");
        status.setColor(Color.ERROR);
        return false;
      }
      if (gcontrol.getDatumInput().getValue() == null)
      {
        status.setValue("Bitte Ausführungsdatum auswählen");
        status.setColor(Color.ERROR);
        return false;
      }
      if (EinstellungRechnungAnzeigen
          && (boolean) rechnungErzeugenInput.getValue())
      {
        if (formularInput.getValue() == null)
        {
          status.setValue("Bitte Erstattungsformular auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
        if (rechnungsDatumInput.getValue() == null)
        {
          status.setValue("Bitte Rechnungsdatum auswählen");
          status.setColor(Color.ERROR);
          return false;
        }
      }
      if ((boolean) gcontrol.getFixerBetragAbrechnenInput().getValue())
      {
        if (gcontrol.getFixerBetragInput().getValue() == null
            || ((Double) gcontrol.getFixerBetragInput().getValue()) < 0.005d)
        {
          status.setValue("Bitte positiven Erstattungsbetrag eingeben");
          status.setColor(Color.ERROR);
          return false;
        }
        if (buchungsartInput.getValue() == null)
        {
          status.setValue("Bitte Buchungsart eingeben");
          status.setColor(Color.ERROR);
          return false;
        }
        if (EinstellungBuchungsklasseInBuchung
            && buchungsklasseInput.getValue() == null)
        {
          status.setValue("Bitte Buchungsklasse eingeben");
          status.setColor(Color.ERROR);
          return false;
        }
        if (EinstellungSteuerInBuchung)
        {
          Buchungsart buchungsart = (Buchungsart) buchungsartInput.getValue();
          Steuer steuer = (Steuer) steuerInput.getValue();
          if (steuer != null && buchungsart != null)
          {
            if (buchungsart.getSpende() || buchungsart.getAbschreibung())
            {
              status.setValue(
                  "Bei Spenden und Abschreibungen ist keine Steuer möglich.");
              status.setColor(Color.ERROR);
              return false;
            }
            if (steuer.getBuchungsart().getArt() != buchungsart.getArt())
            {
              switch (buchungsart.getArt())
              {
                case ArtBuchungsart.AUSGABE:
                  status.setValue("Umsatzsteuer statt Vorsteuer gewählt!");
                  status.setColor(Color.ERROR);
                  return false;
                case ArtBuchungsart.EINNAHME:
                  status.setValue("Vorsteuer statt Umsatzsteuer gewählt!");
                  status.setColor(Color.ERROR);
                  return false;
                // Umbuchung ist bei Anlagebuchungen möglich,
                // Hier ist eine Vorsteuer (Kauf) und Umsatzsteuer (Verkauf)
                // möglich
                case ArtBuchungsart.UMBUCHUNG:
                  break;
              }
            }
          }
        }
      }
    }
    catch (RemoteException re)
    {
      status.setValue("Fehler beim Auswerten der Eingabe!");
      status.setColor(Color.ERROR);
      return false;
    }
    return true;
  }

  private void saveSettings()
  {
    try
    {
      settings.setAttribute("rechnungErzeugen", params.isRechnungErzeugen());
      if (params.isRechnungErzeugen())
      {
        if (EinstellungSpeicherungAnzeigen)
        {
          settings.setAttribute("rechnungsDokumentSpeichern",
              params.isRechnungsDokumentSpeichern());
        }
        settings.setAttribute("formular", params.getFormular().getID());
        settings.setAttribute("rechnungstext", params.getRechnungsText());
      }
    }
    catch (RemoteException ex)
    {
      Logger.error("Fehler beim Speichern der Settings", ex);
    }
  }

  @Override
  protected GutschriftParam getData() throws Exception
  {
    return params;
  }
}
