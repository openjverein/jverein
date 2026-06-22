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
package de.jost_net.jverein.variable;

public enum MitgliedListeFilterVar
{
  MITGLIEDSCHAFT("filter_mitgliedschaft"),
  STICHTAG_F("filter_stichtag_f"),
  NAME("filter_name"),
  BEITRAGSGRUPPE("filter_beitragsgruppe"),
  EIGENSCHAFTEN("filter_eigenschaften"),
  ZUSATZFELDER("filter_zusatzfelder"),
  GESCHLECHT("filter_geschlecht"),
  MAIL("filter_mail"),
  MITGLIEDSNUMMER("filter_mitgliedsnummer"),
  EXT_MITGLIEDSNUMMER("filter_externe_mitgliedsnummer"),
  DATUM_GEBURT_VON_F("filter_geburtsdatum_von_f"),
  DATUM_GEBURT_BIS_F("filter_geburtsdatum_bis_f"),
  DATUM_EINTRITT_VON_F("filter_eintrittsdatum_von_f"),
  DATUM_EINTRITT_BIS_F("filter_eintrittsdatum_bis_f"),
  DATUM_AUSTRITT_VON_F("filter_austrittsdatum_von_f"),
  DATUM_AUSTRITT_BIS_F("filter_austrittsdatum_bis_f"),
  DIFFERENZ("filter_differenz"),
  DIFFERENZ_LIMIT("filter_differenz_limit"),
  DATUM_VON_F("filter_datum_von_f"),
  DATUM_BIS_F("filter_datum_bis_f");

  private String name;

  MitgliedListeFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
