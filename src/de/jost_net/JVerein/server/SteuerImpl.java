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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SteuerImpl extends AbstractJVereinDBObject implements Steuer
{

  private static final long serialVersionUID = -8362187140697518972L;

  public SteuerImpl() throws RemoteException
  {
    super();
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

  @Override
  public Double getSatz() throws RemoteException
  {
    return (Double) getAttribute("satz");
  }

  @Override
  public void setSatz(Double satz) throws RemoteException
  {
    setAttribute("satz", satz);
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Object l = (Object) super.getAttribute("buchungsart");
    if (l == null)
    {
      return null;
    }

    if (l instanceof Buchungsart)
    {
      return (Buchungsart) l;
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public void setBuchungsartId(Long buchungsart) throws RemoteException
  {
    setAttribute("buchungsart", buchungsart);
  }

  @Override
  public void setAktiv(boolean aktiv) throws RemoteException
  {
    setAttribute("aktiv", aktiv);
  }

  @Override
  public boolean getAktiv() throws RemoteException
  {
    Object o = getAttribute("aktiv");
    // Default aktiv
    if (o == null)
    {
      return true;
    }
    return Util.getBoolean(o);
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
    if (hasChanged("satz") || hasChanged("buchungsart"))
    {
      deleteCheck();
    }
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      boolean steuerInBuchung = (Boolean) Einstellungen
          .getEinstellung(Property.STEUERINBUCHUNG);

      // Prüfen ob es abgeschlossene Buchungen mit der Steuer gibt
      ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
          "buchung");
      it.addColumn("buchung.id");
      it.setLimit(1);

      it.join("jahresabschluss",
          "jahresabschluss.von <= buchung.datum and jahresabschluss.bis >= buchung.datum");
      if (steuerInBuchung)
      {
        it.addFilter("buchung.steuer = ?", getID());
      }
      else
      {
        it.join("buchungsart", "buchungsart.id = buchung.buchungsart");
        it.addFilter("buchungsart.steuer = ?", getID());
      }
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht geändert/gelöscht werden, es gibt abgeschlossene Buchungen mit dieser Steuer.");
      }

      // Prüfen ob es eine Rechnung mit dieser Steuer gibt
      it = new ExtendedDBIterator<>(Sollbuchung.TABLE_NAME);
      it.addColumn(Sollbuchung.TABLE_NAME_ID);
      it.setLimit(1);

      it.join("sollbuchungposition",
          "sollbuchungposition.sollbuchung = " + Sollbuchung.TABLE_NAME_ID);
      it.addFilter("rechnung is not null");
      if (steuerInBuchung)
      {
        it.addFilter("sollbuchungposition.steuer = ?", getID());
      }
      else
      {
        it.join("buchungsart",
            "buchungsart.id = sollbuchungposition.buchungsart");
        it.addFilter("buchungsart.steuer = ?", getID());
      }
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht geändert/gelöscht werden, es existieren Rechnungen mit dieser Steuer.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim delete Check", e);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getName() == null || getName().length() == 0)
      {
        throw new ApplicationException("Bitte Name eingeben");
      }
      if (getSatz() == null)
      {
        throw new ApplicationException("Bitte Steuersatz eingeben");
      }
      if (getSatz() < 0)
      {
        throw new ApplicationException("Steuersatz nicht gültig");
      }
      if (getBuchungsart() == null)
      {
        throw new ApplicationException("Bitte Steuer-Buchungsart auswählen.");
      }
      if (getBuchungsart().getArt() == ArtBuchungsart.UMBUCHUNG)
      {
        throw new ApplicationException(
            "Steuer-Buchungsart mit Art Umbuchung ist nicht möglich.");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Steuer kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("buchungsart".equals(fieldName))
      return getBuchungsart();

    return super.getAttribute(fieldName);
  }

  @Override
  protected String getTableName()
  {
    return "steuer";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }
}
