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

public enum DateinameTyp
{
  SPENDENBESCHEINIGUNG(1, "Spendenbescheinigung"),
  SPENDENBESCHEINIGUNG_MITGLIED(2, "Spendenbescheinigung-Mitglied"),
  RECHNUNG(3, "Rechnung"),
  RECHNUNG_MITGLIED(4, "Rechnung-Mitglied"),
  MAHNUNG(5, "Mahnung"),
  MAHNUNG_MITGLIED(6, "Mahnung-Mitglied"),
  KONTOAUSZUG(7, "Kontoauszug"),
  KONTOAUSZUG_MITGLIED(8, "Kontoauszug-Mitglied"),
  FREIES_FORMULAR(9, "Freies Formular"),
  FREIES_FORMULAR_MITGLIED(10, "Freies Formular-Mitglied"),
  CT1_AUSGABE(11, "1ct Ausgabe"),
  PRENOTIFICATION(12, "Pre-Notification"),
  PRENOTIFICATION_MITGLIED(13, "Pre-Notification-Mitglied");

  private final String text;

  private final int key;

  DateinameTyp(int key, String text)
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

  public static DateinameTyp getByKey(int key)
  {
    for (DateinameTyp art : DateinameTyp.values())
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
