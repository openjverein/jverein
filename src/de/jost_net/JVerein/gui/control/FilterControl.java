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

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedskontoControl.DIFFERENZ;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlParameter;
import de.jost_net.JVerein.gui.dialogs.ZusatzfelderAuswahlDialog;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FilterControl extends AbstractControl
{  
  // String für allgemeine Settings z.B. settings1
  protected String settingsprefix = "";

  // String für Zusatzfelder
  protected String additionalparamprefix1 = "";

  // String für Zusatfelder Anzahl
  protected String additionalparamprefix2 = "";

  protected Settings settings = null;
  
  protected Mitgliedstyp typ = Mitgliedstyp.NOT_USED;

  protected TreePart eigenschaftenAuswahlTree = null;

  protected SelectInput suchadresstyp = null;

  protected SelectInput status = null;

  protected TextInput suchexternemitgliedsnummer = null;

  protected DialogInput eigenschaftenabfrage = null;

  protected SelectInput beitragsgruppeausw = null;

  protected TextInput suchname = null;

  protected GeschlechtInput suchgeschlecht = null;

  protected DateInput stichtag = null;

  protected DateInput geburtsdatumvon = null;

  protected DateInput geburtsdatumbis = null;

  protected DateInput sterbedatumvon = null;

  protected DateInput sterbedatumbis = null;

  protected DateInput eintrittvon = null;

  protected DateInput eintrittbis = null;

  protected DateInput austrittvon = null;

  protected DateInput austrittbis = null;
  
  protected DialogInput zusatzfelderabfrage = null;
  
  protected SelectInput mailAuswahl = null;
  
  protected ZusatzfelderAuswahlDialog zad= null;
  
  protected DateInput datumvon = null;

  protected DateInput datumbis = null;
  
  protected SelectInput differenz = null;
  
  protected CheckboxInput ohneabbucher = null;
  
  protected SelectInput suchlehrgangsart = null;
  
  protected DateInput eingabedatumvon = null;

  protected DateInput eingabedatumbis = null;

  protected DateInput abbuchungsdatumvon = null;

  protected DateInput abbuchungsdatumbis = null;

  
  public enum Mitgliedstyp {
    MITGLIED,
    NICHTMITGLIED,
    NOT_USED
  }
  
  
  public FilterControl(AbstractView view)
  {
    super(view);
  }
  
  public void init(String settingsprefix, String additionalparamprefix1, 
      String additionalparamprefix2)
  {
    if(settingsprefix != null)
      this.settingsprefix = settingsprefix;
    if(additionalparamprefix1 != null)
      this.additionalparamprefix1 = additionalparamprefix1;
    if(additionalparamprefix2 != null)
      this.additionalparamprefix2 = additionalparamprefix2;
  }
  
  public String getSettingsprefix()
  {
    return settingsprefix;
  }
  
  public String getAdditionalparamprefix1()
  {
    return additionalparamprefix1;
  }
  
  public String getAdditionalparamprefix2()
  {
    return additionalparamprefix2;
  }
  
  /**
   * Such Input Felder
   */
  public SelectInput getSuchAdresstyp(Mitgliedstyp typ) throws RemoteException
  {
    if (suchadresstyp != null)
    {
      return suchadresstyp;
    }
    this.typ = typ;

    DBIterator<Adresstyp> at = Einstellungen.getDBService()
        .createList(Adresstyp.class);
    switch (typ)
    {
      case MITGLIED:
        at.addFilter("jvereinid = 1");
        break;
      case NICHTMITGLIED:
        at.addFilter("jvereinid != 1 or jvereinid is null");
        break;
      case NOT_USED:
        break;
    }
    at.setOrder("order by bezeichnung");

    if (typ == Mitgliedstyp.MITGLIED)
    {
      Adresstyp def = (Adresstyp) Einstellungen.getDBService()
          .createObject(Adresstyp.class, "1");
      suchadresstyp = new SelectInput(at != null ? PseudoIterator.asList(at) : null, def);
    }
    else if (typ == Mitgliedstyp.NICHTMITGLIED)
    {
      Adresstyp def = null;
      try
      {
        def = (Adresstyp) Einstellungen.getDBService().createObject(
            Adresstyp.class, settings.getString(settingsprefix + "suchadresstyp", "2"));
      }
      catch (Exception e)
      {
        def = null;
      }
      suchadresstyp = new SelectInput(at != null ? PseudoIterator.asList(at) : null, def);
    }
    else
    {
      suchadresstyp = new SelectInput(new ArrayList<>(), null);
    }
    suchadresstyp.setName("Mitgliedstyp");
    suchadresstyp.setPleaseChoose("Bitte auswählen");
    suchadresstyp.addListener(new FilterListener());
    return suchadresstyp;
  }
  
  public boolean isSuchAdresstypActive()
  {
    return suchadresstyp != null;
  }
  
  public Input getMitgliedStatus()
  {
    if (status != null)
    {
      return status;
    }
    status = new SelectInput(
        new String[] { "Angemeldet", "Abgemeldet", "An- und Abgemeldete" },
        settings.getString(settingsprefix + "status.mitglied", "Angemeldet"));
    status.setName("Mitgliedschaft");
    status.addListener(new FilterListener());
    return status;
  }

  public boolean isMitgliedStatusAktiv()
  {
    return status != null;
  }
  
  public TextInput getSuchExterneMitgliedsnummer()
  {
    if (suchexternemitgliedsnummer != null)
    {
      return suchexternemitgliedsnummer;
    }
    suchexternemitgliedsnummer = new TextInput(settings.getString(
        settingsprefix + "suchExterneMitgliedsNummer",""), 50);
    suchexternemitgliedsnummer.setName("Externe Mitgliedsnummer");
    return suchexternemitgliedsnummer;
  }
  
  public boolean isSuchExterneMitgliedsnummerActive()
  {
    return suchexternemitgliedsnummer != null;
  }
  
  public DialogInput getEigenschaftenAuswahl() throws RemoteException
  {
    String  tmp = settings.getString(settingsprefix + "eigenschaften", "");
    final EigenschaftenAuswahlDialog d = new EigenschaftenAuswahlDialog(tmp,
        false, true, this);
    d.addCloseListener(new EigenschaftenCloseListener());

    StringTokenizer stt = new StringTokenizer(tmp, ",");
    StringBuilder text = new StringBuilder();
    while (stt.hasMoreElements())
    {
      if (text.length() > 0)
      {
        text.append(", ");
      }
      try
      {
        Eigenschaft ei = (Eigenschaft) Einstellungen.getDBService()
            .createObject(Eigenschaft.class, stt.nextToken());
        text.append(ei.getBezeichnung());
      }
      catch (ObjectNotFoundException e)
      {
        //
      }
    }
    eigenschaftenabfrage = new DialogInput(text.toString(), d);
    eigenschaftenabfrage.setName("Eigenschaften");
    eigenschaftenabfrage.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {

        d.setDefaults(settings.getString(settingsprefix + "eigenschaften", ""));
      }
    });
    return eigenschaftenabfrage;
  }
  
  public boolean isEigenschaftenAuswahlAktiv()
  {
    return eigenschaftenabfrage != null;
  }
  
  public TreePart getEigenschaftenAuswahlTree(String vorbelegung,
      boolean ohnePflicht) throws RemoteException
  {
    eigenschaftenAuswahlTree = new TreePart(
        new EigenschaftenNode(vorbelegung, ohnePflicht), null);
    eigenschaftenAuswahlTree.setCheckable(true);
    eigenschaftenAuswahlTree.addSelectionListener(
        new EigenschaftListener(eigenschaftenAuswahlTree));
    eigenschaftenAuswahlTree.setFormatter(new EigenschaftTreeFormatter());
    return eigenschaftenAuswahlTree;
  }
  
  public static class EigenschaftTreeFormatter implements TreeFormatter
  {

    @Override
    public void format(TreeItem item)
    {
      EigenschaftenNode eigenschaftitem = (EigenschaftenNode) item.getData();
      if (eigenschaftitem.getNodeType() == EigenschaftenNode.ROOT
          || eigenschaftitem
              .getNodeType() == EigenschaftenNode.EIGENSCHAFTGRUPPE)
      {
        //
      }
      else
      {
        if (eigenschaftitem.getEigenschaften() != null
            || eigenschaftitem.isPreset())
        {
          item.setChecked(true);
        }
        else
        {
          item.setChecked(false);
        }
      }
    }
  }
  
  public String getEigenschaftenString()
  {
    return settings.getString(settingsprefix + "eigenschaften", "");
  }

  public String getEigenschaftenVerknuepfung()
  {
    return settings.getString(settingsprefix + "eigenschaften.verknuepfung", "und");
  }
  
  
  public SelectInput getBeitragsgruppeAusw() throws RemoteException
  {
    if (beitragsgruppeausw != null)
    {
      return beitragsgruppeausw;
    }
    Beitragsgruppe bg = null;
    String beitragsgru = settings.getString(settingsprefix + "beitragsgruppe", "");
    if (beitragsgru.length() > 0)
    {
      try
      {
        bg = (Beitragsgruppe) Einstellungen.getDBService()
            .createObject(Beitragsgruppe.class, beitragsgru);
      }
      catch (ObjectNotFoundException e)
      {
        bg = (Beitragsgruppe) Einstellungen.getDBService()
            .createObject(Beitragsgruppe.class, null);
      }
    }
    DBIterator<Beitragsgruppe> list = Einstellungen.getDBService()
        .createList(Beitragsgruppe.class);
    list.setOrder("ORDER BY bezeichnung");
    beitragsgruppeausw = new SelectInput(list != null ? PseudoIterator.asList(list) : null, bg);
    beitragsgruppeausw.setName("Beitragsgruppe");
    beitragsgruppeausw.setAttribute("bezeichnung");
    beitragsgruppeausw.setPleaseChoose("Bitte auswählen");
    beitragsgruppeausw.addListener(new FilterListener());
    return beitragsgruppeausw;
  }
  
  public boolean isBeitragsgruppeAuswAktiv()
  {
    return beitragsgruppeausw != null;
  }
  
  public TextInput getSuchname()
  {
    if (suchname != null)
    {
      return suchname;
    }
    this.suchname = new TextInput(settings.getString(settingsprefix + "suchname", ""),
          50);
    suchname.setName("Name");
    return suchname;
  }
  
  public boolean isSuchnameAktiv()
  {
    return suchname != null;
  }
  
  public GeschlechtInput getSuchGeschlecht() throws RemoteException
  {
    if (suchgeschlecht != null)
    {
      return suchgeschlecht;
    }
    suchgeschlecht = new GeschlechtInput(
        settings.getString(settingsprefix + "geschlecht", ""));
    suchgeschlecht.setName("Geschlecht");
    suchgeschlecht.setPleaseChoose("Bitte auswählen");
    suchgeschlecht.addListener(new FilterListener());
    return suchgeschlecht;
  }
  
  public boolean isSuchGeschlechtAktiv()
  {
    return suchgeschlecht != null;
  }
  
  public DateInput getDateInput(String setting)
  {

    Date d = null;
    String tmp = settings.getString(settingsprefix + setting, null);
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
    return new DateInput(d, new JVDateFormatTTMMJJJJ());
  }
  
  public DateInput getStichtag()
  {
    if (stichtag != null)
    {
      return stichtag;
    }
    stichtag = getDateInput("stichtag");
    stichtag.setName("Stichtag");
    return stichtag;
  }
  
  public DateInput getStichtag(boolean jahresende)
  {
    if (stichtag != null)
    {
      return stichtag;
    }
    Date d = new Date();
    if (jahresende)
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MONTH, Calendar.DECEMBER);
      cal.set(Calendar.DAY_OF_MONTH, 31);
      d = new Date(cal.getTimeInMillis());
    }
    this.stichtag = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.stichtag.setTitle("Stichtag");
    this.stichtag.setName("Stichtag");
    return stichtag;
  }
  
  public boolean isStichtagAktiv()
  {
    return stichtag != null;
  }
  
  public DateInput getGeburtsdatumvon()
  {
    if (geburtsdatumvon != null)
    {
      return geburtsdatumvon;
    }
    geburtsdatumvon = getDateInput("geburtsdatumvon");
    geburtsdatumvon.setName("Geburtsdatum von");
    return geburtsdatumvon;
  }
  
  public boolean isGeburtsdatumvonAktiv()
  {
    return geburtsdatumvon != null;
  }
  
  public DateInput getGeburtsdatumbis()
  {
    if (geburtsdatumbis != null)
    {
      return geburtsdatumbis;
    }
    geburtsdatumbis = getDateInput("geburtsdatumbis");
    geburtsdatumbis.setName("Geburtsdatum bis");
    return geburtsdatumbis;
  }
  
  public boolean isGeburtsdatumbisAktiv()
  {
    return geburtsdatumbis != null;
  }

  public DateInput getSterbedatumvon()
  {
    if (sterbedatumvon != null)
    {
      return sterbedatumvon;
    }
    sterbedatumvon = getDateInput("sterbedatumvon");
    sterbedatumvon.setName("Sterbedatum von");
    return sterbedatumvon;
  }
  
  public boolean isSterbedatumvonAktiv()
  {
    return sterbedatumvon != null;
  }

  public DateInput getSterbedatumbis()
  {
    if (sterbedatumbis != null)
    {
      return sterbedatumbis;
    }
    sterbedatumbis = getDateInput("sterbedatumbis");
    sterbedatumbis.setName("Sterbedatum bis");
    return sterbedatumbis;
  }
  
  public boolean isSterbedatumbisAktiv()
  {
    return sterbedatumbis != null;
  }

  public DateInput getEintrittvon()
  {
    if (eintrittvon != null)
    {
      return eintrittvon;
    }
    eintrittvon = getDateInput("eintrittvon");
    eintrittvon.setName("Eintrittsdatum von");
    return eintrittvon;
  }

  public boolean isEintrittvonAktiv()
  {
    return eintrittvon != null;
  }

  public DateInput getEintrittbis()
  {
    if (eintrittbis != null)
    {
      return eintrittbis;
    }
    eintrittbis = getDateInput("eintrittbis");
    eintrittbis.setName("Eintrittsdatum bis");
    return eintrittbis;
  }

  public boolean isEintrittbisAktiv()
  {
    return eintrittbis != null;
  }

  public DateInput getAustrittvon()
  {
    if (austrittvon != null)
    {
      return austrittvon;
    }
    austrittvon = getDateInput("austrittvon");
    austrittvon.setName("Austrittsdatum von");
    return austrittvon;
  }

  public boolean isAustrittvonAktiv()
  {
    return austrittvon != null;
  }

  public DateInput getAustrittbis()
  {
    if (austrittbis != null)
    {
      return austrittbis;
    }
    austrittbis = getDateInput("austrittbis");
    austrittbis.setName("Austrittsdatum bis");
    return austrittbis;
  }
  
  public boolean isAustrittbisAktiv()
  {
    return austrittbis != null;
  }

  public DialogInput getZusatzfelderAuswahl()
  {
    if (zusatzfelderabfrage != null)
    {
      return zusatzfelderabfrage;
    }
    zad = new ZusatzfelderAuswahlDialog(settings, additionalparamprefix1, additionalparamprefix2);
    zad.addCloseListener(new ZusatzfelderListener());

    zusatzfelderabfrage = new DialogInput("", zad);
    setZusatzfelderAuswahl();
    zusatzfelderabfrage.setName("Zusatzfelder");
    return zusatzfelderabfrage;
  }
  
  public boolean isZusatzfelderAuswahlAktiv()
  {
    return zusatzfelderabfrage != null;
  }
  
  public void setZusatzfelderAuswahl()
  {
    int selected = settings.getInt(additionalparamprefix2 + "selected", 0);
    if (selected == 0)
    {
      zusatzfelderabfrage.setText("kein Feld ausgewählt");
    }
    else if (selected == 1)
    {
      zusatzfelderabfrage.setText("1 Feld ausgewählt");
    }
    else
    {
      zusatzfelderabfrage
          .setText(String.format("%d Felder ausgewählt", selected));
    }
  }
  
  public SelectInput getMailauswahl() throws RemoteException
  {
    if (mailAuswahl != null)
    {
      return mailAuswahl;
    }
    mailAuswahl = new MailAuswertungInput(settings.getInt(settingsprefix + "mailauswahl", 1));
    mailAuswahl.setName("Mail");
    mailAuswahl.addListener(new FilterListener());
    return mailAuswahl;
  }
  
  public boolean isMailauswahlAktiv()
  {
    return mailAuswahl != null;
  }
  
  public DateInput getDatumvon()
  {
    if (datumvon != null)
    {
      return datumvon;
    }
    datumvon = getDateInput("datumvon");
    datumvon.setName("Datum von");
    return datumvon;
  }
  
  public boolean isDatumvonAktiv()
  {
    return datumvon != null;
  }
  
  public DateInput getDatumbis()
  {
    if (datumbis != null)
    {
      return datumbis;
    }
    datumbis = getDateInput("datumbis");
    datumbis.setName("Datum bis");
    return datumbis;
  }
  
  public boolean isDatumbisAktiv()
  {
    return datumbis != null;
  }
  
  public SelectInput getDifferenz()
  {
    if (differenz != null)
    {
      return differenz;
    }
    DIFFERENZ defaultwert = DIFFERENZ
        .fromString(settings.getString(settingsprefix + "differenz", DIFFERENZ.EGAL.toString()));
    return getDifferenz(defaultwert);
  }
  
  public SelectInput getDifferenz(DIFFERENZ defaultvalue)
  {
    differenz = new SelectInput(DIFFERENZ.values(), defaultvalue);
    differenz.setName("Differenz");
    differenz.addListener(new FilterListener());
    return differenz;
  }
  
  public boolean isDifferenzAktiv()
  {
    return differenz != null;
  }
  
  public CheckboxInput getOhneAbbucher()
  {
    if (ohneabbucher != null)
    {
      return ohneabbucher;
    }
    ohneabbucher = new CheckboxInput(settings.getBoolean(settingsprefix + "ohneabbucher", false));
    return ohneabbucher;
  }
  
  public boolean isOhneAbbucher()
  {
    return ohneabbucher != null;
  }
  
  public SelectInput getSuchLehrgangsart() throws RemoteException
  {
    if (suchlehrgangsart != null)
    {
      return suchlehrgangsart;
    }
    DBIterator<Lehrgangsart> it = Einstellungen.getDBService()
        .createList(Lehrgangsart.class);
    it.setOrder("order by bezeichnung");
    Lehrgangsart letztesuche = null;
    try
    {
      letztesuche = (Lehrgangsart) Einstellungen.getDBService().createObject(
          Lehrgangsart.class, settings.getString(settingsprefix + "suchlehrgangsart", null));
    }
    catch (ObjectNotFoundException e)
    {
      //
    }
    suchlehrgangsart = new SelectInput(it != null ? PseudoIterator.asList(it) : null, letztesuche);
    suchlehrgangsart.setPleaseChoose("Bitte auswählen");
    suchlehrgangsart.addListener(new FilterListener());
    suchlehrgangsart.setName("Lehrgangsart");
    return suchlehrgangsart;
  }
  
  public boolean isSuchLehrgangsartAktiv()
  {
    return suchlehrgangsart != null;
  }
  
  public DateInput getEingabedatumvon()
  {
    if (eingabedatumvon != null)
    {
      return eingabedatumvon;
    }
    eingabedatumvon = getDateInput("eingabedatum.von");
    eingabedatumvon.setName("Eingabedatum von");
    return eingabedatumvon;
  }

  public boolean isEingabedatumvonAktiv()
  {
    return eingabedatumvon != null;
  }
  
  public DateInput getEingabedatumbis()
  {
    if (eingabedatumbis != null)
    {
      return eingabedatumbis;
    }
    eingabedatumbis = getDateInput("eingabedatum.bis");
    eingabedatumbis.setName("Eingabedatum bis");
    return eingabedatumbis;
  }

  public boolean isEingabedatumbisAktiv()
  {
    return eingabedatumbis != null;
  }
  
  public DateInput getAbbuchungsdatumvon()
  {
    if (abbuchungsdatumvon != null)
    {
      return abbuchungsdatumvon;
    }
    abbuchungsdatumvon = getDateInput("abbuchungsdatum.von");
    abbuchungsdatumvon.setName("Abbuchungsdatum von");
    return abbuchungsdatumvon;
  }

  public boolean isAbbuchungsdatumvonAktiv()
  {
    return abbuchungsdatumvon != null;
  }
  
  public DateInput getAbbuchungsdatumbis()
  {
    if (abbuchungsdatumbis != null)
    {
      return abbuchungsdatumbis;
    }
    abbuchungsdatumbis = getDateInput("abbuchungsdatum.bis");
    abbuchungsdatumbis.setName("Abbuchungsdatum bis");
    return abbuchungsdatumbis;
  }

  public boolean isAbbuchungsdatumbisAktiv()
  {
    return abbuchungsdatumbis != null;
  }
  
  /**
   * Buttons
   */
  public Button getSuchenButton()
  {
    Button b = new Button("Suchen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        refresh();
      }
    }, null, true, "search.png");
    return b;
  }
  
  public Button getSpeichernButton()
  {
    Button b = new Button("Filter-Speichern", new Action()
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
          Logger.error("Fehler", e);
        }
      }
    }, null, false, "document-save.png");
    return b;
  }
  
  public Button getResetButton()
  {
    return new Button("Filter-Reset", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        settings.setAttribute("id", "");
        settings.setAttribute("profilname", "");

        if (suchadresstyp != null && typ != Mitgliedstyp.MITGLIED)
          suchadresstyp.setValue(null);
        if (status != null)
          status.setValue("Angemeldet");
        if (suchexternemitgliedsnummer != null)
          suchexternemitgliedsnummer.setValue("");
        if (eigenschaftenabfrage != null)
        {
          settings.setAttribute(settingsprefix + "eigenschaften", "");
          settings.setAttribute(settingsprefix + "eigenschaften.verknuepfung", "und");
          eigenschaftenabfrage.setText("");
          eigenschaftenabfrage.getControl().redraw();
        }
        if (beitragsgruppeausw != null)
          beitragsgruppeausw.setValue(null);
        if (suchname != null)
          suchname.setValue("");
        if(suchgeschlecht != null)
          suchgeschlecht.setValue(null);
        if (stichtag != null)
          stichtag.setValue(null);
        if (geburtsdatumvon != null)
          geburtsdatumvon.setValue(null);
        if (geburtsdatumbis != null)
          geburtsdatumbis.setValue(null);
        if (sterbedatumvon != null)
          sterbedatumvon.setValue(null);
        if (sterbedatumbis != null)
          sterbedatumbis.setValue(null);
        if (eintrittvon != null)
          eintrittvon.setValue(null);
        if (eintrittbis != null)
          eintrittbis.setValue(null);
        if (austrittvon != null)
          austrittvon.setValue(null);
        if (austrittbis != null)
          austrittbis.setValue(null);
        if (zusatzfelderabfrage != null)
        {
          settings.setAttribute(additionalparamprefix2 + "selected", 0);
          setZusatzfelderAuswahl();
          zad.reset();
        }
        if (mailAuswahl != null)
          mailAuswahl.setValue(null);
        if (datumbis != null)
          datumbis.setValue(null);
        if (datumvon != null)
          datumvon.setValue(null);
        if (differenz != null)
          differenz.setValue(DIFFERENZ.EGAL);
        if (ohneabbucher != null)
          ohneabbucher.setValue(Boolean.FALSE);
        if (suchlehrgangsart != null)
          suchlehrgangsart.setValue(null);
        if (eingabedatumvon != null)
          eingabedatumvon.setValue(null);
        if (eingabedatumbis != null)
          eingabedatumbis.setValue(null);
        if (abbuchungsdatumvon != null)
          abbuchungsdatumvon.setValue(null);
        if (abbuchungsdatumbis != null)
          abbuchungsdatumbis.setValue(null);
        refresh();
      }
    }, null, false, "eraser.png");
  }
  
  protected void refresh()
  {
    try
    {
      saveFilterSettings();
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    TabRefresh();
  }
  
  protected void TabRefresh()
  {
  }
  
  /**
   * Listener
   */
  private class ZusatzfelderListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      setZusatzfelderAuswahl();
      refresh();
    }
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
      refresh();
    }
  }
  
  static class EigenschaftListener implements Listener
  {

    private TreePart tree;

    public EigenschaftListener(TreePart tree)
    {
      this.tree = tree;
    }

    @Override
    public void handleEvent(Event event)
    {
      // "o" ist das Objekt, welches gerade markiert
      // wurde oder die Checkbox geaendert wurde.
      GenericObjectNode o = (GenericObjectNode) event.data;

      // Da der Listener sowohl dann aufgerufen wird,j
      // nur nur eine Zeile selektiert wurde als auch,
      // wenn die Checkbox geaendert wurde, musst du jetzt
      // noch ersteres ausfiltern - die Checkboxen sollen
      // ja nicht geaendert werden, wenn nur eine Zeile
      // selektiert aber die Checkbox nicht geaendert wurde.
      // Hierzu schreibe ich in event.detail einen Int-Wert.
      // event.detail = -1 // Nur selektiert
      // event.detail = 1 // Checkbox aktiviert
      // event.detail = 0 // Checkbox deaktiviert

      // Folgende Abfrage deaktiviert wegen Problemen mit Windows
      // if (event.detail == -1)
      // {
      // return;
      // }
      try
      {
        if (o.getChildren() == null)
        {
          return;
        }
        List<?> children = PseudoIterator.asList(o.getChildren());
        boolean b = event.detail > 0;
        tree.setChecked(children.toArray(new Object[children.size()]), b);
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
      }
    }
  }
  
  /**
   * Listener, der die Auswahl der Eigenschaften ueberwacht.
   */
  private class EigenschaftenCloseListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      if (event == null || event.data == null)
      {
        return;
      }
      EigenschaftenAuswahlParameter param = (EigenschaftenAuswahlParameter) event.data;
      StringBuilder id = new StringBuilder();
      StringBuilder text = new StringBuilder();
      for (Object o : param.getEigenschaften())
      {
        if (text.length() > 0)
        {
          id.append(",");
          text.append(", ");
        }
        EigenschaftenNode node = (EigenschaftenNode) o;
        try
        {
          id.append(node.getEigenschaft().getID());
          text.append(node.getEigenschaft().getBezeichnung());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      eigenschaftenabfrage.setText(text.toString());
      settings.setAttribute(settingsprefix + "eigenschaften", id.toString());
      settings.setAttribute(settingsprefix + "eigenschaften.verknuepfung",
          param.getVerknuepfung());
      refresh();
    }
  }

  /**
   * Default-Werte für die MitgliederSuchView speichern.
   * 
   * @throws RemoteException
   */
  public void saveFilterSettings() throws RemoteException
  {
    if (suchadresstyp != null)
    {
      Adresstyp tmp = (Adresstyp) suchadresstyp.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchadresstyp", tmp.getID());
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchadresstyp", "");
      }
    }
    
    if (status != null)
    {
      String tmp = (String) status.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "status.mitglied", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "status.mitglied", "");
      }
    }
    
    if (suchexternemitgliedsnummer != null)
    {
      String tmp = (String) suchexternemitgliedsnummer.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchExterneMitgliedsNummer", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchExterneMitgliedsNummer", "");
      }
    }

    if (beitragsgruppeausw != null)
    {
      Beitragsgruppe tmpbg = (Beitragsgruppe) beitragsgruppeausw.getValue();
      if (tmpbg != null)
      {
        settings.setAttribute(settingsprefix + "beitragsgruppe", tmpbg.getID());
      }
      else
      {
        settings.setAttribute(settingsprefix + "beitragsgruppe", "");
      }
    }
    
    if (suchname != null)
    {
      String tmp = (String) suchname.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchname", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchname", "");
      }
    }
    
    if (suchgeschlecht != null)
    {
      String tmp = (String) suchgeschlecht.getValue();
      if (tmp != null && !getSuchGeschlecht().getText().equals("Bitte auswählen"))
      {
        settings.setAttribute(settingsprefix + "geschlecht", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "geschlecht", "");
      }
    }

    if (stichtag != null)
    {
      saveDate((Date) stichtag.getValue(), "stichtag");
    }

    if (geburtsdatumvon != null)
    {
      saveDate( (Date) geburtsdatumvon.getValue(), "geburtsdatumvon");
    }
    
    if (geburtsdatumbis != null)
    {
      saveDate( (Date) geburtsdatumbis.getValue(), "geburtsdatumbis");
    }

    if (sterbedatumvon != null)
    {
      saveDate( (Date) sterbedatumvon.getValue(), "sterbedatumvon");
    }

    if (sterbedatumbis != null)
    {
      saveDate( (Date) sterbedatumbis.getValue(), "sterbedatumbis");
    }

    if (eintrittvon != null)
    {
      saveDate( (Date) eintrittvon.getValue(), "eintrittvon");
    }

    if (eintrittbis != null)
    {
      saveDate( (Date) eintrittbis.getValue(), "eintrittbis");
    }

    if (austrittvon != null)
    {
      saveDate( (Date) austrittvon.getValue(), "austrittvon");
    }

    if (austrittbis != null)
    {
      saveDate( (Date) austrittbis.getValue(), "austrittbis");
    }
    
    if (mailAuswahl != null)
    {
      Integer tmp = (Integer) mailAuswahl.getValue();
      if (tmp != null)
      {
      settings.setAttribute(settingsprefix + "mailauswahl", tmp.toString());
      }
      else
      {
        settings.setAttribute(settingsprefix + "mailauswahl", "1");
      }
    }
    
    if (datumvon != null)
    {
      saveDate( (Date) datumvon.getValue(), "datumvon");
    }
    
    if (datumbis != null)
    {
      saveDate( (Date) datumbis.getValue(), "datumbis");
    }
    
    if (differenz != null)
    {
      String tmp = differenz.getValue().toString();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "differenz", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "differenz", "");
      }
    }
    
    if (ohneabbucher != null)
    {
      Boolean tmp = (Boolean) ohneabbucher.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "ohneabbucher", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "ohneabbucher", "false");
      }
    }

    if (suchlehrgangsart != null)
    {
      Lehrgangsart la = (Lehrgangsart) getSuchLehrgangsart().getValue();
      if (la != null)
      {
        settings.setAttribute(settingsprefix + "suchlehrgangsart", la.getID());
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchlehrgangsart", "");
      }
    }
    
    if (eingabedatumvon != null)
    {
      saveDate( (Date) eingabedatumvon.getValue(), "eingabedatum.von");
    }
    
    if (eingabedatumbis != null)
    {
      saveDate( (Date) eingabedatumbis.getValue(), "eingabedatum.bis");
    }
    
    if (abbuchungsdatumvon != null)
    {
      saveDate( (Date) abbuchungsdatumvon.getValue(), "abbuchungsdatum.von");
    }
    
    if (abbuchungsdatumbis != null)
    {
      saveDate( (Date) abbuchungsdatumbis.getValue(), "abbuchungsdatum.bis");
    }
    
  }
  
  private void saveDate(Date tmp, String setting)
  {
    if (tmp != null)
    {
      settings.setAttribute(settingsprefix + setting,
          new JVDateFormatTTMMJJJJ().format(tmp));
    }
    else
    {
      settings.setAttribute(settingsprefix + setting, "");
    }
  }
}
