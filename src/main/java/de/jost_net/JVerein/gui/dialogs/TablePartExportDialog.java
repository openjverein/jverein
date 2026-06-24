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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
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

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TablePartExportDialog extends AbstractPartExportDialog
{
  private Table table;

  public TablePartExportDialog(Table table, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename);

    if (table == null || table.isDisposed() || !(table instanceof Table))
    {
      throw new ApplicationException("Tabelle nicht geladen");
    }
    if (table.getItems().length == 0)
    {
      throw new ApplicationException("Tabelle enthält keine Daten");
    }

    this.table = table;
    settings = new Settings(this.getClass());
  }

  @Override
  protected void paint(Composite parent)
      throws ApplicationException, RemoteException
  {
    // Spalten so wie angezeigt sortieren
    List<TableColumn> listeSortiert = new ArrayList<>();
    int[] order = table.getColumnOrder();
    for (int i = 0; i < table.getColumnCount(); i++)
    {
      TableColumn col = table.getColumn(order[i]);
      // Leere Dummy-Spalte am Ende überspringen
      if (col.getText().isBlank())
      {
        continue;
      }
      col.setData(settings.getInt(settingPrefix + "breite." + col.getText(),
          col.getWidth()));
      listeSortiert.add(col);
    }

    spaltenList = new JVereinTablePart(listeSortiert, null)
    {
      // Sortieren verhindern
      @Override
      protected void orderBy(int index)
      {
        return;
      }
    };
    if (art.equals(ExportArt.PDF))
    {
      spaltenList.addChangeListener((object, attribute, newValue) -> {
        try
        {
          ((TableColumn) object).setData(Integer.parseInt(newValue));
        }
        catch (Exception e)
        {
          throw new ApplicationException("Ungültiger Wert");
        }
      });
    }
    createGui(parent, new Action()
    {

      // Action zum Reset der Spaltenbreiten
      @SuppressWarnings("unchecked")
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          for (TableColumn col : (List<TableColumn>) spaltenList.getItems())
          {
            col.setData(col.getWidth());
          }
          spaltenList.removeAll();
          for (TableColumn col : listeSortiert)
          {
            spaltenList.addItem(col);
            spaltenList.setChecked(col, settings
                .getBoolean(settingPrefix + "anzeigen." + col.getText(), true));
          }
        }
        catch (RemoteException re)
        {
          Logger.error("Fehler beim zurücksetzen der Breiten", re);
          throw new ApplicationException(
              "Fehler beim zurücksetzen der Breiten");
        }
      }
    });
  }

  @Override
  protected void exportCSV(File file) throws IOException
  {
    try (ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE))
    {
      @SuppressWarnings("unchecked")
      List<TableColumn> listeAuswahl = spaltenList.getItems();
      List<TableColumn> listeOrig = Arrays.asList(table.getColumns());
      TableItem[] rows = table.getItems();

      CellProcessor[] cellProcessor = new CellProcessor[listeAuswahl.size()];
      String[] header = new String[listeAuswahl.size()];

      int n = 0;
      for (TableColumn col : listeAuswahl)
      {
        header[n] = col.getText();
        cellProcessor[n++] = new ConvertNullTo("");
      }
      writer.writeHeader(header);

      for (TableItem row : rows)
      {
        Map<String, Object> csvzeile = new HashMap<>();
        int i = 0;
        for (TableColumn col : listeAuswahl)
        {
          int index = listeOrig.indexOf(col);
          csvzeile.put(header[i++], row.getText(index));
        }
        writer.write(csvzeile, header, cellProcessor);
      }
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
      List<TableColumn> listeAuswahl = spaltenList.getItems();
      List<TableColumn> listeOrig = Arrays.asList(table.getColumns());
      TableItem[] rows = table.getItems();

      for (TableColumn col : listeAuswahl)
      {
        reporter.addHeaderColumn(col.getText(),
            col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                : Element.ALIGN_RIGHT,
            (int) col.getData(), getHintergrundHeader(),
            getFontHeader(BaseColor.BLACK));
      }
      reporter.createHeader();

      for (TableItem row : rows)
      {
        for (TableColumn col : listeAuswahl)
        {
          int index = listeOrig.indexOf(col);
          String text = row.getText(index);
          // Die Hintergrundfarbe muss in Data gespeichert sein, sonst hängt sie
          // vom verwendeten Theme ab.
          Color bg = (Color) row.getData("background");
          Font font = getFont(text, row.getFont(index).getFontData());

          if (bg == null)
          {
            reporter.addColumn(text,
                col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                    : Element.ALIGN_RIGHT,
                font);
          }
          else
          {
            reporter.addColumn(text,
                col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                    : Element.ALIGN_RIGHT,
                new BaseColor(bg.getRed(), bg.getGreen(), bg.getBlue()), font);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void saveSettings() throws RemoteException
  {
    List<TableColumn> itemsChecked = spaltenList.getItems();
    for (TableColumn col : (List<TableColumn>) spaltenList.getItems(false))
    {
      settings.setAttribute(settingPrefix + "anzeigen." + col.getText(),
          itemsChecked.contains(col));
      if (art.equals(ExportArt.PDF))
      {
        settings.setAttribute(settingPrefix + "breite." + col.getText(),
            (Integer) col.getData());
      }
    }
    super.saveSettings();
  }

  @Override
  void setChecked()
  {
    for (TableColumn col : table.getColumns())
    {
      spaltenList.setChecked(col, settings
          .getBoolean(settingPrefix + "anzeigen." + col.getText(), true));
    }
  }

}
