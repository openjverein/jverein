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
import java.util.List;

public interface Spendenbescheinigung extends JVereinDBObject
{
  public int getSpendenart() throws RemoteException;

  public void setSpendenart(int spendenart) throws RemoteException;

  public String getZeile1() throws RemoteException;

  public void setZeile1(String zeile) throws RemoteException;

  public String getZeile2() throws RemoteException;

  public void setZeile2(String zeile) throws RemoteException;

  public String getZeile3() throws RemoteException;

  public void setZeile3(String zeile) throws RemoteException;

  public String getZeile4() throws RemoteException;

  public void setZeile4(String zeile) throws RemoteException;

  public String getZeile5() throws RemoteException;

  public void setZeile5(String zeile) throws RemoteException;

  public String getZeile6() throws RemoteException;

  public void setZeile6(String zeile) throws RemoteException;

  public String getZeile7() throws RemoteException;

  public void setZeile7(String zeile) throws RemoteException;

  public Date getSpendedatum() throws RemoteException;

  public void setSpendedatum(Date datum) throws RemoteException;

  public Date getBescheinigungsdatum() throws RemoteException;

  public void setBescheinigungsdatum(Date datum) throws RemoteException;

  public Double getBetrag() throws RemoteException;

  public void setBetrag(Double betrag) throws RemoteException;

  public Formular getFormular() throws RemoteException;

  public void setFormular(Formular formular) throws RemoteException;

  public Boolean getErsatzAufwendungen() throws RemoteException;

  public void setErsatzAufwendungen(Boolean ersatzaufwendungen)
      throws RemoteException;

  public Mitglied getMitglied() throws RemoteException;

  public int getMitgliedID() throws RemoteException;

  public void setMitglied(Mitglied mitglied) throws RemoteException;

  public void setMitgliedID(Integer mitglied) throws RemoteException;

  /**
   * Liefert als Kennzeichen zurÃ¼ck, ob die Spendenbescheinigung eine
   * Sammelbestaetigung ist. Dies ist der Fall, wenn die Liste der Buchungen
   * mehr als eine Buchung enthÃ¤lt. Ist keine oder nur eine Buchung zugewiesen,
   * liegt eine EinzelbestÃ¤tigung vor.
   * 
   * @return Flag, ob SammelbestÃ¤tigung
   * @throws RemoteException
   */
  public boolean isSammelbestaetigung() throws RemoteException;

  /**
   * Liefert als Kennzeichen zurÃ¼ck, ob die Spendenbescheinigung eine echte
   * Geldspende ist. Dies ist der Fall, wenn es sich um eine Gelspende handelt
   * bei der bei keiner Buchung das Flag Erstattungsverzicht gesetzt ist.
   * 
   * @return Flag, ob echte Geldspende
   * @throws RemoteException
   */
  public boolean isEchteGeldspende() throws RemoteException;

  /**
   * FÃ¼gt der Liste der Buchungen eine Buchung hinzu. Der Gesamtbetrag der
   * Spendenbescheinigung wird anhand der EinzelbetrÃ¤ge der Buchungen
   * berechnet. Die Spendenart wird auf "GELDSPENDE" gesetzt.
   * 
   * @param buchung
   *          Die Buchung zum HinzufÃ¼gen
   */
  public void addBuchung(Buchung buchung) throws RemoteException;

  /**
   * HÃ¤ngt eine Buchung an die Spendenbescheinigung, wenn es eine
   * EinzelbestÃ¤tigung werden soll. Sollten vorher schon Buchungen eingetragen
   * worden sein, wird die Liste der Buchungen vorher gelÃ¶scht.
   * 
   * @param buchung
   *          Die Buchung, die der Spendenbescheinigung zugeordnet wird
   */
  public void setBuchung(Buchung buchung) throws RemoteException;

  /**
   * Liefert die Liste der Buchungen einer Spendenbescheinigung zurÃ¼ck. Falls
   * die Liste noch nicht angelegt wurde, wird sie aus der Datenbank
   * nachgeladen. Sollten der Spendenbescheinigung noch keine Buchungen
   * zugeordnet sein, wird eine leere Liste zurÃ¼ckgegeben.<br>
   * Beim Laden der Buchungen wird der Gesamtbetrag berechnet
   * 
   * @return Liste der der Spendenbescheinigung zugeordneten Buchungen
   */
  public List<Buchung> getBuchungen() throws RemoteException;

  public String getBezeichnungSachzuwendung() throws RemoteException;

  public void setBezeichnungSachzuwendung(String bezeichungsachzuwendung)
      throws RemoteException;

  public int getHerkunftSpende() throws RemoteException;

  public void setHerkunftSpende(int herkunftspende) throws RemoteException;

  public Boolean getUnterlagenWertermittlung() throws RemoteException;

  public void setUnterlagenWertermittlung(Boolean unterlagenwertermittlung)
      throws RemoteException;

  /**
   * Gibt an, ob die Spendenbescheinigung automaitsch oder manuell erstellt
   * wurde.
   * 
   * @return <code>true</code>, wenn die Spendenbescheinigung automatisch
   *         erzeugt wurde, ansonstenm <code>false</code>
   * @throws RemoteException
   */
  public Boolean getAutocreate() throws RemoteException;

  /**
   * Setzt ein Flag, wenn die Spendenbescheinigung automatisch erzeugt wurde.
   * 
   * @param autocreate
   *          <code>true</code>, wenn die Spendenbescheinigung automatisch
   *          erzeugt wurde, ansonsten <code>false</code>
   * @throws RemoteException
   */
  public void setAutocreate(Boolean autocreate) throws RemoteException;

  public Date getZeitraumBis() throws RemoteException;

}
