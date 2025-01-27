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

import java.rmi.RemoteException;

public enum Staat
{
  //Abchasien (Georgien)
  AB ("ABCHASIEN"),
  AL ("ALBANIEN"),
  AD ("ANDORRA"),
  AM ("ARMENIEN"),
  AZ ("ASERBAIDSCHAN"),
  BY ("BELARUS"),
  BE ("BELGIEN"),
  //Bergkarabach (Armenien/Aserbaidschan)
  AR ("BERGKARABACH"),
  BA ("BOSNIEN UND HERZEGOWINA"),
  BG ("BULGARIEN"),
  DK ("DÄNEMARK"),
  DE ("DEUTSCHLAND"),
  EE ("ESTLAND"),
  FI ("FINNLAND"),
  FR ("FRANKREICH"),
  GE ("GEORGIEN"),
  GR ("GRIECHENLAND"),
  IE ("IRLAND"),
  IS ("ISLAND"),
  IT ("ITALIEN"),
  CA ("KANADA"),
  KZ ("KASACHSTAN"),
  XK ("KOSOVO"),
  HR ("KROATIEN"),
  LV ("LETTLAND"),
  LI ("LIECHTENSTEIN"),
  LT ("LITAUEN"),
  LU ("LUXEMBURG"),
  MT ("MALTA"),
  MD ("MOLDAU, REPUBLIK"),
  MC ("MONACO"),
  ME ("MONTENEGRO"),
  NL ("NIEDERLANDE"),
  MK ("NORDMAZEDONIEN"),
  //Nordzypern (Türkei)
  NC ("NORDZYPERN"),
  NO ("NORWEGEN"),
  AT ("ÖSTERREICH"),
  PL ("POLEN"),
  PT ("PORTUGAL"),
  RO ("RUMÄNIEN"),
  RU ("RUSSISCHE FÖDERATION"),
  SM ("SAN MARINO"),
  SE ("SCHWEDEN"),
  CH ("SCHWEIZ"),
  RS ("SERBIEN"),
  SK ("SLOWAKEI"),
  SI ("SLOWENIEN"),
  ES ("SPANIEN"),
  //Südossetien (Georgien)
  SO ("SÜDOSSETIEN"),
  //Transnistrien (Republik Moldau)
  TN ("TRANSNISTRIEN"),
  CZ ("TSCHECHIEN"),
  TR ("TÜRKEI"),
  UA ("UKRAINE"),
  HU ("UNGARN"),
  VA ("VATIKANSTADT"),
  US ("VEREINIGTE STAATEN VON AMERIKA"),
  GB ("VEREINIGTES KÖNIGREICH"),
  CY ("ZYPERN");

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
    if(key == null)
    {
      return null;
    }
    for (Staat s : Staat.values())
    {
      if (s.getKey().equals(key.toUpperCase()))
      {
        return s;
      }
    }
    return null;
  }
  
  public static Staat getByText(String text)
  {
    if(text == null)
    {
      return null;
    }
    for (Staat s : Staat.values())
    {
      if (s.getText().equals(text.toUpperCase()))
      {
        return s;
      }
    }
    return null;
  }

  public static String getStaat(String code) throws RemoteException
  {
    if (Staat.getByKey(code) != null)
    {
      return Staat.getByKey(code).getText();
    }
    // Wenn der Code nicht vorhanden ist, nehmen wir
    // zur Kompatibilität den Text wie er in der DB Steht
    return code;
  }

  public static String getStaatCode(String code) throws RemoteException
  {
    // Wenn noch das ganze Land drin steht, bestimmen wir den Code
    if (Staat.getByText(code) != null)
      return Staat.getByText(code).getKey();
    return code;
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
