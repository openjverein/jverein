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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.BeitragsUtil;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.OBanToo.SEPA.BankenDaten.Bank;
import de.jost_net.OBanToo.SEPA.BankenDaten.Banken;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedMap extends AbstractMap
{
  public MitgliedMap()
  {
    super();
  }

  public Map<String, Object> getMap(Mitglied m, Map<String, Object> inma)
      throws RemoteException
  {
    return getMap(m, inma, false);
  }

  @SuppressWarnings("deprecation")
  public Map<String, Object> getMap(Mitglied mitglied,
      Map<String, Object> initMap, boolean ohneLesefelder)
      throws RemoteException
  {
    Map<String, Object> map;

    map = Objects.requireNonNullElseGet(initMap, HashMap::new);
    if (mitglied == null || mitglied.getID() == null)
    {
      return getDummyMap(map);
    }

    for (MitgliedVar var : MitgliedVar.values())
    {
      switch (var)
      {
        case ADRESSIERUNGSZUSATZ:
          map.put(var.getName(),
              StringTool.toNotNullString(mitglied.getAdressierungszusatz()));
          break;
        case MITGLIEDSTYP:
          map.put(var.getName(),
              StringTool.toNotNullString(mitglied.getMitgliedstyp().getID()));
          break;
        case ANREDE:
          map.put(var.getName(),
              StringTool.toNotNullString(mitglied.getAnrede()));
          break;
        case ANREDE_FOERMLICH:
          map.put(var.getName(),
              Adressaufbereitung.getAnredeFoermlich(mitglied));
          break;
        case ANREDE_DU:
          map.put(var.getName(), Adressaufbereitung.getAnredeDu(mitglied));
          break;
        case AUSTRITT:
          map.put(var.getName(), Datum.formatDate(mitglied.getAustritt()));
          break;
        case AUSTRITT_F:
          map.put(var.getName(), fromDate(mitglied.getAustritt()));
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG:
          map.put(var.getName(), mitglied.getBeitragsgruppe() != null
              && mitglied.getBeitragsgruppe().getArbeitseinsatzBetrag() != null
                  ? Einstellungen.DECIMALFORMAT.format(
                      mitglied.getBeitragsgruppe().getArbeitseinsatzBetrag())
                  : "");
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN:
          map.put(var.getName(), mitglied.getBeitragsgruppe() != null
              && mitglied.getBeitragsgruppe().getArbeitseinsatzStunden() != null
                  ? Einstellungen.DECIMALFORMAT.format(
                      mitglied.getBeitragsgruppe().getArbeitseinsatzStunden())
                  : "");
          break;
        case BEITRAGSGRUPPE_BETRAG:
          try
          {
            map.put(var.getName(), mitglied.getBeitragsgruppe() != null
                ? Einstellungen.DECIMALFORMAT.format(BeitragsUtil.getBeitrag(
                    Beitragsmodel.getByKey((Integer) Einstellungen
                        .getEinstellung(Property.BEITRAGSMODEL)),
                    mitglied.getZahlungstermin(),
                    mitglied.getZahlungsrhythmus(),
                    mitglied.getBeitragsgruppe(), new Date(), mitglied))
                : "");
          }
          catch (ApplicationException e)
          {
            Logger.error("AplicationException:" + e.getMessage());
          }
          catch (NullPointerException e)
          {
            Logger.error("NullPointerException:" + mitglied.getName());
          }
          break;
        case BEITRAGSGRUPPE_BEZEICHNUNG:
          map.put(var.getName(),
              mitglied.getBeitragsgruppe() != null
                  ? mitglied.getBeitragsgruppe().getBezeichnung()
                  : "");
          break;
        case BEITRAGSGRUPPE_ID:
          map.put(var.getName(),
              mitglied.getBeitragsgruppe() != null
                  ? mitglied.getBeitragsgruppe().getID()
                  : "");
          break;
        case MANDATDATUM:
          map.put(var.getName(), mitglied.getMandatDatum());
          break;
        case MANDATDATUM_F:
          map.put(var.getName(), fromDate(mitglied.getMandatDatum()));
          break;
        case MANDATID:
          map.put(var.getName(), mitglied.getMandatID());
          break;
        case EINGABEDATUM:
          map.put(var.getName(), Datum.formatDate(mitglied.getEingabedatum()));
          break;
        case EINGABEDATUM_F:
          map.put(var.getName(), fromDate(mitglied.getEingabedatum()));
          break;
        case EINTRITT:
          map.put(var.getName(), Datum.formatDate(mitglied.getEintritt()));
          break;
        case EINTRITT_F:
          map.put(var.getName(), fromDate(mitglied.getEintritt()));
          break;
        case EMAIL:
          map.put(var.getName(), mitglied.getEmail());
          break;
        case EMPFAENGER:
          map.put(var.getName(), Adressaufbereitung.getAdressfeld(mitglied));
          break;
        case EXTERNE_MITGLIEDSNUMMER:
          map.put(var.getName(), mitglied.getExterneMitgliedsnummer());
          break;
        case GEBURTSDATUM:
          map.put(var.getName(), Datum.formatDate(mitglied.getGeburtsdatum()));
          break;
        case GEBURTSDATUM_F:
          map.put(var.getName(), fromDate(mitglied.getGeburtsdatum()));
          break;
        case GESCHLECHT:
          map.put(var.getName(), mitglied.getGeschlecht());
          break;
        case HANDY:
          map.put(var.getName(), mitglied.getHandy());
          break;
        case IBANMASKIERT:
          map.put(var.getName(), maskieren(mitglied.getIban()));
          break;
        case IBAN:
          map.put(var.getName(),
              new IBANFormatter().format(mitglied.getIban()));
          break;
        case ID:
          map.put(var.getName(), mitglied.getID());
          break;
        case INDIVIDUELLERBEITRAG:
          if (mitglied.getIndividuellerBeitrag() != null)
          {
            map.put(var.getName(), Einstellungen.DECIMALFORMAT
                .format(mitglied.getIndividuellerBeitrag()));
          }
          else
          {
            map.put(var.getName(), null);
          }

          break;
        case BIC:
          map.put(var.getName(), mitglied.getBic());
          break;
        case BANKNAME:
          String bic = mitglied.getBic();
          if (bic != null)
          {
            Bank bank = Banken.getBankByBIC(bic);
            if (bank != null)
            {
              String name = bank.getBezeichnung();
              if (name != null)
              {
                map.put(var.getName(), name.trim());
              }
            }
          }
          else
          {
            map.put(var.getName(), null);
          }
          break;
        case KONTO_KONTOINHABER:
          map.put(var.getName(), mitglied.getKontoinhaber());
          break;
        case KONTOINHABER:
          map.put(var.getName(),
              mitglied.getKontoinhaber(Mitglied.namenformat.KONTOINHABER));
          break;
        case KONTOINHABER_VORNAMENAME:
          map.put(var.getName(),
              mitglied.getKontoinhaber(Mitglied.namenformat.VORNAME_NAME));
          break;
        case KONTOINHABER_EMPFAENGER:
          map.put(var.getName(),
              mitglied.getKontoinhaber(Mitglied.namenformat.ADRESSE));
          break;
        case KONTOINHABER_ADRESSIERUNGSZUSATZ:
          map.put(var.getName(), mitglied.getAdressierungszusatz());
          break;
        case KONTOINHABER_ANREDE:
          map.put(var.getName(), mitglied.getAnrede());
          break;
        case KONTOINHABER_EMAIL:
          map.put(var.getName(), mitglied.getEmail());
          break;
        case KONTOINHABER_NAME:
          map.put(var.getName(), mitglied.getName());
          break;
        case KONTOINHABER_ORT:
          map.put(var.getName(), mitglied.getOrt());
          break;
        case KONTOINHABER_PERSONENART:
          map.put(var.getName(), mitglied.getPersonenart());
          break;
        case KONTOINHABER_PLZ:
          map.put(var.getName(), mitglied.getPlz());
          break;
        case KONTOINHABER_STAAT:
          map.put(var.getName(), mitglied.getStaat());
          break;
        case KONTOINHABER_STRASSE:
          map.put(var.getName(), mitglied.getStrasse());
          break;
        case KONTOINHABER_TITEL:
          map.put(var.getName(), mitglied.getTitel());
          break;
        case KONTOINHABER_VORNAME:
          map.put(var.getName(), mitglied.getVorname());
          break;
        case KONTOINHABER_GESCHLECHT:
          map.put(var.getName(), mitglied.getGeschlecht());
          break;
        case KUENDIGUNG:
          map.put(var.getName(), Datum.formatDate(mitglied.getKuendigung()));
          break;
        case LETZTEAENDERUNG:
          map.put(var.getName(),
              Datum.formatDate(mitglied.getLetzteAenderung()));
          break;
        case NAME:
          map.put(var.getName(), mitglied.getName());
          break;
        case NAMEVORNAME:
          map.put(var.getName(), Adressaufbereitung.getNameVorname(mitglied));
          break;
        case ORT:
          map.put(var.getName(), mitglied.getOrt());
          break;
        case PERSONENART:
          map.put(var.getName(), mitglied.getPersonenart());
          break;
        case PLZ:
          map.put(var.getName(), mitglied.getPlz());
          break;
        case STAAT:
          map.put(var.getName(), mitglied.getStaat());
          break;
        case STERBETAG:
          map.put(var.getName(), Datum.formatDate(mitglied.getSterbetag()));
          break;
        case STRASSE:
          map.put(var.getName(), mitglied.getStrasse());
          break;
        case TELEFONDIENSTLICH:
          map.put(var.getName(), mitglied.getTelefondienstlich());
          break;
        case TELEFONPRIVAT:
          map.put(var.getName(), mitglied.getTelefonprivat());
          break;
        case TITEL:
          map.put(var.getName(), mitglied.getTitel());
          break;
        case VERMERK1:
          map.put(var.getName(), mitglied.getVermerk1());
          break;
        case VERMERK2:
          map.put(var.getName(), mitglied.getVermerk2());
          break;
        case VORNAME:
          map.put(var.getName(), mitglied.getVorname());
          break;
        case VORNAMENAME:
          map.put(var.getName(), Adressaufbereitung.getVornameName(mitglied));
          break;
        case ZAHLERID:
          map.put(var.getName(), mitglied.getVollZahlerID() == null ? ""
              : mitglied.getVollZahlerID().toString());
          break;
        case ALTERNATIVER_ZAHLER:
          map.put(var.getName(), mitglied.getAbweichenderZahlerID() == null ? ""
              : mitglied.getAbweichenderZahlerID().toString());
          break;
        case ZAHLUNGSRHYTMUS:
        case ZAHLUNGSRHYTHMUS:
          map.put(var.getName(), mitglied.getZahlungsrhythmus() + "");
          break;
        case ZAHLUNGSTERMIN:
          map.put(var.getName(),
              mitglied.getZahlungstermin() != null
                  ? mitglied.getZahlungstermin().getText()
                  : "");
          break;
        case ZAHLUNGSWEG:
          map.put(var.getName(), mitglied.getZahlungsweg() + "");
          break;
        case ZAHLUNGSWEGTEXT:
          String zahlungsweg = "";
          switch (mitglied.getZahlungsweg())
          {
            case Zahlungsweg.BASISLASTSCHRIFT:
              zahlungsweg = (String) Einstellungen
                  .getEinstellung(Property.RECHNUNGTEXTABBUCHUNG);
              zahlungsweg = zahlungsweg.replaceAll("\\$\\{BIC\\}",
                  mitglied.getBic());
              zahlungsweg = zahlungsweg.replaceAll("\\$\\{IBAN\\}",
                  mitglied.getIban());
              zahlungsweg = zahlungsweg.replaceAll("\\$\\{MANDATID\\}",
                  mitglied.getMandatID());
              break;
            case Zahlungsweg.BARZAHLUNG:
              zahlungsweg = (String) Einstellungen
                  .getEinstellung(Property.RECHNUNGTEXTBAR);
              break;
            case Zahlungsweg.ÜBERWEISUNG:
              zahlungsweg = (String) Einstellungen
                  .getEinstellung(Property.RECHNUNGTEXTUEBERWEISUNG);
              break;
          }
          try
          {
            zahlungsweg = VelocityTool.eval(map, zahlungsweg);
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }

          map.put(var.getName(), zahlungsweg);
          break;
      }
    }
    DBIterator<Felddefinition> itfd = Einstellungen.getDBService()
        .createList(Felddefinition.class);
    while (itfd.hasNext())
    {
      Felddefinition fd = itfd.next();
      DBIterator<Zusatzfelder> itzus = Einstellungen.getDBService()
          .createList(Zusatzfelder.class);
      itzus.addFilter("mitglied = ? and felddefinition = ? ", mitglied.getID(),
          fd.getID());
      Zusatzfelder z;
      if (itzus.hasNext())
      {
        z = itzus.next();
      }
      else
      {
        z = Einstellungen.getDBService().createObject(Zusatzfelder.class, null);
      }

      String name = Einstellungen.ZUSATZFELD_PRE + fd.getName();
      switch (fd.getDatentyp())
      {
        case Datentyp.DATUM:
          map.put(name, Datum.formatDate(z.getFeldDatum()));
          break;
        case Datentyp.JANEIN:
          map.put(name, z.getFeldJaNein() ? "X" : " ");
          break;
        case Datentyp.GANZZAHL:
          map.put(name, z.getFeldGanzzahl() + "");
          break;
        case Datentyp.WAEHRUNG:
          if (z.getFeldWaehrung() != null)
          {
            map.put(name,
                Einstellungen.DECIMALFORMAT.format(z.getFeldWaehrung()));
          }
          else
          {
            map.put(name, "");
          }
          break;
        case Datentyp.ZEICHENFOLGE:
          map.put(name, z.getFeld());
          break;
      }
    }

    DBIterator<Eigenschaft> iteig = Einstellungen.getDBService()
        .createList(Eigenschaft.class);
    while (iteig.hasNext())
    {
      Eigenschaft eig = iteig.next();
      DBIterator<Eigenschaften> iteigm = Einstellungen.getDBService()
          .createList(Eigenschaften.class);
      iteigm.addFilter("mitglied = ? and eigenschaft = ?", mitglied.getID(),
          eig.getID());
      String val = "";
      if (iteigm.size() > 0)
      {
        val = "X";
      }
      map.put("mitglied_eigenschaft_" + eig.getName(), val);
    }

    DBIterator<EigenschaftGruppe> eigenschaftGruppeIt = Einstellungen
        .getDBService().createList(EigenschaftGruppe.class);
    while (eigenschaftGruppeIt.hasNext())
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) eigenschaftGruppeIt.next();

      String key = "eigenschaften_" + eg.getName();
      map.put("mitglied_" + key, mitglied.getAttribute(key));
    }

    for (String varname : mitglied.getVariablen().keySet())
    {
      map.put(varname, mitglied.getVariablen().get(varname));
    }

    if (!ohneLesefelder)
    {
      // Füge Lesefelder diesem Mitglied-Objekt hinzu.
      LesefeldAuswerter l = new LesefeldAuswerter();
      l.setLesefelderDefinitionsFromDatabase();
      l.setMap(map);
      map.putAll(l.getLesefelderMap());
    }
    return map;
  }

  public static Map<String, Object> getDummyMap(Map<String, Object> inMap)
      throws RemoteException
  {
    return getDummyMap(inMap, false);
  }

  @SuppressWarnings("deprecation")
  public static Map<String, Object> getDummyMap(Map<String, Object> inMap,
      boolean ohneLesefelder) throws RemoteException
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

    for (MitgliedVar var : MitgliedVar.values())
    {
      switch (var)
      {
        case ADRESSIERUNGSZUSATZ:
        case KONTOINHABER_ADRESSIERUNGSZUSATZ:
          map.put(var.getName(), "Hinterhof bei Müller");
          break;
        case MITGLIEDSTYP:
          map.put(var.getName(), "1");
          break;
        case ANREDE:
        case KONTOINHABER_ANREDE:
          map.put(var.getName(), "Herr");
          break;
        case ANREDE_DU:
          map.put(var.getName(), "Hallo Willi,");
          break;
        case ANREDE_FOERMLICH:
          map.put(var.getName(), "Sehr geehrter Herr Dr. Dr. Wichtig,");
          break;
        case AUSTRITT:
          map.put(var.getName(), "01.01.2025");
          break;
        case AUSTRITT_F:
          map.put(var.getName(), "20250101");
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG:
          map.put(var.getName(), "50");
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN:
          map.put(var.getName(), "10");
          break;
        case BEITRAGSGRUPPE_BEZEICHNUNG:
          map.put(var.getName(), "Beitrag");
          break;
        case BEITRAGSGRUPPE_BETRAG:
          map.put(var.getName(), "300,00");
          break;
        case BEITRAGSGRUPPE_ID:
          map.put(var.getName(), "1");
          break;
        case MANDATDATUM:
          map.put(var.getName(), toDate("01.01.2024"));
          break;
        case MANDATDATUM_F:
          map.put(var.getName(), "20240101");
          break;
        case MANDATID:
          map.put(var.getName(), "12345");
          break;
        case BIC:
          map.put(var.getName(), "BICXXXXXXXX");
          break;
        case EINTRITT:
          map.put(var.getName(), "01.01.2010");
          break;
        case EINTRITT_F:
          map.put(var.getName(), "20100101");
          break;
        case EINGABEDATUM:
          map.put(var.getName(), "01.02.2010");
          break;
        case EINGABEDATUM_F:
          map.put(var.getName(), "20100201");
          break;
        case EMPFAENGER:
        case KONTOINHABER_EMPFAENGER:
          map.put(var.getName(),
              "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland");
          break;
        case EMAIL:
        case KONTOINHABER_EMAIL:
          map.put(var.getName(), "willi.wichtig@jverein.de");
          break;
        case EXTERNE_MITGLIEDSNUMMER:
          map.put(var.getName(), "123456");
          break;
        case GEBURTSDATUM:
          map.put(var.getName(), "02.03.1980");
          break;
        case GEBURTSDATUM_F:
          map.put(var.getName(), "19800302");
          break;
        case GESCHLECHT:
        case KONTOINHABER_GESCHLECHT:
          map.put(var.getName(), GeschlechtInput.MAENNLICH);
          break;
        case HANDY:
          map.put(var.getName(), "0152778899");
          break;
        case IBAN:
          map.put(var.getName(), "DE89 3704 0044 0532 0130 00");
          break;
        case IBANMASKIERT:
          map.put(var.getName(), "XXXXXXXXXXXXXXX3000");
          break;
        case ID:
          map.put(var.getName(), "15");
          break;
        case INDIVIDUELLERBEITRAG:
          map.put(var.getName(), "123,45");
          break;
        case BANKNAME:
          map.put(var.getName(), "XY Bank");
          break;
        case KONTO_KONTOINHABER:
          map.put(var.getName(), "Gemeinschaftskonto Willi und Else Müller");
          break;
        case KONTOINHABER:
          map.put(var.getName(), "Gemeinschaftskonto Willi und Else Müller");
          break;
        case KUENDIGUNG:
          map.put(var.getName(), "01.11.2024");
          break;
        case LETZTEAENDERUNG:
          map.put(var.getName(), "01.11.2024");
          break;
        case NAME:
        case KONTOINHABER_NAME:
          map.put(var.getName(), "Wichtig");
          break;
        case NAMEVORNAME:
          map.put(var.getName(), "Wichtig, Dr. Dr. Willi");
          break;
        case ORT:
        case KONTOINHABER_ORT:
          map.put(var.getName(), "Testenhausen");
          break;
        case PERSONENART:
        case KONTOINHABER_PERSONENART:
          map.put(var.getName(), "n");
          break;
        case PLZ:
        case KONTOINHABER_PLZ:
          map.put(var.getName(), "12345");
          break;
        case STAAT:
        case KONTOINHABER_STAAT:
          map.put(var.getName(), "Deutschland");
          break;
        case STERBETAG:
          map.put(var.getName(), "31.12.2024");
          break;
        case STRASSE:
        case KONTOINHABER_STRASSE:
          map.put(var.getName(), "Bahnhofstr. 22");
          break;
        case TELEFONDIENSTLICH:
          map.put(var.getName(), "011/123456789");
          break;
        case TELEFONPRIVAT:
          map.put(var.getName(), "011/123456");
          break;
        case TITEL:
        case KONTOINHABER_TITEL:
          map.put(var.getName(), "Dr. Dr.");
          break;
        case VERMERK1:
          map.put(var.getName(), "Vermerk 1");
          break;
        case VERMERK2:
          map.put(var.getName(), "Vermerk 2");
          break;
        case VORNAME:
        case KONTOINHABER_VORNAME:
          map.put(var.getName(), "Willi");
          break;
        case VORNAMENAME:
        case KONTOINHABER_VORNAMENAME:
          map.put(var.getName(), "Dr. Dr. Willi Wichtig");
          break;
        case ZAHLUNGSRHYTMUS:
        case ZAHLUNGSRHYTHMUS:
          map.put(var.getName(),
              Zahlungsrhythmus.get(Zahlungsrhythmus.HALBJAEHRLICH));
          break;
        case ZAHLUNGSTERMIN:
          map.put(var.getName(), Zahlungstermin.HALBJAEHRLICH4.toString());
          break;
        case ZAHLUNGSWEG:
          map.put(var.getName(), "2");
          break;
        case ZAHLUNGSWEGTEXT:
          map.put(var.getName(),
              "Bitte überweisen Sie den Betrag auf das angegebene Konto.");
          break;
        case ZAHLERID:
          map.put(var.getName(), "123456");
          break;
        case ALTERNATIVER_ZAHLER:
          map.put(var.getName(), "123456");
      }
    }
    // Liste der Felddefinitionen
    DBIterator<Felddefinition> itfd = Einstellungen.getDBService()
        .createList(Felddefinition.class);
    while (itfd.hasNext())
    {
      Felddefinition fd = itfd.next();
      String name = Einstellungen.ZUSATZFELD_PRE + fd.getName();
      switch (fd.getDatentyp())
      {
        case Datentyp.DATUM:
          map.put(name, "31.12.2024");
          break;
        case Datentyp.JANEIN:
          map.put(name, "X");
          break;
        case Datentyp.GANZZAHL:
          map.put(name, "22");
          break;
        case Datentyp.WAEHRUNG:
          map.put(name, "3.00");
          break;
        case Datentyp.ZEICHENFOLGE:
          map.put(name, "abcd");
          break;
      }
    }

    // Liste der Eigenschaften
    DBIterator<Eigenschaft> iteig = Einstellungen.getDBService()
        .createList(Eigenschaft.class);
    while (iteig.hasNext())
    {
      Eigenschaft eig = iteig.next();
      map.put("mitglied_eigenschaft_" + eig.getName(), "X");
    }

    // Liste der Eigenschaften einer Eigenschaftengruppe
    DBIterator<EigenschaftGruppe> eigenschaftGruppeIt = Einstellungen
        .getDBService().createList(EigenschaftGruppe.class);
    while (eigenschaftGruppeIt.hasNext())
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) eigenschaftGruppeIt.next();

      map.put("mitglied_eigenschaften_" + eg.getName(),
          "Eigenschaft1, Eigenschaft2");
    }

    if (!ohneLesefelder)
    {
      // Füge Lesefelder diesem Mitglied-Objekt hinzu.
      LesefeldAuswerter l = new LesefeldAuswerter();
      l.setLesefelderDefinitionsFromDatabase();
      l.setMap(map);
      map.putAll(l.getLesefelderMap());
    }

    return map;
  }

}
