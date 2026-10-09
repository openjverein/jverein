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
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.WirtschaftsplanItem;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
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
      DBIterator<Buchungsart> it = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      it.addFilter("buchungsklasse = ?", getID());
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Die Buchungsklasse wird von Buchungsarten benutzt.");
      }

      it = Einstellungen.getDBService().createList(Buchung.class);
      it.addFilter("buchungsklasse = ?", getID());
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Die Buchungsklasse wird von Buchungen benutzt.");
      }

      it = Einstellungen.getDBService().createList(Beitragsgruppe.class);
      it.addFilter("buchungsklasse = ?", getID());
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Es existieren Beitragsgruppen mit dieser Buchungsklasse.");
      }

      it = Einstellungen.getDBService().createList(SollbuchungPosition.class);
      it.addFilter("buchungsklasse = ?", getID());
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Es existieren Sollbuchungspositionen mit dieser Buchungsklasse.");
      }

      it = Einstellungen.getDBService().createList(Steuer.class);
      it.addFilter("buchungsklasse = ?", getID());
      it.setLimit(1);
      if (it.size() > 0)
      {
        throw new ApplicationException(
            "Es existieren Steuern mit dieser Buchungsklasse.");
      }

      it = Einstellungen.getDBService().createList(Zusatzbetrag.class);
      it.addFilter("buchungsklasse = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Es gibt Zusatzbeträge mit dieser Buchungsklasse.");
      }

      it = Einstellungen.getDBService().createList(ZusatzbetragVorlage.class);
      it.addFilter("buchungsklasse = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Es gibt Zusatzbetragvorlagen mit dieser Buchungsklasse.");
      }

      it = Einstellungen.getDBService().createList(WirtschaftsplanItem.class);
      it.addFilter("buchungsklasse = ?", this.getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Es gibt Wirtschaftspläne mit dieser Buchungsklasse.");
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
      if (getNummer().length() == 0)
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
  public String getNummer() throws RemoteException
  {
    String i = (String) getAttribute("nummer");
    if (i == null)
      return "";
    return i;
  }

  @Override
  public void setNummer(String i) throws RemoteException
  {
    setAttribute("nummer", i);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("nrbezeichnung"))
    {
      return getNummer() + " - " + getBezeichnung();
    }
    else if (fieldName.equals("bezeichnungnr"))
    {
      return getBezeichnung() + " (" + getNummer() + ")";
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
