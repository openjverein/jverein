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

public interface Vorlage extends JVereinDBObject
{
  public static final String TABLE_NAME = "vorlage";

  public static final String TABLE_NAME_ID = "vorlage.id";

  public static final String PRIMARY_ATTRIBUTE = "id";

  public static final String KEY = "name";

  public static final String T_KEY = TABLE_NAME + "." + KEY;

  public static final String MUSTER = "muster";

  public static final String T_MUSTER = TABLE_NAME + "." + MUSTER;

  public String getKey() throws RemoteException;

  public String getMuster() throws RemoteException;

  public void setMuster(String text) throws RemoteException;
}
