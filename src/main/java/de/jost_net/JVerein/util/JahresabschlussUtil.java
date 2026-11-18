package de.jost_net.JVerein.util;

import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.willuhn.datasource.rmi.DBIterator;

public class JahresabschlussUtil
{
  /**
   * Die Methode prüft ob zum übergebenen Datum ein Jahresabschluss existiert
   * 
   * @param datum
   *          Datum zu dem geprüft wird
   * @return
   * @throws RemoteException
   */
  public static boolean istAbgeschlossen(Date datum) throws RemoteException
  {
    DBIterator<Jahresabschluss> it = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    it.addFilter("von <= ?", datum);
    it.addFilter("bis >= ?", datum);
    it.setLimit(1);
    if (it.hasNext())
    {
      return true;
    }
    return false;
  }
}
