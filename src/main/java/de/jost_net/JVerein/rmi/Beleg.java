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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.keys.BelegArt;
import de.willuhn.util.ApplicationException;

public interface Beleg extends JVereinDBObject
{
  public String getBelegnummer() throws RemoteException;

  public void setBelegnummer(String belegnummer) throws RemoteException;

  public BelegArt getBelegArt() throws RemoteException;

  public void setBelegArt(BelegArt belegart) throws RemoteException;

  String getRootDir() throws RemoteException;

  String getDateiPfad(String filename) throws RemoteException;

  Date getDatum() throws RemoteException;

  void setDatum(Date datum) throws RemoteException;

  String getBemerkung() throws RemoteException;

  void setBemerkung(String bemerkung) throws RemoteException;

  String getUUID() throws RemoteException;

  void setUUID(String uuid) throws RemoteException;

  File getFile() throws IOException, ApplicationException;

  void setFile(File file) throws RemoteException;

  void setHash(byte[] hash) throws RemoteException;

  byte[] getHash() throws RemoteException;

  String getPfad() throws RemoteException;

  void setPfad(String pfad) throws RemoteException;

}
