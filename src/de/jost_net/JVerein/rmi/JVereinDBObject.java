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

package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

/**
 * Basis-Interface fuer alle DB-Klassen in JVerein.
 */
public interface JVereinDBObject extends DBObject
{
  /**
   * Prueft, ob das Objekt seit dem Laden geaendert wurde. Da hasChanged
   * protected ist, muss der Aufruf über diese Funktion erfolgen
   * 
   * @return true, wenn es geaendert wurde.
   * @throws RemoteException
   */
  public boolean isChanged() throws RemoteException;

  // Löschen ohne Delete Check oder eingeschränktem Check
  public void deleteForced() throws RemoteException, ApplicationException;
}
