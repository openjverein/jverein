/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MittelverwendungExportPDF
{

  public MittelverwendungExportPDF(ArrayList<MittelverwendungZeile> zeile,
      final File file, Date datumvon, Date datumbis) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      String subtitle = "Geschäftsjahr: " + new JVDateFormatTTMMJJJJ().format(datumvon)
          + " - " + new JVDateFormatTTMMJJJJ().format(datumbis);
      Reporter reporter = new Reporter(fos, "Mittelverwendungsrechnung", subtitle,
          zeile.size());
      makeHeader(reporter);

      for (MittelverwendungZeile mvz : zeile)
      {
        switch (mvz.getStatus())
        {
          case MittelverwendungZeile.EINNAHME:
          case MittelverwendungZeile.AUSGABE:
          {
            reporter.addColumn(((Integer) mvz.getAttribute("position")).toString(),
                Element.ALIGN_LEFT);
            reporter.addColumn((String) mvz.getAttribute("bezeichnung"),
                Element.ALIGN_LEFT);
            reporter.addColumn((Double) mvz.getAttribute("betrag"));
            reporter.addColumn(" ",  Element.ALIGN_LEFT);
            break;
          }
          case MittelverwendungZeile.SUMME:
          {
            reporter.addColumn(
                ((Integer) mvz.getAttribute("position")).toString(),
                Element.ALIGN_LEFT);
            PdfPCell cell = null;
            cell = new PdfPCell(new Phrase(new Chunk(
                reporter.notNull((String) mvz.getAttribute("bezeichnung")),
                Reporter.getFreeSansBold(9))));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            reporter.addColumn(cell);
            reporter.addColumn(" ",  Element.ALIGN_LEFT);
            Font f = null;
            Double value = (Double) mvz.getAttribute("summe");
            if (value >= 0)
            {
              f = Reporter.getFreeSansBold(9, BaseColor.BLACK);
            }
            else
            {
              f = Reporter.getFreeSansBold(9, BaseColor.RED);
            }
            cell = new PdfPCell(
                new Phrase(Einstellungen.DECIMALFORMAT.format(value), f));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            reporter.addColumn(cell);
            break;
          }
        }
      }
      GUI.getStatusBar().setSuccessText("Export fertig.");
      reporter.closeTable();
      reporter.close();
      fos.close();
      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }

  private void makeHeader(Reporter reporter) throws DocumentException
  {
    reporter.addHeaderColumn("Nr", Element.ALIGN_CENTER, 5,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Mittel", Element.ALIGN_CENTER, 65,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 15,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Summe", Element.ALIGN_CENTER, 15,
        BaseColor.LIGHT_GRAY);
    reporter.createHeader();
  }
}
