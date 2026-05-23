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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class TablePartExportDialog extends AbstractDialog<Object>
{
  public enum ExportArt
  {
    PDF,
    CSV
  }

  private Settings settings;

  private Table table;

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

  public TablePartExportDialog(Table table, String settingPrefix, ExportArt art,
      String title, String subtitle, String filename)
      throws ApplicationException
  {
    super(TablePartExportDialog.POSITION_CENTER);

    if (table == null || table.isDisposed() || !(table instanceof Table))
    {
      throw new ApplicationException("Tabelle nicht geladen");
    }
    if (table.getItems().length == 0)
    {
      throw new ApplicationException("Tabelle enthält keine Daten");
    }

    this.table = table;
    this.title = title;
    this.subtitle = subtitle;
    this.filename = filename;
    this.art = art;
    this.settingPrefix = settingPrefix + art.toString() + ".";

    setTitle("Tabelle exportiere");
    setSize(400, SWT.DEFAULT);

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
      // TODO letze Spalte schmaler?
      col.setData(settings.getInt(settingPrefix + "breite." + col.getText(),
          col.getWidth()));
      listeSortiert.add(col);
    }

    TabFolder folder = new TabFolder(parent, SWT.BORDER);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabGroup tabSpalten = new TabGroup(folder, "Spalten", true, 1);
    TabGroup tabRaender = new TabGroup(folder, "Ränder", true, 2);
    TabGroup tabFormular = new TabGroup(folder, "Formular", true, 2);

    spaltenList = new JVereinTablePart(listeSortiert, null)
    {
      @Override
      protected void orderBy(int index)
      {
        return;
      }
    };
    spaltenList.addColumn("Name", "text");
    spaltenList.addColumn("Breite", "data", null, true);
    spaltenList.setCheckable(true);
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
    tabSpalten.addPart(spaltenList);

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
    // IntegerInput oben2 = new IntegerInput(settings.getInt(id + "oben2", 20));
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
    querformat = new CheckboxInput(
        settings.getBoolean(settingPrefix + "quer", false));
    tabFormular.addLabelPair("Formular Hintergrund", hintergrund);
    tabFormular.addLabelPair("Formular Vordergrund", vordergrund);
    tabFormular.addLabelPair("Querformat", querformat);

    for (TableColumn col : table.getColumns())
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
    // TODO für CSV wirklich auch Spalten auswählen?
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

  private void exportPDF(File file) throws IOException, DocumentException
  {
    try (FileOutputStream fos = new FileOutputStream(file);)
    {
      @SuppressWarnings("unchecked")
      List<TableColumn> listeAuswahl = spaltenList.getItems();
      List<TableColumn> listeOrig = Arrays.asList(table.getColumns());
      TableItem[] rows = table.getItems();

      // TODO Reporter überarbeiten: Formular, Querforamt übergeben
      Reporter reporter = new Reporter(fos, title, subtitle, rows.length,
          (Integer) links.getValue(), (Integer) rechts.getValue(),
          (Integer) oben.getValue(), (Integer) unten.getValue());

      for (TableColumn col : listeAuswahl)
      {
        reporter.addHeaderColumn(col.getText(),
            col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                : Element.ALIGN_RIGHT,
            (int) col.getData(), BaseColor.LIGHT_GRAY);
      }
      reporter.createHeader();

      for (TableItem row : rows)
      {
        for (TableColumn col : listeAuswahl)
        {
          int index = listeOrig.indexOf(col);
          Color bg = row.getBackground(index);
          Font font = null;
          for (FontData data : row.getFont(index).getFontData())
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
          reporter.addColumn(row.getText(index),
              col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                  : Element.ALIGN_RIGHT,
              new BaseColor(bg.getRed(), bg.getGreen(), bg.getBlue()), font);
        }
      }
      // TODO Filter ausgeben?
      reporter.closeTable();
      reporter.close();
    }
  }

  @SuppressWarnings("unchecked")
  private void saveSettings() throws RemoteException
  {
    List<TableColumn> itemsChecked = spaltenList.getItems();
    for (TableColumn col : (List<TableColumn>) spaltenList.getItems(false))
    {
      settings.setAttribute(settingPrefix + "anzeigen." + col.getText(),
          itemsChecked.contains(col));
      settings.setAttribute(settingPrefix + "breite." + col.getText(),
          (Integer) col.getData());
    }

    settings.setAttribute(settingPrefix + "links", (Integer) links.getValue());
    settings.setAttribute(settingPrefix + "rechts",
        (Integer) rechts.getValue());
    settings.setAttribute(settingPrefix + "oben", (Integer) oben.getValue());
    settings.setAttribute(settingPrefix + "unten", (Integer) unten.getValue());

    settings.setAttribute(settingPrefix + "hintergrund",
        ((Formular) hintergrund.getValue()).getID());
    settings.setAttribute(settingPrefix + "vordergrund",
        ((Formular) vordergrund.getValue()).getID());
    settings.setAttribute(settingPrefix + "quer",
        (Boolean) querformat.getValue());
  }

  @Override
  protected List<TableColumn> getData() throws Exception
  {
    return null;
  }

}
