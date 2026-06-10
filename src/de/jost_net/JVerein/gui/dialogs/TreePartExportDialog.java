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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TabFolder;
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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.TablePartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TreePartExportDialog extends AbstractDialog<Boolean>
{

  private boolean success = false;

  private Settings settings;

  private Tree tree;

  private String title;

  private String subtitle;

  private String filename;

  private ExportArt art;

  private String settingPrefix;

  IntegerInput links;

  IntegerInput rechts;

  IntegerInput oben;

  IntegerInput unten;

  private CheckboxInput querformat;

  private SelectInput vordergrund;

  private SelectInput hintergrund;

  private JVereinTablePart spaltenList;

  private CheckboxInput headerTransparent;

  private CheckboxInput zellenTransparent;

  public TreePartExportDialog(Tree tree, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(TreePartExportDialog.POSITION_CENTER);

    if (tree == null || tree.isDisposed() || !(tree instanceof Tree))
    {
      throw new ApplicationException("Tabelle nicht geladen");
    }
    TreeItem[] rootItems = tree.getItems();
    if (rootItems.length == 0)
    {
      throw new ApplicationException("Tabelle enthält keine Daten");
    }
    // wir haben immer den Root Node, darum prüfen wir die Childs
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
    this.title = title;
    this.subtitle = subtitle;
    this.filename = filename;
    this.art = art;
    this.settingPrefix = settingPrefix + art.toString() + ".";

    setTitle("Tree exportieren");
    setSize(400, SWT.DEFAULT);

    settings = new Settings(this.getClass());
  }

  @SuppressWarnings("unchecked")
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
    spaltenList.addColumn("Name", "text");
    if (art.equals(ExportArt.PDF))
    {
      spaltenList.addColumn("Breite", "data", null, true);
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
    spaltenList.setCheckable(true);

    if (art.equals(ExportArt.PDF))
    {
      TabFolder folder = new TabFolder(parent, SWT.BORDER);
      folder.setLayoutData(new GridData(GridData.FILL_BOTH));

      TabGroup tabSpalten = new TabGroup(folder, "Spalten", true, 1);
      TabGroup tabRaender = new TabGroup(folder, "Ränder", true, 2);
      TabGroup tabFormular = new TabGroup(folder, "Formular", true, 2);

      tabSpalten.addPart(spaltenList);
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(new Button("Breiten zurücksetzen", (e) -> {
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

      }, null, false, "eraser.png"));
      tabSpalten.addButtonArea(buttons);

      links = new IntegerInput(settings.getInt(settingPrefix + "links", 20));
      rechts = new IntegerInput(settings.getInt(settingPrefix + "rechts", 20));
      oben = new IntegerInput(settings.getInt(settingPrefix + "oben", 20));
      unten = new IntegerInput(settings.getInt(settingPrefix + "unten", 20));
      tabRaender.addLabelPair("Links", links);
      tabRaender.addLabelPair("Rechts", rechts);
      tabRaender.addLabelPair("Oben", oben);
      tabRaender.addLabelPair("Unten", unten);

      // IntegerInput links2 = new IntegerInput(settings.getInt(id + "links2",
      // 20));
      // IntegerInput rechts2 = new IntegerInput(
      // settings.getInt(id + "rechts2", 20));
      // IntegerInput oben2 = new IntegerInput(settings.getInt(id + "oben2",
      // 20));
      // IntegerInput unten2 = new IntegerInput(settings.getInt(id + "unten2",
      // 20));
      // tabRaender.addLabelPair("Links ab 2. Seite", links2);
      // tabRaender.addLabelPair("Rechts ab 2. Seite", rechts2);
      // tabRaender.addLabelPair("Oben ab 2. Seite", oben2);
      // tabRaender.addLabelPair("Unten ab 2. Seite", unten2);

      hintergrund = new FormularInput(FormularArt.HINTERGRUND,
          settings.getString(settingPrefix + "hintergrund", ""));
      hintergrund.setPleaseChoose("Kein Formular");
      vordergrund = new FormularInput(FormularArt.HINTERGRUND,
          settings.getString(settingPrefix + "vordergrund", ""));
      vordergrund.setPleaseChoose("Kein Formular");
      headerTransparent = new CheckboxInput(settings.getBoolean(
          settingPrefix + "headerTransparent", (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_HEADER_TRANSPARENT)));
      zellenTransparent = new CheckboxInput(settings.getBoolean(
          settingPrefix + "zellenTransparent", (Boolean) Einstellungen
              .getEinstellung(Property.TABELLEN_ZELLEN_TRANSPARENT)));
      querformat = new CheckboxInput(
          settings.getBoolean(settingPrefix + "quer", false));
      tabFormular.addLabelPair("Formular Hintergrund", hintergrund);
      tabFormular.addLabelPair("Formular Vordergrund", vordergrund);
      tabFormular.addLabelPair("Tabellen Header transparent",
          headerTransparent);
      tabFormular.addLabelPair("Tabellen Zellen transparent",
          zellenTransparent);
      tabFormular.addLabelPair("Querformat", querformat);
    }
    else
    {
      spaltenList.paint(parent);
    }

    for (TreeColumn col : tree.getColumns())
    {
      spaltenList.setChecked(col, settings
          .getBoolean(settingPrefix + "anzeigen." + col.getText(), true));
    }

    ButtonArea b = new ButtonArea();
    b.addButton("Speichern", c -> export(), null, true, "ok.png");

    b.addButton("Abbrechen", c -> {
      throw new OperationCanceledException();
    }, null, false, "process-stop.png");

    b.paint(parent);
  }

  private void export() throws ApplicationException
  {
    try
    {
      String extension = "";
      switch (art)
      {
        case CSV:
          extension = ".csv";
          break;
        case PDF:
          extension = ".pdf";
          break;
      }

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");

      String path = settings.getString(settingPrefix + "lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }

      fd.setFileName(filename);
      fd.setFilterExtensions(new String[] { "*" + extension });

      final String p = fd.open();

      if (p == null || p.length() == 0)
      {
        throw new OperationCanceledException("Abgebrochen");
      }

      File file = new File(p);
      settings.setAttribute(settingPrefix + "lastdir", file.getParent());

      switch (art)
      {
        case CSV:
          exportCSV(file);
          break;
        case PDF:
          exportPDF(file);
          break;
      }
      saveSettings();

      FileViewer.show(file);

      success = true;
      close();
    }
    catch (IOException | DocumentException e)
    {
      String fehler = "Fehler beim Export";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  private void exportCSV(File file) throws IOException
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
      TreeColumn first = listeAuswahl.get(0);
      if (listeOrig.indexOf(first) == 0)
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

  private void exportPDF(File file) throws IOException, DocumentException
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
            (int) col.getData(), BaseColor.LIGHT_GRAY);
      }
      reporter.createHeader();

      for (MyTreeItem row : rows)
      {
        for (TreeColumn col : listeAuswahl)
        {
          int index = listeOrig.indexOf(col);
          // Die Hintergrundfarbe muss in Data gespeichert sein, sonst hängt sie
          // vom verwendeten Theme ab.
          Color bg = (Color) row.getTreeItem().getData("background");
          Font font = null;
          for (FontData data : row.getTreeItem().getFont(index).getFontData())
          {
            switch (data.getStyle())
            {
              case SWT.BOLD:
                font = Reporter.getFreeSansBold(8);
                break;
              case SWT.ITALIC:
                font = Reporter.getFreeSansItalic(8);
                break;
              case SWT.NORMAL:
                font = Reporter.getFreeSans(8);
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
            reporter.addColumn(row.getTreeItem().getText(index), alignment,
                font);
          }
          else
          {
            reporter.addColumn(row.getTreeItem().getText(index), alignment,
                new BaseColor(bg.getRed(), bg.getGreen(), bg.getBlue()), font);
          }
        }
      }
    }

  }

  @SuppressWarnings("unchecked")
  private void saveSettings() throws RemoteException
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

    if (art.equals(ExportArt.PDF))
    {
      settings.setAttribute(settingPrefix + "links",
          (Integer) links.getValue());
      settings.setAttribute(settingPrefix + "rechts",
          (Integer) rechts.getValue());
      settings.setAttribute(settingPrefix + "oben", (Integer) oben.getValue());
      settings.setAttribute(settingPrefix + "unten",
          (Integer) unten.getValue());

      settings.setAttribute(settingPrefix + "hintergrund",
          hintergrund.getValue() == null ? null
              : ((Formular) hintergrund.getValue()).getID());
      settings.setAttribute(settingPrefix + "vordergrund",
          vordergrund.getValue() == null ? null
              : ((Formular) vordergrund.getValue()).getID());

      settings.setAttribute(settingPrefix + "headerTransparent",
          (Boolean) headerTransparent.getValue());
      settings.setAttribute(settingPrefix + "zellenTransparent",
          (Boolean) zellenTransparent.getValue());

      settings.setAttribute(settingPrefix + "quer",
          (Boolean) querformat.getValue());
    }
  }

  @Override
  protected Boolean getData() throws Exception
  {
    return success;
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

  public class MyTreeItem
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
}
