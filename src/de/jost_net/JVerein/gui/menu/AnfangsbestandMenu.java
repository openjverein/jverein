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

import de.jost_net.JVerein.gui.action.AnfangsbestandDeleteAction;
import de.jost_net.JVerein.gui.action.AnfangsbestandDetailAction;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Anfangsbeständen.
 */
public class AnfangsbestandMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Zusatzbeträge.
   */
  public AnfangsbestandMenu()
  {
    addItem(new SingleAnfangsbestandItem("Bearbeiten",
        new AnfangsbestandDetailAction(), "text-x-generic.png"));
    addItem(new SingleAnfangsbestandItem("Löschen",
        new AnfangsbestandDeleteAction(), "user-trash-full.png"));
  }

  private static class SingleAnfangsbestandItem
      extends CheckedSingleContextMenuItem
  {
    private SingleAnfangsbestandItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Anfangsbestand)
      {
        Anfangsbestand a = (Anfangsbestand) o;
        try
        {
          return a.getJahresabschluss() == null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }
}
