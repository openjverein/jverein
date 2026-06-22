/**********************************************************************
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
 **********************************************************************/
package de.jost_net.jverein.variable;

public enum WiedervorlageListeFilterVar
{
  NAME("filter_name"),
  VERMERK("filter_vermerk"),
  DATUM_VON_F("filter_datum_von_f"),
  DATUM_BIS_F("filter_datum_bis_f"),
  DATUM_ERLEDIGUNG_VON_F("filter_erledigung_von_f"),
  DATUM_ERLEDIGUNG_BIS_F("filter_erledigung_bis_f"),
  OHNE_ERLEDIGUNG("filter_ohne_erledigung");

  private String name;

  WiedervorlageListeFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
