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
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.AllgemeineVar;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.MitgliedVar;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.rmi.Einstellung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.rmi.Mitglied;
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

  private static final int links = 1;

  private static final int rechts = 2;

  private static final String EPC_STRING = "BCD";

  private static final String EPC_VERSION = "002";

  private static final String EPC_CHARSET_NR = "2"; // 2 = ISO-8859-1, 1 = UTF-8

  private static final String EPC_CHARSET = "ISO-8859-1"; // must match above

  private static final String EPC_ID = "SCT";

  private static final String EPC_EUR = "EUR";

  private int buendig = links;

  /**
   * �ffnet die Datei und startet die PDF-Generierung
   * 
   * @param f
   *          Die Datei, in die geschrieben werden soll
   * @throws RemoteException
   */
  public FormularAufbereitung(final File f) throws RemoteException
  {
    this.f = f;
    try
    {
      doc = new Document();
      fos = new FileOutputStream(f);

      writer = PdfWriter.getInstance(doc, fos);
      doc.open();

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
      Einstellung e = Einstellungen.getEinstellung();
      Integer zaehlerLaenge = e.getZaehlerLaenge();

      for (int i = 1; i <= numOfPages; i++)
      {
        doc.setPageSize(reader.getPageSize(i));
        doc.newPage();
        PdfImportedPage page = writer.getImportedPage(reader, i);
        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.addTemplate(page, 0, 0);

        DBIterator<Formularfeld> it = Einstellungen.getDBService()
            .createList(Formularfeld.class);
        it.addFilter("formular = ? and seite = ?",
            new Object[] { formular.getID(), i });
        
        Boolean increased = false;
        
        while (it.hasNext())
        {
          Formularfeld f = (Formularfeld) it.next();
          
          // Increase counter if form field is zaehler or qrcode (counter is
          // needed in QR code, so it needs to be incremented)
          if ((f.getName().equals(AllgemeineVar.ZAEHLER.getName())
              || f.getName().equals(RechnungVar.QRCODE_SUMME.getName()))
              && !increased)
          {
            zaehler++;
            // Prevent multiple increases by next page
            increased = true;
            // Set new value to field with leading zero to get the defined length
            map.put(AllgemeineVar.ZAEHLER.getName(), StringTool.lpad(
                zaehler.toString(), zaehlerLaenge, "0"));
          }
          
          // create QR code for invoice sum if form field is QRCODE_SUM
          if (f.getName().equals(RechnungVar.QRCODE_SUMME.getName()))
          {
            map.put(RechnungVar.QRCODE_SUMME.getName(), getPaymentQRCode(map));
            // Update QR code
          }

          goFormularfeld(contentByte, f, map.get(f.getName()));
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

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Image getPaymentQRCode(Map fieldsMap) throws RemoteException
  {

    Einstellung e = Einstellungen.getEinstellung();

    boolean festerText = e.getQRCodeFesterText();
    boolean rechnungDatum = e.getQRCodeDatum();
    boolean rechnungNummer = e.getQRCodeReNu();
    boolean mitgliedNummer = e.getQRCodeMember();

    float sz = mm2point(((Integer) e.getQRCodeSizeInMm()).floatValue());

    StringBuilder sb = new StringBuilder();
    String verwendungszweck;
    String infoToMitglied;

    if (true == festerText)
    {
      String zahlungsgruende_raw = getString(
          fieldsMap.get(RechnungVar.ZAHLUNGSGRUND.getName()));
      String[] zahlungsgruende = zahlungsgruende_raw.split("\n");
      if (zahlungsgruende.length == 1 && e.getQRCodeSnglLine())
      {
        sb.append(zahlungsgruende[0]);
      }
      else
      {
        sb.append(e.getQRCodeText());
      }
      if (rechnungDatum || rechnungNummer || mitgliedNummer)
      {
        sb.append(", ");
      }
    }

    if (rechnungDatum || rechnungNummer)
    {
      if (e.getQRCodeKuerzen())
      {
        sb.append("Re. ");
      }
      else
      {
        sb.append("Rechnung ");
      }
      if (true == rechnungNummer)
      {
        sb.append(fieldsMap.get(AllgemeineVar.ZAEHLER.getName()));
        if (true == rechnungDatum)
        {
          sb.append(" ");
        }
      }
      if (true == rechnungDatum)
      {
        if (e.getQRCodeKuerzen())
        {
          sb.append("v. ");
        }
        else
        {
          sb.append("vom ");
        }
        sb.append(fieldsMap.get(AllgemeineVar.TAGESDATUM.getName()));
      }
      if (true == mitgliedNummer)
      {
        sb.append(", ");
      }
    }

    if (true == mitgliedNummer)
    {
      if (true == e.getQRCodeKuerzen())
      {
        sb.append("Mitgl. ");
      }
      else
      {
        sb.append("Mitglied ");
      }

      if (true == e.getExterneMitgliedsnummer())
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

    infoToMitglied = e.getQRCodeInfoM();
    if (null == infoToMitglied)
    {
      infoToMitglied = "";
    }

    StringBuilder sbEpc = new StringBuilder();
    sbEpc.append(EPC_STRING).append("\n");
    sbEpc.append(EPC_VERSION).append("\n");
    sbEpc.append(EPC_CHARSET_NR).append("\n");
    sbEpc.append(EPC_ID).append("\n");
    sbEpc.append(e.getBic()).append("\n");
    sbEpc.append(e.getName()).append("\n");
    sbEpc.append(e.getIban()).append("\n");
    sbEpc.append(EPC_EUR);
    Object[] oPosten = (Object[]) fieldsMap
        .get(RechnungVar.BETRAG.getName());
    // Der letzte Eintrag in dem Array ist die Rechnungssumme
    // Ersetze das Dezimalkomma durch einen Punkt, um der Spezifikation zu entsprechen
    String betrag = getString(oPosten[oPosten.length - 1]).replace(',', '.');
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
    Map hintMap = new HashMap();
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
   * Schlie�en des aktuellen Formulars, damit die Datei korrekt gespeichert wird
   * 
   * @throws IOException
   */
  public void closeFormular() throws IOException
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
      Object val) throws DocumentException, IOException
  {
    BaseFont bf = null;
    if (feld.getFont().startsWith("FreeSans"))
    {
      String filename = "/fonts/FreeSans";
      if (feld.getFont().length() > 8)
      {
        filename += feld.getFont().substring(9);
      }
      bf = BaseFont.createFont(filename+".ttf", BaseFont.IDENTITY_H, true);
    } else if (feld.getFont().startsWith("PTSans")) {
      String filename = String.format("/fonts/%s.ttf", feld.getFont());
      bf = BaseFont.createFont(filename, BaseFont.IDENTITY_H, true);
    }
    else
    {
      bf = BaseFont.createFont(feld.getFont(), BaseFont.CP1250, false);
    }

    float x = mm2point(feld.getX().floatValue());
    float y = mm2point(feld.getY().floatValue());
    if (val == null)
    {
      return;
    }
    else if (val instanceof Image)
    {
      com.itextpdf.text.Image i = com.itextpdf.text.Image
          .getInstance((Image) val, Color.BLACK);
      float sz = mm2point(Einstellungen.getEinstellung().getQRCodeSizeInMm());
      contentByte.addImage(i, sz, 0, 0, sz, x, y);
    }
    else
    {
      buendig = links;
      String stringVal = getString(val);
      stringVal = stringVal.replace("\\n", "\n");
      stringVal = stringVal.replaceAll("\r\n", "\n");
      String[] ss = stringVal.split("\n");
      for (String s : ss)
      {
        contentByte.setFontAndSize(bf, feld.getFontsize().floatValue());
        contentByte.beginText();
        float offset = 0;
        if (buendig == rechts)
        {
          offset = contentByte.getEffectiveStringWidth(s, true);
        }
        contentByte.moveText(x - offset, y);
        contentByte.showText(s);
        contentByte.endText();
        y -= feld.getFontsize().floatValue() + 3;
      }
    }
  }

  private float mm2point(float mm)
  {
    return mm / 0.3514598f;
  }

  private String getString(Object val)
  {
    StringBuilder stringVal = new StringBuilder();
    if (val instanceof Object[])
    {
      Object[] o = (Object[]) val;
      if (o.length == 0)
      {
        return "";
      }
      if (o[0] instanceof String)
      {
        for (Object ostr : o)
        {
          stringVal.append((String) ostr);
          stringVal.append("\n");
        }
        
        // Format Strings with percent numbers and closing bracket e.g. taxes
        if (((String) o[0]).contains("%)")) {
          buendig = rechts;
        }
      }
      if (o[0] instanceof Date)
      {
        for (Object od : o)
        {
          stringVal.append(new JVDateFormatTTMMJJJJ().format((Date) od));
          stringVal.append("\n");
        }
      }
      if (o[0] instanceof Double)
      {
        for (Object od : o)
        {
          stringVal.append(Einstellungen.DECIMALFORMAT.format(od));
          stringVal.append("\n");
        }
        buendig = rechts;
      }

    }
    if (val instanceof String)
    {
      stringVal = new StringBuilder((String) val);

      // Format Strings with percent numbers and closing bracket e.g. taxes
      if (((String) val).contains("%)")) {
        buendig = rechts;
      }
    }
    if (val instanceof Double)
    {
      stringVal = new StringBuilder(Einstellungen.DECIMALFORMAT.format(val));
      buendig = rechts;
    }
    if (val instanceof Integer)
    {
      stringVal = new StringBuilder(val.toString());
      buendig = rechts;
    }
    if (val instanceof Date)
    {
      stringVal = new StringBuilder(
          new JVDateFormatTTMMJJJJ().format((Date) val));
    }
    return stringVal.toString();
  }
  
  public void printNeueSeite()
  {
    // Neue Seite mit Anschrift f�r Fenster in querem Brief
      doc.newPage();
  }
  
  public void printAdressfenster(String aussteller, String empfaenger)
      throws RemoteException
  {
    // Neue Seite mit Anschrift f�r Fenster in querem Brief
    try
    {
      doc.add(new Paragraph(" ", Reporter.getFreeSans(12)));
      doc.add(new Paragraph("\n\n\n\n\n\n", Reporter.getFreeSans(12)));
      Paragraph paragraph = new Paragraph(aussteller, Reporter.getFreeSansUnderline(8));
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
      doc.add(new Paragraph("\n\n\n",  Reporter.getFreeSans(12)));
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
        p = new Paragraph(wtext.getBuffer().toString(), Reporter.getFreeSans(10));
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
  
}
