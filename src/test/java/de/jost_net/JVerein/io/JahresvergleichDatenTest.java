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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.jost_net.JVerein.io.JahresvergleichDaten.Abschnitt;
import de.jost_net.JVerein.io.JahresvergleichDaten.Kennzahl;
import de.jost_net.JVerein.io.JahresvergleichDaten.Vergleichszeile;
import de.jost_net.JVerein.io.JahresvergleichDaten.Werte;

class JahresvergleichDatenTest
{
  @Test
  void vergleichtAlleGruppenAusBeidenJahrenUndSetztFehlendeWerteAufNull()
  {
    JahresvergleichDaten daten = new JahresvergleichDaten();
    Map<String, String> namen = Map.of("1", "Mitgliedsbeiträge",
        "2", "Veranstaltungen");
    Map<String, Werte> aktuell = new LinkedHashMap<>();
    Werte aktuelleBeitraege = new Werte();
    aktuelleBeitraege.add(Kennzahl.EINNAHMEN, new BigDecimal("120.00"));
    aktuell.put("1", aktuelleBeitraege);
    Map<String, Werte> vorjahr = new LinkedHashMap<>();
    Werte veranstaltungen = new Werte();
    veranstaltungen.add(Kennzahl.AUSGABEN, new BigDecimal("-45.00"));
    vorjahr.put("2", veranstaltungen);

    daten.addBereich(Abschnitt.BUCHUNGSART, namen, aktuell, vorjahr);

    List<Vergleichszeile> zeilen = daten.getZeilen(Abschnitt.BUCHUNGSART);
    assertEquals(4, zeilen.size());
    Vergleichszeile beitraege = zeilen.stream()
        .filter(z -> z.getName().equals("Mitgliedsbeiträge"))
        .filter(z -> z.getKennzahl() == Kennzahl.EINNAHMEN).findFirst()
        .orElseThrow();
    assertEquals(new BigDecimal("120.00"), beitraege.getAktuell());
    assertEquals(BigDecimal.ZERO, beitraege.getVorjahr());
    assertEquals(new BigDecimal("120.00"), beitraege.getVeraenderung());
    assertEquals("–", beitraege.getVeraenderungProzent());

    Vergleichszeile ausgaben = zeilen.stream()
        .filter(z -> z.getName().equals("Veranstaltungen"))
        .filter(z -> z.getKennzahl() == Kennzahl.AUSGABEN).findFirst()
        .orElseThrow();
    assertEquals(BigDecimal.ZERO, ausgaben.getAktuell());
    assertEquals(new BigDecimal("-45.00"), ausgaben.getVorjahr());
    assertEquals(new BigDecimal("45.00"), ausgaben.getVeraenderung());
    assertEquals("-100,00 %", ausgaben.getVeraenderungProzent());
  }

  @Test
  void berechnetAbsoluteUndProzentualeVeraenderung()
  {
    Vergleichszeile zeile = new Vergleichszeile(Abschnitt.GESAMT, "Gesamt",
        Kennzahl.EINNAHMEN, new BigDecimal("150.00"),
        new BigDecimal("100.00"));

    assertEquals(new BigDecimal("50.00"), zeile.getVeraenderung());
    assertEquals("50,00 %", zeile.getVeraenderungProzent());
    assertTrue(zeile.getAktuell().compareTo(zeile.getVorjahr()) > 0);
  }

  @Test
  void kannBuchungsartenAufEineSaldozeileReduzieren()
  {
    JahresvergleichDaten daten = new JahresvergleichDaten();
    Map<String, String> namen = Map.of("1", "Mitgliedsbeiträge");
    Map<String, Werte> aktuell = new LinkedHashMap<>();
    Werte aktuelleWerte = new Werte();
    aktuelleWerte.add(Kennzahl.EINNAHMEN, new BigDecimal("120.00"));
    aktuelleWerte.add(Kennzahl.AUSGABEN, new BigDecimal("-20.00"));
    aktuell.put("1", aktuelleWerte);

    daten.addBereich(Abschnitt.BUCHUNGSART, namen, aktuell, Map.of(), false);

    List<Vergleichszeile> zeilen = daten.getZeilen(Abschnitt.BUCHUNGSART);
    assertEquals(1, zeilen.size());
    assertEquals(Kennzahl.SALDO, zeilen.get(0).getKennzahl());
    assertEquals("Saldo", zeilen.get(0).getKennzahl().getBezeichnung());
    assertEquals(new BigDecimal("100.00"), zeilen.get(0).getAktuell());
  }

  @Test
  void gruppiertBuchungsartenUndFuegtBuchungsklassenZwischensummeEin()
  {
    JahresvergleichDaten daten = new JahresvergleichDaten();
    Werte aktuelleArt = new Werte();
    aktuelleArt.add(Kennzahl.EINNAHMEN, new BigDecimal("100.00"));
    Werte vorherigeArt = new Werte();
    vorherigeArt.add(Kennzahl.EINNAHMEN, new BigDecimal("75.00"));
    vorherigeArt.add(Kennzahl.AUSGABEN, new BigDecimal("-25.00"));

    daten.addBuchungsklassenBereich(Abschnitt.BUCHUNGSART,
        Map.of("klasse-1", "Buchungsklasse 1"),
        Map.of("klasse-1", Map.of("art-1", "Buchungsart 1")),
        Map.of("klasse-1", Map.of("art-1", aktuelleArt)),
        Map.of("klasse-1", Map.of("art-1", vorherigeArt)), false);

    List<Vergleichszeile> zeilen = daten.getZeilen(Abschnitt.BUCHUNGSART);
    assertEquals(3, zeilen.size());
    assertEquals(JahresvergleichDaten.Zeilentyp.GRUPPENKOPF,
        zeilen.get(0).getTyp());
    assertEquals("Buchungsklasse 1", zeilen.get(0).getName());
    assertEquals("Saldo", zeilen.get(1).getKennzahl().getBezeichnung());
    assertEquals(JahresvergleichDaten.Zeilentyp.ZWISCHENSUMME,
        zeilen.get(2).getTyp());
    assertEquals(new BigDecimal("100.00"), zeilen.get(2).getAktuell());
    assertEquals(new BigDecimal("50.00"), zeilen.get(2).getVorjahr());
  }

  @Test
  void pdfBeruecksichtigtNurAusgewaehlteBerichtsabschnitte()
  {
    JahresvergleichPDF pdf = new JahresvergleichPDF(new JahresvergleichDaten(),
        EnumSet.of(Abschnitt.PROJEKT));

    assertTrue(pdf.istAbschnittAusgewaehlt(Abschnitt.PROJEKT));
    assertFalse(pdf.istAbschnittAusgewaehlt(Abschnitt.GESAMT));
    assertFalse(pdf.istAbschnittAusgewaehlt(Abschnitt.BUCHUNGSART));
  }
}
