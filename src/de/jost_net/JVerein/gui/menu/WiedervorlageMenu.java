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

import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.WiedervorlageDeleteAction;
import de.jost_net.JVerein.gui.action.WiedervorlageErledigungAction;
import de.jost_net.JVerein.gui.action.WiedervorlageErledigungDeleteAction;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.WiedervorlageDetailView;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Wiedervorlagen.
 */
public class WiedervorlageMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Wiedervorlagen.
   */
  public WiedervorlageMenu(JVereinTablePart table)
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new EditAction(WiedervorlageDetailView.class, table),
        "text-x-generic.png"));
    addItem(new WiedervorlageNichtErledigtItem("Erledigung setzen",
        new WiedervorlageErledigungAction(), "check.png"));
    addItem(new WiedervorlageErledigtItem("Erledigung löschen",
        new WiedervorlageErledigungDeleteAction(), "user-trash-full.png"));
    addItem(new CheckedContextMenuItem("Löschen",
        new WiedervorlageDeleteAction(), "user-trash-full.png"));
    if (table != null)
    {
      addItem(ContextMenuItem.SEPARATOR);
      addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
          new MitgliedDetailAction(), "user-friends.png"));
    }
  }

  private static class WiedervorlageErledigtItem
      extends CheckedSingleContextMenuItem
  {
    private WiedervorlageErledigtItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Wiedervorlage)
      {
        Wiedervorlage w = (Wiedervorlage) o;
        try
        {
          return w.getErledigung() != null;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }

  private static class WiedervorlageNichtErledigtItem
      extends CheckedSingleContextMenuItem
  {
    private WiedervorlageNichtErledigtItem(String text, Action action,
        String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Wiedervorlage)
      {
        Wiedervorlage w = (Wiedervorlage) o;
        try
        {
          return w.getErledigung() == null;
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
