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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;

import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.util.VorlageUtil;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

public class KontenrahmenExportXML extends KontenrahmenExport
{

  private Writer output;

  private XMLWriter xmlwriter;

  private IXMLElement xmltree;

  private IXMLElement xmltreeklassen;

  private IXMLElement xmlkl;

  private IXMLElement xmlklbarten;

  private IXMLElement xmlversion;

  @Override
  public String getName()
  {
    return "Kontenrahmen XML-Export V1";
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
        return KontenrahmenExportXML.this.getName();
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

  @Override
  public String getDateiname(Object object)
  {
    return VorlageUtil.getName(VorlageTyp.KONTENRAHMEN_DATEINAME_V1) + ".xml";
  }

  @Override
  protected void open() throws IOException
  {
    output = new FileWriter(file);
    xmltree = new XMLElement("kontenrahmen");
    xmlversion = new XMLElement("version");
    xmlversion.setAttribute("version", "1");
    xmltree.addChild(xmlversion);
    xmltreeklassen = xmltree.createElement("buchungsklassen");
    xmltree.addChild(xmltreeklassen);
    xmlwriter = new XMLWriter(output);
  }

  @Override
  protected void addKlasse(Buchungsklasse klasse) throws RemoteException
  {
    xmlkl = new XMLElement("buchungsklasse");
    xmlkl.setAttribute("bezeichnung", klasse.getBezeichnung());
    xmlkl.setAttribute("nummer", klasse.getNummer() + "");
    xmltreeklassen.addChild(xmlkl);
    xmlklbarten = xmlkl.createElement("buchungsarten");
    xmlkl.addChild(xmlklbarten);
  }

  @Override
  protected void addBuchungsart(Buchungsart buchungsart) throws RemoteException
  {
    IXMLElement xmlba = new XMLElement("buchungsart");
    xmlba.setAttribute("nummer", buchungsart.getNummer() + "");
    xmlba.setAttribute("bezeichnung", buchungsart.getBezeichnung());
    xmlba.setAttribute("art", buchungsart.getArt() + "");
    xmlba.setAttribute("spende", buchungsart.getSpende().toString());
    xmlba.setAttribute("steuersatz", buchungsart.getSteuer() == null ? "0"
        : buchungsart.getSteuer().getSatz().toString());
    if (buchungsart.getSteuer() != null)
      xmlba.setAttribute("steuer_buchungsart",
          buchungsart.getSteuer().getBuchungsart().getNummer() + "");
    else
      xmlba.setAttribute("steuer_buchungsart", "");
    xmlba.setAttribute("status", buchungsart.getStatus() + "");
    xmlba.setAttribute("abschreibung",
        buchungsart.getAbschreibung().toString());
    xmlklbarten.addChild(xmlba);
  }

  @Override
  protected void close() throws IOException
  {
    xmlwriter.write(xmltree);
    output.close();
  }

}
