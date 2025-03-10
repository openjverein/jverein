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
import java.io.FileInputStream;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungPrintAction;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.BuchungPartAnzeigenMenu;
import de.jost_net.JVerein.gui.menu.SpendenbescheinigungMenu;
import de.jost_net.JVerein.gui.parts.BuchungListPart;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.io.SpendenbescheinigungExportCSV;
import de.jost_net.JVerein.io.SpendenbescheinigungExportPDF;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.keys.SuchSpendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.SpbAdressaufbereitung;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class SpendenbescheinigungControl extends DruckMailControl
{

  // Spendenbescheinigung View
  private TablePart spbList;

  private SelectInput spendenart;

  private AbstractInput mitglied;
  
  private TextInput zeile1;

  private TextInput zeile2;

  private TextInput zeile3;

  private TextInput zeile4;

  private TextInput zeile5;

  private TextInput zeile6;

  private TextInput zeile7;

  private DateInput spendedatum;

  private DateInput bescheinigungsdatum;

  private DecimalInput betrag;

  private FormularInput formular;

  private CheckboxInput ersatzaufwendungen;

  private TextInput bezeichnungsachzuwendung;

  private SelectInput herkunftspende;

  private CheckboxInput unterlagenwertermittlung;

  private Spendenbescheinigung spendenbescheinigung;
  
  private boolean and = false;

  private String sql = "";

  private boolean editable = false;

  final static String ExportPDF = "PDF";

  final static String ExportCSV = "CSV";

  public SpendenbescheinigungControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public void setEditable()
  {
    editable = true;
  }

  public Spendenbescheinigung getSpendenbescheinigung()
  {
    if (spendenbescheinigung != null)
    {
      return spendenbescheinigung;
    }
    spendenbescheinigung = (Spendenbescheinigung) getCurrentObject();
    return spendenbescheinigung;
  }

  public SelectInput getSpendenart() throws RemoteException
  {
    if (spendenart != null)
    {
      return spendenart;
    }
    spendenart = new SelectInput(Spendenart.getArray(),
        new Spendenart(getSpendenbescheinigung().getSpendenart()));
    spendenart.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        enableSachspende();
      }
    });
    spendenart.setEnabled(false);
    return spendenart;
  }

  private void enableSachspende()
  {
    try
    {
      if (!getSpendenbescheinigung().isNewObject())
      {
        getBezeichnungSachzuwendung().setEnabled(false);
        getHerkunftSpende().setEnabled(false);
        getUnterlagenWertermittlung().setEnabled(false);
      }
      else
      {
      Spendenart spa = (Spendenart) getSpendenart().getValue();
      getBezeichnungSachzuwendung()
          .setEnabled(spa.getKey() == Spendenart.SACHSPENDE);
      getHerkunftSpende().setEnabled(spa.getKey() == Spendenart.SACHSPENDE);
      getUnterlagenWertermittlung()
          .setEnabled(spa.getKey() == Spendenart.SACHSPENDE);
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
  }
  
  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    Mitglied m = getSpendenbescheinigung().getMitglied();
    mitglied = new MitgliedInput().getMitgliedInput(mitglied, m,
        Einstellungen.getEinstellung().getMitgliedAuswahl());
    mitglied.addListener(new MitgliedListener());
    if (mitglied instanceof SelectInput)
    {
      ((SelectInput) mitglied).setPleaseChoose("Optional auswählen");
      if (m == null)
      {
        ((SelectInput) mitglied).setPreselected(null);
      }
    }
    if (m != null || !editable)
    {
      mitglied.disable();
    }
    return mitglied;
  }

  public TextInput getZeile1(boolean withFocus) throws RemoteException
  {
    if (zeile1 != null)
    {
      return zeile1;
    }
    zeile1 = new TextInput(getSpendenbescheinigung().getZeile1(), 80);
    if (withFocus)
    {
      zeile1.focus();
    }
    zeile1.setEnabled(editable);
    return zeile1;
  }

  public TextInput getZeile2() throws RemoteException
  {
    if (zeile2 != null)
    {
      return zeile2;
    }
    zeile2 = new TextInput(getSpendenbescheinigung().getZeile2(), 80);
    zeile2.setEnabled(editable);
    return zeile2;
  }

  public TextInput getZeile3() throws RemoteException
  {
    if (zeile3 != null)
    {
      return zeile3;
    }
    zeile3 = new TextInput(getSpendenbescheinigung().getZeile3(), 80);
    zeile3.setEnabled(editable);
    return zeile3;
  }

  public TextInput getZeile4() throws RemoteException
  {
    if (zeile4 != null)
    {
      return zeile4;
    }
    zeile4 = new TextInput(getSpendenbescheinigung().getZeile4(), 80);
    zeile4.setEnabled(editable);
    return zeile4;
  }

  public TextInput getZeile5() throws RemoteException
  {
    if (zeile5 != null)
    {
      return zeile5;
    }
    zeile5 = new TextInput(getSpendenbescheinigung().getZeile5(), 80);
    zeile5.setEnabled(editable);
    return zeile5;
  }

  public TextInput getZeile6() throws RemoteException
  {
    if (zeile6 != null)
    {
      return zeile6;
    }
    zeile6 = new TextInput(getSpendenbescheinigung().getZeile6(), 80);
    zeile6.setEnabled(editable);
    return zeile6;
  }

  public TextInput getZeile7() throws RemoteException
  {
    if (zeile7 != null)
    {
      return zeile7;
    }
    zeile7 = new TextInput(getSpendenbescheinigung().getZeile7(), 80);
    zeile7.setEnabled(editable);
    return zeile7;
  }

  public DateInput getSpendedatum() throws RemoteException
  {
    if (spendedatum != null)
    {
      return spendedatum;
    }
    spendedatum = new DateInput(getSpendenbescheinigung().getSpendedatum());
    spendedatum.setEnabled(editable);
    return spendedatum;
  }

  public DateInput getBescheinigungsdatum() throws RemoteException
  {
    if (bescheinigungsdatum != null)
    {
      return bescheinigungsdatum;
    }
    bescheinigungsdatum = new DateInput(
        getSpendenbescheinigung().getBescheinigungsdatum());
    bescheinigungsdatum.setEnabled(editable);
    return bescheinigungsdatum;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getSpendenbescheinigung().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    if (getSpendenbescheinigung().getSpendenart() == Spendenart.GELDSPENDE)
    {
      betrag.setEnabled(false);
    }
    else
    {
      betrag.setEnabled(editable);
    }
    return betrag;
  }

  public SelectInput getFormular() throws RemoteException
  {
    if (formular != null)
    {
      return formular;
    }
    String def = null;
    if (getSpendenbescheinigung().getFormular() != null)
    {
      def = getSpendenbescheinigung().getFormular().getID();
    }
    if (getSpendenbescheinigung().getBuchungen() != null
        && getSpendenbescheinigung().getBuchungen().size() > 1)
    {
      formular = new FormularInput(FormularArt.SAMMELSPENDENBESCHEINIGUNG, def);
    }
    else
    {
      formular = new FormularInput(FormularArt.SPENDENBESCHEINIGUNG, def);
    }
    formular.setPleaseChoose("Standard");
    return formular;
  }

  public CheckboxInput getErsatzAufwendungen() throws RemoteException
  {
    if (ersatzaufwendungen != null)
    {
      return ersatzaufwendungen;
    }
    List<Buchung> buchungen = getSpendenbescheinigung().getBuchungen();
    boolean check = false;
    if (buchungen != null && buchungen.size() == 1)
    {
      // Es ist keine Sachspende und keine Sammelspendenbescheinigung
      if (getSpendenbescheinigung().getAutocreate())
      {
        // Verzicht aus Buchung lesen
        check = buchungen.get(0).getVerzicht();
      }
      else
      {
        // Wegen Kompabilität zu früher
        check = getSpendenbescheinigung().getErsatzAufwendungen();
      }
    }
    ersatzaufwendungen = new CheckboxInput(check);
    if (buchungen != null && buchungen.size() > 1)
    {
      // Sammelspendenbescheinigung
      ersatzaufwendungen.setName("*siehe Buchungsliste");
    }
    ersatzaufwendungen.disable();
    return ersatzaufwendungen;
  }

  public TextInput getBezeichnungSachzuwendung() throws RemoteException
  {
    if (bezeichnungsachzuwendung != null)
    {
      return bezeichnungsachzuwendung;
    }
    bezeichnungsachzuwendung = new TextInput(
        getSpendenbescheinigung().getBezeichnungSachzuwendung(), 100);
    enableSachspende();
    return bezeichnungsachzuwendung;
  }

  public SelectInput getHerkunftSpende() throws RemoteException
  {
    if (herkunftspende != null)
    {
      return herkunftspende;
    }
    herkunftspende = new SelectInput(HerkunftSpende.getArray(),
        new HerkunftSpende(getSpendenbescheinigung().getHerkunftSpende()));
    enableSachspende();
    return herkunftspende;
  }

  public CheckboxInput getUnterlagenWertermittlung() throws RemoteException
  {
    if (unterlagenwertermittlung != null)
    {
      return unterlagenwertermittlung;
    }
    unterlagenwertermittlung = new CheckboxInput(
        getSpendenbescheinigung().getUnterlagenWertermittlung());
    enableSachspende();
    return unterlagenwertermittlung;
  }

  public Part getBuchungListPart() throws RemoteException
  {
    return new BuchungListPart(getSpendenbescheinigung().getBuchungen(),
        new BuchungAction(false), new BuchungPartAnzeigenMenu());
  }

  /**
   * This method stores the project using the current values.
   */
  public void handleStore()
  {
    try
    {
      Spendenbescheinigung spb = getSpendenbescheinigung();
      Spendenart spa = (Spendenart) getSpendenart().getValue();
      spb.setSpendenart(spa.getKey());
      spb.setZeile1((String) getZeile1(false).getValue());
      spb.setZeile2((String) getZeile2().getValue());
      spb.setZeile3((String) getZeile3().getValue());
      spb.setZeile4((String) getZeile4().getValue());
      spb.setZeile5((String) getZeile5().getValue());
      spb.setZeile6((String) getZeile6().getValue());
      spb.setZeile7((String) getZeile7().getValue());
      spb.setSpendedatum((Date) getSpendedatum().getValue());
      spb.setBescheinigungsdatum((Date) getBescheinigungsdatum().getValue());
      spb.setBetrag((Double) getBetrag().getValue());
      spb.setErsatzAufwendungen((Boolean) getErsatzAufwendungen().getValue());
      spb.setBezeichnungSachzuwendung(
          (String) getBezeichnungSachzuwendung().getValue());
      spb.setFormular((Formular) getFormular().getValue());
      HerkunftSpende hsp = (HerkunftSpende) getHerkunftSpende().getValue();
      spb.setHerkunftSpende(hsp.getKey());
      spb.setUnterlagenWertermittlung(
          (Boolean) getUnterlagenWertermittlung().getValue());
      spb.store();

      GUI.getStatusBar().setSuccessText("Spendenbescheinigung gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei Speichern der Spendenbescheinigung";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Button getDruckUndMailButton()
  {
  
    Button b = new Button("Druck und Mail", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Spendenbescheinigung spb = getSpendenbescheinigung();
        try
        {
          if (spb.isNewObject())
          {
            GUI.getStatusBar()
            .setErrorText("Spendenbescheinigung bitte erst speichern!");
            return;
          }
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler bei der Aufbereitung der Spendenbescheinigung");
        }
        GUI.startView(SpendenbescheinigungMailView.class, 
            new Spendenbescheinigung[] { (Spendenbescheinigung) spb });
      }
    }, getSpendenbescheinigung(), false, "document-print.png");
    return b;
  }

  public Part getSpendenbescheinigungList() throws RemoteException
  {
    if (spbList != null)
    {
      return spbList;
    }
    spbList = new TablePart(getSpendenbescheinigungen(),
        new SpendenbescheinigungAction(Spendenart.SACHSPENDE));
    spbList.addColumn("Nr", "id-int");
    spbList.addColumn("Spender", "mitglied");
    spbList.addColumn("Spendenart", "spendenart", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        return new Spendenart((Integer) o).getText();
      }
    });
    spbList.addColumn("Bescheinigungsdatum", "bescheinigungsdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    spbList.addColumn("Spendedatum", "spendedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    spbList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    spbList.addColumn("Zeile 1", "zeile1");
    spbList.addColumn("Zeile 2", "zeile2");
    spbList.addColumn("Zeile 3", "zeile3");
    spbList.addColumn("Zeile 4", "zeile4");
    spbList.addColumn("Zeile 5", "zeile5");
    spbList.addColumn("Zeile 6", "zeile6");
    spbList.addColumn("Zeile 7", "zeile7");

    spbList.setRememberColWidths(true);
    spbList.setContextMenu(new SpendenbescheinigungMenu());
    spbList.setRememberOrder(true);
    spbList.addFeature(new FeatureSummary());
    spbList.setMulti(true);
    return spbList;
  }

  public void TabRefresh()
  {
    if (spbList != null)
    {
      try
      {
        spbList.removeAll();
        for (Spendenbescheinigung spb : getSpendenbescheinigungen())
        {
          spbList.addItem(spb);
        }
        spbList.sort();
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private ArrayList<Spendenbescheinigung> getSpendenbescheinigungen()
      throws RemoteException
  {
    SuchSpendenart suchSpendenart = SuchSpendenart.ALLE;
    if (isSuchSpendenartAktiv())
    {
      suchSpendenart = (SuchSpendenart) getSuchSpendenart().getValue();
    }
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<Long> queryIds = querySpendenbescheinigungen(suchSpendenart);

    // Bei GELDSPENDE_ECHT liefert das Query auch Splittbuchungen die neben
    // echten Geldbuchungen auch Geldbuchungen mit Verzicht haben.
    // Darum lesen wir nochmal mit ERSTATTUNGSVERZICHT. Wenn eine Id da
    // auch dabei ist dürfen wir die Splittbuchung nicht nehmen
    if (suchSpendenart == SuchSpendenart.GELDSPENDE_ECHT)
    {
      ArrayList<Long> erstattungsIds = querySpendenbescheinigungen(
          SuchSpendenart.ERSTATTUNGSVERZICHT);
      for (Long id : queryIds)
      {
        if (!erstattungsIds.contains(id))
        {
          ids.add(id);
        }
      }
    }
    else
    {
      ids = queryIds;
    }

    if (ids.size() == 0)
      return new ArrayList<Spendenbescheinigung>();

    DBIterator<Spendenbescheinigung> list = Einstellungen.getDBService()
        .createList(Spendenbescheinigung.class);
    list.addFilter("id in (" + StringUtils.join(ids, ",") + ")");
    list.setOrder(" ORDER BY bescheinigungsdatum desc, spendedatum desc ");
    ArrayList<Spendenbescheinigung> spendenbescheinigungen = list != null
        ? (ArrayList<Spendenbescheinigung>) PseudoIterator.asList(list)
        : null;
    return spendenbescheinigungen;
  }

  @SuppressWarnings("unchecked")
  private ArrayList<Long> querySpendenbescheinigungen(
      SuchSpendenart suchSpendenart) throws RemoteException
  {
    final DBService service = Einstellungen.getDBService();
    ArrayList<Object> bedingungen = new ArrayList<>();
    and = false;

    sql = "select DISTINCT spendenbescheinigung.id, bescheinigungsdatum from spendenbescheinigung ";
    int mailauswahl = MailAuswertungInput.ALLE;
    if (isMailauswahlAktiv())
    {
      mailauswahl = (Integer) getMailauswahl().getValue();
      if (mailauswahl != MailAuswertungInput.ALLE)
      {
        sql += "left join mitglied on (spendenbescheinigung.mitglied = mitglied.id) ";
      }
    }
    if (suchSpendenart != SuchSpendenart.ALLE
        && suchSpendenart != SuchSpendenart.GELDSPENDE
        && suchSpendenart != SuchSpendenart.SACHSPENDE)
    {
      sql += "left join buchung on (spendenbescheinigung.id = buchung.spendenbescheinigung) ";
    }

    if (isMailauswahlAktiv())
    {
      if (mailauswahl == MailAuswertungInput.OHNE)
      {
        addCondition("(email is null or length(email) = 0) ");
      }
      if (mailauswahl == MailAuswertungInput.MIT)
      {
        addCondition("(email is not null and length(email) > 0) ");
      }
    }

    if (isSuchSpendenartAktiv())
    {
      switch (suchSpendenart)
      {
        case ALLE:
          break;
        case GELDSPENDE:
          addCondition("spendenart = ?");
          bedingungen.add(Spendenart.GELDSPENDE);
          break;
        case SACHSPENDE:
          addCondition("spendenart = ?");
          bedingungen.add(Spendenart.SACHSPENDE);
          break;
        case ERSTATTUNGSVERZICHT:
          addCondition("buchung.verzicht = 1");
          break;
        case GELDSPENDE_ECHT:
          addCondition(
              "(buchung.verzicht != 1 or buchung.verzicht is null) AND spendenart = ?");
          bedingungen.add(Spendenart.GELDSPENDE);
          break;
        case SACHSPENDE_ERSTATTUNGSVERZICHT:
          addCondition("(buchung.verzicht = 1 OR spendenart = ?)");
          bedingungen.add(Spendenart.SACHSPENDE);
          break;
        default:
          break;
      }
    }

    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        addCondition("(lower(zeile2) like ?)");
        bedingungen.add("%" + tmpSuchname.toLowerCase() + "%");
      }
    }

    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      addCondition("bescheinigungsdatum >= ?");
      Date d = (Date) getDatumvon().getValue();
      bedingungen.add(new java.sql.Date(d.getTime()));
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      addCondition("bescheinigungsdatum <= ?");
      Date d = (Date) getDatumbis().getValue();
      bedingungen.add(new java.sql.Date(d.getTime()));
    }
    if (isEingabedatumvonAktiv() && getEingabedatumvon().getValue() != null)
    {
      addCondition("spendedatum >= ?");
      Date d = (Date) getEingabedatumvon().getValue();
      bedingungen.add(new java.sql.Date(d.getTime()));
    }
    if (isEingabedatumbisAktiv() && getEingabedatumbis().getValue() != null)
    {
      addCondition("spendedatum <= ?");
      Date d = (Date) getEingabedatumbis().getValue();
      bedingungen.add(new java.sql.Date(d.getTime()));
    }

    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Long> list = new ArrayList<>();
        while (rs.next())
        {
          list.add(rs.getLong(1));
        }
        return list;
      }
    };

    return (ArrayList<Long>) service.execute(sql, bedingungen.toArray(), rs);
  }

  private void addCondition(String condition)
  {
    if (and)
    {
      sql += " AND ";
    }
    else
    {
      sql += "where ";
    }
    and = true;
    sql += condition;
  }

  // Mail/Drucken View
  @Override
  public String getInfoText(Object spbArray)
  {
    Spendenbescheinigung[] spbArr = (Spendenbescheinigung[]) spbArray;
    String text = "Es wurden " + spbArr.length + 
        " Spendenbescheinigungen ausgewählt"
        + "\nFolgende Mitglieder haben keine Mailadresse:";
    try
    {
      for (Spendenbescheinigung spb: spbArr)
      {
        Mitglied m = spb.getMitglied();
        if (m != null && ( m.getEmail() == null || m.getEmail().isEmpty()))
        {
          text = text + "\n - " + m.getName()
              + ", " + m.getVorname();
        }
      }
      text = text 
          + "\nFür folgende Spendenbescheinigungen existiert kein Mitglied und keine Mailadresse:";
      for (Spendenbescheinigung spb: spbArr)
      {
        if (spb.getMitglied() == null)
        {
          text = text  + "\n - " + spb.getZeile1()
              + ", " + spb.getZeile2() + ", " + spb.getZeile3();
        }
      }
    }
    catch (Exception ex)
    {
      GUI.getStatusBar().setErrorText("Fehler beim Ermitteln der Mitglieder aus den Spendenbescheinigungen");
    }
    return text;
  }
  
  public Button getStartButton(final Object currentObject)
  {
    Button button = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        try
        {
          saveDruckMailSettings();
          Spendenbescheinigung[] spbArray = null;
          if (currentObject == null)
          {
            ArrayList<Spendenbescheinigung> spblist = getSpendenbescheinigungen();
            if (spblist.size() == 0)
            {
              GUI.getStatusBar()
              .setSuccessText("Für die gewählten Filterkriterien wurden "
                  + "keine Spendenbescheinigungen gefunden");
              return;
            }
            spbArray = spblist.toArray(new Spendenbescheinigung[spblist.size()]);
          }
          else if (currentObject instanceof Spendenbescheinigung[])
          {
            spbArray = (Spendenbescheinigung[]) currentObject;
          }
          else
          {
            return;
          }
          generatePdf((String) mailtext.getValue(), (Adressblatt) adressblatt.getValue(),
               spbArray, (Ausgabeart) ausgabeart.getValue());
          if ((Ausgabeart) ausgabeart.getValue() == Ausgabeart.MAIL)
          {
            sendeMail((String) mailbetreff.getValue(),
                (String) mailtext.getValue(), spbArray);
          }
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
        }
      }
    }, null, true, "walking.png");
    return button;
  }

  private void generatePdf(String text, Adressblatt adressblatt, 
      Spendenbescheinigung[] spba, Ausgabeart ausgabeart)
      throws ApplicationException
  {
    boolean open = false;
    if (ausgabeart ==  Ausgabeart.DRUCK)
      open = true;
    SpendenbescheinigungPrintAction action = 
        new SpendenbescheinigungPrintAction(text, adressblatt, open);
    action.handleAction(spba);
  }
  
  private void sendeMail(final String betr, final String txt,
      final Spendenbescheinigung[] spba) throws RemoteException
  {

    BackgroundTask t = new BackgroundTask()
    {

      private boolean cancel = false;

      @Override
      public void run(ProgressMonitor monitor)
      {
        try
        {
          MailSender sender = new MailSender(
              Einstellungen.getEinstellung().getSmtpServer(),
              Einstellungen.getEinstellung().getSmtpPort(),
              Einstellungen.getEinstellung().getSmtpAuthUser(),
              Einstellungen.getEinstellung().getSmtpAuthPwd(),
              Einstellungen.getEinstellung().getSmtpFromAddress(),
              Einstellungen.getEinstellung().getSmtpFromAnzeigename(),
              Einstellungen.getEinstellung().getMailAlwaysBcc(),
              Einstellungen.getEinstellung().getMailAlwaysCc(),
              Einstellungen.getEinstellung().getSmtpSsl(),
              Einstellungen.getEinstellung().getSmtpStarttls(),
              Einstellungen.getEinstellung().getMailVerzoegerung(),
              Einstellungen.getImapCopyData());

          Velocity.init();
          Logger.debug("preparing velocity context");
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setPercentComplete(0);

          int sentCount = 0;
          int size = spba.length;
          for (int i=0; i < size; i++)
          {
            if(isInterrupted())
            {
              monitor.setStatus(ProgressMonitor.STATUS_ERROR);
              monitor.setStatusText("Mailversand abgebrochen");
              monitor.setPercentComplete(100);
              return;
            }
            
            double proz = (double) i / (double) size * 100d;
            monitor.setPercentComplete((int) proz);
            Mitglied m = spba[i].getMitglied();
            if (m == null || m.getEmail() == null || m.getEmail().isEmpty())
            {
              continue;
            }
            VelocityContext context = new VelocityContext();
            context.put("dateformat", new JVDateFormatTTMMJJJJ());
            context.put("decimalformat", Einstellungen.DECIMALFORMAT);
            context.put("email", m.getEmail());

            Map<String, Object> map = new MitgliedMap().getMap(m, null);
            map = new AllgemeineMap().getMap(map);
            VarTools.add(context, map);

            StringWriter wtext1 = new StringWriter();
            Velocity.evaluate(context, wtext1, "LOG", betr);

            StringWriter wtext2 = new StringWriter();
            Velocity.evaluate(context, wtext2, "LOG", txt);

            try
            {
              String path = Einstellungen.getEinstellung()
                  .getSpendenbescheinigungverzeichnis();
              if (path == null || path.length() == 0)
              {
                path = settings.getString("lastdir", System.getProperty("user.home"));
              }

              settings.setAttribute("lastdir", path);
              path = path.endsWith(File.separator) ? path : path + File.separator;
              TreeSet<MailAnhang> anhang = new TreeSet<MailAnhang>();
              MailAnhang anh = (MailAnhang) Einstellungen.getDBService()
                  .createObject(MailAnhang.class, null);
              String fileName = new Dateiname(m,
                  spba[i].getSpendedatum(), "Spendenbescheinigung",
                  Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                  "pdf").get();
              anh.setDateiname(fileName);
              fileName = path + fileName;
              File file = new File(fileName);
              FileInputStream fis = new FileInputStream(file);
              byte[] buffer = new byte[(int) file.length()];
              fis.read(buffer);
              anh.setAnhang(buffer);
              anhang.add(anh);
              fis.close();

              try
              {
                sender.sendMail(m.getEmail(), wtext1.getBuffer().toString(),
                    wtext2.getBuffer().toString(), anhang);
              }
              // Wenn eine ApplicationException geworfen wurde, wurde die
              // Mails erfolgreich versendet, erst danach trat ein Fehler auf.
              catch (ApplicationException ae)
              {
                Logger.error("Fehler: ", ae);
                monitor.log(m.getEmail() + " - " + ae.getMessage());
              }
              sentCount++;

              // Mail in die Datenbank schreiben
              Mail mail = (Mail) Einstellungen.getDBService()
                  .createObject(Mail.class, null);
              Timestamp ts = new Timestamp(new Date().getTime());
              mail.setBearbeitung(ts);
              mail.setBetreff(wtext1.getBuffer().toString());
              mail.setTxt(wtext2.getBuffer().toString());
              mail.setVersand(ts);
              mail.store();
              MailEmpfaenger empf = (MailEmpfaenger) Einstellungen
                  .getDBService().createObject(MailEmpfaenger.class, null);
              empf.setMail(mail);
              empf.setMitglied(m);
              empf.setVersand(ts);
              empf.store();
              anh.setMail(mail);
              if (Einstellungen.getEinstellung().getAnhangSpeichern())
              {
                anh.store();
              }

              monitor.log(m.getEmail() + " - versendet");
            }
            catch (Exception e)
            {
              Logger.error("Fehler beim Mailversand", e);
              monitor.log(m.getEmail() + " - " + e.getMessage());
            }
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText(
              String.format("Anzahl verschickter Mails: %d", sentCount));
          GUI.getStatusBar().setSuccessText(
              "Mail" + (sentCount > 1 ? "s" : "") +  " verschickt");
        }
        catch (Exception re)
        {
          Logger.error("", re);
          monitor.log(re.getMessage());
        }
      }

      @Override
      public void interrupt()
      {
        this.cancel = true;
      }

      @Override
      public boolean isInterrupted()
      {
        return this.cancel;
      }
    };
    Application.getController().start(t);
  }
  
  public Button getPDFExportButton()
  {
    Button b = new Button("PDF", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        starteExport(ExportPDF);
      }
    }, null, false, "file-pdf.png");
    // button
    return b;
  }

  public Button getCSVExportButton()
  {
    Button b = new Button("CSV", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        starteExport(ExportCSV);
      }
    }, null, false, "xsd.png");
    // button
    return b;
  }

  private void starteExport(String type) throws ApplicationException
  {
    try
    {
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei wählen.");
      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("spendenbescheinigungen", "",
          Einstellungen.getEinstellung().getDateinamenmuster(), type).get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());
      ArrayList<Spendenbescheinigung> spbList = getSpendenbescheinigungen();
      ausgabe(type, file, spbList);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          String.format("Fehler beim Aufbau des Reports: %s", e.getMessage()));
    }
  }

  private void ausgabe(final String type, final File file,
      final ArrayList<Spendenbescheinigung> spbList)
  {
    BackgroundTask t = new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          switch (type)
          {
            case ExportCSV:
              new SpendenbescheinigungExportCSV(file, spbList);
              break;
            case ExportPDF:
              new SpendenbescheinigungExportPDF(file, spbList, 4);
              break;
          }
          GUI.getCurrentView().reload();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
        }
        FileViewer.show(file);
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
        Mitglied selected = (Mitglied) getMitglied().getValue();
        if (selected != null)
        {
          SpbAdressaufbereitung.adressaufbereitung(selected,
              spendenbescheinigung);
          zeile1.setValue(spendenbescheinigung.getZeile1());
          zeile2.setValue(spendenbescheinigung.getZeile2());
          zeile3.setValue(spendenbescheinigung.getZeile3());
          zeile4.setValue(spendenbescheinigung.getZeile4());
          zeile5.setValue(spendenbescheinigung.getZeile5());
          zeile6.setValue(spendenbescheinigung.getZeile6());
          zeile7.setValue(spendenbescheinigung.getZeile7());
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Setzen des Mitglieds:", e);
      }
    }
  }

}
