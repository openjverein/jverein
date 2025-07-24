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
import java.sql.Clob;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.io.AltersgruppenParser;
import de.jost_net.JVerein.io.JubilaeenParser;
import de.jost_net.JVerein.rmi.Einstellung;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.VonBis;
import de.jost_net.OBanToo.SEPA.IBAN;
import de.jost_net.OBanToo.SEPA.SEPAException;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class EinstellungImpl extends AbstractDBObject implements Einstellung
{

  private static final long serialVersionUID = 3513343626868776722L;

  public EinstellungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      // Name
      if (hasChanged(Property.NAME.getKey())
          && ((String) getAttribute(Property.NAME.getKey())).isBlank())
      {
        throw new ApplicationException("Bitte Namen eingeben");
      }

      // Altersgruppen
      if (hasChanged(Property.ALTERSGRUPPEN.getKey()))
      {
        try
        {
          new AltersgruppenParser(
              (String) getAttribute(Property.ALTERSGRUPPEN.getKey()));
        }
        catch (RuntimeException e)
        {
          throw new ApplicationException(e.getMessage());
        }
      }

      // Beitrag-Altersstufen
      if (hasChanged(Property.BEITRAGALTERSSTUFEN.getKey()))
      {
        try
        {
          AltersgruppenParser ap = new AltersgruppenParser(
              (String) getAttribute(Property.BEITRAGALTERSSTUFEN.getKey()));
          ArrayList<VonBis> vbs = new ArrayList<VonBis>();
          while (ap.hasNext())
          {
            vbs.add(ap.getNext());
          }
          for (int i = 0; i < 100; i++)
          {
            boolean found = false;
            for (VonBis vb : vbs)
            {
              if (i >= vb.getVon() && i <= vb.getBis())
              {
                if (found)
                  throw new ApplicationException(
                      i + " Jahre ist in mehreren Altersstufen enthalten");
                found = true;
              }
            }
            if (!found)
              throw new ApplicationException(
                  "Keine passende Altersstufe gefunden für " + i + " Jahre");
          }
        }
        catch (RuntimeException e)
        {
          throw new ApplicationException(e.getMessage().replace("\n", " "));
        }
      }

      // Begin-Geschäftsjahr
      if (hasChanged(Property.BEGINNGESCHAEFTSJAHR.getKey()))
      {
        try
        {
          Datum.toDate(
              (String) getAttribute(Property.BEGINNGESCHAEFTSJAHR.getKey())
                  + 2000);
        }
        catch (ParseException e)
        {
          throw new ApplicationException("Ungültiges Datumsformat: "
              + getAttribute(Property.BEGINNGESCHAEFTSJAHR.getKey()));
        }
      }

      // IBAN
      if (hasChanged(Property.IBAN.getKey())
          && getAttribute(Property.IBAN.getKey()) != null)
      {
        try
        {
          new IBAN((String) getAttribute(Property.IBAN.getKey()));
        }
        catch (SEPAException e)
        {
          throw new ApplicationException(e.getMessage());
        }
      }

      // Dokumentenspeicherung
      if (hasChanged(Property.DOKUMENTENSPEICHERUNG.getKey())
          && (Boolean) getAttribute(Property.DOKUMENTENSPEICHERUNG.getKey()))
      {
        if (!JVereinPlugin.isArchiveServiceActive())
        {
          throw new ApplicationException(
              "Plugin jameica.messaging ist nicht installiert oder im LAN verfügbar!"
                  + " Wird zur Dokumentenspeicherung benötigt!");
        }
      }

      // Jubiläen
      if (hasChanged(Property.JUBILAEEN.getKey())
          || hasChanged(Property.ALTERSJUBILAEEN.getKey()))
      {
        try
        {
          new JubilaeenParser(
              (String) getAttribute(Property.JUBILAEEN.getKey()));
          new JubilaeenParser(
              (String) getAttribute(Property.ALTERSJUBILAEEN.getKey()));
        }
        catch (RuntimeException e)
        {
          throw new ApplicationException(e.getMessage());
        }
      }

      // Spendenbescheinigung Mindestbetrag
      if (hasChanged(Property.SPENDENBESCHEINIGUNGMINBETRAG.getKey())
          && (Double) getAttribute(
              Property.SPENDENBESCHEINIGUNGMINBETRAG.getKey()) < 0.01d)
      {
        throw new ApplicationException(
            "Mindestbetrag für Spendenbescheinigungen darf nicht kleiner als 0.01 sein");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Einstellung kann nicht gespeichert werden. Siehe system log";
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
  protected String getTableName()
  {
    return "einstellungneu";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "id";
  }

  @Override
  public String getKey() throws RemoteException
  {
    return (String) getAttribute("name");
  }

  @Override
  public void setKey(String name) throws RemoteException
  {
    setAttribute("name", name);
  }

  @Override
  public String getValue() throws RemoteException
  {
    Object o = getAttribute("wert");
    if (o == null)
    {
      return null;
    }
    if (o instanceof Clob)
    {
      Clob c = (Clob) o;
      try
      {
        return c.getSubString(1, (int) c.length());
      }
      catch (SQLException e)
      {
        throw new RemoteException("Fehler beim lesen von Clob", e);
      }
    }
    return (String) o;
  }

  @Override
  public void setValue(Object wert) throws RemoteException
  {
    setAttribute("wert", wert);
  }
}
