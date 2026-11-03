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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;

public class MittelverwendungDialog extends AbstractDialog<Boolean>
{

  private BigDecimalInput verwendungsrueckstandInput = null;

  private BigDecimalInput zwanghafteWeitergabeInput = null;

  private LabelInput status = null;

  private TextInput nameInput = null;

  private boolean fortfahren = false;

  private BigDecimal verwendungsrueckstand;

  private BigDecimal zwanghafteWeitergabe;

  private String name;

  public MittelverwendungDialog(BigDecimal verwendungsrueckstand,
      BigDecimal zwanghafteWeitergabe, String name)
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

  private BigDecimalInput getVerwendungsrueckstandInput()
      throws RemoteException, ParseException
  {
    if (verwendungsrueckstandInput != null)
    {
      return verwendungsrueckstandInput;
    }

    if (verwendungsrueckstand == null)
    {
      verwendungsrueckstandInput = new BigDecimalInput(
          Einstellungen.BIGDECIMALFORMAT);
    }
    else
    {
      verwendungsrueckstandInput = new BigDecimalInput(verwendungsrueckstand,
          Einstellungen.BIGDECIMALFORMAT);
    }
    return verwendungsrueckstandInput;
  }

  private BigDecimalInput getZwanghafteWeitergabeInput()
      throws RemoteException, ParseException
  {
    if (zwanghafteWeitergabeInput != null)
    {
      return zwanghafteWeitergabeInput;
    }

    if (zwanghafteWeitergabe == null)
    {
      zwanghafteWeitergabeInput = new BigDecimalInput(
          Einstellungen.BIGDECIMALFORMAT);
    }
    else
    {
      zwanghafteWeitergabeInput = new BigDecimalInput(zwanghafteWeitergabe,
          Einstellungen.BIGDECIMALFORMAT);
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
    if (name != null && !name.isEmpty())
    {
      nameInput.setEnabled(false);
    }
    return nameInput;
  }

  public BigDecimal getVerwendungsrueckstand()
  {
    return verwendungsrueckstand;
  }

  public BigDecimal getZwanghafteWeitergabe()
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
      verwendungsrueckstand = (BigDecimal) verwendungsrueckstandInput
          .getValue();
      zwanghafteWeitergabe = (BigDecimal) zwanghafteWeitergabeInput.getValue();
      name = (String) nameInput.getValue();
      fortfahren = true;
      close();
    }, null, false, "ok.png");
    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");
    buttons.paint(parent);
  }
}
