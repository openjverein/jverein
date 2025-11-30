package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

/**
 * Klassen die deises Interface implementieren, werden im menue mit einer Nummer
 * an ungelesenen Einträgen versehen
 */
public interface UnreadCounter
{
  /**
   * Liefer die anzahl ungelesener Einträge
   * 
   * @return die Anzahl Einträge
   * @throws RemoteException
   */
  public int getUeberfaellig() throws RemoteException;

  /**
   * Liefer die ID der Menueeintrags
   * 
   * @return
   */
  public String getMenueID();
}
