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

import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.MahnungSendAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.RechnungDeleteAction;
import de.jost_net.JVerein.gui.action.RechnungSendAction;
import de.jost_net.JVerein.gui.view.RechnungView;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

/**
 * Kontext-Menu zu den Spendenbescheinigungen.
 */
public class RechnungMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Spendenbescheinigungen.
   */
  public RechnungMenu()
  {
    addItem(new CheckedSingleContextMenuItem("Anzeigen",
        new EditAction(RechnungView.class), "text-x-generic.png"));
    addItem(new CheckedContextMenuItem("Löschen",
        new RechnungDeleteAction(), "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
        new MitgliedDetailAction(), "user-friends.png"));
    addItem(new CheckedContextMenuItem("Druck und Mail",
        new RechnungSendAction(), "document-print.png"));
    addItem(new CheckedContextMenuItem("Mahnung Druck und Mail",
        new MahnungSendAction(), "document-print.png"));
  }

}
