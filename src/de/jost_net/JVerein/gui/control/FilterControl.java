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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.control.SollbuchungControl.DIFFERENZ;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlDialog;
import de.jost_net.JVerein.gui.dialogs.EigenschaftenAuswahlParameter;
import de.jost_net.JVerein.gui.dialogs.ZusatzfelderAuswahlDialog;
import de.jost_net.JVerein.gui.input.GeschlechtInput;
import de.jost_net.JVerein.gui.input.IntegerNullInput;
import de.jost_net.JVerein.gui.input.MailAuswertungInput;
import de.jost_net.JVerein.gui.parts.ToolTipButton;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.keys.SuchSpendenart;
import de.jost_net.JVerein.keys.Vorlageart;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Eigenschaft;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.server.EigenschaftenNode;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class FilterControl extends VorZurueckControl
{
  public final static String ALLE = "Alle";

  // String für allgemeine Settings z.B. settings1
  protected String settingsprefix = "";

  // String für Zusatzfelder
  protected String additionalparamprefix1 = "";

  // String für Zusatfelder Anzahl
  protected String additionalparamprefix2 = "";

  protected Settings settings = null;

  protected Mitgliedstypen typ = Mitgliedstypen.NOT_USED;

  protected TreePart eigenschaftenAuswahlTree = null;

  protected SelectInput suchmitgliedstyp = null;

  protected SelectInput status = null;

  protected SelectInput art = null;

  protected TextInput suchexternemitgliedsnummer = null;

  protected IntegerNullInput suchmitgliedsnummer = null;

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

  protected ZusatzfelderAuswahlDialog zad = null;

  protected DateInput datumvon = null;

  protected DateInput datumbis = null;

  protected SelectInput differenz = null;

  protected CheckboxInput ohneabbucher = null;

  protected SelectInput suchlehrgangsart = null;

  protected DateInput eingabedatumvon = null;

  protected DateInput eingabedatumbis = null;

  protected DateInput abbuchungsdatumvon = null;

  protected DateInput abbuchungsdatumbis = null;

  protected TextInput suchtext = null;

  protected SelectInput abrechnungslaufausw = null;

  protected IntegerNullInput integerausw = null;

  protected SelectInput suchstatus = null;

  protected SelectInput suchbuchungsklasse = null;

  protected SelectInput suchbuchungsartart = null;

  protected SelectInput suchkontoart = null;

  protected DecimalInput doubleauswahl = null;

  protected CheckboxInput checkboxauswahl = null;

  protected SelectInput vorlagenart = null;

  private Calendar calendar = Calendar.getInstance();

  private enum RANGE
  {
    MONAT,
    TAG
  }

  protected SelectInput suchspendenart = null;

  public enum Mitgliedstypen
  {
    MITGLIED,
    NICHTMITGLIED,
    NOT_USED,
    ALLE
  }

  public FilterControl(AbstractView view)
  {
    super(view);
  }

  public void init(String settingsprefix, String additionalparamprefix1,
      String additionalparamprefix2)
  {
    if (settingsprefix != null)
      this.settingsprefix = settingsprefix;
    if (additionalparamprefix1 != null)
      this.additionalparamprefix1 = additionalparamprefix1;
    if (additionalparamprefix2 != null)
      this.additionalparamprefix2 = additionalparamprefix2;
  }

  public Settings getSettings()
  {
    return settings;
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
  public SelectInput getSuchMitgliedstyp(Mitgliedstypen typ)
      throws RemoteException
  {
    if (suchmitgliedstyp != null)
    {
      return suchmitgliedstyp;
    }
    this.typ = typ;

    DBIterator<Mitgliedstyp> mtIt = Einstellungen.getDBService()
        .createList(Mitgliedstyp.class);
    switch (typ)
    {
      case MITGLIED:
        mtIt.addFilter(Mitgliedstyp.JVEREINID + " = " + Mitgliedstyp.MITGLIED);
        break;
      case NICHTMITGLIED:
        mtIt.addFilter(Mitgliedstyp.JVEREINID + " != " + Mitgliedstyp.MITGLIED
            + " OR " + Mitgliedstyp.JVEREINID + " IS NULL");
        break;
      case NOT_USED:
      case ALLE:
        break;
    }
    mtIt.setOrder("order by " + Mitgliedstyp.BEZEICHNUNG);

    if (typ == Mitgliedstypen.MITGLIED)
    {
      Mitgliedstyp mt = (Mitgliedstyp) Einstellungen.getDBService()
          .createObject(Mitgliedstyp.class, Mitgliedstyp.MITGLIED);
      suchmitgliedstyp = new SelectInput(
          mtIt != null ? PseudoIterator.asList(mtIt) : null, mt);
    }
    else if (typ == Mitgliedstypen.NICHTMITGLIED || typ == Mitgliedstypen.ALLE)
    {
      Mitgliedstyp mt = null;
      try
      {
        mt = (Mitgliedstyp) Einstellungen.getDBService().createObject(
            Mitgliedstyp.class,
            settings.getString(settingsprefix + "suchadresstyp", "2"));
      }
      catch (Exception e)
      {
        mt = null;
      }
      suchmitgliedstyp = new SelectInput(
          mtIt != null ? PseudoIterator.asList(mtIt) : null, mt);
    }
    else
    {
      suchmitgliedstyp = new SelectInput(new ArrayList<>(), null);
    }
    suchmitgliedstyp.setName("Mitgliedstyp");
    suchmitgliedstyp.setPleaseChoose(ALLE);
    suchmitgliedstyp.addListener(new FilterListener());
    return suchmitgliedstyp;
  }

  public boolean isSuchMitgliedstypActive()
  {
    return suchmitgliedstyp != null;
  }

  public SelectInput getMitgliedStatus()
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

  public Input getMitgliedArt()
  {
    if (art != null)
    {
      return art;
    }
    art = new SelectInput(new String[] { "Mitglied", "Nicht-Mitglied" },
        settings.getString(settingsprefix + "status.art", ""));
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
      {
        art = new SelectInput(
            new String[] { "Mitglied", "Nicht-Mitglied", "Kursteilnehmer" },
            settings.getString(settingsprefix + "status.art", ""));
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler beim lesen der Einstellungen");
    }
    art.setName("Mitgliedsart");
    art.setPleaseChoose(ALLE);
    art.addListener(new FilterListener());
    return art;
  }

  public boolean isMitgliedArtAktiv()
  {
    return art != null;
  }

  public IntegerNullInput getSuchMitgliedsnummer()
  {
    if (suchmitgliedsnummer != null)
    {
      return suchmitgliedsnummer;
    }
    String tmp = settings.getString(settingsprefix + "suchMitgliedsNummer", "");
    if (tmp != null && !tmp.isEmpty())
    {
      suchmitgliedsnummer = new IntegerNullInput(Integer.parseInt(tmp));
    }
    else
    {
      suchmitgliedsnummer = new IntegerNullInput();
    }
    suchmitgliedsnummer.setName("Mitgliedsnummer");
    return suchmitgliedsnummer;
  }

  public boolean isSuchMitgliedsnummerActive()
  {
    return suchmitgliedsnummer != null;
  }

  public TextInput getSuchExterneMitgliedsnummer()
  {
    if (suchexternemitgliedsnummer != null)
    {
      return suchexternemitgliedsnummer;
    }
    suchexternemitgliedsnummer = new TextInput(
        settings.getString(settingsprefix + "suchExterneMitgliedsNummer", ""),
        50);
    suchexternemitgliedsnummer.setName("Externe Mitgliedsnummer");
    return suchexternemitgliedsnummer;
  }

  public boolean isSuchExterneMitgliedsnummerActive()
  {
    return suchexternemitgliedsnummer != null;
  }

  public DialogInput getEigenschaftenAuswahl() throws RemoteException
  {
    String tmp = settings.getString(settingsprefix + "eigenschaften", "");
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
        String s = stt.nextToken();
        String eigenschaftId = s.substring(0, s.length() - 1);
        String plusMinus = s.substring(s.length() - 1);
        if (eigenschaftId.isEmpty()
            || !(plusMinus.equals(EigenschaftenNode.PLUS)
                || plusMinus.equals(EigenschaftenNode.MINUS)))
        {
          text = new StringBuilder();
          tmp = "";
          settings.setAttribute(settingsprefix + "eigenschaften", tmp);
          break;
        }
        String prefix = "+";
        if (plusMinus.equals(EigenschaftenNode.MINUS))
          prefix = "-";
        Eigenschaft ei = (Eigenschaft) Einstellungen.getDBService()
            .createObject(Eigenschaft.class, eigenschaftId);
        text.append(prefix + ei.getBezeichnung());
      }
      catch (ObjectNotFoundException e)
      {
        //
      }
    }
    final EigenschaftenAuswahlDialog d = new EigenschaftenAuswahlDialog(tmp,
        true, this, false);
    d.addCloseListener(new EigenschaftenCloseListener());
    eigenschaftenabfrage = new DialogInput(text.toString(), d);
    eigenschaftenabfrage.setName("Eigenschaften");
    eigenschaftenabfrage.disableClientControl();
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

  public void updateEigenschaftenAuswahlTooltip()
  {
    eigenschaftenabfrage.getControl()
        .setToolTipText(eigenschaftenabfrage.getText());
  }

  public TreePart getEigenschaftenAuswahlTree(String vorbelegung,
      boolean onlyChecked, Mitglied[] mitglieder) throws RemoteException
  {
    eigenschaftenAuswahlTree = new TreePart(
        new EigenschaftenNode(vorbelegung, onlyChecked, mitglieder), null);
    eigenschaftenAuswahlTree.addSelectionListener(new EigenschaftListener());
    eigenschaftenAuswahlTree.setFormatter(new EigenschaftTreeFormatter());
    return eigenschaftenAuswahlTree;
  }

  public static class EigenschaftTreeFormatter implements TreeFormatter
  {

    @Override
    public void format(TreeItem item)
    {
      EigenschaftenNode eigenschaftitem = (EigenschaftenNode) item.getData();
      if (eigenschaftitem.getNodeType() == EigenschaftenNode.ROOT)
      {
        item.setImage(SWTUtil.getImage("document-properties.png"));
      }
      else if (eigenschaftitem
          .getNodeType() == EigenschaftenNode.EIGENSCHAFTGRUPPE)
      {
        try
        {
          boolean pflicht = eigenschaftitem.getEigenschaftGruppe().getPflicht();
          boolean maxeins = eigenschaftitem.getEigenschaftGruppe().getMax1();
          if (pflicht && maxeins)
          {
            item.setImage(SWTUtil.getImage("pflicht-maxeins.png"));
          }
          if (pflicht && !maxeins)
          {
            item.setImage(SWTUtil.getImage("pflicht.png"));
          }
          if (!pflicht && maxeins)
          {
            item.setImage(SWTUtil.getImage("maxeins.png"));
          }
        }
        catch (RemoteException e)
        {

        }
      }
      else
      {
        if (eigenschaftitem.getPreset().equals(EigenschaftenNode.PLUS))
        {
          item.setImage(SWTUtil.getImage("list-add.png"));
        }
        else if (eigenschaftitem.getPreset().equals(EigenschaftenNode.MINUS))
        {
          item.setImage(SWTUtil.getImage("list-remove.png"));
        }
        else if (eigenschaftitem.getPreset().equals(EigenschaftenNode.CHECKED))
        {
          item.setImage(SWTUtil.getImage("tree-checked.png"));
        }
        else if (eigenschaftitem.getPreset()
            .equals(EigenschaftenNode.CHECKED_PARTLY))
        {
          item.setImage(SWTUtil.getImage("tree-checked-partly.png"));
        }
        else
        {
          item.setImage(SWTUtil.getImage("tree-empty.png"));
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
    return settings.getString(settingsprefix + "eigenschaften.verknuepfung",
        "und");
  }

  public SelectInput getBeitragsgruppeAusw() throws RemoteException
  {
    if (beitragsgruppeausw != null)
    {
      return beitragsgruppeausw;
    }
    Beitragsgruppe bg = null;
    String beitragsgru = settings.getString(settingsprefix + "beitragsgruppe",
        "");
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
    beitragsgruppeausw = new SelectInput(
        list != null ? PseudoIterator.asList(list) : null, bg);
    beitragsgruppeausw.setName("Beitragsgruppe");
    beitragsgruppeausw.setAttribute("bezeichnung");
    beitragsgruppeausw.setPleaseChoose(ALLE);
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
    this.suchname = new TextInput(
        settings.getString(settingsprefix + "suchname", ""), 50);
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
    suchgeschlecht.setPleaseChoose(ALLE);
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
    zad = new ZusatzfelderAuswahlDialog(settings, additionalparamprefix1,
        additionalparamprefix2);
    zad.addCloseListener(new ZusatzfelderListener());

    zusatzfelderabfrage = new DialogInput("", zad);
    setZusatzfelderAuswahl();
    zusatzfelderabfrage.setName("Zusatzfelder");
    zusatzfelderabfrage.disableClientControl();
    return zusatzfelderabfrage;
  }

  public boolean isZusatzfelderAuswahlAktiv()
  {
    return zusatzfelderabfrage != null;
  }

  public void updateZusatzfelderAuswahlTooltip()
  {
    zusatzfelderabfrage.getControl()
        .setToolTipText(zusatzfelderabfrage.getText());
  }

  public void setZusatzfelderAuswahl()
  {
    int selected = settings.getInt(additionalparamprefix2 + "selected", 0);
    String string = "";
    if (selected == 0)
    {
      string = "Kein Feld ausgewählt";
    }
    else if (selected == 1)
    {
      string = "1 Feld ausgewählt";
    }
    else
    {
      string = String.format("%d Felder ausgewählt", selected);
    }
    zusatzfelderabfrage.setText(string);
  }

  public SelectInput getMailauswahl() throws RemoteException
  {
    if (mailAuswahl != null)
    {
      return mailAuswahl;
    }
    mailAuswahl = new MailAuswertungInput(
        settings.getInt(settingsprefix + "mailauswahl", 1));
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
    DIFFERENZ defaultwert = DIFFERENZ.fromString(settings
        .getString(settingsprefix + "differenz", DIFFERENZ.EGAL.toString()));
    return getDifferenz(defaultwert);
  }

  public SelectInput getDifferenz(DIFFERENZ defaultvalue)
  {
    differenz = new SelectInput(DIFFERENZ.values(), defaultvalue);
    differenz.setName("Differenz");
    differenz.addListener(new DifferenzListener());
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
    ohneabbucher = new CheckboxInput(
        settings.getBoolean(settingsprefix + "ohneabbucher", false));
    ohneabbucher.addListener(new FilterListener());
    return ohneabbucher;
  }

  public boolean isOhneAbbucherAktiv()
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
          Lehrgangsart.class,
          settings.getString(settingsprefix + "suchlehrgangsart", null));
    }
    catch (ObjectNotFoundException e)
    {
      //
    }
    suchlehrgangsart = new SelectInput(
        it != null ? PseudoIterator.asList(it) : null, letztesuche);
    suchlehrgangsart.setPleaseChoose(ALLE);
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

  public TextInput getSuchtext()
  {
    if (suchtext != null)
    {
      return suchtext;
    }
    this.suchtext = new TextInput(
        settings.getString(settingsprefix + "suchtext", ""), 50);
    suchtext.setName("Text");
    return suchtext;
  }

  public boolean isSuchtextAktiv()
  {
    return suchtext != null;
  }

  public SelectInput getAbrechnungslaufAusw(int anzahl) throws RemoteException
  {
    if (abrechnungslaufausw != null)
    {
      return abrechnungslaufausw;
    }
    DBIterator<Abrechnungslauf> list = Einstellungen.getDBService()
        .createList(Abrechnungslauf.class);
    list.setOrder("ORDER BY id desc");
    ArrayList<Abrechnungslauf> liste = null;
    if (list != null)
    {
      liste = new ArrayList<>();
      int count = 0;
      while (list.hasNext() && count < anzahl)
      {
        liste.add(list.next());
        count++;
      }
    }
    if (liste.size() > 0)
      abrechnungslaufausw = new SelectInput(liste, liste.get(0));
    else
      abrechnungslaufausw = new SelectInput(liste, null);
    abrechnungslaufausw.setName("Abrechnungslauf");
    abrechnungslaufausw.setAttribute("idtext");
    return abrechnungslaufausw;
  }

  public boolean isAbrechnungslaufAuswAktiv()
  {
    return abrechnungslaufausw != null;
  }

  public IntegerNullInput getIntegerAusw()
  {
    if (integerausw != null)
    {
      return integerausw;
    }
    String tmp = settings.getString(settingsprefix + "intergerauswahl", "");
    if (tmp != null && !tmp.isEmpty())
    {
      integerausw = new IntegerNullInput(Integer.parseInt(tmp));
    }
    else
    {
      integerausw = new IntegerNullInput();
    }
    integerausw.setName("Auswahl");
    return integerausw;
  }

  public boolean isIntegerAuswAktiv()
  {
    return integerausw != null;
  }

  public DecimalInput getDoubleAusw()
  {
    if (doubleauswahl != null)
    {
      return doubleauswahl;
    }
    String tmp = settings.getString(settingsprefix + "doubleauswahl", "");
    if (tmp != null && !tmp.isEmpty())
    {
      doubleauswahl = new DecimalInput(Double.parseDouble(tmp),
          Einstellungen.DECIMALFORMAT);
    }
    else
    {
      doubleauswahl = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    doubleauswahl.setName("Auswahl");

    // Falls der Input in Zusammenhang mit Differenz benutzt wird
    if (differenz != null && differenz.getValue() != null)
    {
      DIFFERENZ diff = (DIFFERENZ) differenz.getValue();
      if (diff == DIFFERENZ.FEHLBETRAG || diff == DIFFERENZ.UEBERZAHLUNG)
      {
        doubleauswahl.enable();
      }
      else
      {
        doubleauswahl.setValue(null);
        doubleauswahl.disable();
      }
    }
    return doubleauswahl;
  }

  public boolean isDoubleAuswAktiv()
  {
    return doubleauswahl != null;
  }

  public CheckboxInput getCheckboxAuswahl()
  {
    if (checkboxauswahl != null)
    {
      return checkboxauswahl;
    }
    checkboxauswahl = new CheckboxInput(
        settings.getBoolean(settingsprefix + "checkboxauswahl", false));
    checkboxauswahl.addListener(new FilterListener());
    return checkboxauswahl;
  }

  public boolean isCheckboxAuswahlAktiv()
  {
    return checkboxauswahl != null;
  }

  public SelectInput getSuchSpendenart()
  {
    if (suchspendenart != null)
    {
      return suchspendenart;
    }
    SuchSpendenart defaultwert = SuchSpendenart
        .getByKey(settings.getInt(settingsprefix + "suchspendenart.key", 1));
    suchspendenart = new SelectInput(SuchSpendenart.values(), defaultwert);
    suchspendenart.setName("Spendenart");
    suchspendenart.addListener(new FilterListener());
    return suchspendenart;
  }

  public boolean isSuchSpendenartAktiv()
  {
    return suchspendenart != null;
  }

  public SelectInput getSuchStatus(String suchstring) throws RemoteException
  {
    if (suchstatus != null)
    {
      return suchstatus;
    }
    suchstatus = new SelectInput(new String[] { ALLE, suchstring },
        settings.getString(settingsprefix + "suchstatus", ALLE));
    suchstatus.addListener(new FilterListener());
    suchstatus.setName("Status");
    return suchstatus;
  }

  public boolean isSuchStatusAktiv()
  {
    return suchstatus != null;
  }

  public SelectInput getSuchBuchungsklasse() throws RemoteException
  {
    if (suchbuchungsklasse != null)
    {
      return suchbuchungsklasse;
    }
    Buchungsklasse bk = null;
    String buchungskl = settings
        .getString(settingsprefix + "suchbuchungsklasse", "");
    if (buchungskl.length() > 0)
    {
      try
      {
        bk = (Buchungsklasse) Einstellungen.getDBService()
            .createObject(Buchungsklasse.class, buchungskl);
      }
      catch (ObjectNotFoundException e)
      {
        bk = (Buchungsklasse) Einstellungen.getDBService()
            .createObject(Buchungsklasse.class, null);
      }
    }
    DBIterator<Buchungsklasse> list = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    list.setOrder("ORDER BY bezeichnung");
    suchbuchungsklasse = new SelectInput(
        list != null ? PseudoIterator.asList(list) : null, bk);
    suchbuchungsklasse.setName("Buchungsklasse");
    suchbuchungsklasse.setAttribute("bezeichnung");
    suchbuchungsklasse.setPleaseChoose(ALLE);
    suchbuchungsklasse.addListener(new FilterListener());
    return suchbuchungsklasse;
  }

  public boolean isSuchBuchungsklasseAktiv()
  {
    return suchbuchungsklasse != null;
  }

  public SelectInput getSuchBuchungsartArt() throws RemoteException
  {
    if (suchbuchungsartart != null)
    {
      return suchbuchungsartart;
    }
    String art = settings.getString(settingsprefix + "suchbuchungsartart", "");
    ArtBuchungsart artb = null;
    if (art.length() > 0)
    {
      try
      {
        artb = new ArtBuchungsart(Integer.valueOf(art));
      }
      catch (Exception e)
      {
        //
      }
    }
    suchbuchungsartart = new SelectInput(ArtBuchungsart.getArray(), artb);
    suchbuchungsartart.setName("Art");
    suchbuchungsartart.setPleaseChoose(ALLE);
    suchbuchungsartart.addListener(new FilterListener());
    return suchbuchungsartart;
  }

  public boolean isSuchBuchungsartArtAktiv()
  {
    return suchbuchungsartart != null;
  }

  public SelectInput getSuchKontoart() throws RemoteException
  {
    if (suchkontoart != null)
    {
      return suchkontoart;
    }
    ArrayList<Kontoart> values = new ArrayList<Kontoart>(
        Arrays.asList(Kontoart.values()));
    values.remove(Kontoart.LIMIT);
    values.remove(Kontoart.LIMIT_RUECKLAGE);
    String key = settings.getString(settingsprefix + "suchkontoart.key", null);
    if (key != null && !key.isEmpty())
    {
      Kontoart defaultwert = Kontoart.getByKey(Integer.parseInt(key));
      suchkontoart = new SelectInput(values, defaultwert);
    }
    else
    {
      suchkontoart = new SelectInput(values, null);
    }
    suchkontoart.setName("Kontoart");
    suchkontoart.setPleaseChoose(ALLE);
    suchkontoart.addListener(new FilterListener());
    return suchkontoart;
  }

  public boolean isSuchKontoartAktiv()
  {
    return suchkontoart != null;
  }

  public SelectInput getVorlagenart()
  {
    if (vorlagenart != null)
    {
      return vorlagenart;
    }
    Vorlageart art = null;
    String key = settings.getString(settingsprefix + "vorlagenart.key", "");
    if (key.length() > 0)
    {
      art = Vorlageart.getByKey(Integer.parseInt(key));
    }
    vorlagenart = new SelectInput(Vorlageart.values(), art);
    vorlagenart.setName("Vorlagenart");
    vorlagenart.setPleaseChoose(ALLE);
    vorlagenart.addListener(new FilterListener());
    return vorlagenart;
  }

  public boolean isVorlagenartAktiv()
  {
    return vorlagenart != null;
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

        if (suchmitgliedstyp != null && typ != Mitgliedstypen.MITGLIED)
          suchmitgliedstyp.setValue(null);
        if (status != null)
          status.setValue("Angemeldet");
        if (art != null)
          art.setValue(null);
        if (suchexternemitgliedsnummer != null)
          suchexternemitgliedsnummer.setValue("");
        if (suchmitgliedsnummer != null)
          suchmitgliedsnummer.setValue(null);
        if (eigenschaftenabfrage != null)
        {
          settings.setAttribute(settingsprefix + "eigenschaften", "");
          settings.setAttribute(settingsprefix + "eigenschaften.verknuepfung",
              "und");
          eigenschaftenabfrage.setText("");
          eigenschaftenabfrage.getControl().redraw();
        }
        if (beitragsgruppeausw != null)
          beitragsgruppeausw.setValue(null);
        if (suchname != null)
          suchname.setValue("");
        if (suchgeschlecht != null)
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
        if (suchtext != null)
          suchtext.setValue("");
        if (integerausw != null)
          integerausw.setValue(null);
        if (suchspendenart != null)
          suchspendenart.setValue(SuchSpendenart.ALLE);
        if (suchstatus != null)
          suchstatus.setValue(ALLE);
        if (suchbuchungsklasse != null)
          suchbuchungsklasse.setValue(null);
        if (suchbuchungsartart != null)
          suchbuchungsartart.setValue(null);
        if (suchkontoart != null)
          suchkontoart.setValue(null);
        if (doubleauswahl != null)
          doubleauswahl.setValue(null);
        if (checkboxauswahl != null)
          checkboxauswahl.setValue(Boolean.FALSE);
        if (vorlagenart != null)
          vorlagenart.setValue(null);
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

  abstract protected void TabRefresh();

  /**
   * Listener
   */
  private class ZusatzfelderListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      setZusatzfelderAuswahl();
      zusatzfelderabfrage.getControl()
          .setToolTipText(zusatzfelderabfrage.getText());
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

  public class DifferenzListener implements Listener
  {

    DifferenzListener()
    {
    }

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      DIFFERENZ diff = (DIFFERENZ) getDifferenz().getValue();
      if (doubleauswahl != null && diff != null)
      {
        if (diff == DIFFERENZ.FEHLBETRAG || diff == DIFFERENZ.UEBERZAHLUNG)
        {
          doubleauswahl.enable();
        }
        else
        {
          doubleauswahl.setValue(null);
          doubleauswahl.disable();
        }
      }
      refresh();
    }
  }

  static class EigenschaftListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      // "o" ist das Objekt, welches gerade markiert wurde
      GenericObjectNode o = (GenericObjectNode) event.data;

      if (o instanceof EigenschaftenNode)
      {
        EigenschaftenNode node = (EigenschaftenNode) o;
        if (node.getNodeType() == EigenschaftenNode.EIGENSCHAFTEN)
        {
          node.incPreset();
          TreeItem item = (TreeItem) event.item;
          new EigenschaftTreeFormatter().format(item);
        }
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
      for (Object o : param.getEigenschaftenNodes())
      {
        if (text.length() > 0)
        {
          id.append(",");
          text.append(", ");
        }
        EigenschaftenNode node = (EigenschaftenNode) o;
        try
        {
          id.append(node.getEigenschaft().getID() + node.getPreset());
          String prefix = "+";
          if (node.getPreset().equals(EigenschaftenNode.MINUS))
            prefix = "-";
          text.append(prefix + node.getEigenschaft().getBezeichnung());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
      String string = text.toString();
      eigenschaftenabfrage.setText(string);
      eigenschaftenabfrage.getControl().setToolTipText(string);
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
    if (suchmitgliedstyp != null)
    {
      Mitgliedstyp tmp = (Mitgliedstyp) suchmitgliedstyp.getValue();
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

    if (art != null)
    {
      String tmp = (String) art.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "status.art", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "status.art", "");
      }
    }

    if (suchexternemitgliedsnummer != null)
    {
      String tmp = (String) suchexternemitgliedsnummer.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchExterneMitgliedsNummer",
            tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchExterneMitgliedsNummer",
            "");
      }
    }

    if (suchmitgliedsnummer != null)
    {
      Integer tmp = (Integer) suchmitgliedsnummer.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchMitgliedsNummer", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchMitgliedsNummer", "");
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
      if (tmp != null)
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
      saveDate((Date) geburtsdatumvon.getValue(), "geburtsdatumvon");
    }

    if (geburtsdatumbis != null)
    {
      saveDate((Date) geburtsdatumbis.getValue(), "geburtsdatumbis");
    }

    if (sterbedatumvon != null)
    {
      saveDate((Date) sterbedatumvon.getValue(), "sterbedatumvon");
    }

    if (sterbedatumbis != null)
    {
      saveDate((Date) sterbedatumbis.getValue(), "sterbedatumbis");
    }

    if (eintrittvon != null)
    {
      saveDate((Date) eintrittvon.getValue(), "eintrittvon");
    }

    if (eintrittbis != null)
    {
      saveDate((Date) eintrittbis.getValue(), "eintrittbis");
    }

    if (austrittvon != null)
    {
      saveDate((Date) austrittvon.getValue(), "austrittvon");
    }

    if (austrittbis != null)
    {
      saveDate((Date) austrittbis.getValue(), "austrittbis");
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
      saveDate((Date) datumvon.getValue(), "datumvon");
    }

    if (datumbis != null)
    {
      saveDate((Date) datumbis.getValue(), "datumbis");
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
      saveDate((Date) eingabedatumvon.getValue(), "eingabedatum.von");
    }

    if (eingabedatumbis != null)
    {
      saveDate((Date) eingabedatumbis.getValue(), "eingabedatum.bis");
    }

    if (abbuchungsdatumvon != null)
    {
      saveDate((Date) abbuchungsdatumvon.getValue(), "abbuchungsdatum.von");
    }

    if (abbuchungsdatumbis != null)
    {
      saveDate((Date) abbuchungsdatumbis.getValue(), "abbuchungsdatum.bis");
    }

    if (suchtext != null)
    {
      String tmp = (String) suchtext.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchtext", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchtext", "");
      }
    }

    if (integerausw != null)
    {
      Integer tmp = (Integer) integerausw.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "intergerauswahl", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "intergerauswahl", "");
      }
    }

    if (doubleauswahl != null)
    {
      Double tmp = (Double) doubleauswahl.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "doubleauswahl", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "doubleauswahl", "");
      }
    }

    if (suchspendenart != null)
    {
      SuchSpendenart ss = (SuchSpendenart) suchspendenart.getValue();
      settings.setAttribute(settingsprefix + "suchspendenart.key", ss.getKey());
    }

    if (suchstatus != null)
    {
      String tmp = (String) suchstatus.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "suchstatus", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchstatus", "");
      }
    }

    if (suchbuchungsklasse != null)
    {
      Buchungsklasse tmpbk = (Buchungsklasse) suchbuchungsklasse.getValue();
      if (tmpbk != null)
      {
        settings.setAttribute(settingsprefix + "suchbuchungsklasse",
            tmpbk.getID());
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchbuchungsklasse", "");
      }
    }

    if (suchbuchungsartart != null)
    {
      ArtBuchungsart art = (ArtBuchungsart) suchbuchungsartart.getValue();
      if (art != null)
      {
        settings.setAttribute(settingsprefix + "suchbuchungsartart",
            art.getKey());
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchbuchungsartart", "");
      }
    }

    if (suchkontoart != null)
    {
      Kontoart ka = (Kontoart) suchkontoart.getValue();
      if (ka != null)
      {
        settings.setAttribute(settingsprefix + "suchkontoart.key", ka.getKey());
      }
      else
      {
        settings.setAttribute(settingsprefix + "suchkontoart.key", "");
      }
    }

    if (checkboxauswahl != null)
    {
      Boolean tmp = (Boolean) checkboxauswahl.getValue();
      if (tmp != null)
      {
        settings.setAttribute(settingsprefix + "checkboxauswahl", tmp);
      }
      else
      {
        settings.setAttribute(settingsprefix + "checkboxauswahl", "false");
      }
    }

    if (vorlagenart != null)
    {
      Vorlageart art = (Vorlageart) vorlagenart.getValue();
      if (art != null)
      {
        settings.setAttribute(settingsprefix + "vorlagenart.key", art.getKey());
      }
      else
      {
        settings.setAttribute(settingsprefix + "vorlagenart.key", "");
      }
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

  public ToolTipButton getZurueckButton(DateInput vonDatum, DateInput bisDatum)
  {
    return new ToolTipButton("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Date von = (Date) vonDatum.getValue();
        Date bis = (Date) bisDatum.getValue();
        if (getRangeTyp(von, bis) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(von.toInstant(),
              bis.toInstant());
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.DAY_OF_MONTH, -delta);
          vonDatum.setValue(calendar.getTime());
          calendar.setTime(bis);
          calendar.add(Calendar.DAY_OF_MONTH, -delta);
          bisDatum.setValue(calendar.getTime());
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
          vonDatum.setValue(calendar.getTime());
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
          bisDatum.setValue(calendar.getTime());
        }
        refresh();
      }
    }, null, false, "go-previous.png");
  }

  public ToolTipButton getVorButton(DateInput vonDatum, DateInput bisDatum)
  {
    return new ToolTipButton("", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Date von = (Date) vonDatum.getValue();
        Date bis = (Date) bisDatum.getValue();
        if (getRangeTyp(von, bis) == RANGE.TAG)
        {
          int delta = (int) ChronoUnit.DAYS.between(von.toInstant(),
              bis.toInstant());
          delta++;
          calendar.setTime(von);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          vonDatum.setValue(calendar.getTime());
          calendar.setTime(bis);
          calendar.add(Calendar.DAY_OF_MONTH, delta);
          bisDatum.setValue(calendar.getTime());
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
          vonDatum.setValue(calendar.getTime());
          calendar.add(Calendar.MONTH, delta);
          calendar.add(Calendar.DAY_OF_MONTH, -1);
          bisDatum.setValue(calendar.getTime());
        }
        refresh();
      }
    }, null, false, "go-next.png");
  }

  private RANGE getRangeTyp(Date von, Date bis) throws ApplicationException
  {
    checkDate(von, bis);
    calendar.setTime(von);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    calendar.setTime(bis);
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    if (calendar.get(Calendar.DAY_OF_MONTH) != 1)
      return RANGE.TAG;
    return RANGE.MONAT;
  }

  private void checkDate(Date von, Date bis) throws ApplicationException
  {
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
}
