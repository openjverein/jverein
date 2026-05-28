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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.jost_net.JVerein.gui.dialogs.TablePartExportDialog;
import de.jost_net.JVerein.gui.dialogs.TablePartExportDialog.ExportArt;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class JVereinTablePart extends TablePart
{

  private Control tableControl;

  private List<Column> allColumns = new LinkedList<Column>();

  private String tablePartId;

  private String tableName = null;

  /**
   * Erzeugt eine neue leere Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(Action action)
  {
    this((List<?>) null, action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(GenericIterator<?> list, Action action)
  {
    this(asList(list), action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(List<?> list, Action action)
  {
    super(list, action);
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

    this.tableControl = ctx.control;

    if (!e.equals(Feature.Event.PAINT))
    {
      return ctx;
    }
    Table table = (Table) ctx.control;

    // Die letzte Spalte packen wir nach Titelbreite, falls diese kleiner als
    // der gespeicherte Wert ist. So wird ggf. verhindert, dass eine horizontale
    // Scrollbar angezeigt wird, wenn es gar nicht nötig ist.
    if (table.getColumnCount() == 0)
    {
      return ctx;
    }
    TableColumn c = table.getColumn(table.getColumnCount() - 1);
    int widthOld = c.getWidth();
    c.pack();
    if (c.getWidth() > widthOld)
    {
      c.setWidth(widthOld);
    }

    return ctx;
  }

  // Überschrieben um den Checked-Status beim sortieren beizubehalten
  @Override
  protected void orderBy(int index)
  {
    if (checkable)
    {
      try
      {
        List<?> l = getItems();
        super.orderBy(index);
        setChecked(l.toArray(), true);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Sortieren");
      }
    }
    else
    {
      super.orderBy(index);
    }
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
  public List<Column> getAllColums()
  {
    return allColumns;
  }

  /**
   * Holt alle sichtbaren Spalten der Tabelle
   * 
   * @return
   */
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
      if (!new TablePartExportDialog((Table) tableControl, getTablePartID(),
          art, title, subtitle, filename).open())
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

  public String getTableName()
  {
    return tableName;
  }
}
