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
 * Ausrichtung von Formularfeldern
 */
public enum Ausrichtung
{

  LINKS("Links", 0),
  RECHTS("Rechts", 1),
  MITTE("Mitte", 2);

  private final int key;

  private String text;

  Ausrichtung(String text, int key)
  {
    this.text = text;
    this.key = key;
  }

  public int getKey()
  {
    return key;
  }

  public static Ausrichtung getByKey(int key)
  {
    for (Ausrichtung a : Ausrichtung.values())
    {
      if (a.getKey() == key)
      {
        return a;
      }
    }
    return null;
  }

  @Override
  public String toString()
  {
    return text;
  }
}
