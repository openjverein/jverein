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
package de.jost_net.JVerein.util;

import java.text.DecimalFormat;

/**
 * Decimal Format, bei dem auch ein String übergeben werden kann. Dieser wird
 * direkt wieder zurückgegeben. So kann der Formatter aufgerufen werden, auch
 * wenn die Zahl bereits formatiert ist.
 */
public class JVDecimalFormat extends DecimalFormat
{
  private static final long serialVersionUID = 5948836553985834880L;

  public JVDecimalFormat(String pattern)
  {
    super(pattern);
  }

  public String format(String str)
  {
    return str;
  }
}
