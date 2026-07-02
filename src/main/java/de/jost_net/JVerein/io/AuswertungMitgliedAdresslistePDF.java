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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedAdresslistePDF
    extends AuswertungMitgliedAbstractPDF
{

  @SuppressWarnings("unchecked")
  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    /*
     * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
     * objects[2] ist Mitgliedstyp, objects[3] ist der Filter
     */
    ArrayList<Mitglied> list = (ArrayList<Mitglied>) objects[0];
    Map<Filter, String> filterparams = (Map<Filter, String>) objects[1];
    Mitgliedstyp mitgliedstyp = (Mitgliedstyp) objects[2];
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, params);

      reporter.addHeaderColumn("Name", Element.ALIGN_CENTER, 60,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Adresse", Element.ALIGN_CENTER, 100,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Telefon", Element.ALIGN_CENTER, 50,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Email", Element.ALIGN_CENTER, 80,
          params.getColorHeader(), params.getFontHeader());
      if ((Boolean) Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
        reporter.addHeaderColumn("Geburtsdatum", Element.ALIGN_CENTER, 30,
            params.getColorHeader(), params.getFontHeader());
      reporter.createHeader(100, Element.ALIGN_CENTER);

      for (int i = 0; i < list.size(); i++)
      {
        Mitglied m = list.get(i);
        reporter.addColumn(Adressaufbereitung.getNameVorname(m),
            Element.ALIGN_LEFT, params.getFontNormal());
        reporter.addColumn(Adressaufbereitung.getAnschrift(m),
            Element.ALIGN_LEFT, params.getFontNormal());
        String telefon = "";
        if (m.getTelefonprivat() != null && m.getTelefonprivat().length() > 0)
        {
          telefon = m.getTelefonprivat();
        }
        if (m.getTelefondienstlich() != null
            && m.getTelefondienstlich().length() > 0)
        {
          telefon += "\n" + "dienstl: " + m.getTelefondienstlich();
        }
        if (m.getHandy() != null && m.getHandy().length() > 0)
        {
          telefon += "\n" + "Handy: " + m.getHandy();
        }
        reporter.addColumn(telefon, Element.ALIGN_LEFT, params.getFontNormal());
        // Bei verwendung von mehreren Mailadresse im Fomar
        // NAME:Mail1@xx.de,mail2@xx.de; trennen wir die Mailadressen
        String mail = m.getEmail();
        if (mail.indexOf(":") > 0)
        {
          mail = mail.substring(mail.indexOf(":") + 1).replace(",", "\n")
              .replace(";", "").trim();
        }
        reporter.addColumn(mail, Element.ALIGN_LEFT, params.getFontNormal());
        if ((Boolean) Einstellungen
            .getEinstellung(Property.GEBURTSDATUMPFLICHT))
          reporter.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT,
              params.getFontNormal());
      }
      reporter.closeTable();

      reporter.add(new Paragraph(
          String.format("Anzahl %s: %d",
              mitgliedstyp == null ? "Nicht-Mitglieder"
                  : mitgliedstyp.getBezeichnungPlural(),
              list.size()),
          params.getFontNormal()));

      reporter.addParams(filterparams);
      reporter.close();
      fos.close();
      GUI.getStatusBar().setSuccessText(
          String.format("Auswertung fertig. %d Sätze.", list.size()));
    }
    catch (Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }

  @Override
  public String getName()
  {
    return "Adressliste PDF";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != MitgliedListeView.class
        && objectType != NichtMitgliedListeView.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return AuswertungMitgliedAdresslistePDF.this.getName();
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
}
