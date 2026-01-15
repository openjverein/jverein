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
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.util.ApplicationException;

/**
 * Interface für den zugriff auf Mail Betreff und Text
 */
public interface IMailControl
{

  /**
   * @return Betreff der Mail
   */
  public String getBetreffString() throws RemoteException;

  /**
   * @return Text der Mail
   */
  public String getTxtString() throws RemoteException;

  /**
   * @return Liste der Mailempfänger
   */
  default List<Mitglied> getEmpfaengerList()
      throws RemoteException, ApplicationException
  {
    return null;
  }

  /**
   * @return Liste der Mailempfänger und Anhänge
   */
  default Map<Mitglied, Object> getDruckMailList()
      throws RemoteException, ApplicationException
  {
    return null;
  }
}
