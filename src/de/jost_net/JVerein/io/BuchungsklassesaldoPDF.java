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

import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.BuchungsklasseSaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsklassesaldoPDF implements ISaldoExport
{

  private boolean umbuchung;

  public BuchungsklassesaldoPDF(boolean umbuchung)
  {
    this.umbuchung = umbuchung;
  }

  @Override
  public void export(ArrayList<PseudoDBObject> zeile,
      final File file, Date datumvon, Date datumbis, String title)
      throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      String subtitle = new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      Reporter reporter = new Reporter(fos, title, subtitle,
          zeile.size());
      makeHeader(reporter, umbuchung);

      for (PseudoDBObject bkz : zeile)
      {
        switch (bkz.getInteger(AbstractSaldoControl.ART))
        {
          case AbstractSaldoControl.ART_HEADER:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_LEFT, new BaseColor(220, 220, 220), 4);
            break;
          }
          case AbstractSaldoControl.ART_DETAIL:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.BUCHUNGSART),
                Element.ALIGN_LEFT);
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN));
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.AUSGABEN));
            if (umbuchung)
            {
              reporter
                  .addColumn(bkz.getDouble(AbstractSaldoControl.UMBUCHUNGEN));
            }
            break;
          }
          case AbstractSaldoControl.ART_SALDOFOOTER:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT);
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN));
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.AUSGABEN));
            if (umbuchung)
            {
              reporter
                  .addColumn(bkz.getDouble(AbstractSaldoControl.UMBUCHUNGEN));
            }
            break;
          }
          case AbstractSaldoControl.ART_GESAMTSALDOFOOTER:
          {
            reporter.addColumn("Gesamt", Element.ALIGN_LEFT, 4);
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT);
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN));
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.AUSGABEN));
            if (umbuchung)
            {
              reporter
                  .addColumn(bkz.getDouble(AbstractSaldoControl.UMBUCHUNGEN));
            }
            break;
          }
          case AbstractSaldoControl.ART_GESAMTGEWINNVERLUST:
          case AbstractSaldoControl.ART_SALDOGEWINNVERLUST:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT);
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN));
            reporter.addColumn("", Element.ALIGN_LEFT, 2);
            break;
          }
          case AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_LEFT);
            reporter
                .addColumn(
                    bkz.getInteger(BuchungsklasseSaldoControl.EINNAHMEN));
            reporter.addColumn("", Element.ALIGN_LEFT, 2);
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

  private void makeHeader(Reporter reporter, boolean umbuchung)
      throws DocumentException
  {
    reporter.addHeaderColumn("Buchungsart", Element.ALIGN_CENTER, 90,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Einnahmen", Element.ALIGN_CENTER, 45,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Ausgaben", Element.ALIGN_CENTER, 45,
        BaseColor.LIGHT_GRAY);
    if (umbuchung)
    {
      reporter.addHeaderColumn("Umbuchungen", Element.ALIGN_CENTER, 45,
          BaseColor.LIGHT_GRAY);
    }
    reporter.createHeader();
  }
}
