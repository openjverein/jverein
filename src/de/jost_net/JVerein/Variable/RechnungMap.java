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

    Double ist = re.getIstSumme();
    for (RechnungVar var : RechnungVar.values())
    {
      Object value = null;
      switch (var)
      {
        case BUCHUNGSDATUM:
        case MK_BUCHUNGSDATUM:
          value = String.join("\n", buchungDatum);
          break;
        case ZAHLUNGSGRUND:
        case MK_ZAHLUNGSGRUND:
        case ZAHLUNGSGRUND1:
          value = String.join("\n", zweck);
          break;
        case ZAHLUNGSGRUND2:
          value = "";
          break;
        case NETTOBETRAG:
        case MK_NETTOBETRAG:
          value = String.join("\n", nettobetrag);
          break;
        case STEUERSATZ:
        case MK_STEUERSATZ:
          value = String.join("\n", steuersatz);
          break;
        case STEUERBETRAG:
        case MK_STEUERBETRAG:
          value = String.join("\n", steuerbetrag);
          break;
        case BETRAG:
        case MK_BETRAG:
          value = String.join("\n", betrag);
          break;
        case SUMME:
          value = Einstellungen.DECIMALFORMAT.format(summe);
          break;
        case IST:
          value = Einstellungen.DECIMALFORMAT.format(ist);
          break;
        case MK_SUMME_OFFEN:
        case SUMME_OFFEN:
          value = Einstellungen.DECIMALFORMAT.format(summe - ist);
          break;
        case MK_STAND:
        case STAND:
          value = Einstellungen.DECIMALFORMAT.format(ist - summe);

          // Deise Felder gibt es nicht mehr in der Form, damit bei alten
          // Rechnungs-Formularen nicht der Variablennamen steht hier trotzdem
          // hinzufügen
        case DIFFERENZ:
        case MK_IST:
          value = "";
          break;
        case QRCODE_INTRO:
          value = Einstellungen.getEinstellung(Property.QRCODEINTRO);
          break;
        case DATUM:
          value = formatDate(re.getDatum());
          break;
        case DATUM_F:
          value = fromDate(re.getDatum());
          break;
        case NUMMER:
          value = StringTool.lpad(re.getID(),
              (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE),
              "0");
          break;
        case PERSONENART:
          value = re.getPersonenart();
          break;
        case GESCHLECHT:
          value = re.getGeschlecht();
          break;
        case ANREDE:
          value = re.getAnrede();
          break;
        case ANREDE_DU:
          value = Adressaufbereitung.getAnredeDu(re);
          break;
        case ANREDE_FOERMLICH:
          value = Adressaufbereitung.getAnredeFoermlich(re);
          break;
        case TITEL:
          value = re.getTitel();
          break;
        case NAME:
          value = re.getName();
          break;
        case VORNAME:
          value = re.getVorname();
          break;
        case STRASSE:
          value = re.getStrasse();
          break;
        case ADRESSIERUNGSZUSATZ:
          value = re.getAdressierungszusatz();
          break;
        case PLZ:
          value = re.getPlz();
          break;
        case ORT:
          value = re.getOrt();
          break;
        case STAAT:
          value = re.getStaat();
          break;
        case MANDATID:
          value = re.getMandatID();
          break;
        case MANDATDATUM:
          value = formatDate(re.getMandatDatum());
          break;
        case MANDATDATUM_F:
          value = fromDate(re.getMandatDatum());
          break;
        case BIC:
          value = re.getBIC();
          break;
        case BANKNAME:
          String bic = re.getBIC();
          if (bic != null)
          {
            Bank bank = Banken.getBankByBIC(bic);
            if (bank != null)
            {
              String name = bank.getBezeichnung();
              if (name != null)
              {
                value = name.trim();
              }
            }
          }
        case IBAN:
          value = new IBANFormatter().format(re.getIBAN());
          break;
        case IBANMASKIERT:
          value = maskieren(re.getIBAN());
          break;
        case EMPFAENGER:
          value = Adressaufbereitung.getAdressfeld(re);
          break;
        case ZAHLUNGSWEG:
          value = re.getZahlungsweg().getKey();
          break;
        case ZAHLUNGSWEGTEXT:
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
            value = VelocityTool.eval(new AllgemeineMap().getMap(map),
                zahlungsweg);
          }
          catch (IOException e)
          {
            e.printStackTrace();
            value = zahlungsweg;
          }
          break;
        case KOMMENTAR:
          value = re.getKommentar();
          break;
        case QRCODE_SUMME:
          // Wird erst in FormularAufbereitung gesetzt
          break;
      }
      map.put(var.getName(), value);
    }
    return map;
  }

  @SuppressWarnings("deprecation")
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

    boolean optiert = (Boolean) Einstellungen
        .getEinstellung(Property.OPTIERTPFLICHT);

    for (RechnungVar var : RechnungVar.values())
    {
      Object value = null;
      switch (var)
      {
        case BUCHUNGSDATUM:
          value = formatDate(new Date()) + "\n" + formatDate(new Date());
        case ZAHLUNGSGRUND:
          if (optiert)
          {
            value = "Mitgliedsbeitrag\nZusatzbetrag\n\ninkl. 19% USt. von 10,00\nSumme";
          }
          else
          {
            value = "Mitgliedsbeitrag\nZusatzbetrag\n\nSumme";
          }
          break;
        case NETTOBETRAG:
          if (optiert)
          {
            value = Einstellungen.DECIMALFORMAT.format(8.4) + "\n"
                + Einstellungen.DECIMALFORMAT.format(13.8);
          }
          else
          {
            value = Einstellungen.DECIMALFORMAT.format(10d) + "\n"
                + Einstellungen.DECIMALFORMAT.format(13.8);
          }
          break;
        case STEUERSATZ:
          if (optiert)
          {
            value = "(19%)\n(0%)";
          }
          else
          {
            value = "(0%)\n(0%)";
          }
          break;
        case STEUERBETRAG:
          if (optiert)
          {
            value = Einstellungen.DECIMALFORMAT.format(1.6) + "\n"
                + Einstellungen.DECIMALFORMAT.format(0d);
          }
          else
          {
            value = Einstellungen.DECIMALFORMAT.format(0d) + "\n"
                + Einstellungen.DECIMALFORMAT.format(0d);
          }
          break;
        case BETRAG:
          if (optiert)
          {
            value = Einstellungen.DECIMALFORMAT.format(10d) + "\n"
                + Einstellungen.DECIMALFORMAT.format(13.8) + "\n\n"
                + Einstellungen.DECIMALFORMAT.format(1.6) + "\n"
                + Einstellungen.DECIMALFORMAT.format(23.8);
          }
          else
          {
            value = Einstellungen.DECIMALFORMAT.format(10d) + "\n"
                + Einstellungen.DECIMALFORMAT.format(13.8) + "\n\n"
                + Einstellungen.DECIMALFORMAT.format(23.8);
          }
          break;
        case SUMME:
          value = Einstellungen.DECIMALFORMAT.format(23.8);
          break;
        case IST:
          value = Einstellungen.DECIMALFORMAT.format(10d);
          break;
        case STAND:
          value = Einstellungen.DECIMALFORMAT.format(-13.8);
          break;
        case SUMME_OFFEN:
          value = Einstellungen.DECIMALFORMAT.format(13.8);
          break;
        case QRCODE_INTRO:
          value = "Bequem bezahlen mit Girocode. Einfach mit der Banking-App auf dem Handy abscannen.";
          break;
        case DATUM:
          value = formatDate(toDate("10.01.2025"));
          break;
        case DATUM_F:
          value = "20251001";
          break;
        case NUMMER:
          value = StringTool.lpad("11",
              (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE),
              "0");
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
        case ADRESSIERUNGSZUSATZ:
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
        case GESCHLECHT:
          value = GeschlechtInput.MAENNLICH;
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
        case MANDATID:
          value = "12345";
          break;
        case MANDATDATUM:
          value = formatDate(toDate("01.01.2024"));
          break;
        case MANDATDATUM_F:
          value = "20240101";
          break;
        case BIC:
          value = "XXXXXXXXXXX";
          break;
        case IBAN:
          value = "DE89 3704 0044 0532 0130 00";
          break;
        case IBANMASKIERT:
          value = "XXXXXXXXXXXXXXX3000";
          break;
        case BANKNAME:
          value = "XY Bank";
          break;
        case EMPFAENGER:
          value = "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland";
          break;
        case ZAHLUNGSWEGTEXT:
          value = "Bitte überweisen Sie den Betrag auf das angegebene Konto.";
          break;
        case ZAHLUNGSWEG:
          value = Zahlungsweg.get(Zahlungsweg.ÜBERWEISUNG);
          break;
        case KOMMENTAR:
          value = "Der Rechnungskommentar";
          break;
        case QRCODE_SUMME:
          // Wird erst in FormularAufbereitung gesetzt
        case DIFFERENZ:
        case MK_BETRAG:
        case MK_BUCHUNGSDATUM:
        case MK_IST:
        case MK_NETTOBETRAG:
        case MK_STAND:
        case MK_STEUERBETRAG:
        case MK_STEUERSATZ:
        case MK_SUMME_OFFEN:
        case MK_ZAHLUNGSGRUND:
        case ZAHLUNGSGRUND1:
        case ZAHLUNGSGRUND2:
          // Deprecated Einträg nicht in der DummyMap, damit sie nicht für neue
          // Formulare etc. verwendet werden
          continue;
      }
      map.put(var.getName(), value);
    }
    return map;
  }
}
