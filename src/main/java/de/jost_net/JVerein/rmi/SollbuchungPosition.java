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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

public interface SollbuchungPosition extends JVereinDBObject
{

  public BigDecimal getBetrag() throws RemoteException;

  public void setBetrag(BigDecimal betrag) throws RemoteException;

  public BigDecimal getSteuersatz() throws RemoteException;

  public void setSteuer(Steuer steuer) throws RemoteException;

  public Long getBuchungsartId() throws RemoteException;

  public void setBuchungsartId(Long buchungsart) throws RemoteException;

  public Buchungsart getBuchungsart() throws RemoteException;

  public Buchungsklasse getBuchungsklasse() throws RemoteException;

  public Long getBuchungsklasseId() throws RemoteException;

  public void setBuchungsklasseId(Long buchungsklasse) throws RemoteException;

  public Date getDatum() throws RemoteException;

  public void setDatum(Date datum) throws RemoteException;

  public void setSollbuchung(String id) throws RemoteException;

  public Sollbuchung getSollbuchung() throws RemoteException;

  public void setZweck(String zweck) throws RemoteException;

  public String getZweck() throws RemoteException;

  public BigDecimal getNettobetrag() throws RemoteException;

  public Steuer getSteuer() throws RemoteException;

  public BigDecimal getSteuerbetrag() throws RemoteException;
}
