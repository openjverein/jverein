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

import de.jost_net.JVerein.gui.action.SpendenbescheinigungDeleteAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungDuplizierenAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungEmailAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungSendAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungPrintAction;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

/**
 * Kontext-Menu zu den Spendenbescheinigungen.
 */
public class SpendenbescheinigungMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Spendenbescheinigungen.
   */
  public SpendenbescheinigungMenu()
  {
    addItem(new CheckedContextMenuItem("PDF (Standard)",
        new SpendenbescheinigungPrintAction(true, false), "file-pdf.png"));
    addItem(new CheckedContextMenuItem("PDF (Standard, Mit Adressblatt)",
        new SpendenbescheinigungPrintAction(true, true), "file-pdf.png"));
    addItem(new CheckedContextMenuItem("PDF (Individuell)",
        new SpendenbescheinigungPrintAction(false, false), "file-pdf.png"));
    addItem(new CheckedContextMenuItem("PDF (Individuell, Mit Adressblatt)",
        new SpendenbescheinigungPrintAction(false, true), "file-pdf.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("E-Mail an Spender",
        new SpendenbescheinigungEmailAction(), "envelope-open.png"));
    addItem(new CheckedContextMenuItem("Spendenbescheinigungen versenden",
        new SpendenbescheinigungSendAction(), "envelope-open.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new DuplicateMenuItem("Als Vorlage f�r neue Spende",
        new SpendenbescheinigungDuplizierenAction(), "edit-copy.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem("L�schen",
        new SpendenbescheinigungDeleteAction(), "user-trash-full.png"));
  }

  private static class DuplicateMenuItem extends CheckedContextMenuItem
  {

    /**
     * @param text
     * @param action
     * @param icon
     *          Optionale Angabe eines Icons.
     */
    private DuplicateMenuItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Spendenbescheinigung)
      {
        return true;
      }
      if (o instanceof Spendenbescheinigung[])
      {
        return false;
      }
      return super.isEnabledFor(o);
    }
  }

}
