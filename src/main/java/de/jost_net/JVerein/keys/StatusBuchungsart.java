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

/**
 * Art der Buchungsart
 */
public class StatusBuchungsart
{
  public static final int ACTIVE = 0;

  public static final int INACTIVE = 1;

  public static final int AUTO = 2;

  private int art;

  public StatusBuchungsart(int key)
  {
    this.art = key;
  }

  public int getKey()
  {
    return art;
  }

  public String getText()
  {
    return get(art);
  }

  public static String get(int key)
  {
    switch (key)
    {
      case ACTIVE:
        return "Aktiv";
      case INACTIVE:
        return "Deaktiviert";
      case AUTO:
        return "Auto";
      default:
        return null;
    }
  }

  public static ArrayList<StatusBuchungsart> getArray()
  {
    ArrayList<StatusBuchungsart> ret = new ArrayList<>();
    ret.add(new StatusBuchungsart(ACTIVE));
    ret.add(new StatusBuchungsart(INACTIVE));
    ret.add(new StatusBuchungsart(AUTO));
    return ret;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof StatusBuchungsart)
    {
      StatusBuchungsart v = (StatusBuchungsart) obj;
      return (getKey() == v.getKey());
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return art;
  }

  @Override
  public String toString()
  {
    return get(art);
  }
}
