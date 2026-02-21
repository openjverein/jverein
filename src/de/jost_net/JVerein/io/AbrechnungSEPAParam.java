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
import de.jost_net.JVerein.keys.Abrechnungsausgabe;
import de.jost_net.JVerein.keys.Monat;
import de.jost_net.JVerein.rmi.Formular;
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
  public final int abbuchungsmodus;

  public final int abrechnungsmonat;

  public final Date faelligkeit;

  public final Date stichtag;

  public final Abrechnungsausgabe abbuchungsausgabe;

  public final Date vondatum;

  public final Date bisdatum;

  public final String verwendungszweck;

  public final Boolean zusatzbetraege;

  public final Boolean kursteilnehmer;

  public final Boolean kompakteabbuchung;

  public final boolean sollbuchungenzusammenfassen;

  public final boolean rechnung;

  public final boolean rechnungsdokumentSpeichern;

  public final Formular rechnungsformular;

  public final String rechnungstext;

  public final Date rechnungsdatum;

  public final Boolean sepaprint;

  public final Boolean sepacheckdisable;

  public final File sepafileRCUR;

  public final SepaVersion sepaVersion;

  public final String pdffileRCUR;

  public final DBService service;

  public Konto konto;

  private String text = "";

  public Date voneingabedatum;

  List<Zusatzbetrag> zusatzbetraegeList;

  public AbrechnungSEPAParam(AbrechnungSEPAControl ac, File sepafileRCUR,
      SepaVersion sepaVersion, String pdffileRCUR)
      throws ApplicationException, RemoteException
  {
    zusatzbetraegeList = ac.getZusatzbetraegeList();
    abbuchungsmodus = (Integer) ac.getAbbuchungsmodus().getValue();
    Monat monat = (Monat) ac.getAbrechnungsmonat().getValue();
    abbuchungsausgabe = (Abrechnungsausgabe) ac.getAbbuchungsausgabe()
        .getValue();
    abrechnungsmonat = monat.getKey();
    faelligkeit = (Date) ac.getFaelligkeit().getValue();
    stichtag = ac.isStichtagSupported() ? (Date) ac.getStichtag().getValue()
        : null;
    vondatum = ac.isVondatumSupported() ? (Date) ac.getVondatum().getValue()
        : null;
    voneingabedatum = ac.isVoneingabgedatumSupported()
        ? (Date) ac.getVonEingabedatum().getValue()
        : null;
    bisdatum = ac.isBisdatumSupported() ? (Date) ac.getBisdatum().getValue()
        : null;
    verwendungszweck = (String) ac.getZahlungsgrund().getValue();
    zusatzbetraege = ac.isZusatzbetragSupported()
        ? (Boolean) ac.getZusatzbetrag().getValue()
        : false;
    kursteilnehmer = ac.isKursteilnehmerSupported()
        ? (Boolean) ac.getKursteilnehmer().getValue()
        : false;
    kompakteabbuchung = (Boolean) ac.getKompakteAbbuchung().getValue();
    sollbuchungenzusammenfassen = (Boolean) ac.getSollbuchungenZusammenfassen()
        .getValue();
    if (ac.isRechnungSupported())
    {
      rechnung = (Boolean) ac.getRechnung().getValue();
      if (ac.isRechnungsdokumentSupported())
      {
        rechnungsdokumentSpeichern = (Boolean) ac
            .getRechnungsdokumentSpeichern().getValue();
      }
      else
      {
        rechnungsdokumentSpeichern = false;
      }
      rechnungsformular = (Formular) ac.getRechnungFormular().getValue();
      rechnungstext = (String) ac.getRechnungstext().getValue();
      rechnungsdatum = (Date) ac.getRechnungsdatum().getValue();
    }
    else
    {
      rechnung = false;
      rechnungsdokumentSpeichern = false;
      rechnungsformular = null;
      rechnungstext = null;
      rechnungsdatum = null;
    }
    sepaprint = (Boolean) ac.getSEPAPrint().getValue();
    sepacheckdisable = (Boolean) ac.getSEPACheck().getValue();
    this.pdffileRCUR = pdffileRCUR;
    this.sepafileRCUR = sepafileRCUR;
    this.sepaVersion = sepaVersion;

    if (abbuchungsausgabe == Abrechnungsausgabe.HIBISCUS)
    {
      // DB-Service holen
      try
      {
        service = (DBService) Application.getServiceFactory().lookup(HBCI.class,
            "database");
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

  public String getText()
  {
    return text;
  }

  public void setText(String in)
  {
    text = in;
  }
}
