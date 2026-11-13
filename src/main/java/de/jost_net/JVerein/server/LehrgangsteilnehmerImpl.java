/**********************************************************************
 * 
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
 **********************************************************************/
package de.jost_net.JVerein.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LehrgangsteilnehmerImpl extends AbstractJVereinDBObject
    implements Lehrgangsteilnehmer, IMitglied
{

  private static final long serialVersionUID = 380278347818535726L;

  public LehrgangsteilnehmerImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "lehrgangsteilnehmer";
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
      if (getMitglied() == null)
      {
        throw new ApplicationException("Bitte Mitglied eingeben!");
      }
      if (getLehrgang() == null)
      {
        throw new ApplicationException("Bitte Lehrgang auswählen!");
      }
      DBIterator<Lehrgangsteilnehmer> it = Einstellungen.getDBService()
          .createList(Lehrgangsteilnehmer.class);
      it.addFilter("lehrgang = ?", this.getLehrgang().getID());
      it.addFilter("mitglied = ?", this.getMitglied().getID());
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Das Mitglied ist bereits bei dem Lehrgang eingetragen!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Lehrgangsteilnehmer kann nicht gespeichert werden. Siehe system log.";
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
    if ("mitglied".equals(arg0))
    {
      return Mitglied.class;
    }
    if ("lehrgangsart".equals(arg0))
    {
      return Lehrgangsart.class;
    }
    if ("lehrgang".equals(arg0))
    {
      return Lehrgang.class;
    }
    return null;
  }

  @Override
  public Mitglied getMitglied() throws RemoteException
  {
    return (Mitglied) getAttribute("mitglied");
  }

  @Override
  public void setMitglied(Integer mitglied) throws RemoteException
  {
    setAttribute("mitglied", mitglied);
  }

  @Override
  public Lehrgang getLehrgang() throws RemoteException
  {
    return (Lehrgang) getAttribute("lehrgang");
  }

  @Override
  public void setLehrgang(Long lehrgang) throws RemoteException
  {
    setAttribute("lehrgang", lehrgang);
  }

  @Override
  public Lehrgangsart getLehrgangsart() throws RemoteException
  {
    return (Lehrgangsart) getAttribute("lehrgangsart");
  }

  @Override
  public void setLehrgangsart(Long lehrgangsart) throws RemoteException
  {
    setAttribute("lehrgangsart", lehrgangsart);
  }

  @Override
  public Date getVon() throws RemoteException
  {
    return (Date) getAttribute("von");
  }

  @Override
  public void setVon(Date von) throws RemoteException
  {
    setAttribute("von", von);
  }

  @Override
  public Date getBis() throws RemoteException
  {
    return (Date) getAttribute("bis");
  }

  @Override
  public void setBis(Date bis) throws RemoteException
  {
    setAttribute("bis", bis);
  }

  @Override
  public String getVeranstalter() throws RemoteException
  {
    return (String) getAttribute("veranstalter");
  }

  @Override
  public void setVeranstalter(String veranstalter) throws RemoteException
  {
    setAttribute("veranstalter", veranstalter);
  }

  @Override
  public String getErgebnis() throws RemoteException
  {
    return (String) getAttribute("ergebnis");
  }

  @Override
  public void setErgebnis(String ergebnis) throws RemoteException
  {
    setAttribute("ergebnis", ergebnis);
  }

  @Override
  public Object getAttributeDefault(String fieldName)
  {
    switch (fieldName)
    {
      case "ergebnis":
      case "veranstalter":
      case "bezeichnung":
        return "";
      default:
        return null;
    }
  }

  @Override
  public String getObjektName()
  {
    return "Lehrgangsteilnehmer";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Lehrgangsteilnehmer";
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

}
