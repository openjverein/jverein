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

import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.io.ILastschrift;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.willuhn.util.ApplicationException;

public interface Mitglied extends JVereinDBObject, ILastschrift
{
  public enum namenformat
  {
    NAME_VORNAME,
    VORNAME_NAME,
    ADRESSE
  }

  public static final String TABLE_NAME = "mitglied";

  public static final String PRIMARY_ATTRIBUTE = "namevorname";

  public static final String MITGLIEDSTYP = "adresstyp";

  public void setExterneMitgliedsnummer(String extnr) throws RemoteException;

  public String getExterneMitgliedsnummer() throws RemoteException;

  public void setID(String id) throws RemoteException;

  public void setMitgliedstyp(Integer mitgliedstyp) throws RemoteException;

  public Mitgliedstyp getMitgliedstyp() throws RemoteException;

  public void setPersonenart(String personenart) throws RemoteException;

  public void setAnrede(String anrede) throws RemoteException;

  public void setTitel(String titel) throws RemoteException;

  public void setName(String name) throws RemoteException;

  public void setVorname(String vorname) throws RemoteException;

  public void setAdressierungszusatz(String adressierungszusatz)
      throws RemoteException;

  public void setStrasse(String strasse) throws RemoteException;

  public void setPlz(String plz) throws RemoteException;

  public void setOrt(String ort) throws RemoteException;

  public void setStaat(String staat) throws RemoteException;

  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException;

  public Zahlungsrhythmus getZahlungsrhythmus() throws RemoteException;

  public void setZahlungsrhythmus(Integer zahlungsrhythmus)
      throws RemoteException;

  public void setZahlungstermin(Integer zahlungstermin) throws RemoteException;

  public Zahlungstermin getZahlungstermin() throws RemoteException;

  public void setMandatDatum(Date mandatdatum) throws RemoteException;

  public Integer getMandatVersion() throws RemoteException;

  public void setMandatVersion(Integer mandatversion) throws RemoteException;

  public String getKtoiPersonenart() throws RemoteException;

  public void setKtoiPersonenart(String ktoipersonenart) throws RemoteException;

  public String getKtoiAnrede() throws RemoteException;

  public void setKtoiAnrede(String ktoianrede) throws RemoteException;

  public String getKtoiTitel() throws RemoteException;

  public void setKtoiTitel(String ktoititel) throws RemoteException;

  public String getKtoiName() throws RemoteException;

  public void setKtoiName(String ktoiname) throws RemoteException;

  public String getKtoiVorname() throws RemoteException;

  public void setKtoiVorname(String ktoivorname) throws RemoteException;

  public String getKtoiStrasse() throws RemoteException;

  public void setKtoiStrasse(String ktoiStrasse) throws RemoteException;

  public String getKtoiAdressierungszusatz() throws RemoteException;

  public void setKtoiAdressierungszusatz(String ktoiAdressierungszusatz)
      throws RemoteException;

  public String getKtoiPlz() throws RemoteException;

  public void setKtoiPlz(String ktoiPlz) throws RemoteException;

  public String getKtoiOrt() throws RemoteException;

  public void setKtoiOrt(String ktoiOrt) throws RemoteException;

  public String getKtoiStaat() throws RemoteException;

  public void setKtoiStaat(String ktoiStaat) throws RemoteException;

  public String getKtoiEmail() throws RemoteException;

  public void setKtoiEmail(String ktoiEmail) throws RemoteException;

  public String getKtoiGeschlecht() throws RemoteException;

  public void setKtoiGeschlecht(String ktoigeschlecht) throws RemoteException;

  public String getKontoinhaber(namenformat art)
      throws RemoteException;

  public Date getGeburtsdatum() throws RemoteException;

  public void setGeburtsdatum(Date geburtsdatum) throws RemoteException;

  public void setGeburtsdatum(String geburtsdatum) throws RemoteException;

  public Integer getAlter() throws RemoteException;

  @Override
  public String getGeschlecht() throws RemoteException;

  public void setGeschlecht(String geschlecht) throws RemoteException;

  public String getTelefonprivat() throws RemoteException;

  public void setTelefonprivat(String telefonprivat) throws RemoteException;

  public String getTelefondienstlich() throws RemoteException;

  public void setTelefondienstlich(String telefondienstlich)
      throws RemoteException;

  public String getHandy() throws RemoteException;

  public void setHandy(String handy) throws RemoteException;

  public String getEmail() throws RemoteException;

  public void setEmail(String email) throws RemoteException;

  public Date getEintritt() throws RemoteException;

  public void setEintritt(Date eintritt) throws RemoteException;

  public void setEintritt(String eintritt) throws RemoteException;

  public Beitragsgruppe getBeitragsgruppe() throws RemoteException;

  public int getBeitragsgruppeId() throws RemoteException;

  public void setBeitragsgruppe(Beitragsgruppe beitragsgruppe)
      throws RemoteException;

  public Double getIndividuellerBeitrag() throws RemoteException;

  public void setIndividuellerBeitrag(Double individuellerbeitrag)
      throws RemoteException;

  /**
   * Ist das Mitglied Teil in einem Familienverband, wird das voll zahlende
   * Mitglied zur�ck geliefert.
   */
  public Mitglied getVollZahler() throws RemoteException;

  public Long getVollZahlerID() throws RemoteException;

  public void setVollZahlerID(Long id) throws RemoteException;

  /**
   * Liefert das Mitglied welches den Beitrag f�r das Mitglied bezahlt. Es ist
   * normalerweise das Mitglied selbst. Ist das Mitglied Teil in einem
   * Familienverband und als Zahlungsweg "Vollzahler" konfiguriert, wird das
   * voll zahlende Mitglied zur�ckgeliefert.
   */
  public Mitglied getZahler() throws RemoteException;

  public Long getZahlerID() throws RemoteException;

  public Date getAustritt() throws RemoteException;

  public void setAustritt(Date austritt) throws RemoteException;

  public void setAustritt(String austritt) throws RemoteException;

  public Date getKuendigung() throws RemoteException;

  public void setKuendigung(Date kuendigung) throws RemoteException;

  public void setKuendigung(String kuendigung) throws RemoteException;

  public Date getSterbetag() throws RemoteException;

  public void setSterbetag(Date sterbetag) throws RemoteException;

  public void setSterbetag(String sterbetag) throws RemoteException;

  public String getVermerk1() throws RemoteException;

  public void setVermerk1(String vermerk1) throws RemoteException;

  public String getVermerk2() throws RemoteException;

  public void setVermerk2(String vermerk2) throws RemoteException;

  public void insert() throws RemoteException, ApplicationException;

  public void setEingabedatum() throws RemoteException;

  public Date getEingabedatum() throws RemoteException;

  public void setLetzteAenderung() throws RemoteException;

  public Date getLetzteAenderung() throws RemoteException;

  public Mitgliedfoto getFoto() throws RemoteException;

  public void setFoto(Mitgliedfoto foto) throws RemoteException;

  public boolean isAngemeldet(Date stichtag) throws RemoteException;

  public void addVariable(String name, String wert) throws RemoteException;

  public Map<String, String> getVariablen() throws RemoteException;

  public String getKtoiStaatCode() throws RemoteException;

  public String getLeitwegID() throws RemoteException;

  public void setLeitwegID(String leitwegid) throws RemoteException;

  public boolean checkSEPA() throws RemoteException, ApplicationException;

  public String getMandatID() throws RemoteException;

  public void setMandatID(String mandatid) throws RemoteException;

  public void setBeitragsgruppeId(Integer beitragsgruppe)
      throws RemoteException;
}
