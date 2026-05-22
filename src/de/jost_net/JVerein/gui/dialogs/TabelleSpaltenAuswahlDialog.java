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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.rmi.JVereinDBObject;
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
  /**
   * Map mit dem TablePart aus der View als Key, und dem TablePart im Dialog als
   * Value.
   */
  private Map<JVereinTablePart, JVereinTablePart> tableMap = new LinkedHashMap<>();

  public TabelleSpaltenAuswahlDialog(JVereinTablePart... tableParts)
      throws RemoteException, ApplicationException
  {
    super(TabelleSpaltenAuswahlDialog.POSITION_CENTER);

    boolean leer = true;
    for (JVereinTablePart table : tableParts)
    {
      if (table == null)
      {
        continue;
      }
      tableMap.put(table, null);
      if (table.getItems().size() > 0)
      {
        leer = false;
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
    for (JVereinTablePart table : tableMap.keySet())
    {
      if (table.getItems().size() == 0)
      {
        continue;
      }
      JVereinTablePart part = new JVereinTablePart(table.getAllColums(), null);
      part.addColumn("Name", "name");
      part.setCheckable(true);

      String name = table.getTableName();
      Object o = table.getItems().get(0);
      if (o instanceof JVereinDBObject && name == null)
      {
        name = ((JVereinDBObject) o).getObjektNameMehrzahl();
      }
      if (name == null)
      {
        name = "Tabelle";
      }
      if (tableMap.size() > 1)
      {
        if (folder == null)
        {
          folder = new TabFolder(parent, SWT.BORDER);
          folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        }
        TabGroup tab = new TabGroup(folder, name, true, 1);
        tab.addPart(part);
      }
      else
      {
        LabelGroup group = new LabelGroup(parent, name);
        group.addPart(part);
      }

      List<Column> auswahl = table.getColums();
      for (Column col : table.getAllColums())
      {
        part.setChecked(col, auswahl.contains(col));
      }
      tableMap.put(table, part);
    }

    ButtonArea b = new ButtonArea();
    b.addButton("Speichern", c -> {
      try
      {
        for (Entry<JVereinTablePart, JVereinTablePart> entry : tableMap
            .entrySet())
        {
          if (entry.getValue() == null)
          {
            continue;
          }
          entry.getKey().saveSpalten(entry.getValue().getItems());
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
