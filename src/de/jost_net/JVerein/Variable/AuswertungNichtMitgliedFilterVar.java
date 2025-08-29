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

public enum AuswertungNichtMitgliedFilterVar
{
  MITGLIEDSTYP("filter_mitgliedstyp"),
  EIGENSCHAFTEN("filter_eigenschaften"),
  ZUSATZFELDER("filter_zusatzfelder"),
  MAIL("filter_mail"),
  GESCHLECHT("filter_geschlecht"),
  DATUM_GEBURT_VON_F("filter_geburtsdatum_von_f"),
  DATUM_GEBURT_VON_U("filter_geburtsdatum_von_u"),
  DATUM_GEBURT_BIS_F("filter_geburtsdatum_bis_f"),
  DATUM_GEBURT_BIS_U("filter_geburtsdatum_bis_u"),
  SORTIERUNG("ausgabe_sortierung"),
  UEBERSCHRIFT("ausgabe_ueberschrift"),
  AUSGABE("ausgabe_ausgabe");

  private String name;

  AuswertungNichtMitgliedFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
