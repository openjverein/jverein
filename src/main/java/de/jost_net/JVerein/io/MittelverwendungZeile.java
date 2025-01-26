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

import de.willuhn.datasource.GenericObject;

/**
 * Hilfs-Objekt
 */
public class MittelverwendungZeile implements GenericObject
{
  private Integer position;

  private String art = "";

  private String bezeichnung = "";

  private Double betrag = null;

  private Double summe = null;

  private String kommentar = "";

  public static final int UNDEFINED = 0;

  public static final int EINNAHME = 1;

  public static final int AUSGABE = 2;

  public static final int SUMME = 3;

  public static final int LEERZEILE = 4;

  public static final int ART = 5;

  private int status = UNDEFINED;

  public MittelverwendungZeile(int status, Integer position,
      String bezeichnung, Double betrag, Double summe, String kommentar)
  {
    this.position = position;
    this.status = status;
    this.bezeichnung = bezeichnung;
    this.betrag = betrag;
    this.summe = summe;
    this.kommentar = kommentar;
  }

  public MittelverwendungZeile(int status, Integer position, String bezeichnung,
      Double betrag, Double summe, String kommentar, String art)
  {
    this.position = position;
    this.status = status;
    this.art = art;
    this.bezeichnung = bezeichnung;
    this.betrag = betrag;
    this.summe = summe;
    this.kommentar = kommentar;
  }

  public int getStatus()
  {
    return status;
  }

  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
    if (arg0.equals("art"))
    {
      return art;
    }
    else if (arg0.equals("bezeichnung"))
    {
      return bezeichnung;
    }
    else if (arg0.equals("betrag"))
    {
      return betrag;
    }
    else if (arg0.equals("summe"))
    {
      return summe;
    }
    else if (arg0.equals("position"))
    {
      return position;
    }
    else if (arg0.equals("kommentar"))
    {
      return kommentar;
    }
    throw new RemoteException(
        String.format("Ungültige Spaltenbezeichung: %s", arg0));
  }

  @Override
  public String[] getAttributeNames()
  {
    return new String[] { "bezeichnung", "betrag" };
  }

  @Override
  public String getID() throws RemoteException
  {
    return Integer.toString(position);
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "bezeichnung";
  }

  @Override
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof MittelverwendungZeile))
    {
      return false;
    }
    return this.getID().equals(arg0.getID());
  }
}
