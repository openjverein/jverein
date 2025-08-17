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
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungPrintAction;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.menu.BuchungPartAnzeigenMenu;
import de.jost_net.JVerein.gui.menu.SpendenbescheinigungMenu;
import de.jost_net.JVerein.gui.parts.BuchungListPart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
import de.jost_net.JVerein.gui.view.SpendenbescheinigungMailView;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.SpendenbescheinigungExportCSV;
import de.jost_net.JVerein.io.SpendenbescheinigungExportPDF;
import de.jost_net.JVerein.io.ZipMailer;
import de.jost_net.JVerein.keys.Adressblatt;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.keys.SuchSpendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.SpbAdressaufbereitung;
import de.jost_net.JVerein.util.VorlageUtil;
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
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class SpendenbescheinigungControl extends DruckMailControl
    implements Savable
{

  // Spendenbescheinigung View
  private JVereinTablePart spbList;

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
    spendenart.setEnabled(false);
    return spendenart;
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }
    Mitglied m = getSpendenbescheinigung().getMitglied();
    mitglied = new MitgliedInput().getMitgliedInput(mitglied, m,
        (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
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
    spendedatum.setEnabled(false);
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
    betrag.setEnabled(false);
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
    if (getSpendenbescheinigung().getSpendenart() == Spendenart.SACHSPENDE)
    {
      formular = new FormularInput(FormularArt.SACHSPENDENBESCHEINIGUNG, def);
      // Wegen kompatibilität zu früher
      if (def != null)
      {
        Formular f = getSpendenbescheinigung().getFormular();
        @SuppressWarnings("unchecked")
        List<Formular> list = formular.getList();
        if (!list.contains(f))
        {
          list.add(f);
          formular.setList(list);
        }
      }
    }
    else if (getSpendenbescheinigung().getBuchungen().size() > 1)
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
    if (buchungen != null && buchungen.size() == 1
        && getSpendenbescheinigung().getSpendenart() == Spendenart.GELDSPENDE)
    {
      // Verzicht aus Buchung lesen
      check = buchungen.get(0).getVerzicht();
    }
    ersatzaufwendungen = new CheckboxInput(check);
    if (buchungen != null && buchungen.size() > 1
        && getSpendenbescheinigung().getSpendenart() == Spendenart.GELDSPENDE)
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
    bezeichnungsachzuwendung.disable();
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
    herkunftspende.disable();
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
    unterlagenwertermittlung.disable();
    return unterlagenwertermittlung;
  }

  public Part getBuchungListPart() throws RemoteException
  {
    return new BuchungListPart(getSpendenbescheinigung().getBuchungen(),
        new BuchungAction(false), new BuchungPartAnzeigenMenu());
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Spendenbescheinigung spb = getSpendenbescheinigung();
    Spendenart spa = (Spendenart) getSpendenart().getValue();
    spb.setMitglied((Mitglied) getMitglied().getValue());
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
    spb.setBezeichnungSachzuwendung(
        (String) getBezeichnungSachzuwendung().getValue());
    spb.setFormular((Formular) getFormular().getValue());
    HerkunftSpende hsp = (HerkunftSpende) getHerkunftSpende().getValue();
    spb.setHerkunftSpende(hsp.getKey());
    spb.setUnterlagenWertermittlung(
        (Boolean) getUnterlagenWertermittlung().getValue());
    return spb;
  }

  /**
   * This method stores the project using the current values.
   * 
   * @throws ApplicationException
   */
  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei Speichern der Spendenbescheinigung";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public ButtonRtoL getDruckUndMailButton()
  {

    ButtonRtoL b = new ButtonRtoL("Druck und Mail", new Action()
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
    spbList = new JVereinTablePart(getSpendenbescheinigungen(), null);
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
    spbList.setContextMenu(new SpendenbescheinigungMenu(spbList));
    spbList.setRememberOrder(true);
    spbList.addFeature(new FeatureSummary());
    spbList.setMulti(true);
    spbList.setAction(
        new EditAction(SpendenbescheinigungDetailView.class, spbList));
    VorZurueckControl.setObjektListe(null, null);
    return spbList;
  }

  @Override
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
    String text = "Es wurden " + spbArr.length
        + " Spendenbescheinigungen ausgewählt"
        + "\nFolgende Mitglieder haben keine Mailadresse:";
    try
    {
      for (Spendenbescheinigung spb : spbArr)
      {
        Mitglied m = spb.getMitglied();
        if (m != null && (m.getEmail() == null || m.getEmail().isEmpty()))
        {
          text = text + "\n - " + m.getName() + ", " + m.getVorname();
        }
      }
      text = text
          + "\nFür folgende Spendenbescheinigungen existiert kein Mitglied und keine Mailadresse:";
      for (Spendenbescheinigung spb : spbArr)
      {
        if (spb.getMitglied() == null)
        {
          text = text + "\n - " + spb.getZeile1() + ", " + spb.getZeile2()
              + ", " + spb.getZeile3();
        }
      }
    }
    catch (Exception ex)
    {
      GUI.getStatusBar().setErrorText(
          "Fehler beim Ermitteln der Mitglieder aus den Spendenbescheinigungen");
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

          Spendenbescheinigung[] spbArray = getSpbArray(currentObject);

          generatePdf((String) mailtext.getValue(),
              (Adressblatt) adressblatt.getValue(), spbArray,
              (Ausgabeart) ausgabeart.getValue());
          if ((Ausgabeart) ausgabeart.getValue() == Ausgabeart.MAIL)
          {
            sendeMail((String) mailbetreff.getValue(),
                (String) mailtext.getValue(), spbArray);
          }
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
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
    if (ausgabeart == Ausgabeart.DRUCK)
      open = true;
    SpendenbescheinigungPrintAction action = new SpendenbescheinigungPrintAction(
        text, adressblatt, open);
    action.handleAction(spba);
  }

  private void sendeMail(final String betr, String text,
      final Spendenbescheinigung[] spba) throws IOException
  {

    File zip = File.createTempFile("Spendenbescheinigungen", ".zip");
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));

    for (Spendenbescheinigung spb : spba)
    {
      Mitglied m = spb.getMitglied();
      if (m == null || m.getEmail() == null || m.getEmail().isEmpty())
      {
        continue;
      }
      // PDF wurde bereits erstellt, dieses holen wir und schreiben es in das
      // ZIP
      String path = (String) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGVERZEICHNIS);
      if (path == null || path.length() == 0)
      {
        path = settings.getString("lastdir", System.getProperty("user.home"));
      }
      settings.setAttribute("lastdir", path);
      path = path.endsWith(File.separator) ? path : path + File.separator;
      String fileName = VorlageUtil.getName(
          VorlageTyp.SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME, spb, m) + ".pdf";

      // MITGLIED-ID#ART#ART-ID#MAILADRESSE#DATEINAME.pdf
      zos.putNextEntry(new ZipEntry(m.getID() + "#spendenbescheinigung#"
          + spb.getID() + "#" + m.getEmail() + "#" + fileName));
      FileInputStream in = new FileInputStream(new File(path + fileName));
      // buffer size
      byte[] b = new byte[1024];
      int count;
      while ((count = in.read(b)) > 0)
      {
        zos.write(b, 0, count);
      }
      in.close();
    }
    zos.close();
    new ZipMailer(zip, (String) betr, text);
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
          (String) Einstellungen.getEinstellung(Property.DATEINAMENMUSTER),
          type).get());

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
        else
        {
          spendenbescheinigung.setMitglied(null);
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Setzen des Mitglieds:", e);
      }
    }
  }

  private Spendenbescheinigung[] getSpbArray(Object object)
      throws RemoteException, ApplicationException
  {
    Spendenbescheinigung[] spbArray = null;
    if (object == null)
    {
      ArrayList<Spendenbescheinigung> spblist = getSpendenbescheinigungen();
      spbArray = spblist.toArray(new Spendenbescheinigung[spblist.size()]);
    }
    else if (object instanceof Spendenbescheinigung[])
    {
      spbArray = (Spendenbescheinigung[]) object;
    }
    if (spbArray == null || spbArray.length == 0)
    {
      throw new ApplicationException(
          "Für die gewählten Filterkriterien wurden keine Spendenbescheinigungen gefunden");
    }
    return spbArray;
  }

  @Override
  DruckMailEmpfaenger getDruckMailMitglieder(Object object, String option)
      throws RemoteException, ApplicationException
  {
    List<DruckMailEmpfaengerEntry> liste = new ArrayList<>();
    String text = "";
    int ohneMail = 0;
    int ohneMitglied = 0;
    Spendenbescheinigung[] spbs = getSpbArray(object);
    Mitglied m;
    String dokument = "";
    for (Spendenbescheinigung spb : spbs)
    {
      m = spb.getMitglied();
      if (m != null)
      {
        String mail = m.getEmail();
        if ((mail == null || mail.isEmpty())
            && getAusgabeart().getValue() == Ausgabeart.MAIL)
        {
          ohneMail++;
        }
        dokument = "Spendenbescheinigung von "
            + Datum.formatDate(spb.getBescheinigungsdatum()) + " über "
            + Einstellungen.DECIMALFORMAT.format(spb.getBetrag()) + "€";
        liste.add(new DruckMailEmpfaengerEntry(dokument, mail, m.getName(),
            m.getVorname(), m.getMitgliedstyp()));
      }
      else
      {
        ohneMitglied++;
        dokument = "Spendenbescheinigung von "
            + Datum.formatDate(spb.getBescheinigungsdatum()) + " über "
            + Einstellungen.DECIMALFORMAT.format(spb.getBetrag())
            + "€ und Zeile 2: " + spb.getZeile2();
        liste.add(
            new DruckMailEmpfaengerEntry(dokument, null, null, null, null));
      }
    }
    if (ohneMail > 0 && ohneMitglied == 0)
    {
      text = getMailText(ohneMail, false);
    }
    if (ohneMail == 0 && ohneMitglied > 0)
    {
      text = getMitgliedText(ohneMitglied,
          getAusgabeart().getValue() != Ausgabeart.MAIL);
    }
    if (ohneMail > 0 && ohneMitglied > 0)
    {
      text = getMailText(ohneMail, true) + getMitgliedText(ohneMitglied, false);
    }
    return new DruckMailEmpfaenger(liste, text);
  }

  private String getMailText(int ohneMail, boolean druck)
  {
    String text = "";
    String zusatz = ".";
    if (druck)
    {
      zusatz = " und ";
    }
    if (ohneMail == 1)
    {
      text = ohneMail + " Mitglied hat keine Mail Adresse" + zusatz;
    }
    else if (ohneMail > 1)
    {
      text = ohneMail + " Mitglieder haben keine Mail Adresse" + zusatz;
    }
    return text;
  }

  private String getMitgliedText(int ohneMitglied, boolean druck)
  {
    String text = "";
    String zusatz = ".";
    if (druck && ohneMitglied == 1)
    {
      zusatz = ", wird aber gedruckt.";
    }
    if (druck && ohneMitglied > 1)
    {
      zusatz = ", werden aber gedruckt.";
    }
    if (ohneMitglied == 1)
    {
      text = ohneMitglied + " Spendenbescheinigung hat kein Mitglied gesetzt"
          + zusatz;
    }
    else if (ohneMitglied > 1)
    {
      text = ohneMitglied
          + " Spendenbescheinigungen haben kein Mitglied gesetzt" + zusatz;
    }
    return text;
  }
}
