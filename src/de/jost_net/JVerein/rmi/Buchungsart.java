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

public interface Buchungsart extends JVereinDBObject
{
  public int getNummer() throws RemoteException;

  public void setNummer(int nummer) throws RemoteException;

  public String getBezeichnung() throws RemoteException;

  public void setBezeichnung(String bezeichnung) throws RemoteException;

  public int getArt() throws RemoteException;

  public void setArt(int art) throws RemoteException;

  public Buchungsklasse getBuchungsklasse() throws RemoteException;

  public Long getBuchungsklasseId() throws RemoteException;

  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException;

  public Boolean getSpende() throws RemoteException;

  public void setSpende(Boolean spende) throws RemoteException;

  public int getStatus() throws RemoteException;

  public void setStatus(int status) throws RemoteException;

  public Boolean getAbschreibung() throws RemoteException;

  public void setAbschreibung(Boolean abschreibung) throws RemoteException;

  public String getSuchbegriff() throws RemoteException;

  public void setSuchbegriff(String suchbegriff) throws RemoteException;

  public boolean getRegexp() throws RemoteException;

  public void setRegexp(boolean regexp) throws RemoteException;

  public Steuer getSteuer() throws RemoteException;

  public void setSteuer(Steuer steuer) throws RemoteException;

  public void setSteuerId(Integer id) throws RemoteException;

}
