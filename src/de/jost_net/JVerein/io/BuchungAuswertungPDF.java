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
import java.util.TreeMap;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungAuswertungPDF
{

  private double summe = 0;

  private double summeeinnahmen = 0;

  private double summeausgaben = 0;

  private double summeumbuchungen = 0;

  private boolean kontonummer_in_buchungsliste = false;

  public BuchungAuswertungPDF(ArrayList<Buchungsart> buchungsarten,
      final File file, BuchungQuery query, boolean einzel,
      final TreeMap<String, String> params) throws ApplicationException
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      String title = null;
      if (einzel)
      {
        title = "Buchungsliste";
      }
      else
      {
        title = "Summenliste";
      }

      if (Boolean.valueOf((Boolean) Einstellungen
          .getEinstellung(Property.KONTONUMMERINBUCHUNGSLISTE)))
        kontonummer_in_buchungsliste = true;

      Reporter reporter = new Reporter(fos, title, query.getSubtitle(),
          buchungsarten.size());
      if (kontonummer_in_buchungsliste)
      {
        reporter = new Reporter(fos, title, query.getSubtitle(),
            buchungsarten.size(), 50, 30, 20, 20);
      }

      if (!einzel)
      {
        createTableHeaderSumme(reporter);
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
                || bua.getBuchungsklasse().getNummer() != bukla.getNummer())
            {
              continue;
            }
          }
          if (einzel)
          {
            query.getOrder("ORDER_DATUM_ID");
          }
          List<Buchung> liste = getBuchungenEinerBuchungsart(query.get(), bua,
              bukla);
          if (liste.size() > 0)
          {
            anzahlBuchungsarten += 1;
          }
          nichtLeer = createTableContent(reporter, bua, bukla, liste, einzel)
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
        if (einzel)
        {
          query.getOrder("ORDER_DATUM_ID");
        }
        List<Buchung> liste = getBuchungenEinerBuchungsart(query.get(), bua);
        if (liste.size() > 0)
        {
          anzahlBuchungsarten += 1;
        }
        nichtLeer = createTableContent(reporter, bua, null, liste, einzel)
            || nichtLeer;
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
        nichtLeer = createTableContent(reporter, bua, null, liste, einzel)
            || nichtLeer;
      }

      if (anzahlBuchungsarten > 1)
      {
        if (einzel)
        {
          createTableHeaderEinzel(reporter);
          reporter.addColumn("", Element.ALIGN_RIGHT);
          if (kontonummer_in_buchungsliste)
            reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("Gesamtsumme", Element.ALIGN_LEFT);
          reporter.addColumn(summe);
          reporter.closeTable();
        }
        else
        {
          reporter.addColumn("Summe Einnahmen", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn(summeeinnahmen);
          reporter.addColumn("Summe Ausgaben", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn(summeausgaben);
          reporter.addColumn("Summe Umbuchungen", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn(summeumbuchungen);
          reporter.addColumn("Saldo", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn(summeeinnahmen + summeausgaben + summeumbuchungen);
          reporter.closeTable();
        }
      }
      else
      {
        if (!einzel && nichtLeer)
        {
          reporter.closeTable();
        }
      }
      if (!nichtLeer)
      {
        if (einzel)
        {
          createTableHeaderEinzel(reporter);
          reporter.addColumn("", Element.ALIGN_RIGHT);
          if (kontonummer_in_buchungsliste)
            reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("Keine Buchungen", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.closeTable();
        }
        else
        {
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.addColumn("Keine Buchungen", Element.ALIGN_LEFT);
          reporter.addColumn("", Element.ALIGN_LEFT);
          reporter.closeTable();
        }
      }
      reporter.addParams(params);
      GUI.getStatusBar().setSuccessText("Auswertung fertig.");

      reporter.close();
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

  private void createTableHeaderEinzel(Reporter reporter)
      throws DocumentException, RemoteException
  {
    reporter.addHeaderColumn("Nummer", Element.ALIGN_CENTER, 22,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Datum", Element.ALIGN_CENTER, 32,
        BaseColor.LIGHT_GRAY);
    if (kontonummer_in_buchungsliste)
      reporter.addHeaderColumn("Konto", Element.ALIGN_CENTER, 34,
          BaseColor.LIGHT_GRAY);
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

  private void createTableHeaderSumme(Reporter reporter)
      throws DocumentException
  {
    reporter.addHeaderColumn("Buchungsklasse", Element.ALIGN_CENTER, 50,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Buchungsart", Element.ALIGN_CENTER, 150,
        BaseColor.LIGHT_GRAY);
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 60,
        BaseColor.LIGHT_GRAY);
    reporter.createHeader();
  }

  private boolean createTableContent(Reporter reporter, Buchungsart bua,
      Buchungsklasse bukla, List<Buchung> buchungen, boolean einzel)
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
    if (einzel)
    {
      Paragraph pBuchungsart = new Paragraph(
          buchungsklasseBezeichnung + " - " + buchungsartBezeichnung,
          Reporter.getFreeSansBold(10));
      reporter.add(pBuchungsart);
    }
    double buchungsartSumme = 0;
    if (einzel)
    {
      createTableHeaderEinzel(reporter);
    }

    for (Buchung b : buchungen)
    {
      if (einzel)
      {
        reporter.addColumn(b.getID(), Element.ALIGN_RIGHT);
        reporter.addColumn(new JVDateFormatTTMMJJJJ().format(b.getDatum()),
            Element.ALIGN_CENTER);
        if (kontonummer_in_buchungsliste)
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
        reporter.addColumn(b.getBetrag());
        if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
        {
          reporter.addColumn(
              b.getSteuer() == null ? null : b.getSteuer().getSatz());
        }
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
    if (einzel)
    {
      reporter.addColumn("", Element.ALIGN_RIGHT);
      reporter.addColumn("", Element.ALIGN_CENTER);
      if (kontonummer_in_buchungsliste)
        reporter.addColumn("", Element.ALIGN_RIGHT);
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
    }
    else
    {
      reporter.addColumn(buchungsklasseBezeichnung, Element.ALIGN_LEFT);
      reporter.addColumn(buchungsartBezeichnung, Element.ALIGN_LEFT);
      if (buchungen.size() == 0)
      {
        reporter.addColumn("Keine Buchung", Element.ALIGN_LEFT);
      }
      else
      {
        reporter.addColumn(buchungsartSumme);
      }
    }
    if (einzel)
    {
      reporter.closeTable();
    }
    return true;
  }

  private List<Buchung> getBuchungenEinerBuchungsart(List<Buchung> buchungen,
      Buchungsart bua, Buchungsklasse bukla) throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() == null
          || b.getBuchungsart().getNummer() != bua.getNummer())
      {
        continue;
      }

      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        if (b.getBuchungsklasseId() == null
            || b.getBuchungsklasse().getNummer() != bukla.getNummer())
        {
          continue;
        }
      }
      liste.add(b);
    }
    return liste;
  }

  // Buchungen einer Buchungsart ohne Buchungsklasse
  private List<Buchung> getBuchungenEinerBuchungsart(List<Buchung> buchungen,
      Buchungsart bua) throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() == null
          || b.getBuchungsart().getNummer() != bua.getNummer())
      {
        continue;
      }

      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        if (b.getBuchungsklasseId() != null)
        {
          continue;
        }
      }
      liste.add(b);
    }
    return liste;
  }

  // Buchungen ohne Buchungsart
  private List<Buchung> getBuchungenOhneBuchungsart(List<Buchung> buchungen)
      throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() != null)
      {
        continue;
      }
      liste.add(b);
    }
    return liste;
  }
}
