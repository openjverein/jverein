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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoMahnungAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoRechnungAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoSollbuchungEditAction;
import de.jost_net.JVerein.gui.action.MitgliedskontoSollbuchungLoeschenAction;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Mitgliedskonten.
 */
public class SollbuchungMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer Mitgliedskonten.
   */
  public SollbuchungMenu()
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten",
        new MitgliedskontoSollbuchungEditAction(), "text-x-generic.png"));
    addItem(new SollOhneIstItem("Löschen",
        new MitgliedskontoSollbuchungLoeschenAction(), "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Mitglied anzeigen",
        new MitgliedDetailAction(), "user-friends.png"));
    /*addItem(new CheckedContextMenuItem("Rechnung erstellen",
        new MitgliedskontoRechnungAction(), "file-invoice.png"));
    addItem(new CheckedContextMenuItem("Mahnung erstellen",
        new MitgliedskontoMahnungAction(), "file-invoice.png"));
        */
  }

  private static class SollOhneIstItem extends CheckedContextMenuItem
  {

    private SollOhneIstItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Mitgliedskonto)
      {
        Mitgliedskonto mk = (Mitgliedskonto) o;
        DBIterator<Buchung> it;
        try
        {
          it = Einstellungen.getDBService().createList(Buchung.class);
          it.addFilter("mitgliedskonto = ?", new Object[] { mk.getID() });
          if (it.size() == 0)
          {
            return true;
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return false;
      }
      return false;
    }
  }
}
