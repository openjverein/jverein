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
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedskontoImpl extends AbstractDBObject implements
    Mitgliedskonto
{

  private static final long serialVersionUID = -1234L;

  private Double ist = null;

  public MitgliedskontoImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "mitgliedskonto";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "mitglied";
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
    return null;
  }

  @Override
  public Abrechnungslauf getAbrechnungslauf() throws RemoteException
  {
	Object o = (Object) super.getAttribute("abrechnungslauf");
    if (o == null)
      return null;
    
    if(o instanceof Abrechnungslauf)
      return (Abrechnungslauf)o;
   
    Cache cache = Cache.get(Abrechnungslauf.class,true);
    return (Abrechnungslauf) cache.get(o);
  }

  @Override
  public void setAbrechnungslauf(Abrechnungslauf abrechnungslauf)
      throws RemoteException
  {
    setAttribute("abrechnungslauf", Integer.valueOf(abrechnungslauf.getID()));
  }
  
  @Override
  public Rechnung getRechnung() throws RemoteException
  {
    Object o = (Object) super.getAttribute("rechnung");
    if (o == null)
      return null;
    
    if(o instanceof Rechnung)
      return (Rechnung)o;
   
    Cache cache = Cache.get(Rechnung.class,true);
    return (Rechnung) cache.get(o);
  }

  @Override
  public void setRechnung(Rechnung rechnung)
      throws RemoteException
  {
    if(rechnung != null)
      setAttribute("rechnung", Long.valueOf(rechnung.getID()));
    else
      setAttribute("rechnung", null);
  }
  
  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Long l = (Long) super.getAttribute("buchungsart");
    if (l == null)
    {
      return null; // Keine Buchungsart zugeordnet
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(l);
  }

  @Override
  public void setBuchungsart(Buchungsart buchungsart) throws RemoteException
  {
    if (buchungsart != null)
      setAttribute("buchungsart", Long.valueOf(buchungsart.getID()));
    else
      setAttribute("buchungsart", null);
  }
  
  @Override
  public Long getBuchungsartId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsart");
  }
  
  @Override
  public void setBuchungsartId(Long buchungsartId) throws RemoteException
  {
    setAttribute("buchungsart", buchungsartId);
  }
  
  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Long l = (Long) super.getAttribute("buchungsklasse");
    if (l == null)
    {
      return null; // Keine Buchungsklasse zugeordnet
    }

    Cache cache = Cache.get(Buchungsklasse.class, true);
    return (Buchungsklasse) cache.get(l);
  }

  @Override
  public Long getBuchungsklasseId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsklasse");
  }
  
  @Override
  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException
  {
    setAttribute("buchungsklasse", buchungsklasseId);
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
	Object o = (Object) super.getAttribute("mitglied");
    if (o == null)
      return null;

    if(o instanceof Mitglied)
      return (Mitglied)o;
   
    Cache cache = Cache.get(Mitglied.class,true);
    return (Mitglied) cache.get(o);
  }
  
  public String getMitgliedId() throws RemoteException
  {
    return String.valueOf(super.getAttribute("mitglied"));
  }

  @Override
  public void setMitglied(Mitglied mitglied) throws RemoteException
  {
    setAttribute("mitglied", Integer.valueOf(mitglied.getID()));
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }

  @Override
  public void setDatum(Date datum) throws RemoteException
  {
    setAttribute("datum", datum);
  }

  @Override
  public String getZweck1() throws RemoteException
  {
    return (String) getAttribute("zweck1");
  }

  @Override
  public void setZweck1(String zweck1) throws RemoteException
  {
    setAttribute("zweck1", zweck1);
  }

  @Override
  public Integer getZahlungsweg() throws RemoteException
  {
    return (Integer) getAttribute("zahlungsweg");
  }

  @Override
  public void setZahlungsweg(Integer zahlungsweg) throws RemoteException
  {
    setAttribute("zahlungsweg", zahlungsweg);
  }

  @Override
  public Double getNettobetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("nettobetrag");
    if (d == null)
    {
      return 0.0d;
    }
    return d;
  }

  @Override
  public void setNettobetrag(Double d) throws RemoteException
  {
    setAttribute("nettobetrag", d);
  }

  @Override
  public Double getSteuersatz() throws RemoteException
  {
    Double d = (Double) getAttribute("steuersatz");
    if (d == null)
    {
      return 0.0d;
    }
    return d;
  }

  @Override
  public void setSteuersatz(Double d) throws RemoteException
  {
    setAttribute("steuersatz", d);
  }

  @Override
  public Double getSteuerbetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("steuerbetrag");
    if (d == null)
    {
      return 0.0d;
    }
    return d;
  }

  @Override
  public void setSteuerbetrag(Double d) throws RemoteException
  {
    setAttribute("steuerbetrag", d);
  }

  @Override
  public Double getBetrag() throws RemoteException
  {
    return (Double) super.getAttribute("betrag");
  }

  @Override
  public void setBetrag(Double d) throws RemoteException
  {
    setAttribute("betrag", d);
  }

  @Override
  public Double getIstSumme() throws RemoteException
  {
    if (ist != null)
    {
      return ist;
    }
    DBService service = Einstellungen.getDBService();
    String sql = "select sum(betrag) from buchung where mitgliedskonto = "
        + this.getID();

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
    if (fieldName.equals("istsumme"))
    {
      return getIstSumme();
    }
    if (fieldName.equals("mitglied"))
    {
      return getMitglied();
    }
    if (fieldName.equals("abrechnungslauf"))
    {
      return getAbrechnungslauf();
    }
    if (fieldName.equals("buchungsklasse"))
    {
      return getBuchungsklasse();
    }
    if (fieldName.equals("buchungsart"))
    {
      return getBuchungsart();
    }
    return super.getAttribute(fieldName);
  }
}
