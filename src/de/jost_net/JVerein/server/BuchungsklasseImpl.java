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
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsklasseImpl extends AbstractJVereinDBObject
    implements Buchungsklasse
{
  private static final long serialVersionUID = 500102542884220658L;

  public BuchungsklasseImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "buchungsklasse";
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
      // Prüfen ob Buchungsklasse schon verwendet wird
      DBIterator<Buchungsart> baIt = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      baIt.addFilter("buchungsklasse = ?", new Object[] { getID() });
      if (baIt.size() > 0)
      {
        throw new ApplicationException(String.format(
            "Die Buchungsklasse wird von %d Buchungsart(en) benutzt.",
            baIt.size()));
      }

      DBIterator<Buchung> buIt = Einstellungen.getDBService()
          .createList(Buchung.class);
      buIt.addFilter("buchungsklasse = ?", new Object[] { getID() });
      if (buIt.size() > 0)
      {
        throw new ApplicationException(
            String.format("Die Buchungsklasse wird von %d Buchung(en) benutzt.",
                buIt.size()));
      }

    }
    catch (RemoteException e)
    {
      String fehler = "Buchungsklasse kann nicht gelöscht werden. Siehe system log";
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
      if (getNummer() < 0)
      {
        throw new ApplicationException("Bitte Nummer eingeben!");
      }
      DBIterator<Buchungsklasse> klassenIt = Einstellungen.getDBService()
          .createList(Buchungsklasse.class);
      if (!this.isNewObject())
      {
        klassenIt.addFilter("id != ?", getID());
      }
      klassenIt.addFilter("nummer = ?", getNummer());
      if (klassenIt.hasNext())
      {
        throw new ApplicationException("Bitte eindeutige Nummer eingeben!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Buchungsklasse kann nicht gespeichert werden. Siehe system log";
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
  public int getNummer() throws RemoteException
  {
    Integer i = (Integer) getAttribute("nummer");
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override
  public void setNummer(int i) throws RemoteException
  {
    setAttribute("nummer", Integer.valueOf(i));
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("nrbezeichnung"))
    {
      return String.valueOf(getNummer()) + " - " + getBezeichnung();
    }
    else if (fieldName.equals("bezeichnungnr"))
    {
      int nr = getNummer();
      if (nr >= 0)
      {
        return getBezeichnung() + " (" + nr + ")";
      }
    }
    return super.getAttribute(fieldName);
  }

  @Override
  public String getObjektName()
  {
    return "Buchungsklasse";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Buchungsklassen";
  }
}
