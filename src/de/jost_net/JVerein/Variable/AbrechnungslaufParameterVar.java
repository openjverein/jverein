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

public enum AbrechnungslaufParameterVar
{
  DATUM_FAELLIGKEIT_F("parameter_faelligkeit_f"),
  DATUM_STICHTAG_F("parameter_stichtag_f"),
  DATUM_EINTRITT_F("parameter_eintritt_f"),
  DATUM_EINGABE_F("parameter_eingabe_f"),
  DATUM_AUSTRITT_F("parameter_austritt_f"),
  ABREACHNUNGSMONAT("parameter_abrechnungsmonat"),
  ZAHLUNGSGRUND("parameter_zahlungsgrund"),
  MODUS("parameter_modus");

  private String name;

  AbrechnungslaufParameterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
