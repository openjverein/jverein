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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.dialogs.TreePartExportDialog;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class JVereinTreePart extends TreePart implements IJVereinPart
{

  private Control treeControl;

  private List<Column> allColumns = new LinkedList<Column>();

  private String tablePartId;

  private String tableName = null;

  /**
   * Erzeugt einen neuen Tree basierend auf dem uebergebenen Objekt.
   * 
   * @param object
   *          Das Objekt, fuer das der Baum erzeugt werden soll.
   * @param action
   *          Action, die bei der Auswahl eines Elements ausgeloest werden soll.
   */
  public JVereinTreePart(Object object, Action action)
  {
    super(object, action);
    setRememberColWidths(true);
    setRememberOrder(true);
  }

  public void setAction(Action action)
  {
    this.action = action;
  }

  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    if (columns.size() > 1
        && (Boolean) Einstellungen.getEinstellung(Property.LEERESPALTE))
    {
      addColumn(new Column("leer", ""));
    }
    super.paint(parent);
  }

  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);

    if (e.equals(Feature.Event.PAINT))
    {
      this.treeControl = ctx.control;
    }
    return ctx;
  }

  @Override
  public void addColumn(Column col)
  {
    addColumn(col, true);
  }

  public void addColumn(Column col, boolean defaultVisible)
  {
    try
    {
      if (settings.getBoolean(getTablePartID() + col.getName(), defaultVisible))
      {
        super.addColumn(col);
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim ermitteln der TablePartID", e);
      // Dann zeigen wir sie mit an
      super.addColumn(col);
    }
    // Leere Dummy-Spalte brauchen wir hier nicht
    if (!col.getName().isBlank())
    {
      this.allColumns.add(col);
    }
  }

  /**
   * Speichert die anzuzeigenden Spalten
   * 
   * @param columns
   * @throws RemoteException
   */
  @Override
  public void saveSpalten(List<Column> columns) throws RemoteException
  {
    for (Column c : allColumns)
    {
      settings.setAttribute(getTablePartID() + c.getName(),
          columns.contains(c));
    }
  }

  /**
   * Ermittelt die ID der Tablepart aus der View und dem Objekttyp
   * 
   * @return
   * @throws RemoteException
   */
  private String getTablePartID() throws RemoteException
  {
    if (tablePartId != null)
    {
      return tablePartId;
    }
    List<?> items = getItems();

    if (items.size() == 0)
    {
      tablePartId = "";
      return tablePartId;
    }
    StringBuilder sb = new StringBuilder();

    sb.append(GUI.getCurrentView().getClass().getSimpleName());
    sb.append(".");
    if (tableName != null)
    {
      sb.append(tableName);
    }
    else
    {
      sb.append(items.get(0).getClass().getSimpleName());
    }
    sb.append(".");

    tablePartId = sb.toString();
    return tablePartId;
  }

  /**
   * Holt alle Spalten der Tabelle, auch die ausgeblendeten
   * 
   * @return
   */
  @Override
  public List<Column> getAllColums()
  {
    return allColumns;
  }

  /**
   * Holt alle sichtbaren Spalten der Tabelle
   * 
   * @return
   */
  @Override
  public List<Column> getColums()
  {
    return columns;
  }

  /**
   * Exportiert die aktuell angezeigte Tabelle als PDF oder CSV
   * 
   * @param title
   * @param subtitle
   * @param filename
   * @param art
   * @throws ApplicationException
   */
  public void export(String title, String subtitle, String filename,
      ExportArt art) throws ApplicationException
  {
    try
    {
      if (!new TreePartExportDialog((Tree) treeControl, getTablePartID(), art,
          title, subtitle, filename).open())
      {
        throw new OperationCanceledException();
      }
    }
    catch (OperationCanceledException | ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Erstellen der Auswertung.", e);
      throw new ApplicationException("Fehler beim Erstellen der Auswertung.");
    }
  }

  public void setTableName(String name)
  {
    tableName = name;
  }

  @Override
  public String getTableName()
  {
    return tableName;
  }

}
