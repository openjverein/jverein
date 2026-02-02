package de.jost_net.JVerein.io;

import java.util.Date;

import de.jost_net.JVerein.keys.UeberweisungAusgabe;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Steuer;

public class GutschriftParam
{

  private UeberweisungAusgabe ausgabe;

  private Date datum;

  private String verwendungszweck;

  // Rechnung
  private boolean rechnungErzeugen = false;

  private boolean rechnungsDokumentSpeichern = false;

  private Formular formular = null;

  private String rechnungsText;

  private Date rechnungsDatum;

  // Fixer Betrag
  private boolean fixerBetragAbrechnen = false;

  private Double fixerBetrag;

  private Buchungsart buchungsart;

  private Buchungsklasse buchungsklasse;

  private Steuer steuer;

  public Formular getFormular()
  {
    return formular;
  }

  public void setFormular(Formular formular)
  {
    this.formular = formular;
  }

  public Date getDatum()
  {
    return datum;
  }

  public void setDatum(Date datum)
  {
    this.datum = datum;
  }

  public String getVerwendungszweck()
  {
    return verwendungszweck;
  }

  public void setVerwendungszweck(String verwendungszweck)
  {
    this.verwendungszweck = verwendungszweck;
  }

  public UeberweisungAusgabe getAusgabe()
  {
    return ausgabe;
  }

  public void setAusgabe(UeberweisungAusgabe ausgabe)
  {
    this.ausgabe = ausgabe;
  }

  public boolean isRechnungErzeugen()
  {
    return rechnungErzeugen;
  }

  public void setRechnungErzeugen(boolean rechnungErzeugen)
  {
    this.rechnungErzeugen = rechnungErzeugen;
  }

  public boolean isRechnungsDokumentSpeichern()
  {
    return rechnungsDokumentSpeichern;
  }

  public void setRechnungsDokumentSpeichern(boolean rechnungsDokumentSpeichern)
  {
    this.rechnungsDokumentSpeichern = rechnungsDokumentSpeichern;
  }

  public String getRechnungsText()
  {
    return rechnungsText;
  }

  public void setRechnungsText(String rechnungsText)
  {
    this.rechnungsText = rechnungsText;
  }

  public Date getRechnungsDatum()
  {
    return rechnungsDatum;
  }

  public void setRechnungsDatum(Date rechnungsDatum)
  {
    this.rechnungsDatum = rechnungsDatum;
  }

  public boolean isFixerBetragAbrechnen()
  {
    return fixerBetragAbrechnen;
  }

  public void setFixerBetragAbrechnen(boolean fixerBetragAbrechnen)
  {
    this.fixerBetragAbrechnen = fixerBetragAbrechnen;
  }

  public Double getFixerBetrag()
  {
    return fixerBetrag;
  }

  public void setFixerBetrag(Double fixerBetrag)
  {
    this.fixerBetrag = fixerBetrag;
  }

  public Buchungsart getBuchungsart()
  {
    return buchungsart;
  }

  public void setBuchungsart(Buchungsart buchungsart)
  {
    this.buchungsart = buchungsart;
  }

  public Buchungsklasse getBuchungsklasse()
  {
    return buchungsklasse;
  }

  public void setBuchungsklasse(Buchungsklasse buchungsklasse)
  {
    this.buchungsklasse = buchungsklasse;
  }

  public Steuer getSteuer()
  {
    return steuer;
  }

  public void setSteuer(Steuer steuer)
  {
    this.steuer = steuer;
  }
}
