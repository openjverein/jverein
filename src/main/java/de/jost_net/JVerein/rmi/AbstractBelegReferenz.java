package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

public interface AbstractBelegReferenz extends JVereinDBObject, Changeable
{
  public BuchungDokument getDokument() throws RemoteException;

  public void setDokument(BuchungDokument dokument) throws RemoteException;

  public DBObject getBuchung() throws RemoteException;

  public void setBuchung(DBObject buchung) throws RemoteException;

  /**
   * Prüft, ob änderungen am Beleg aus sicht der Referenz erlaubt sind, wenn
   * nicht wird eine Exception geworfen
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void checkChangesAllowed()
      throws RemoteException, ApplicationException;
}
