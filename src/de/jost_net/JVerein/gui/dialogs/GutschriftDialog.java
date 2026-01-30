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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.GutschriftMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

public class GutschriftDialog extends AbstractDialog<Boolean>
{

  private boolean isMitglied;

  private FormularInput formularInput = null;

  private DateInput datumInput;

  private TextInput zweckInput;

  private SelectInput ausgabeInput;

  private Formular formular = null;

  private Date datum;

  private String zweck;

  private UeberweisungAusgabe ausgabe;

  private LabelInput status = null;

  private boolean fortfahren = false;

  private boolean rechnungErzeugen = false;

  private CheckboxInput rechnungErzeugenInput;

  private boolean rechnungsDokumentSpeichern = false;

  private CheckboxInput rechnungsDokumentSpeichernInput;

  private boolean fixerBetragAbrechnen;

  private CheckboxInput fixerBetragAbrechnenInput;

  private Double fixerBetrag;

  private DecimalInput fixerBetragInput;

  private Buchungsart buchungsart;

  private AbstractInput buchungsartInput;

  private Buchungsklasse buchungsklasse;

  private SelectInput buchungsklasseInput;

  private Steuer steuer;

  private SelectInput steuerInput;

  private boolean rechnungAnzeigen = false;

  private boolean buchungsklasseInBuchung = false;

  private boolean steuerInBuchung = false;

  private boolean speicherungAnzeigen = false;

  private Settings settings = null;

  public GutschriftDialog(boolean isMitglied)
  {
    super(SWT.CENTER);
    setTitle("Gutschrift(en) erstellen");
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.isMitglied = isMitglied;
    setSize(700, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws RemoteException
  {
    rechnungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.RECHNUNGENANZEIGEN);
    speicherungAnzeigen = (Boolean) Einstellungen
        .getEinstellung(Property.DOKUMENTENSPEICHERUNG)
        && JVereinPlugin.isArchiveServiceActive();
    buchungsklasseInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG);
    steuerInBuchung = (Boolean) Einstellungen
        .getEinstellung(Property.STEUERINBUCHUNG);

    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(getStatus());
    group.addHeadline("Überweisung");
    group.addLabelPair("Ausgabe", getAusgabeInput());
    group.addLabelPair("Ausführungsdatum", getDatumInput());
    group.addLabelPair("Verwendungszweck", getZweckInput());

    // Nur anzeigen wenn Rechnungen aktiviert sind
    if (rechnungAnzeigen)
    {
      group.addHeadline("Rechnung");
      group.addLabelPair("Rechnung zur Gutschrift erzeugen",
          getRechnungErzeugenInput());
      group.addLabelPair("Erstattung Formular", getFormularInput());
      if (speicherungAnzeigen)
      {
        group.addLabelPair("Rechnung als Buchungsdokument speichern",
            getRechnungsDokumentSpeichernInput());
      }
    }

    // Fixen Betrag erstatten
    group.addHeadline("Fixer Betrag");
    group.addLabelPair("Fixen Betrag erstatten",
        getFixerBetragAbrechnenInput());
    group.addLabelPair("Erstattungsbetrag", getFixerBetragInput());
    group.addLabelPair("Buchungsart", getBuchungsartInput());
    if (buchungsklasseInBuchung)
    {
      group.addLabelPair("Buchungsklasse", getBuchungsklasseInput());
    }
    if (steuerInBuchung)
    {
      group.addLabelPair("Steuer", getSteuerInput());
    }

    Map<String, Object> map = GutschriftMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);

    // Buttons
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.GUTSCHRIFT, false, "question-circle.png");
    buttons.addButton("Variablen anzeigen", new InsertVariableDialogAction(map),
        null, false, "bookmark.png");
    buttons.addButton("Gutschriften(en) erstellen", context -> {
      if (zweckInput.getValue() == null
          || ((String) zweckInput.getValue()).isEmpty())
      {
        status.setValue("Bitte Zweck eingeben");
        status.setColor(Color.ERROR);
        return;
      }
      if (datumInput.getValue() == null)
      {
        status.setValue("Bitte Datum auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      if (rechnungAnzeigen && formularInput.getValue() == null
          && (boolean) rechnungErzeugenInput.getValue())
      {
        status.setValue("Bitte Formular auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      if ((fixerBetragInput.getValue() == null
          || ((Double) fixerBetragInput.getValue()) < 0.005d)
          && (boolean) fixerBetragAbrechnenInput.getValue())
      {
        status.setValue("Bitte positiven Erstattungsbetrag eingeben");
        status.setColor(Color.ERROR);
        return;
      }
      if (buchungsartInput.getValue() == null
          && (boolean) fixerBetragAbrechnenInput.getValue())
      {
        status.setValue("Bitte Buchungsart eingeben");
        status.setColor(Color.ERROR);
        return;
      }
      storeValues();
      saveSettings();
      fortfahren = true;
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context ->

    close(), null, false, "process-stop.png");
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

  // Ausgabe
  private SelectInput getAusgabeInput()
  {
    UeberweisungAusgabe aus = UeberweisungAusgabe.getByKey(
        settings.getInt("ausgabe", UeberweisungAusgabe.HIBISCUS.getKey()));
    if (aus != UeberweisungAusgabe.SEPA_DATEI
        && aus != UeberweisungAusgabe.HIBISCUS)
    {
      aus = UeberweisungAusgabe.HIBISCUS;
    }
    ausgabeInput = new SelectInput(UeberweisungAusgabe.values(), aus);
    ausgabeInput.setMandatory(true);
    return ausgabeInput;
  }

  // Ausführungsdatum
  private DateInput getDatumInput()
  {
    datumInput = new DateInput(new Date());
    datumInput.setMandatory(true);
    return datumInput;
  }

  // Verwendungszweck
  private TextInput getZweckInput()
  {
    zweckInput = new TextInput(settings.getString("verwendungszweck", ""));
    zweckInput.setMandatory(true);
    return zweckInput;
  }

  // Rechnung zur Gutschrift erzeugen
  private CheckboxInput getRechnungErzeugenInput()
  {
    rechnungErzeugenInput = new CheckboxInput(
        settings.getBoolean("rechnungErzeugen", false));
    rechnungErzeugenInput.addListener(e -> {
      // Die Reihenfolge von mandatory und enabled ist abhängig von
      // enable/disable. Sonst klappt das mit der gelben Farbe nicht
      if ((boolean) rechnungErzeugenInput.getValue())
      {
        formularInput.setEnabled(true);
        formularInput.setMandatory(true);
      }
      else
      {
        formularInput.setMandatory(false);
        formularInput.setEnabled(false);
      }
      rechnungsDokumentSpeichernInput
          .setEnabled((boolean) rechnungErzeugenInput.getValue());
    });
    return rechnungErzeugenInput;
  }

  // Erstattung Formular
  private FormularInput getFormularInput() throws RemoteException
  {
    formularInput = new FormularInput(FormularArt.RECHNUNG,
        settings.getString("formular", ""));
    formularInput.setMandatory((boolean) rechnungErzeugenInput.getValue());
    formularInput.setEnabled((boolean) rechnungErzeugenInput.getValue());
    return formularInput;
  }

  // Rechnung als Buchungsdokument speichern
  private CheckboxInput getRechnungsDokumentSpeichernInput()
  {
    rechnungsDokumentSpeichernInput = new CheckboxInput(
        settings.getBoolean("rechnungsDokumentSpeichern", false));
    rechnungsDokumentSpeichernInput
        .setEnabled((boolean) rechnungErzeugenInput.getValue());
    return rechnungsDokumentSpeichernInput;
  }

  // Fixen Betrag erstatten
  private CheckboxInput getFixerBetragAbrechnenInput()
  {
    if (isMitglied)
    {
      fixerBetragAbrechnenInput = new CheckboxInput(true);
      fixerBetragAbrechnenInput.addListener(e -> {
        fixerBetragAbrechnenInput.setValue(true);
      });
    }
    else
    {
      fixerBetragAbrechnenInput = new CheckboxInput(
          settings.getBoolean("fixerBetragAbrechnen", false));
      fixerBetragAbrechnenInput.addListener(e -> {
        fixerBetragInput
            .setMandatory((boolean) fixerBetragAbrechnenInput.getValue());
        fixerBetragInput
            .setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
        // Die Reihenfolge von mandatory und enabled ist abhängig von
        // enable/disable. Sonst klappt das mit der gelben Farbe nicht
        if ((boolean) fixerBetragAbrechnenInput.getValue())
        {
          buchungsartInput.setEnabled(true);
          buchungsartInput.setMandatory(true);
        }
        else
        {
          buchungsartInput.setMandatory(false);
          buchungsartInput.setEnabled(false);
        }
        if (buchungsklasseInput != null)
        {
          buchungsklasseInput
              .setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
        }
        if (steuerInput != null)
        {
          steuerInput
              .setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
        }
      });
    }
    fixerBetragAbrechnenInput.setName(
        " *sonst ganzen Betrag erstatten und bereits bezahlten Betrag überweisen");
    return fixerBetragAbrechnenInput;
  }

  // Erstattungsbetrag
  private DecimalInput getFixerBetragInput()
  {
    String tmp = settings.getString("fixerBetrag", "");
    if (tmp != null && !tmp.isEmpty())
    {
      fixerBetragInput = new DecimalInput(Double.parseDouble(tmp),
          Einstellungen.DECIMALFORMAT);
    }
    else
    {
      fixerBetragInput = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    fixerBetragInput
        .setMandatory((boolean) fixerBetragAbrechnenInput.getValue());
    fixerBetragInput.setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
    return fixerBetragInput;
  }

  public AbstractInput getBuchungsartInput() throws RemoteException
  {
    Buchungsart ba = null;
    String buchungsart = settings.getString("buchungsart", "");
    if (buchungsart.length() > 0)
    {
      try
      {
        ba = (Buchungsart) Einstellungen.getDBService()
            .createObject(Buchungsart.class, buchungsart);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann erste aus der Liste
      }
    }
    buchungsartInput = new BuchungsartInput().getBuchungsartInput(
        buchungsartInput, ba, buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    buchungsartInput.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          Buchungsart bua = (Buchungsart) buchungsartInput.getValue();
          if (buchungsklasseInput != null
              && buchungsklasseInput.getValue() == null && bua != null)
            buchungsklasseInput.setValue(bua.getBuchungsklasse());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    buchungsartInput.addListener(e -> {
      if (steuerInput != null && buchungsartInput.getValue() != null)
      {
        try
        {
          steuerInput.setValue(
              ((Buchungsart) buchungsartInput.getValue()).getSteuer());
        }
        catch (RemoteException e1)
        {
          Logger.error("Fehler", e1);
        }
      }
    });
    buchungsartInput
        .setMandatory((boolean) fixerBetragAbrechnenInput.getValue());
    buchungsartInput.setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
    return buchungsartInput;
  }

  public SelectInput getBuchungsklasseInput() throws RemoteException
  {
    Buchungsklasse bk = null;
    String buchungskl = settings.getString("buchungsklasse", "");
    if (buchungskl.length() > 0)
    {
      try
      {
        bk = (Buchungsklasse) Einstellungen.getDBService()
            .createObject(Buchungsklasse.class, buchungskl);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann erste aus der Liste
      }
    }
    buchungsklasseInput = new BuchungsklasseInput()
        .getBuchungsklasseInput(buchungsklasseInput, bk);
    buchungsklasseInput
        .setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
    return buchungsklasseInput;
  }

  public SelectInput getSteuerInput() throws RemoteException
  {
    Steuer st = null;
    String steuer = settings.getString("steuer", "");
    if (steuer.length() > 0)
    {
      try
      {
        st = (Steuer) Einstellungen.getDBService().createObject(Steuer.class,
            steuer);
      }
      catch (ObjectNotFoundException e)
      {
        // Dann erste aus der Liste
      }
    }
    steuerInput = new SteuerInput(st);
    steuerInput.setPleaseChoose("Keine Steuer");
    steuerInput.setEnabled((boolean) fixerBetragAbrechnenInput.getValue());
    return steuerInput;
  }

  private void storeValues()
  {
    datum = (Date) datumInput.getValue();
    if (rechnungAnzeigen)
    {
      formular = (Formular) formularInput.getValue();
      rechnungErzeugen = (boolean) rechnungErzeugenInput.getValue();
      if (rechnungsDokumentSpeichernInput != null)
      {
        rechnungsDokumentSpeichern = (boolean) rechnungsDokumentSpeichernInput
            .getValue();
      }
    }
    zweck = (String) zweckInput.getValue();
    ausgabe = (UeberweisungAusgabe) ausgabeInput.getValue();
    fixerBetragAbrechnen = (boolean) fixerBetragAbrechnenInput.getValue();
    if (fixerBetragAbrechnen)
    {
      fixerBetrag = (Double) fixerBetragInput.getValue();
      buchungsart = (Buchungsart) buchungsartInput.getValue();
      if (buchungsklasseInBuchung)
      {
        buchungsklasse = (Buchungsklasse) buchungsklasseInput.getValue();
      }
      if (steuerInBuchung)
      {
        steuer = (Steuer) steuerInput.getValue();
      }
    }
  }

  private void saveSettings()
  {
    try
    {
      settings.setAttribute("ausgabe", ausgabe.getKey());
      settings.setAttribute("verwendungszweck", zweck);
      if (rechnungErzeugen)
      {
        settings.setAttribute("formular", formular.getID());
        settings.setAttribute("rechnungErzeugen", rechnungErzeugen);
        settings.setAttribute("rechnungsDokumentSpeichern",
            rechnungsDokumentSpeichern);
      }
      if (!isMitglied)
      {
        settings.setAttribute("fixerBetragAbrechnen", fixerBetragAbrechnen);
        if (fixerBetragAbrechnen)
        {
          if (fixerBetrag != null)
          {
            settings.setAttribute("fixerBetrag", fixerBetrag);
          }
          else
          {
            settings.setAttribute("fixerBetrag", "");
          }
          if (buchungsart != null)
          {
            settings.setAttribute("buchungsart", buchungsart.getID());
          }
          else
          {
            settings.setAttribute("buchungsart", "");
          }
          if (buchungsklasse != null)
          {
            settings.setAttribute("buchungsklasse", buchungsklasse.getID());
          }
          else
          {
            settings.setAttribute("buchungsklasse", "");
          }
          if (steuer != null)
          {
            settings.setAttribute("steuer", steuer.getID());
          }
          else
          {
            settings.setAttribute("steuer", "");
          }
        }
      }
    }
    catch (RemoteException ex)
    {
      Logger.error("fehler beim Speichern der Settings", ex);
    }
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return fortfahren;
  }

  public Formular getFormular()
  {
    return formular;
  }

  public Date getDatum()
  {
    return datum;
  }

  public boolean getRechnungErzeugen()
  {
    return rechnungErzeugen;
  }

  public boolean getRechnungsDokumentSpeichern()
  {
    return rechnungsDokumentSpeichern;
  }

  public String getZweck()
  {
    return zweck;
  }

  public UeberweisungAusgabe getAusgabe()
  {
    return ausgabe;
  }

  public boolean getFixerBetragAbrechnen()
  {
    return fixerBetragAbrechnen;
  }

  public Double getFixerBetrag()
  {
    return fixerBetrag;
  }

  public Buchungsart getBuchungsart()
  {
    return buchungsart;
  }

  public Buchungsklasse getBuchungsklasse()
  {
    return buchungsklasse;
  }

  public Steuer getSteuer()
  {
    return steuer;
  }
}
