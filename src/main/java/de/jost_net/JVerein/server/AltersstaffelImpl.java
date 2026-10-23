package de.jost_net.JVerein.server;

import java.math.BigDecimal;
import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Altersstaffel;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.willuhn.datasource.db.AbstractDBObject;

public class AltersstaffelImpl extends AbstractDBObject implements Altersstaffel
{

  private static final long serialVersionUID = 1L;

  public AltersstaffelImpl() throws RemoteException
  {
    super();
  }

  @Override
  public BigDecimal getBetrag() throws RemoteException
  {
    return (BigDecimal) getAttribute("betrag");
  }

  @Override
  public void setBetrag(BigDecimal betrag) throws RemoteException
  {
    setAttribute("betrag", betrag);
  }

  @Override
  public Beitragsgruppe getBeitragsgruppe() throws RemoteException
  {
    return (Beitragsgruppe) getAttribute("beitragsgruppe");
  }

  @Override
  public void setBeitragsgruppe(Beitragsgruppe beitragsgruppe)
      throws RemoteException
  {
    setAttribute("beitragsgruppe", beitragsgruppe);
  }

  @Override
  protected String getTableName()
  {
    return "altersstaffel";
  }

  @Override
  public int getNummer() throws RemoteException
  {
    Integer i = (Integer) getAttribute("nummer");
    if (i == null)
    {
      return 0;
    }
    return i.intValue();
  }

  @Override
  public void setNummer(int nummer) throws RemoteException
  {
    setAttribute("nummer", Integer.valueOf(nummer));
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  protected Class<?> getForeignObject(String arg0)
  {
    if (arg0.equals("beitragsgruppe"))
    {
      return Beitragsgruppe.class;
    }
    return null;
  }
}
