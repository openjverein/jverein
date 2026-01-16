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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;

public class RechnungDialog extends AbstractDialog<Boolean>
{

  private FormularInput formularRechnungInput;

  private FormularInput formularErstattungInput;

  private DateInput datumInput;

  private Formular formularRechnung;

  private Formular formularErstattung;

  private Date datum;

  private LabelInput status = null;

  private boolean fortfahren = false;

  private boolean sollbuchungsdatum;

  private CheckboxInput sollbuchungsdatumInput;

  public RechnungDialog()
  {
    super(SWT.CENTER);
    setTitle("Rechnung(en) erstellen");
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

  public Formular getFormularRechnung()
  {
    return formularRechnung;
  }

  public Formular getFormularErstattung()
  {
    return formularErstattung;
  }

  public Date getDatum()
  {
    return datum;
  }

  public boolean getSollbuchungsdatum()
  {
    return sollbuchungsdatum;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addText(
        "Bitte Rechnungsdatum und zu verwendende Formulare auswählen.", true);
    group.addInput(getStatus());
    formularRechnungInput = new FormularInput(FormularArt.RECHNUNG);
    group.addLabelPair("Formular Rechnung", formularRechnungInput);
    formularErstattungInput = new FormularInput(FormularArt.RECHNUNG);
    group.addLabelPair("Formular Erstattung", formularErstattungInput);

    datumInput = new DateInput(new Date());
    group.addLabelPair("Datum", datumInput);

    sollbuchungsdatumInput = new CheckboxInput(false);
    sollbuchungsdatumInput.setName("Datum der Sollbuchung verwenden");
    sollbuchungsdatumInput.addListener(e -> datumInput
        .setEnabled(!(boolean) sollbuchungsdatumInput.getValue()));
    group.addInput(sollbuchungsdatumInput);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Rechnung(en) erstellen", context -> {
      if (formularRechnungInput.getValue() == null
          || (formularErstattungInput.getValue() == null))
      {
        status.setValue("Bitte Formular auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      if (datumInput.getValue() == null
          && !(boolean) sollbuchungsdatumInput.getValue())
      {
        status.setValue("Bitte Datum auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      formularRechnung = (Formular) formularRechnungInput.getValue();
      formularErstattung = (Formular) formularErstattungInput.getValue();
      datum = (Date) datumInput.getValue();
      sollbuchungsdatum = (boolean) sollbuchungsdatumInput.getValue();
      fortfahren = true;
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }
}
