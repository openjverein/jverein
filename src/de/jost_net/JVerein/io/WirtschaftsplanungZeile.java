/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.datasource.GenericObject;

import java.rmi.RemoteException;
import java.util.Objects;

public class WirtschaftsplanungZeile implements GenericObject
{
  private Wirtschaftsplan wirtschaftsplan;

  private Double planEinnahme;

  private Double planAusgabe;

  private Double istEinnahme;

  private Double istAusgabe;

  public WirtschaftsplanungZeile(Wirtschaftsplan wirtschaftsplan)
      throws RemoteException
  {
    this.wirtschaftsplan = wirtschaftsplan;
    this.planEinnahme = 0.;
    this.planAusgabe = 0.;
    this.istAusgabe = 0.;
    this.istEinnahme = 0.;
  }

  @Override
  public Object getAttribute(String s) throws RemoteException
  {
    double planSaldo = planEinnahme + planAusgabe;
    double istSaldo = istEinnahme + istAusgabe;

    switch (s)
    {
      case "id":
        return wirtschaftsplan.getID();
      case "datum_von":
        return wirtschaftsplan.getDatumVon();
      case "datum_bis":
        return wirtschaftsplan.getDatumBis();
      case "planEinnahme":
        return planEinnahme;
      case "planAusgabe":
        return planAusgabe;
      case "istEinnahme":
        return istEinnahme;
      case "istAusgabe":
        return istAusgabe;
      case "planSaldo":
        return planSaldo;
      case "istSaldo":
        return istSaldo;
      case "differenz":
        return istSaldo - planSaldo;
      default:
        throw new RemoteException(
            String.format("Ungültige Spaltenbezeichnung: %s", s));
    }
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "id", "datum_von", "datum_bis", "planEinnahme",
        "planAusgabe", "istEinnahme", "istAusgabe", "planSaldo", "istSaldo",
        "differenz" };
  }

  @Override
  public String getID() throws RemoteException
  {
    return wirtschaftsplan.getID();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  public Double getPlanEinnahme()
  {
    return planEinnahme;
  }

  public void setPlanEinnahme(Double planEinnahme)
  {
    this.planEinnahme = planEinnahme;
  }

  public Double getPlanAusgabe()
  {
    return planAusgabe;
  }

  public void setPlanAusgabe(Double planAusgabe)
  {
    this.planAusgabe = planAusgabe;
  }

  public void setWirtschaftsplan(Wirtschaftsplan wirtschaftsplan)
  {
    this.wirtschaftsplan = wirtschaftsplan;
  }

  public Wirtschaftsplan getWirtschaftsplan()
  {
    return wirtschaftsplan;
  }

  public Double getIstEinnahme()
  {
    return istEinnahme;
  }

  public void setIstEinnahme(Double istEinnahme)
  {
    this.istEinnahme = istEinnahme;
  }

  public Double getIstAusgabe()
  {
    return istAusgabe;
  }

  public void setIstAusgabe(Double istAusgabe)
  {
    this.istAusgabe = istAusgabe;
  }

  @Override
  public boolean equals(GenericObject o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    WirtschaftsplanungZeile that = (WirtschaftsplanungZeile) o;
    try
    {
      return wirtschaftsplan.equals(that.getWirtschaftsplan());
    }
    catch (RemoteException e)
    {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(wirtschaftsplan);
  }
}
