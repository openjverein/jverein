/**********************************************************************
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
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.server.Tools.EigenschaftenTool;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedPDF extends AuswertungMitgliedAbstractPDF
{

  @SuppressWarnings("unchecked")
  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    try
    {
      /*
       * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
       * objects[2] ist Mitgliedstyp, objects[3] ist der Filter
       */
      ArrayList<Mitglied> list = (ArrayList<Mitglied>) objects[0];
      Map<Filter, String> filterparams = (Map<Filter, String>) objects[1];
      Mitgliedstyp mitgliedstyp = (Mitgliedstyp) objects[2];

      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, params);

      reporter.addHeaderColumn("Name", Element.ALIGN_CENTER, 100,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Anschrift\nKommunikation", Element.ALIGN_CENTER,
          130, params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Geburts- datum", Element.ALIGN_CENTER, 30,
          params.getColorHeader(), params.getFontHeader());
      if (mitgliedauswahl == MitgliedAuswahl.MITGLIEDER)
      {
        reporter.addHeaderColumn(
            "Eintritt / \nAustritt / \nKündigung"
                + ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM)
                    ? ("/\n" + "Sterbedatum")
                    : ""),
            Element.ALIGN_CENTER, 30, params.getColorHeader(),
            params.getFontHeader());
      }
      reporter.addHeaderColumn(
          "Beitragsgruppe /\nEigenschaften"
              + ((Boolean) Einstellungen.getEinstellung(
                  Property.EXTERNEMITGLIEDSNUMMER) ? "\nMitgliedsnummer" : ""),
          Element.ALIGN_CENTER, 60, params.getColorHeader(),
          params.getFontHeader());
      reporter.createHeader(100, Element.ALIGN_CENTER);

      for (int i = 0; i < list.size(); i++)
      {
        Mitglied m = list.get(i);
        reporter.addColumn(Adressaufbereitung.getNameVorname(m),
            Element.ALIGN_LEFT, params.getFontNormal());
        String anschriftkommunikation = Adressaufbereitung.getAnschrift(m);
        if (m.getTelefonprivat() != null && m.getTelefonprivat().length() > 0)
        {
          anschriftkommunikation += "\n" + "Tel. priv: " + m.getTelefonprivat();
        }
        if (m.getTelefondienstlich() != null
            && m.getTelefondienstlich().length() > 0)
        {
          anschriftkommunikation += "\n" + "Tel. dienstl: "
              + m.getTelefondienstlich();
        }
        if (m.getHandy() != null && m.getHandy().length() > 0)
        {
          anschriftkommunikation += "\n" + "Handy: " + m.getHandy();
        }
        if (m.getEmail() != null && m.getEmail().length() > 0)
        {
          anschriftkommunikation += "\n" + "EMail: " + m.getEmail();
        }
        reporter.addColumn(anschriftkommunikation, Element.ALIGN_LEFT,
            params.getFontNormal());
        reporter.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT,
            params.getFontNormal());

        Date d = m.getEintritt();
        if (d.equals(Einstellungen.NODATE))
        {
          d = null;
        }
        String zelle = "";
        if (d != null)
        {
          zelle = new JVDateFormatTTMMJJJJ().format(d);
        }

        if (m.getAustritt() != null)
        {
          zelle += "\n" + new JVDateFormatTTMMJJJJ().format(m.getAustritt());
        }
        if (m.getKuendigung() != null)
        {
          zelle += "\n" + new JVDateFormatTTMMJJJJ().format(m.getKuendigung());
        }
        if (m.getSterbetag() != null)
        {
          zelle += "\n" + new JVDateFormatTTMMJJJJ().format(m.getSterbetag());
        }
        if (mitgliedauswahl == MitgliedAuswahl.MITGLIEDER)
        {
          reporter.addColumn(zelle, Element.ALIGN_LEFT, params.getFontNormal());
        }
        StringBuilder beitragsgruppebemerkung = new StringBuilder();
        if (m.getBeitragsgruppe() != null)
        {
          beitragsgruppebemerkung
              .append(m.getBeitragsgruppe().getBezeichnung());
        }
        StringBuilder eigenschaften = new StringBuilder();
        ArrayList<String> eig = new EigenschaftenTool()
            .getEigenschaften(m.getID());
        for (int i2 = 0; i2 < eig.size(); i2 = i2 + 2)
        {
          if (i2 == 0)
          {
            beitragsgruppebemerkung.append("\n");
          }
          eigenschaften.append(eig.get(i2));
          eigenschaften.append(": ");
          eigenschaften.append(eig.get(i2 + 1));
          eigenschaften.append("\n");
        }

        zelle = "";
        if ((Boolean) Einstellungen
            .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
        {
          zelle += (m.getExterneMitgliedsnummer() != null
              ? m.getExterneMitgliedsnummer()
              : "");
        }

        reporter.addColumn(beitragsgruppebemerkung.toString()
            + eigenschaften.toString() + zelle, Element.ALIGN_LEFT,
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
      reporter.closeTable();
      reporter.close();
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
    return "Mitgliederliste PDF";
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
        return AuswertungMitgliedPDF.this.getName();
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
