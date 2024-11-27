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
  AM ("ARMENIEN"),
  AT ("ÖSTER­REICH"),
  AZ ("ASERBAIDSCHAN"),
  BA ("BOSNIEN UND HERZEGOWINA"),
  BE ("BELGIEN"),
  BG ("BULGARIEN"),
  BY ("BELARUS"),
  CA ("KANADA"),
  CH ("SCHWEIZ"),
  CY ("ZYPERN"),
  CZ ("TSCHECHIEN"),
  DE ("DEUTSCHLAND"),
  DK ("DÄNEMARK"),
  EE ("ESTLAND"),
  ES ("SPANIEN"),
  FI ("FINNLAND"),
  FR ("FRANKREICH"),
  GB ("VEREINIGTES KÖNIGREICH"),
  GE ("GEORGIEN"),
  GR ("GRIECHENLAND"),
  HR ("KROATIEN"),
  HU ("UNGARN"),
  IE ("IRLAND"),
  IS ("ISLAND"),
  IT ("ITALIEN"),
  KZ ("KASACHSTAN"),
  LI ("LIECHTENSTEIN"),
  LT ("LITAUEN"),
  LU ("LUXEM­BURG"),
  LV ("LETTLAND"),
  MC ("MONACO"),
  MD ("MOLDAU, REPUBLIK"),
  ME ("MONTENEGRO"),
  MK ("NORDMAZEDONIEN"),
  MT ("MALTA"),
  NL ("NIEDER­LANDE"),
  NO ("NORWEGEN"),
  PL ("POLEN"),
  PT ("PORTUGAL"),
  RO ("RUMÄNIEN"),
  RS ("SERBIEN"),
  RU ("RUSSISCHE FÖDERATION"),
  SE ("SCHWEDEN"),
  SI ("SLOWENIEN"),
  SK ("SLOWAKEI"),
  SM ("SAN MARINO"),
  TR ("TÜRKEI"),
  UA ("UKRAINE"),
  US ("VEREINIGTE STAATEN VON AMERIKA"),
  VA ("VATIKANSTADT"),
  // Nicht universell anerkannte Länder
  XK ("KOSOVO"),
  // Transnistrien (Republik Moldau)
  TN ("TRANSNISTRIEN"),
  // Südossetien (Georgien)
  SO ("SÜDOSSETIEN"),
  // Abchasien (Georgien)
  AB ("ABCHASIEN"),
  // Nordzypern (Türkei)
  NC ("NORDZYPERN"),
  // Bergkarabach (Armenien/Aserbaidschan)
  AR ("BERGKARABACH");

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
