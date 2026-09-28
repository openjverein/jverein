package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

/**
 * Datenbank-Objekt, dass einen Belg referenzieren kann.
 */
public interface AbstractBelegDBObject extends JVereinDBObject
{

  /**
   * Ordnet dem Objekt einen Beleg zu
   * 
   * @param beleg
   * @return
   * @throws RemoteException
   * @throws ApplicationException
   */
  public AbstractBelegReferenz addBeleg(Beleg beleg)
      throws RemoteException, ApplicationException;

  /**
   * Entfernt die Zuordnung des Beleges zum Objekt
   * 
   * @param beleg
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void removeBeleg(Beleg beleg)
      throws RemoteException, ApplicationException;

  /**
   * Liefert eine Liste mit allen Belegen die diesem Objekt zugeordnet sind
   * 
   * @return
   * @throws RemoteException
   */
  public DBIterator<Beleg> getBelegList() throws RemoteException;
}
