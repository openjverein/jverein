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

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.mustangproject.BankDetails;
import org.mustangproject.Contact;
import org.mustangproject.DirectDebit;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.IZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromPDFA;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ibm.icu.util.Calendar;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfAConformanceException;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.AllgemeineVar;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.MitgliedVar;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.StringTool;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

public class FormularAufbereitung
{

  private Document doc;

  private FileOutputStream fos;

  private PdfWriter writer;

  private File f;

  // Constanten für QR-Code
  private static final String EPC_STRING = "BCD";

  private static final String EPC_VERSION = "002";

  private static final String EPC_CHARSET_NR = "2"; // 2 = ISO-8859-1, 1 = UTF-8

  private static final String EPC_CHARSET = "ISO-8859-1"; // must match above

  private static final String EPC_ID = "SCT";

  private static final String EPC_EUR = "EUR";

  /**
   * Öffnet die Datei und startet die PDF-Generierung
   * 
   * @param f
   *          Die Datei, in die geschrieben werden soll
   * @throws RemoteException
   */
  public FormularAufbereitung(final File f, boolean pdfa, boolean encrypt)
      throws RemoteException
  {
    this.f = f;
    try
    {
      doc = new Document();
      fos = new FileOutputStream(f);

      if (pdfa)
      {
        writer = PdfAWriter.getInstance(doc, fos,
            PdfAConformanceLevel.PDF_A_3B);

        writer.createXmpMetadata();
        doc.open();

        ICC_Profile icc = ICC_Profile
            .getInstance(Class.forName("org.mustangproject.Invoice")
                .getClassLoader().getResourceAsStream("sRGB.icc"));
        writer.setOutputIntents("Custom", "", "http://www.color.org",
            "sRGB IEC61966-2.1", icc);

        writer.setCompressionLevel(9);
      }
      else
      {
        writer = PdfWriter.getInstance(doc, fos);
        if (encrypt)
        {
          writer.setEncryption(null, null,
              PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_SCREENREADERS
                  | PdfWriter.ALLOW_COPY,
              PdfWriter.ENCRYPTION_AES_256 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
        }
        doc.open();
      }
    }
    catch (IOException e)
    {
      throw new RemoteException("Fehler", e);
    }
    catch (DocumentException e)
    {
      throw new RemoteException("Fehler", e);
    }
    catch (ClassNotFoundException e)
    {
      throw new RemoteException("Fehler", e);
    }
  }

  public void writeForm(Formular formular, Map<String, Object> map)
      throws RemoteException
  {
    try
    {
      PdfReader reader = new PdfReader(formular.getInhalt());
      int numOfPages = reader.getNumberOfPages();

      // Get current counter
      Integer zaehler = formular.getZaehler();
      // Get settings and length of counter
      Integer zaehlerLaenge = (Integer) Einstellungen
          .getEinstellung(Property.ZAEHLERLAENGE);

      for (int i = 1; i <= numOfPages; i++)
      {
        doc.setPageSize(reader.getPageSize(i));
        doc.newPage();
        PdfImportedPage page = writer.getImportedPage(reader, i);
        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.addTemplate(page, 0, 0);

        DBIterator<Formularfeld> it = Einstellungen.getDBService()
            .createList(Formularfeld.class);
        it.addFilter("formular = ? and seite = ?", formular.getID(), i);

        Boolean increased = false;

        while (it.hasNext())
        {
          Formularfeld f = (Formularfeld) it.next();

          // Increase counter if form field is zaehler or qrcode (counter is
          // needed in QR code, so it needs to be incremented)
          if (!increased && (f.getName().toLowerCase()
              .contains(AllgemeineVar.ZAEHLER.getName().toLowerCase())
              || f.getName().toLowerCase()
                  .contains(RechnungVar.QRCODE_SUMME.getName().toLowerCase())))
          {
            zaehler++;
            // Prevent multiple increases by next page
            increased = true;
            // Set new value to field with leading zero to get the defined
            // length
            map.put(AllgemeineVar.ZAEHLER.getName(),
                StringTool.lpad(zaehler.toString(), zaehlerLaenge, "0"));
          }

          goFormularfeld(contentByte, f, map);
        }
      }

      // Set counter to form (not yet saved to the DB)
      formular.setZaehler(zaehler);
    }
    catch (IOException e)
    {
      throw new RemoteException("Fehler", e);
    }
    catch (DocumentException e)
    {
      throw new RemoteException("Fehler", e);
    }
  }

  private Image getPaymentQRCode(Map<String, Object> fieldsMap)
      throws RemoteException
  {
    boolean festerText = (Boolean) Einstellungen
        .getEinstellung(Property.QRCODEFESTERTEXT);
    boolean rechnungDatum = (Boolean) Einstellungen
        .getEinstellung(Property.QRCODEDATUM);
    boolean rechnungNummer = (Boolean) Einstellungen
        .getEinstellung(Property.QRCODERENU);
    boolean mitgliedNummer = (Boolean) Einstellungen
        .getEinstellung(Property.QRCODEMEMBER);

    float sz = mm2point(
        ((Integer) Einstellungen.getEinstellung(Property.QRCODESIZEINMM))
            .floatValue());

    StringBuilder sb = new StringBuilder();
    String verwendungszweck;

    if (festerText)
    {
      String[] zahlungsgruende = ((String) fieldsMap
          .get(RechnungVar.ZAHLUNGSGRUND.getName())).split("\n");
      if (zahlungsgruende.length == 1
          && (Boolean) Einstellungen.getEinstellung(Property.QRCODESNGLLINE))
      {
        sb.append(zahlungsgruende[0]);
      }
      else
      {
        sb.append((String) Einstellungen.getEinstellung(Property.QRCODETEXT));
      }
      if (rechnungDatum || rechnungNummer || mitgliedNummer)
      {
        sb.append(", ");
      }
    }

    if (rechnungDatum || rechnungNummer)
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.QRCODEKUERZEN))
      {
        sb.append("Re. ");
      }
      else
      {
        sb.append("Rechnung ");
      }
      if (rechnungNummer)
      {
        sb.append(fieldsMap.get(AllgemeineVar.ZAEHLER.getName()));
        if (rechnungDatum)
        {
          sb.append(" ");
        }
      }
      if (rechnungDatum)
      {
        if ((Boolean) Einstellungen.getEinstellung(Property.QRCODEKUERZEN))
        {
          sb.append("v. ");
        }
        else
        {
          sb.append("vom ");
        }
        sb.append(fieldsMap.get(AllgemeineVar.TAGESDATUM.getName()));
      }
      if (mitgliedNummer)
      {
        sb.append(", ");
      }
    }

    if (mitgliedNummer)
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.QRCODEKUERZEN))
      {
        sb.append("Mitgl. ");
      }
      else
      {
        sb.append("Mitglied ");
      }

      if ((Boolean) Einstellungen
          .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        sb.append(getString(
            fieldsMap.get(MitgliedVar.EXTERNE_MITGLIEDSNUMMER.getName())));
      }
      else
      {
        sb.append(getString(fieldsMap.get(MitgliedVar.ID.getName())));
      }
    }

    verwendungszweck = sb.toString();

    String infoToMitglied = (String) Einstellungen
        .getEinstellung(Property.QRCODEINFOM);
    if (null == infoToMitglied)
    {
      infoToMitglied = "";
    }

    StringBuilder sbEpc = new StringBuilder();
    sbEpc.append(EPC_STRING).append("\n");
    sbEpc.append(EPC_VERSION).append("\n");
    sbEpc.append(EPC_CHARSET_NR).append("\n");
    sbEpc.append(EPC_ID).append("\n");
    sbEpc.append((String) Einstellungen.getEinstellung(Property.BIC))
        .append("\n");
    sbEpc.append((String) Einstellungen.getEinstellung(Property.NAME))
        .append("\n");
    sbEpc.append((String) Einstellungen.getEinstellung(Property.IBAN))
        .append("\n");
    sbEpc.append(EPC_EUR);
    // Ersetze das Dezimalkomma durch einen Punkt, um der Spezifikation zu
    // entsprechen

    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
    otherSymbols.setDecimalSeparator('.');
    String betrag = new DecimalFormat("0.00", otherSymbols)
        .format(fieldsMap.get(RechnungVar.SUMME_OFFEN.getName()));
    sbEpc.append(betrag);
    sbEpc.append("\n");
    sbEpc.append("\n"); // currently purpose code not used here
    sbEpc.append("\n"); // Reference not used, unstructured text used instead
    sbEpc.append(
        verwendungszweck.substring(0, Math.min(verwendungszweck.length(), 140)))
        .append("\n"); // trim to 140 chars max.
    sbEpc.append(
        infoToMitglied.substring(0, Math.min(infoToMitglied.length(), 70)));
    // trim to 70 chars max.
    String charset = EPC_CHARSET;
    Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    try
    {
      BitMatrix matrix = new MultiFormatWriter().encode(
          new String(sbEpc.toString().getBytes(charset), charset),
          BarcodeFormat.QR_CODE, (int) sz, (int) sz, hintMap);
      return MatrixToImageWriter.toBufferedImage(matrix);
    }
    catch (UnsupportedEncodingException e1)
    {
      throw new RemoteException("Fehler", e1);
    }
    catch (WriterException e1)
    {
      throw new RemoteException("Fehler", e1);
    }
  }

  /**
   * Schließen des aktuellen Formulars, damit die Datei korrekt gespeichert wird
   * 
   * @throws IOException
   */
  public void closeFormular() throws IOException, PdfAConformanceException
  {
    doc.close();
    writer.close();
    fos.close();
  }

  /**
   * Anzeige des gerade aufbereiteten Formulars. Die Ausgabedatei wird vorher
   * geschlossen.
   * 
   * @throws IOException
   */
  public void showFormular() throws IOException
  {
    closeFormular();
    GUI.getDisplay().asyncExec(new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          new Program().handleAction(f);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(
              ae.getLocalizedMessage(), StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }

  private void goFormularfeld(PdfContentByte contentByte, Formularfeld feld,
      Map<String, Object> map) throws DocumentException, IOException
  {
    String filename = String.format("/fonts/%s.ttf", feld.getFont());
    BaseFont baseFont = BaseFont.createFont(filename, BaseFont.IDENTITY_H,
        true);

    float x = mm2point(feld.getX().floatValue());
    float y = mm2point(feld.getY().floatValue());

    Object val;
    String inhalt = feld.getName();
    // (Alte) Felder mit nur einer Variable direkt aus der Map holen
    if (inhalt.matches("^\\$?[a-zA-Z0-9_]+$"))
    {
      val = map.get(inhalt.replace("$", ""));
      if (val == null)
      {
        val = inhalt;
      }
    }
    else
    {
      // Felder mit Text und Variablen
      VelocityContext context = new VelocityContext();
      context.put("dateformat", new JVDateFormatTTMMJJJJ());
      context.put("decimalformat", Einstellungen.DECIMALFORMAT);
      VarTools.add(context, map);

      StringWriter wtext = new StringWriter();
      Velocity.evaluate(context, wtext, "LOG", inhalt);
      val = wtext.toString();
    }

    String stringVal = getString(val).replace("\\n", "\n").replaceAll("\r\n",
        "\n");
    for (String s : stringVal.split("\n"))
    {
      Object o = null;
      // Unterschrift und QR-Code durch Bild ersetzen
      if (s.matches("^\\$?[a-zA-Z0-9_]+$"))
      {
        if (s.replace("$", "")
            .equalsIgnoreCase(RechnungVar.QRCODE_SUMME.getName()))
        {
          com.itextpdf.text.Image i = com.itextpdf.text.Image
              .getInstance(getPaymentQRCode(map), Color.BLACK);
          float sz = mm2point(
              (Integer) Einstellungen.getEinstellung(Property.QRCODESIZEINMM));
          float offset = 0;
          switch (feld.getAusrichtung())
          {
            case RECHTS:
              offset = sz;
              break;
            case MITTE:
              offset = sz / 2;
            default:
              break;
          }
          contentByte.addImage(i, sz, 0, 0, sz, x - offset, y - sz);
          y -= sz + 3;
          continue;
        }

        // Unterschrift
        o = map.get(s.replace("$", ""));
        if (o instanceof com.itextpdf.text.Image)
        {
          com.itextpdf.text.Image i = (com.itextpdf.text.Image) val;
          float sh = i.getScaledHeight();
          float sw = i.getScaledWidth();
          float offset = 0;
          switch (feld.getAusrichtung())
          {
            case RECHTS:
              offset = sw;
              break;
            case MITTE:
              offset = sw / 2;
            default:
              break;
          }
          contentByte.addImage(i, sw, 0, 0, sh, x - offset, y);
          y -= sh + 3;
          continue;
        }
        else if (o instanceof String)
        {
          s = (String) o;
        }
      }
      contentByte.setFontAndSize(baseFont, feld.getFontsize().floatValue());
      contentByte.beginText();
      float offset = 0;
      switch (feld.getAusrichtung())
      {
        case RECHTS:
          offset = contentByte.getEffectiveStringWidth(s, true);
          break;
        case MITTE:
          offset = contentByte.getEffectiveStringWidth(s, true) / 2;
        default:
          break;
      }
      contentByte.moveText(x - offset, y);
      contentByte.showText(s);
      contentByte.endText();
      y -= feld.getFontsize().floatValue() + 3;
    }
  }

  private float mm2point(float mm)
  {
    return mm / 0.3514598f;
  }

  private String getString(Object val)
  {
    if (val instanceof Double)
    {
      return Einstellungen.DECIMALFORMAT.format(val);
    }
    if (val instanceof Date)
    {
      return new JVDateFormatTTMMJJJJ().format((Date) val);
    }
    return val.toString();
  }

  public void printNeueSeite()
  {
    // Neue Seite mit Anschrift für Fenster in querem Brief
    doc.newPage();
  }

  public void printAdressfenster(String aussteller, String empfaenger)
      throws RemoteException
  {
    // Neue Seite mit Anschrift für Fenster in querem Brief
    try
    {
      doc.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      doc.add(new Paragraph("\n\n\n\n\n\n", Reporter.getFreeSans(12)));
      Paragraph paragraph = new Paragraph(aussteller,
          Reporter.getFreeSansUnderline(8));
      paragraph.setIndentationLeft(40);
      doc.add(paragraph);
      paragraph = new Paragraph(empfaenger, Reporter.getFreeSans(9));
      paragraph.setIndentationLeft(40);
      doc.add(paragraph);
    }
    catch (DocumentException e)
    {
      throw new RemoteException("Fehler", e);
    }
  }

  public void printAnschreiben(Spendenbescheinigung spb, String text)
      throws RemoteException
  {
    // Anschreiben drucken
    try
    {
      doc.add(new Paragraph("\n\n\n", Reporter.getFreeSans(12)));
      Mitglied m = spb.getMitglied();
      Paragraph p = null;
      if (m != null)
      {
        VelocityContext context = new VelocityContext();
        context.put("dateformat", new JVDateFormatTTMMJJJJ());
        context.put("decimalformat", Einstellungen.DECIMALFORMAT);
        if (m.getEmail() != null)
          context.put("email", m.getEmail());
        Map<String, Object> mmap = new MitgliedMap().getMap(m, null);
        mmap = new AllgemeineMap().getMap(mmap);
        VarTools.add(context, mmap);
        StringWriter wtext = new StringWriter();
        Velocity.evaluate(context, wtext, "LOG", text);
        p = new Paragraph(wtext.getBuffer().toString(),
            Reporter.getFreeSans(10));
      }
      else
      {
        p = new Paragraph(text, Reporter.getFreeSans(10));
      }
      p.setIndentationLeft(40);
      doc.add(p);
    }
    catch (DocumentException e)
    {
      throw new RemoteException("Fehler", e);
    }
  }

  @SuppressWarnings("resource")
  public void addZUGFeRD(Rechnung re, boolean mahnung) throws IOException
  {
    ArrayList<Sollbuchung> sollbs = re.getSollbuchungList();
    if (sollbs.isEmpty())
    {
      return;
    }

    String sourcePDF = f.getAbsolutePath();
    IZUGFeRDExporter ze = new ZUGFeRDExporterFromPDFA().ignorePDFAErrors()
        .load(sourcePDF).setProducer("JVerein")
        .setCreator(System.getProperty("user.name"));

    Calendar cal1 = Calendar.getInstance();
    cal1.add(Calendar.YEAR, -100);
    Calendar cal2 = Calendar.getInstance();
    for (Sollbuchung sollb : sollbs)
    {
      cal2.setTime(sollb.getDatum());
      if (cal2.after(cal1))
      {
        cal1.setTime(sollb.getDatum());
      }
    }
    Invoice invoice = new Invoice()
        // Fälligkeitsdatum
        .setDueDate(cal1.getTime())
        // Lieferdatum
        .setDeliveryDate(cal1.getTime())
        // Rechnungsdatum
        .setIssueDate(re.getDatum())
        // Rechnungsnummer
        .setNumber(re.getID());

    // Rechnungssteller
    TradeParty sender = new TradeParty(
        (String) Einstellungen.getEinstellung(Property.NAME),
        StringTool.toNotNullString(
            (String) Einstellungen.getEinstellung(Property.STRASSE)),
        StringTool.toNotNullString(
            (String) Einstellungen.getEinstellung(Property.PLZ)),
        StringTool.toNotNullString(
            (String) Einstellungen.getEinstellung(Property.ORT)),
        (String) Einstellungen.getEinstellung(Property.STAAT)).addTaxID(
            (String) Einstellungen.getEinstellung(Property.STEUERNUMMER));
    if (((String) Einstellungen.getEinstellung(Property.USTID)).length() > 0)
      sender.addVATID((String) Einstellungen.getEinstellung(Property.USTID));

    if (re.getZahlungsweg().getKey() == Zahlungsweg.BASISLASTSCHRIFT)
    {
      // Mandat
      sender.addDebitDetails(new DirectDebit(re.getIBAN(), re.getMandatID()));
      // Gläubiger identifikationsnummer
      invoice.setCreditorReferenceID(
          (String) Einstellungen.getEinstellung(Property.GLAEUBIGERID));
    }
    else
    {
      sender.addBankDetails(new BankDetails(
          StringTool.toNotNullString(
              (String) Einstellungen.getEinstellung(Property.IBAN)),
          StringTool.toNotNullString(
              (String) Einstellungen.getEinstellung(Property.BIC))));
    }
    invoice.setSender(sender);

    if (mahnung)
    {
      // Bereits gezahlt
      invoice.setTotalPrepaidAmount(new BigDecimal(re.getIstSumme()));
    }

    String id = re.getMitglied().getID();
    if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      id = re.getMitglied().getExterneMitgliedsnummer();

    // Rechnungsempfänger
    invoice.setRecipient(new TradeParty(
        StringTool.toNotNullString(re.getVorname()) + " "
            + StringTool.toNotNullString(re.getName()),
        StringTool.toNotNullString(re.getStrasse()),
        StringTool.toNotNullString(re.getPlz()),
        StringTool.toNotNullString(re.getOrt()),
        re.getStaatCode() == null || re.getStaatCode().length() == 0
            ? (String) Einstellungen.getEinstellung(Property.STAAT)
            : re.getStaatCode())
                .setID(id)
                .setContact(new Contact(
                    StringTool.toNotNullString(re.getVorname()) + " "
                        + StringTool.toNotNullString(re.getName()),
                    re.getMitglied().getTelefonprivat(),
                    re.getMitglied().getEmail()))
                .setAdditionalAddress(
                    StringTool.toNotNullString(re.getAdressierungszusatz())));

    // LeitwegID
    if (re.getLeitwegID() != null && re.getLeitwegID().length() > 0)
    {
      invoice.setReferenceNumber(re.getLeitwegID());
    }

    // Sollbuchungspositionen
    for (SollbuchungPosition sp : re.getSollbuchungPositionList())
    {
      BigDecimal betrag = new BigDecimal(sp.getNettobetrag());
      invoice.addItem(new Item(new Product(sp.getZweck(), "", "LS", // LS =
                                                                    // pauschal
          new BigDecimal(sp.getSteuersatz()).setScale(2, RoundingMode.HALF_UP)),
          betrag.abs().setScale(4, RoundingMode.HALF_UP),
          new BigDecimal(betrag.signum())));
    }
    ze.setTransaction(invoice);
    ze.export(f.getAbsolutePath());
    ze.close();
  }

}
