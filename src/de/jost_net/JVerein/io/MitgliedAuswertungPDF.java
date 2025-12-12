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
import java.util.Date;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.Tools.EigenschaftenTool;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedAuswertungPDF extends MitgliedAbstractPDF
{

  public MitgliedAuswertungPDF(MitgliedControl control)
  {
    super(control);
  }

  @Override
  public void go(ArrayList<Mitglied> list, final File file)
      throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);

      Reporter report = new Reporter(fos, title, subtitle, list.size(), 50, 10,
          20, 25);

      report.addHeaderColumn("Name", Element.ALIGN_CENTER, 100,
          BaseColor.LIGHT_GRAY);
      report.addHeaderColumn("Anschrift\nKommunikation", Element.ALIGN_CENTER,
          130, BaseColor.LIGHT_GRAY);
      report.addHeaderColumn("Geburts- datum", Element.ALIGN_CENTER, 30,
          BaseColor.LIGHT_GRAY);
      if (mitgliedstyp != null
          && mitgliedstyp.getID().equals(Mitgliedstyp.MITGLIED))
      {
        report
            .addHeaderColumn(
                "Eintritt / \nAustritt / \nKündigung"
                    + ((Boolean) Einstellungen.getEinstellung(
                        Property.STERBEDATUM) ? ("/\n" + "Sterbedatum") : ""),
                Element.ALIGN_CENTER, 30, BaseColor.LIGHT_GRAY);
      }
      report.addHeaderColumn(

          "Beitragsgruppe /\nEigenschaften"
              + ((Boolean) Einstellungen.getEinstellung(
                  Property.EXTERNEMITGLIEDSNUMMER) ? "\nMitgliedsnummer" : ""),
          Element.ALIGN_CENTER, 60, BaseColor.LIGHT_GRAY);
      report.createHeader(100, Element.ALIGN_CENTER);

      for (int i = 0; i < list.size(); i++)
      {
        Mitglied m = list.get(i);
        report.addColumn(Adressaufbereitung.getNameVorname(m),
            Element.ALIGN_LEFT);
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
        report.addColumn(anschriftkommunikation, Element.ALIGN_LEFT);
        report.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT);

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
        if (mitgliedstyp != null
            && mitgliedstyp.getID().equals(Mitgliedstyp.MITGLIED))
        {
          report.addColumn(zelle, Element.ALIGN_LEFT);
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

        report.addColumn(beitragsgruppebemerkung.toString() + " "
            + eigenschaften.toString() + " " + zelle, Element.ALIGN_LEFT);
      }
      report.closeTable();

      report.add(new Paragraph(
          String.format("Anzahl %d: %s", list.size(),
              mitgliedstyp == null ? "Nicht-Mitglieder"
                  : mitgliedstyp.getBezeichnungPlural()),
          Reporter.getFreeSans(8)));

      report.addParams(params);
      report.closeTable();
      report.close();
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
  public String getDateiname()
  {
    return "auswertung";
  }

  @Override
  public String getDateiendung()
  {
    return "pdf";
  }

  @Override
  public String toString()
  {
    return "Mitgliederliste PDF";
  }

  @Override
  public boolean openFile()
  {
    return true;
  }
}
