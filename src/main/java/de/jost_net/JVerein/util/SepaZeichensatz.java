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

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Transliteriert Text in den für SEPA-Zahlungsverkehrsdateien zulässigen
 * Zeichensatz (Großbuchstaben A-Z, Ziffern, Leerzeichen und die Satzzeichen
 * {@code '()+,-./:?}). Ersatz für OBanToos {@code StringLatin.Zeichen}.
 * <p>
 * Deutsche Umlaute/ß und einige weitere Buchstaben mit feststehender
 * SEPA-Schreibweise (z.B. Ä zu AE, ß zu SS) werden explizit aufgelöst, alle
 * übrigen Buchstaben mit diakritischen Zeichen werden per Unicode-NFD-
 * Normalisierung auf ihren Basisbuchstaben reduziert (z.B. é zu E). Zeichen,
 * die danach immer noch außerhalb des zulässigen Zeichensatzes liegen, werden
 * durch ein Leerzeichen ersetzt.
 *
 * @since 4.3.0
 */
public final class SepaZeichensatz
{
  private static final Pattern KOMBINATIONSZEICHEN = Pattern.compile("\\p{M}");

  private static final Pattern ERLAUBT = Pattern
      .compile("[A-Z0-9 '()+,\\-./:?]");

  private SepaZeichensatz()
  {
    throw new UnsupportedOperationException(String.format(
        "Utility class %s cannot be instantiated", getClass().getSimpleName()));
  }

  /**
   * @param text
   *          der zu konvertierende Text.
   * @return der Text im SEPA-Zeichensatz.
   */
  public static String konvertieren(String text)
  {
    if (text == null)
    {
      return null;
    }
    StringBuilder ergebnis = new StringBuilder();
    for (int i = 0; i < text.length(); i++)
    {
      char zeichen = text.charAt(i);
      String ersatz = sonderfall(zeichen);
      if (ersatz == null)
      {
        ersatz = KOMBINATIONSZEICHEN.matcher(
            Normalizer.normalize(String.valueOf(zeichen), Normalizer.Form.NFD))
            .replaceAll("").toUpperCase();
      }
      if (ersatz.isEmpty())
      {
        // Alleinstehendes Kombinationszeichen, z.B. U+0302 - verschwindet
        // spurlos.
        continue;
      }
      if (ersatz.length() == 1 && !ERLAUBT.matcher(ersatz).matches())
      {
        ersatz = " ";
      }
      ergebnis.append(ersatz);
    }
    return ergebnis.toString();
  }

  /**
   * Buchstaben, die keine reine Akzent-Variante eines Basisbuchstabens sind
   * (Unicode-NFD zerlegt sie nicht in Basisbuchstabe + Kombinationszeichen),
   * oder die SEPA-weit eine feststehende, vom Basisbuchstaben abweichende
   * Schreibweise haben (z.B. Umlaute als Digraph).
   *
   * @param zeichen
   *          ein einzelnes Zeichen.
   * @return die SEPA-konforme Ersetzung oder NULL, wenn keine Sonderregel
   *         greift und die allgemeine NFD-Normalisierung angewendet werden
   *         soll.
   */
  private static String sonderfall(char zeichen)
  {
    return switch (zeichen)
    {
      case 'Ä', 'ä' -> "AE";
      case 'Ö', 'ö' -> "OE";
      case 'Ü', 'ü' -> "UE";
      case 'ß', 'ẞ' -> "SS";
      case 'Æ', 'æ' -> "AE";
      case 'Œ', 'œ' -> "OE";
      case 'Å', 'å' -> "AA";
      case 'Ø', 'ø' -> "OE";
      case 'Þ', 'þ' -> "TH";
      case 'Ð', 'ð', 'Đ', 'đ' -> "D";
      case 'Ħ', 'ħ' -> "H";
      case 'ĸ' -> "K";
      case 'Ŀ', 'ŀ', 'Ł', 'ł' -> "L";
      case 'Ŋ', 'ŋ', 'ŉ' -> "N";
      case 'Ŧ', 'ŧ' -> "T";
      case 'Ə', 'ə' -> "E";
      case 'Ʒ', 'ʒ' -> "Y";
      case 'Ǥ', 'ǥ' -> "G";
      case 'Ǯ', 'ǯ' -> "Y";
      case 'Ǽ', 'ǽ' -> "AE";
      case 'Ǿ', 'ǿ' -> "O";
      case 'ª' -> "A";
      default -> null;
    };
  }

}
