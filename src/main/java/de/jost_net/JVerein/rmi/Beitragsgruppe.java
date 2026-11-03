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

import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.willuhn.datasource.rmi.DBIterator;

public interface Beitragsgruppe extends JVereinDBObject
{
  public String getBezeichnung() throws RemoteException;

  public void setBezeichnung(String bezeichnung) throws RemoteException;

  public Boolean getSekundaer() throws RemoteException;

  public void setSekundaer(Boolean sekundaer) throws RemoteException;

  public BigDecimal getBetrag() throws RemoteException;

  public void setBetrag(BigDecimal betrag) throws RemoteException;

  public BigDecimal getBetragMonatlich() throws RemoteException;

  public void setBetragMonatlich(BigDecimal betrag) throws RemoteException;

  public BigDecimal getBetragVierteljaehrlich() throws RemoteException;

  public void setBetragVierteljaehrlich(BigDecimal betrag)
      throws RemoteException;

  public BigDecimal getBetragHalbjaehrlich() throws RemoteException;

  public void setBetragHalbjaehrlich(BigDecimal betrag) throws RemoteException;

  public BigDecimal getBetragJaehrlich() throws RemoteException;

  public void setBetragJaehrlich(BigDecimal betrag) throws RemoteException;

  public ArtBeitragsart getBeitragsArt() throws RemoteException;

  public void setBeitragsArt(int art) throws RemoteException;

  public BigDecimal getArbeitseinsatzStunden() throws RemoteException;

  public void setArbeitseinsatzStunden(BigDecimal arbeitseinsatzStunden)
      throws RemoteException;

  public BigDecimal getArbeitseinsatzBetrag() throws RemoteException;

  public void setArbeitseinsatzBetrag(BigDecimal arbeitseinsatzBetrag)
      throws RemoteException;

  public Buchungsart getBuchungsart() throws RemoteException;

  public Long getBuchungsartId() throws RemoteException;

  public void setBuchungsart(Buchungsart buchungsart) throws RemoteException;

  public Buchungsklasse getBuchungsklasse() throws RemoteException;

  public Long getBuchungsklasseId() throws RemoteException;

  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException;

  public String getNotiz() throws RemoteException;

  public void setNotiz(String notiz) throws RemoteException;

  public boolean getHasAltersstaffel() throws RemoteException;

  public void setHasAltersstaffel(boolean b) throws RemoteException;

  public DBIterator<Altersstaffel> getAltersstaffelIterator()
      throws RemoteException;

  public Altersstaffel getAltersstaffel(int nummer) throws RemoteException;

  public Steuer getSteuer() throws RemoteException;

  void setSteuer(Steuer steuer) throws RemoteException;
}
