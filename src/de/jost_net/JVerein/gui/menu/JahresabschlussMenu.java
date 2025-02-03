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
import de.jost_net.JVerein.gui.action.JahresabschlussDeleteAction;
import de.jost_net.JVerein.gui.action.JahresabschlussDetailAction;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.logging.Logger;

/**
 * Kontext-Menu zu den Jahresabschlüssen.
 */
public class JahresabschlussMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu fuer die Liste der Jahresabschlüsse
   * 
   * @throws RemoteException
   */
  public JahresabschlussMenu() throws RemoteException
  {
    if (Einstellungen.getEinstellung().getMittelverwendung())
    {
      addItem(new SingleAnzeigenMenuItem("Anzeigen",
          new JahresabschlussDetailAction(), "text-x-generic.png"));
      addItem(new SingleBearbeitenMenuItem("Bearbeiten",
          new JahresabschlussDetailAction(), "text-x-generic.png"));
    }
    else
    {
      addItem(new CheckedSingleContextMenuItem("Anzeigen",
          new JahresabschlussDetailAction(), "text-x-generic.png"));
    }
    addItem(new CheckedContextMenuItem("Löschen",
        new JahresabschlussDeleteAction(), "user-trash-full.png"));
  }

  private static class SingleAnzeigenMenuItem
      extends CheckedSingleContextMenuItem
  {
    private SingleAnzeigenMenuItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Jahresabschluss)
      {
        Jahresabschluss ja = (Jahresabschluss) o;
        try
        {
          if (ja.getVerwendungsrueckstand() == null
              || ja.getZwanghafteWeitergabe() == null)
          {
            return false;
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return true;
    }
  }

  private static class SingleBearbeitenMenuItem
      extends CheckedSingleContextMenuItem
  {
    private SingleBearbeitenMenuItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Jahresabschluss)
      {
        Jahresabschluss ja = (Jahresabschluss) o;
        try
        {
          if (ja.getVerwendungsrueckstand() == null
              || ja.getZwanghafteWeitergabe() == null)
          {
            return true;
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      return false;
    }
  }
}
