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

import de.jost_net.JVerein.gui.dialogs.WirtschaftsplanungNeuDialog;
import de.jost_net.JVerein.gui.view.WirtschaftsplanungView;
import de.jost_net.JVerein.io.WirtschaftsplanungZeile;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanungNeuAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      WirtschaftsplanungNeuDialog neuDialog = new WirtschaftsplanungNeuDialog();
      WirtschaftsplanungZeile wirtschaftsplan = neuDialog.open();
      GUI.startView(WirtschaftsplanungView.class, wirtschaftsplan);
    }
    catch (Exception e)
    {
      throw new ApplicationException(e);
    }
  }
}
