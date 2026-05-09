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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TabelleSpaltenAuswahlDialog extends AbstractDialog<Object>
{
  private List<Column> auswahl;

  private JVereinTablePart spaltenList;

  private JVereinTablePart tablePart;

  public TabelleSpaltenAuswahlDialog(JVereinTablePart tablePart)
  {
    super(TabelleSpaltenAuswahlDialog.POSITION_CENTER);

    this.tablePart = tablePart;
    auswahl = tablePart.getColums();
    setTitle("Spalten auswählen");
    setSize(400, SWT.DEFAULT);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup options = new LabelGroup(parent, "");
    options.addPart(getList());

    for (Column col : tablePart.getAllColums())
    {
      spaltenList.setChecked(col, auswahl.contains(col));
    }

    ButtonArea b = new ButtonArea();
    b.addButton("Speichern", c -> {
      try
      {
        tablePart.saveSpalten(spaltenList.getItems());
        GUI.getCurrentView().reload();
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Serverfehler");
      }
      close();
    }, null, true, "ok.png");
    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  private Part getList()
  {
    if (spaltenList != null)
    {
      return spaltenList;
    }
    spaltenList = new JVereinTablePart(tablePart.getAllColums(), null);
    spaltenList.addColumn("Name", "name");
    spaltenList.setCheckable(true);
    return spaltenList;
  }

  @Override
  protected Object getData()
  {
    return null;
  }

}
