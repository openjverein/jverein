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
import de.jost_net.JVerein.gui.control.UmsatzsteuerSaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class UmsatzsteuerSaldoPDF implements ISaldoExport
{

  @Override
  public void export(ArrayList<PseudoDBObject> zeile, final File file,
      SaldoExportParam params) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, params.getTitle(),
          params.getSubtitle(), params.getLinks(), params.getRechts(),
          params.getOben(), params.getUnten(), false, params.getVordergrund(),
          params.getHintergrund(), params.getQuerformat(),
          params.getHeaderTransparent(), params.getZellenTransparent());
      makeHeader(reporter, params.getColorHeader(), params.getFontHeader());

      for (PseudoDBObject bkz : zeile)
      {
        switch (bkz.getInteger(AbstractSaldoControl.ART))
        {
          case AbstractSaldoControl.ART_HEADER:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_LEFT, params.getColorTable(), true,
                params.getFontNormal(), 3);
            break;
          }
          case AbstractSaldoControl.ART_DETAIL:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(UmsatzsteuerSaldoControl.STEUER),
                Element.ALIGN_LEFT, params.getFontNormal());
            reporter.addColumn(
                bkz.getDouble(UmsatzsteuerSaldoControl.BEMESSUNGSGRUNDLAGE),
                params.getFontNormal(), params.getNegativRot());
            reporter.addColumn(
                bkz.getDouble(UmsatzsteuerSaldoControl.STEUERBETRAG),
                params.getFontNormal(), params.getNegativRot());
            break;
          }
          case AbstractSaldoControl.ART_SALDOFOOTER:
          case AbstractSaldoControl.ART_GESAMTSALDOFOOTER:
          {
            reporter.addColumn(
                (String) bkz.getAttribute(AbstractSaldoControl.GRUPPE),
                Element.ALIGN_RIGHT, null, true, params.getFontFett(), 2);
            reporter.addColumn(
                bkz.getDouble(UmsatzsteuerSaldoControl.STEUERBETRAG),
                params.getFontFett(), params.getNegativRot());
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

  private void makeHeader(Reporter reporter, BaseColor color, Font font)
      throws DocumentException
  {
    reporter.addHeaderColumn("Steuer Name", Element.ALIGN_CENTER, 45, color,
        font);
    reporter.addHeaderColumn("Bemessungsgrundlage", Element.ALIGN_CENTER, 45,
        color, font);
    reporter.addHeaderColumn("Steuer", Element.ALIGN_CENTER, 45, color, font);
    reporter.createHeader();
  }
}
