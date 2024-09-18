/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de | www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.GenericObject;

/**
 * Hilfs-Objekt
 */
public class AnlagenlisteZeile implements GenericObject
{

  private Buchungsklasse buchungsklasse;

  private Buchungsart buchungsart;

  private String text_buchungsklasse;
  
  private String bezeichnung;
  
  private Integer nutzungsdauer;
  
  private Buchungsart afaart;
  
  private Date anschaffung;
  
  private Double kosten;

  private Double startwert;

  private Double abschreibung;
  
  private Double zugang;

  private Double abgang;

  private Double endwert;

  public static final int UNDEFINED = 0;

  public static final int HEADER = 1;
  
  public static final int HEADER2 = 2;

  public static final int DETAIL = 3;

  public static final int SALDOFOOTER = 4;

  public static final int GESAMTSALDOFOOTER = 5;


  private int status = UNDEFINED;

  public AnlagenlisteZeile(int status, Buchungsklasse buchungsklasse)
  {
    this.status = status;
    this.buchungsklasse = buchungsklasse;
    this.buchungsart = null;
    this.text_buchungsklasse = null;
    this.bezeichnung = null;
    this.nutzungsdauer = null;
    this.anschaffung = null;
    this.afaart = null;
    this.kosten = null;
    this.startwert = null;
    this.abschreibung = null;
    this.zugang = null;
    this.abgang = null;
    this.endwert = null;
  }
  
  public AnlagenlisteZeile(int status, Buchungsart buchungsart)
  {
    this.status = status;
    this.buchungsklasse = null;
    this.buchungsart = buchungsart;
    this.text_buchungsklasse = null;
    this.bezeichnung = null;
    this.nutzungsdauer = null;
    this.anschaffung = null;
    this.afaart = null;
    this.kosten = null;
    this.startwert = null;
    this.abschreibung = null;
    this.zugang = null;
    this.abgang = null;
    this.endwert = null;
  }

  public AnlagenlisteZeile(int status, String bezeichnung, 
      Integer nutzungsdauer, Date anschaffung, Buchungsart afaart, 
      Double kosten, Double startwert, Double zugang, 
      Double abschreibung, Double abgang, Double endwert)
  {
    this.status = status;
    this.buchungsklasse = null;
    this.buchungsart = null;
    this.text_buchungsklasse = null;
    this.bezeichnung = bezeichnung;
    this.nutzungsdauer = nutzungsdauer;
    this.anschaffung = anschaffung;
    this.afaart = afaart;
    this.kosten = kosten;
    this.startwert = startwert;
    this.abschreibung = abschreibung;
    this.zugang = zugang;
    this.abgang = abgang;
    this.endwert = endwert;
  }

  public AnlagenlisteZeile(int status, String text, 
      Double startwert, Double zugang, Double abschreibung, 
      Double abgang, Double endwert)
  {
    this.status = status;
    this.buchungsklasse = null;
    this.buchungsart = null;
    this.text_buchungsklasse = text;
    this.bezeichnung = null;
    this.nutzungsdauer = null;
    this.anschaffung = null;
    this.afaart = null;
    this.kosten = null;
    this.startwert = startwert;
    this.abschreibung = abschreibung;
    this.zugang = zugang;
    this.abgang = abgang;
    this.endwert = endwert;
  }
  
  // Für Leerzeile
  public AnlagenlisteZeile(int status, String text)
  {
    this.status = status;
    this.buchungsklasse = null;
    this.buchungsart = null;
    this.text_buchungsklasse = text;
    this.bezeichnung = null;
    this.nutzungsdauer = null;
    this.anschaffung = null;
    this.startwert = null;
    this.abschreibung = null;
    this.zugang = null;
    this.abgang = null;
    this.endwert = null;
  }

  public int getStatus()
  {
    return status;
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if (arg0.equals("anlagenart"))
    {
      if (buchungsklasse == null && buchungsart == null && text_buchungsklasse != null)
        return text_buchungsklasse;
      else if (buchungsklasse != null)
        return buchungsklasse.getBezeichnung();
      else if (buchungsart != null)
        return buchungsart.getBezeichnung();
      else
       return "";
    }
    if (arg0.equals("buchungsklassenbezeichnung"))
    {
      if (buchungsklasse == null && text_buchungsklasse != null)
      {
        return text_buchungsklasse;
      }
      return buchungsklasse != null ? buchungsklasse.getBezeichnung() : "";
    }
    else if (arg0.equals("buchungsartbezeichnung"))
    {
      return (buchungsart != null ? buchungsart.getBezeichnung() : "");
    }
    else if (arg0.equals("afaartbezeichnung"))
    {
      return (afaart != null ? afaart.getBezeichnung() : "");
    }
    else if (arg0.equals("bezeichnung"))
    {
      return bezeichnung;
    }
    else if (arg0.equals("nutzungsdauer"))
    {
      return nutzungsdauer;
    }
    else if (arg0.equals("anschaffung"))
    {
      return anschaffung;
    }
    else if (arg0.equals("kosten"))
    {
      return kosten;
    }
    else if (arg0.equals("startwert"))
    {
      return startwert;
    }
    else if (arg0.equals("zugang"))
    {
      return zugang;
    }
    else if (arg0.equals("abschreibung"))
    {
      return abschreibung;
    }
    else if (arg0.equals("abgang"))
    {
      return abgang;
    }
    else if (arg0.equals("endwert"))
    {
      return endwert;
    }
    throw new RemoteException(
        String.format("Ungültige Spaltenbezeichung: %s", arg0));
  }

  @Override
  public String[] getAttributeNames()
  {
    return new String[] { "buchungsklassenbezeichnung", "buchungsartbezeichnung", 
        "afaartbezeichnung", "bezeichnung", "nutzungsdauer", "anschaffung",
        "kosten", "startwert", "abschreibung", "endwert" };
  }

  @Override
  public String getID() throws RemoteException
  {
    return buchungsklasse.getID();
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "buchungsklasse";
  }

  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof AnlagenlisteZeile))
    {
      return false;
    }
    return this.getID().equals(arg0.getID());
  }
}
