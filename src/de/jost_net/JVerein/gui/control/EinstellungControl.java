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
package de.jost_net.JVerein.gui.control;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.EmailInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.KontoauswahlInput;
import de.jost_net.JVerein.gui.input.SEPALandInput;
import de.jost_net.JVerein.gui.input.SEPALandObject;
import de.jost_net.JVerein.gui.input.StaatSearchInput;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.io.MailSender.IMAPCopyData;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.keys.AfaOrt;
import de.jost_net.JVerein.keys.Altermodel;
import de.jost_net.JVerein.keys.ArbeitsstundenModel;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.MailAnhang;

import de.jost_net.JVerein.util.SteuerUtil;

import de.jost_net.JVerein.util.MitgliedSpaltenauswahl;
import de.jost_net.OBanToo.SEPA.Land.SEPALaender;
import de.jost_net.OBanToo.SEPA.Land.SEPALand;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.DirectoryInput;
import de.willuhn.jameica.gui.input.ImageInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.PasswordInput;
import de.willuhn.jameica.gui.input.ScaleInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;

public class EinstellungControl extends AbstractControl
{

  private Input name;

  private Input strasse;

  private Input plz;

  private Input ort;

  private TextInput finanzamt;

  private TextInput steuernummer;

  private DateInput bescheiddatum;

  private CheckboxInput vorlaeufig;

  private DateInput veranlagungvon;

  private DateInput veranlagungbis;

  private TextInput beguenstigterzweck;

  private CheckboxInput mitgliedsbetraege;

  private TextInput bic;

  private IBANInput iban;

  private TextInput glaeubigerid;

  private CheckboxInput geburtsdatumpflicht;

  private CheckboxInput nichtmitgliedgeburtsdatumpflicht;

  private CheckboxInput nichtmitgliedpflichteigenschaften;

  private CheckboxInput jmitgliedpflichteigenschaften;

  private CheckboxInput jnichtmitgliedpflichteigenschaften;

  private CheckboxInput eintrittsdatumpflicht;

  private CheckboxInput sterbedatum;

  private CheckboxInput kommunikationsdaten;

  private CheckboxInput sekundaerebeitragsgruppen;

  private CheckboxInput zusatzbetrag;

  private CheckboxInput vermerke;

  private CheckboxInput wiedervorlage;

  private CheckboxInput kursteilnehmer;

  private CheckboxInput lehrgaenge;

  private CheckboxInput juristischepersonen;

  private CheckboxInput mitgliedfoto;

  private CheckboxInput uselesefelder;

  private CheckboxInput usezusatzfelder;

  private CheckboxInput zusatzadressen;

  private CheckboxInput auslandsadressen;

  private CheckboxInput arbeitseinsatz;

  private CheckboxInput dokumentenspeicherung;

  private CheckboxInput individuellebeitraege;

  private CheckboxInput kursteilnehmergebpflicht;

  private CheckboxInput kursteilnehmergespflicht;

  private TextInput rechnungtextabbuchung;

  private TextInput rechnungtextueberweisung;

  private TextInput rechnungtextbar;

  private IntegerInput zaehlerlaenge;

  private CheckboxInput externemitgliedsnummer;

  private SelectInput arbeitsstundenmodel;

  private SelectInput beitragsmodel;

  private SelectInput sepamandatidsourcemodel;

  private TextInput dateinamenmuster;

  private DirectoryInput vorlagenCsvVerzeichnis;

  private DecimalInput spendenbescheinigungminbetrag;

  private DirectoryInput spendenbescheinigungverzeichnis;

  private CheckboxInput spendenbescheinigungprintbuchungsart;

  private TextInput beginngeschaeftsjahr;

  private CheckboxInput autobuchunguebernahme;

  private CheckboxInput unterdrueckungohnebuchung;

  private CheckboxInput kontonummer_in_buchungsliste;

  private IntegerInput unterdrueckunglaenge;

  private IntegerInput unterdrueckungkonten;

  private CheckboxInput automatische_buchungskorrektur_hibiscus;

  private TextInput smtp_server;

  private IntegerInput smtp_port;

  private TextInput smtp_auth_user;

  private PasswordInput smtp_auth_pwd;

  private EmailInput smtp_from_address;

  private TextInput smtp_from_anzeigename;

  private CheckboxInput smtp_ssl;

  private CheckboxInput smtp_starttls;

  private ScaleInput mailverzoegerung;

  private TextInput alwaysBccTo;

  private TextInput alwaysCcTo;

  private CheckboxInput copyToImapFolder;

  private TextInput imapAuthUser;

  private PasswordInput imapAuthPwd;

  private TextInput imapHost;

  private IntegerInput imapPort;

  private CheckboxInput imap_ssl;

  private CheckboxInput imap_starttls;

  private TextInput imapSentFolder;

  private TextAreaInput mailsignatur;

  private SelectInput zahlungsweg;

  private SelectInput zahlungsrhytmus;

  private SelectInput sepaland;

  private SelectInput sepaversion;

  private SelectInput ct1sepaversion;

  private Input altersgruppen;

  private Input jubilaeen;

  private Input altersjubilaeen;

  private IntegerInput jubilarStartAlter;

  private Settings settings;

  private MitgliedSpaltenauswahl spalten;

  private IntegerInput AnzahlSpaltenStammdatenInput;

  private IntegerInput AnzahlSpaltenZusatzfelderInput;

  private IntegerInput AnzahlSpaltenMitgliedschaftInput;

  private IntegerInput AnzahlSpaltenZahlungInput;

  private CheckboxInput ZeigeStammdatenInTabInput;

  private CheckboxInput ZeigeMitgliedschaftInTabInput;

  private CheckboxInput ZeigeZahlungInTabInput;

  private CheckboxInput ZeigeZusatzbeitraegeInTabInput;

  private CheckboxInput ZeigeMitgliedskontoInTabInput;

  private CheckboxInput ZeigeVermerkeInTabInput;

  private CheckboxInput ZeigeWiedervorlageInTabInput;

  private CheckboxInput ZeigeMailsInTabInput;

  private CheckboxInput ZeigeEigenschaftenInTabInput;

  private CheckboxInput ZeigeZusatzfelderInTabInput;

  private CheckboxInput ZeigeLehrgaengeInTabInput;

  private CheckboxInput ZeigeFotoInTabInput;

  private CheckboxInput ZeigeLesefelderInTabInput;

  private CheckboxInput ZeigeArbeitseinsatzInTabInput;

  private CheckboxInput ZeigeDokumenteInTabInput;

  private CheckboxInput zusatzbetragAusgetretene;

  private CheckboxInput keineistbuchungbeilastschrift;

  private SelectInput altersmodel;

  private ScaleInput sepadatumoffset;

  private SelectInput buchungBuchungsartAuswahl;

  private SelectInput mitgliedAuswahl;

  private SelectInput buchungsartsort;

  private CheckboxInput abrlabschliessen;

  private CheckboxInput optiert;

  private CheckboxInput optiertpflicht;

  private CheckboxInput unterschriftdrucken;

  private ImageInput unterschrift;

  private CheckboxInput anhangspeichern;

  private CheckboxInput freiebuchungsklasse;

  private CheckboxInput wirtschaftsplanung;

  private CheckboxInput summenAnlagenkonto;

  private IntegerInput qrcodesize;

  private CheckboxInput qrcodeptext;

  private CheckboxInput qrcodepdate;

  private CheckboxInput qrcodeprenum;

  private CheckboxInput qrcodepmnum;

  private TextInput qrcodetext;

  private CheckboxInput qrcodesngl;

  private TextInput qrcodeinfom;

  private TextInput qrcodeintro;

  private CheckboxInput qrcodekuerzen;

  private DecimalInput afarestwert;

  private SelectInput afaort;

  private TextInput beitragaltersstufen;

  private CheckboxInput nummeranzeigen;

  private CheckboxInput familienbeitrag;

  private CheckboxInput anlagenkonten;

  private CheckboxInput ruecklagenkonten;

  private CheckboxInput verbindlichkeitenforderungen;

  private CheckboxInput mittelverwendung;

  private CheckboxInput projekte;

  private CheckboxInput spendenbescheinigungen;

  private CheckboxInput rechnungen;

  private TextInput ustid;

  private StaatSearchInput staat;

  private DialogInput verrechnungskonto;

  private CheckboxInput splitpositionzweck;

  private CheckboxInput geprueftsynchronisieren;

  private CheckboxInput steuerInBuchung;

  public EinstellungControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Input getName(boolean withFocus) throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput((String) Einstellungen.getEinstellung(Property.NAME),
        70);
    name.setMandatory(true);
    if (withFocus)
    {
      name.focus();
    }
    return name;
  }

  public Input getStrasse() throws RemoteException
  {
    if (strasse != null)
    {
      return strasse;
    }
    strasse = new TextInput(
        (String) Einstellungen.getEinstellung(Property.STRASSE), 50);
    return strasse;
  }

  public Input getPlz() throws RemoteException
  {
    if (plz != null)
    {
      return plz;
    }
    plz = new TextInput((String) Einstellungen.getEinstellung(Property.PLZ), 5);
    return plz;
  }

  public Input getOrt() throws RemoteException
  {
    if (ort != null)
    {
      return ort;
    }
    ort = new TextInput((String) Einstellungen.getEinstellung(Property.ORT),
        50);
    return ort;
  }

  public StaatSearchInput getStaat() throws RemoteException
  {
    if (staat != null)
    {
      return staat;
    }
    staat = new StaatSearchInput();
    staat.setSearchString("Zum Suchen tippen");
    staat.setValue(
        Staat.getByKey((String) Einstellungen.getEinstellung(Property.STAAT)));
    staat.setName("Staat");
    return staat;
  }

  public Input getUstID() throws RemoteException
  {
    if (ustid != null)
    {
      return ustid;
    }
    ustid = new TextInput((String) Einstellungen.getEinstellung(Property.USTID),
        50);
    return ustid;
  }

  public TextInput getFinanzamt() throws RemoteException
  {
    if (finanzamt != null)
    {
      return finanzamt;
    }
    finanzamt = new TextInput(
        (String) Einstellungen.getEinstellung(Property.FINANZAMT), 30);
    return finanzamt;
  }

  public TextInput getSteuernummer() throws RemoteException
  {
    if (steuernummer != null)
    {
      return steuernummer;
    }
    steuernummer = new TextInput(
        (String) Einstellungen.getEinstellung(Property.STEUERNUMMER), 30);
    return steuernummer;
  }

  public DateInput getBescheiddatum() throws RemoteException
  {
    if (bescheiddatum != null)
    {
      return bescheiddatum;
    }
    bescheiddatum = new DateInput(
        (Date) Einstellungen.getEinstellung(Property.BESCHEIDDATUM));
    return bescheiddatum;
  }

  public CheckboxInput getVorlaeufig() throws RemoteException
  {
    if (vorlaeufig != null)
    {
      return vorlaeufig;
    }
    vorlaeufig = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.VORLAEUFIG));
    return vorlaeufig;
  }

  public DateInput getVeranlagungVon() throws RemoteException
  {
    if (veranlagungvon != null)
    {
      return veranlagungvon;
    }
    veranlagungvon = new DateInput(
        (Date) Einstellungen.getEinstellung(Property.VERANLAGUNGVON));
    return veranlagungvon;
  }

  public DateInput getVeranlagungBis() throws RemoteException
  {
    if (veranlagungbis != null)
    {
      return veranlagungbis;
    }
    veranlagungbis = new DateInput(
        (Date) Einstellungen.getEinstellung(Property.VERANLAGUNGBIS));
    return veranlagungbis;
  }

  public TextInput getBeguenstigterzweck() throws RemoteException
  {
    if (beguenstigterzweck != null)
    {
      return beguenstigterzweck;
    }
    beguenstigterzweck = new TextInput(
        (String) Einstellungen.getEinstellung(Property.BEGUENSTIGTERZWECK),
        100);
    return beguenstigterzweck;
  }

  public CheckboxInput getMitgliedsbetraege() throws RemoteException
  {
    if (mitgliedsbetraege != null)
    {
      return mitgliedsbetraege;
    }
    mitgliedsbetraege = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.MITGLIEDSBETRAEGE));
    return mitgliedsbetraege;
  }

  public TextInput getBic() throws RemoteException
  {
    if (bic != null && !bic.getControl().isDisposed())
    {
      return bic;
    }
    bic = new BICInput((String) Einstellungen.getEinstellung(Property.BIC));
    return bic;
  }

  public IBANInput getIban() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }
    iban = new IBANInput(new IBANFormatter()
        .format((String) Einstellungen.getEinstellung(Property.IBAN)), bic);
    return iban;
  }

  public TextInput getGlaeubigerID() throws RemoteException
  {
    if (glaeubigerid != null)
    {
      return glaeubigerid;
    }
    glaeubigerid = new TextInput(
        (String) Einstellungen.getEinstellung(Property.GLAEUBIGERID), 35);
    return glaeubigerid;
  }

  public ScaleInput getSEPADatumOffset() throws RemoteException
  {
    if (sepadatumoffset != null)
    {
      return sepadatumoffset;
    }
    sepadatumoffset = new ScaleInput(
        (Integer) Einstellungen.getEinstellung(Property.SEPADATUMOFFSET),
        SWT.HORIZONTAL);
    sepadatumoffset.setScaling(0, 14, 1, 1);
    sepadatumoffset.setName("Zusätzliche SEPA-Vorlaufzeit");
    SEPADatumOffsetListener listener = new SEPADatumOffsetListener();
    sepadatumoffset.addListener(listener);
    listener.handleEvent(null); // einmal initial ausloesen
    return sepadatumoffset;
  }

  public CheckboxInput getGeburtsdatumPflicht() throws RemoteException
  {
    if (geburtsdatumpflicht != null)
    {
      return geburtsdatumpflicht;
    }
    geburtsdatumpflicht = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT));
    return geburtsdatumpflicht;
  }

  public CheckboxInput getNichtMitgliedGeburtsdatumPflicht()
      throws RemoteException
  {
    if (nichtmitgliedgeburtsdatumpflicht != null)
    {
      return nichtmitgliedgeburtsdatumpflicht;
    }
    nichtmitgliedgeburtsdatumpflicht = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT));
    return nichtmitgliedgeburtsdatumpflicht;
  }

  public CheckboxInput getNichtMitgliedPflichtEigenschaften()
      throws RemoteException
  {
    if (nichtmitgliedpflichteigenschaften != null)
    {
      return nichtmitgliedpflichteigenschaften;
    }
    nichtmitgliedpflichteigenschaften = new CheckboxInput(
        (Boolean) Einstellungen
            .getEinstellung(Property.NICHTMITGLIEDPFLICHTEIGENSCHAFTEN));
    return nichtmitgliedpflichteigenschaften;
  }

  public CheckboxInput getJMitgliedPflichtEigenschaften() throws RemoteException
  {
    if (jmitgliedpflichteigenschaften != null)
    {
      return jmitgliedpflichteigenschaften;
    }
    jmitgliedpflichteigenschaften = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.JMITGLIEDPFLICHTEIGENSCHAFTEN));
    return jmitgliedpflichteigenschaften;
  }

  public CheckboxInput getJNichtMitgliedPflichtEigenschaften()
      throws RemoteException
  {
    if (jnichtmitgliedpflichteigenschaften != null)
    {
      return jnichtmitgliedpflichteigenschaften;
    }
    jnichtmitgliedpflichteigenschaften = new CheckboxInput(
        (Boolean) Einstellungen
            .getEinstellung(Property.JNICHTMITGLIEDPFLICHTEIGENSCHAFTEN));
    return jnichtmitgliedpflichteigenschaften;
  }

  public CheckboxInput getEintrittsdatumPflicht() throws RemoteException
  {
    if (eintrittsdatumpflicht != null)
    {
      return eintrittsdatumpflicht;
    }
    eintrittsdatumpflicht = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.EINTRITTSDATUMPFLICHT));
    return eintrittsdatumpflicht;
  }

  public CheckboxInput getSterbedatum() throws RemoteException
  {
    if (sterbedatum != null)
    {
      return sterbedatum;
    }
    sterbedatum = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM));
    return sterbedatum;
  }

  public CheckboxInput getKommunikationsdaten() throws RemoteException
  {
    if (kommunikationsdaten != null)
    {
      return kommunikationsdaten;
    }
    kommunikationsdaten = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.KOMMUNIKATIONSDATEN));
    return kommunikationsdaten;
  }

  public CheckboxInput getSekundaereBeitragsgruppen() throws RemoteException
  {
    if (sekundaerebeitragsgruppen != null)
    {
      return sekundaerebeitragsgruppen;
    }
    sekundaerebeitragsgruppen = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN));
    return sekundaerebeitragsgruppen;
  }

  public CheckboxInput getZusatzbetrag() throws RemoteException
  {
    if (zusatzbetrag != null)
    {
      return zusatzbetrag;
    }
    zusatzbetrag = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG));
    return zusatzbetrag;
  }

  public CheckboxInput getVermerke() throws RemoteException
  {
    if (vermerke != null)
    {
      return vermerke;
    }
    vermerke = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.VERMERKE));
    return vermerke;
  }

  public CheckboxInput getWiedervorlage() throws RemoteException
  {
    if (wiedervorlage != null)
    {
      return wiedervorlage;
    }
    wiedervorlage = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.WIEDERVORLAGE));
    return wiedervorlage;
  }

  public CheckboxInput getKursteilnehmer() throws RemoteException
  {
    if (kursteilnehmer != null)
    {
      return kursteilnehmer;
    }
    kursteilnehmer = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER));
    return kursteilnehmer;
  }

  public CheckboxInput getLehrgaenge() throws RemoteException
  {
    if (lehrgaenge != null)
    {
      return lehrgaenge;
    }
    lehrgaenge = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.LEHRGAENGE));
    return lehrgaenge;
  }

  public CheckboxInput getJuristischePersonen() throws RemoteException
  {
    if (juristischepersonen != null)
    {
      return juristischepersonen;
    }
    juristischepersonen = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.JURISTISCHEPERSONEN));
    return juristischepersonen;
  }

  public CheckboxInput getMitgliedfoto() throws RemoteException
  {
    if (mitgliedfoto != null)
    {
      return mitgliedfoto;
    }
    mitgliedfoto = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.MITGLIEDFOTO));
    return mitgliedfoto;
  }

  public CheckboxInput getKursteilnehmerGebPflicht() throws RemoteException
  {
    if (kursteilnehmergebpflicht != null)
    {
      return kursteilnehmergebpflicht;
    }
    kursteilnehmergebpflicht = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.KURSTEILNEHMERGEBPFLICHT));
    return kursteilnehmergebpflicht;
  }

  public CheckboxInput getKursteilnehmerGesPflicht() throws RemoteException
  {
    if (kursteilnehmergespflicht != null)
    {
      return kursteilnehmergespflicht;
    }
    kursteilnehmergespflicht = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.KURSTEILNEHMERGESPFLICHT));
    return kursteilnehmergespflicht;
  }

  public CheckboxInput getUseZusatzfelder() throws RemoteException
  {
    if (usezusatzfelder == null)
    {
      usezusatzfelder = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.USEZUSATZFELDER));
    }
    return usezusatzfelder;
  }

  public CheckboxInput getUseLesefelder() throws RemoteException
  {
    if (uselesefelder == null)
    {
      uselesefelder = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.USELESEFELDER));
    }
    return uselesefelder;
  }

  public CheckboxInput getZusatzadressen() throws RemoteException
  {
    if (zusatzadressen != null)
    {
      return zusatzadressen;
    }
    zusatzadressen = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.ZUSATZADRESSEN));
    return zusatzadressen;
  }

  public CheckboxInput getAuslandsadressen() throws RemoteException
  {
    if (auslandsadressen != null)
    {
      return auslandsadressen;
    }
    auslandsadressen = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.AUSLANDSADRESSEN));
    return auslandsadressen;
  }

  public CheckboxInput getArbeitseinsatz() throws RemoteException
  {
    if (arbeitseinsatz != null)
    {
      return arbeitseinsatz;
    }
    arbeitseinsatz = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.ARBEITSEINSATZ));
    return arbeitseinsatz;
  }

  public CheckboxInput getDokumentenspeicherung() throws RemoteException
  {
    if (dokumentenspeicherung != null)
    {
      return dokumentenspeicherung;
    }
    dokumentenspeicherung = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG));
    return dokumentenspeicherung;
  }

  public CheckboxInput getIndividuelleBeitraege() throws RemoteException
  {
    if (individuellebeitraege != null)
    {
      return individuellebeitraege;
    }
    individuellebeitraege = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.INDIVIDUELLEBEITRAEGE));
    return individuellebeitraege;
  }

  public TextInput getRechnungTextAbbuchung() throws RemoteException
  {
    if (rechnungtextabbuchung != null)
    {
      return rechnungtextabbuchung;
    }
    rechnungtextabbuchung = new TextInput(
        (String) Einstellungen.getEinstellung(Property.RECHNUNGTEXTABBUCHUNG),
        500);
    return rechnungtextabbuchung;
  }

  public TextInput getRechnungTextUeberweisung() throws RemoteException
  {
    if (rechnungtextueberweisung != null)
    {
      return rechnungtextueberweisung;
    }
    rechnungtextueberweisung = new TextInput((String) Einstellungen
        .getEinstellung(Property.RECHNUNGTEXTUEBERWEISUNG), 500);
    return rechnungtextueberweisung;
  }

  public TextInput getRechnungTextBar() throws RemoteException
  {
    if (rechnungtextbar != null)
    {
      return rechnungtextbar;
    }
    rechnungtextbar = new TextInput(
        (String) Einstellungen.getEinstellung(Property.RECHNUNGTEXTBAR), 500);
    return rechnungtextbar;
  }

  public IntegerInput getZaehlerLaenge() throws RemoteException
  {
    if (null == zaehlerlaenge)
    {
      zaehlerlaenge = new IntegerInput(
          (Integer) Einstellungen.getEinstellung(Property.ZAEHLERLAENGE));
    }
    return zaehlerlaenge;
  }

  public CheckboxInput getOptiert() throws RemoteException
  {
    if (optiert != null)
    {
      return optiert;
    }
    optiert = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.OPTIERT));
    optiert.addListener(e -> {
      try
      {
        getOptiertPflicht().setValue(Boolean.FALSE);
        getSteuerInBuchung().setValue(Boolean.FALSE);
        getOptiertPflicht().setEnabled((boolean) optiert.getValue());
        getSteuerInBuchung().setEnabled((boolean) optiert.getValue());
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler beim Optiert-Listener", e1);
      }
    });
    return optiert;
  }

  public CheckboxInput getOptiertPflicht() throws RemoteException
  {
    if (optiertpflicht != null)
    {
      return optiertpflicht;
    }
    optiertpflicht = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.OPTIERTPFLICHT));
    optiertpflicht.setEnabled((boolean) getOptiert().getValue());
    return optiertpflicht;
  }

  public CheckboxInput getSteuerInBuchung() throws RemoteException
  {
    if (steuerInBuchung != null)
    {
      return steuerInBuchung;
    }
    steuerInBuchung = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG));
    steuerInBuchung.setEnabled((boolean) getOptiert().getValue());
    return steuerInBuchung;
  }

  public CheckboxInput getSplitPositionZweck() throws RemoteException
  {
    if (splitpositionzweck != null)
    {
      return splitpositionzweck;
    }
    splitpositionzweck = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.SPLITPOSITIONZWECK));
    return splitpositionzweck;
  }

  public CheckboxInput getGeprueftSynchronisieren() throws RemoteException
  {
    if (geprueftsynchronisieren != null)
    {
      return geprueftsynchronisieren;
    }
    geprueftsynchronisieren = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.GEPRUEFTSYNCHRONISIEREN));
    return geprueftsynchronisieren;
  }

  public CheckboxInput getFreieBuchungsklasse() throws RemoteException
  {
    if (freiebuchungsklasse != null)
    {
      return freiebuchungsklasse;
    }
    freiebuchungsklasse = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG));
    return freiebuchungsklasse;
  }

  public CheckboxInput getWirtschaftsplanung() throws RemoteException
  {
    if (wirtschaftsplanung != null)
    {
      return wirtschaftsplanung;
    }
    wirtschaftsplanung = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.WIRTSCHAFTSPLANANZEIGEN));
    return wirtschaftsplanung;
  }

  public CheckboxInput getExterneMitgliedsnummer() throws RemoteException
  {
    if (externemitgliedsnummer != null)
    {
      return externemitgliedsnummer;
    }
    externemitgliedsnummer = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER));
    return externemitgliedsnummer;
  }

  public SelectInput getBeitragsmodel() throws RemoteException
  {
    if (beitragsmodel != null)
    {
      return beitragsmodel;
    }
    beitragsmodel = new SelectInput(Beitragsmodel.values(),
        Beitragsmodel.getByKey(
            (Integer) Einstellungen.getEinstellung(Property.BEITRAGSMODEL)));
    return beitragsmodel;
  }

  public SelectInput getArbeitsstundenmodel() throws RemoteException
  {
    if (arbeitsstundenmodel != null)
    {
      return arbeitsstundenmodel;
    }
    arbeitsstundenmodel = new SelectInput(ArbeitsstundenModel.getArray(),
        new ArbeitsstundenModel((Integer) Einstellungen
            .getEinstellung(Property.ARBEITSSTUNDENMODEL)));
    return arbeitsstundenmodel;
  }

  public Input getBeitragAltersgruppen() throws RemoteException
  {
    if (beitragaltersstufen != null)
    {
      return beitragaltersstufen;
    }
    beitragaltersstufen = new TextInput(
        (String) Einstellungen.getEinstellung(Property.BEITRAGALTERSSTUFEN),
        200);
    return beitragaltersstufen;
  }

  public SelectInput getSepamandatidsourcemodel() throws RemoteException
  {
    if (sepamandatidsourcemodel != null)
    {
      return sepamandatidsourcemodel;
    }
    sepamandatidsourcemodel = new SelectInput(SepaMandatIdSource.getArray(),
        new SepaMandatIdSource((Integer) Einstellungen
            .getEinstellung(Property.SEPAMANDATIDSOURCE)));
    return sepamandatidsourcemodel;
  }

  public SelectInput getAltersModel() throws RemoteException
  {
    if (null != altersmodel)
    {
      return altersmodel;
    }

    altersmodel = new SelectInput(Altermodel.getArray(), new Altermodel(
        (Integer) Einstellungen.getEinstellung(Property.ALTERSMODEL)));

    return altersmodel;
  }

  public TextInput getDateinamenmuster() throws RemoteException
  {
    if (dateinamenmuster != null)
    {
      return dateinamenmuster;
    }
    dateinamenmuster = new TextInput(
        (String) Einstellungen.getEinstellung(Property.DATEINAMENMUSTER), 30);
    dateinamenmuster
        .setComment("a$ = Aufgabe, d$ = Datum, s$ = Sortierung, z$ = Zeit");
    return dateinamenmuster;
  }

  public DirectoryInput getVorlagenCsvVerzeichnis() throws RemoteException
  {
    if (vorlagenCsvVerzeichnis != null)
    {
      return vorlagenCsvVerzeichnis;
    }
    String lastValue = (String) Einstellungen
        .getEinstellung(Property.VORLAGENCSVVERZEICHNIS);
    vorlagenCsvVerzeichnis = new DirectoryInput(lastValue);
    return vorlagenCsvVerzeichnis;
  }

  public DecimalInput getSpendenbescheinigungminbetrag() throws RemoteException
  {
    if (spendenbescheinigungminbetrag != null)
    {
      return spendenbescheinigungminbetrag;
    }
    spendenbescheinigungminbetrag = new DecimalInput(
        (Double) Einstellungen
            .getEinstellung(Property.SPENDENBESCHEINIGUNGMINBETRAG),
        new DecimalFormat("###0.00"));
    return spendenbescheinigungminbetrag;
  }

  public DirectoryInput getSpendenbescheinigungverzeichnis()
      throws RemoteException
  {
    if (spendenbescheinigungverzeichnis != null)
    {
      return spendenbescheinigungverzeichnis;
    }
    spendenbescheinigungverzeichnis = new DirectoryInput((String) Einstellungen
        .getEinstellung(Property.SPENDENBESCHEINIGUNGVERZEICHNIS));
    return spendenbescheinigungverzeichnis;
  }

  public CheckboxInput getSpendenbescheinigungPrintBuchungsart()
      throws RemoteException
  {
    if (spendenbescheinigungprintbuchungsart != null)
    {
      return spendenbescheinigungprintbuchungsart;
    }
    spendenbescheinigungprintbuchungsart = new CheckboxInput(
        (Boolean) Einstellungen
            .getEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART));
    return spendenbescheinigungprintbuchungsart;
  }

  public TextInput getBeginnGeschaeftsjahr() throws RemoteException
  {
    if (beginngeschaeftsjahr != null)
    {
      return beginngeschaeftsjahr;
    }
    beginngeschaeftsjahr = new TextInput(
        (String) Einstellungen.getEinstellung(Property.BEGINNGESCHAEFTSJAHR),
        6);
    return beginngeschaeftsjahr;
  }

  public CheckboxInput getAutoBuchunguebernahme() throws RemoteException
  {
    if (autobuchunguebernahme != null)
    {
      return autobuchunguebernahme;
    }
    autobuchunguebernahme = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.AUTOBUCHUNGUEBERNAHME));
    return autobuchunguebernahme;
  }

  public CheckboxInput getUnterdrueckungOhneBuchung() throws RemoteException
  {
    if (unterdrueckungohnebuchung != null)
    {
      return unterdrueckungohnebuchung;
    }
    unterdrueckungohnebuchung = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG));
    return unterdrueckungohnebuchung;
  }

  public IntegerInput getUnterdrueckungLaenge() throws RemoteException
  {
    if (null == unterdrueckunglaenge)
    {
      unterdrueckunglaenge = new IntegerInput((Integer) Einstellungen
          .getEinstellung(Property.UNTERDRUECKUNGLAENGE));
    }
    return unterdrueckunglaenge;
  }

  public IntegerInput getUnterdrueckungKonten() throws RemoteException
  {
    if (null == unterdrueckungkonten)
    {
      unterdrueckungkonten = new IntegerInput((Integer) Einstellungen
          .getEinstellung(Property.UNTERDRUECKUNGKONTEN));
    }
    return unterdrueckungkonten;
  }

  public CheckboxInput getKontonummerInBuchungsliste() throws RemoteException
  {
    if (kontonummer_in_buchungsliste != null)
    {
      return kontonummer_in_buchungsliste;
    }
    kontonummer_in_buchungsliste = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.KONTONUMMERINBUCHUNGSLISTE));
    return kontonummer_in_buchungsliste;
  }

  public CheckboxInput getAutomatischeBuchungskorrekturHibiscus()
      throws RemoteException
  {
    if (automatische_buchungskorrektur_hibiscus != null)
    {
      return automatische_buchungskorrektur_hibiscus;
    }
    automatische_buchungskorrektur_hibiscus = new CheckboxInput(
        (Boolean) Einstellungen
            .getEinstellung(Property.AUTOMATISCHEBUCHUNGSKORREKTURHIBISCUS));
    return automatische_buchungskorrektur_hibiscus;
  }

  public TextInput getSmtpServer() throws RemoteException
  {
    if (smtp_server != null)
    {
      return smtp_server;
    }
    smtp_server = new TextInput(
        (String) Einstellungen.getEinstellung(Property.SMTPSERVER), 50);
    return smtp_server;
  }

  public IntegerInput getSmtpPort() throws RemoteException
  {
    if (smtp_port != null)
    {
      return smtp_port;
    }
    String port = (String) Einstellungen.getEinstellung(Property.SMTPPORT);
    if (port != null && port.length() > 0)
    {
      smtp_port = new IntegerInput(Integer.valueOf(port));
    }
    else
    {
      smtp_port = new IntegerInput();
    }
    return smtp_port;
  }

  public TextInput getSmtpAuthUser() throws RemoteException
  {
    if (smtp_auth_user != null)
    {
      return smtp_auth_user;
    }
    smtp_auth_user = new TextInput(
        (String) Einstellungen.getEinstellung(Property.SMTPAUTHUSER), 140);
    return smtp_auth_user;
  }

  public PasswordInput getSmtpAuthPwd() throws RemoteException
  {
    if (smtp_auth_pwd != null)
    {
      return smtp_auth_pwd;
    }
    smtp_auth_pwd = new PasswordInput(Einstellungen.getSmtpAuthPwd());
    return smtp_auth_pwd;
  }

  public EmailInput getSmtpFromAddress() throws RemoteException
  {
    if (smtp_from_address != null)
    {
      return smtp_from_address;
    }
    smtp_from_address = new EmailInput(
        (String) Einstellungen.getEinstellung(Property.SMTPFROMADDRESS));
    return smtp_from_address;
  }

  public TextInput getSmtpFromAnzeigename() throws RemoteException
  {
    if (smtp_from_anzeigename != null)
    {
      return smtp_from_anzeigename;
    }
    smtp_from_anzeigename = new TextInput(
        (String) Einstellungen.getEinstellung(Property.SMTPFROMANZEIGENAME),
        50);
    return smtp_from_anzeigename;
  }

  public CheckboxInput getSmtpSsl() throws RemoteException
  {
    if (smtp_ssl != null)
    {
      return smtp_ssl;
    }
    smtp_ssl = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.SMTPSSL));
    return smtp_ssl;
  }

  public CheckboxInput getSmtpStarttls() throws RemoteException
  {
    if (smtp_starttls != null)
    {
      return smtp_starttls;
    }
    smtp_starttls = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.SMTPSTARTTLS));
    return smtp_starttls;
  }

  public ScaleInput getMailVerzoegerung() throws RemoteException
  {
    if (mailverzoegerung != null)
    {
      return mailverzoegerung;
    }
    mailverzoegerung = new ScaleInput(
        (Integer) Einstellungen.getEinstellung(Property.MAILVERZOEGERUNG));
    mailverzoegerung.setScaling(0, 10000, 100, 100);
    mailverzoegerung.setComment("");
    MailVerzoegerungListener listener = new MailVerzoegerungListener();
    mailverzoegerung.addListener(listener);
    listener.handleEvent(null); // einmal initial ausloesen

    return mailverzoegerung;
  }

  public void testMail()
  {
    IMAPCopyData imapCopyData;
    try
    {
      imapCopyData = new IMAPCopyData(
          (Boolean) getCopyToImapFolder().getValue(),
          (String) getImapAuthUser().getValue(),
          (String) getImapAuthPwd().getValue(),
          (String) getImapHost().getValue(),
          getImapPort().getValue() != null
              ? Integer.toString((Integer) getImapPort().getValue())
              : "",
          (Boolean) getImap_ssl().getValue(),
          (Boolean) getImap_starttls().getValue(),
          (String) getImapSentFolder().getValue());

      MailSender sender = new MailSender((String) getSmtpServer().getValue(),
          Integer.toString((Integer) getSmtpPort().getValue()),
          (String) getSmtpAuthUser().getValue(),
          (String) getSmtpAuthPwd().getValue(),
          (String) getSmtpFromAddress().getValue(),
          (String) getSmtpFromAnzeigename().getValue(),
          (String) getAlwaysBccTo().getValue(),
          (String) getAlwaysCcTo().getValue(),
          (Boolean) getSmtpSsl().getValue(),
          (Boolean) getSmtpStarttls().getValue(),
          (Integer) getMailVerzoegerung().getValue(), imapCopyData);

      String email = (String) getSmtpFromAddress().getValue();

      sender.sendMail(email, "Test",
          "Testnachricht" + Einstellungen.getMailSignatur(true),
          new TreeSet<MailAnhang>());
      GUI.getStatusBar().setSuccessText("Testmail versendet an: " + email);
    }
    catch (Exception e)
    {
      GUI.getStatusBar()
          .setErrorText("Fehler beim senden der Testmail: " + e.getMessage());
      Logger.error("Fehler beim senden der Testmail", e);
    }
  }

  public TextInput getAlwaysBccTo() throws RemoteException
  {
    if (alwaysBccTo != null)
    {
      return alwaysBccTo;
    }
    alwaysBccTo = new TextInput(
        (String) Einstellungen.getEinstellung(Property.MAILALWAYSBCC));
    return alwaysBccTo;
  }

  public TextInput getAlwaysCcTo() throws RemoteException
  {
    if (alwaysCcTo != null)
    {
      return alwaysCcTo;
    }
    alwaysCcTo = new TextInput(
        (String) Einstellungen.getEinstellung(Property.MAILALWAYSCC));
    return alwaysCcTo;
  }

  public CheckboxInput getCopyToImapFolder() throws RemoteException
  {
    if (copyToImapFolder != null)
    {
      return copyToImapFolder;
    }
    copyToImapFolder = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.COPYTOIMAPFOLDER));
    return copyToImapFolder;
  }

  public TextInput getImapAuthUser() throws RemoteException
  {
    if (imapAuthUser != null)
    {
      return imapAuthUser;
    }
    imapAuthUser = new TextInput(
        (String) Einstellungen.getEinstellung(Property.IMAPAUTHUSER));
    return imapAuthUser;
  }

  public TextInput getImapAuthPwd() throws RemoteException
  {
    if (imapAuthPwd != null)
    {
      return imapAuthPwd;
    }
    imapAuthPwd = new PasswordInput(Einstellungen.getImapAuthPwd());
    return imapAuthPwd;
  }

  public TextInput getImapHost() throws RemoteException
  {
    if (imapHost != null)
    {
      return imapHost;
    }
    imapHost = new TextInput(
        (String) Einstellungen.getEinstellung(Property.IMAPHOST));
    return imapHost;
  }

  public IntegerInput getImapPort() throws RemoteException
  {
    if (imapPort != null)
    {
      return imapPort;
    }
    String port = (String) Einstellungen.getEinstellung(Property.IMAPPORT);
    if (port != null && port.length() > 0)
    {
      imapPort = new IntegerInput(Integer.valueOf(port));
    }
    else
    {
      imapPort = new IntegerInput();
    }
    return imapPort;
  }

  public CheckboxInput getImap_ssl() throws RemoteException
  {
    if (imap_ssl != null)
    {
      return imap_ssl;
    }
    imap_ssl = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.IMAPSSL));
    return imap_ssl;
  }

  public CheckboxInput getImap_starttls() throws RemoteException
  {
    if (imap_starttls != null)
    {
      return imap_starttls;
    }
    imap_starttls = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.IMAPSTARTTLS));
    return imap_starttls;
  }

  public TextInput getImapSentFolder() throws RemoteException
  {
    if (imapSentFolder != null)
    {
      return imapSentFolder;
    }
    imapSentFolder = new TextInput(
        (String) Einstellungen.getEinstellung(Property.IMAPSENTFOLDER));
    return imapSentFolder;
  }

  public Input getMailSignatur() throws RemoteException
  {
    if (mailsignatur != null)
    {
      return mailsignatur;
    }
    mailsignatur = new TextAreaInput(
        (String) Einstellungen.getEinstellung(Property.MAILSIGNATUR), 1000);
    mailsignatur.setHeight(50);
    return mailsignatur;
  }

  public SelectInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }
    zahlungsweg = new SelectInput(Zahlungsweg.getArray(false), new Zahlungsweg(
        (Integer) Einstellungen.getEinstellung(Property.ZAHLUNGSWEG)));
    zahlungsweg.setName("Standard-Zahlungsweg f. neue Mitglieder");
    return zahlungsweg;
  }

  public SelectInput getZahlungsrhytmus() throws RemoteException
  {
    if (zahlungsrhytmus != null)
    {
      return zahlungsrhytmus;
    }
    zahlungsrhytmus = new SelectInput(Zahlungsrhythmus.getArray(),
        new Zahlungsrhythmus(
            (Integer) Einstellungen.getEinstellung(Property.ZAHLUNGSRHYTMUS)));
    zahlungsrhytmus.setName("Standard-Zahlungsrhytmus f. neue Mitglieder");
    return zahlungsrhytmus;
  }

  public SelectInput getDefaultSEPALand() throws RemoteException
  {
    if (sepaland != null)
    {
      return sepaland;
    }
    SEPALand sl = SEPALaender
        .getLand((String) Einstellungen.getEinstellung(Property.DEFAULTLAND));
    sepaland = new SEPALandInput(sl);
    return sepaland;
  }

  public SelectInput getSepaVersion() throws RemoteException
  {
    if (sepaversion != null)
    {
      return sepaversion;
    }
    List<SepaVersion> list = SepaVersion
        .getKnownVersions(org.kapott.hbci.sepa.SepaVersion.Type.PAIN_008);
    sepaversion = new SelectInput(list, SepaVersion
        .byURN((String) Einstellungen.getEinstellung(Property.SEPAVERSION)));
    sepaversion.setAttribute("file");
    return sepaversion;
  }

  public SelectInput getCt1SepaVersion() throws RemoteException
  {
    if (ct1sepaversion != null)
    {
      return ct1sepaversion;
    }
    List<SepaVersion> list = SepaVersion
        .getKnownVersions(org.kapott.hbci.sepa.SepaVersion.Type.PAIN_001);
    ct1sepaversion = new SelectInput(list, SepaVersion
        .byURN((String) Einstellungen.getEinstellung(Property.CT1SEPAVERSION)));
    ct1sepaversion.setAttribute("file");
    return ct1sepaversion;
  }

  public DialogInput getVerrechnungskonto() throws RemoteException
  {
    if (verrechnungskonto != null)
    {
      return verrechnungskonto;
    }
    verrechnungskonto = new KontoauswahlInput(null).getKontoAuswahl(false,
        Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID) == null
            ? null
            : Einstellungen.getEinstellung(Property.VERRECHNUNGSKONTOID)
                .toString(),
        false, false, null);
    return verrechnungskonto;
  }

  public Input getAltersgruppen() throws RemoteException
  {
    if (altersgruppen != null)
    {
      return altersgruppen;
    }
    altersgruppen = new TextInput(
        (String) Einstellungen.getEinstellung(Property.ALTERSGRUPPEN), 200);
    return altersgruppen;
  }

  public Input getJubilaeen() throws RemoteException
  {
    if (jubilaeen != null)
    {
      return jubilaeen;
    }
    jubilaeen = new TextInput(
        (String) Einstellungen.getEinstellung(Property.JUBILAEEN), 50);
    return jubilaeen;
  }

  public Input getAltersjubilaeen() throws RemoteException
  {
    if (altersjubilaeen != null)
    {
      return altersjubilaeen;
    }
    altersjubilaeen = new TextInput(
        (String) Einstellungen.getEinstellung(Property.ALTERSJUBILAEEN), 200);
    return altersjubilaeen;
  }

  public IntegerInput getJubilarStartAlter() throws RemoteException
  {
    if (null == jubilarStartAlter)
    {
      jubilarStartAlter = new IntegerInput(
          (Integer) Einstellungen.getEinstellung(Property.JUBILARSTARTALTER));
    }
    return jubilarStartAlter;
  }

  public TablePart getSpaltendefinitionTable() throws RemoteException
  {
    if (spalten == null)
    {
      spalten = new MitgliedSpaltenauswahl();
    }
    return spalten.paintSpaltenpaintSpaltendefinitionTable();
  }

  public void setCheckSpalten()
  {
    spalten.setCheckSpalten();
  }

  public IntegerInput getAnzahlSpaltenStammdatenInput() throws RemoteException
  {
    {
      if (AnzahlSpaltenStammdatenInput != null)
      {
        return AnzahlSpaltenStammdatenInput;
      }
      AnzahlSpaltenStammdatenInput = new IntegerInput(
          Einstellungen.getSettingInt("AnzahlSpaltenStammdaten", 2));
      return AnzahlSpaltenStammdatenInput;
    }
  }

  public IntegerInput getAnzahlSpaltenMitgliedschaftInput()
      throws RemoteException
  {
    {
      if (AnzahlSpaltenMitgliedschaftInput != null)
      {
        return AnzahlSpaltenMitgliedschaftInput;
      }
      AnzahlSpaltenMitgliedschaftInput = new IntegerInput(
          Einstellungen.getSettingInt("AnzahlSpaltenMitgliedschaft", 1));
      return AnzahlSpaltenMitgliedschaftInput;
    }
  }

  public IntegerInput getAnzahlSpaltenZahlungInput() throws RemoteException
  {
    {
      if (AnzahlSpaltenZahlungInput != null)
      {
        return AnzahlSpaltenZahlungInput;
      }
      AnzahlSpaltenZahlungInput = new IntegerInput(
          Einstellungen.getSettingInt("AnzahlSpaltenZahlung", 1));
      return AnzahlSpaltenZahlungInput;
    }
  }

  public IntegerInput getAnzahlSpaltenZusatzfelderInput() throws RemoteException
  {
    {
      if (AnzahlSpaltenZusatzfelderInput != null)
      {
        return AnzahlSpaltenZusatzfelderInput;
      }
      AnzahlSpaltenZusatzfelderInput = new IntegerInput(
          Einstellungen.getSettingInt("AnzahlSpaltenZusatzfelder", 1));
      return AnzahlSpaltenZusatzfelderInput;
    }
  }

  public CheckboxInput getZeigeStammdatenInTabCheckbox() throws RemoteException
  {
    if (ZeigeStammdatenInTabInput != null)
    {
      return ZeigeStammdatenInTabInput;
    }
    ZeigeStammdatenInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeStammdatenInTab", true));
    return ZeigeStammdatenInTabInput;
  }

  public CheckboxInput getZeigeMitgliedschaftInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeMitgliedschaftInTabInput != null)
    {
      return ZeigeMitgliedschaftInTabInput;
    }
    ZeigeMitgliedschaftInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeMitgliedschaftInTab", true));
    return ZeigeMitgliedschaftInTabInput;
  }

  public CheckboxInput getZeigeZahlungInTabCheckbox() throws RemoteException
  {
    if (ZeigeZahlungInTabInput != null)
    {
      return ZeigeZahlungInTabInput;
    }
    ZeigeZahlungInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeZahlungInTab", true));
    return ZeigeZahlungInTabInput;
  }

  public CheckboxInput getZeigeZusatzbetrageInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeZusatzbeitraegeInTabInput != null)
    {
      return ZeigeZusatzbeitraegeInTabInput;
    }
    ZeigeZusatzbeitraegeInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeZusatzbetraegeInTab", true));
    return ZeigeZusatzbeitraegeInTabInput;
  }

  public CheckboxInput getZeigeMitgliedskontoInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeMitgliedskontoInTabInput != null)
    {
      return ZeigeMitgliedskontoInTabInput;
    }
    ZeigeMitgliedskontoInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeMitgliedskontoInTab", true));
    return ZeigeMitgliedskontoInTabInput;
  }

  public CheckboxInput getZeigeVermerkeInTabCheckbox() throws RemoteException
  {
    if (ZeigeVermerkeInTabInput != null)
    {
      return ZeigeVermerkeInTabInput;
    }
    ZeigeVermerkeInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeVermerkeInTab", true));
    return ZeigeVermerkeInTabInput;
  }

  public CheckboxInput getZeigeWiedervorlageInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeWiedervorlageInTabInput != null)
    {
      return ZeigeWiedervorlageInTabInput;
    }
    ZeigeWiedervorlageInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeWiedervorlageInTab", true));
    return ZeigeWiedervorlageInTabInput;
  }

  public CheckboxInput getZeigeMailsInTabCheckbox() throws RemoteException
  {
    if (ZeigeMailsInTabInput != null)
    {
      return ZeigeMailsInTabInput;
    }
    ZeigeMailsInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeMailsInTab", true));
    return ZeigeMailsInTabInput;
  }

  public CheckboxInput getZeigeEigenschaftenInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeEigenschaftenInTabInput != null)
    {
      return ZeigeEigenschaftenInTabInput;
    }
    ZeigeEigenschaftenInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeEigenschaftenInTab", true));
    return ZeigeEigenschaftenInTabInput;
  }

  public CheckboxInput getZeigeZusatzfelderInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeZusatzfelderInTabInput != null)
    {
      return ZeigeZusatzfelderInTabInput;
    }
    ZeigeZusatzfelderInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeZusatzfelderInTab", true));
    return ZeigeZusatzfelderInTabInput;
  }

  public CheckboxInput getZeigeLehrgaengeInTabCheckbox() throws RemoteException
  {
    if (ZeigeLehrgaengeInTabInput != null)
    {
      return ZeigeLehrgaengeInTabInput;
    }
    ZeigeLehrgaengeInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeLehrgaengeInTab", true));
    return ZeigeLehrgaengeInTabInput;
  }

  public CheckboxInput getZeigeFotoInTabCheckbox() throws RemoteException
  {
    if (ZeigeFotoInTabInput != null)
    {
      return ZeigeFotoInTabInput;
    }
    ZeigeFotoInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeFotoInTab", true));
    return ZeigeFotoInTabInput;
  }

  public CheckboxInput getZeigeLesefelderInTabCheckbox() throws RemoteException
  {
    if (ZeigeLesefelderInTabInput != null)
    {
      return ZeigeLesefelderInTabInput;
    }
    ZeigeLesefelderInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeLesefelderInTab", true));
    return ZeigeLesefelderInTabInput;
  }

  public CheckboxInput getZeigeArbeitseinsatzInTabCheckbox()
      throws RemoteException
  {
    if (ZeigeArbeitseinsatzInTabInput != null)
    {
      return ZeigeArbeitseinsatzInTabInput;
    }
    ZeigeArbeitseinsatzInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeArbeitseinsatzInTab", true));
    return ZeigeArbeitseinsatzInTabInput;
  }

  public CheckboxInput getZeigeDokumenteInTabCheckbox() throws RemoteException
  {
    if (ZeigeDokumenteInTabInput != null)
    {
      return ZeigeDokumenteInTabInput;
    }
    ZeigeDokumenteInTabInput = new CheckboxInput(
        Einstellungen.getSettingBoolean("ZeigeDokumenteInTab", true));
    return ZeigeDokumenteInTabInput;
  }

  public SelectInput getBuchungBuchungsartAuswahl() throws RemoteException
  {
    if (null != buchungBuchungsartAuswahl)
    {
      return buchungBuchungsartAuswahl;
    }

    buchungBuchungsartAuswahl = new SelectInput(AbstractInputAuswahl.getArray(),
        new AbstractInputAuswahl((Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL)));

    return buchungBuchungsartAuswahl;
  }

  public SelectInput getMitgliedAuswahl() throws RemoteException
  {
    if (null != mitgliedAuswahl)
    {
      return mitgliedAuswahl;
    }

    mitgliedAuswahl = new SelectInput(AbstractInputAuswahl.getArray(),
        new AbstractInputAuswahl(
            (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL)));

    return mitgliedAuswahl;
  }

  public SelectInput getBuchungsartSort() throws RemoteException
  {
    if (buchungsartsort != null)
    {
      return buchungsartsort;
    }
    buchungsartsort = new SelectInput(BuchungsartSort.getArray(),
        new BuchungsartSort(
            (Integer) Einstellungen.getEinstellung(Property.BUCHUNGSARTSORT)));
    return buchungsartsort;
  }

  public IntegerInput getQRCodeSizeInMm() throws RemoteException
  {
    if (null == qrcodesize)
    {
      qrcodesize = new IntegerInput(
          (Integer) Einstellungen.getEinstellung(Property.QRCODESIZEINMM));
    }
    return qrcodesize;
  }

  public TextInput getQRCodeVerwendungszweck() throws RemoteException
  {
    if (null == qrcodetext)
    {
      qrcodetext = new TextInput(
          (String) Einstellungen.getEinstellung(Property.QRCODETEXT));
    }
    return qrcodetext;
  }

  public CheckboxInput getQRCodePrintVerwendungszweck() throws RemoteException
  {
    if (null == qrcodeptext)
    {
      qrcodeptext = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.QRCODEFESTERTEXT));
    }
    return qrcodeptext;
  }

  public CheckboxInput getQRCodeSingle() throws RemoteException
  {
    if (null == qrcodesngl)
    {
      qrcodesngl = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.QRCODESNGLLINE));
    }
    return qrcodesngl;
  }

  public CheckboxInput getQRCodeReDa() throws RemoteException
  {
    if (null == qrcodepdate)
    {
      qrcodepdate = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.QRCODEDATUM));
    }
    return qrcodepdate;
  }

  public CheckboxInput getQRCodeReNr() throws RemoteException
  {
    if (null == qrcodeprenum)
    {
      qrcodeprenum = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.QRCODERENU));
    }
    return qrcodeprenum;
  }

  public CheckboxInput getQRCodeMemberNr() throws RemoteException
  {
    if (null == qrcodepmnum)
    {
      qrcodepmnum = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.QRCODEMEMBER));
    }
    return qrcodepmnum;
  }

  public TextInput getQRCodeInfoToMember() throws RemoteException
  {
    if (null == qrcodeinfom)
    {
      qrcodeinfom = new TextInput(
          (String) Einstellungen.getEinstellung(Property.QRCODEINFOM));
    }
    return qrcodeinfom;
  }

  public CheckboxInput getQRCodeKuerzen() throws RemoteException
  {
    if (null == qrcodekuerzen)
    {
      qrcodekuerzen = new CheckboxInput(
          (Boolean) Einstellungen.getEinstellung(Property.QRCODEKUERZEN));
    }
    return qrcodekuerzen;
  }

  public TextInput getQRCodeIntro() throws RemoteException
  {
    if (null == qrcodeintro)
    {
      qrcodeintro = new TextInput(
          (String) Einstellungen.getEinstellung(Property.QRCODEINTRO));
    }
    return qrcodeintro;
  }

  // // public void handleStore()
  // {
  // try
  // {
  // Einstellung e = Einstellungen.getEinstellung();
  //
  // Einstellungen.setEinstellung(Property.NAME, (String)
  // getName(false).getValue());
  // Einstellungen.setEinstellung(Property.STRASSE, (String)
  // getStrasse().getValue());
  // Einstellungen.setEinstellung(Property.PLZ, (String) getPlz().getValue());
  // Einstellungen.setEinstellung(Property.ORT, (String) getOrt().getValue());
  // Einstellungen.setEinstellung(Property.FINANZAMT, (String)
  // getFinanzamt().getValue());
  // Einstellungen.setEinstellung(Property.STEUERNUMMER, (String)
  // getSteuernummer().getValue());
  // Einstellungen.setEinstellung(Property.BESCHEIDDATUM, (Date)
  // getBescheiddatum().getValue());
  // Einstellungen.setEinstellung(Property.VORLAEUFIG, (Boolean)
  // getVorlaeufig().getValue());
  // Einstellungen.setEinstellung(Property.VORLAEUFIGAB, (Date)
  // getVorlaeufigab().getValue());
  // Einstellungen.setEinstellung(Property.VERANLAGUNGVON, (Date)
  // getVeranlagungVon().getValue());
  // Einstellungen.setEinstellung(Property.VERANLAGUNGBIS, (Date)
  // getVeranlagungBis().getValue());
  // Einstellungen.setEinstellung(Property.BEGUENSTIGTERZWECK, (String)
  // getBeguenstigterzweck().getValue());
  // Einstellungen.setEinstellung(Property.MITGLIEDSBEITRAEGE, (Boolean)
  // getMitgliedsbetraege().getValue());
  // Einstellungen.setEinstellung(Property.BIC, (String) getBic().getValue());
  // Einstellungen.setEinstellung(Property.IBAN, (String) getIban().getValue());
  // Einstellungen.setEinstellung(Property.GLAEUBIGERID, (String)
  // getGlaeubigerID().getValue());
  // Einstellungen.setEinstellung(Property.BLZ, (String) getBlz().getValue());
  // Einstellungen.setEinstellung(Property.KONTO, (String)
  // getKonto().getValue());
  // Einstellungen.setEinstellung(Property.GEBURTSDATUMPFLICHT, (Boolean)
  // geburtsdatumpflicht.getValue());
  // Einstellungen.setEinstellung(Property.EINTRITTSDATUMPFLICHT, (Boolean)
  // eintrittsdatumpflicht.getValue());
  // Einstellungen.setEinstellung(Property.STERBEDATUM, (Boolean)
  // sterbedatum.getValue());
  // Einstellungen.setEinstellung(Property.KOMMUNIKATIONSDATEN, (Boolean)
  // kommunikationsdaten.getValue());
  // Einstellungen.setEinstellung(Property.ZUSATZBETRAG, (Boolean)
  // zusatzbetrag.getValue());
  // Einstellungen.setEinstellung(Property.ZUSATZBETRAGAUSGETRETENE, (Boolean)
  // zusatzbetragAusgetretene
  // .getValue());
  // Einstellungen.setEinstellung(Property.VERMERKE, (Boolean)
  // vermerke.getValue());
  // Einstellungen.setEinstellung(Property.WIEDERVORLAGE, (Boolean)
  // wiedervorlage.getValue());
  // Einstellungen.setEinstellung(Property.KURSTEILNEHMER, (Boolean)
  // kursteilnehmer.getValue());
  // Einstellungen.setEinstellung(Property.LEHRGAENGE, (Boolean)
  // lehrgaenge.getValue());
  // Einstellungen.setEinstellung(Property.JURISTISCHEPERSONEN, (Boolean)
  // juristischepersonen.getValue());
  // Einstellungen.setEinstellung(Property.MITGLIEDFOTO, (Boolean)
  // mitgliedfoto.getValue());
  // Einstellungen.setEinstellung(Property.USELESEFELDER, (Boolean)
  // uselesefelder.getValue());
  // Einstellungen.setEinstellung(Property.ZUSATZADRESSEN, (Boolean)
  // zusatzadressen.getValue());
  // Einstellungen.setEinstellung(Property.AUSLANDSADRESSEN, (Boolean)
  // auslandsadressen.getValue());
  // Einstellungen.setEinstellung(Property.ARBEITSEINSATZ, (Boolean)
  // arbeitseinsatz.getValue());
  // Einstellungen.setEinstellung(Property.DOKUMENTENSPEICHERUNG, (Boolean)
  // dokumentenspeicherung.getValue());
  // Einstellungen.setEinstellung(Property.INDIVIDUELLEBEITRAEGE, (Boolean)
  // individuellebeitraege.getValue());
  // Einstellungen.setEinstellung(Property.RECHNUNGTEXTABBUCHUNG, (String)
  // rechnungtextabbuchung.getValue());
  // Einstellungen.setEinstellung(Property.RECHNUNGTEXTABBUCHUNG, (String)
  // rechnungtextabbuchung.getValue());
  // Einstellungen.setEinstellung(Property.RECHNUNGTEXTUEBERWEISUNG, (String)
  // rechnungtextueberweisung
  // .getValue());
  // Einstellungen.setEinstellung(Property.RECHNUNGTEXTBAR, (String)
  // rechnungtextbar.getValue());
  // Einstellungen.setEinstellung(Property.EXTERNEMITGLIEDSNUMMER, (Boolean)
  // externemitgliedsnummer.getValue());
  // Beitragsmodel bm = (Beitragsmodel) beitragsmodel.getValue();
  // Einstellungen.setEinstellung(Property.BEITRAGSMODEL, bm.getKey());
  // ArbeitsstundenModel am = (ArbeitsstundenModel) arbeitsstundenmodel
  // .getValue();
  // Einstellungen.setEinstellung(Property.ARBEITSSTUNDENMODEL, am.getKey());
  // SepaMandatIdSource sepaSource = (SepaMandatIdSource)
  // sepamandatidsourcemodel
  // .getValue();
  // Einstellungen.setEinstellung(Property.SEPAMANDATIDSOURCE,
  // sepaSource.getKey());
  // Altermodel amValue = (Altermodel) altersmodel.getValue();
  // Einstellungen.setEinstellung(Property.ALTERSMODEL, amValue.getKey());
  // Einstellungen.setEinstellung(Property.DATEINAMENMUSTER, (String)
  // dateinamenmuster.getValue());
  // Einstellungen.setEinstellung(Property.VORLAGENCSVVERZEICHNIS, (String)
  // vorlagenCsvVerzeichnis.getValue());
  // Einstellungen.setEinstellung(Property.SPENDENBESCHEINIGUNGMINBETRAG,
  // (Double) spendenbescheinigungminbetrag
  // .getValue());
  // Einstellungen.setEinstellung(Property.SPENDENBESCHEINIGUNGVERZEICHNIS,
  // (String)
  // spendenbescheinigungverzeichnis
  // .getValue());
  // Einstellungen.setEinstellung(Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART,
  // (Boolean)
  // spendenbescheinigungprintbuchungsart
  // .getValue());
  // Einstellungen.setEinstellung(Property.BEGINNGESCHAEFTSJAHR, (String)
  // beginngeschaeftsjahr.getValue());
  // Einstellungen.setEinstellung(Property.AUTOBUCHUNGUEBERNAHME, (Boolean)
  // autobuchunguebernahme.getValue());
  // Einstellungen.setEinstellung(Property.SMTPSERVER, (String)
  // smtp_server.getValue());
  // Integer port = (Integer) smtp_port.getValue();
  // Einstellungen.setEinstellung(Property.SMTPPORT, port.toString());
  // Einstellungen.setEinstellung(Property.SMTPAUTHUSER, (String)
  // smtp_auth_user.getValue());
  // Einstellungen.setEinstellung(Property.SMTPAUTHPWD, (String)
  // smtp_auth_pwd.getValue());
  // Einstellungen.setEinstellung(Property.SMTPFROMADDRESS, (String)
  // smtp_from_address.getValue());
  // Einstellungen.setEinstellung(Property.SMTPFROMANZEIGENAME, (String)
  // smtp_from_anzeigename.getValue());
  // Einstellungen.setEinstellung(Property.SMTPSSL, (Boolean)
  // smtp_ssl.getValue());
  // Einstellungen.setEinstellung(Property.SMTPSTARTTLS, (Boolean)
  // smtp_starttls.getValue());
  // Einstellungen.setEinstellung(Property.MAILALWAYSCC, (String)
  // alwaysCcTo.getValue());
  // Einstellungen.setEinstellung(Property.MAILALWAYSBCC, (String)
  // alwaysBccTo.getValue());
  //
  // Einstellungen.setEinstellung(Property.COPYTOIMAPFOLDER, (Boolean)
  // copyToImapFolder.getValue());
  // Einstellungen.setEinstellung(Property.IMAPHOST, (String)
  // imapHost.getValue());
  // Einstellungen.setEinstellung(Property.IMAPPORT, (String)
  // imapPort.getValue());
  // Einstellungen.setEinstellung(Property.IMAPAUTHUSER, ((String)
  // imapAuthUser.getValue()));
  // Einstellungen.setEinstellung(Property.IMAPAUTHPWD, ((String)
  // imapAuthPwd.getValue()));
  // Einstellungen.setEinstellung(Property.IMAPSSL, (Boolean)
  // imap_ssl.getValue());
  // Einstellungen.setEinstellung(Property.IMAPSTARTTLS, (Boolean)
  // imap_starttls.getValue());
  // Einstellungen.setEinstellung(Property.IMAPSENTFOLDER, (String)
  // imapSentFolder.getValue());
  // Einstellungen.setEinstellung(Property.MAILSIGNATUR, (String)
  // mailsignatur.getValue());
  //
  // Zahlungsrhytmus zr = (Zahlungsrhytmus) zahlungsrhytmus.getValue();
  // Einstellungen.setEinstellung(Property.ZAHLUNGSRHYTMUS, zr.getKey());
  // Zahlungsweg zw = (Zahlungsweg) zahlungsweg.getValue();
  // Einstellungen.setEinstellung(Property.ZAHLUNGSWEG, zw.getKey());
  // SEPALandObject slo = (SEPALandObject) getDefaultSEPALand().getValue();
  // Einstellungen.setEinstellung(Property.DEFAULTLAND,
  // slo.getLand().getKennzeichen());
  // Einstellungen.setEinstellung(Property.ALTERSGRUPPEN, (String)
  // getAltersgruppen().getValue());
  // Einstellungen.setEinstellung(Property.JUBILAEEN, (String)
  // getJubilaeen().getValue());
  // Einstellungen.setEinstellung(Property.ALTERSJUBILAEEN, (String)
  // getAltersjubilaeen().getValue());
  // Integer jubilaeumStartAlter = (Integer) jubilarStartAlter.getValue();
  // Einstellungen.setEinstellung(Property.JUBILARSTARTALTER,
  // jubilaeumStartAlter);
  // e.store();
  // spalten.save();
  // Einstellungen.setEinstellung(e);
  //
  // Einstellungen.setEinstellung(Property.ANZAHLSPALTENSTAMMDATEN, (Integer)
  // getAnzahlSpaltenStammdatenInput()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ANZAHLSPALTENLESEFELDER, (Integer)
  // getAnzahlSpaltenLesefelderInput()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ANZAHLSPALTENZUSATZFELDER, (Integer)
  // getAnzahlSpaltenZusatzfelderInput()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ANZAHLSPALTENMITGLIEDSCHAFT,
  // (Integer)
  // getAnzahlSpaltenMitgliedschaftInput()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ANZAHLSPALTENZAHLUNG, (Integer)
  // getAnzahlSpaltenZahlungInput()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGESTAMMDATENINTAB, (Boolean)
  // getZeigeStammdatenInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEMITGLIEDSCHAFTINTAB, (Boolean)
  // getZeigeMitgliedschaftInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEZAHLUNGINTAB, (Boolean)
  // getZeigeZahlungInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEZUSATZBETRAGEINTAB, (Boolean)
  // getZeigeZusatzbetrageInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEMITGLIEDSKONTOINTAB, (Boolean)
  // getZeigeMitgliedskontoInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEVERMERKEINTAB, (Boolean)
  // getZeigeVermerkeInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEWIEDERVORLAGEINTAB, (Boolean)
  // getZeigeWiedervorlageInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEMAILSINTAB, (Boolean)
  // getZeigeMailsInTabCheckbox().getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEEIGENTSCHAFTENINTAB, (Boolean)
  // getZeigeEigenschaftenInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEZUSATZFELDERINTAB, (Boolean)
  // getZeigeZusatzfelderInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGELEHRGAENGEINTAB, (Boolean)
  // getZeigeLehrgaengeInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEFOTOINTAB, (Boolean)
  // getZeigeFotoInTabCheckbox().getValue());
  // Einstellungen.setEinstellung(Property.ZEIGELESEFELDERINTAB, (Boolean)
  // getZeigeLesefelderInTabCheckbox()
  // .getValue());
  // Einstellungen.setEinstellung(Property.ZEIGEARBEITSEINSATZINTAB, (Boolean)
  // getZeigeArbeitseinsatzInTabCheckbox()
  // .getValue());
  // GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
  // }
  // catch (RemoteException e)
  // {
  // GUI.getStatusBar().setErrorText(e.getMessage());
  // }
  // catch (ApplicationException e)
  // {
  // GUI.getStatusBar().setErrorText(e.getMessage());
  // }
  // }

  public CheckboxInput getZusatzbetragAusgetretene() throws RemoteException
  {
    if (zusatzbetragAusgetretene != null)
    {
      return zusatzbetragAusgetretene;
    }
    zusatzbetragAusgetretene = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.ZUSATZBETRAGAUSGETRETENE));
    zusatzbetragAusgetretene.setName(" ");
    return zusatzbetragAusgetretene;
  }

  public CheckboxInput getKeineIstbuchungBeiLastschrift() throws RemoteException
  {
    if (keineistbuchungbeilastschrift != null)
    {
      return keineistbuchungbeilastschrift;
    }
    keineistbuchungbeilastschrift = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.KEINEISTBUCHUNGBEILASTSCHRIFT));
    keineistbuchungbeilastschrift.setName(" ");
    return keineistbuchungbeilastschrift;
  }

  public CheckboxInput getAbrlAbschliessen() throws RemoteException
  {
    if (abrlabschliessen != null)
    {
      return abrlabschliessen;
    }
    abrlabschliessen = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.ABRLABSCHLIESSEN));
    abrlabschliessen.setName("Funktion einschalten");
    return abrlabschliessen;
  }

  public CheckboxInput getUnterschriftdrucken() throws RemoteException
  {
    if (unterschriftdrucken != null)
    {
      return unterschriftdrucken;
    }
    unterschriftdrucken = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.UNTERSCHRIFTDRUCKEN));
    unterschriftdrucken.setName(
        " *Die maschinelle Erstellung von Zuwendungsbestätigungen muss "
            + "vorab dem zuständigen Finanzamt angezeigt worden sein.");
    return unterschriftdrucken;
  }

  public ImageInput getUnterschrift() throws IOException
  {
    if (unterschrift != null)
    {
      return unterschrift;
    }

    String imgString = (String) Einstellungen
        .getEinstellung(Property.UNTERSCHRIFT);
    byte[] img = null;
    if (imgString != null && !imgString.isBlank())
    {
      img = Base64.decode(imgString);
    }

    unterschrift = new ImageInput(img, 400, 75);
    return unterschrift;
  }

  public CheckboxInput getAnhangSpeichern() throws RemoteException
  {
    if (anhangspeichern != null)
    {
      return anhangspeichern;
    }
    anhangspeichern = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.ANHANGSPEICHERN));
    anhangspeichern
        .setName("Bei Mail Versand von Formularen Anhang in DB speichern");
    return anhangspeichern;
  }

  public CheckboxInput getSummenAnlagenkonto() throws RemoteException
  {
    if (summenAnlagenkonto != null)
    {
      return summenAnlagenkonto;
    }
    summenAnlagenkonto = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.SUMMENANLAGENKONTO));
    return summenAnlagenkonto;
  }

  public DecimalInput getAfaRestwert() throws RemoteException
  {
    if (afarestwert != null)
    {
      return afarestwert;
    }
    afarestwert = new DecimalInput(
        (Double) Einstellungen.getEinstellung(Property.AFARESTWERT),
        new DecimalFormat("###0.00"));
    return afarestwert;
  }

  public SelectInput getAfaOrt() throws RemoteException
  {
    if (afaort != null)
    {
      return afaort;
    }
    Boolean isinjahresabschluss = (Boolean) Einstellungen
        .getEinstellung(Property.AFAINJAHRESABSCHLUSS);
    if (isinjahresabschluss)
      afaort = new SelectInput(AfaOrt.getArray(),
          new AfaOrt(AfaOrt.JAHRESABSCHLUSS));
    else
      afaort = new SelectInput(AfaOrt.getArray(),
          new AfaOrt(AfaOrt.ANLAGENBUCHUNGEN));
    return afaort;
  }

  public CheckboxInput getMitgliedsnummerAnzeigen() throws RemoteException
  {
    if (nummeranzeigen != null)
    {
      return nummeranzeigen;
    }
    nummeranzeigen = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.MITGLIEDSNUMMERANZEIGEN));
    return nummeranzeigen;
  }

  public CheckboxInput getFamilienbeitrag() throws RemoteException
  {
    if (familienbeitrag != null)
    {
      return familienbeitrag;
    }
    familienbeitrag = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.FAMILIENBEITRAG));
    return familienbeitrag;
  }

  public CheckboxInput getAnlagenkonten() throws RemoteException
  {
    if (anlagenkonten != null)
    {
      return anlagenkonten;
    }
    anlagenkonten = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.ANLAGENKONTEN));
    return anlagenkonten;
  }

  public CheckboxInput getRuecklagenkonten() throws RemoteException
  {
    if (ruecklagenkonten != null)
    {
      return ruecklagenkonten;
    }
    ruecklagenkonten = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.RUECKLAGENKONTEN));
    return ruecklagenkonten;
  }

  public CheckboxInput getVerbindlichkeitenForderungen() throws RemoteException
  {
    if (verbindlichkeitenforderungen != null)
    {
      return verbindlichkeitenforderungen;
    }
    verbindlichkeitenforderungen = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.VERBINDLICHKEITEN_FORDERUNGEN));
    return verbindlichkeitenforderungen;
  }

  public CheckboxInput getMittelverwendung() throws RemoteException
  {
    if (mittelverwendung != null)
    {
      return mittelverwendung;
    }
    mittelverwendung = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.MITTELVERWENDUNG));
    return mittelverwendung;
  }

  public CheckboxInput getProjekte() throws RemoteException
  {
    if (projekte != null)
    {
      return projekte;
    }
    projekte = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN));
    return projekte;
  }

  public CheckboxInput getSpendenbescheinigungen() throws RemoteException
  {
    if (spendenbescheinigungen != null)
    {
      return spendenbescheinigungen;
    }
    spendenbescheinigungen = new CheckboxInput((Boolean) Einstellungen
        .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN));
    return spendenbescheinigungen;
  }

  public CheckboxInput getRechnungen() throws RemoteException
  {
    if (rechnungen != null)
    {
      return rechnungen;
    }
    rechnungen = new CheckboxInput(
        (Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN));
    return rechnungen;
  }

  public void handleStoreAllgemein()
  {
    try
    {
      DBTransaction.starten();
      Einstellungen.setEinstellung(Property.NAME,
          (String) getName(false).getValue());
      Einstellungen.setEinstellung(Property.STRASSE,
          (String) getStrasse().getValue());
      Einstellungen.setEinstellung(Property.PLZ, (String) getPlz().getValue());
      Einstellungen.setEinstellung(Property.ORT, (String) getOrt().getValue());
      Einstellungen.setEinstellung(Property.BIC, (String) getBic().getValue());
      String ib = (String) getIban().getValue();
      if (ib == null || ib.isBlank())
        Einstellungen.setEinstellung(Property.IBAN, null);
      else
        Einstellungen.setEinstellung(Property.IBAN,
            ib.toUpperCase().replace(" ", ""));
      Einstellungen.setEinstellung(Property.GLAEUBIGERID,
          (String) getGlaeubigerID().getValue());
      if (getStaat().getValue() == null)
      {
        throw new ApplicationException("Bitte Staat auswählen");
      }
      Einstellungen.setEinstellung(Property.STAAT,
          ((Staat) getStaat().getValue()).getKey());
      Einstellungen.setEinstellung(Property.USTID,
          (String) getUstID().getValue());
      Einstellungen.setEinstellung(Property.GEBURTSDATUMPFLICHT,
          (Boolean) geburtsdatumpflicht.getValue());
      Einstellungen.setEinstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT,
          (Boolean) nichtmitgliedgeburtsdatumpflicht.getValue());
      Einstellungen.setEinstellung(Property.EINTRITTSDATUMPFLICHT,
          (Boolean) eintrittsdatumpflicht.getValue());
      Einstellungen.setEinstellung(Property.KURSTEILNEHMERGEBPFLICHT,
          (Boolean) kursteilnehmergebpflicht.getValue());
      Einstellungen.setEinstellung(Property.KURSTEILNEHMERGESPFLICHT,
          (Boolean) kursteilnehmergespflicht.getValue());
      Einstellungen.setEinstellung(Property.JMITGLIEDPFLICHTEIGENSCHAFTEN,
          (Boolean) jmitgliedpflichteigenschaften.getValue());
      Einstellungen.setEinstellung(Property.JNICHTMITGLIEDPFLICHTEIGENSCHAFTEN,
          (Boolean) jnichtmitgliedpflichteigenschaften.getValue());
      Einstellungen.setEinstellung(Property.NICHTMITGLIEDPFLICHTEIGENSCHAFTEN,
          (Boolean) nichtmitgliedpflichteigenschaften.getValue());
      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen Allgemein gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreAnzeige()
  {
    try
    {
      DBTransaction.starten();
      Einstellungen.setEinstellung(Property.STERBEDATUM,
          (Boolean) sterbedatum.getValue());
      Einstellungen.setEinstellung(Property.KOMMUNIKATIONSDATEN,
          (Boolean) kommunikationsdaten.getValue());
      Einstellungen.setEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN,
          (Boolean) sekundaerebeitragsgruppen.getValue());
      Einstellungen.setEinstellung(Property.ZUSATZBETRAG,
          (Boolean) zusatzbetrag.getValue());
      Einstellungen.setEinstellung(Property.VERMERKE,
          (Boolean) vermerke.getValue());
      Einstellungen.setEinstellung(Property.WIEDERVORLAGE,
          (Boolean) wiedervorlage.getValue());
      Einstellungen.setEinstellung(Property.KURSTEILNEHMER,
          (Boolean) kursteilnehmer.getValue());
      Einstellungen.setEinstellung(Property.LEHRGAENGE,
          (Boolean) lehrgaenge.getValue());
      Einstellungen.setEinstellung(Property.JURISTISCHEPERSONEN,
          (Boolean) juristischepersonen.getValue());
      Einstellungen.setEinstellung(Property.MITGLIEDFOTO,
          (Boolean) mitgliedfoto.getValue());
      Einstellungen.setEinstellung(Property.USEZUSATZFELDER,
          (Boolean) usezusatzfelder.getValue());
      Einstellungen.setEinstellung(Property.USELESEFELDER,
          (Boolean) uselesefelder.getValue());
      Einstellungen.setEinstellung(Property.ZUSATZADRESSEN,
          (Boolean) zusatzadressen.getValue());
      Einstellungen.setEinstellung(Property.AUSLANDSADRESSEN,
          (Boolean) auslandsadressen.getValue());
      Einstellungen.setEinstellung(Property.ARBEITSEINSATZ,
          (Boolean) arbeitseinsatz.getValue());
      Einstellungen.setEinstellung(Property.DOKUMENTENSPEICHERUNG,
          (Boolean) dokumentenspeicherung.getValue());
      Einstellungen.setEinstellung(Property.INDIVIDUELLEBEITRAEGE,
          (Boolean) individuellebeitraege.getValue());
      Einstellungen.setEinstellung(Property.EXTERNEMITGLIEDSNUMMER,
          (Boolean) externemitgliedsnummer.getValue());
      Einstellungen.setEinstellung(Property.SUMMENANLAGENKONTO,
          (Boolean) summenAnlagenkonto.getValue());
      Altermodel amValue = (Altermodel) altersmodel.getValue();
      Einstellungen.setEinstellung(Property.ALTERSMODEL, amValue.getKey());
      AbstractInputAuswahl bbaAuswahl = (AbstractInputAuswahl) buchungBuchungsartAuswahl
          .getValue();
      Einstellungen.setEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL,
          bbaAuswahl.getKey());
      AbstractInputAuswahl mAuswahl = (AbstractInputAuswahl) mitgliedAuswahl
          .getValue();
      Einstellungen.setEinstellung(Property.MITGLIEDAUSWAHL, mAuswahl.getKey());
      Einstellungen.setEinstellung(Property.BUCHUNGSARTSORT,
          ((BuchungsartSort) buchungsartsort.getValue()).getKey());
      if (((AfaOrt) afaort.getValue()).getKey() == 0)
        Einstellungen.setEinstellung(Property.AFAINJAHRESABSCHLUSS, false);
      else
        Einstellungen.setEinstellung(Property.AFAINJAHRESABSCHLUSS, true);
      Einstellungen.setEinstellung(Property.MITGLIEDSNUMMERANZEIGEN,
          (Boolean) nummeranzeigen.getValue());
      Einstellungen.setEinstellung(Property.ANLAGENKONTEN,
          (Boolean) anlagenkonten.getValue());
      Einstellungen.setEinstellung(Property.FAMILIENBEITRAG,
          (Boolean) familienbeitrag.getValue());
      Einstellungen.setEinstellung(Property.RUECKLAGENKONTEN,
          (Boolean) ruecklagenkonten.getValue());
      Einstellungen.setEinstellung(Property.VERBINDLICHKEITEN_FORDERUNGEN,
          (Boolean) verbindlichkeitenforderungen.getValue());
      Einstellungen.setEinstellung(Property.MITTELVERWENDUNG,
          (Boolean) mittelverwendung.getValue());
      Einstellungen.setEinstellung(Property.PROJEKTEANZEIGEN,
          (Boolean) projekte.getValue());
      Einstellungen.setEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN,
          (Boolean) spendenbescheinigungen.getValue());
      Einstellungen.setEinstellung(Property.RECHNUNGENANZEIGEN,
          (Boolean) rechnungen.getValue());
      Einstellungen.setEinstellung(Property.UNTERDRUECKUNGOHNEBUCHUNG,
          (Boolean) unterdrueckungohnebuchung.getValue());
      Integer ulength = (Integer) unterdrueckunglaenge.getValue();
      Einstellungen.setEinstellung(Property.UNTERDRUECKUNGLAENGE, ulength);
      Integer klength = (Integer) unterdrueckungkonten.getValue();
      Einstellungen.setEinstellung(Property.UNTERDRUECKUNGKONTEN, klength);
      Einstellungen.setEinstellung(Property.WIRTSCHAFTSPLANANZEIGEN,
          wirtschaftsplanung.getValue());

      DBTransaction.commit();
      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreAbrechnung()
  {
    try
    {
      DBTransaction.starten();

      Beitragsmodel bm = (Beitragsmodel) beitragsmodel.getValue();
      Einstellungen.setEinstellung(Property.BEITRAGSMODEL, bm.getKey());
      ArbeitsstundenModel am = (ArbeitsstundenModel) arbeitsstundenmodel
          .getValue();
      Einstellungen.setEinstellung(Property.ARBEITSSTUNDENMODEL, am.getKey());
      SepaMandatIdSource sepaSource = (SepaMandatIdSource) sepamandatidsourcemodel
          .getValue();
      Einstellungen.setEinstellung(Property.SEPAMANDATIDSOURCE,
          sepaSource.getKey());
      Zahlungsrhythmus zr = (Zahlungsrhythmus) zahlungsrhytmus.getValue();
      Einstellungen.setEinstellung(Property.ZAHLUNGSRHYTMUS, zr.getKey());
      Zahlungsweg zw = (Zahlungsweg) zahlungsweg.getValue();
      Einstellungen.setEinstellung(Property.ZAHLUNGSWEG, zw.getKey());
      SEPALandObject slo = (SEPALandObject) getDefaultSEPALand().getValue();
      Einstellungen.setEinstellung(Property.DEFAULTLAND,
          slo.getLand().getKennzeichen());
      Einstellungen.setEinstellung(Property.SEPAVERSION,
          ((SepaVersion) sepaversion.getValue()).getFile());
      Einstellungen.setEinstellung(Property.CT1SEPAVERSION,
          ((SepaVersion) ct1sepaversion.getValue()).getFile());
      Einstellungen.setEinstellung(Property.SEPADATUMOFFSET,
          (Integer) sepadatumoffset.getValue());
      Einstellungen.setEinstellung(Property.ABRLABSCHLIESSEN,
          (Boolean) abrlabschliessen.getValue());
      Einstellungen.setEinstellung(Property.BEITRAGALTERSSTUFEN,
          (String) beitragaltersstufen.getValue());
      if (verrechnungskonto.getValue() != null)
      {
        Einstellungen.setEinstellung(Property.VERRECHNUNGSKONTOID,
            (Integer.parseInt(
                (String) ((Konto) verrechnungskonto.getValue()).getID())));
      }
      Einstellungen.setEinstellung(Property.ZUSATZBETRAGAUSGETRETENE,
          (Boolean) zusatzbetragAusgetretene.getValue());
      Einstellungen.setEinstellung(Property.KEINEISTBUCHUNGBEILASTSCHRIFT,
          (Boolean) keineistbuchungbeilastschrift.getValue());
      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreDateinamen()
  {
    try
    {
      DBTransaction.starten();

      Einstellungen.setEinstellung(Property.DATEINAMENMUSTER,
          (String) dateinamenmuster.getValue());
      Einstellungen.setEinstellung(Property.VORLAGENCSVVERZEICHNIS,
          (String) vorlagenCsvVerzeichnis.getValue());
      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreSpendenbescheinigungen()
  {
    try
    {
      DBTransaction.starten();

      Einstellungen.setEinstellung(Property.FINANZAMT,
          (String) getFinanzamt().getValue());
      Einstellungen.setEinstellung(Property.STEUERNUMMER,
          (String) getSteuernummer().getValue());
      Einstellungen.setEinstellung(Property.BESCHEIDDATUM,
          (Date) getBescheiddatum().getValue());
      Einstellungen.setEinstellung(Property.VORLAEUFIG,
          (Boolean) getVorlaeufig().getValue());
      Einstellungen.setEinstellung(Property.VERANLAGUNGVON,
          (Date) getVeranlagungVon().getValue());
      Einstellungen.setEinstellung(Property.VERANLAGUNGBIS,
          (Date) getVeranlagungBis().getValue());
      Einstellungen.setEinstellung(Property.BEGUENSTIGTERZWECK,
          (String) getBeguenstigterzweck().getValue());
      Einstellungen.setEinstellung(Property.MITGLIEDSBETRAEGE,
          (Boolean) getMitgliedsbetraege().getValue());
      Einstellungen.setEinstellung(Property.SPENDENBESCHEINIGUNGMINBETRAG,
          (Double) spendenbescheinigungminbetrag.getValue());
      Einstellungen.setEinstellung(Property.SPENDENBESCHEINIGUNGVERZEICHNIS,
          (String) spendenbescheinigungverzeichnis.getValue());
      Einstellungen.setEinstellung(
          Property.SPENDENBESCHEINIGUNGPRINTBUCHUNGSART,
          (Boolean) spendenbescheinigungprintbuchungsart.getValue());
      Einstellungen.setEinstellung(Property.UNTERSCHRIFTDRUCKEN,
          (Boolean) unterschriftdrucken.getValue());
      Einstellungen.setEinstellung(Property.UNTERSCHRIFT,
          unterschrift.getValue() == null ? null
              : Base64.encode((byte[]) unterschrift.getValue()));
      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreBuchfuehrung()
  {
    try
    {
      String successText = "";

      // ggf. Steuer in buchungen übernehmen
      if (!(Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG)
          && (Boolean) getSteuerInBuchung().getValue())
      {
        YesNoDialog dialog = new YesNoDialog(SWT.CENTER);
        dialog.setTitle("Migration Steuer in Buchung");

        dialog.setText("Soll die Steuer aus den Buchungsarten in die\n"
            + "Buchungen, Sollbuchungspositionen, Zusatzbeträge und Beitragsgruppen\n"
            + "übernommen werden?\n"
            + "Das wird für alle bisherigen Einträge gemacht,\n"
            + "so dass die bisherige Steuer erhalten bleibt.");
        try
        {
          if ((boolean) dialog.open())
          {
            int anzahl = SteuerUtil.setSteuerToBuchung();
            successText = "Steuer in " + anzahl + " Einträgen übernommen. ";
          }
        }
        catch (Exception ex)
        {
          String fehler = "Fehler beim Steuer-In-Buchung Daialog";
          Logger.error(fehler, ex);
          throw new ApplicationException(fehler);
        }
      }

      DBTransaction.starten();

      Einstellungen.setEinstellung(Property.BEGINNGESCHAEFTSJAHR,
          (String) beginngeschaeftsjahr.getValue());
      Einstellungen.setEinstellung(Property.AUTOBUCHUNGUEBERNAHME,
          (Boolean) autobuchunguebernahme.getValue());
      Einstellungen.setEinstellung(
          Property.AUTOMATISCHEBUCHUNGSKORREKTURHIBISCUS,
          (Boolean) getAutomatischeBuchungskorrekturHibiscus().getValue());
      Einstellungen.setEinstellung(Property.AFARESTWERT,
          (Double) afarestwert.getValue());
      Einstellungen.setEinstellung(Property.KONTONUMMERINBUCHUNGSLISTE,
          (Boolean) kontonummer_in_buchungsliste.getValue());
      Einstellungen.setEinstellung(Property.OPTIERT,
          (Boolean) getOptiert().getValue());
      Einstellungen.setEinstellung(Property.OPTIERTPFLICHT,
          (Boolean) getOptiertPflicht().getValue());
      Einstellungen.setEinstellung(Property.STEUERINBUCHUNG,
          (Boolean) getSteuerInBuchung().getValue());
      Einstellungen.setEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG,
          (Boolean) getFreieBuchungsklasse().getValue());
      Einstellungen.setEinstellung(Property.SPLITPOSITIONZWECK,
          (Boolean) getSplitPositionZweck().getValue());
      Einstellungen.setEinstellung(Property.GEPRUEFTSYNCHRONISIEREN,
          (Boolean) getGeprueftSynchronisieren().getValue());
      DBTransaction.commit();

      GUI.getStatusBar()
          .setSuccessText(successText + "Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreRechnungen()
  {
    try
    {
      DBTransaction.starten();

      Einstellungen.setEinstellung(Property.RECHNUNGTEXTABBUCHUNG,
          (String) rechnungtextabbuchung.getValue());
      Einstellungen.setEinstellung(Property.RECHNUNGTEXTUEBERWEISUNG,
          (String) rechnungtextueberweisung.getValue());
      Einstellungen.setEinstellung(Property.RECHNUNGTEXTBAR,
          (String) rechnungtextbar.getValue());
      Integer length = (Integer) zaehlerlaenge.getValue();
      Einstellungen.setEinstellung(Property.ZAEHLERLAENGE, length);
      Einstellungen.setEinstellung(Property.QRCODESIZEINMM,
          (Integer) qrcodesize.getValue());
      Einstellungen.setEinstellung(Property.QRCODEDATUM,
          (Boolean) qrcodepdate.getValue());
      Einstellungen.setEinstellung(Property.QRCODEFESTERTEXT,
          (Boolean) qrcodeptext.getValue());
      Einstellungen.setEinstellung(Property.QRCODEINFOM,
          (String) qrcodeinfom.getValue());
      Einstellungen.setEinstellung(Property.QRCODEMEMBER,
          (Boolean) qrcodepmnum.getValue());
      Einstellungen.setEinstellung(Property.QRCODERENU,
          (Boolean) qrcodeprenum.getValue());
      Einstellungen.setEinstellung(Property.QRCODESNGLLINE,
          (Boolean) qrcodesngl.getValue());
      Einstellungen.setEinstellung(Property.QRCODETEXT,
          (String) qrcodetext.getValue());
      Einstellungen.setEinstellung(Property.QRCODEINTRO,
          (String) qrcodeintro.getValue());
      Einstellungen.setEinstellung(Property.QRCODEKUERZEN,
          (Boolean) qrcodekuerzen.getValue());

      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreMitgliederSpalten()
  {
    try
    {
      spalten.save();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException e)
    {
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreMail()
  {
    try
    {
      DBTransaction.starten();

      Einstellungen.setEinstellung(Property.SMTPSERVER,
          (String) smtp_server.getValue());
      Integer port = (Integer) smtp_port.getValue();
      if (port != null)
      {
        Einstellungen.setEinstellung(Property.SMTPPORT, port.toString());
      }
      else
      {
        Einstellungen.setEinstellung(Property.SMTPPORT, null);
      }
      Einstellungen.setEinstellung(Property.SMTPAUTHUSER,
          (String) smtp_auth_user.getValue());
      Einstellungen.setSmtpAuthPwd((String) smtp_auth_pwd.getValue());
      Einstellungen.setEinstellung(Property.SMTPFROMADDRESS,
          (String) smtp_from_address.getValue());
      Einstellungen.setEinstellung(Property.SMTPFROMANZEIGENAME,
          (String) smtp_from_anzeigename.getValue());
      Einstellungen.setEinstellung(Property.SMTPSSL,
          (Boolean) smtp_ssl.getValue());
      Einstellungen.setEinstellung(Property.SMTPSTARTTLS,
          (Boolean) smtp_starttls.getValue());
      Einstellungen.setEinstellung(Property.MAILVERZOEGERUNG,
          (Integer) mailverzoegerung.getValue());
      Einstellungen.setEinstellung(Property.MAILALWAYSCC,
          (String) alwaysCcTo.getValue());
      Einstellungen.setEinstellung(Property.MAILALWAYSBCC,
          (String) alwaysBccTo.getValue());

      Einstellungen.setEinstellung(Property.COPYTOIMAPFOLDER,
          (Boolean) copyToImapFolder.getValue());
      Einstellungen.setEinstellung(Property.IMAPHOST,
          (String) imapHost.getValue());
      port = (Integer) imapPort.getValue();
      if (port != null)
      {
        Einstellungen.setEinstellung(Property.IMAPPORT, port.toString());
      }
      else
      {
        Einstellungen.setEinstellung(Property.IMAPPORT, null);
      }
      Einstellungen.setEinstellung(Property.IMAPAUTHUSER,
          (String) imapAuthUser.getValue());
      Einstellungen.setImapAuthPwd(((String) imapAuthPwd.getValue()));
      Einstellungen.setEinstellung(Property.IMAPSSL,
          (Boolean) imap_ssl.getValue());
      Einstellungen.setEinstellung(Property.IMAPSTARTTLS,
          (Boolean) imap_starttls.getValue());
      Einstellungen.setEinstellung(Property.IMAPSENTFOLDER,
          (String) imapSentFolder.getValue());
      Einstellungen.setEinstellung(Property.MAILSIGNATUR,
          (String) mailsignatur.getValue());

      Einstellungen.setEinstellung(Property.ANHANGSPEICHERN,
          (Boolean) anhangspeichern.getValue());

      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreStatistik()
  {
    try
    {
      DBTransaction.starten();

      Einstellungen.setEinstellung(Property.ALTERSGRUPPEN,
          (String) getAltersgruppen().getValue());
      Einstellungen.setEinstellung(Property.JUBILAEEN,
          (String) getJubilaeen().getValue());
      Einstellungen.setEinstellung(Property.ALTERSJUBILAEEN,
          (String) getAltersjubilaeen().getValue());
      Integer jubilaeumStartAlter = (Integer) jubilarStartAlter.getValue();
      Einstellungen.setEinstellung(Property.JUBILARSTARTALTER,
          jubilaeumStartAlter);
      DBTransaction.commit();

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException | ApplicationException e)
    {
      DBTransaction.rollback();
      Logger.error("Speichern felgeschlagen", e);
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public void handleStoreMitgliedAnsicht()
  {
    try
    {
      Einstellungen.setSettingInt("AnzahlSpaltenStammdaten",
          (Integer) getAnzahlSpaltenStammdatenInput().getValue());
      Einstellungen.setSettingInt("AnzahlSpaltenZusatzfelder",
          (Integer) getAnzahlSpaltenZusatzfelderInput().getValue());
      Einstellungen.setSettingInt("AnzahlSpaltenMitgliedschaft",
          (Integer) getAnzahlSpaltenMitgliedschaftInput().getValue());
      Einstellungen.setSettingInt("AnzahlSpaltenZahlung",
          (Integer) getAnzahlSpaltenZahlungInput().getValue());
      Einstellungen.setSettingBoolean("ZeigeStammdatenInTab",
          (Boolean) getZeigeStammdatenInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeMitgliedschaftInTab",
          (Boolean) getZeigeMitgliedschaftInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeZahlungInTab",
          (Boolean) getZeigeZahlungInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeZusatzbetrageInTab",
          (Boolean) getZeigeZusatzbetrageInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeMitgliedskontoInTab",
          (Boolean) getZeigeMitgliedskontoInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeVermerkeInTab",
          (Boolean) getZeigeVermerkeInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeWiedervorlageInTab",
          (Boolean) getZeigeWiedervorlageInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeMailsInTab",
          (Boolean) getZeigeMailsInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeEigentschaftenInTab",
          (Boolean) getZeigeEigenschaftenInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeZusatzfelderInTab",
          (Boolean) getZeigeZusatzfelderInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeLehrgaengeInTab",
          (Boolean) getZeigeLehrgaengeInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeFotoInTab",
          (Boolean) getZeigeFotoInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeLesefelderInTab",
          (Boolean) getZeigeLesefelderInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeArbeitseinsatzInTab",
          (Boolean) getZeigeArbeitseinsatzInTabCheckbox().getValue());
      Einstellungen.setSettingBoolean("ZeigeDokumenteInTab",
          (Boolean) getZeigeDokumenteInTabCheckbox().getValue());

      GUI.getStatusBar().setSuccessText("Einstellungen gespeichert");
    }
    catch (RemoteException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  /**
   * Hilfsklasse zum Aktualisieren des Kommentars hinter SEPADatumOffset
   */
  private class SEPADatumOffsetListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        int start = ((Integer) getSEPADatumOffset().getValue()).intValue();
        if (start == 1)
        {
          getSEPADatumOffset().setComment("1 Tage");
        }
        else
        {
          getSEPADatumOffset().setComment(start + " Tage");
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to update comment", e);
      }
    }
  }

  /**
   * Hilfsklasse zum Aktualisieren des Kommentars von MailVerzoegerung.
   */
  private class MailVerzoegerungListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        int pause = ((Integer) getMailVerzoegerung().getValue()).intValue();
        if (pause == 0)
        {
          getMailVerzoegerung().setComment("keine Pause");
        }
        else
        {
          getMailVerzoegerung().setComment(pause + " Millisekunden");
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to update comment", e);
      }
    }
  }

}
