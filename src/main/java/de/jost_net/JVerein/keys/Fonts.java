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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.itextpdf.text.FontFactory;

import de.willuhn.logging.Logger;

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

  // Unbekannte Fontnamen, die schon geloggt wurden, damit bei Massenausgaben
  // (z.B. Rechnungslauf) nicht jedes Feld dieselbe Warnung erzeugt
  private static final Set<String> UNBEKANNT_GEMELDET = ConcurrentHashMap
      .newKeySet();

  private static volatile boolean registriert = false;

  public static synchronized void register()
  {
    if (registriert)
    {
      return;
    }
    for (Fonts font : Fonts.values())
    {
      FontFactory.register(font.getResourcePath(), font.getName());
    }
    registriert = true;
  }

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
    // Default Font verwenden
    if (UNBEKANNT_GEMELDET.add(String.valueOf(name)))
    {
      Logger.warn(
          "Schrift '" + name + "' nicht gefunden, verwende Standardschrift.");
    }
    return CarlitoRegular;
  }

  public String getResourcePath()
  {
    return "/fonts/" + getName() + ".ttf";
  }

  @Override
  public String toString()
  {
    return getName();
  }
}
