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

import de.jost_net.JVerein.gui.action.SaldoDetailAction;
import de.jost_net.JVerein.gui.action.SaldoSteuerbuchungAction;
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.BuchungsklasseSaldoControl;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.SaldoBuchungAction;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.logging.Logger;

public class SaldoMenu extends ContextMenu
{
  public SaldoMenu(AbstractSaldoControl control) throws RemoteException
  {
    addItem(new BearbeitenItem("Bearbeiten", new SaldoDetailAction(),
        "text-x-generic.png"));
    addItem(new BuchungenItem("Buchungen anzeigen",
        new SaldoBuchungAction(control), "text-x-generic.png"));
    if ((boolean) Einstellungen.getEinstellung(Property.OPTIERT)
        && control instanceof BuchungsklasseSaldoControl)
    {
      addItem(new SteuerBuchungenItem("Steuerbuchungen anzeigen",
          new SaldoSteuerbuchungAction(control), "text-x-generic.png"));
    }
  }

  private class BearbeitenItem extends CheckedSingleContextMenuItem
  {
    public BearbeitenItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (!(o instanceof PseudoDBObject))
      {
        return false;
      }
      try
      {
        Integer art = ((PseudoDBObject) o).getInteger(AbstractSaldoControl.ART);
        if (art == null)
        {
          return false;
        }

        return art.equals(AbstractSaldoControl.ART_DETAIL)
            || art.equals(AbstractSaldoControl.ART_HEADER);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }

  private class BuchungenItem extends CheckedSingleContextMenuItem
  {
    public BuchungenItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (!(o instanceof PseudoDBObject))
      {
        return false;
      }
      try
      {
        Integer art = ((PseudoDBObject) o).getInteger(AbstractSaldoControl.ART);
        if (art == null)
        {
          return false;
        }

        return art.equals(AbstractSaldoControl.ART_DETAIL)
            || art.equals(AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }

  private class SteuerBuchungenItem extends CheckedSingleContextMenuItem
  {
    public SteuerBuchungenItem(String text, Action action, String icon)
    {
      super(text, action, icon);
    }

    @Override
    public boolean isEnabledFor(Object o)
    {
      if (!(o instanceof PseudoDBObject))
      {
        return false;
      }
      try
      {
        Object steuer = ((PseudoDBObject) o)
            .getInteger(AbstractSaldoControl.STEUERBETRAG);

        return steuer != null;
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      return false;
    }
  }
}
