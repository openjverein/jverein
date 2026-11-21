package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.util.JahresabschlussUtil;
import de.jost_net.JVerein.rmi.Beleg;
import de.jost_net.JVerein.rmi.BelegRechnung;
import de.willuhn.util.ApplicationException;

public class BelegRechnungImpl extends AbstractBelegReferenzImpl
    implements BelegRechnung
{

  private static final long serialVersionUID = -810016324287845770L;

  public BelegRechnungImpl() throws RemoteException
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
      return Rechnung.class;
    }
    return null;
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Beleg-Rechnung";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Beleg-Rechnungen";
  }

  @Override
  protected String getTableName()
  {
    return "belegrechnung";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  protected void deleteCheck() throws ApplicationException
  {
    throw new ApplicationException(
        "Zugeordnete Rechnungen können nicht entfernt werden.");
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    try
    {
      if (JahresabschlussUtil.istAbgeschlossen(getBeleg().getDatum()))
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

}
