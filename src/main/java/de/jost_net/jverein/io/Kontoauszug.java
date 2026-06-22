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
package de.jost_net.jverein.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.jverein.gui.control.MitgliedskontoNode;
import de.jost_net.jverein.gui.control.SollbuchungControl;
import de.jost_net.jverein.keys.VorlageTyp;
import de.jost_net.jverein.rmi.Formular;
import de.jost_net.jverein.rmi.Mitglied;
import de.jost_net.jverein.util.StringTool;
import de.jost_net.jverein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBObject;

public class Kontoauszug extends AbstractAusgabe
{
  private FileOutputStream fos;

  private Reporter rpt;

  private SollbuchungControl control;

  public Kontoauszug(SollbuchungControl control) throws Exception
  {
    this.control = control;
  }

  private void generiereMitglied(File file, Mitglied m)
      throws DocumentException, IOException
  {
    MitgliedskontoNode node = new MitgliedskontoNode(m,
        (Date) control.getDatumvon().getValue(),
        (Date) control.getDatumbis().getValue());

    if (fos == null)
    {
      fos = new FileOutputStream(file);
    }

    if (rpt == null)
    {
      rpt = new Reporter(fos, "", "", 60, 30, 20, 20, false);
    }
    else
    {
      rpt.resetPageCount();
      rpt.newPage();
    }

    String title = VorlageUtil.getName(VorlageTyp.KONTOAUSZUG_TITEL, null, m);
    String subtitle = VorlageUtil.getName(VorlageTyp.KONTOAUSZUG_SUBTITEL, null,
        m);
    Paragraph pTitle = new Paragraph(title, Reporter.getFreeSansBold(13));
    pTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(pTitle);
    Paragraph psubTitle = new Paragraph(subtitle, Reporter.getFreeSansBold(10));
    psubTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(psubTitle);

    rpt.addHeaderColumn(" ", Element.ALIGN_CENTER, 15, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Datum", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Zweck", Element.ALIGN_LEFT, 50, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Zahlungsweg", Element.ALIGN_LEFT, 20,
        BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Soll", Element.ALIGN_RIGHT, 20, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Ist", Element.ALIGN_RIGHT, 20, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Differenz", Element.ALIGN_RIGHT, 20,
        BaseColor.LIGHT_GRAY);
    rpt.createHeader();

    generiereZeile(node);
    @SuppressWarnings("rawtypes")
    GenericIterator gi1 = node.getChildren();
    while (gi1.hasNext())
    {
      MitgliedskontoNode n1 = (MitgliedskontoNode) gi1.next();
      generiereZeile(n1);
      @SuppressWarnings("rawtypes")
      GenericIterator gi2 = n1.getChildren();
      while (gi2.hasNext())
      {
        MitgliedskontoNode n2 = (MitgliedskontoNode) gi2.next();
        generiereZeile(n2);
      }
    }
    rpt.closeTable();
  }

  private void generiereZeile(MitgliedskontoNode node)
  {
    switch (node.getType())
    {
      case MitgliedskontoNode.MITGLIED:
        rpt.addColumn("Gesamt", Element.ALIGN_LEFT);
        break;
      case MitgliedskontoNode.SOLL:
        rpt.addColumn("Soll", Element.ALIGN_CENTER);
        break;
      case MitgliedskontoNode.IST:
        rpt.addColumn("Ist", Element.ALIGN_RIGHT);
        break;
    }
    rpt.addColumn((Date) node.getAttribute("datum"), Element.ALIGN_CENTER);
    rpt.addColumn((String) node.getAttribute("zweck1"), Element.ALIGN_LEFT);
    rpt.addColumn((String) node.getAttribute("zahlungsweg"),
        Element.ALIGN_LEFT);
    rpt.addColumn((Double) node.getAttribute("soll"));
    if (node.getType() != MitgliedskontoNode.SOLL)
    {
      rpt.addColumn((Double) node.getAttribute("ist"));
    }
    else
    {
      rpt.addColumn((Double) null);
    }
    rpt.addColumn((Double) node.getAttribute("differenz"));
  }

  @Override
  protected void createPDF(Formular formular, FormularAufbereitung aufbereitung,
      File file, DBObject object) throws IOException, DocumentException
  {
    generiereMitglied(file, (Mitglied) object);
  }

  @Override
  protected void closeDocument(FormularAufbereitung formularaufbereitung,
      DBObject object) throws IOException, DocumentException
  {
    rpt.close();
    fos.close();
    rpt = null;
    fos = null;
  }

  @Override
  protected String getZipDateiname(DBObject object) throws RemoteException
  {
    String filename = object.getID() + "#kontoauszug# #";
    filename += StringTool.toNotNullString(((Mitglied) object).getEmail());
    filename += "#Kontoauszug";
    return filename;
  }

  @Override
  protected Map<String, Object> getMap(DBObject object)
  {
    // Brauchen wir nicht, da keinFormular aufbereitet wird
    return null;
  }

  @Override
  protected String getDateiname(DBObject object)
  {
    if (object != null)
    {
      return VorlageUtil.getName(VorlageTyp.KONTOAUSZUG_MITGLIED_DATEINAME,
          null, (Mitglied) object);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.KONTOAUSZUG_DATEINAME);
    }
  }

  @Override
  protected Formular getFormular(DBObject object)
  {
    // Es gibt kein Formular, nur den Report
    return null;
  }
}
