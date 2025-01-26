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

public class AfaMode
{

  public static final int MANUELL = 1;

  public static final int AUTO = 2;

  public static final int ANGEPASST = 3;
  

  private int afamode;

  public AfaMode(int key)
  {
    this.afamode = key;
  }

  public int getKey()
  {
    return afamode;
  }

  public String getText()
  {
    return get(afamode);
  }

  public static String get(int key)
  {
    switch (key)
    {
      case MANUELL:
        return "Manuelle AfA";
      case AUTO:
        return "Auto AfA";
      case ANGEPASST:
        return "Angepasste AfA";
      default:
        return null;
    }
  }

  public static ArrayList<AfaMode> getArray()
  {
    ArrayList<AfaMode> ret = new ArrayList<>();
    ret.add(new AfaMode(MANUELL));
    ret.add(new AfaMode(AUTO));
    ret.add(new AfaMode(ANGEPASST));
    return ret;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof AfaMode)
    {
      AfaMode v = (AfaMode) obj;
      return (getKey() == v.getKey());
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return afamode;
  }

  @Override
  public String toString()
  {
    return get(afamode);
  }
}
