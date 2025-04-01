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
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class KontenSaldoPDF
{

  public KontenSaldoPDF(ArrayList<KontoSaldoZeile> zeile, final File file,
      final Date datumvon, final Date datumbis) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      String subtitle = new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      Reporter reporter = new Reporter(fos, "Kontensaldo", subtitle,
          zeile.size());

      reporter.addHeaderColumn("Konto-\nnummer", Element.ALIGN_CENTER, 50,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Kontobezeichnung", Element.ALIGN_CENTER, 90,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Anfangs-\nbestand", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Einnahmen", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Ausgaben", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Um-\nbuchungen", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Endbestand", Element.ALIGN_CENTER, 55,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Bemerkung", Element.ALIGN_CENTER, 90,
          BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      for (KontoSaldoZeile sz : zeile)
      {
        reporter.addColumn((String) sz.getAttribute("kontonummer"),
            Element.ALIGN_LEFT);
        reporter.addColumn((String) sz.getAttribute("kontobezeichnung"),
            Element.ALIGN_LEFT);
        reporter.addColumn((Double) sz.getAttribute("anfangsbestand"));
        reporter.addColumn((Double) sz.getAttribute("einnahmen"));
        reporter.addColumn((Double) sz.getAttribute("ausgaben"));
        reporter.addColumn((Double) sz.getAttribute("umbuchungen"));
        reporter.addColumn((Double) sz.getAttribute("endbestand"));
        reporter.addColumn((String) sz.getAttribute("bemerkung"),
            Element.ALIGN_LEFT);
      }
      reporter.closeTable();
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");

      reporter.close();
      fos.close();
      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler beim Erzeugen des Reports", e);
    }
  }

}
