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
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Messaging.SplitbuchungMessage;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.dialogs.SammelueberweisungAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.SollbuchungAuswahlDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.KontoauswahlInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.gui.menu.SplitBuchungMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.SplitbuchungListTablePart;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Kontenfilter;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Projekt;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
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
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungDetailControl extends VorZurueckControl implements Savable
{

  public final static String GELDKONTO_PREFIX = "geldkonto.";

  public final static String ANLAGENKONTO_PREFIX = "anlagenkonto.";

  public final static String KONTO_ID = "kontoid";

  /* Split-Buchnungen */
  private JVereinTablePart splitbuchungsList;

  /* Controls */
  private Input id;

  private Input umsatzid;

  private DialogInput konto;

  private IntegerInput auszugsnummer;

  private IntegerInput blattnummer;

  private Input name;

  private BigDecimalInput betrag;

  private TextAreaInput zweck;

  private DateInput datum = null;

  private Input art;

  private DialogInput sollbuchungInput;

  private TextAreaInput kommentar;

  private SelectInput steuer;

  // Definition für beide Auswahlvarianten (SelectInput und
  // BuchungsartSearchInput)
  private AbstractInput buchungsart;

  private SelectInput buchungsklasse;

  private SelectInput projekt;

  private CheckboxInput verzicht;

  private TextAreaInput bezeichnungsachzuwendung;

  private SelectInput herkunftspende;

  private CheckboxInput unterlagenwertermittlung;

  private Buchung buchung;

  private Button sammelueberweisungButton;

  private TextInput iban = null;

  private CheckboxInput geprueft;

  private boolean editable = false;

  private SplitbuchungMessageConsumer splitbuchungConsumer = null;

  private SollbuchungAuswahlDialog sollbuchungDialog = null;

  private Kontenfilter kontenfilter = Kontenfilter.ALLE;

  private Settings settings = null;

  private String settingsprefix = "";

  public BuchungDetailControl(AbstractView view, Kontenfilter kontenfilter)
  {
    super(view);
    this.kontenfilter = kontenfilter;
    settingsprefix = GELDKONTO_PREFIX;
    if (kontenfilter == Kontenfilter.ANLAGEKONTO)
    {
      settingsprefix = ANLAGENKONTO_PREFIX;
    }
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public String getSettingsPrefix()
  {
    return settingsprefix;
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
      b.setIban(ib.replace(" ", ""));

    b.setBetrag((BigDecimal) getBetrag().getValue());
    b.setZweck((String) getZweck().getValue());
    b.setDatum((Date) getDatum().getValue());
    b.setArt((String) getArt().getValue());
    if (verzicht != null)
    {
      b.setVerzicht((Boolean) getVerzicht().getValue());
    }
    b.setKommentar((String) getKommentar().getValue());
    b.setSollbuchung((Sollbuchung) getSollbuchungInput().getValue());
    b.setGeprueft((Boolean) getGeprueft().getValue());
    if (steuer != null)
    {
      b.setSteuer((Steuer) getSteuer().getValue());
    }
    if (bezeichnungsachzuwendung != null)
    {
      b.setBezeichnungSachzuwendung(
          (String) getBezeichnungSachzuwendung().getValue());
    }
    if (herkunftspende != null)
    {
      HerkunftSpende hsp = (HerkunftSpende) getHerkunftSpende().getValue();
      b.setHerkunftSpende(hsp.getKey());
    }
    if (unterlagenwertermittlung != null)
    {
      b.setUnterlagenWertermittlung(
          (Boolean) getUnterlagenWertermittlung().getValue());
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

  public DialogInput getKonto() throws RemoteException
  {
    if (konto != null)
    {
      return konto;
    }
    String kontoid = getVorauswahlKontoId();
    konto = new KontoauswahlInput(getBuchung().getKonto())
        .getKontoAuswahl(false, kontoid, false, true, kontenfilter);
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
    return settings.getString(settingsprefix + BuchungDetailControl.KONTO_ID,
        "");
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

  public BigDecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }

    if (getBuchung().isNewObject() && getBuchung().getBetrag() == null)
    {
      betrag = new BigDecimalInput(Einstellungen.BIGDECIMALFORMAT);
    }
    else
    {
      betrag = new BigDecimalInput(getBuchung().getBetrag(),
          Einstellungen.BIGDECIMALFORMAT);
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

  public TextAreaInput getBezeichnungSachzuwendung() throws RemoteException
  {
    if (bezeichnungsachzuwendung != null)
    {
      return bezeichnungsachzuwendung;
    }
    bezeichnungsachzuwendung = new TextAreaInput(
        getBuchung().getBezeichnungSachzuwendung(), 1000);
    bezeichnungsachzuwendung.setHeight(50);
    bezeichnungsachzuwendung.setEnabled(editable);
    return bezeichnungsachzuwendung;
  }

  public SelectInput getHerkunftSpende() throws RemoteException
  {
    if (herkunftspende != null)
    {
      return herkunftspende;
    }
    herkunftspende = new SelectInput(HerkunftSpende.getArray(),
        new HerkunftSpende(getBuchung().getHerkunftSpende()));
    herkunftspende.setEnabled(editable);
    return herkunftspende;
  }

  public CheckboxInput getUnterlagenWertermittlung() throws RemoteException
  {
    if (unterlagenwertermittlung != null)
    {
      return unterlagenwertermittlung;
    }
    unterlagenwertermittlung = new CheckboxInput(
        getBuchung().getUnterlagenWertermittlung());
    unterlagenwertermittlung.setEnabled(editable);
    return unterlagenwertermittlung;
  }

  public DialogInput getSollbuchungInput() throws RemoteException
  {
    if (sollbuchungInput != null && !sollbuchungInput.getControl().isDisposed())
    {
      return sollbuchungInput;
    }
    sollbuchungDialog = new SollbuchungAuswahlDialog(getBuchung(), false);
    sollbuchungDialog.addCloseListener(new SollbuchungListener());
    Sollbuchung sollb = getBuchung().getSollbuchung();
    sollbuchungInput = new DialogInput(
        sollb != null
            ? Adressaufbereitung.getNameVorname(sollb.getMitglied()) + ", "
                + new JVDateFormatTTMMJJJJ().format(sollb.getDatum()) + ", "
                + Einstellungen.BIGDECIMALFORMAT.format(sollb.getBetrag())
            : "",
        sollbuchungDialog);
    sollbuchungInput.disableClientControl();
    sollbuchungInput.setValue(sollb);
    return sollbuchungInput;
  }

  /**
   * Listener, der die Auswahl der Sollbuchung ueberwacht
   */
  private class SollbuchungListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {

      if (event == null)
      {
        return;
      }

      if (event.data == null)
      {
        try
        {
          if (event.detail != SWT.CANCEL)
          {
            getSollbuchungInput().setText("");
            GUI.getStatusBar()
                .setSuccessText("Sollbuchung Zuordnung gelöscht.");
          }
          return;
        }
        catch (RemoteException er)
        {
          String error = "Fehler bei Auswahl der Sollbuchung";
          Logger.error(error, er);
          GUI.getStatusBar().setErrorText(error);
        }
      }
      try
      {
        DBTransaction.starten();
        String b = "";
        String message = "";
        Sollbuchung sollb = null;
        if (event.data instanceof Mitglied)
        {
          Mitglied mitglied = (Mitglied) event.data;

          sollb = (Sollbuchung) Einstellungen.getDBService()
              .createObject(Sollbuchung.class, null);
          sollb.setBetrag((BigDecimal) getBetrag().getValue());
          sollb.setDatum((Date) getDatum().getValue());
          sollb.setMitglied(mitglied);
          sollb.setZahler(mitglied);
          sollb.setZahlungsweg(Zahlungsweg.ÜBERWEISUNG);
          sollb.setZweck1((String) getZweck().getValue());
          sollb.store();

          SollbuchungPosition sbp = (SollbuchungPosition) Einstellungen
              .getDBService().createObject(SollbuchungPosition.class, null);
          sbp.setBetrag(sollb.getBetrag());
          sbp.setBuchungsartId(getSelectedBuchungsArtId());
          sbp.setBuchungsklasseId(getSelectedBuchungsKlasseId());
          if (steuer != null)
          {
            sbp.setSteuer((Steuer) getSteuer().getValue());
          }
          sbp.setDatum(sollb.getDatum());
          sbp.setZweck(sollb.getZweck1());
          sbp.setSollbuchung(sollb.getID());
          sbp.store();
          message = "Sollbuchung erzeugt und zugeordnet.";
          sollbuchungDialog.setData(sollb);
        }

        if (event.data instanceof Sollbuchung)
        {
          sollb = (Sollbuchung) event.data;
          String name = (String) getName().getValue();
          String zweck1 = (String) getZweck().getValue();
          BigDecimal betrag = (BigDecimal) getBetrag().getValue();
          if ((name == null || name.length() == 0)
              && (zweck1 == null || zweck1.length() == 0) && betrag == null)
          {
            getName().setValue(
                Adressaufbereitung.getNameVorname(sollb.getMitglied()));
            getZweck().setValue(sollb.getZweck1());
            getBetrag().setValue(sollb.getBetrag());
          }
          List<SollbuchungPosition> sbpList = sollb
              .getSollbuchungPositionList();
          if (sbpList.size() > 0)
          {
            if (getBuchungsart().getValue() == null)
            {
              getBuchungsart().setValue(sbpList.get(0).getBuchungsart());
            }
            if (isBuchungsklasseActive()
                && getBuchungsklasse().getValue() == null)
            {
              getBuchungsklasse().setValue(sbpList.get(0).getBuchungsklasse());
            }
            if (steuer != null && getSteuer().getValue() == null)
            {
              getSteuer().setValue(sbpList.get(0).getSteuer());
            }
          }
          message = "Sollbuchung zugeordnet.";
        }
        if (sollb != null)
        {
          b = Adressaufbereitung.getNameVorname(sollb.getMitglied()) + ", "
              + new JVDateFormatTTMMJJJJ().format(sollb.getDatum()) + ", "
              + Einstellungen.BIGDECIMALFORMAT.format(sollb.getBetrag());
        }
        getSollbuchungInput().setText(b);
        DBTransaction.commit();
        GUI.getStatusBar().setSuccessText(message);
      }
      catch (ApplicationException ae)
      {
        DBTransaction.rollback();
        String error = ae.getMessage();
        Logger.error(error, ae);
        GUI.getStatusBar().setErrorText(error);
      }
      catch (RemoteException er)
      {
        DBTransaction.rollback();
        String error = "Fehler bei Zuordnung der Sollbuchung";
        Logger.error(error, er);
        GUI.getStatusBar().setErrorText(error);
      }
      catch (Exception e)
      {
        DBTransaction.rollback();
        Logger.error("Fehler bei der Zuordnung der Sollbuchung", e);
        GUI.getStatusBar()
            .setErrorText("Fehler bei der Zuordnung der Sollbuchung");
      }
    }
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
    buchungsart = new BuchungsartInput().getBuchungsartInput(
        getBuchung().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    if (getBuchung().isSplitbuchung())
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
    if (getBuchung().isSplitbuchung() && (Boolean) Einstellungen
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
              b.setBetrag(BigDecimal.valueOf(ssub.getBetrag()).negate()
                  .setScale(2, RoundingMode.HALF_UP));
              b.setBlattnummer(master.getBlattnummer());
              b.setBuchungsartId(master.getBuchungsartId());
              b.setBuchungsklasseId(master.getBuchungsklasseId());
              b.setDatum(su.getAusfuehrungsdatum());
              b.setKonto(master.getKonto());
              b.setName(ssub.getGegenkontoName());
              b.setSplitbuchung(false);
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

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Buchung b = (Buchung) prepareStore();

      if (!b.isSplitbuchung())
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
      Konto konto = (Konto) getKonto().getValue();
      if (konto == null)
      {
        throw new ApplicationException(
            "Kein Konto Ausgewählt. Ggfs. erst unter Buchführung->Konten ein Konto anlegen.");
      }
      settings.setAttribute(settingsprefix + BuchungDetailControl.KONTO_ID,
          konto.getID());
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
          new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
      {
        splitbuchungsList.addColumn("Netto", "netto",
            new CurrencyFormatter("", Einstellungen.BIGDECIMALFORMAT));
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
      splitbuchungsList.addColumn("Mitglied - Sollbuchung", Buchung.SOLLBUCHUNG,
          new SollbuchungFormatter());
      if ((Boolean) Einstellungen.getEinstellung(Property.PROJEKTEANZEIGEN))
      {
        splitbuchungsList.addColumn("Projekt", "projekt");
      }
      splitbuchungsList.setContextMenu(new SplitBuchungMenu(this));
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

  public Settings getSettings()
  {
    return settings;
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
        if (!getBuchung().isSplitbuchung() && getBuchung().getSplitId() != null)
        {
          GUI.getStatusBar().setErrorText(
              "Buchung kann nicht bearbeitet werden. Sie ist einer Splitbuchung zugeordnet.");
          return editable = false;
        }
        if (getBuchung().getAbrechnungslauf() != null
            && getBuchung().getAbrechnungslauf().getAbgeschlossen())
        {
          GUI.getStatusBar().setErrorText(
              "Buchung kann nicht bearbeitet werden. Der zugehörige Abrechnungslauf ist abgeschlossen.");
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
      throw new ApplicationException(e.getMessage());
    }
  }

  public TextInput getIban() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }
    iban = new IBANInput(new IBANFormatter().format(getBuchung().getIban()),
        new TextInput(""));
    iban.setEnabled(editable);
    return iban;
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
