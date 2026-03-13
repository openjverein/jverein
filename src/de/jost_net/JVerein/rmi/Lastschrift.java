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
import java.util.Date;

import de.jost_net.JVerein.io.IAdresse;
import de.jost_net.JVerein.io.ILastschrift;
import de.jost_net.JVerein.server.IGutschriftProvider;

public interface Lastschrift
    extends JVereinDBObject, ILastschrift, IGutschriftProvider
{

  public Abrechnungslauf getAbrechnungslauf() throws RemoteException;

  public void setAbrechnungslauf(int abrechnungslauf) throws RemoteException;

  public Kursteilnehmer getKursteilnehmer() throws RemoteException;

  public void setKursteilnehmer(int kursteilnehmer) throws RemoteException;

  public void setPersonenart(String personenart) throws RemoteException;

  public void setAnrede(String anrede) throws RemoteException;

  public void setTitel(String titel) throws RemoteException;

  public void setName(String name) throws RemoteException;

  public void setVorname(String vorname) throws RemoteException;

  public void setStrasse(String strasse) throws RemoteException;

  public void setAdressierungszusatz(String adressierungszusatz)
      throws RemoteException;

  public void setPlz(String plz) throws RemoteException;

  public void setOrt(String ort) throws RemoteException;

  public void setStaat(String staat) throws RemoteException;

  public void setEmail(String email) throws RemoteException;

  public void setMandatID(String mandatid) throws RemoteException;

  public void setMandatDatum(Date mandatdatum) throws RemoteException;

  public String getMandatSequence() throws RemoteException;

  public void setMandatSequence(String mandatsequence) throws RemoteException;

  public String getVerwendungszweck() throws RemoteException;

  public void setVerwendungszweck(String verwendungszweck)
      throws RemoteException;

  public void setBetrag(Double betrag) throws RemoteException;

  public void setGeschlecht(String geschlecht) throws RemoteException;

  public void set(IAdresse adr) throws RemoteException;

  public Date getVersanddatum() throws RemoteException;

  public void setVersanddatum(Date datum) throws RemoteException;
}
