/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.menu;

import de.jost_net.JVerein.gui.action.PseudoDBObjectDeleteAction;
import de.jost_net.JVerein.gui.action.PseudoDBObjectEditAction;
import de.jost_net.JVerein.gui.view.MailDetailView;
import de.jost_net.JVerein.rmi.Mail;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu zu den Mails.
 */
public class MitgliedMailMenu extends ContextMenu
{

  public MitgliedMailMenu()
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new PseudoDBObjectEditAction(Mail.class, MailDetailView.class),
        "text-x-generic.png"));
    addItem(new CheckedContextMenuItem("Löschen",
        new PseudoDBObjectDeleteAction(Mail.class), "user-trash-full.png"));
  }
}
