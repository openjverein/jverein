/**********************************************************************
 * 
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

import de.jost_net.JVerein.gui.action.LehrgangsmitgliederAction;
import de.jost_net.JVerein.gui.control.LehrgangDetailControl;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

/**
 * Kontext-Menu zu den Lehrgängen
 */
public class LehrgangsmitgliederMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Lehrgangsmitglieder.
   */
  public LehrgangsmitgliederMenu(LehrgangDetailControl control)
  {
    addItem(
        new CheckedSingleContextMenuItem("Bearbeiten",
            new LehrgangsmitgliederAction(
                LehrgangsmitgliederAction.ART.BEARBEITEN, control),
            "text-x-generic.png"));
    addItem(new DeleteMenueItem(new LehrgangsmitgliederAction(
        LehrgangsmitgliederAction.ART.LOESCHEN, control)));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
        new LehrgangsmitgliederAction(
            LehrgangsmitgliederAction.ART.MITGLIED_ANZEIGEN, control),
        "user-friends.png"));
    addItem(
        new CheckedContextMenuItem("Forderung erstellen",
            new LehrgangsmitgliederAction(
                LehrgangsmitgliederAction.ART.FORDERUNG, control),
            "lastschrift.png"));
  }
}
