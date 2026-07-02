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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedCSV extends AuswertungMitgliedAbstractCSV
{
  private String[] headerUser;

  private String[] headerKeys;

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    /*
     * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
     * objects[2] ist Mitgliedstyp
     */
    @SuppressWarnings("unchecked")
    ArrayList<Mitglied> list = (ArrayList<Mitglied>) objects[0];
    go(list, file);
  }

  public void go(ArrayList<Mitglied> list, File file)
      throws ApplicationException
  {
    try
    {
      ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

      Mitglied m = null;
      if (list.size() > 0)
      {
        m = list.get(0);
      }
      else
      {
        m = (Mitglied) Einstellungen.getDBService().createObject(Mitglied.class,
            null);
      }

      headerKeys = createHeader(m);
      headerUser = headerKeys;

      Logger.debug("Header");
      for (String s : headerKeys)
      {
        Logger.debug(s);
      }

      Map<String, Object> map = new MitgliedMap().getMap(m, null);
      // check headerKeys against map
      for (String key : headerKeys)
      {
        if (!map.containsKey(key))
        {
          writer.close();
          throw new ApplicationException("Invalid key: " + key);
        }
      }

      CellProcessor[] processors = CellProcessors.createCellProcessors(map,
          headerKeys);

      writer.writeHeader(headerUser);

      for (Mitglied mit : list)
      {
        writer.write(new MitgliedMap().getMap(mit, null), headerKeys,
            processors);
      }
      writer.close();
    }
    catch (Exception e)
    {
      Logger.error("Error while creating report", e);
      throw new ApplicationException(
          "Fehler beim Erzeugen des Reports (" + e.getMessage() + ")", e);
    }
  }

  private String[] createHeader(Mitglied m)
  {
    try
    {
      return new MitgliedMap().getMap(m, null).keySet().toArray(new String[0]);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return null;
  }

  @Override
  public String getName()
  {
    return "Mitgliederliste CSV";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != MitgliedListeView.class
        && objectType != NichtMitgliedListeView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return AuswertungMitgliedCSV.this.getName();
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
}
