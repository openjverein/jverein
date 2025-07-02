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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;

/**
 * Dialog zur Zuordnung der Steuer.
 */
public class SteuerZuordnungDialog extends AbstractDialog<Steuer>
{

  private SelectInput steuerInput = null;

  private CheckboxInput ueberschreiben = null;

  private LabelInput status = null;

  private Steuer steuer = null;

  private boolean ueberschr;

  private boolean abort = true;

  /**
   * @param position
   */
  public SteuerZuordnungDialog(int position)
  {
    super(position);
    setTitle("Zuordnung Steuer");
    setSize(400, SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, "");
    group.addLabelPair("Steuer", getSteuerAuswahl());
    group.addLabelPair("Steuer überschreiben", getUeberschreiben());
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", context -> {
      if (steuerInput.getValue() == null)
      {
        status.setValue("Bitte Steuer auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      steuer = (Steuer) steuerInput.getValue();
      ueberschr = (Boolean) getUeberschreiben().getValue();
      abort = false;
      close();
    }, null, true, "ok.png");

    buttons.addButton("Entfernen", context -> {
      steuer = null;
      abort = false;
      close();
    }, null, false, "user-trash-full.png");

    buttons.addButton("Abbrechen", context -> close(), null, false,
        "process-stop.png");

    buttons.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  public Steuer getData() throws Exception
  {
    return steuer;
  }

  public boolean getOverride()
  {
    return ueberschr;
  }

  public boolean getAbort()
  {
    return abort;
  }

  private SelectInput getSteuerAuswahl() throws RemoteException
  {
    if (steuerInput != null)
    {
      return steuerInput;
    }
    DBIterator<Steuer> it = Einstellungen.getDBService()
        .createList(Steuer.class);
    it.addFilter("aktiv = true");

    steuerInput = new SelectInput(PseudoIterator.asList(it), null);
    steuerInput.setAttribute("name");
    steuerInput.setPleaseChoose("Keine Steuer");

    return steuerInput;
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

  private CheckboxInput getUeberschreiben()
  {
    if (ueberschreiben != null)
    {
      return ueberschreiben;
    }
    ueberschreiben = new CheckboxInput(false);
    return ueberschreiben;
  }
}
