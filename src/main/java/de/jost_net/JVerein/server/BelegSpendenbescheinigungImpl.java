package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JahresabschlussUtil;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.BelegSpendenbescheinigung;
import de.willuhn.util.ApplicationException;

public class BelegSpendenbescheinigungImpl extends AbstractBelegReferenzImpl
    implements BelegSpendenbescheinigung
{

  private static final long serialVersionUID = -810016324287845770L;

  public BelegSpendenbescheinigungImpl() throws RemoteException
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
      return Spendenbescheinigung.class;
    }
    return null;
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Beleg-Spendenbescheinigung";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Beleg-Spendenbescheinigungen";
  }

  @Override
  protected String getTableName()
  {
    return "belegspendenbescheinigung";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      if (getBeleg().getVersand() != null)
      {
        throw new ApplicationException(
            "Bereits versendete Belege können nicht gelöscht werden.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim deleteCheck");
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      if (JahresabschlussUtil.istAbgeschlossen(getBeleg().getDatum()))
      {
        throw new ApplicationException(
            "Der Spendenbescheinigung Beleg kann nicht geändert werden, sein Datum liegt in einem abgeschlossenen Geschäftsjahr.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim updateCheck");
    }
    super.updateCheck();
  }

}
