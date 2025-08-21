/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.gui.dialogs.DropdownDialog;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.server.WirtschaftsplanImpl;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanAddBuchungsartAction implements Action
{
  private final WirtschaftsplanControl control;

  private final int art;

  public WirtschaftsplanAddBuchungsartAction(WirtschaftsplanControl control,
      int art)
  {
    this.control = control;
    this.art = art;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof WirtschaftsplanNode))
    {
      throw new ApplicationException(
          "Kein Eintrag im Wirtschaftsplan ausgewählt");
    }
    WirtschaftsplanNode node = (WirtschaftsplanNode) context;
    try
    {
      @SuppressWarnings("rawtypes")
      GenericIterator childrenIterator = node.getChildren();
      List<WirtschaftsplanNode> items = new ArrayList<>();

      while (childrenIterator.hasNext())
      {
        WirtschaftsplanNode child = (WirtschaftsplanNode) childrenIterator
            .next();
        items.add(child);
      }

      DBIterator<Buchungsart> iterator;
      List<Buchungsart> buchungsarten = new ArrayList<>();

      iterator = Einstellungen.getDBService().createList(Buchungsart.class);
      iterator.addFilter("art = ?", art);
      if (!(boolean) Einstellungen
          .getEinstellung(Einstellungen.Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        String buchungsklasseId = node.getBuchungsklasse().getID();
        iterator.addFilter("buchungsklasse = ?", buchungsklasseId);
      }

      while (iterator.hasNext())
      {
        Buchungsart buchungsart = iterator.next();
        if (items.stream().map(WirtschaftsplanNode::getBuchungsart)
            .noneMatch(art1 -> {
              try
              {
                return art1.equals(buchungsart);
              }
              catch (RemoteException e)
              {
                throw new RuntimeException(e);
              }
            }))
        {
          buchungsarten.add(buchungsart);
        }
      }

      DropdownDialog<Buchungsart> dialog = new DropdownDialog<>(buchungsarten);
      Buchungsart buchungsart = dialog.open();

      if (buchungsart == null)
      {
        throw new OperationCanceledException();
      }

      node.addChild(new WirtschaftsplanNode(node, buchungsart, art,
          control.getWirtschaftsplan()));

      if (art == WirtschaftsplanImpl.EINNAHME)
      {
        control.getEinnahmen();
      }
      else
      {
        control.getAusgaben();
      }
    }
    catch (OperationCanceledException ignored)
    {
    }
    catch (Exception e)
    {
      throw new ApplicationException("Fehler beim Hinzufügen der Buchungsart");
    }
  }
}
