/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.gui.dialogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

public class TablePartExportDialog extends AbstractPartExportDialog
{
  private Table table;

  private JVereinTablePart tablePart;

  public TablePartExportDialog(Table table, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename,
      JVereinTablePart tablePart) throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename, "Tabelle exportieren",
        tablePart);

    if (table == null || table.isDisposed() || !(table instanceof Table))
    {
      throw new ApplicationException("Tabelle nicht geladen");
    }
    if (table.getItems().length == 0)
    {
      throw new ApplicationException("Tabelle enthält keine Daten");
    }

    this.table = table;
    this.tablePart = tablePart;
    settings = new Settings(this.getClass());
  }

  @Override
  protected void exportCSV(File file) throws IOException
  {
    try (ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE))
    {
      @SuppressWarnings("unchecked")
      List<ExportSpalte> spalten = spaltenList.getItems();

      CellProcessor[] cellProcessor = new CellProcessor[spalten.size()];
      String[] header = new String[spalten.size()];

      int n = 0;
      for (ExportSpalte col : spalten)
      {
        header[n] = col.getColumn().getName();
        cellProcessor[n++] = new ConvertNullTo("");
      }
      writer.writeHeader(header);

      for (Object item : tablePart.getItems())
      {
        Map<String, Object> csvzeile = new HashMap<>();
        int i = 0;
        for (ExportSpalte col : spalten)
        {
          Object o = BeanUtil.get(item, col.getColumn().getColumnId());
          String text = col.getColumn().getFormattedValue(o, col);
          // Haken "Geprüft" und Schloß "Abgeschlossen" Icons ersetzen
          if (text.equals("\u2705") || text.equals("\uD83D\uDD12"))
          {
            text = "ja";
          }
          csvzeile.put(header[i++], text);
        }
        writer.write(csvzeile, header, cellProcessor);
      }
      FileViewer.show(file);
    }
  }

  @Override
  protected void exportPDF(File file) throws IOException, DocumentException
  {
    try (FileOutputStream fos = new FileOutputStream(file);
        Reporter reporter = new Reporter(fos, title, subtitle,
            (Integer) links.getValue(), (Integer) rechts.getValue(),
            (Integer) oben.getValue(), (Integer) unten.getValue(), false,
            (Formular) vordergrund.getValue(),
            (Formular) hintergrund.getValue(), (Boolean) querformat.getValue(),
            (Boolean) headerTransparent.getValue(),
            (Boolean) zellenTransparent.getValue());)
    {
      @SuppressWarnings("unchecked")
      // TODO sortierung
      List<ExportSpalte> listeAuswahl = spaltenList.getItems();

      Object testObject = tablePart.getItems().get(0);
      for (ExportSpalte col : listeAuswahl)
      {
        switch (col.getColumn().getAlign())
        {
          case Column.ALIGN_LEFT:
            col.setAlign(Element.ALIGN_LEFT);
            break;
          case Column.ALIGN_RIGHT:
            col.setAlign(Element.ALIGN_RIGHT);
            break;
          case Column.ALIGN_CENTER:
            col.setAlign(Element.ALIGN_CENTER);
            break;
          case Column.ALIGN_AUTO:
            // Ansonsten Testobjekt laden für automatische Ausrichtung von
            // Spalten
            Object value = BeanUtil.get(testObject,
                col.getColumn().getColumnId());
            col.setAlign(value instanceof Number ? Element.ALIGN_RIGHT
                : Element.ALIGN_LEFT);
            break;
        }

        reporter.addHeaderColumn(col.getColumn().getName(), col.getAlign(),
            col.getBreite(), getHintergrundHeader(),
            getFontHeader(BaseColor.BLACK));
      }
      reporter.createHeader();

      for (Object origObj : tablePart.getItems())
      {
        TableItem tItem = getItem(table.getItems(), origObj);
        for (ExportSpalte spalte : listeAuswahl)
        {
          Object o = BeanUtil.get(origObj, spalte.getColumn().getColumnId());
          String text = spalte.getColumn().getFormattedValue(o, spalte);

          // Die Hintergrundfarbe muss in Data gespeichert sein, sonst hängt sie
          // vom verwendeten Theme ab.
          Color bg = (Color) tItem.getData("background");
          Font font = getFont(text, tItem.getFont().getFontData());

          // Icons ersetzen die in den Standard Fonts nicht enthalten sind
          Font iconfont = FontFactory.getFont("/fonts/fontawesome-webfont.ttf",
              BaseFont.IDENTITY_H, font.getSize(), Font.UNDEFINED, null);
          if (text.equals("\u2705"))
          {
            // Der Haken "Geprüft" in der Buchungsliste
            text = "\uF00C";
            font = iconfont;
          }
          if (text.equals("\uD83D\uDD12"))
          {
            // Das Schloß "Abgeschlossen" in der Abrechnungslaufliste
            text = "\uF023";
            font = iconfont;
          }

          if (bg == null)
          {
            reporter.addColumn(text, spalte.getAlign(), font);
          }
          else
          {
            reporter.addColumn(text, spalte.getAlign(), getHintergrundTabelle(),
                font);
          }
        }
      }
      FileViewer.show(file);
    }
  }

  private TableItem getItem(TableItem[] treeItems, Object o)
  {
    for (TableItem i : treeItems)
    {
      if (i.getData().equals(o))
      {
        return i;
      }
    }
    return null;
  }

  @Override
  Item getColumn(String name)
  {
    for (TableColumn c : table.getColumns())
    {
      if (c.getText().equals(name))

      {
        return c;
      }
    }
    return null;
  }
}
