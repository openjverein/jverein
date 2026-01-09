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
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.UmsatzsteuerSaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class UmsatzsteuerSaldoCSV implements ISaldoExport
{

  private static CellProcessor[] getProcessors()
  {

    final CellProcessor[] processors = new CellProcessor[] {
        new ConvertNullTo(""), // Steuerart,
        new ConvertNullTo(""), // Steuer-Name
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Bemessungsgrundlage
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Steuer
    };
    return processors;
  }

  @Override
  public void export(ArrayList<PseudoDBObject> zeile, final File file,
      String title, String subtitle) throws ApplicationException
  {
    ICsvMapWriter writer = null;
    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final CellProcessor[] processors = getProcessors();
      Map<String, Object> csvzeile = new HashMap<>();
      String[] header;
      header = new String[] { "Steuerart", "Steuer Name", "Bemessungsgrundlage",
          "Steuer" };
      writer.writeHeader(header);

      csvzeile.put(header[0], subtitle);
      writer.write(csvzeile, header, processors);

      for (PseudoDBObject bkz : zeile)
      {
        csvzeile = new HashMap<>();
        switch (bkz.getInteger(AbstractSaldoControl.ART))
        {
          case AbstractSaldoControl.ART_HEADER:
          {
            csvzeile.put(header[0],
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE));
            break;
          }
          case AbstractSaldoControl.ART_DETAIL:
          {
            csvzeile.put(header[1],
                (String) bkz.getAttribute(UmsatzsteuerSaldoControl.STEUER));
            csvzeile.put(header[2],
                bkz.getDouble(UmsatzsteuerSaldoControl.BEMESSUNGSGRUNDLAGE));
            csvzeile.put(header[3],
                bkz.getDouble(UmsatzsteuerSaldoControl.STEUERBETRAG));
            break;
          }
          case AbstractSaldoControl.ART_SALDOFOOTER:
          case AbstractSaldoControl.ART_GESAMTSALDOFOOTER:
          {
            csvzeile.put(header[0],
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE));
            csvzeile.put(header[3],
                bkz.getDouble(UmsatzsteuerSaldoControl.STEUERBETRAG));
            break;
          }
          default:
          {
            csvzeile.put(header[0], "");
          }
        }
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
