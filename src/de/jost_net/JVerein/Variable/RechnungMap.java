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
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;

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

    ArrayList<Date> buchungDatum = new ArrayList<>();
    ArrayList<String> zweck = new ArrayList<>();
    ArrayList<Double> nettobetrag = new ArrayList<>();
    ArrayList<String> steuersatz = new ArrayList<>();
    ArrayList<Double> steuerbetrag = new ArrayList<>();
    ArrayList<Double> betrag = new ArrayList<>();
    HashMap<Double, Double> steuerMap = new HashMap<>();
    HashMap<Double, Double> steuerBetragMap = new HashMap<>();

    DecimalFormat format = new DecimalFormat("0.##");
    CurrencyFormatter formatter = new CurrencyFormatter("%", format);
    double summe = 0;
    for (SollbuchungPosition sp : re.getSollbuchungPositionList())
    {
      buchungDatum.add(sp.getDatum());
      zweck.add(sp.getZweck());
      nettobetrag.add(sp.getNettobetrag());
      steuersatz.add(
          "(" + formatter.format(sp.getSteuersatz()) + ")");
      steuerbetrag.add(sp.getSteuerbetrag());
      betrag.add(sp.getBetrag());
      summe += sp.getBetrag();

      Double steuer = steuerMap.getOrDefault(sp.getSteuersatz(), 0d);
      steuerMap.put(sp.getSteuersatz(), steuer + sp.getSteuerbetrag());
      Double brutto = steuerBetragMap.getOrDefault(sp.getSteuersatz(), 0d);
      steuerBetragMap.put(sp.getSteuersatz(), brutto + sp.getBetrag());
    }
    if (buchungDatum.size() > 1)
    {
      if (Einstellungen.getEinstellung().getOptiert())
      {
        for (Double satz : steuerMap.keySet())
        {
          zweck.add("inkl. " + satz + "% USt.  von "
              + Einstellungen.DECIMALFORMAT.format(steuerBetragMap.get(satz)));
          betrag.add(+steuerMap.get(satz));
        }
      }
      zweck.add("Summe");
      betrag.add(summe);
    }
    map.put(RechnungVar.BUCHUNGSDATUM.getName(), buchungDatum.toArray());
    map.put(RechnungVar.MK_BUCHUNGSDATUM.getName(), buchungDatum.toArray());
    map.put(RechnungVar.ZAHLUNGSGRUND.getName(), zweck.toArray());
    map.put(RechnungVar.MK_ZAHLUNGSGRUND.getName(), zweck.toArray());
    map.put(RechnungVar.ZAHLUNGSGRUND1.getName(), zweck.toArray());
    map.put(RechnungVar.ZAHLUNGSGRUND2.getName(), "");
    map.put(RechnungVar.NETTOBETRAG.getName(), nettobetrag.toArray());
    map.put(RechnungVar.MK_NETTOBETRAG.getName(), nettobetrag.toArray());
    map.put(RechnungVar.STEUERSATZ.getName(), steuersatz.toArray());
    map.put(RechnungVar.MK_STEUERSATZ.getName(), steuersatz.toArray());
    map.put(RechnungVar.STEUERBETRAG.getName(), steuerbetrag.toArray());
    map.put(RechnungVar.MK_STEUERBETRAG.getName(), steuerbetrag.toArray());
    map.put(RechnungVar.BETRAG.getName(), betrag.toArray());
    map.put(RechnungVar.MK_BETRAG.getName(), betrag.toArray());

    Double ist = 0d;
    if (re.getSollbuchung() != null)
    {
      ist = re.getSollbuchung().getIstSumme();
    }
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
        Einstellungen.getEinstellung().getQRCodeIntro());

    map.put(RechnungVar.DATUM.getName(), re.getDatum());
    map.put(RechnungVar.NUMMER.getName(), StringTool.lpad(re.getID(),
        Einstellungen.getEinstellung().getZaehlerLaenge(), "0"));

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
    map.put(RechnungVar.BIC.getName(), re.getBIC());
    map.put(RechnungVar.IBAN.getName(), re.getIBAN());
    map.put(RechnungVar.IBANMASKIERT.getName(),
        VarTools.maskieren(re.getIBAN()));
    map.put(RechnungVar.EMPFAENGER.getName(),
        Adressaufbereitung.getAdressfeld(re));
    
    String zahlungsweg = "";
    switch (re.getZahlungsweg().getKey())
    {
      case Zahlungsweg.BASISLASTSCHRIFT:
      {
        zahlungsweg = Einstellungen.getEinstellung().getRechnungTextAbbuchung();
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{BIC\\}", re.getBIC());
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{IBAN\\}", re.getIBAN());
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{MANDATID\\}",
            re.getMandatID());
        break;
      }
      case Zahlungsweg.BARZAHLUNG:
      {
        zahlungsweg = Einstellungen.getEinstellung().getRechnungTextBar();
        break;
      }
      case Zahlungsweg.ÜBERWEISUNG:
      {
        zahlungsweg = Einstellungen.getEinstellung()
            .getRechnungTextUeberweisung();
        break;
      }
    }
    try
    {
      zahlungsweg = VelocityTool.eval(map, zahlungsweg);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    map.put(RechnungVar.ZAHLUNGSWEGTEXT.getName(), zahlungsweg);
    map.put(RechnungVar.KOMMENTAR.getName(), re.getKommentar());

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
        new Date[] { new Date(), new Date() });
    if (Einstellungen.getEinstellung().getOptiert())
    {
      map.put(RechnungVar.ZAHLUNGSGRUND.getName(),
          new String[] { "Mitgliedsbeitrag", "Zusatzbetrag",
              "inkl. 19% USt. von 10.00", "Summe" });
      map.put(RechnungVar.NETTOBETRAG.getName(), new Double[] { 8.4d, 13.8d });
      map.put(RechnungVar.STEUERSATZ.getName(),
          new String[] { "(19%)", "(0%)" });
      map.put(RechnungVar.STEUERBETRAG.getName(), new Double[] { 1.6d, 0d });
      map.put(RechnungVar.BETRAG.getName(),
          new Double[] { 10d, 13.8d, 1.6d, 23.8d });
    }
    else
    {
      map.put(RechnungVar.ZAHLUNGSGRUND.getName(),
          new String[] { "Mitgliedsbeitrag", "Zusatzbetrag", "Summe" });
      map.put(RechnungVar.NETTOBETRAG.getName(), new Double[] { 10d, 13.8d });
      map.put(RechnungVar.STEUERSATZ.getName(),
          new String[] { "(0%)", "(0%)" });
      map.put(RechnungVar.STEUERBETRAG.getName(), new Double[] { 0d, 0d });
      map.put(RechnungVar.BETRAG.getName(),
          new Double[] { 10d, 13.8d, 23.8d });
    }
    
    map.put(RechnungVar.SUMME.getName(), Double.valueOf("23.80"));
    map.put(RechnungVar.IST.getName(), Double.valueOf("10.00"));
    map.put(RechnungVar.STAND.getName(), Double.valueOf("-13.80"));
    map.put(RechnungVar.SUMME_OFFEN.getName(), Double.valueOf("13.80"));
    map.put(RechnungVar.QRCODE_INTRO.getName(),
        "Bequem bezahlen mit Girocode. Einfach mit der Banking-App auf dem Handy abscannen.");
    map.put(RechnungVar.DATUM.getName(), toDate("10.01.2025"));
    map.put(RechnungVar.NUMMER.getName(), StringTool.lpad("11",
        Einstellungen.getEinstellung().getZaehlerLaenge(), "0"));
    map.put(RechnungVar.ANREDE.getName(), "Herrn");
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
    map.put(RechnungVar.BIC.getName(), "XXXXXXXXXXX");
    map.put(RechnungVar.IBAN.getName(), "DE89370400440532013000");
    map.put(RechnungVar.IBANMASKIERT.getName(), "XXXXXXXXXXXXXXX3000");
    map.put(RechnungVar.EMPFAENGER.getName(),
        "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland");
    map.put(RechnungVar.ZAHLUNGSWEGTEXT.getName(),
        "Bitte überweisen Sie den Betrag auf das angegebene Konto.");
    map.put(RechnungVar.KOMMENTAR.getName(), "Der Rechnungskommentar");
    return map;
  }
}
