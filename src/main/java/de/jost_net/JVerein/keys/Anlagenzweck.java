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

public enum Anlagenzweck
{
  // Verwendungszweck der Anlage

  NUTZUNGSGEBUNDEN(1, "Nutzungsgebundene Anlage"), // Anlage im ideellen
                                                   // Bereich oder Zweckbetrieb
  ZWECKFREMD_EINGESETZT(2, "Zweckfremde Anlage"); // Anlage in der
                                                  // Vermögensverwaltung oder im
                                                  // wirtschaftlichen
                                                  // Geschäftsbetrieb

  private final String text;

  private final int key;

  Anlagenzweck(int key, String text)
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

  public static Anlagenzweck getByKey(int key)
  {
    for (Anlagenzweck art : Anlagenzweck.values())
    {
      if (art.getKey() == key)
      {
        return art;
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
