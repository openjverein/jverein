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

import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
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

  private FormularInput formularInput;

  private DateInput datumInput;

  private TextInput zweckInput;

  private SelectInput ausgabeInput;

  private Formular formular;

  private Date datum;

  private String zweck;

  private UeberweisungAusgabe ausgabe;

  private LabelInput status = null;

  private boolean fortfahren = false;

  private boolean rechnungErzeugen;

  private CheckboxInput rechnungErzeugenInput;

  private boolean buchungErzeugen;

  private CheckboxInput buchungErzeugenInput;

  private boolean rechnungsDokumentSpeichern;

  private CheckboxInput rechnungsDokumentSpeichernInput;

  private Settings settings = null;

  public GutschriftDialog()
  {
    super(SWT.CENTER);
    setTitle("Gutschrift(en) erstellen");
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
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

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addInput(getStatus());

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

    datumInput = new DateInput(new Date());
    datumInput.setMandatory(true);
    group.addLabelPair("Ausführungsdatum", datumInput);

    zweckInput = new TextInput(settings.getString("verwendungszweck", ""));
    zweckInput.setMandatory(true);
    group.addLabelPair("Verwendungszweck", zweckInput);

    buchungErzeugenInput = new CheckboxInput(
        settings.getBoolean("buchungErzeugen", true));
    buchungErzeugenInput.addListener(e -> {
      rechnungsDokumentSpeichernInput
          .setEnabled((boolean) rechnungErzeugenInput.getValue()
              && (boolean) buchungErzeugenInput.getValue());
    });
    group.addLabelPair("Buchung zur Gutschrift erzeugen", buchungErzeugenInput);

    rechnungErzeugenInput = new CheckboxInput(
        settings.getBoolean("rechnungErzeugen", false));
    rechnungErzeugenInput.addListener(e -> {
      formularInput.setEnabled((boolean) rechnungErzeugenInput.getValue());
      rechnungsDokumentSpeichernInput
          .setEnabled((boolean) rechnungErzeugenInput.getValue()
              && (boolean) buchungErzeugenInput.getValue());
    });
    group.addLabelPair("Rechnung zur Gutschrift erzeugen",
        rechnungErzeugenInput);

    formularInput = new FormularInput(FormularArt.RECHNUNG,
        settings.getString("formular", ""));
    formularInput.setEnabled((boolean) rechnungErzeugenInput.getValue());
    group.addLabelPair("Erstattung Formular", formularInput);

    rechnungsDokumentSpeichernInput = new CheckboxInput(
        settings.getBoolean("rechnungsDokumentSpeichern", false));
    rechnungsDokumentSpeichernInput
        .setEnabled((boolean) rechnungErzeugenInput.getValue()
            && (boolean) buchungErzeugenInput.getValue());
    group.addLabelPair("Rechnung als Buchungsdokument speichern",
        rechnungsDokumentSpeichernInput);

    Map<String, Object> map = LastschriftMap.getDummyMap(null);
    map = new AllgemeineMap().getMap(map);
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
      if (formularInput.getValue() == null
          && (boolean) rechnungErzeugenInput.getValue())
      {
        status.setValue("Bitte Formular auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      formular = (Formular) formularInput.getValue();
      try
      {
        settings.setAttribute("formular", formular.getID());
      }
      catch (RemoteException re)
      {
        Logger.error("Fehler beim lesen der Formular ID", re);
      }
      datum = (Date) datumInput.getValue();
      buchungErzeugen = (boolean) buchungErzeugenInput.getValue();
      settings.setAttribute("buchungErzeugen", buchungErzeugen);
      rechnungErzeugen = (boolean) rechnungErzeugenInput.getValue();
      settings.setAttribute("rechnungErzeugen", rechnungErzeugen);
      rechnungsDokumentSpeichern = (boolean) rechnungsDokumentSpeichernInput
          .getValue();
      settings.setAttribute("rechnungsDokumentSpeichern",
          rechnungsDokumentSpeichern);
      zweck = (String) zweckInput.getValue();
      settings.setAttribute("verwendungszweck", zweck);
      ausgabe = (UeberweisungAusgabe) ausgabeInput.getValue();
      settings.setAttribute("ausgabe", ausgabe.getKey());
      fortfahren = true;
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }
}
