/**********************************************************************
 * Copyright (c) by Thomas Laubrock
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MittelverwendungControl;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MittelverwendungExportCSV
{

  private static CellProcessor[] getProcessors()
  {

    final CellProcessor[] processors = new CellProcessor[] { 
        new ConvertNullTo(""), // Nr oder Art
        new ConvertNullTo(""), // Bezeichnung
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Betrag
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Summe
        new ConvertNullTo(""), // Kommentar
    };

    return processors;
  }

  public MittelverwendungExportCSV(ArrayList<MittelverwendungZeile> zeile,
      final File file, Date datumvon, Date datumbis, int tab) throws ApplicationException
  {
    ICsvMapWriter writer = null;
    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final CellProcessor[] processors = getProcessors();
      Map<String, Object> csvzeile = new HashMap<>();

      String[] header = { "Nr", "Bezeichnung", "Betrag", "Summe", " " };
      if (tab == MittelverwendungControl.SALDO_REPORT)
      {
        String[] header2 = { "Art", "Konto", "Betrag", "Summe", "Kommentar" };
        header = header2;
      }
      writer.writeHeader(header);

      String title = "";
      switch (tab)
      {
        case MittelverwendungControl.FLOW_REPORT:
          title = "Mittelverwendungsrechnung (Zufluss-basiert)";
          csvzeile.put(header[1], title);
          break;
        case MittelverwendungControl.SALDO_REPORT:
          title = "Mittelverwendungsrechnung (Saldo-basiert)";
          csvzeile.put(header[0], title);
          break;
      }
      writer.write(csvzeile, header, processors);
      csvzeile = new HashMap<>();
      String subtitle = "Geschäftsjahr " + new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      switch (tab)
      {
        case MittelverwendungControl.FLOW_REPORT:
          csvzeile.put(header[1], subtitle);
          break;
        case MittelverwendungControl.SALDO_REPORT:
          csvzeile.put(header[0], subtitle);
          break;
      }
      writer.write(csvzeile, header, processors);
      
      csvzeile = new HashMap<>();
      csvzeile.put(header[1], " ");
      writer.write(csvzeile, header, processors);

      for (MittelverwendungZeile mvz : zeile)
      {
        csvzeile = new HashMap<>();
        switch (mvz.getStatus())
        {
          case MittelverwendungZeile.EINNAHME:
          case MittelverwendungZeile.AUSGABE:
          {
            String position = "";
            Integer pos = (Integer) mvz.getAttribute("position");
            if (pos != null)
            {
              position = pos.toString();
            }
            csvzeile.put(header[0], position);
            csvzeile.put(header[1], (String) mvz.getAttribute("bezeichnung"));
            csvzeile.put(header[2],(Double) mvz.getAttribute("betrag"));
            csvzeile.put(header[3], (Double) mvz.getAttribute("summe"));
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              csvzeile.put(header[4], (String) mvz.getAttribute("kommentar"));
            }
            break;
          }
          case MittelverwendungZeile.SUMME:
          case MittelverwendungZeile.ART:
          {
            String position = "";
            Integer pos = (Integer) mvz.getAttribute("position");
            if (pos != null)
            {
              position = pos.toString();
            }
            String art = (String) mvz.getAttribute("art");
            if (mvz.getStatus() == MittelverwendungZeile.ART)
            {
              csvzeile.put(header[0], art);
            }
            else
            {
              csvzeile.put(header[0], position);
            }
            csvzeile.put(header[1], (String) mvz.getAttribute("bezeichnung"));
            csvzeile.put(header[2], (Double) mvz.getAttribute("betrag"));
            csvzeile.put(header[3], (Double) mvz.getAttribute("summe"));
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              csvzeile.put(header[4], (String) mvz.getAttribute("kommentar"));
            }
            break;
          }
          case MittelverwendungZeile.LEERZEILE:
          {
            String position = "";
            Integer pos = (Integer) mvz.getAttribute("position");
            if (pos != null)
            {
              position = pos.toString();
            }
            csvzeile.put(header[0], position);
            csvzeile.put(header[1], (String) mvz.getAttribute("bezeichnung"));
            break;
          }
          case MittelverwendungZeile.UNDEFINED:
          {
            continue;
          }
        }

        writer.write(csvzeile, header, processors);
      }
      GUI.getStatusBar().setSuccessText("Export fertig.");
      writer.close();
      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("Error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
        }
        catch (Exception e)
        {
          Logger.error("Error while creating report", e);
          throw new ApplicationException("Fehler", e);
        }
      }
    }

  }

}
