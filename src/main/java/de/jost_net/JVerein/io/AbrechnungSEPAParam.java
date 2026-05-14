/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.AbrechnungSEPAControl;
import de.jost_net.JVerein.gui.control.AbstractAbrechnungControl;
import de.jost_net.JVerein.gui.control.ForderungControl;
import de.jost_net.JVerein.gui.control.GutschriftControl;
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.Monat;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbrechnungSEPAParam
{
  // Abstract Abrechnung
  public Date faelligkeit;

  public Boolean kompakteabbuchung;

  public boolean sollbuchungenzusammenfassen;

  public boolean rechnung;

  public boolean rechnungsdokumentSpeichern;

  public Formular rechnungsformular;

  public String rechnungstext;

  public String rechnungskommentar;

  public Date rechnungsdatum;

  public Boolean sepaprint;

  public Boolean sepacheckdisable;

  public File sepafileRCUR = null;

  public String pdffileRCUR = null;

  public DBService service;

  public Konto konto;

  private String text = "";

  // Bei allen
  public Abrechnungsausgabe abbuchungsausgabe;

  public final int abbuchungsmodus;

  public final String verwendungszweck;

  public final SepaVersion sepaVersion;

  // Abrechnung
  public final int abrechnungsmonat;

  public final Date stichtag;

  public final Date vondatum;

  public final Date voneingabedatum;

  public final Date bisdatum;

  public final Boolean zusatzbetraege;

  public final Boolean kursteilnehmer;

  // Gutschrift
  public final Boolean fixerBetragAbrechnen;

  public final Double betrag;

  public final Buchungsart buchungsart;

  public final Buchungsklasse buchungsklasse;

  public final Steuer steuer;

  List<Zusatzbetrag> zusatzbetraegeList;

  public AbrechnungSEPAParam(GutschriftControl gc, SepaVersion sepaVersion)
      throws ApplicationException, RemoteException
  {
    setAbstractParams(gc);
    this.sepaVersion = sepaVersion;
    zusatzbetraegeList = null;
    abbuchungsmodus = Abrechnungsmodi.GUTSCHRIFT;
    verwendungszweck = (String) gc.getZweckInput().getValue();
    fixerBetragAbrechnen = (Boolean) gc.getFixerBetragAbrechnenInput()
        .getValue();
    if (fixerBetragAbrechnen)
    {
      betrag = (Double) gc.getFixerBetragInput().getValue();
      buchungsart = (Buchungsart) gc.getBuchungsartInput().getValue();
      buchungsklasse = gc.isBuchungsklasseInputActiv()
          ? (Buchungsklasse) gc.getBuchungsklasseInput().getValue()
          : null;
      steuer = gc.isSteuerInputActiv() ? (Steuer) gc.getSteuerInput().getValue()
          : null;
    }
    else
    {
      betrag = null;
      buchungsart = null;
      buchungsklasse = null;
      steuer = null;
    }
    abrechnungsmonat = 12;
    stichtag = faelligkeit;
    vondatum = null;
    voneingabedatum = null;
    bisdatum = null;
    zusatzbetraege = false;
    kursteilnehmer = false;
  }

  public AbrechnungSEPAParam(ForderungControl fc, SepaVersion sepaVersion)
      throws ApplicationException, RemoteException
  {
    setAbstractParams(fc);
    this.sepaVersion = sepaVersion;
    zusatzbetraegeList = fc.getZusatzbetraegeList();
    abbuchungsmodus = Abrechnungsmodi.FORDERUNG;
    verwendungszweck = (String) fc.getPart().getBuchungstext().getValue();
    abrechnungsmonat = 12;
    stichtag = null;
    vondatum = null;
    voneingabedatum = null;
    bisdatum = null;
    zusatzbetraege = false;
    kursteilnehmer = false;
    fixerBetragAbrechnen = true;
    betrag = (Double) fc.getPart().getBetrag().getValue();
    buchungsart = (Buchungsart) fc.getPart().getBuchungsart().getValue();
    buchungsklasse = fc.getPart().isBuchungsklasseActive()
        ? (Buchungsklasse) fc.getPart().getBuchungsklasse().getValue()
        : null;
    steuer = fc.getPart().isSteuerActive()
        ? (Steuer) fc.getPart().getSteuer().getValue()
        : null;
  }

  public AbrechnungSEPAParam(AbrechnungSEPAControl ac, SepaVersion sepaVersion)
      throws ApplicationException, RemoteException
  {
    setAbstractParams(ac);
    this.sepaVersion = sepaVersion;
    zusatzbetraegeList = null;
    abbuchungsmodus = (Integer) ac.getAbbuchungsmodus().getValue();
    verwendungszweck = (String) ac.getZahlungsgrund().getValue();
    if (ac.isAbrechnungsmonatSupported())
    {
      Monat monat = (Monat) ac.getAbrechnungsmonat().getValue();
      abrechnungsmonat = monat.getKey();
    }
    else
    {
      abrechnungsmonat = 12;
    }
    stichtag = ac.isStichtagActiv() ? (Date) ac.getStichtag().getValue() : null;
    vondatum = ac.isVondatumSupported() ? (Date) ac.getVondatum().getValue()
        : null;
    voneingabedatum = ac.isVoneingabgedatumSupported()
        ? (Date) ac.getVonEingabedatum().getValue()
        : null;
    bisdatum = ac.isBisdatumSupported() ? (Date) ac.getBisdatum().getValue()
        : null;
    zusatzbetraege = ac.isZusatzbetragSupported()
        ? (Boolean) ac.getZusatzbetrag().getValue()
        : false;
    kursteilnehmer = ac.isKursteilnehmerSupported()
        ? (Boolean) ac.getKursteilnehmer().getValue()
        : false;
    fixerBetragAbrechnen = false;
    betrag = null;
    buchungsart = null;
    buchungsklasse = null;
    steuer = null;
  }

  protected void setAbstractParams(AbstractAbrechnungControl ac)
      throws ApplicationException, RemoteException
  {
    faelligkeit = ac.isFaelligkeitActiv()
        ? (Date) ac.getFaelligkeit().getValue()
        : null;
    kompakteabbuchung = ac.isKompakteAbbuchungActiv()
        ? (Boolean) ac.getKompakteAbbuchung().getValue()
        : false;
    sollbuchungenzusammenfassen = ac.isSollbuchungenZusammenfassenActiv()
        ? (Boolean) ac.getSollbuchungenZusammenfassen().getValue()
        : false;
    sepaprint = ac.isSEPAPrintActiv() ? (Boolean) ac.getSEPAPrint().getValue()
        : false;
    sepacheckdisable = ac.isSEPACheckActiv()
        ? (Boolean) ac.getSEPACheck().getValue()
        : false;
    if (ac.isRechnungActiv())
    {
      rechnung = (Boolean) ac.getRechnung().getValue();
      rechnungsdokumentSpeichern = ac.isRechnungsdokumentSpeichernActiv()
          ? (Boolean) ac.getRechnungsdokumentSpeichern().getValue()
          : false;
      rechnungsformular = ac.isRechnungsformularActiv()
          ? (Formular) ac.getRechnungsformular().getValue()
          : null;
      rechnungstext = ac.isRechnungstextActiv()
          ? (String) ac.getRechnungstext(null).getValue()
          : null;
      rechnungsdatum = ac.isRechnungsdatumActiv()
          ? (Date) ac.getRechnungsdatum().getValue()
          : null;
      rechnungskommentar = ac.isRechnungskommentarActiv()
          ? (String) ac.getRechnungskommentar().getValue()
          : null;
    }
    else
    {
      rechnung = false;
      rechnungsdokumentSpeichern = false;
      rechnungsformular = null;
      rechnungstext = null;
      rechnungskommentar = null;
      rechnungsdatum = null;
    }

    if (ac.isAbbuchungsausgabeActiv())
    {
      abbuchungsausgabe = (Abrechnungsausgabe) ac.getAbbuchungsausgabe()
          .getValue();

      if (abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS)
      {
        // DB-Service holen
        try
        {
          service = (DBService) Application.getServiceFactory()
              .lookup(HBCI.class, "database");
          DBIterator<Konto> konten = service.createList(Konto.class);
          Logger.debug("Vereinskonto: "
              + (String) Einstellungen.getEinstellung(Property.IBAN));
          while (konten.hasNext())
          {
            konto = (Konto) konten.next();
            Logger.debug("Hibiscus-Konto: " + konto.getIban());
            if (((String) Einstellungen.getEinstellung(Property.IBAN))
                .equals(konto.getIban()))
            {
              // passendes Konto gefunden
              break;
            }
            else
            {
              konto = null;
            }
          }
          if (konto == null)
          {
            // Kein passendes Konto gefunden. Deshalb Kontoauswahldialog.
            KontoAuswahlDialog d = new KontoAuswahlDialog(
                KontoAuswahlDialog.POSITION_CENTER);
            konto = (Konto) d.open();
            if (konto == null)
            {
              throw new ApplicationException("Bitte wählen Sie ein Konto aus");
            }
          }
        }
        catch (OperationCanceledException e)
        {
          throw new ApplicationException("Bitte wählen Sie ein Konto aus");
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          throw new ApplicationException(
              "Hibiscus-Datenbank kann nicht geöffnet werden.");
        }
      }
      else
      {
        service = null;
      }
    }
  }

  public String getText()
  {
    return text;
  }

  public void setText(String in)
  {
    text = in;
  }
}
