/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.util.ProgressMonitor;

public class AuswertungGeburtstagslistePDF
    extends AuswertungMitgliedAbstractPDF
{
  private record Geburtstag(int monat, int tag, int jahr, String name)
  {
  }

  @Override
  public String getName()
  {
    return "Geburtstagsliste PDF";
  }

  @Override
  public String getTitle(Object object)
  {
    return "Geburtstagsliste " + Year.now().getValue();
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != MitgliedListeView.class)
    {
      return null;
    }
    return new IOFormat[] { new IOFormat()
    {
      @Override
      public String getName()
      {
        return AuswertungGeburtstagslistePDF.this.getName();
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.pdf" };
      }
    } };
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws DocumentException, IOException
  {
    int jahr = Year.now().getValue();
    List<Geburtstag> geburtstage = new ArrayList<>();
    int ohneGeburtsdatum = 0;
    Calendar kalender = Calendar.getInstance();
    for (Object object : (List<?>) objects[0])
    {
      Mitglied mitglied = (Mitglied) object;
      Date geburtsdatum = mitglied.getGeburtsdatum();
      if (geburtsdatum == null)
      {
        ohneGeburtsdatum++;
        continue;
      }
      kalender.setTime(geburtsdatum);
      geburtstage.add(new Geburtstag(kalender.get(Calendar.MONTH) + 1,
          kalender.get(Calendar.DAY_OF_MONTH), kalender.get(Calendar.YEAR),
          Adressaufbereitung.getVornameName(mitglied).trim()));
    }
    geburtstage.sort(Comparator.comparingInt(Geburtstag::monat)
        .thenComparingInt(Geburtstag::tag));

    try (FileOutputStream out = new FileOutputStream(file);
        Reporter reporter = new Reporter(out, params))
    {
      for (Month monat : Month.values())
      {
        List<Geburtstag> monatsgeburtstage = geburtstage.stream()
            .filter(g -> g.monat() == monat.getValue()).toList();
        PdfPTable tabelle = new PdfPTable(3);
        tabelle.setWidthPercentage(100);
        tabelle.setSpacingBefore(6);
        tabelle.setKeepTogether(true);
        tabelle.setSplitLate(false);
        PdfPCell ueberschrift = new PdfPCell(new Phrase(
            monat.getDisplayName(TextStyle.FULL, Locale.GERMAN),
            params.getFontHeader()));
        ueberschrift.setColspan(3);
        ueberschrift.setBorder(Rectangle.BOTTOM);
        ueberschrift.setBorderColor(BaseColor.LIGHT_GRAY);
        ueberschrift.setPaddingBottom(4);
        tabelle.addCell(ueberschrift);
        tabelle.setHeaderRows(1);
        int zeilen = Math.max(1, (monatsgeburtstage.size() + 2) / 3);
        for (int zeile = 0; zeile < zeilen; zeile++)
        {
          for (int spalte = 0; spalte < 3; spalte++)
          {
            int index = spalte * zeilen + zeile;
            String text = "";
            if (index < monatsgeburtstage.size())
            {
              Geburtstag geburtstag = monatsgeburtstage.get(index);
              text = String.format(Locale.GERMAN, "%d.%d.%d %s (%d)",
                  geburtstag.tag(), geburtstag.monat(), geburtstag.jahr(),
                  geburtstag.name(), jahr - geburtstag.jahr());
            }
            PdfPCell zelle = new PdfPCell(
                new Phrase(text, params.getFontNormal()));
            zelle.setBorder(Rectangle.NO_BORDER);
            zelle.setPaddingTop(2);
            zelle.setPaddingBottom(2);
            zelle.setPaddingRight(8);
            tabelle.addCell(zelle);
          }
        }
        Paragraph inhalt = new Paragraph();
        inhalt.add(tabelle);
        reporter.add(inhalt);
      }
      if (ohneGeburtsdatum > 0)
      {
        Paragraph hinweis = new Paragraph(
            "Mitglieder ohne Geburtsdatum (nicht aufgeführt): "
                + ohneGeburtsdatum,
            params.getFontNormal());
        hinweis.setSpacingBefore(12);
        reporter.add(hinweis);
      }
    }
  }
}
