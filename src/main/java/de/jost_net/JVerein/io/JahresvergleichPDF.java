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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;

import de.jost_net.JVerein.server.PseudoDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/** PDF-Ausgabe des Geschäftsjahresvergleichs. */
public class JahresvergleichPDF implements ISaldoExport
{
  private final JahresvergleichDaten daten;

  private final Set<JahresvergleichDaten.Abschnitt> abschnitte;

  public JahresvergleichPDF(JahresvergleichDaten daten)
  {
    this(daten, EnumSet.allOf(JahresvergleichDaten.Abschnitt.class));
  }

  public JahresvergleichPDF(JahresvergleichDaten daten,
      Set<JahresvergleichDaten.Abschnitt> abschnitte)
  {
    this.daten = daten;
    this.abschnitte = Set.copyOf(abschnitte);
  }

  boolean istAbschnittAusgewaehlt(JahresvergleichDaten.Abschnitt abschnitt)
  {
    return abschnitte.contains(abschnitt);
  }

  @Override
  public void export(ArrayList<PseudoDBObject> ignored, File file,
      ExportLayoutParam params) throws ApplicationException
  {
    try (FileOutputStream fos = new FileOutputStream(file);
        Reporter reporter = new Reporter(fos, params))
    {
      if (istAbschnittAusgewaehlt(JahresvergleichDaten.Abschnitt.GESAMT))
      {
        addSection(reporter, "1. Gesamtübersicht",
            JahresvergleichDaten.Abschnitt.GESAMT, params);
      }
      if (istAbschnittAusgewaehlt(
          JahresvergleichDaten.Abschnitt.BUCHUNGSART))
      {
        addSection(reporter, "2. Buchungsartenvergleich",
            JahresvergleichDaten.Abschnitt.BUCHUNGSART, params);
      }
      if (istAbschnittAusgewaehlt(JahresvergleichDaten.Abschnitt.PROJEKT))
      {
        addSection(reporter, "3. Projektvergleich",
            JahresvergleichDaten.Abschnitt.PROJEKT, params);
      }
      reporter.closeTable();
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");
      FileViewer.show(file);
    }
    catch (Exception e)
    {
      Logger.error("error while creating annual comparison", e);
      throw new ApplicationException("Fehler beim Erstellen des Jahresvergleichs.", e);
    }
  }

  private void addSection(Reporter reporter, String title,
      JahresvergleichDaten.Abschnitt abschnitt, ExportLayoutParam params)
      throws DocumentException
  {
    reporter.closeTable();
    reporter.add(title, 11);
    addHeaders(reporter, params.getColorHeader(), params.getFontHeader());
    reporter.createHeader();
    List<JahresvergleichDaten.Vergleichszeile> zeilen = daten
        .getZeilen(abschnitt);
    String vorherigesProjekt = null;
    boolean ersteBuchungsklasse = true;
    for (JahresvergleichDaten.Vergleichszeile zeile : zeilen)
    {
      if (abschnitt == JahresvergleichDaten.Abschnitt.BUCHUNGSART
          && zeile.getTyp() == JahresvergleichDaten.Zeilentyp.GRUPPENKOPF)
      {
        if (!ersteBuchungsklasse)
        {
          reporter.addColumn(getProjektTrennzeile());
        }
        ersteBuchungsklasse = false;
        reporter.addColumn(getBuchungsklasseKopf(zeile.getName(), params));
        continue;
      }
      if (abschnitt == JahresvergleichDaten.Abschnitt.PROJEKT)
      {
        if (vorherigesProjekt != null
            && !vorherigesProjekt.equals(zeile.getName()))
        {
          reporter.addColumn(getProjektTrennzeile());
        }
        vorherigesProjekt = zeile.getName();
      }
      if (zeile.getTyp() == JahresvergleichDaten.Zeilentyp.ZWISCHENSUMME)
      {
        reporter.addColumn("Saldo Buchungsklasse", Element.ALIGN_LEFT,
            params.getFontFett());
        reporter.addColumn(zeile.getAktuell(), params.getColorTable(),
            params.getFontFett(), params.getNegativRot());
        reporter.addColumn(zeile.getVorjahr(), params.getColorTable(),
            params.getFontFett(), params.getNegativRot());
        reporter.addColumn(zeile.getVeraenderung(), params.getColorTable(),
            params.getFontFett(), params.getNegativRot());
        reporter.addColumn(zeile.getVeraenderungProzent(),
            Element.ALIGN_RIGHT, params.getFontFett());
        continue;
      }
      Font zeilenFont = params.getFontNormal();
      if (abschnitt == JahresvergleichDaten.Abschnitt.PROJEKT
          && zeile.getKennzahl() == JahresvergleichDaten.Kennzahl.SALDO)
      {
        zeilenFont = new Font(zeilenFont);
        zeilenFont.setStyle(Font.BOLD);
      }
      reporter.addColumn(zeile.getName() + " - "
          + zeile.getKennzahl().getBezeichnung(), Element.ALIGN_LEFT,
          zeilenFont);
      reporter.addColumn(zeile.getAktuell(), zeilenFont,
          params.getNegativRot());
      reporter.addColumn(zeile.getVorjahr(), zeilenFont,
          params.getNegativRot());
      reporter.addColumn(zeile.getVeraenderung(), zeilenFont,
          params.getNegativRot());
      reporter.addColumn(zeile.getVeraenderungProzent(), Element.ALIGN_RIGHT,
          zeilenFont);
    }
    reporter.closeTable();
  }

  private PdfPCell getBuchungsklasseKopf(String name,
      ExportLayoutParam params)
  {
    PdfPCell kopf = new PdfPCell(new Phrase(name, params.getFontFett()));
    kopf.setColspan(5);
    kopf.setPadding(4f);
    kopf.setBackgroundColor(params.getColorTable());
    kopf.setBorder(Rectangle.BOTTOM);
    kopf.setBorderWidthBottom(1.2f);
    kopf.setBorderColorBottom(new BaseColor(120, 120, 120));
    return kopf;
  }

  private PdfPCell getProjektTrennzeile()
  {
    PdfPCell trennzeile = new PdfPCell(new Phrase(""));
    trennzeile.setColspan(5);
    trennzeile.setFixedHeight(4f);
    trennzeile.setBorder(Rectangle.BOTTOM);
    trennzeile.setBorderWidthBottom(1.2f);
    trennzeile.setBorderColorBottom(new BaseColor(150, 150, 150));
    return trennzeile;
  }

  private void addHeaders(Reporter reporter, BaseColor color, Font font)
  {
    reporter.addHeaderColumn("Position", Element.ALIGN_LEFT, 65, color, font);
    reporter.addHeaderColumn("Aktuell", Element.ALIGN_RIGHT, 20, color, font);
    reporter.addHeaderColumn("Vorjahr", Element.ALIGN_RIGHT, 20, color, font);
    reporter.addHeaderColumn("Veränderung", Element.ALIGN_RIGHT, 23, color,
        font);
    reporter.addHeaderColumn("Veränderung %", Element.ALIGN_RIGHT, 22, color,
        font);
  }
}
