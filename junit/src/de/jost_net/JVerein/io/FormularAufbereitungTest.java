package de.jost_net.JVerein.io;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.Ausrichtung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.util.ApplicationException;

public class FormularAufbereitungTest
{

  @Test
  @DisplayName("Formularaufbereitng und ZUGFeRD Rechnung testen")
  void formularTest()
      throws ApplicationException, IOException, DocumentException
  {
    try (MockedStatic<Einstellungen> einstellungen = Mockito
        .mockStatic(Einstellungen.class))
    {
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
      einstellungen.when(
          () -> Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
          .thenReturn(false);

      Formularfeld formularfeld = mock(Formularfeld.class);
      doReturn("test").when(formularfeld).getName();
      doReturn("FreeSans").when(formularfeld).getFont();
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
      aufbereitung.writeForm(formular,
          Collections.singletonMap("test", "Test"));
      aufbereitung.closeFormular();

      aufbereitung.addZUGFeRD(rechnung, false);

      file.deleteOnExit();
    }
  }
}
