package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.MitgliederImport.ZeilenDurchlauf;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.server.AbstractH2Test;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.util.ProgressMonitor;

class MitgliederImportH2Test extends AbstractH2Test
{

  private static final String DB_HEADER = "name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid";

  private static final String ALLE_SPALTEN = "id;name;vorname;beitragsgruppe;voll_zahlerid;"
      + "abweichende_zahlerid;eintritt;zahlungsweg;geburtsdatum";

  private static final String MITGLIEDERWERTE = ";1.1.2020;2;1.1.1990";

  private MockedConstruction<YesNoDialog> dialoge;

  private boolean dialogAntwort = true;

  private ProgressMonitor monitor;

  @BeforeEach
  void setUp() throws Exception
  {
    monitor = mock(ProgressMonitor.class);

    dialoge = Mockito.mockConstruction(YesNoDialog.class,
        (dialog, context) -> when(dialog.open())
            .thenAnswer(invocation -> dialogAntwort));

    Einstellungen.setEinstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
    Einstellungen.setEinstellung(Property.EINTRITTSDATUMPFLICHT, false);
    Einstellungen.setEinstellung(Property.GEBURTSDATUMPFLICHT, false);
    Einstellungen.setEinstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT,
        false);
    Einstellungen.setEinstellung(Property.INDIVIDUELLEBEITRAEGE, false);
    Einstellungen.setEinstellung(Property.ZAHLUNGSWEG, Zahlungsweg.ÜBERWEISUNG);
    Einstellungen.setEinstellung(Property.ZAHLUNGSRHYTMUS, 12);
    Einstellungen.setEinstellung(Property.BEITRAGSMODEL,
        Beitragsmodel.GLEICHERTERMINFUERALLE.getKey());
    Einstellungen.setEinstellung(Property.SEPAMANDATIDSOURCE,
        SepaMandatIdSource.DBID);
    Einstellungen.setEinstellung(Property.ABWEICHENDEZAHLER, true);
    Einstellungen.setEinstellung(Property.FAMILIENBEITRAG, true);

    beitragsgruppe("Vollzahler", ArtBeitragsart.NORMAL);
    beitragsgruppe("Angehoeriger", ArtBeitragsart.FAMILIE_ANGEHOERIGER);
  }

  @AfterEach
  void tearDown() throws Exception
  {
    if (dialoge != null)
      dialoge.close();
  }

  private void importieren(File file) throws Exception
  {
    new MitgliederImport().doImport(null, null, file, "UTF-8", monitor);
  }

  private void importFehler(File file, String meldung) throws Exception
  {
    importieren(file);
    verify(monitor).log(contains("Import abgebrochen: " + meldung));
  }

  private int anzahlMitglieder() throws Exception
  {
    return db.createList(Mitglied.class).size();
  }

  private Mitglied mitgliedMitVorname(String vorname) throws Exception
  {
    DBIterator<Mitglied> it = db.createList(Mitglied.class);
    it.addFilter("vorname = ?", vorname);

    if (!it.hasNext())
      return null;

    Mitglied result = it.next();
    if (it.hasNext())
      throw new AssertionError("Mehr als ein Mitglied mit Vorname " + vorname);

    return result;
  }

  private Beitragsgruppe gruppe(String bezeichnung) throws Exception
  {
    DBIterator<Beitragsgruppe> it = db.createList(Beitragsgruppe.class);
    it.addFilter("bezeichnung = ?", bezeichnung);

    if (!it.hasNext())
      throw new AssertionError("Beitragsgruppe nicht gefunden: " + bezeichnung);

    Beitragsgruppe result = it.next();
    if (it.hasNext())
      throw new AssertionError(
          "Beitragsgruppe mehrfach vorhanden: " + bezeichnung);

    return result;
  }

  private void beitragsgruppe(String name, ArtBeitragsart art) throws Exception
  {
    Beitragsgruppe gruppe = (Beitragsgruppe) db
        .createObject(Beitragsgruppe.class, null);
    gruppe.setBezeichnung(name);
    gruppe.setBeitragsArt(art.getKey());
    gruppe.setBetrag(new BigDecimal("50.00"));
    gruppe.setHasAltersstaffel(false);
    gruppe.store();
  }

  private void vorhandenesMitglied(String name, String vorname,
      Beitragsgruppe beitragsgruppe) throws Exception
  {
    Mitglied mitglied = (Mitglied) db.createObject(Mitglied.class, null);
    mitglied.setMitgliedstyp(Long.valueOf(Mitgliedstyp.MITGLIED));
    mitglied.setPersonenart("N");
    mitglied.setName(name);
    mitglied.setVorname(vorname);
    mitglied.setGeschlecht("o");

    mitglied.setStrasse("");
    mitglied.setPlz("");
    mitglied.setOrt("");
    mitglied.setStaat("DE");
    mitglied.setAdressierungszusatz("");

    mitglied.setEintritt(new Date());
    mitglied.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
    mitglied.setBeitragsgruppe(beitragsgruppe);
    mitglied.store();
  }

  private File csv(String... zeilen) throws Exception
  {
    File file = File.createTempFile("mitglieder-import-", ".csv");
    file.deleteOnExit();
    Files.write(file.toPath(), List.of(zeilen), StandardCharsets.UTF_8);
    return file;
  }

  private String mitgliedszeile(String id, String name, String vorname,
      String beitragsgruppe, String vollzahlerId, String abweichenderZahlerId)
  {
    return id + ";" + name + ";" + vorname + ";" + beitragsgruppe + ";"
        + vollzahlerId + ";" + abweichenderZahlerId + MITGLIEDERWERTE;
  }

  // ---- Reihenfolge und lokale Verweise ----

  @Test
  void zeileKommtNachDerZeileAufDieSieVerweist() throws Exception
  {
    File file = csv(ALLE_SPALTEN,
        mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "-1", ""),
        mitgliedszeile("-1", "Mustermann", "Max", "Vollzahler", "", ""));

    importieren(file);

    Mitglied max = mitgliedMitVorname("Max");
    Mitglied anna = mitgliedMitVorname("Anna");

    assertNotNull(max, "Vollzahler wurde nicht importiert");
    assertNotNull(anna, "Angehöriger wurde nicht importiert");
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void ohneVerweiseBleibtDieReihenfolgeDerDatei() throws Exception
  {
    File file = csv(ALLE_SPALTEN,
        mitgliedszeile("2", "Mustermann", "Paul", "Vollzahler", "", ""),
        mitgliedszeile("1", "Mustermann", "Pia", "Vollzahler", "", ""));

    List<String> reihenfolge = zeilenDurchlauf(file);

    assertEquals(List.of("2:1", "1:2"), reihenfolge);
  }

  @Test
  void mehrereLokaleVerweiseWerdenAufgeloest() throws Exception
  {
    File file = csv(ALLE_SPALTEN,
        mitgliedszeile("", "Meier", "Anna", "Angehoeriger", "-1", ""),
        mitgliedszeile("-1", "Meier", "Max", "Vollzahler", "", ""),
        mitgliedszeile("", "Wichtig", "Eva", "Angehoeriger", "-2", ""),
        mitgliedszeile("-2", "Wichtig", "Hans", "Vollzahler", "", ""));

    importieren(file);

    Mitglied anna = mitgliedMitVorname("Anna");
    Mitglied max = mitgliedMitVorname("Max");
    Mitglied eva = mitgliedMitVorname("Eva");
    Mitglied hans = mitgliedMitVorname("Hans");

    assertNotNull(anna);
    assertNotNull(max);
    assertNotNull(eva);
    assertNotNull(hans);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    assertEquals(Long.valueOf(hans.getID()), eva.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  private List<String> zeilenDurchlauf(File file) throws Exception
  {
    String name = file.getName();
    int punkt = name.lastIndexOf('.');

    java.util.Properties properties = new java.util.Properties();
    properties.put("separator", ";");
    properties.put("suppressHeaders", "false");
    properties.put("charset", "UTF-8");
    properties.put("fileExtension", name.substring(punkt));

    Class.forName("org.relique.jdbc.csv.CsvDriver");
    try (
        java.sql.Connection connection = java.sql.DriverManager
            .getConnection("jdbc:relique:csv:" + file.getParent(), properties);
        java.sql.Statement statement = connection.createStatement(
            java.sql.ResultSet.TYPE_SCROLL_SENSITIVE,
            java.sql.ResultSet.CONCUR_READ_ONLY);
        java.sql.ResultSet results = statement
            .executeQuery("SELECT * FROM \"" + name.substring(0, punkt) + "\""))
    {
      ZeilenDurchlauf durchlauf = new ZeilenDurchlauf(results);
      List<String> order = new ArrayList<>();
      while (durchlauf.next())
      {
        order.add(results.getString("id") + ":" + results.getRow());
      }
      return order;
    }
  }

  // ---- Vollzahler-IDs ----

  @Test
  void zahleridIstEineDatenbankId() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", gruppe("Vollzahler"));

    importieren(csv(DB_HEADER + ";eintritt;zahlungsweg;geburtsdatum",
        "Mustermann;Anna;Angehoeriger;1;;1.1.2020;2;1.1.1990"));

    Mitglied anna = mitgliedMitVorname("Anna");
    assertNotNull(anna);
    assertEquals(Long.valueOf(1), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void unbekannteDatenbankIdBrichtImportAb() throws Exception
  {
    int vorher = anzahlMitglieder();

    importFehler(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;99;"),
        "Zeile 1: Vollzahler nicht gefunden: 99");

    assertEquals(vorher, anzahlMitglieder());
  }

  @Test
  void fehlendeLokaleIdBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "-2", ""),
            mitgliedszeile("-1", "Mustermann", "Berta", "Vollzahler", "", "")),
        "Zeile 1: Vollzahler für Id -2 nicht gefunden.");
  }

  @Test
  void vollzahlerIdKeinIntegerBrichtImportAb() throws Exception
  {
    importFehler(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;abc;"),
        "Zeile 1 : Vollzahler Id abc ist keine gültige Integer Zahl.");
  }

  @Test
  void mitgliedIdKeinIntegerBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("abc", "Mustermann", "Anna", "Angehoeriger", "",
                "")),
        "Zeile 1: Mitglied ID abc ist keine gültige Integer Zahl.");
  }

  @Test
  void vollzahlerIdFehltBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "", "")),
        "Zeile 1: Vollzahler Id ist nicht gesetzt.");
  }

  @Test
  void doppelteNegativeIdBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("-1", "Mustermann", "Anna", "Angehoeriger", "-1",
                ""),
            mitgliedszeile("-1", "Mustermann", "Berta", "Vollzahler", "", "")),
        "Zeile 1: Mitglied ID -1 wurde bereits bei einem anderen Mitglied verwendet.");
  }

  @Test
  void negativeIdWirdNumerischVerglichen() throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("-01", "Mustermann", "Max", "Vollzahler", "", ""),
        mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "-1", "")));

    Mitglied anna = mitgliedMitVorname("Anna");
    Mitglied max = mitgliedMitVorname("Max");

    assertNotNull(anna);
    assertNotNull(max);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void zweiImporteTeilenKeineNegativenIds() throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("-1", "Mustermann", "Max", "Vollzahler", "", "")));
    verify(monitor, never()).log(anyString());

    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "-1", "")),
        "Zeile 1: Vollzahler für Id -1 nicht gefunden.");
  }

  @Test
  void falscheBeitragsgruppeBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Gibtesnicht", "-1", ""),
            mitgliedszeile("-1", "Mustermann", "Berta", "Vollzahler", "", "")),
        "Zeile 1: Beitragsgruppe nicht gefunden: Gibtesnicht");
  }

  @Test
  void vollzahlerReferenziertVollzahlerBrichtImportAb() throws Exception
  {
    vorhandenesMitglied("Meier", "Hans", gruppe("Vollzahler"));

    importFehler(csv(DB_HEADER, "Mustermann;Anna;Vollzahler;1;"),
        "Zeile 1: Vollzahler Id ist nur bei Beitragsgruppen der Art Familienangehöriger erlaubt: 1");
  }

  @Test
  void nichtMitgliedDarfKeineVollzahlerIdHaben() throws Exception
  {
    int vorher = anzahlMitglieder();

    importFehler(csv(
        "id;name;vorname;adresstyp;beitragsgruppe;voll_zahlerid;eintritt;zahlungsweg;geburtsdatum",
        "-1;Mustermann;Max;1;Vollzahler;;1.1.2020;2;1.1.1990",
        ";Mustermann;Anna;2;Angehoeriger;-1;1.1.2020;2;1.1.1990"),
        "Zeile 2: Für Nicht-Mitglieder ist keine Vollzahler Id erlaubt: -1");

    assertEquals(vorher, anzahlMitglieder(),
        "Rollback muss bereits gespeicherte Mitglieder entfernen");
  }

  @Test
  void fehlerBeiVollzahlerPruefungKommtVorDemErstenSpeichern() throws Exception
  {
    int vorher = anzahlMitglieder();

    importFehler(csv(
        "id;name;vorname;adresstyp;beitragsgruppe;voll_zahlerid;eintritt;zahlungsweg;geburtsdatum",
        "-1;Mustermann;Max;1;Vollzahler;;1.1.2020;2;1.1.1990",
        ";Mustermann;Anna;2;Angehoeriger;-1;1.1.2020;2;1.1.1990"),
        "Zeile 2: Für Nicht-Mitglieder ist keine Vollzahler Id erlaubt: -1");

    assertEquals(vorher, anzahlMitglieder());
  }

  @Test
  void vollzahlerIdBeiFalscherBeitragsgruppeBrichtVorSpeichernAb()
      throws Exception
  {
    int vorher = anzahlMitglieder();

    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("-1", "Mustermann", "Max", "Vollzahler", "", ""),
            mitgliedszeile("", "Mustermann", "Anna", "Vollzahler", "-1", "")),
        "Zeile 2: Vollzahler Id ist nur bei Beitragsgruppen der Art Familienangehöriger erlaubt");

    assertEquals(vorher, anzahlMitglieder());
  }

  // ---- Abweichender Zahler ----

  @Test
  void fehlenderAbweichenderZahlerBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Vollzahler", "", "99")),
        "Zeile 1: Abweichenden Zahler nicht gefunden: 99");
  }

  @Test
  void abweichenderZahlerKeinIntegerBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Vollzahler", "", "abc")),
        "Zeile 1: Abweichender Zahler Id abc ist keine gültige Integer Zahl.");
  }

  @Test
  void abweichenderZahlerLokaleIdNichtGefundenBrichtImportAb() throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("-1", "Mustermann", "Anna", "Vollzahler", "", ""),
            mitgliedszeile("", "Mustermann", "Tom", "Vollzahler", "", "-2")),
        "Zeile 2: Abweichender Zahler für Id -2 nicht gefunden.");
  }

  @Test
  void abweichenderZahlerGefundenImportOk() throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("-1", "Mustermann", "Anna", "Vollzahler", "", ""),
        mitgliedszeile("", "Mustermann", "Tom", "Vollzahler", "", "-1")));

    Mitglied anna = mitgliedMitVorname("Anna");
    Mitglied tom = mitgliedMitVorname("Tom");

    assertNotNull(anna);
    assertNotNull(tom);
    assertEquals(Long.valueOf(anna.getID()), tom.getAbweichenderZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void abweichenderZahlerKannDatenbankIdSein() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", gruppe("Vollzahler"));

    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("", "Mustermann", "Tom", "Vollzahler", "", "1")));

    Mitglied tom = mitgliedMitVorname("Tom");
    assertNotNull(tom);
    assertEquals(Long.valueOf(1), tom.getAbweichenderZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void vollzahlerUndAbweichenderZahlerDuerfenGleichSein() throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("-1", "Mustermann", "Max", "Vollzahler", "", ""),
        mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "-1", "-1")));

    Mitglied max = mitgliedMitVorname("Max");
    Mitglied anna = mitgliedMitVorname("Anna");

    assertNotNull(max);
    assertNotNull(anna);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    assertEquals(Long.valueOf(max.getID()), anna.getAbweichenderZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void abweichenderZahlerKannWeiterUntenStehenderAngehoerigerSein()
      throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("", "Meier", "Tom", "Vollzahler", "", "-3"),
        mitgliedszeile("-2", "Wichtig", "Hans", "Vollzahler", "", ""),
        mitgliedszeile("-3", "Wichtig", "Eva", "Angehoeriger", "-2", "")));

    Mitglied tom = mitgliedMitVorname("Tom");
    Mitglied eva = mitgliedMitVorname("Eva");
    Mitglied hans = mitgliedMitVorname("Hans");

    assertNotNull(tom);
    assertNotNull(eva);
    assertNotNull(hans);
    assertEquals(Long.valueOf(eva.getID()), tom.getAbweichenderZahlerID());
    assertEquals(Long.valueOf(hans.getID()), eva.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void abweichendeZahlerDuerfenSichGegenseitigZahlen() throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("-1", "Mustermann", "Max", "Vollzahler", "", "-2"),
        mitgliedszeile("-2", "Mustermann", "Anna", "Vollzahler", "", "-1")));

    Mitglied max = mitgliedMitVorname("Max");
    Mitglied anna = mitgliedMitVorname("Anna");

    assertNotNull(max);
    assertNotNull(anna);
    assertEquals(Long.valueOf(anna.getID()), max.getAbweichenderZahlerID());
    assertEquals(Long.valueOf(max.getID()), anna.getAbweichenderZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void fehlerMitAbweichendemZahlerMeldetUrspruenglicheCsvZeile()
      throws Exception
  {
    importFehler(
        csv(ALLE_SPALTEN,
            mitgliedszeile("", "Mustermann", "Anna", "Angehoeriger", "-2",
                "99"),
            mitgliedszeile("-2", "Mustermann", "Max", "Vollzahler", "", "")),
        "Zeile 1: Abweichenden Zahler nicht gefunden: 99");
  }

  // ---- Bestehende Mitglieder überschreiben ----

  @Test
  void positiveIdUeberschreibtBestehendesMitglied() throws Exception
  {
    vorhandenesMitglied("Alt", "Paul", gruppe("Vollzahler"));

    importieren(csv("id;name", "1;Neu"));

    Mitglied paul = mitgliedMitVorname("Paul");
    assertNotNull(paul);
    assertEquals("Neu", paul.getName());
    assertEquals(1, anzahlMitglieder());
    assertTrue(dialoge.constructed().size() == 1);
    verify(monitor, never()).log(anyString());
  }

  @Test
  void unbekanntePositiveIdBrichtImportAb() throws Exception
  {
    importFehler(csv("id;name", "99;Neu"),
        "Zeile 1: Mitglied mit ID 99 nicht vorhanden.");
  }

  @Test
  void nurNegativeIdsZeigenKeinenUeberschreibungsdialog() throws Exception
  {
    importieren(csv(ALLE_SPALTEN,
        mitgliedszeile("-1", "Mustermann", "Max", "Vollzahler", "", ""),
        mitgliedszeile("-2", "Mustermann", "Anna", "Angehoeriger", "-1", "")));

    assertEquals(0, dialoge.constructed().size());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void positiveIdsZeigenDenUeberschreibungsdialogEinmal() throws Exception
  {
    vorhandenesMitglied("Alt", "Paul", gruppe("Vollzahler"));
    vorhandenesMitglied("Alt", "Pia", gruppe("Vollzahler"));

    importieren(csv("id;name", "1;Neu", "2;Neu"));

    assertEquals(1, dialoge.constructed().size());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void dialogMitNeinBrichtImportAb() throws Exception
  {
    dialogAntwort = false;
    vorhandenesMitglied("Alt", "Paul", gruppe("Vollzahler"));

    importFehler(csv("id;name", "1;Neu"), "Import abgebrochen");

    Mitglied paul = mitgliedMitVorname("Paul");
    assertNotNull(paul);
    assertEquals("Alt", paul.getName());
  }

  // ---- Weitere Einstellungen und Regressionen ----

  @Test
  void externeMitgliedsnummerAendertLokaleVerweiseNicht() throws Exception
  {
    Einstellungen.setEinstellung(Property.EXTERNEMITGLIEDSNUMMER, true);

    importieren(csv(
        "id;externemitgliedsnummer;name;vorname;beitragsgruppe;voll_zahlerid;eintritt;zahlungsweg;geburtsdatum",
        "-1;M-1;Mustermann;Max;Vollzahler;;1.1.2020;2;1.1.1990",
        "-2;M-2;Mustermann;Anna;Angehoeriger;-1;1.1.2020;2;1.1.1990"));

    Mitglied max = mitgliedMitVorname("Max");
    Mitglied anna = mitgliedMitVorname("Anna");

    assertNotNull(max);
    assertNotNull(anna);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }
}
