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

public enum AuswertungMitgliedFilterVar
{
  MITGLIEDSCHAFT("filter_mitgliedschaft"),
  EXT_MITGLIEDSNUMMER("filter_externe_mitgliedsnummer"),
  EIGENSCHAFTEN("filter_eigenschaften"),
  BEITRAGSGRUPPE("filter_beitragsgruppe"),
  ZUSATZFELDER("filter_zusatzfelder"),
  MAIL("filter_mail"),
  GESCHLECHT("filter_geschlecht"),
  STICHTAG_F("filter_geburtsdatum_von_f"),
  DATUM_GEBURT_VON_F("filter_geburtsdatum_von_f"),
  DATUM_GEBURT_BIS_F("filter_geburtsdatum_bis_f"),
  DATUM_EINTRITT_VON_F("filter_eintrittsdatum_von_f"),
  DATUM_EINTRITT_BIS_F("filter_eintrittsdatum_bis_f"),
  DATUM_AUSTRITT_VON_F("filter_austrittsdatum_von_f"),
  DATUM_AUSTRITT_BIS_F("filter_austrittsdatum_bis_f"),
  DATUM_STERBE_VON_F("filter_sterbedatum_von_f"),
  DATUM_STERBE_BIS_F("filter_sterbedatum_bis_f"),
  SORTIERUNG("ausgabe_sortierung"),
  UEBERSCHRIFT("ausgabe_ueberschrift"),
  AUSGABE("ausgabe_ausgabe");

  private String name;

  AuswertungMitgliedFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
