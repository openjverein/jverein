package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

final class HolidaysTest
{
  // Deckt alle 13 bundesweiten Einträge der früheren
  // Holidays_bankfeiertage.xml ab (Bundesländer-Einträge wurden nie
  // abgefragt, siehe Bankarbeitstage).
  @ParameterizedTest
  @CsvSource({
      "2026-01-01", // Neujahr
      "2026-05-01", // Tag der Arbeit
      "2026-10-03", // Tag der Deutschen Einheit
      "2026-12-24", // Heiligabend
      "2026-12-25", // 1. Weihnachtstag
      "2026-12-26", // 2. Weihnachtstag
      "2026-12-31", // Silvester
      "1980-06-17", // Tag der Einheit vor 1990
      "2026-04-03", // Karfreitag (Ostern 2026: 5. April)
      "2026-04-05", // Ostersonntag
      "2026-04-06", // Ostermontag
      "2026-05-14", // Christi Himmelfahrt
      "2026-05-24", // Pfingstsonntag
      "2026-05-25"  // Pfingstmontag
  })
  void isHolidayRecognizesHolidays(LocalDate date)
  {
    assertTrue(Holidays.isHoliday(date));
  }

  @ParameterizedTest
  @CsvSource({
      "2026-01-02", // gewöhnlicher Tag
      "1980-10-03", // Tag der Einheit vor 1990 noch nicht am 3.10.
      "1991-06-17"  // Tag der Einheit ab 1990 nicht mehr am 17.6.
  })
  void isHolidayReturnsFalseForNonHolidays(LocalDate date)
  {
    assertFalse(Holidays.isHoliday(date));
  }

  @ParameterizedTest
  @CsvSource({
      "2026, 2026-04-05",
      "2027, 2027-03-28",
      "2028, 2028-04-16",
      "2029, 2029-04-01",
      "2030, 2030-04-21",
      "2031, 2031-04-13",
      "2032, 2032-03-28",
      "2033, 2033-04-17",
      "2034, 2034-04-09",
      "2035, 2035-03-25"
  })
  void calculateEasterSundayForUpcomingYears(int year, LocalDate expectedEasterSunday)
  {
    assertEquals(expectedEasterSunday, Holidays.calculateEasterSunday(year));
  }
}
