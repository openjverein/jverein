package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.BelegKonto;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.willuhn.datasource.rmi.DBIterator;
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
  public void checkUpdateAllowed() throws ApplicationException
  {
    updateCheck();
  }

  @Override
  public void checkDeleteAllowed() throws ApplicationException
  {
    deleteCheck();
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      if (istAbgeschlossen())
      {
        throw new ApplicationException(
            "Der Konto Beleg kann nicht entfernt werden, sein Datum liegt in einem abgeschlossenen Geschäftsjahr.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim deleteCheck");
    }
    super.deleteCheck();
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      if (istAbgeschlossen())
      {
        throw new ApplicationException(
            "Der Konto Beleg kann nicht geändert werden, sein Datum liegt in einem abgeschlossenen Geschäftsjahr.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim updateCheck");
    }
    super.updateCheck();
  }

  private boolean istAbgeschlossen() throws RemoteException
  {
    DBIterator<Jahresabschluss> it = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    it.addFilter("von <= ?", new Object[] { getBeleg().getDatum() });
    it.addFilter("bis >= ?", new Object[] { getBeleg().getDatum() });
    it.setLimit(1);
    if (it.hasNext())
    {
      return true;
    }
    return false;
  }
}
