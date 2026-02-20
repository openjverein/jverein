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

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.OBanToo.SEPA.BankenDaten.Bank;
import de.jost_net.OBanToo.SEPA.BankenDaten.Banken;

public class RechnungMap extends AbstractMap
{

  public RechnungMap()
  {
    super();
  }

  @SuppressWarnings("deprecation")
  public Map<String, Object> getMap(Rechnung re, Map<String, Object> inMap)
      throws RemoteException
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

    ArrayList<String> buchungDatum = new ArrayList<>();
    ArrayList<String> zweck = new ArrayList<>();
    ArrayList<String> nettobetrag = new ArrayList<>();
    ArrayList<String> steuersatz = new ArrayList<>();
    ArrayList<String> steuerbetrag = new ArrayList<>();
    ArrayList<String> betrag = new ArrayList<>();
    HashMap<Double, Double> steuerMap = new HashMap<>();
    HashMap<Double, Double> steuerBetragMap = new HashMap<>();

    DecimalFormat format = new DecimalFormat("0.##");
    double summe = 0;
    for (SollbuchungPosition sp : re.getSollbuchungPositionList())
    {
      buchungDatum.add(new JVDateFormatTTMMJJJJ().format(sp.getDatum()));
      zweck.add(sp.getZweck());
      nettobetrag.add(Einstellungen.DECIMALFORMAT.format(sp.getNettobetrag()));
      steuersatz.add("(" + format.format(sp.getSteuersatz()) + "%)");
      steuerbetrag
          .add(Einstellungen.DECIMALFORMAT.format(sp.getSteuerbetrag()));
      betrag.add(Einstellungen.DECIMALFORMAT.format(sp.getBetrag()));
      summe += sp.getBetrag();
      if (sp.getSteuersatz() > 0)
      {
        Double steuer = steuerMap.getOrDefault(sp.getSteuersatz(), 0d);
        steuerMap.put(sp.getSteuersatz(), steuer + sp.getSteuerbetrag());
        Double brutto = steuerBetragMap.getOrDefault(sp.getSteuersatz(), 0d);
        steuerBetragMap.put(sp.getSteuersatz(), brutto + sp.getBetrag());
      }
    }
    if (buchungDatum.size() > 1 || steuerMap.size() > 0)
    {
      zweck.add("");
      betrag.add("");
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERTPFLICHT))
    {
      for (Double satz : steuerMap.keySet())
      {
        zweck.add("inkl. " + satz + "% USt. von "
            + Einstellungen.DECIMALFORMAT.format(steuerBetragMap.get(satz)));
        betrag.add(Einstellungen.DECIMALFORMAT.format(steuerMap.get(satz)));
      }
    }
    if (buchungDatum.size() > 1)
    {
      zweck.add("Summe");
      betrag.add(Einstellungen.DECIMALFORMAT.format(summe));
    }
    map.put(RechnungVar.BUCHUNGSDATUM.getName(),
        String.join("\n", buchungDatum));
    map.put(RechnungVar.MK_BUCHUNGSDATUM.getName(),
        String.join("\n", buchungDatum));
    map.put(RechnungVar.ZAHLUNGSGRUND.getName(), String.join("\n", zweck));
    map.put(RechnungVar.MK_ZAHLUNGSGRUND.getName(), String.join("\n", zweck));
    map.put(RechnungVar.ZAHLUNGSGRUND1.getName(), String.join("\n", zweck));
    map.put(RechnungVar.ZAHLUNGSGRUND2.getName(), "");
    map.put(RechnungVar.NETTOBETRAG.getName(), String.join("\n", nettobetrag));
    map.put(RechnungVar.MK_NETTOBETRAG.getName(),
        String.join("\n", nettobetrag));
    map.put(RechnungVar.STEUERSATZ.getName(), String.join("\n", steuersatz));
    map.put(RechnungVar.MK_STEUERSATZ.getName(), String.join("\n", steuersatz));
    map.put(RechnungVar.STEUERBETRAG.getName(),
        String.join("\n", steuerbetrag));
    map.put(RechnungVar.MK_STEUERBETRAG.getName(),
        String.join("\n", steuerbetrag));
    map.put(RechnungVar.BETRAG.getName(), String.join("\n", betrag));
    map.put(RechnungVar.MK_BETRAG.getName(), String.join("\n", betrag));

    Double ist = re.getIstSumme();
    map.put(RechnungVar.SUMME.getName(), summe);
    map.put(RechnungVar.IST.getName(), ist);
    map.put(RechnungVar.MK_SUMME_OFFEN.getName(), summe - ist);
    map.put(RechnungVar.SUMME_OFFEN.getName(), summe - ist);
    map.put(RechnungVar.MK_STAND.getName(), ist - summe);
    map.put(RechnungVar.STAND.getName(), ist - summe);

    // Deise Felder gibt es nicht mehr in der Form, damit bei alten
    // Rechnungs-Formularen nicht der Variablennamen steht hier trotzdem
    // hinzufügen
    map.put(RechnungVar.DIFFERENZ.getName(), "");
    map.put(RechnungVar.MK_IST.getName(), "");

    map.put(RechnungVar.QRCODE_INTRO.getName(),
        (String) Einstellungen.getEinstellung(Property.QRCODEINTRO));

    map.put(RechnungVar.DATUM.getName(), re.getDatum());
    map.put(RechnungVar.DATUM_F.getName(), fromDate(re.getDatum()));
    map.put(RechnungVar.NUMMER.getName(), StringTool.lpad(re.getID(),
        (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE), "0"));

    map.put(RechnungVar.PERSONENART.getName(), re.getPersonenart());
    map.put(RechnungVar.GESCHLECHT.getName(), re.getGeschlecht());
    map.put(RechnungVar.ANREDE.getName(), re.getAnrede());
    map.put(RechnungVar.ANREDE_DU.getName(),
        Adressaufbereitung.getAnredeDu(re));
    map.put(RechnungVar.ANREDE_FOERMLICH.getName(),
        Adressaufbereitung.getAnredeFoermlich(re));
    map.put(RechnungVar.TITEL.getName(), re.getTitel());
    map.put(RechnungVar.NAME.getName(), re.getName());
    map.put(RechnungVar.VORNAME.getName(), re.getVorname());
    map.put(RechnungVar.STRASSE.getName(), re.getStrasse());
    map.put(RechnungVar.ADRESSIERUNGSZUSATZ.getName(),
        re.getAdressierungszusatz());
    map.put(RechnungVar.PLZ.getName(), re.getPlz());
    map.put(RechnungVar.ORT.getName(), re.getOrt());
    map.put(RechnungVar.STAAT.getName(), re.getStaat());
    map.put(RechnungVar.MANDATID.getName(), re.getMandatID());
    map.put(RechnungVar.MANDATDATUM.getName(), re.getMandatDatum());
    map.put(RechnungVar.MANDATDATUM_F.getName(), fromDate(re.getMandatDatum()));
    String bic = re.getBIC();
    map.put(RechnungVar.BIC.getName(), bic);
    if (bic != null)
    {
      Bank bank = Banken.getBankByBIC(bic);
      if (bank != null)
      {
        String name = bank.getBezeichnung();
        if (name != null)
        {
          map.put(RechnungVar.BANKNAME.getName(), name.trim());
        }
      }
    }
    else
    {
      map.put(RechnungVar.BANKNAME.getName(), null);
    }
    map.put(RechnungVar.IBAN.getName(),
        new IBANFormatter().format(re.getIBAN()));
    map.put(RechnungVar.IBANMASKIERT.getName(),
        VarTools.maskieren(re.getIBAN()));
    map.put(RechnungVar.EMPFAENGER.getName(),
        Adressaufbereitung.getAdressfeld(re));

    String zahlungsweg = "";
    switch (re.getZahlungsweg().getKey())
    {
      case Zahlungsweg.BASISLASTSCHRIFT:
      {
        zahlungsweg = (String) Einstellungen
            .getEinstellung(Property.RECHNUNGTEXTABBUCHUNG);
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{BIC\\}", re.getBIC());
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{IBAN\\}",
            new IBANFormatter().format(re.getIBAN()));
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{MANDATID\\}",
            re.getMandatID());
        break;
      }
      case Zahlungsweg.BARZAHLUNG:
      {
        zahlungsweg = (String) Einstellungen
            .getEinstellung(Property.RECHNUNGTEXTBAR);
        break;
      }
      case Zahlungsweg.ÜBERWEISUNG:
      {
        zahlungsweg = (String) Einstellungen
            .getEinstellung(Property.RECHNUNGTEXTUEBERWEISUNG);
        break;
      }
    }
    try
    {
      zahlungsweg = VelocityTool.eval(new AllgemeineMap().getMap(map),
          zahlungsweg);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    map.put(RechnungVar.ZAHLUNGSWEGTEXT.getName(), zahlungsweg);
    map.put(RechnungVar.KOMMENTAR.getName(), re.getKommentar());
    map.put(RechnungVar.RECHNUNGSTEXT.getName(), re.getRechnungstext());
    if (re.getReferenzrechnungID() != null)
    {
      map.put(RechnungVar.REFERENZRECHNUNG.getName(),
          StringTool.lpad(re.getReferenzrechnungID().toString(),
              (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE),
              "0"));
    }
    else
    {
      map.put(RechnungVar.REFERENZRECHNUNG.getName(), "");
    }
    map.put(RechnungVar.ERSTATTUNGSBETRAG.getName(),
        re.getErstattungsbetrag() != null
            ? Einstellungen.DECIMALFORMAT.format(re.getErstattungsbetrag())
            : "");

    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
      throws RemoteException
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

    map.put(RechnungVar.BUCHUNGSDATUM.getName(),
        new JVDateFormatTTMMJJJJ().format(new Date()) + "\n"
            + new JVDateFormatTTMMJJJJ().format(new Date()));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERTPFLICHT))
    {
      map.put(RechnungVar.ZAHLUNGSGRUND.getName(),
          "Mitgliedsbeitrag\nZusatzbetrag\n\ninkl. 19% USt. von 10,00\nSumme");
      map.put(RechnungVar.NETTOBETRAG.getName(), "8,40\n13,80");
      map.put(RechnungVar.STEUERSATZ.getName(), "(19%)\n(0%)");
      map.put(RechnungVar.STEUERBETRAG.getName(), "1,60\n0,00");
      map.put(RechnungVar.BETRAG.getName(), "10,00\n13,80\n\n1,60\n23,80");
    }
    else
    {
      map.put(RechnungVar.ZAHLUNGSGRUND.getName(),
          "Mitgliedsbeitrag\nZusatzbetrag\n\nSumme");
      map.put(RechnungVar.NETTOBETRAG.getName(), "10,00\n13,80");
      map.put(RechnungVar.STEUERSATZ.getName(), "(0%)\n(0%)");
      map.put(RechnungVar.STEUERBETRAG.getName(), "0,00\n0,00");
      map.put(RechnungVar.BETRAG.getName(), "10,00\n13,80\n\n23,80");
    }

    map.put(RechnungVar.SUMME.getName(), Double.valueOf("23.80"));
    map.put(RechnungVar.IST.getName(), Double.valueOf("10.00"));
    map.put(RechnungVar.STAND.getName(), Double.valueOf("-13.80"));
    map.put(RechnungVar.SUMME_OFFEN.getName(), Double.valueOf("13.80"));
    map.put(RechnungVar.QRCODE_INTRO.getName(),
        "Bequem bezahlen mit Girocode. Einfach mit der Banking-App auf dem Handy abscannen.");
    map.put(RechnungVar.DATUM.getName(), toDate("10.01.2025"));
    map.put(RechnungVar.DATUM_F.getName(), "20251001");
    map.put(RechnungVar.NUMMER.getName(), StringTool.lpad("11",
        (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE), "0"));
    map.put(RechnungVar.ANREDE.getName(), "Herr");
    map.put(RechnungVar.TITEL.getName(), "Dr. Dr.");
    map.put(RechnungVar.NAME.getName(), "Wichtig");
    map.put(RechnungVar.VORNAME.getName(), "Willi");
    map.put(RechnungVar.STRASSE.getName(), "Bahnhofstr. 22");
    map.put(RechnungVar.ADRESSIERUNGSZUSATZ.getName(), "Hinterhof bei Müller");
    map.put(RechnungVar.PLZ.getName(), "12345");
    map.put(RechnungVar.ORT.getName(), "Testenhausen");
    map.put(RechnungVar.STAAT.getName(), "Deutschland");
    map.put(RechnungVar.GESCHLECHT.getName(), GeschlechtInput.MAENNLICH);
    map.put(RechnungVar.ANREDE_DU.getName(), "Hallo Willi,");
    map.put(RechnungVar.ANREDE_FOERMLICH.getName(),
        "Sehr geehrter Herr Dr. Dr. Wichtig,");
    map.put(RechnungVar.PERSONENART.getName(), "n");
    map.put(RechnungVar.MANDATID.getName(), "12345");
    map.put(RechnungVar.MANDATDATUM.getName(), toDate("01.01.2024"));
    map.put(RechnungVar.MANDATDATUM_F.getName(), "20240101");
    map.put(RechnungVar.BIC.getName(), "XXXXXXXXXXX");
    map.put(RechnungVar.IBAN.getName(), "DE89 3704 0044 0532 0130 00");
    map.put(RechnungVar.IBANMASKIERT.getName(), "XXXXXXXXXXXXXXX3000");
    map.put(RechnungVar.BANKNAME.getName(), "XY Bank");
    map.put(RechnungVar.EMPFAENGER.getName(),
        "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland");
    map.put(RechnungVar.ZAHLUNGSWEGTEXT.getName(),
        "Bitte überweisen Sie den Betrag auf das angegebene Konto.");
    map.put(RechnungVar.KOMMENTAR.getName(), "Der Rechnungskommentar");
    map.put(RechnungVar.RECHNUNGSTEXT.getName(), "Der Rechnungstext");
    map.put(RechnungVar.REFERENZRECHNUNG.getName(), "00333");
    map.put(RechnungVar.ERSTATTUNGSBETRAG.getName(), "100,00");
    return map;
  }
}
