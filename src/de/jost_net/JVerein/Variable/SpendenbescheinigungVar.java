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

public enum SpendenbescheinigungVar {
  ANREDE("spendenbescheinigung_anrede"), //
  EMPFAENGER("spendenbescheinigung_empfaenger"), //
  BETRAG("spendenbescheinigung_betrag"), //
  BETRAGINWORTEN("spendenbescheinigung_betraginworten"), //
  BESCHEINIGUNGDATUM("spendenbescheinigung_datum"), //
  SPENDEART("spendenbescheinigung_spendenart"), //
  SPENDEDATUM("spendenbescheinigung_spendedatum"), //
  SPENDEDATUM_ERSTES("spendenbescheinigung_spendedatum_erstes"), //
  SPENDENZEITRAUM("spendenbescheinigung_spendenzeitraum"), //
  ERSATZAUFWENDUNGEN("spendenbescheinigung_ersatzaufwendungen"), //
  ERSATZAUFWENDUNGEN_JA("spendenbescheinigung_ersatzaufwendungen_ja"), //
  ERSATZAUFWENDUNGEN_NEIN("spendenbescheinigung_ersatzaufwendungen_nein"), //
  BUCHUNGSLISTE("spendenbescheinigung_buchungsliste"), //
  BUCHUNGSLISTE_DATEN("spendenbescheinigung_buchungsliste_daten"), //
  BUCHUNGSLISTE_ART("spendenbescheinigung_buchungsliste_art"), //
  BUCHUNGSLISTE_VERZICHT("spendenbescheinigung_buchungsliste_verzicht"), //
  BUCHUNGSLISTE_BETRAG("spendenbescheinigung_buchungsliste_betrag"), //
  BEZEICHNUNGSACHZUWENDUNG("spendenbescheinigung_bezeichnungsachzuwendung"), //
  HERKUNFTSACHZUWENDUNG("spendenbescheinigung_herkunftsachzuwendung"), //
  UNTERLAGENWERTERMITTUNG("spendenbescheinigung_unterlagenwertermittlung"), //
  FINANZAMT("spendenbescheinigung_finanzamt"), //
  STEUER_NR("spendenbescheinigung_steuer_nummmer"), //
  DATUM_BESCHEID("spendenbescheinigung_datum_bescheid"), //
  VERANLAGUNGSZEITRAUM("spendenbescheinigung_veranlagungszeitraum"), //
  ZWECK("spendenbescheinigung_beguenstigter_zweck"), //
  UNTERSCHRIFT("spendenbescheinigung_unterschrift"), //
  ZEILE1("spendenbescheinigung_zeile1"), //
  ZEILE2("spendenbescheinigung_zeile2"), //
  ZEILE3("spendenbescheinigung_zeile3"), //
  ZEILE4("spendenbescheinigung_zeile4"), //
  ZEILE5("spendenbescheinigung_zeile5"), //
  ZEILE6("spendenbescheinigung_zeile6"), //
  ZEILE7("spendenbescheinigung_zeile7"); //

  private String name;

  SpendenbescheinigungVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
