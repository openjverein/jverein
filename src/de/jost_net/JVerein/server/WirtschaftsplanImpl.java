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

import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Wirtschaftsplan;
import de.willuhn.util.ApplicationException;

public class WirtschaftsplanImpl extends AbstractJVereinDBObject
    implements Wirtschaftsplan
{
  public final static int EINNAHME = 0;

  public final static int AUSGABE = 1;

  private static final long serialVersionUID = 1L;

  private final static String BETRAG_ALIAS = "betrag";

  private final static String ID_ALIAS = "id";

  private static final String BUCHUNGSART_ART = "buchungsart_art";

  private static final String KONTOART_GRUPPE = "kontoart_gruppe";

  private Double planEinnahme;

  private Double planAusgabe;

  private Double istEinnahme;

  private Double istRuecklagenGebildet;

  private Double istForderungen;

  private Double istAusgabe;

  private Double istRuecklagenAufgeloest;

  private Double istVerbindlichkeiten;

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
  public double getPlanEinnahme() throws RemoteException
  {
    if (planEinnahme == null)
    {
      loadSoll();
    }
    return planEinnahme == null ? 0d : planEinnahme;
  }

  @Override
  public double getPlanAusgabe() throws RemoteException
  {
    if (planAusgabe == null)
    {
      loadSoll();
    }
    return planAusgabe == null ? 0d : planAusgabe;
  }

  private void loadSoll() throws RemoteException
  {
    planAusgabe = 0d;
    planEinnahme = 0d;

    ExtendedDBIterator<PseudoDBObject> sollIterator = new ExtendedDBIterator<>(
        "wirtschaftsplan, wirtschaftsplanitem, buchungsart");
    sollIterator.addColumn("wirtschaftsplan.id as " + ID_ALIAS);
    sollIterator.addColumn("SUM(wirtschaftsplanitem.soll) as " + BETRAG_ALIAS);
    sollIterator.addColumn("buchungsart.art " + BUCHUNGSART_ART);
    sollIterator
        .addFilter("wirtschaftsplan.id = wirtschaftsplanitem.wirtschaftsplan");
    sollIterator.addFilter("wirtschaftsplanitem.buchungsart = buchungsart.id");
    sollIterator.addFilter("wirtschaftsplan.id = ?", this.getID());
    sollIterator.addGroupBy("wirtschaftsplan.id");
    sollIterator.addGroupBy("buchungsart.art");

    while (sollIterator.hasNext())
    {
      PseudoDBObject o = sollIterator.next();
      switch ((Integer) o.getAttribute(BUCHUNGSART_ART))
      {
        case ArtBuchungsart.AUSGABE:
          planAusgabe = o.getDouble(BETRAG_ALIAS);
          break;
        case ArtBuchungsart.EINNAHME:
          planEinnahme = o.getDouble(BETRAG_ALIAS);
          break;
        default:
      }
    }
  }

  @Override
  public double getIstEinnahme() throws RemoteException
  {
    if (istEinnahme == null)
    {
      loadIst();
    }
    return istEinnahme == null ? 0d : istEinnahme;
  }

  @Override
  public double getIstRuecklagenGebildet() throws RemoteException
  {
    if (istRuecklagenGebildet == null)
    {
      loadIst();
    }
    return istRuecklagenGebildet == null ? 0d : istRuecklagenGebildet;
  }

  @Override
  public double getIstForderungen() throws RemoteException
  {
    if (istForderungen == null)
    {
      loadIst();
    }
    return istForderungen == null ? 0d : istForderungen;
  }

  @Override
  public double getIstAusgabe() throws RemoteException
  {
    if (istAusgabe == null)
    {
      loadIst();
    }
    return istAusgabe == null ? 0d : istAusgabe;
  }

  @Override
  public double getIstRuecklagenAufgeloest() throws RemoteException
  {
    if (istRuecklagenAufgeloest == null)
    {
      loadIst();
    }
    return istRuecklagenAufgeloest == null ? 0d : istRuecklagenAufgeloest;
  }

  @Override
  public double getIstVerbindlichkeiten() throws RemoteException
  {
    if (istVerbindlichkeiten == null)
    {
      loadIst();
    }
    return istVerbindlichkeiten == null ? 0d : istVerbindlichkeiten;
  }

  private void loadIst() throws RemoteException
  {
    istEinnahme = 0d;
    istRuecklagenGebildet = 0d;
    istForderungen = 0d;
    istAusgabe = 0d;
    istRuecklagenAufgeloest = 0d;
    istVerbindlichkeiten = 0d;

    ExtendedDBIterator<PseudoDBObject> istIterator = new ExtendedDBIterator<>(
        "wirtschaftsplan, buchungsart, buchung, konto");
    istIterator.addColumn("wirtschaftsplan.id as " + ID_ALIAS);
    istIterator.addColumn("SUM(buchung.betrag) as " + BETRAG_ALIAS);
    istIterator.addColumn("buchungsart.art " + BUCHUNGSART_ART);
    istIterator.addColumn(
        "case when konto.kontoart < ? then 1 when konto.kontoart > ? then 3 else 2 end as "
            + KONTOART_GRUPPE,
        Kontoart.LIMIT.getKey(), Kontoart.LIMIT_RUECKLAGE.getKey());
    istIterator.addFilter("buchung.buchungsart = buchungsart.id");
    istIterator.addFilter("buchung.konto = konto.id");
    istIterator.addFilter("buchung.datum >= wirtschaftsplan.datum_von");
    istIterator.addFilter("buchung.datum <= wirtschaftsplan.datum_bis");
    istIterator.addFilter("wirtschaftsplan.id = ?", this.getID());
    istIterator.addGroupBy("wirtschaftsplan.id");
    istIterator.addGroupBy("buchungsart.art");
    istIterator.addGroupBy(KONTOART_GRUPPE);

    while (istIterator.hasNext())
    {
      PseudoDBObject o = istIterator.next();
      switch ((Integer) o.getAttribute(BUCHUNGSART_ART))
      {
        case ArtBuchungsart.EINNAHME:
          switch ((Integer) o.getAttribute(KONTOART_GRUPPE))
          {
            // Unter LIMIT
            case 1:
              istEinnahme = o.getDouble(BETRAG_ALIAS);
              break;
            // Zwischen LIMIT und LIMIT_RUECKLAGE
            case 2:
              istRuecklagenGebildet = o.getDouble(BETRAG_ALIAS);
              break;
            // Über LIMIT_Rücklage
            case 3:
              istForderungen = o.getDouble(BETRAG_ALIAS);
              break;
            default:
          }
          break;
        case ArtBuchungsart.AUSGABE:
          switch ((Integer) o.getAttribute(KONTOART_GRUPPE))
          {
            // Unter LIMIT
            case 1:
              istAusgabe = o.getDouble(BETRAG_ALIAS);
              break;
            // Zwischen LIMIT und LIMIT_RUECKLAGE
            case 2:
              istRuecklagenAufgeloest = o.getDouble(BETRAG_ALIAS);
              break;
            // Über LIMIT_Rücklage
            case 3:
              istVerbindlichkeiten = o.getDouble(BETRAG_ALIAS);
              break;
            default:
          }
          break;
        default:
      }
    }
  }

  @Override
  public Object getAttribute(String s) throws RemoteException
  {
    switch (s)
    {
      case "planEinnahme":
        return getPlanEinnahme();
      case "planAusgabe":
        return getPlanAusgabe();
      case "istEinnahme":
        return getIstEinnahme();
      case "istAusgabe":
        return getIstAusgabe();
      case "istRücklagenGebildet":
        return getIstRuecklagenGebildet();
      case "istRücklagenAufgelöst":
        return getIstRuecklagenAufgeloest();
      case "istForderungen":
        return getIstForderungen();
      case "istVerbindlichkeiten":
        return getIstVerbindlichkeiten();
      case "istPlus":
        return getIstEinnahme() + getIstForderungen();
      case "istMinus":
        return getIstAusgabe() + getIstVerbindlichkeiten();
      case "planSaldo":
        return getPlanEinnahme() + getPlanAusgabe();
      case "istSaldo":
        return getIstEinnahme() + (getIstAusgabe() + getIstForderungen())
            + getIstVerbindlichkeiten();
      case "differenz":
        return (Double) getAttribute("istSaldo")
            - (Double) getAttribute("planSaldo");
      default:
        return super.getAttribute(s);
    }
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    super.store();

    // Werte zurücksetzen, damit sie neu berechnet werden
    planAusgabe = null;
    planEinnahme = null;
    istEinnahme = null;
    istRuecklagenGebildet = null;
    istForderungen = null;
    istAusgabe = null;
    istRuecklagenAufgeloest = null;
    istVerbindlichkeiten = null;
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
