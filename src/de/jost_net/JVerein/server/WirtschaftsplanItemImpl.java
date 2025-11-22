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
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;

public class WirtschaftsplanItemImpl extends AbstractJVereinDBObject
    implements WirtschaftsplanItem
{
  private static final long serialVersionUID = 1L;

  public WirtschaftsplanItemImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "wirtschaftsplanitem";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public void setId(String id) throws RemoteException
  {
    setAttribute("id", id);
  }

  @Override
  public Wirtschaftsplan getWirtschaftsplan() throws RemoteException
  {
    Object l = super.getAttribute("wirtschaftsplan");
    if (l == null)
    {
      return null; // Kein Wirtschaftsplan zugeordnet
    }

    Cache cache = Cache.get(Wirtschaftsplan.class, true);
    return (Wirtschaftsplan) cache.get(l);
  }

  @Override
  public void setWirtschaftsplanId(String wirtschaftsplanId)
      throws RemoteException
  {
    setAttribute("wirtschaftsplan", wirtschaftsplanId);

  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Object l = super.getAttribute("buchungsart");
    if (l == null)
    {
      return null; // Keine Buchungsart zugeordnet
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public void setBuchungsartId(String buchungsartId) throws RemoteException
  {
    setAttribute("buchungsart", buchungsartId);
  }

  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Object l = super.getAttribute("buchungsklasse");
    if (l == null)
    {
      return null; // Keine Buchungsklasse zugeordnet
    }

    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(l);
  }

  @Override
  public void setBuchungsklasseId(String buchungsklasseId)
      throws RemoteException
  {
    setAttribute("buchungsklasse", buchungsklasseId);
  }

  @Override
  public String getPosten() throws RemoteException
  {
    return (String) getAttribute("posten");
  }

  @Override
  public void setPosten(String posten) throws RemoteException
  {
    setAttribute("posten", posten);
  }

  @Override
  public double getSoll() throws RemoteException
  {
    return (double) getAttribute("soll");
  }

  @Override
  public void setSoll(double soll) throws RemoteException
  {
    setAttribute("soll", soll);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    switch (fieldName)
    {
      case "wirtschaftsplan":
        return getWirtschaftsplan();
      case "buchungsart":
        return getBuchungsart();
      case "buchungsklasse":
        return getBuchungsklasse();
      default:
        return super.getAttribute(fieldName);
    }
  }

  @Override
  public Object getAttributeDefault(String fieldName)
  {
    switch (fieldName)
    {
      case "soll":
        return 0d;
      case "art":
        return 1;
      default:
        return null;
    }
  }

  @Override
  public int getArt() throws RemoteException
  {
    return (int) getAttribute("art");
  }

  @Override
  public void setArt(int art) throws RemoteException
  {
    setAttribute("art", art);
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Wirtschaftsplan Item";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Wirtschaftsplan Item";
  }
}
