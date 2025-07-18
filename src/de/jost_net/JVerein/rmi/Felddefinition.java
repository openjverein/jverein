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

public interface Felddefinition extends JVereinDBObject
{
  public String getName() throws RemoteException;

  public void setName(String name) throws RemoteException;

  public String getLabel() throws RemoteException;

  public void setLabel(String label) throws RemoteException;

  public int getDatentyp() throws RemoteException;

  public void setDatentyp(int datentyp) throws RemoteException;

  public int getLaenge() throws RemoteException;

  public void setLaenge(int laenge) throws RemoteException;
}
