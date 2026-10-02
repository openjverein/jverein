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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

import de.jost_net.JVerein.gui.parts.JVereinTreePart;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

public class TreePartExportDialog extends AbstractPartExportDialog
{

  private Tree tree;

  private JVereinTreePart treePart;

  public TreePartExportDialog(Tree tree, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename, JVereinTreePart treePart)
      throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename, "Baum exportieren",
        treePart);

    if (tree == null || tree.isDisposed() || !(tree instanceof Tree))
    {
      throw new ApplicationException("Tabelle nicht geladen");
    }
    TreeItem[] rootItems = tree.getItems();
    boolean leer = true;
    for (TreeItem item : rootItems)
    {
      if (item.getItems().length > 0)
      {
        leer = false;
        break;
      }
    }
    if (leer)
    {
      throw new ApplicationException("Tabelle enthält keine Daten");
    }

    this.tree = tree;
    this.treePart = treePart;
    settings = new Settings(this.getClass());
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void exportCSV(File file) throws IOException
  {
    try (ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE))
    {
      List<ExportSpalte> spalten = spaltenList.getItems();
      List<MyItem> rows = new ArrayList<>();

      for (GenericObjectNode item : (List<GenericObjectNode>) treePart
          .getItems())
      {
        // Die Root Ebene geben wir nicht aus
        getItemRekursiv(rows, item, 0);
      }

      int ebenen = 0;
      for (MyItem item : rows)
      {
        ebenen = Math.max(ebenen, item.getEbene());
      }
      int size = spalten.size() + ebenen;

      CellProcessor[] cellProcessor = new CellProcessor[size];
      String[] header = new String[size];

      int n = 0;
      for (ExportSpalte col : spalten)
      {
        if (n == 0)
        {
          for (int i = 0; i <= ebenen; i++)
          {
            header[n] = col.getColumn().getName() + " - " + (i + 1);
            cellProcessor[n++] = new ConvertNullTo("");
          }
        }
        else
        {
          header[n] = col.getColumn().getName();
          cellProcessor[n++] = new ConvertNullTo("");
        }
      }
      writer.writeHeader(header);

      for (MyItem row : rows)
      {
        Map<String, Object> csvzeile = new HashMap<>();
        int i = 0;
        for (ExportSpalte col : spalten)
        {
          Object o = BeanUtil.get(row.getItem(), col.getColumn().getColumnId());
          String text = col.getColumn().getFormattedValue(o, col);
          if (i == 0)
          {
            csvzeile.put(header[row.getEbene() + i++], text);
          }
          else
          {
            csvzeile.put(header[ebenen + i++], text);
          }
        }
        writer.write(csvzeile, header, cellProcessor);
      }
      FileViewer.show(file);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void exportPDF(File file)
      throws IOException, DocumentException, ApplicationException
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
      List<ExportSpalte> listeAuswahl = spaltenList.getItems();
      List<MyItem> rows = new ArrayList<>();

      for (GenericObjectNode item : (List<GenericObjectNode>) treePart
          .getItems())
      {
        // Die Root Ebene geben wir nicht aus
        getItemRekursiv(rows, item, 0);
      }

      Object testObject = treePart.getItems().get(0);
      for (ExportSpalte col : listeAuswahl)
      {
        if (col.getBreite() <= 0)
        {
          throw new ApplicationException(
              col.getColumn().getName() + ": Breite muss größer 0 sein!");
        }
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

      for (MyItem row : rows)
      {
        Item tItem = getItem(tree.getItems(), row.getItem());
        int n = 0;
        for (ExportSpalte spalte : listeAuswahl)
        {
          Object o = BeanUtil.get(row.getItem(),
              spalte.getColumn().getColumnId());
          String text = spalte.getColumn().getFormattedValue(o, spalte);

          // Die Hintergrundfarbe muss in Data gespeichert sein, sonst hängt sie
          // vom verwendeten Theme ab.
          Color bg = (Color) tItem.getData("background");
          Font font = getFont(text, ((TreeItem) tItem).getFont().getFontData());

          int alignment = spalte.getAlign();
          if (row.getEbene() == 1 && n++ == 0)
          {
            alignment = Element.ALIGN_RIGHT;
          }
          if (bg == null)
          {
            reporter.addColumn(text, alignment, font);
          }
          else
          {
            reporter.addColumn(text, alignment, getHintergrundTabelle(), font);
          }
        }
      }
      FileViewer.show(file);
    }
  }

  private TreeItem getItem(TreeItem[] treeItems, Object o)
  {
    for (TreeItem i : treeItems)
    {
      if (i.getData().equals(o))
      {
        return i;
      }
      TreeItem found = getItem(i.getItems(), o);
      if (found != null)
      {
        return found;
      }
    }
    return null;
  }

  private void getItemRekursiv(List<MyItem> rows, GenericObjectNode item,
      int ebene)
  {
    // Unterelemente durchlaufen
    try
    {
      for (Object o : PseudoIterator.asList(item.getChildren()))
      {
        GenericObjectNode child = (GenericObjectNode) o;
        rows.add(new MyItem(child, ebene));
        getItemRekursiv(rows, child, ebene + 1);
      }
    }
    catch (RemoteException e)
    {
      // kann bei PseudoIterator nicht pssieren
    }
  }

  private class MyItem
  {
    private GenericObjectNode obj;

    private int ebene;

    private MyItem(GenericObjectNode obj, int ebene)
    {
      this.obj = obj;
      this.ebene = ebene;
    }

    public GenericObjectNode getItem()
    {
      return obj;
    }

    public int getEbene()
    {
      return ebene;
    }
  }

  @Override
  Item getColumn(String name)
  {
    for (TreeColumn c : tree.getColumns())
    {
      if (c.getText().equals(name))
      {
        return c;
      }
    }
    return null;
  }
}
