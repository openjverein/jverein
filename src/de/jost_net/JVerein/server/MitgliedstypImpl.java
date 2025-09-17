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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedstypImpl extends AbstractJVereinDBObject
    implements Mitgliedstyp
{

  private static final long serialVersionUID = 500102542884220658L;

  public MitgliedstypImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryAttribute()
  {
    return PRIMARY_ATTRIBUTE;
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      if (getJVereinid() > 0)
      {
        throw new ApplicationException(
            "Dieser Datensatz darf nicht gelöscht werden!");
      }
      DBIterator<Mitglied> it = Einstellungen.getDBService()
          .createList(Mitglied.class);
      it.addFilter(Mitglied.MITGLIEDSTYP + " = ?", new Object[] { getID() });
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Es existieren Nicht-Mitglieder dieses Typs.");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(e);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().isEmpty())
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben!");
      }
      if (getBezeichnungPlural() == null || getBezeichnungPlural().isEmpty())
      {
        throw new ApplicationException("Bitte Bezeichnung Plural eingeben!");
      }
      DBIterator<Mitgliedstyp> typIt = Einstellungen.getDBService()
          .createList(Mitgliedstyp.class);
      if (!this.isNewObject())
      {
        typIt.addFilter("id != ?", getID());
      }
      typIt.addFilter("bezeichnung = ?", getBezeichnung());
      if (typIt.hasNext())
      {
        throw new ApplicationException(
            "Bitte eindeutige Bezeichnung eingeben!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Mitgliedstyp kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  protected Class<?> getForeignObject(String arg0)
  {
    return null;
  }

  @Override
  public String getBezeichnung() throws RemoteException
  {
    return (String) getAttribute(BEZEICHNUNG);
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute(BEZEICHNUNG, bezeichnung);
  }

  @Override
  public String getBezeichnungPlural() throws RemoteException
  {
    return (String) getAttribute(BEZEICHNUNG_PLURAL);
  }

  @Override
  public void setBezeichnungPlural(String bezeichnungplural)
      throws RemoteException
  {
    setAttribute(BEZEICHNUNG_PLURAL, bezeichnungplural);
  }

  @Override
  public int getJVereinid() throws RemoteException
  {
    Integer i = (Integer) getAttribute(JVEREINID);
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override
  public void setJVereinid(int jvereinid) throws RemoteException
  {
    setAttribute(JVEREINID, Integer.valueOf(jvereinid));
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    return super.getAttribute(fieldName);
  }

  @Override
  public String getObjektName()
  {
    return "Mitgliedstyp";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Mitgliedstypen";
  }
}
