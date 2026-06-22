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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.jverein.Einstellungen;
import de.jost_net.jverein.Einstellungen.Property;
import de.jost_net.jverein.gui.control.BuchungsControl;
import de.jost_net.jverein.gui.formatter.BuchungsartFormatter;
import de.jost_net.jverein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.jverein.gui.view.AnlagenbuchungListeView;
import de.jost_net.jverein.gui.view.BuchungListeView;
import de.jost_net.jverein.keys.ArtBuchungsart;
import de.jost_net.jverein.keys.VorlageTyp;
import de.jost_net.jverein.queries.BuchungQuery;
import de.jost_net.jverein.rmi.Buchung;
import de.jost_net.jverein.rmi.Buchungsart;
import de.jost_net.jverein.rmi.Buchungsklasse;
import de.jost_net.jverein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.jverein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungAuswertungEinzelExportPDF extends BuchungAuswertungExportPDF
{
  private String title;

  private String subtitle;

  boolean geldkonto;

  @Override
  public String getName()
  {
    return "PDF Einzelbuchungen";
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
        return BuchungAuswertungEinzelExportPDF.this.getName();
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
      return VorlageUtil.getName(VorlageTyp.EINZELBUCHUNGEN_DATEINAME, object)
          + ".pdf";
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_EINZELBUCHUNGEN_DATEINAME,
          object) + ".pdf";
    }
  }

  @Override
  public void calculateTitle(Object object)
  {
    title = VorlageUtil.getName(VorlageTyp.EINZELBUCHUNGEN_TITEL, object);
  }

  @Override
  public void calculateSubitle(Object object)
  {
    subtitle = VorlageUtil.getName(VorlageTyp.EINZELBUCHUNGEN_SUBTITEL, object);
  }

  @Override
  public void doExport(Object[] objects, IOFormat format, File file,
      ProgressMonitor monitor) throws RemoteException, ApplicationException,
      FileNotFoundException, DocumentException, IOException
  {
    BuchungsControl control = (BuchungsControl) objects[0];
    BuchungQuery query = control.getQuery();
    ArrayList<Buchungsart> buchungsarten = getBuchungsarten(query);
    kontonummer_in_buchungsliste = getKontonummer();
    FileOutputStream fos = new FileOutputStream(file);

    Reporter reporter = new Reporter(fos, title, subtitle);
    if (kontonummer_in_buchungsliste)
    {
      reporter = new Reporter(fos, title, subtitle, 50, 30, 20, 20, false);
    }

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
        query.getOrder("ORDER_DATUM_ID");
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
      query.getOrder("ORDER_DATUM_ID");
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
      createTableHeader(reporter);
      reporter.addColumn("", Element.ALIGN_RIGHT);
      if (kontonummer_in_buchungsliste)
      {
        reporter.addColumn("", Element.ALIGN_LEFT);
      }
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn("Gesamtsumme", Element.ALIGN_LEFT);
      reporter.addColumn(summe);
      reporter.closeTable();
    }

    if (!nichtLeer)
    {
      createTableHeader(reporter);
      reporter.addColumn("", Element.ALIGN_RIGHT);
      if (kontonummer_in_buchungsliste)
      {
        reporter.addColumn("", Element.ALIGN_LEFT);
      }
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.addColumn("Keine Buchungen", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
      reporter.closeTable();
    }
    reporter.addParams(control.getParams());
    reporter.close();
    fos.close();
  }

  private void createTableHeader(Reporter reporter)
      throws DocumentException, RemoteException
  {
    reporter.addHeaderColumn("Nummer", Element.ALIGN_CENTER, 22,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Datum", Element.ALIGN_CENTER, 32,
        BaseColor.LIGHT_GRAY);
    if (kontonummer_in_buchungsliste)
    {
      reporter.addHeaderColumn("Konto", Element.ALIGN_CENTER, 34,
          BaseColor.LIGHT_GRAY);
    }
    reporter.addHeaderColumn("Auszug", Element.ALIGN_CENTER, 20,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Name", Element.ALIGN_CENTER,
        (kontonummer_in_buchungsliste) ? 86 : 100, BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Zahlungsgrund", Element.ALIGN_CENTER, 100,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 40,
        BaseColor.LIGHT_GRAY);
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addHeaderColumn("Steuersatz", Element.ALIGN_CENTER, 25,
          BaseColor.LIGHT_GRAY);
    }
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
    Paragraph pBuchungsart = new Paragraph(
        buchungsklasseBezeichnung + " - " + buchungsartBezeichnung,
        Reporter.getFreeSansBold(10));
    reporter.add(pBuchungsart);
    double buchungsartSumme = 0;

    createTableHeader(reporter);

    for (Buchung b : buchungen)
    {
      reporter.addColumn(b.getID(), Element.ALIGN_RIGHT);
      reporter.addColumn(new JVDateFormatTTMMJJJJ().format(b.getDatum()),
          Element.ALIGN_CENTER);
      if (kontonummer_in_buchungsliste)
      {
        reporter.addColumn(b.getKonto().getNummer(), Element.ALIGN_RIGHT);
      }
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
      reporter.addColumn(b.getBetrag());
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        reporter
            .addColumn(b.getSteuer() == null ? null : b.getSteuer().getSatz());
      }
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

    reporter.addColumn("", Element.ALIGN_RIGHT);
    reporter.addColumn("", Element.ALIGN_CENTER);
    if (kontonummer_in_buchungsliste)
    {
      reporter.addColumn("", Element.ALIGN_RIGHT);
    }
    reporter.addColumn("", Element.ALIGN_LEFT);
    reporter.addColumn("", Element.ALIGN_LEFT);
    if (buchungen.size() == 0)
    {
      reporter.addColumn("Keine Buchung", Element.ALIGN_LEFT);
      reporter.addColumn("", Element.ALIGN_LEFT);
    }
    else
    {
      reporter.addColumn(String.format("Summe %s", bua.getBezeichnung()),
          Element.ALIGN_LEFT);
      summe += buchungsartSumme;
      reporter.addColumn(buchungsartSumme);
    }
    reporter.closeTable();
    return true;
  }
}
