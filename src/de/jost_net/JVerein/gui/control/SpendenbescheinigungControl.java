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
import java.io.IOException;
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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.Variable.VarTools;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungPrintAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.MitgliedskontoFormatter;
import de.jost_net.JVerein.gui.input.FormularInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.menu.SpendenbescheinigungMenu;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.FormularAufbereitung;
import de.jost_net.JVerein.io.MailSender;
import de.jost_net.JVerein.keys.Ausgabeart;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.HerkunftSpende;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.rmi.Mail;
import de.jost_net.JVerein.rmi.MailAnhang;
import de.jost_net.JVerein.rmi.MailEmpfaenger;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
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

  private TablePart buchungsList;

  private Spendenbescheinigung spendenbescheinigung;
  
  private boolean and = false;

  private String sql = "";
  

  public SpendenbescheinigungControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
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
    return spendenart;
  }

  private void enableSachspende()
  {
    try
    {
      Spendenart spa = (Spendenart) getSpendenart().getValue();
      getBezeichnungSachzuwendung()
          .setEnabled(spa.getKey() == Spendenart.SACHSPENDE);
      getHerkunftSpende().setEnabled(spa.getKey() == Spendenart.SACHSPENDE);
      getUnterlagenWertermittlung()
          .setEnabled(spa.getKey() == Spendenart.SACHSPENDE);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
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
    return zeile1;
  }

  public TextInput getZeile2() throws RemoteException
  {
    if (zeile2 != null)
    {
      return zeile2;
    }
    zeile2 = new TextInput(getSpendenbescheinigung().getZeile2(), 80);
    return zeile2;
  }

  public TextInput getZeile3() throws RemoteException
  {
    if (zeile3 != null)
    {
      return zeile3;
    }
    zeile3 = new TextInput(getSpendenbescheinigung().getZeile3(), 80);
    return zeile3;
  }

  public TextInput getZeile4() throws RemoteException
  {
    if (zeile4 != null)
    {
      return zeile4;
    }
    zeile4 = new TextInput(getSpendenbescheinigung().getZeile4(), 80);
    return zeile4;
  }

  public TextInput getZeile5() throws RemoteException
  {
    if (zeile5 != null)
    {
      return zeile5;
    }
    zeile5 = new TextInput(getSpendenbescheinigung().getZeile5(), 80);
    return zeile5;
  }

  public TextInput getZeile6() throws RemoteException
  {
    if (zeile6 != null)
    {
      return zeile6;
    }
    zeile6 = new TextInput(getSpendenbescheinigung().getZeile6(), 80);
    return zeile6;
  }

  public TextInput getZeile7() throws RemoteException
  {
    if (zeile7 != null)
    {
      return zeile7;
    }
    zeile7 = new TextInput(getSpendenbescheinigung().getZeile7(), 80);
    return zeile7;
  }

  public DateInput getSpendedatum() throws RemoteException
  {
    if (spendedatum != null)
    {
      return spendedatum;
    }
    spendedatum = new DateInput(getSpendenbescheinigung().getSpendedatum());
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

  public Part getBuchungsList() throws RemoteException
  {
    Spendenbescheinigung spb = getSpendenbescheinigung();
    if (buchungsList == null)
    {

      buchungsList = new BuchungListTablePart(spb.getBuchungen(), null);
      buchungsList.addColumn("Nr", "id-int");
      buchungsList.addColumn("Konto", "konto", new Formatter()
      {

        @Override
        public String format(Object o)
        {
          Konto k = (Konto) o;
          if (k != null)
          {
            try
            {
              return k.getBezeichnung();
            }
            catch (RemoteException e)
            {
              Logger.error("Fehler", e);
            }
          }
          return "";
        }
      });
      buchungsList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      buchungsList.addColumn("Auszug", "auszugsnummer");
      buchungsList.addColumn("Blatt", "blattnummer");
      buchungsList.addColumn("Name", "name");
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
      buchungsList.addColumn("Buchungsart", "buchungsart",
          new BuchungsartFormatter());
      buchungsList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      buchungsList.addColumn("Mitglied", "mitgliedskonto",
          new MitgliedskontoFormatter());
      buchungsList.addColumn("Ersatz für Aufwendungen", "verzicht", new JaNeinFormatter());
      buchungsList.setMulti(true);
      // buchungsList.setContextMenu(new BuchungMenu(this));
      buchungsList.setRememberColWidths(true);
      buchungsList.setRememberOrder(true);
      buchungsList.setRememberState(true);
      buchungsList.addFeature(new FeatureSummary());
    }
    else
    {
      buchungsList.removeAll();
      for (Buchung bu : spb.getBuchungen())
      {
        buchungsList.addItem(bu);
      }
      buchungsList.sort();
    }
    return buchungsList;
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

  public Button getPDFStandardButton(final boolean adressblatt)
  {
    String label = "PDF (Standard)";
    if (adressblatt)
    {
      label = "PDF (Standard, Mit Adressblatt)";
    }
    Button b = new Button(label, new Action()
    {

      /**
       * Diese Action verwendet die "SpendenbescheinigungPrintAction" für die
       * Aufbereitung des Dokumentes. Als Rahmen ist der Dialog zur Dateiauswahl
       * und die Anzeige des Dokumentes um die Generierung gesetzt.
       */
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          Spendenbescheinigung spb = getSpendenbescheinigung();
          if (spb.isNewObject())
          {
            GUI.getStatusBar()
                .setErrorText("Spendenbescheinigung bitte erst speichern!");
            return;
          }
          FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
          fd.setText("Ausgabedatei wählen.");
          String path = Einstellungen.getEinstellung()
              .getSpendenbescheinigungverzeichnis();
          if (path != null && path.length() > 0)
          {
            fd.setFilterPath(path);
          }
          if (spb.getMitglied() != null)
          {
            fd.setFileName(new Dateiname(spb.getMitglied(),
                spb.getBescheinigungsdatum(), "Spendenbescheinigung",
                Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                "pdf").get());
          }
          else
          {
            fd.setFileName(new Dateiname(spb.getZeile1(), spb.getZeile2(),
                spb.getBescheinigungsdatum(), "Spendenbescheinigung",
                Einstellungen.getEinstellung().getDateinamenmusterSpende(),
                "pdf").get());
          }
          fd.setFilterExtensions(new String[] { "*.pdf" });

          String s = fd.open();
          if (s == null || s.length() == 0)
          {
            return;
          }
          if (!s.toLowerCase().endsWith(".pdf"))
          {
            s = s + ".pdf";
          }
          final File file = new File(s);
          //
          SpendenbescheinigungPrintAction spa = new SpendenbescheinigungPrintAction(
              true, adressblatt, s);
          spa.handleAction(spb);
          GUI.getStatusBar().setSuccessText("Spendenbescheinigung erstellt");
          FileViewer.show(file);
        }
        catch (Exception e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler bei der Aufbereitung der Spendenbescheinigung");
        }
      }
    }, getSpendenbescheinigung(), false, "file-pdf.png");
    return b;
  }

  public Button getPDFIndividuellButton(final boolean adressblatt)
  {
    String label = "PDF (Individuell)";
    if (adressblatt)
    {
      label = "PDF (Individuell, Mit Adressblatt)";
    }
    Button b = new Button(label, new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          generiereSpendenbescheinigungIndividuell(adressblatt);
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler bei der Aufbereitung der Spendenbescheinigung");
        }
        catch (IOException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler bei der Aufbereitung der Spendenbescheinigung");
        }
      }
    }, null, false, "file-pdf.png");
    return b;
  }

  private void generiereSpendenbescheinigungIndividuell(boolean adressblatt) throws IOException
  {
    Spendenbescheinigung spb = getSpendenbescheinigung();
    if (spb.isNewObject())
    {
      GUI.getStatusBar()
          .setErrorText("Spendenbescheinigung bitte erst speichern!");
      return;
    }
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei wählen.");
    String path = Einstellungen.getEinstellung()
        .getSpendenbescheinigungverzeichnis();
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    if (spb.getMitglied() != null)
    {
      fd.setFileName(new Dateiname(spb.getMitglied(),
          spb.getBescheinigungsdatum(), "Spendenbescheinigung",
          Einstellungen.getEinstellung().getDateinamenmusterSpende(), "pdf")
              .get());
    }
    else
    {
      fd.setFileName(new Dateiname(spb.getZeile1(), spb.getZeile2(),
          spb.getBescheinigungsdatum(), "Spendenbescheinigung",
          Einstellungen.getEinstellung().getDateinamenmusterSpende(), "pdf")
              .get());
    }
    fd.setFilterExtensions(new String[] { "*.pdf" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith(".pdf"))
    {
      s = s + ".pdf";
    }
    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());

    /* Check ob auch ein Formular ausgewaehlt ist */
    Formular spendeformular = getSpendenbescheinigung().getFormular();
    if (spendeformular == null)
    {
      GUI.getStatusBar().setErrorText("Bitte Formular auswaehlen");
      return;
    }

    Formular fo = (Formular) Einstellungen.getDBService()
        .createObject(Formular.class, spendeformular.getID());
    Map<String, Object> map = getSpendenbescheinigung().getMap(null);
    map = new AllgemeineMap().getMap(map);
    FormularAufbereitung fa = new FormularAufbereitung(file);
    fa.writeForm(fo, map);
    // Brieffenster drucken bei Spendenbescheinigung
    if (adressblatt)
    {
      fa.printAdressfenster(getAussteller(), 
          (String) map.get(SpendenbescheinigungVar.EMPFAENGER.getName()));
    }
    fa.showFormular();

  }
  
  private String getAussteller() throws RemoteException
  {
    return Einstellungen.getEinstellung().getName() + ", "
        + Einstellungen.getEinstellung().getStrasse() + ", "
        + Einstellungen.getEinstellung().getPlz() + " "
        + Einstellungen.getEinstellung().getOrt();
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
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private ArrayList<Spendenbescheinigung> getSpendenbescheinigungen() throws RemoteException
  {
    final DBService service = Einstellungen.getDBService();
    ArrayList<Object> bedingungen = new ArrayList<>();
    and = false;
    
    sql = "select spendenbescheinigung.*  from spendenbescheinigung ";
    sql +=  "left join mitglied on (spendenbescheinigung.mitglied = mitglied.id) ";
    
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        addCondition("(lower(zeile2) like ?)");
        bedingungen.add("%" + tmpSuchname.toLowerCase() + "%");
      }
    }
    if (isMailauswahlAktiv())
    {
      int mailauswahl = (Integer) getMailauswahl().getValue();
      if (mailauswahl == MailAuswertungInput.OHNE)
      {
        addCondition("(email is null or length(email) = 0)");
      }
      if (mailauswahl == MailAuswertungInput.MIT)
      {
        addCondition("(email is  not null and length(email) > 0)");
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
    sql += " ORDER BY bescheinigungsdatum desc ";
    
    ResultSetExtractor rs = new ResultSetExtractor()
    {
      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        ArrayList<Spendenbescheinigung> list = new ArrayList<>();
        while (rs.next())
        {
          list.add(
              (Spendenbescheinigung) service.createObject(Spendenbescheinigung.class, rs.getString(1)));
        }
        return list;
      }
    };

    return (ArrayList<Spendenbescheinigung>) service.execute(sql, bedingungen.toArray(),
        rs);
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
          generatePdf((String) art.getValue(),
              (String) adressblatt.getValue(), spbArray);
          if (ausgabeart == null || 
              (Ausgabeart) ausgabeart.getValue() == Ausgabeart.MAIL)
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

  private void generatePdf(String ar, String ab, Spendenbescheinigung[] spba)
      throws ApplicationException
  {
    boolean standard = true;
    if (ar.equalsIgnoreCase("Individuell"))
      standard = false;
    boolean adressblatt = false;
    if (ab.equalsIgnoreCase("Mit"))
      adressblatt = true;
    SpendenbescheinigungPrintAction action = 
        new SpendenbescheinigungPrintAction(standard, adressblatt);
    action.handleAction(spba);
  }
  
  private void sendeMail(final String betr, final String txt,
      final Spendenbescheinigung[] spba) throws RemoteException
  {

    BackgroundTask t = new BackgroundTask()
    {

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
                  spba[i].getBescheinigungsdatum(), "Spendenbescheinigung",
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

              sender.sendMail(m.getEmail(), wtext1.getBuffer().toString(),
                  wtext2.getBuffer().toString(), anhang);
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

}
