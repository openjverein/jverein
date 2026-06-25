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

import com.itextpdf.text.Element;

import de.jost_net.JVerein.gui.control.KontensaldoControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class KontenSaldoPDF implements ISaldoExport
{

  public KontenSaldoPDF()
  {
  }

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

      reporter.addHeaderColumn("Konto-\nnummer", Element.ALIGN_CENTER, 50,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Kontobezeichnung", Element.ALIGN_CENTER, 90,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Anfangs-\nbestand", Element.ALIGN_CENTER, 45,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Einnahmen", Element.ALIGN_CENTER, 45,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Ausgaben", Element.ALIGN_CENTER, 45,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Um-\nbuchungen", Element.ALIGN_CENTER, 45,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Endbestand", Element.ALIGN_CENTER, 55,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Bemerkung", Element.ALIGN_CENTER, 90,
          params.getColorHeader(), params.getFontHeader());
      reporter.createHeader();

      for (PseudoDBObject sz : zeile)
      {
        reporter.addColumn(
            (String) sz.getAttribute(KontensaldoControl.KONTO_NUMMER),
            Element.ALIGN_LEFT, params.getFontNormal());
        reporter.addColumn((String) sz.getAttribute(KontensaldoControl.GRUPPE),
            Element.ALIGN_LEFT, params.getFontNormal());
        reporter.addColumn(
            (Double) sz.getAttribute(KontensaldoControl.ANFANGSBESTAND),
            params.getFontNormal(), params.getNegativRot());
        reporter.addColumn(
            (Double) sz.getAttribute(KontensaldoControl.EINNAHMEN),
            params.getFontNormal(), params.getNegativRot());
        reporter.addColumn(
            (Double) sz.getAttribute(KontensaldoControl.AUSGABEN),
            params.getFontNormal(), params.getNegativRot());
        reporter.addColumn(
            (Double) sz.getAttribute(KontensaldoControl.UMBUCHUNGEN),
            params.getFontNormal(), params.getNegativRot());
        reporter.addColumn(
            (Double) sz.getAttribute(KontensaldoControl.ENDBESTAND),
            params.getFontNormal(), params.getNegativRot());
        reporter.addColumn(
            (String) sz.getAttribute(KontensaldoControl.BEMERKUNG),
            Element.ALIGN_LEFT, params.getFontNormal());
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
