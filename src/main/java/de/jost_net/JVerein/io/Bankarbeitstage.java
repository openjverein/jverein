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
package de.jost_net.JVerein.io;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;

import de.jost_net.JVerein.util.Holidays;

/**
 * Rechnet mit zwei Kalendern. Der TARGET-Kalender bestimmt, an welchen Tagen
 * SEPA-Zahlungen ausgeführt werden: an allen Tagen außer Wochenenden und
 * TARGET-Feiertagen. Der Bankkalender bestimmt, an welchen Tagen die Bank
 * Einreichungen bearbeitet. Das ist von Bank zu Bank verschieden; hier wird
 * vorsichtshalber angenommen, dass sie es zusätzlich an den übrigen
 * bundesweiten Feiertagen, an Heiligabend und an Silvester nicht tut.
 */
public class Bankarbeitstage
{
  /**
   * Zählt wie {@link #zaehleTargetTage(LocalDate, int)}.
   *
   * @param from
   *          Datum, ab dem gezählt wird; wird auf das Ergebnis gesetzt
   * @param anzahl
   *          Anzahl der TARGET-Tage
   * @return das übergebene Objekt mit dem berechneten Datum
   */
  public Calendar getCalendar(Calendar from, int anzahl)
  {
    LocalDate datum = zaehleTargetTage(toLocalDate(from), anzahl);
    from.set(datum.getYear(), datum.getMonthValue() - 1,
        datum.getDayOfMonth());
    return from;
  }

  /**
   * Zählt vom Starttag die angegebene Anzahl TARGET-Tage weiter. Starttag ist
   * das übergebene Datum, wenn die Bank an diesem Tag arbeitet, sonst ihr
   * nächster Arbeitstag.
   *
   * @param from
   *          Datum, ab dem gezählt wird
   * @param anzahl
   *          Anzahl der TARGET-Tage
   * @return das berechnete Datum
   */
  public static LocalDate zaehleTargetTage(LocalDate from, int anzahl)
  {
    LocalDate datum = naechsterBanktag(from);
    for (int i = 0; i < anzahl; i++)
    {
      datum = naechsterTargetTag(datum.plusDays(1));
    }
    return datum;
  }

  /**
   * @param datum
   *          der zu prüfende Tag
   * @return true, wenn an dem Tag SEPA-Zahlungen ausgeführt werden
   */
  public static boolean isTargetTag(LocalDate datum)
  {
    return !isWochenende(datum) && !Holidays.isTargetHoliday(datum);
  }

  /**
   * @param datum
   *          Tag, ab dem gesucht wird
   * @return das Datum selbst, wenn es ein TARGET-Tag ist, sonst der nächste
   *         TARGET-Tag
   */
  public static LocalDate naechsterTargetTag(LocalDate datum)
  {
    while (!isTargetTag(datum))
    {
      datum = datum.plusDays(1);
    }
    return datum;
  }

  /**
   * @param datum
   *          Tag, ab dem gesucht wird
   * @return das Datum selbst, wenn die Bank an dem Tag Einreichungen
   *         bearbeitet, sonst ihr nächster Arbeitstag
   */
  public static LocalDate naechsterBanktag(LocalDate datum)
  {
    while (isWochenende(datum) || Holidays.isHoliday(datum))
    {
      datum = datum.plusDays(1);
    }
    return datum;
  }

  private static boolean isWochenende(LocalDate datum)
  {
    DayOfWeek tag = datum.getDayOfWeek();
    return tag == DayOfWeek.SATURDAY || tag == DayOfWeek.SUNDAY;
  }

  private static LocalDate toLocalDate(Calendar cal)
  {
    return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH));
  }
}
