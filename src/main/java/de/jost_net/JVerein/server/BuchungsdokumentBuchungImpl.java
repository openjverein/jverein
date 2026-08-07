package de.jost_net.JVerein.server;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.BuchungDokument;
import de.jost_net.JVerein.rmi.BuchungsdokumentBuchung;
import de.willuhn.util.ApplicationException;

public class BuchungsdokumentBuchungImpl extends AbstractBelegReferenzImpl
    implements BuchungsdokumentBuchung
{

  private static final long serialVersionUID = -810016324287845770L;

  public BuchungsdokumentBuchungImpl() throws RemoteException
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
            "Dokument kann nicht entfernt werden, ist einer abgeschlossenen Buchung zugeordnet.");
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
            "Dokument kann nicht geändert werden, ist einer abgeschlossenen Buchung zugeordnet.");
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
            "Dokument kann nicht zugeordnet werden, Buchung ist abgeschlossen.");
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
    return ((Buchung) getBuchung()).getJahresabschluss() != null;
  }

  @Override
  protected Class<?> getForeignObject(String field)
  {
    if ("dokument".equals(field))
    {
      return BuchungDokument.class;
    }
    else if ("buchung".equals(field))
    {
      return Buchung.class;
    }
    return null;
  }

  @Override
  public String getObjektName() throws RemoteException
  {
    return "Buchungsdokument-Buchung";
  }

  @Override
  public String getObjektNameMehrzahl() throws RemoteException
  {
    return "Buchungsdokument-Buchungen";
  }

  @Override
  protected String getTableName()
  {
    return "buchungsdokumentbuchung";
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
