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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbrechnungslaufBuchungPDF
{

  public AbrechnungslaufBuchungPDF(DBIterator<Buchung> it, final File file,
      String title, String subtitle) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);

      Reporter reporter = new Reporter(fos, title, subtitle, it.size());
      reporter.addHeaderColumn("Nr", Element.ALIGN_RIGHT, 40,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Datum", Element.ALIGN_CENTER, 60,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Mitglied", Element.ALIGN_CENTER, 150,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zahler", Element.ALIGN_CENTER, 150,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Zweck", Element.ALIGN_CENTER, 200,
          BaseColor.LIGHT_GRAY);
      reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 60,
          BaseColor.LIGHT_GRAY);
      reporter.createHeader();

      while (it.hasNext())
      {
        Buchung buch = it.next();
        reporter.addColumn(buch.getID(), Element.ALIGN_RIGHT);
        reporter.addColumn(new JVDateFormatTTMMJJJJ().format(buch.getDatum()),
            Element.ALIGN_LEFT);
        if (buch.getSollbuchung() != null
            && buch.getSollbuchung().getMitglied() != null)
        {
          reporter.addColumn(
              Adressaufbereitung
                  .getNameVorname(buch.getSollbuchung().getMitglied()),
              Element.ALIGN_LEFT, false);
        }
        else
        {
          reporter.addColumn("", Element.ALIGN_LEFT);
        }
        if (buch.getSollbuchung() != null
            && buch.getSollbuchung().getZahler() != null)
        {
          reporter.addColumn(
              Adressaufbereitung
                  .getNameVorname(buch.getSollbuchung().getZahler()),
              Element.ALIGN_LEFT, false);
        }
        else
        {
          reporter.addColumn("", Element.ALIGN_LEFT);
        }
        reporter.addColumn(buch.getZweck(), Element.ALIGN_LEFT, false);
        reporter.addColumn(buch.getBetrag());
      }
      reporter.closeTable();
      reporter.close();

      GUI.getStatusBar().setSuccessText("Auswertung fertig.");

      fos.close();
      FileViewer.show(file);
    }
    catch (DocumentException e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
    catch (FileNotFoundException e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
    catch (IOException e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }
}
