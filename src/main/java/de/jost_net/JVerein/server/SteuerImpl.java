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
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.datasource.rmi.DBIterator;
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
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Object l = (Object) super.getAttribute("buchungsklasse");
    if (l == null)
    {
      return null;
    }

    if (l instanceof Buchungsklasse)
    {
      return (Buchungsklasse) l;
    }

    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(l);
  }

  @Override
  public void setBuchungsklasse(Buchungsklasse buchungsklasse)
      throws RemoteException
  {
    setAttribute("buchungsklasse", buchungsklasse);
  }

  @Override
  public void setAktiv(boolean aktiv) throws RemoteException
  {
    setAttribute("aktiv", aktiv);
  }

  @Override
  public boolean getAktiv() throws RemoteException
  {
    return Util.getBoolean(getAttribute("aktiv"));
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
      // Prüfen ob es Buchungen mit der Steuer gibt
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("steuer = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht gelöscht werden, es gibt Buchungen mit dieser Steuer.");
      }

      // Prüfen ob es Buchungsarten mit der Steuer gibt
      it = Einstellungen.getDBService().createList(Buchungsart.class);
      it.addFilter("steuer = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht gelöscht werden, es gibt Buchungsarten mit dieser Steuer.");
      }

      // Prüfen ob es Beitragsgruppen mit der Steuer gibt
      it = Einstellungen.getDBService().createList(Beitragsgruppe.class);
      it.addFilter("steuer = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht gelöscht werden, es gibt Beitragsgruppen mit dieser Steuer.");
      }

      // Prüfen ob es Zusatzbeträge mit der Steuer gibt
      it = Einstellungen.getDBService().createList(Zusatzbetrag.class);
      it.addFilter("steuer = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht gelöscht werden, es gibt Zusatzbeträge mit dieser Steuer.");
      }

      // Prüfen ob es Zusatzbetragvorlagen mit der Steuer gibt
      it = Einstellungen.getDBService().createList(ZusatzbetragVorlage.class);
      it.addFilter("steuer = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht gelöscht werden, es gibt Zusatzbetragvorlagen mit dieser Steuer.");
      }

      // Prüfen ob es Sollbuchungspositionen mit der Steuer gibt
      it = Einstellungen.getDBService().createList(SollbuchungPosition.class);
      it.addFilter("steuer = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Steuer kann nicht gelöscht werden, es gibt Sollbuchungspositionen mit dieser Steuer.");
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

      if (getBuchungsart().hasBuchungen())
      {
        throw new ApplicationException(
            "Buchungsart enthält bereit Buchungen. Kann nicht als Steuer-Buchungsart verwendet werden.");
      }

      if (getBuchungsart().getSteuer() != null)
      {
        throw new ApplicationException(
            "Buchungsart hat eine Steuer gesetzt. Kann nicht als Steuer-Buchungsart verwendet werden.");
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
    else if ("buchungsklasse".equals(fieldName))
      return getBuchungsklasse();

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
    return "name";
  }

  @Override
  public Object getAttributeDefault(String fieldName)
  {
    switch (fieldName)
    {
      case "aktiv":
        return true;
      default:
        return null;
    }
  }

  @Override
  public String getObjektName()
  {
    return "Steuer";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Steuern";
  }
}
