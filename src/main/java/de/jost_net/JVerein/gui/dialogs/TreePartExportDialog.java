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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
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

import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TreePartExportDialog extends AbstractPartExportDialog
{

  private Tree tree;

  public TreePartExportDialog(Tree tree, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(settingPrefix, art, title, subtitle, filename);

    if (tree == null || tree.isDisposed() || !(tree instanceof Tree))
    {
      throw new ApplicationException("Tabelle nicht geladen");
    }
    TreeItem[] rootItems = tree.getItems();
    if (rootItems.length == 0)
    {
      throw new ApplicationException("Tabelle enthält keine Daten");
    }
    // Wir haben immer den Root Node, darum prüfen wir die Childs
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
    settings = new Settings(this.getClass());
  }

  @Override
  protected void paint(Composite parent)
      throws ApplicationException, RemoteException
  {
    // Spalten so wie angezeigt sortieren
    List<TreeColumn> listeSortiert = new ArrayList<>();
    int[] order = tree.getColumnOrder();
    for (int i = 0; i < tree.getColumnCount(); i++)
    {
      TreeColumn col = tree.getColumn(order[i]);
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
          ((TreeColumn) object).setData(Integer.parseInt(newValue));
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
          for (TreeColumn col : (List<TreeColumn>) spaltenList.getItems())
          {
            col.setData(col.getWidth());
          }
          spaltenList.removeAll();
          for (TreeColumn col : listeSortiert)
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
      List<TreeColumn> listeAuswahl = spaltenList.getItems();
      List<TreeColumn> listeOrig = Arrays.asList(tree.getColumns());
      List<MyTreeItem> rows = new ArrayList<>();

      for (TreeItem item : tree.getItems())
      {
        // Die Root Ebene geben wir nicht aus
        getItemRekursiv(rows, item, 0);
      }
      int ebenen = 0;
      for (MyTreeItem item : rows)
      {
        ebenen = Math.max(ebenen, item.getEbene());
      }
      int size = listeAuswahl.size();
      if (listeOrig.indexOf(listeAuswahl.get(0)) == 0)
      {
        size += ebenen;
      }
      else
      {
        ebenen = 0;
      }

      CellProcessor[] cellProcessor = new CellProcessor[size];
      String[] header = new String[size];

      int n = 0;
      for (TreeColumn col : listeAuswahl)
      {
        if (listeOrig.indexOf(col) == 0)
        {
          for (int i = 0; i <= ebenen; i++)
          {
            header[n] = col.getText() + " - " + (i + 1);
            cellProcessor[n++] = new ConvertNullTo("");
          }
        }
        else
        {
          header[n] = col.getText();
          cellProcessor[n++] = new ConvertNullTo("");
        }
      }
      writer.writeHeader(header);

      for (MyTreeItem row : rows)
      {
        Map<String, Object> csvzeile = new HashMap<>();
        int i = 0;
        for (TreeColumn col : listeAuswahl)
        {
          int index = listeOrig.indexOf(col);
          if (index == 0)
          {
            csvzeile.put(header[row.getEbene() + i++],
                row.getTreeItem().getText(index));
          }
          else
          {
            csvzeile.put(header[ebenen + i++],
                row.getTreeItem().getText(index));
          }
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
      List<TreeColumn> listeAuswahl = spaltenList.getItems();
      List<TreeColumn> listeOrig = Arrays.asList(tree.getColumns());
      List<MyTreeItem> rows = new ArrayList<>();

      for (TreeItem item : tree.getItems())
      {
        // Die Root Ebene geben wir nicht aus
        getItemRekursiv(rows, item, 0);
      }

      for (TreeColumn col : listeAuswahl)
      {
        reporter.addHeaderColumn(col.getText(),
            col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                : Element.ALIGN_RIGHT,
            (int) col.getData(), BaseColor.LIGHT_GRAY,
            getFontNormal(BaseColor.BLACK));
      }
      reporter.createHeader();

      for (MyTreeItem row : rows)
      {
        for (TreeColumn col : listeAuswahl)
        {
          int index = listeOrig.indexOf(col);
          String text = row.getTreeItem().getText(index);
          BaseColor color = BaseColor.BLACK;
          try
          {
            String text2 = text.replaceAll("\\.", "").replaceAll("\\,", "\\.");
            Double value = Double.valueOf(text2);
            if (value < 0)
            {
              color = BaseColor.RED;
            }
          }
          catch (NumberFormatException ex)
          {
            // Dann bleibt es Schwarz
          }
          // Die Hintergrundfarbe muss in Data gespeichert sein, sonst hängt sie
          // vom verwendeten Theme ab.
          Color bg = (Color) row.getTreeItem().getData("background");
          Font font = null;
          for (FontData data : row.getTreeItem().getFont(index).getFontData())
          {
            switch (data.getStyle())
            {
              case SWT.BOLD:
                font = getFontFett(color);
                break;
              case SWT.ITALIC:
                font = getFontKursiv(color);
                break;
              case SWT.NORMAL:
                font = getFontNormal(color);
                break;
            }
          }
          int alignment = col.getAlignment() == Column.ALIGN_LEFT
              ? Element.ALIGN_LEFT
              : Element.ALIGN_RIGHT;
          if (row.getEbene() == 1 && index == 0)
          {
            alignment = Element.ALIGN_RIGHT;
          }
          if (bg == null)
          {
            reporter.addColumn(text, alignment, font);
          }
          else
          {
            reporter.addColumn(text, alignment,
                new BaseColor(bg.getRed(), bg.getGreen(), bg.getBlue()), font);
          }
        }
      }
    }

  }

  private void getItemRekursiv(List<MyTreeItem> rows, TreeItem item, int ebene)
  {
    // Unterelemente durchlaufen
    for (TreeItem child : item.getItems())
    {
      rows.add(new MyTreeItem(child, ebene));
      getItemRekursiv(rows, child, ebene + 1);
    }
  }

  private class MyTreeItem
  {
    private TreeItem treeItem;

    private int ebene;

    private MyTreeItem(TreeItem treeItem, int ebene)
    {
      this.treeItem = treeItem;
      this.ebene = ebene;
    }

    public TreeItem getTreeItem()
    {
      return treeItem;
    }

    public int getEbene()
    {
      return ebene;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void saveSettings() throws RemoteException
  {
    List<TreeColumn> itemsChecked = spaltenList.getItems();
    for (TreeColumn col : (List<TreeColumn>) spaltenList.getItems(false))
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
    for (TreeColumn col : tree.getColumns())
    {
      spaltenList.setChecked(col, settings
          .getBoolean(settingPrefix + "anzeigen." + col.getText(), true));
    }
  }
}
