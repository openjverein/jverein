package de.jost_net.JVerein.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

final class SEPALaenderTest
{
  @Test
  void getLandLiefertDeutschland()
  {
    SEPALand land = SEPALaender.getLand("DE");
    assertEquals("DE", land.getKennzeichen());
    assertEquals("Deutschland", land.getBezeichnung());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {
      "XX", // unbekanntes Kennzeichen
      "TR"  // Tuerkei hat eine IBAN-Struktur, ist aber kein SEPA-Land
  })
  void getLandLiefertNullBeiUnbekanntemOderNichtSepaKennzeichen(String kennzeichen)
  {
    assertNull(SEPALaender.getLand(kennzeichen));
  }

  @Test
  void getLaenderEnthaeltDeutschland()
  {
    boolean gefunden = SEPALaender.getLaender().stream()
        .anyMatch(land -> "DE".equals(land.getKennzeichen()));
    assertTrue(gefunden);
  }
}
