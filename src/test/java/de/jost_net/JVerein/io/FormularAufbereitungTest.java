package de.jost_net.JVerein.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.Ausrichtung;
import de.jost_net.JVerein.keys.Fonts;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.redsix.pdfcompare.PdfComparator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.util.ApplicationException;

public class FormularAufbereitungTest
{

  private static MockedStatic<Einstellungen> einstellungen;

  @BeforeAll
  static void init()
  {
    einstellungen = Mockito.mockStatic(Einstellungen.class);

    einstellungen.when(() -> Einstellungen.getEinstellung(Property.NAME))
        .thenReturn("Testverein");
    einstellungen.when(() -> Einstellungen.getEinstellung(Property.STRASSE))
        .thenReturn("Testweg");
    einstellungen.when(() -> Einstellungen.getEinstellung(Property.PLZ))
        .thenReturn("10000");
    einstellungen.when(() -> Einstellungen.getEinstellung(Property.ORT))
        .thenReturn("Testhausen");
    einstellungen.when(() -> Einstellungen.getEinstellung(Property.STAAT))
        .thenReturn("DE");
    einstellungen
        .when(() -> Einstellungen.getEinstellung(Property.STEUERNUMMER))
        .thenReturn("01/001/001");
    einstellungen.when(() -> Einstellungen.getEinstellung(Property.USTID))
        .thenReturn("DE121212121");
    einstellungen
        .when(() -> Einstellungen.getEinstellung(Property.GLAEUBIGERID))
        .thenReturn("DE00000000");
    einstellungen
        .when(
            () -> Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
        .thenReturn(false);
  }

  @AfterAll
  static void cleanup()
  {
    einstellungen.close();
  }

  @Test
  @DisplayName("Formularaufbereitng und ZUGFeRD Rechnung testen")
  void formularTest()
      throws ApplicationException, IOException, DocumentException
  {
    Formularfeld formularfeld = mock(Formularfeld.class);
    doReturn("test").when(formularfeld).getName();
    doReturn(Fonts.FreeSans.getName()).when(formularfeld).getFont();
    doReturn(20d).when(formularfeld).getX();
    doReturn(120d).when(formularfeld).getY();
    doReturn(Ausrichtung.LINKS).when(formularfeld).getAusrichtung();
    doReturn(8).when(formularfeld).getFontsize();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter writer = PdfWriter.getInstance(doc, bos);
    doc.open();
    doc.newPage();
    doc.add(new Paragraph("Formulartest"));
    doc.close();
    writer.close();

    Formular formular = mock(Formular.class);
    doReturn(1).when(formular).getZaehler();
    doReturn(bos.toByteArray()).when(formular).getInhalt();
    doReturn(PseudoIterator.fromArray(new Formularfeld[] { formularfeld }))
        .when(formular).getFormularfelder(1);

    Sollbuchung sollbuchung = mock(Sollbuchung.class);
    doReturn(new Date()).when(sollbuchung).getDatum();

    SollbuchungPosition sbp = mock(SollbuchungPosition.class);
    doReturn(100d).when(sbp).getNettobetrag();
    doReturn("Mitgliedsbeitrag").when(sbp).getZweck();
    doReturn(7d).when(sbp).getSteuersatz();

    Mitglied mitglied = mock(Mitglied.class);
    doReturn("0100").when(mitglied).getTelefonprivat();
    doReturn("test@test.de").when(mitglied).getEmail();

    Rechnung rechnung = mock(Rechnung.class);
    doReturn(Arrays.asList(sollbuchung)).when(rechnung).getSollbuchungList();
    doReturn(new Date()).when(rechnung).getDatum();
    doReturn("111").when(rechnung).getID();
    doReturn(new Zahlungsweg(Zahlungsweg.BASISLASTSCHRIFT)).when(rechnung)
        .getZahlungsweg();
    doReturn("DE00").when(rechnung).getIBAN();
    doReturn("1111").when(rechnung).getMandatID();
    doReturn(mitglied).when(rechnung).getMitglied();
    doReturn("Hans").when(rechnung).getVorname();
    doReturn("Meier").when(rechnung).getName();
    doReturn("Testweg").when(rechnung).getStrasse();
    doReturn("10000").when(rechnung).getPlz();
    doReturn("Testhausen").when(rechnung).getOrt();
    doReturn("").when(rechnung).getAdressierungszusatz();
    doReturn(Arrays.asList(sbp)).when(rechnung).getSollbuchungPositionList();
    doReturn(107d).when(rechnung).getBetrag();

    File file = File.createTempFile("formular", ".pdf");

    FormularAufbereitung aufbereitung = new FormularAufbereitung(file, true,
        false);
    aufbereitung.writeForm(formular, Collections.singletonMap("test", "Test"));
    aufbereitung.closeFormular();

    aufbereitung.addZUGFeRD(rechnung, false);

    file.deleteOnExit();
  }

  @Test
  @DisplayName("Html in Formular testen")
  void formularHtmlTest()
      throws ApplicationException, IOException, DocumentException
  {
    Formularfeld feld1 = mock(Formularfeld.class);
    doReturn(
        "<table border='1'><tr><td>Spalte</td><td><b>Fett</b></td><td><i>Kursiv</i></td></tr>"
            + "<tr><td><strong>Strong</strong></td><td><small>Klein</small></td><td><s>Durchgestrichen</s></td></tr></table>"
            + "<ul style='list-style-type:\"-\"'><li>Aufzählung</li><li>mit mehreren</li><li>Punkten</li></ul>")
                .when(feld1).getName();
    doReturn(Fonts.FreeSans.getName()).when(feld1).getFont();
    doReturn(30d).when(feld1).getX();
    doReturn(260d).when(feld1).getY();
    doReturn(Ausrichtung.LINKS).when(feld1).getAusrichtung();
    doReturn(10).when(feld1).getFontsize();

    Formularfeld feld2 = mock(Formularfeld.class);
    doReturn(
        "<p>Feld mit ungültiger Font,<br /> Fallback soll verwenet werden</p>")
            .when(feld2).getName();
    doReturn("font-gibt-es-nicht").when(feld2).getFont();
    doReturn(170d).when(feld2).getX();
    doReturn(180d).when(feld2).getY();
    doReturn(Ausrichtung.RECHTS).when(feld2).getAusrichtung();
    doReturn(15).when(feld2).getFontsize();

    Formularfeld feld3 = mock(Formularfeld.class);
    doReturn("<p>Feld über mehrere Seiten.</p>"
        + "<div style='width:60px'><p>Das ist ein langer Text, die Breite ist per CSS festgelegt.</p></div>[[newPage]]"
        + "<p>Das steht auf der 2. Seite an der gleichen Position wie auf der 1. Seite.</p>")
            .when(feld3).getName();
    doReturn(Fonts.CourierPrime.getName()).when(feld3).getFont();
    doReturn(40d).when(feld3).getX();
    doReturn(220d).when(feld3).getY();
    doReturn(Ausrichtung.MITTE).when(feld3).getAusrichtung();
    doReturn(12).when(feld3).getFontsize();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter writer = PdfWriter.getInstance(doc, bos);
    doc.open();
    doc.newPage();
    doc.add(new Paragraph("Formulartest"));
    doc.close();
    writer.close();

    Formular formular = mock(Formular.class);
    doReturn(1).when(formular).getZaehler();
    doReturn(bos.toByteArray()).when(formular).getInhalt();
    doReturn(
        PseudoIterator.fromArray(new Formularfeld[] { feld1, feld2, feld3 }))
            .when(formular).getFormularfelder(1);

    File file = File.createTempFile("formular", ".pdf");

    FormularAufbereitung aufbereitung = new FormularAufbereitung(file, true,
        false);
    aufbereitung.writeForm(formular, Collections.singletonMap("test", "Test"));
    aufbereitung.closeFormular();

    // Erstelltes PDF mit Soll-PDF vergleichen
    String soll = getClass().getClassLoader()
        .getResource("formular-test-html.pdf").getFile();
    assertTrue(
        new PdfComparator<>(soll, file.getAbsolutePath()).compare().isEqual(),
        "Das PDF das aus einem HTML-Formular generiert wurde, sieht nicht wie erwartet aus.");

    file.deleteOnExit();
  }

  @Test
  @DisplayName("Ungültiges XHtml in Formular testen")
  void formularUngueltigesHtmlTest()
      throws ApplicationException, IOException, DocumentException
  {
    Formularfeld formularfeld = mock(Formularfeld.class);
    doReturn("<p>Test<div></p>").when(formularfeld).getName();
    doReturn(Fonts.FreeSans.getName()).when(formularfeld).getFont();
    doReturn(30d).when(formularfeld).getX();
    doReturn(260d).when(formularfeld).getY();
    doReturn(Ausrichtung.LINKS).when(formularfeld).getAusrichtung();
    doReturn(10).when(formularfeld).getFontsize();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Document doc = new Document();
    PdfWriter writer = PdfWriter.getInstance(doc, bos);
    doc.open();
    doc.newPage();
    doc.add(new Paragraph("Formulartest"));
    doc.close();
    writer.close();

    Formular formular = mock(Formular.class);
    doReturn(1).when(formular).getZaehler();
    doReturn(bos.toByteArray()).when(formular).getInhalt();
    doReturn(PseudoIterator.fromArray(new Formularfeld[] { formularfeld }))
        .when(formular).getFormularfelder(1);

    File file = File.createTempFile("formular", ".pdf");

    FormularAufbereitung aufbereitung = new FormularAufbereitung(file, false,
        false);
    assertThrows(RuntimeWorkerException.class, () -> aufbereitung
        .writeForm(formular, Collections.singletonMap("test", "Test")));

    aufbereitung.closeFormular();

    file.deleteOnExit();
  }
}
