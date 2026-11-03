package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.BelegMitglied;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.util.ApplicationException;

public class BelegMitgliedImpl extends AbstractBelegReferenzImpl
    implements BelegMitglied
{

  private static final long serialVersionUID = -810016324287845770L;

  public BelegMitgliedImpl() throws RemoteException
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
      return Mitglied.class;
    }
    return null;
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Beleg-Mitglied";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Beleg-Mitglieder";
  }

  @Override
  protected String getTableName()
  {
    return "belegmitglied";
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
