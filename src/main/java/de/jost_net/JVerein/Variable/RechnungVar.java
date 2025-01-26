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
  @Deprecated
  MK_ZAHLUNGSGRUND("mitgliedskonto_zahlungsgrund"), //
  ZAHLUNGSGRUND("rechnung_zahlungsgrund"), //
  @Deprecated
  ZAHLUNGSGRUND1("mitgliedskonto_zahlungsgrund1"), //
  @Deprecated
  ZAHLUNGSGRUND2("mitgliedskonto_zahlungsgrund2"), //
  @Deprecated
  MK_BUCHUNGSDATUM("mitgliedskonto_buchungsdatum"), //
  BUCHUNGSDATUM("rechnung_buchungsdatum"), //
  @Deprecated
  MK_NETTOBETRAG("mitgliedskonto_nettobetrag"), //
  NETTOBETRAG("rechnung_nettobetrag"), //
  @Deprecated
  MK_STEUERSATZ("mitgliedskonto_steuersatz"), //
  STEUERSATZ("rechnung_steuersatz"), //
  @Deprecated
  MK_STEUERBETRAG("mitgliedskonto_steuerbetrag"), //
  STEUERBETRAG("rechnung_steuerbetrag"), //
  @Deprecated
  MK_BETRAG("mitgliedskonto_betrag"), //
  BETRAG("rechnung_betrag"), //
  @Deprecated
  MK_IST("mitgliedskonto_ist"), //
  IST("rechnung_ist"), //
  @Deprecated
  DIFFERENZ("mitgliedskonto_differenz"), //
  @Deprecated
  MK_STAND("mitgliedskonto_stand"), //
  STAND("rechnung_stand"), //
  @Deprecated
  MK_SUMME_OFFEN("mitgliedskonto_summe_offen"), //
  SUMME_OFFEN("rechnung_summe_offen"), //
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
