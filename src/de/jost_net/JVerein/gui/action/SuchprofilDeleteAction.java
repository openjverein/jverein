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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.control.MitgliedSuchProfilControl;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Suchprofil;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eines Suchprofiles
 */
public class SuchprofilDeleteAction extends DeleteAction
{
  private MitgliedSuchProfilControl control;

  public SuchprofilDeleteAction(MitgliedSuchProfilControl control)
  {
    this.control = control;
  }

  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Suchprofil))
    {
      return;
    }

    Suchprofil sp = (Suchprofil) object;
    if (control.getSettings().getString("id", "").equals(sp.getID()))
    {
      control.getSettings().setAttribute("id", "");
      control.getSettings().setAttribute("profilname", "");
    }
    sp.delete();
  }
}
