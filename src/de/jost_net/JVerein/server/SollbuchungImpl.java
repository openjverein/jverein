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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SollbuchungImpl extends AbstractJVereinDBObject
    implements Sollbuchung
{

  private static final long serialVersionUID = -1234L;

  private Double ist = null;

  public SollbuchungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return TABLE_NAME;
  }

  @Override
  public String getPrimaryAttribute()
  {
    return PRIMARY_ATTRIBUTE;
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      if (this.getRechnung() != null)
      {
        throw new ApplicationException(
            "Sollbuchung kann nicht gelöscht werden weil sie zu einer "
                + "Rechnung gehört");
      }
    }
    catch (ObjectNotFoundException e)
    {
      // Alles ok, es gibt keine Rechnung
      // Das passiert wenn sie kurz vorher gelöscht wurde aber
      // die ID noch im Cache gespeichert ist
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(
          "Sollbuchung kann nicht gelöscht werden. Siehe system log");
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getMitglied() == null)
      {
        throw new ApplicationException("Bitte Mitglied eingeben");
      }
      if (getDatum() == null)
      {
        throw new ApplicationException("Datum fehlt");
      }
      if (getZweck1().length() == 0)
      {
        throw new ApplicationException("Verwendungszweck fehlt");
      }
      if (getBetrag() == null)
      {
        String fehler = "Betrag fehlt";
        Logger.error(fehler);
        throw new ApplicationException(fehler);
      }

    }
    catch (RemoteException e)
    {
      String fehler = "Sollbuchung kann nicht gespeichert werden. Siehe system log";
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
    if (arg0.equals(RECHNUNG))
    {
      return Rechnung.class;
    }
    return null;
  }

  @Override
  public Abrechnungslauf getAbrechnungslauf() throws RemoteException
  {
    Object o = (Object) super.getAttribute(ABRECHNUNGSLAUF);
    if (o == null)
      return null;

    if (o instanceof Abrechnungslauf)
      return (Abrechnungslauf) o;

    Cache cache = Cache.get(Abrechnungslauf.class, true);
    return (Abrechnungslauf) cache.get(o);
  }

  @Override
  public void setAbrechnungslauf(Abrechnungslauf abrechnungslauf)
      throws RemoteException
  {
    setAttribute(ABRECHNUNGSLAUF, Integer.valueOf(abrechnungslauf.getID()));
  }

  @Override
  public Rechnung getRechnung() throws RemoteException
  {
    return (Rechnung) getAttribute(RECHNUNG);
  }

  @Override
  public void setRechnung(Rechnung rechnung) throws RemoteException
  {
    if (rechnung != null)
      setAttribute(RECHNUNG, Long.valueOf(rechnung.getID()));
    else
      setAttribute(RECHNUNG, null);
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    Object o = (Object) super.getAttribute(MITGLIED);
    if (o == null)
      return null;

    if (o instanceof Mitglied)
      return (Mitglied) o;

    Cache cache = Cache.get(Mitglied.class, true);
    return (Mitglied) cache.get(o);
  }

  public String getMitgliedId() throws RemoteException
  {
    return String.valueOf(super.getAttribute(MITGLIED));
  }

  @Override
  public void setMitglied(Mitglied mitglied) throws RemoteException
  {
    setAttribute(MITGLIED, Integer.valueOf(mitglied.getID()));
  }

  @Override
  public Mitglied getZahler() throws RemoteException
  {
    Object o = (Object) super.getAttribute(ZAHLER);
    if (o == null)
    {
      return null;
    }

    if (o instanceof Mitglied)
    {
      return (Mitglied) o;
    }

    Cache cache = Cache.get(Mitglied.class, true);
    return (Mitglied) cache.get(o);
  }

  @Override
  public void setZahler(Mitglied zahler) throws RemoteException
  {
    if (zahler != null)
    {
      setAttribute(ZAHLER, Long.valueOf(zahler.getID()));
    }
    else
    {
      setAttribute(ZAHLER, null);
    }
  }

  public Long getZahlerId() throws RemoteException
  {
    return (Long) super.getAttribute(ZAHLER);
  }

  @Override
  public void setZahlerId(Long zahlerId) throws RemoteException
  {
    setAttribute(ZAHLER, zahlerId);
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute(DATUM);
  }

  @Override
  public void setDatum(Date datum) throws RemoteException
  {
    setAttribute(DATUM, datum);
  }

  @Override
  public String getZweck1() throws RemoteException
  {
    return (String) getAttribute(ZWECK1);
  }

  @Override
  public void setZweck1(String zweck1) throws RemoteException
  {
    setAttribute(ZWECK1, zweck1);
  }

  @Override
  public Integer getZahlungsweg() throws RemoteException
  {
    return (Integer) getAttribute(ZAHLUNGSWEG);
  }

  @Override
  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException
  {
    setAttribute(ZAHLUNGSWEG, zahlungsweg);
  }

  @Override
  public Double getBetrag() throws RemoteException
  {
    return (Double) super.getAttribute(BETRAG);
  }

  @Override
  public void setBetrag(Double d) throws RemoteException
  {
    setAttribute(BETRAG, d);
  }

  @Override
  public Double getIstSumme() throws RemoteException
  {
    if (ist != null)
    {
      return ist;
    }
    DBService service = Einstellungen.getDBService();
    String sql = "select sum(betrag) from buchung where " + Buchung.SOLLBUCHUNG
        + " = " + this.getID();

    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        if (!rs.next())
        {
          return Double.valueOf(0.0d);
        }
        return Double.valueOf(rs.getDouble(1));
      }
    };
    ist = Double.valueOf((Double) service.execute(sql, new Object[] {}, rs));
    return ist;
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("id-int".equals(fieldName))
    {
      try
      {
        return Integer.valueOf(getID());
      }
      catch (Exception e)
      {
        Logger.error("unable to parse id: " + getID());
        return getID();
      }
    }
    if (fieldName.equals(ISTSUMME))
    {
      return getIstSumme();
    }
    if (fieldName.equals(MITGLIED))
    {
      return getMitglied();
    }
    if (fieldName.equals(ZAHLER))
    {
      return getZahler();
    }
    if (fieldName.equals(ABRECHNUNGSLAUF))
    {
      return getAbrechnungslauf();
    }
    return super.getAttribute(fieldName);
  }

  @Override
  public ArrayList<SollbuchungPosition> getSollbuchungPositionList()
      throws RemoteException
  {
    ArrayList<SollbuchungPosition> sps = new ArrayList<>();
    DBIterator<SollbuchungPosition> it = Einstellungen.getDBService()
        .createList(SollbuchungPosition.class);
    it.addFilter("sollbuchungposition.sollbuchung = ?", getID());
    it.setOrder("ORDER BY datum");
    while (it.hasNext())
    {
      sps.add((SollbuchungPosition) it.next());
    }
    return sps;
  }

  @Override
  public List<Buchung> getBuchungList() throws RemoteException
  {
    ArrayList<Buchung> buchungen = new ArrayList<>();
    DBIterator<Buchung> it = Einstellungen.getDBService()
        .createList(Buchung.class);
    it.addFilter(Buchung.SOLLBUCHUNG + " = ?", getID());
    it.setOrder("ORDER BY datum asc");
    while (it.hasNext())
    {
      Buchung bu = it.next();
      buchungen.add(bu);
    }
    return buchungen;
  }
}
