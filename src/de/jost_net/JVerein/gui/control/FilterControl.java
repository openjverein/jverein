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
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlParameter;
import de.jost_net.JVerein.gui.dialogs.ZusatzfelderAuswahlDialog;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Eigenschaft;
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
  private String settingsprefix = "";

  // String für Zusatzfelder
  private String additionalparamprefix1 = "";

  // String für Zusatfelder Anzahl
  private String additionalparamprefix2 = "";

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
   * 
   * @param Mitgliedstyp
   * @throws RemoteException
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
        settings.getString("status.mitglied", "Angemeldet"));
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
  
  public DateInput getStichtag()
  {
    if (stichtag != null)
    {
      return stichtag;
    }
    Date d = null;
    String tmp = settings.getString(settingsprefix + "stichtag", null);
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
    this.stichtag = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.stichtag.setTitle("Stichtag");
    this.stichtag.setText("Stichtag");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "geburtsdatumvon", null);
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
    this.geburtsdatumvon = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.geburtsdatumvon.setTitle("Geburtsdatum");
    this.geburtsdatumvon.setText("Beginn des Geburtszeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "geburtsdatumbis", null);
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
    this.geburtsdatumbis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.geburtsdatumbis.setTitle("Geburtsdatum");
    this.geburtsdatumbis.setText("Ende des Geburtszeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "sterbedatumvon", null);
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
    this.sterbedatumvon = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.sterbedatumvon.setTitle("Sterbedatum");
    this.sterbedatumvon.setText("Beginn des Sterbezeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "sterbedatumbis", null);
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
    this.sterbedatumbis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.sterbedatumbis.setTitle("Sterbedatum");
    this.sterbedatumbis.setText("Ende des Sterbezeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "eintrittvon", null);
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
    this.eintrittvon = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.eintrittvon.setTitle("Eintrittsdatum");
    this.eintrittvon.setText("Beginn des Eintrittszeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "eintrittbis", null);
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
    this.eintrittbis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.eintrittbis.setTitle("Eintrittsdatum");
    this.eintrittbis.setText("Ende des Eintrittszeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "austrittvon", null);
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
    this.austrittvon = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.austrittvon.setTitle("Austrittsdatum");
    this.austrittvon.setText("Beginn des Austrittszeitraumes");
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
    Date d = null;
    String tmp = settings.getString(settingsprefix + "austrittbis", null);
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
    this.austrittbis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.austrittbis.setTitle("Austrittsdatum");
    this.austrittbis.setText("Ende des Austrittszeitraumes");
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
   * Listener, der die Auswahl der Zusatzfelder ueberwacht.
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
        settings.setAttribute("status.mitglied", tmp);
      }
      else
      {
        settings.setAttribute("status.mitglied", "");
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
      Date tmp = (Date) stichtag.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "stichtag",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "stichtag", "");
      }
    }

    if (geburtsdatumvon != null)
    {
      Date tmp = (Date) geburtsdatumvon.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "geburtsdatumvon",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "geburtsdatumvon", "");
      }
    }
    
    if (geburtsdatumbis != null)
    {
      Date tmp = (Date) geburtsdatumbis.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "geburtsdatumbis",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "geburtsdatumbis", "");
      }
    }

    if (sterbedatumvon != null)
    {
      Date tmp = (Date) sterbedatumvon.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "sterbedatumvon",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "sterbedatumvon", "");
      }
    }

    if (sterbedatumbis != null)
    {
      Date tmp = (Date) sterbedatumbis.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "sterbedatumbis",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "sterbedatumbis", "");
      }
    }

    if (eintrittvon != null)
    {
      Date tmp = (Date) eintrittvon.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "eintrittvon",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "eintrittvon", "");
      }
    }

    if (eintrittbis != null)
    {
      Date tmp = (Date) eintrittbis.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "eintrittbis",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "eintrittbis", "");
      }
    }

    if (austrittvon != null)
    {
      Date tmp = (Date) austrittvon.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "austrittvon",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "austrittvon", "");
      }
    }

    if (austrittbis != null)
    {
      Date tmp = (Date) austrittbis.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "austrittbis",
            new JVDateFormatTTMMJJJJ().format(tmp));
      }
      else
      {
        settings.setAttribute(settingsprefix + "austrittbis", "");
      }
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
  }
}
