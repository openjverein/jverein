package de.jost_net.JVerein.rmi;

import de.willuhn.datasource.rmi.DBObject;

import java.rmi.RemoteException;

public interface Wirtschaftsplan extends DBObject
{
  int getGeschaeftsjahr() throws RemoteException;

  void setGeschaeftsjahr(int gj) throws RemoteException;

  Buchungsart getBuchungsart() throws RemoteException;

  Long getBuchungsartId() throws RemoteException;

  void setBuchungsartId(Long buchungsartId) throws RemoteException;

  double getBetrag() throws RemoteException;

  void setBetrag(double betrag) throws RemoteException;
}
