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
import java.util.List;

import de.willuhn.datasource.rmi.DBObject;

public interface Sollbuchung extends DBObject
{
  public static final String TABLE_NAME = "mitgliedskonto";

  public static final String TABLE_NAME_ID = "mitgliedskonto.id";

  public static final String PRIMARY_ATTRIBUTE = "mitglied";

  public static final String ABRECHNUNGSLAUF = "abrechnungslauf";

  public static final String T_ABRECHNUNGSLAUF = TABLE_NAME + "."
      + ABRECHNUNGSLAUF;

  public static final String MITGLIED = "mitglied";

  public static final String T_MITGLIED = TABLE_NAME + "." + MITGLIED;

  public static final String ZAHLER = "zahler";

  public static final String T_ZAHLER = TABLE_NAME + "." + ZAHLER;

  public static final String DATUM = "datum";

  public static final String T_DATUM = TABLE_NAME + "." + DATUM;

  public static final String ZWECK1 = "zweck1";

  public static final String T_ZWECK1 = TABLE_NAME + "." + ZWECK1;

  public static final String ZAHLUNGSWEG = "zahlungsweg";

  public static final String T_ZAHLUNGSWEG = TABLE_NAME + "." + ZAHLUNGSWEG;

  public static final String BETRAG = "betrag";

  public static final String T_BETRAG = TABLE_NAME + "." + BETRAG;

  public static final String ISTSUMME = "istsumme";

  public static final String T_ISTSUMME = TABLE_NAME + "." + ISTSUMME;

  public static final String RECHNUNG = "rechnung";

  public static final String T_RECHNUNG = TABLE_NAME + "." + RECHNUNG;


  public Abrechnungslauf getAbrechnungslauf() throws RemoteException;

  public void setAbrechnungslauf(Abrechnungslauf abrechnungslauf)
      throws RemoteException;

  public Mitglied getMitglied() throws RemoteException;

  public String getMitgliedId() throws RemoteException;

  public void setMitglied(Mitglied mitglied) throws RemoteException;

  public Mitglied getZahler() throws RemoteException;

  public void setZahler(Mitglied zahler) throws RemoteException;

  public Long getZahlerId() throws RemoteException;

  public void setZahlerId(Long zahlerId) throws RemoteException;

  public Date getDatum() throws RemoteException;

  public void setDatum(Date datum) throws RemoteException;

  public String getZweck1() throws RemoteException;

  public void setZweck1(String zweck1) throws RemoteException;

  public Integer getZahlungsweg() throws RemoteException;

  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException;

  public void setBetrag(Double betrag) throws RemoteException;

  public Double getBetrag() throws RemoteException;

  public Double getIstSumme() throws RemoteException;

  public Rechnung getRechnung() throws RemoteException;

  public void setRechnung(Rechnung rechnung) throws RemoteException;

  ArrayList<SollbuchungPosition> getSollbuchungPositionList()
      throws RemoteException;

  public List<Buchung> getBuchungList() throws RemoteException;

}
