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
package de.jost_net.JVerein.gui.menu;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.action.LastschriftDeleteAction;
import de.jost_net.JVerein.gui.action.PreNotificationAction;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu zu den Lastschriften
 */
public class LastschriftMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Lastschriften.
   */
  public LastschriftMenu()
  {
    addItem(new AbgeschlossenDisabledItem("Pre-Notification",
        new PreNotificationAction(), "document-new.png"));
    addItem(new CheckedContextMenuItem("Löschen...", new LastschriftDeleteAction(),
        "user-trash-full.png"));
  }
  
  private static class AbgeschlossenDisabledItem extends CheckedContextMenuItem
  {

    private AbgeschlossenDisabledItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Lastschrift)
      {
        try
        {
          Abrechnungslauf abrl = (Abrechnungslauf) ((Lastschrift) o).getAbrechnungslauf();
          if (abrl.getAbgeschlossen())
          {
            return false;
          }
          else
          {
            return true;
          }
        }
        catch (RemoteException e)
        {
          return false;
        }
      }
      return false;
    }
  }
}
