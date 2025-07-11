/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 * <p>
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.server;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBService;

import java.rmi.RemoteException;
import java.util.Date;

public class WirtschaftsplanImpl extends AbstractDBObject
    implements Wirtschaftsplan
{
  private static final long serialVersionUID = 1L;

  public final static int EINNAHME = 0;
  public final static int AUSGABE = 1;

  private final static int BETRAG_COL = 2;

  public WirtschaftsplanImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "wirtschaftsplan";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public void setId(String id) throws RemoteException
  {
    this.setID(id);
    setAttribute("id", id);
  }

  @Override
  public String getBezeichung() throws RemoteException {
    return (String) getAttribute("bezeichnung");
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException {
    setAttribute("bezeichnung", bezeichnung);
  }

  @Override
  public Date getDatumVon() throws RemoteException
  {
    return (Date) getAttribute("datum_von");
  }

  @Override
  public void setDatumVon(Date date) throws RemoteException
  {
    setAttribute("datum_von", date);
  }

  @Override
  public Date getDatumBis() throws RemoteException
  {
    return (Date) getAttribute("datum_bis");
  }

  @Override
  public void setDatumBis(Date date) throws RemoteException
  {
    setAttribute("datum_bis", date);
  }

  @Override
  public Object getAttribute(String s) throws RemoteException
  {
    DBService service = Einstellungen.getDBService();

    String sqlSoll = "SELECT wirtschaftsplan.id, SUM(wirtschaftsplanitem.soll) " +
        "FROM wirtschaftsplan, wirtschaftsplanitem, buchungsart " +
        "WHERE wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan " +
        "AND wirtschaftsplanitem.buchungsart = buchungsart.id " +
        "AND buchungsart.art = ? " +
        "AND wirtschaftsplan.id = ? " +
        "GROUP BY wirtschaftsplan.id";

    String sqlIst = "SELECT wirtschaftsplan.id, SUM(buchung.betrag) AS ist " +
        "FROM wirtschaftsplan, buchungsart, buchung, konto " +
        "WHERE buchung.buchungsart = buchungsart.id " +
        "AND buchung.konto = konto.id " +
        "AND buchung.datum >= wirtschaftsplan.datum_von " +
        "AND buchung.datum <= wirtschaftsplan.datum_bis " +
        "AND buchungsart.art = ? " +
        "AND konto.kontoart > ? " +
        "AND konto.kontoart < ? " +
        "AND wirtschaftsplan.id = ? " +
        "GROUP BY wirtschaftsplan.id";

    switch (s)
    {
      case "planEinnahme":
        return service.execute(sqlSoll, new Object[] { EINNAHME, this.getID() }, resultSet -> {
          try
          {
            resultSet.next();
            return resultSet.getDouble(BETRAG_COL);
          }
          catch (Exception e)
          {
            return 0.;
          }
        });
      case "planAusgabe":
        return service.execute(sqlSoll, new Object[] { AUSGABE, this.getID() }, resultSet -> {
          try
          {
            resultSet.next();
            return resultSet.getDouble(BETRAG_COL);
          }
          catch (Exception e)
          {
            return 0.;
          }
        });
      case "istEinnahme":
        return service.execute(sqlIst,
            new Object[] { EINNAHME, 0, Kontoart.LIMIT.getKey(), this.getID() },
            resultSet -> {
          try
          {
            resultSet.next();
            return resultSet.getDouble(BETRAG_COL);
          }
          catch (Exception e)
          {
            return 0.;
          }
        });
      case "istAusgabe":
        return service.execute(sqlIst,
            new Object[] { AUSGABE, 0, Kontoart.LIMIT.getKey(), this.getID() },
            resultSet -> {
          try
          {
            resultSet.next();
            return resultSet.getDouble(BETRAG_COL);
          }
          catch (Exception e)
          {
            return 0.;
          }
        });
      case "istRücklagenGebildet":
        return service.execute(sqlIst,
            new Object[] { EINNAHME, Kontoart.LIMIT.getKey(),
                Kontoart.LIMIT_RUECKLAGE.getKey(), this.getID() },
            resultSet -> {
              try
              {
                resultSet.next();
                return resultSet.getDouble(BETRAG_COL);
              }
              catch (Exception e)
              {
                return 0.;
              }
            });
      case "istRücklagenAufgelöst":
        return service.execute(sqlIst,
            new Object[] { AUSGABE, Kontoart.LIMIT.getKey(),
                Kontoart.LIMIT_RUECKLAGE.getKey(), this.getID() },
            resultSet -> {
              try
              {
                resultSet.next();
                return resultSet.getDouble(BETRAG_COL);
              }
              catch (Exception e)
              {
                return 0.;
              }
            });
      case "istForderungen":
        return service.execute(sqlIst,
            new Object[] { EINNAHME, Kontoart.LIMIT_RUECKLAGE.getKey(),
                Integer.MAX_VALUE, this.getID() }, resultSet -> {
              try
              {
                resultSet.next();
                return resultSet.getDouble(BETRAG_COL);
              }
              catch (Exception e)
              {
                return 0.;
              }
            });
      case "istVerbindlichkeiten":
        return service.execute(sqlIst,
            new Object[] { AUSGABE, Kontoart.LIMIT_RUECKLAGE.getKey(),
                Integer.MAX_VALUE, this.getID() }, resultSet -> {
              try
              {
                resultSet.next();
                return resultSet.getDouble(BETRAG_COL);
              }
              catch (Exception e)
              {
                return 0.;
              }
            });
      case "istPlus":
        return (Double) getAttribute("istEinnahme") + (Double) getAttribute(
            "istRücklagenGebildet") + (Double) getAttribute("istForderungen");
      case "istMinus":
        return (Double) getAttribute("istAusgabe") + (Double) getAttribute(
            "istRücklagenAufgelöst") + (Double) getAttribute(
            "istVerbindlichkeiten");
      case "planSaldo":
        return (Double) getAttribute("planEinnahme") + (Double) getAttribute("planAusgabe");
      case "istSaldo":
        return (Double) getAttribute("istEinnahme") + (Double) getAttribute(
            "istAusgabe") + (Double) getAttribute(
            "istRücklagenGebildet") + (Double) getAttribute(
            "istRücklagenAufgelöst") + (Double) getAttribute(
            "istForderungen") + (Double) getAttribute("istVerbindlichkeiten");
      case "differenz":
        return (Double) getAttribute("istSaldo") - (Double) getAttribute("planSaldo");
      default:
        return super.getAttribute(s);
    }
  }
}
