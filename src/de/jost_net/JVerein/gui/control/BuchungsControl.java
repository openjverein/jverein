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

import java.io.File;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Messaging.SplitbuchungMessage;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Queries.BuchungQuery;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.BuchungSollbuchungZuordnungAutomatischAction;
import de.jost_net.JVerein.gui.dialogs.BuchungsjournalSortDialog;
import de.jost_net.JVerein.gui.dialogs.SammelueberweisungAuswahlDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.KontoauswahlInput;
import de.jost_net.JVerein.gui.input.SollbuchungAuswahlInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.gui.menu.BuchungMenu;
import de.jost_net.JVerein.gui.menu.SplitBuchungMenu;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.gui.parts.SplitbuchungListTablePart;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.gui.util.AfaUtil;
import de.jost_net.JVerein.io.BuchungAuswertungCSV;
import de.jost_net.JVerein.io.BuchungAuswertungPDF;
import de.jost_net.JVerein.io.BuchungsjournalPDF;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.AbstractInputAuswahl;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungsControl extends VorZurueckControl implements Savable
{

  private de.willuhn.jameica.system.Settings settings;

  private BuchungListTablePart buchungsList;

  /* Split-Buchnungen */
  private TablePart splitbuchungsList;

  /* Controls */
  private Input id;

  private Input umsatzid;

  private DialogInput konto;

  private IntegerInput auszugsnummer;

  private IntegerInput blattnummer;

  private Input name;

  private DecimalInput betrag;

  private TextAreaInput zweck;

  private DateInput datum = null;

  private Input art;

  private DialogInput sollbuchung;

  private TextAreaInput kommentar;

  // Definition für beide Auswahlvarianten (SelectInput und
  // BuchungsartSearchInput)
  private AbstractInput buchungsart;

  private SelectInput buchungsklasse;

  private SelectInput projekt;

  private DialogInput suchkonto;

  private SelectInput suchbuchungsart;

  private SelectInput suchprojekt;

  private SelectInput hasmitglied;

  private DateInput vondatum = null;

  private DateInput bisdatum = null;

  private TextInput suchtext = null;

  private TextInput suchbetrag = null;

  private TextInput mitglied = null;

  private SelectInput suchsteuer;

  private CheckboxInput verzicht;

  private Buchung buchung;

  private Button sammelueberweisungButton;

  private BuchungQuery query;

  private TextInput iban = null;

  public static final String BUCHUNGSART = "suchbuchungsart";

  public static final String PROJEKT = "suchprojekt";

  public static final String MITGLIEDZUGEORDNET = "suchmitgliedzugeordnet";

  public static final String SUCHSTEUER = "suchsteuer";

  private Vector<Listener> changeKontoListener = new Vector<>();

  protected String settingsprefix = "geldkonto.";

  private Kontenfilter kontenfilter = Kontenfilter.ALLE;

  private boolean geldkonto = true;

  private TreeMap<String, String> params;

  private boolean editable = false;

  private SplitbuchungMessageConsumer splitbuchungConsumer = null;

  public enum Kontenfilter
  {
    GELDKONTO, // Beinhaltet Rückstellungen
    ANLAGEKONTO,
    ALLE
  }

  public enum SplitFilter
  {
    ALLE(0, "Alle"),
    SPLIT(1, "Nur Splitbuchungen"),
    HAUPT(2, "Nur Hauptbuchungen");

    private String text;

    private int key;

    SplitFilter(int k, String t)
    {
      text = t;
      key = k;
    }

    public String getText()
    {
      return text;
    }

    public int getKey()
    {
      return key;
    }

    @Override
    public String toString()
    {
      return getText();
    }

    public static SplitFilter getByKey(int key)
    {
      for (SplitFilter split : SplitFilter.values())
      {
        if (split.getKey() == key)
        {
          return split;
        }
      }
      return null;
    }
  }

  private Calendar calendar = Calendar.getInstance();

  private SelectInput suchsplitbuchung;

  private CheckboxInput ungeprueft;

  private SelectInput steuer;

  private CheckboxInput geprueft;

  private enum RANGE
  {
    MONAT,
    TAG
  }

  public BuchungsControl(AbstractView view, Kontenfilter kontenfilter)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    this.kontenfilter = kontenfilter;
    if (kontenfilter == Kontenfilter.ANLAGEKONTO)
    {
      geldkonto = false;
      settingsprefix = "anlagenkonto.";
    }
  }

  public Buchung getBuchung() throws RemoteException
  {
    if (buchung != null)
    {
      return buchung;
    }
    buchung = (Buchung) getCurrentObject();
    if (buchung == null)
    {
      buchung = (Buchung) Einstellungen.getDBService()
          .createObject(Buchung.class, null);
    }
    return buchung;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Buchung b = getBuchung();
    b.setBuchungsartId(getSelectedBuchungsArtId());
    b.setBuchungsklasseId(getSelectedBuchungsKlasseId());
    b.setProjektID(getSelectedProjektId());
    b.setKonto(getSelectedKonto());
    b.setAuszugsnummer(getAuszugsnummerWert());
    b.setBlattnummer(getBlattnummerWert());
    b.setName((String) getName().getValue());
    String ib = (String) getIban().getValue();
    if (ib == null)
      b.setIban(null);
    else
      b.setIban(ib.toUpperCase().replace(" ", ""));
    if (getBetrag().getValue() != null)
    {
      b.setBetrag((Double) getBetrag().getValue());
    }
    else
    {
      // Nötig um für den Check den letzten gesetzten Wert zu löschen
      b.setBetragNull();
    }
    b.setZweck((String) getZweck().getValue());
    b.setDatum((Date) getDatum().getValue());
    b.setArt((String) getArt().getValue());
    b.setVerzicht((Boolean) getVerzicht().getValue());
    b.setKommentar((String) getKommentar().getValue());
    b.setSollbuchung((Sollbuchung) getSollbuchung().getValue());
    b.setGeprueft((Boolean) getGeprueft().getValue());
    if (getSteuer() != null)
    {
      b.setSteuer((Steuer) getSteuer().getValue());
    }
    return b;
  }

  public Input getID() throws RemoteException
  {
    if (id != null)
    {
      return id;
    }
    id = new TextInput(getBuchung().getID(), 10);
    id.setEnabled(false);
    return id;
  }

  public Input getUmsatzid() throws RemoteException
  {
    if (umsatzid != null)
    {
      return umsatzid;
    }
    Integer ui = getBuchung().getUmsatzid();
    if (ui == null)
    {
      ui = Integer.valueOf(0);
    }
    umsatzid = new IntegerInput(ui);
    umsatzid.setEnabled(false);
    return umsatzid;
  }

  public DialogInput getKonto(boolean withFocus) throws RemoteException
  {
    if (konto != null)
    {
      return konto;
    }
    String kontoid = getVorauswahlKontoId();
    konto = new KontoauswahlInput(getBuchung().getKonto())
        .getKontoAuswahl(false, kontoid, false, true, kontenfilter);
    if (withFocus)
    {
      konto.focus();
    }
    konto.setMandatory(true);
    konto.setEnabled(editable);
    return konto;
  }

  private String getVorauswahlKontoId() throws RemoteException
  {
    Buchung buchung = getBuchung();
    if (null != buchung)
    {
      Konto konto = buchung.getKonto();
      if (null != konto)
        return konto.getID();
    }
    return settings.getString(settingsprefix + "kontoid", "");
  }

  public Input getAuszugsnummer()
  {
    if (auszugsnummer != null)
    {
      return auszugsnummer;
    }
    Integer intAuszugsnummer;
    try
    {
      intAuszugsnummer = getBuchung().getAuszugsnummer();
    }
    catch (RemoteException e)
    {
      intAuszugsnummer = null;
    }
    auszugsnummer = new IntegerInput(
        intAuszugsnummer != null ? intAuszugsnummer : -1);
    auszugsnummer.setEnabled(editable);
    return auszugsnummer;
  }

  public Input getBlattnummer()
  {
    if (blattnummer != null)
    {
      return blattnummer;
    }
    Integer intBlattnummer;
    try
    {
      intBlattnummer = getBuchung().getBlattnummer();
    }
    catch (RemoteException e)
    {
      intBlattnummer = null;
    }
    blattnummer = new IntegerInput(
        intBlattnummer != null ? intBlattnummer : -1);
    blattnummer.setEnabled(editable);
    return blattnummer;
  }

  public Input getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getBuchung().getName(), 100);
    name.setEnabled(editable);
    return name;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }

    if (getBuchung().isNewObject() && getBuchung().isBetragNull())
    {
      betrag = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    else
    {
      betrag = new DecimalInput(getBuchung().getBetrag(),
          Einstellungen.DECIMALFORMAT);
    }
    betrag.setMandatory(true);
    betrag.setEnabled(editable);
    return betrag;
  }

  public Input getZweck() throws RemoteException
  {
    if (zweck != null)
    {
      return zweck;
    }
    zweck = new TextAreaInput(getBuchung().getZweck(), 500);
    zweck.setHeight(50);
    zweck.setEnabled(editable);
    return zweck;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    Date d = getBuchung().getDatum();
    this.datum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.datum.setTitle("Datum");
    this.datum.setText("Bitte Datum wählen");
    datum.setMandatory(true);
    datum.setEnabled(editable);
    return datum;
  }

  public TextInput getSuchtext()
  {
    if (suchtext != null)
    {
      return suchtext;
    }
    suchtext = new TextInput(
        settings.getString(settingsprefix + "suchtext", ""), 35);
    return suchtext;
  }

  public boolean isSuchtextAktiv()
  {
    return suchtext != null;
  }

  public TextInput getSuchBetrag() throws RemoteException
  {
    if (suchbetrag != null)
    {
      return suchbetrag;
    }
    suchbetrag = new TextInput(
        settings.getString(settingsprefix + "suchbetrag", ""));
    return suchbetrag;
  }

  public boolean isSuchBetragAktiv()
  {
    return suchbetrag != null;
  }

  public TextInput getMitglied()
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    mitglied = new TextInput(
        settings.getString(settingsprefix + "mitglied", ""), 35);
    return mitglied;
  }

  public boolean isMitgliedAktiv()
  {
    return mitglied != null;
  }

  public SelectInput getSuchSteuer() throws RemoteException
  {
    if (suchsteuer != null)
    {
      return suchsteuer;
    }
    ArrayList<Steuer> steuerliste = new ArrayList<>();
    Steuer s1 = (Steuer) Einstellungen.getDBService().createObject(Steuer.class,
        null);
    s1.setName("Ohne Steuer");
    steuerliste.add(s1);

    DBIterator<Steuer> it = Einstellungen.getDBService()
        .createList(Steuer.class);
    it.setOrder("order by name");
    while (it.hasNext())
    {
      steuerliste.add(it.next());
    }
    int swert = settings.getInt(settingsprefix + SUCHSTEUER, -2);
    Steuer letztesuche = null;
    if (swert == 0)
    {
      letztesuche = steuerliste.get(0);
    }
    else
    {
      try
      {
        letztesuche = (Steuer) Einstellungen.getDBService()
            .createObject(Steuer.class, String.valueOf(swert));
      }
      catch (ObjectNotFoundException e)
      {
        //
      }
    }
    suchsteuer = new SelectInput(steuerliste, letztesuche);
    suchsteuer.setAttribute("name");
    suchsteuer.setPleaseChoose("Alle");
    suchsteuer.addListener(new FilterListener());
    return suchsteuer;
  }

  public boolean isSuchSteuerAktiv()
  {
    return suchsteuer != null;
  }

  public CheckboxInput getVerzicht() throws RemoteException
  {
    if (verzicht != null)
    {
      return verzicht;
    }

    Boolean vz = buchung.getVerzicht();
    if (vz == null)
    {
      vz = Boolean.FALSE;
    }
    verzicht = new CheckboxInput(vz);
    verzicht.setEnabled(editable);
    return verzicht;
  }

  public DialogInput getSollbuchung() throws RemoteException
  {
    if (sollbuchung != null)
    {
      return sollbuchung;
    }
    sollbuchung = new SollbuchungAuswahlInput(getBuchung())
        .getSollbuchungAuswahl();
    sollbuchung.addListener(event -> {
      try
      {
        String name = (String) getName().getValue();
        String zweck1 = (String) getZweck().getValue();
        if (sollbuchung.getValue() != null && name.length() == 0
            && zweck1.length() == 0)
        {
          if (sollbuchung.getValue() instanceof Sollbuchung)
          {
            Sollbuchung sb = (Sollbuchung) sollbuchung.getValue();
            getName()
                .setValue(Adressaufbereitung.getNameVorname(sb.getMitglied()));
            getBetrag().setValue(sb.getBetrag());
            getZweck().setValue(sb.getZweck1());
            getDatum().setValue(sb.getDatum());
          }
          if (sollbuchung.getValue() instanceof Mitglied)
          {
            Mitglied m2 = (Mitglied) sollbuchung.getValue();
            getName().setValue(Adressaufbereitung.getNameVorname(m2));
            getDatum().setValue(new Date());
          }
        }
        if (sollbuchung.getValue() instanceof Sollbuchung)
        {
          Sollbuchung sb = (Sollbuchung) sollbuchung.getValue();
          ArrayList<SollbuchungPosition> sbpList = sb
              .getSollbuchungPositionList();
          if (getBuchungsart().getValue() == null && sbpList.size() > 0)
          {
            getBuchungsart().setValue(sbpList.get(0).getBuchungsart());
          }
          if (isBuchungsklasseActive() && getBuchungsklasse().getValue() == null
              && sbpList.size() > 0)
          {
            getBuchungsklasse().setValue(sbpList.get(0).getBuchungsklasse());
          }
          if (getSteuer() != null && getSteuer().getValue() == null
              && sbpList.size() > 0)
          {
            getSteuer().setValue(sbpList.get(0).getSteuer());
          }
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
    });
    sollbuchung.setEnabled(editable);
    return sollbuchung;
  }

  public Input getArt() throws RemoteException
  {
    if (art != null && !art.getControl().isDisposed())
    {
      return art;
    }
    art = new TextInput(getBuchung().getArt(), 100);
    art.setEnabled(editable);
    return art;
  }

  public Input getKommentar() throws RemoteException
  {
    if (kommentar != null && !kommentar.getControl().isDisposed())
    {
      return kommentar;
    }
    kommentar = new TextAreaInput(getBuchung().getKommentar(), 1024);
    kommentar.setHeight(50);
    kommentar.setEnabled(editable);
    return kommentar;
  }

  public CheckboxInput getGeprueft() throws RemoteException
  {
    if (geprueft != null && !geprueft.getControl().isDisposed())
    {
      return geprueft;
    }
    geprueft = new CheckboxInput(getBuchung().getGeprueft());
    geprueft.setEnabled(editable);
    return geprueft;
  }

  public Input getBuchungsart() throws RemoteException
  {
    if (buchungsart != null && !buchungsart.getControl().isDisposed())
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(buchungsart,
        getBuchung().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    if (!getBuchung().getSpeicherung())
    {
      buchungsart.setMandatory(true);
    }
    buchungsart.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          Buchungsart bua = (Buchungsart) buchungsart.getValue();
          if (buchungsklasse != null && buchungsklasse.getValue() == null
              && bua != null)
            buchungsklasse.setValue(bua.getBuchungsklasse());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    buchungsart.addListener(e -> {
      if (steuer != null && buchungsart.getValue() != null)
      {
        try
        {
          steuer.setValue(((Buchungsart) buchungsart.getValue()).getSteuer());
        }
        catch (RemoteException e1)
        {
          Logger.error("Fehler", e1);
        }
      }
    });
    buchungsart.setEnabled(editable);
    return buchungsart;
  }

  public Input getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null && !buchungsklasse.getControl().isDisposed())
    {
      return buchungsklasse;
    }
    buchungsklasse = new BuchungsklasseInput().getBuchungsklasseInput(
        buchungsklasse, getBuchung().getBuchungsklasse());
    if (!getBuchung().getSpeicherung() && (Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      buchungsklasse.setMandatory(true);
    }
    buchungsklasse.setEnabled(editable);
    return buchungsklasse;
  }

  public boolean isBuchungsklasseActive()
  {
    return buchungsklasse != null;
  }

  public Input getProjekt() throws RemoteException
  {
    if (projekt != null && !projekt.getControl().isDisposed())
    {
      return projekt;
    }
    DBIterator<Projekt> list = Einstellungen.getDBService()
        .createList(Projekt.class);
    Date buchungsDatum = getBuchung().getDatum() == null ? new Date()
        : getBuchung().getDatum();
    list.addFilter(
        "((startdatum is null or startdatum <= ?) and (endedatum is null or endedatum >= ?))",
        new Object[] { buchungsDatum, buchungsDatum });
    list.setOrder("ORDER BY bezeichnung");
    projekt = new SelectInput(list != null ? PseudoIterator.asList(list) : null,
        getBuchung().getProjekt());
    projekt.setValue(getBuchung().getProjekt());
    projekt.setAttribute("bezeichnung");
    projekt.setPleaseChoose("Bitte auswählen");
    projekt.setEnabled(editable);
    return projekt;
  }

  public DialogInput getSuchKonto() throws RemoteException
  {
    if (suchkonto != null)
    {
      return suchkonto;
    }
    String kontoid = settings.getString(settingsprefix + "suchkontoid", "");
    suchkonto = new KontoauswahlInput().getKontoAuswahl(true, kontoid, false,
        true, kontenfilter);
    suchkonto.addListener(new FilterListener());
    return suchkonto;
  }

  public boolean isSuchKontoAktiv()
  {
    return suchkonto != null;
  }

  public Button getSammelueberweisungButton()
  {
    sammelueberweisungButton = new Button("Sammelüberweisung", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        Buchung master = (Buchung) getCurrentObject();
        SammelueberweisungAuswahlDialog suad = new SammelueberweisungAuswahlDialog(
            master);
        try
        {
          SepaSammelUeberweisung su = suad.open();
          if (su != null)
          {
            for (SepaSammelUeberweisungBuchung ssub : su.getBuchungen())
            {
              Buchung b = (Buchung) Einstellungen.getDBService()
                  .createObject(Buchung.class, null);
              b.setAuszugsnummer(master.getAuszugsnummer());
              b.setBetrag(ssub.getBetrag() * -1);
              b.setBlattnummer(master.getBlattnummer());
              b.setBuchungsartId(master.getBuchungsartId());
              b.setBuchungsklasseId(master.getBuchungsklasseId());
              b.setDatum(su.getAusfuehrungsdatum());
              b.setKonto(master.getKonto());
              b.setName(ssub.getGegenkontoName());
              b.setSpeicherung(true);
              b.setSplitId(master.getSplitId());
              b.setSplitTyp(SplitbuchungTyp.SPLIT);
              b.setZweck(ssub.getZweck());
              SplitbuchungsContainer.add(b);
            }
            refreshSplitbuchungen();
          }
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }

    }, null, false, "list.png");
    return sammelueberweisungButton;
  }

  public Input getSuchProjekt() throws RemoteException
  {
    if (suchprojekt != null)
    {
      return suchprojekt;
    }
    ArrayList<Projekt> projektliste = new ArrayList<>();
    Projekt p1 = (Projekt) Einstellungen.getDBService()
        .createObject(Projekt.class, null);
    p1.setBezeichnung("Ohne Projekt");
    projektliste.add(p1);

    DBIterator<Projekt> list = Einstellungen.getDBService()
        .createList(Projekt.class);
    list.setOrder("ORDER BY bezeichnung");
    while (list.hasNext())
    {
      projektliste.add(list.next());
    }

    int pwert = settings.getInt(settingsprefix + PROJEKT, -2);
    Projekt p = null;
    if (pwert == 0)
    {
      p = projektliste.get(0);
    }
    else
    {
      int size = projektliste.size();
      for (int i = 1; i < size; i++)
      {
        if (projektliste.get(i).getID().equalsIgnoreCase(String.valueOf(pwert)))
        {
          p = projektliste.get(i);
          break;
        }
      }
    }

    suchprojekt = new SelectInput(projektliste, p);
    suchprojekt.addListener(new FilterListener());
    suchprojekt.setAttribute("bezeichnung");
    suchprojekt.setPleaseChoose("Keine Einschränkung");
    return suchprojekt;
  }

  public boolean isSuchProjektAktiv()
  {
    return suchprojekt != null;
  }

  public SelectInput getSuchSplibuchung()
  {
    if (suchsplitbuchung != null)
    {
      return suchsplitbuchung;
    }
    int split = settings.getInt(settingsprefix + "split",
        SplitFilter.ALLE.getKey());
    suchsplitbuchung = new SelectInput(SplitFilter.values(),
        SplitFilter.getByKey(split));
    suchsplitbuchung.addListener(new FilterListener());

    return suchsplitbuchung;
  }

  public boolean isSuchSplibuchungAktiv()
  {
    return suchsplitbuchung != null;
  }

  public SelectInput getSuchBuchungsart() throws RemoteException
  {
    if (suchbuchungsart != null)
    {
      return suchbuchungsart;
    }

    suchbuchungsart = (SelectInput) new BuchungsartInput().getBuchungsartInput(
        suchbuchungsart, null, buchungsarttyp.BUCHUNGSART,
        AbstractInputAuswahl.ComboBox);

    @SuppressWarnings("unchecked")
    List<Buchungsart> suchliste = (List<Buchungsart>) suchbuchungsart.getList();
    ArrayList<Buchungsart> liste = new ArrayList<>();
    Buchungsart b2 = (Buchungsart) Einstellungen.getDBService()
        .createObject(Buchungsart.class, null);
    b2.setNummer(-1);
    b2.setBezeichnung("Ohne Buchungsart");
    b2.setArt(-1);
    liste.add(b2);
    for (Buchungsart ba : suchliste)
      liste.add(ba);

    int bwert = settings.getInt(settingsprefix + BUCHUNGSART, -2);
    Buchungsart b = null;
    int size = liste.size();
    for (int i = 0; i < size; i++)
    {
      if (liste.get(i).getNummer() == bwert)
      {
        b = liste.get(i);
        break;
      }
    }
    suchbuchungsart.setList(liste);
    suchbuchungsart.setValue(b);
    suchbuchungsart.addListener(new FilterListener());
    if (suchbuchungsart instanceof SelectInput)
    {
      suchbuchungsart.setPleaseChoose(FilterControl.ALLE);
    }
    return suchbuchungsart;
  }

  public boolean isSuchBuchungsartAktiv()
  {
    return suchbuchungsart != null;
  }

  public SelectInput getSteuer() throws RemoteException
  {
    if (steuer != null)
    {
      return steuer;
    }

    steuer = new SteuerInput(getBuchung().getSteuer());
    steuer.setAttribute("name");
    steuer.setPleaseChoose("Keine Steuer");
    steuer.setEnabled(editable);
    return steuer;
  }

  public DateInput getVondatum()
  {
    if (vondatum != null)
    {
      return vondatum;
    }
    Date d = null;
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString(settingsprefix + "vondatum", "01.01.2006"));
    }
    catch (ParseException e)
    {
      //
    }
    this.vondatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.vondatum.setTitle("Anfangsdatum");
    this.vondatum.setText("Bitte Anfangsdatum wählen");
    this.vondatum.setMandatory(true);
    return vondatum;
  }

  public boolean isVondatumAktiv()
  {
    return vondatum != null;
  }

  public DateInput getBisdatum()
  {
    if (bisdatum != null)
    {
      return bisdatum;
    }
    Date d = null;
    try
    {
      d = new JVDateFormatTTMMJJJJ()
          .parse(settings.getString(settingsprefix + "bisdatum", "31.12.2006"));
    }
    catch (ParseException e)
    {
      //
    }
    this.bisdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.bisdatum.setTitle("Anfangsdatum");
    this.bisdatum.setText("Bitte Anfangsdatum wählen");
    this.bisdatum.setMandatory(true);
    return bisdatum;
  }

  public boolean isBisdatumAktiv()
  {
    return bisdatum != null;
  }

  public CheckboxInput getUngeprueft()
  {
    if (ungeprueft != null)
    {
      return ungeprueft;
    }
    ungeprueft = new CheckboxInput(
        settings.getBoolean(settingsprefix + "ungeprueft", false));
    ungeprueft.addListener(new FilterListener());
    return ungeprueft;
  }

  public boolean isUngeprueftAktiv()
  {
    return ungeprueft != null;
  }

  public Button getStartAuswertungEinzelbuchungenButton()
  {
    Button b = new Button("PDF Einzelbuchungen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        starteAuswertung(true);
      }
    }, null, false, "file-pdf.png");
    return b;
  }

  public Button getStartCSVAuswertungButton()
  {
    Button b = new Button("CSV", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        starteCSVExport();
      }
    }, null, false, "xsd.png");
    return b;
  }

  public Button getStartAuswertungSummenButton()
  {
    Button b = new Button("PDF Summen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        starteAuswertung(false);
      }
    }, null, false, "file-pdf.png");
    return b;
  }

  public Button getStarteBuchungSollbuchungZuordnungAutomatischButton()
  {
    Button b = new Button("Zuordnung",
        new BuchungSollbuchungZuordnungAutomatischAction(getVondatum(),
            getBisdatum()),
        null, false, "user-friends.png");
    return b;
  }

  public Button getStartAuswertungBuchungsjournalButton()
  {
    Button b = new Button("PDF Buchungsjournal", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        starteAuswertungBuchungsjournal();
      }
    }, null, false, "file-pdf.png");
    return b;
  }

  public Button getAfaButton()
  {
    Button b = new Button("Erzeuge Abschreibungen", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          new AfaUtil(new Geschaeftsjahr(new Date()), null);
          refreshBuchungsList();
        }
        catch (RemoteException e)
        {
          GUI.getStatusBar()
              .setErrorText("Fehler bei der Erstellung der Abschreibungen");
        }
        catch (ParseException ex)
        {
          GUI.getStatusBar().setErrorText(ex.getLocalizedMessage());
        }
      }
    }, null, false, "document-new.png");
    return b;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Buchung b = (Buchung) prepareStore();

      if (b.getSpeicherung())
      {
        b.store();
        getID().setValue(b.getID());
        GUI.getStatusBar().setSuccessText("Buchung gespeichert");
      }
      else
      {
        b.plausi();
        SplitbuchungsContainer.add(b);
        GUI.getStatusBar().setSuccessText("Buchung übernommen");
      }
    }
    catch (RemoteException ex)
    {
      final String meldung = "Fehler beim Speichern der Buchung.";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  private Integer getBlattnummerWert() throws ApplicationException
  {
    Integer intBlatt = (Integer) getBlattnummer().getValue();
    if (intBlatt != null && intBlatt <= 0)
    {
      final String meldung = "Blattnummer kann nicht gespeichert werden. Muss leer oder eine positive Zahl sein.";
      Logger.error(meldung);
      throw new ApplicationException(meldung);
    }
    return intBlatt;
  }

  private Integer getAuszugsnummerWert() throws ApplicationException
  {
    Integer intAuszugsnummer = (Integer) auszugsnummer.getValue();
    if (intAuszugsnummer != null && intAuszugsnummer <= 0)
    {
      final String meldung = "Auszugsnummer kann nicht gespeichert werden. Muss leer oder eine positive Zahl sein.";
      Logger.error(meldung);
      throw new ApplicationException(meldung);
    }
    return intAuszugsnummer;
  }

  private Konto getSelectedKonto() throws ApplicationException
  {
    try
    {
      Konto konto = (Konto) getKonto(false).getValue();
      if (konto == null)
      {
        throw new ApplicationException(
            "Kein Konto Ausgewählt. Ggfs. erst unter Buchführung->Konten ein Konto anlegen.");
      }
      settings.setAttribute(settingsprefix + "kontoid", konto.getID());
      return konto;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Konto der Buchung kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  private Long getSelectedProjektId() throws ApplicationException
  {
    try
    {
      if (!(Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        return null;
      }
      Projekt projekt = (Projekt) getProjekt().getValue();
      if (null == projekt)
        return null;
      Long id = Long.valueOf(projekt.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewähltes Projekt kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  private Long getSelectedBuchungsArtId() throws ApplicationException
  {
    try
    {
      Buchungsart buchungsArt = (Buchungsart) getBuchungsart().getValue();
      if (null == buchungsArt)
        return null;
      Long id = Long.valueOf(buchungsArt.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsart kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  private Long getSelectedBuchungsKlasseId() throws ApplicationException
  {
    try
    {
      if (null == buchungsklasse)
        return null;
      Buchungsklasse buchungsKlasse = (Buchungsklasse) getBuchungsklasse()
          .getValue();
      if (null == buchungsKlasse)
        return null;
      Long id = Long.valueOf(buchungsKlasse.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsklasse kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public BuchungListTablePart getBuchungsList() throws RemoteException
  {
    params = new TreeMap<>();

    // Werte speichern und Parameter füllen
    Date dv = null;
    if (isVondatumAktiv())
    {
      dv = (Date) getVondatum().getValue();
    }
    if (dv == null)
    {
      throw new RemoteException("Bitte Von Datum eingeben!");
    }
    settings.setAttribute(settingsprefix + "vondatum",
        new JVDateFormatTTMMJJJJ().format(dv));

    Date db = null;
    if (isBisdatumAktiv())
    {
      db = (Date) getBisdatum().getValue();
    }
    if (db == null)
    {
      throw new RemoteException("Bitte Bis Datum eingeben!");
    }
    settings.setAttribute(settingsprefix + "bisdatum",
        new JVDateFormatTTMMJJJJ().format(db));

    Konto k = null;
    if (isSuchKontoAktiv())
    {
      if (getSuchKonto().getValue() != null)
      {
        k = (Konto) getSuchKonto().getValue();
        settings.setAttribute(settingsprefix + "suchkontoid", k.getID());
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchkontoid", "");
      }
    }
    Boolean mvalue = null;
    if (isSuchMitgliedZugeordnetAktiv()
        && getSuchMitgliedZugeordnet().getValue() != null)
    {
      MitgliedZustand m = (MitgliedZustand) getSuchMitgliedZugeordnet()
          .getValue();
      mvalue = m.getValue();
      settings.setAttribute(settingsprefix + MITGLIEDZUGEORDNET, m.getText());
      if (!m.getText().equalsIgnoreCase("Beide"))
      {
        params.put("Mitglied zugeordnet? ", m.getText());
      }
    }
    Buchungsart b = null;
    if (isSuchBuchungsartAktiv())
    {
      b = (Buchungsart) getSuchBuchungsart().getValue();
    }
    if (b != null && b.getNummer() != 0)
    {
      settings.setAttribute(settingsprefix + BuchungsControl.BUCHUNGSART,
          b.getNummer());
      params.put("Buchungsart ", b.getBezeichnung());
    }
    else
    {
      settings.setAttribute(settingsprefix + BuchungsControl.BUCHUNGSART, -2);
    }
    Projekt p = null;
    if (isSuchProjektAktiv())
    {
      if (getSuchProjekt().getValue() != null)
      {
        p = (Projekt) getSuchProjekt().getValue();
        if (p.isNewObject())
        {
          settings.setAttribute(settingsprefix + BuchungsControl.PROJEKT, 0);
        }
        else
        {
          settings.setAttribute(settingsprefix + BuchungsControl.PROJEKT,
              p.getID());
        }
      }
      else
      {
        settings.setAttribute(settingsprefix + BuchungsControl.PROJEKT, -2);
      }
    }
    Boolean ungeprueft = null;
    if (isUngeprueftAktiv())
    {
      ungeprueft = (Boolean) getUngeprueft().getValue();
      settings.setAttribute(settingsprefix + "ungeprueft", ungeprueft);
      if (ungeprueft)
      {
        params.put("Nur ungeprüfte ", ungeprueft.toString());
      }
    }
    String suchtext = null;
    if (isSuchtextAktiv())
    {
      suchtext = (String) getSuchtext().getValue();
      settings.setAttribute(settingsprefix + "suchtext", suchtext);
      if (suchtext != null && !suchtext.isEmpty())
      {
        params.put("Enthaltener Text ", suchtext);
      }
    }
    String suchbetrag = null;
    if (isSuchBetragAktiv())
    {
      suchbetrag = (String) getSuchBetrag().getValue();
      settings.setAttribute(settingsprefix + "suchbetrag", suchbetrag);
      if (suchbetrag != null && !suchbetrag.isEmpty())
      {
        params.put("Betrag ", suchbetrag);
      }
    }
    String mitglied = null;
    if (isMitgliedAktiv())
    {
      mitglied = (String) getMitglied().getValue();
      settings.setAttribute(settingsprefix + "mitglied", mitglied);
      if (mitglied != null && !mitglied.isEmpty())
      {
        params.put("Mitglied Name ", mitglied);
      }
    }
    SplitFilter split = null;
    if (isSuchSplibuchungAktiv())
    {
      split = (SplitFilter) getSuchSplibuchung().getValue();
      settings.setAttribute(settingsprefix + "split", (int) split.getKey());
      if (split != SplitFilter.ALLE)
      {
        params.put("Splitbuchung ", split.getText());
      }
    }

    Steuer steuer = null;
    if (isSuchSteuerAktiv())
    {
      if (getSuchSteuer().getValue() != null)
      {
        steuer = (Steuer) getSuchSteuer().getValue();
        if (steuer.isNewObject())
        {
          settings.setAttribute(settingsprefix + SUCHSTEUER, 0);
        }
        else
        {
          settings.setAttribute(settingsprefix + SUCHSTEUER, steuer.getID());
        }
        params.put("Steuer ", steuer.getName());
      }
      else
      {
        settings.setAttribute(settingsprefix + SUCHSTEUER, -2);
      }
    }

    query = new BuchungQuery(dv, db, k, b, p, suchtext, suchbetrag, mvalue,
        mitglied, geldkonto, split, ungeprueft, steuer);

    List<Buchung> buchungen = query.get();

    if (buchungsList == null)
    {
      buchungsList = new BuchungListTablePart(buchungen,
          new BuchungAction(false, null));
      buchungsList.addColumn("Nr", "id-int");
      buchungsList.addColumn("Geprüft", "geprueft", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          return (Boolean) o ? "\u2705" : "";
        }
      });
      if ((Boolean) Einstellungen
          .getEinstellung(Property.DOKUMENTENSPEICHERUNG))
      {
        buchungsList.addColumn("D", "document");
      }
      buchungsList.addColumn("S", "splittyp", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          Integer typ = (Integer) o;
          return SplitbuchungTyp.get(typ).substring(0, 1);
        }
      });

      buchungsList.addColumn("Konto", "konto");
      buchungsList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));

      if (geldkonto)
      {
        buchungsList.addColumn("Auszugsnummer", "auszugsnummer");
        buchungsList.addColumn("Blatt", "blattnummer");
      }

      buchungsList.addColumn("Name", "name");
      if (geldkonto)
        buchungsList.addColumn("IBAN oder Kontonummer", "iban",
            new IbanFormatter());
      buchungsList.addColumn("Verwendungszweck", "zweck", new Formatter()
      {
        @Override
        public String format(Object value)
        {
          if (value == null)
          {
            return null;
          }
          String s = value.toString();
          s = s.replaceAll("\r\n", " ");
          s = s.replaceAll("\r", " ");
          s = s.replaceAll("\n", " ");
          return s;
        }
      });
      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        buchungsList.addColumn("Buchungsklasse", "buchungsklasse",
            new BuchungsklasseFormatter());
      }

      buchungsList.addColumn("Buchungsart", "buchungsart",
          new BuchungsartFormatter());
      buchungsList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT) && geldkonto)
      {
        buchungsList.addColumn("Netto", "netto",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
        {
          buchungsList.addColumn("Steuer", "steuer", o -> {
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
      }
      if (geldkonto)
        buchungsList.addColumn(new Column(Buchung.SOLLBUCHUNG, "Mitglied",
            new SollbuchungFormatter(), false, Column.ALIGN_AUTO,
            Column.SORT_BY_DISPLAY));
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        buchungsList.addColumn("Projekt", "projekt");
      }
      buchungsList.addColumn("Abrechnungslauf", "abrechnungslauf");
      buchungsList.setMulti(true);
      buchungsList.setContextMenu(new BuchungMenu(this));
      buchungsList.setRememberColWidths(true);
      buchungsList.setRememberOrder(true);
      buchungsList.setRememberState(true);
      buchungsList.addFeature(new FeatureSummary());
      buchungsList.updateSaldo((Konto) getSuchKonto().getValue());
      buchungsList.setAction(new BuchungAction(false, buchungsList));
      VorZurueckControl.setObjektListe(null, null);
    }
    else
    {
      buchungsList.updateSaldo((Konto) getSuchKonto().getValue());

      buchungsList.removeAll();

      for (Buchung bu : buchungen)
      {
        buchungsList.addItem(bu);
      }

      // Summenzeile neu laden
      buchungsList.featureEvent(
          de.willuhn.jameica.gui.parts.table.Feature.Event.REFRESH, null);
      buchungsList.sort();
    }
    informKontoChangeListener();

    return buchungsList;
  }

  public Part getSplitBuchungsList() throws RemoteException
  {
    if (splitbuchungsList == null)
    {
      splitbuchungsList = new SplitbuchungListTablePart(
          SplitbuchungsContainer.get(), new BuchungAction(true));
      splitbuchungsList.addColumn("Nr", "id-int");
      splitbuchungsList.addColumn("Konto", "konto");
      splitbuchungsList.addColumn("Typ", "splittyp", new Formatter()
      {
        @Override
        public String format(Object o)
        {
          Integer typ = (Integer) o;
          return SplitbuchungTyp.get(typ);
        }
      });
      splitbuchungsList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      splitbuchungsList.addColumn("Auszug", "auszugsnummer");
      splitbuchungsList.addColumn("Blatt", "blattnummer");
      splitbuchungsList.addColumn("Name", "name");
      splitbuchungsList.addColumn("Verwendungszweck", "zweck");
      if ((Boolean) Einstellungen
          .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
      {
        splitbuchungsList.addColumn("Buchungsklasse", "buchungsklasse",
            new BuchungsklasseFormatter());
      }
      splitbuchungsList.addColumn("Buchungsart", "buchungsart",
          new BuchungsartFormatter());
      splitbuchungsList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
      {
        splitbuchungsList.addColumn("Netto", "netto",
            new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
        if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
        {
          splitbuchungsList.addColumn("Steuer", "steuer", o -> {
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
      }
      splitbuchungsList.addColumn("Mitglied", Buchung.SOLLBUCHUNG,
          new SollbuchungFormatter());
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        splitbuchungsList.addColumn("Projekt", "projekt");
      }
      splitbuchungsList.setContextMenu(new SplitBuchungMenu(this));
      splitbuchungsList.setRememberColWidths(true);
      splitbuchungsList.addFeature(new FeatureSummary());
      splitbuchungsList.setFormatter(new TableFormatter()
      {
        /**
         * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
         */
        @Override
        public void format(TableItem item)
        {
          if (item == null)
          {
            return;
          }
          try
          {
            Buchung b = (Buchung) item.getData();
            if (b.isToDelete())
            {
              item.setForeground(new Color(null, new RGB(255, 0, 0)));
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to format line", e);
          }
        }
      });
      splitbuchungConsumer = new SplitbuchungMessageConsumer();
      Application.getMessagingFactory()
          .registerMessageConsumer(splitbuchungConsumer);
    }
    else
    {
      refreshSplitbuchungen();
    }
    splitbuchungsList.setMulti(true);
    return splitbuchungsList;
  }

  public void refreshSplitbuchungen() throws RemoteException
  {
    if (splitbuchungsList == null)
    {
      return;
    }
    splitbuchungsList.removeAll();

    for (Buchung b : SplitbuchungsContainer.get())
    {
      splitbuchungsList.addItem(b);
    }
    splitbuchungsList.sort();
  }

  private void starteAuswertung(boolean einzelbuchungen)
  {

    try
    {
      ArrayList<Buchungsart> buchungsarten = new ArrayList<>();
      if (!(query.getBuchungsart() != null
          && query.getBuchungsart().getID() == null))
      {
        DBIterator<Buchungsart> list = Einstellungen.getDBService()
            .createList(Buchungsart.class);
        if (query.getBuchungsart() != null
            && query.getBuchungsart().getID() != null)
        {
          list.addFilter("id = ?",
              new Object[] { query.getBuchungsart().getID() });
        }

        list.setOrder("ORDER BY nummer");

        while (list.hasNext())
        {
          buchungsarten.add(list.next());
        }
      }

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");

      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("buchungen", "",
          (String) Einstellungen.getEinstellung(Property.DATEINAMENMUSTER),
          "PDF").get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());

      auswertungBuchungPDF(buchungsarten, file, einzelbuchungen);
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
    }
  }

  private void starteCSVExport()
  {

    try
    {
      final List<Buchung> buchungen = query.get();

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");

      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("buchungen", "",
          (String) Einstellungen.getEinstellung(Property.DATEINAMENMUSTER),
          "CSV").get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());

      BackgroundTask t = new BackgroundTask()
      {

        @Override
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          try
          {
            new BuchungAuswertungCSV(buchungen, file, monitor);
            GUI.getCurrentView().reload();
          }
          catch (Exception ae)
          {
            Logger.error("Fehler", ae);
            GUI.getStatusBar().setErrorText(ae.getMessage());
            throw new ApplicationException(ae);
          }
        }

        @Override
        public void interrupt()
        {
          //
        }

        @Override
        public boolean isInterrupted()
        {
          return false;
        }
      };
      Application.getController().start(t);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }

  private void starteAuswertungBuchungsjournal()
  {

    try
    {

      BuchungsjournalSortDialog djs = new BuchungsjournalSortDialog(
          BuchungsjournalSortDialog.POSITION_CENTER);

      // 20220823: sbuer: Statische Variablen fuer neue Sortiermöglichkeiten
      String sort = djs.open();
      if (djs.getClosed())
        return;
      query.setOrdername(sort);

      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");

      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("buchungsjournal", "",
          (String) Einstellungen.getEinstellung(Property.DATEINAMENMUSTER),
          "PDF").get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());

      auswertungBuchungsjournalPDF(query, file, params);
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
  }

  private void auswertungBuchungPDF(final ArrayList<Buchungsart> buchungsarten,
      final File file, final boolean einzelbuchungen)
  {
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          GUI.getStatusBar().setSuccessText("Auswertung gestartet");
          new BuchungAuswertungPDF(buchungsarten, file, query, einzelbuchungen,
              params);
        }
        catch (ApplicationException ae)
        {
          Logger.error("Fehler", ae);
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
      }

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);
  }

  public Settings getSettings()
  {
    return settings;
  }

  private void auswertungBuchungsjournalPDF(final BuchungQuery query,
      final File file, final TreeMap<String, String> params)
  {
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          new BuchungsjournalPDF(query, file, params);
          GUI.getCurrentView().reload();
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
      }

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);
  }

  public class FilterListener implements Listener
  {

    FilterListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }

      try
      {
        getBuchungsList();
      }
      catch (RemoteException e)
      {
        GUI.getStatusBar().setErrorText(e.getMessage());
      }
    }

  }

  public void refreshBuchungsList()
  {
    try
    {
      getBuchungsList();
    }
    catch (RemoteException e)

    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  private void informKontoChangeListener() throws RemoteException
  {
    Konto k = (Konto) getSuchKonto().getValue();
    Event event = new Event();
    event.data = k;
    for (Listener listener : changeKontoListener)
    {
      listener.handleEvent(event);
    }
  }

  public void addKontoChangeListener(Listener listener)
  {
    this.changeKontoListener.add(listener);
  }

  public boolean isBuchungEditable() throws ApplicationException
  {
    try
    {
      if (!getBuchung().isNewObject())
      {
        Jahresabschluss ja = getBuchung().getJahresabschluss();
        if (ja != null)
        {
          GUI.getStatusBar().setErrorText(String.format(
              "Buchung wurde bereits am %s von %s abgeschlossen.",
              new JVDateFormatTTMMJJJJ().format(ja.getDatum()), ja.getName()));
          return editable = false;
        }
        Spendenbescheinigung spb = getBuchung().getSpendenbescheinigung();
        if (spb != null)
        {
          GUI.getStatusBar().setErrorText(
              "Buchung kann nicht bearbeitet werden. Sie ist einer Spendenbescheinigung zugeordnet.");
          return editable = false;
        }
        // Aufruf einer Splitbuchung aus Vor Zurueck
        if (getBuchung().getSpeicherung() && getBuchung().getSplitId() != null)
        {
          GUI.getStatusBar().setErrorText(
              "Buchung kann nicht bearbeitet werden. Sie ist einer Splitbuchung zugeordnet.");
          return editable = false;
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Status der aktuellen Buchung kann nicht geprüft werden.", e);
    }
    return editable = true;
  }

  public boolean isSplitBuchungEditable() throws ApplicationException
  {
    try
    {
      if (!getBuchung().isNewObject())
      {
        DBIterator<Buchung> it = Einstellungen.getDBService()
            .createList(Buchung.class);
        it.addFilter("splitid = ?", getBuchung().getSplitId());
        while (it.hasNext())
        {
          Buchung buchung = (Buchung) it.next();
          Jahresabschluss ja = buchung.getJahresabschluss();
          if (ja != null)
          {
            GUI.getStatusBar()
                .setErrorText(String.format(
                    "Buchung wurde bereits am %s von %s abgeschlossen.",
                    new JVDateFormatTTMMJJJJ().format(ja.getDatum()),
                    ja.getName()));
            return editable = false;
          }
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Status der aktuellen Buchung kann nicht geprüft werden.", e);
    }
    return editable = true;
  }

  public void buchungSpeichern() throws ApplicationException
  {
    try
    {
      DBTransaction.starten();
      handleStore();
      DBTransaction.commit();
      refreshSplitbuchungen();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();
      throw new ApplicationException(e);
    }
  }

  public Input getSuchMitgliedZugeordnet()
  {
    if (hasmitglied != null)
    {
      return hasmitglied;
    }

    ArrayList<MitgliedZustand> liste = new ArrayList<>();

    MitgliedZustand ja = new MitgliedZustand(true, "Ja");
    liste.add(ja);

    MitgliedZustand nein = new MitgliedZustand(false, "Nein");
    liste.add(nein);

    MitgliedZustand beide = new MitgliedZustand(null, "Beide");
    liste.add(beide);

    String bwert = settings.getString(settingsprefix + MITGLIEDZUGEORDNET,
        "Beide");
    MitgliedZustand b = ja;
    for (int i = 0; i < liste.size(); i++)
    {
      if (liste.get(i).getText().equals(bwert))
      {
        b = liste.get(i);
        break;
      }
    }

    hasmitglied = new SelectInput(liste, b);
    hasmitglied.addListener(new FilterListener());

    return hasmitglied;
  }

  public boolean isSuchMitgliedZugeordnetAktiv()
  {
    return hasmitglied != null;
  }

  public TextInput getIban() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }
    iban = new IBANInput(HBCIProperties.formatIban(getBuchung().getIban()),
        new TextInput(""));
    iban.setEnabled(editable);
    return iban;
  }

  /**
   * Hilfsklasse zur Anzeige der Importer.
   */
  private class MitgliedZustand
      implements GenericObject, Comparable<MitgliedZustand>
  {

    private Boolean value = null;

    private String text = null;

    private MitgliedZustand(Boolean value, String text)
    {
      this.value = value;
      this.text = text;
    }

    public Boolean getValue()
    {
      return value;
    }

    @SuppressWarnings("unused")
    public void setValue(Boolean value)
    {
      this.value = value;
    }

    public String getText()
    {
      return text;
    }

    @SuppressWarnings("unused")
    public void setText(String text)
    {
      this.text = text;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String arg0)
    {
      return getText();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    @Override
    public String[] getAttributeNames()
    {
      return new String[] { "name" };
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    @Override
    public String getID()
    {
      String repr = "null";
      if (getValue() != null)
        Boolean.toString(getValue());

      return getText() + "#" + repr;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    @Override
    public String getPrimaryAttribute()
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    @Override
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MitgliedZustand o)
    {
      if (o == null)
      {
        return -1;
      }
      try
      {
        return this.getText().compareTo((o).getText());
      }
      catch (Exception e)
      {
        // Tss, dann halt nicht
      }
      return 0;
    }

  }

  public void resetFilter()
  {
    try
    {
      if (isSuchBuchungsartAktiv())
      {
        suchbuchungsart.setValue(null);
      }
      if (isSuchProjektAktiv())
      {
        suchprojekt.setValue(null);
      }
      if (isSuchBetragAktiv())
      {
        suchbetrag.setValue("");
      }
      if (isSuchSplibuchungAktiv())
      {
        suchsplitbuchung.setValue(SplitFilter.ALLE);
      }
      if (isSuchMitgliedZugeordnetAktiv())
      {
        hasmitglied.setValue(hasmitglied.getList().get(2));
      }
      Calendar calendar = Calendar.getInstance();
      Integer year = calendar.get(Calendar.YEAR);
      Date startGJ = Datum.toDate(
          (String) Einstellungen.getEinstellung(Property.BEGINNGESCHAEFTSJAHR)
              + year);
      if (calendar.getTime().before(startGJ))
      {
        year = year - 1;
        startGJ = Datum.toDate(
            (String) Einstellungen.getEinstellung(Property.BEGINNGESCHAEFTSJAHR)
                + year);
      }
      if (isVondatumAktiv())
      {
        vondatum.setValue(startGJ);
      }
      calendar.setTime(startGJ);
      calendar.add(Calendar.YEAR, 1);
      calendar.add(Calendar.DAY_OF_MONTH, -1);
      if (isBisdatumAktiv())
      {
        bisdatum.setValue(calendar.getTime());
      }
      if (isUngeprueftAktiv())
      {
        ungeprueft.setValue(false);
      }
      if (isSuchtextAktiv())
      {
        suchtext.setValue("");
      }
      if (isMitgliedAktiv())
      {
        mitglied.setValue("");
      }
      if (isSuchSteuerAktiv())
      {
        suchsteuer.setValue(null);
      }

      refreshBuchungsList();
    }
    catch (Exception ex)
    {
      Logger.error("Error filter reset", ex);
    }
  }

  public boolean getGeldkonto()
  {
    return geldkonto;
  }

  public String getSettingsPrefix()
  {
    return settingsprefix;
  }

  public ToolTipButton getTTZurueckButton()
  {
    return new ToolTipButton("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Date von = (Date) getVondatum().getValue();
        Date bis = (Date) getBisdatum().getValue();
        if (getRangeTyp(von, bis) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(von.toInstant(),
              bis.toInstant());
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.DAY_OF_MONTH, -delta);
          getVondatum().setValue(calendar.getTime());
          calendar.setTime(bis);
          calendar.add(Calendar.DAY_OF_MONTH, -delta);
          getBisdatum().setValue(calendar.getTime());
        }
        else
        {
          LocalDate lvon = von.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          LocalDate lbis = bis.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.MONTH, -delta);
          getVondatum().setValue(calendar.getTime());
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
          getBisdatum().setValue(calendar.getTime());
        }
        try
        {
          getBuchungsList();
        }
        catch (RemoteException ex)
        {
          throw new ApplicationException(ex.getMessage());
        }
      }
    }, null, false, "go-previous.png");
  }

  public ToolTipButton getTTVorButton()
  {
    return new ToolTipButton("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Date von = (Date) getVondatum().getValue();
        Date bis = (Date) getBisdatum().getValue();
        if (getRangeTyp(von, bis) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(von.toInstant(),
              bis.toInstant());
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          getVondatum().setValue(calendar.getTime());
          calendar.setTime(bis);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          getBisdatum().setValue(calendar.getTime());
        }
        else
        {
          LocalDate lvon = von.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          LocalDate lbis = bis.toInstant().atZone(ZoneId.systemDefault())
              .toLocalDate();
          int delta = (int) ChronoUnit.MONTHS.between(lvon, lbis);
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.MONTH, delta);
          getVondatum().setValue(calendar.getTime());
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
          getBisdatum().setValue(calendar.getTime());
        }
        try
        {
          getBuchungsList();
        }
        catch (RemoteException ex)
        {
          throw new ApplicationException(ex.getMessage());
        }
      }
    }, null, false, "go-next.png");
  }

  private RANGE getRangeTyp(Date von, Date bis) throws ApplicationException
  {
    checkDate();
    calendar.setTime(von);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    calendar.setTime(bis);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    return RANGE.MONAT;
  }

  private void checkDate() throws ApplicationException
  {
    Date von = (Date) getVondatum().getValue();
    Date bis = (Date) getBisdatum().getValue();
    if (von == null)
    {
      throw new ApplicationException("Bitte Von Datum eingeben!");
    }
    if (bis == null)
    {
      throw new ApplicationException("Bitte Bis Datum eingeben!");
    }
    if (von.after(bis))
    {
      throw new ApplicationException("Von Datum ist nach Bis Datum!");
    }
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class SplitbuchungMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    @Override
    public Class<?>[] getExpectedMessageTypes()
    {
      return new Class[] { SplitbuchungMessage.class };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(final Message message) throws Exception
    {
      GUI.getDisplay().syncExec(new Runnable()
      {

        @Override
        public void run()
        {
          try
          {
            refreshSplitbuchungen();
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("Fehler beim Update der Splitbuchung Anzeige.", e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(SplitbuchungMessageConsumer.this);
          }
        }
      });
    }
  }

  public void deregisterSplitbuchungConsumer()
  {
    Application.getMessagingFactory()
        .unRegisterMessageConsumer(splitbuchungConsumer);
  }
}
