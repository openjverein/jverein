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
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
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

    map.put(GutschriftVar.ABRECHNUNGSLAUF_NR.getName(), abrl.getID());
    map.put(GutschriftVar.ABRECHNUNGSLAUF_DATUM.getName(),
        new JVDateFormatTTMMJJJJ().format(abrl.getDatum()));
    // Damit Pre-Notifications für mit Versionen bis 2.8.18 erstellte
    // Abrechnungsläufe
    // korrekt erstellt werden, werden beide Felder verwendet.
    if (ls.getMandatSequence().equals("FRST"))
    {
      map.put(GutschriftVar.ABRECHNUNGSLAUF_FAELLIGKEIT.getName(),
          new JVDateFormatTTMMJJJJ().format(abrl.getFaelligkeit()));
    }
    else
    {
      Date d = (Date) abrl.getAttribute("faelligkeit2");
      if (d == null)
      {
        d = Einstellungen.NODATE;
      }
      map.put(GutschriftVar.ABRECHNUNGSLAUF_FAELLIGKEIT.getName(),
          new JVDateFormatTTMMJJJJ().format(d));
    }
    map.put(GutschriftVar.PERSONENART.getName(), ls.getPersonenart());
    map.put(GutschriftVar.GESCHLECHT.getName(), ls.getGeschlecht());
    map.put(GutschriftVar.ANREDE.getName(), ls.getAnrede());
    map.put(GutschriftVar.ANREDE_DU.getName(),
        Adressaufbereitung.getAnredeDu(ls));
    map.put(GutschriftVar.ANREDE_FOERMLICH.getName(),
        Adressaufbereitung.getAnredeFoermlich(ls));
    map.put(GutschriftVar.TITEL.getName(), ls.getTitel());
    map.put(GutschriftVar.NAME.getName(), ls.getName());
    map.put(GutschriftVar.VORNAME.getName(), ls.getVorname());
    map.put(GutschriftVar.STRASSE.getName(), ls.getStrasse());
    map.put(GutschriftVar.ADRESSSIERUNGSZUSATZ.getName(),
        ls.getAdressierungszusatz());
    map.put(GutschriftVar.PLZ.getName(), ls.getPlz());
    map.put(GutschriftVar.ORT.getName(), ls.getOrt());
    map.put(GutschriftVar.STAAT.getName(), ls.getStaat());
    map.put(GutschriftVar.EMAIL.getName(), ls.getEmail());
    map.put(GutschriftVar.MANDATID.getName(), ls.getMandatID());
    map.put(GutschriftVar.MANDATDATUM.getName(),
        new JVDateFormatTTMMJJJJ().format(ls.getMandatDatum()));
    map.put(GutschriftVar.BIC.getName(), ls.getBIC());
    map.put(GutschriftVar.IBAN.getName(),
        new IBANFormatter().format(ls.getIBAN()));
    if (ls.getBIC() != null)
    {
      Bank bank = Banken.getBankByBIC(ls.getBIC());
      if (bank != null)
      {
        String name = bank.getBezeichnung();
        if (name != null)
        {
          map.put(GutschriftVar.BANKNAME.getName(), name.trim());
        }
      }
    }
    else
    {
      map.put(GutschriftVar.BANKNAME.getName(), null);
    }
    map.put(GutschriftVar.IBANMASKIERT.getName(),
        VarTools.maskieren(ls.getIBAN()));
    map.put(GutschriftVar.VERWENDUNGSZWECK.getName(), ls.getVerwendungszweck());
    map.put(GutschriftVar.BETRAG.getName(),
        ls.getBetrag() != null
            ? Einstellungen.DECIMALFORMAT.format(ls.getBetrag())
            : "");

    map.put(GutschriftVar.EMPFAENGER.getName(),
        Adressaufbereitung.getAdressfeld(ls));

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

    map.put(GutschriftVar.ABRECHNUNGSLAUF_NR.getName(), "99");
    map.put(GutschriftVar.ABRECHNUNGSLAUF_DATUM.getName(), "01.01.2025");
    map.put(GutschriftVar.ABRECHNUNGSLAUF_FAELLIGKEIT.getName(), "10.01.2025");
    map.put(GutschriftVar.ANREDE_DU.getName(), "Hallo Willi,");
    map.put(GutschriftVar.ANREDE_FOERMLICH.getName(),
        "Sehr geehrter Herr Dr. Dr. Wichtig,");
    map.put(GutschriftVar.PERSONENART.getName(), "n");
    map.put(GutschriftVar.GESCHLECHT.getName(), GeschlechtInput.MAENNLICH);
    map.put(GutschriftVar.ANREDE.getName(), "Herrn");
    map.put(GutschriftVar.TITEL.getName(), "Dr. Dr.");
    map.put(GutschriftVar.NAME.getName(), "Wichtig");
    map.put(GutschriftVar.VORNAME.getName(), "Willi");
    map.put(GutschriftVar.STRASSE.getName(), "Bahnhofstr. 22");
    map.put(GutschriftVar.ADRESSSIERUNGSZUSATZ.getName(),
        "Hinterhof bei Müller");
    map.put(GutschriftVar.PLZ.getName(), "12345");
    map.put(GutschriftVar.ORT.getName(), "Testenhausen");
    map.put(GutschriftVar.STAAT.getName(), "Deutschland");
    map.put(GutschriftVar.EMAIL.getName(), "willi.wichtig@email.de");
    map.put(GutschriftVar.MANDATID.getName(), "12345");
    map.put(GutschriftVar.MANDATDATUM.getName(), "01.01.2024");
    map.put(GutschriftVar.BIC.getName(), "XXXXXXXXXXX");
    map.put(GutschriftVar.IBAN.getName(), "DE89 3704 0044 0532 0130 00");
    map.put(GutschriftVar.BANKNAME.getName(), "XY Bank");
    map.put(GutschriftVar.IBANMASKIERT.getName(), "XXXXXXXXXXXXXXX3000");
    map.put(GutschriftVar.VERWENDUNGSZWECK.getName(), "Zweck");
    map.put(GutschriftVar.BETRAG.getName(), "23,80");
    map.put(GutschriftVar.EMPFAENGER.getName(),
        "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland");
    return map;
  }
}
