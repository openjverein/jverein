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
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.keys.DateinameTyp;
import de.jost_net.JVerein.rmi.DateinamenVorlage;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class DateinamenVorlageImpl extends AbstractJVereinDBObject
    implements DateinamenVorlage
{

  private static final long serialVersionUID = 1L;

  public DateinamenVorlageImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "dateinamenvorlage";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "id";
  }

  @Override
  protected void deleteCheck()
  {
    //
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    //
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  public String getDateiname() throws RemoteException
  {
    return (String) getAttribute("dateiname");
  }

  @Override
  public void setDateiname(String dateiname) throws RemoteException
  {
    setAttribute("dateiname", dateiname);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("id-int".equals(fieldName))
    {
      try
      {
        return DateinameTyp.getByKey(Integer.valueOf(getID())).toString();
      }
      catch (Exception e)
      {
        Logger.error("Unable to parse id: " + getID());
        return getID();
      }
    }

    return super.getAttribute(fieldName);
  }
}
