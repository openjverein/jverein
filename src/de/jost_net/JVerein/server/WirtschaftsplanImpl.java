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

import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.datasource.rmi.DBService;

public class WirtschaftsplanImpl extends AbstractJVereinDBObject
    implements Wirtschaftsplan
{
  public final static int EINNAHME = 0;

  public final static int AUSGABE = 1;

  private static final long serialVersionUID = 1L;

  private final static String BETRAG_ALIAS = "betrag";

  private final static String ID_ALIAS = "id";

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
  public String getBezeichung() throws RemoteException
  {
    return (String) getAttribute("bezeichnung");
  }

  @Override
  public void setBezeichnung(String bezeichnung) throws RemoteException
  {
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

    ExtendedDBIterator<PseudoDBObject> sollIterator = new ExtendedDBIterator<>(
        "wirtschaftsplan, wirtschaftsplanitem, buchungsart");
    sollIterator.addColumn("wirtschaftsplan.id as " + ID_ALIAS);
    sollIterator.addColumn("SUM(wirtschaftsplanitem.soll) as " + BETRAG_ALIAS);
    sollIterator
        .addFilter("wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan");
    sollIterator.addFilter("wirtschaftsplanitem.buchungsart = buchungsart.id");
    sollIterator.addFilter("wirtschaftsplan.id = ?", this.getID());
    sollIterator.addGroupBy("wirtschaftsplan.id");

    ExtendedDBIterator<PseudoDBObject> istIterator = new ExtendedDBIterator<>(
        "wirtschaftsplan, buchungsart, buchung, konto");
    istIterator.addColumn("wirtschaftsplan.id as " + ID_ALIAS);
    istIterator.addColumn("SUM(buchung.betrag) as " + BETRAG_ALIAS);
    istIterator.addFilter("buchung.buchungsart = buchungsart.id");
    istIterator.addFilter("buchung.konto = konto.id");
    istIterator.addFilter("buchung.datum >= wirtschaftsplan.datum_von");
    istIterator.addFilter("buchung.datum <= wirtschaftsplan.datum_bis");
    istIterator.addFilter("wirtschaftsplan.id = ?", this.getID());
    istIterator.addGroupBy("wirtschaftsplan.id");

    switch (s)
    {
      case "planEinnahme":
        sollIterator.addFilter("buchungsart.art = ?", EINNAHME);
        if (sollIterator.hasNext())
        {
          return sollIterator.next().getDouble(BETRAG_ALIAS);
        }
        else
        {
          return 0.;
        }
      case "planAusgabe":
        sollIterator.addFilter("buchungsart.art = ?", AUSGABE);
        if (sollIterator.hasNext())
        {
          return sollIterator.next().getDouble(BETRAG_ALIAS);
        }
        else
        {
          return 0.;
        }
      case "istEinnahme":
        istIterator.addFilter("konto.kontoart > ?", 0);
        istIterator.addFilter("konto.kontoart < ?", Kontoart.LIMIT.getKey());
        istIterator.addFilter("buchungsart.art = ?", EINNAHME);
        if (!istIterator.hasNext())
        {
          return 0.;
        }

        return istIterator.next().getDouble(BETRAG_ALIAS);
      case "istAusgabe":
        istIterator.addFilter("konto.kontoart > ?", 0);
        istIterator.addFilter("konto.kontoart < ?", Kontoart.LIMIT.getKey());
        istIterator.addFilter("buchungsart.art = ?", AUSGABE);
        if (!istIterator.hasNext())
        {
          return 0.;
        }

        return istIterator.next().getDouble(BETRAG_ALIAS);
      case "istRücklagenGebildet":
        istIterator.addFilter("konto.kontoart >= ?", Kontoart.LIMIT.getKey());
        istIterator.addFilter("konto.kontoart < ?",
            Kontoart.LIMIT_RUECKLAGE.getKey());
        istIterator.addFilter("buchungsart.art = ?", EINNAHME);
        if (!istIterator.hasNext())
        {
          return 0.;
        }

        return istIterator.next().getDouble(BETRAG_ALIAS);
      case "istRücklagenAufgelöst":
        istIterator.addFilter("konto.kontoart >= ?", Kontoart.LIMIT.getKey());
        istIterator.addFilter("konto.kontoart < ?",
            Kontoart.LIMIT_RUECKLAGE.getKey());
        istIterator.addFilter("buchungsart.art = ?", AUSGABE);
        if (!istIterator.hasNext())
        {
          return 0.;
        }

        return istIterator.next().getDouble(BETRAG_ALIAS);
      case "istForderungen":
        istIterator.addFilter("konto.kontoart >= ?",
            Kontoart.LIMIT_RUECKLAGE.getKey());
        istIterator.addFilter("buchungsart.art = ?", EINNAHME);
        if (!istIterator.hasNext())
        {
          return 0.;
        }

        return istIterator.next().getDouble(BETRAG_ALIAS);
      case "istVerbindlichkeiten":
        istIterator.addFilter("konto.kontoart >= ?",
            Kontoart.LIMIT_RUECKLAGE.getKey());
        istIterator.addFilter("buchungsart.art = ?", AUSGABE);
        if (!istIterator.hasNext())
        {
          return 0.;
        }

        return istIterator.next().getDouble(BETRAG_ALIAS);
      case "istPlus":
        return (Double) getAttribute("istEinnahme")
            + (Double) getAttribute("istForderungen");
      case "istMinus":
        return (Double) getAttribute("istAusgabe")
            + (Double) getAttribute("istVerbindlichkeiten");
      case "planSaldo":
        return (Double) getAttribute("planEinnahme")
            + (Double) getAttribute("planAusgabe");
      case "istSaldo":
        return (Double) getAttribute("istEinnahme")
            + (Double) getAttribute("istAusgabe")
            + (Double) getAttribute("istForderungen")
            + (Double) getAttribute("istVerbindlichkeiten");
      case "differenz":
        return (Double) getAttribute("istSaldo")
            - (Double) getAttribute("planSaldo");
      default:
        return super.getAttribute(s);
    }
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Wirtschaftsplan";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Wirtschaftspläne";
  }
}
