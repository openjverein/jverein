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
package de.jost_net.JVerein.util;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;

public class SpbAdressaufbereitung
{

  public static void adressaufbereitung(Mitglied m, Spendenbescheinigung spb)
      throws RemoteException
  {
    spb.setMitglied(m);
    ArrayList<String> adresse = new ArrayList<>();
    adresse.add(m.getAnrede());
    adresse.add(Adressaufbereitung.getVornameName(m));
    if (m.getAdressierungszusatz() != null
        && m.getAdressierungszusatz().length() > 0)
    {
      adresse.add(m.getAdressierungszusatz());
    }
    adresse.add(m.getStrasse());
    adresse.add(m.getPlz() + " " + m.getOrt());
    adresse.add(m.getStaat());

    // Alle Zeilen erstmal leer füllen, dait nicht null in der DB steht
    spb.setZeile1("");
    spb.setZeile2("");
    spb.setZeile3("");
    spb.setZeile4("");
    spb.setZeile5("");
    spb.setZeile6("");
    spb.setZeile7("");
    switch (adresse.size())
    {
      case 7:
        spb.setZeile7(adresse.get(6));
      case 6:
        spb.setZeile6(adresse.get(5));
      case 5:
        spb.setZeile5(adresse.get(4));
      case 4:
        spb.setZeile4(adresse.get(3));
      case 3:
        spb.setZeile3(adresse.get(2));
      case 2:
        spb.setZeile2(adresse.get(1));
      case 1:
        spb.setZeile1(adresse.get(0));
    }

  }

}
