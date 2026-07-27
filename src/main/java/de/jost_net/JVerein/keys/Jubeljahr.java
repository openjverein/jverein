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

import java.util.Calendar;

/**
 * Jahre +/-2 um aktuelles Jahr
 */
public class Jubeljahr implements KeyEnum
{

  private int jjahr;

  public Jubeljahr(int key)
  {
    jjahr = key;
  }

  public static Jubeljahr[] getList()
  {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -2);
    Jubeljahr[] jahre = new Jubeljahr[5];
    for (int i = 0; i < 5; i++)
    {
      jahre[i] = new Jubeljahr(cal.get(Calendar.YEAR));
      cal.add(Calendar.YEAR, 1);
    }
    return jahre;
  }

  @Override
  public int getKey()
  {
    return jjahr;
  }

  public String getText()
  {
    return toString();
  }

  @Override
  public String toString()
  {
    return ((Integer) jjahr).toString();
  }
}
