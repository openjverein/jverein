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
import de.jost_net.JVerein.gui.action.FormularAnzeigeAction;
import de.jost_net.JVerein.gui.action.FormularDeleteAction;
import de.jost_net.JVerein.gui.action.FormularDuplizierenAction;
import de.jost_net.JVerein.gui.action.FormularExportAction;
import de.jost_net.JVerein.gui.control.FormularControl;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.FormularDetailView;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu zu den Formularen.
 */
public class FormularMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Formulare.
   */
  public FormularMenu(FormularControl control, JVereinTablePart part)
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new EditAction(FormularDetailView.class, part), "text-x-generic.png"));
    addItem(new CheckedSingleContextMenuItem("Anzeigen",
        new FormularAnzeigeAction(), "edit-copy.png"));
    addItem(new CheckedSingleContextMenuItem("Duplizieren",
        new FormularDuplizierenAction(control), "edit-copy.png"));
    addItem(new CheckedSingleContextMenuItem("Löschen",
        new FormularDeleteAction(), "user-trash-full.png"));
    addItem(new CheckedContextMenuItem("Exportieren",
        new FormularExportAction(), "document-save.png"));
  }
}
