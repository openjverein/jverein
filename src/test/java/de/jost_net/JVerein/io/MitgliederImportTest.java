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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.io.MitgliederImport.ZeilenDurchlauf;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ProgressMonitor;

class MitgliederImportTest
{
  /** Familien-Testdaten bei aktiver externer Mitgliedsnummer (Verweise per externer Nummer und ^). */
  private static final String FAMILIEN = "mitglieder-import-externenummer.csv";

  /** Familien-Testdaten ohne externe Mitgliedsnummer (Schlüssel in der Datei, ^ und Datenbank-ID). */
  private static final String VERWEISE = "mitglieder-import-verweise.csv";

  private static final String ALLE_SPALTEN = "externemitgliedsnummer;name;vorname;beitragsgruppe;zahlerid;externezahlerid;alternativer_zahlerid;externealternativer_zahlerid";

  private static final String DB_HEADER = "name;vorname;beitragsgruppe;zahlerid;alternativer_zahlerid";


  /** Gespeicherte Mitglieder des Imports, in Speicherreihenfolge. */
  private List<Mitglied> gespeichert;

  private MockedStatic<Einstellungen> einstellungen;

  /**
   * JVereinDBService und die GUI-Klassen (TreePart) legen beim Laden statisch
   * ein Jameica-Settings an, das ohne laufende Application nicht existiert.
   */
  private MockedConstruction<Settings> settings;

  /** TreePart lädt im Konstruktor SWT-Images, was ohne GUI nicht geht. */
  private MockedConstruction<TreePart> treePart;

  /**
   * Simulierte DB-Tabelle: ID -> Feldwerte (Setter-Name ohne "set") zum
   * Zeitpunkt des letzten store().
   */
  private Map<String, Map<String, Object>> datenbank;

  private ProgressMonitor monitor;

  /** Von oeffnen() angelegte JDBC-Ressourcen, werden in tearDown geschlossen. */
  private final List<AutoCloseable> offen = new ArrayList<>();

  @BeforeEach
  void setUp() throws Exception
  {
    settings = Mockito.mockConstruction(Settings.class);
    treePart = Mockito.mockConstruction(TreePart.class);
    gespeichert = new ArrayList<>();
    datenbank = new HashMap<>();
    monitor = mock(ProgressMonitor.class);

    Mitgliedstyp mitgliedstyp = mock(Mitgliedstyp.class);
    Mockito.doReturn(Mitgliedstyp.MITGLIED).when(mitgliedstyp).getID();

    List<Beitragsgruppe> beitragsgruppen = List.of(
        beitragsgruppe("1", "Vollzahler", ArtBeitragsart.NORMAL),
        beitragsgruppe("2", "Angehoeriger", ArtBeitragsart.FAMILIE_ANGEHOERIGER));

    // Fake-DBService: Mitglieder werden im Speicher gehalten, andere Listen
    // sind leer.
    JVereinDBService db = mock(JVereinDBService.class, invocation ->
    {
      String name = invocation.getMethod().getName();
      if (name.equals("createObject"))
      {
        Class<?> klasse = invocation.getArgument(0);
        if (klasse == Mitglied.class)
        {
          String id = invocation.getArgument(1);
          return id == null ? neuesMitglied(mitgliedstyp)
              : gespeichertMitId(id);
        }
        return mock(klasse);
      }
      if (name.equals("createList"))
      {
        Class<?> klasse = invocation.getArgument(0);
        if (klasse == Mitglied.class)
        {
          return iterator(gespeichert, (m, spalte) -> {
            try
            {
              return spalte.equals("id") ? m.getID()
                  : m.getExterneMitgliedsnummer();
            }
            catch (Exception e)
            {
              throw new IllegalStateException(e);
            }
          });
        }
        if (klasse == Beitragsgruppe.class)
        {
          return iterator(beitragsgruppen, (bg, spalte) -> {
            try
            {
              return bg.getBezeichnung();
            }
            catch (Exception e)
            {
              throw new IllegalStateException(e);
            }
          });
        }
        return iterator(List.of(), (o, spalte) -> null);
      }
      return null;
    });

    einstellungen = Mockito.mockStatic(Einstellungen.class);
    einstellungen.when(Einstellungen::getDBService).thenReturn(db);
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, true);
    einstellung(Property.EINTRITTSDATUMPFLICHT, false);
    einstellung(Property.GEBURTSDATUMPFLICHT, false);
    einstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT, false);
    einstellung(Property.INDIVIDUELLEBEITRAEGE, false);
    einstellung(Property.ZAHLUNGSWEG, Zahlungsweg.ÜBERWEISUNG);
    einstellung(Property.ZAHLUNGSRHYTMUS, 12);
    einstellung(Property.BEITRAGSMODEL,
        Beitragsmodel.GLEICHERTERMINFUERALLE.getKey());
    einstellung(Property.SEPAMANDATIDSOURCE, SepaMandatIdSource.DBID);
    einstellung(Property.ABWEICHENDEZAHLER, true);
  }

  @AfterEach
  void tearDown() throws Exception
  {
    for (AutoCloseable c : offen)
    {
      c.close();
    }
    offen.clear();
    if (einstellungen != null)
    {
      einstellungen.close();
    }
    if (treePart != null)
    {
      treePart.close();
    }
    if (settings != null)
    {
      settings.close();
    }
  }

  private void einstellung(Property property, Object wert)
  {
    einstellungen.when(() -> Einstellungen.getEinstellung(property))
        .thenReturn(wert);
  }

  private Beitragsgruppe beitragsgruppe(String id, String bezeichnung,
      ArtBeitragsart art) throws Exception
  {
    Beitragsgruppe bg = mock(Beitragsgruppe.class);
    Mockito.doReturn(id).when(bg).getID();
    Mockito.doReturn(bezeichnung).when(bg).getBezeichnung();
    Mockito.doReturn(art).when(bg).getBeitragsArt();
    return bg;
  }

  /**
   * Mitglied-Mock, der gesetzte Werte über die passenden Getter
   * zurückliefert. store() vergibt eine ID und merkt das Mitglied.
   */
  private Mitglied neuesMitglied(Mitgliedstyp mitgliedstyp)
  {
    Map<String, Object> werte = new HashMap<>();
    Answer<Object> answer = invocation -> {
      String name = invocation.getMethod().getName();
      Class<?> typ = invocation.getMethod().getReturnType();
      if (name.equals("store"))
      {
        if (!werte.containsKey("ID"))
        {
          werte.put("ID", String.valueOf(gespeichert.size() + 1));
          gespeichert.add((Mitglied) invocation.getMock());
        }
        // Nur was bis zu diesem Zeitpunkt gesetzt wurde, landet in der DB
        datenbank.put((String) werte.get("ID"), new HashMap<>(werte));
        return null;
      }
      if (name.equals("getMitgliedstyp"))
      {
        return mitgliedstyp;
      }
      if (name.startsWith("set") && invocation.getArguments().length == 1)
      {
        werte.put(name.substring(3), invocation.getArgument(0));
        return null;
      }
      if (name.startsWith("get") && werte.containsKey(name.substring(3)))
      {
        return werte.get(name.substring(3));
      }
      if (name.equals("getPersonenart"))
      {
        return "N";
      }
      if (typ == String.class)
      {
        return "";
      }
      if (typ == int.class)
      {
        return 0;
      }
      if (typ == boolean.class)
      {
        return false;
      }
      return null;
    };
    return mock(Mitglied.class, answer);
  }

  /**
   * Liefert einen DBIterator über die Liste. addFilter("spalte = ?", wert)
   * schränkt die Liste auf Einträge ein, deren Wert (aus wert.apply(eintrag,
   * spalte)) gleich dem Parameter ist; Filter ohne Parameter werden ignoriert.
   */
  @SuppressWarnings("unchecked")
  private <T extends DBObject> DBIterator<T> iterator(List<T> alle,
      BiFunction<T, String, Object> wert)
  {
    List<T> treffer = new ArrayList<>(alle);
    int[] position = { 0 };
    return mock(DBIterator.class, invocation -> {
      switch (invocation.getMethod().getName())
      {
        case "addFilter":
          if (invocation.getArguments().length > 1)
          {
            String spalte = ((String) invocation.getArgument(0)).split(" ")[0];
            Object parameter = invocation.getArgument(1);
            treffer.removeIf(
                e -> !Objects.equals(wert.apply(e, spalte), parameter));
          }
          return null;
        case "hasNext":
          return position[0] < treffer.size();
        case "next":
          return treffer.get(position[0]++);
        case "size":
          return treffer.size();
        default:
          return null;
      }
    });
  }

  private Mitglied gespeichertMitId(String id) throws Exception
  {
    for (Mitglied m : gespeichert)
    {
      if (id.equals(m.getID()))
      {
        return m;
      }
    }
    throw new AssertionError("Mitglied nicht gespeichert: " + id);
  }

  private Mitglied gespeichert(String externeMitgliedsnummer) throws Exception
  {
    for (Mitglied m : gespeichert)
    {
      if (externeMitgliedsnummer.equals(m.getExterneMitgliedsnummer()))
      {
        return m;
      }
    }
    throw new AssertionError(
        "Mitglied nicht gespeichert: " + externeMitgliedsnummer);
  }

  /** Wert eines Feldes, wie er für das Mitglied in der DB stünde. */
  private Object persistiert(String externeMitgliedsnummer, String feld)
      throws Exception
  {
    return datenbank.get(gespeichert(externeMitgliedsnummer).getID())
        .get(feld);
  }

  private boolean nichtPersistiert(String externeMitgliedsnummer)
  {
    return datenbank.values().stream().noneMatch(
        werte -> externeMitgliedsnummer.equals(werte.get("ExterneMitgliedsnummer")));
  }

  private static String beschreibung(Map<String, Object> werte)
      throws Exception
  {
    Object nummer = werte.get("ExterneMitgliedsnummer");
    return werte.get("Name") + ", " + werte.get("Vorname") + " ("
        + (nummer == null ? "" : nummer + ", ")
        + ((Beitragsgruppe) werte.get("Beitragsgruppe")).getBezeichnung() + ")";
  }

  /**
   * Baut die Familienverbände aus dem simulierten DB-Stand so auf wie die GUI:
   * Angehörige eines Vollzahlers sind alle Mitglieder mit zahlerid = ID des
   * Vollzahlers (MitgliedControl.refreshFamilienangehoerigeTable,
   * FamilienbeitragNode), sortiert nach Name, Vorname. Wurzel ist jeweils der
   * Vollzahler, der selbst keinen Vollzahler haben darf.
   */
  private String familienverbaende() throws Exception
  {
    Comparator<Map<String, Object>> nachName = Comparator
        .comparing((Map<String, Object> w) -> (String) w.get("Name"))
        .thenComparing(w -> (String) w.get("Vorname"));
    List<Map<String, Object>> alle = new ArrayList<>(datenbank.values());
    alle.sort(nachName);

    StringBuilder baum = new StringBuilder();
    for (Map<String, Object> zahler : alle)
    {
      Long zahlerId = Long.valueOf((String) zahler.get("ID"));
      List<Map<String, Object>> angehoerige = new ArrayList<>();
      for (Map<String, Object> w : alle)
      {
        if (zahlerId.equals(w.get("VollZahlerID")))
        {
          angehoerige.add(w);
        }
      }
      if (angehoerige.isEmpty())
      {
        continue;
      }
      assertNull(zahler.get("VollZahlerID"),
          "Vollzahler darf nicht selbst Angehöriger sein");
      baum.append(beschreibung(zahler)).append('\n');
      for (int i = 0; i < angehoerige.size(); i++)
      {
        baum.append(i == angehoerige.size() - 1 ? "`-- " : "|-- ")
            .append(beschreibung(angehoerige.get(i))).append('\n');
      }
    }
    return baum.toString();
  }

  private File resource(String name) throws Exception
  {
    return new File(getClass().getResource("/" + name).toURI());
  }

  /**
   * Schreibt eine CSV-Datei (Header + Zeilen) in eine temporäre Datei. Kein
   * @TempDir, weil doImport bei Fehlern die Verbindung offen lässt und Windows
   * die Datei dann nicht löschen kann.
   */
  private File csv(String... zeilen) throws IOException
  {
    File file = File.createTempFile("mitglieder-import-test", ".csv");
    file.deleteOnExit();
    Files.write(file.toPath(), List.of(zeilen), StandardCharsets.UTF_8);
    return file;
  }

  private void importieren(File file) throws Exception
  {
    new MitgliederImport().doImport(null, null, file, "UTF-8", monitor);
  }

  private ResultSet oeffnen(File file) throws Exception
  {
    String fil = file.getName();
    int pos = fil.lastIndexOf('.');

    Properties props = new Properties();
    props.put("separator", ";");
    props.put("suppressHeaders", "false");
    props.put("charset", "UTF-8");
    props.put("fileExtension", fil.substring(pos));

    Class.forName("org.relique.jdbc.csv.CsvDriver");
    Connection conn = DriverManager
        .getConnection("jdbc:relique:csv:" + file.getParent(), props);
    offen.add(conn);
    Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
    offen.add(stmt);
    return stmt.executeQuery("SELECT * FROM \"" + fil.substring(0, pos) + "\"");
  }

  /**
   * Öffnet die CSV-Datei so wie MitgliederImport.doImport und liefert pro
   * Zeile in der Reihenfolge des ZeilenDurchlaufs "externemitgliedsnummer:Zeilennummer".
   */
  private List<String> durchlaufen(File file) throws Exception
  {
    try (ResultSet results = oeffnen(file))
    {
      ZeilenDurchlauf durchlauf = new ZeilenDurchlauf(results);
      List<String> reihenfolge = new ArrayList<>();
      while (durchlauf.next())
      {
        reihenfolge.add(results.getString("externemitgliedsnummer") + ":"
            + results.getRow());
      }
      return reihenfolge;
    }
  }

  /** Gespeicherter Stand des Mitglieds mit diesem Vornamen. */
  private Map<String, Object> persistiertNachVorname(String vorname)
  {
    return datenbank.values().stream()
        .filter(werte -> vorname.equals(werte.get("Vorname"))).findFirst()
        .orElseThrow(() -> new AssertionError(
            "Mitglied nicht gespeichert: " + vorname));
  }

  /** DB-ID als Long, wie sie in VollZahlerID / AbweichenderZahlerID steht. */
  private Long dbId(String vorname)
  {
    return Long.valueOf((String) persistiertNachVorname(vorname).get("ID"));
  }

  /** Legt ein bereits vorhandenes Mitglied in der Fake-DB an. */
  private void vorhandenesMitglied(String name, String vorname,
      String externeMitgliedsnummer) throws Exception
  {
    Mitglied m = (Mitglied) Einstellungen.getDBService()
        .createObject(Mitglied.class, null);
    m.setName(name);
    m.setVorname(vorname);
    if (externeMitgliedsnummer != null)
    {
      m.setExterneMitgliedsnummer(externeMitgliedsnummer);
    }
    m.store();
  }

  private static String baum(String... zeilen)
  {
    return String.join("\n", zeilen) + "\n";
  }

  // ---- Reihenfolge der Verarbeitung ----

  @Test
  void vollzahlerKommenVorAngehoerigen() throws Exception
  {
    // Zeile 1 (Anna) und Zeile 4 (Eva) haben eine Vollzahler-Angabe und kommen
    // nach den Zeilen 2, 3 und 5
    assertEquals(List.of("M-1:2", "M-3:3", "M-5:5", "M-2:1", "M-4:4"),
        durchlaufen(resource(FAMILIEN)));
  }

  @Test
  void ohneVerweiseBleibtDieReihenfolgeDerDatei() throws Exception
  {
    assertEquals(List.of("2:1", "1:2", "3:3"),
        durchlaufen(csv("externemitgliedsnummer;name", "2;Mustermann",
            "1;Mustermann", "3;Meier")));
  }

  // ---- Beispieldateien ----

  @Test
  void beispieldateiMitExternenMitgliedsnummern() throws Exception
  {
    // Einstellung "Externe Mitgliedsnummer" an
    importieren(resource(FAMILIEN));

    verify(monitor, never()).log(anyString());
    assertEquals(5, datenbank.size());
    String baum = familienverbaende();
    System.out.println("Familienverbände aus " + FAMILIEN + ":\n" + baum);
    assertEquals(baum("Meier, Hans (M-3, Vollzahler)",
        "`-- Meier, Eva (M-4, Angehoeriger)",
        "Mustermann, Max (M-1, Vollzahler)",
        "`-- Mustermann, Anna (M-2, Angehoeriger)"), baum);
    // Willi: abweichender Zahler über die externe Nummer M-1
    assertEquals(dbId("Max"),
        persistiertNachVorname("Willi").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Max").get("AbweichenderZahlerID"));
  }

  @Test
  void beispieldateiOhneExterneMitgliedsnummer() throws Exception
  {
    // Einstellung aus: externe Nummern sind nur Schlüssel innerhalb der Datei
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
    vorhandenesMitglied("Zahler", "Paul", null); // bekommt die ID 1

    importieren(resource(VERWEISE));

    verify(monitor, never()).log(anyString());
    String baum = familienverbaende();
    System.out.println("Familienverbände aus " + VERWEISE + ":\n" + baum);
    assertEquals(baum("Meier, Hans (Vollzahler)",
        "|-- Meier, Ben (Angehoeriger)", "`-- Meier, Eva (Angehoeriger)",
        "Mustermann, Max (Vollzahler)",
        "|-- Mustermann, Anna (Angehoeriger)",
        "`-- Mustermann, Tom (Angehoeriger)"), baum);
    // Abweichender Zahler begründet keinen Familienverband
    assertEquals(Long.valueOf(1),
        persistiertNachVorname("Willi").get("AbweichenderZahlerID"));
    // Die Schlüssel in der Datei werden nicht gespeichert
    assertNull(persistiertNachVorname("Max").get("ExterneMitgliedsnummer"));
  }

  // ---- ^ ----

  @Test
  void zeileOhneEigenenZahlerBeendetDenFamilienverband() throws Exception
  {
    // Karl gibt keinen Zahler an, ein ^ darunter gehört deshalb zu Karl
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;",
        "2;Mustermann;Anna;Angehoeriger;^;;;", "3;Meier;Karl;Vollzahler;;;;",
        "4;Meier;Tom;Angehoeriger;;^;;", "5;Wichtig;Hans;Vollzahler;;;;",
        "6;Wichtig;Eva;Angehoeriger;^;;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"), persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(dbId("Karl"), persistiertNachVorname("Tom").get("VollZahlerID"));
    assertEquals(dbId("Hans"), persistiertNachVorname("Eva").get("VollZahlerID"));
  }

  @Test
  void andereVerweiseZwischenZweiFamilienStoerenCaretNicht() throws Exception
  {
    // Zwischen Max und Hans stehen Verweise per externer Nummer und
    // Datenbank-ID. Sie sind keine Zahler und ändern nicht, worauf ^ zeigt.
    vorhandenesMitglied("Zahler", "Paul", "9"); // bekommt die ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;",
        "2;Mustermann;Anna;Angehoeriger;^;;;", "3;Meier;Tom;Angehoeriger;;7;;",
        "4;Mustermann;Lisa;Angehoeriger;;1;;",
        "5;Mustermann;Uwe;Angehoeriger;1;;;",
        "6;Mustermann;Ben;Angehoeriger;^;;;", "7;Meier;Hans;Vollzahler;;;;",
        "8;Meier;Eva;Angehoeriger;;^;;"));

    verify(monitor, never()).log(anyString());
    Long max = dbId("Max");
    Long hans = dbId("Hans");
    assertEquals(max, persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(hans, persistiertNachVorname("Tom").get("VollZahlerID"));
    assertEquals(max, persistiertNachVorname("Lisa").get("VollZahlerID"));
    assertEquals(Long.valueOf(1), persistiertNachVorname("Uwe").get("VollZahlerID"));
    assertEquals(max, persistiertNachVorname("Ben").get("VollZahlerID"));
    assertEquals(hans, persistiertNachVorname("Eva").get("VollZahlerID"));
  }

  // ---- Datenbank-ID (zahlerid, alternativer_zahlerid) ----

  @Test
  void zahleridIstEineDatenbankId() throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
    vorhandenesMitglied("Zahler", "Paul", null); // ID 1

    importieren(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;1;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void datenbankIdBleibtAuchBeiAktiverExternerMitgliedsnummerEineDatenbankId()
      throws Exception
  {
    // Paul hat keine externe Nummer (z. B. Nicht-Mitglied)
    vorhandenesMitglied("Zahler", "Paul", null); // ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;1;;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void unbekannteDatenbankIdBrichtImportAb() throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    importieren(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;99;"));

    verify(monitor).log(contains("Zeile 1: Vollzahler nicht gefunden: 99"));
    assertTrue(datenbank.isEmpty());
  }

  @Test
  void keineZahlAlsDatenbankIdBrichtImportAb() throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    importieren(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;abc;"));

    verify(monitor).log(
        contains("Zeile 1: Ungültige Datenbank-ID für Vollzahler: abc"));
  }

  // ---- externe Mitgliedsnummer (externezahlerid, externealternativer_zahlerid)

  @Test
  void zahlerPerExternerMitgliedsnummerAusDerDatenbank() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;;9;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void datenbankIdUndExterneNummerKoennenGemischtWerden() throws Exception
  {
    // Paul ist ein Nicht-Mitglied ohne externe Nummer (ID 1), Pia hat die
    // externe Nummer 9 (ID 2)
    vorhandenesMitglied("Zahler", "Paul", null);
    vorhandenesMitglied("Zahler", "Pia", "9");

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;1;;;9"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(Long.valueOf(2),
        persistiertNachVorname("Anna").get("AbweichenderZahlerID"));
  }

  @Test
  void unbekannteExterneMitgliedsnummerBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;;17;;"));

    verify(monitor).log(contains(
        "Zeile 1: Vollzahler mit externer Mitgliedsnummer nicht gefunden: 17"));
    assertTrue(datenbank.isEmpty());
  }

  @Test
  void mehrfachVorhandeneExterneMitgliedsnummerBrichtAb() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9");
    vorhandenesMitglied("Zahler", "Pia", "9");

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;;9;;"));

    verify(monitor).log(contains(
        "Zeile 1: Externe Mitgliedsnummer des Zahlers ist mehrfach vorhanden: 9"));
  }

  @Test
  void externeNummerVerweistOhneAktiveEinstellungNurAufDieDatei()
      throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    // Eva steht vor ihrem Zahler Hans, h ist nur ein Schlüssel der Datei
    importieren(csv(ALLE_SPALTEN, "x;Meier;Eva;Angehoeriger;;h;;",
        "h;Meier;Hans;Vollzahler;;;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Hans"), persistiertNachVorname("Eva").get("VollZahlerID"));
    assertNull(persistiertNachVorname("Hans").get("ExterneMitgliedsnummer"));
  }

  @Test
  void unbekannterSchluesselBeiAusgeschalteterEinstellungBrichtAb()
      throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
    vorhandenesMitglied("Zahler", "Paul", "9");

    importieren(csv(ALLE_SPALTEN, "x;Meier;Eva;Angehoeriger;;9;;"));

    verify(monitor).log(contains(
        "Zeile 1: Vollzahler mit externer Mitgliedsnummer 9 nicht in der Datei gefunden"));
  }

  @Test
  void idUndExterneNummerZeigenAufDasSelbeMitglied() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;1;9;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void findetDieIdKeinMitgliedWirdDieExterneNummerVerwendet() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;99;9;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void findetDieIdEinMitgliedWirdDieUnbekannteExterneNummerIgnoriert()
      throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;1;unbekannt;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void idUndExterneNummerAufVerschiedeneMitgliederBrechenAb() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1
    vorhandenesMitglied("Zahler", "Pia", "8"); // ID 2

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;1;8;;"));

    verify(monitor).log(contains("Zeile 1: Vollzahler: zahlerid 1 und "
        + "externezahlerid 8 verweisen auf verschiedene Mitglieder"));
    assertTrue(nichtPersistiert("1"));
  }

  @Test
  void findetWederIdNochExterneNummerBrichtAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;99;17;;"));

    verify(monitor).log(contains(
        "Zeile 1: Vollzahler weder mit zahlerid 99 noch mit externezahlerid 17 gefunden"));
    assertTrue(datenbank.isEmpty());
  }

  @Test
  void idUndExterneNummerDesAbweichendenZahlersWerdenGleichBehandelt()
      throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1

    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Vollzahler;;;99;9"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1),
        persistiertNachVorname("Anna").get("AbweichenderZahlerID"));
  }

  @Test
  void caretMitZweiterAngabeZurSelbenRolleBrichtAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;",
        "2;Mustermann;Anna;Angehoeriger;^;5;;"));

    verify(monitor).log(contains("Zeile 2: ^ kann nicht zusammen mit einer "
        + "zweiten Angabe zum Vollzahler stehen"));
    assertTrue(datenbank.isEmpty());
  }

  // ---- alternativer Zahler ----

  @Test
  void alternativerZahlerMitCaretUndLeerenZellen() throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    importieren(csv(DB_HEADER, "Mustermann;Max;Vollzahler;;",
        "Meier;Tom;Vollzahler;;^", "Meier;Hans;Vollzahler;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"),
        persistiertNachVorname("Tom").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Max").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Hans").get("AbweichenderZahlerID"));
  }

  @Test
  void zahleridUndAlternativerZahleridGleichzeitigIstOhneWarnungErlaubt()
      throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;",
        "2;Mustermann;Anna;Angehoeriger;^;;^;"));

    verify(monitor, never()).log(anyString());
    Long max = dbId("Max");
    assertEquals(max, persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(max, persistiertNachVorname("Anna").get("AbweichenderZahlerID"));
  }

  @Test
  void alternativerZahlerOhneAktiveAbweichendeZahlerWarntEinmal()
      throws Exception
  {
    einstellung(Property.ABWEICHENDEZAHLER, false);
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    importieren(csv(DB_HEADER, "Mustermann;Max;Vollzahler;;",
        "Meier;Tom;Vollzahler;;^", "Wichtig;Eva;Vollzahler;;^"));

    verify(monitor).log(contains(
        "Zeile 2: Warnung: Abweichende Zahler sind in den Einstellungen nicht aktiviert"));
    // Die Meldung kommt nur einmal, nicht für jede Zeile
    verify(monitor, times(1)).log(contains("Warnung"));
    verify(monitor, never()).log(contains("abgebrochen"));
    Long max = dbId("Max");
    assertEquals(max, persistiertNachVorname("Tom").get("AbweichenderZahlerID"));
    assertEquals(max, persistiertNachVorname("Eva").get("AbweichenderZahlerID"));
  }

  @Test
  void unbekannterAlternativerZahlerBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;99"));

    verify(monitor).log(contains(
        "Zeile 1: Alternativer Zahler mit externer Mitgliedsnummer nicht gefunden: 99"));
  }

  // ---- Fehler bei der Planung (vor dem Import) ----

  @Test
  void zeilennummerBleibtDiePositionInDerCsv() throws Exception
  {
    // Zeile 1 (Anna) wird erst nach Zeile 2 (Max) verarbeitet, der Fehler muss
    // trotzdem "Zeile 1" melden
    importieren(csv(ALLE_SPALTEN, "2;Mustermann;Anna;Angehoeriger;;1;;unbekannt",
        "1;Mustermann;Max;Vollzahler;;;;"));

    verify(monitor).log(contains(
        "Zeile 1: Alternativer Zahler mit externer Mitgliedsnummer nicht gefunden: unbekannt"));
  }

  @Test
  void ungueltigeVerweiseBrechenAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;^;;;"));
    verify(monitor).log(contains("Zeile 1: Kein Zahler oberhalb für ^"));

    // Max ist beim Verweis noch nicht gespeichert (Familienangehörige nach
    // allen anderen Zeilen), ein Zahler kann nicht sein eigener Angehöriger sein
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Angehoeriger;;1;;"));
    verify(monitor).log(contains(
        "Zeile 1: Vollzahler mit externer Mitgliedsnummer nicht gefunden: 1"));

    assertTrue(datenbank.isEmpty());
  }

  @Test
  void mehrfachVorkommendeExterneNummerInDerDateiBrichtBeimVerweisAb()
      throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;",
        "1;Mustermann;Tom;Vollzahler;;;;", "2;Mustermann;Anna;Angehoeriger;;1;;"));

    verify(monitor).log(contains(
        "Zeile 3: Externe Mitgliedsnummer 1 kommt in der Datei mehrfach vor"));
  }

  // ---- Reihenfolge ohne Abhängigkeitsplanung ----

  @Test
  void vollzahlerMitAbweichendemZahlerUndAngehoerigerDarueber() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", "9"); // ID 1

    // Anna steht über ihrem Vollzahler Max, Max hat Paul als abweichenden Zahler
    importieren(csv(ALLE_SPALTEN, "2;Mustermann;Anna;Angehoeriger;;1;;",
        "1;Mustermann;Max;Vollzahler;;;;9"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"), persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(Long.valueOf(1),
        persistiertNachVorname("Max").get("AbweichenderZahlerID"));
  }

  @Test
  void abweichenderZahlerKannEinWeiterUntenStehenderAngehoerigerSein()
      throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Meier;Tom;Vollzahler;;;;E",
        "2;Wichtig;Hans;Vollzahler;;;;", "E;Wichtig;Eva;Angehoeriger;;2;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Eva"),
        persistiertNachVorname("Tom").get("AbweichenderZahlerID"));
    assertEquals(dbId("Hans"), persistiertNachVorname("Eva").get("VollZahlerID"));
  }

  @Test
  void abweichendeZahlerDuerfenSichGegenseitigZahlen() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Max;Vollzahler;;;;2",
        "2;Mustermann;Anna;Vollzahler;;;;1"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Anna"),
        persistiertNachVorname("Max").get("AbweichenderZahlerID"));
    assertEquals(dbId("Max"),
        persistiertNachVorname("Anna").get("AbweichenderZahlerID"));
  }

  @Test
  void angehoerigerKannNichtAufWeiterUntenStehendenAngehoerigenVerweisen()
      throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "1;Mustermann;Anna;Angehoeriger;;2;;",
        "2;Mustermann;Tom;Angehoeriger;;3;;", "3;Mustermann;Max;Vollzahler;;;;"));

    verify(monitor).log(contains(
        "Zeile 1: Vollzahler mit externer Mitgliedsnummer nicht gefunden: 2"));
  }
}
