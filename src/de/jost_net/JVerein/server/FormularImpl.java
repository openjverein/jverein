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
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularImpl extends AbstractJVereinDBObject implements Formular
{

  private static final long serialVersionUID = 1603994510932244220L;

  public FormularImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "formular";
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "bezeichnung";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      DBIterator<Spendenbescheinigung> spb = Einstellungen.getDBService()
          .createList(Spendenbescheinigung.class);
      spb.addFilter("formular = ?", new Object[] { getID() });
      spb.setLimit(1);
      if (spb.size() > 0)
      {
        throw new ApplicationException(
            "Es ist bei Spendenbescheinigungen hinterlegt.");
      }

      DBIterator<Rechnung> reIt = Einstellungen.getDBService()
          .createList(Rechnung.class);
      reIt.addFilter("formular = ?", new Object[] { getID() });
      reIt.setLimit(1);
      if (reIt.size() > 0)
      {
        throw new ApplicationException("Es ist bei Rechnungen hinterlegt.");
      }

      // Do not delete a form if it is linked by other forms
      if (hasFormlinks())
      {
        throw new ApplicationException(String.format(
            "Es ist noch mit %d Formular(en) verknüpft.", getLinked().size()));
      }

      // Do not delete a form if it is linked to another
      Long formlink = getFormlink();
      if (formlink > 0)
      {
        Formular fo = (Formular) Einstellungen.getDBService()
            .createObject(Formular.class, String.valueOf(formlink));
        throw new ApplicationException(String.format(
            "Es ist mit dem Formular \"%s\" verknüpft.", fo.getBezeichnung()));
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Formular kann nicht gelöscht werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getInhalt() == null)
      {
        throw new ApplicationException("Bitte gültigen Dateinamen angeben!");
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    updateCheck();
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      if (getBezeichnung() == null || getBezeichnung().isEmpty())
      {
        throw new ApplicationException("Bitte eine Bezeichnung eingeben!");
      }
      DBIterator<Formular> formIt = Einstellungen.getDBService()
          .createList(Formular.class);
      if (!this.isNewObject())
      {
        formIt.addFilter("id != ?", getID());
      }
      formIt.addFilter("bezeichnung = ?", getBezeichnung());
      if (formIt.hasNext())
      {
        throw new ApplicationException(
            "Bitte eindeutige Bezeichnung eingeben!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Formular kann nicht gespeichert werden. Siehe system log";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
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
  public byte[] getInhalt() throws RemoteException
  {
    return (byte[]) this.getAttribute("inhalt");
  }

  @Override
  public void setInhalt(byte[] inhalt) throws RemoteException
  {
    setAttribute("inhalt", inhalt);
  }

  @Override
  public FormularArt getArt() throws RemoteException
  {
    Integer art = (Integer) getAttribute("art");
    if (art == null)
    {
      return null;
    }
    for (FormularArt form : FormularArt.values())
    {
      if (form.getKey() == art)
      {
        return form;
      }
    }
    return null;
  }

  @Override
  public void setArt(FormularArt art) throws RemoteException
  {
    setAttribute("art", art == null ? 0 : art.getKey());
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    return super.getAttribute(fieldName);
  }

  @Override
  public int getZaehler() throws RemoteException
  {
    Integer counter = (Integer) getAttribute("zaehler");
    return counter != null ? (int) counter : 0;
  }

  @Override
  public void setZaehler(int zaehler) throws RemoteException
  {
    setAttribute("zaehler", zaehler);
  }

  @Override
  public void setZaehlerToFormlink(int zaehler) throws RemoteException
  {
    DBIterator<Formular> formList = getLinked();
    while (formList.hasNext())
    {
      Formular form = formList.next();
      form.setZaehler(zaehler);
      try
      {
        form.store();
      }
      catch (RemoteException e)
      {
        String fehler = "Fortlaufende Nr. kann nicht gespeichert werden. Siehe system log.";
        Logger.error(fehler, e);
        throw new RemoteException(fehler);
      }
      catch (ApplicationException e)
      {
        String fehler = "Fortlaufende Nr. kann nicht gespeichert werden. Siehe system log.";
        Logger.error(fehler, e);
      }
    }
  }

  @Override
  public Long getFormlink() throws RemoteException
  {
    Long formId = (Long) getAttribute("formlink");
    if (formId == null)
    {
      return 0l;
    }

    return formId;
  }

  @Override
  public void setFormlink(Long formlink) throws RemoteException
  {
    setAttribute("formlink", formlink);
  }

  @Override
  public DBIterator<Formular> getLinked() throws RemoteException
  {
    DBIterator<Formular> formList = Einstellungen.getDBService()
        .createList(Formular.class);
    // In case current form is linked to another form
    if (this.getFormlink() > 0)
    {
      formList.addFilter("formlink = ? OR id = ?", this.getFormlink(),
          this.getFormlink());
    }
    else
    {
      // In case current form isn't linked to another form
      formList.addFilter("formlink = ?", this.getID());
    }

    return formList;
  }

  @Override
  public boolean hasFormlinks() throws RemoteException
  {
    // Return FALSE for new forms
    if (this.getID() == null || this.getFormlink() > 0)
    {
      return Boolean.FALSE;
    }

    DBIterator<Formular> formList = getLinked();

    return Boolean.valueOf(formList.size() > 0);
  }

  @Override
  public String getObjektName()
  {
    return "Formular";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Formulare";
  }

}
