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

public class KontenSaldoCSV
{

  private static CellProcessor[] getProcessors()
  {

    final CellProcessor[] processors = new CellProcessor[] { 
        new ConvertNullTo(""), // Kontonummer
        new ConvertNullTo(""), // Bezeichnung
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Anfangsbestand
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Einnahmen
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Ausgaben
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Umbuchung
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Endbestand
        new ConvertNullTo(""), // Bemerkumg
    };

    return processors;
  }

  public KontenSaldoCSV(ArrayList<KontoSaldoZeile> zeile,
      final File file, Date datumvon, Date datumbis) throws ApplicationException
  {
    ICsvMapWriter writer = null;
    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final CellProcessor[] processors = getProcessors();
      Map<String, Object> csvzeile = new HashMap<>();

      String[] header = { "Kontonummer", "Kontobezeichnung", "Anfangsbestand",
          "Einnahmen", "Ausgaben", "Umbuchungen", "Endbestand", "Bemerkung" };
      writer.writeHeader(header);

      String subtitle = new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      csvzeile.put(header[0], subtitle);
      writer.write(csvzeile, header, processors);

      for (KontoSaldoZeile sz : zeile)
      {
        csvzeile = new HashMap<>();

        csvzeile.put(header[0], (String) sz.getAttribute("kontonummer"));
        csvzeile.put(header[1], (String) sz.getAttribute("kontobezeichnung"));
        csvzeile.put(header[2], (Double) sz.getAttribute("anfangsbestand"));
        csvzeile.put(header[3], (Double) sz.getAttribute("einnahmen"));
        csvzeile.put(header[4], (Double) sz.getAttribute("ausgaben"));
        csvzeile.put(header[5], (Double) sz.getAttribute("umbuchungen"));
        csvzeile.put(header[6], (Double) sz.getAttribute("endbestand"));
        csvzeile.put(header[7], (String) sz.getAttribute("bemerkung"));

        writer.write(csvzeile, header, processors);
      }
      GUI.getStatusBar().setSuccessText("Auswertung fertig");
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
