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
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.HyphenationAuto;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Kapselt den Export von Daten im PDF-Format.
 */
public class Reporter implements AutoCloseable
{
  private ArrayList<PdfPCell> headers;

  private ArrayList<Integer> widths;

  private OutputStream out;

  private Document rpt;

  private PdfWriter writer;

  private PdfPTable table;

  private HyphenationAuto hyph;

  private BaseColor zellenColor = BaseColor.WHITE;

  private boolean resetPageCount = false;

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

  public static Font getFreeSansItalic(float size, BaseColor color)
  {
    return FontFactory.getFont("/fonts/FreeSans-Oblique.ttf",
        BaseFont.IDENTITY_H, size, Font.ITALIC, color);
  }

  public static Font getFreeSansItalic(float size)
  {
    return getFreeSansItalic(size, null);
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

  public Reporter(OutputStream out, ExportLayoutParam params)
      throws DocumentException, IOException
  {
    this(out, params.getTitle(), params.getSubtitle(), params.getLinks(),
        params.getRechts(), params.getOben(), params.getUnten(), false,
        params.getVordergrund(), params.getHintergrund(),
        params.getQuerformat(), params.getHeaderTransparent(),
        params.getZellenTransparent());
  }

  public Reporter(OutputStream out, String title, String subtitle)
      throws DocumentException, IOException
  {
    this(out, title, subtitle, 80, 30, 20, 20, false);
  }

  public Reporter(OutputStream out, float linkerRand, float rechterRand,
      float obererRand, float untererRand, boolean encrypt)
      throws DocumentException, IOException
  {
    this(out, null, null, linkerRand, rechterRand, obererRand, untererRand,
        encrypt);
  }

  public Reporter(OutputStream out, String title, String subtitle,
      float linkerRand, float rechterRand, float obererRand, float untererRand,
      boolean encrypt) throws DocumentException, IOException
  {
    this(out, title, subtitle, linkerRand, rechterRand, obererRand, untererRand,
        encrypt, getDefaultFormular(Property.FORMULAR_VORDERGRUND),
        getDefaultFormular(Property.FORMULAR_HINTERGRUND), false, null, null);
  }

  public Reporter(OutputStream out, String title, String subtitle,
      float linkerRand, float rechterRand, float obererRand, float untererRand,
      boolean encrypt, Formular vordergrund, Formular hintergrund,
      boolean querformat, Boolean headerTransparent, Boolean zellenTransparent)
      throws DocumentException, IOException
  {
    if (headerTransparent != null)
    {
      this.headerTransparent = headerTransparent;
    }
    if (zellenTransparent != null)
    {
      this.zellenTransparent = zellenTransparent;
    }
    this.out = out;
    rpt = new Document(querformat ? PageSize.A4.rotate() : PageSize.A4);
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
    if (hintergrund != null)
    {
      PdfReader reader = new PdfReader(hintergrund.getInhalt());
      writer.setPageEvent(new ReportHintergrund(reader));
      PdfImportedPage page = writer.getImportedPage(reader, 1);
      // Hintergrund für erste Seite hier setzen da kein neuPage Event
      PdfContentByte contentByte = writer.getDirectContentUnder();
      contentByte.addTemplate(page, 0, 0);
    }
    // Vordergrund Event immer setzen weil es für das Rücksetzen der
    // Seitennummer gebraucht wird
    PdfReader reader = vordergrund != null
        ? new PdfReader(vordergrund.getInhalt())
        : null;
    writer.setPageEvent(new ReportVordergrund(reader));

    if (this.zellenTransparent)
    {
      zellenColor = null;
    }

    // Fuss und Kopfzeile werden nur ausgegeben, wenn auch Titel oder Subtitel
    // gesetzt sind
    if (title != null || subtitle != null)
    {
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
  }

  private static Formular getDefaultFormular(Property einstellung)
      throws RemoteException
  {
    String id = (String) Einstellungen.getEinstellung(einstellung);
    if (id != null && !id.isBlank())
    {
      return (Formular) Einstellungen.getDBService()
          .createObject(Formular.class, id);
    }
    return null;
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
    addHeaderColumn(text, align, width, color, getFreeSans(8));
  }

  /**
   * Fuegt der Tabelle einen neuen Spaltenkopf hinzu.
   * 
   * @param text
   * @param align
   * @param width
   * @param color
   * @param font
   */
  public void addHeaderColumn(String text, int align, int width,
      BaseColor color, Font font)
  {
    headers.add(getDetailCell(text, align, headerTransparent ? null : color,
        true, font, 1));
    widths.add(width);
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
  public void addColumn(String text, int align)
  {
    addColumn(text, align, true);
  }

  public void addColumn(String text, int align, int colspan)
  {
    addColumn(text, align, zellenColor, colspan);
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(String text, int align, BaseColor backgroundcolor)
  {
    addColumn(text, align, backgroundcolor, null);
  }

  public void addColumn(String text, int align, Font font)
  {
    addColumn(text, align, zellenColor, font);
  }

  public void addColumn(String text, int align, boolean silbentrennung)
  {
    addColumn(text, align, zellenColor, silbentrennung, null, 1);
  }

  public void addColumn(String text, int align, boolean silbentrennung,
      Font font)
  {
    addColumn(text, align, zellenColor, silbentrennung, font, 1);
  }

  public void addColumn(String text, int align, BaseColor color, Font font)
  {
    addColumn(text, align, color, true, font, 1);
  }

  public void addColumn(String text, int align, BaseColor backgroundcolor,
      int colspan)
  {
    addColumn(text, align, backgroundcolor, true, null, colspan);
  }

  public void addColumn(String text, int align, BaseColor color,
      boolean silbentrennung, Font font, int colspan)
  {
    addColumn(getDetailCell(text, align, zellenTransparent ? null : color,
        silbentrennung, font, colspan));
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(Double value)
  {
    addColumn(value, zellenColor);
  }

  public void addColumn(Double value, BaseColor backgroundcolor)
  {
    Font font = getFreeSans(8, BaseColor.BLACK);
    String text = "";
    if (value != null)
    {
      if (value < 0)
      {
        font = getFreeSans(8, BaseColor.RED);
      }
      text = Einstellungen.DECIMALFORMAT.format(value);
    }
    addColumn(text, Element.ALIGN_RIGHT, backgroundcolor, font);
  }

  public void addColumn(Double value, Font font, Boolean red)
  {
    addColumn(value, zellenColor, font, red);
  }

  public void addColumn(Double value, BaseColor backgroundcolor, Font font,
      Boolean red)
  {
    String text = "";
    if (value != null)
    {
      text = Einstellungen.DECIMALFORMAT.format(value);
      if (value < 0 && red)
      {
        Font newfont = new Font(font);
        newfont.setColor(BaseColor.RED);
        addColumn(text, Element.ALIGN_RIGHT, backgroundcolor, newfont);
        return;
      }
    }
    addColumn(text, Element.ALIGN_RIGHT, backgroundcolor, font);
  }

  /**
   * Fuegt eine neue Zelle zur Tabelle hinzu.
   */
  public void addColumn(Date value, int align)
  {
    addColumn(value, align, null);
  }

  public void addColumn(Date value, int align, Font font)
  {
    addColumn(value, align, zellenColor, font);
  }

  public void addColumn(Date value, int align, BaseColor backgroundcolor,
      Font font)
  {
    String text = "";
    if (value != null && !value.equals(Einstellungen.NODATE))
    {
      text = new SimpleDateFormat("dd.MM.yyyy").format(value);
    }
    addColumn(text, align, backgroundcolor, false, font, 1);
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
      w[i] = widths.get(i);
    }
    table.setWidths(w);
    table.setSpacingBefore(10);
    table.setSpacingAfter(0);
    for (int i = 0; i < headers.size(); i++)
    {
      table.addCell(headers.get(i));
    }
    table.setHeaderRows(1);
  }

  /**
   * Neue Seite hinzufügen
   */
  public void newPage()
  {
    rpt.newPage();
  }

  /**
   * Schreibt die Tabelle ins PDF. Nötig um weiteren Text oder Tabellen
   * hinzuzufügen
   * 
   * @throws DocumentException
   */
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
  @Override
  public void close() throws IOException, DocumentException
  {
    try
    {
      if (table != null)
      {
        rpt.add(table);
      }
      rpt.close();
    }
    finally
    {
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
      BaseColor backgroundcolor, boolean silbentrennung, Font font, int colspan)
  {
    PdfPCell cell = null;
    Chunk chunk = new Chunk(text == null ? "" : text,
        font == null ? getFreeSans(8) : font);
    cell = new PdfPCell(new Phrase(chunk));
    if (silbentrennung)
    {
      chunk.setHyphenation(hyph);
    }
    cell = new PdfPCell(new Phrase(chunk));

    cell.setHorizontalAlignment(align);
    cell.setBackgroundColor(backgroundcolor);
    cell.setColspan(colspan);
    return cell;
  }

  /**
   * Gibt die Parameter als Tablelle aus
   * 
   * @param map
   * @throws DocumentException
   * @throws RemoteException
   */
  public void addParams(final Map<Filter, String> map)
      throws DocumentException, RemoteException
  {
    if (map.size() > 0)
    {
      add(new Paragraph("Filter-Parameter", Reporter.getFreeSans(12)));
      BaseColor bcolor = headerTransparent ? null : BaseColor.LIGHT_GRAY;
      addHeaderColumn("Parameter", Element.ALIGN_RIGHT, 100, bcolor);
      addHeaderColumn("Wert", Element.ALIGN_LEFT, 200, bcolor);
      createHeader(75f, Element.ALIGN_LEFT);
      for (Entry<Filter, String> entry : map.entrySet())
      {
        addColumn(entry.getKey().getAnzeigeText(), Element.ALIGN_RIGHT);
        addColumn(entry.getValue(), Element.ALIGN_LEFT);
      }
      closeTable();
    }
  }

  /**
   * Gibt die Parameter als Tablelle aus
   * 
   * @param params
   * @throws DocumentException
   */
  // TODO wird nur noch für Buchungen verwendet, sollte auch da zu Filter
  // umgestellt werden.
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

  /**
   * Setzen eines Hintergrundes bei Reports.
   */
  private class ReportHintergrund extends PdfPageEventHelper
  {
    private PdfReader reader;

    public ReportHintergrund(PdfReader reader)
    {
      this.reader = reader;
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document)
    {
      if (reader != null)
      {
        int number = writer.getPageNumber() <= reader.getNumberOfPages()
            ? writer.getPageNumber()
            : reader.getNumberOfPages();

        PdfImportedPage page = writer.getImportedPage(reader, number);

        PdfContentByte contentByte = writer.getDirectContentUnder();
        contentByte.addTemplate(page, 0, 0);
      }
    }
  }

  /**
   * Setzen eines Vordergrund bei Reports.
   */
  private class ReportVordergrund extends PdfPageEventHelper
  {

    private PdfReader reader;

    public ReportVordergrund(PdfReader reader)
    {
      this.reader = reader;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document)
    {
      if (reader != null)
      {
        int number = writer.getPageNumber() <= reader.getNumberOfPages()
            ? writer.getPageNumber()
            : reader.getNumberOfPages();
        PdfImportedPage page = writer.getImportedPage(reader, number);

        PdfContentByte contentByte = writer.getDirectContent();
        contentByte.saveState();
        contentByte.addTemplate(page, 0, 0);
        contentByte.restoreState();
      }
      if (resetPageCount)
      {
        document.resetPageCount();
        resetPageCount = false;
      }
    }
  }

  /**
   * Ersatz für die HeaderFooter-Klasse, die es bis iText 1.x gab. Wird zur Zeit
   * nur für den Footer gebraucht.
   */
  private class HeaderFooter extends PdfPageEventHelper
  {

    String footer = null;

    public void setFooter(String footer)
    {
      this.footer = footer;
    }

    /**
     * Adds the header and the footer.
     * 
     */
    @Override
    public void onStartPage(PdfWriter writer, Document document)
    {
      Rectangle rect = document.getPageSize();

      float left = rect.getLeft() + document.leftMargin();
      float right = rect.getRight() - document.rightMargin();
      float bottom = rect.getBottom() + document.bottomMargin();
      PdfContentByte pc = writer.getDirectContent();
      pc.setColorStroke(BaseColor.BLACK);
      pc.setLineWidth(0.5f);
      pc.moveTo(left, bottom - 5);
      pc.lineTo(right, bottom - 5);
      pc.stroke();
      ColumnText.showTextAligned(pc, Element.ALIGN_CENTER,
          new Phrase(footer + " " + writer.getPageNumber(), getFreeSans(7)),
          (left + right) / 2, bottom - 15, 0);
    }
  }

  public void resetPageCount()
  {
    resetPageCount = true;
  }
}
