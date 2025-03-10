/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.gui.action.WirtschaftsplanAddBuchungsartAction;
import de.jost_net.JVerein.gui.action.WirtschaftsplanAddPostenAction;
import de.jost_net.JVerein.gui.action.WirtschaftsplanDeletePostenAction;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

public class WirtschaftsplanMenu extends ContextMenu
{
  public WirtschaftsplanMenu(int art, WirtschaftsplanControl control)
  {
    addItem(new BuchungsklasseItem("Buchungsart hinzufügen", new WirtschaftsplanAddBuchungsartAction(control, art), "list-add.png"));
    addItem(new BuchungsartItem("Posten hinzufügen", new WirtschaftsplanAddPostenAction(control, art), "list-add.png"));
    addItem(ContextMenuItem.SEPARATOR);

    addItem(new CheckedContextMenuItem("Posten löschen", new WirtschaftsplanDeletePostenAction(control, art), "user-trash-full.png"));
  }

  private static class BuchungsklasseItem extends CheckedContextMenuItem
  {
    private BuchungsklasseItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof WirtschaftsplanNode)
      {
        WirtschaftsplanNode node = (WirtschaftsplanNode) o;
        return node.getType()
            .equals(WirtschaftsplanNode.Type.BUCHUNGSKLASSE);
      }
      return false;
    }
  }

  private static class BuchungsartItem extends CheckedContextMenuItem
  {
    private BuchungsartItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof WirtschaftsplanNode)
      {
        WirtschaftsplanNode node = (WirtschaftsplanNode) o;
        return node.getType().equals(WirtschaftsplanNode.Type.BUCHUNGSART);
      }
      return false;
    }
  }
}
