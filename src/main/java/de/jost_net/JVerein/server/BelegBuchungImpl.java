package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.BelegBuchung;
import de.willuhn.util.ApplicationException;

public class BelegBuchungImpl extends AbstractBelegReferenzImpl
    implements BelegBuchung
{

  private static final long serialVersionUID = -810016324287845770L;

  public BelegBuchungImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    try
    {
      if (istAbgeschlossen())
      {
        throw new ApplicationException(
            "Beleg kann nicht entfernt werden, ist einer abgeschlossenen Buchung zugeordnet.");
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
            "Beleg kann nicht geändert werden, ist einer abgeschlossenen Buchung zugeordnet.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim updateCheck");
    }
    super.updateCheck();
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (istAbgeschlossen())
      {
        throw new ApplicationException(
            "Beleg kann nicht zugeordnet werden, Buchung ist abgeschlossen.");
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim insertCheck");
    }
    super.insertCheck();
  }

  private boolean istAbgeschlossen() throws RemoteException
  {
    return ((Buchung) getReferenz()).getJahresabschluss() != null;
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
      return Buchung.class;
    }
    return null;
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Beleg-Buchung";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Beleg-Buchungen";
  }

  @Override
  protected String getTableName()
  {
    return "belegbuchung";
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
