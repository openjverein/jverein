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

import java.io.File;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.BelegMap;
import de.jost_net.JVerein.Variable.BelegVar;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.AbstractBelegReferenz;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

public class BuchungDokumentImpl extends AbstractDokumentImpl
    implements BuchungDokument
{

  // Hier müssen alle Implementierungen von AbstractBelegReferenz
  // aufgelistet werden
  private List<Class<? extends AbstractBelegReferenz>> belegReferenzList = Arrays
      .asList(BuchungsdokumentBuchungImpl.class);

  private static final long serialVersionUID = 1L;

  public BuchungDokumentImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      for (Class<? extends AbstractBelegReferenz> c : belegReferenzList)
      {
        DBIterator<AbstractBelegReferenz> it = Einstellungen.getDBService()
            .createList(c);
        it.addFilter("dokument = ?", getID());
        while (it.hasNext())
        {
          it.next().checkChangesAllowed();
        }
      }
      if (getBemerkung() == null || getBemerkung().isBlank())
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim updateCheck");
    }
    super.updateCheck();
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBemerkung() == null || getBemerkung().isBlank())
      {
        throw new ApplicationException("Bitte Bezeichnung eingeben");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim insertCheck");
    }
    super.insertCheck();
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      // Alle vorhandenn erferenzen des Objects prüfen, ob sie geändert/gelöscht
      // werden dürfen
      for (Class<? extends AbstractBelegReferenz> c : belegReferenzList)
      {
        DBIterator<AbstractBelegReferenz> it = Einstellungen.getDBService()
            .createList(c);
        it.addFilter("dokument = ?", getID());
        while (it.hasNext())
        {
          it.next().checkChangesAllowed();
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim insertCheck");
    }
  }

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    try
    {
      // Alle vorhandenn erferenzen des Objects impliziet löschen, damit dort im
      // deleteCheck geprüft werden kann
      DBTransaction.starten();
      for (Class<? extends AbstractBelegReferenz> c : belegReferenzList)
      {
        DBIterator<DBObject> it = Einstellungen.getDBService().createList(c);
        it.addFilter("dokument = ?", getID());
        while (it.hasNext())
        {
          it.next().delete();
        }
      }
      DBTransaction.commit();
    }
    catch (ApplicationException | RemoteException e)
    {
      DBTransaction.rollback();
      throw e;
    }
    super.delete();
  }

  @Override
  protected String getTableName()
  {
    return "buchungdokument";
  }

  @Override
  protected String getVerzeichnis()
  {
    return "buchungen";
  }

  @Override
  public String getRootDir()
  {
    return Einstellungen.getBuchungDokumentVerzeichnis() + File.separator;
  }

  @Override
  protected String getDateiPfad() throws RemoteException
  {
    return VorlageUtil.getName(VorlageTyp.BELEG_PFAD, this);
  }

  @Override
  public String getBelegnummer() throws RemoteException
  {
    return (String) getAttribute("belegnummer");
  }

  @Override
  public void setBelegnummer(String belegnummer) throws RemoteException
  {
    setAttribute("belegnummer", belegnummer);
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    if (!isNewObject())
    {
      super.store();
      return;
    }
    try
    {
      transactionBegin();

      // Belegnummernummer erstellen
      Map<String, Object> map = new AllgemeineMap().getMap(null);
      map = new BelegMap().getMap(this, map);
      String nummer = VelocityTool.eval(map,
          (String) Einstellungen.getEinstellung(Property.BELEGNUMMER));

      if (nummer.length() > 50)
      {
        throw new ApplicationException(
            "Belegnummer zu lang, maximal 50 Zeichen erlaubt!");
      }
      setBelegnummer(nummer);

      // Prüfen, ob es schon einen Beleg mit dieser Nummer gibt
      DBIterator<?> it = getList();
      it.addFilter("belegnummer = ?", nummer);
      if (it.hasNext())
      {
        throw new ApplicationException(
            "Beleg mit dieser Nummer existiert bereits. Belegnummer in Einstellungen korrigieren!");
      }
      super.store();
      // Belegnummer hochzählen
      Einstellungen.setEinstellung(Property.BELEG_ZAEHLER,
          Integer.parseInt(map.get(BelegVar.BELEG_ZAEHLER.getName()).toString())
              + 1);

      transactionCommit();
    }
    catch (Exception e)
    {
      transactionRollback();
      throw e;
    }
  }

  @Override
  public String getNummer() throws RemoteException
  {
    // Wird zum Speichern per Messaging benötigt
    return getBelegnummer();
  }

  @Override
  public void setReferenz(Long referenz) throws RemoteException
  {
    throw new RemoteException("set Referenz bei Belegen nicht möglich!");
  }
}
