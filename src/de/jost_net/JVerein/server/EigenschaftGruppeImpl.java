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
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class EigenschaftGruppeImpl extends AbstractJVereinDBObject
    implements EigenschaftGruppe
{

  private static final long serialVersionUID = -5906609226109964967L;

  public EigenschaftGruppeImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "eigenschaftgruppe";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "bezeichnung";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      DBIterator<Eigenschaft> it = Einstellungen.getDBService()
          .createList(Eigenschaft.class);
      it.addFilter("eigenschaftgruppe = ?", new Object[] { getID() });
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException("Sie enthält noch Eigenschaften.");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "EigenschaftGruppe kann nicht gelöscht werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
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
      if (getName() == null || getName().isEmpty())
      {
        throw new ApplicationException("Bitte Name eingeben!");
      }
      if (!getName().matches("^[a-zA-Z0-9_]+$"))
      {
        throw new ApplicationException(
            "Name enthält ungültige Zeichen, nur 0-9, a-z, A-Z, _ erlaubt!");
      }
      DBIterator<EigenschaftGruppe> gruppeIt = Einstellungen.getDBService()
          .createList(EigenschaftGruppe.class);
      if (!this.isNewObject())
      {
        gruppeIt.addFilter("id != ?", getID());
      }
      gruppeIt.addFilter("bezeichnung = ?", getBezeichnung());
      if (gruppeIt.hasNext())
      {
        throw new ApplicationException(
            "Bitte eindeutige Bezeichnung eingeben!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "EigenschaftGruppe kann nicht gespeichert werden. Siehe system log";
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
    return (String) getAttribute("bezeichnung");
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
    setAttribute("bezeichnung", bezeichnung);
  }

  @Override
  public Boolean getPflicht() throws RemoteException
  {
    return Util.getBoolean(getAttribute("pflicht"));
  }

  @Override
  public void setPflicht(Boolean pflicht) throws RemoteException
  {
    setAttribute("pflicht", pflicht);
  }

  @Override
  public Boolean getMax1() throws RemoteException
  {
    return Util.getBoolean(getAttribute("max1"));
  }

  @Override
  public void setMax1(Boolean max1) throws RemoteException
  {
    setAttribute("max1", max1);
  }

  @Override
  public String getObjektName()
  {
    return "Eigenschaftengruppe";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Eigenschaftengruppen";
  }

  @Override
  public String getName() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public void setName(String name) throws RemoteException
  {
    setAttribute("name", name);
  }
}
