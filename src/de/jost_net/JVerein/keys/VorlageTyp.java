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

public enum VorlageTyp
{
  SPENDENBESCHEINIGUNG("spendenbescheinigung-dateiname",
      "Spendenbescheinigung Dateiname"),
  SPENDENBESCHEINIGUNG_MITGLIED("spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-Mitglied Dateiname"),
  RECHNUNG("rechnung-dateiname", "Rechnung Dateiname"),
  RECHNUNG_MITGLIED("rechnung-mitglied-dateiname",
      "Rechnung-Mitglied Dateiname"),
  MAHNUNG("mahnung-dateiname", "Mahnung Dateiname"),
  MAHNUNG_MITGLIED("mahnung-mitglied-dateiname", "Mahnung-Mitglied Dateiname"),
  KONTOAUSZUG("kontoauszug-dateiname", "Kontoauszug Dateiname"),
  KONTOAUSZUG_MITGLIED("kontoauszug-mitglied-dateiname",
      "Kontoauszug-Mitglied Dateiname"),
  FREIES_FORMULAR("freies-formular-dateiname", "Freies Formular Dateiname"),
  FREIES_FORMULAR_MITGLIED("freies-formular-mitglied-dateiname",
      "Freies Formular-Mitglied Dateiname"),
  CT1_AUSGABE("1ct-ausgabe-dateiname", "1ct Ausgabe Dateiname"),
  PRENOTIFICATION("pre-notification-dateiname", "Pre-Notification Dateiname"),
  PRENOTIFICATION_MITGLIED("pre-notification-mitglied-dateiname",
      "Pre-Notification-Mitglied Dateiname");

  private final String text;

  private final String key;

  VorlageTyp(String key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public String getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public static VorlageTyp getByKey(String key)
  {
    for (VorlageTyp art : VorlageTyp.values())
    {
      if (art.getKey().matches(key))
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
