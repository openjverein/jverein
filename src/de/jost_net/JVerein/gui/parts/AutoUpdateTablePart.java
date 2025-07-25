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

import org.eclipse.swt.SWTException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.Listener;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;

public class AutoUpdateTablePart extends JVereinTablePart
{

  private Listener storeListener = new StoreListener();

  public AutoUpdateTablePart(Action action)
  {
    super(action);
  }

  public AutoUpdateTablePart(List<?> list, Action action)
  {
    super(list, action);
  }

  @SuppressWarnings("rawtypes")
  public AutoUpdateTablePart(GenericIterator list, Action action)
  {
    super(list, action);
  }

  @Override
  public void addItem(final Object object, int index, boolean checked)
      throws RemoteException
  {
    super.addItem(object, index, checked);
    ((DBObject) object).removeStoreListener(this.storeListener);
    ((DBObject) object).addStoreListener(this.storeListener);
  }

  /**
   * Der Listener ueberwacht das speichern von Objekten und aktuelisiert die
   * Objekte in der Tabelle.
   */
  private class StoreListener implements de.willuhn.datasource.rmi.Listener
  {

    @Override
    public void handleEvent(final de.willuhn.datasource.rmi.Event e)
        throws RemoteException
    {
      try
      {
        // Dieser Aufruf ist nur, damit wir die Exception bekommen. Bei
        // updateItem wird sie vorher abgefangen
        getItems();
        updateItem(e.getObject(), e.getObject());
      }
      catch (SWTException ex)
      {
        // Fallback: Wir versuchens mal synchronisiert
        GUI.getDisplay().syncExec(new Runnable()
        {

          @Override
          public void run()
          {
            try
            {
              updateItem(e.getObject(), e.getObject());
            }
            catch (Exception ignore)
            {
            }
          }

        });
      }
    }
  }
}
