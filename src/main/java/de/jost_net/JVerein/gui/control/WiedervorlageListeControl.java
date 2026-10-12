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
package de.jost_net.JVerein.gui.control;

import java.rmi.RemoteException;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.WiedervorlageList;
import de.jost_net.JVerein.gui.view.WiedervorlageListeView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.util.ApplicationException;

public class WiedervorlageListeControl extends FilterControl
{

  private WiedervorlageList wiedervorlageList = null;

  public WiedervorlageListeControl(AbstractView view)
  {
    super(view, WiedervorlageControl.class);
  }

  @Override
  public AutoUpdateTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    if (wiedervorlageList != null)
    {
      return wiedervorlageList.getWiedervorlageList();
    }
    wiedervorlageList = new WiedervorlageList(
        new StartViewAction(WiedervorlageListeView.class), this);
    return wiedervorlageList.getWiedervorlageList();
  }

  @Override
  protected void TabRefresh() throws ApplicationException
  {
    if (wiedervorlageList == null)
    {
      return;
    }
    wiedervorlageList.refresh();
  }

  @Override
  protected String getTableTitle()
  {
    return VorlageUtil.getName(VorlageTyp.WIEDERVORLAGEN_TITEL, this);
  }

  @Override
  protected String getTableSubtitle()
  {
    return VorlageUtil.getName(VorlageTyp.WIEDERVORLAGEN_SUBTITEL, this);
  }

  @Override
  protected String getTableDateiname()
  {
    return VorlageUtil.getName(VorlageTyp.WIEDERVORLAGEN_DATEINAME, this);
  }
}
