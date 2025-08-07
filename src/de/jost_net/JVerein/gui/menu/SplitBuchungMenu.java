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

import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.SplitBuchungDeleteAction;
import de.jost_net.JVerein.gui.action.SplitBuchungWiederherstellenAction;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.logging.Logger;

public class SplitBuchungMenu extends ContextMenu
{
  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Splitbuchungen.
   */

  public SplitBuchungMenu(BuchungsControl control)
  {
    addItem(new CheckedSplitBuchungItem("Bearbeiten", new BuchungAction(true),
        "text-x-generic.png"));
    addItem(new DeleteSplitBuchungItem("Löschen",
        new SplitBuchungDeleteAction(control), "user-trash-full.png"));
    addItem(new RestoreSplitBuchungItem("Wiederherstellen",
        new SplitBuchungWiederherstellenAction(control), "edit-undo.png"));
  }

  private static class CheckedSplitBuchungItem
      extends CheckedSingleContextMenuItem
  {
    private CheckedSplitBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        try
        {
          return !((Buchung) o).isToDelete();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }

  private static class DeleteSplitBuchungItem extends CheckedContextMenuItem
  {
    private DeleteSplitBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        Buchung b = (Buchung) o;
        try
        {
          return !b.isToDelete() && b.getSplitTyp() == SplitbuchungTyp.SPLIT;
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }

  private static class RestoreSplitBuchungItem extends CheckedContextMenuItem
  {
    private RestoreSplitBuchungItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Buchung)
      {
        try
        {
          return ((Buchung) o).isToDelete();
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
