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
  /** Familien-Testdaten: Mustermann (externezahlerid) und Meier (zahlerid). */
  private static final String FAMILIEN = "mitglieder-import-externenummer.csv";

  /** Dieselben Personen mit Verweisen innerhalb der Datei (#lfdnr, ^). */
  private static final String VERWEISE = "mitglieder-import-verweise.csv";

  private static final String HEADER = "externemitgliedsnummer;name;vorname;beitragsgruppe;zahlerid;externezahlerid";


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
          return neuesMitglied(mitgliedstyp);
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
      ZeilenDurchlauf durchlauf = new ZeilenDurchlauf(results, true);
      List<String> reihenfolge = new ArrayList<>();
      while (durchlauf.next())
      {
        reihenfolge.add(results.getString("externemitgliedsnummer") + ":"
            + results.getRow());
      }
      return reihenfolge;
    }
  }

  @Test
  void zeilenMitExternerZahlerIdKommenZuletzt() throws Exception
  {
    // Datei: 2 Anna (externezahlerid), 1 Max, 3 Hans, 4 Eva (zahlerid)
    assertEquals(List.of("1:2", "3:3", "4:4", "2:1"),
        durchlaufen(resource(FAMILIEN)));
  }

  @Test
  void ohneSpalteExternezahleridBleibtReihenfolge() throws Exception
  {
    assertEquals(List.of("2:1", "1:2", "3:3"),
        durchlaufen(csv("externemitgliedsnummer;name", "2;Mustermann",
            "1;Mustermann", "3;Meier")));
  }

  @Test
  void leereZelleZaehltAlsOhneExternezahlerid() throws Exception
  {
    try (ResultSet results = oeffnen(resource(FAMILIEN)))
    {
      results.next(); // Zeile 1: Anna mit externezahlerid
      assertEquals("1", ZeilenDurchlauf.getExterneZahlerId(results));
      results.next(); // Zeile 2: Max, Zelle leer
      assertNull(ZeilenDurchlauf.getExterneZahlerId(results));
    }
  }

  @Test
  void angehoerigeWerdenMitIhremVollzahlerVerknuepft() throws Exception
  {
    // Anna (externezahlerid) steht in der Datei vor ihrem Vollzahler Max.
    // Eva verweist über zahlerid auf die DB-ID von Hans; die IDs werden in
    // Speicherreihenfolge vergeben (Max 1, Hans 2, Eva 3, Anna 4).
    importieren(resource(FAMILIEN));

    // doImport meldet Fehler nur über das Monitor-Log
    verify(monitor, never()).log(anyString());
    assertEquals(4, gespeichert.size());

    // Geprüft wird der Stand beim store(), nicht der des Objekts danach
    assertEquals(Long.valueOf(gespeichert("1").getID()),
        persistiert("2", "VollZahlerID"));
    assertEquals(Long.valueOf(gespeichert("3").getID()),
        persistiert("4", "VollZahlerID"));
    assertNull(persistiert("1", "VollZahlerID"));
    assertNull(persistiert("3", "VollZahlerID"));

    // Familienverbände so, wie sie die GUI aus der DB aufbauen würde
    String baum = familienverbaende();
    System.out.println("Familienverbände nach dem Import:\n" + baum);
    assertEquals(String.join("\n",
        "Meier, Hans (3, Vollzahler)",
        "`-- Meier, Eva (4, Angehoeriger)",
        "Mustermann, Max (1, Vollzahler)",
        "`-- Mustermann, Anna (2, Angehoeriger)", ""), baum);
  }

  @Test
  void unbekannteExternezahleridBrichtImportAb() throws Exception
  {
    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;;99"));

    verify(monitor).log(contains(
        "Vollzahler mit externer Mitgliedsnummer nicht gefunden: 99"));
    assertTrue(nichtPersistiert("2"));
  }

  @Test
  void unbekannteZahleridBrichtImportAb() throws Exception
  {
    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;99;"));

    verify(monitor).log(contains("Vollzahler nicht gefunden: 99"));
    assertTrue(nichtPersistiert("2"));
  }

  @Test
  void zahleridUndExternezahleridGleichzeitigBrichtImportAb() throws Exception
  {
    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;1;1"));

    verify(monitor).log(contains(
        "zahlerid und externezahlerid dürfen nicht gleichzeitig angegeben werden"));
    assertTrue(nichtPersistiert("2"));
  }

  /** Gespeicherter Stand des Mitglieds mit diesem Vornamen. */
  private Map<String, Object> persistiertNachVorname(String vorname)
  {
    return datenbank.values().stream()
        .filter(werte -> vorname.equals(werte.get("Vorname"))).findFirst()
        .orElseThrow(() -> new AssertionError(
            "Mitglied nicht gespeichert: " + vorname));
  }

  /** DB-ID als Long, wie sie in vollZahlerID / abweichenderZahlerID steht. */
  private Long dbId(String vorname)
  {
    return Long.valueOf((String) persistiertNachVorname(vorname).get("ID"));
  }

  @Test
  void externezahleridWirdOhneExterneMitgliedsnummerIgnoriert() throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;;1"));

    // Keine Exception, der Import läuft, nur ein Hinweis im Log
    verify(monitor).log(contains("externezahlerid wird ignoriert"));
    verify(monitor, never()).log(contains("abgebrochen"));
    assertEquals(2, datenbank.size());
    assertNull(persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void beispieldateiMitVerweisenErgibtDieErwartetenFamilien() throws Exception
  {
    // Eva (Zeile 1) verweist per #2 auf Hans weiter unten, Anna und Tom stehen
    // per ^ direkt unter Max, Willi hat Max als abweichenden Zahler (#1).
    // Ohne externe Mitgliedsnummer: die Datei hat keine Spalte dafür
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
    importieren(resource(VERWEISE));

    verify(monitor, never()).log(anyString());
    assertEquals(6, datenbank.size());
    String baum = familienverbaende();
    System.out.println("Familienverbände aus " + VERWEISE + ":\n" + baum);
    assertEquals(String.join("\n",
        "Meier, Hans (Vollzahler)",
        "`-- Meier, Eva (Angehoeriger)",
        "Mustermann, Max (Vollzahler)",
        "|-- Mustermann, Anna (Angehoeriger)",
        "`-- Mustermann, Tom (Angehoeriger)", ""), baum);

    // Abweichender Zahler begründet keinen Familienverband
    assertEquals(dbId("Max"),
        persistiertNachVorname("Willi").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Max").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Hans").get("AbweichenderZahlerID"));
  }

  @Test
  void zahlerMitCaretVerweisIstDieZeileDarueber() throws Exception
  {
    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;^;", "3;Mustermann;Tom;Angehoeriger;^;",
        "4;Meier;Hans;Vollzahler;;", "5;Meier;Eva;Angehoeriger;^;"));

    verify(monitor, never()).log(anyString());
    String baum = familienverbaende();
    System.out.println("Familienverbände mit ^:\n" + baum);
    assertEquals(String.join("\n",
        "Meier, Hans (4, Vollzahler)",
        "`-- Meier, Eva (5, Angehoeriger)",
        "Mustermann, Max (1, Vollzahler)",
        "|-- Mustermann, Anna (2, Angehoeriger)",
        "`-- Mustermann, Tom (3, Angehoeriger)", ""), baum);
  }

  private static final String LFDNR_HEADER = "externemitgliedsnummer;lfdnr;name;vorname;beitragsgruppe;zahlerid;alternativer_zahlerid";

  @Test
  void zahlerMitLfdnrVerweisAuchWennErWeiterUntenSteht() throws Exception
  {
    // Angehöriger oben, Zahler unten, lfdnr nicht fortlaufend
    importieren(csv(LFDNR_HEADER,
        "2;12;Mustermann;Anna;Angehoeriger;#7;",
        "1;7;Mustermann;Max;Vollzahler;;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"), persistiertNachVorname("Anna").get("VollZahlerID"));
    assertNull(persistiertNachVorname("Max").get("VollZahlerID"));
  }

  @Test
  void lfdnrWirdNumerischVerglichenUndHeaderIstCaseInsensitiv()
      throws Exception
  {
    importieren(csv("externemitgliedsnummer;LfdNr;name;vorname;beitragsgruppe;zahlerid",
        "2;2;Mustermann;Anna;Angehoeriger;#01",
        "1;01;Mustermann;Max;Vollzahler;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void zahlerMitKeyVerweis() throws Exception
  {
    importieren(csv("externemitgliedsnummer;key;name;vorname;beitragsgruppe;zahlerid",
        "2;;Mustermann;Anna;Angehoeriger;#mueller-1",
        "1;mueller-1;Mustermann;Max;Vollzahler;"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"), persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void alternativerZahlerMitVerweisUndLeerenZellen() throws Exception
  {
    // Tom (oben) zahlt über #5 Max (unten), Anna hat Max als Zeile darüber,
    // Hans hat keinen abweichenden Zahler (leere Zelle)
    importieren(csv(LFDNR_HEADER,
        "3;;Meier;Tom;Vollzahler;;#5",
        "1;5;Mustermann;Max;Vollzahler;;",
        "2;;Mustermann;Anna;Vollzahler;;^",
        "4;;Meier;Hans;Vollzahler;;"));

    verify(monitor, never()).log(anyString());
    Long max = dbId("Max");
    assertEquals(max, persistiertNachVorname("Tom").get("AbweichenderZahlerID"));
    assertEquals(max, persistiertNachVorname("Anna").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Max").get("AbweichenderZahlerID"));
    assertNull(persistiertNachVorname("Hans").get("AbweichenderZahlerID"));
    assertEquals("", familienverbaende());
  }

  @Test
  void zahleridUndAlternativerZahleridGleichzeitigWarnt() throws Exception
  {
    importieren(csv(LFDNR_HEADER, "1;;Mustermann;Max;Vollzahler;;",
        "2;;Mustermann;Anna;Angehoeriger;^;^"));

    verify(monitor).log(contains(
        "Zeile 2: Warnung: zahlerid und alternativer_zahlerid sind beide angegeben"));
    verify(monitor, never()).log(contains("abgebrochen"));
    Long max = dbId("Max");
    assertEquals(max, persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(max, persistiertNachVorname("Anna").get("AbweichenderZahlerID"));
  }

  @Test
  void zeilennummerBleibtDiePositionInDerCsv() throws Exception
  {
    // Zeile 1 wird erst nach Zeile 2 (Max) verarbeitet, der Fehler muss
    // trotzdem "Zeile 1" melden
    importieren(csv("externemitgliedsnummer;lfdnr;name;vorname;beitragsgruppe;zahlerid;externezahlerid",
        "2;12;Mustermann;Anna;Angehoeriger;#7;9",
        "1;7;Mustermann;Max;Vollzahler;;"));

    verify(monitor).log(contains("Zeile 1: zahlerid und externezahlerid"));
    assertEquals(1, datenbank.size());
  }

  @Test
  void ungueltigeVerweiseBrechenVorDemImportAb() throws Exception
  {
    importieren(csv("externemitgliedsnummer;lfdnr;name;vorname;beitragsgruppe;zahlerid",
        "1;abc;Mustermann;Max;Vollzahler;"));
    verify(monitor).log(contains("Zeile 1: lfdnr muss eine ganze Zahl sein: abc"));

    importieren(csv("externemitgliedsnummer;lfdnr;name;vorname;beitragsgruppe;zahlerid",
        "1;7;Mustermann;Max;Vollzahler;", "2;7;Mustermann;Anna;Angehoeriger;#7"));
    verify(monitor).log(
        contains("Zeile 2: lfdnr 7 ist bereits in Zeile 1 vergeben"));

    importieren(csv("externemitgliedsnummer;lfdnr;name;vorname;beitragsgruppe;zahlerid",
        "1;7;Mustermann;Max;Vollzahler;", "2;8;Mustermann;Anna;Angehoeriger;#99"));
    verify(monitor).log(contains("Zeile 2: Verweis #99 in zahlerid"));

    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;#7;"));
    verify(monitor).log(contains("keine Spalte lfdnr oder key"));

    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;^;"));
    verify(monitor).log(contains("Zeile 1: Kein Zahler oberhalb für ^"));

    importieren(csv("externemitgliedsnummer;lfdnr;key;name;vorname;beitragsgruppe",
        "1;1;a;Mustermann;Max;Vollzahler"));
    verify(monitor).log(contains("lfdnr und key dürfen nicht gleichzeitig"));

    // Nichts davon darf etwas gespeichert haben
    assertTrue(datenbank.isEmpty());
  }

  @Test
  void zyklischeVerweiseBrechenAb() throws Exception
  {
    importieren(csv(LFDNR_HEADER,
        "1;1;Mustermann;Max;Angehoeriger;#2;",
        "2;2;Mustermann;Anna;Angehoeriger;#1;"));

    verify(monitor).log(contains("Zahler-Verweise sind zyklisch"));
    assertTrue(datenbank.isEmpty());
  }

  @Test
  void zeileOhneEigenenZahlerBeendetDenFamilienverband() throws Exception
  {
    // Karl gibt keinen Zahler an, ein ^ darunter gehört deshalb zu Karl und
    // nicht mehr zu Max
    importieren(csv("externemitgliedsnummer;name;vorname;beitragsgruppe;zahlerid",
        "1;Mustermann;Max;Vollzahler;", "2;Mustermann;Anna;Angehoeriger;^",
        "3;Meier;Karl;Vollzahler;", "4;Meier;Tom;Angehoeriger;^",
        "5;Wichtig;Hans;Vollzahler;", "6;Wichtig;Eva;Angehoeriger;^"));

    verify(monitor, never()).log(anyString());
    assertEquals(dbId("Max"), persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(dbId("Karl"), persistiertNachVorname("Tom").get("VollZahlerID"));
    assertEquals(dbId("Hans"), persistiertNachVorname("Eva").get("VollZahlerID"));
  }

  @Test
  void andereVerweiseZwischenZweiFamilienStoerenCaretNicht() throws Exception
  {
    // Zwischen Max und Hans stehen Zeilen mit #lfdnr, externezahlerid,
    // Datenbank-ID und alternativer_zahlerid. Sie sind keine Zahler und ändern
    // nicht, worauf ein ^ weiter unten verweist.
    vorhandenerZahler();

    importieren(csv("externemitgliedsnummer;lfdnr;name;vorname;beitragsgruppe;zahlerid;externezahlerid;alternativer_zahlerid",
        "1;;Mustermann;Max;Vollzahler;;;",
        "2;;Mustermann;Anna;Angehoeriger;^;;",
        "3;;Meier;Tom;Angehoeriger;#9;;",
        "4;;Mustermann;Lisa;Angehoeriger;;1;#9",
        "5;;Mustermann;Uwe;Angehoeriger;1;;",
        "6;;Mustermann;Ben;Angehoeriger;^;;",
        "7;9;Meier;Hans;Vollzahler;;;",
        "8;;Meier;Eva;Angehoeriger;^;;"));

    verify(monitor, never()).log(anyString());
    Long max = dbId("Max");
    Long hans = dbId("Hans");
    assertEquals(max, persistiertNachVorname("Anna").get("VollZahlerID"));
    assertEquals(hans, persistiertNachVorname("Tom").get("VollZahlerID"));
    assertEquals(max, persistiertNachVorname("Lisa").get("VollZahlerID"));
    assertEquals(Long.valueOf(1), persistiertNachVorname("Uwe").get("VollZahlerID"));
    assertEquals(max, persistiertNachVorname("Ben").get("VollZahlerID"));
    assertEquals(hans, persistiertNachVorname("Lisa").get("AbweichenderZahlerID"));
    assertEquals(hans, persistiertNachVorname("Eva").get("VollZahlerID"));
  }

  @Test
  void caretFunktioniertAuchInExternezahlerid() throws Exception
  {
    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;;^", "3;Mustermann;Tom;Angehoeriger;;^",
        "4;Meier;Hans;Vollzahler;;", "5;Meier;Eva;Angehoeriger;;^"));

    verify(monitor, never()).log(anyString());
    assertEquals(String.join("\n",
        "Meier, Hans (4, Vollzahler)",
        "`-- Meier, Eva (5, Angehoeriger)",
        "Mustermann, Max (1, Vollzahler)",
        "|-- Mustermann, Anna (2, Angehoeriger)",
        "`-- Mustermann, Tom (3, Angehoeriger)", ""), familienverbaende());
  }

  @Test
  void caretInExternezahleridUndZahleridGleichzeitigBrichtAb() throws Exception
  {
    importieren(csv(HEADER, "1;Mustermann;Max;Vollzahler;;",
        "2;Mustermann;Anna;Angehoeriger;1;^"));

    verify(monitor).log(contains(
        "Zeile 2: zahlerid und externezahlerid dürfen nicht gleichzeitig angegeben werden"));
  }


  @Test
  void zahleridFunktioniertOhneExterneMitgliedsnummer() throws Exception
  {
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);

    importieren(csv("name;vorname;beitragsgruppe;zahlerid",
        "Mustermann;Max;Vollzahler;", "Mustermann;Anna;Angehoeriger;1"));

    verify(monitor, never()).log(anyString());
    assertEquals(2, datenbank.size());
    Map<String, Object> max = datenbank.get("1");
    Map<String, Object> anna = datenbank.get("2");
    assertEquals("Max", max.get("Vorname"));
    assertEquals("Anna", anna.get("Vorname"));
    assertNull(max.get("VollZahlerID"));
    assertEquals(Long.valueOf(1), anna.get("VollZahlerID"));
  }

  /** Legt ein bereits vorhandenes Mitglied (ID 1) in der Fake-DB an. */
  private void vorhandenerZahler() throws Exception
  {
    Mitglied zahler = (Mitglied) Einstellungen.getDBService()
        .createObject(Mitglied.class, null);
    zahler.setName("Zahler");
    zahler.setVorname("Paul");
    zahler.setExterneMitgliedsnummer("9");
    zahler.store();
  }

  @Test
  void alternativerZahlerIdWirdGesetztOhneFamilienverband() throws Exception
  {
    vorhandenerZahler();

    importieren(csv("externemitgliedsnummer;name;vorname;beitragsgruppe;alternativer_zahlerid",
        "1;Mustermann;Max;Vollzahler;1"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1), persistiert("1", "AbweichenderZahlerID"));
    assertNull(persistiert("1", "VollZahlerID"));
    // Ein abweichender Zahler begründet keinen Familienverband
    assertEquals("", familienverbaende());
  }

  @Test
  void unbekannterAlternativerZahlerBrichtImportAb() throws Exception
  {
    importieren(csv("externemitgliedsnummer;name;vorname;beitragsgruppe;alternativer_zahlerid",
        "1;Mustermann;Max;Vollzahler;99"));

    verify(monitor)
        .log(contains("Alternativen Zahler nicht gefunden: 99"));
    assertTrue(nichtPersistiert("1"));
  }
}
