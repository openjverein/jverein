/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.Beleggruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BeleggruppeImpl extends AbstractJVereinDBObject
    implements Beleggruppe
{

  private static final long serialVersionUID = 1L;

  public BeleggruppeImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "beleggruppe";
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
      // Prüfen ob Beleggruppe schon verwendet wird
      DBIterator<Beleg> it = Einstellungen.getDBService()
          .createList(Beleg.class);
      it.addFilter("beleggruppe = ?", getID());
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Die Beleggruppe wird von Belegen benutzt.");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Beleggruppe kann nicht gelöscht werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().isBlank())
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Beleggruppe kann nicht gespeichert werden. Siehe system log.";
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
  public String getObjektName()
  {
    return "Beleggruppe";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Beleggruppen";
  }

}
