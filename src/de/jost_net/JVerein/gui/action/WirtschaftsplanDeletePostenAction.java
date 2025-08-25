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

import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanDeletePostenAction implements Action
{
  private final WirtschaftsplanControl control;

  public WirtschaftsplanDeletePostenAction(WirtschaftsplanControl control)
  {
    this.control = control;
  }

  @SuppressWarnings("rawtypes")
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
      GenericIterator artIterator;
      switch (node.getType())
      {
        case POSTEN:
          ((WirtschaftsplanNode) node.getParent()).removeChild(node);
          control.reloadSoll((WirtschaftsplanNode) node.getParent());
          break;
        case BUCHUNGSART:
          artIterator = node.getChildren();
          while (artIterator.hasNext())
          {
            WirtschaftsplanNode currentNode = (WirtschaftsplanNode) artIterator
                .next();
            ((WirtschaftsplanNode) currentNode.getParent())
                .removeChild(currentNode);
          }
          control.reloadSoll(node);
          break;
        case BUCHUNGSKLASSE:
          GenericIterator klasseIterator = node.getChildren();
          while (klasseIterator.hasNext())
          {
            WirtschaftsplanNode currentNode = (WirtschaftsplanNode) klasseIterator
                .next();
            artIterator = currentNode.getChildren();
            while (artIterator.hasNext())
            {
              WirtschaftsplanNode posten = (WirtschaftsplanNode) artIterator
                  .next();
              ((WirtschaftsplanNode) posten.getParent()).removeChild(posten);
            }
          }
          control.reloadSoll(node);
          break;
        case UNBEKANNT:
          throw new ApplicationException("Fehler beim Löschen der Posten");
      }
      control.setToChanged();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Löschen der Posten");
    }
  }
}
