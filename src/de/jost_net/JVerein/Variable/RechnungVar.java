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

public enum RechnungVar
{
  ZAHLUNGSGRUND("mitgliedskonto_zahlungsgrund"), //
  ZAHLUNGSGRUND1("mitgliedskonto_zahlungsgrund1"), //
  ZAHLUNGSGRUND2("mitgliedskonto_zahlungsgrund2"), //
  BUCHUNGSDATUM("mitgliedskonto_buchungsdatum"), //
  NETTOBETRAG("mitgliedskonto_nettobetrag"), //
  STEUERSATZ("mitgliedskonto_steuersatz"), //
  STEUERBETRAG("mitgliedskonto_steuerbetrag"), //
  BETRAG("mitgliedskonto_betrag"), //
  IST("mitgliedskonto_ist"), //
  DIFFERENZ("mitgliedskonto_differenz"), //
  STAND("mitgliedskonto_stand"), //
  SUMME_OFFEN("mitgliedskonto_summe_offen"), //
  QRCODE_SUMME("qrcode_summe"), //
  QRCODE_INTRO("qrcode_intro"),
  DATUM("rechnung_datum"),
  NUMMER("rechnung_nummer"),
  ANREDE("rechnung_anrede"),
  TITEL("rechnung_titel"),
  NAME("rechnung_name"),
  VORNAME("rechnung_vorname"),
  STRASSE("rechnung_strasse"),
  ADRESSIERUNGSZUSATZ("rechnung_adressierungszusatz"),
  PLZ("rechnung_plz"),
  ORT("rechnung_ort"),
  STAAT("rechnung_staat"),
  GESCHLECHT("rechnung_geschlecht"),
  ANREDE_DU("rechnung_anrede_du"), //
  ANREDE_FOERMLICH("rechnung_anrede_foermlich"), //
  PERSONENART("rechnung_personenart"), //
  MANDATID("rechnung_mandatid"), //
  MANDATDATUM("rechnung_mandatdatum"), //
  BIC("rechnung_bic"), //
  IBAN("rechnung_iban"), //
  IBANMASKIERT("rechnung_ibanmaskiert"), //
  EMPFAENGER("rechnung_empfaenger"), 
  ZAHLUNGSWEGTEXT("rechnung_zahlungsweg_text");

  private String name;

  RechnungVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
