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
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsartImpl extends AbstractDBObject implements Buchungsart
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
  protected void deleteCheck()
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
      if (getSteuersatz() > 0 && getSteuerBuchungsart() == null)
      {
        throw new ApplicationException("Bitte Steuer Buchungsart auswählen.");
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
  public double getSteuersatz() throws RemoteException
  {
    Double i = (Double) getAttribute("steuersatz");
    if (i == null)
    {
      return 0;
    }
    return i.doubleValue();
  }

  @Override
  public void setSteuersatz(double steuersatz) throws RemoteException
  {
    setAttribute("steuersatz", steuersatz);
  }

  @Override
  public Buchungsart getSteuerBuchungsart() throws RemoteException
  {
    Long id = (Long) getAttribute("steuer_buchungsart");
    if (id == null) {
      return null;
    }
    else {
      DBIterator<Buchungsart> steuer_buchungsart = Einstellungen.getDBService()
        .createList(Buchungsart.class);
        steuer_buchungsart.addFilter("ID = " + id);
      return steuer_buchungsart.next();
    }
  }

  @Override
  public void setSteuerBuchungsart(Long steuer_buchungsart) throws RemoteException
  {
    setAttribute("steuer_buchungsart", steuer_buchungsart);
  }
  
  @Override
  public Boolean getAbschreibung() throws RemoteException
  {
    return Util.getBoolean(getAttribute("abschreibung"));
  }

  @Override
  public void setAbschreibung(Boolean abschreibung) throws RemoteException
  {
    setAttribute("abschreibung",abschreibung);
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
    else if (fieldName.equals("steuerbuchungsart"))
    {
      return getSteuerBuchungsart();
    }
    else if (fieldName.equals("buchungsklasse"))
    {
      return getBuchungsklasse();
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
