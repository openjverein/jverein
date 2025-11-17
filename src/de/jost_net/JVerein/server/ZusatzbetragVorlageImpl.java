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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragVorlageImpl extends AbstractJVereinDBObject
    implements ZusatzbetragVorlage
{

  private static final long serialVersionUID = 1L;

  public ZusatzbetragVorlageImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "zusatzbetragvorlage";
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
      if (getIntervall() == null)
      {
        throw new ApplicationException("Bitte Intervall eingeben");
      }
      if (getBuchungstext() == null || getBuchungstext().length() == 0)
      {
        throw new ApplicationException("Bitte Buchungstext eingeben");
      }
      if (getBetrag() <= 0)
      {
        throw new ApplicationException("Betrag nicht gültig");
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        if (getSteuer() != null && getBuchungsart() != null && getSteuer()
            .getBuchungsart().getArt() != getBuchungsart().getArt())
        {
          switch (getBuchungsart().getArt())
          {
            case ArtBuchungsart.AUSGABE:
              throw new ApplicationException(
                  "Umsatzsteuer statt Vorsteuer gewählt.");
            case ArtBuchungsart.EINNAHME:
              throw new ApplicationException(
                  "Vorsteuer statt Umsatzsteuer gewählt.");
            // Umbuchung ist bei Anlagebuchungen möglich,
            // Hier ist eine Vorsteuer (Kauf) und Umsatzsteuer (Verkauf) möglich
            case ArtBuchungsart.UMBUCHUNG:
              break;
          }
        }
        if (getSteuer() != null && getBuchungsart() != null
            && (getBuchungsart().getSpende()
                || getBuchungsart().getAbschreibung()))
        {
          throw new ApplicationException(
              "Bei Spenden und Abschreibungen ist keine Steuer möglich.");
        }
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Zusatzbetrag-Vorlage kann nicht gespeichert werden. Siehe system log";
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
    if (arg0.equals("buchungsart"))
    {
      return Buchungsart.class;
    }
    return null;
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
  public double getBetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("betrag");
    if (d == null)
    {
      return 0;
    }
    return d.doubleValue();
  }

  @Override
  public void setBetrag(double d) throws RemoteException
  {
    setAttribute("betrag", Double.valueOf(d));
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
  public void setBuchungsart(Buchungsart buchungsart) throws RemoteException
  {
    setAttribute("buchungsart", buchungsart);
  }

  @Override
  public Buchungsart getBuchungsart() throws RemoteException
  {
    return (Buchungsart) getAttribute("buchungsart");
  }

  @Override
  public Buchungsklasse getBuchungsklasse() throws RemoteException
  {
    Object l = (Object) super.getAttribute("buchungsklasse");
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
    if (fieldName.equals("buchungsklasse"))
    {
      return getBuchungsklasse();
    }
    if (fieldName.equals("steuer"))
    {
      return getSteuer();
    }
    return super.getAttribute(fieldName);
  }

  @Override
  public Zahlungsweg getZahlungsweg() throws RemoteException
  {
    return new Zahlungsweg((Integer) getAttribute("zahlungsweg"));
  }

  @Override
  public void setZahlungsweg(Zahlungsweg zahlungsweg) throws RemoteException
  {
    if (zahlungsweg == null)
    {
      setAttribute("zahlungsweg", Zahlungsweg.STANDARD);
    }
    else
    {
      setAttribute("zahlungsweg", zahlungsweg.getKey());
    }
  }

  @Override
  public Steuer getSteuer() throws RemoteException
  {
    Object l = (Object) super.getAttribute("steuer");
    if (l == null)
    {
      return null; // Keine Steuer zugeordnet
    }

    if (l instanceof Steuer)
    {
      return (Steuer) l;
    }

    Cache cache = Cache.get(Steuer.class, true);
    return (Steuer) cache.get(l);
  }

  @Override
  public void setSteuer(Steuer steuer) throws RemoteException
  {
    setAttribute("steuer", steuer);
  }

  @Override
  public Object getAttributeDefault(String fieldName)
  {
    switch (fieldName)
    {
      case "zahlungsweg":
        return Zahlungsweg.STANDARD;
      case "intervall":
        return IntervallZusatzzahlung.KEIN;
      case "mitgliedzahltselbst":
        return false;
      default:
        return null;
    }
  }

  @Override
  public String getObjektName()
  {
    return "Zusatzbetrag Vorlage";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Zusatzbetrag Vorlagen";
  }

  @Override
  public void setMitgliedzahltSelbst(boolean mitgliedzahltselbst)
      throws RemoteException
  {
    setAttribute("mitgliedzahltselbst", mitgliedzahltselbst);
  }

  @Override
  public boolean getMitgliedzahltSelbst() throws RemoteException
  {
    return Util.getBoolean(getAttribute("mitgliedzahltselbst"));
  }
}
