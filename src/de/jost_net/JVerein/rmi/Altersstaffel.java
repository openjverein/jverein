package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

public interface Altersstaffel extends DBObject
{

  public double getBetrag() throws RemoteException;

  public void setBetrag(double betrag) throws RemoteException;

  public Beitragsgruppe getBeitragsgruppe() throws RemoteException;

  public void setBeitragsgruppe(Beitragsgruppe beitragsgruppe)
      throws RemoteException;

  public int getNummer() throws RemoteException;

  public void setNummer(int nummer) throws RemoteException;
}
