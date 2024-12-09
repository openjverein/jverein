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
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungExportPDF
{

  public SpendenbescheinigungExportPDF(final File file,
      final ArrayList<Spendenbescheinigung> spbList)
          throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, "Spendenbescheinigungen", "",
          spbList.size());
      reporter.addHeaderColumn("Bescheinigungsdatum", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Spendedatum", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 1", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 2", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 3", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 4", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 5", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 5", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zeile 7", Element.ALIGN_LEFT, 10,
          BaseColor.LIGHT_GRAY);
      reporter.createHeader();
      for (Spendenbescheinigung spb : spbList)
      {
        reporter.addColumn(spb.getBescheinigungsdatum(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getSpendedatum(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getBetrag());
        reporter.addColumn(spb.getZeile1(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getZeile2(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getZeile3(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getZeile4(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getZeile5(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getZeile6(), Element.ALIGN_LEFT);
        reporter.addColumn(spb.getZeile7(), Element.ALIGN_LEFT);
      }
      reporter.closeTable();
      reporter.close();
      fos.close();
      GUI.getStatusBar().setSuccessText("Ausgabe fertig.");
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }
}
