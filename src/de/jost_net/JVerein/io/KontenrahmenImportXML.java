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
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class KontenrahmenImportXML implements Importer
{

  @Override
  public void doImport(Object context, IOFormat format, File file,
      String encoding, ProgressMonitor monitor) throws Exception
  {
    DBIterator<Buchungsklasse> it = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    if (it.size() > 0)
    {
      throw new ApplicationException(
          "Import abgebrochen! Es sind bereits Buchungsklassen vorhanden.");
    }

    it = Einstellungen.getDBService().createList(Buchungsart.class);
    if (it.size() > 0)
    {
      throw new ApplicationException(
          "Import abgebrochen! Es sind bereits Buchungsarten vorhanden.");
    }
    // Parser erzeugen
    IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
    parser.setReader(new StdXMLReader(new FileInputStream(file)));

    // Root-Element "kontenrahmen" ermitteln
    IXMLElement root = (IXMLElement) parser.parse();
    
    // Version lesen
    @SuppressWarnings("rawtypes")
    Enumeration enu = root.enumerateChildren();
    IXMLElement ele = (IXMLElement) enu.nextElement();
    if (ele != null && ele.hasAttribute("version"))
    {
      String version = ele.getAttribute("version", "");
      if (version != null && !version.isEmpty() && !version.equalsIgnoreCase("1"))
        throw new ApplicationException("Versions Mismatch: Version 1 erwartet, Version " + version + " gelesen");
    }

    // Element "buchungsklassen" holen
    IXMLElement buchungsklassen = root.getFirstChildNamed("buchungsklassen");
    @SuppressWarnings("rawtypes")
    Enumeration enubu = buchungsklassen.enumerateChildren();
    while (enubu.hasMoreElements())
    {
      IXMLElement element = (IXMLElement) enubu.nextElement();
      Buchungsklasse bukl = (Buchungsklasse) Einstellungen.getDBService()
          .createObject(Buchungsklasse.class, null);
      bukl.setBezeichnung(element.getAttribute("bezeichnung", ""));
      bukl.setNummer(element.getAttribute("nummer", 0));
      bukl.store();
      IXMLElement buchungsarten = element.getFirstChildNamed("buchungsarten");
      @SuppressWarnings("rawtypes")
      Enumeration enubua = buchungsarten.enumerateChildren();
      HashMap<String, Double> mapsatz = new HashMap<String, Double>();
      HashMap<String, String> mapst = new HashMap<String, String>();
      while (enubua.hasMoreElements())
      {
        IXMLElement buaelement = (IXMLElement) enubua.nextElement();
        Buchungsart buchungsart = (Buchungsart) Einstellungen.getDBService()
            .createObject(Buchungsart.class, null);
        buchungsart.setArt(buaelement.getAttribute("art", 0));
        buchungsart.setBezeichnung(buaelement.getAttribute("bezeichnung", ""));
        buchungsart.setBuchungsklasseId(Long.valueOf(bukl.getID()));
        buchungsart.setNummer(buaelement.getAttribute("nummer", 0));
        String spende = buaelement.getAttribute("spende", "false");
        if (spende.equalsIgnoreCase("true"))
          buchungsart.setSpende(true);
        else
          buchungsart.setSpende(false);
        buchungsart.setStatus(buaelement.getAttribute("status", 0));
        String abschreibung = buaelement.getAttribute("abschreibung", "false");
        if (abschreibung.equalsIgnoreCase("true"))
          buchungsart.setAbschreibung(true);
        else
          buchungsart.setAbschreibung(false);
        buchungsart.store();
        Double steuersatz = Double.valueOf(buaelement.getAttribute("steuersatz", "0.00"));
        if (steuersatz != 0)
        {
          mapsatz.put(buchungsart.getID(), steuersatz);
          String start = buaelement.getAttribute("steuer_buchungsart", "");
          mapst.put(buchungsart.getID(), start);
        }
      }
      for (String id : mapsatz.keySet())
      {
        DBIterator<Buchungsart> buait = Einstellungen.getDBService()
            .createList(Buchungsart.class);
        buait.addFilter("ID = " + id);
        Buchungsart bua = buait.next();
        // TODO hier müssen wir noch die Steuer bestimmen. Evtl. die beim Update
        // erstellte steuer verwenden?
        // bua.setSteuersatz(mapsatz.get(id));
        DBIterator<Buchungsart> stbuait = Einstellungen.getDBService()
            .createList(Buchungsart.class);
        stbuait.addFilter("nummer = ?", mapst.get(id));
        Buchungsart stbua = stbuait.next();
        // bua.setSteuerBuchungsart(Long.parseLong(stbua.getID()));
        bua.store();
      }
    }
  }

  @Override
  public String getName()
  {
    return "Kontenrahmen-Import XML V1";
  }

  public boolean hasFileDialog()
  {
    return true;
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (objectType != Buchungsklasse.class)
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return KontenrahmenImportXML.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.xml" };
      }
    };
    return new IOFormat[] { f };
  }
}