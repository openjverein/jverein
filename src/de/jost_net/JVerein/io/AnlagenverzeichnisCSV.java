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
import de.jost_net.JVerein.gui.control.AnlagenlisteControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AnlagenverzeichnisCSV implements ISaldoExport
{

  private static CellProcessor[] getProcessors()
  {

    final CellProcessor[] processors = new CellProcessor[] { 
        new ConvertNullTo(""), // BuchungsArt/Klasse, Summe
        new ConvertNullTo(""), // Bezeichnung
        new ConvertNullTo("", new FmtNumber(Einstellungen.INTFORMAT)), // Nutzungsdauer
        new ConvertNullTo(""), // Afa Bezeichnung
        new ConvertNullTo(""), // Anschaffung
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Kosten
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Startwert
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Zugang
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Abschreibung
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)), // Abgang
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)) // Endwert
    };

    return processors;
  }

  @Override
  public void export(ArrayList<PseudoDBObject> zeilen, File file, Date datumvon,
      Date datumbis, String titel) throws ApplicationException
  {
    ICsvMapWriter writer = null;
    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      final CellProcessor[] processors = getProcessors();
      Map<String, Object> csvzeile = new HashMap<>();

      String[] header = { "Anlagenart", "Bezeichnung", "Nutzungsdauer", "Afa Art", 
          "Anschaffung", "Anschaffungskosten", "Buchwert Beginn GJ", "Zugang", 
          "Abschreibung", "Abgang", "Buchwert Ende GJ"};
      writer.writeHeader(header);

      String subtitle = new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      csvzeile.put(header[0], subtitle);
      writer.write(csvzeile, header, processors);

      for (PseudoDBObject akz : zeilen)
      {
        csvzeile = new HashMap<>();
        switch ((Integer) akz.getAttribute(AnlagenlisteControl.ART))
        {
          case AnlagenlisteControl.ART_HEADER:
          {
            csvzeile.put(header[0],
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE));
            break;
          }
          case AnlagenlisteControl.ART_DETAIL:
          {
            csvzeile.put(header[1],
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE));
            csvzeile.put(header[2],
                (Integer) akz.getAttribute(AnlagenlisteControl.NUTZUNGSDAUER));
            csvzeile.put(header[3],
                (String) akz.getAttribute(AnlagenlisteControl.AFAART));
            csvzeile.put(header[4],
                (Date) akz.getAttribute(AnlagenlisteControl.ANSCHAFFUNG_DATUM));
            csvzeile.put(header[5],
                (Double) akz.getAttribute(AnlagenlisteControl.BETRAG));
            csvzeile.put(header[6],
                (Double) akz.getAttribute(AnlagenlisteControl.STARTWERT));
            csvzeile.put(header[7],
                (Double) akz.getAttribute(AnlagenlisteControl.ZUGANG));
            csvzeile.put(header[8],
                (Double) akz.getAttribute(AnlagenlisteControl.ABSCHREIBUNG));
            csvzeile.put(header[9],
                (Double) akz.getAttribute(AnlagenlisteControl.ABGANG));
            csvzeile.put(header[10],
                (Double) akz.getAttribute(AnlagenlisteControl.ENDWERT));
            break;
          }
          case AnlagenlisteControl.ART_SALDOFOOTER:
          {
            csvzeile.put(header[0],
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE));
            csvzeile.put(header[6],
                (Double) akz.getAttribute(AnlagenlisteControl.STARTWERT));
            csvzeile.put(header[7],
                (Double) akz.getAttribute(AnlagenlisteControl.ZUGANG));
            csvzeile.put(header[8],
                (Double) akz.getAttribute(AnlagenlisteControl.ABSCHREIBUNG));
            csvzeile.put(header[9],
                (Double) akz.getAttribute(AnlagenlisteControl.ABGANG));
            csvzeile.put(header[10],
                (Double) akz.getAttribute(AnlagenlisteControl.ENDWERT));
            break;
          }
          case AnlagenlisteControl.ART_GESAMTSALDOFOOTER:
          {
            csvzeile.put(header[0],
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE));
            csvzeile.put(header[6],
                (Double) akz.getAttribute(AnlagenlisteControl.STARTWERT));
            csvzeile.put(header[7],
                (Double) akz.getAttribute(AnlagenlisteControl.ZUGANG));
            csvzeile.put(header[8],
                (Double) akz.getAttribute(AnlagenlisteControl.ABSCHREIBUNG));
            csvzeile.put(header[9],
                (Double) akz.getAttribute(AnlagenlisteControl.ABGANG));
            csvzeile.put(header[10],
                (Double) akz.getAttribute(AnlagenlisteControl.ENDWERT));
            break;
          }
        }

        writer.write(csvzeile, header, processors);
      }
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");
      writer.close();

      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
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
          Logger.error("error while creating report", e);
          throw new ApplicationException("Fehler", e);
        }
      }
    }
  }
}
