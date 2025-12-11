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
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.WirtschaftsplanNode;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.server.WirtschaftsplanImpl;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class WirtschaftsplanCSV implements Exporter
{

  @SuppressWarnings("unchecked")
  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws ApplicationException, RemoteException
  {
    Wirtschaftsplan[] wirtschaftsplaene;
    if (objects[0] instanceof Wirtschaftsplan[])
    {
      wirtschaftsplaene = (Wirtschaftsplan[]) objects[0];
    }
    else if (objects[0] instanceof Wirtschaftsplan)
    {
      wirtschaftsplaene = new Wirtschaftsplan[] {
          (Wirtschaftsplan) objects[0] };
    }
    else
    {
      throw new ApplicationException("Keine Pläne ausgewählt");
    }

    try (ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE))
    {
      String[] header = new String[3 + wirtschaftsplaene.length * 2 + 1];
      header[0] = "Buchungsklasse";
      header[1] = "Buchungsart";
      header[2] = "Posten";
      int i = 3;

      CellProcessor[] cellProcessor = new CellProcessor[3
          + wirtschaftsplaene.length * 2 + 1];
      cellProcessor[0] = new NotNull(); // Buchungsklasse
      cellProcessor[1] = new NotNull(); // Buchungsart
      cellProcessor[2] = new NotNull(); // Posten
      int j = 3;

      for (Wirtschaftsplan plan : wirtschaftsplaene)
      {
        header[i++] = plan.getBezeichung() + " "
            + new JVDateFormatTTMMJJJJ().format(plan.getDatumVon()) + "-"
            + new JVDateFormatTTMMJJJJ().format(plan.getDatumBis()) + " SOLL";

        header[i++] = plan.getBezeichung() + " "
            + new JVDateFormatTTMMJJJJ().format(plan.getDatumVon()) + "-"
            + new JVDateFormatTTMMJJJJ().format(plan.getDatumBis()) + " IST";

        cellProcessor[j++] = new ConvertNullTo("",
            new FmtNumber(Einstellungen.DECIMALFORMAT));
        cellProcessor[j++] = new ConvertNullTo("",
            new FmtNumber(Einstellungen.DECIMALFORMAT));
      }
      writer.writeHeader(header);

      DBIterator<Buchungsklasse> buchungsklasseIterator = Einstellungen
          .getDBService().createList(Buchungsklasse.class);
      switch ((Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT))
      {
        case BuchungsartSort.NACH_NUMMER:
          buchungsklasseIterator.setOrder("Order by nummer is null, nummer");
          break;
        default:
          buchungsklasseIterator
              .setOrder("Order by bezeichnung is NULL, bezeichnung");
          break;
      }

      while (buchungsklasseIterator.hasNext())
      {
        Buchungsklasse buchungsklasse = buchungsklasseIterator.next();
        for (int art : new int[] { WirtschaftsplanImpl.EINNAHME,
            WirtschaftsplanImpl.AUSGABE, WirtschaftsplanImpl.RUECKLAGE })
        {
          // Map mit allen anzuzeigenden Einträgen
          // <Buchungsart<Posten<Header,Wert>>>
          HashMap<String, HashMap<String, HashMap<String, Object>>> map = new HashMap<>();

          int n = -1;
          for (Wirtschaftsplan plan : wirtschaftsplaene)
          {
            n++;
            WirtschaftsplanNode buchungsklasseNode = new WirtschaftsplanNode(
                buchungsklasse, art, plan);
            String klasseKey = (String) buchungsklasseNode
                .getAttribute("buchungsklassebezeichnung");
            GenericIterator<WirtschaftsplanNode> buchungsartIt = buchungsklasseNode
                .getChildren();
            while (buchungsartIt.hasNext())
            {
              WirtschaftsplanNode buchungsartNode = buchungsartIt.next();

              String buchungsartKey = (String) buchungsartNode
                  .getAttribute("buchungsklassebezeichnung");

              HashMap<String, HashMap<String, Object>> entryMap = map
                  .getOrDefault(buchungsartKey, new HashMap<>());

              HashMap<String, Object> entry = entryMap.get("-");
              if (entry == null)
              {
                entry = new HashMap<String, Object>();
                entry.put(header[0], klasseKey);
                entry.put(header[1], buchungsartKey);
                entry.put(header[2], "");
              }

              entry.put(header[3 + n * 2], buchungsartNode.getSoll());
              entry.put(header[3 + n * 2 + 1], buchungsartNode.getIst());
              entryMap.put("-", entry);

              GenericIterator<WirtschaftsplanNode> it = buchungsartNode
                  .getChildren();
              while (it.hasNext())
              {
                WirtschaftsplanNode node = it.next();

                String nodekey = (String) node
                    .getAttribute("buchungsklassebezeichnung");

                entry = entryMap.get(nodekey);
                if (entry == null)
                {
                  entry = new HashMap<String, Object>();
                  entry.put(header[0], klasseKey);
                  entry.put(header[1], buchungsartKey);
                  entry.put(header[2], nodekey);
                }
                entry.put(header[3 + n * 2], node.getSoll());
                entry.put(header[3 + n * 2 + 1], node.getIst());
                entryMap.put(nodekey, entry);
              }
              map.put(buchungsartKey, entryMap);
            }
          }
          map.entrySet().stream().sorted(Map.Entry
              .<String, HashMap<String, HashMap<String, Object>>> comparingByKey())
              .forEach(buchungsartEntry -> {
                // Posten
                buchungsartEntry.getValue().entrySet().stream()
                    .sorted(Map.Entry
                        .<String, HashMap<String, Object>> comparingByKey())
                    .forEach(postenEntry -> {
                      try
                      {
                        writer.write(postenEntry.getValue(), header,
                            cellProcessor);
                      }
                      catch (IOException e)
                      {
                        Logger.error("Fehler beim Wirtschaftsplan Export", e);
                      }
                    });
              });
        }
      }

    }
    catch (Exception e)
    {
      Logger.error("Error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }

  @Override
  public String getName()
  {
    return "Wirtschaftsplan CSV-Export";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Wirtschaftsplan.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return WirtschaftsplanCSV.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.csv" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public String getDateiname(Object object)
  {
    return VorlageUtil
        .getName(object == null ? VorlageTyp.WIRTSCHAFTSPLAN_MEHRERE_DATEINAME
            : VorlageTyp.WIRTSCHAFTSPLAN_DATEINAME, object)
        + ".csv";
  }

  @Override
  public void calculateTitle(Object object)
  {
    // Bei CSV nicht nötig
  }

  @Override
  public void calculateSubitle(Object object)
  {
    // Bei CSV nicht nötig
  }
}
