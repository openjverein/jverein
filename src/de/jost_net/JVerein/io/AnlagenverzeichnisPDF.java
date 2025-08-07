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

import de.jost_net.JVerein.gui.control.AnlagenlisteControl;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AnlagenverzeichnisPDF implements ISaldoExport
{

  @Override
  public void export(ArrayList<PseudoDBObject> zeilen, File file, Date datumvon,
      Date datumbis) throws ApplicationException
  {
    try
    {
      boolean hasZugang = false;
      boolean hasAbgang = false;
      int anzahlSpalten = 10;
      for (PseudoDBObject alz : zeilen)
      {
        if (alz.getAttribute(AnlagenlisteControl.ZUGANG) != null
            && Math.abs(alz.getDouble(AnlagenlisteControl.ZUGANG)) >= 0.01d)
        {
          hasZugang = true;
        }
        if (alz.getAttribute(AnlagenlisteControl.ABGANG) != null
            && Math.abs(alz.getDouble(AnlagenlisteControl.ABGANG)) >= 0.01d)
        {
          hasAbgang = true;
        }
      }
      if (!hasAbgang)
      {
        anzahlSpalten--;
      }
      if (!hasZugang)
      {
        anzahlSpalten--;
      }

      FileOutputStream fos = new FileOutputStream(file);
      String subtitle = "Geschäftsjahr: "
          + new JVDateFormatTTMMJJJJ().format(datumvon) + " - "
          + new JVDateFormatTTMMJJJJ().format(datumbis);
      Reporter reporter = new Reporter(fos, "Anlagenverzeichnis", subtitle,
          zeilen.size());
      makeHeader(reporter, anzahlSpalten, hasZugang, hasAbgang);

      for (PseudoDBObject akz : zeilen)
      {
        switch ((Integer) akz.getAttribute(AnlagenlisteControl.ART))
        {
          case AnlagenlisteControl.ART_HEADER:
          {
            reporter.addColumn(
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE),
                Element.ALIGN_LEFT, new BaseColor(220, 220, 220),
                anzahlSpalten);
            break;
          }
          case AnlagenlisteControl.ART_DETAIL:
          {
            reporter.addColumn(
                (String) akz.getAttribute(AnlagenlisteControl.KONTO),
                Element.ALIGN_LEFT);
            Integer tmp = (Integer) akz
                .getAttribute(AnlagenlisteControl.NUTZUNGSDAUER);
            reporter.addColumn(tmp == null ? "" : tmp.toString(),
                Element.ALIGN_RIGHT);
            reporter.addColumn(
                (String) akz.getAttribute(AnlagenlisteControl.AFAART),
                Element.ALIGN_LEFT);
            reporter.addColumn(
                (Date) akz.getAttribute(AnlagenlisteControl.ANSCHAFFUNG_DATUM),
                Element.ALIGN_RIGHT);
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.BETRAG));
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.STARTWERT));
            if (hasZugang)
            {
              reporter.addColumn(
                  (Double) akz.getAttribute(AnlagenlisteControl.ZUGANG));
            }
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.ABSCHREIBUNG));
            if (hasAbgang)
            {
              reporter.addColumn(
                  (Double) akz.getAttribute(AnlagenlisteControl.ABGANG));
            }
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.ENDWERT));
            break;
          }
          case AnlagenlisteControl.ART_SALDOFOOTER:
          {
            reporter.addColumn(
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE),
                Element.ALIGN_RIGHT, 5);
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.STARTWERT));
            if (hasZugang)
            {
              reporter.addColumn(
                  (Double) akz.getAttribute(AnlagenlisteControl.ZUGANG));
            }
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.ABSCHREIBUNG));
            if (hasAbgang)
            {
              reporter.addColumn(
                  (Double) akz.getAttribute(AnlagenlisteControl.ABGANG));
            }
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.ENDWERT));
            break;
          }
          case AnlagenlisteControl.ART_GESAMTSALDOFOOTER:
          {
            reporter.addColumn("Gesamt", Element.ALIGN_LEFT, anzahlSpalten);
            reporter.addColumn(
                (String) akz.getAttribute(AnlagenlisteControl.GRUPPE),
                Element.ALIGN_RIGHT, 5);
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.STARTWERT));
            if (hasZugang)
            {
              reporter.addColumn(
                  (Double) akz.getAttribute(AnlagenlisteControl.ZUGANG));
            }
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.ABSCHREIBUNG));
            if (hasAbgang)
            {
              reporter.addColumn(
                  (Double) akz.getAttribute(AnlagenlisteControl.ABGANG));
            }
            reporter.addColumn(
                (Double) akz.getAttribute(AnlagenlisteControl.ENDWERT));
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

  private void makeHeader(Reporter reporter, int anzahlSpalten,
      boolean hasZugang, boolean hasAbgang) throws DocumentException
  {
    int width = 25;
    switch (anzahlSpalten)
    {
      case 9:
        width = 35;
        break;
      case 8:
        width = 50;
    }
    reporter.addHeaderColumn("Bezeichnung", Element.ALIGN_CENTER, width,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("ND", Element.ALIGN_CENTER, 10,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Afa Art", Element.ALIGN_CENTER, width,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Anschaffung", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Anschaffungskosten", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Buchwert Beginn GJ", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    if (hasZugang)
    {
      reporter.addHeaderColumn("Zugang", Element.ALIGN_CENTER, 20,
          BaseColor.LIGHT_GRAY);
    }
    reporter.addHeaderColumn("Abschreibung", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    if (hasAbgang)
    {
      reporter.addHeaderColumn("Abgang", Element.ALIGN_CENTER, 20,
          BaseColor.LIGHT_GRAY);
    }
    reporter.addHeaderColumn("Buchwert Ende GJ", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.createHeader();
  }
}
