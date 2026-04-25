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
package de.jost_net.JVerein.gui.parts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.gui.dialogs.SpaltenAuswahlDialog;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class JVereinTablePart extends TablePart
{

  private Control tableControl;

  public enum ExportArt
  {
    PDF,
    CSV
  }

  /**
   * Erzeugt eine neue leere Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(Action action)
  {
    super(action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(GenericIterator<?> list, Action action)
  {
    super(list, action);
  }

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * 
   * @param list
   *          Liste mit Objekten, die angezeigt werden soll.
   * @param action
   *          die beim Doppelklick auf ein Element ausgefuehrt wird.
   */
  public JVereinTablePart(List<?> list, Action action)
  {
    super(list, action);
  }

  public void setAction(Action action)
  {
    this.action = action;
  }

  @Override
  protected Context createFeatureEventContext(Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);

    this.tableControl = ctx.control;

    if (!e.equals(Feature.Event.PAINT))
    {
      return ctx;
    }
    Table table = (Table) ctx.control;

    // Die letzte Spalte packen wir nach Titelbreite, falls diese kleiner als
    // der gespeicherte Wert ist. So wird ggf. verhindert, dass eine horizontale
    // Scrollbar angezeigt wird, wenn es gar nicht nötig ist.
    TableColumn c = table.getColumn(table.getColumnCount() - 1);
    int widthOld = c.getWidth();
    c.pack();
    if (c.getWidth() > widthOld)
    {
      c.setWidth(widthOld);
    }

    return ctx;
  }

  // Überschrieben um den Checked-Status beim sortieren beizubehalten
  @Override
  protected void orderBy(int index)
  {
    if (checkable)
    {
      try
      {
        List<?> l = getItems();
        super.orderBy(index);
        setChecked(l.toArray(), true);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim Sortieren");
      }
    }
    else
    {
      super.orderBy(index);
    }
  }

  /**
   * Exportiert die aktuell angezeigte Tabelle als PDF oder CSV
   * 
   * @param title
   * @param subtitle
   * @param filename
   * @param settingPrefix
   * @param art
   * @throws ApplicationException
   */
  public void export(String title, String subtitle, String filename,
      String settingPrefix, ExportArt art) throws ApplicationException
  {
    if (tableControl.isDisposed())
      return;

    if (!(tableControl instanceof Table))
      return;

    try
    {
      Table t = (Table) tableControl;
      TableItem[] rows = t.getItems();
      if (rows == null || rows.length == 0)
      {
        throw new ApplicationException("Tabelle enthält keine Daten");
      }

      // Spalten so wie angezeigt sortieren
      List<TableColumn> listeSortiert = new ArrayList<>();
      int[] order = t.getColumnOrder();
      for (int i = 0; i < t.getColumnCount(); i++)
      {
        listeSortiert.add(t.getColumn(order[i]));
      }

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
      settingPrefix += extension + ".";

      List<TableColumn> listeAuswahl = new SpaltenAuswahlDialog(listeSortiert,
          settingPrefix).open();
      List<TableColumn> listeOrig = Arrays.asList(t.getColumns());

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

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        throw new OperationCanceledException("Abgebrochen");
      }

      File file = new File(s);
      settings.setAttribute(settingPrefix + "lastdir", file.getParent());

      switch (art)
      {
        case CSV:
          ICsvMapWriter writer = null;

          writer = new CsvMapWriter(new FileWriter(file),
              CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

          CellProcessor[] cellProcessor = new CellProcessor[t.getColumnCount()];
          String[] header = new String[listeSortiert.size()];

          int n = 0;
          for (TableColumn col : listeSortiert)
          {
            header[n] = col.getText();
            cellProcessor[n++] = new NotNull();
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
          writer.close();
          break;
        case PDF:
          FileOutputStream fos = new FileOutputStream(file);
          Reporter reporter = new Reporter(fos, title, subtitle, rows.length,
              20, 20, 20, 20);

          for (TableColumn col : listeAuswahl)
          {
            reporter.addHeaderColumn(col.getText(),
                col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                    : Element.ALIGN_RIGHT,
                col.getWidth(), BaseColor.LIGHT_GRAY);
          }
          reporter.createHeader();
          ArrayList<Rectangle> r = new ArrayList<>();

          for (TableItem row : rows)
          {
            for (TableColumn col : listeAuswahl)
            {
              int index = listeOrig.indexOf(col);
              r.add(row.getTextBounds(index));
              reporter.addColumn(row.getText(index),
                  col.getAlignment() == Column.ALIGN_LEFT ? Element.ALIGN_LEFT
                      : Element.ALIGN_RIGHT);
            }
          }
          reporter.closeTable();
          reporter.close();
          fos.close();
          break;
      }
      FileViewer.show(file);
    }
    catch (OperationCanceledException e)
    {
      throw new OperationCanceledException(e);
    }
    catch (ApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Erstellen der Auswertung.", e);
      throw new ApplicationException("Fehler beim Erstellen der Auswertung.");
    }
  }
}
