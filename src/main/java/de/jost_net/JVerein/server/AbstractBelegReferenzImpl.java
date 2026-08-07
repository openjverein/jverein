package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.AbstractBelegReferenz;
import de.jost_net.JVerein.rmi.BuchungDokument;
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
  public BuchungDokument getDokument() throws RemoteException
  {
    return (BuchungDokument) super.getAttribute("dokument");
  }

  @Override
  public void setDokument(BuchungDokument dokument) throws RemoteException
  {
    setAttribute("dokument", dokument);
  }

  @Override
  public DBObject getBuchung() throws RemoteException
  {
    return (DBObject) super.getAttribute("buchung");
  }

  @Override
  public void setBuchung(DBObject buchung) throws RemoteException
  {
    setAttribute("buchung", buchung);
  }
}
