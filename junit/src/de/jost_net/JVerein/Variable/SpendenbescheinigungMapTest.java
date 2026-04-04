package de.jost_net.JVerein.Variable;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpendenbescheinigungMapTest {
    private static java.util.Date date(int year, int month, int day) {
        return java.sql.Date.valueOf(java.time.LocalDate.of(year, month, day));
    }

    @Test
    void getMapLiefertGrundannahmenFuerGeldspende() throws Exception {
        Date spendenDatum = date(2024, 1, 1);
        Date bescheinigungsDatum = date(2025, 1, 1);
        Date bescheidDatum = date(2024, 6, 1);
        Date veranlagungVon = date(2022, 1, 1);
        Date veranlagungBis = date(2024, 1, 1);

        Buchung buchung = mock(Buchung.class);
        when(buchung.getVerzicht()).thenReturn(Boolean.FALSE);

        Spendenbescheinigung spb = mock(Spendenbescheinigung.class);
        when(spb.getID()).thenReturn("1");
        when(spb.getBuchungen()).thenReturn(List.of(buchung));
        when(spb.getSpendenart()).thenReturn(Spendenart.GELDSPENDE);
        when(spb.getBetrag()).thenReturn(123.45);
        when(spb.getZeile1()).thenReturn("Herr");
        when(spb.getZeile2()).thenReturn("Max Mustermann");
        when(spb.getZeile3()).thenReturn("Musterstr. 1");
        when(spb.getZeile4()).thenReturn("12345 Musterstadt");
        when(spb.getZeile5()).thenReturn("");
        when(spb.getZeile6()).thenReturn("");
        when(spb.getZeile7()).thenReturn("");
        when(spb.getBescheinigungsdatum()).thenReturn(bescheinigungsDatum);
        when(spb.getSpendedatum()).thenReturn(spendenDatum);
        when(spb.getZeitraumBis()).thenReturn(spendenDatum);
        when(spb.getBezeichnungSachzuwendung()).thenReturn("");
        when(spb.getUnterlagenWertermittlung()).thenReturn(Boolean.FALSE);
        when(spb.getHerkunftSpende()).thenReturn(HerkunftSpende.PRIVATVERMOEGEN);
        when(spb.isEchteGeldspende()).thenReturn(Boolean.FALSE);

        try (MockedStatic<Einstellungen> einstellungen = Mockito
                .mockStatic(Einstellungen.class)) {
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.MITGLIEDSBETRAEGE))
                    .thenReturn(Boolean.FALSE);
            einstellungen.when(() -> Einstellungen.getEinstellung(Property.FINANZAMT))
                    .thenReturn("Test-Finanzamt");
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.STEUERNUMMER))
                    .thenReturn("12/345/67890");
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.BESCHEIDDATUM))
                    .thenReturn(bescheidDatum);
            einstellungen.when(() -> Einstellungen.getEinstellung(Property.VORLAEUFIG))
                    .thenReturn(Boolean.FALSE);
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.VERANLAGUNGVON))
                    .thenReturn(veranlagungVon);
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.VERANLAGUNGBIS))
                    .thenReturn(veranlagungBis);
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.BEGUENSTIGTERZWECK))
                    .thenReturn("Foerderung des Sports");
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.UNTERSCHRIFT))
                    .thenReturn(null);
            einstellungen.when(
                            () -> Einstellungen.getEinstellung(Property.UNTERSCHRIFTDRUCKEN))
                    .thenReturn(Boolean.FALSE);
            einstellungen.when(() -> Einstellungen
                            .getEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART))
                    .thenReturn(Boolean.FALSE);

            Map<String, Object> map = new SpendenbescheinigungMap().getMap(spb, null);

            assertNotNull(map);
            assertEquals("Herr Max Mustermann",
                    map.get(SpendenbescheinigungVar.ANREDE.getName()));
            assertEquals("123,45",
                    map.get(SpendenbescheinigungVar.BETRAG.getName()));
            assertEquals("-einhundertdreiundzwanzig Euro fünfundvierzig-",
                    map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()));
            assertEquals("Geldzuwendungen",
                    map.get(SpendenbescheinigungVar.SPENDEART.getName()));
            assertEquals("Nein",
                    map.get(SpendenbescheinigungVar.ERSATZAUFWENDUNGEN.getName()));
            assertEquals("Test-Finanzamt",
                    map.get(SpendenbescheinigungVar.FINANZAMT.getName()));
            assertEquals("Foerderung des Sports",
                    map.get(SpendenbescheinigungVar.ZWECK.getName()));
            assertEquals("2022 bis 2024",
                    map.get(SpendenbescheinigungVar.VERANLAGUNGSZEITRAUM.getName()));
        }
    }

    @Test
    void getMapLiefertDummyMapWennKeineIdVorhanden()
            throws Exception {
        Spendenbescheinigung spb = mock(Spendenbescheinigung.class);
        when(spb.getID()).thenReturn(null);

        try (MockedStatic<Einstellungen> einstellungen = Mockito
                .mockStatic(Einstellungen.class)) {
            einstellungen.when(() -> Einstellungen.getEinstellung(Property.VORLAEUFIG))
                    .thenReturn(Boolean.FALSE);

            Map<String, Object> map = new SpendenbescheinigungMap().getMap(spb, null);

            assertEquals("Herr Willi Wichtig",
                    map.get(SpendenbescheinigungVar.ANREDE.getName()));
            assertEquals("dreihundert",
                    map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()));
            assertEquals("Testhausen",
                    map.get(SpendenbescheinigungVar.FINANZAMT.getName()));
        }
    }
}
