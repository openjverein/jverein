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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.AbrechnungslaufAbschliessenAction;
import de.jost_net.JVerein.gui.action.AbrechnungslaufDeleteAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.GutschriftAction;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.PreNotificationMailView;
import de.jost_net.JVerein.gui.view.AbrechnungslaufDetailView;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Abrechnungsläufen
 */
public class AbrechnungslaufMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Abrechnungläufe
   */
  public AbrechnungslaufMenu(JVereinTablePart part)
  {
    addItem(new ContextMenuItem("Bearbeiten",
        new EditAction(AbrechnungslaufDetailView.class, part),
        "text-x-generic.png"));
    addItem(new CheckedSingleContextMenuItem("Löschen",
        new AbrechnungslaufDeleteAction(), "user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem("Gutschrift erstellen",
        new GutschriftAction(), "ueberweisung.png"));
    addItem(new CheckedSingleContextMenuItem("Pre-Notification",
        new StartViewAction(PreNotificationMailView.class, true),
        "document-print.png"));
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.ABRLABSCHLIESSEN))
      {
        addItem(ContextMenuItem.SEPARATOR);
        addItem(new AbgeschlossenDisabledItem("Abschließen",
            new AbrechnungslaufAbschliessenAction(true), "locked.png", false));
        addItem(new AbgeschlossenDisabledItem("Aufschließen",
            new AbrechnungslaufAbschliessenAction(false), "unlocked.png",
            true));
      }
    }
    catch (RemoteException e)
    {
      Logger.error("unable to extend context menu");
    }
  }

  private static class AbgeschlossenDisabledItem extends CheckedContextMenuItem
  {
    boolean abgeschlossen;

    private AbgeschlossenDisabledItem(String text, Action action, String icon,
        boolean abgeschlossen)
    {
      super(text, action, icon);
      this.abgeschlossen = abgeschlossen;
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Abrechnungslauf)
      {
        Abrechnungslauf abrl = (Abrechnungslauf) o;
        try
        {
          return !abgeschlossen ^ abrl.getAbgeschlossen()
              && !abrl.isJahrAbgeschlossen();
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
