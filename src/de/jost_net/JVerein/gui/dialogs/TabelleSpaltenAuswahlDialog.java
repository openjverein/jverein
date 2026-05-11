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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TabelleSpaltenAuswahlDialog extends AbstractDialog<Object>
{
  private JVereinTablePart[] tableParts;

  public TabelleSpaltenAuswahlDialog(JVereinTablePart... tableParts)
      throws RemoteException, ApplicationException
  {
    super(TabelleSpaltenAuswahlDialog.POSITION_CENTER);

    this.tableParts = tableParts;

    boolean leer = true;
    for (JVereinTablePart table : tableParts)
    {
      if (table.getItems().size() > 0)
      {
        leer = false;
        break;
      }
    }
    if (leer)
    {
      throw new ApplicationException("Tabelle ist leer");
    }
    setTitle("Spalten auswählen");
    setSize(400, SWT.DEFAULT);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void paint(Composite parent) throws Exception
  {
    TabFolder folder = null;
    List<JVereinTablePart> parts = new ArrayList<>();
    int nummer = 1;
    for (JVereinTablePart table : tableParts)
    {
      if (table.getItems().size() == 0)
      {
        continue;
      }
      JVereinTablePart part = new JVereinTablePart(table.getAllColums(), null);
      part.addColumn("Name", "name");
      part.setCheckable(true);

      if (tableParts.length > 1)
      {
        if (folder == null)
        {
          folder = new TabFolder(parent, SWT.BORDER);
          folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        }

        TabGroup tab = new TabGroup(folder, "Tabelle " + nummer++, true, 1);
        tab.addPart(part);
      }
      else
      {
        LabelGroup group = new LabelGroup(parent, "Tabelle");
        group.addPart(part);
      }

      List<Column> auswahl = table.getColums();
      for (Column col : table.getAllColums())
      {
        part.setChecked(col, auswahl.contains(col));
      }
      parts.add(part);
    }

    ButtonArea b = new ButtonArea();
    b.addButton("Speichern", c -> {
      try
      {
        for (int i = 0; i < tableParts.length; i++)
        {
          tableParts[i].saveSpalten(parts.get(i).getItems());
        }
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

  @Override
  protected Object getData()
  {
    return null;
  }

}
