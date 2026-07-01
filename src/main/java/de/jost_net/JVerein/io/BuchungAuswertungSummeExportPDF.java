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
import java.util.List;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.view.AnlagenbuchungListeView;
import de.jost_net.JVerein.gui.view.BuchungListeView;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungAuswertungSummeExportPDF extends BuchungAuswertungExportPDF
{
  boolean geldkonto;

  private ExportLayoutParam params;

  @Override
  public String getName()
  {
    return "PDF Summenbuchungen";
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
        return BuchungAuswertungSummeExportPDF.this.getName();
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
      return VorlageUtil.getName(VorlageTyp.SUMMENBUCHUNGEN_DATEINAME, object)
          + ".pdf";
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_SUMMENBUCHUNGEN_DATEINAME,
          object) + ".pdf";
    }
  }

  @Override
  public String getTitle(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.SUMMENBUCHUNGEN_TITEL, object);
  }

  @Override
  public String getSubtitle(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.SUMMENBUCHUNGEN_SUBTITEL, object);
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    this.params = params;
    BuchungsControl control = (BuchungsControl) objects[0];
    BuchungQuery query = control.getQuery();
    ArrayList<Buchungsart> buchungsarten = getBuchungsarten(query);
    kontonummer_in_buchungsliste = getKontonummer();

    FileOutputStream fos = new FileOutputStream(file);
    Reporter reporter = new Reporter(fos, params);

    createTableHeader(reporter);
    boolean nichtLeer = false;
    int anzahlBuchungsarten = 0;
    DBIterator<Buchungsklasse> buchungsklassen = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    buchungsklassen.setOrder("ORDER BY nummer");
    while (buchungsklassen.hasNext())
    {
      Buchungsklasse bukla = buchungsklassen.next();
      for (Buchungsart bua : buchungsarten)
      {
        if (!(Boolean) Einstellungen
            .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
        {
          if (bua.getBuchungsklasseId() == null
              || !bua.getBuchungsklasse().getNummer().equals(bukla.getNummer()))
          {
            continue;
          }
        }
        List<Buchung> liste = getBuchungenEinerBuchungsart(query.get(), bua,
            bukla);
        if (liste.size() > 0)
        {
          anzahlBuchungsarten += 1;
        }
        nichtLeer = createTableContent(reporter, bua, bukla, liste)
            || nichtLeer;
      }
    }
    // Buchungsarten ohne Buchungsklassen
    for (Buchungsart bua : buchungsarten)
    {
      if (!(Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        if (bua.getBuchungsklasseId() != null)
        {
          continue;
        }
      }
      List<Buchung> liste = getBuchungenEinerBuchungsart(query.get(), bua);
      if (liste.size() > 0)
      {
        anzahlBuchungsarten += 1;
      }
      nichtLeer = createTableContent(reporter, bua, null, liste) || nichtLeer;
    }
    // Buchungen ohne Buchungsarten, wenn explizite Buchungsart angegeben ist,
    // dann nur wenn auch ohne Buchungsart ausgewählt ist (ID == null)
    if (query.getBuchungsart() == null || (query.getBuchungsart() != null
        && query.getBuchungsart().getID() == null))
    {
      List<Buchung> liste = getBuchungenOhneBuchungsart(query.get());
      if (liste.size() > 0)
      {
        anzahlBuchungsarten += 1;
      }
      Buchungsart bua = (Buchungsart) Einstellungen.getDBService()
          .createObject(Buchungsart.class, null);
      bua.setBezeichnung("Ohne Buchungsart");
      nichtLeer = createTableContent(reporter, bua, null, liste) || nichtLeer;
    }

    if (anzahlBuchungsarten > 1)
    {
      reporter.addColumn("Summe Einnahmen", Element.ALIGN_RIGHT,
          params.getColorTable(), true, params.getFontFett(), 2);
      reporter.addColumn(summeeinnahmen, params.getColorTable(),
          params.getFontFett(), params.getNegativRot());
      reporter.addColumn("Summe Ausgaben", Element.ALIGN_RIGHT,
          params.getColorTable(), true, params.getFontFett(), 2);
      reporter.addColumn(summeausgaben, params.getColorTable(),
          params.getFontFett(), params.getNegativRot());
      reporter.addColumn("Summe Umbuchungen", Element.ALIGN_RIGHT,
          params.getColorTable(), true, params.getFontFett(), 2);
      reporter.addColumn(summeumbuchungen, params.getColorTable(),
          params.getFontFett(), params.getNegativRot());
      reporter.addColumn("Saldo", Element.ALIGN_RIGHT, params.getColorTable(),
          true, params.getFontFett(), 2);
      reporter.addColumn(summeeinnahmen + summeausgaben + summeumbuchungen,
          params.getColorTable(), params.getFontFett(), params.getNegativRot());
      reporter.closeTable();
    }
    else if (nichtLeer)
    {
      reporter.closeTable();
    }
    if (!nichtLeer)
    {
      reporter.addColumn("", Element.ALIGN_LEFT, params.getColorTable());
      reporter.addColumn("Keine Buchungen", Element.ALIGN_LEFT,
          params.getColorTable(), params.getFontNormal());
      reporter.addColumn("", Element.ALIGN_LEFT, params.getColorTable());
      reporter.closeTable();
    }
    reporter.addParams(control.getParams());

    reporter.close();
    fos.close();
  }

  private void createTableHeader(Reporter reporter) throws DocumentException
  {
    reporter.addHeaderColumn("Buchungsklasse", Element.ALIGN_CENTER, 50,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Buchungsart", Element.ALIGN_CENTER, 150,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 60,
        params.getColorHeader(), params.getFontHeader());
    reporter.createHeader();
  }

  private boolean createTableContent(Reporter reporter, Buchungsart bua,
      Buchungsklasse bukla, List<Buchung> buchungen)
      throws RemoteException, DocumentException
  {
    if ((Boolean) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG)
        && buchungen.size() == 0)
    {
      return false;
    }
    String buchungsklasseBezeichnung = "Ohne Buchungsklasse";
    if (bukla != null)
    {
      buchungsklasseBezeichnung = new BuchungsklasseFormatter().format(bukla);
    }
    String buchungsartBezeichnung = "Ohne Buchungsart";
    if (!bua.getBezeichnung().equalsIgnoreCase("Ohne Buchungsart"))
    {
      buchungsartBezeichnung = new BuchungsartFormatter().format(bua);
    }
    double buchungsartSumme = 0;

    for (Buchung b : buchungen)
    {

      buchungsartSumme += b.getBetrag();
      if (bua.getArt() == ArtBuchungsart.EINNAHME)
      {
        summeeinnahmen += b.getBetrag();
      }
      if (bua.getArt() == ArtBuchungsart.AUSGABE)
      {
        summeausgaben += b.getBetrag();
      }
      if (bua.getArt() == ArtBuchungsart.UMBUCHUNG)
      {
        summeumbuchungen += b.getBetrag();
      }
    }

    reporter.addColumn(buchungsklasseBezeichnung, Element.ALIGN_LEFT,
        params.getFontNormal());
    reporter.addColumn(buchungsartBezeichnung, Element.ALIGN_LEFT,
        params.getFontNormal());
    if (buchungen.size() == 0)
    {
      reporter.addColumn("Keine Buchung", Element.ALIGN_LEFT,
          params.getFontNormal());
    }
    else
    {
      reporter.addColumn(buchungsartSumme);
    }
    return true;
  }
}
