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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.gui.control.SollbuchungControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.dialogs.ExporterExportDialog;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.StringTool;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class Kontoauszug extends AbstractAusgabe
{
  private FileOutputStream fos;

  private Reporter rpt;

  private SollbuchungControl control;

  private ExportLayoutParam params;

  public Kontoauszug(SollbuchungControl control) throws Exception
  {
    this.control = control;
  }

  private void generiereMitglied(File file, Mitglied m)
      throws DocumentException, IOException, ApplicationException
  {
    MitgliedskontoNode node = new MitgliedskontoNode(m,
        (Date) control.getFilter().get(Filter.DATUM_VON),
        (Date) control.getFilter().get(Filter.DATUM_BIS));

    if (fos == null)
    {
      fos = new FileOutputStream(file);
    }

    if (rpt == null)
    {
      rpt = new Reporter(fos, params);
    }
    else
    {
      rpt.resetPageCount();
      rpt.newPage();
    }

    String title = VorlageUtil.getName(VorlageTyp.KONTOAUSZUG_TITEL, m);
    String subtitle = VorlageUtil.getName(VorlageTyp.KONTOAUSZUG_SUBTITEL, m);

    Paragraph pTitle = new Paragraph(title, Reporter.getFreeSansBold(13));
    pTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(pTitle);
    Paragraph psubTitle = new Paragraph(subtitle, Reporter.getFreeSansBold(10));
    psubTitle.setAlignment(Element.ALIGN_CENTER);
    rpt.add(psubTitle);

    rpt.addHeaderColumn(" ", Element.ALIGN_CENTER, 15, params.getColorHeader(),
        params.getFontHeader());
    rpt.addHeaderColumn("Datum", Element.ALIGN_CENTER, 20,
        params.getColorHeader(), params.getFontHeader());
    rpt.addHeaderColumn("Zweck", Element.ALIGN_LEFT, 50,
        params.getColorHeader(), params.getFontHeader());
    rpt.addHeaderColumn("Zahlungsweg", Element.ALIGN_LEFT, 20,
        params.getColorHeader(), params.getFontHeader());
    rpt.addHeaderColumn("Soll", Element.ALIGN_RIGHT, 20,
        params.getColorHeader(), params.getFontHeader());
    rpt.addHeaderColumn("Ist", Element.ALIGN_RIGHT, 20, params.getColorHeader(),
        params.getFontHeader());
    rpt.addHeaderColumn("Differenz", Element.ALIGN_RIGHT, 20,
        params.getColorHeader(), params.getFontHeader());
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
        rpt.addColumn("Gesamt", Element.ALIGN_LEFT, params.getFontNormal());
        break;
      case MitgliedskontoNode.SOLL:
        rpt.addColumn("Soll", Element.ALIGN_CENTER, params.getColorTable(),
            params.getFontNormal());
        break;
      case MitgliedskontoNode.IST:
        rpt.addColumn("Ist", Element.ALIGN_RIGHT, params.getFontNormal());
        break;
    }

    if (node.getType() != MitgliedskontoNode.SOLL)
    {
      rpt.addColumn((Date) node.getAttribute("datum"), Element.ALIGN_CENTER,
          params.getFontNormal());
      rpt.addColumn((String) node.getAttribute("zweck1"), Element.ALIGN_LEFT,
          params.getFontNormal());
      rpt.addColumn((String) node.getAttribute("zahlungsweg"),
          Element.ALIGN_LEFT, params.getFontNormal());
      rpt.addColumn((Double) node.getAttribute("soll"), params.getFontNormal(),
          params.getNegativRot());
      rpt.addColumn((Double) node.getAttribute("ist"), params.getFontNormal(),
          params.getNegativRot());
      rpt.addColumn((Double) node.getAttribute("differenz"),
          params.getFontNormal(), params.getNegativRot());
    }
    else
    {
      rpt.addColumn((Date) node.getAttribute("datum"), Element.ALIGN_CENTER,
          params.getColorTable(), params.getFontNormal());
      rpt.addColumn((String) node.getAttribute("zweck1"), Element.ALIGN_LEFT,
          params.getColorTable(), params.getFontNormal());
      rpt.addColumn((String) node.getAttribute("zahlungsweg"),
          Element.ALIGN_LEFT, params.getColorTable(), params.getFontNormal());
      rpt.addColumn((Double) node.getAttribute("soll"), params.getColorTable(),
          params.getFontNormal(), params.getNegativRot());
      rpt.addColumn((Double) null, params.getColorTable());
      rpt.addColumn((Double) node.getAttribute("differenz"),
          params.getColorTable(), params.getFontNormal(),
          params.getNegativRot());
    }

  }

  @Override
  protected void createPDF(Formular formular, FormularAufbereitung aufbereitung,
      File file, DBObject object)
      throws IOException, DocumentException, ApplicationException
  {
    if (params == null)
    {
      ExporterExportDialog d = new ExporterExportDialog(false, "Kontoauszug",
          ExportArt.PDF, "", "", null);
      try
      {
        if (!d.open())
        {
          throw new OperationCanceledException();
        }
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        String text = "Fehler beim Erstellen des Reports.";
        Logger.error(text, e);
        throw new ApplicationException(text);
      }
      params = d.getParams();
    }
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
          (Mitglied) object);
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
