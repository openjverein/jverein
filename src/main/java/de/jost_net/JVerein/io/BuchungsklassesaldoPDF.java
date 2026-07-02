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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;

import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.BuchungsklasseSaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
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
  public void export(ArrayList<PseudoDBObject> zeile, final File file,
      ExportLayoutParam params) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, params);
      makeHeader(reporter, params.getColorHeader(), params.getFontHeader(),
          umbuchung);

      for (PseudoDBObject bkz : zeile)
      {
        switch (bkz.getInteger(AbstractSaldoControl.ART))
        {
          case AbstractSaldoControl.ART_HEADER:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_LEFT, params.getColorTable(), true,
                params.getFontNormal(), 4);
            break;
          }
          case AbstractSaldoControl.ART_DETAIL:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.BUCHUNGSART),
                Element.ALIGN_LEFT, params.getFontNormal());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN),
                params.getFontNormal(), params.getNegativRot());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.AUSGABEN),
                params.getFontNormal(), params.getNegativRot());
            if (umbuchung)
            {
              reporter.addColumn(
                  bkz.getDouble(AbstractSaldoControl.UMBUCHUNGEN),
                  params.getFontNormal(), params.getNegativRot());
            }
            break;
          }
          case AbstractSaldoControl.ART_SALDOFOOTER:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT, params.getFontFett());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN),
                params.getFontFett(), params.getNegativRot());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.AUSGABEN),
                params.getFontFett(), params.getNegativRot());
            if (umbuchung)
            {
              reporter.addColumn(
                  bkz.getDouble(AbstractSaldoControl.UMBUCHUNGEN),
                  params.getFontFett(), params.getNegativRot());
            }
            break;
          }
          case AbstractSaldoControl.ART_GESAMTSALDOFOOTER:
          {
            reporter.addColumn("Gesamt", Element.ALIGN_LEFT,
                params.getColorTable(), true, params.getFontNormal(), 4);
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT, params.getFontFett());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN),
                params.getFontFett(), params.getNegativRot());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.AUSGABEN),
                params.getFontFett(), params.getNegativRot());
            if (umbuchung)
            {
              reporter.addColumn(
                  bkz.getDouble(AbstractSaldoControl.UMBUCHUNGEN),
                  params.getFontFett(), params.getNegativRot());
            }
            break;
          }
          case AbstractSaldoControl.ART_GESAMTGEWINNVERLUST:
          case AbstractSaldoControl.ART_SALDOGEWINNVERLUST:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT, params.getFontFett());
            reporter.addColumn(bkz.getDouble(AbstractSaldoControl.EINNAHMEN),
                params.getFontFett(), params.getNegativRot());
            reporter.addColumn("", Element.ALIGN_LEFT, 2);
            break;
          }
          case AbstractSaldoControl.ART_NICHTZUGEORDNETEBUCHUNGEN:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_LEFT, params.getFontItalic());
            reporter.addColumn(
                bkz.getDouble(BuchungsklasseSaldoControl.EINNAHMEN),
                params.getFontItalic(), params.getNegativRot());
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

  private void makeHeader(Reporter reporter, BaseColor color, Font font,
      boolean umbuchung) throws DocumentException
  {
    reporter.addHeaderColumn("Buchungsart", Element.ALIGN_CENTER, 90, color,
        font);
    reporter.addHeaderColumn("Einnahmen", Element.ALIGN_CENTER, 30, color,
        font);
    reporter.addHeaderColumn("Ausgaben", Element.ALIGN_CENTER, 30, color, font);
    if (umbuchung)
    {
      reporter.addHeaderColumn("Umbuchungen", Element.ALIGN_CENTER, 30, color,
          font);
    }
    reporter.createHeader();
  }
}
