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
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.server.AbstractH2Test;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.util.ProgressMonitor;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Zusatzfelder;

class MitgliederImportIntegrationTest extends AbstractH2Test
{

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

    createBeitragsgruppe("Vollzahler", ArtBeitragsart.NORMAL);
    createBeitragsgruppe("Angehoeriger", ArtBeitragsart.FAMILIE_ANGEHOERIGER);
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
    return Einstellungen.getDBService().createList(Mitglied.class).size();
  }

  private Mitglied getMitgliedMitVorname(String vorname) throws Exception
  {
    DBIterator<Mitglied> it = Einstellungen.getDBService()
        .createList(Mitglied.class);
    it.addFilter("vorname = ?", vorname);

    if (!it.hasNext())
      return null;

    Mitglied result = it.next();
    if (it.hasNext())
      throw new AssertionError("Mehr als ein Mitglied mit Vorname " + vorname);

    return result;
  }

  private Beitragsgruppe getBeitragsgruppe(String bezeichnung) throws Exception
  {
    DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    it.addFilter("bezeichnung = ?", bezeichnung);

    if (!it.hasNext())
      throw new AssertionError("Beitragsgruppe nicht gefunden: " + bezeichnung);

    Beitragsgruppe result = it.next();
    if (it.hasNext())
      throw new AssertionError(
          "Beitragsgruppe mehrfach vorhanden: " + bezeichnung);

    return result;
  }

  private void createBeitragsgruppe(String name, ArtBeitragsart art)
      throws Exception
  {
    Beitragsgruppe gruppe = (Beitragsgruppe) Einstellungen.getDBService()
        .createObject(Beitragsgruppe.class, null);
    gruppe.setBezeichnung(name);
    gruppe.setBeitragsArt(art.getKey());
    gruppe.setBetrag(new BigDecimal("50.00"));
    gruppe.setHasAltersstaffel(false);
    gruppe.store();
  }

  private void createMitglied(String name, String vorname,
      Beitragsgruppe beitragsgruppe) throws Exception
  {
    Mitglied mitglied = (Mitglied) Einstellungen.getDBService()
        .createObject(Mitglied.class, null);
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

  private EigenschaftGruppe createEigenschaftGruppe(String name)
      throws Exception
  {
    EigenschaftGruppe gruppe = (EigenschaftGruppe) Einstellungen.getDBService()
        .createObject(EigenschaftGruppe.class, null);

    gruppe.setBezeichnung(name);
    gruppe.setName(name);
    gruppe.setPflicht(false);
    gruppe.setMax1(false);
    gruppe.store();

    return gruppe;
  }

  private Eigenschaft createEigenschaft(String bezeichnung,
      EigenschaftGruppe gruppe) throws Exception
  {
    Eigenschaft eigenschaft = (Eigenschaft) Einstellungen.getDBService()
        .createObject(Eigenschaft.class, null);

    eigenschaft.setBezeichnung(bezeichnung);
    eigenschaft.setName(bezeichnung);
    eigenschaft.setEigenschaftGruppe(Long.valueOf(gruppe.getID()));
    eigenschaft.store();

    return eigenschaft;
  }

  private Felddefinition createFelddefinition(String name, int datentyp)
      throws Exception
  {
    Felddefinition feld = (Felddefinition) Einstellungen.getDBService()
        .createObject(Felddefinition.class, null);

    feld.setName(name);
    feld.setLabel(name);
    feld.setDatentyp(datentyp);
    feld.setLaenge(255);
    feld.store();

    return feld;
  }

  private int anzahlEigenschaften(Mitglied mitglied, Eigenschaft eigenschaft)
      throws Exception
  {
    DBIterator<Eigenschaften> it = Einstellungen.getDBService()
        .createList(Eigenschaften.class);
    it.addFilter("mitglied = ?", mitglied.getID());
    it.addFilter("eigenschaft = ?", eigenschaft.getID());
    return it.size();
  }

  private int anzahlZusatzfelder(Mitglied mitglied, Felddefinition feld)
      throws Exception
  {
    DBIterator<Zusatzfelder> it = Einstellungen.getDBService()
        .createList(Zusatzfelder.class);
    it.addFilter("mitglied = ?", mitglied.getID());
    it.addFilter("felddefinition = ?", feld.getID());
    return it.size();
  }

  private File csv(String... zeilen) throws Exception
  {
    File file = File.createTempFile("mitglieder-import", ".csv");
    file.deleteOnExit();
    Files.write(file.toPath(), List.of(zeilen), StandardCharsets.UTF_8);
    return file;
  }

  // ---- Reihenfolge und lokale Verweise ----

  @Test
  void zeileKommtNachDerZeileAufDieSieVerweist() throws Exception
  {
    File file = csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Angehoeriger;-1;
        -1;Mustermann;Max;Vollzahler;;""");

    importieren(file);

    Mitglied max = getMitgliedMitVorname("Max");
    Mitglied anna = getMitgliedMitVorname("Anna");

    assertNotNull(max, "Vollzahler wurde nicht importiert");
    assertNotNull(anna, "Angehöriger wurde nicht importiert");
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void mehrereLokaleVerweiseWerdenAufgeloest() throws Exception
  {
    File file = csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Meier;Anna;Angehoeriger;-1;
        -1;Meier;Max;Vollzahler;;
        ;Wichtig;Eva;Angehoeriger;-2;
        -2;Wichtig;Hans;Vollzahler;;""");

    importieren(file);

    Mitglied anna = getMitgliedMitVorname("Anna");
    Mitglied max = getMitgliedMitVorname("Max");
    Mitglied eva = getMitgliedMitVorname("Eva");
    Mitglied hans = getMitgliedMitVorname("Hans");

    assertNotNull(anna);
    assertNotNull(max);
    assertNotNull(eva);
    assertNotNull(hans);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    assertEquals(Long.valueOf(hans.getID()), eva.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void zahleridIstEineDatenbankId() throws Exception
  {
    createMitglied("Zahler", "Paul", getBeitragsgruppe("Vollzahler"));

    importieren(csv("""
        name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        Mustermann;Anna;Angehoeriger;1;"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);
    assertEquals(Long.valueOf(1), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void unbekannteDatenbankIdBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        Mustermann;Anna;Angehoeriger;99;"""),
        "Zeile 1: Vollzahler nicht gefunden: 99");

    assertEquals(0, anzahlMitglieder());
  }

  @Test
  void fehlendeLokaleIdBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Angehoeriger;-2;
        -1;Mustermann;Berta;Vollzahler;"""),
        "Zeile 1: Vollzahler für Id -2 nicht gefunden.");
  }

  @Test
  void vollzahlerIdKeinIntegerBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        Mustermann;Anna;Angehoeriger;abc;"""),
        "Zeile 1 : Vollzahler Id abc ist keine gültige Integer Zahl.");
  }

  @Test
  void mitgliedIdKeinIntegerBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        abc;Mustermann;Anna;Angehoeriger;;"""),
        "Zeile 1: Mitglied ID abc ist keine gültige Integer Zahl.");
  }

  @Test
  void vollzahlerIdFehltBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Angehoeriger;;"""),
        "Zeile 1: Vollzahler Id ist nicht gesetzt.");
  }

  @Test
  void doppelteNegativeIdBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Anna;Angehoeriger;-1;
        -1;Mustermann;Berta;Vollzahler;;"""),
        "Zeile 1: Mitglied ID -1 wurde bereits bei einem anderen Mitglied verwendet.");
  }

  @Test
  void negativeIdWirdNumerischVerglichen() throws Exception
  {
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -01;Mustermann;Max;Vollzahler;;
        ;Mustermann;Anna;Angehoeriger;-1;"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    Mitglied max = getMitgliedMitVorname("Max");

    assertNotNull(anna);
    assertNotNull(max);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void zweiImporteTeilenKeineNegativenIds() throws Exception
  {
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Max;Vollzahler;;"""));
    verify(monitor, never()).log(anyString());

    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Angehoeriger;-1;"""),
        "Zeile 1: Vollzahler für Id -1 nicht gefunden.");
  }

  @Test
  void falscheBeitragsgruppeBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Gibtesnicht;-1;
        -1;Mustermann;Berta;Vollzahler;;"""),
        "Zeile 1: Beitragsgruppe nicht gefunden: Gibtesnicht");
  }

  @Test
  void vollzahlerReferenziertVollzahlerBrichtImportAb() throws Exception
  {
    createMitglied("Meier", "Hans", getBeitragsgruppe("Vollzahler"));

    importFehler(csv("""
        name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        Mustermann;Anna;Vollzahler;1;"""),
        "Zeile 1: Vollzahler Id ist nur bei Beitragsgruppen der Art Familienangehöriger erlaubt: 1");
  }

  @Test
  void nichtMitgliedDarfKeineVollzahlerIdHaben() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;adresstyp;beitragsgruppe;voll_zahlerid
        -1;Mustermann;Max;1;Vollzahler;
        ;Mustermann;Anna;2;Angehoeriger;-1"""),
        "Zeile 2: Für Nicht-Mitglieder ist keine Vollzahler Id erlaubt: -1");

    assertEquals(0, anzahlMitglieder(),
        "Rollback muss bereits gespeicherte Mitglieder entfernen");
  }

  @Test
  void fehlerBeiVollzahlerPruefungKommtVorDemErstenSpeichern() throws Exception
  {
    int vorher = anzahlMitglieder();

    importFehler(csv("""
        id;name;vorname;adresstyp;beitragsgruppe;voll_zahlerid
        -1;Mustermann;Max;1;Vollzahler;
        ;Mustermann;Anna;2;Angehoeriger;-1"""),
        "Zeile 2: Für Nicht-Mitglieder ist keine Vollzahler Id erlaubt: -1");

    assertEquals(vorher, anzahlMitglieder());
  }

  @Test
  void vollzahlerIdBeiFalscherBeitragsgruppeBrichtVorSpeichernAb()
      throws Exception
  {
    int vorher = anzahlMitglieder();

    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Max;Vollzahler;;
        ;Mustermann;Anna;Vollzahler;-1;"""),
        "Zeile 2: Vollzahler Id ist nur bei Beitragsgruppen der Art Familienangehöriger erlaubt");

    assertEquals(vorher, anzahlMitglieder());
  }

  // ---- Abweichender Zahler ----

  @Test
  void fehlenderAbweichenderZahlerBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Vollzahler;;99"""),
        "Zeile 1: Abweichenden Zahler nicht gefunden: 99");
  }

  @Test
  void abweichenderZahlerKeinIntegerBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Vollzahler;;abc"""),
        "Zeile 1: Abweichender Zahler Id abc ist keine gültige Integer Zahl.");
  }

  @Test
  void abweichenderZahlerLokaleIdNichtGefundenBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Anna;Vollzahler;;
        ;Mustermann;Tom;Vollzahler;;-2"""),
        "Zeile 2: Abweichender Zahler für Id -2 nicht gefunden.");
  }

  @Test
  void abweichenderZahlerGefundenImportOk() throws Exception
  {
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Anna;Vollzahler;;
        ;Mustermann;Tom;Vollzahler;;-1"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    Mitglied tom = getMitgliedMitVorname("Tom");

    assertNotNull(anna);
    assertNotNull(tom);
    assertEquals(Long.valueOf(anna.getID()), tom.getAbweichenderZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void abweichenderZahlerKannDatenbankIdSein() throws Exception
  {
    createMitglied("Zahler", "Paul", getBeitragsgruppe("Vollzahler"));

    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Tom;Vollzahler;;1"""));

    Mitglied tom = getMitgliedMitVorname("Tom");
    assertNotNull(tom);
    assertEquals(Long.valueOf(1), tom.getAbweichenderZahlerID());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void vollzahlerUndAbweichenderZahlerDuerfenGleichSein() throws Exception
  {
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Max;Vollzahler;;
        ;Mustermann;Anna;Angehoeriger;-1;-1"""));

    Mitglied max = getMitgliedMitVorname("Max");
    Mitglied anna = getMitgliedMitVorname("Anna");

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
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Meier;Tom;Vollzahler;;-3
        -2;Wichtig;Hans;Vollzahler;;
        -3;Wichtig;Eva;Angehoeriger;-2;"""));

    Mitglied tom = getMitgliedMitVorname("Tom");
    Mitglied eva = getMitgliedMitVorname("Eva");
    Mitglied hans = getMitgliedMitVorname("Hans");

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
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Max;Vollzahler;;-2
        -2;Mustermann;Anna;Vollzahler;;-1"""));

    Mitglied max = getMitgliedMitVorname("Max");
    Mitglied anna = getMitgliedMitVorname("Anna");

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
    importFehler(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        ;Mustermann;Anna;Angehoeriger;-2;99
        -2;Mustermann;Max;Vollzahler;;"""),
        "Zeile 1: Abweichenden Zahler nicht gefunden: 99");
  }

  // ---- Bestehende Mitglieder überschreiben ----

  @Test
  void positiveIdUeberschreibtBestehendesMitglied() throws Exception
  {
    createMitglied("Alt", "Paul", getBeitragsgruppe("Vollzahler"));

    importieren(csv("""
        id;name
        1;Neu"""));

    Mitglied paul = getMitgliedMitVorname("Paul");
    assertNotNull(paul);
    assertEquals("Neu", paul.getName());
    assertEquals(1, anzahlMitglieder());
    assertTrue(dialoge.constructed().size() == 1);
    verify(monitor, never()).log(anyString());
  }

  @Test
  void unbekanntePositiveIdBrichtImportAb() throws Exception
  {
    importFehler(csv("""
        id;name
        99;Neu"""), "Zeile 1: Mitglied mit ID 99 nicht vorhanden.");
  }

  @Test
  void nurNegativeIdsZeigenKeinenUeberschreibungsdialog() throws Exception
  {
    importieren(csv("""
        id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid
        -1;Mustermann;Max;Vollzahler;;
        -2;Mustermann;Anna;Angehoeriger;-1;"""));

    assertEquals(0, dialoge.constructed().size());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void positiveIdsZeigenDenUeberschreibungsdialogEinmal() throws Exception
  {
    createMitglied("Alt", "Paul", getBeitragsgruppe("Vollzahler"));
    createMitglied("Alt", "Pia", getBeitragsgruppe("Vollzahler"));

    importieren(csv("""
        id;name
        1;Neu
        2;Neu"""));

    assertEquals(1, dialoge.constructed().size());
    verify(monitor, never()).log(anyString());
  }

  @Test
  void dialogMitNeinBrichtImportAb() throws Exception
  {
    dialogAntwort = false;
    createMitglied("Alt", "Paul", getBeitragsgruppe("Vollzahler"));

    importFehler(csv("""
        id;name
        1;Neu"""), "Import abgebrochen");

    Mitglied paul = getMitgliedMitVorname("Paul");
    assertNotNull(paul);
    assertEquals("Alt", paul.getName());
  }

  // ---- Weitere Einstellungen und Regressionen ----

  @Test
  void externeMitgliedsnummerAendertLokaleVerweiseNicht() throws Exception
  {
    Einstellungen.setEinstellung(Property.EXTERNEMITGLIEDSNUMMER, true);

    importieren(csv("""
        id;externemitgliedsnummer;name;vorname;beitragsgruppe;voll_zahlerid
        -1;M-1;Mustermann;Max;Vollzahler;
        -2;M-2;Mustermann;Anna;Angehoeriger;-1"""));

    Mitglied max = getMitgliedMitVorname("Max");
    Mitglied anna = getMitgliedMitVorname("Anna");

    assertNotNull(max);
    assertNotNull(anna);
    assertEquals(Long.valueOf(max.getID()), anna.getVollZahlerID());
    verify(monitor, never()).log(anyString());
  }

  // ---- Eigenschaften ----

  @Test
  void eigenschaftWirdBeimImportAngelegt() throws Exception
  {
    EigenschaftGruppe gruppe = createEigenschaftGruppe("interessen");
    Eigenschaft eigenschaft = createEigenschaft("newsletter", gruppe);

    importieren(csv("""
        name;vorname;beitragsgruppe;eigenschaft_newsletter
        Mustermann;Anna;Vollzahler;true"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);

    assertEquals(1, anzahlEigenschaften(anna, eigenschaft));
    verify(monitor, never()).log(anyString());
  }

  @Test
  void eigenschaftWirdBeiJaUndXAngelegt() throws Exception
  {
    EigenschaftGruppe gruppe = createEigenschaftGruppe("interessen");
    Eigenschaft ja = createEigenschaft("newsletter", gruppe);
    Eigenschaft x = createEigenschaft("sport", gruppe);

    importieren(csv("""
        name;vorname;beitragsgruppe;eigenschaft_newsletter;eigenschaft_sport
        Mustermann;Anna;Vollzahler;ja;x"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);

    assertEquals(1, anzahlEigenschaften(anna, ja));
    assertEquals(1, anzahlEigenschaften(anna, x));
    verify(monitor, never()).log(anyString());
  }

  @Test
  void negativeEigenschaftswerteWerdenNichtAngelegt() throws Exception
  {
    EigenschaftGruppe gruppe = createEigenschaftGruppe("interessen");
    Eigenschaft falseWert = createEigenschaft("newsletter", gruppe);
    Eigenschaft neinWert = createEigenschaft("sport", gruppe);
    Eigenschaft nullWert = createEigenschaft("musik", gruppe);

    importieren(
        csv("""
            name;vorname;beitragsgruppe;eigenschaft_newsletter;eigenschaft_sport;eigenschaft_musik
            Mustermann;Anna;Vollzahler;false;nein;0"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);

    assertEquals(0, anzahlEigenschaften(anna, falseWert));
    assertEquals(0, anzahlEigenschaften(anna, neinWert));
    assertEquals(0, anzahlEigenschaften(anna, nullWert));
    verify(monitor, never()).log(anyString());
  }

  @Test
  void eigenschaftWirdBeimErneutenImportNichtDoppeltAngelegt() throws Exception
  {
    EigenschaftGruppe gruppe = createEigenschaftGruppe("interessen");
    Eigenschaft eigenschaft = createEigenschaft("newsletter", gruppe);

    importieren(csv("""
        name;vorname;beitragsgruppe;eigenschaft_newsletter
        Mustermann;Anna;Vollzahler;ja"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);

    importieren(csv("""
        id;name;eigenschaft_newsletter
        %s;Mustermann;ja""".formatted(anna.getID())));

    assertEquals(1, anzahlEigenschaften(anna, eigenschaft));
    verify(monitor, never()).log(anyString());
  }

  @Test
  void leereEigenschaftLoeschtVorhandeneEigenschaftBeimUeberschreiben()
      throws Exception
  {
    EigenschaftGruppe gruppe = createEigenschaftGruppe("interessen");
    Eigenschaft eigenschaft = createEigenschaft("newsletter", gruppe);

    importieren(csv("""
        name;vorname;beitragsgruppe;eigenschaft_newsletter
        Mustermann;Anna;Vollzahler;ja"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);
    assertEquals(1, anzahlEigenschaften(anna, eigenschaft));

    importieren(csv("""
        id;name;eigenschaft_newsletter
        %s;Mustermann;""".formatted(anna.getID())));

    assertEquals(0, anzahlEigenschaften(anna, eigenschaft));
    verify(monitor, never()).log(anyString());
  }

  @Test
  void neinLoeschtVorhandeneEigenschaftBeimUeberschreiben() throws Exception
  {
    EigenschaftGruppe gruppe = createEigenschaftGruppe("interessen");
    Eigenschaft eigenschaft = createEigenschaft("newsletter", gruppe);

    importieren(csv("""
        name;vorname;beitragsgruppe;eigenschaft_newsletter
        Mustermann;Anna;Vollzahler;ja"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);

    importieren(csv("""
        id;name;eigenschaft_newsletter
        %s;Mustermann;nein""".formatted(anna.getID())));

    assertEquals(0, anzahlEigenschaften(anna, eigenschaft));
    verify(monitor, never()).log(anyString());
  }

  // ---- Zusatzfelder ----

  @Test
  void zusatzfeldWirdBeimImportAngelegt() throws Exception
  {
    Felddefinition feld = createFelddefinition("zusatz", Datentyp.ZEICHENFOLGE);

    importieren(csv("""
        name;vorname;beitragsgruppe;zusatzfeld_zusatz
        Mustermann;Anna;Vollzahler;test"""));

    Mitglied anna = getMitgliedMitVorname("Anna");
    assertNotNull(anna);

    assertEquals(1, anzahlZusatzfelder(anna, feld));
    verify(monitor, never()).log(anyString());
  }

}
