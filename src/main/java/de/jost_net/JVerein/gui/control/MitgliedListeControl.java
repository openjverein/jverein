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
import java.util.ArrayList;
import java.util.List;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Queries.MitgliedQuery;
import de.jost_net.JVerein.Queries.MitgliedQuery.MitgliedAuswahl;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.action.MitgliedDetailAction;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.dialogs.ExportDialog;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.StaatFormatter;
import de.jost_net.JVerein.gui.formatter.ZahlungsrhythmusFormatter;
import de.jost_net.JVerein.gui.formatter.ZahlungsterminFormatter;
import de.jost_net.JVerein.gui.formatter.ZahlungswegFormatter;
import de.jost_net.JVerein.gui.menu.MitgliedMenu;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.MitgliedListeView;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.gui.view.NichtMitgliedListeView;
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.EigenschaftGruppe;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MitgliedListeControl extends FilterControl
{

  private JVereinTablePart mitgliedList;

  public static MitgliedListeControl control = null;

  private boolean isMitglied = false;

  private MitgliedAuswahl mitgliedAuswahl = MitgliedAuswahl.MITGLIEDER;

  // Zeitstempel merken, wann der Letzte refresh ausgeführt wurde.
  private long lastrefresh = 0;

  public MitgliedListeControl(AbstractView view)
  {
    super(view);
    control = this;
    if (view instanceof MitgliedListeView)
    {
      isMitglied = true;
    }
  }

  public Button getExportButton()
  {
    @SuppressWarnings("unchecked")
    Button b = new Button("Export", context -> {
      try
      {
        saveFilterSettings();
        Mitgliedstyp mitgliedstyp;
        ExportDialog d;
        refresh();
        ArrayList<Mitglied> list = (ArrayList<Mitglied>) getTablePart()
            .getItems();
        if (mitgliedAuswahl == MitgliedAuswahl.MITGLIEDER)
        {
          mitgliedstyp = Einstellungen.getDBService()
              .createObject(Mitgliedstyp.class, Mitgliedstyp.MITGLIED);
        }
        else
        {
          mitgliedstyp = (Mitgliedstyp) getFilter().get(Filter.MITGLIEDSTYP);
        }
        Object[] objects = new Object[] { list, getFilterText(false),
            mitgliedstyp, getFilter() };
        /*
         * objects[0] ist ArrayList<Mitglied>, objects[1] ist der Filtertext,
         * objects[2] ist Mitgliedstyp, objects[3] ist der Filter
         */
        if (mitgliedAuswahl == MitgliedAuswahl.MITGLIEDER)
        {
          d = new ExportDialog(objects, MitgliedListeView.class,
              DokumentationUtil.MITGLIEDSUCHE, this);
        }
        else
        {
          d = new ExportDialog(objects, NichtMitgliedListeView.class,
              DokumentationUtil.ADRESSEN, this);
        }
        d.open();
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(e);
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException("Fehler beim exportieren des Reports");
      }
    }, null, false, "document-save.png");
    return b;
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    return getTablePart(null);
  }

  public JVereinTablePart getTablePart(Action detailaction)
      throws RemoteException, ApplicationException
  {
    if (mitgliedList != null)
    {
      return mitgliedList;
    }
    mitgliedList = new JVereinTablePart(
        new MitgliedQuery(this).get(mitgliedAuswahl, null), null);
    add("Status", "status", false, new Formatter()
    {
      @Override
      public String format(Object o)
      {
        return (Boolean) o ? "\u2705" : "\u2757";
      }
    }, Column.ALIGN_LEFT, true);
    add("Mitgliedsnummer", "idint", false, true);
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
      {
        add("Externe Mitgliedsnummer", "externemitgliedsnummer", false, false);
      }
    }
    catch (RemoteException re)
    {
      //
    }
    add("Kontostand", "kontostand", false, new Formatter()
    {
      @Override
      public String format(Object o)
      {
        String anzeige = Einstellungen.DECIMALFORMAT.format((Double) o) + " ";
        anzeige += ((Double) o) > -0.0049 ? "\u2705" : "\u2757";
        return anzeige;
      }
    }, Column.ALIGN_RIGHT, true);
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.DOKUMENTENSPEICHERUNG))
      {
        add("D", "document", false, true);
      }
    }
    catch (RemoteException e)
    {
      //
    }
    add("Anrede", "anrede", false, true);
    add("Titel", "titel", false, true);
    add("Name", "name", true, true);
    add("Vorname", "vorname", true, true);
    add("Adressierungszusatz", "adressierungszusatz", false, true);
    add("Straße", "strasse", true, true);
    add("PLZ", "plz", false, true);
    add("Ort", "ort", true, true);
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.AUSLANDSADRESSEN))
      {
        add("Staat", "staat", false, new StaatFormatter(), Column.ALIGN_LEFT,
            true);
      }
    }
    catch (RemoteException ignore)
    {
    }
    add("Zahlungsweg", "zahlungsweg", false, new ZahlungswegFormatter(),
        Column.ALIGN_LEFT, true);
    add("Zahlungsrhytmus", "zahlungsrhytmus", false,
        new ZahlungsrhythmusFormatter(), Column.ALIGN_LEFT, false);
    add("Zahlungstermin", "zahlungstermin", false,
        new ZahlungsterminFormatter(), Column.ALIGN_LEFT, true);
    add("Datum des Mandats", "mandatdatum", false, true);
    add("BIC", "bic", false, true);
    add("IBAN", "iban", false, new IBANFormatter(), Column.ALIGN_LEFT, true);
    add("Kontoinhaber", "kontoinhaber", false, true);
    add("Abweichender Zahler", "altzahlerstring", false, true);
    add("Mandat Version", "mandatversion", false, true);
    add("Mandat ID", "mandatid", false, true);
    add("Geburtsdatum", "geburtsdatum", true,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO, true);
    add("Alter", "alter", false, true);
    add("Geschlecht", "geschlecht", false, true);
    add("Telefon privat", "telefonprivat", true, true);
    add("Telefon dienstlich", "telefondienstlich", false, true);
    add("Handy", "handy", false, true);
    add("Email", "email", false, true);
    add("Eintritt", "eintritt", true,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
        false);
    add("Beitragsgruppe", "beitragsgruppe", false, false);
    add("Austritt", "austritt", true,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
        false);
    add("Kündigung", "kuendigung", false,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
        false);
    add("Leitweg ID", "leitwegid", false, true);
    add("Vollzahler", "vollzahlerstring", false, false);
    try
    {
      if ((Boolean) Einstellungen
          .getEinstellung(Property.INDIVIDUELLEBEITRAEGE))
      {
        add("Individueller Beitrag", "individuellerbeitrag", false, false);
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.STERBEDATUM))
      {
        add("Sterbedatum", "sterbetag", false,
            new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO,
            true);
      }
    }
    catch (RemoteException re)
    {
      //
    }
    add("Eingabedatum", "eingabedatum", false,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO, true);
    add("Letzte Änderung", "letzteaenderung", false,
        new DateFormatter(new JVDateFormatTTMMJJJJ()), Column.ALIGN_AUTO, true);
    try
    {
      DBIterator<Felddefinition> it = Einstellungen.getDBService()
          .createList(Felddefinition.class);
      while (it.hasNext())
      {
        Felddefinition fd = (Felddefinition) it.next();
        switch (fd.getDatentyp())
        {
          case Datentyp.DATUM:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false,
                new DateFormatter(new JVDateFormatTTMMJJJJ()),
                Column.ALIGN_AUTO, true);
            break;
          case Datentyp.WAEHRUNG:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false,
                new CurrencyFormatter("", Einstellungen.DECIMALFORMAT),
                Column.ALIGN_AUTO, true);
            break;
          case Datentyp.JANEIN:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false,
                new JaNeinFormatter(), Column.ALIGN_AUTO, true);
            break;
          default:
            add(fd.getLabel(), "zusatzfelder_" + fd.getName(), false, true);
            break;
        }
      }

      DBIterator<EigenschaftGruppe> eigenschaftGruppeit = Einstellungen
          .getDBService().createList(EigenschaftGruppe.class);
      while (eigenschaftGruppeit.hasNext())
      {
        EigenschaftGruppe eg = (EigenschaftGruppe) eigenschaftGruppeit.next();

        add(eg.getBezeichnung(), "eigenschaften_" + eg.getName(), false, true);
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }

    mitgliedList.setContextMenu(new MitgliedMenu(detailaction, mitgliedList));
    mitgliedList.setMulti(true);
    mitgliedList.setRememberState(true);
    if (detailaction instanceof MitgliedDetailAction)
    {
      mitgliedList
          .setAction(new EditAction(MitgliedDetailView.class, mitgliedList));
    }
    else if (detailaction instanceof NichtMitgliedDetailAction)
    {
      mitgliedList.setAction(
          new EditAction(NichtMitgliedDetailView.class, mitgliedList));
    }
    VorZurueckControl.setObjektListe(null, null);
    return mitgliedList;
  }

  private void add(String spaltenbezeichnung, String spaltenname,
      boolean defaultvalue, boolean auchNichtMitglied)
  {
    add(spaltenbezeichnung, spaltenname, defaultvalue, null, Column.ALIGN_AUTO,
        auchNichtMitglied);
  }

  private void add(String spaltenbezeichnung, String spaltenname,
      boolean defaultVisible, Formatter formatter, int align,
      boolean auchNichtMitglied)
  {
    if (isMitglied || auchNichtMitglied)
    {
      mitgliedList.addColumn(
          new Column(spaltenname, spaltenbezeichnung, formatter, false, align),
          defaultVisible);
    }
  }

  public void refreshMitgliedTable()
      throws RemoteException, ApplicationException
  {
    if (System.currentTimeMillis() - lastrefresh < 500)
    {
      Logger.debug(String.format("Zeit zwischen den Refreshs: %s",
          (System.currentTimeMillis() - lastrefresh)));
      return;
    }
    lastrefresh = System.currentTimeMillis();
    mitgliedList.removeAll();
    ArrayList<Mitglied> mitglieder = new MitgliedQuery(this)
        .get(mitgliedAuswahl, null);
    for (Mitglied m : mitglieder)
    {
      mitgliedList.addItem(m);
    }
    mitgliedList.sort();
  }

  @Override
  public void TabRefresh() throws ApplicationException
  {
    if (mitgliedList != null)
    {
      try
      {
        refreshMitgliedTable();
      }
      catch (RemoteException e1)
      {
        Logger.error("Fehler", e1);
      }
    }
  }

  // Überschrieben, um ggf. "Mitglied" aus der Liste der Mitgliedsarten zu
  // entfernen
  @Override
  public Input getFilterInput(Filter filter)
      throws RemoteException, ApplicationException
  {
    Input input = super.getFilterInput(filter);
    if (filter.equals(Filter.MITGLIEDSTYP)
        && mitgliedAuswahl.equals(MitgliedAuswahl.NICHTMITGLIEDER))
    {
      List<?> list = ((SelectInput) input).getList();
      for (Object o : list)
      {
        if (((Mitgliedstyp) o).getJVereinid() == Integer
            .parseInt(Mitgliedstyp.MITGLIED))
        {
          list.remove(o);
          break;
        }
      }
    }
    return input;
  }

  @Override
  protected String getTableTitle()
  {
    if (isMitglied)
    {
      return VorlageUtil.getName(VorlageTyp.MITGLIEDER_TITEL, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.NICHT_MITGLIEDER_TITEL, this);
    }
  }

  @Override
  protected String getTableSubtitle()
  {
    if (isMitglied)
    {
      return VorlageUtil.getName(VorlageTyp.MITGLIEDER_SUBTITEL, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.NICHT_MITGLIEDER_SUBTITEL, this);
    }
  }

  @Override
  protected String getTableDateiname()
  {
    if (isMitglied)
    {
      return VorlageUtil.getName(VorlageTyp.MITGLIEDER_DATEINAME, this);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.NICHT_MITGLIEDER_DATEINAME, this);
    }
  }

  public void setMitgliedAuswahl(MitgliedAuswahl mitgliedAuswahl)
  {
    this.mitgliedAuswahl = mitgliedAuswahl;
  }

  public MitgliedAuswahl getMitgliedAuswahl()
  {
    return mitgliedAuswahl;
  }

}
