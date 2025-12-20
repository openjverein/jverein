/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfAConformanceException;
import com.itextpdf.text.pdf.PdfPCell;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatJJJJ;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;

/**
 * Action zur Generierung von Spendenbescheinigungen aus der Datenbank.<br>
 * Diese Klasse kapselt die Generierung des Standard-Formulars und wird auch bei
 * der Generierung eines Dokuments aus der Detailansicht der
 * Spendenbescheinigung heraus verwendet.
 */
public class SpendenbescheinigungAusgabe extends AbstractAusgabe
{

  private Adressblatt adressblatt = Adressblatt.OHNE_ADRESSBLATT;

  private String text = null;

  private FileOutputStream fos;

  private Reporter rpt;

  public SpendenbescheinigungAusgabe(String text, Adressblatt adressblatt)
  {
    this.adressblatt = adressblatt;
    this.text = text;
  }

  @Override
  public void aufbereiten(ArrayList<? extends DBObject> list, Ausgabeart art,
      String betreff, String text, boolean pdfa, boolean encrypt)
      throws IOException, ApplicationException, PdfAConformanceException,
      DocumentException
  {
    GregorianCalendar gc = new GregorianCalendar();
    boolean standard = false;
    boolean individuell = false;
    for (DBObject o : list)
    {
      Spendenbescheinigung spb = (Spendenbescheinigung) o;
      gc.setTime(spb.getBescheinigungsdatum());
      Formular spendeformular = spb.getFormular();
      if (spendeformular == null && (gc.get(GregorianCalendar.YEAR) <= 2013))
      {
        throw new ApplicationException(
            "Standard Spendenbescheinigungen vor 2014 werden nicht mehr unterstützt!");
      }
      if (spendeformular == null)
      {
        standard = true;
      }
      else
      {
        individuell = true;
      }
      if (individuell && standard && art == Ausgabeart.PDF)
      {
        throw new ApplicationException(
            "PDF mit Standard und individuellen Spendenbescheinigungen wird nicht unterstützt!");
      }
    }
    super.aufbereiten(list, art, betreff, text, pdfa, encrypt);
  }

  /**
   * Generierung des Standard-Dokumentes zu verwenden für Spendenbescheinigungen
   * ab 01.01.2014
   * 
   * @param spb
   *          Die Spendenbescheinigung aus der Datenbank
   * @param fileName
   *          Der Dateiname, wohin das Dokument geschrieben werden soll
   * @throws IOException
   * @throws DocumentException
   */
  private void generiereSpendenbescheinigungStandardAb2014(
      Spendenbescheinigung spb, File file, Adressblatt adressblatt)
      throws IOException, DocumentException
  {
    if (fos == null)
    {
      fos = new FileOutputStream(file);
    }
    Map<String, Object> map = new SpendenbescheinigungMap().getMap(spb, null);
    map = new AllgemeineMap().getMap(map);
    boolean isSammelbestaetigung = spb.isSammelbestaetigung();
    if (rpt == null)
    {
      rpt = new Reporter(fos, 80, 50, 30, 20, true);
    }
    else
    {
      rpt.newPage();
    }

    // Aussteller, kein Header
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(
        "Aussteller (Bezeichnung und Anschrift der steuerbegünstigten Einrichtung)"
            + "\n\n" + getAussteller() + "\n ",
        Element.ALIGN_LEFT, (BaseColor) null);
    rpt.closeTable();

    rpt.add(new Paragraph(" ", Reporter.getFreeSans(4)));
    if (isSammelbestaetigung)
    {
      rpt.add("Sammelbestätigung über "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 9);
    }
    else
    {
      rpt.add("Bestätigung über "
          + map.get(SpendenbescheinigungVar.SPENDEART.getName()), 9);
    }
    rpt.addLight(
        "im Sinne des § 10b des Einkommenssteuergesetzes an eine der in § 5 Abs. 1 Nr. 9 des "
            + "Körperschaftssteuergesetzes bezeichneten Körperschaften, Personenvereinigungen "
            + "oder Vermögensmassen\n",
        8);

    // Name und Anschrift, kein Header
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(
        "Name und Anschrift des Zuwendenden\n\n"
            + (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()),
        Element.ALIGN_LEFT, (BaseColor) null);
    rpt.closeTable();

    // Betrag und Tag der Zuwendeung, kein Header
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 150, BaseColor.LIGHT_GRAY);
    if (!isSammelbestaetigung)
    {
      rpt.addHeaderColumn("", Element.ALIGN_CENTER, 50, BaseColor.LIGHT_GRAY);
    }
    else
    {
      rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    }
    rpt.createHeader();
    if (spb.getSpendenart() == Spendenart.SACHSPENDE)
    {
      rpt.addColumn(
          "Wert der Zuwendung -in Ziffern-\n" + "-"
              + Einstellungen.DECIMALFORMAT.format(
                  map.get(SpendenbescheinigungVar.BETRAG.getName()))
              + "-",
          Element.ALIGN_CENTER, (BaseColor) null);
    }
    else
    {
      rpt.addColumn(
          "Betrag der Zuwendung -in Ziffern-\n" + "-"
              + Einstellungen.DECIMALFORMAT.format(
                  map.get(SpendenbescheinigungVar.BETRAG.getName()))
              + "-",
          Element.ALIGN_CENTER, (BaseColor) null);
    }
    rpt.addColumn("-in Buchstaben-\n"
        + (String) map.get(SpendenbescheinigungVar.BETRAGINWORTEN.getName()),
        Element.ALIGN_CENTER, (BaseColor) null);
    if (!isSammelbestaetigung)
    {
      rpt.addColumn(
          "Tag der Zuwendung\n"
              + (String) map.get(SpendenbescheinigungVar.SPENDEDATUM.getName()),
          Element.ALIGN_LEFT, (BaseColor) null);
    }
    else
    {
      rpt.addColumn("Zeitraum der Sammelbestätigung\n"
          + (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          Element.ALIGN_LEFT, (BaseColor) null);
    }
    rpt.closeTable();

    if (spb.getSpendenart() == Spendenart.SACHSPENDE)
    {
      rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      rpt.addColumn(
          "Genaue Bezeichnung der Sachzuwendung mit Alter, Zustand, Kaufpreis usw.\n\n"
              + spb.getBezeichnungSachzuwendung(),
          Element.ALIGN_LEFT);
      rpt.closeTable();

      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getHerkunftSpende() == HerkunftSpende.BETRIEBSVERMOEGEN)
        p.add(new Chunk((char) 53,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(
          "     Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Betriebsvermögen. "
              + "Die Zuwendung wurde mit dem Wert der Entnahme (ggf. mit dem niedrigeren gemeinen "
              + "Wert) und nach der Umsatzsteuer, die auf die Entnahme entfällt, bewertet.\n");
      rpt.add(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getHerkunftSpende() == HerkunftSpende.PRIVATVERMOEGEN)
        p.add(new Chunk((char) 53,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(
          "     Die Sachzuwendung stammt nach den Angaben des Zuwendenden aus dem Privatvermögen.\n");
      rpt.add(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getHerkunftSpende() == HerkunftSpende.KEINEANGABEN)
        p.add(new Chunk((char) 53,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(
          "     Der Zuwendende hat trotz Aufforderung keine Angaben zur Herkunft der Sachzuwendung gemacht.\n");
      rpt.add(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      if (spb.getUnterlagenWertermittlung())
        p.add(new Chunk((char) 53,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      else
        p.add(new Chunk((char) 113,
            FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(
          "     Geeignete Unterlagen, die zur Wertermittlung gedient haben, z. B. Rechnung, Gutachten, liegen vor.\n");
      rpt.add(p);
    }

    /*
     * Bei Sammelbestätigungen ist der Verweis auf Verzicht in der Anlage
     * vermerkt
     */
    char verzichtJa = (char) 113; // box leer
    char verzichtNein = (char) 53; // X

    if (!isSammelbestaetigung && spb.getSpendenart() != Spendenart.SACHSPENDE)
    {
      if (spb.getBuchungen().get(0).getVerzicht().booleanValue())
      {
        verzichtJa = (char) 53; // X
        verzichtNein = (char) 113; // box leer
      }
      Paragraph p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_LEFT);
      p.add(new Chunk(
          "Es handelt sich um den Verzicht auf Erstattung von Aufwendungen "));
      p.add(new Chunk(" Ja ", Reporter.getFreeSansBold(8)));
      p.add(new Chunk(verzichtJa,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8)));
      p.add(new Chunk("   Nein ", Reporter.getFreeSansBold(8)));
      p.add(new Chunk(verzichtNein,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8)));
      p.add(new Chunk("\n\n"));
      rpt.add(p);
    }
    else
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(8)));
    }
    if ((Boolean) Einstellungen.getEinstellung(Property.VORLAEUFIG))
    {
      // Verein neu gegründet, hat noch keinen Bescheid
      String txt = "     Wir sind wegen "
          + "Förderung (Angabe des begünstigten Zweck / der begünstigten Zwecke) ...............\n"
          + "nach dem Freistellungsbescheid bzw. nach der Anlage zum Körperschaftssteuerbescheid "
          + "des Finanzamtes ..........\n" + ", StNr. .........."
          + ", vom ..........."
          + " für den letzten Veranlagungszeitraum ........"
          + " nach § 5 Abs. 1 Nr. 9 des Körperschaftsteuergesetzes von der Körperschaftsteuer und nach "
          + "§ 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.\n ";
      Paragraph p = new Paragraph();
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 113,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(txt);
      rpt.add(p);

      txt = "     Die Einhaltung der satzungsgemäßen Voraussetzungen nach den §§ 51, 59, 60 und 61 "
          + "AO wurde vom Finanzamt "
          + (String) Einstellungen.getEinstellung(Property.FINANZAMT)
          + ", StNr. "
          + (String) Einstellungen.getEinstellung(Property.STEUERNUMMER)
          + ", mit Bescheid vom "
          + new JVDateFormatTTMMJJJJ().format(
              (Date) Einstellungen.getEinstellung(Property.BESCHEIDDATUM))
          + " nach § 60a AO gesondert festgestellt. Wir fördern nach unserer Satzung "
          + (String) Einstellungen.getEinstellung(Property.BEGUENSTIGTERZWECK)
          + ".";
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 53,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      p.add(txt);
      rpt.add(p);
    }
    else
    {
      // Verein existiert und hat einen Bescheid bekommen
      String txt = "     Wir sind wegen "
          + (String) Einstellungen.getEinstellung(Property.BEGUENSTIGTERZWECK)
          + " nach dem Freistellungsbescheid bzw. nach der Anlage zum Körperschaftssteuerbescheid "
          + "des Finanzamtes "
          + (String) Einstellungen.getEinstellung(Property.FINANZAMT)
          + ", StNr. "
          + (String) Einstellungen.getEinstellung(Property.STEUERNUMMER)
          + ", vom "
          + new JVDateFormatTTMMJJJJ().format(
              (Date) Einstellungen.getEinstellung(Property.BESCHEIDDATUM))
          + " für den letzten Veranlagungszeitraum "
          + new JVDateFormatJJJJ().format(
              (Date) Einstellungen.getEinstellung(Property.VERANLAGUNGVON))
          + " bis "
          + new JVDateFormatJJJJ().format(
              (Date) Einstellungen.getEinstellung(Property.VERANLAGUNGBIS))
          + " nach § 5 Abs. 1 Nr. 9 des Körperschaftsteuergesetzes von der Körperschaftsteuer und nach "
          + "§ 3 Nr. 6 des Gewerbesteuergesetzes von der Gewerbesteuer befreit.\n ";
      Paragraph p = new Paragraph();
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 53,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // X
      p.add(txt);
      rpt.add(p);
      txt = "     Die Einhaltung der satzungsgemäßen Voraussetzungen nach den §§ 51, 59, 60 und 61 "
          + "AO wurde vom Finanzamt ..........\n" + ", StNr. ..........."
          + ", mit Bescheid vom ............"
          + " nach § 60a AO gesondert festgestellt. Wir fördern nach unserer Satzung "
          + "(Angabe des begünstigten Zweck / der begünstigten Zwecke) ............. .";
      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk((char) 113,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8))); // box leer
      p.add(txt);
      rpt.add(p);

    }

    // Rahmen über Unterschrift
    PdfPCell cell = new PdfPCell();
    Paragraph p = new Paragraph();
    p.setFont(Reporter.getFreeSans(8));
    p.setAlignment(Element.ALIGN_LEFT);
    p.setMultipliedLeading(1.5f);
    p.add(new Chunk("Es wird bestätigt, dass die Zuwendung nur zur "
        + (String) Einstellungen.getEinstellung(Property.BEGUENSTIGTERZWECK)
        + " verwendet wird.\n  "));
    cell.addElement(p);

    if (spb.getSpendenart() != Spendenart.SACHSPENDE)
    {
      p = new Paragraph();
      p.setFont(Reporter.getFreeSansBold(8));
      p.setAlignment(Element.ALIGN_LEFT);
      p.setMultipliedLeading(1.5f);
      p.add(new Chunk(
          "Nur für steuerbegünstigte Einrichtungen, bei denen die Mitgliedsbeiträge "
              + "steuerlich nicht abziehbar sind:\n"));
      cell.addElement(p);

      p = new Paragraph();
      p.setFont(Reporter.getFreeSans(8));
      p.setAlignment(Element.ALIGN_JUSTIFIED);
      p.setFirstLineIndent((float) -17.5);
      p.setIndentationLeft((float) 17.5);
      p.setMultipliedLeading(1.5f);
      char mitgliedBetraege = (char) 113; // box leer
      if (!(Boolean) Einstellungen.getEinstellung(Property.MITGLIEDSBETRAEGE))
      {
        mitgliedBetraege = (char) 53; // X
      }
      p.add(new Chunk(mitgliedBetraege,
          FontFactory.getFont(FontFactory.ZAPFDINGBATS, 8)));
      p.add(new Chunk(
          "   Es wird bestätigt, dass es sich nicht um einen Mitgliedsbeitrag handelt, "
              + "dessen Abzug nach § 10b Abs. 1 des Einkommensteuergesetzes ausgeschlossen ist."));
      cell.addElement(p);
    }

    rpt.add(new Paragraph(" ", Reporter.getFreeSans(8)));
    rpt.addHeaderColumn("", Element.ALIGN_CENTER, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    rpt.addColumn(cell);
    rpt.closeTable();

    if (isSammelbestaetigung)
    {
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(6)));
      rpt.addLight(
          "Es wird bestätigt, dass über die in der Gesamtsumme enthaltenen Zuwendungen keine weiteren Bestätigungen, weder formelle Zuwendungsbestätigungen noch Beitragsquittungen oder ähnliches ausgestellt wurden und werden.\n",
          8);
      rpt.addLight(
          "\nOb es sich um den Verzicht auf Erstattung von Aufwendungen handelt, ist der Anlage zur Sammelbestätigung zu entnehmen.",
          8);
    }

    boolean unterschriftDrucken = false;
    String unterschrift = (String) Einstellungen
        .getEinstellung(Property.UNTERSCHRIFT);
    if ((Boolean) Einstellungen.getEinstellung(Property.UNTERSCHRIFTDRUCKEN)
        && unterschrift != null && !unterschrift.isBlank()
        && spb.isEchteGeldspende())
    {
      unterschriftDrucken = true;
    }

    if (unterschriftDrucken)
    {
      rpt.add("\n", 8);
      rpt.add(Base64.decode(unterschrift), 400, 55, 0);
    }
    else
    {
      rpt.add("\n\n\n\n", 8);
    }
    rpt.add(
        "\n" + (String) Einstellungen.getEinstellung(Property.ORT) + ", "
            + new JVDateFormatTTMMJJJJ().format(spb.getBescheinigungsdatum()),
        9);

    rpt.addLight(
        "..............................................................................."
            + "...............................................................................\n"
            + "(Ort, Datum und Unterschrift des Zuwendungsempfängers)",
        8);

    if (unterschriftDrucken)
    {
      rpt.addLight(
          "\nDie maschinelle Erstellung der Zuwendungsbestätigung wurde dem "
              + "zuständigen Finanzamt "
              + (String) Einstellungen.getEinstellung(Property.FINANZAMT)
              + " angezeigt.",
          8);
    }

    rpt.add("\nHinweis:", 8);
    rpt.addLight(
        "Wer vorsätzlich oder grob fahrlässig eine unrichtige Zuwendungsbestätigung erstellt "
            + "oder veranlasst, dass Zuwendungen nicht zu den in der Zuwendungsbestätigung "
            + "angegebenen steuerbegünstigten Zwecken verwendet werden, haftet für die entgangene "
            + "Steuer (§ 10b Absatz 4 EStG, § 9 Absatz 3 KStG, § 9 Nummer 5 GewStG).\n"
            + "\n"
            + "Diese Bestätigung wird nicht als Nachweis für die steuerliche Berücksichtigung der "
            + "Zuwendung anerkannt, wenn das Datum des Freistellungsbescheides länger als 5 Jahre "
            + "bzw. das Datum der Feststellung der Einhaltung der satzungsmäßigen Voraussetzungen "
            + "nach § 60a Abs. 1 AO länger als 3 Jahre seit Ausstellung des Bescheides zurückliegt "
            + "(§ 63 Abs. 5 AO).",
        7);

    /* Es sind mehrere Spenden für diese Spendenbescheinigung vorhanden */
    if (isSammelbestaetigung)
    {
      List<Buchung> buchungen = spb.getBuchungen();

      rpt.newPage();
      rpt.add(getAussteller(), 10);
      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      rpt.add("\n", 12);
      rpt.add("Anlage zur Sammelbestätigung vom " + (String) map
          .get(SpendenbescheinigungVar.BESCHEINIGUNGDATUM.getName()), 8);
      rpt.add("für den Zeitraum vom "
          + (String) map.get(SpendenbescheinigungVar.SPENDENZEITRAUM.getName()),
          8);

      rpt.add(new Paragraph(" ", Reporter.getFreeSans(12)));

      /* Kopfzeile */
      rpt.addHeaderColumn("Datum der\nZuwendung", Element.ALIGN_LEFT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Art der\nZuwendung", Element.ALIGN_LEFT, 400,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Verzicht auf die\nErstattung von Aufwendungen",
          Element.ALIGN_LEFT, 300, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 150,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();

      boolean printBuchungsart = (Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART);

      /* Buchungszeilen */
      for (Buchung buchung : buchungen)
      {
        rpt.addColumn(buchung.getDatum(), Element.ALIGN_RIGHT);
        String verwendung = "";
        if (printBuchungsart)
        {
          verwendung = buchung.getBuchungsart().getBezeichnung();
        }
        else
        {
          verwendung = buchung.getZweck();
        }
        rpt.addColumn(verwendung, Element.ALIGN_LEFT);
        String verzicht = "";
        if (buchung.getVerzicht().booleanValue())
        {
          verzicht = "ja";
        }
        else
        {
          verzicht = "nein";
        }
        rpt.addColumn(verzicht, Element.ALIGN_CENTER);
        rpt.addColumn(Double.valueOf(buchung.getBetrag()));
      }

      /* Summenzeile */
      // String sumString =
      // Einstellungen.DECIMALFORMAT.format(spb.getBetrag());
      rpt.addColumn("Gesamtsumme", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn("", Element.ALIGN_LEFT, BaseColor.LIGHT_GRAY);
      rpt.addColumn(Double.valueOf(spb.getBetrag()));
      // rpt.addColumn(sumString, Element.ALIGN_RIGHT,
      // BaseColor.LIGHT_GRAY);

      rpt.closeTable();
    }

    if (adressblatt != Adressblatt.OHNE_ADRESSBLATT)
    {
      // Neue Seite für Anschrift in Fenster in querem Brief
      // oder für Anschreiben
      rpt.newPage();
    }

    if (adressblatt == Adressblatt.MIT_ADRESSE
        || adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
    {
      // Anschrift für Fenster in querem Brief
      rpt.add("\n\n\n\n\n\n", 11);
      rpt.addUnderline(getAussteller(), 8);
      rpt.addLight(
          (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()), 10);
    }

    if (adressblatt == Adressblatt.MIT_ANSCHREIBEN
        || adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
    {
      // Anschreiben
      rpt.add("\n\n\n", 12);
      Mitglied m = spb.getMitglied();
      if (m != null)
      {
        VelocityContext context = new VelocityContext();
        context.put("dateformat", new JVDateFormatTTMMJJJJ());
        context.put("decimalformat", Einstellungen.DECIMALFORMAT);
        if (m.getEmail() != null)
          context.put("email", m.getEmail());
        Map<String, Object> mmap = new MitgliedMap().getMap(m, null);
        mmap = new AllgemeineMap().getMap(mmap);
        mmap = new SpendenbescheinigungMap().getMap(spb, mmap);
        VarTools.add(context, mmap);
        StringWriter wtext = new StringWriter();
        Velocity.evaluate(context, wtext, "LOG", text);
        rpt.addLight(wtext.getBuffer().toString(), 10);
      }
      else
      {
        rpt.addLight(text, 10);
      }
    }
  }

  private String getAussteller() throws RemoteException
  {
    return (String) Einstellungen.getEinstellung(Property.NAME) + ", "
        + (String) Einstellungen.getEinstellung(Property.STRASSE) + ", "
        + (String) Einstellungen.getEinstellung(Property.PLZ) + " "
        + (String) Einstellungen.getEinstellung(Property.ORT);
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    // MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf
    Spendenbescheinigung spb = (Spendenbescheinigung) object;
    return spb.getMitgliedID() + "#spendenbescheinigung#" + spb.getID() + "#"
        + (spb.getMitglied() == null ? " " : spb.getMitglied().getEmail())
        + "#Spendenbescheinigung";
  }

  @Override
  protected Map<String, Object> getMap(DBObject object) throws RemoteException
  {
    Spendenbescheinigung spb = (Spendenbescheinigung) object;
    Map<String, Object> map = new SpendenbescheinigungMap().getMap(spb, null);
    map = new AllgemeineMap().getMap(map);
    if (spb.getMitglied() != null)
      map = new MitgliedMap().getMap(spb.getMitglied(), map);
    return map;
  }

  @Override
  protected String getDateiname(DBObject object) throws RemoteException
  {
    if (object != null)
    {
      return VorlageUtil.getName(
          VorlageTyp.SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME, object,
          ((Spendenbescheinigung) object).getMitglied());
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.SPENDENBESCHEINIGUNG_DATEINAME);
    }
  }

  @Override
  protected Formular getFormular(DBObject object) throws RemoteException
  {
    return ((Spendenbescheinigung) object).getFormular();
  }

  @Override
  protected void createPDF(Formular formular, FormularAufbereitung aufbereitung,
      File file, DBObject object)
      throws IOException, DocumentException, ApplicationException
  {
    if (formular == null)
    {
      generiereSpendenbescheinigungStandardAb2014((Spendenbescheinigung) object,
          file, adressblatt);
    }
    else
    {
      super.createPDF(formular, aufbereitung, file, object);

      if (adressblatt != Adressblatt.OHNE_ADRESSBLATT)
      {
        // Neue Seite für Anschrift in Fenster in querem Brief
        // oder für Anschreiben
        aufbereitung.printNeueSeite();
      }
      // Brieffenster drucken bei Spendenbescheinigung
      if (adressblatt == Adressblatt.MIT_ADRESSE
          || adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
      {
        aufbereitung.printAdressfenster(getAussteller(), (String) getMap(object)
            .get(SpendenbescheinigungVar.EMPFAENGER.getName()));
      }
      // Anschreiben drucken
      if (adressblatt == Adressblatt.MIT_ANSCHREIBEN
          || adressblatt == Adressblatt.MIT_ADRESSE_ANSCHREIBEN)
      {
        aufbereitung.printAnschreiben((Spendenbescheinigung) object, text);
      }
    }
  }

  @Override
  protected void closeDocument(FormularAufbereitung formularaufbereitung,
      DBObject object) throws IOException, DocumentException
  {
    if (object != null && ((Spendenbescheinigung) object).getFormular() != null)
    {
      super.closeDocument(formularaufbereitung, object);
    }
    else
    {
      rpt.close();
      fos.close();
    }
  }
}
