/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 **********************************************************************/
package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.sql.Date;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.system.Application;

class AuswertungGeburtstagslistePDFTest
{
  @TempDir
  Path directory;

  private final List<RenderedText> rendered = new ArrayList<>();

  private int pageCount;

  private record RenderedText(String text, int page, float x, float y,
      float height)
  {
  }

  @Test
  void offersPdfForMembers()
  {
    AuswertungGeburtstagslistePDF exporter = new AuswertungGeburtstagslistePDF();
    IOFormat[] formats = exporter.getIOFormats(MitgliedListeView.class);
    assertEquals(1, formats.length);
    assertEquals("Geburtstagsliste PDF", formats[0].getName());
    assertArrayEquals(new String[] { "*.pdf" },
        formats[0].getFileExtensions());
    assertNull(exporter.getIOFormats(NichtMitgliedListeView.class));
    assertNull(exporter.getAusgabeParameter(null));
  }

  @Test
  void groupsAllMonthsAndSortsByDayRegardlessOfBirthYear() throws Exception
  {
    List<Mitglied> members = List.of(
        member("1984-03-25", "Gerd", "Müller"),
        member("2000-12-31", "Eva", "Winter"),
        member("1960-03-30", "Anna", "Ende"),
        member("2004-02-29", "Leo", "Schaltjahr"),
        member("2010-03-02", "Ben", "Anfang"),
        member("1990-01-01", "Ute", "Neujahr"));
    String text = export(members);

    assertMonthsInOrder(text);
    assertInOrder(text, "Januar", "1.1.1990 Ute Neujahr (36)", "Februar",
        "29.2.2004 Leo Schaltjahr (22)", "März",
        "2.3.2010 Ben Anfang (16)", "25.3.1984 Gerd Müller (42)",
        "30.3.1960 Anna Ende (66)", "April", "Dezember",
        "31.12.2000 Eva Winter (26)");
    RenderedText first = position("2.3.2010 Ben Anfang (16)");
    RenderedText second = position("25.3.1984 Gerd Müller (42)");
    RenderedText third = position("30.3.1960 Anna Ende (66)");
    assertTrue(second.x() > first.x());
    assertTrue(third.x() > second.x());
    assertEquals(first.y(), second.y(), 0.01);
    assertEquals(first.y(), third.y(), 0.01);
    assertFalse(text.contains("ohne Geburtsdatum"));
    assertEquals("Gerd", members.get(0).getVorname());
  }

  @Test
  void keepsEmptyMonthsAndReportsMissingBirthdays() throws Exception
  {
    String text = export(List.of(member(null, "Ohne", "Datum")));
    assertMonthsInOrder(text);
    assertTrue(text.contains(
        "Mitglieder ohne Geburtsdatum (nicht aufgeführt): 1"));
    assertFalse(text.contains("Ohne Datum"));
  }

  @Test
  void exportsAllMonthsForAnEmptyList() throws Exception
  {
    String text = export(List.of());
    assertMonthsInOrder(text);
    assertTrue(text.contains("Geburtstagsliste 2026"));
    assertFalse(text.contains("Geburtstage 2026"));
    position("Geburtstagsliste 2026");
    assertFalse(text.contains("ohne Geburtsdatum"));
  }

  @Test
  void preservesEveryMemberAcrossPageBreaks() throws Exception
  {
    List<Mitglied> members = new ArrayList<>();
    for (int i = 0; i < 150; i++)
    {
      members.add(member("1984-03-25", "Mitglied", "Nummer" + i));
    }
    String text = export(members);
    assertTrue(pageCount > 1);
    assertMonthsInOrder(text);
    for (int i = 0; i < members.size(); i++)
    {
      assertTrue(text.contains("25.3.1984 Mitglied Nummer" + i + " (42)"));
    }
  }

  @Test
  void fillsColumnsTopToBottomWithAnUnevenNumberOfBirthdays() throws Exception
  {
    List<Mitglied> members = new ArrayList<>();
    for (int day = 7; day >= 1; day--)
    {
      members.add(member("1984-03-0" + day, "Gerd", "Muster"));
    }
    export(members);
    RenderedText first = position("1.3.1984 Gerd Muster (42)");
    RenderedText fourth = position("4.3.1984 Gerd Muster (42)");
    RenderedText seventh = position("7.3.1984 Gerd Muster (42)");
    assertTrue(fourth.x() > first.x());
    assertTrue(seventh.x() > fourth.x());
    assertEquals(first.y(), fourth.y(), 0.01);
    assertEquals(first.y(), seventh.y(), 0.01);
    for (int day = 1; day <= 7; day++)
    {
      RenderedText entry = position(day + ".3.1984 Gerd Muster (42)");
      assertEquals(first.page(), entry.page());
      float x = day <= 3 ? first.x() : day <= 6 ? fourth.x() : seventh.x();
      assertEquals(x, entry.x(), 0.01);
      if (day != 1 && day != 4 && day != 7)
      {
        assertTrue(position((day - 1) + ".3.1984 Gerd Muster (42)")
            .y() > entry.y());
      }
    }
  }

  @Test
  void fitsFortyEightBirthdaysOnOnePageWithConfiguredMonthHeadings()
      throws Exception
  {
    List<Mitglied> members = new ArrayList<>();
    for (int month = 1; month <= 12; month++)
    {
      for (int day = 1; day <= 4; day++)
      {
        members.add(member(String.format("1984-%02d-%02d", month, day),
            "Gerd", "Muster"));
      }
    }
    String text = export(members);
    assertMonthsInOrder(text);
    assertEquals(1, pageCount);
    for (int month = 1; month <= 12; month++)
    {
      for (int day = 1; day <= 4; day++)
      {
        position(day + "." + month + ".1984 Gerd Muster (42)");
      }
    }
    BaseFont font = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
        .getCalculatedBaseFont(false);
    float height = font.getFontDescriptor(BaseFont.ASCENT, 12)
        - font.getFontDescriptor(BaseFont.DESCENT, 12);
    for (String month : List.of("Januar", "Februar", "März", "April",
        "Mai", "Juni", "Juli", "August", "September", "Oktober", "November",
        "Dezember"))
    {
      assertEquals(height, position(month).height(), 0.01);
    }
    assertFalse(text.contains("Geburtstage 2026"));
  }

  @Test
  void wrapsLongNamesWithoutLosingTextOrOverlappingTheNextRow()
      throws Exception
  {
    String name = "Anna Maria Elisabeth";
    String surname = "von Musterhausen und Beispielstadt";
    export(List.of(member("1984-03-01", name, surname),
        member("1984-03-02", "Ben", "Muster"),
        member("1984-03-03", "Gerd", "Muster"),
        member("1984-03-04", "Eva", "Muster")));
    String text = rendered.stream().map(RenderedText::text)
        .collect(Collectors.joining(" ")).replaceAll("\\s+", " ");
    assertTrue(text.contains("1.3.1984 " + name + " " + surname + " (42)"));
    RenderedText nextRow = position("2.3.1984 Ben Muster (42)");
    RenderedText rightColumn = position("3.3.1984 Gerd Muster (42)");
    List<RenderedText> wrappedLines = rendered.stream()
        .filter(entry -> entry.text().contains("1.3.1984")
            || entry.text().contains("Beispielstadt"))
        .toList();
    assertEquals(2, wrappedLines.size());
    for (RenderedText line : wrappedLines)
    {
      assertTrue(line.x() < rightColumn.x());
      assertTrue(line.y() > nextRow.y());
    }
  }

  @Test
  void propagatesMemberReadErrors() throws Exception
  {
    Mitglied member = mock(Mitglied.class);
    when(member.getGeburtsdatum()).thenThrow(new RemoteException("Lesefehler"));
    assertThrows(RemoteException.class, () -> export(List.of(member)));
  }

  private Mitglied member(String birthday, String firstName, String lastName)
      throws RemoteException
  {
    Mitglied member = mock(Mitglied.class);
    when(member.getGeburtsdatum())
        .thenReturn(birthday == null ? null : Date.valueOf(birthday));
    when(member.getPersonenart()).thenReturn("n");
    when(member.getVorname()).thenReturn(firstName);
    when(member.getName()).thenReturn(lastName);
    return member;
  }

  private String export(List<Mitglied> members) throws Exception
  {
    ExportLayoutParam params = new ExportLayoutParam();
    params.setLinks(30);
    params.setRechts(30);
    params.setOben(30);
    params.setUnten(30);
    params.setQuerformat(false);
    params.setFontHeader(new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
    params.setFontNormal(new Font(Font.FontFamily.HELVETICA, 10));
    File file = directory.resolve("geburtstage.pdf").toFile();
    Year year = Year.of(2026);
    try (MockedStatic<Application> application = mockStatic(Application.class,
        RETURNS_DEEP_STUBS);
        MockedStatic<Einstellungen> settings = mockStatic(Einstellungen.class);
        MockedStatic<Year> years = mockStatic(Year.class))
    {
      var pluginLoader = Application.getPluginLoader();
      doReturn(mock(JVereinPlugin.class, RETURNS_DEEP_STUBS))
          .when(pluginLoader).getPlugin(JVereinPlugin.class);
      settings.when(() -> Einstellungen
          .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT))
          .thenReturn(false);
      settings.when(() -> Einstellungen
          .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT))
          .thenReturn(false);
      years.when(Year::now).thenReturn(year);
      AuswertungGeburtstagslistePDF exporter = new AuswertungGeburtstagslistePDF();
      params.setTitle(exporter.getTitle(null));
      assertEquals("Geburtstagsliste 2026", params.getTitle());
      exporter.doExport(new Object[] { members },
          exporter.getIOFormats(MitgliedListeView.class)[0], file, params,
          null);
    }
    PdfReader reader = new PdfReader(Files.readAllBytes(file.toPath()));
    try
    {
      rendered.clear();
      pageCount = reader.getNumberOfPages();
      StringBuilder text = new StringBuilder();
      for (int page = 1; page <= pageCount; page++)
      {
        final int pageNumber = page;
        text.append(PdfTextExtractor.getTextFromPage(reader, page,
            new LocationTextExtractionStrategy()
            {
              @Override
              public void renderText(TextRenderInfo info)
              {
                super.renderText(info);
                Vector start = info.getBaseline().getStartPoint();
                float height = info.getAscentLine().getStartPoint()
                    .subtract(info.getDescentLine().getStartPoint()).length();
                rendered.add(new RenderedText(info.getText(), pageNumber,
                    start.get(Vector.I1), start.get(Vector.I2), height));
              }
            })).append('\n');
      }
      return text.toString();
    }
    finally
    {
      reader.close();
    }
  }

  private RenderedText position(String text)
  {
    List<RenderedText> matches = rendered.stream()
        .filter(entry -> entry.text().equals(text)).toList();
    assertEquals(1, matches.size(), text);
    return matches.get(0);
  }

  private void assertMonthsInOrder(String text)
  {
    assertInOrder(text, "Januar", "Februar", "März", "April", "Mai",
        "Juni", "Juli", "August", "September", "Oktober", "November",
        "Dezember");
  }

  private void assertInOrder(String text, String... entries)
  {
    int previous = -1;
    for (String entry : entries)
    {
      int index = text.indexOf(entry);
      assertTrue(index > previous, "Fehlt oder falsch sortiert: " + entry);
      previous = index;
    }
  }
}
