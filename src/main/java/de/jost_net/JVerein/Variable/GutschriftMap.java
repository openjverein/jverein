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
package de.jost_net.JVerein.Variable;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.OBanToo.SEPA.BankenDaten.Bank;
import de.jost_net.OBanToo.SEPA.BankenDaten.Banken;

public class GutschriftMap extends AbstractMap
{

  public GutschriftMap()
  {
    super();
  }

  public Map<String, Object> getMap(Lastschrift ls, Map<String, Object> inma)
      throws RemoteException
  {
    Map<String, Object> map = null;
    if (inma == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inma;
    }

    if (ls == null)
    {
      return getDummyMap(map);
    }

    Abrechnungslauf abrl = ls.getAbrechnungslauf();

    for (GutschriftVar var : GutschriftVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ABRECHNUNGSLAUF_NR:
          value = abrl.getID();
          break;
        case ABRECHNUNGSLAUF_DATUM:
          value = Datum.formatDate(abrl.getDatum());
          break;
        case ABRECHNUNGSLAUF_FAELLIGKEIT:
          // Damit Pre-Notifications für mit Versionen bis 2.8.18 erstellte
          // Abrechnungsläufe korrekt erstellt werden, werden beide Felder
          // verwendet.
          if (ls.getMandatSequence().equals("FRST"))
          {
            value = Datum.formatDate(abrl.getFaelligkeit());
          }
          else
          {
            Date d = (Date) abrl.getAttribute("faelligkeit2");
            if (d == null)
            {
              d = Einstellungen.NODATE;
            }
            value = Datum.formatDate(d);
          }
          break;
        case PERSONENART:
          value = ls.getPersonenart();
          break;
        case GESCHLECHT:
          value = ls.getGeschlecht();
          break;
        case ANREDE:
          value = ls.getAnrede();
          break;
        case ANREDE_DU:
          value = Adressaufbereitung.getAnredeDu(ls);
          break;
        case ANREDE_FOERMLICH:
          value = Adressaufbereitung.getAnredeFoermlich(ls);
          break;
        case TITEL:
          value = ls.getTitel();
          break;
        case NAME:
          value = ls.getName();
          break;
        case VORNAME:
          value = ls.getVorname();
          break;
        case STRASSE:
          value = ls.getStrasse();
          break;
        case ADRESSSIERUNGSZUSATZ:
          value = ls.getAdressierungszusatz();
          break;
        case PLZ:
          value = ls.getPlz();
          break;
        case ORT:
          value = ls.getOrt();
          break;
        case STAAT:
          value = ls.getStaat();
          break;
        case EMAIL:
          value = ls.getEmail();
          break;
        case MANDATID:
          value = ls.getMandatID();
          break;
        case MANDATDATUM:
          value = Datum.formatDate(ls.getMandatDatum());
          break;
        case BIC:
          value = ls.getBic();
          break;
        case IBAN:
          value = new IBANFormatter().format(ls.getIban());
          break;
        case BANKNAME:
          if (ls.getBic() != null)
          {
            Bank bank = Banken.getBankByBIC(ls.getBic());
            if (bank != null)
            {
              String name = bank.getBezeichnung();
              if (name != null)
              {
                value = name.trim();
              }
            }
          }
          break;
        case IBANMASKIERT:
          value = ibanMaskieren(ls.getIban());
          break;
        case VERWENDUNGSZWECK:
          value = ls.getVerwendungszweck();
          break;
        case BETRAG:
          value = ls.getBetrag() != null
              ? Einstellungen.DECIMALFORMAT.format(ls.getBetrag())
              : "";
          break;
        case EMPFAENGER:
          value = Adressaufbereitung.getAdressfeld(ls);
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
  {
    Map<String, Object> map = null;
    if (inMap == null)
    {
      map = new HashMap<>();
    }
    else
    {
      map = inMap;
    }

    for (LastschriftVar var : LastschriftVar.values())
    {
      Object value = null;
      switch (var)
      {
        case ABRECHNUNGSLAUF_NR:
          value = "99";
          break;
        case ABRECHNUNGSLAUF_DATUM:
          value = "01.01.2025";
          break;
        case ABRECHNUNGSLAUF_FAELLIGKEIT:
          value = "10.01.2025";
          break;
        case ANREDE_DU:
          value = "Hallo Willi,";
          break;
        case ANREDE_FOERMLICH:
          value = "Sehr geehrter Herr Dr. Dr. Wichtig,";
          break;
        case PERSONENART:
          value = "n";
          break;
        case GESCHLECHT:
          value = GeschlechtInput.MAENNLICH;
          break;
        case ANREDE:
          value = "Herr";
          break;
        case TITEL:
          value = "Dr. Dr.";
          break;
        case NAME:
          value = "Wichtig";
          break;
        case VORNAME:
          value = "Willi";
          break;
        case STRASSE:
          value = "Bahnhofstr. 22";
          break;
        case ADRESSSIERUNGSZUSATZ:
          value = "Hinterhof bei Müller";
          break;
        case PLZ:
          value = "12345";
          break;
        case ORT:
          value = "Testenhausen";
          break;
        case STAAT:
          value = "Deutschland";
          break;
        case EMAIL:
          value = "willi.wichtig@email.de";
          break;
        case MANDATID:
          value = "12345";
          break;
        case MANDATDATUM:
          value = "01.01.2024";
          break;
        case BIC:
          value = "XXXXXXXXXXX";
          break;
        case IBAN:
          value = "DE89 3704 0044 0532 0130 00";
          break;
        case BANKNAME:
          value = "XY Bank";
          break;
        case IBANMASKIERT:
          value = "XXXXXXXXXXXXXXX3000";
          break;
        case VERWENDUNGSZWECK:
          value = "Zweck";
          break;
        case BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(23.8);
          break;
        case EMPFAENGER:
          value = "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland";
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
