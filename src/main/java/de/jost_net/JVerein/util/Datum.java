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
package de.jost_net.JVerein.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Altermodel;

public class Datum
{

  /**
   * Berechnung der Differenz in Monaten zwischen 2 Daten. Die Tagesangaben
   * bleiben unberücksichtigt.
   * 
   * @param datum1
   *          von-Datum
   * @param datum2
   *          bis-Datum
   * @return Anzahl Monate
   */
  public static int getDifferenzInMonaten(Date datum1, Date datum2)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(datum2);
    int diff = cal.get(Calendar.YEAR) * 12;
    diff = diff + cal.get(Calendar.MONTH);
    cal.setTime(datum1);
    diff = diff - (cal.get(Calendar.YEAR) * 12);
    diff = diff - cal.get(Calendar.MONTH);
    return diff;
  }

  /**
   * Prüft, ob ein Datum auf einem Termin der Intervallfolge liegt, die mit dem
   * Startdatum beginnt. Gibt es den Tag des Startdatums im Monat des Datums
   * nicht (29., 30. oder 31.), ist der letzte Tag dieses Monats der Termin.
   *
   * @param datum1
   *          Startdatum
   * @param datum2
   *          zu prüfendes Datum
   * @param intervall
   *          Intervall in Monaten, 0 = kein Intervall
   * @return true, wenn das Datum im Intervall liegt
   */
  public static boolean isImInterval(Date datum1, Date datum2, int intervall)
  {
    if (intervall == 0)
    {
      return true;
    }
    Calendar cal1 = Calendar.getInstance();
    cal1.setTime(datum1);
    Calendar cal2 = Calendar.getInstance();
    cal2.setTime(datum2);
    int tag = Math.min(cal1.get(Calendar.DAY_OF_MONTH),
        cal2.getActualMaximum(Calendar.DAY_OF_MONTH));
    if (cal2.get(Calendar.DAY_OF_MONTH) != tag)
    {
      return false;
    }
    return getDifferenzInMonaten(datum1, datum2) % intervall == 0;
  }

  /**
   * Verschiebt eine Fälligkeit um ein Intervall. Gerechnet wird vom Startdatum
   * aus (Startdatum + n × Intervall) und nicht von der letzten Fälligkeit,
   * damit der Tag des Startdatums erhalten bleibt. Gibt es diesen Tag im
   * Zielmonat nicht, wird auf den letzten Tag des Monats gekappt. Beispiel
   * monatlich ab 31.01.: 28.02., 31.03., 30.04.
   *
   * @param letztefaelligkeit
   *          Fälligkeit, die verschoben wird
   * @param intervall
   *          Intervall in Monaten, negativ für die vorherige Fälligkeit, 0 =
   *          kein Intervall
   * @param startdatum
   *          Startdatum der Intervallfolge
   * @return die verschobene Fälligkeit
   */
  public static Date addInterval(Date letztefaelligkeit, int intervall,
      Date startdatum)
  {
    if (intervall == 0)
    {
      return letztefaelligkeit;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(startdatum);
    // Calendar.add() kappt auf den letzten Tag des Zielmonats
    cal.add(Calendar.MONTH,
        getDifferenzInMonaten(startdatum, letztefaelligkeit) + intervall);
    return cal.getTime();
  }

  public static Date subtractInterval(Date letztefaelligkeit, int intervall,
      Date startdatum)
  {
    Date datum = addInterval(letztefaelligkeit, -intervall, startdatum);
    if (datum.getTime() < startdatum.getTime())
    {
      return null;
    }
    return datum;
  }

  public static Date addTage(Date datum, int tage)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(datum);
    cal.add(Calendar.DAY_OF_MONTH, tage);
    return cal.getTime();
  }

  /**
   * Addiert (oder subtrahiert bei negativem Wert) Jahre zu einem Datum.
   *
   * @param datum das Ausgangsdatum.
   * @param jahre die Anzahl der zu addierenden Jahre.
   * @return das um jahre verschobene Datum.
   */
  public static Date addJahre(Date datum, int jahre)
  {
    return Date.from(datum.toInstant().atZone(ZoneId.systemDefault())
        .plusYears(jahre).toInstant());
  }

  public static Date toDate(String value) throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    return sdf.parse(value);
  }

  /**
   * Gibt das heutige Datum zurück. (Ohne Zeitanteil)
   */
  public final static Date getHeute()
  {
    return getDatumOhneZeit(new Date());
  }

  public final static Date getDatumOhneZeit(Date datum)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(datum);
    cal.set(Calendar.HOUR, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  /**
   * Gibt einen Leerstring aus, falls der Text null ist.
   * 
   * @return der Text oder Leerstring - niemals null.
   */
  public final static String formatDate(Date d)
  {
    return d == null ? "" : new JVDateFormatTTMMJJJJ().format(d);
  }

  public final static Integer getAlter(Date geburtstag, int altersModel)
  {
    if (Altermodel.JAHRES_ENDE == altersModel)
      return getAlter(geburtstag, getJahresEnde());
    if (Altermodel.JAHRES_START == altersModel)
      return getAlter(geburtstag, getJahresStart());
    return getAlter(geburtstag);
  }

  private static Date getJahresStart()
  {
    Calendar datum = Calendar.getInstance();
    int jahr = datum.get(Calendar.YEAR);
    datum.clear();
    datum.set(jahr, Calendar.JANUARY, 1);
    return datum.getTime();
  }

  private static Date getJahresEnde()
  {
    Calendar datum = Calendar.getInstance();
    int jahr = datum.get(Calendar.YEAR);
    datum.clear();
    datum.set(jahr, Calendar.DECEMBER, 31);
    return datum.getTime();
  }

  public final static Integer getAlter(Date geburtstag)
  {
    return getAlter(geburtstag, new Date());
  }

  /**
   * Gibt das Alter zum übergebenen Referenzdatum zurück oder <code>null</code>,
   * falls kein Geburtstag übergeben wurde oder das Alter kleiner als eins ist.
   * 
   * @param geburtstag
   * @return alter
   */
  public final static Integer getAlter(Date geburtstag, Date referenzDatum)
  {
    if (null == geburtstag)
      return null;
    if (Einstellungen.NODATE == geburtstag)
      return null;
    Calendar heute = Calendar.getInstance();
    heute.setTime(referenzDatum);
    Calendar birthDate = Calendar.getInstance();
    birthDate.setTime(geburtstag);
    int alter = heute.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
    if (heute.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH)
        || (heute.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH) && heute
            .get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH)))
    {
      --alter;
    }
    if (alter < 0)
      return null;
    return Integer.valueOf(alter);
  }

  public final static int getJahr(Date datum)
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(datum);
    return cal.get(Calendar.YEAR);
  }
}
