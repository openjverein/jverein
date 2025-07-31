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

public enum SollbuchungListeFilterVar
{
  ZAHLER("filter_zahler"),
  MITGLIED("filter_mitglied"),
  ZAHLER_MAIL("filter_zahler_mail"),
  DIFFERENZ("filter_differenz"),
  DIFFERENZ_LIMIT("filter_differenz_limit"),
  OHNE_ABBUCHER("filter_ohne_abbucher"),
  DATUM_VON_F("filter_datum_von_f"),
  DATUM_VON_U("filter_datum_von_u"),
  DATUM_BIS_F("filter_datum_bis_f"),
  DATUM_BIS_U("filter_datum_bis_u");

  private String name;

  SollbuchungListeFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
