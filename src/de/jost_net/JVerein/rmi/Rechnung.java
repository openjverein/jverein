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
import java.util.ArrayList;
import java.util.Date;

import de.jost_net.JVerein.io.IAdresse;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.server.IGutschriftProvider;
import de.willuhn.util.ApplicationException;

public interface Rechnung
    extends JVereinDBObject, IAdresse, IGutschriftProvider
{
  public Mitglied getMitglied() throws RemoteException;

  public void setMitglied(int mitglied) throws RemoteException;

  public void setFormular(Formular formular) throws RemoteException;

  /**
   * Füllt alle Daten aus der Sollbuchung in die Rechnung. Das Datum und das
   * Formular müssen selbst hinzugefügt werden
   * 
   * @param mk
   *          die Sollbuchung aus der eine Rechnung erstellt werden soll
   */
  public void fill(Sollbuchung sollb)
      throws RemoteException, ApplicationException;

  Double getBetrag() throws RemoteException;

  void setBetrag(double betrag) throws RemoteException;

  public void setDatum(Date date) throws RemoteException;

  public Date getDatum() throws RemoteException;

  public Formular getFormular() throws RemoteException;

  public void setAnrede(String anrede) throws RemoteException;

  @Override
  public String getTitel() throws RemoteException;

  public void setTitel(String titel) throws RemoteException;

  @Override
  public String getName() throws RemoteException;

  public void setName(String name) throws RemoteException;

  @Override
  public String getVorname() throws RemoteException;

  public void setVorname(String vorname) throws RemoteException;

  @Override
  public String getStrasse() throws RemoteException;

  public void setStrasse(String strasse) throws RemoteException;

  @Override
  public String getAdressierungszusatz() throws RemoteException;

  public void setAdressierungszusatz(String adressierungszusatz)
      throws RemoteException;

  @Override
  public String getPlz() throws RemoteException;

  public void setPlz(String plz) throws RemoteException;

  @Override
  public String getOrt() throws RemoteException;

  @Override
  public String getStaat() throws RemoteException;

  public void setOrt(String ort) throws RemoteException;

  public void setStaat(String staat) throws RemoteException;

  @Override
  public String getGeschlecht() throws RemoteException;

  public void setGeschlecht(String geschlecht) throws RemoteException;

  @Override
  public String getAnrede() throws RemoteException;

  void setPersonenart(String personenart) throws RemoteException;

  public String getMandatID() throws RemoteException;

  public void setMandatID(String id) throws RemoteException;

  public Date getMandatDatum() throws RemoteException;

  public void setMandatDatum(Date datum) throws RemoteException;

  public String getBIC() throws RemoteException;

  public void setBIC(String bic) throws RemoteException;

  public String getIBAN() throws RemoteException;

  public void setIBAN(String iban) throws RemoteException;

  public Double getIstSumme() throws RemoteException;

  public Zahlungsweg getZahlungsweg() throws RemoteException;

  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException;

  public ArrayList<SollbuchungPosition> getSollbuchungPositionList()
      throws RemoteException;

  public ArrayList<Sollbuchung> getSollbuchungList() throws RemoteException;

  public String getLeitwegID() throws RemoteException;

  public void setLeitwegID(String leitwegid) throws RemoteException;

  public void setKommentar(String value) throws RemoteException;

  public String getKommentar() throws RemoteException;

  public Date getVersanddatum() throws RemoteException;

  public void setVersanddatum(Date datum) throws RemoteException;

  public Mitglied getZahler() throws RemoteException;

  void setZahler(Mitglied zahler) throws RemoteException;
}
