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

package de.jost_net.jverein.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.DocumentException;

import de.jost_net.jverein.Einstellungen;
import de.jost_net.jverein.gui.control.BuchungsControl;
import de.jost_net.jverein.gui.view.AnlagenbuchungListeView;
import de.jost_net.jverein.gui.view.BuchungListeView;
import de.jost_net.jverein.keys.VorlageTyp;
import de.jost_net.jverein.queries.BuchungQuery;
import de.jost_net.jverein.rmi.Buchung;
import de.jost_net.jverein.util.VorlageUtil;
import de.jost_net.jverein.variable.BuchungMap;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungAuswertungExportCSV implements Exporter
{
  boolean geldkonto;

  @Override
  public String getName()
  {
    return "CSV Buchungen";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType == BuchungListeView.class)
    {
      geldkonto = true;
    }
    else if (objectType == AnlagenbuchungListeView.class)
    {
      geldkonto = false;
    }
    else
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return BuchungAuswertungExportCSV.this.getName();
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
    if (geldkonto)
    {
      return VorlageUtil.getName(VorlageTyp.CSVBUCHUNGEN_DATEINAME, object)
          + ".csv";
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_CSVBUCHUNGEN_DATEINAME,
          object) + ".csv";
    }
  }

  @Override
  public void calculateTitle(Object object)
  {
    // Kein Titel bei CSV
  }

  @Override
  public void calculateSubitle(Object object)
  {
    // Kein Subtitel bei CSV
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws RemoteException, ApplicationException,
      FileNotFoundException, DocumentException, IOException
  {
    BuchungsControl control = (BuchungsControl) objects[0];
    BuchungQuery query = control.getQuery();
    final List<Buchung> buchungen = query.get();

    ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

    String[] header = createHeader();

    Buchung bu = (Buchung) Einstellungen.getDBService()
        .createObject(Buchung.class, null);
    Map<String, Object> map = new BuchungMap().getMap(bu, null);
    CellProcessor[] processors = CellProcessors.createCellProcessors(map);

    writer.writeHeader(header);

    for (Buchung b : buchungen)
    {
      writer.write(new BuchungMap().getMap(b, null), header, processors);
    }
    writer.close();
  }

  private String[] createHeader()
  {
    try
    {
      Buchung b = (Buchung) Einstellungen.getDBService()
          .createObject(Buchung.class, null);
      return new BuchungMap().getMap(b, null).keySet().toArray(new String[0]);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return null;
  }
}
