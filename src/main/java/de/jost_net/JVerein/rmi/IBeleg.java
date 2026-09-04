package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.util.ApplicationException;

public interface IBeleg extends Changeable
{
  public AbstractBelegReferenz addBeleg(BuchungDokument dokument)
      throws RemoteException, ApplicationException;

  public void removeBeleg(BuchungDokument dokument)
      throws RemoteException, ApplicationException;

  public String getObjektName() throws RemoteException;

  public DBIterator<BuchungDokument> getBelegList() throws RemoteException;
}
