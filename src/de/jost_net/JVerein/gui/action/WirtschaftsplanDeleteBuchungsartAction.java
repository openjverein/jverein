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

public class WirtschaftsplanDeleteBuchungsartAction implements Action
{
  private final WirtschaftsplanControl control;

  public WirtschaftsplanDeleteBuchungsartAction(WirtschaftsplanControl control)
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
        case BUCHUNGSART:
          artIterator = node.getChildren();
          while (artIterator.hasNext())
          {
            WirtschaftsplanNode currentNode = (WirtschaftsplanNode) artIterator
                .next();
            ((WirtschaftsplanNode) currentNode.getParent())
                .removeChild(currentNode);
          }
          ((WirtschaftsplanNode) node.getParent()).removeChild(node);
          control.reloadSoll((WirtschaftsplanNode) node.getParent());
          break;
        case UNBEKANNT:
        default:
          throw new ApplicationException("Fehler beim Löschen der Buchungsart");
      }
      control.setToChanged();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Löschen der Buchungsart");
    }
  }
}
