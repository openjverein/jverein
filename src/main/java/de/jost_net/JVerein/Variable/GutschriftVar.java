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

public enum GutschriftVar
{
  ABRECHNUNGSLAUF_NR("gutschrift_abrechnungslauf_nr"), //
  ABRECHNUNGSLAUF_DATUM("gutschrift_abrechnungslauf_datum"), //
  ABRECHNUNGSLAUF_FAELLIGKEIT("gutschrift_abrechnungslauf_faelligkeit"), //
  ANREDE_DU("gutschrift_anrede_du"), //
  ANREDE_FOERMLICH("gutschrift_anrede_foermlich"), //
  PERSONENART("gutschrift_personenart"), //
  GESCHLECHT("gutschrift_geschlecht"), //
  ANREDE("gutschrift_anrede"), //
  TITEL("gutschrift_titel"), //
  NAME("gutschrift_name"), //
  VORNAME("gutschrift_vorname"), //
  STRASSE("gutschrift_strasse"), //
  ADRESSSIERUNGSZUSATZ("gutschrift_adressierungszusatz"), //
  PLZ("gutschrift_plz"), //
  ORT("gutschrift_ort"), //
  STAAT("gutschrift_staat"), //
  EMAIL("gutschrift_email"), //
  MANDATID("gutschrift_mandatid"), //
  MANDATDATUM("gutschrift_mandatdatum"), //
  BIC("gutschrift_bic"), //
  IBAN("gutschrift_iban"), //
  IBANMASKIERT("gutschrift_ibanmaskiert"), //
  VERWENDUNGSZWECK("gutschrift_verwendungszweck"), //
  BETRAG("gutschrift_betrag"), //
  EMPFAENGER("gutschrift_empfaenger"),
  BANKNAME("gutschrift_bankname");

  private String name;

  GutschriftVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
