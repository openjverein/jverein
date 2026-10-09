package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.AbstractBelegReferenz;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.datasource.rmi.DBObject;

public abstract class AbstractBelegReferenzImpl extends AbstractJVereinDBObject
    implements AbstractBelegReferenz
{

  private static final long serialVersionUID = -1625655887687027193L;

  public AbstractBelegReferenzImpl() throws RemoteException
  {
    super();
  }

  @Override
  public Beleg getBeleg() throws RemoteException
  {
    return (Beleg) super.getAttribute("beleg");
  }

  @Override
  public void setBeleg(Beleg dokument) throws RemoteException
  {
    setAttribute("beleg", dokument);
  }

  @Override
  public DBObject getReferenz() throws RemoteException
  {
    return (DBObject) super.getAttribute("referenz");
  }

  @Override
  public void setReferenz(DBObject referenz) throws RemoteException
  {
    setAttribute("referenz", referenz);
  }
}
