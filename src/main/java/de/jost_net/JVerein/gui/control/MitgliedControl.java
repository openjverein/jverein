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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.LesefelddefinitionenAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.NewAction;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.action.SollbuchungNeuAction;
import de.jost_net.JVerein.gui.dialogs.AbweichenderZahlerNeuDialog;
import de.jost_net.JVerein.gui.dialogs.ExportDialog;
import de.jost_net.JVerein.gui.dialogs.PersonenartDialog;
import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog.ExportArt;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.StaatFormatter;
import de.jost_net.JVerein.gui.formatter.ZahlungsrhythmusFormatter;
import de.jost_net.JVerein.gui.formatter.ZahlungsterminFormatter;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.EmailInput;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.IntegerNullInput;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.input.SelectNoScrollInput;
import de.jost_net.JVerein.gui.input.SpinnerNoScrollInput;
import de.jost_net.JVerein.gui.input.StaatSearchInput;
import de.jost_net.JVerein.gui.input.VollzahlerInput;
import de.jost_net.JVerein.gui.input.VollzahlerSearchInput;
import de.jost_net.JVerein.gui.menu.ArbeitseinsatzMenu;
import de.jost_net.JVerein.gui.menu.LehrgangMenu;
import de.jost_net.JVerein.gui.menu.MailMenu;
import de.jost_net.JVerein.gui.menu.MitgliedMenu;
import de.jost_net.JVerein.gui.menu.MitgliedNextBGruppeMenue;
import de.jost_net.JVerein.gui.menu.WiedervorlageMenu;
import de.jost_net.JVerein.gui.menu.ZusatzbetraegeMenu;
import de.jost_net.JVerein.gui.parts.AutoUpdateTablePart;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.EigenschaftenTree;
import de.jost_net.JVerein.gui.parts.Familienverband;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.MitgliedNextBGruppePart;
import de.jost_net.JVerein.gui.parts.MitgliedSekundaereBeitragsgruppePart;
import de.jost_net.JVerein.gui.view.AbstractMitgliedDetailView;
import de.jost_net.JVerein.gui.view.ArbeitseinsatzDetailView;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.gui.view.LehrgangDetailView;
import de.jost_net.JVerein.gui.view.MailDetailView;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.MitgliedNextBGruppeView;
import de.jost_net.JVerein.gui.view.MitgliedSuchProfilListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.gui.view.WiedervorlageDetailView;
import de.jost_net.JVerein.gui.view.ZusatzbetragDetailView;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.SepaMandatIdSource;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsrhythmus;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedNextBGruppe;
import de.jost_net.JVerein.rmi.Mitgliedfoto;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatTIMESTAMP;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.ImageInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedControl extends FilterControl implements Savable
{
  final LesefeldControl lesefeldControl = new LesefeldControl(null);

  private DokumentControl dcontrol;

  private JVereinTablePart mitgliedList;

  private SelectNoScrollInput mitgliedstyp;

  private TextInput externemitgliedsnummer;

  private TextInput mitgliedsnummer;

  private Input anrede;

  private Input titel;

  private TextInput name;

  private TextInput vorname;

  private Input adressierungszusatz;

  private TextInput strasse;

  private Input plz;

  private Input ort;

  private StaatSearchInput staat;

  private TextInput leitwegID;

  private DateInput geburtsdatum = null;

  private GeschlechtInput geschlecht;

  private SelectNoScrollInput zahlungsweg;

  private LabelGroup bankverbindungLabelGroup;

  private SelectNoScrollInput zahlungsrhytmus;

  private SelectNoScrollInput zahlungstermin;

  private TextInput mandatid = null;

  private DateInput mandatdatum = null;

  private SpinnerNoScrollInput mandatversion = null;

  private DateInput letztelastschrift = null;

  private TextInput bic;

  private TextInput iban;

  private TextInput kontoinhaber;

  private Input telefonprivat;

  private Input telefondienstlich;

  private Input handy;

  private EmailInput email;

  private DateInput eintritt = null;

  private SelectNoScrollInput beitragsgruppe;

  private TreePart sekundaerebeitragsgruppe;

  private DecimalInput individuellerbeitrag;

  private Familienverband famverb;

  private MitgliedSekundaereBeitragsgruppePart mitgliedSekundaereBeitragsgruppeView;

  private MitgliedNextBGruppePart zukueftigeBeitraegeView;

  private AbstractInput zahler;

  private AbstractInput abweichenderZahlerInput;

  private DateInput austritt = null;

  private DateInput kuendigung = null;

  private DateInput sterbetag = null;

  private Input[] zusatzfelder;

  private TreePart eigenschaftenTree;

  private TextAreaInput vermerk1;

  private TextAreaInput vermerk2;

  private Mitglied mitglied;

  // Liste aller Zusatzbeträge
  private BetragSummaryTablePart zusatzbetraegeList;

  // Liste der Wiedervorlagen
  private AutoUpdateTablePart wiedervorlageList;

  // Liste der Mails
  private JVereinTablePart mailList;

  // Liste der Arbeitseinsätze
  private JVereinTablePart arbeitseinsatzList;

  // Liste der Lehrgänge
  private JVereinTablePart lehrgaengeList;

  private JVereinTablePart familienangehoerige;

  private ImageInput foto;

  private JVereinTablePart beitragsTabelle;

  private ArrayList<SekundaereBeitragsgruppe> listeSeB;

  // Zeitstempel merken, wann der Letzte refresh ausgeführt wurde.
  private long lastrefresh = 0;

  private String eigenschaftenHash;

  public static MitgliedControl control = null;

  private boolean isMitglied = false;

  private JVereinTablePart zahtlFuer;

  private TabSelection tabSelection = TabSelection.NO_LIST_TAB;

  private MitgliedAuswahl mitgliedAuswahl = MitgliedAuswahl.MITGLIEDER;

  public enum TabSelection
  {
    TAB_ZUSATZBETRAEGE,
    TAB_WIEDERVORLAGEN,
    TAB_MAILS,
    TAB_LEHRGAENGE,
    TAB_LESEFELDER,
    TAB_ARBEITSEINSAETZE,
    TAB_DOKUMENTE,
    NO_LIST_TAB;
  }

  public void setTabSelection(TabSelection selection)
  {
    tabSelection = selection;
  }

  public MitgliedControl(AbstractView view)
  {
    super(view);
    control = this;
    if (view instanceof AbstractMitgliedDetailView)
    {
      isMitglied = ((AbstractMitgliedDetailView) view).isMitgliedDetail();
    }
    if (view instanceof MitgliedListeView)
    {
      isMitglied = true;
    }
  }

  public Mitglied getMitglied()
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    mitglied = (Mitglied) getCurrentObject();
    return mitglied;
  }

  public void setMitglied(Mitglied mitglied)
  {
    this.mitglied = mitglied;
  }

  public SelectNoScrollInput getMitgliedstyp() throws RemoteException
  {
    if (mitgliedstyp != null)
    {
      return mitgliedstyp;
    }
    Mitgliedstyp typ = null;
    try
    {
      typ = getMitglied().getMitgliedstyp();
    }
    catch (ObjectNotFoundException e)
    {
      // Weil z.B. der default Mitgliedstyp gelöscht wurde
      // Dann wird es das erste aus der Liste
    }
    DBIterator<Mitgliedstyp> mtIt = Einstellungen.getDBService()
        .createList(Mitgliedstyp.class);
    mtIt.addFilter(Mitgliedstyp.JVEREINID + " != " + Mitgliedstyp.MITGLIED
        + " OR " + Mitgliedstyp.JVEREINID + " IS NULL");
    mtIt.setOrder("order by " + Mitgliedstyp.BEZEICHNUNG);
    mitgliedstyp = new SelectNoScrollInput(
        mtIt != null ? PseudoIterator.asList(mtIt) : null, typ);
    mitgliedstyp.setName("Mitgliedstyp");
    mitgliedstyp.addListener(event -> {
      try
      {
        Einstellungen.setSettingInt("defaultmitgliedstyp", (Integer
            .parseInt(((Mitgliedstyp) getMitgliedstyp().getValue()).getID())));
      }
      catch (RemoteException | NumberFormatException e1)
      {
        Logger.error("Fehler", e1);
      }
    });
    return mitgliedstyp;
  }

  public TextInput getExterneMitgliedsnummer() throws RemoteException
  {
    if (externemitgliedsnummer != null)
    {
      return externemitgliedsnummer;
    }
    externemitgliedsnummer = new TextInput(
        getMitglied().getExterneMitgliedsnummer(), 50);
    externemitgliedsnummer.setName("Ext. Mitgliedsnummer");
    externemitgliedsnummer.setMandatory(isExterneMitgliedsnummerMandatory());
    return externemitgliedsnummer;
  }

  private boolean isExterneMitgliedsnummerMandatory() throws RemoteException
  {
    if (!((Boolean) Einstellungen
        .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER)))
    {
      return false;
    }
    return isMitglied;
  }

  public TextInput getMitgliedsnummer() throws RemoteException
  {
    if (mitgliedsnummer != null)
    {
      return mitgliedsnummer;
    }
    mitgliedsnummer = new TextInput(getMitglied().getID(), 10);
    mitgliedsnummer.setName("Mitgliedsnummer");
    mitgliedsnummer.setEnabled(false);
    return mitgliedsnummer;
  }

  public Input getAnrede() throws RemoteException
  {
    if (anrede != null)
    {
      return anrede;
    }
    anrede = new TextInput(getMitglied().getAnrede(), 40);
    anrede.setName("Anrede");
    return anrede;
  }

  public Input getTitel() throws RemoteException
  {
    if (titel != null)
    {
      return titel;
    }
    titel = new TextInput(getMitglied().getTitel(), 40);
    titel.setName("Titel");
    return titel;
  }

  public TextInput getName(boolean withFocus) throws RemoteException
  {
    if (name != null)
    {
      return name;
    }

    name = new TextInput(getMitglied().getName(), 40);
    name.setName("Name");
    name.setMandatory(true);
    if (withFocus)
    {
      name.focus();
    }
    return name;
  }

  public TextInput getVorname() throws RemoteException
  {
    if (vorname != null)
    {
      return vorname;
    }

    vorname = new TextInput(getMitglied().getVorname(), 40);
    vorname.setName("Vorname");
    vorname.setMandatory(true);
    return vorname;
  }

  public Input getAdressierungszusatz() throws RemoteException
  {
    if (adressierungszusatz != null)
    {
      return adressierungszusatz;
    }
    adressierungszusatz = new TextInput(getMitglied().getAdressierungszusatz(),
        40);
    adressierungszusatz.setName("Adressierungszusatz");
    return adressierungszusatz;
  }

  public TextInput getStrasse() throws RemoteException
  {
    if (strasse != null)
    {
      return strasse;
    }
    strasse = new TextInput(getMitglied().getStrasse(), 40);

    strasse.setName("Straße");
    return strasse;
  }

  public Input getPlz() throws RemoteException
  {
    if (plz != null)
    {
      return plz;
    }
    plz = new TextInput(getMitglied().getPlz(), 10);
    plz.setName("PLZ");
    plz.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        if (event.type == SWT.FocusOut)
        {
          String hplz = (String) plz.getValue();
          if (hplz.equals(""))
          {
            return;
          }
          try
          {
            DBIterator<Mitglied> it = Einstellungen.getDBService()
                .createList(Mitglied.class);
            it.addFilter("plz='" + (String) plz.getValue() + "'");
            if (it.hasNext())
            {
              Mitglied mplz = it.next();
              ort.setValue(mplz.getOrt());
            }
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
        }
      }
    });
    return plz;
  }

  public Input getOrt() throws RemoteException
  {
    if (ort != null)
    {
      return ort;
    }
    ort = new TextInput(getMitglied().getOrt(), 40);
    ort.setName("Ort");
    return ort;
  }

  public StaatSearchInput getStaat() throws RemoteException
  {
    if (staat != null)
    {
      return staat;
    }
    if (getMitglied().getStaat() != null
        && getMitglied().getStaat().length() > 0
        && Staat.getByKey(getMitglied().getStaatCode()) == null)
    {
      GUI.getStatusBar().setErrorText("Konnte Staat \""
          + getMitglied().getStaat() + "\" nicht finden, bitte anpassen.");
    }
    staat = new StaatSearchInput();
    staat.setSearchString("Zum Suchen tippen");
    staat.setValue(Staat.getByKey(getMitglied().getStaatCode()));
    staat.setName("Staat");
    return staat;
  }

  public TextInput getLeitwegID() throws RemoteException
  {
    if (leitwegID != null)
    {
      return leitwegID;
    }
    leitwegID = new TextInput(getMitglied().getLeitwegID(), 50);
    leitwegID.setName("LeitwegID");
    return leitwegID;
  }

  public DateInput getGeburtsdatum() throws RemoteException
  {
    if (geburtsdatum != null)
    {
      return geburtsdatum;
    }
    Date d = getMitglied().getGeburtsdatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    this.geburtsdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.geburtsdatum.setName("Geburtsdatum");
    this.geburtsdatum.setTitle("Geburtsdatum");
    this.geburtsdatum.setText("Bitte Geburtsdatum wählen");
    zeigeAlter(d);
    if (isMitglied)
    {
      this.geburtsdatum.setMandatory(
          (Boolean) Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT));
    }
    else
    {
      this.geburtsdatum.setMandatory((Boolean) Einstellungen
          .getEinstellung(Property.NICHTMITGLIEDGEBURTSDATUMPFLICHT));
    }
    return geburtsdatum;
  }

  private void zeigeAlter(Date datum)
  {
    Integer alter = Datum.getAlter(datum);
    if (null != alter)
      geburtsdatum.setComment(" Alter: " + alter.toString());
    else
      geburtsdatum.setComment(" ");
  }

  public GeschlechtInput getGeschlecht() throws RemoteException
  {
    if (geschlecht != null)
    {
      return geschlecht;
    }
    String g = getMitglied().getGeschlecht();
    geschlecht = new GeschlechtInput(
        g == null ? "o" : getMitglied().getGeschlecht());
    geschlecht.setName("Geschlecht");
    geschlecht.setPleaseChoose("Bitte auswählen");
    geschlecht.setMandatory(true);
    geschlecht.setName("Geschlecht");
    return geschlecht;
  }

  public SelectNoScrollInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }

    ArrayList<Zahlungsweg> weg = Zahlungsweg.getArray();

    if (getMitglied().getZahlungsweg() != null)
    {
      zahlungsweg = new SelectNoScrollInput(weg,
          new Zahlungsweg(getMitglied().getZahlungsweg().intValue()));
    }
    else
    {
      zahlungsweg = new SelectNoScrollInput(weg, new Zahlungsweg(
          (Integer) Einstellungen.getEinstellung(Property.ZAHLUNGSWEG)));
    }

    zahlungsweg.setName("Zahlungsweg des Mitglieds");
    zahlungsweg.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        if (event != null && event.type == SWT.Selection)
        {
          try
          {
            if (((Zahlungsweg) getZahlungsweg().getValue())
                .getKey() != Zahlungsweg.BASISLASTSCHRIFT)
            {
              mandatid.setMandatory(false);
              mandatdatum.setMandatory(false);
              mandatversion.setMandatory(false);
              iban.setMandatory(false);
            }
            else
            {
              mandatid.setMandatory(true);
              mandatdatum.setMandatory(true);
              mandatversion.setMandatory(true);
              iban.setMandatory(true);
            }
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler beim Zahlungsweg setzen.", e);
          }
        }
      }
    });
    return zahlungsweg;
  }

  public Input getAbweichenderZahler() throws RemoteException
  {
    if (abweichenderZahlerInput != null)
    {
      return abweichenderZahlerInput;
    }
    abweichenderZahlerInput = new MitgliedInput().getMitgliedInput(
        abweichenderZahlerInput, getMitglied().getAbweichenderZahler(),
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    if (abweichenderZahlerInput instanceof SelectInput)
    {
      ((SelectInput) abweichenderZahlerInput)
          .setPleaseChoose("Kein abweichender Zahler");
      if (getMitglied().getAbweichenderZahler() == null)
      {
        ((SelectInput) abweichenderZahlerInput).setPreselected(null);
      }
    }
    if (isMitglied)
    {
      abweichenderZahlerInput
          .setName("Abweichender Zahler für Beiträge und Zusatzbeträge");
    }
    else
    {
      abweichenderZahlerInput.setName("Abweichender Zahler für Zusatzbeträge");
    }
    return abweichenderZahlerInput;
  }

  private Long getSelectedAbweichenderZahlerId() throws ApplicationException
  {
    try
    {
      if (abweichenderZahlerInput == null)
      {
        return null;
      }
      Mitglied derAltZahler = (Mitglied) getAbweichenderZahler().getValue();
      if (null == derAltZahler)
      {
        return null;
      }
      return Long.valueOf(derAltZahler.getID());
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählter abweichender Zahler kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  // Lösche alle Daten aus der Bankverbindungsmaske
  private void deleteBankverbindung()
  {
    try
    {
      getZahlungsrhythmus().setValue(new Zahlungsrhythmus(
          (Integer) Einstellungen.getEinstellung(Property.ZAHLUNGSRHYTMUS)));
      getMandatID().setValue(null);
      getMandatDatum().setValue(null);
      getMandatVersion().setValue(null);
      getLetzteLastschrift().setValue(null);
      getBic().setValue(null);
      getIban().setValue(null);
      getKontoinhaber().setValue(null);
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim Leeren der Bankverbindungsdaten", e);
    }
  }

  public LabelGroup getBankverbindungLabelGroup(Composite parent)
  {
    if (bankverbindungLabelGroup == null)
    {
      bankverbindungLabelGroup = new LabelGroup(parent, "Bankverbindung");
    }
    return bankverbindungLabelGroup;
  }

  public SelectInput getZahlungsrhythmus() throws RemoteException
  {
    if (zahlungsrhytmus != null)
    {
      return zahlungsrhytmus;
    }
    if (getMitglied().getZahlungsrhythmus() != null)
    {
      zahlungsrhytmus = new SelectNoScrollInput(Zahlungsrhythmus.getArray(),
          new Zahlungsrhythmus(getMitglied().getZahlungsrhythmus().getKey()));
    }
    else if (getMitglied().isNewObject())
    {
      zahlungsrhytmus = new SelectNoScrollInput(Zahlungsrhythmus.getArray(),
          new Zahlungsrhythmus((Integer) Einstellungen
              .getEinstellung(Property.ZAHLUNGSRHYTMUS)));
    }
    else
    {
      zahlungsrhytmus = new SelectNoScrollInput(Zahlungsrhythmus.getArray(),
          null);
    }
    zahlungsrhytmus.setName("Zahlungsrhytmus");
    zahlungsrhytmus.setPleaseChoose("Bitte wählen");
    zahlungsrhytmus.setMandatory(true);
    return zahlungsrhytmus;
  }

  public SelectInput getZahlungstermin() throws RemoteException
  {
    if (zahlungstermin != null)
    {
      return zahlungstermin;
    }
    zahlungstermin = new SelectNoScrollInput(Zahlungstermin.values(),
        getMitglied().getZahlungstermin());
    zahlungstermin.setName("Zahlungstermin");
    zahlungstermin.setPleaseChoose("Bitte wählen");
    zahlungstermin.setMandatory(true);
    return zahlungstermin;
  }

  public TextInput getBic() throws RemoteException
  {
    if (bic != null)
    {
      return bic;
    }
    bic = new BICInput(getMitglied().getBic());
    return bic;
  }

  public TextInput getKontoinhaber() throws RemoteException
  {
    if (kontoinhaber != null)
    {
      return kontoinhaber;
    }
    kontoinhaber = new TextInput(getMitglied().getKontoinhaber(), 70);
    kontoinhaber.setName("Kontoinhaber");
    kontoinhaber.setHint("Optional");
    return kontoinhaber;
  }

  public TextInput getMandatID() throws RemoteException
  {
    if (mandatid != null)
    {
      return mandatid;
    }
    mandatid = new TextInput(getMitglied().getMandatID(), 35);
    mandatid.setName("Mandats-ID");
    if (((Zahlungsweg) getZahlungsweg().getValue())
        .getKey() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      mandatid.setMandatory(false);
    }
    else
    {
      mandatid.setMandatory(true);
    }
    if ((Integer) Einstellungen.getEinstellung(
        Property.SEPAMANDATIDSOURCE) != SepaMandatIdSource.INDIVIDUELL)
    {
      mandatid.disable();
    }
    return mandatid;
  }

  public DateInput getMandatDatum() throws RemoteException
  {
    if (mandatdatum != null)
    {
      return mandatdatum;
    }

    Date d = getMitglied().getMandatDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    this.mandatdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.mandatdatum.setTitle("Datum des Mandats");
    this.mandatdatum.setName("Datum des Mandats");
    this.mandatdatum.setText("Bitte Datum des Mandats wählen");
    if (((Zahlungsweg) getZahlungsweg().getValue())
        .getKey() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      mandatdatum.setMandatory(false);
    }
    else
    {
      mandatdatum.setMandatory(true);
    }
    return mandatdatum;
  }

  public SpinnerInput getMandatVersion() throws RemoteException
  {
    if (mandatversion != null)
    {
      return mandatversion;
    }
    mandatversion = new SpinnerNoScrollInput(0, 1000,
        getMitglied().getMandatVersion());
    mandatversion.setName("Mandatsversion");
    mandatversion.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          getMitglied()
              .setMandatVersion((Integer) getMandatVersion().getValue());
          mandatid.setValue(getMitglied().getMandatID());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }

      }
    });
    if (((Zahlungsweg) getZahlungsweg().getValue())
        .getKey() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      mandatversion.setMandatory(false);
    }
    else
    {
      mandatversion.setMandatory(true);
    }
    return mandatversion;
  }

  public DateInput getLetzteLastschrift() throws RemoteException
  {
    if (letztelastschrift != null)
    {
      return letztelastschrift;
    }

    Date d = getMitglied().getLetzteLastschrift();
    this.letztelastschrift = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.letztelastschrift.setEnabled(false);
    this.letztelastschrift.setName("Letzte Lastschrift");
    return letztelastschrift;
  }

  public TextInput getIban() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }
    iban = new IBANInput(new IBANFormatter().format(getMitglied().getIban()),
        getBic());
    if (((Zahlungsweg) getZahlungsweg().getValue())
        .getKey() != Zahlungsweg.BASISLASTSCHRIFT)
    {
      iban.setMandatory(false);
    }
    else
    {
      iban.setMandatory(true);
    }
    return iban;
  }

  public Input getTelefonprivat() throws RemoteException
  {
    if (telefonprivat != null)
    {
      return telefonprivat;
    }
    telefonprivat = new TextInput(getMitglied().getTelefonprivat(), 20);
    telefonprivat.setName("Telefon priv.");
    return telefonprivat;
  }

  public Input getTelefondienstlich() throws RemoteException
  {
    if (telefondienstlich != null)
    {
      return telefondienstlich;
    }
    telefondienstlich = new TextInput(getMitglied().getTelefondienstlich(), 20);
    telefondienstlich.setName("Telefon dienstl.");
    return telefondienstlich;
  }

  public Input getHandy() throws RemoteException
  {
    if (handy != null)
    {
      return handy;
    }
    handy = new TextInput(getMitglied().getHandy(), 20);
    handy.setName("Handy");
    return handy;
  }

  public EmailInput getEmail() throws RemoteException
  {
    if (email != null)
    {
      return email;
    }
    email = new EmailInput(getMitglied().getEmail());
    return email;
  }

  public DateInput getEintritt() throws RemoteException
  {
    if (eintritt != null)
    {
      return eintritt;
    }

    Date d = getMitglied().getEintritt();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    this.eintritt = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.eintritt.setTitle("Eintrittsdatum");
    this.eintritt.setName("Eintrittsdatum");
    this.eintritt.setText("Bitte Eintrittsdatum wählen");
    this.eintritt.setMandatory(
        (Boolean) Einstellungen.getEinstellung(Property.EINTRITTSDATUMPFLICHT));
    return eintritt;
  }

  public Input getBeitragsgruppe(boolean allgemein) throws RemoteException
  {
    if (beitragsgruppe != null)
    {
      return beitragsgruppe;
    }
    DBIterator<Beitragsgruppe> list = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    list.addFilter("(sekundaer is null or sekundaer=?)", false);
    list.setOrder("ORDER BY bezeichnung");
    if (!allgemein)
    {
      // alte Beitragsgruppen hatten das Feld Beitragsarten noch nicht gesetzt
      // (NULL)
      // diese Beitragsgruppen müssen hier auch erlaubt sein.
      list.addFilter("beitragsart <> ? or beitragsart IS NULL",
          new Object[] { ArtBeitragsart.FAMILIE_ANGEHOERIGER.getKey() });
    }
    beitragsgruppe = new SelectNoScrollInput(
        list != null ? PseudoIterator.asList(list) : null,
        getMitglied().getBeitragsgruppe());
    beitragsgruppe.setName("Beitragsgruppe");
    beitragsgruppe.setValue(getMitglied().getBeitragsgruppe());
    beitragsgruppe.setMandatory(true);
    beitragsgruppe.setAttribute("bezeichnung");
    beitragsgruppe.setPleaseChoose("Bitte auswählen");
    beitragsgruppe.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        if (event.type != SWT.Selection)
        {
          return;
        }
        try
        {
          Beitragsgruppe bg = (Beitragsgruppe) beitragsgruppe.getValue();
          // Feld zahler ist nur aktiviert, wenn aktuelles Mitglied nicht das
          // zahlende Mitglied der Familie ist.
          if (bg != null
              && bg.getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER)
          {
            if (zahler != null)
            {
              zahler.setEnabled(true);
            }
            // Aktiviere "richtigen" Tab in der Tabs-Tabelle Familienverband
            if (famverb != null)
            {
              famverb.setBeitragsgruppe(bg);
            }
          }
          else
          {
            getMitglied().setVollZahlerID(null);
            disableZahler();
          }
          refreshFamilienangehoerigeTable();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    return beitragsgruppe;
  }

  private void disableZahler()
  {
    if (zahler != null)
    {
      if (zahler instanceof SelectNoScrollInput)
      {
        ((SelectNoScrollInput) zahler).setPreselected(null);
      }
      else if (zahler instanceof VollzahlerSearchInput)
      {
        ((VollzahlerSearchInput) zahler).setValue("Zum Suchen tippen");
      }
      zahler.setEnabled(false);
    }
  }

  public MitgliedSekundaereBeitragsgruppePart getMitgliedSekundaereBeitragsgruppeView()
  {
    if (null == mitgliedSekundaereBeitragsgruppeView)
      mitgliedSekundaereBeitragsgruppeView = new MitgliedSekundaereBeitragsgruppePart(
          this);
    return mitgliedSekundaereBeitragsgruppeView;
  }

  public TreePart getSekundaereBeitragsgruppe() throws RemoteException
  {
    if (sekundaerebeitragsgruppe != null)
    {
      return sekundaerebeitragsgruppe;
    }
    listeSeB = new ArrayList<>();

    DBIterator<Beitragsgruppe> bei = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    bei.addFilter("sekundaer=?", true);
    bei.setOrder("ORDER BY bezeichnung");
    while (bei.hasNext())
    {
      Beitragsgruppe b = bei.next();
      DBIterator<SekundaereBeitragsgruppe> sebei = Einstellungen.getDBService()
          .createList(SekundaereBeitragsgruppe.class);
      sebei.addFilter("mitglied=?", getMitglied().getID());
      sebei.addFilter("beitragsgruppe=?", b.getID());
      if (sebei.hasNext())
      {
        SekundaereBeitragsgruppe sb = (SekundaereBeitragsgruppe) sebei.next();
        listeSeB.add(sb);
      }
      else
      {
        SekundaereBeitragsgruppe sb = (SekundaereBeitragsgruppe) Einstellungen
            .getDBService().createObject(SekundaereBeitragsgruppe.class, null);
        sb.setBeitragsgruppe(Integer.parseInt(b.getID()));
        listeSeB.add(sb);
      }
    }

    sekundaerebeitragsgruppe = new TreePart(listeSeB, null);
    sekundaerebeitragsgruppe.addColumn("Beitragsgruppe",
        "beitragsgruppebezeichnung");
    sekundaerebeitragsgruppe.setCheckable(true);
    sekundaerebeitragsgruppe.setMulti(true);
    sekundaerebeitragsgruppe.setFormatter(new TreeFormatter()
    {
      @Override
      public void format(TreeItem item)
      {
        SekundaereBeitragsgruppe sb = (SekundaereBeitragsgruppe) item.getData();
        try
        {
          item.setChecked(!sb.isNewObject());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler beim TreeFormatter", e);
        }
      }
    });
    return sekundaerebeitragsgruppe;
  }

  public DecimalInput getIndividuellerBeitrag() throws RemoteException
  {
    if (individuellerbeitrag != null)
    {
      return individuellerbeitrag;
    }
    individuellerbeitrag = new DecimalInput(
        getMitglied().getIndividuellerBeitrag(), Einstellungen.DECIMALFORMAT);
    individuellerbeitrag.setName("Individueller Beitrag");
    return individuellerbeitrag;
  }

  /**
   * Liefert ein Part zurück, das den Familienverband anzeigt. Da Container
   * jedoch nur das Hinzufügen von Parts zulassen, ist das Part Familienverband
   * dynamisch: Entweder wird der Familienverband angezeigt (setShow(true)),
   * oder ein leeres Composite (setShow(false))
   * 
   * @return Familienverband Part
   * @throws RemoteException
   */
  public Familienverband getFamilienverband() throws RemoteException
  {
    if (famverb != null)
    {
      return famverb;
    }
    famverb = new Familienverband(this, getMitglied().getBeitragsgruppe());
    return famverb;
  }

  public MitgliedNextBGruppePart getZukuenftigeBeitraegeView()
  {
    if (null == zukueftigeBeitraegeView)
      zukueftigeBeitraegeView = new MitgliedNextBGruppePart(this);
    return zukueftigeBeitraegeView;
  }

  public Input getZahler() throws RemoteException
  {
    return getZahler(false);
  }

  public Input getZahler(boolean force) throws RemoteException
  {
    if (zahler != null)
    {
      // wenn force nicht gesetzt, gib aktuellen zahler zurück.
      if (!force)
        return zahler;
      // ansonsten: erzeuge neuen...
      // Dies ist nötig, wenn Zahler ausgeblendet wurde und daher der
      // Parent vom GC disposed wurde.
    }
    zahler = new VollzahlerInput().getMitgliedInput(zahler, getMitglied(),
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));

    zahler.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        try
        {
          Mitglied m = (Mitglied) zahler.getValue();
          if (m != null && m.getID() != null)
          {
            getMitglied().setVollZahlerID(Long.valueOf(m.getID()));

            // Nachfrage, ob der neue Vollzahler auch als abweichender Zahler
            // gesetzt werden soll
            YesNoDialog ynd = new YesNoDialog(AbstractDialog.POSITION_CENTER);
            ynd.setText(
                "Soll der Vollzahler auch als abweichender Zahler gesetzt werden?");
            ynd.setTitle("Vollzahler auch als abweichenden Zahler setzen");
            Boolean choice;
            try
            {
              choice = (Boolean) ynd.open();
              if (choice.booleanValue())
              {
                getAbweichenderZahler().setValue(m);
              }
            }
            catch (Exception e)
            {
              Logger.error("Fehler", e);
            }
          }
          else
          {
            getMitglied().setVollZahlerID(null);
          }
          refreshFamilienangehoerigeTable();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });

    if (getBeitragsgruppe(true) != null
        && getBeitragsgruppe(true).getValue() != null
        && ((Beitragsgruppe) getBeitragsgruppe(true).getValue())
            .getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER)
    {
      zahler.setEnabled(true);
    }
    else
    {
      disableZahler();
    }

    return zahler;
  }

  public DateInput getAustritt() throws RemoteException
  {
    if (austritt != null)
    {
      return austritt;
    }
    Date d = getMitglied().getAustritt();

    this.austritt = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.austritt.setTitle("Austrittsdatum");
    this.austritt.setName("Austrittsdatum");
    this.austritt.setText("Bitte Austrittsdatum wählen");
    return austritt;
  }

  public DateInput getKuendigung() throws RemoteException
  {
    if (kuendigung != null)
    {
      return kuendigung;
    }
    Date d = getMitglied().getKuendigung();

    this.kuendigung = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.kuendigung.setName("Kündigungsdatum");
    this.kuendigung.setTitle("Kündigungsdatum");
    this.kuendigung.setText("Bitte Kündigungsdatum wählen");
    return kuendigung;
  }

  public DateInput getSterbetag() throws RemoteException
  {
    if (sterbetag != null)
    {
      return sterbetag;
    }
    Date d = getMitglied().getSterbetag();

    this.sterbetag = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.sterbetag.setName("Sterbetag");
    this.sterbetag.setTitle("Sterbetag");
    this.sterbetag.setText("Bitte Sterbetag wählen");
    return sterbetag;
  }

  public TextAreaInput getVermerk1() throws RemoteException
  {
    if (vermerk1 != null)
    {
      return vermerk1;
    }
    vermerk1 = new TextAreaInput(getMitglied().getVermerk1(), 2000);
    vermerk1.setName("Vermerk 1");
    return vermerk1;
  }

  public TextAreaInput getVermerk2() throws RemoteException
  {
    if (vermerk2 != null)
    {
      return vermerk2;
    }
    vermerk2 = new TextAreaInput(getMitglied().getVermerk2(), 2000);
    vermerk2.setName("Vermerk 2");
    return vermerk2;
  }

  public ImageInput getFoto() throws RemoteException
  {
    if (foto != null)
    {
      return foto;
    }
    DBIterator<Mitgliedfoto> it = Einstellungen.getDBService()
        .createList(Mitgliedfoto.class);
    it.addFilter("mitglied = ?", new Object[] { mitglied.getID() });
    Mitgliedfoto fo = null;
    if (it.size() > 0)
    {
      fo = (Mitgliedfoto) it.next();
    }
    byte[] f = null;
    if (fo != null)
    {
      f = fo.getFoto();
    }
    foto = new ImageInput(f, 150, 200);
    return foto;
  }

  public Input[] getZusatzfelder() throws RemoteException
  {
    if (zusatzfelder != null)
    {
      return zusatzfelder;
    }
    DBIterator<Felddefinition> it = Einstellungen.getDBService()
        .createList(Felddefinition.class);
    it.setOrder("order by label");
    int anzahl = it.size();
    if (anzahl == 0)
    {
      return null;
    }
    zusatzfelder = new Input[anzahl];
    Zusatzfelder zf = null;
    int i = 0;
    while (it.hasNext())
    {
      Felddefinition fd = it.next();
      zf = (Zusatzfelder) Einstellungen.getDBService()
          .createObject(Zusatzfelder.class, null);
      zf.setFelddefinition(Integer.parseInt(fd.getID()));

      if (getMitglied().getID() != null)
      {
        DBIterator<Zusatzfelder> it2 = Einstellungen.getDBService()
            .createList(Zusatzfelder.class);
        it2.addFilter("mitglied=?", new Object[] { getMitglied().getID() });
        it2.addFilter("felddefinition=?", new Object[] { fd.getID() });
        if (it2.size() > 0)
        {
          zf.setMitglied(Integer.parseInt(getMitglied().getID()));
          zf = it2.next();
        }
      }
      switch (fd.getDatentyp())
      {
        case Datentyp.ZEICHENFOLGE:
          zusatzfelder[i] = new TextInput(zf.getFeld(), fd.getLaenge());
          break;
        case Datentyp.DATUM:
          Date d = zf.getFeldDatum();
          DateInput di = new DateInput(d, new JVDateFormatTTMMJJJJ());
          di.setName(fd.getLabel());
          di.setTitle(fd.getLabel());
          di.setText(String.format("Bitte %s wählen", fd.getLabel()));
          zusatzfelder[i] = di;
          break;
        case Datentyp.GANZZAHL:
          if (zf.getFeldGanzzahl() == null)
          {
            zusatzfelder[i] = new IntegerNullInput();
          }
          else
          {
            zusatzfelder[i] = new IntegerNullInput(zf.getFeldGanzzahl());
          }
          break;
        case Datentyp.WAEHRUNG:
          zusatzfelder[i] = new DecimalInput(zf.getFeldWaehrung(),
              Einstellungen.DECIMALFORMAT);
          break;
        case Datentyp.JANEIN:
          zusatzfelder[i] = new CheckboxInput(zf.getFeldJaNein());
          break;
        default:
          zusatzfelder[i] = new TextInput("", fd.getLaenge());
          break;
      }
      zusatzfelder[i].setName(fd.getLabel());
      if (fd.getLabel() == null)
      {
        zusatzfelder[i].setName(fd.getName());
      }
      // Alten wert speichern
      zusatzfelder[i].setData("old", zusatzfelder[i].getValue());
      i++;
    }
    return zusatzfelder;
  }

  public void refreshFamilienangehoerigeTable() throws RemoteException
  {
    if (familienangehoerige == null)
      return;
    familienangehoerige.removeAll();
    DBService service = Einstellungen.getDBService();
    DBIterator<Mitglied> famiter = service.createList(Mitglied.class);
    famiter.addFilter("zahlerid = ? or zahlerid = ? or id = ? or id = ?",
        getMitglied().getID(), getMitglied().getVollZahlerID(),
        getMitglied().getID(), getMitglied().getVollZahlerID());
    famiter.setOrder("ORDER BY name, vorname");
    while (famiter.hasNext())
    {
      Mitglied m = famiter.next();
      // Wenn der Iterator auf das aktuelle Mitglied zeigt,
      // nutze stattdessen getMitglied() damit nicht das alte, unveränderte
      // Mitglied
      // aus der DB verwendet wird, sondern das vom Nutzer veränderte Mitglied.
      if (m.getID().equalsIgnoreCase(getMitglied().getID()))
        m = getMitglied();
      familienangehoerige.addItem(m);
    }
    familienangehoerige.sort();
  }

  public JVereinTablePart getFamilienangehoerigenTable() throws RemoteException
  {
    if (familienangehoerige != null)
    {
      return familienangehoerige;
    }

    familienangehoerige = new JVereinTablePart(new MitgliedDetailAction());
    refreshFamilienangehoerigeTable();
    familienangehoerige.addColumn("Name", "name");
    familienangehoerige.addColumn("Vorname", "vorname");
    familienangehoerige.addColumn("", "zahlerid", new Formatter()
    {

      @Override
      public String format(Object o)
      {
        // Alle Familienmitglieder, die eine Zahler-ID eingetragen haben, sind
        // nicht selbst das vollzahlende Mitglied.
        // Der Eintrag ohne zahlerid ist also das vollzahlende Mitglied.
        if (o == null)
          return "";
        else
          return "Familienmitglied";
      }
    });

    return familienangehoerige;
  }

  public boolean isZahltFuerVisible() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Mitglied> mitglieder = service.createList(Mitglied.class);
    mitglieder.addFilter("altzahler = " + getMitglied().getID());
    return mitglieder.hasNext();
  }

  public JVereinTablePart getZahltFuer() throws RemoteException
  {
    if (zahtlFuer != null)
    {
      return zahtlFuer;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Mitglied> mitglieder = service.createList(Mitglied.class);
    mitglieder.addFilter("altzahler = " + getMitglied().getID());

    zahtlFuer = new JVereinTablePart(mitglieder, new MitgliedDetailAction());
    zahtlFuer.addColumn("Name", "name");
    zahtlFuer.addColumn("Vorname", "vorname");

    return zahtlFuer;
  }

  public JVereinTablePart getZusatzbetraegeTable() throws RemoteException
  {
    if (zusatzbetraegeList != null)
    {
      return zusatzbetraegeList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Zusatzbetrag> zusatzbetraege = service
        .createList(Zusatzbetrag.class);
    zusatzbetraege.addFilter("mitglied = " + getMitglied().getID());
    zusatzbetraegeList = new BetragSummaryTablePart(zusatzbetraege,
        new EditAction(ZusatzbetragDetailView.class));
    zusatzbetraegeList.setTableName("Zusatzbeträge");
    zusatzbetraegeList.setMulti(true);
    zusatzbetraegeList.addColumn("Erste Fälligkeit", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Nächste Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Letzte abgerechnete Fälligkeit",
        "ausfuehrung", new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Intervall", "intervalltext");
    zusatzbetraegeList.addColumn("Nicht mehr ausführen ab", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Buchungstext", "buchungstext");
    zusatzbetraegeList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    zusatzbetraegeList.addColumn("Zahlungsweg", "zahlungsweg",
        o -> new Zahlungsweg((Integer) o).getText());
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      zusatzbetraegeList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    zusatzbetraegeList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      zusatzbetraegeList.addColumn("Steuer", "steuer", o -> {
        if (o == null)
        {
          return "";
        }
        try
        {
          return ((Steuer) o).getName();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return "";
      }, false, Column.ALIGN_RIGHT);
    }
    zusatzbetraegeList.setContextMenu(new ZusatzbetraegeMenu(null));
    return zusatzbetraegeList;
  }

  public JVereinTablePart getWiedervorlageTable() throws RemoteException
  {
    if (wiedervorlageList != null)
    {
      return wiedervorlageList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Zusatzbetrag> wiedervorlagen = service
        .createList(Wiedervorlage.class);
    wiedervorlagen.addFilter("mitglied = " + getMitglied().getID());
    wiedervorlagen.setOrder("ORDER BY datum DESC");
    wiedervorlageList = new AutoUpdateTablePart(wiedervorlagen,
        new EditAction(WiedervorlageDetailView.class));
    wiedervorlageList.setTableName("Widervorlagen");
    wiedervorlageList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    wiedervorlageList.addColumn("Vermerk", "vermerk");
    wiedervorlageList.addColumn("Erledigung", "erledigung",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    wiedervorlageList.setContextMenu(new WiedervorlageMenu(null));
    wiedervorlageList.setMulti(true);
    return wiedervorlageList;
  }

  public JVereinTablePart getMailTable() throws RemoteException
  {
    if (mailList != null)
    {
      return mailList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Mail> me = service.createList(Mail.class);
    me.join("mailempfaenger");
    me.addFilter("mailempfaenger.mail = mail.id");
    me.addFilter("mailempfaenger.mitglied = ?", getMitglied().getID());
    mailList = new JVereinTablePart(me, new EditAction(MailDetailView.class));
    mailList.setTableName("Mails");
    mailList.setMulti(true);
    mailList.addColumn("Bearbeitung", "bearbeitung",
        new DateFormatter(new JVDateFormatTIMESTAMP()));
    mailList.addColumn("Versand", "versand",
        new DateFormatter(new JVDateFormatTIMESTAMP()));
    mailList.addColumn("Betreff", "betreff");
    mailList.addColumn("Anhänge", "anhaenge");
    mailList.setContextMenu(new MailMenu(null));
    return mailList;
  }

  public JVereinTablePart getArbeitseinsatzTable() throws RemoteException
  {
    if (arbeitseinsatzList != null)
    {
      return arbeitseinsatzList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Arbeitseinsatz> arbeitseinsaetze = service
        .createList(Arbeitseinsatz.class);
    arbeitseinsaetze.addFilter("mitglied = " + getMitglied().getID());
    arbeitseinsaetze.setOrder("ORDER by datum desc");
    arbeitseinsatzList = new JVereinTablePart(arbeitseinsaetze,
        new EditAction(ArbeitseinsatzDetailView.class));
    arbeitseinsatzList.setTableName("Arbeitseinsätze");
    arbeitseinsatzList.setContextMenu(new ArbeitseinsatzMenu(null));
    arbeitseinsatzList.setMulti(true);
    arbeitseinsatzList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    arbeitseinsatzList.addColumn("Stunden", "stunden",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    arbeitseinsatzList.addColumn("Bemerkung", "bemerkung");
    // wiedervorlageList.setContextMenu(new
    // WiedervorlageMenu(wiedervorlageList));
    return arbeitseinsatzList;
  }

  public JVereinTablePart getLehrgaengeTable() throws RemoteException
  {
    if (lehrgaengeList != null)
    {
      return lehrgaengeList;
    }
    DBService service = Einstellungen.getDBService();
    DBIterator<Lehrgang> lehrgaenge = service.createList(Lehrgang.class);
    lehrgaenge.addFilter("mitglied = " + getMitglied().getID());
    lehrgaengeList = new JVereinTablePart(lehrgaenge,
        new EditAction(LehrgangDetailView.class));
    lehrgaengeList.setTableName("Lehrgänge");
    lehrgaengeList.setMulti(true);
    lehrgaengeList.addColumn("Lehrgangsart", "lehrgangsart");
    lehrgaengeList.addColumn("Bezeichnung", "bezeichnung");
    lehrgaengeList.addColumn("Von/am", "von",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lehrgaengeList.addColumn("Bis", "bis",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lehrgaengeList.addColumn("Veranstalter", "veranstalter");
    lehrgaengeList.addColumn("Ergebnis", "ergebnis");
    lehrgaengeList.setContextMenu(new LehrgangMenu(null));
    return lehrgaengeList;
  }

  public Button getKontoDatenLoeschenButton()
  {
    Button b = new Button("Daten löschen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        YesNoDialog dialog = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        dialog.setTitle("Bankverbindung-Daten löschen");
        dialog.setText("Bankverbindung-Daten löschen?");
        boolean delete = false;
        try
        {
          delete = ((Boolean) dialog.open()).booleanValue();
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Bankverbindung-Löschen-Dialog.", e);
        }
        if (delete)
        {
          deleteBankverbindung();
        }
      }
    }, null, false, "user-trash-full.png");
    // button
    return b;
  }

  public Button getAbweichenderZahlerErzeugenButton()
  {
    Button b = new Button("Abweichenden Zahler anlegen (Nicht-Mitglied)",
        new Action()
        {
          @SuppressWarnings("unchecked")
          @Override
          public void handleAction(Object context) throws ApplicationException
          {

            try
            {
              boolean ktoi = false;
              Mitglied m = getMitglied();
              Mitglied nm = Einstellungen.getDBService()
                  .createObject(Mitglied.class, null);
              if (m.getAttribute("ktoiname") != null
                  && ((String) m.getAttribute("ktoiname")).length() > 0)
              {
                // Für den Fall, dass ein alternativer Kontoinhaber konfiguriert
                // war übernehmen wir diese Daten
                ktoi = true;
                nm.setMitgliedstyp(Long
                    .valueOf(Einstellungen.getSettingInt("defaultmitgliedstyp",
                        Integer.valueOf(Mitgliedstyp.SPENDER))));
                nm.setPersonenart((String) m.getAttribute("ktoipersonenart"));
                nm.setAnrede((String) m.getAttribute("ktoianrede"));
                nm.setTitel((String) m.getAttribute("ktoititel"));
                nm.setName((String) m.getAttribute("ktoiname"));
                nm.setVorname((String) m.getAttribute("ktoivorname"));
                nm.setAdressierungszusatz(
                    (String) m.getAttribute("ktoiadressierungszusatz"));
                nm.setStrasse((String) m.getAttribute("ktoistrasse"));
                nm.setPlz((String) m.getAttribute("ktoiplz"));
                nm.setOrt((String) m.getAttribute("ktoiort"));
                nm.setStaat(
                    Staat.getStaatCode((String) m.getAttribute("ktoistaat")));
                nm.setEmail((String) m.getAttribute("ktoiemail"));
                nm.setGeschlecht((String) m.getAttribute("ktoigeschlecht"));
                nm.setZahlungsweg(m.getZahlungsweg());
                nm.setMandatID(m.getMandatID());
                nm.setMandatDatum(m.getMandatDatum());
                nm.setMandatVersion(m.getMandatVersion());
                nm.setIban(m.getIban());
                nm.setBic(m.getBic());
              }
              else
              {
                if ((Boolean) Einstellungen
                    .getEinstellung(Property.JURISTISCHEPERSONEN))
                {
                  PersonenartDialog pad = new PersonenartDialog(
                      PersonenartDialog.POSITION_CENTER);
                  String pa = pad.open();
                  if (pa == null)
                  {
                    return;
                  }
                  nm.setPersonenart(pa);
                }
                else
                {
                  nm.setPersonenart("n");
                }
                nm.setMitgliedstyp(Long
                    .valueOf(Einstellungen.getSettingInt("defaultmitgliedstyp",
                        Integer.valueOf(Mitgliedstyp.SPENDER))));
                nm.setAnrede("");
                nm.setName((String) getName(false).getValue());
                nm.setVorname("");
                nm.setAdressierungszusatz(
                    (String) getAdressierungszusatz().getValue());
                nm.setStrasse((String) getStrasse().getValue());
                nm.setPlz((String) getPlz().getValue());
                nm.setOrt((String) getOrt().getValue());
                nm.setEmail((String) getEmail().getValue());
                if ((Boolean) Einstellungen
                    .getEinstellung(Property.AUSLANDSADRESSEN))
                {
                  nm.setStaat(getStaat().getValue() == null ? ""
                      : ((Staat) getStaat().getValue()).getKey());
                }
              }

              AbweichenderZahlerNeuDialog dialog = new AbweichenderZahlerNeuDialog(
                  AbweichenderZahlerNeuDialog.POSITION_CENTER, nm);
              if (!dialog.open())
              {
                if (dialog.getStatus() != null)
                {
                  throw new Exception(dialog.getStatus());
                }
                else
                {
                  // Den neuen abweichenden Zahler setzen
                  // Beim SelectInput muss das neue Mitglied erst hinzugefügt
                  // werden. Sonst kann man es nicht zur Anzeige bringen
                  if (getAbweichenderZahler() instanceof SelectInput)
                  {
                    SelectInput input = (SelectInput) getAbweichenderZahler();
                    List<Mitglied> list = new ArrayList<>();
                    list.addAll(input.getList());
                    list.add(nm);
                    input.setList(list);
                  }
                  getAbweichenderZahler().setValue(nm);
                  // Wenn alte ktoi Daten verwendet wurden, dann diese löschen
                  if (ktoi)
                  {
                    getZahlungsweg()
                        .setValue(new Zahlungsweg(Zahlungsweg.ÜBERWEISUNG));
                    deleteBankverbindung();
                    mandatid.setMandatory(false);
                    mandatdatum.setMandatory(false);
                    mandatversion.setMandatory(false);
                    iban.setMandatory(false);
                    m.clearKtoi();
                  }
                }
              }

            }
            catch (OperationCanceledException oce)
            {
              throw oce;
            }
            catch (Exception e)
            {
              throw new ApplicationException(
                  "Fehler beim Erzeugen eines Nicht-Mitgliedes: "
                      + e.getMessage());
            }
          }
        }, null, false, "document-new.png");
    // button
    return b;
  }

  public Button getProfileButton()
  {
    Button b = new Button("Such-Profile", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          saveFilterSettings();
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(e);
        }
        GUI.startView(MitgliedSuchProfilListeView.class.getName(), settings);
      }
    }, null, true, "user-check.png"); // "true" defines this button as the
                                      // default button
    return b;
  }

  public Button getLesefelderEdit()
  {
    return new Button("Bearbeiten",
        new LesefelddefinitionenAction(getMitglied()), null, false,
        "text-x-generic.png");
  }

  public Button getZusatzbetragNeu()
  {
    return new Button(
        "Neuer Zusatzbetrag", new NewAction(ZusatzbetragDetailView.class,
            Zusatzbetrag.class, getMitglied()),
        null, false, "document-new.png");
  }

  public Button getSollbuchungNeu()
  {
    return new Button("Neue Sollbuchung",
        new SollbuchungNeuAction(getMitglied()), null, false,
        "document-new.png");
  }

  public Button getWiedervorlageNeu()
  {
    return new Button(
        "Neue Wiedervorlage", new NewAction(WiedervorlageDetailView.class,
            Wiedervorlage.class, getMitglied()),
        null, false, "document-new.png");
  }

  public Button getArbeitseinsatzNeu()
  {
    return new Button(
        "Neuer Arbeitseinsatz", new NewAction(ArbeitseinsatzDetailView.class,
            Arbeitseinsatz.class, getMitglied()),
        null, false, "document-new.png");
  }

  public Button getLehrgangNeu()
  {
    return new Button("Neuer Lehrgang",
        new NewAction(LehrgangDetailView.class, Lehrgang.class, getMitglied()),
        null, false, "document-new.png");
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    return getTablePart(null);
  }

  public JVereinTablePart getTablePart(Action detailaction)
      throws RemoteException, ApplicationException
  {
    if (mitgliedList != null)
    {
      return mitgliedList;
    }
    mitgliedList = new JVereinTablePart(
        new MitgliedQuery(this).get(mitgliedAuswahl, null), null);
    add("Status", "status", false, new Formatter()
    {
      @Override
      public String format(Object o)
      {
        return (Boolean) o ? "\u2705" : "\u2757";
      }
    }, Column.ALIGN_LEFT, true);
    add("Mitgliedsnummer", "idint", false, true);
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        add("Externe Mitgliedsnummer", "externemitgliedsnummer", false, false);
      }
    }
    catch (RemoteException re)
    {
      //
    }
    add("Kontostand", "kontostand", false, new Formatter()
    {
      @Override
      public String format(Object o)
      {
        String anzeige = Einstellungen.DECIMALFORMAT.format((Double) o) + " ";
        anzeige += ((Double) o) > -0.0049 ? "\u2705" : "\u2757";
        return anzeige;
      }
    }, Column.ALIGN_RIGHT, true);
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.DOKUMENTENSPEICHERUNG))
      {
        add("D", "document", false, true);
      }
    }
    catch (RemoteException e)
    {
      //
    }
    add("Anrede", "anrede", false, true);
    add("Titel", "titel", false, true);
    add("Name", "name", true, true);
    add("Vorname", "vorname", true, true);
    add("Adressierungszusatz", "adressierungszusatz", false, true);
    add("Straße", "strasse", true, true);
    add("PLZ", "plz", false, true);
    add("Ort", "ort", true, true);
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.AUSLANDSADRESSEN))
      {
        add("Staat", "staat", false, new StaatFormatter(), Column.ALIGN_LEFT,
            true);
      }
    }
    catch (RemoteException ignore)
    {
    }
    add("Zahlungsweg", "zahlungsweg", false, new ZahlungswegFormatter(),
        Column.ALIGN_LEFT, true);
    add("Zahlungsrhytmus", "zahlungsrhytmus", false,
        new ZahlungsrhythmusFormatter(), Column.ALIGN_LEFT, false);
    add("Zahlungstermin", "zahlungstermin", false,
        new ZahlungsterminFormatter(), Column.ALIGN_LEFT, true);
    add("Datum des Mandats", "mandatdatum", false, true);
    add("BIC", "bic", false, true);
    add("IBAN", "iban", false, new IBANFormatter(), Column.ALIGN_LEFT, true);
    add("Kontoinhaber", "kontoinhaber", false, true);
    add("Abweichender Zahler", "altzahlerstring", false, true);
    add("Mandat Version", "mandatversion", false, true);
    add("Mandat ID", "mandatid", false, true);
    add("Geburtsdatum", "geburtsdatum", true,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO, true);
    add("Alter", "alter", false, true);
    add("Geschlecht", "geschlecht", false, true);
    add("Telefon privat", "telefonprivat", true, true);
    add("Telefon dienstlich", "telefondienstlich", false, true);
    add("Handy", "handy", false, true);
    add("Email", "email", false, true);
    add("Eintritt", "eintritt", true,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
        false);
    add("Beitragsgruppe", "beitragsgruppe", false, false);
    add("Austritt", "austritt", true,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
        false);
    add("Kündigung", "kuendigung", false,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
        false);
    add("Leitweg ID", "leitwegid", false, true);
    add("Vollzahler", "vollzahlerstring", false, false);
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.INDIVIDUELLEBEITRAEGE))
      {
        add("Individueller Beitrag", "individuellerbeitrag", false, false);
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
      {
        add("Sterbedatum", "sterbetag", false,
            new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
            true);
      }
    }
    catch (RemoteException re)
    {
      //
    }
    add("Eingabedatum", "eingabedatum", false,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO, true);
    add("Letzte Änderung", "letzteaenderung", false,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO, true);
    try
    {
      DBIterator<Felddefinition> it = Einstellungen.getDBService()
          .createList(Felddefinition.class);
      while (it.hasNext())
      {
        Felddefinition fd = (Felddefinition) it.next();
        switch (fd.getDatentyp())
        {
          case Datentyp.DATUM:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false,
                new DateFormatter(new JVDateFormatTTMMJJJJ()),
                Column.ALIGN_AUTO, true);
            break;
          case Datentyp.WAEHRUNG:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false,
                new CurrencyFormatter("", Einstellungen.DECIMALFORMAT),
                Column.ALIGN_AUTO, true);
            break;
          case Datentyp.JANEIN:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false,
                new JaNeinFormatter(), Column.ALIGN_AUTO, true);
            break;
          default:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false, true);
            break;
        }
      }

      DBIterator<EigenschaftGruppe> eigenschaftGruppeit = Einstellungen
          .getDBService().createList(EigenschaftGruppe.class);
      while (eigenschaftGruppeit.hasNext())
      {
        EigenschaftGruppe eg = (EigenschaftGruppe) eigenschaftGruppeit.next();

        add(eg.getBezeichnung(), "eigenschaften_" + eg.getName(), false, true);
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }

    mitgliedList.setContextMenu(new MitgliedMenu(detailaction, mitgliedList));
    mitgliedList.setMulti(true);
    mitgliedList.setRememberState(true);
    if (detailaction instanceof MitgliedDetailAction)
    {
      mitgliedList
          .setAction(new EditAction(MitgliedDetailView.class, mitgliedList));
    }
    else if (detailaction instanceof NichtMitgliedDetailAction)
    {
      mitgliedList.setAction(
          new EditAction(NichtMitgliedDetailView.class, mitgliedList));
    }
    VorZurueckControl.setObjektListe(null, null);
    return mitgliedList;
  }

  private void add(String spaltenbezeichnung, String spaltenname,
      boolean defaultvalue, boolean auchNichtMitglied)
  {
    add(spaltenbezeichnung, spaltenname, defaultvalue, null, Column.ALIGN_AUTO,
        auchNichtMitglied);
  }

  private void add(String spaltenbezeichnung, String spaltenname,
      boolean defaultVisible, Formatter formatter, int align,
      boolean auchNichtMitglied)
  {
    if (isMitglied || auchNichtMitglied)
    {
      mitgliedList.addColumn(
          new Column(spaltenname, spaltenbezeichnung, formatter, false, align),
          defaultVisible);
    }
  }

  public void refreshMitgliedTable()
      throws RemoteException, ApplicationException
  {
    if (System.currentTimeMillis() - lastrefresh < 500)
    {
      Logger.debug(String.format("Zeit zwischen den Refreshs: %s",
          (System.currentTimeMillis() - lastrefresh)));
      return;
    }
    lastrefresh = System.currentTimeMillis();
    mitgliedList.removeAll();
    ArrayList<Mitglied> mitglieder = new MitgliedQuery(this)
        .get(mitgliedAuswahl, null);
    for (Mitglied m : mitglieder)
    {
      mitgliedList.addItem(m);
    }
    mitgliedList.sort();
  }

  public TreePart getEigenschaftenTree() throws RemoteException
  {
    if (eigenschaftenTree != null)
    {
      return eigenschaftenTree;
    }
    eigenschaftenTree = new EigenschaftenTree(mitglied);
    eigenschaftenHash = createEigenschaftenHash();

    return eigenschaftenTree;
  }

  /**
   * Zur überwachung der Änderungen einen Hash erzeugen
   * 
   * @throws RemoteException
   */
  private String createEigenschaftenHash() throws RemoteException
  {
    String hash = "";
    if (eigenschaftenTree != null)
    {
      for (Object o : eigenschaftenTree.getItems())
      {
        EigenschaftenNode node = (EigenschaftenNode) o;
        for (EigenschaftenNode n : node.getCheckedNodes())
        {
          if (n.getEigenschaft() != null)
          {
            hash += n.getEigenschaft().getID();
          }
        }
      }
    }
    return hash;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Mitglied m = getMitglied();

    if (m.getPersonenart().equalsIgnoreCase("n"))
    {
      // Für natürliche Personen
      m.setTitel((String) getTitel().getValue());
      m.setGeburtsdatum((Date) getGeburtsdatum().getValue());
      m.setGeschlecht((String) getGeschlecht().getValue());
    }
    else
    {
      // Für juristische Personen
      m.setLeitwegID((String) getLeitwegID().getValue());
    }

    // Für Mitglieder
    if (isMitglied)
    {
      m.setMitgliedstyp(Long.valueOf(Mitgliedstyp.MITGLIED));
      Beitragsgruppe bg = (Beitragsgruppe) getBeitragsgruppe(true).getValue();
      m.setBeitragsgruppe(bg);
      if (bg != null
          && bg.getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
      {
        m.setVollZahlerID(null);
      }
      if ((Boolean) Einstellungen
          .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        String mitgliedsnummer = (String) getExterneMitgliedsnummer()
            .getValue();
        if (mitgliedsnummer != null && !mitgliedsnummer.isEmpty())
        {
          m.setExterneMitgliedsnummer(mitgliedsnummer);
        }
        else
        {
          m.setExterneMitgliedsnummer(null);
        }
      }
      if ((Boolean) Einstellungen
          .getEinstellung(Property.INDIVIDUELLEBEITRAEGE))
      {
        m.setIndividuellerBeitrag(
            (Double) getIndividuellerBeitrag().getValue());
      }
      else
      {
        m.setIndividuellerBeitrag(null);
      }
      m.setEintritt((Date) getEintritt().getValue());
      m.setAustritt((Date) getAustritt().getValue());
      m.setKuendigung((Date) getKuendigung().getValue());
      m.setSterbetag((Date) getSterbetag().getValue());
    }
    else
    {
      Mitgliedstyp mt = (Mitgliedstyp) getMitgliedstyp().getValue();
      m.setMitgliedstyp(Long.valueOf(mt.getID()));
    }

    // Stammdaten
    m.setAnrede((String) getAnrede().getValue());
    m.setName((String) getName(false).getValue());
    m.setVorname((String) getVorname().getValue());
    m.setAdressierungszusatz((String) getAdressierungszusatz().getValue());
    m.setStrasse((String) getStrasse().getValue());
    m.setPlz((String) getPlz().getValue());
    m.setOrt((String) getOrt().getValue());
    m.setStaat(getStaat().getValue() == null ? ""
        : ((Staat) getStaat().getValue()).getKey());
    m.setTelefonprivat((String) getTelefonprivat().getValue());
    m.setHandy((String) getHandy().getValue());
    m.setTelefondienstlich((String) getTelefondienstlich().getValue());
    m.setEmail((String) getEmail().getValue());

    // Zahlung
    Zahlungsweg zw = (Zahlungsweg) getZahlungsweg().getValue();
    m.setZahlungsweg(zw.getKey());
    if (zahlungsrhytmus != null)
    {
      Zahlungsrhythmus zr = (Zahlungsrhythmus) getZahlungsrhythmus().getValue();
      if (zr != null)
      {
        m.setZahlungsrhythmus(zr.getKey());
      }
    }
    if (zahlungstermin != null)
    {
      Zahlungstermin zt = (Zahlungstermin) getZahlungstermin().getValue();
      if (zt != null)
      {
        m.setZahlungstermin(zt.getKey());
      }
    }
    m.setMandatDatum((Date) getMandatDatum().getValue());
    m.setMandatVersion((Integer) getMandatVersion().getValue());
    m.setBic((String) getBic().getValue());
    String ib = (String) getIban().getValue();
    if (ib == null)
      m.setIban("");
    else
      m.setIban(ib.replace(" ", ""));
    m.setAbweichenderZahlerID(getSelectedAbweichenderZahlerId());
    m.setKontoinhaber((String) getKontoinhaber().getValue());

    // Vermerke
    m.setVermerk1((String) getVermerk1().getValue());
    m.setVermerk2((String) getVermerk2().getValue());

    if (m.getID() == null)
    {
      m.setEingabedatum();
    }

    // ManadatID hier setzen wenn sie editierbar ist
    int sepaMandatIdSource = (Integer) Einstellungen
        .getEinstellung(Property.SEPAMANDATIDSOURCE);
    if (sepaMandatIdSource != SepaMandatIdSource.EXTERNE_MITGLIEDSNUMMER
        && sepaMandatIdSource != SepaMandatIdSource.DBID)
    {
      m.setMandatID((String) getMandatID().getValue());
    }

    return m;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Mitglied m = (Mitglied) prepareStore();

      // Es wird hier geprüft weil die Daten nur im Tree sind und erst nach dem
      // store() in die DB geschrieben werden
      m.checkEigenschaften(eigenschaftenTree);
      // MandatID hier setzen weil sie bei früheren Mitgliedern nicht
      // gespeichert war
      int sepaMandatIdSource = (Integer) Einstellungen
          .getEinstellung(Property.SEPAMANDATIDSOURCE);
      if (sepaMandatIdSource == SepaMandatIdSource.EXTERNE_MITGLIEDSNUMMER
          || sepaMandatIdSource == SepaMandatIdSource.DBID)
      {
        m.setMandatID((String) getMandatID().getValue());
      }
      m.store();
      // Änderungsdatum nur speichern wenn wirklich geändert wurde
      // Wenn der insert oder update Check schief geht nicht speichern
      m.setLetzteAenderung();
      m.store();

      boolean ist_mitglied = m.getMitgliedstyp().getID()
          .equals(Mitgliedstyp.MITGLIED);
      if ((Boolean) Einstellungen.getEinstellung(Property.MITGLIEDFOTO)
          && ist_mitglied)
      {
        Mitgliedfoto f = null;
        DBIterator<Mitgliedfoto> it = Einstellungen.getDBService()
            .createList(Mitgliedfoto.class);
        it.addFilter("mitglied = ?", new Object[] { m.getID() });
        if (it.size() > 0)
        {
          f = it.next();
          if (foto == null)
          {
            f.delete();
          }
          else
          {
            f.setFoto((byte[]) foto.getValue());
            f.store();
          }
        }
        else
        {
          f = (Mitgliedfoto) Einstellungen.getDBService()
              .createObject(Mitgliedfoto.class, null);
          f.setMitglied(m);
          f.setFoto((byte[]) foto.getValue());
          f.store();
        }
      }
      if (eigenschaftenTree != null)
      {
        ArrayList<?> rootNodes = (ArrayList<?>) eigenschaftenTree.getItems(); // liefert
                                                                              // nur
                                                                              // den
                                                                              // Root
        EigenschaftenNode root = (EigenschaftenNode) rootNodes.get(0);
        if (!getMitglied().isNewObject())
        {
          DBIterator<Eigenschaften> it = Einstellungen.getDBService()
              .createList(Eigenschaften.class);
          it.addFilter("mitglied = ?", new Object[] { getMitglied().getID() });
          while (it.hasNext())
          {
            Eigenschaften ei = it.next();
            ei.delete();
          }
        }
        for (EigenschaftenNode checkedNode : root.getCheckedNodes())
        {
          Eigenschaften eig = (Eigenschaften) Einstellungen.getDBService()
              .createObject(Eigenschaften.class, null);
          eig.setEigenschaft(checkedNode.getEigenschaft().getID());
          eig.setMitglied(getMitglied().getID());
          eig.store();
        }
        eigenschaftenHash = createEigenschaftenHash();
      }

      if (zusatzfelder != null)
      {
        for (Input ti : zusatzfelder)
        {
          // Felddefinition ermitteln
          DBIterator<Felddefinition> it0 = Einstellungen.getDBService()
              .createList(Felddefinition.class);
          it0.addFilter("label = ?", new Object[] { ti.getName() });
          Felddefinition fd = it0.next();
          // Ist bereits ein Datensatz für diese Definiton vorhanden ?
          DBIterator<Zusatzfelder> it = Einstellungen.getDBService()
              .createList(Zusatzfelder.class);
          it.addFilter("mitglied =?", new Object[] { m.getID() });
          it.addFilter("felddefinition=?", new Object[] { fd.getID() });
          Zusatzfelder zf = null;
          if (it.size() > 0)
          {
            zf = it.next();
          }
          else
          {
            zf = (Zusatzfelder) Einstellungen.getDBService()
                .createObject(Zusatzfelder.class, null);
          }
          zf.setMitglied(Integer.valueOf(m.getID()));
          zf.setFelddefinition(Integer.valueOf(fd.getID()));
          switch (fd.getDatentyp())
          {
            case Datentyp.ZEICHENFOLGE:
              zf.setFeld((String) ti.getValue());
              break;
            case Datentyp.DATUM:
              zf.setFeldDatum((Date) ti.getValue());
              break;
            case Datentyp.GANZZAHL:
              if (ti.getValue() != null)
              {
                zf.setFeldGanzzahl((Integer) ti.getValue());
              }
              else
              {
                zf.setFeldGanzzahl(null);
              }
              break;
            case Datentyp.WAEHRUNG:
              if (ti.getValue() != null)
              {
                zf.setFeldWaehrung(BigDecimal.valueOf((Double) ti.getValue()));
              }
              else
              {
                zf.setFeldWaehrung(null);
              }
              break;
            case Datentyp.JANEIN:
              zf.setFeldJaNein((Boolean) ti.getValue());
              break;
            default:
              zf.setFeld((String) ti.getValue());
              break;
          }
          zf.store();
          // Den neuen Wert in "old" speichern
          ti.setData("old", ti.getValue());
        }
      }
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN) && ist_mitglied)
      {
        // Schritt 1: Die selektierten sekundären Beitragsgruppe prüfen, ob sie
        // bereits gespeichert sind. Ggfls. speichern.
        @SuppressWarnings("rawtypes")
        List items = sekundaerebeitragsgruppe.getItems();
        for (Object o1 : items)
        {
          SekundaereBeitragsgruppe sb = (SekundaereBeitragsgruppe) o1;
          if (sb.isNewObject())
          {
            sb.setMitglied(Integer.parseInt(m.getID()));
            sb.store();
          }
        }
        // Schritt 2: Die sekundären Beitragsgruppe in der Liste, die nicht mehr
        // selektiert sind, müssen gelöscht werden.
        for (SekundaereBeitragsgruppe sb : listeSeB)
        {
          if (!sb.isNewObject() && !items.contains(sb))
          {
            sb.delete();
          }
        }
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei Speichern des Mitgliedes";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  public JVereinTablePart getMitgliedBeitraegeTabelle() throws RemoteException
  {
    if (beitragsTabelle != null)
    {
      beitragsTabelle.setContextMenu(new MitgliedNextBGruppeMenue(this));
      return beitragsTabelle;
    }

    beitragsTabelle = new JVereinTablePart(
        new EditAction(MitgliedNextBGruppeView.class));
    beitragsTabelle.setContextMenu(new MitgliedNextBGruppeMenue(this));
    beitragsTabelle.addColumn("Ab Datum", MitgliedNextBGruppe.COL_AB_DATUM,
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    beitragsTabelle.addColumn("Beitragsgruppe",
        MitgliedNextBGruppe.VIEW_BEITRAGSGRUPPE);
    beitragsTabelle.addColumn("Bemerkung", MitgliedNextBGruppe.COL_BEMERKUNG);
    refreshMitgliedBeitraegeTabelle();
    return beitragsTabelle;
  }

  public void refreshMitgliedBeitraegeTabelle() throws RemoteException
  {
    if (beitragsTabelle == null)
      return;
    beitragsTabelle.removeAll();

    DBService service = Einstellungen.getDBService();
    DBIterator<MitgliedNextBGruppe> datenIterator = service
        .createList(MitgliedNextBGruppe.class);
    datenIterator.addFilter(MitgliedNextBGruppe.COL_MITGLIED + " = ? ",
        getMitglied().getID());
    datenIterator.setOrder("order by " + MitgliedNextBGruppe.COL_AB_DATUM);

    while (datenIterator.hasNext())
    {
      MitgliedNextBGruppe m = datenIterator.next();
      beitragsTabelle.addItem(m);
    }
    beitragsTabelle.sort();
  }

  @Override
  public void TabRefresh() throws ApplicationException
  {
    if (mitgliedList != null)
    {
      try
      {
        refreshMitgliedTable();
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
    }
  }

  @Override
  public boolean hasChanged() throws RemoteException
  {
    // Zusatzfelder testen
    if (zusatzfelder != null)
    {
      for (Input i : zusatzfelder)
      {
        if (i.getValue() != null && i.getData("old") != null
            && !i.getValue().equals(i.getData("old")))
        {
          return true;
        }
      }
    }

    // Eigenschaften testen
    if (!createEigenschaftenHash().equals(eigenschaftenHash))
    {
      return true;
    }

    // Sekundäre Beitragsgruppen testen
    Mitglied m = getMitglied();
    if ((Boolean) Einstellungen
        .getEinstellung(Property.SEKUNDAEREBEITRAGSGRUPPEN)
        && m.getMitgliedstyp().getID().equals(Mitgliedstyp.MITGLIED))
    {
      // Schritt 1: Die selektierten sekundären Beitragsgruppe prüfen, ob sie
      // bereits gespeichert sind. Ggfls. speichern.
      @SuppressWarnings("rawtypes")
      List items = sekundaerebeitragsgruppe.getItems();
      for (Object o1 : items)
      {
        SekundaereBeitragsgruppe sb = (SekundaereBeitragsgruppe) o1;
        if (sb.isNewObject())
        {
          return true;
        }
      }
      // Schritt 2: Die sekundären Beitragsgruppe in der Liste, die nicht mehr
      // selektiert sind, müssen gelöscht werden.
      for (SekundaereBeitragsgruppe sb : listeSeB)
      {
        if (!sb.isNewObject() && !items.contains(sb))
        {
          return true;
        }
      }
    }
    return false;
  }

  public LesefeldControl getLesefeldControl()
  {
    return lesefeldControl;
  }

  public void setDokumentControl(DokumentControl dcontrol)
  {
    this.dcontrol = dcontrol;
  }

  public PanelButton exportDetailButton(ExportArt art)
      throws ApplicationException
  {

    return new PanelButton(
        art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png", context -> {
          JVereinTablePart liste = null;
          try
          {
            liste = getDetailTablePart();
          }
          catch (ObjectNotFoundException ex)
          {
            throw new ApplicationException(
                "Es ist kein Tab mit einer Tabelle ausgewählt.");
          }
          if (liste == null)
          {
            throw new ApplicationException(
                "PDF Button kann nicht erstellt werden, Tabelle ist nicht geladen.");
          }

          switch (tabSelection)
          {
            case TAB_ZUSATZBETRAEGE:
              liste.export(
                  VorlageUtil.getName(VorlageTyp.MITGLIED_ZUSATZBETRAEGE_TITEL,
                      getMitglied()),
                  VorlageUtil.getName(
                      VorlageTyp.MITGLIED_ZUSATZBETRAEGE_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(
                      VorlageTyp.MITGLIED_ZUSATZBETRAEGE_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case TAB_WIEDERVORLAGEN:
              liste.export(
                  VorlageUtil.getName(VorlageTyp.MITGLIED_WIEDERVORLAGEN_TITEL,
                      getMitglied()),
                  VorlageUtil.getName(
                      VorlageTyp.MITGLIED_WIEDERVORLAGEN_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(
                      VorlageTyp.MITGLIED_WIEDERVORLAGEN_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case TAB_MAILS:
              liste.export(
                  VorlageUtil.getName(VorlageTyp.MITGLIED_MAILS_TITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_MAILS_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_MAILS_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case TAB_LEHRGAENGE:
              liste.export(
                  VorlageUtil.getName(VorlageTyp.MITGLIED_LEHRGAENGE_TITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_LEHRGAENGE_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_LEHRGAENGE_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case TAB_LESEFELDER:
              liste.export(
                  VorlageUtil.getName(VorlageTyp.MITGLIED_LESEFELDER_TITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_LESEFELDER_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_LESEFELDER_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case TAB_ARBEITSEINSAETZE:
              liste.export(VorlageUtil.getName(
                  VorlageTyp.MITGLIED_ARBEITSEINSAETZE_TITEL, getMitglied()),
                  VorlageUtil.getName(
                      VorlageTyp.MITGLIED_ARBEITSEINSAETZE_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(
                      VorlageTyp.MITGLIED_ARBEITSEINSAETZE_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case TAB_DOKUMENTE:
              liste.export(
                  VorlageUtil.getName(VorlageTyp.MITGLIED_DOKUMENTE_TITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_DOKUMENTE_SUBTITEL,
                      getMitglied()),
                  VorlageUtil.getName(VorlageTyp.MITGLIED_DOKUMENTE_DATEINAME,
                      getMitglied()),
                  art);
              break;
            case NO_LIST_TAB:
              break;
          }
        }, art.equals(ExportArt.PDF) ? "PDF" : "CSV");
  }

  public PanelButton getSpaltenDetailPanelButton()
  {
    return new PanelButton("document-properties.png", context -> {
      try
      {
        new TabelleSpaltenAuswahlDialog(zusatzbetraegeList, wiedervorlageList,
            mailList, lehrgaengeList, lesefeldControl.getLesefeldMitgliedList(),
            arbeitseinsatzList, getDocumentPart()).open();
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (ObjectNotFoundException e)
      {
        return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Fehler beim Spalten-Auswahl-Dialog");
      }
    }, "Spalten auswählen");
  }

  private JVereinTablePart getDetailTablePart()
      throws ApplicationException, ObjectNotFoundException
  {
    switch (tabSelection)
    {
      case TAB_ZUSATZBETRAEGE:
        return zusatzbetraegeList;
      case TAB_WIEDERVORLAGEN:
        return wiedervorlageList;
      case TAB_MAILS:
        return mailList;
      case TAB_LEHRGAENGE:
        return lehrgaengeList;
      case TAB_LESEFELDER:
        try
        {
          return lesefeldControl.getLesefeldMitgliedList();
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(e.getMessage());
        }
      case TAB_ARBEITSEINSAETZE:
        return arbeitseinsatzList;
      case TAB_DOKUMENTE:
        try
        {
          return dcontrol.getDokumenteList();
        }
        catch (RemoteException e)
        {
          throw new ApplicationException(e.getMessage());
        }
      case NO_LIST_TAB:
        throw new ObjectNotFoundException();
    }
    return null;
  }

  private JVereinTablePart getDocumentPart() throws RemoteException
  {
    if (dcontrol == null)
    {
      return null;
    }
    return dcontrol.getDokumenteList();
  }

  // Überschrieben, um ggf. "Mitglied" aus der Liste der Mitgliedsarten zu
  // entfernen
  @Override
  public Input getFilterInput(Filter filter)
      throws RemoteException, ApplicationException
  {
    Input input = super.getFilterInput(filter);
    if (filter.equals(Filter.MITGLIEDSTYP)
        && mitgliedAuswahl.equals(MitgliedAuswahl.NICHTMITGLIEDER))
    {
      List<?> list = ((SelectInput) input).getList();
      for (Object o : list)
      {
        if (((Mitgliedstyp) o).getJVereinid() == Integer
            .parseInt(Mitgliedstyp.MITGLIED))
        {
          list.remove(o);
          break;
        }
      }
    }
    return input;
  }

  public Button getExportButton()
  {
    @SuppressWarnings("unchecked")
    Button b = new Button("Export", context -> {
      try
      {
        saveFilterSettings();
        Mitgliedstyp mitgliedstyp;
        ExportDialog d;
        ArrayList<Mitglied> list = (ArrayList<Mitglied>) getTablePart()
            .getItems();
        if (mitgliedAuswahl == MitgliedAuswahl.MITGLIEDER)
        {
          mitgliedstyp = Einstellungen.getDBService()
              .createObject(Mitgliedstyp.class, Mitgliedstyp.MITGLIED);
        }
        else
        {
          mitgliedstyp = (Mitgliedstyp) getFilter().get(Filter.MITGLIEDSTYP);
        }
        Object[] objects = new Object[] { list, getFilterText(false),
            mitgliedstyp, getFilter() };
        /*
         * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
         * objects[2] ist Mitgliedstyp, objects[3] ist der Filter
         */
        if (mitgliedAuswahl == MitgliedAuswahl.MITGLIEDER)
        {
          d = new ExportDialog(objects, MitgliedListeView.class,
              DokumentationUtil.MITGLIEDSUCHE, this);
        }
        else
        {
          d = new ExportDialog(objects, NichtMitgliedListeView.class,
              DokumentationUtil.ADRESSEN, this);
        }
        d.open();
      }
      catch (OperationCanceledException oce)
      {
        Logger.info(oce.getMessage());
        return;
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("Fehler", e);
        GUI.getStatusBar().setErrorText("Fehler beim exportieren des Reports");
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    return b;
  }

  @Override
  protected String getTableTitle()
  {
    if (isMitglied)
    {
      return VorlageUtil.getName(VorlageTyp.MITGLIEDER_TITEL, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.NICHT_MITGLIEDER_TITEL, this);
    }
  }

  @Override
  protected String getTableSubtitle()
  {
    if (isMitglied)
    {
      return VorlageUtil.getName(VorlageTyp.MITGLIEDER_SUBTITEL, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.NICHT_MITGLIEDER_SUBTITEL, this);
    }
  }

  @Override
  protected String getTableDateiname()
  {
    if (isMitglied)
    {
      return VorlageUtil.getName(VorlageTyp.MITGLIEDER_DATEINAME, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.NICHT_MITGLIEDER_DATEINAME, this);
    }
  }

  public void setMitgliedAuswahl(MitgliedAuswahl mitgliedAuswahl)
  {
    this.mitgliedAuswahl = mitgliedAuswahl;
  }

  public MitgliedAuswahl getMitgliedAuswahl()
  {
    return mitgliedAuswahl;
  }
}
