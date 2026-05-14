/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.keys;

/**
 * Eigenschaftenauswahl
 */
public enum Eigenschaftenauswahl
{

  KEINE(1, "Keine Auswahl"),
  ALLE(2, "Alles ausgewählt"),
  AKTIVE_MITGLIEDER(3, "Aktive Mitglieder"),
  INAKTIVE_MITGLIEDER(4, "Inaktive Mitglieder"),
  NICHT_MITGLIEDER(5, "Nicht-Mitglieder"),
  MITGLIEDER(6, "Mitglieder"),
  INAKTIVE_MITGLIEDER_NICHT_MITGLIEDER(7,
      "Inaktive Mitglieder und Nicht-Mitglieder"),
  AKTIVE_MITGLIEDER_NICHT_MITGLIEDER(8,
      "Aktive Mitglieder und Nicht-Mitglieder");

  private final String text;

  private final int key;

  Eigenschaftenauswahl(int key, String text)
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

  public static Eigenschaftenauswahl getByKey(int key)
  {
    for (Eigenschaftenauswahl ara : Eigenschaftenauswahl.values())
    {
      if (ara.getKey() == key)
      {
        return ara;
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
