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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.BelegMap;
import de.jost_net.JVerein.Variable.BelegVar;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.keys.BelegArt;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.AbstractBelegReferenz;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BelegImpl extends AbstractJVereinDBObject implements Beleg
{

  // Hier müssen alle Implementierungen von AbstractBelegReferenz
  // aufgelistet werden
  private List<Class<? extends AbstractBelegReferenz>> belegReferenzList = Arrays
      .asList(BelegBuchungImpl.class);

  protected static String HASH_ALGORITHM = "SHA-512";

  private File file;

  private static final long serialVersionUID = 1L;

  public BelegImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      if (hasChanged("datum"))
      {
        throw new ApplicationException("Datum darf nicht verändert werden!");
      }
      if (hasChanged("uuid"))
      {
        throw new ApplicationException("UUID darf nicht verändert werden!");
      }
      if (hasChanged("hash"))
      {
        throw new ApplicationException("Hash darf nicht verändert werden!");
      }
      if (hasChanged("pfad"))
      {
        throw new ApplicationException("Pfad darf nicht verändert werden!");
      }
      for (Class<? extends AbstractBelegReferenz> c : belegReferenzList)
      {
        DBIterator<AbstractBelegReferenz> it = Einstellungen.getDBService()
            .createList(c);
        it.addFilter("beleg = ?", getID());
        while (it.hasNext())
        {
          it.next().checkChangesAllowed();
        }
      }
      if (getBemerkung() == null || getBemerkung().isBlank())
      {
        throw new ApplicationException("Bitte Bemerkung eingeben");
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
      if (file == null)
      {
        throw new ApplicationException("Keine Datei angegeben!");
      }
      if (getBemerkung() == null || getBemerkung().isBlank())
      {
        throw new ApplicationException("Bitte Bemerkung eingeben");
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
        it.addFilter("beleg = ?", getID());
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
        it.addFilter("beleg = ?", getID());
        while (it.hasNext())
        {
          it.next().delete();
        }
      }

      super.delete();
      DBTransaction.commit();

      if (getUUID() != null)
      {
        QueryMessage qm = new QueryMessage(getUUID(), null);
        Application.getMessagingFactory()
            .getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
      }
      else if (getPfad() != null)
      {
        new File(getRootDir() + getPfad()).delete();
      }
      else
      {
        throw new ApplicationException("Datei hat keine UUID und keinen Pfad!");
      }
    }
    catch (ApplicationException | RemoteException e)
    {
      DBTransaction.rollback();
      throw e;
    }
  }

  @Override
  protected String getTableName()
  {
    return "beleg";
  }

  private String getVerzeichnis() throws RemoteException
  {
    // Für per Messaging gespeicherte Belege nötig. Damit auch bestehende Belege
    // gefunden werden, bleibt es hier bei "buchungen"
    switch (getBelegArt())
    {
      case BUCHUNG:
        return "buchungen";
      case MITGLIED:
        return "mitglieder";
      case SONSTIGES:
        throw new RemoteException(
            "Sonstige Belege mit Messaging nicht möglich");
    }
    return null;
  }

  @Override
  public String getRootDir() throws RemoteException
  {
    switch (getBelegArt())
    {
      case BUCHUNG:
        return Einstellungen.getBelegBuchungVerzeichnis() + File.separator;
      case SONSTIGES:
        return Einstellungen.getBelegAllgemeinVerzeichnis() + File.separator;
      case MITGLIED:
        return Einstellungen.getBelegMitgliedVerzeichnis() + File.separator;
    }
    return null;
  }

  @Override
  public String getDateiPfad(String filename) throws RemoteException
  {
    switch (getBelegArt())
    {
      case BUCHUNG:
        return VorlageUtil.getName(VorlageTyp.BELEG_BUCHUNGEN_PFAD, this,
            filename);
      case SONSTIGES:
        return VorlageUtil.getName(VorlageTyp.BELEG_ALLGEMEIN_PFAD, this,
            filename);
      case MITGLIED:
        return VorlageUtil.getName(VorlageTyp.BELEG_MITGLIED_PFAD, this,
            filename);
    }
    return null;
  }

  @Override
  public String getBelegnummer() throws RemoteException
  {
    return (String) getAttribute("belegnummer");
  }

  @Override
  public Object getAttribute(String fieldName) throws RemoteException
  {
    if ("belegnummer".equals(fieldName))
    {
      String nr = (String) super.getAttribute("belegnummer");
      if (nr != null && !nr.isBlank())
      {
        return nr;
      }

      // Alte Dokumente haben zT. keine Belegnummer (Wenn es mehrere Dokumente
      // in einer Buchung gab), dann als Fallback die Referenz verwenden
      Long ref = (Long) getAttribute("referenz");
      return ref == null ? null : ref.toString() + "-" + getID();
    }
    else if ("vollpfad".equals(fieldName) && getPfad() != null)
    {
      return getRootDir() + getPfad();
    }
    return super.getAttribute(fieldName);
  }

  @Override
  public void setBelegnummer(String belegnummer) throws RemoteException
  {
    setAttribute("belegnummer", belegnummer);
  }

  @Override
  public BelegArt getBelegArt() throws RemoteException
  {
    Integer art = (Integer) getAttribute("belegart");
    if (art == null)
    {
      return null;
    }
    return BelegArt.getByKey(art);
  }

  @Override
  public void setBelegArt(BelegArt belegart) throws RemoteException
  {
    setAttribute("belegart", belegart.getKey());
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
      if (getBelegArt() == null)
      {
        throw new ApplicationException("Beleg-Art ist nicht gesetzt");
      }

      transactionBegin();

      // Belegnummernummer erstellen
      Map<String, Object> map = new AllgemeineMap().getMap(null);
      map = new BelegMap().getMap(this, map);

      String nummer = "";
      switch (getBelegArt())
      {
        case BUCHUNG:
          nummer = (String) Einstellungen
              .getEinstellung(Property.BELEGNUMMER_BUCHUNG);
          break;
        case SONSTIGES:
          nummer = (String) Einstellungen
              .getEinstellung(Property.BELEGNUMMER_SONSTIGES);
          break;
        case MITGLIED:
          nummer = (String) Einstellungen
              .getEinstellung(Property.BELEGNUMMER_MITGLIED);
          break;
      }
      nummer = VelocityTool.eval(map, nummer);

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
      Property prop = null;
      switch (getBelegArt())
      {
        case BUCHUNG:
          prop = Property.BELEG_ZAEHLER_BUCHUNG;
          break;
        case SONSTIGES:
          prop = Property.BELEG_ZAEHLER_SONSTIGES;
          break;
        case MITGLIED:
          prop = Property.BELEG_ZAEHLER_MITGLIED;
          break;
      }
      Einstellungen.setEinstellung(prop,
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
  public String getPrimaryAttribute()
  {
    return "bemerkung";
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
  public String getBemerkung() throws RemoteException
  {
    return (String) getAttribute("bemerkung");
  }

  @Override
  public void setBemerkung(String bemerkung) throws RemoteException
  {
    setAttribute("bemerkung",
        bemerkung != null && bemerkung.length() > 50
            ? bemerkung.substring(0, 50)
            : bemerkung);
  }

  @Override
  public String getUUID() throws RemoteException
  {
    return (String) getAttribute("uuid");
  }

  @Override
  public void setUUID(String uuid) throws RemoteException
  {
    setAttribute("uuid", uuid);
  }

  @Override
  public File getFile() throws IOException, ApplicationException
  {
    if (isNewObject())
    {
      return null;
    }
    File file = null;
    if (getUUID() != null)
    {
      if (!JVereinPlugin.isArchiveServiceActive())
      {
        throw new ApplicationException(
            "Dokument wurde per jameica.messaging gespeichert. Das Plugin ist aber nicht installiert.");
      }
      QueryMessage qm = new QueryMessage(getUUID(), null);

      Application.getMessagingFactory()
          .getMessagingQueue("jameica.messaging.getmeta").sendSyncMessage(qm);
      @SuppressWarnings("rawtypes")
      Map map = (Map) qm.getData();
      if (map == null)
      {
        throw new ApplicationException("Datei existiert nicht");
      }
      qm = new QueryMessage(getUUID(), null);
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.messaging.get").sendSyncMessage(qm);
      byte[] data = (byte[]) qm.getData();
      file = new File(
          System.getProperty("java.io.tmpdir") + "/" + map.get("filename"));
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(data);
      fos.close();
      file.deleteOnExit();
    }
    else if (getPfad() != null)
    {
      file = new File(getRootDir() + getPfad());
      if (!file.exists())
      {
        throw new ApplicationException("Datei existiert nicht");
      }
    }
    if (getHash() != null && getHash().length > 0 && file != null)
    {
      try (FileInputStream fis = new FileInputStream(file))
      {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(fis.readAllBytes());
        if (!Arrays.equals(getHash(), md.digest()))
        {
          throw new ApplicationException(
              "Datei wurde seit dem Speichern verändert, Hash stimmt nicht überein!");
        }
      }
      catch (NoSuchAlgorithmException e)
      {
        throw new ApplicationException(
            "Fehler beim erstellen des Datei-Hashes");
      }
    }
    return file;
  }

  @Override
  public void setFile(File file) throws RemoteException
  {
    this.file = file;
  }

  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    if (getDatum() == null)
    {
      setAttribute("datum", new Date());
    }
    File newFile = null;

    // vor dem Kopieren der Datei schon prüfen, ob alle Angaben gemacht sind
    insertCheck();
    // Wenn getPfad gesetzt ist, wurde schon gespeichern, also ist irgendwas
    // faul
    if (file != null && getPfad() == null)
    {
      if (file.isDirectory())
      {
        throw new ApplicationException(
            "Verzeichnisse können nicht gespeichert werden.");
      }
      if (!file.exists())
      {
        throw new ApplicationException("Datei existiert nicht");
      }

      try (FileInputStream fis = new FileInputStream(file))
      {
        if (fis.available() <= 0)
        {
          throw new ApplicationException("Datei ist leer");
        }
        if (!(Boolean) Einstellungen
            .getEinstellung(Property.DOKUMENTSPEICHERUNG_MESSAGING))
        {
          String pfad = getDateiPfad(file.getName());

          newFile = new File(getRootDir() + pfad);
          if (newFile.exists())
          {
            throw new ApplicationException("Datei existiert bereits!");
          }
          newFile.getParentFile().mkdirs();

          try (FileOutputStream fos = new FileOutputStream(newFile, true))
          {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0)
            {
              fos.write(buffer, 0, length);
            }
          }

          setPfad(pfad);
        }
        else
        {
          if (!JVereinPlugin.isArchiveServiceActive())
          {
            throw new ApplicationException(
                "Dokument kann nicht per jameica.messaging gespeichert werden. Das Plugin ist nicht installiert."
                    + " Bitte in Einstellungen korrigieren.");
          }
          QueryMessage qm = new QueryMessage(
              getVerzeichnis() + "." + getBelegnummer(), fis);
          Application.getMessagingFactory()
              .getMessagingQueue("jameica.messaging.put").sendSyncMessage(qm);

          String uuid = qm.getData().toString();
          setUUID(uuid);

          // Zusätzliche Eigenschaft speichern
          Map<String, String> map = new HashMap<>();
          map.put("filename", file.getName());
          qm = new QueryMessage(uuid, map);
          Application.getMessagingFactory()
              .getMessagingQueue("jameica.messaging.putmeta").sendMessage(qm);
        }
        try (FileInputStream stream = new FileInputStream(file))
        {
          MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
          md.update(stream.readAllBytes());
          setHash(md.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
          throw new ApplicationException(
              "Fehler beim erstellen des Datei-Hashes");
        }
      }
      catch (FileNotFoundException e)
      {
        throw new ApplicationException("Datei existiert nicht");
      }
      catch (IOException e)
      {
        Logger.error("Ein-/Ausgabefehler", e);
        throw new ApplicationException("Allgemeiner Ein-/Ausgabe-Fehler");
      }
    }
    try
    {
      super.insert();
    }
    catch (Exception e)
    {
      // Wenn das speichern fehlschlägt, Datei wieder löschen
      if (newFile != null)
      {
        if (newFile.exists())
        {
          newFile.delete();
        }
      }
      else
      {
        QueryMessage qm = new QueryMessage(getUUID(), null);
        Application.getMessagingFactory()
            .getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
      }
      throw e;
    }
  }

  @Override
  public void setHash(byte[] hash) throws RemoteException
  {
    setAttribute("hash", hash);
  }

  @Override
  public byte[] getHash() throws RemoteException
  {
    return (byte[]) getAttribute("hash");
  }

  @Override
  public String getObjektName()
  {
    return "Dokument";
  }

  @Override
  public String getObjektNameMehrzahl()
  {
    return "Dokumente";
  }

  @Override
  public String getPfad() throws RemoteException
  {
    return (String) getAttribute("pfad");
  }

  @Override
  public void setPfad(String pfad) throws RemoteException
  {
    setAttribute("pfad", pfad);
  }
}
