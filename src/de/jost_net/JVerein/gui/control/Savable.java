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

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

/**
 * Dieses Intervace ermöglicht die Überwachung von Änderungen im Control. Das
 * Aufbereiten und Zuordnen der Daten (prepareStore()) wird vom Speichern
 * (handleSotre()) getrennt.
 */
public interface Savable
{
  /**
   * Bereitet die Daten zum Speichern vor, es wird jedoch noch nicht
   * gespeichert.
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  public DBObject prepareStore()
      throws RemoteException, ApplicationException;

  /**
   * Speichert die vorbereiteten Daten in der DB. Ruft vorher prepareStotre()
   * auf.
   * 
   * @throws ApplicationException
   */
  public void handleStore() throws ApplicationException;

  /**
   * Diese Funktion kann implementiert werden, um selbst zu testen, ob der
   * Eintrag geändert wurde. In diesem Fall muss true zurückgegeben werden.
   * 
   * @return true wenn der Eintrag geändert wurde
   * @throws RemoteException
   */
  public default boolean hasChanged() throws RemoteException
  {
    return false;
  }
}
