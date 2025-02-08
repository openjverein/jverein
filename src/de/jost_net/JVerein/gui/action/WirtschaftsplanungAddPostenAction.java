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
import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungPostenDialog;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanungAddPostenAction implements Action
{
  private final WirtschaftsplanungControl control;
  private final int art;

  public WirtschaftsplanungAddPostenAction(WirtschaftsplanungControl control,
      int art)
  {
    this.control = control;
    this.art = art;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (! (context instanceof WirtschaftsplanungNode))
    {
      throw new ApplicationException("Fehler beim Anlegen des Postens");
    }
    WirtschaftsplanungNode node = (WirtschaftsplanungNode) context;

    try
    {
      WirtschaftsplanItem item = Einstellungen.getDBService()
          .createObject(WirtschaftsplanItem.class, null);
      item.setBuchungsartId(node.getBuchungsart().getID());
      item.setBuchungsklasseId(
          ((WirtschaftsplanungNode) node.getParent()).getBuchungsklasse()
              .getID());
      WirtschaftsplanungPostenDialog dialog = new WirtschaftsplanungPostenDialog(
          item);
      node.addChild(new WirtschaftsplanungNode(node, dialog.open()));

      control.reloadSoll(node, art);
    }
    catch (Exception e)
    {
      throw new ApplicationException("Fehler beim Anlegen des Postens");
    }
  }
}
