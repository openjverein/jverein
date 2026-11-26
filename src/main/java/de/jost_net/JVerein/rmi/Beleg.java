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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.keys.BelegTyp;
import de.willuhn.util.ApplicationException;

public interface Beleg extends JVereinDBObject
{
  public String getBelegzaehler() throws RemoteException;

  public void setBelegzaehler(String belegzaehler) throws RemoteException;

  public String getBelegnummer() throws RemoteException;

  public void setBelegnummer(String belegnummer) throws RemoteException;

  public BelegArt getBelegArt() throws RemoteException;

  public void setBelegArt(BelegArt belegart) throws RemoteException;

  public BelegTyp getBelegTyp() throws RemoteException;

  public void setBelegTyp(BelegTyp belegtyp) throws RemoteException;

  public Beleggruppe getBelegGruppe() throws RemoteException;

  public void setBelegGruppe(Beleggruppe beleggruppe) throws RemoteException;

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

  public Timestamp getVersand() throws RemoteException;

  public void setVersand(Timestamp versand) throws RemoteException;

  /**
   * Prüft die Checksum der Datei
   * 
   * @throws IOException
   * @throws ApplicationException
   * @return true wenn korrekt, false wenn Falsch, null wenn nicht vorhanden
   */
  Boolean checkChecksum() throws IOException, ApplicationException;

  List<AbstractBelegReferenz> getReferenzes() throws RemoteException;

  public void erzeugeBelegnummer()
      throws NumberFormatException, RemoteException, ApplicationException;

  public void delete(boolean vonDbBereinigen, boolean forced)
      throws RemoteException, ApplicationException;
}
