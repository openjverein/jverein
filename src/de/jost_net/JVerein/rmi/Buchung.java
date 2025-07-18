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
import java.util.Map;

import de.willuhn.util.ApplicationException;

public interface Buchung extends JVereinDBObject
{
  public static final String TABLE_NAME = "buchung";

  public static final String TABLE_NAME_ID = "buchung.id";

  public static final String PRIMARY_ATTRIBUTE = "id";

  public static final String SOLLBUCHUNG = "mitgliedskonto";

  public static final String T_SOLLBUCHUNG = TABLE_NAME + "." + SOLLBUCHUNG;


  public void setID(String id) throws RemoteException;

  public Integer getUmsatzid() throws RemoteException;

  public void setUmsatzid(Integer umsatzid) throws RemoteException;

  public Konto getKonto() throws RemoteException;

  public void setKonto(Konto konto) throws RemoteException;

  public Integer getAuszugsnummer() throws RemoteException;

  public void setAuszugsnummer(Integer auszugsnummer) throws RemoteException;

  public Integer getBlattnummer() throws RemoteException;

  public void setBlattnummer(Integer blattnummer) throws RemoteException;

  public String getName() throws RemoteException;

  public void setName(String name) throws RemoteException;

  public String getIban() throws RemoteException;

  public void setIban(String iban) throws RemoteException;

  public boolean isBetragNull() throws RemoteException;
  
  public void setBetragNull() throws RemoteException;
  
  public double getBetrag() throws RemoteException;

  public void setBetrag(double betrag) throws RemoteException;

  public String getZweck() throws RemoteException;

  public void setZweck(String zweck) throws RemoteException;

  public Date getDatum() throws RemoteException;

  public void setDatum(Date datum) throws RemoteException;

  public String getArt() throws RemoteException;

  public void setArt(String art) throws RemoteException;

  public String getKommentar() throws RemoteException;

  public void setKommentar(String kommentar) throws RemoteException;

  public Buchungsart getBuchungsart() throws RemoteException;

  public Long getBuchungsartId() throws RemoteException;

  public void setBuchungsartId(Long buchungsart) throws RemoteException;
  
  public Buchungsklasse getBuchungsklasse() throws RemoteException;
  
  public Long getBuchungsklasseId() throws RemoteException;

  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException;

  public Jahresabschluss getAbschluss() throws RemoteException;

  public Long getAbschlussId() throws RemoteException;

  public void setAbschluss(Jahresabschluss abschluss)
      throws RemoteException;

  public void setAbschlussId(Long abschlussId) throws RemoteException;
  
  public Abrechnungslauf getAbrechnungslauf() throws RemoteException;

  public Long getAbrechnungslaufID() throws RemoteException;

  public void setAbrechnungslauf(Abrechnungslauf abrechnungslauf)
      throws RemoteException;

  public void setAbrechnungslauf(Long abrechnungslauf) throws RemoteException;

  public Sollbuchung getSollbuchung() throws RemoteException;

  public Long getSollbuchungID() throws RemoteException;

  public void setSollbuchung(Sollbuchung sollbuchung)
      throws RemoteException;

  public void setSollbuchungID(Long sollbuchungID) throws RemoteException;

  public Projekt getProjekt() throws RemoteException;

  public Long getProjektID() throws RemoteException;

  public void setProjekt(Projekt projekt) throws RemoteException;

  public void setProjektID(Long projekt) throws RemoteException;

  public Jahresabschluss getJahresabschluss() throws RemoteException;

  public Long getSplitId() throws RemoteException;

  public void setSplitId(Long splitid) throws RemoteException;

  public Integer getSplitTyp() throws RemoteException;

  public void setSplitTyp(Integer splittyp) throws RemoteException;

  public Spendenbescheinigung getSpendenbescheinigung() throws RemoteException;

  public void setSpendenbescheinigungId(Long spendenbescheinigung)
      throws RemoteException;

  public Map<String, Object> getMap(Map<String, Object> map)
      throws RemoteException;

  public Boolean getVerzicht() throws RemoteException;

  public void setVerzicht(Boolean verzicht) throws RemoteException;

  /**
   * Soll der Datensatz in die Datenbank geschrieben werden?<br>
   * 
   * @param speicherung
   *          true: ja, Normalfall <br>
   *          false: nein, bei Splitbuchungen werden die Datens�tze zun�chst in
   *          einer ArrayList gehalten und sp�ter in die Datenbank geschrieben.
   */
  public void setSpeicherung(boolean speicherung) throws RemoteException;

  public boolean getSpeicherung() throws RemoteException;

  public void setDelete(boolean delete) throws RemoteException;

  public boolean isToDelete() throws RemoteException;

  public void plausi() throws RemoteException, ApplicationException;
  
  public void store(boolean check) throws RemoteException, ApplicationException;

  public Boolean getGeprueft() throws RemoteException;

  public void setGeprueft(Boolean geprueft) throws RemoteException;

  public Double getNetto() throws RemoteException;

  public Steuer getSteuer() throws RemoteException;

  public void setSteuer(Steuer steuer) throws RemoteException;

  public void setSteuerId(Long id) throws RemoteException;

}
