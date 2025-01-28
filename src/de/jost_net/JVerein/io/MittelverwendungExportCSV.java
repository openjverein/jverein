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
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MittelverwendungExportCSV
{

  private static CellProcessor[] getProcessors()
  {

    final CellProcessor[] processors = new CellProcessor[] { 
        new ConvertNullTo(""), // Nr
        new ConvertNullTo(""), // Bezeichnung
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Betrag
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Summe
    };

    return processors;
  }

  public MittelverwendungExportCSV(ArrayList<MittelverwendungZeile> zeile,
      final File file, Date datumvon, Date datumbis) throws ApplicationException
  {
    ICsvMapWriter writer = null;
    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final CellProcessor[] processors = getProcessors();
      Map<String, Object> csvzeile = new HashMap<>();

      String[] header = {"Nr", "Bezeichnung", "Betrag", "Summe"};
      writer.writeHeader(header);

      String title = "Mittelverwendungsrechnung";
      csvzeile.put(header[1], title);
      writer.write(csvzeile, header, processors);
      csvzeile = new HashMap<>();
      String subtitle = "Geschäftsjahr " + new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      csvzeile.put(header[1], subtitle);
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
            if ((Integer) mvz.getAttribute("position") != null);
            {
              position = ((Integer) mvz.getAttribute("position")).toString();
            }
            csvzeile.put(header[0], position);
            csvzeile.put(header[1], (String) mvz.getAttribute("bezeichnung"));
            csvzeile.put(header[2],(Double) mvz.getAttribute("betrag"));
            csvzeile.put(header[3], (Double) mvz.getAttribute("summe"));
            break;
          }
          case MittelverwendungZeile.SUMME:
          {
            String position = "";
            if ((Integer) mvz.getAttribute("position") != null);
            {
              position = ((Integer) mvz.getAttribute("position")).toString();
            }
            csvzeile.put(header[0], position);
            csvzeile.put(header[1], (String) mvz.getAttribute("bezeichnung"));
            csvzeile.put(header[2],(Double) mvz.getAttribute("betrag"));
            csvzeile.put(header[3], (Double) mvz.getAttribute("summe"));
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
