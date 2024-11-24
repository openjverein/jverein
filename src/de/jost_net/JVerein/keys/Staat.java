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

public enum Staat
{
  AD ("ANDORRA"),
  AL ("ALBANIEN"),
  AT ("�STER�REICH"),
  BA ("BOSNIEN UND HERZEGOWINA"),
  BE ("BELGIEN"),
  BG ("BULGARIEN"),
  BY ("BELARUS"),
  CH ("SCHWEIZ"),
  CY ("ZYPERN"),
  CZ ("TSCHECHIEN"),
  DE ("DEUTSCHLAND"),
  DK ("D�NEMARK"),
  EE ("ESTLAND"),
  ES ("SPANIEN"),
  FI ("FINNLAND"),
  FR ("FRANKREICH"),
  GB ("VEREINIGTES K�NIGREICH"),
  GR ("GRIECHENLAND"),
  HR ("KROATIEN"),
  HU ("UNGARN"),
  IE ("IRLAND"),
  IS ("ISLAND"),
  IT ("ITALIEN"),
  LI ("LIECHTENSTEIN"),
  LT ("LITAUEN"),
  LU ("LUXEM�BURG"),
  LV ("LETTLAND"),
  MC ("MONACO"),
  MD ("MOLDAU, REPUBLIK"),
  ME ("MONTENEGRO"),
  MK ("NORDMAZEDONIEN"),
  MT ("MALTA"),
  NL ("NIEDER�LANDE"),
  NO ("NORWEGEN"),
  PL ("POLEN"),
  PT ("PORTUGAL"),
  RO ("RUM�NIEN"),
  RS ("SERBIEN"),
  RU ("RUSSISCHE F�DERATION"),
  SE ("SCHWEDEN"),
  SI ("SLOWENIEN"),
  SK ("SLOWAKEI"),
  SM ("SAN MARINO"),
  TR ("T�RKEI"),
  UA ("UKRAINE"),
  VA ("VATIKANSTADT");

  private final String text;

  Staat(String text)
  {
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  public String getKey()
  {
    return this.name();
  }

  public static Staat getByKey(String key)
  {
    for (Staat s : Staat.values())
    {
      if (s.getKey().equals(key))
      {
        return s;
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
