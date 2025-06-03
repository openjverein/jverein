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

import de.jost_net.JVerein.gui.control.DateinameControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

public class DateinameVorschauAction implements Action
{

  public DateinameVorschauAction()
  {
    //
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    DateinameControl control = null;

    if (context instanceof DateinameControl)
    {
      control = (DateinameControl) context;
    }
    else
    {
      throw new ApplicationException("Falscher Kontext");
    }
    try
    {
      control.updateVorschau();
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
  }
}
