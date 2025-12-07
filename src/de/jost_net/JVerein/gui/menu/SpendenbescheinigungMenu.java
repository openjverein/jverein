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

import de.jost_net.JVerein.gui.action.DeleteAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungEmailAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungSendAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungVersandAction;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungPrintAction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Spendenbescheinigungen.
 */
public class SpendenbescheinigungMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Spendenbescheinigungen.
   */
  public SpendenbescheinigungMenu(JVereinTablePart part)
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new EditAction(SpendenbescheinigungDetailView.class, part),
        "text-x-generic.png"));
    addItem(new VersandSpendenbescheinigungItem("Als \"versendet\" markieren",
        new SpendenbescheinigungVersandAction(true), "emblem-default.png",
        false));
    addItem(new VersandSpendenbescheinigungItem(
        "Als \"nicht versendet\" markieren",
        new SpendenbescheinigungVersandAction(false), "edit-undo.png", true));
    addItem(new CheckedContextMenuItem("Löschen", new DeleteAction(),
        "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
        new MitgliedDetailAction(), "user-friends.png"));
    addItem(new CheckedContextMenuItem("PDF",
        new SpendenbescheinigungPrintAction(Adressblatt.OHNE_ADRESSBLATT, true),
        "file-pdf.png"));
    addItem(new CheckedContextMenuItem("Druck und Mail",
        new SpendenbescheinigungSendAction(), "document-print.png"));
    addItem(new CheckedContextMenuItem("Mail an Spender",
        new SpendenbescheinigungEmailAction(), "envelope-open.png"));
  }

  private static class VersandSpendenbescheinigungItem
      extends CheckedContextMenuItem
  {
    boolean versendet;

    private VersandSpendenbescheinigungItem(String text, Action action,
        String icon, boolean versendet)
    {
      super(text, action, icon);
      this.versendet = versendet;
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Spendenbescheinigung)
      {
        Spendenbescheinigung spb = (Spendenbescheinigung) o;
        try
        {
          return !versendet ^ spb.getVersand();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return true;
    }
  }

}
