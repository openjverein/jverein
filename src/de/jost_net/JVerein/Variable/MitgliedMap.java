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
    map.put(MitgliedVar.ADRESSIERUNGSZUSATZ.getName(),
        StringTool.toNotNullString(mitglied.getAdressierungszusatz()));
    map.put(MitgliedVar.MITGLIEDSTYP.getName(),
        StringTool.toNotNullString(mitglied.getMitgliedstyp().getID()));
    map.put(MitgliedVar.ANREDE.getName(),
        StringTool.toNotNullString(mitglied.getAnrede()));
    map.put(MitgliedVar.ANREDE_FOERMLICH.getName(),
        Adressaufbereitung.getAnredeFoermlich(mitglied));
    map.put(MitgliedVar.ANREDE_DU.getName(),
        Adressaufbereitung.getAnredeDu(mitglied));
    map.put(MitgliedVar.AUSTRITT.getName(),
        Datum.formatDate(mitglied.getAustritt()));
    map.put(MitgliedVar.BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG.getName(),
        mitglied.getBeitragsgruppe() != null ? Einstellungen.DECIMALFORMAT
            .format(mitglied.getBeitragsgruppe().getArbeitseinsatzBetrag())
            : "");
    map.put(MitgliedVar.BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN.getName(),
        mitglied.getBeitragsgruppe() != null ? Einstellungen.DECIMALFORMAT
            .format(mitglied.getBeitragsgruppe().getArbeitseinsatzStunden())
            : "");
    try
    {
      map.put(MitgliedVar.BEITRAGSGRUPPE_BETRAG.getName(),
          mitglied.getBeitragsgruppe() != null
              ? Einstellungen.DECIMALFORMAT.format(BeitragsUtil.getBeitrag(
                  Beitragsmodel.getByKey((Integer) Einstellungen
                      .getEinstellung(Property.BEITRAGSMODEL)),
                  mitglied.getZahlungstermin(),
                  mitglied.getZahlungsrhythmus().getKey(),
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
    map.put(MitgliedVar.BEITRAGSGRUPPE_BEZEICHNUNG.getName(),
        mitglied.getBeitragsgruppe() != null
            ? mitglied.getBeitragsgruppe().getBezeichnung()
            : "");
    map.put(MitgliedVar.BEITRAGSGRUPPE_ID.getName(),
        mitglied.getBeitragsgruppe() != null
            ? mitglied.getBeitragsgruppe().getID()
            : "");
    map.put(MitgliedVar.MANDATDATUM.getName(), mitglied.getMandatDatum());
    map.put(MitgliedVar.MANDATID.getName(), mitglied.getMandatID());
    map.put(MitgliedVar.BIC.getName(), mitglied.getBic());
    map.put(MitgliedVar.EINGABEDATUM.getName(),
        Datum.formatDate(mitglied.getEingabedatum()));
    map.put(MitgliedVar.EINTRITT.getName(),
        Datum.formatDate(mitglied.getEintritt()));
    map.put(MitgliedVar.EMAIL.getName(), mitglied.getEmail());
    map.put(MitgliedVar.EMPFAENGER.getName(),
        Adressaufbereitung.getAdressfeld(mitglied));
    map.put(MitgliedVar.EXTERNE_MITGLIEDSNUMMER.getName(),
        mitglied.getExterneMitgliedsnummer());
    map.put(MitgliedVar.GEBURTSDATUM.getName(),
        Datum.formatDate(mitglied.getGeburtsdatum()));
    map.put(MitgliedVar.GESCHLECHT.getName(), mitglied.getGeschlecht());
    map.put(MitgliedVar.HANDY.getName(), mitglied.getHandy());
    map.put(MitgliedVar.IBANMASKIERT.getName(),
        VarTools.maskieren(mitglied.getIban()));
    map.put(MitgliedVar.IBAN.getName(), mitglied.getIban());
    map.put(MitgliedVar.ID.getName(), mitglied.getID());
    if (mitglied.getIndividuellerBeitrag() != null)
    {
      map.put(MitgliedVar.INDIVIDUELLERBEITRAG.getName(),
          Einstellungen.DECIMALFORMAT
              .format(mitglied.getIndividuellerBeitrag()));
    }
    else
    {
      map.put(MitgliedVar.INDIVIDUELLERBEITRAG.getName(), null);
    }
    map.put(MitgliedVar.BANKNAME.getName(), getBankname(mitglied));
    map.put(MitgliedVar.KONTOINHABER.getName(),
        mitglied.getKontoinhaber(Mitglied.namenformat.NAME_VORNAME));
    map.put(MitgliedVar.KONTOINHABER_VORNAMENAME.getName(),
        mitglied.getKontoinhaber(Mitglied.namenformat.VORNAME_NAME));
    map.put(MitgliedVar.KONTOINHABER_EMPFAENGER.getName(),
        mitglied.getKontoinhaber(Mitglied.namenformat.ADRESSE));
    map.put(MitgliedVar.KONTOINHABER_ADRESSIERUNGSZUSATZ.getName(),
        mitglied.getKtoiAdressierungszusatz());
    map.put(MitgliedVar.KONTOINHABER_ANREDE.getName(),
        mitglied.getKtoiAnrede());
    map.put(MitgliedVar.KONTOINHABER_EMAIL.getName(), mitglied.getKtoiEmail());
    map.put(MitgliedVar.KONTOINHABER_NAME.getName(), mitglied.getKtoiName());
    map.put(MitgliedVar.KONTOINHABER_ORT.getName(), mitglied.getKtoiOrt());
    map.put(MitgliedVar.KONTOINHABER_PERSONENART.getName(),
        mitglied.getKtoiPersonenart());
    map.put(MitgliedVar.KONTOINHABER_PLZ.getName(), mitglied.getKtoiPlz());
    map.put(MitgliedVar.KONTOINHABER_STAAT.getName(), mitglied.getKtoiStaat());
    map.put(MitgliedVar.KONTOINHABER_STRASSE.getName(),
        mitglied.getKtoiStrasse());
    map.put(MitgliedVar.KONTOINHABER_TITEL.getName(), mitglied.getKtoiTitel());
    map.put(MitgliedVar.KONTOINHABER_VORNAME.getName(),
        mitglied.getKtoiVorname());
    map.put(MitgliedVar.KONTOINHABER_GESCHLECHT.getName(),
        mitglied.getKtoiGeschlecht());
    map.put(MitgliedVar.KUENDIGUNG.getName(),
        Datum.formatDate(mitglied.getKuendigung()));
    map.put(MitgliedVar.LETZTEAENDERUNG.getName(),
        Datum.formatDate(mitglied.getLetzteAenderung()));
    map.put(MitgliedVar.NAME.getName(), mitglied.getName());
    map.put(MitgliedVar.NAMEVORNAME.getName(),
        Adressaufbereitung.getNameVorname(mitglied));
    map.put(MitgliedVar.ORT.getName(), mitglied.getOrt());
    map.put(MitgliedVar.PERSONENART.getName(), mitglied.getPersonenart());
    map.put(MitgliedVar.PLZ.getName(), mitglied.getPlz());
    map.put(MitgliedVar.STAAT.getName(), mitglied.getStaat());
    map.put(MitgliedVar.STERBETAG.getName(),
        Datum.formatDate(mitglied.getSterbetag()));
    map.put(MitgliedVar.STRASSE.getName(), mitglied.getStrasse());
    map.put(MitgliedVar.TELEFONDIENSTLICH.getName(),
        mitglied.getTelefondienstlich());
    map.put(MitgliedVar.TELEFONPRIVAT.getName(), mitglied.getTelefonprivat());
    map.put(MitgliedVar.TITEL.getName(), mitglied.getTitel());
    map.put(MitgliedVar.VERMERK1.getName(), mitglied.getVermerk1());
    map.put(MitgliedVar.VERMERK2.getName(), mitglied.getVermerk2());
    map.put(MitgliedVar.VORNAME.getName(), mitglied.getVorname());
    map.put(MitgliedVar.VORNAMENAME.getName(),
        Adressaufbereitung.getVornameName(mitglied));
    map.put(MitgliedVar.ZAHLERID.getName(), mitglied.getVollZahlerID());
    map.put(MitgliedVar.ZAHLUNGSRHYTMUS.getName(),
        mitglied.getZahlungsrhythmus() + "");
    map.put(MitgliedVar.ZAHLUNGSRHYTHMUS.getName(),
        mitglied.getZahlungsrhythmus() + "");
    map.put(MitgliedVar.ZAHLUNGSTERMIN.getName(),
        mitglied.getZahlungstermin() != null
            ? mitglied.getZahlungstermin().getText()
            : "");
    map.put(MitgliedVar.ZAHLUNGSWEG.getName(), mitglied.getZahlungsweg() + "");

    String zahlungsweg = "";
    switch (mitglied.getZahlungsweg())
    {
      case Zahlungsweg.BASISLASTSCHRIFT:
        zahlungsweg = (String) Einstellungen
            .getEinstellung(Property.RECHNUNGTEXTABBUCHUNG);
        zahlungsweg = zahlungsweg.replaceAll("\\$\\{BIC\\}", mitglied.getBic());
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
    map.put(MitgliedVar.ZAHLUNGSWEGTEXT.getName(), zahlungsweg);

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

      switch (fd.getDatentyp())
      {
        case Datentyp.DATUM:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(),
              Datum.formatDate(z.getFeldDatum()));
          break;
        case Datentyp.JANEIN:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(),
              z.getFeldJaNein() ? "X" : " ");
          break;
        case Datentyp.GANZZAHL:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(),
              z.getFeldGanzzahl() + "");
          break;
        case Datentyp.WAEHRUNG:
          if (z.getFeldWaehrung() != null)
          {
            map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(),
                Einstellungen.DECIMALFORMAT.format(z.getFeldWaehrung()));
          }
          else
          {
            map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), "");
          }
          break;
        case Datentyp.ZEICHENFOLGE:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), z.getFeld());
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
      map.put("mitglied_eigenschaft_" + eig.getBezeichnung(), val);
    }

    DBIterator<EigenschaftGruppe> eigenschaftGruppeIt = Einstellungen
        .getDBService().createList(EigenschaftGruppe.class);
    while (eigenschaftGruppeIt.hasNext())
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) eigenschaftGruppeIt.next();

      String key = "eigenschaften_" + eg.getBezeichnung();
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

  private Object getBankname(Mitglied m) throws RemoteException
  {
    String bic = m.getBic();
    if (null != bic)
    {
      Bank bank = Banken.getBankByBIC(bic);
      if (null != bank)
      {
        return formatBankname(bank);
      }
    }
    return null;
  }

  private String formatBankname(Bank bank)
  {
    String name = bank.getBezeichnung();
    if (null != name)
    {
      return name.trim();
    }
    return null;
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

    map.put(MitgliedVar.ADRESSIERUNGSZUSATZ.getName(), "Hinterhof bei Müller");
    map.put(MitgliedVar.MITGLIEDSTYP.getName(), "1");
    map.put(MitgliedVar.ANREDE.getName(), "Herrn");
    map.put(MitgliedVar.ANREDE_DU.getName(), "Hallo Willi,");
    map.put(MitgliedVar.ANREDE_FOERMLICH.getName(),
        "Sehr geehrter Herr Dr. Dr. Wichtig,");
    map.put(MitgliedVar.AUSTRITT.getName(), "01.01.2025");
    map.put(MitgliedVar.BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG.getName(), "50");
    map.put(MitgliedVar.BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN.getName(), "10");
    map.put(MitgliedVar.BEITRAGSGRUPPE_BEZEICHNUNG.getName(), "Beitrag");
    map.put(MitgliedVar.BEITRAGSGRUPPE_BETRAG.getName(), "300,00");
    map.put(MitgliedVar.BEITRAGSGRUPPE_ID.getName(), "1");
    map.put(MitgliedVar.MANDATDATUM.getName(), toDate("01.01.2024"));
    map.put(MitgliedVar.MANDATID.getName(), "12345");
    map.put(MitgliedVar.BIC.getName(), "BICXXXXXXXX");
    map.put(MitgliedVar.BLZ.getName(), "");
    map.put(MitgliedVar.EINTRITT.getName(), "01.01.2010");
    map.put(MitgliedVar.EINGABEDATUM.getName(), "01.02.2010");
    map.put(MitgliedVar.EMPFAENGER.getName(),
        "Herr\nDr. Dr. Willi Wichtig\nHinterhof bei Müller\nBahnhofstr. 22\n12345 Testenhausen\nDeutschland");
    map.put(MitgliedVar.EMAIL.getName(), "willi.wichtig@jverein.de");
    map.put(MitgliedVar.EXTERNE_MITGLIEDSNUMMER.getName(), "123456");
    map.put(MitgliedVar.GEBURTSDATUM.getName(), "02.03.1980");
    map.put(MitgliedVar.GESCHLECHT.getName(), GeschlechtInput.MAENNLICH);
    map.put(MitgliedVar.HANDY.getName(), "0152778899");
    map.put(MitgliedVar.IBAN.getName(), "DE89370400440532013000");
    map.put(MitgliedVar.IBANMASKIERT.getName(), "XXXXXXXXXXXXXXX3000");
    map.put(MitgliedVar.ID.getName(), "15");
    map.put(MitgliedVar.INDIVIDUELLERBEITRAG.getName(), "123,45");
    map.put(MitgliedVar.KONTO.getName(), "");
    map.put(MitgliedVar.BANKNAME.getName(), "XY Bank");
    map.put(MitgliedVar.KONTOINHABER.getName(), "Maier, Dr. Werner");
    map.put(MitgliedVar.KONTOINHABER_VORNAMENAME.getName(), "Dr. Werner Maier");
    map.put(MitgliedVar.KONTOINHABER_EMPFAENGER.getName(),
        "Herr\nDr. Werner Maier\nAdresszusatz\nKirchenstrasse 5\n5678 Essen\nDeutschland");
    map.put(MitgliedVar.KONTOINHABER_PERSONENART.getName(), "n");
    map.put(MitgliedVar.KONTOINHABER_ANREDE.getName(), "Herr");
    map.put(MitgliedVar.KONTOINHABER_TITEL.getName(), "Dr.");
    map.put(MitgliedVar.KONTOINHABER_NAME.getName(), "Maier");
    map.put(MitgliedVar.KONTOINHABER_VORNAME.getName(), "Werner");
    map.put(MitgliedVar.KONTOINHABER_STRASSE.getName(), "Kirchenstrasse 5");
    map.put(MitgliedVar.KONTOINHABER_ADRESSIERUNGSZUSATZ.getName(),
        "Adresszusatz");
    map.put(MitgliedVar.KONTOINHABER_PLZ.getName(), "5678");
    map.put(MitgliedVar.KONTOINHABER_ORT.getName(), "Essen");
    map.put(MitgliedVar.KONTOINHABER_STAAT.getName(), "Deutschland");
    map.put(MitgliedVar.KONTOINHABER_EMAIL.getName(),
        "werner.maier@jverein.de");
    map.put(MitgliedVar.KONTOINHABER_GESCHLECHT.getName(),
        GeschlechtInput.MAENNLICH);
    map.put(MitgliedVar.KUENDIGUNG.getName(), "01.11.2024");
    map.put(MitgliedVar.LETZTEAENDERUNG.getName(), "01.11.2024");
    map.put(MitgliedVar.NAME.getName(), "Wichtig");
    map.put(MitgliedVar.NAMEVORNAME.getName(), "Wichtig, Dr. Dr. Willi");
    map.put(MitgliedVar.ORT.getName(), "Testenhausen");
    map.put(MitgliedVar.PERSONENART.getName(), "n");
    map.put(MitgliedVar.PLZ.getName(), "12345");
    map.put(MitgliedVar.STAAT.getName(), "Deutschland");
    map.put(MitgliedVar.STERBETAG.getName(), "31.12.2024");
    map.put(MitgliedVar.STRASSE.getName(), "Bahnhofstr. 22");
    map.put(MitgliedVar.TELEFONDIENSTLICH.getName(), "011/123456789");
    map.put(MitgliedVar.TELEFONPRIVAT.getName(), "011/123456");
    map.put(MitgliedVar.TITEL.getName(), "Dr. Dr.");
    map.put(MitgliedVar.VERMERK1.getName(), "Vermerk 1");
    map.put(MitgliedVar.VERMERK2.getName(), "Vermerk 2");
    map.put(MitgliedVar.VORNAME.getName(), "Willi");
    map.put(MitgliedVar.VORNAMENAME.getName(), "Dr. Dr. Willi Wichtig");
    map.put(MitgliedVar.ZAHLUNGSRHYTMUS.getName(),
        Zahlungsrhythmus.get(Zahlungsrhythmus.HALBJAEHRLICH));
    map.put(MitgliedVar.ZAHLUNGSRHYTHMUS.getName(),
        Zahlungsrhythmus.get(Zahlungsrhythmus.HALBJAEHRLICH));
    map.put(MitgliedVar.ZAHLUNGSTERMIN.getName(),
        Zahlungstermin.HALBJAEHRLICH4.toString());
    map.put(MitgliedVar.ZAHLUNGSWEG.getName(), "2");
    map.put(MitgliedVar.ZAHLUNGSWEGTEXT.getName(),
        "Bitte überweisen Sie den Betrag auf das angegebene Konto.");
    map.put(MitgliedVar.ZAHLERID.getName(), "123456");

    // Liste der Felddefinitionen
    DBIterator<Felddefinition> itfd = Einstellungen.getDBService()
        .createList(Felddefinition.class);
    while (itfd.hasNext())
    {
      Felddefinition fd = itfd.next();
      switch (fd.getDatentyp())
      {
        case Datentyp.DATUM:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), "31.12.2024");
          break;
        case Datentyp.JANEIN:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), "X");
          break;
        case Datentyp.GANZZAHL:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), "22");
          break;
        case Datentyp.WAEHRUNG:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), "3.00");
          break;
        case Datentyp.ZEICHENFOLGE:
          map.put(Einstellungen.ZUSATZFELD_PRE + fd.getName(), "abcd");
          break;
      }
    }

    // Liste der Eigenschaften
    DBIterator<Eigenschaft> iteig = Einstellungen.getDBService()
        .createList(Eigenschaft.class);
    while (iteig.hasNext())
    {
      Eigenschaft eig = iteig.next();
      map.put("mitglied_eigenschaft_" + eig.getBezeichnung(), "X");
    }

    // Liste der Eigenschaften einer Eigenschaftengruppe
    DBIterator<EigenschaftGruppe> eigenschaftGruppeIt = Einstellungen
        .getDBService().createList(EigenschaftGruppe.class);
    while (eigenschaftGruppeIt.hasNext())
    {
      EigenschaftGruppe eg = (EigenschaftGruppe) eigenschaftGruppeIt.next();

      String key = "eigenschaften_" + eg.getBezeichnung();
      map.put("mitglied_" + key, "Eigenschaft1, Eigenschaft2");
    }

    // Füge Lesefelder diesem Mitglied-Objekt hinzu.
    LesefeldAuswerter l = new LesefeldAuswerter();
    l.setLesefelderDefinitionsFromDatabase();
    l.setMap(map);
    map.putAll(l.getLesefelderMap());

    return map;
  }

}
