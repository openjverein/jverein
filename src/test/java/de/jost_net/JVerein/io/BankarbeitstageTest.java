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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BankarbeitstageTest
{
  @ParameterizedTest
  @CsvSource({
      "2026-11-27, 1, 2026-11-30", // Freitag: Wochenende zählt nicht
      "2026-11-28, 1, 2026-12-01", // Samstag: Start am Montag
      "2026-11-29, 1, 2026-12-01", // Sonntag: Start am Montag
      "2026-11-28, 0, 2026-11-30", // Samstag ohne Vorlauf: nächster Banktag
      "2026-11-30, 0, 2026-11-30", // Banktag ohne Vorlauf bleibt
      "2026-04-02, 1, 2026-04-07", // Karfreitag und Ostermontag zählen nicht
      "2026-04-03, 1, 2026-04-08", // Start an Karfreitag: ab Dienstag, 7.4.
      "2026-04-30, 1, 2026-05-04", // 1. Mai zählt nicht
      "2026-05-13, 1, 2026-05-14", // Christi Himmelfahrt ist TARGET-Tag
      "2026-05-14, 1, 2026-05-18", // Start an Christi Himmelfahrt: ab Freitag
      "2026-05-22, 1, 2026-05-25", // Pfingstmontag ist TARGET-Tag
      "2025-10-02, 1, 2025-10-03", // Tag der Deutschen Einheit ist TARGET-Tag
      "2026-12-22, 2, 2026-12-24", // Heiligabend ist TARGET-Tag
      "2026-12-24, 2, 2026-12-30", // Start an Heiligabend: ab Montag, 28.12.
      "2026-12-30, 2, 2027-01-04", // Silvester zählt, Neujahr nicht
      "2013-12-30, 1, 2013-12-31"  // Silvester ist TARGET-Tag
  })
  void zaehleTargetTageZaehltAbBanktag(LocalDate start, int anzahl,
      LocalDate erwartet)
  {
    assertEquals(erwartet, Bankarbeitstage.zaehleTargetTage(start, anzahl));
  }

  @Test
  void getCalendarZaehltUndBehaeltUhrzeitUndObjekt()
  {
    Calendar cal = new GregorianCalendar(2026, Calendar.DECEMBER, 30, 15, 30,
        45);

    Calendar ergebnis = new Bankarbeitstage().getCalendar(cal, 2);

    assertSame(cal, ergebnis);
    assertEquals(new GregorianCalendar(2027, Calendar.JANUARY, 4, 15, 30, 45),
        ergebnis);
  }

  @ParameterizedTest
  @CsvSource({
      "2026-11-30", // Montag
      "2026-05-14", // Christi Himmelfahrt
      "2026-05-25", // Pfingstmontag
      "2025-10-03", // Tag der Deutschen Einheit
      "2026-12-24", // Heiligabend
      "2026-12-31"  // Silvester
  })
  void isTargetTagErkenntTargetTage(LocalDate datum)
  {
    assertTrue(Bankarbeitstage.isTargetTag(datum));
  }

  @ParameterizedTest
  @CsvSource({
      "2026-11-28", // Samstag
      "2026-11-29", // Sonntag
      "2026-01-01", // Neujahr
      "2026-04-03", // Karfreitag
      "2026-04-06", // Ostermontag
      "2026-05-01", // 1. Mai
      "2026-12-25", // 1. Weihnachtstag
      "2025-12-26"  // 2. Weihnachtstag
  })
  void isTargetTagErkenntGeschlosseneTage(LocalDate datum)
  {
    assertFalse(Bankarbeitstage.isTargetTag(datum));
  }

  @ParameterizedTest
  @CsvSource({
      "2026-11-30, 2026-11-30", // TARGET-Tag bleibt
      "2026-05-14, 2026-05-14", // Christi Himmelfahrt bleibt
      "2026-12-24, 2026-12-24", // Heiligabend bleibt
      "2026-11-28, 2026-11-30", // Samstag -> Montag
      "2026-04-03, 2026-04-07", // Karfreitag -> Dienstag nach Ostern
      "2026-12-25, 2026-12-28"  // 1. Weihnachtstag -> Montag
  })
  void naechsterTargetTag(LocalDate datum, LocalDate erwartet)
  {
    assertEquals(erwartet, Bankarbeitstage.naechsterTargetTag(datum));
  }

  @ParameterizedTest
  @CsvSource({
      "2026-11-30, 2026-11-30", // Banktag bleibt
      "2026-11-28, 2026-11-30", // Samstag -> Montag
      "2026-05-14, 2026-05-15", // Christi Himmelfahrt -> Freitag
      "2026-05-23, 2026-05-26", // Pfingstsamstag -> Dienstag
      "2025-10-03, 2025-10-06", // Tag der Deutschen Einheit -> Montag
      "2026-12-24, 2026-12-28", // Heiligabend -> Montag
      "2026-12-31, 2027-01-04"  // Silvester -> Montag
  })
  void naechsterBanktag(LocalDate datum, LocalDate erwartet)
  {
    assertEquals(erwartet, Bankarbeitstage.naechsterBanktag(datum));
  }
}
