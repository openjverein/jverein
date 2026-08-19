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

public enum Fonts
{
  CarlitoRegular("Carlito-Regular"),
  CarlitoBold("Carlito-Bold"),
  CarlitoItalic("Carlito-Italic"),
  CarlitoBoldItalic("Carlito-BoldItalic"),
  PTSansRegular("PTSans-Regular"),
  PTSansBold("PTSans-Bold"),
  PTSansItalic("PTSans-Italic"),
  PTSansBoldItalic("PTSans-BoldItalic"),
  FreeSans("FreeSans"),
  FreeSansBold("FreeSans-Bold"),
  FreeSansBoldOblique("FreeSans-BoldOblique"),
  FreeSansOblique("FreeSans-Oblique"),

  CourierPrime("Courier Prime"),
  CourierPrimeBold("Courier Prime Bold"),
  CourierPrimeBoldItalic("Courier Prime Bold Italic"),
  CourierPrimeItalic("Courier Prime Italic"),
  LiberationSansBold("LiberationSans-Bold"),
  LiberationSansBoldItalic("LiberationSans-BoldItalic"),
  LiberationSansItalic("LiberationSans-Italic"),
  LiberationSansRegular("LiberationSans-Regular"),
  LiberationSerifBold("LiberationSerif-Bold"),
  LiberationSerifBoldItalic("LiberationSerif-BoldItalic"),
  LiberationSerifItalic("LiberationSerif-Italic"),
  LiberationSerifRegular("LiberationSerif-Regular");

  private final String name;

  Fonts(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public static Fonts getByName(String name)
  {
    for (Fonts font : Fonts.values())
    {
      if (font.getName().equals(name))
      {
        return font;
      }
    }
    return null;
  }

  @Override
  public String toString()
  {
    return getName();
  }
}
