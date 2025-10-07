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

public enum SpendenbescheinigungListeFilterVar
{
  DATUM_BESCHEINIGUNG_VON_F("filter_bescheinigungdatum_von_f"),
  DATUM_BESCHEINIGUNG_BIS_F("filter_bescheinigungdatum_bis_f"),
  DATUM_SPENDE_VON_F("filter_spendedatum_von_f"),
  DATUM_SPENDE_BIS_F("filter_spendedatum_bis_f"),
  ZEILE2("filter_zeile2"),
  MAIL("filter_mail"),
  SPENDENART("filter_spendeart");

  private String name;

  SpendenbescheinigungListeFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
