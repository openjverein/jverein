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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.jost_net.JVerein.gui.dialogs.DropdownDialog;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class WirtschaftsplanungAddBuchungsartAction implements Action
{
  private final WirtschaftsplanungControl control;
  private final int art;

  public WirtschaftsplanungAddBuchungsartAction(
      WirtschaftsplanungControl control, int art)
  {
    this.control = control;
    this.art = art;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (! (context instanceof WirtschaftsplanungNode)) {
      throw new ApplicationException("Fehler beim Hinzufügen der Buchunsgart");
    }
    WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;
    try
    {
      @SuppressWarnings("rawtypes") GenericIterator childrenIterator = node.getChildren();
      List<WirtschaftsplanungNode> items = new ArrayList<>();

      while (childrenIterator.hasNext())
      {
        WirtschaftsplanungNode child = (WirtschaftsplanungNode) childrenIterator.next();
        items.add(child);
      }

      DBIterator<Buchungsart> iterator;
      List<Buchungsart> buchungsarten = new ArrayList<>();

      iterator = Einstellungen.getDBService().createList(Buchungsart.class);
      iterator.addFilter("art = ?", art);
      if (!Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
      {
        String buchungsklasseId = node.getBuchungsklasse().getID();
        iterator.addFilter("buchungsklasse = ?", buchungsklasseId);
      }

      while (iterator.hasNext())
      {
        Buchungsart buchungsart = iterator.next();
        if (items.stream().map(WirtschaftsplanungNode::getBuchungsart)
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

      DropdownDialog<Buchungsart> dialog = new DropdownDialog<>(
          buchungsarten);
      Buchungsart buchungsart = dialog.open();

      node.addChild(new WirtschaftsplanungNode(node, buchungsart, art,
          control.getWirtschaftsplanungZeile()));

      if (art == 0)
      {
        control.getEinnahmen();
      }
      else
      {
        control.getAusgaben();
      }
    }
    catch (OperationCanceledException ignored) {}
    catch (Exception e)
    {
      throw new ApplicationException(
          "Fehler beim Hinzufügen der Buchungsart");
    }
  }
}
