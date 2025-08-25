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

public enum BuchungListeFilterVar
{
  KONTO_NR("filter_konto_nr"),
  KONTO_BEZEICHNUNG("filter_konto_bezeichnung"),
  BUCHUNGSART("filter_buchungsart"),
  PROJEKT("filter_projekt"),
  SPLITBUCHUNG("filter_splitbuchung"),
  BETRAG("filter_betrag"),
  DATUM_VON_F("filter_datum_von_f"),
  DATUM_VON_U("filter_datum_von_u"),
  DATUM_BIS_F("filter_datum_bis_f"),
  DATUM_BIS_U("filter_datum_bis_u"),
  UNGEPRUEFT("filter_nur_ungepruefte"),
  ENTHALTENER_TEXT("filter_enthaltener_text"),
  MITGLIED_ZUGEORDNET("filter_mitglied_zugeordnet"),
  MITGLIED_NAME("filter_mitglied_name"),
  STEUER("filter_steuer");

  private String name;

  BuchungListeFilterVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
