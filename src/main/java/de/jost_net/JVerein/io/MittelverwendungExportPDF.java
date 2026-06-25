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

import de.jost_net.JVerein.gui.control.MittelverwendungControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MittelverwendungExportPDF implements ISaldoExport
{

  private int tab;

  public MittelverwendungExportPDF(int tab)
  {
    this.tab = tab;
  }

  @Override
  public void export(ArrayList<PseudoDBObject> zeile, final File file,
      String title, String subtitle, SaldoExportParam params)
      throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, title, subtitle, params.getLinks(),
          params.getRechts(), params.getOben(), params.getUnten(), false,
          params.getVordergrund(), params.getHintergrund(),
          params.getQuerformat(), params.getHeaderTransparent(),
          params.getZellenTransparent());
      makeHeader(reporter, params.getColorHeader(), params.getFontHeader(),
          tab);

      for (PseudoDBObject mvz : zeile)
      {
        Integer pos = (Integer) mvz.getAttribute(MittelverwendungControl.NR);
        String position = pos == null ? "" : pos.toString();
        switch (mvz.getInteger(MittelverwendungControl.ART))
        {
          case MittelverwendungControl.ART_HEADER:
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn(
                  (String) mvz.getAttribute(MittelverwendungControl.GRUPPE),
                  Element.ALIGN_LEFT, params.getColorTable(), true,
                  params.getFontNormal(), 4);
            }
            break;
          case MittelverwendungControl.ART_DETAIL:
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn(
                  (String) mvz
                      .getAttribute(MittelverwendungControl.BEZEICHNUNG),
                  Element.ALIGN_LEFT, params.getFontNormal());
            }
            else
            {
              reporter.addColumn(position, Element.ALIGN_RIGHT,
                  params.getFontNormal());
              reporter.addColumn(
                  (String) mvz.getAttribute(MittelverwendungControl.GRUPPE),
                  Element.ALIGN_LEFT, params.getFontNormal());
            }
            reporter.addColumn(mvz.getDouble(MittelverwendungControl.BETRAG),
                params.getFontNormal(), params.getNegativRot());
            reporter.addColumn(" ", Element.ALIGN_LEFT);
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn(
                  (String) mvz.getAttribute(MittelverwendungControl.KOMMENTAR),
                  Element.ALIGN_LEFT, params.getFontNormal());
            }
            break;
          case MittelverwendungControl.ART_SALDOFOOTER:
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn(
                  (String) mvz.getAttribute(MittelverwendungControl.GRUPPE),
                  Element.ALIGN_RIGHT, null, true, params.getFontFett(), 2);
            }
            else
            {
              reporter.addColumn(position, Element.ALIGN_RIGHT,
                  params.getFontNormal());
              reporter.addColumn(
                  (String) mvz.getAttribute(MittelverwendungControl.GRUPPE),
                  Element.ALIGN_RIGHT, params.getFontFett());
              reporter.addColumn(" ", Element.ALIGN_LEFT);
            }

            Double value = mvz.getDouble(MittelverwendungControl.SUMME);
            if (value != null)
            {
              reporter.addColumn(value, params.getFontFett(),
                  params.getNegativRot());
            }
            else
            {
              reporter.addColumn(" ", Element.ALIGN_RIGHT);
            }
            if (tab == MittelverwendungControl.SALDO_REPORT)
            {
              reporter.addColumn(
                  (String) mvz.getAttribute(MittelverwendungControl.KOMMENTAR),
                  Element.ALIGN_LEFT, params.getFontNormal());
            }
            break;
          case MittelverwendungControl.ART_LEERZEILE:
            reporter.addColumn(" ", Element.ALIGN_LEFT, 4);
            break;
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

  private void makeHeader(Reporter reporter, BaseColor color, Font font,
      int tab) throws DocumentException
  {
    switch (tab)
    {
      case MittelverwendungControl.FLOW_REPORT:
        reporter.addHeaderColumn("Nr", Element.ALIGN_CENTER, 5, color, font);
        reporter.addHeaderColumn("Mittel", Element.ALIGN_CENTER, 69, color,
            font);
        reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 13, color,
            font);
        reporter.addHeaderColumn("Summe", Element.ALIGN_CENTER, 13, color,
            font);
        reporter.createHeader();
        break;
      case MittelverwendungControl.SALDO_REPORT:
        reporter.addHeaderColumn("Konto", Element.ALIGN_CENTER, 27, color,
            font);
        reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 13, color,
            font);
        reporter.addHeaderColumn("Summe", Element.ALIGN_CENTER, 13, color,
            font);
        reporter.addHeaderColumn("Kommentar", Element.ALIGN_CENTER, 20, color,
            font);
        reporter.createHeader();
        break;
    }
  }
}
