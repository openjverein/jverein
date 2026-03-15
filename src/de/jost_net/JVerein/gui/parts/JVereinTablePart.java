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
import java.util.List;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.logging.Logger;

public class JVereinTablePart extends TablePart
{

  /**
   * Erzeugt eine neue leere Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(Action action)
  {
    super(action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(@SuppressWarnings("rawtypes") GenericIterator list,
      Action action)
  {
    super(list, action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(@SuppressWarnings("rawtypes") List list,
      Action action)
  {
    super(list, action);
  }

  public void setAction(Action action)
  {
    this.action = action;
  }

  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);

    if (!e.equals(Feature.Event.PAINT))
    {
      return ctx;
    }
    Table table = (Table) ctx.control;

    // Die letzte Spalte packen wir nach Titelbreite, falls diese kleiner als
    // der gespeicherte Wert ist. So wird ggf. verhindert, dass eine horizontale
    // Scrollbar angezeigt wird, wenn es gar nicht nötig ist.
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

}
