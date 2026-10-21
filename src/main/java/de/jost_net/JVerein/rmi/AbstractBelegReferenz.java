package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.util.ApplicationException;

public interface AbstractBelegReferenz extends JVereinDBObject, Changeable
{
  public Beleg getBeleg() throws RemoteException;

  public void setBeleg(Beleg beleg) throws RemoteException;

  public DBObject getReferenz() throws RemoteException;

  public void setReferenz(DBObject referenz) throws RemoteException;

  /**
   * Prüft, ob Änderungen am Beleg aus sicht der Referenz erlaubt sind, wenn
   * nicht wird eine Exception geworfen
   * 
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void checkChangesAllowed()
      throws RemoteException, ApplicationException;
}
