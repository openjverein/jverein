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

public enum ZahlerVar
{
  ADRESSIERUNGSZUSATZ("zahler_adressierungszusatz"), //
  MITGLIEDSTYP("zahler_adresstyp"), //
  ANREDE("zahler_anrede"), //
  ANREDE_DU("zahler_anrede_du"), //
  ANREDE_FOERMLICH("zahler_anrede_foermlich"), //
  AUSTRITT("zahler_austritt"), //
  AUSTRITT_F("zahler_austritt_f"), //
  BEITRAGSGRUPPE_ARBEITSEINSATZ_BETRAG("zahler_arbeitseinsatz_betrag"), //
  BEITRAGSGRUPPE_ARBEITSEINSATZ_STUNDEN("zahler_arbeitseinsatz_stunden"), //
  BEITRAGSGRUPPE_BEZEICHNUNG("zahler_beitragsgruppe_bezeichnung"), //
  BEITRAGSGRUPPE_BETRAG("zahler_beitragsgruppe_betrag"), //
  BEITRAGSGRUPPE_ID("zahler_beitragsgruppe_id"), //
  MANDATDATUM("zahler_mandatdatum"), //
  MANDATDATUM_F("zahler_mandatdatum_f"), //
  MANDATID("zahler_mandatid"), //
  BIC("zahler_bic"), //
  EINTRITT("zahler_eintritt"), //
  EINTRITT_F("zahler_eintritt_f"), //
  EINGABEDATUM("zahler_eingabedatum"), //
  EINGABEDATUM_F("zahler_eingabedatum_f"), //
  EMPFAENGER("zahler_empfaenger"), //
  EMAIL("zahler_email"), //
  EXTERNE_MITGLIEDSNUMMER("zahler_externe_mitgliedsnummer"), //
  GEBURTSDATUM("zahler_geburtsdatum"), //
  GEBURTSDATUM_F("zahler_geburtsdatum_f"), //
  GESCHLECHT("zahler_geschlecht"), //
  HANDY("zahler_handy"), //
  IBAN("zahler_iban"), //
  IBANMASKIERT("zahler_iban_maskiert"), //
  ID("zahler_id"), //
  INDIVIDUELLERBEITRAG("zahler_individuellerbeitrag"), //
  BANKNAME("zahler_bankname"), //
  KONTO_KONTOINHABER("zahler_konto_kontoinhaber"), //
  KONTOINHABER("zahler_kontoinhaber"), //
  KUENDIGUNG("zahler_kuendigung"), //
  LETZTEAENDERUNG("zahler_letzte.aenderung"), //
  NAME("zahler_name"), //
  NAMEVORNAME("zahler_namevorname"), //
  ORT("zahler_ort"), //
  PERSONENART("zahler_personenart"), //
  PLZ("zahler_plz"), //
  STAAT("zahler_staat"), //
  STERBETAG("zahler_sterbetag"), //
  STRASSE("zahler_strasse"), //
  TELEFONDIENSTLICH("zahler_telefon_dienstlich"), //
  TELEFONPRIVAT("zahler_telefon_privat"), //
  TITEL("zahler_titel"), //
  VERMERK1("zahler_vermerk1"), //
  VERMERK2("zahler_vermerk2"), //
  VORNAME("zahler_vorname"), //
  VORNAMENAME("zahler_vornamename"), //
  ZAHLUNGSRHYTHMUS("zahler_zahlungsrhythmus"), //
  ZAHLUNGSTERMIN("zahler_zahlungstermin"), //
  ZAHLUNGSWEG("zahler_zahlungsweg"), //
  ZAHLERID("zahler_zahlerid"),
  ALTERNATIVER_ZAHLER("zahler_alternativer_zahlerid");

  private String name;

  ZahlerVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
