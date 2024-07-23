/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe, Leonardo Mörlein
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
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Messaging.MitgliedskontoMessage;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.menu.MitgliedskontoMenu;
import de.jost_net.JVerein.gui.parts.SollbuchungListTablePart;
import de.jost_net.JVerein.io.Kontoauszug;
import de.jost_net.JVerein.io.Mahnungsausgabe;
import de.jost_net.JVerein.io.Rechnungsausgabe;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.Ausgabesortierung;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedskontoControl extends AbstractControl
{
  public enum DIFFERENZ
  {
    EGAL("Egal"), FEHLBETRAG("Fehlbetrag"), UEBERZAHLUNG("Überzahlung");
    private final String titel;

    private DIFFERENZ(String titel)
    {
      this.titel = titel;
    }

    @Override
    public String toString()
    {
      return titel;
    }

    public static DIFFERENZ fromString(final String text)
    {
      for (DIFFERENZ item : DIFFERENZ.values())
      {
        if (item.titel.equals(text))
          return item;
      }
      return null;
    }
  }

  private Settings settings;

  private DateInput datum = null;

  private TextAreaInput zweck1;

  private SelectInput zahlungsweg;

  private DecimalInput betrag;

  private AbstractInput buchungsart;

  private FormularInput formular = null;

  private Mitgliedskonto mkto;

  private TablePart mitgliedskontoList;

  private TablePart mitgliedskontoList2;

  private TreePart mitgliedskontoTree;

  public static final String DATUM_MITGLIEDSKONTO = "datum.mitgliedskonto.";

  //
  // public static final String DATUM_RECHNUNG = "datum.rechnung.";
  //
  // public static final String DATUM_MAHNUNG = "datum.mahnung.";

  public enum TYP
  {
    RECHNUNG, MAHNUNG
  }

  private String datumverwendung = null;

  private DateInput vondatum = null;

  private DateInput bisdatum = null;

  private CheckboxInput ohneabbucher = null;

  private SelectInput ausgabeart = null;

  private SelectInput ausgabesortierung = null;

  private TextInput suchname = null;

  private TextInput suchname2 = null;

  private SelectInput differenz = null;

  private CheckboxInput spezialsuche1 = null;
  
  private CheckboxInput spezialsuche2 = null;

  private TextInput betreff = null;

  private TextAreaInput txt = null;

  // private CheckboxInput offenePosten = null;

  private MitgliedskontoMessageConsumer mc = null;

  private Action action;
  
  private boolean umwandeln;

  public MitgliedskontoControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Mitgliedskonto getMitgliedskonto()
  {
    if (mkto != null)
    {
      return mkto;
    }
    mkto = (Mitgliedskonto) getCurrentObject();
    return mkto;
  }

  public Settings getSettings()
  {
    return settings;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }

    Date d = new Date();
    if (getMitgliedskonto() != null)
    {
      d = getMitgliedskonto().getDatum();
    }

    this.datum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.datum.setTitle("Datum");
    this.datum.setText("Bitte Datum wählen");
    this.datum.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) datum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    this.datum.setMandatory(true);
    return datum;
  }

  public TextAreaInput getZweck1() throws RemoteException
  {
    if (zweck1 != null)
    {
      return zweck1;
    }
    String z = "";
    if (getMitgliedskonto() != null)
    {
      z = getMitgliedskonto().getZweck1();
    }
    zweck1 = new TextAreaInput(z, 500);
    zweck1.setHeight(50);
    zweck1.setMandatory(true);
    return zweck1;
  }

  public SelectInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }
    Integer z = null;
    if (getMitgliedskonto() != null)
    {
      z = getMitgliedskonto().getZahlungsweg();
    }
    zahlungsweg = new SelectInput(Zahlungsweg.getArray(),
        z == null
            ? new Zahlungsweg(Einstellungen.getEinstellung().getZahlungsweg())
            : new Zahlungsweg(getMitgliedskonto().getZahlungsweg()));
    zahlungsweg.setName("Zahlungsweg");
    return zahlungsweg;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    Double b = Double.valueOf(0.0d);
    if (getMitgliedskonto() != null)
    {
      b = getMitgliedskonto().getBetrag();
    }
    betrag = new DecimalInput(b, Einstellungen.DECIMALFORMAT);
    return betrag;
  }

  public Input getBuchungsart() throws RemoteException
  {
    if (buchungsart != null && !buchungsart.getControl().isDisposed())
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(buchungsart,
        getMitgliedskonto().getBuchungsart());
    return buchungsart;
  }

  public FormularInput getFormular(FormularArt mahnung) throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    formular = new FormularInput(mahnung);
    return formular;
  }

  public String getDatumverwendung()
  {
    return this.datumverwendung;
  }

  public DateInput getVondatum(String datumverwendung)
  {
    if (vondatum != null)
    {
      return vondatum;
    }
    Date d = null;
    this.datumverwendung = datumverwendung;
    
    String tmp = settings.getString(datumverwendung + "datumvon", null);
    
    if (tmp != null)
    {
      try
      {
        d = new JVDateFormatTTMMJJJJ().parse(tmp);
      }
      catch (ParseException e)
      {
        //
      }
    }

    this.vondatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.vondatum.setTitle("Anfangsdatum");
    this.vondatum.setText("Bitte Anfangsdatum wählen");
    return vondatum;
  }

  public DateInput getBisdatum(String datumverwendung)
  {
    if (bisdatum != null)
    {
      return bisdatum;
    }
    this.datumverwendung = datumverwendung;
    Date d = null;
    String tmp = settings.getString(datumverwendung + "datumbis", null);
    if (tmp != null)
    {
      try
      {
        d = new JVDateFormatTTMMJJJJ().parse(tmp);
      }
      catch (ParseException e)
      {
        //
      }
    }
    this.bisdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.bisdatum.setTitle("Endedatum");
    this.bisdatum.setText("Bitte Endedatum wählen");
    return bisdatum;
  }

  public Object[] getCVSExportGrenzen(Mitglied selectedMitglied)
  {
    return new Object[] {
        getVondatum(MitgliedskontoControl.DATUM_MITGLIEDSKONTO).getValue(),
        getBisdatum(MitgliedskontoControl.DATUM_MITGLIEDSKONTO).getValue(),
        getDifferenz().getValue(), getCVSExportGrenzeOhneAbbucher(),
        selectedMitglied };
  }

  private Boolean getCVSExportGrenzeOhneAbbucher()
  {
    if (null == ohneabbucher)
      return Boolean.FALSE;
    return (Boolean) ohneabbucher.getValue();
  }

  public CheckboxInput getOhneAbbucher()
  {
    if (ohneabbucher != null)
    {
      return ohneabbucher;
    }
    ohneabbucher = new CheckboxInput(false);
    return ohneabbucher;
  }

  public CheckboxInput getSpezialSuche1()
  {
    if (spezialsuche1 != null && !spezialsuche1.getControl().isDisposed())
    {
      return spezialsuche1;
    }
    spezialsuche1 = new CheckboxInput(false);
    spezialsuche1.setName("Erlaube Teilstring Vergleich");
    spezialsuche1.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        try
        {
          refreshMitgliedkonto1();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });

    return spezialsuche1;
  }
  
  public CheckboxInput getSpezialSuche2()
  {
    if (spezialsuche2 != null && !spezialsuche2.getControl().isDisposed())
    {
      return spezialsuche2;
    }
    spezialsuche2 = new CheckboxInput(false);
    spezialsuche2.setName("Erlaube Teilstring Vergleich");
    spezialsuche2.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        try
        {
          refreshMitgliedkonto2();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });

    return spezialsuche2;
  }

  public SelectInput getDifferenz()
  {
    if (differenz != null)
    {
      return differenz;
    }
    DIFFERENZ defaultwert = DIFFERENZ
        .fromString(settings.getString("differenz", DIFFERENZ.EGAL.toString()));
    return getDifferenz(defaultwert);
  }

  public SelectInput getDifferenz(DIFFERENZ defaultvalue)
  {
    differenz = new SelectInput(DIFFERENZ.values(), defaultvalue);
    differenz.setName("Differenz");
    differenz.addListener(new FilterListener());
    return differenz;
  }

  // Für SollbuchungListeView
  public TextInput getSuchName()
  {
    if (suchname != null && !suchname.getControl().isDisposed())
    {
      return suchname;
    }
    suchname = new TextInput(settings.getString("sollbuchung.suchname",""), 30);
    suchname.setName("Name");
    return suchname;
  }

  // Für SollbuchungAuswahlDialog
  public TextInput getSuchName1(boolean newcontrol)
  {
    if (!newcontrol && suchname != null)
    {
      return suchname;
    }
    suchname = new TextInput("", 30);
    suchname.setName("Name");
    return suchname;
  }
  
  //Für SollbuchungAuswahlDialog
  public TextInput getSuchName2(boolean newcontrol)
  {
    if (!newcontrol && suchname2 != null)
    {
      return suchname2;
    }
    suchname2 = new TextInput("", 30);
    suchname2.setName("Name");
    return suchname2;
  }

  public SelectInput getAusgabeart()
  {
    if (ausgabeart != null)
    {
      return ausgabeart;
    }
    ausgabeart = new SelectInput(Ausgabeart.values(),
        Ausgabeart.valueOf(settings.getString("ausgabeart", "DRUCK")));
    ausgabeart.setName("Ausgabe");
    return ausgabeart;
  }

  public SelectInput getAusgabesortierung()
  {
    if (ausgabesortierung != null)
    {
      return ausgabesortierung;
    }
    ausgabesortierung = new SelectInput(Ausgabesortierung.values(),
        Ausgabesortierung.getByKey(settings.getInt("ausgabesortierung", 1)));
    ausgabesortierung.setName("Sortierung");
    return ausgabesortierung;
  }

  public TextInput getBetreff(String verwendung)
  {
    if (betreff != null)
    {
      return betreff;
    }
    betreff = new TextInput(
        settings.getString(verwendung + ".mail.betreff", ""), 100);
    betreff.setName("Betreff");
    return betreff;
  }

  public TextAreaInput getTxt(String verwendung)
  {
    if (txt != null)
    {
      return txt;
    }
    txt = new TextAreaInput(settings.getString(verwendung + ".mail.text", ""),
        10000);
    txt.setName("Text");
    return txt;
  }
  
  public void saveDefaults()
  {	  
    if (this.vondatum != null)
    {
      Date tmp = (Date) getVondatum("kontoauszugdatumvon").getValue();
      if (tmp != null)
      {
        settings.setAttribute("kontoauszugdatumvon",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute("kontoauszugdatumvon", "");
      }
    }

    if (this.bisdatum != null)
    {
      Date tmp = (Date) getBisdatum("kontoauszugdatumbis").getValue();
      if (tmp != null)
      {
        settings.setAttribute("kontoauszugdatumbis",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute("kontoauszugbatumbis", "");
      }
    }	  
  }
  
  public void handleStore()
  {
    try
    {
      Mitgliedskonto mkto = getMitgliedskonto();
      mkto.setBetrag((Double) getBetrag().getValue());
      mkto.setDatum((Date) getDatum().getValue());
      Zahlungsweg zw = (Zahlungsweg) getZahlungsweg().getValue();
      mkto.setZahlungsweg(zw.getKey());
      mkto.setZweck1((String) getZweck1().getValue());

      double steuersatz = 0d;
      if (getBuchungsart().getValue() != null)
      {
        mkto.setBuchungsart((Buchungsart) getBuchungsart().getValue());
        Buchungsart bart = mkto.getBuchungsart();
        steuersatz = bart.getSteuersatz();
      }
      // Update taxes and netto amount
      mkto.setSteuersatz(steuersatz);
      double netto = ((Double) getBetrag().getValue() / (1d + (steuersatz / 100d)));
      mkto.setNettobetrag(netto);
      mkto.setSteuerbetrag((Double) getBetrag().getValue() - netto);
      
      mkto.store();
      GUI.getStatusBar().setSuccessText("Sollbuchung gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim speichern der Sollbuchung";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getMitgliedskontoTree(Mitglied mitglied) throws RemoteException
  {
    mitgliedskontoTree = new TreePart(new MitgliedskontoNode(mitglied),
        (Action) null)
    {

      @SuppressWarnings("unchecked")
      @Override
      public void paint(Composite composite) throws RemoteException
      {
        super.paint(composite);
        List<MitgliedskontoNode> items = mitgliedskontoTree.getItems();
        for (MitgliedskontoNode mkn : items)
        {
          @SuppressWarnings("rawtypes")
          GenericIterator items2 = mkn.getChildren();
          while (items2.hasNext())
          {
            MitgliedskontoNode mkn2 = (MitgliedskontoNode) items2.next();
            mitgliedskontoTree.setExpanded(mkn2, false);
          }
        }
      }
    };
    mitgliedskontoTree.addColumn("Name, Vorname", "name");
    mitgliedskontoTree.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    mitgliedskontoTree.addColumn("Zweck1", "zweck1");
    mitgliedskontoTree.addColumn("Zahlungsweg", "zahlungsweg",
        new ZahlungswegFormatter());
    mitgliedskontoTree.addColumn("Soll", "soll",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    mitgliedskontoTree.addColumn("Ist", "ist",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    mitgliedskontoTree.addColumn("Differenz", "differenz",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    mitgliedskontoTree.setContextMenu(new MitgliedskontoMenu());
    mitgliedskontoTree.setRememberColWidths(true);
    mitgliedskontoTree.setRememberOrder(true);
    mitgliedskontoTree.setFormatter(new MitgliedskontoTreeFormatter());
    this.mc = new MitgliedskontoMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

    return mitgliedskontoTree;
  }

  public TablePart getMitgliedskontoList(Action action, ContextMenu menu, boolean umwandeln)
      throws RemoteException
  {
    this.action = action;
    this.umwandeln = umwandeln;
    @SuppressWarnings("rawtypes")
    GenericIterator mitgliedskonten = getMitgliedskontoIterator(umwandeln);
    settings.setAttribute(datumverwendung + "differenz", getDifferenz().getValue().toString());
    if (mitgliedskontoList == null)
    {
      mitgliedskontoList = new SollbuchungListTablePart(mitgliedskonten, action);
      mitgliedskontoList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      mitgliedskontoList.addColumn("Abrechnungslauf", "abrechnungslauf");
      mitgliedskontoList.addColumn("Name", "mitglied");
      mitgliedskontoList.addColumn("Zweck", "zweck1");
      mitgliedskontoList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      mitgliedskontoList.addColumn("Zahlungseingang", "istsumme",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      mitgliedskontoList.setContextMenu(menu);
      mitgliedskontoList.setRememberColWidths(true);
      mitgliedskontoList.setRememberOrder(true);
      mitgliedskontoList.setMulti(true);
      mitgliedskontoList.addFeature(new FeatureSummary());
    }
    else
    {
      mitgliedskontoList.removeAll();
      while (mitgliedskonten.hasNext())
      {
        mitgliedskontoList.addItem(mitgliedskonten.next());
      }
    }
    return mitgliedskontoList;
  }

  public TablePart getMitgliedskontoList2(Action action, ContextMenu menu)
      throws RemoteException
  {
    this.action = action;
    GenericIterator<Mitglied> mitglieder = getMitgliedIterator();
    if (mitgliedskontoList2 == null)
    {
      mitgliedskontoList2 = new TablePart(mitglieder, action);
      mitgliedskontoList2.addColumn("Name", "name");
      mitgliedskontoList2.addColumn("Vorname", "vorname");
      mitgliedskontoList2.setContextMenu(menu);
      mitgliedskontoList2.setRememberColWidths(true);
      mitgliedskontoList2.setRememberOrder(true);
      mitgliedskontoList2.setMulti(true);
      mitgliedskontoList2.addFeature(new FeatureSummary());
    }
    else
    {
      mitgliedskontoList2.removeAll();
      while (mitglieder.hasNext())
      {
        mitgliedskontoList2.addItem(mitglieder.next());
      }
    }
    return mitgliedskontoList2;
  }

  private void refreshMitgliedkonto2() throws RemoteException
  {
    GenericIterator<Mitglied> mitglieder = getMitgliedIterator();
    mitgliedskontoList2.removeAll();
    while (mitglieder.hasNext())
    {
      mitgliedskontoList2.addItem(mitglieder.next());
    }
  }

  private GenericIterator<Mitglied> getMitgliedIterator() throws RemoteException
  {
    DBIterator<Mitglied> mitglieder = Einstellungen.getDBService()
        .createList(Mitglied.class);
    // MitgliedUtils.setMitgliedOderSpender(mitglieder);
    if (suchname2 != null && suchname2.getValue() != null)
    {
      StringBuffer where = new StringBuffer();
      ArrayList<String> object = new ArrayList<>();
      StringTokenizer tok = new StringTokenizer((String) suchname2.getValue(),
          " ,-");
      where.append("(");
      boolean first = true;
      while (tok.hasMoreElements())
      {
        if (!first)
        {
          where.append("or ");
        }
        first = false;
        where.append(
            "upper(name) like upper(?) or upper(vorname) like upper(?) ");
        String o = tok.nextToken();
        if ((Boolean) getSpezialSuche2().getValue())
        {
          o = "%" + o + "%";
        }
        object.add(o);
        object.add(o);
      }
      where.append(")");
      if (where.length() > 2)
      {
        mitglieder.addFilter(where.toString(), object.toArray());
      }
    }
    mitglieder.setOrder("order by name, vorname");
    return mitglieder;
  }

  
  public void refreshMitgliedkonto1() throws RemoteException
  {
    @SuppressWarnings("rawtypes")
    GenericIterator mitgliedskonten = getMitgliedskontoIterator(umwandeln);
    settings.setAttribute(datumverwendung + "differenz", getDifferenz().getValue().toString());
    mitgliedskontoList.removeAll();
    while (mitgliedskonten.hasNext())
    {
      mitgliedskontoList.addItem(mitgliedskonten.next());
    }
  }
  
  @SuppressWarnings("rawtypes")
  public GenericIterator getMitgliedskontoIterator(boolean umwandeln) throws RemoteException
  {
    this.umwandeln = umwandeln;
    Date d1 = null;
    java.sql.Date vd = null;
    java.sql.Date bd = null;
    if (vondatum != null)
    {
      d1 = (Date) vondatum.getValue();
      if (d1 != null)
      {
        settings.setAttribute(datumverwendung + "datumvon",
            new JVDateFormatTTMMJJJJ().format(d1));
        vd = new java.sql.Date(d1.getTime());
      }
      else
      {
        settings.setAttribute(datumverwendung + "datumvon", "");
      }
    }
    if (bisdatum != null)
    {
      d1 = (Date) bisdatum.getValue();
      if (d1 != null)
      {
        settings.setAttribute(datumverwendung + "datumbis",
            new JVDateFormatTTMMJJJJ().format(d1));
        bd = new java.sql.Date(d1.getTime());
      }
      else
      {
        settings.setAttribute(datumverwendung + "datumbis", "");
      }
    }
    
    DIFFERENZ diff = DIFFERENZ.EGAL;
    if (differenz != null)
    {
      diff = (DIFFERENZ) differenz.getValue();
    }
    
    // Falls kein Name und keine Differenz dann alles lesen
    if ((suchname == null || suchname.getValue() == null || 
        ((String) suchname.getValue()).isEmpty()) && diff == DIFFERENZ.EGAL)
    {
      DBIterator<Mitgliedskonto> sollbuchungen = Einstellungen.getDBService()
          .createList(Mitgliedskonto.class);
      if (vd != null)
      {
      sollbuchungen.addFilter("mitgliedskonto.datum >= ? ",
          new Object[] { vd });
      }
      if (bd != null)
      {
      sollbuchungen.addFilter("mitgliedskonto.datum <= ? ",
          new Object[] { bd });
      }
      return sollbuchungen;
    }
    
    // Falls ein Name aber keine Differenz dann alles des Mitglieds lesen
    if (suchname != null && suchname.getValue() != null && 
        !((String) suchname.getValue()).isEmpty() && diff == DIFFERENZ.EGAL)
    {
      String name = (String) suchname.getValue();
      DBIterator<Mitgliedskonto> sollbuchungen = Einstellungen.getDBService()
          .createList(Mitgliedskonto.class);
      if (!umwandeln)
      {
        // Der Name kann so verwendet werden ohne Umwandeln der Umlaute
        sollbuchungen.join("mitglied");
        sollbuchungen.addFilter("mitglied.id = mitgliedskonto.mitglied");
        sollbuchungen.addFilter("((lower(mitglied.name) like ?)"
            + " OR (lower(mitglied.vorname) like ?))",
            new Object[] {name.toLowerCase() + "%", name.toLowerCase() + "%"});
      }
      else
      {
        // Der Name muss umgewandelt werden, es kann mehrere Matches geben
        ArrayList<BigDecimal> namenids = getNamenIds();
        if (namenids != null)
        {
          int anzahl = namenids.size();
          String querystring = null;
          
          for (int i = 1; i <= anzahl; i++)
          {
            if (anzahl == 1)
            {
              querystring = "(mitgliedskonto.mitglied = ?) ";
            }
            else if (i == 1)
            {
              querystring =  "((mitgliedskonto.mitglied = ?) OR ";
            }
            else if (i < anzahl)
            {
              querystring =  querystring + "(mitgliedskonto.mitglied = ?) OR ";
            }
            else if (i == anzahl)
            {
              querystring =  querystring + "(mitgliedskonto.mitglied = ?)) ";
            }
          }
          sollbuchungen.addFilter(querystring, namenids.toArray() );
        }
      }

      if (vd != null)
      {
        sollbuchungen.addFilter("(mitgliedskonto.datum >= ?) ",
            new Object[] { vd });
      }
      if (bd != null)
      {
        sollbuchungen.addFilter("(mitgliedskonto.datum <= ?) ",
            new Object[] { bd });
      }
      return sollbuchungen;
    }
    
    // Eine Differenz ist ausgewählt
    final DBService service = Einstellungen.getDBService();
    String sql = "SELECT  mitgliedskonto.id, mitglied.name, mitglied.vorname, "
        + " mitgliedskonto.betrag, sum(buchung.betrag) FROM mitgliedskonto "
        + "JOIN mitglied on (mitgliedskonto.mitglied = mitglied.id) "
        + "LEFT JOIN buchung on mitgliedskonto.id = buchung.mitgliedskonto ";
    String where = "";
    ArrayList<Object> param = new ArrayList<>();
    if (suchname != null && suchname.getValue() != null && 
        !((String) suchname.getValue()).isEmpty() && umwandeln == false)
    {
      // Der Name kann so verwendet werden ohne Umwandeln der Umlaute
      String tmpSuchname = (String) suchname.getValue();
      where += (where.length() > 0 ? "and " : "")
          + "((lower(mitglied.name) like ?) OR (lower(mitglied.vorname) like ?)) ";
      param.add(tmpSuchname.toLowerCase() + "%");
      param.add(tmpSuchname.toLowerCase() + "%");
    }
    else if (suchname != null && suchname.getValue() != null && 
        !((String) suchname.getValue()).isEmpty() && umwandeln == true)
    {
      // Der Name muss umgewandelt werden, es kann mehrere Matches geben
      ArrayList<BigDecimal> namenids = getNamenIds();
      if (namenids != null)
      {
        int count = 0;
        int anzahl = namenids.size();
        for (BigDecimal id: namenids)
        {
          count++;
          if (anzahl == 1)
          {
          where += (where.length() > 0 ? "and " : "")
              + "mitgliedskonto.mitglied = ? ";
          }
          else if (count == 1)
          {
            where += (where.length() > 0 ? "and " : "")
                + "(mitgliedskonto.mitglied = ? ";
          }
          else if (count < anzahl)
          {
            where += " OR mitgliedskonto.mitglied = ? ";
          }
          else if (count == anzahl)
          {
            where += " OR mitgliedskonto.mitglied = ?) ";
          }
          param.add(id);
        }
      }
    }
    if (vd != null)
    {
      where += (where.length() > 0 ? "and " : "")
          + "mitgliedskonto.datum >= ? ";
      param.add(vd);
    }
    if (bd != null)
    {
      where += (where.length() > 0 ? "and " : "")
          + "mitgliedskonto.datum <= ? ";
      param.add(bd);
    }
    if (where.length() > 0)
    {
      sql += "WHERE " + where;
    }
    sql += "group by mitgliedskonto.id ";

    if (DIFFERENZ.FEHLBETRAG == diff)
    {
      sql += "having sum(buchung.betrag) < mitgliedskonto.betrag or "
          + "(sum(buchung.betrag) is null and mitgliedskonto.betrag > 0) ";
    }
    if (DIFFERENZ.UEBERZAHLUNG == diff)
    {
      sql += "having sum(buchung.betrag) > mitgliedskonto.betrag ";
    }
    sql += "order by mitglied.name, mitglied.vorname, mitgliedskonto.datum desc";
    @SuppressWarnings("unchecked")
    ArrayList<Mitgliedskonto> mitgliedskonten = (ArrayList<Mitgliedskonto>) service.execute(sql,
        param.toArray(), new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs)
          throws RemoteException, SQLException
      {
        ArrayList<Mitgliedskonto> list = new ArrayList<>();
        while (rs.next())
        {
          list.add(
            (Mitgliedskonto) service.createObject(Mitgliedskonto.class, rs.getString(1)));
        }
        return list;
      }
    });
    
    return PseudoIterator.fromArray(
        mitgliedskonten.toArray(new GenericObject[mitgliedskonten.size()]));
  }
  
  private ArrayList<BigDecimal> getNamenIds() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    String sql = "SELECT  mitglied.id, mitglied.name, mitglied.vorname from mitglied";
    
    @SuppressWarnings("unchecked")
    ArrayList<BigDecimal> mitgliedids = (ArrayList<BigDecimal>) service.execute(sql,
        new Object[] { }, new ResultSetExtractor()
        {
          @Override
          public Object extract(ResultSet rs)
              throws RemoteException, SQLException
          {
            ArrayList<BigDecimal> ergebnis = new ArrayList<>();

            // In case the text search input is used, we calculate
            // an "equality" score for each Mitglied entry. 
            // Only the entries with
            // score == maxScore will be shown.
            Integer maxScore = 0;
            int count = 0;
            String name = reduceWord((String) suchname.getValue());
            BigDecimal mgid = null;
            String nachname = null;
            String vorname = null;
            while (rs.next())
            {
              count++;
              // Nur die ids der Mitglieder speichern
              mgid = rs.getBigDecimal(1);

              StringTokenizer tok = new StringTokenizer(name, " ,-");
              Integer score = 0;
              nachname = reduceWord(rs.getString(2));
              vorname = reduceWord(rs.getString(3));                
              while (tok.hasMoreElements())
              {
                String nextToken = tok.nextToken();
                if (nextToken.length() > 2)
                {
                  score += scoreWord(nextToken, nachname);
                  score += scoreWord(nextToken, vorname);
                }
              }

              if (maxScore < score)
              {
                maxScore = score;
                // We found a Mitgliedskonto matching with a higher equality
                // score, so we drop all previous matches, because they were
                // less equal.
                ergebnis.clear();
              }
              else if (maxScore > score)
              {
                // This match is worse, so skip it.
                continue;
              }
              ergebnis.add(mgid);
            }
            if (ergebnis.size() != count)
            {
              return ergebnis;
            }
            else
            {
              // Kein Match
              return null;
            }
          }
        });
    return mitgliedids;
  }

  public Integer scoreWord(String word, String in)
  {
    Integer wordScore = 0;
    StringTokenizer tok = new StringTokenizer(in, " ,-");

    while (tok.hasMoreElements())
    {
      String nextToken = tok.nextToken();

      // Full match is twice worth
      if (nextToken.equals(word))
      {
        wordScore += 2;
      }
      else if (nextToken.contains(word))
      {
        wordScore += 1;
      }
    }

    return wordScore;
  }

  public String reduceWord(String word)
  {
    // We replace "ue" -> "u" and "ü" -> "u", because some bank institutions
    // remove the dots "ü" -> "u". So we get "u" == "ü" == "ue".
    return word.toLowerCase().replaceAll("ä", "a").replaceAll("ae", "a")
        .replaceAll("ö", "o").replaceAll("oe", "o").replaceAll("ü", "u")
        .replaceAll("ue", "u").replaceAll("ß", "s").replaceAll("ss", "s");
  }

  public Button getStartRechnungButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          generiereRechnung(currentObject);
        }
        catch (RemoteException e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
        catch (IOException e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  public Button getStartKontoauszugButton(final Object currentObject,
      final DateInput von, final DateInput bis)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDefaults();
          new Kontoauszug(currentObject, (Date) von.getValue(), (Date) bis.getValue());
        }
        catch (Exception e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private void generiereRechnung(Object currentObject) throws IOException
  {
    Ausgabeart aa = (Ausgabeart) getAusgabeart().getValue();
    settings.setAttribute("ausgabeart", aa.toString());
    Ausgabesortierung as = (Ausgabesortierung) getAusgabesortierung()
        .getValue();
    settings.setAttribute("ausgabesortierung", as.getKey());
    settings.setAttribute(TYP.RECHNUNG.name() + ".mail.betreff",
        (String) getBetreff(TYP.RECHNUNG.name()).getValue());
    settings.setAttribute(TYP.RECHNUNG.name() + ".mail.text",
        (String) getTxt(TYP.RECHNUNG.name()).getValue());
    new Rechnungsausgabe(this);
  }

  public Button getStartMahnungButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          generiereMahnung(currentObject);
        }
        catch (RemoteException e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
        catch (IOException e)
        {
          Logger.error("", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private void generiereMahnung(Object currentObject) throws IOException
  {
    Ausgabeart aa = (Ausgabeart) getAusgabeart().getValue();
    settings.setAttribute("ausgabeart", aa.toString());
    settings.setAttribute(TYP.MAHNUNG.name() + ".mail.betreff",
        (String) getBetreff(TYP.MAHNUNG.name()).getValue());
    settings.setAttribute(TYP.MAHNUNG.name() + ".mail.text",
        (String) getTxt(TYP.MAHNUNG.name()).getValue());
    new Mahnungsausgabe(this);
  }

  private class FilterListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      if (event.type == SWT.Selection || event.type != SWT.FocusOut)
      {
        try
        {
          getMitgliedskontoList(action, null, umwandeln);
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    }
  }
  
  // Für Sollbuchungen View
  public void refreshMitgliedskontoList()
  {
    try
    {
      settings.setAttribute("sollbuchung.suchname", getSuchName().getValue().toString());
      getMitgliedskontoList(action, null, umwandeln);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }
  
  // Für SollbuchungAuswahlDialog
  public void refreshMitgliedskontoList1()
  {
    try
    {
      refreshMitgliedkonto1();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }
  
  // Für SollbuchungAuswahlDialog
  public void refreshMitgliedskontoList2()
  {
    try
    {
      refreshMitgliedkonto2();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }

  public static class MitgliedskontoTreeFormatter implements TreeFormatter
  {

    @Override
    public void format(TreeItem item)
    {
      MitgliedskontoNode mkn = (MitgliedskontoNode) item.getData();
      switch (mkn.getType())
      {
        case MitgliedskontoNode.MITGLIED:
          item.setImage(0, SWTUtil.getImage("user.png"));
          break;
        case MitgliedskontoNode.SOLL:
          item.setImage(0, SWTUtil.getImage("calculator.png"));
          item.setExpanded(false);
          break;
        case MitgliedskontoNode.IST:
          item.setImage(0, SWTUtil.getImage("euro-sign.png"));
          break;
      }
    }
  }

  /**
   * Wird benachrichtigt um die Anzeige zu aktualisieren.
   */
  private class MitgliedskontoMessageConsumer implements MessageConsumer
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
      return new Class[] { MitgliedskontoMessage.class };
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
            if (mitgliedskontoTree == null)
            {
              // Eingabe-Feld existiert nicht. Also abmelden
              Application.getMessagingFactory().unRegisterMessageConsumer(
                  MitgliedskontoMessageConsumer.this);
              return;
            }

            MitgliedskontoMessage msg = (MitgliedskontoMessage) message;
            Mitglied mitglied = (Mitglied) msg.getObject();
            mitgliedskontoTree.setRootObject(new MitgliedskontoNode(mitglied));
          }
          catch (Exception e)
          {
            // Wenn hier ein Fehler auftrat, deregistrieren wir uns wieder
            Logger.error("unable to refresh saldo", e);
            Application.getMessagingFactory()
                .unRegisterMessageConsumer(MitgliedskontoMessageConsumer.this);
          }
        }

      });
    }
  }

}
