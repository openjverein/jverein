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
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Map.Entry;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.AuswertungKursteilnehmerView;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungKursteilnehmerPDF implements Exporter
{

  @SuppressWarnings("unchecked")
  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    try
    {
      /*
       * objects[0] ist der Filter
       */
      final DBIterator<Kursteilnehmer> list = getIterator(
          (Map<Filter, Object>) objects[0]);

      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, params.getTitle(),
          params.getSubtitle(), params.getLinks(), params.getRechts(),
          params.getOben(), params.getUnten(), false, params.getVordergrund(),
          params.getHintergrund(), params.getQuerformat(),
          params.getHeaderTransparent(), params.getZellenTransparent());

      reporter.addHeaderColumn("Datum", Element.ALIGN_LEFT, 50,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Name", Element.ALIGN_LEFT, 80,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Verwendungszweck", Element.ALIGN_LEFT, 80,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 40,
          params.getColorHeader(), params.getFontHeader());
      reporter.createHeader();
      while (list.hasNext())
      {
        Kursteilnehmer kt = list.next();
        reporter.addColumn(kt.getAbbudatum(), Element.ALIGN_LEFT,
            params.getFontNormal());
        reporter.addColumn(kt.getName(), Element.ALIGN_LEFT,
            params.getFontNormal());
        reporter.addColumn(kt.getVZweck1(), Element.ALIGN_LEFT,
            params.getFontNormal());
        reporter.addColumn(kt.getBetrag(), params.getFontNormal(),
            params.getNegativRot());
      }
      reporter.close();
      fos.close();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException("Fehler: " + e.getMessage());
    }
  }

  private DBIterator<Kursteilnehmer> getIterator(Map<Filter, Object> filter)
      throws RemoteException, ApplicationException
  {
    DBIterator<Kursteilnehmer> kursteilnehmer = Einstellungen.getDBService()
        .createList(Kursteilnehmer.class);

    for (Entry<Filter, Object> entry : filter.entrySet())
    {
      Object value = entry.getValue();
      switch (entry.getKey())
      {
        case ABBUCHUNGSDATUM_VON:
          kursteilnehmer.addFilter("abbudatum >= ?", value);
          break;
        case ABBUCHUNGSDATUM_BIS:
          kursteilnehmer.addFilter("abbudatum <= ?", value);
          break;
        default:
          throw new ApplicationException(
              "Filter nicht implementiert: " + entry.getKey().getAnzeigeText());
      }
    }
    return kursteilnehmer;
  }

  @Override
  public String getName()
  {
    return "Auswertng Kursteilnehmer PDF";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != AuswertungKursteilnehmerView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return AuswertungKursteilnehmerPDF.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.pdf" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public String getDateiname(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_KURSTEILNEHMER_DATEINAME,
        object) + ".pdf";
  }

  @Override
  public String getTitle(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_KURSTEILNEHMER_TITEL,
        object);
  }

  @Override
  public String getSubtitle(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.AUSWERTUNG_KURSTEILNEHMER_SUBTITEL,
        object);
  }
}
