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
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;

public class MittelverwendungDialog extends AbstractDialog<Boolean>
{

  private DecimalInput verwendungsrueckstandInput = null;

  private DecimalInput zwanghafteWeitergabeInput = null;

  private LabelInput status = null;

  private TextInput nameInput = null;

  private boolean fortfahren = false;

  private Double verwendungsrueckstand;

  private Double zwanghafteWeitergabe;

  private String name;

  public MittelverwendungDialog(Double verwendungsrueckstand,
      Double zwanghafteWeitergabe, String name)
  {
    super(SWT.CENTER);
    setTitle("Startwerte setzen");
    this.verwendungsrueckstand = verwendungsrueckstand;
    this.zwanghafteWeitergabe = zwanghafteWeitergabe;
    this.name = name;
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

  private DecimalInput getVerwendungsrueckstandInput()
      throws RemoteException, ParseException
  {
    if (verwendungsrueckstandInput != null)
    {
      return verwendungsrueckstandInput;
    }

    if (verwendungsrueckstand == null)
    {
      verwendungsrueckstandInput = new DecimalInput(
          Einstellungen.DECIMALFORMAT);
    }
    else
    {
      verwendungsrueckstandInput = new DecimalInput(verwendungsrueckstand,
          Einstellungen.DECIMALFORMAT);
    }
    return verwendungsrueckstandInput;
  }

  private DecimalInput getZwanghafteWeitergabeInput()
      throws RemoteException, ParseException
  {
    if (zwanghafteWeitergabeInput != null)
    {
      return zwanghafteWeitergabeInput;
    }

    if (zwanghafteWeitergabe == null)
    {
      zwanghafteWeitergabeInput = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    else
    {
      zwanghafteWeitergabeInput = new DecimalInput(zwanghafteWeitergabe,
          Einstellungen.DECIMALFORMAT);
    }
    return zwanghafteWeitergabeInput;
  }

  private TextInput getNameInput() throws RemoteException, ParseException
  {
    if (nameInput != null)
    {
      return nameInput;
    }
    nameInput = new TextInput(name, 50);
    return nameInput;
  }

  public Double getVerwendungsrueckstand()
  {
    return verwendungsrueckstand;
  }

  public Double getZwanghafteWeitergabe()
  {
    return zwanghafteWeitergabe;
  }

  public String getName()
  {
    return name;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Rest des Verwendungsrückstand aus\ndem vorletzten GJ",
        getVerwendungsrueckstandInput());
    group.addLabelPair(
        "Zwanghafte satzungsgemäße \nWeitergabe von Mitteln im letzten GJ",
        getZwanghafteWeitergabeInput());
    group.addLabelPair("Name", getNameInput());
    group.addInput(getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", context -> {
      if (zwanghafteWeitergabeInput.getValue() == null
          || verwendungsrueckstandInput.getValue() == null)
      {
        status.setValue("Bitte Daten eingeben");
        status.setColor(Color.ERROR);
        return;
      }
      String value = (String) nameInput.getValue();
      if (value == null || value.isEmpty())
      {
        status.setValue("Bitte Namen eingeben");
        status.setColor(Color.ERROR);
        return;
      }
      verwendungsrueckstand = (Double) verwendungsrueckstandInput.getValue();
      zwanghafteWeitergabe = (Double) zwanghafteWeitergabeInput.getValue();
      name = (String) nameInput.getValue();
      fortfahren = true;
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }
}
