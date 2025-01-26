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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungImpl extends AbstractDBObject
    implements Spendenbescheinigung
{

  private static final long serialVersionUID = -1861750218155086064L;

  private List<Buchung> buchungen = null;

  public SpendenbescheinigungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "spendenbescheinigung";
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
      if (getBetrag().doubleValue() <= 0)
      {
        throw new ApplicationException("Betrag größer als 0 eingeben.");
      }
      if (getSpendedatum() == null)
      {
        throw new ApplicationException("Spendedatum fehlt.");
      }
      if (getBescheinigungsdatum() == null)
      {
        throw new ApplicationException("Datum der Bescheinigung fehlt.");
      }
      if (getZeile1() == null && getZeile2() == null && getZeile3() == null)
      {
        throw new ApplicationException("Spenderadresse fehlt");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException("Fehler bei der Plausi");
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("formular".equals(field))
    {
      return Formular.class;
    }
    if ("mitglied".equals(field))
    {
      return Mitglied.class;
    }

    return null;
  }

  @Override
  public int getSpendenart() throws RemoteException
  {
    Integer ret = (Integer) getAttribute("spendenart");
    if (ret == null)
    {
      ret = Spendenart.GELDSPENDE;
    }
    return ret;
  }

  @Override
  public void setSpendenart(int spendenart) throws RemoteException
  {
    setAttribute("spendenart", spendenart);
  }

  @Override
  public String getZeile1() throws RemoteException
  {
    return getAttribute("zeile1") == null ? ""
        : (String) getAttribute("zeile1");
  }

  @Override
  public void setZeile1(String zeile1) throws RemoteException
  {
    setAttribute("zeile1", zeile1);
  }

  @Override
  public String getZeile2() throws RemoteException
  {
    return getAttribute("zeile2") == null ? ""
        : (String) getAttribute("zeile2");
  }

  @Override
  public void setZeile2(String zeile2) throws RemoteException
  {
    setAttribute("zeile2", zeile2);
  }

  @Override
  public String getZeile3() throws RemoteException
  {
    return getAttribute("zeile3") == null ? ""
        : (String) getAttribute("zeile3");
  }

  @Override
  public void setZeile3(String zeile3) throws RemoteException
  {
    setAttribute("zeile3", zeile3);
  }

  @Override
  public String getZeile4() throws RemoteException
  {
    return getAttribute("zeile4") == null ? ""
        : (String) getAttribute("zeile4");
  }

  @Override
  public void setZeile4(String zeile4) throws RemoteException
  {
    setAttribute("zeile4", zeile4);
  }

  @Override
  public String getZeile5() throws RemoteException
  {
    return getAttribute("zeile5") == null ? ""
        : (String) getAttribute("zeile5");
  }

  @Override
  public void setZeile5(String zeile5) throws RemoteException
  {
    setAttribute("zeile5", zeile5);
  }

  @Override
  public String getZeile6() throws RemoteException
  {
    return getAttribute("zeile6") == null ? ""
        : (String) getAttribute("zeile6");
  }

  @Override
  public void setZeile6(String zeile6) throws RemoteException
  {
    setAttribute("zeile6", zeile6);
  }

  @Override
  public String getZeile7() throws RemoteException
  {
    return getAttribute("zeile7") == null ? ""
        : (String) getAttribute("zeile7");
  }

  @Override
  public void setZeile7(String zeile7) throws RemoteException
  {
    setAttribute("zeile7", zeile7);
  }

  @Override
  public Double getBetrag() throws RemoteException
  {
    Double ret = (Double) getAttribute("betrag");
    if (ret == null)
    {
      ret = Double.valueOf(0);
    }
    return ret;
  }

  @Override
  public void setSpendedatum(Date datum) throws RemoteException
  {
    setAttribute("spendedatum", datum);
  }

  @Override
  public Date getSpendedatum() throws RemoteException
  {
    return (Date) getAttribute("spendedatum");
  }

  /**
   * Liefert aus der Buchungsliste entweder das größte Datum zurück. Falls noch
   * keine Buchungen eingetragen sind, wird das Spendendatum zurückgeliefert.
   * 
   * @throws RemoteException
   */
  @Override
  public Date getZeitraumBis() throws RemoteException
  {
    Date maxDate = getSpendedatum();
    if (buchungen.size() > 0)
    {
      for (Buchung b : buchungen)
      {
        if (maxDate.before(b.getDatum()))
        {
          maxDate = b.getDatum();
        }
      }
    }
    return maxDate;
  }

  @Override
  public void setBescheinigungsdatum(Date datum) throws RemoteException
  {
    setAttribute("bescheinigungsdatum", datum);
  }

  @Override
  public Date getBescheinigungsdatum() throws RemoteException
  {
    return (Date) getAttribute("bescheinigungsdatum");
  }

  @Override
  public void setBetrag(Double betrag) throws RemoteException
  {
    setAttribute("betrag", betrag);
  }

  @Override
  public Formular getFormular() throws RemoteException
  {
    return (Formular) getAttribute("formular");
  }

  @Override
  public void setFormular(Formular formular) throws RemoteException
  {
    setAttribute("formular", formular);
  }

  @Override
  public Boolean getErsatzAufwendungen() throws RemoteException
  {
    return Util.getBoolean(getAttribute("ersatzaufwendungen"));
  }

  @Override
  public void setErsatzAufwendungen(Boolean ersatzaufwendungen)
      throws RemoteException
  {
    setAttribute("ersatzaufwendungen", Boolean.valueOf(ersatzaufwendungen));
  }

  @Override
  public String getBezeichnungSachzuwendung() throws RemoteException
  {
    return (String) getAttribute("bezeichnungsachzuwendung");
  }

  @Override
  public void setBezeichnungSachzuwendung(String bezeichnungsachzuwendung)
      throws RemoteException
  {
    setAttribute("bezeichnungsachzuwendung", bezeichnungsachzuwendung);
  }

  @Override
  public int getHerkunftSpende() throws RemoteException
  {
    Integer ret = (Integer) getAttribute("herkunftspende");
    if (ret == null)
    {
      ret = HerkunftSpende.KEINEANGABEN;
    }
    return ret;
  }

  @Override
  public void setHerkunftSpende(int herkunftspende) throws RemoteException
  {
    setAttribute("herkunftspende", herkunftspende);
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
    return super.getAttribute(fieldName);
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    super.store();
    Long id = Long.valueOf(getID());
    if (buchungen != null)
    {
      for (Buchung b : buchungen)
      {
        b.setSpendenbescheinigungId(id);
        b.store(false);
      }
    }
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    return (Mitglied) getAttribute("mitglied");
  }

  @Override
  public int getMitgliedID() throws RemoteException
  {
    return Integer.parseInt(getMitglied().getID());
  }

  @Override
  public void setMitgliedID(Integer mitgliedID) throws RemoteException
  {
    setAttribute("mitglied", mitgliedID);
  }

  @Override
  public void setMitglied(Mitglied mitglied) throws RemoteException
  {
    if (mitglied != null)
    {
      setAttribute("mitglied", Integer.valueOf(mitglied.getID()));
    }
    else
    {
      setAttribute("mitglied", null);
    }
  }

  /**
   * Liefert als Kennzeichen zurück, ob die Spendenbescheinigung eine
   * Sammelbestaetigung ist. Dies ist der Fall, wenn die Liste der Buchungen
   * mehr als eine Buchung enthält. Ist keine oder nur eine Buchung zugewiesen,
   * liegt eine Einzelbestätigung vor.
   * 
   * @return Flag, ob Sammelbestätigung
   * @throws RemoteException
   */
  @Override
  public boolean isSammelbestaetigung() throws RemoteException
  {
    if (getBuchungen() == null)
      return false;
    return getBuchungen().size() > 1;
  }
  
  /**
   * Liefert als Kennzeichen zurück, ob die Spendenbescheinigung eine echte
   * Geldspende ist. Dies ist der Fall, wenn es sich um eine Gelspende handelt
   * bei der bei keiner Buchung das Flag Erstattungsverzicht gesetzt ist.
   * 
   * @return Flag, ob echte Geldspende
   * @throws RemoteException
   */
  public boolean isEchteGeldspende() throws RemoteException
  {
    if (getBuchungen() == null)
      return false;
    for (Buchung buchung : getBuchungen())
    {
      if (buchung.getVerzicht())
        return false;
    }
    return true;
  }

  /**
   * Fügt der Liste der Buchungen eine Buchung hinzu. Der Gesamtbetrag der
   * Spendenbescheinigung wird anhand der Einzelbeträge der Buchungen berechnet.
   * Die Spendenart wird auf "GELDSPENDE" gesetzt. Das Spendendatum wird auf das
   * kleinste Datum der Buchungen gesetzt.
   * 
   * @param buchung
   *          Die Buchung zum Hinzufügen
   */
  @Override
  public void addBuchung(Buchung buchung) throws RemoteException
  {
    if (buchung != null)
    {
      double betrag = 0.0;
      if (buchungen == null)
      {
        buchungen = new ArrayList<>();
      }
      buchungen.add(buchung);
      for (Buchung b : buchungen)
      {
        betrag += b.getBetrag();
      }
      setBetrag(betrag);
      if (getSpendedatum() != null
          && buchung.getDatum().before(getSpendedatum()))
      {
        setSpendedatum(buchung.getDatum());
      }
      else if (getSpendedatum() == null)
      {
        setSpendedatum(buchung.getDatum());
      }
      setSpendenart(Spendenart.GELDSPENDE);
    }
  }

  /**
   * Hängt eine Buchung an die Spendenbescheinigung, wenn es eine
   * Einzelbestätigung werden soll. Sollten vorher schon Buchungen eingetragen
   * worden sein, wird die Liste der Buchungen vorher gelöscht.
   * 
   * @param buchung
   *          Die Buchung, die der Spendenbescheinigung zugeordnet wird
   */
  @Override
  public void setBuchung(Buchung buchung) throws RemoteException
  {
    if (buchungen != null)
    {
      buchungen.clear();
      setBetrag(0.0);
    }
    if (buchung != null)
    {
      addBuchung(buchung);
      setSpendedatum(buchung.getDatum());
    }
  }

  /**
   * Liefert die Liste der Buchungen einer Spendenbescheinigung zurück. Falls
   * die Liste noch nicht angelegt wurde, wird sie aus der Datenbank
   * nachgeladen. Sollten der Spendenbescheinigung noch keine Buchungen
   * zugeordnet sein, wird eine leere Liste zurückgegeben.<br>
   * Beim Laden der Buchungen wird der Gesamtbetrag berechnet
   * 
   * @return Liste der der Spendenbescheinigung zugeordneten Buchungen
   */
  @Override
  public List<Buchung> getBuchungen() throws RemoteException
  {
    if (getSpendenart() == Spendenart.GELDSPENDE && buchungen == null)
    {
      buchungen = new ArrayList<>();
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("spendenbescheinigung = ?", getID());
      it.setOrder("ORDER BY datum asc");
      double summe = 0.0;
      while (it.hasNext())
      {
        Buchung bu = it.next();
        buchungen.add(bu);
        summe += bu.getBetrag();
      }
      if (!buchungen.isEmpty())
      {
        setBetrag(summe);
      }
    }
    return buchungen;
  }

  @Override
  public Boolean getUnterlagenWertermittlung() throws RemoteException
  {
    return Util.getBoolean(getAttribute("unterlagenwertermittlung"));
  }

  @Override
  public void setUnterlagenWertermittlung(Boolean unterlagenwertermittlung)
      throws RemoteException
  {
    setAttribute("unterlagenwertermittlung",
        Boolean.valueOf(unterlagenwertermittlung));
  }

  @Override
  public Boolean getAutocreate() throws RemoteException
  {
    return Util.getBoolean(getAttribute("autocreate"));
  }

  @Override
  public void setAutocreate(Boolean autocreate) throws RemoteException
  {
    setAttribute("autocreate", Boolean.valueOf(autocreate));
  }

}
