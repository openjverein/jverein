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

import de.jost_net.JVerein.gui.control.WirtschaftsplanungControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanungNode;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

import java.rmi.RemoteException;

public class WirtschaftsplanungDeletePostenAction implements Action
{
  private final WirtschaftsplanungControl control;
  private final int art;

  public WirtschaftsplanungDeletePostenAction(WirtschaftsplanungControl control,
      int art)
  {
    this.control = control;
    this.art = art;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof WirtschaftsplanungNode))
    {
      throw new ApplicationException("Fehler beim Löschen der Posten");
    }

    WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

    try
    {
      switch (node.getType())
      {
        case POSTEN:
          ((WirtschaftsplanungNode) node.getParent()).removeChild(node);
          control.reloadSoll((WirtschaftsplanungNode) node.getParent(), art);
          break;
        case BUCHUNGSART:
          GenericIterator iterator = node.getChildren();
          while (iterator.hasNext())
          {
            WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator.next();
            ((WirtschaftsplanungNode) currentNode.getParent()).removeChild(
                currentNode);
          }
          if (node.getIst() == 0.)
          {
            ((WirtschaftsplanungNode) node.getParent()).removeChild(node);
          }

          control.reloadSoll(node, art);
          break;
        case BUCHUNGSKLASSE:
          GenericIterator iterator1 = node.getChildren();
          while (iterator1.hasNext())
          {
            boolean hasChanged = false;

            WirtschaftsplanungNode currentNode = (WirtschaftsplanungNode) iterator1.next();
            GenericIterator iterator2 = currentNode.getChildren();
            while (iterator2.hasNext())
            {
              WirtschaftsplanungNode posten = (WirtschaftsplanungNode) iterator2.next();
              ((WirtschaftsplanungNode) posten.getParent()).removeChild(
                  posten);
              hasChanged = true;
            }

            if (currentNode.getIst() == 0.)
            {
              ((WirtschaftsplanungNode) currentNode.getParent()).removeChild(
                  currentNode);
            }

            if (hasChanged)
            {
              control.reloadSoll(currentNode, art);
            }
          }
          break;
        case UNBEKANNT:
          throw new ApplicationException("Fehler beim Löschen der Posten");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Löschen der Posten");
    }
  }
}
