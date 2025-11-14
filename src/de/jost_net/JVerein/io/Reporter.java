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

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.HyphenationAuto;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Kapselt den Export von Daten im PDF-Format.
 */
public class Reporter
{

  // private I18N i18n = null;

  private ArrayList<PdfPCell> headers;

  private ArrayList<Integer> widths;

  private OutputStream out;

  private Document rpt;

  private PdfWriter writer;

  private PdfPTable table;

  private HyphenationAuto hyph;

  private BaseColor zellenColor = BaseColor.WHITE;

  private boolean headerTransparent = (Boolean) Einstellungen
      .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT);

  private boolean zellenTransparent = (Boolean) Einstellungen
      .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT);

  public static Font getFreeSans(float size, BaseColor color)
  {
    return FontFactory.getFont("/fonts/FreeSans.ttf", BaseFont.IDENTITY_H, size,
        Font.UNDEFINED, color);
  }

  public static Font getFreeSansUnderline(float size, BaseColor color)
  {
    return FontFactory.getFont("/fonts/FreeSans.ttf", BaseFont.IDENTITY_H, size,
        Font.UNDERLINE, color);
  }

  public static Font getFreeSans(float size)
  {
    return getFreeSans(size, null);
  }

  public static Font getFreeSansUnderline(float size)
  {
    return getFreeSansUnderline(size, null);
  }

  public static Font getFreeSansBold(float size, BaseColor color)
  {
    return FontFactory.getFont("/fonts/FreeSans-Bold.ttf", BaseFont.IDENTITY_H,
        size, Font.UNDEFINED, color);
  }

  public static Font getFreeSansBold(float size)
  {
    return getFreeSansBold(size, null);
  }

  public Reporter(OutputStream out, String title, String subtitle,
      int maxRecords) throws DocumentException, IOException
  {
    this(out, title, subtitle, maxRecords, 80, 30, 20, 20);
  }

  public Reporter(OutputStream out, float linkerRand, float rechterRand,
      float obererRand, float untererRand, boolean encrypt)
      throws DocumentException, IOException
  {
    this.out = out;
    rpt = new Document();
    rpt.setMargins(linkerRand, rechterRand, obererRand, untererRand);
    hyph = new HyphenationAuto("de", "DE", 2, 2);
    writer = PdfWriter.getInstance(rpt, out);
    if (encrypt)
    {
      writer.setEncryption(null, null,
          PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_SCREENREADERS
              | PdfWriter.ALLOW_COPY,
          PdfWriter.ENCRYPTION_AES_256 | PdfWriter.DO_NOT_ENCRYPT_METADATA);
    }
    AbstractPlugin plugin = Application.getPluginLoader()
        .getPlugin(JVereinPlugin.class);
    rpt.addAuthor(plugin.getManifest().getName() + " - Version "
        + plugin.getManifest().getVersion());
    rpt.open();
    headers = new ArrayList<>();
    widths = new ArrayList<>();

    // Hintergrund und Vordergrund initialisieren
    Formular formular = (Formular) FormularInput.initdefault(
        (String) Einstellungen.getEinstellung(Property.FORMULAR_HINTERGRUND));
    if (formular != null)
    {
      PdfReader reader = new PdfReader(formular.getInhalt());
      PdfImportedPage hintergrund = writer.getImportedPage(reader, 1);
      writer.setPageEvent(new ReportHintergrund(hintergrund));
      // Hintergrund für erste Seite hier setzen da kein neuPage Event
      PdfContentByte contentByte = writer.getDirectContentUnder();
      contentByte.addTemplate(hintergrund, 0, 0);
    }
    formular = (Formular) FormularInput.initdefault(
        (String) Einstellungen.getEinstellung(Property.FORMULAR_VORDERGRUND));
    if (formular != null)
    {
      PdfReader reader = new PdfReader(formular.getInhalt());
      PdfImportedPage vordergrund = writer.getImportedPage(reader, 1);
      writer.setPageEvent(new ReportVordergrund(vordergrund));
    }
    if (zellenTransparent)
    {
      zellenColor = null;
    }
  }

  public Reporter(OutputStream out, String title, String subtitle,
      int maxRecords, float linkerRand, float rechterRand, float obererRand,
      float untererRand) throws DocumentException, IOException
  {
    this(out, linkerRand, rechterRand, obererRand, untererRand, false);

    StringBuilder fuss = new StringBuilder();
    if (title != null && title.length() > 0)
    {
      Paragraph pTitle = new Paragraph(title, getFreeSansBold(13));
      pTitle.setAlignment(Element.ALIGN_CENTER);
      rpt.add(pTitle);
      fuss.append(title + " | ");
    }
    if (subtitle != null && subtitle.length() > 0)
    {
      rpt.addTitle(subtitle);
      Paragraph psubTitle = new Paragraph(subtitle, getFreeSansBold(10));
      psubTitle.setAlignment(Element.ALIGN_CENTER);
      rpt.add(psubTitle);
      fuss.append(subtitle + " | ");
    }
    fuss.append("erstellt am " + new JVDateFormatTTMMJJJJ().format(new Date())
        + " | Seite: ");
    HeaderFooter hf = new HeaderFooter();
    hf.setFooter(fuss.toString());
    writer.setPageEvent(hf);
    // Fusszeile wird onStartPage gesetzt damit später bei EndPage der
    // Vordergrund darüber gelegt werden kann
    // Fusszeile für erste Seite hier setzen da kein neuPage Event
    hf.onStartPage(writer, rpt);
  }

  /**
   * Fuegt einen neuen Absatz hinzu.
   * 
   * @param p
   * @throws DocumentException
   */
  public void add(Paragraph p) throws DocumentException
  {
    rpt.add(p);
  }

  public void add(String text, int size) throws DocumentException
  {
    Paragraph p = new Paragraph(text, getFreeSansBold(size));
    p.setAlignment(Element.ALIGN_LEFT);
    rpt.add(p);
  }

  public void addLight(String text, int size) throws DocumentException
  {
    Paragraph p = new Paragraph(text, getFreeSans(size));
    p.setAlignment(Element.ALIGN_LEFT);
    rpt.add(p);
  }

  public void addUnderline(String text, int size) throws DocumentException
  {
    Paragraph p = new Paragraph(text, getFreeSansUnderline(size));
    p.setAlignment(Element.ALIGN_LEFT);
    rpt.add(p);
  }

  /**
   * Fuegt der Tabelle einen neuen Spaltenkopf hinzu.
   * 
   * @param text
   * @param align
   * @param width
   * @param color
   */
  public void addHeaderColumn(String text, int align, int width,
      BaseColor color)
  {
    BaseColor bcolor = headerTransparent ? null : color;
    headers.add(getDetailCell(text, align, bcolor, true));
    widths.add(Integer.valueOf(width));
  }

  /**
   * Fuegt der Tabelle einen neuen Spaltenkopf hinzu.
   * 
   * @param text
   * @param align
   * @param width
   * @param color
   */
  public void addHeaderColumn(String text, int align, int width,
      BaseColor color, boolean silbentrennung)
  {
    BaseColor bcolor = headerTransparent ? null : color;
    headers.add(getDetailCell(text, align, bcolor, silbentrennung));
    widths.add(Integer.valueOf(width));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   * 
   * @param cell
   */
  public void addColumn(PdfPCell cell)
  {
    table.addCell(cell);
  }

  public void addColumn(byte[] image, int width, int height,
      int horizontalalignment)
      throws BadElementException, MalformedURLException, IOException
  {
    Image i = Image.getInstance(image);
    float w = i.getWidth() / width;
    float h = i.getHeight() / height;
    if (w > h)
    {
      h = i.getHeight() / w;
      w = width;
    }
    else
    {
      w = i.getWidth() / h;
      h = height;
    }
    i.scaleToFit(w, h);
    PdfPCell cell = new PdfPCell(i, false);
    cell.setPadding(3);
    cell.setHorizontalAlignment(horizontalalignment);
    table.addCell(cell);
  }

  public void add(byte[] image, int width, int height, int horizontalalignment)
      throws BadElementException, MalformedURLException, IOException,
      DocumentException
  {
    Image i = Image.getInstance(image);
    float w = i.getWidth() / width;
    float h = i.getHeight() / height;
    if (w > h)
    {
      h = i.getHeight() / w;
      w = width;
    }
    else
    {
      w = i.getWidth() / h;
      h = height;
    }
    i.scaleToFit(w, h);
    rpt.add(i);
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(String text, int align, BaseColor backgroundcolor)
  {
    BaseColor bcolor = zellenTransparent ? null : backgroundcolor;
    addColumn(getDetailCell(text, align, bcolor, true));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(String text, int align, BaseColor backgroundcolor,
      boolean silbentrennung)
  {
    BaseColor bcolor = zellenTransparent ? null : backgroundcolor;
    addColumn(getDetailCell(text, align, bcolor, silbentrennung));
  }

  /**
   * Fuegt eine neue Zelle mit einem boolean-Value zur Tabelle hinzu
   */
  public void addColumn(boolean value)
  {
    addColumn(value ? "X" : "", Element.ALIGN_CENTER, zellenColor, true);
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(String text, int align, Font font)
  {
    addColumn(getDetailCell(text, align, zellenColor, true, font));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(String text, int align)
  {
    addColumn(getDetailCell(text, align, zellenColor, true));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(String text, int align, boolean silbentrennung)
  {
    addColumn(getDetailCell(text, align, zellenColor, silbentrennung));
  }

  public void addColumn(String text, int align, int colspan)
  {
    addColumn(getDetailCell(text, align, zellenColor, colspan));
  }

  public void addColumn(String text, int align, BaseColor backgroundcolor,
      int colspan)
  {
    BaseColor bcolor = zellenTransparent ? null : backgroundcolor;
    addColumn(getDetailCell(text, align, bcolor, colspan));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(Double value)
  {
    if (value != null)
    {
      addColumn(getDetailCell(value.doubleValue(), zellenColor));
    }
    else
    {
      addColumn(getDetailCell("", Element.ALIGN_LEFT, zellenColor, false));
    }
  }

  public void addColumn(double value, BaseColor backgroundcolor)
  {
    Font f = null;
    if (value >= 0)
    {
      f = getFreeSans(8, BaseColor.BLACK);
    }
    else
    {
      f = getFreeSans(8, BaseColor.RED);
    }
    PdfPCell cell = new PdfPCell(
        new Phrase(Einstellungen.DECIMALFORMAT.format(value), f));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    cell.setBackgroundColor(backgroundcolor);
    addColumn(cell);
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(double value)
  {
    addColumn(getDetailCell(value, zellenColor));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(Date value, int align)
  {
    if (value != null)
    {
      addColumn(getDetailCell(value, align, zellenColor));
    }
    else
    {
      addColumn("", Element.ALIGN_LEFT, false);
    }
  }

  /**
   * Erzeugt den Tabellen-Header mit 100 % Breite.
   * 
   * @throws DocumentException
   */
  public void createHeader() throws DocumentException
  {
    createHeader(100f, Element.ALIGN_LEFT);
  }

  /**
   * Erzeugt den Tabellen-Header.
   * 
   * @param tabellenbreiteinprozent
   *          Breite der Tabelle in Prozent
   * @param alignment
   *          Horizontale Ausrichtung der Tabelle (siehe com.lowagie.Element.)
   * @throws DocumentException
   */
  public void createHeader(float tabellenbreiteinprozent, int alignment)
      throws DocumentException
  {
    table = new PdfPTable(headers.size());
    table.setWidthPercentage(tabellenbreiteinprozent);
    table.setHorizontalAlignment(alignment);
    float[] w = new float[headers.size()];
    for (int i = 0; i < headers.size(); i++)
    {
      Integer breite = widths.get(i);
      w[i] = breite.intValue();
    }
    table.setWidths(w);
    table.setSpacingBefore(10);
    table.setSpacingAfter(0);
    for (int i = 0; i < headers.size(); i++)
    {
      PdfPCell cell = headers.get(i);
      table.addCell(cell);
    }
    table.setHeaderRows(1);
  }

  public void newPage()
  {
    rpt.newPage();
  }

  public void closeTable() throws DocumentException
  {
    if (table == null)
    {
      return;
    }
    rpt.add(table);
    table = null;
    headers = new ArrayList<>();
    widths = new ArrayList<>();
  }

  /**
   * Schliesst den Report.
   * 
   * @throws IOException
   * @throws DocumentException
   */
  public void close() throws IOException, DocumentException
  {
    try
    {
      GUI.getStatusBar().setSuccessText("PDF-Export beendet");
      if (table != null)
      {
        rpt.add(table);
      }
      rpt.close();
    }
    finally
    {
      // Es muss sichergestellt sein, dass der OutputStream
      // immer geschlossen wird
      out.close();
    }
  }

  /**
   * Erzeugt eine Zelle der Tabelle.
   * 
   * @param text
   *          der anzuzeigende Text.
   * @param align
   *          die Ausrichtung.
   * @param backgroundcolor
   *          die Hintergundfarbe.
   * @return die erzeugte Zelle.
   */
  private PdfPCell getDetailCell(String text, int align,
      BaseColor backgroundcolor, boolean silbentrennung)
  {
    PdfPCell cell = null;
    if (silbentrennung)
    {
      cell = new PdfPCell(new Phrase(
          new Chunk(notNull(text), getFreeSans(8)).setHyphenation(hyph)));
    }
    else
    {
      cell = new PdfPCell(new Phrase(new Chunk(notNull(text), getFreeSans(8))));
    }
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  private PdfPCell getDetailCell(String text, int align,
      BaseColor backgroundcolor, boolean silbentrennung, Font font)
  {
    PdfPCell cell = null;
    if (silbentrennung)
    {
      cell = new PdfPCell(
          new Phrase(new Chunk(notNull(text), font).setHyphenation(hyph)));
    }
    else
    {
      cell = new PdfPCell(new Phrase(new Chunk(notNull(text), font)));
    }
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  private PdfPCell getDetailCell(String text, int align,
      BaseColor backgroundcolor, int colspan)
  {
    PdfPCell cell = new PdfPCell(new Phrase(
        new Chunk(notNull(text), getFreeSans(8)).setHyphenation(hyph)));
    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    cell.setColspan(colspan);
    return cell;
  }

  /**
   * Erzeugt eine Zelle fuer die uebergebene Zahl.
   * 
   * @param value
   *          die Zahl.
   * @return die erzeugte Zelle.
   */
  private PdfPCell getDetailCell(double value, BaseColor backgroundcolor)
  {
    Font f = null;
    if (value >= 0)
    {
      f = getFreeSans(8, BaseColor.BLACK);
    }
    else
    {
      f = getFreeSans(8, BaseColor.RED);
    }
    PdfPCell cell = new PdfPCell(
        new Phrase(Einstellungen.DECIMALFORMAT.format(value), f));
    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    cell.setBackgroundColor(backgroundcolor);
    return cell;
  }

  /**
   * Erzeugt eine Zelle fuer das uebergebene Datum.
   * 
   * @param value
   *          das Datum.
   * @return die erzeugte Zelle.
   */
  private PdfPCell getDetailCell(Date value, int align,
      BaseColor backgroundcolor)
  {
    if (value.equals(Einstellungen.NODATE))
    {
      return getDetailCell("", Element.ALIGN_LEFT, backgroundcolor, false);
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    return getDetailCell(sdf.format(value), align, backgroundcolor, false);
  }

  /**
   * Gibt einen Leerstring aus, falls der Text null ist.
   * 
   * @param text
   *          der Text.
   * @return der Text oder Leerstring - niemals null.
   */
  public String notNull(String text)
  {
    return text == null ? "" : text;
  }

  public void addParams(final TreeMap<String, String> params)
      throws DocumentException
  {
    if (!params.keySet().isEmpty())
    {
      add(new Paragraph("Filter-Parameter", Reporter.getFreeSans(12)));
      BaseColor bcolor = headerTransparent ? null : BaseColor.LIGHT_GRAY;
      addHeaderColumn("Parameter", Element.ALIGN_RIGHT, 100, bcolor);
      addHeaderColumn("Wert", Element.ALIGN_LEFT, 200, bcolor);
      createHeader(75f, Element.ALIGN_LEFT);
      for (String key : params.keySet())
      {
        addColumn(key, Element.ALIGN_RIGHT);
        addColumn(params.get(key), Element.ALIGN_LEFT);
      }
      closeTable();
    }
  }
}
