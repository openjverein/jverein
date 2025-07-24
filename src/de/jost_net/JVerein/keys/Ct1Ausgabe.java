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
package de.jost_net.JVerein.keys;

/**
 * Abrechnungsausgabe
 */
public enum Ct1Ausgabe
{

  SEPA_DATEI(1, "Datei"),
  HIBISCUS(2, "Hibiscus");

  private final String text;

  private final int key;

  Ct1Ausgabe(int key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public static Ct1Ausgabe getByKey(int key)
  {
    for (Ct1Ausgabe ara : Ct1Ausgabe.values())
    {
      if (ara.getKey() == key)
      {
        return ara;
      }
    }
    return null;
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
