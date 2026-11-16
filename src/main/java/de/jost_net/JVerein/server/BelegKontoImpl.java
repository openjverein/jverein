package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.BelegKonto;
import de.willuhn.util.ApplicationException;

public class BelegKontoImpl extends AbstractBelegReferenzImpl
    implements BelegKonto
{

  private static final long serialVersionUID = -810016324287845770L;

  public BelegKontoImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("beleg".equals(field))
    {
      return Beleg.class;
    }
    else if ("referenz".equals(field))
    {
      return Konto.class;
    }
    return null;
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Beleg-Konto";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Beleg-Konten";
  }

  @Override
  protected String getTableName()
  {
    return "belegkonto";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public void checkChangesAllowed() throws ApplicationException
  {
    updateCheck();
  }
}
