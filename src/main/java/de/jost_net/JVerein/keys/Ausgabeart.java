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

public enum Ausgabeart
{

  PDF(1, "Eine PDF-Datei"),
  PDF_EINZELN(3, "Einzelne PDF-Dateien"),
  MAIL(2, "Mail");

  private final String text;

  private final int key;

  Ausgabeart(int key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public static Ausgabeart getByKey(int key)
  {
    for (Ausgabeart art : Ausgabeart.values())
    {
      if (art.getKey() == key)
      {
        return art;
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
