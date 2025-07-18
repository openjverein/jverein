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

public interface Einstellung extends DBObject
{
  public static final String COL_SEPA_MANDANTID_SOURCE = "mandatid_source";

  public static final String COL_BUCHUNG_BUCHUNGSART_AUSWAHL = "buchungbuchungsartauswahl";

  public String getValue() throws RemoteException;

  public void setValue(Object value) throws RemoteException;

  public String getKey() throws RemoteException;

  public void setKey(String key) throws RemoteException;
}
