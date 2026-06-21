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

import java.util.ArrayList;

public class AfaOrt
{

  public static final int ANLAGENBUCHUNGEN = 0;

  public static final int JAHRESABSCHLUSS = 1;

  private int afaort;

  public AfaOrt(int key)
  {
    this.afaort = key;
  }

  public int getKey()
  {
    return afaort;
  }

  public String getText()
  {
    return get(afaort);
  }

  public static String get(int key)
  {
    switch (key)
    {
      case ANLAGENBUCHUNGEN:
        return "Button in Anlagen Buchungen";
      case JAHRESABSCHLUSS:
        return "Checkbox in Jahresabschluss";
      default:
        return null;
    }
  }

  public static ArrayList<AfaOrt> getArray()
  {
    ArrayList<AfaOrt> ret = new ArrayList<>();
    ret.add(new AfaOrt(JAHRESABSCHLUSS));
    ret.add(new AfaOrt(ANLAGENBUCHUNGEN));
    return ret;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof AfaOrt)
    {
      AfaOrt v = (AfaOrt) obj;
      return (getKey() == v.getKey());
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return afaort;
  }

  @Override
  public String toString()
  {
    return get(afaort);
  }
}
