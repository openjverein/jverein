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

import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.util.VorlageUtil;

public class AuswertungJahrgangsstatistikPDF
    extends AuswertungJahrgangsstatistikAbstract
{

  private FileOutputStream fos;

  private Reporter reporter;

  @Override
  public String getName()
  {
    return "Jahrgangsstatistik PDF";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != MitgliedListeView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return AuswertungJahrgangsstatistikPDF.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.pdf" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public String getDateiname(Object object)
  {
    return VorlageUtil.getName(
        VorlageTyp.AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME, object) + ".pdf";
  }

  @Override
  protected void open() throws DocumentException, IOException
  {
    fos = new FileOutputStream(file);
    reporter = new Reporter(fos, params);

    reporter.addHeaderColumn("Jahrgang", Element.ALIGN_CENTER, 100,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Insgesamt", Element.ALIGN_CENTER, 100,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Männlich", Element.ALIGN_CENTER, 100,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Weiblich", Element.ALIGN_CENTER, 100,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Ohne Angabe", Element.ALIGN_CENTER, 100,
        params.getColorHeader(), params.getFontHeader());
    reporter.createHeader(100, Element.ALIGN_LEFT);
    int summegesamt = 0;
    int summemaennlich = 0;
    int summeweiblich = 0;
    int summeohne = 0;
    for (String key : statistik.keySet())
    {
      reporter.addColumn(key, Element.ALIGN_CENTER, params.getFontNormal());
      StatistikJahrgang dsbj = statistik.get(key);
      reporter.addColumn(Einstellungen.INTFORMAT.format(dsbj.getAnzahlgesamt()),
          Element.ALIGN_RIGHT, params.getFontNormal());
      reporter.addColumn(
          Einstellungen.INTFORMAT.format(dsbj.getAnzahlmaennlich()),
          Element.ALIGN_RIGHT, params.getFontNormal());
      reporter.addColumn(
          Einstellungen.INTFORMAT.format(dsbj.getAnzahlweiblich()),
          Element.ALIGN_RIGHT, params.getFontNormal());
      reporter.addColumn(Einstellungen.INTFORMAT.format(dsbj.getAnzahlOhne()),
          Element.ALIGN_RIGHT, params.getFontNormal());
      summegesamt += dsbj.getAnzahlgesamt();
      summemaennlich += dsbj.getAnzahlmaennlich();
      summeweiblich += dsbj.getAnzahlweiblich();
      summeohne += dsbj.getAnzahlOhne();
    }
    reporter.addColumn("Summe", Element.ALIGN_CENTER, params.getColorTable(),
        params.getFontNormal());
    reporter.addColumn(Einstellungen.INTFORMAT.format(summegesamt),
        Element.ALIGN_RIGHT, params.getColorTable(), params.getFontNormal());
    reporter.addColumn(Einstellungen.INTFORMAT.format(summemaennlich),
        Element.ALIGN_RIGHT, params.getColorTable(), params.getFontNormal());
    reporter.addColumn(Einstellungen.INTFORMAT.format(summeweiblich),
        Element.ALIGN_RIGHT, params.getColorTable(), params.getFontNormal());
    reporter.addColumn(Einstellungen.INTFORMAT.format(summeohne),
        Element.ALIGN_RIGHT, params.getColorTable(), params.getFontNormal());
    reporter.closeTable();

  }

  @Override
  protected void close() throws IOException, DocumentException
  {
    reporter.close();
    fos.close();
  }

}
