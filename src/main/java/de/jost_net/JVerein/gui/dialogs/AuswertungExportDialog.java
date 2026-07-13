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
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.control.AuswertungControl;
import de.jost_net.JVerein.keys.Filter;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AuswertungExportDialog extends AbstractDialog<AuswertungControl>
{
  private LabelInput status = null;

  private AuswertungControl data = null;

  private Filter[] filter;

  private AuswertungControl control = new AuswertungControl(null);

  public AuswertungExportDialog(int position, Filter[] filter)
  {
    super(position);
    this.filter = filter;
    setTitle("Auswertungsparameter");
  }

  @Override
  protected void paint(Composite parent)
      throws RemoteException, ApplicationException
  {
    Container container = new SimpleContainer(parent);
    container.addText("Bitte Parameter für die Auswertung eingeben.", true);

    LabelGroup group = new LabelGroup(container.getComposite(), "Parameter");
    for (Filter f : filter)
    {
      group.addInput(control.getFilterInput(f));
    }
    group.addLabelPair("", getStatus());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Übernehmen", context -> {
      if (control.checkFilter() == false)
      {
        status.setValue("Bitte auswählen");
        status.setColor(Color.ERROR);
        return;
      }
      data = control;
      try
      {
        // Filter Daten von GUI lesen weil der Dialog geschlossen wird
        control.generateFilter();
      }
      catch (RemoteException e)
      {
        String text = "Fehler beim Lesen der Daten.";
        Logger.error(text, e);
        throw new ApplicationException(text);
      }
      try
      {
        control.saveFilterSettings();
      }
      catch (RemoteException e)
      {
        String text = "Fehler beim Speichern der Settings.";
        Logger.error(text, e);
      }
      close();
    }, null, true, "ok.png");

    buttons.addButton("Abbrechen", context -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    container.addButtonArea(buttons);
  }

  @Override
  protected AuswertungControl getData()
  {
    return data;
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
}
