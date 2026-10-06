/**********************************************************************
 * Copyright (c) by Markus Spann
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
package de.jost_net.JVerein.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Bundesweite deutsche Feiertage sowie Heiligabend und Silvester (als
 * Bankfeiertage, nicht gesetzlich) - Ersatz für die bisher über jollyday
 * geladene Konfiguration, die ohnehin nur auf nationaler Ebene (ohne
 * Bundesländer) abgefragt wurde.
 *
 * @since 4.3.0
 */
public final class Holidays
{
  private Holidays()
  {
    throw new UnsupportedOperationException(String.format(
        "Utility class %s cannot be instantiated", getClass().getSimpleName()));
  }

  /**
   * @param date der zu prüfende Tag.
   * @return true, wenn date ein bundesweiter Feiertag, Heiligabend oder
   *         Silvester ist.
   */
  public static boolean isHoliday(LocalDate date)
  {
    int year = date.getYear();
    Month month = date.getMonth();
    int day = date.getDayOfMonth();

    // Tag der Deutschen Einheit: bis 1990 am 17. Juni, seitdem am 3. Oktober.
    if (month == Month.JANUARY && day == 1
        || month == Month.MAY && day == 1
        || month == Month.JUNE && day == 17 && year >= 1954 && year <= 1990
        || month == Month.OCTOBER && day == 3 && year >= 1990
        || month == Month.DECEMBER
            && (day == 24 || day == 25 || day == 26 || day == 31))
    {
      return true;
    }

    LocalDate easter = calculateEasterSunday(year);
    return date.equals(easter)
        || date.equals(easter.minusDays(2))  // Karfreitag
        || date.equals(easter.plusDays(1))   // Ostermontag
        || date.equals(easter.plusDays(39))  // Christi Himmelfahrt
        || date.equals(easter.plusDays(49))  // Pfingstsonntag
        || date.equals(easter.plusDays(50)); // Pfingstmontag
  }

  /**
   * Berechnet den Ostersonntag per Gaußscher Osterformel. Die Formel
   * rechnet in zwei Schritten: Zuerst wird (über den Mondzyklus) der Tag
   * des auf den 21. März folgenden Vollmonds bestimmt, danach der nächste
   * Sonntag danach - das ist nach kirchlicher Definition Ostern.
   *
   * @see <a href="https://de.wikipedia.org/wiki/Gau%C3%9Fsche_Osterformel">
   *      Gaußsche Osterformel (Wikipedia)</a>
   * @param year das Jahr.
   * @return der Ostersonntag des Jahres.
   */
  static LocalDate calculateEasterSunday(int year)
  {
    // a: Position des Jahres im 19-jährigen Mondzyklus (Metonischer Zyklus) - bestimmt, wie weit der Mond am 21. März
    // schon fortgeschritten ist.
    int a = year % 19;
    // b, c: Position im 4- bzw. 7-jährigen Zyklus von Schaltjahren bzw. Wochentagen - zusammen ergeben sie, zusammen
    // mit n (siehe unten), den Wochentagsversatz.
    int b = year % 4;
    int c = year % 7;
    // k: Jahrhundert, wird für die beiden folgenden Kalenderkorrekturen gebraucht.
    int k = year / 100;
    // p: Korrektur für die Abweichung des Mondzyklus vom echten astronomischen Mondjahr - wird alle paar
    // Jahrhunderte um einen Tag nachjustiert (Mondgleichung).
    int p = (13 + 8 * k) / 25;
    // q: Korrektur für die ausgelassenen gregorianischen Schaltjahre (Sonnengleichung) - jahrhundertweise ein Tag
    // weniger als im julianischen Kalender.
    int q = k / 4;
    // m: Verschiebung des Vollmondtermins im Vergleich zum julianischen Kalender, unter Berücksichtigung von p und q.
    int m = (15 - p + k - q) % 30;
    // n: Verschiebung des Wochentags, ebenfalls unter Berücksichtigung von q.
    int n = (4 + k - q) % 7;
    // d: Anzahl der Tage vom 21. März bis zum (kirchlichen, nicht astronomischen) Vollmond.
    int d = (19 * a + m) % 30;
    // e: Anzahl der Tage vom Vollmond bis zum nächsten Sonntag - das ist dann Ostern.
    int e = (2 * b + 4 * c + 6 * d + n) % 7;
    // Ostern fällt damit auf den (22 + d + e)-ten März (fällt dieser Wert über 31, rutscht das Datum automatisch in
    // den April).
    int day = 22 + d + e;
    // Zwei historisch bedingte Ausnahmen der Formel: Ohne sie würde Ostern in diesen Fällen auf den 26. bzw. 25.
    // April fallen, was im kirchlichen Kalender nicht vorkommen darf (Ostern fällt nie nach dem 25. April) - die
    // Korrektur verschiebt jeweils eine Mondphase (29 Tage) früher auf den 19. bzw. 18. April.
    if (d == 29 && e == 6 || d == 28 && e == 6 && (11 * m + 11) % 30 < 19)
    {
      day -= 7;
    }
    return LocalDate.of(year, Month.MARCH, 1).plusDays(day - 1);
  }
}
