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
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class EigenschaftImpl extends AbstractJVereinDBObject
    implements Eigenschaft
{

  private static final long serialVersionUID = 1L;

  public EigenschaftImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "eigenschaft";
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
      // Prüfen ob Eigenschaft schon verwendet wird
      DBIterator<Eigenschaften> it = Einstellungen.getDBService()
          .createList(Eigenschaften.class);
      it.addFilter("eigenschaft = ?", new Object[] { getID() });
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Sie ist noch mit Mitgliedern verknüpft.");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Eigenschaft kann nicht gelöscht werden. Siehe system log";
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
      if (!getName().matches("^[a-z0-9_]+$"))
      {
        throw new ApplicationException(
            "Name enthält ungültige Zeichen, nur 0-9, a-z, _ erlaubt!");
      }
      if (getEigenschaftGruppe() == null)
      {
        throw new ApplicationException("Bitte Eigenschaftengruppe auswählen!");
      }
      DBIterator<Eigenschaft> eigIt = Einstellungen.getDBService()
          .createList(Eigenschaft.class);
      if (!this.isNewObject())
      {
        eigIt.addFilter("id != ?", getID());
      }
      eigIt.addFilter("name = ?", getName());
      if (eigIt.hasNext())
      {
        throw new ApplicationException("Bitte eindeutigen Name eingeben!");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("insert check of eigenschaft failed", e);
      throw new ApplicationException(
          "Eigenschaft kann nicht gespeichert werden. Siehe system log.");
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("eigenschaftgruppe".equals(field))
    {
      return EigenschaftGruppe.class;
    }
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
  public EigenschaftGruppe getEigenschaftGruppe() throws RemoteException
  {
    return (EigenschaftGruppe) getAttribute("eigenschaftgruppe");
  }

  @Override
  public long getEigenschaftGruppeId() throws RemoteException
  {
    return Long.parseLong(getEigenschaftGruppe().getID());
  }

  @Override
  public void setEigenschaftGruppe(Long eigenschaftgruppe)
      throws RemoteException
  {
    setAttribute("eigenschaftgruppe", eigenschaftgruppe);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    return super.getAttribute(fieldName);
  }

  @Override
  public String getObjektName()
  {
    return "Eigenschaft";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Eigenschaften";
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
