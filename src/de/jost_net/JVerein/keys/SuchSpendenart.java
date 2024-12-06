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

/**
 * Suchspendenart
 */
public enum SuchSpendenart
{
<<<<<<< HEAD:src/de/jost_net/JVerein/keys/SuchSpendenart.java

<<<<<<< HEAD:src/de/jost_net/JVerein/keys/SuchSpendenart.java
  ALLE(1, "Alle"),
  GELDSPENDE(2, "Geldspende"),
  SACHSPENDE(3, "Sachspende"),
  ERSTATTUNGSVERZICHT(4, "Geldspende mit Erstattungsverzicht"),
  GELDSPENDE_ECHT(5, "Geldspende ohne Erstattungsverzicht"),
  SACHSPENDE_ERSTATTUNGSVERZICHT(6, "Sachspende oder Geldspende mit Erstattungsverzicht");
=======
=======
  // LIMIT ist keine Kontoart sondern dient zur Abgrenzung.
  // Ids unter dem Limit werden regulär im Buchungsklassensaldo und Kontensaldo
  // berücksichtigt.
  // Ids über dem Limit werden in beiden Salden ignoriert.
>>>>>>> b3a42199 (Add Kommentar):src/de/jost_net/JVerein/keys/KontoArt.java
  GELD(1, "Geldkonto"),
  ANLAGE(2, "Anlagenkonto"),
  LIMIT(100, "-- Limit --"),
  RUECKLAGE(101, "Rücklagenkonto");
>>>>>>> 5cc29b7b (Endstand):src/de/jost_net/JVerein/keys/KontoArt.java

  private final String text;

  private final int key;

  SuchSpendenart(int key, String text)
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

  public static SuchSpendenart getByKey(int key)
  {
    for (SuchSpendenart sb : SuchSpendenart.values())
    {
      if (sb.getKey() == key)
      {
        return sb;
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
