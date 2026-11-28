/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 * even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 **********************************************************************/
package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DatumTest
{

  @ParameterizedTest
  @CsvSource({
      // Startdatum, Datum, Intervall, erwartet
      "15.01.2026, 15.02.2026, 1, true",
      "15.01.2026, 14.02.2026, 1, false",
      "15.01.2026, 15.04.2026, 3, true",
      "15.01.2026, 15.03.2026, 3, false",
      // Tag des Startdatums existiert im Monat nicht: letzter Tag des Monats
      "29.11.2026, 28.02.2027, 3, true",
      "30.11.2026, 28.02.2027, 3, true",
      "31.01.2027, 28.02.2027, 1, true",
      "31.01.2027, 30.04.2027, 1, true",
      "31.01.2027, 30.04.2027, 3, true",
      "31.01.2028, 29.02.2028, 1, true",
      "29.02.2028, 28.02.2029, 12, true",
      // Tag des Startdatums existiert im Monat: nur dieser Tag ist gültig
      "29.11.2026, 28.05.2027, 3, false",
      "29.11.2026, 29.05.2027, 3, true",
      "31.01.2027, 30.03.2027, 1, false",
      "31.01.2028, 28.02.2028, 1, false",
      "28.02.2027, 31.03.2027, 1, false",
      // Monat liegt nicht im Intervall
      "29.11.2026, 31.01.2027, 3, false",
      // Kein Intervall
      "29.11.2026, 03.01.2027, 0, true" })
  void isImInterval(String startdatum, String datum, int intervall,
      boolean erwartet) throws ParseException
  {
    assertEquals(erwartet, Datum.isImInterval(Datum.toDate(startdatum),
        Datum.toDate(datum), intervall));
  }

  @ParameterizedTest
  @CsvSource({
      // Startdatum, Intervall, erwartete Fälligkeiten
      "15.01.2026, 1, 15.02.2026;15.03.2026;15.04.2026",
      // Beispiel aus Issue #1846
      "29.11.2026, 3, 28.02.2027;29.05.2027;29.08.2027;29.11.2027;29.02.2028",
      "30.11.2026, 3, 28.02.2027;30.05.2027;30.08.2027",
      "31.08.2026, 3, 30.11.2026;28.02.2027;31.05.2027;31.08.2027",
      "31.12.2026, 1, 31.01.2027;28.02.2027;31.03.2027;30.04.2027;31.05.2027",
      "31.12.2027, 1, 31.01.2028;29.02.2028;31.03.2028",
      "31.08.2026, 6, 28.02.2027;31.08.2027;29.02.2028",
      "29.02.2028, 12, 28.02.2029;28.02.2030;28.02.2031;29.02.2032" })
  void faelligkeitenfolge(String startdatum, int intervall, String erwartet)
      throws ParseException
  {
    Date start = Datum.toDate(startdatum);
    String[] folge = erwartet.split(";");

    // Vorwärts, wie im Abrechnungslauf
    Date faelligkeit = start;
    for (String termin : folge)
    {
      faelligkeit = Datum.addInterval(faelligkeit, intervall, start);
      assertEquals(Datum.toDate(termin), faelligkeit);
      assertTrue(Datum.isImInterval(start, faelligkeit, intervall),
          termin + " liegt nicht im Intervall");
    }

    // Rückwärts, wie beim Löschen eines Abrechnungslaufs
    for (int i = folge.length - 2; i >= 0; i--)
    {
      faelligkeit = Datum.subtractInterval(faelligkeit, intervall, start);
      assertEquals(Datum.toDate(folge[i]), faelligkeit);
    }
    assertEquals(start, Datum.subtractInterval(faelligkeit, intervall, start));
  }

  @Test
  void subtractIntervalNichtVorStartdatum() throws ParseException
  {
    Date start = Datum.toDate("29.11.2026");
    assertNull(Datum.subtractInterval(start, 3, start));
  }

  @Test
  void addIntervalNegativ() throws ParseException
  {
    Date start = Datum.toDate("29.11.2026");
    assertEquals(Datum.toDate("29.11.2026"),
        Datum.addInterval(Datum.toDate("28.02.2027"), -3, start));
    assertEquals(Datum.toDate("28.02.2027"),
        Datum.addInterval(Datum.toDate("29.05.2027"), -3, start));
  }

  @Test
  void addIntervalOhneIntervall() throws ParseException
  {
    Date faelligkeit = Datum.toDate("03.01.2027");
    assertEquals(faelligkeit,
        Datum.addInterval(faelligkeit, 0, Datum.toDate("29.11.2026")));
  }
}
