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
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.rmi.AbstractDokument;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractDokumentImpl extends AbstractJVereinDBObject
    implements AbstractDokument
{

  private static final long serialVersionUID = 1L;

  private static String HASH_ALGORITHM = "SHA-512";

  private File file;

  public AbstractDokumentImpl() throws RemoteException
  {
    super();
  }

  @Override
  public String getPrimaryAttribute()
  {
    return "bemerkung";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    if (file == null)
    {
      throw new ApplicationException("Keine Datei angegeben!");
    }
    try
    {
      if (getDatum() == null)
      {
        throw new ApplicationException("Bitte Datum eingeben!");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Dokument kann nicht gespeichert werden. Siehe system log.";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    return null;
  }

  @Override
  public Long getReferenz() throws RemoteException
  {
    return (Long) getAttribute("referenz");
  }

  @Override
  public void setReferenz(Long referenz) throws RemoteException
  {
    setAttribute("referenz", referenz);
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
        bemerkung.length() > 50 ? bemerkung.substring(0, 50) : bemerkung);
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
  public Object getAttribute(String fieldName) throws RemoteException
  {
    return super.getAttribute(fieldName);
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
  public void store() throws RemoteException, ApplicationException
  {
    File newFile = null;
    if (file != null)
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
          String pfad = getDateiPfad() + File.separator + file.getName();

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
              getVerzeichnis() + "." + getReferenz(), fis);
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
      super.store();
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

  protected abstract String getDateiPfad() throws RemoteException;

  @Override
  public void delete() throws RemoteException, ApplicationException
  {
    super.delete();
    if (getUUID() != null)
    {
      QueryMessage qm = new QueryMessage(getUUID(), null);
      Application.getMessagingFactory()
          .getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
    }
    else if (getPfad() != null)
    {
      new File(getPfad()).delete();
    }
    else
    {
      throw new ApplicationException("Datei hat keine UUID und keinen Pfad!");
    }
  }

  protected abstract String getVerzeichnis();

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

  protected abstract String getRootDir();
}
