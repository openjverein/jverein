/**********************************************************************
 * Copyright (c) by Markus Spann
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.util.ApplicationException;

/**
 * Druckt die Übersicht einer {@link Basislastschrift} als PDF - Ersatz für
 * OBanToos {@code Basislastschrift.Basislastschrift2Pdf}, im Stil der
 * übrigen JVerein-PDF-Reports (siehe {@link Reporter}).
 *
 * @since 4.3.0
 */
public class Basislastschrift2Pdf
{
  /**
   * @param lastschrift die zu druckende Basislastschrift.
   * @param pdfDatei der Pfad der zu erzeugenden PDF-Datei.
   * @throws IOException wenn die PDF-Datei nicht geschrieben werden kann.
   * @throws DocumentException wenn das PDF nicht erzeugt werden kann.
   * @throws ApplicationException wenn ein Feld eines Zahlers ungültig ist.
   */
  public Basislastschrift2Pdf(Basislastschrift lastschrift, String pdfDatei)
      throws IOException, DocumentException, ApplicationException
  {
    try (FileOutputStream fos = new FileOutputStream(pdfDatei);
        Reporter reporter = new Reporter(fos, "SEPA-Basislastschrift",
            lastschrift.getName()))
    {
      reporter.add(new Paragraph(String.format(
          "Gläubiger-ID: %s | BIC: %s | IBAN: %s", lastschrift.getGlaeubigerID(),
          lastschrift.getBIC(), lastschrift.getIBAN()), Reporter.getFreeSans(9)));
      reporter.add(new Paragraph(String.format(
          "Erstellungsdatum: %s | Anzahl Buchungen: %d | Kontrollsumme: %s | "
              + "Message-ID: %s",
          new JVDateFormatTTMMJJJJ().format(lastschrift.getCreationDateTime()),
          lastschrift.getAnzahlBuchungen(),
          Einstellungen.DECIMALFORMAT
              .format(lastschrift.getKontrollsumme().doubleValue()),
          lastschrift.getMessageID()), Reporter.getFreeSans(9)));

      reporter.addHeaderColumn("Name", Element.ALIGN_CENTER, 80,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Verwendungszweck", Element.ALIGN_CENTER, 95,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Bankverbindung", Element.ALIGN_CENTER, 85,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Mandat", Element.ALIGN_CENTER, 65,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 35,
          BaseColor.LIGHT_GRAY);
      reporter.createHeader(100, Element.ALIGN_CENTER);

      lastschrift.getZahler().sort(Comparator.comparing(z -> {
        try
        {
          return z.getName();
        }
        catch (ApplicationException e)
        {
          return "";
        }
      }));
      for (Zahler z : lastschrift.getZahler())
      {
        reporter.addColumn(z.getName(), Element.ALIGN_LEFT);
        reporter.addColumn(z.getVerwendungszweck(), Element.ALIGN_LEFT);
        reporter.addColumn(z.getBic() + "\n" + z.getIban(), Element.ALIGN_LEFT);
        reporter.addColumn(z.getMandatid() + " " + z.getMandatsequence().getTxt()
            + "\n" + new JVDateFormatTTMMJJJJ().format(z.getMandatdatum()),
            Element.ALIGN_LEFT);
        reporter.addColumn(z.getBetrag().doubleValue());
      }
      reporter.closeTable();
    }
  }
}
