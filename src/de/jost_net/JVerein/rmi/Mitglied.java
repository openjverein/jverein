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

import de.jost_net.JVerein.io.ILastschrift;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.util.ApplicationException;

public interface Mitglied extends JVereinDBObject, ILastschrift
{
  public enum namenformat
  {
    KONTOINHABER,
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

  public void setMitgliedstyp(Long mitgliedstyp) throws RemoteException;

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

  public String getKontoinhaber(namenformat art) throws RemoteException;

  public String getKontoinhaber() throws RemoteException;

  public void setKontoinhaber(String kontoinhaber) throws RemoteException;

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
   * Mitglied zurück geliefert.
   */
  public Mitglied getVollZahler() throws RemoteException;

  public Long getVollZahlerID() throws RemoteException;

  public void setVollZahlerID(Long id) throws RemoteException;

  /**
   * Zahlt das Mitglied nicht selbst ist hier der alternative Beitragszahler
   * konfiguriert.
   */
  public Mitglied getAlternativerZahler() throws RemoteException;

  public Long getAlternativerZahlerID() throws RemoteException;

  public void setAlternativerZahlerID(Long id) throws RemoteException;

  /**
   * Liefert das Mitglied/Nicht-Mitglied welches den Beitrag für das Mitglied
   * bezahlt. Es ist normalerweise das Mitglied selbst. Ist ein alternativer
   * Beitragszahler konfiguriert, wird dieser zurückgeliefert.
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

  public String getLeitwegID() throws RemoteException;

  public void setLeitwegID(String leitwegid) throws RemoteException;

  public boolean checkSEPA() throws RemoteException, ApplicationException;

  @Override
  public String getMandatID() throws RemoteException;

  public void setMandatID(String mandatid) throws RemoteException;

  public void setBeitragsgruppeId(Integer beitragsgruppe)
      throws RemoteException;

  public void checkEigenschaften() throws RemoteException, ApplicationException;

  public void checkEigenschaften(TreePart eigenschaftenTree)
      throws RemoteException, ApplicationException;

  // Nur für die Migration aus Versionen vor 4.0.0
  public String getKtoiPersonenart() throws RemoteException;

  public String getKtoiAnrede() throws RemoteException;

  public String getKtoiTitel() throws RemoteException;

  public String getKtoiName() throws RemoteException;

  public String getKtoiVorname() throws RemoteException;

  public String getKtoiStrasse() throws RemoteException;

  public String getKtoiAdressierungszusatz() throws RemoteException;

  public String getKtoiPlz() throws RemoteException;

  public String getKtoiOrt() throws RemoteException;

  public String getKtoiStaat() throws RemoteException;

  public String getKtoiStaatCode() throws RemoteException;

  public String getKtoiEmail() throws RemoteException;

  public String getKtoiGeschlecht() throws RemoteException;
}
