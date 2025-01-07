/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.server;

import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.willuhn.datasource.db.AbstractDBObject;

import java.rmi.RemoteException;

public class WirtschaftsplanItemImpl extends AbstractDBObject
    implements WirtschaftsplanItem
{
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
  protected Class<?> getForeignObject(String field) throws RemoteException
  {
    switch (field)
    {
      case "wirtschaftsplan":
        return Wirtschaftsplan.class;
      case "buchungsart":
        return Buchungsart.class;
      case "buchungsklasse":
        return Buchungsklasse.class;
      default:
        return null;
    }
  }

  @Override
  public void setId(String id) throws RemoteException
  {
    setAttribute("id", id);
  }

  @Override
  public Long getWirtschaftsplanId() throws RemoteException
  {
    return (Long) getAttribute("wirtschaftsplan");
  }

  @Override
  public Wirtschaftsplan getWirtschaftsplan() throws RemoteException
  {
    Long l = (Long) getAttribute("wirtschaftsplan");
    if (l == null)
    {
      return null;
    }
    Cache cache = Cache.get(Wirtschaftsplan.class, true);
    return (Wirtschaftsplan) cache.get(l);
  }

  @Override
  public void setWirtschaftsplanId(Long wirtschaftsplanId)
      throws RemoteException
  {
    setAttribute("wirtschaftsplan", wirtschaftsplanId);

  }

  @Override
  public Long getBuchungsartId() throws RemoteException
  {
    return (Long) getAttribute("buchungsart");
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Long l = (Long) getAttribute("buchungsart");
    if (l == null)
    {
      return null;
    }
    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public void setBuchungsartId(Long buchungsartId) throws RemoteException
  {
    setAttribute("buchungsart", buchungsartId);
  }

  @Override
  public Long getBuchungsklasseId() throws RemoteException
  {
    return (Long) getAttribute("buchungsklasse");
  }

  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Long l = (Long) getAttribute("buchungsklasse");
    if (l == null)
    {
      return null;
    }
    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(l);
  }

  @Override
  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException
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
    Double d = (Double) getAttribute("soll");
    if (d == null)
    {
      return 0.;
    }
    return d;
  }

  @Override
  public void setSoll(double soll) throws RemoteException
  {
    setAttribute("soll", soll);
  }
}
