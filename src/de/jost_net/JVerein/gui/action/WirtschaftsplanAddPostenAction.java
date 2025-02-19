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
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanPostenDialog;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanAddPostenAction implements Action
{
  private final WirtschaftsplanControl control;
  private final int art;

  public WirtschaftsplanAddPostenAction(WirtschaftsplanControl control,
      int art)
  {
    this.control = control;
    this.art = art;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (! (context instanceof WirtschaftsplanNode))
    {
      throw new ApplicationException("Fehler beim Anlegen des Postens");
    }
    WirtschaftsplanNode node = (WirtschaftsplanNode) context;

    try
    {
      WirtschaftsplanItem item = Einstellungen.getDBService()
          .createObject(WirtschaftsplanItem.class, null);
      item.setBuchungsartId(node.getBuchungsart().getID());
      item.setBuchungsklasseId(
          ((WirtschaftsplanNode) node.getParent()).getBuchungsklasse()
              .getID());
      WirtschaftsplanPostenDialog dialog = new WirtschaftsplanPostenDialog(
          item);

      WirtschaftsplanItem neuesItem = dialog.open();
      if (neuesItem == null)
      {
        throw new OperationCanceledException();
      }

      node.addChild(new WirtschaftsplanNode(node, item));

      control.reloadSoll(node, art);
    }
    catch (OperationCanceledException ignored) {}
    catch (Exception e)
    {
      throw new ApplicationException("Fehler beim Anlegen des Postens");
    }
  }
}
