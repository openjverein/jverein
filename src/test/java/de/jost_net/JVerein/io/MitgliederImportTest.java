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
  /** Familien-Testdaten (Verweise per Id). */
  private static final String FAMILIEN = "mitglieder-import.csv";

  /** Familien-Testdaten ). */
  private static final String VERWEISE = "mitglieder-import-verweise.csv";

  private static final String ALLE_SPALTEN = "id;name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid";

  private static final String DB_HEADER = "name;vorname;beitragsgruppe;voll_zahlerid;abweichende_zahlerid";

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

  /**
   * Von oeffnen() angelegte JDBC-Ressourcen, werden in tearDown geschlossen.
   */
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
        beitragsgruppe("2", "Angehoeriger",
            ArtBeitragsart.FAMILIE_ANGEHOERIGER));

    // Fake-DBService: Mitglieder werden im Speicher gehalten, andere Listen
    // sind leer.
    JVereinDBService db = mock(JVereinDBService.class, invocation -> {
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
              return m.getID();
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
    einstellung(Property.EXTERNEMITGLIEDSNUMMER, false);
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
    einstellung(Property.FAMILIENBEITRAG, true);
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
   * Mitglied-Mock, der gesetzte Werte über die passenden Getter zurückliefert.
   * store() vergibt eine ID und merkt das Mitglied.
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

  private static String beschreibung(Map<String, Object> werte) throws Exception
  {
    Object nummer = werte.get("ID");
    return werte.get("Name") + ", " + werte.get("Vorname") + " ("
        + (nummer == null ? "" : nummer + ", ")
        + ((Beitragsgruppe) werte.get("Beitragsgruppe")).getBezeichnung() + ")";
  }

  /**
   * Baut die Familienverbände aus dem simulierten DB-Stand so auf wie die GUI:
   * Angehörige eines Vollzahlers sind alle Mitglieder mit voll_zahlerid = ID
   * des Vollzahlers (MitgliedControl.refreshFamilienangehoerigeTable,
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
   * Schreibt eine CSV-Datei (Header + Zeilen) in eine temporäre Datei.
   * Kein @TempDir, weil doImport bei Fehlern die Verbindung offen lässt und
   * Windows die Datei dann nicht löschen kann.
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
   * Öffnet die CSV-Datei so wie MitgliederImport.doImport und liefert pro Zeile
   * in der Reihenfolge des ZeilenDurchlaufs "id:Zeilennummer".
   */
  private List<String> durchlaufen(File file) throws Exception
  {
    try (ResultSet results = oeffnen(file))
    {
      ZeilenDurchlauf durchlauf = new ZeilenDurchlauf(results);
      List<String> reihenfolge = new ArrayList<>();
      while (durchlauf.next())
      {
        reihenfolge.add(results.getString("id") + ":" + results.getRow());
      }
      return reihenfolge;
    }
  }

  /** Gespeicherter Stand des Mitglieds mit diesem Vornamen. */
  private Map<String, Object> persistiertNachVorname(String vorname)
  {
    return datenbank.values().stream()
        .filter(werte -> vorname.equals(werte.get("Vorname"))).findFirst()
        .orElseThrow(
            () -> new AssertionError("Mitglied nicht gespeichert: " + vorname));
  }

  /** Legt ein bereits vorhandenes Mitglied in der Fake-DB an. */
  private void vorhandenesMitglied(String name, String vorname,
      Beitragsgruppe beitragsgruppe) throws Exception
  {
    Mitglied m = (Mitglied) Einstellungen.getDBService()
        .createObject(Mitglied.class, null);
    m.setName(name);
    m.setVorname(vorname);
    m.setBeitragsgruppe(beitragsgruppe);
    m.store();
  }

  private static String baum(String... zeilen)
  {
    return String.join("\n", zeilen) + "\n";
  }

  // ---- Reihenfolge der Verarbeitung ----

  @Test
  void zeileKommtNachDerZeileAufDieSieVerweist() throws Exception
  {
    // Zeile 1 (Anna) verweist auf -1 (Zeile 2), Zeile 4 (Eva) verweist auf
    // Zeile 3
    assertEquals(List.of("-1:2", "-2:3", ":5", ":1", ":4"),
        durchlaufen(resource(FAMILIEN)));
  }

  @Test
  void ohneVerweiseBleibtDieReihenfolgeDerDatei() throws Exception
  {
    assertEquals(List.of("2:1", "1:2", "3:3"),
        durchlaufen(csv("id;name", "2;Mustermann", "1;Mustermann", "3;Meier")));
  }

  // ---- Beispieldateien ----

  @Test
  void beispieldateiLokaleVerweiseImport() throws Exception
  {
    importieren(resource(FAMILIEN));

    verify(monitor, never()).log(anyString());
    assertEquals(5, datenbank.size());
    String baum = familienverbaende();
    System.out.println("Familienverbände aus " + FAMILIEN + ":\n" + baum);
    assertEquals(baum("Meier, Hans (2, Vollzahler)",
        "`-- Meier, Eva (5, Angehoeriger)", "Mustermann, Max (1, Vollzahler)",
        "`-- Mustermann, Anna (4, Angehoeriger)"), baum);
  }

  @Test
  void beispieldateiBestehendeMitglieder() throws Exception
  {
    Beitragsgruppe bg = null;
    DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    it.addFilter("bezeichnung = ?", "Vollzahler");
    if (it.hasNext())
    {
      bg = it.next();
    }
    vorhandenesMitglied("Meier", "Hans", bg); // bekommt die ID 1
    vorhandenesMitglied("Mustermann", "Max", bg); // bekommt die ID 2

    importieren(resource(VERWEISE));

    verify(monitor, never()).log(anyString());
    String baum = familienverbaende();
    System.out.println("Familienverbände aus " + VERWEISE + ":\n" + baum);
    assertEquals(baum("Meier, Hans (1, Vollzahler)",
        "|-- Meier, Ben (5, Angehoeriger)", "`-- Meier, Eva (8, Angehoeriger)",
        "Mustermann, Max (2, Vollzahler)",
        "|-- Mustermann, Anna (6, Angehoeriger)",
        "`-- Mustermann, Tom (7, Angehoeriger)",
        "Wichtig, Willi (3, Vollzahler)",
        "`-- Wichtig, Ulla (4, Angehoeriger)"), baum);
  }

  // ---- Datenbank-ID (voll_zahlerid) ----

  @Test
  void zahleridIstEineDatenbankId() throws Exception
  {
    vorhandenesMitglied("Zahler", "Paul", null); // ID 1

    importieren(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;1;"));

    verify(monitor, never()).log(anyString());
    assertEquals(Long.valueOf(1),
        persistiertNachVorname("Anna").get("VollZahlerID"));
  }

  @Test
  void unbekannteDatenbankIdBrichtImportAb() throws Exception
  {
    importieren(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;99;"));

    verify(monitor).log(contains("Zeile 1: Vollzahler nicht gefunden: 99"));
    assertTrue(datenbank.isEmpty());
  }

  @Test
  void fehlendeLokaleidBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, ";Mustermann;Anna;Angehoeriger;-2;",
        "-1;Mustermann;Berta;Vollzahler;;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Vollzahler für Id -2 nicht gefunden."));
  }

  @Test
  void vollzahleridKeinIntegerdBrichtImportAb() throws Exception
  {
    importieren(csv(DB_HEADER, "Mustermann;Anna;Angehoeriger;abc;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1 : Vollzahler Id abc ist keine gültige Integer Zahl."));
  }

  @Test
  void mitgliedidKeinIntegerBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "abc;Mustermann;Anna;Angehoeriger;;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Mitglied ID abc ist keine gültige Integer Zahl."));
  }

  @Test
  void vollzahleridFehltBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, ";Mustermann;Anna;Angehoeriger;;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Vollzahler Id ist nicht gesetzt."));
  }

  @Test
  void doppelteIdBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "-1;Mustermann;Anna;Angehoeriger;-1;",
        "-1;Mustermann;Berta;Vollzahler;;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Mitglied ID -1 wurde bereits bei einem anderen Mitglied verwendet."));
  }

  @Test
  void falscheBeitragsgruppeBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, ";Mustermann;Anna;Gibtesnicht;-1;",
        "-1;Mustermann;Berta;Vollzahler;;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Beitragsgruppe nicht gefunden: Gibtesnicht"));
  }

  @Test
  void vollzahlerReferenziertVollzahlerBrichtImportAb() throws Exception
  {
    Beitragsgruppe bg = null;
    DBIterator<Beitragsgruppe> it = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    it.addFilter("bezeichnung = ?", "Vollzahler");
    if (it.hasNext())
    {
      bg = it.next();
    }
    vorhandenesMitglied("Meier", "Hans", bg); // bekommt die ID 1

    importieren(csv(DB_HEADER, "Mustermann;Anna;Vollzahler;1;"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Vollzahler Id ist nur bei Beitragsgruppen der Art Familienangehöriger erlaubt: 1"));
  }

  @Test
  void fehlenderAbweichenderZahlerBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, ";Mustermann;Anna;Vollzahler;;99"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Abweichenden Zahler nicht gefunden: 99"));
  }

  @Test
  void abweichenderZahlerKeinIntegerBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, ";Mustermann;Anna;Vollzahler;;abc"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 1: Abweichender Zahler Id abc ist keine gültige Integer Zahl."));
  }

  @Test
  void abweichenderZahlerNichtGefundenBrichtImportAb() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "-1;Mustermann;Anna;Vollzahler;;",
        ";Mustermann;Tom;Vollzahler;;-2"));

    verify(monitor).log(contains(
        "Import abgebrochen: Zeile 2: Abweichender Zahler für Id -2 nicht gefunden."));
  }

  @Test
  void abweichenderZahlerGefundenImportOk() throws Exception
  {
    importieren(csv(ALLE_SPALTEN, "-1;Mustermann;Anna;Vollzahler;;",
        ";Mustermann;Tom;Vollzahler;;-1"));
  }
}
