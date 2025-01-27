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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Messaging.MitgliedskontoMessage;
import de.jost_net.JVerein.Queries.SollbuchungQuery;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.BuchungPartBearbeitenMenu;
import de.jost_net.JVerein.gui.menu.MitgliedskontoMenu;
import de.jost_net.JVerein.gui.menu.SollbuchungPositionMenu;
import de.jost_net.JVerein.gui.parts.BuchungListPart;
import de.jost_net.JVerein.gui.parts.SollbuchungListTablePart;
import de.jost_net.JVerein.gui.parts.SollbuchungPositionListPart;
import de.jost_net.JVerein.gui.view.BuchungView;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.gui.view.SollbuchungPositionView;
import de.jost_net.JVerein.io.Kontoauszug;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
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

public class MitgliedskontoControl extends DruckMailControl
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

  // SollbuchungDetailView
  private DateInput datum = null;

  private TextAreaInput zweck1;

  private SelectInput zahlungsweg;

  private DecimalInput betrag;

  private AbstractInput mitglied;
  
  private AbstractInput zahler;

  private Mitgliedskonto mkto;

  private TreePart mitgliedskontoTree;

  // SollbuchungListeView, SollbuchungAuswahldialog
  private TablePart mitgliedskontoList;

  private TablePart mitgliedskontoList2;

  private TextInput suchname2 = null;

  private CheckboxInput spezialsuche2 = null;

  // private CheckboxInput offenePosten = null;

  private MitgliedskontoMessageConsumer mc = null;

  private Action action;

  private boolean umwandeln;

  private TablePart buchungList;

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
    zweck1.setHeight(30);
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
    boolean mitVollzahler = false;
    if (getMitglied().getValue() != null
        && ((Mitglied) getMitglied().getValue()).getZahlerID() != null)
      mitVollzahler = true;
    ArrayList<Zahlungsweg> weg = Zahlungsweg.getArray(mitVollzahler);

    zahlungsweg = new SelectInput(weg,
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
    betrag.setEnabled(false);
    return betrag;
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

  // Für SollbuchungAuswahlDialog
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

  public void handleStore()
  {
    try
    {
      Mitgliedskonto mkto = getMitgliedskonto();
      if (mkto.isNewObject())
      {
        if (getMitglied().getValue() != null)
        {
          mkto.setMitglied((Mitglied) getMitglied().getValue());
        }
        else
        {
          throw new ApplicationException("Bitte Mitglied eingeben");
        }
      }

      if (getZahler().getValue() == null)
      {
        throw new ApplicationException("Zahler fehlt");
      }

      if (mkto.getRechnung() != null)
        throw new ApplicationException(
            "Sollbuchung kann nicht geändert werden, es existiert eine Rechnung darüber.");
      mkto.setZahlerId(getSelectedZahlerId());
      mkto.setBetrag((Double) getBetrag().getValue());
      mkto.setDatum((Date) getDatum().getValue());
      Zahlungsweg zw = (Zahlungsweg) getZahlungsweg().getValue();
      mkto.setZahlungsweg(zw.getKey());
      mkto.setZweck1((String) getZweck1().getValue());

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
        new Action()
        {

          @Override
          public void handleAction(Object context) throws ApplicationException
          {
            if (context == null || !(context instanceof MitgliedskontoNode))
            {
              return;
            }
            try
            {
              MitgliedskontoNode mkn = (MitgliedskontoNode) context;
              if (mkn.getType() == MitgliedskontoNode.IST)
              {
                Buchung bu = (Buchung) Einstellungen.getDBService()
                    .createObject(Buchung.class, mkn.getID());
                GUI.startView(BuchungView.class.getName(), bu);
              }
              if (mkn.getType() == MitgliedskontoNode.SOLL)
              {
                Mitgliedskonto mk = (Mitgliedskonto) Einstellungen
                    .getDBService()
                    .createObject(Mitgliedskonto.class, mkn.getID());
                GUI.startView(
                    new SollbuchungDetailView(), mk);
              }
            }
            catch (RemoteException e)
            {
              Logger.error(e.getMessage());
              throw new ApplicationException(
                  "Fehler beim Editieren der Buchung");
            }
          }
        })

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

  public TablePart getMitgliedskontoList(Action action, ContextMenu menu,
      boolean umwandeln) throws RemoteException, ApplicationException
  {
    this.action = action;
    this.umwandeln = umwandeln;
    @SuppressWarnings("rawtypes")
    GenericIterator mitgliedskonten = new SollbuchungQuery(this, umwandeln,
        null).get();
    if (mitgliedskontoList == null)
    {
      mitgliedskontoList = new SollbuchungListTablePart(mitgliedskonten,
          action);
      mitgliedskontoList.addColumn("Nr", "id-int");
      mitgliedskontoList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      mitgliedskontoList.addColumn("Abrechnungslauf", "abrechnungslauf");
      mitgliedskontoList.addColumn("Mitglied", "mitglied");
      mitgliedskontoList.addColumn("Zahler", "zahler");
      mitgliedskontoList.addColumn("Zweck", "zweck1");
      mitgliedskontoList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      mitgliedskontoList.addColumn("Zahlungsweg","zahlungsweg", new Formatter() {
        @Override
        public String format(Object o)
        {
          return new Zahlungsweg((Integer)o).getText();
        }
      });
      mitgliedskontoList.addColumn("Zahlungseingang", "istsumme",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      mitgliedskontoList.addColumn("Rechnung", "rechnung");
      mitgliedskontoList.setContextMenu(menu);
      mitgliedskontoList.setRememberColWidths(true);
      mitgliedskontoList.setRememberOrder(true);
      mitgliedskontoList.setMulti(true);
      mitgliedskontoList.addFeature(new FeatureSummary());
    }
    else
    {
      mitgliedskontoList.removeAll();
      if (mitgliedskonten != null)
      {
        while (mitgliedskonten.hasNext())
        {
          mitgliedskontoList.addItem(mitgliedskonten.next());
        }
      }
      mitgliedskontoList.sort();
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
      mitgliedskontoList2.sort();
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
    mitgliedskontoList2.sort();
  }

  public Part getSollbuchungPositionListPart(boolean hasRechnung) throws RemoteException
  {
    if (buchungList != null)
    {
      return buchungList;
    }
    ArrayList<SollbuchungPosition> list = getMitgliedskonto()
        .getSollbuchungPositionList();

    if (hasRechnung)
    {
      buchungList = new SollbuchungPositionListPart(list, null);
    }
    else
    {
      buchungList = new SollbuchungPositionListPart(list,
          new EditAction(SollbuchungPositionView.class));
    }

    buchungList.setRememberColWidths(true);
    if (!hasRechnung)
    {
      buchungList.setContextMenu(new SollbuchungPositionMenu());
    }
    return buchungList;
  }

  public Part getBuchungListPart() throws RemoteException
  {
    return new BuchungListPart(getMitgliedskonto().getBuchungList(),
        new BuchungAction(false), new BuchungPartBearbeitenMenu());
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

  public void refreshMitgliedkonto1() throws RemoteException, ApplicationException
  {
    @SuppressWarnings("rawtypes")
    GenericIterator mitgliedskonten = new SollbuchungQuery(this, umwandeln,
        null).get();
    mitgliedskontoList.removeAll();
    if (mitgliedskonten != null)
    {
      while (mitgliedskonten.hasNext())
      {
        mitgliedskontoList.addItem(mitgliedskonten.next());
      }
    }
    mitgliedskontoList.sort();
  }

  public Button getStartKontoauszugButton(final Object currentObject,
      final MitgliedskontoControl control)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();
          new Kontoauszug(currentObject, control);
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

  // Für Sollbuchungen View
  public void TabRefresh()
  {
    if (mitgliedskontoList != null)
    {
      try
      {
        getMitgliedskontoList(action, null, umwandeln);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
      catch (ApplicationException e)
      {
        GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
      }
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
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());;
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

  @Override
  public String getInfoText(Object selection)
  {
    Mitglied[] mitglieder = null;
    String text = "";

    if (selection instanceof Mitglied)
    {
      mitglieder = new Mitglied[] { (Mitglied) selection };
    }
    else if (selection instanceof Mitglied[])
    {
      mitglieder = (Mitglied[]) selection;
    }
    else
    {
      return "";
    }

    try
    {
      // Aufruf aus Mitglieder View
      if (mitglieder != null)
      {
        text = "Es wurden " + mitglieder.length + " Mitglieder ausgewählt"
            + "\nFolgende Mitglieder haben keine Mailadresse:";
        for (Mitglied m : mitglieder)
        {
          if (m.getEmail() == null || m.getEmail().isEmpty())
          {
            text = text + "\n - " + m.getName() + ", " + m.getVorname();
          }
        }
      }
    }
    catch (Exception ex)
    {
      GUI.getStatusBar().setErrorText("Fehler beim Ermitteln der Info");
    }
    return text;
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    if (getMitgliedskonto().getMitglied() != null)
    {
      Mitglied[] mitgliedArray = { getMitgliedskonto().getMitglied() };
      mitglied = new SelectInput(mitgliedArray,
          getMitgliedskonto().getMitglied());
      mitglied.setEnabled(false);
    }
    else
    {
      mitglied = new MitgliedInput().getMitgliedInput(mitglied, null,
          Einstellungen.getEinstellung().getMitgliedAuswahl());
      mitglied.addListener(new MitgliedListener());
    }
    mitglied.setMandatory(true);
    return mitglied;
  }
  
  public Input getZahler() throws RemoteException
  {
    if (zahler != null)
    {
      return zahler;
    }
    zahler = new MitgliedInput().getMitgliedInput(zahler,
        getMitgliedskonto().getZahler(),
        Einstellungen.getEinstellung().getMitgliedAuswahl());
    zahler.setMandatory(true);
    return zahler;
  }

  private Long getSelectedZahlerId() throws ApplicationException
  {
    try
    {
      if (zahler == null)
      {
        return null;
      }
      Mitglied derZahler = (Mitglied) getZahler().getValue();
      if (null == derZahler)
      {
        return null;
      }
      return Long.valueOf(derZahler.getID());
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählter Zahler kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public class MitgliedListener implements Listener
  {

    MitgliedListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      try
      {
        @SuppressWarnings("unchecked")
        ArrayList<Zahlungsweg> list = (ArrayList<Zahlungsweg>) getZahlungsweg()
            .getList();
        list.remove(new Zahlungsweg(Zahlungsweg.VOLLZAHLER));
        Mitglied m = (Mitglied) getMitglied().getValue();
        Mitglied z = (Mitglied) getZahler().getValue();
        if (m.getZahlerID() != null)
        {
          list.add(new Zahlungsweg(Zahlungsweg.VOLLZAHLER));
          if (z == null)
          {
            getZahler().setValue(m.getZahler());
          }
        }
        else
        {
          if (z == null)
          {
            getZahler().setValue(getMitglied().getValue());
          }
        }
        getZahlungsweg().setList(list);
      }
      catch (RemoteException e)
      {
        e.printStackTrace();
      }
    }
  }

  public boolean hasRechnung() throws RemoteException
  {
    if (getMitgliedskonto().getRechnung() != null)
    {
      GUI.getStatusBar().setErrorText(
          "Sollbuchung kann nicht bearbeitet werden. Es wurde bereits eine Rechnung über diese Sollbuchung erstellt.");
      return true;
    }
    return false;
  }

  public Object[] getCVSExportGrenzen() throws RemoteException
  {
    return new Object[] { getSuchname().getValue(), getDifferenz().getValue(),
        getOhneAbbucher().getValue(), getDatumvon().getValue(),
        getDatumbis().getValue(), getMailauswahl().getValue() };
  }

}
