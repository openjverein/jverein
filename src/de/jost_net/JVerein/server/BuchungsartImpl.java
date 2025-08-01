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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsartImpl extends AbstractJVereinDBObject
    implements Buchungsart
{

  private static final long serialVersionUID = 500102542884220658L;

  public BuchungsartImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "buchungsart";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "bezeichnung";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    //
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().length() == 0)
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben");
      }
      if (getNummer() < 0)
      {
        throw new ApplicationException("Nummer nicht gültig");
      }
      DBIterator<Buchungsart> artIt = Einstellungen.getDBService()
          .createList(Buchungsart.class);
      while (artIt.hasNext())
      {
        Buchungsart test = artIt.next();
        if (test.getNummer() == getNummer())
        {
          if (!test.getID().equalsIgnoreCase(this.getID()))
          {
            throw new ApplicationException("Bitte eindeutige Nummer eingeben!");
          }
        }
      }
      if (getSteuer() != null
          && getSteuer().getBuchungsart().getArt() != getArt())
      {
        switch (getArt())
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
      if (getSteuer() != null && (getSpende() || getAbschreibung()))
      {
        throw new ApplicationException(
            "Bei Spenden und Abschreibungen ist keine Steuer möglich.");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Buchungsart kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
    try
    {
      if (hasChanged("steuer")
          && !(Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {

        // Prüfen ob es abgeschlossene Buchungen mit der Buchungsart gibt
        ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
            "buchung");
        it.addColumn("buchung.id");
        it.setLimit(1);

        it.join("jahresabschluss",
            "jahresabschluss.von <= buchung.datum and jahresabschluss.bis >= buchung.datum");

        it.join("buchungsart", "buchungsart.id = buchung.buchungsart");
        it.addFilter("buchungsart.id = ?", getID());
        if (it.hasNext())
        {
          throw new ApplicationException(
              "Steuer kann nicht geändert werden, es gibt abgeschlossene Buchungen mit dieser Buchungsart.");
        }

        // Prüfen ob es eine Rechnung mit dieser Buchungsart gibt
        it = new ExtendedDBIterator<>(Sollbuchung.TABLE_NAME);
        it.addColumn(Sollbuchung.TABLE_NAME_ID);
        it.setLimit(1);

        it.join("sollbuchungposition",
            "sollbuchungposition.sollbuchung = " + Sollbuchung.TABLE_NAME_ID);
        it.addFilter("rechnung is not null");
        it.join("buchungsart",
            "buchungsart.id = sollbuchungposition.buchungsart");
        it.addFilter("buchungsart.id = ?", getID());
        if (it.hasNext())
        {
          throw new ApplicationException(
              "Steuer kann nicht geändert werden, es existieren Rechnungen mit dieser Buchungsart.");
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Update Check", e);
    }
  }

  @Override
  protected Class<?> getForeignObject(String arg0)
  {
    return null;
  }

  @Override
  public Steuer getSteuer() throws RemoteException
  {
    Object l = (Object) super.getAttribute("steuer");
    if (l == null)
    {
      return null;
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
    super.setAttribute("steuer", steuer);
  }

  @Override
  public void setSteuerId(Integer id) throws RemoteException
  {
    super.setAttribute("steuer", id);
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
  public int getNummer() throws RemoteException
  {
    Integer i = (Integer) getAttribute("nummer");
    if (i == null)
      return 0;
    return i.intValue();
  }

  @Override
  public void setNummer(int i) throws RemoteException
  {
    setAttribute("nummer", Integer.valueOf(i));
  }

  @Override
  public int getArt() throws RemoteException
  {
    Integer i = (Integer) getAttribute("art");
    if (i == null)
    {
      return 0;
    }
    return i.intValue();
  }

  @Override
  public void setArt(int art) throws RemoteException
  {
    setAttribute("art", art);
  }

  @Override
  public int getStatus() throws RemoteException
  {
    Integer i = (Integer) getAttribute("status");
    if (i == null)
    {
      return 0;
    }
    return i.intValue();
  }

  @Override
  public void setStatus(int status) throws RemoteException
  {
    setAttribute("status", status);
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
  public Boolean getSpende() throws RemoteException
  {
    return Util.getBoolean(getAttribute("spende"));
  }

  @Override
  public void setSpende(Boolean spende) throws RemoteException
  {
    setAttribute("spende", Boolean.valueOf(spende));
  }

  @Override
  public String getSuchbegriff() throws RemoteException
  {
    String s = (String) getAttribute("suchbegriff");
    if (s == null)
    {
      return "";
    }
    return s;
  }

  @Override
  public void setRegexp(boolean regexp) throws RemoteException
  {
    setAttribute("regularexp", regexp);
  }

  @Override
  public boolean getRegexp() throws RemoteException
  {
    Boolean b = (Boolean) getAttribute("regularexp");
    if (b == null)
    {
      return false;
    }
    return b;
  }

  @Override
  public void setSuchbegriff(String suchbegriff) throws RemoteException
  {
    setAttribute("suchbegriff", suchbegriff);
  }

  @Override
  public Boolean getAbschreibung() throws RemoteException
  {
    return Util.getBoolean(getAttribute("abschreibung"));
  }

  @Override
  public void setAbschreibung(Boolean abschreibung) throws RemoteException
  {
    setAttribute("abschreibung", abschreibung);
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if (fieldName.equals("nrbezeichnung"))
    {
      int nr = getNummer();
      if (nr >= 0)
      {
        return nr + " - " + getBezeichnung();
      }
      else
      {
        return getBezeichnung();
      }
    }
    else if (fieldName.equals("bezeichnungnr"))
    {
      int nr = getNummer();
      if (nr >= 0)
      {
        return getBezeichnung() + " (" + nr + ")";
      }
      else
      {
        return getBezeichnung();
      }
    }
    else if (fieldName.equals("klasse-art-bez"))
    {
      Buchungsklasse klasse = getBuchungsklasse();
      StringBuilder stb = new StringBuilder(80);
      if (null != klasse)
        stb.append(klasse.getBezeichnung());
      stb.append(getArtCode());
      stb.append(getNummer());
      stb.append(" ");
      stb.append(getBezeichnung());
      return stb.toString();
    }
    else if (fieldName.equals("buchungsklasse"))
    {
      return getBuchungsklasse();
    }
    else if (fieldName.equals("steuer"))
    {
      return getSteuer();
    }
    else
    {
      return super.getAttribute(fieldName);
    }
  }

  private String getArtCode() throws RemoteException
  {
    switch (getArt())
    {
      case 0:
        return " + ";
      case 1:
        return " - ";
      case 2:
        return " T ";
      default:
        return "   ";
    }
  }

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    super.delete();
    Cache.get(Buchungsart.class, false).remove(this); // Aus Cache loeschen
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    Cache.get(Buchungsart.class, false).put(this); // Cache aktualisieren
  }

  @Override
  public boolean equals(Object bart)
  {
    try
    {
      if (this.getID().equalsIgnoreCase(((Buchungsart) bart).getID()))
        return true;
      else
        return false;
    }
    catch (RemoteException e)
    {
      // Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }
}
