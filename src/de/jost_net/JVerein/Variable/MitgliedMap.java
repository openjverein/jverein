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
import java.util.Objects;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.io.BeitragsUtil;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.OBanToo.SEPA.BankenDaten.Bank;
import de.jost_net.OBanToo.SEPA.BankenDaten.Banken;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedMap extends AbstractMap
{
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
      Object value = null;
      switch (var)
      {
        case ADRESSIERUNGSZUSATZ:
        case KONTOINHABER_ADRESSIERUNGSZUSATZ:
          value = StringTool.toNotNullString(mitglied.getAdressierungszusatz());
          break;
        case MITGLIEDSTYP:
          value = StringTool
              .toNotNullString(mitglied.getMitgliedstyp().getID());
          break;
        case ANREDE:
        case KONTOINHABER_ANREDE:
          value = StringTool.toNotNullString(mitglied.getAnrede());
          break;
        case ANREDE_FOERMLICH:
          value = Adressaufbereitung.getAnredeFoermlich(mitglied);
          break;
        case ANREDE_DU:
          value = Adressaufbereitung.getAnredeDu(mitglied);
          break;
        case AUSTRITT:
          value = Datum.formatDate(mitglied.getAustritt());
          break;
        case AUSTRITT_F:
          value = fromDate(mitglied.getAustritt());
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG:
          value = mitglied.getBeitragsgruppe() != null
              && mitglied.getBeitragsgruppe().getArbeitseinsatzBetrag() != null
                  ? Einstellungen.DECIMALFORMAT.format(
                      mitglied.getBeitragsgruppe().getArbeitseinsatzBetrag())
                  : "";
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN:
          value = mitglied.getBeitragsgruppe() != null
              && mitglied.getBeitragsgruppe().getArbeitseinsatzStunden() != null
                  ? Einstellungen.DECIMALFORMAT.format(
                      mitglied.getBeitragsgruppe().getArbeitseinsatzStunden())
                  : "";
          break;
        case BEITRAGSGRUPPE_BETRAG:
          try
          {
            value = mitglied.getBeitragsgruppe() != null
                ? Einstellungen.DECIMALFORMAT.format(BeitragsUtil.getBeitrag(
                    Beitragsmodel.getByKey((Integer) Einstellungen
                        .getEinstellung(Property.BEITRAGSMODEL)),
                    mitglied.getZahlungstermin(),
                    mitglied.getZahlungsrhythmus(),
                    mitglied.getBeitragsgruppe(), new Date(), mitglied))
                : "";
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
          value = mitglied.getBeitragsgruppe() != null
              ? mitglied.getBeitragsgruppe().getBezeichnung()
              : "";
          break;
        case BEITRAGSGRUPPE_ID:
          value = mitglied.getBeitragsgruppe() != null
              ? mitglied.getBeitragsgruppe().getID()
              : "";
          break;
        case MANDATDATUM:
          value = new JVDateFormatTTMMJJJJ().format(mitglied.getMandatDatum());
          break;
        case MANDATDATUM_F:
          value = fromDate(mitglied.getMandatDatum());
          break;
        case MANDATID:
          value = mitglied.getMandatID();
          break;
        case EINGABEDATUM:
          value = Datum.formatDate(mitglied.getEingabedatum());
          break;
        case EINGABEDATUM_F:
          value = fromDate(mitglied.getEingabedatum());
          break;
        case EINTRITT:
          value = Datum.formatDate(mitglied.getEintritt());
          break;
        case EINTRITT_F:
          value = fromDate(mitglied.getEintritt());
          break;
        case EMAIL:
        case KONTOINHABER_EMAIL:
          value = mitglied.getEmail();
          break;
        case EMPFAENGER:
          value = Adressaufbereitung.getAdressfeld(mitglied);
          break;
        case EXTERNE_MITGLIEDSNUMMER:
          value = mitglied.getExterneMitgliedsnummer();
          break;
        case GEBURTSDATUM:
          value = Datum.formatDate(mitglied.getGeburtsdatum());
          break;
        case GEBURTSDATUM_F:
          value = fromDate(mitglied.getGeburtsdatum());
          break;
        case GESCHLECHT:
        case KONTOINHABER_GESCHLECHT:
          value = mitglied.getGeschlecht();
          break;
        case HANDY:
          value = mitglied.getHandy();
          break;
        case IBANMASKIERT:
          value = maskieren(mitglied.getIban());
          break;
        case IBAN:
          value = new IBANFormatter().format(mitglied.getIban());
          break;
        case ID:
          value = mitglied.getID();
          break;
        case INDIVIDUELLERBEITRAG:
          if (mitglied.getIndividuellerBeitrag() != null)
          {
            value = Einstellungen.DECIMALFORMAT
                .format(mitglied.getIndividuellerBeitrag());
          }
          break;
        case BIC:
          value = mitglied.getBic();
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
                value = name.trim();
              }
            }
          }
          break;
        case KONTO_KONTOINHABER:
          value = mitglied.getKontoinhaber();
          break;
        case KONTOINHABER:
          value = mitglied.getKontoinhaber(Mitglied.namenformat.KONTOINHABER);
          break;
        case KONTOINHABER_VORNAMENAME:
          value = mitglied.getKontoinhaber(Mitglied.namenformat.VORNAME_NAME);
          break;
        case KONTOINHABER_EMPFAENGER:
          value = mitglied.getKontoinhaber(Mitglied.namenformat.ADRESSE);
          break;
        case KUENDIGUNG:
          value = Datum.formatDate(mitglied.getKuendigung());
          break;
        case LETZTEAENDERUNG:
          value = Datum.formatDate(mitglied.getLetzteAenderung());
          break;
        case NAME:
        case KONTOINHABER_NAME:
          value = mitglied.getName();
          break;
        case NAMEVORNAME:
          value = Adressaufbereitung.getNameVorname(mitglied);
          break;
        case ORT:
        case KONTOINHABER_ORT:
          value = mitglied.getOrt();
          break;
        case PERSONENART:
        case KONTOINHABER_PERSONENART:
          value = mitglied.getPersonenart();
          break;
        case PLZ:
        case KONTOINHABER_PLZ:
          value = mitglied.getPlz();
          break;
        case STAAT:
        case KONTOINHABER_STAAT:
          value = mitglied.getStaat();
          break;
        case STERBETAG:
          value = Datum.formatDate(mitglied.getSterbetag());
          break;
        case STRASSE:
        case KONTOINHABER_STRASSE:
          value = mitglied.getStrasse();
          break;
        case TELEFONDIENSTLICH:
          value = mitglied.getTelefondienstlich();
          break;
        case TELEFONPRIVAT:
          value = mitglied.getTelefonprivat();
          break;
        case TITEL:
        case KONTOINHABER_TITEL:
          value = mitglied.getTitel();
          break;
        case VERMERK1:
          value = mitglied.getVermerk1();
          break;
        case VERMERK2:
          value = mitglied.getVermerk2();
          break;
        case VORNAME:
        case KONTOINHABER_VORNAME:
          value = mitglied.getVorname();
          break;
        case VORNAMENAME:
          value = Adressaufbereitung.getVornameName(mitglied);
          break;
        case ZAHLERID:
          value = mitglied.getVollZahlerID() == null ? ""
              : mitglied.getVollZahlerID().toString();
          break;
        case ALTERNATIVER_ZAHLER:
          value = mitglied.getAbweichenderZahlerID() == null ? ""
              : mitglied.getAbweichenderZahlerID().toString();
          break;
        case ZAHLUNGSRHYTMUS:
        case ZAHLUNGSRHYTHMUS:
          value = mitglied.getZahlungsrhythmus() + "";
          break;
        case ZAHLUNGSTERMIN:
          value = mitglied.getZahlungstermin() != null
              ? mitglied.getZahlungstermin().getText()
              : "";
          break;
        case ZAHLUNGSWEG:
          value = mitglied.getZahlungsweg() + "";
          break;
      }
      map.put(var.getName(), value);
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
      EigenschaftGruppe eg = eigenschaftGruppeIt.next();

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
      Object value = null;
      switch (var)
      {
        case ADRESSIERUNGSZUSATZ:
          value = "Hinterhof bei Müller";
          break;
        case MITGLIEDSTYP:
          value = "1";
          break;
        case ANREDE:
          value = "Herr";
          break;
        case ANREDE_DU:
          value = "Hallo Willi,";
          break;
        case ANREDE_FOERMLICH:
          value = "Sehr geehrter Herr Dr. Dr. Wichtig,";
          break;
        case AUSTRITT:
          value = "01.01.2025";
          break;
        case AUSTRITT_F:
          value = "20250101";
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG:
          value = "50";
          break;
        case BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN:
          value = "10";
          break;
        case BEITRAGSGRUPPE_BEZEICHNUNG:
          value = "Beitrag";
          break;
        case BEITRAGSGRUPPE_BETRAG:
          value = Einstellungen.DECIMALFORMAT.format(300d);
          break;
        case BEITRAGSGRUPPE_ID:
          value = "1";
          break;
        case MANDATDATUM:
          value = new JVDateFormatTTMMJJJJ().format(toDate("01.01.2024"));
          break;
        case MANDATDATUM_F:
          value = "20240101";
          break;
        case MANDATID:
          value = "12345";
          break;
        case BIC:
          value = "BICXXXXXXXX";
          break;
        case EINTRITT:
          value = "01.01.2010";
          break;
        case EINTRITT_F:
          value = "20100101";
          break;
        case EINGABEDATUM:
          value = "01.02.2010";
          break;
        case EINGABEDATUM_F:
          value = "20100201";
          break;
        case EMPFAENGER:
          value = "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland";
          break;
        case EMAIL:
          value = "willi.wichtig@jverein.de";
          break;
        case EXTERNE_MITGLIEDSNUMMER:
          value = "123456";
          break;
        case GEBURTSDATUM:
          value = "02.03.1980";
          break;
        case GEBURTSDATUM_F:
          value = "19800302";
          break;
        case GESCHLECHT:
          value = GeschlechtInput.MAENNLICH;
          break;
        case HANDY:
          value = "0152778899";
          break;
        case IBAN:
          value = "DE89 3704 0044 0532 0130 00";
          break;
        case IBANMASKIERT:
          value = "XXXXXXXXXXXXXXX3000";
          break;
        case ID:
          value = "15";
          break;
        case INDIVIDUELLERBEITRAG:
          value = Einstellungen.DECIMALFORMAT.format(123.45);
          break;
        case BANKNAME:
          value = "XY Bank";
          break;
        case KONTO_KONTOINHABER:
          value = "Gemeinschaftskonto Willi und Else Müller";
          break;
        case KONTOINHABER:
          value = "Gemeinschaftskonto Willi und Else Müller";
          break;
        case KUENDIGUNG:
          value = "01.11.2024";
          break;
        case LETZTEAENDERUNG:
          value = "01.11.2024";
          break;
        case NAME:
          value = "Wichtig";
          break;
        case NAMEVORNAME:
          value = "Wichtig, Dr. Dr. Willi";
          break;
        case ORT:
          value = "Testenhausen";
          break;
        case PERSONENART:
          value = "n";
          break;
        case PLZ:
          value = "12345";
          break;
        case STAAT:
          value = "Deutschland";
          break;
        case STERBETAG:
          value = "31.12.2024";
          break;
        case STRASSE:
          value = "Bahnhofstr. 22";
          break;
        case TELEFONDIENSTLICH:
          value = "011/123456789";
          break;
        case TELEFONPRIVAT:
          value = "011/123456";
          break;
        case TITEL:
          value = "Dr. Dr.";
          break;
        case VERMERK1:
          value = "Vermerk 1";
          break;
        case VERMERK2:
          value = "Vermerk 2";
          break;
        case VORNAME:
          value = "Willi";
          break;
        case VORNAMENAME:
          value = "Dr. Dr. Willi Wichtig";
          break;
        case ZAHLUNGSRHYTHMUS:
          value = Zahlungsrhythmus.get(Zahlungsrhythmus.HALBJAEHRLICH);
          break;
        case ZAHLUNGSTERMIN:
          value = Zahlungstermin.HALBJAEHRLICH4.toString();
          break;
        case ZAHLUNGSWEG:
          value = "2";
          break;
        case ZAHLERID:
          value = "123456";
          break;
        case ALTERNATIVER_ZAHLER:
          value = "123456";
        case KONTOINHABER_ADRESSIERUNGSZUSATZ:
        case KONTOINHABER_ANREDE:
        case KONTOINHABER_EMAIL:
        case KONTOINHABER_EMPFAENGER:
        case KONTOINHABER_GESCHLECHT:
        case KONTOINHABER_NAME:
        case KONTOINHABER_ORT:
        case KONTOINHABER_PERSONENART:
        case KONTOINHABER_PLZ:
        case KONTOINHABER_STAAT:
        case KONTOINHABER_STRASSE:
        case KONTOINHABER_TITEL:
        case KONTOINHABER_VORNAME:
        case KONTOINHABER_VORNAMENAME:
        case ZAHLUNGSRHYTMUS:
          // Deprecated Einträg nicht in der DummyMap, damit sie nicht für neue
          // Formulare etc. verwendet werden
          continue;
      }
      map.put(var.getName(), value);
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
          map.put(name, Einstellungen.DECIMALFORMAT.format(3d));
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
      EigenschaftGruppe eg = eigenschaftGruppeIt.next();

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
