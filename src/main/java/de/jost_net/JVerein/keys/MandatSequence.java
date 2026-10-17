/**********************************************************************
 * Copyright (c) by Markus Spann
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
package de.jost_net.JVerein.keys;

/**
 * SEPA-Mandatssequenz einer Basislastschrift (Ersatz für OBanToos
 * {@code Basislastschrift.MandatSequence}). Der Textwert ({@link #getTxt()})
 * entspricht dem SEPA-Code aus pain.008 und wird unverändert als
 * {@code Lastschrift.mandatSequence} persistiert.
 *
 * @since 4.3.0
 */
public enum MandatSequence
{

  FRST("Erste Lastschrift"),
  RCUR("Folgelastschrift"),
  FNAL("Letzte Lastschrift"),
  OOFF("Einmallastschrift");

  private final String anzeige;

  MandatSequence(String anzeige)
  {
    this.anzeige = anzeige;
  }

  public String getTxt()
  {
    return name();
  }

  @Override
  public String toString()
  {
    return anzeige;
  }
}
