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

public enum AllgemeineVar
{
  AKTUELLESDATUM("aktuellesdatum"), //
  AKTUELLEZEIT("aktuellezeit"), //
  AKTUELLERMONAT("aktuellermonat"), //
  AKTUELLESJAHR("aktuellesjahr"), //
  NURMONAT("nurmonat"), //
  NURTAG("nurtag"), //
  FOLGEJAHR("folgejahr"), //
  FOLGEMONAT("folgemonat"), //
  TAGESDATUM("tagesdatum"), //
  TAGESDATUMTT("tagesdatumtt"), //
  TAGESDATUMMM("tagesdatummm"), //
  TAGESDATUMJJJJ("tagesdatumjjjj"), //
  VORMONAT("vormonat"), //
  VORJAHR("vorjahr"), //
  ZAEHLER("zaehler"), //
  NAME("verein_name"), //
  STRASSE("verein_strasse"), //
  PLZ("verein_plz"), //
  ORT("verein_ort"), //
  STAAT("verein_staat"), //
  ABSENDER("verein_absender"), //
  IBAN("verein_iban"), //
  BIC("verein_bic"), //
  BANK_NAME("verein_bank_name"),
  GLAEUBIGER_ID("verein_glaeubiger_id"), //
  UST_ID("verein_ust_id"), //
  STEUER_NR("verein_steuer_nr"); //

  private String name;

  AllgemeineVar(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
