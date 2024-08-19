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
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AnlagenverzeichnisPDF
{

  public AnlagenverzeichnisPDF(ArrayList<AnlagenlisteZeile> zeile,
      final File file, Date datumvon, Date datumbis) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      String subtitle = new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      Reporter reporter = new Reporter(fos, "Anlagenverzeichnis", subtitle,
          zeile.size());
      makeHeader(reporter);

      for (AnlagenlisteZeile akz : zeile)
      {
        switch (akz.getStatus())
        {
          case AnlagenlisteZeile.HEADER:
          {
            reporter.addColumn(
                (String) akz.getAttribute("buchungsklassenbezeichnung"),
                Element.ALIGN_LEFT, new BaseColor(220, 220, 220), 8);
            break;
          }
          case AnlagenlisteZeile.HEADER2:
          {
            reporter.addColumn(
                (String) akz.getAttribute("buchungsartbezeichnung"),
                Element.ALIGN_LEFT, 8);
            break;
          }
          case AnlagenlisteZeile.DETAIL:
          {
            reporter.addColumn(
                (String) akz.getAttribute("bezeichnung"),
                Element.ALIGN_RIGHT);
            if (akz.getAttribute("nutzungsdauer") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
            {
              Integer tmp = (Integer) akz.getAttribute("nutzungsdauer");
              reporter.addColumn(tmp.toString(), Element.ALIGN_RIGHT);
            }
            reporter.addColumn((String) akz.getAttribute("afaartbezeichnung"),
                Element.ALIGN_LEFT);
            if (akz.getAttribute("anschaffung") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Date) akz.getAttribute("anschaffung"),
                Element.ALIGN_RIGHT);
            if (akz.getAttribute("kosten") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("kosten"));
            if (akz.getAttribute("startwert") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("startwert"));
            if (akz.getAttribute("abschreibung") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("abschreibung"));
            if (akz.getAttribute("endwert") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("endwert"));
            break;
          }
          case AnlagenlisteZeile.SALDOFOOTER:
          {
            reporter.addColumn(
                (String) akz.getAttribute("buchungsklassenbezeichnung"),
                Element.ALIGN_RIGHT, 5);
            if (akz.getAttribute("startwert") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("startwert"));
            if (akz.getAttribute("abschreibung") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("abschreibung"));
            if (akz.getAttribute("endwert") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("endwert"));
            break;
          }
          case AnlagenlisteZeile.GESAMTSALDOFOOTER:
          {
            reporter.addColumn("Gesamt", Element.ALIGN_LEFT, 8);
            reporter.addColumn(
                (String) akz.getAttribute("buchungsklassenbezeichnung"),
                Element.ALIGN_RIGHT, 5);
            if (akz.getAttribute("startwert") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("startwert"));
            if (akz.getAttribute("abschreibung") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("abschreibung"));
            if (akz.getAttribute("endwert") == null)
              reporter.addColumn("", Element.ALIGN_LEFT);
            else
              reporter.addColumn((Double) akz.getAttribute("endwert"));
            break;
          }
        }
      }
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");
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
    reporter.addHeaderColumn("Bezeichnung", Element.ALIGN_CENTER, 50,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("ND", Element.ALIGN_CENTER, 10,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Afa Art", Element.ALIGN_CENTER, 50,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Anschaffung", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Anschaffungskosten", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Buchwert 01. Jan.", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Abschreibung", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Buchwert 31. Dez.", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.createHeader();
  }
}
