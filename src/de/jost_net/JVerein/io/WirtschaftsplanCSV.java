/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanCSV
{
  public WirtschaftsplanCSV(List<WirtschaftsplanNode> einnahmenList,
      List<WirtschaftsplanNode> ausgabenList, final File file)
      throws ApplicationException
  {
    ICsvMapWriter writer = null;

    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

      String[] header = { "Buchungsklasse", "Buchungsart", "Posten",
          "Einnahmen", "Ausgaben" };
      writer.writeHeader(header);

      ICsvMapWriter finalWriter = writer;
      einnahmenList.forEach(einnahme -> {
        try
        {
          iterateOverNodes(einnahme.getChildren(), finalWriter, header, true);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      });
      ausgabenList.forEach(ausgabe -> {
        try
        {
          iterateOverNodes(ausgabe.getChildren(), finalWriter, header, false);
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      });

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

  private static CellProcessor[] getProcessors()
  {

    return new CellProcessor[] { new NotNull(), // Buchungsklasse
        new NotNull(), // Buchungsart
        new NotNull(), // Posten
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT)),
        // Einnahmen
        new ConvertNullTo("", new FmtNumber(Einstellungen.DECIMALFORMAT))
        // Ausgaben
    };
  }

  private void iterateOverNodes(
      @SuppressWarnings("rawtypes") GenericIterator iterator,
      ICsvMapWriter writer, String[] header, boolean einnahme)
      throws IOException
  {
    while (iterator.hasNext())
    {
      WirtschaftsplanNode currentNode = (WirtschaftsplanNode) iterator.next();

      if (currentNode.getType().equals(WirtschaftsplanNode.Type.POSTEN))
      {
        Map<String, Object> csvzeile = new HashMap<>();

        WirtschaftsplanNode parent = (WirtschaftsplanNode) currentNode
            .getParent();
        WirtschaftsplanNode root = (WirtschaftsplanNode) parent.getParent();

        csvzeile.put(header[0], root.getBuchungsklasse().getBezeichnung());
        csvzeile.put(header[1], parent.getBuchungsart().getBezeichnung());
        csvzeile.put(header[2],
            currentNode.getWirtschaftsplanItem().getPosten());

        if (einnahme)
        {
          csvzeile.put(header[3],
              currentNode.getWirtschaftsplanItem().getSoll());
        }
        else
        {
          csvzeile.put(header[4],
              currentNode.getWirtschaftsplanItem().getSoll());
        }

        writer.write(csvzeile, header, getProcessors());
      }
      else
      {
        iterateOverNodes(currentNode.getChildren(), writer, header, einnahme);
      }
    }
  }
}
