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
  SPENDENBESCHEINIGUNG_DATEINAME("spendenbescheinigung-dateiname",
      "Spendenbescheinigung Dateiname"),
  SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME("spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-Mitglied Dateiname"),
  RECHNUNG_DATEINAME("rechnung-dateiname", "Rechnung Dateiname"),
  RECHNUNG_MITGLIED_DATEINAME("rechnung-mitglied-dateiname",
      "Rechnung-Mitglied Dateiname"),
  MAHNUNG_DATEINAME("mahnung-dateiname", "Mahnung Dateiname"),
  MAHNUNG_MITGLIED("mahnung-mitglied-dateiname", "Mahnung-Mitglied Dateiname"),
  KONTOAUSZUG_DATEINAME("kontoauszug-dateiname", "Kontoauszug Dateiname"),
  KONTOAUSZUG_MITGLIED_DATEINAME("kontoauszug-mitglied-dateiname",
      "Kontoauszug-Mitglied Dateiname"),
  FREIES_FORMULAR_DATEINAME("freies-formular-dateiname", "Freies Formular Dateiname"),
  FREIES_FORMULAR_MITGLIED_DATEINAME("freies-formular-mitglied-dateiname",
      "Freies Formular-Mitglied Dateiname"),
  CT1_AUSGABE_DATEINAME("1ct-ausgabe-dateiname", "1ct Ausgabe Dateiname"),
  PRENOTIFICATION_DATEINAME("pre-notification-dateiname", "Pre-Notification Dateiname"),
  PRENOTIFICATION_MITGLIED_DATEINAME("pre-notification-mitglied-dateiname",
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
