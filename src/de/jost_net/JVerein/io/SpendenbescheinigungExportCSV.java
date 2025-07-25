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
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungExportCSV
{

  private static CellProcessor[] getProcessors()
  {
    final CellProcessor[] processors = new CellProcessor[] {
        new ConvertNullTo(""), new ConvertNullTo(""),
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Betrag
        new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo(""),
        new ConvertNullTo(""), new ConvertNullTo(""), new ConvertNullTo(""),
        new ConvertNullTo(""), };

    return processors;
  }

  public SpendenbescheinigungExportCSV(final File file,
      final ArrayList<Spendenbescheinigung> spbList) throws ApplicationException
  {
    ICsvMapWriter writer = null;
    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final CellProcessor[] processors = getProcessors();
      Map<String, Object> csvzeile = new HashMap<>();

      // Header
      String[] header = { "Bescheinigungsdatum", "Spendedatum", "Betrag",
          "Zeile 1", "Zeile 2", "Zeile 3", "Zeile 4", "Zeile 5", "Zeile 6",
          "Zeile 7" };
      writer.writeHeader(header);

      // Einträge
      for (Spendenbescheinigung spb : spbList)
      {
        csvzeile.put(header[0], spb.getBescheinigungsdatum());
        csvzeile.put(header[1], spb.getSpendedatum());
        csvzeile.put(header[2], (Double) spb.getBetrag());
        csvzeile.put(header[3], spb.getZeile1());
        csvzeile.put(header[4], spb.getZeile2());
        csvzeile.put(header[5], spb.getZeile3());
        csvzeile.put(header[6], spb.getZeile4());
        csvzeile.put(header[7], spb.getZeile5());
        csvzeile.put(header[8], spb.getZeile6());
        csvzeile.put(header[9], spb.getZeile7());
        writer.write(csvzeile, header, processors);
      }

      GUI.getStatusBar().setSuccessText("Ausgabe fertig");
      writer.close();
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
