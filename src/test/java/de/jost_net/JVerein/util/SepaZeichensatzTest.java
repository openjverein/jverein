package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Prüft {@link SepaZeichensatz} gegen das Verhalten von OBanToos
 * {@code StringLatin.Zeichen.convert}, das sie ersetzt.
 */
final class SepaZeichensatzTest
{
  @ParameterizedTest
  @CsvSource({
      "0123456789, 0123456789",
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ, ABCDEFGHIJKLMNOPQRSTUVWXYZ",
      "abcdefghijklmnopqrstuvwxyz, ABCDEFGHIJKLMNOPQRSTUVWXYZ",
      "' ''()+,-./:?', ' ''()+,-./:?'"
  })
  void erlaubteBasiszeichenBleibenUnverandertOderWerdenGrossGeschrieben(
      String text, String erwartet)
  {
    assertEquals(erwartet, SepaZeichensatz.konvertieren(text));
  }

  @ParameterizedTest
  @CsvSource({
      "Ä, AE", "ä, AE",
      "Ö, OE", "ö, OE",
      "Ü, UE", "ü, UE",
      "ß, SS", "ẞ, SS"
  })
  void deutscheUmlauteUndSzWerdenAlsDigraphAufgeloest(String zeichen, String erwartet)
  {
    assertEquals(erwartet, SepaZeichensatz.konvertieren(zeichen));
  }

  @ParameterizedTest
  @CsvSource({
      "Æ, AE", "æ, AE",
      "Œ, OE", "œ, OE",
      "Å, AA", "å, AA",
      "Ø, OE", "ø, OE",
      "Þ, TH", "þ, TH"
  })
  void weitereDigrapheWerdenAufgeloest(String zeichen, String erwartet)
  {
    assertEquals(erwartet, SepaZeichensatz.konvertieren(zeichen));
  }

  @ParameterizedTest
  @CsvSource({
      "à, A", "é, E", "ç, C", "ñ, N",
      "ô, O", "û, U", "č, C", "š, S"
  })
  void akzentbuchstabenWerdenPerNfdAufBasisbuchstabeReduziert(String zeichen,
      String erwartet)
  {
    assertEquals(erwartet, SepaZeichensatz.konvertieren(zeichen));
  }

  // Diese Buchstaben sind eigenstaendige Glyphen, keine Akzent-Varianten -
  // Unicode-NFD zerlegt sie nicht, sie muessen explizit behandelt werden.
  @ParameterizedTest
  @CsvSource({ "Ł, L", "ł, L", "Ð, D", "ð, D" })
  void buchstabenOhneAkzentzerlegungWerdenUeberSonderfalllisteAufgeloest(
      String zeichen, String erwartet)
  {
    assertEquals(erwartet, SepaZeichensatz.konvertieren(zeichen));
  }

  @ParameterizedTest
  @ValueSource(strings = { "€", "@", "中" })
  void nichtAbbildbareZeichenWerdenZuLeerzeichen(String zeichen)
  {
    assertEquals(" ", SepaZeichensatz.konvertieren(zeichen));
  }

  @Test
  void konvertierenLiefertNullBeiNull()
  {
    assertNull(SepaZeichensatz.konvertieren(null));
  }

  @Test
  void alleinstehendesKombinationszeichenVerschwindetSpurlos()
  {
    assertEquals("", SepaZeichensatz.konvertieren("̂"));
  }

  @Test
  void realistischerSatzMitUmlautenWirdKorrektKonvertiert()
  {
    assertEquals("MUELLER-LUEDENSCHEIDT ZAHLT FUER STRASSE 12 , GROSS-UEMGEBUNG ",
        SepaZeichensatz
            .konvertieren("Müller-Lüdenscheidt zahlt für Straße 12½, Groß-Ümgebung!"));
  }
}
