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

import de.jost_net.JVerein.gui.action.WirtschaftsplanDuplizierenAction;
import de.jost_net.JVerein.gui.action.WirtschaftsplanungDeleteAction;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

public class WirtschaftsplanungListMenu extends ContextMenu
{
  public WirtschaftsplanungListMenu()
  {
    addItem(new CheckedContextMenuItem("Duplizieren", new WirtschaftsplanDuplizierenAction(), "edit-copy.png"));
    addItem(new CheckedContextMenuItem("Löschen", new WirtschaftsplanungDeleteAction(), "user-trash-full.png"));
  }
}
