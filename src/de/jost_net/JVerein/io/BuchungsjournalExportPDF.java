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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.dialogs.BuchungsjournalSortDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.view.AnlagenbuchungListeView;
import de.jost_net.JVerein.gui.view.BuchungListeView;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungsjournalExportPDF implements Exporter
{
  private String title;

  private String subtitle;

  boolean geldkonto;

  @Override
  public String getName()
  {
    return "PDF Buchungsjournal";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType == BuchungListeView.class)
    {
      geldkonto = true;
    }
    else if (objectType == AnlagenbuchungListeView.class)
    {
      geldkonto = false;
    }
    else
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return BuchungsjournalExportPDF.this.getName();
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
    if (geldkonto)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGSJOURNAL_DATEINAME, object)
          + ".pdf";
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGSJOURNAL_DATEINAME,
          object) + ".pdf";
    }
  }

  @Override
  public void calculateTitle(Object object)
  {
    title = VorlageUtil.getName(VorlageTyp.BUCHUNGSJOURNAL_TITEL, object);
  }

  @Override
  public void calculateSubitle(Object object)
  {
    subtitle = VorlageUtil.getName(VorlageTyp.BUCHUNGSJOURNAL_SUBTITEL, object);
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws RemoteException, ApplicationException,
      FileNotFoundException, DocumentException, IOException
  {
    BuchungsControl control = (BuchungsControl) objects[0];
    BuchungQuery query = control.getQuery();
    BuchungsjournalSortDialog djs = new BuchungsjournalSortDialog(
        BuchungsjournalSortDialog.POSITION_CENTER);
    String sort = null;
    try
    {
      sort = djs.open();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
    if (djs.getClosed())
    {
      throw new OperationCanceledException("Ausgabe abgebrochen");
    }
    if (sort != null)
    {
      query.setOrdername(sort);
    }

    FileOutputStream fos = new FileOutputStream(file);
    Reporter reporter = new Reporter(fos, title, subtitle);

    double einnahmen = 0;
    double ausgaben = 0;
    double umbuchungen = 0;
    double nichtzugeordnet = 0;

    createTableHeader(reporter);

    for (Buchung b : query.get())
    {
      reporter.addColumn(b.getID(), Element.ALIGN_RIGHT);
      reporter.addColumn(new JVDateFormatTTMMJJJJ().format(b.getDatum()),
          Element.ALIGN_LEFT);
      reporter.addColumn(b.getKonto().getNummer(), Element.ALIGN_RIGHT);
      if (b.getAuszugsnummer() != null)
      {
        reporter.addColumn(
            b.getAuszugsnummer() + "/"
                + (b.getBlattnummer() != null ? b.getBlattnummer() : "-"),
            Element.ALIGN_LEFT);
      }
      else
      {
        reporter.addColumn("", Element.ALIGN_LEFT);
      }
      reporter.addColumn(b.getName(), Element.ALIGN_LEFT);
      reporter.addColumn(b.getZweck(), Element.ALIGN_LEFT);
      String buklaString = "-: ";
      String buaString = "--";
      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        if (b.getBuchungsklasseId() != null)
        {
          buklaString = b.getBuchungsklasse().getNummer() + ": ";
        }
      }
      else
      {
        if (b.getBuchungsartId() != null
            && b.getBuchungsart().getBuchungsklasseId() != null)
        {
          buklaString = b.getBuchungsart().getBuchungsklasse().getNummer()
              + ": ";
        }
      }
      if (b.getBuchungsartId() != null)
      {
        buaString = new BuchungsartFormatter().format(b.getBuchungsart());
      }
      reporter.addColumn(buklaString + buaString, Element.ALIGN_LEFT);
      reporter.addColumn(b.getBetrag());
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        reporter
            .addColumn(b.getSteuer() == null ? null : b.getSteuer().getSatz());
      }
      if (b.getBuchungsart() != null)
      {
        int buchungsartart = b.getBuchungsart().getArt();
        switch (buchungsartart)
        {
          case ArtBuchungsart.EINNAHME:
          {
            einnahmen += b.getBetrag();
            break;
          }
          case ArtBuchungsart.AUSGABE:
          {
            ausgaben += b.getBetrag();
            break;
          }
          case ArtBuchungsart.UMBUCHUNG:
          {
            umbuchungen += b.getBetrag();
            break;
          }
        }
      }
      else
      {
        nichtzugeordnet += b.getBetrag();
      }
    }

    for (int i = 0; i < 5; i++)
    {
      reporter.addColumn("", Element.ALIGN_LEFT);
    }
    reporter.addColumn("Summe Einnahmen", Element.ALIGN_LEFT);
    reporter.addColumn("", Element.ALIGN_LEFT);
    reporter.addColumn(einnahmen);

    for (int i = 0; i < 5; i++)
    {
      reporter.addColumn("", Element.ALIGN_LEFT);
    }
    reporter.addColumn("Summe Ausgaben", Element.ALIGN_LEFT);
    reporter.addColumn("", Element.ALIGN_LEFT);
    reporter.addColumn(ausgaben);

    for (int i = 0; i < 5; i++)
    {
      reporter.addColumn("", Element.ALIGN_LEFT);
    }
    reporter.addColumn("Summe Umbuchungen", Element.ALIGN_LEFT);
    reporter.addColumn("", Element.ALIGN_LEFT);
    reporter.addColumn(umbuchungen);

    if (nichtzugeordnet != 0)
    {
      for (int i = 0; i < 5; i++)
      {
        reporter.addColumn("", Element.ALIGN_LEFT);
      }
      reporter.addColumn("Summe nicht zugeordnet", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn(nichtzugeordnet);
    }

    reporter.closeTable();
    reporter.addParams(control.getParams());

    reporter.close();
    fos.close();
  }

  private void createTableHeader(Reporter reporter)
      throws DocumentException, RemoteException
  {
    reporter.addHeaderColumn("Nr", Element.ALIGN_RIGHT, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Datum", Element.ALIGN_CENTER, 45,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Konto", Element.ALIGN_CENTER, 40,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Auszug", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Name", Element.ALIGN_CENTER, 90,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Zahlungsgrund", Element.ALIGN_CENTER, 100,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Buchungsklasse Nummer:\nBuchungsart",
        Element.ALIGN_CENTER, 70, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 50,
        BaseColor.LIGHT_GRAY);
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addHeaderColumn("Steuersatz", Element.ALIGN_CENTER, 30,
          BaseColor.LIGHT_GRAY);
    }
    reporter.createHeader();
  }
}
