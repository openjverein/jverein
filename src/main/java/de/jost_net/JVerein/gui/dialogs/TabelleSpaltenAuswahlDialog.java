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

import de.jost_net.JVerein.gui.parts.IJVereinPart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
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
  private Map<IJVereinPart, JVereinTablePart> tableMap = new LinkedHashMap<>();

  public TabelleSpaltenAuswahlDialog(IJVereinPart... tableParts)
      throws RemoteException, ApplicationException
  {
    super(TabelleSpaltenAuswahlDialog.POSITION_CENTER);

    for (IJVereinPart table : tableParts)
    {
      if (table == null)
      {
        continue;
      }
      tableMap.put(table, null);
    }
    setTitle("Spalten auswählen");
    setSize(400, SWT.DEFAULT);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void paint(Composite parent) throws Exception
  {
    TabFolder folder = null;
    for (IJVereinPart table : tableMap.keySet())
    {
      JVereinTablePart part = new JVereinTablePart(table.getAllColums(), null)
      {
        @Override
        protected void orderBy(int index)
        {
          return;
        }
      };

      part.addColumn("Name", "name");
      part.setCheckable(true);

      if (tableMap.size() > 1)
      {
        if (folder == null)
        {
          folder = new TabFolder(parent, SWT.BORDER);
          folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        }
        TabGroup tab = new TabGroup(folder, table.getTableName(), true, 1);
        tab.addPart(part);
      }
      else
      {
        part.paint(parent);
      }

      List<Column> auswahl = table.getColums();
      for (Column col : table.getAllColums())
      {
        part.setChecked(col, auswahl.contains(col));
      }
      tableMap.put(table, part);
    }

    ButtonArea buttons = new ButtonArea();

    buttons.addButton("Speichern", c -> {
      try
      {
        for (Entry<IJVereinPart, JVereinTablePart> entry : tableMap.entrySet())
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

    buttons.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    buttons.paint(parent);
  }

  @Override
  protected Object getData()
  {
    return null;
  }
}
