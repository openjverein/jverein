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

public enum Kontoart
{
  // LIMIT ist keine Kontoart sondern dient zur Abgrenzung.
  // Ids unter dem Limit werden regulär im Buchungsklassensaldo und Kontensaldo
  // berücksichtigt.
  // Ids über dem Limit werden in beiden Salden ignoriert.
  // Ebenfals is LIMIT_RUECKLAGE keine Kontoart, sondern grenzt die
  // Rücklagekonten ab.
  GELD(1, "Geldkonto", "Geldvermögen"),
  ANLAGE(2, "Anlagenkonto", "Anlagevermögen"),
  SCHULDEN(3, "Fremdkapital"),
  LIMIT(100, "-- Limit --"),
  RUECKLAGE_ZWECK_GEBUNDEN(101, "Zweckgebundene Rücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_BETRIEBSMITTEL(102, "Betriebsmittelrücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_INVESTITION(103, "Investitionsrücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_INSTANDHALTUNG(104, "Instandhaltungsrücklage nach § 62 Abs. 1 Nr. 1 AO"),
  RUECKLAGE_WIEDERBESCHAFFUNG(105, "Wiederbeschaffungsrücklage nach § 62 Abs. 1 Nr. 2 AO"),
  RUECKLAGE_FREI(106, "Freie Rücklage nach § 62 Abs. 1 Nr. 3 AO"),
  RUECKLAGE_ERWERB(107, "Rücklage für Gesellschaftsrechte nach § 62 Abs. 1 Nr. 4 AO"),
  VERMOEGEN(108, "Vermögen nach § 62 Abs. 3 und 4 AO"),
  RUECKLAGE_SONSTIG(109, "Sonstige Rücklagen und Vermögen"),
  LIMIT_RUECKLAGE(200, "-- Limit Rücklage --"),
  VERBINDLICHKEITEN(201, "Verbindlichkeiten"),
  FORDERUNGEN(202, "Forderungen");

  private final String text;

  private final int key;

  private String textVermoegen;
  
  Kontoart(int key, String text)
  {
    this(key, text, text);
  }

  Kontoart(int key, String text, String textVermoegen)
  {
    this.key = key;
    this.text = text;
    this.textVermoegen = textVermoegen;
  }

  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public String getTextVermoegen()
  {
    return textVermoegen;
  }

  public static Kontoart getByKey(int key)
  {
    for (Kontoart art : Kontoart.values())
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
