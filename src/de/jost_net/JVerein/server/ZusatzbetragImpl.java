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
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.Datum;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragImpl extends AbstractDBObject implements Zusatzbetrag
{

  private static final long serialVersionUID = 1L;

  public ZusatzbetragImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "zusatzabbuchung";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "id";
  }

  @Override
  protected void deleteCheck()
  {
    //
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getStartdatum() == null)
      {
        throw new ApplicationException("Bitte Startdatum eingeben");
      }
      if (getFaelligkeit() == null)
      {
        throw new ApplicationException("Bitte nächste Fälligkeit eingeben");
      }
      if (getIntervall() == null)
      {
        throw new ApplicationException("Bitte Intervall eingeben");
      }
      if (getBuchungstext() == null || getBuchungstext().length() == 0)
      {
        throw new ApplicationException("Bitte Buchungstext eingeben");
      }
      if (getEndedatum() != null)
      {
        if (!Datum.isImInterval(getStartdatum(), getEndedatum(),
            getIntervall()))
        {
          throw new ApplicationException("Endedatum liegt nicht im Intervall");
        }
      }
      if (getFaelligkeit().getTime() < getStartdatum().getTime())
      {
        throw new ApplicationException(
            "Das Fälligkeitsdatum darf nicht vor dem Startdatum liegen");
      }
      if (!Datum.isImInterval(getStartdatum(), getFaelligkeit(),
          getIntervall()))
      {
        throw new ApplicationException(
            "Nächste Fälligkeit liegt nicht im Intervall");
      }
      if (getBetrag() == null)
      {
        throw new ApplicationException("Bitte Betrag eingeben");
      }
      if (getZahlungsweg().getKey() == Zahlungsweg.BASISLASTSCHRIFT)
      {
        if (getMitglied().getZahlungsweg() == Zahlungsweg.VOLLZAHLER)
        {
          Mitglied m = Einstellungen.getDBService().createObject(
              MitgliedImpl.class, getMitglied().getZahlerID().toString());
          if (m.getIban().length() == 0
              || m.getMandatDatum().equals(Einstellungen.NODATE))
          {
            throw new ApplicationException(
                "Beim Vollzahler ist keine IBAN oder Mandatdatum hinterlegt.");
          }
        }
        else if (getMitglied().getIban().length() == 0
            || getMitglied().getMandatDatum().equals(Einstellungen.NODATE))
        {
          throw new ApplicationException(
              "Beim Mitglied ist keine IBAN oder Mandatdatum hinterlegt.");
        }
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Zusatzbetrag kann nicht gespeichert werden. Siehe system log";
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
  public Mitglied getMitglied() throws RemoteException
  {
    Object o = super.getAttribute("mitglied");
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
  public void setMitglied(int mitglied) throws RemoteException
  {
    setAttribute("mitglied", Integer.valueOf(mitglied));
  }

  @Override
  public Date getFaelligkeit() throws RemoteException
  {
    return (Date) getAttribute("faelligkeit");
  }

  @Override
  public void setFaelligkeit(Date faelligkeit) throws RemoteException
  {
    setAttribute("faelligkeit", faelligkeit);
  }

  @Override
  public String getBuchungstext() throws RemoteException
  {
    return (String) getAttribute("buchungstext");
  }

  @Override
  public void setBuchungstext(String buchungstext) throws RemoteException
  {
    setAttribute("buchungstext", buchungstext);
  }

  @Override
  public Double getBetrag() throws RemoteException
  {
    return (Double) getAttribute("betrag");
  }

  @Override
  public void setBetrag(Double d) throws RemoteException
  {
    setAttribute("betrag", d);
  }

  @Override
  public Date getAusfuehrung() throws RemoteException
  {
    return (Date) getAttribute("ausfuehrung");
  }

  @Override
  public Date getStartdatum() throws RemoteException
  {
    return (Date) getAttribute("startdatum");
  }

  @Override
  public void setStartdatum(Date value) throws RemoteException
  {
    setAttribute("startdatum", value);
  }

  @Override
  public Integer getIntervall() throws RemoteException
  {
    return (Integer) getAttribute("intervall");
  }

  @Override
  public String getIntervallText() throws RemoteException
  {
    return IntervallZusatzzahlung.get(getIntervall());
  }

  @Override
  public void setIntervall(Integer value) throws RemoteException
  {
    setAttribute("intervall", value);
  }

  @Override
  public Date getEndedatum() throws RemoteException
  {
    return (Date) getAttribute("endedatum");
  }

  @Override
  public void setEndedatum(Date value) throws RemoteException
  {
    setAttribute("endedatum", value);
  }

  @Override
  public void setAusfuehrung(Date ausfuehrung) throws RemoteException
  {
    setAttribute("ausfuehrung", ausfuehrung);
  }

  @Override
  public void setBuchungsart(Buchungsart buchungsart) throws RemoteException
  {
    setAttribute("buchungsart", buchungsart);
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    Object o = super.getAttribute("buchungsart");
    if (o == null)
    {
      return null;
    }

    if (o instanceof Buchungsart)
    {
      return (Buchungsart) o;
    }

    Cache cache = Cache.get(Buchungsart.class, true);
    return (Buchungsart) cache.get(o);
  }

  @Override
  public Long getBuchungsartId() throws RemoteException
  {
    return (Long) super.getAttribute("buchungsart");
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
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("intervalltext"))
    {
      return getIntervallText();
    }
    if (fieldName.equals("mitglied"))
    {
      return getMitglied();
    }
    if (fieldName.equals("buchungsart"))
    {
      return getBuchungsart();
    }
    if (fieldName.equals("buchungsklasse"))
    {
      return getBuchungsklasse();
    }
    return super.getAttribute(fieldName);
  }

  /*
   * 
   */
  @Override
  public boolean isOffen(Date datum) throws RemoteException
  {
    if (!getMitglied().isAngemeldet(datum))
    {
      if (!Einstellungen.getEinstellung().getZusatzbetragAusgetretene())
      {
        return false;
      }
    }
    // Einmalige Ausführung
    if (getIntervall().intValue() == IntervallZusatzzahlung.KEIN)
    {
      return (getAusfuehrung() == null);
    }

    // Wenn das Endedatum gesetzt ist und das Fälligkeitsdatum liegt zum oder
    // hinter
    // dem Endedatum: nicht mehr ausführen
    if (getEndedatum() != null
        && getFaelligkeit().getTime() >= getEndedatum().getTime())
    {
      return false;
    }
    return true;
  }

  @Override
  public boolean isAktiv(Date datum) throws RemoteException
  {
    if (!getMitglied().isAngemeldet(datum))
    {
      if (!Einstellungen.getEinstellung().getZusatzbetragAusgetretene())
      {
        return false;
      }
    }
    // Einmalige Ausführung
    if (getIntervall().intValue() == IntervallZusatzzahlung.KEIN)
    {
      // Ist das Ausführungsdatum gesetzt?
      if (getAusfuehrung() == null)
      {
        if (getFaelligkeit().getTime() <= datum.getTime())
        {
          return true;
        }
        else
        {
          return false;
        }
      }
      else
      {
        // ja: nicht mehr ausführen
        return false;
      }
    }

    // Wenn das Endedatum gesetzt ist und das Ausführungsdatum liegt hinter
    // dem Endedatum: nicht mehr ausführen
    if ((getEndedatum() != null && datum.getTime() >= getEndedatum().getTime())
        || getFaelligkeit().getTime() > datum.getTime())
    {
      return false;
    }
    return true;
  }

  @Override
  public void naechsteFaelligkeit() throws RemoteException
  {
    Date vorh = Datum.addInterval(getFaelligkeit(), getIntervall());
    if (vorh == null)
    {
      throw new RemoteException("Datum kann nicht weiter vorgesetzt werden");
    }
    else
    {
      setFaelligkeit(vorh);
    }

  }

  @Override
  public void vorherigeFaelligkeit() throws RemoteException
  {
    Date vorh = Datum.subtractInterval(getFaelligkeit(), getIntervall(),
        getStartdatum());
    if (vorh == null)
    {
      throw new RemoteException("Datum kann nicht weiter zurückgesetzt werden");
    }
    else
    {
      setFaelligkeit(vorh);
    }
  }

  @Override
  public Zahlungsweg getZahlungsweg() throws RemoteException
  {
    Object o = getAttribute("zahlungsweg");
    if (o == null)
    {
      return new Zahlungsweg(0);
    }
    return new Zahlungsweg((Integer) o);
  }

  @Override
  public void setZahlungsweg(Zahlungsweg zahlungsweg) throws RemoteException
  {
    if (zahlungsweg == null)
    {
      setAttribute("zahlungsweg", 0);
    }
    else
    {
      setAttribute("zahlungsweg", zahlungsweg.getKey());
    }
  }
}
