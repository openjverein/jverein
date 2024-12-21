package de.jost_net.JVerein.io;

import de.willuhn.datasource.GenericObject;

import java.rmi.RemoteException;
import java.util.Objects;

public class WirtschaftsplanungZeile implements GenericObject
{
  private Integer geschaeftsjahr;

  private Double planEinnahme;

  private Double planAusgabe;

  private Double istEinnahme;

  private Double istAusgabe;

  public WirtschaftsplanungZeile(Integer geschaeftsjahr)
  {
    this.geschaeftsjahr = geschaeftsjahr;
    this.planEinnahme = 0.;
    this.planAusgabe = 0.;
    this.istAusgabe = 0.;
    this.istEinnahme = 0.;
  }

  @Override
  public Object getAttribute(String s) throws RemoteException
  {
    double planSaldo = planEinnahme + planAusgabe;
    double istSaldo = istEinnahme + istAusgabe;

    switch (s)
    {
      case "geschaeftsjahr":
        return geschaeftsjahr;
      case "planEinnahme":
        return planEinnahme;
      case "planAusgabe":
        return planAusgabe;
      case "istEinnahme":
        return istEinnahme;
      case "istAusgabe":
        return istAusgabe;
      case "planSaldo":
        return planSaldo;
      case "istSaldo":
        return istSaldo;
      case "differenz":
        return istSaldo - planSaldo;
      default:
        throw new RemoteException(
            String.format("Ungültige Spaltenbezeichung: %s", s));
    }
  }

  @Override
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] {"geschaeftsjahr", "planEinnahme", "planAusgabe", "istEinnahme", "istAusgabe", "planSaldo", "istSaldo", "differenz"};
  }

  @Override
  public String getID() throws RemoteException
  {
    return geschaeftsjahr.toString();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "geschaeftsjahr";
  }

  @Override
  public boolean equals(GenericObject genericObject) throws RemoteException
  {
    if (genericObject instanceof WirtschaftsplanungZeile) {
      return this.getID().equals(genericObject.getID());
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(geschaeftsjahr);
  }

  public Integer getGeschaeftsjahr()
  {
    return geschaeftsjahr;
  }

  public void setGeschaeftsjahr(Integer geschaeftsjahr)
  {
    this.geschaeftsjahr = geschaeftsjahr;
  }

  public Double getPlanEinnahme()
  {
    return planEinnahme;
  }

  public void setPlanEinnahme(Double planEinnahme)
  {
    this.planEinnahme = planEinnahme;
  }

  public Double getPlanAusgabe()
  {
    return planAusgabe;
  }

  public void setPlanAusgabe(Double planAusgabe)
  {
    this.planAusgabe = planAusgabe;
  }

  public Double getIstEinnahme()
  {
    return istEinnahme;
  }

  public void setIstEinnahme(Double istEinnahme)
  {
    this.istEinnahme = istEinnahme;
  }

  public Double getIstAusgabe()
  {
    return istAusgabe;
  }

  public void setIstAusgabe(Double istAusgabe)
  {
    this.istAusgabe = istAusgabe;
  }
}
