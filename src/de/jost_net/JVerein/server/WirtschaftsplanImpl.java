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

import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.datasource.db.AbstractDBObject;

import java.rmi.RemoteException;
import java.util.Date;

public class WirtschaftsplanImpl extends AbstractDBObject implements Wirtschaftsplan
{
  private static final long serialVersionUID = 1L;

  public WirtschaftsplanImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "wirtschaftsplan";
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
  public Date getDatumVon() throws RemoteException
  {
    return (Date) getAttribute("datum_von");
  }

  @Override
  public void setDatumVon(Date date) throws RemoteException
  {
    setAttribute("datum_von", date);
  }

  @Override
  public Date getDatumBis() throws RemoteException
  {
    return (Date) getAttribute("datum_bis");
  }

  @Override
  public void setDatumBis(Date date) throws RemoteException
  {
    setAttribute("datum_bis", date);
  }
}
