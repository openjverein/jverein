package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.io.TestDBSupportH2Impl;

public class TestJVereinDBService extends JVereinDBServiceImpl
{
  private static final long serialVersionUID = 1L;

  public TestJVereinDBService() throws RemoteException
  {
    super(TestDBSupportH2Impl.class.getName());
  }

  @Override
  protected boolean getAutoCommit() throws RemoteException
  {
    return false;
  }
}
