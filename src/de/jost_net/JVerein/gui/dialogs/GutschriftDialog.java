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
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.GutschriftMap;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.InsertVariableDialogAction;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
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

  private boolean buchungErzeugen;

  private CheckboxInput buchungErzeugenInput;

  private boolean sollbuchungErzeugen;

  private CheckboxInput sollbuchungErzeugenInput;

  private boolean rechnungsDokumentSpeichern = false;

  private CheckboxInput rechnungsDokumentSpeichernInput;

  private boolean teilbetragAbrechnen;

  private CheckboxInput teilbetragAbrechnenInput;

  private Double teilbetrag;

  private DecimalInput teilbetragInput;

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
  protected Boolean getData() throws Exception
  {
    return fortfahren;
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

  public boolean getBuchungErzeugen()
  {
    return buchungErzeugen;
  }

  public boolean getSollbuchungErzeugen()
  {
    return sollbuchungErzeugen;
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

  public boolean getTeilbetragAbrechnen()
  {
    return teilbetragAbrechnen;
  }

  public Double getTeilbetrag()
  {
    return teilbetrag;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(getStatus());

    // Ausgabe
    UeberweisungAusgabe aus = UeberweisungAusgabe.getByKey(
        settings.getInt("ausgabe", UeberweisungAusgabe.HIBISCUS.getKey()));
    if (aus != UeberweisungAusgabe.SEPA_DATEI
        && aus != UeberweisungAusgabe.HIBISCUS)
    {
      aus = UeberweisungAusgabe.HIBISCUS;
    }
    ausgabeInput = new SelectInput(UeberweisungAusgabe.values(), aus);
    ausgabeInput.setMandatory(true);
    group.addLabelPair("Ausgabe", ausgabeInput);

    // Ausführungsdatum
    datumInput = new DateInput(new Date());
    datumInput.setMandatory(true);
    group.addLabelPair("Ausführungsdatum", datumInput);

    // Verwendungszweck
    zweckInput = new TextInput(settings.getString("verwendungszweck", ""));
    zweckInput.setMandatory(true);
    group.addLabelPair("Verwendungszweck", zweckInput);

    // Solluchung zur Gutschrift erzeugen
    sollbuchungErzeugenInput = new CheckboxInput(
        settings.getBoolean("sollbuchungErzeugen", true));
    group.addLabelPair("Sollbuchung zur Gutschrift erzeugen",
        sollbuchungErzeugenInput);

    // Buchung zur Gutschrift erzeugen
    buchungErzeugenInput = new CheckboxInput(
        settings.getBoolean("buchungErzeugen", true));
    buchungErzeugenInput.addListener(e -> {
      rechnungsDokumentSpeichernInput
          .setEnabled(getRechnungsDokumentSpeichernInputEnabled());
    });
    group.addLabelPair("Buchung zur Gutschrift erzeugen", buchungErzeugenInput);

    // Rechnung zur Gutschrift erzeugen
    rechnungErzeugenInput = new CheckboxInput(
        settings.getBoolean("rechnungErzeugen", false));
    rechnungErzeugenInput.addListener(e -> {
      formularInput.setEnabled(getFormularInputEnabled());
      rechnungsDokumentSpeichernInput
          .setEnabled(getRechnungsDokumentSpeichernInputEnabled());
    });

    // Erstattung Formular
    formularInput = new FormularInput(FormularArt.RECHNUNG,
        settings.getString("formular", ""));
    formularInput.setEnabled(getFormularInputEnabled());

    // Rechnung als Buchungsdokument speichern
    rechnungsDokumentSpeichernInput = new CheckboxInput(
        settings.getBoolean("rechnungsDokumentSpeichern", false));
    rechnungsDokumentSpeichernInput
        .setEnabled(getRechnungsDokumentSpeichernInputEnabled());

    // Nur anzeigen wenn Rechnungen aktiviert sind
    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      group.addLabelPair("Rechnung zur Gutschrift erzeugen",
          rechnungErzeugenInput);
      group.addLabelPair("Erstattung Formular", formularInput);
      group.addLabelPair("Rechnung als Buchungsdokument speichern",
          rechnungsDokumentSpeichernInput);
    }

    // Fixen Betrag erstatten
    if (isMitglied)
    {
      teilbetragAbrechnenInput = new CheckboxInput(true);
      teilbetragAbrechnenInput.addListener(e -> {
        teilbetragAbrechnenInput.setValue(true);
      });
    }
    else
    {
      teilbetragAbrechnenInput = new CheckboxInput(
          settings.getBoolean("teilbetragAbrechnen", false));
      teilbetragAbrechnenInput.addListener(e -> {
        teilbetragInput
            .setMandatory((boolean) teilbetragAbrechnenInput.getValue());
        teilbetragInput
            .setEnabled((boolean) teilbetragAbrechnenInput.getValue());
      });
    }
    group.addLabelPair("Fixen Betrag erstatten", teilbetragAbrechnenInput);

    // Erstattungsbetrag
    String tmp = settings.getString("teilbetrag", "");
    if (tmp != null && !tmp.isEmpty())
    {
      teilbetragInput = new DecimalInput(Double.parseDouble(tmp),
          Einstellungen.DECIMALFORMAT);
    }
    else
    {
      teilbetragInput = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    teilbetragInput.setMandatory((boolean) teilbetragAbrechnenInput.getValue());
    teilbetragInput.setEnabled((boolean) teilbetragAbrechnenInput.getValue());
    group.addLabelPair("Erstattungsbetrag", teilbetragInput);

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
      try
      {
        if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN)
            && formularInput.getValue() == null
            && (boolean) rechnungErzeugenInput.getValue())
        {
          status.setValue("Bitte Formular auswählen");
          status.setColor(Color.ERROR);
          return;
        }
      }
      catch (RemoteException e1)
      {
        status.setValue("Fehler beim Zugriff auf die Einstellungen");
        status.setColor(Color.ERROR);
        return;
      }
      if ((teilbetragInput.getValue() == null
          || ((Double) teilbetragInput.getValue()) < 0.005d)
          && (boolean) teilbetragAbrechnenInput.getValue())
      {
        status.setValue("Bitte positiven Erstattungsbetrag eingeben");
        status.setColor(Color.ERROR);
        return;
      }

      datum = (Date) datumInput.getValue();
      buchungErzeugen = (boolean) buchungErzeugenInput.getValue();
      settings.setAttribute("buchungErzeugen", buchungErzeugen);
      sollbuchungErzeugen = (boolean) sollbuchungErzeugenInput.getValue();
      settings.setAttribute("sollbuchungErzeugen", sollbuchungErzeugen);
      try
      {
        if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
        {
          formular = (Formular) formularInput.getValue();
          settings.setAttribute("formular", formular.getID());
          rechnungErzeugen = (boolean) rechnungErzeugenInput.getValue();
          settings.setAttribute("rechnungErzeugen", rechnungErzeugen);
          rechnungsDokumentSpeichern = (boolean) rechnungsDokumentSpeichernInput
              .getValue();
          settings.setAttribute("rechnungsDokumentSpeichern",
              rechnungsDokumentSpeichern);
        }
      }
      catch (RemoteException e1)
      {
        status.setValue("Fehler beim Lesen der Formular ID");
        status.setColor(Color.ERROR);
        return;
      }
      zweck = (String) zweckInput.getValue();
      settings.setAttribute("verwendungszweck", zweck);
      ausgabe = (UeberweisungAusgabe) ausgabeInput.getValue();
      settings.setAttribute("ausgabe", ausgabe.getKey());
      teilbetragAbrechnen = (boolean) teilbetragAbrechnenInput.getValue();
      settings.setAttribute("teilbetragAbrechnen", teilbetragAbrechnen);
      teilbetrag = (Double) teilbetragInput.getValue();
      if (teilbetrag != null)
      {
        settings.setAttribute("teilbetrag", teilbetrag);
      }
      fortfahren = true;
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }

  private boolean getRechnungsDokumentSpeichernInputEnabled()
  {
    return (boolean) rechnungErzeugenInput.getValue()
        && (boolean) buchungErzeugenInput.getValue();
  }

  private boolean getFormularInputEnabled()
  {
    return (boolean) rechnungErzeugenInput.getValue();
  }

}
