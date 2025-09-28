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
 */
package de.jost_net.JVerein.gui.formatter;

import de.willuhn.jameica.gui.formatter.Formatter;

public class IBANFormatter implements Formatter
{
  /**
   * Gruppiert eine IBAN in Gruppen zu je 4 Zeichen und schreibt die ersten
   * beiden Buchstaben (Laenderkennzeichen) gross.
   * 
   * @param s
   *          die IBAN.
   * @return die formatierte Darstellung.
   */
  public String format(Object o)
  {
    if (!(o instanceof String))
    {
      return "";
    }
    String s = (String) o;

    // Wenn es falsche Zeien enthält, nicht formatieren
    if (!s.trim().matches("^[a-zA-Z]{2}[0-9 ]+$"))
    {
      return s;
    }
    return s.replaceAll(" ", "").replaceAll("(.{4})", "$0 ").trim()
        .toUpperCase();
  }
}
