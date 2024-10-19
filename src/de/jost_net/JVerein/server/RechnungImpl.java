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
import java.util.ArrayList;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;

public class RechnungImpl extends AbstractDBObject implements Rechnung
{

  /**
   * 
   */
  private static final long serialVersionUID = -286067581211521888L;

  public RechnungImpl() throws RemoteException
  {
    super();
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    return (Mitglied) getAttribute("mitglied");
  }

  @Override
  public void setMitglied(int mitglied) throws RemoteException
  {
    setAttribute("mitglied", mitglied);
  }

  @Override
  public double getBetrag() throws RemoteException
  {
    return (double) getAttribute("betrag");
  }

  @Override
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", betrag);
  }

  @Override
  protected String getTableName()
  {
    return "rechnung";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public void setFormular(Formular formular) throws RemoteException
  {
    setAttribute("formular", Long.valueOf(formular.getID()));
  }

  @Override
  public Formular getFormular() throws RemoteException
  {
    return (Formular) getAttribute("formular");
  }

  @Override
  public void setDatum(Date date) throws RemoteException
  {
    setAttribute("datum", date);
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("id-int".equals(fieldName))
    {
      try
      {
        return Integer.valueOf(getID());
      }
      catch (Exception e)
      {
        Logger.error("unable to parse id: " + getID());
        return getID();
      }
    }
    return super.getAttribute(fieldName);
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("formular".equals(field))
    {
      return Formular.class;
    }
    if ("mitglied".equals(field))
    {
      return Mitglied.class;
    }
    return null;
  }

  @Override
  public ArrayList<Mitgliedskonto> getMitgliedskontoList()
      throws RemoteException
  {
    ArrayList<Mitgliedskonto> mks = new ArrayList<>();
    DBIterator<Mitgliedskonto> it = Einstellungen.getDBService()
        .createList(Mitgliedskonto.class);
    it.addFilter("rechnung = ?", getID());
    it.setOrder("ORDER BY datum");
    while (it.hasNext())
    {
      mks.add((Mitgliedskonto) it.next());
    }
    return mks;
  }

}
