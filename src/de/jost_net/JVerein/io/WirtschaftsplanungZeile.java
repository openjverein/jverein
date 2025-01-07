package de.jost_net.JVerein.io;

import de.willuhn.datasource.GenericObject;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Objects;

public class WirtschaftsplanungZeile implements GenericObject
{
  private final Long id;

  private Date von;

  private Date bis;

  private Double planEinnahme;

  private Double planAusgabe;

  private Double istEinnahme;

  private Double istAusgabe;

  public WirtschaftsplanungZeile(Long id, Date von, Date bis)
  {
    this.id = id;
    this.von = von;
    this.bis = bis;
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
      case "id":
        return id;
      case "datum_von":
        return von;
      case "datum_bis":
        return bis;
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
    return new String[] { "id", "datum_von", "datum_bis", "planEinnahme", "planAusgabe",
        "istEinnahme", "istAusgabe", "planSaldo", "istSaldo", "differenz" };
  }

  @Override
  public String getID() throws RemoteException
  {
    return id.toString();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "id";
  }

  @Override
  public boolean equals(GenericObject o)
  {
    if (this == o)
      return true;
    if (!(o instanceof WirtschaftsplanungZeile))
      return false;
    WirtschaftsplanungZeile zeile = (WirtschaftsplanungZeile) o;
    return getId().equals(zeile.getId()) && getVon().equals(
        zeile.getVon()) && getBis().equals(zeile.getBis());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(getId(), getVon(), getBis());
  }

  public Long getId()
  {
    return id;
  }

  public Date getVon()
  {
    return von;
  }

  public void setVon(Date von)
  {
    this.von = von;
  }

  public Date getBis()
  {
    return bis;
  }

  public void setBis(Date bis)
  {
    this.bis = bis;
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
