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
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.input.BICInput;
import de.jost_net.JVerein.gui.input.EmailInput;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IBANInput;
import de.jost_net.JVerein.gui.input.PersonenartInput;
import de.jost_net.JVerein.gui.menu.KursteilnehmerMenu;
import de.jost_net.JVerein.gui.view.KursteilnehmerDetailView;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.keys.Staat;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class KursteilnehmerControl extends FilterControl
    implements Savable
{

  private PersonenartInput personenart;

  private TextInput anrede;

  private TextInput titel;

  private TextInput name;

  private TextInput vorname;

  private TextInput strasse;

  private TextInput adressierungszusatz;

  private TextInput plz;

  private TextInput ort;

  private SelectInput staat;

  private EmailInput email;

  private DecimalInput betrag;

  private Input vzweck1;

  private DateInput mandatdatum;

  private BICInput bic;

  private IBANInput iban;

  private DateInput geburtsdatum = null;

  private GeschlechtInput geschlecht;

  private Kursteilnehmer ktn;

  private TablePart part;

  public KursteilnehmerControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Kursteilnehmer getKursteilnehmer()
  {
    if (ktn != null)
    {
      return ktn;
    }
    ktn = (Kursteilnehmer) getCurrentObject();
    return ktn;
  }

  public PersonenartInput getPersonenart() throws RemoteException
  {
    if (personenart != null)
    {
      return personenart;
    }
    personenart = new PersonenartInput(getKursteilnehmer().getPersonenart());
    personenart.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        String pa = (String) personenart.getValue();
        if (pa.toLowerCase().startsWith("n"))
        {
          name.setName("Name");
          vorname.setName("Vorname");
        }
        else
        {
          name.setName("Zeile 1");
          vorname.setName("Zeile 2");
        }
      }

    });
    personenart.setName("Personenart");
    return personenart;
  }

  public TextInput getAnrede() throws RemoteException
  {
    if (anrede != null)
    {
      return anrede;
    }
    anrede = new TextInput(getKursteilnehmer().getAnrede(), 10);
    anrede.setName("Anrede");
    return anrede;
  }

  public TextInput getTitel() throws RemoteException
  {
    if (titel != null)
    {
      return titel;
    }
    titel = new TextInput(getKursteilnehmer().getTitel(), 40);
    titel.setName("Titel");
    return titel;
  }

  public TextInput getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getKursteilnehmer().getName(), 40);
    name.setName("Name");
    name.setMandatory(true);
    return name;
  }

  public TextInput getVorname() throws RemoteException
  {
    if (vorname != null)
    {
      return vorname;
    }
    vorname = new TextInput(getKursteilnehmer().getVorname(), 40);
    vorname.setName("Vorname");
    return vorname;
  }

  public Input getStrasse() throws RemoteException
  {
    if (strasse != null)
    {
      return strasse;
    }
    strasse = new TextInput(getKursteilnehmer().getStrasse(), 40);
    strasse.setName("Straﬂe");
    return strasse;
  }

  public TextInput getAdressierungszusatz() throws RemoteException
  {
    if (adressierungszusatz != null)
    {
      return adressierungszusatz;
    }
    adressierungszusatz = new TextInput(
        getKursteilnehmer().getAdressierungszusatz(), 40);
    adressierungszusatz.setName("Adressierungszusatz");
    return adressierungszusatz;
  }

  public Input getPLZ() throws RemoteException
  {
    if (plz != null)
    {
      return plz;
    }
    plz = new TextInput(getKursteilnehmer().getPlz(), 10);
    plz.setName("PLZ");
    return plz;
  }

  public Input getOrt() throws RemoteException
  {
    if (ort != null)
    {
      return ort;
    }
    ort = new TextInput(getKursteilnehmer().getOrt(), 40);
    ort.setName("Ort");
    return ort;
  }

  public SelectInput getStaat() throws RemoteException
  {
    if (staat != null)
    {
      return staat;
    }
    if (getKursteilnehmer().getStaat() != null
        && getKursteilnehmer().getStaat().length() > 0
        && Staat.getByKey(getKursteilnehmer().getStaatCode()) == null)
    {
      GUI.getStatusBar()
          .setErrorText("Konnte Staat \"" + getKursteilnehmer().getStaat()
              + "\" nicht finden, bitte anpassen.");
    }
    staat = new SelectInput(Staat.values(),
        Staat.getByKey(getKursteilnehmer().getStaatCode()));
    staat.setPleaseChoose("Nicht gesetzt");
    staat.setName("Staat");
    return staat;
  }

  public EmailInput getEmail() throws RemoteException
  {
    if (email != null)
    {
      return email;
    }
    email = new EmailInput(getKursteilnehmer().getEmail());
    return email;
  }

  public Input getVZweck1() throws RemoteException
  {
    if (vzweck1 != null)
    {
      return vzweck1;
    }
    vzweck1 = new TextInput(getKursteilnehmer().getVZweck1(), 140);
    vzweck1.setName("Verwendungszweck");
    vzweck1.setMandatory(true);
    return vzweck1;
  }

  public DateInput getMandatDatum() throws RemoteException
  {
    if (mandatdatum != null)
    {
      return mandatdatum;
    }

    Date d = getKursteilnehmer().getMandatDatum();
    if (d.equals(Einstellungen.NODATE))
    {
      d = null;
    }
    this.mandatdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.mandatdatum.setTitle("Datum des Mandats");
    this.mandatdatum.setName("Datum des Mandats");
    this.mandatdatum.setText("Bitte Datum des Mandats w‰hlen");
    this.mandatdatum.setName("Datum des Mandats");
    this.mandatdatum.setMandatory(true);
    return mandatdatum;
  }

  public BICInput getBIC() throws RemoteException
  {
    if (bic != null)
    {
      return bic;
    }
    bic = new BICInput(getKursteilnehmer().getBic());
    bic.setName("BIC");
    bic.setMandatory(true);
    return bic;
  }

  public IBANInput getIBAN() throws RemoteException
  {
    if (iban != null)
    {
      return iban;
    }
    iban = new IBANInput(HBCIProperties.formatIban(getKursteilnehmer().getIban()), getBIC());
    iban.setName("IBAN");
    iban.setMandatory(true);
    return iban;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getKursteilnehmer().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setName("Betrag");
    betrag.setMandatory(true);
    return betrag;
  }

  public DateInput getGeburtsdatum() throws RemoteException
  {
    if (geburtsdatum != null)
    {
      return geburtsdatum;
    }
    Date d = getKursteilnehmer().getGeburtsdatum();
    this.geburtsdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.geburtsdatum.setTitle("Geburtsdatum");
    this.geburtsdatum.setText("Bitte Geburtsdatum w‰hlen");
    if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMERGEBGESPFLICHT))
    {
      this.geburtsdatum.setMandatory(true);
    }
    return geburtsdatum;
  }

  public GeschlechtInput getGeschlecht() throws RemoteException
  {
    if (geschlecht != null)
    {
      return geschlecht;
    }
    geschlecht = new GeschlechtInput(getKursteilnehmer().getGeschlecht());
    geschlecht.setName("Geschlecht");
    geschlecht.setPleaseChoose("Bitte ausw‰hlen");
    if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMERGEBGESPFLICHT))
    {
      geschlecht.setMandatory(true);
    }
    return geschlecht;
  }

  public Part getKursteilnehmerTable() throws RemoteException
  {
    if (part != null)
    {
      return part;
    }
    DBIterator<Kursteilnehmer> kursteilnehmer = getIterator();
    part = new TablePart(kursteilnehmer,
        new EditAction(KursteilnehmerDetailView.class));

    part.addColumn("Name", "name");
    part.addColumn("Vorname", "vorname");
    part.addColumn("Straﬂe", "strasse");
    part.addColumn("PLZ", "plz");
    part.addColumn("Ort", "ort");
    part.addColumn("Verwendungszweck", "vzweck1");
    part.addColumn("BIC", "bic");
    part.addColumn("IBAN", "iban", new IbanFormatter());
    part.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    part.addColumn("Mandats-ID", "mandatid");
    part.addColumn("Eingabedatum", "eingabedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    part.addColumn("Abbuchungsdatum", "abbudatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    part.setContextMenu(new KursteilnehmerMenu(part));
    part.setMulti(true);

    return part;
  }

  public void TabRefresh()
  {

    try
    {
      if (part == null)
      {
        return;
      }
      part.removeAll();
      DBIterator<Kursteilnehmer> kursteilnehmer = getIterator();
      while (kursteilnehmer.hasNext())
      {
        Kursteilnehmer kt = kursteilnehmer.next();
        part.addItem(kt);
      }
      part.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  public Button getStartAuswertungButton()
  {
    Button b = new Button("Starten", new Action()
    {

      @Override
      public void handleAction(Object context)
      {
        starteAuswertung();
      }
    }, null, true, "walking.png"); // "true" defines this button as the default
    // button
    return b;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Kursteilnehmer k = getKursteilnehmer();
    String p = (String) getPersonenart().getValue();
    p = p.substring(0, 1);
    k.setPersonenart(p);
    k.setAnrede((String) getAnrede().getValue());
    k.setTitel((String) getTitel().getValue());
    k.setName((String) getName().getValue());
    k.setVorname((String) getVorname().getValue());
    k.setStrasse((String) getStrasse().getValue());
    k.setAdressierungszuatz((String) getAdressierungszusatz().getValue());
    k.setPlz((String) getPLZ().getValue());
    k.setOrt((String) getOrt().getValue());
    k.setStaat(getStaat().getValue() == null ? ""
        : ((Staat) getStaat().getValue()).getKey());
    k.setEmail((String) getEmail().getValue());
    k.setVZweck1((String) getVZweck1().getValue());
    k.setMandatDatum((Date) getMandatDatum().getValue());
    String ib = (String) getIBAN().getValue();
    if (ib == null)
      k.setIban(null);
    else
      k.setIban(ib.toUpperCase().replace(" ", ""));
    k.setBic((String) getBIC().getValue());
    k.setBetrag((Double) getBetrag().getValue());
    if (getGeburtsdatum() != null)
    {
      k.setGeburtsdatum((Date) getGeburtsdatum().getValue());
    }
    if (getGeschlecht() != null)
    {
      k.setGeschlecht((String) getGeschlecht().getValue());
    }

    if (k.getID() == null)
    {
      k.setEingabedatum();
    }
    return k;
  }

  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei Speichern des Kursteilnehmers";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }

  private void starteAuswertung()
  {
    // Alle Kursteilnehmer lesen
    
    try
    {
      saveFilterSettings();
      String subtitle = "";
      final DBIterator<Kursteilnehmer> list = getIterator();
      
      FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
      fd.setText("Ausgabedatei w‰hlen.");

      String path = settings.getString("lastdir",
          System.getProperty("user.home"));
      if (path != null && path.length() > 0)
      {
        fd.setFilterPath(path);
      }
      fd.setFileName(new Dateiname("kursteilnehmer", "",
          (String) Einstellungen.getEinstellung(Property.DATEINAMENMUSTER), "PDF")
              .get());

      final String s = fd.open();

      if (s == null || s.length() == 0)
      {
        // close();
        return;
      }

      final File file = new File(s);
      settings.setAttribute("lastdir", file.getParent());
      final String subtitle2 = subtitle;

      BackgroundTask t = new BackgroundTask()
      {

        @Override
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          try
          {
            Reporter rpt = new Reporter(new FileOutputStream(file),
                "Kursteilnehmer", subtitle2, list.size());

            GUI.getCurrentView().reload();

            rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 50,
                BaseColor.LIGHT_GRAY);
            rpt.addHeaderColumn("Name", Element.ALIGN_LEFT, 80,
                BaseColor.LIGHT_GRAY);
            rpt.addHeaderColumn("Verwendungszweck", Element.ALIGN_LEFT, 80,
                BaseColor.LIGHT_GRAY);
            rpt.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 40,
                BaseColor.LIGHT_GRAY);
            rpt.createHeader();
            while (list.hasNext())
            {
              Kursteilnehmer kt = list.next();
              rpt.addColumn(kt.getAbbudatum(), Element.ALIGN_LEFT);
              rpt.addColumn(kt.getName(), Element.ALIGN_LEFT);
              rpt.addColumn(kt.getVZweck1(), Element.ALIGN_LEFT);
              rpt.addColumn(kt.getBetrag());
            }
            rpt.close();
            FileViewer.show(file);
          }
          catch (ApplicationException ae)
          {
            Logger.error("Fehler", ae);
            GUI.getStatusBar().setErrorText(ae.getMessage());
            throw ae;
          }
          catch (Exception re)
          {
            Logger.error("Fehler", re);
            GUI.getStatusBar().setErrorText(re.getMessage());
            throw new ApplicationException(re);
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

  private DBIterator<Kursteilnehmer> getIterator() throws RemoteException
  {
    DBIterator<Kursteilnehmer> kursteilnehmer = Einstellungen.getDBService()
        .createList(Kursteilnehmer.class);
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String suchN = (String) getSuchname().getValue();
      if (suchN.length() > 0)
      {
        kursteilnehmer.addFilter("name like ?",
            new Object[] { "%" + suchN + "%" });
      }
    }
    if (isSuchtextAktiv() && getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        kursteilnehmer.addFilter("(lower(vzweck1) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%"});
      }
    }
    if (isEingabedatumvonAktiv() && getEingabedatumvon().getValue() != null)
    {
      kursteilnehmer.addFilter("eingabedatum >= ?",
          new Object[] { (Date) getEingabedatumvon().getValue() });
    }
    if (isEingabedatumbisAktiv() && getEingabedatumbis().getValue() != null)
    {
      kursteilnehmer.addFilter("eingabedatum <= ?",
          new Object[] { (Date) getEingabedatumbis().getValue() });
    }
    if (isAbbuchungsdatumvonAktiv() && getAbbuchungsdatumvon().getValue() != null)
    {
      kursteilnehmer.addFilter("abbudatum >= ?",
          new Object[] { (Date) getAbbuchungsdatumvon().getValue() });
    }
    if (isAbbuchungsdatumbisAktiv() && getAbbuchungsdatumbis().getValue() != null)
    {
      kursteilnehmer.addFilter("abbudatum <= ?",
          new Object[] { (Date) getAbbuchungsdatumbis().getValue() });
    }
    return kursteilnehmer;
  }

}
