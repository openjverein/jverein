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
import java.util.Date;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.dialogs.TabelleSpaltenAuswahlDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.menu.BuchungAbrechnugslaufMenu;
import de.jost_net.JVerein.gui.menu.LastschriftMenu;
import de.jost_net.JVerein.gui.menu.SollbuchungMenu;
import de.jost_net.JVerein.gui.menu.ZusatzbetraegeMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.view.LastschriftDetailView;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.gui.view.ZusatzbetragDetailView;
import de.jost_net.JVerein.keys.Abrechnungsmodi;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbrechnungslaufControl extends VorZurueckControl implements Savable
{

  private Abrechnungslauf abrl;

  private LabelInput datum;

  private LabelInput abgeschlossen;

  private LabelInput modus;

  private LabelInput faelligkeit;

  private LabelInput astichtag;

  private LabelInput eintrittsdatum;

  private LabelInput austrittsdatum;

  private LabelInput zahlungsgrund;

  private LabelInput zusatzabrechnungen;

  private TextInput bemerkung;

  private JVereinTablePart buchungList;

  private BetragSummaryTablePart sollbuchungList;

  private BetragSummaryTablePart lastschriftList;

  private BetragSummaryTablePart zusatzbetraegeList;

  private JVereinTablePart allebuchungList;

  public final static int TAB_BUCHUNGEN = 0;

  public final static int TAB_SOLLBUCHUNGEN = 1;

  public final static int TAB_LASTSCHRIFTEN = 2;

  public final static int TAB_ZUSATZBETRAEGE = 3;

  public final static int TAB_ALLEBUCHUNGEN = 4;

  private int selectedTab = TAB_BUCHUNGEN;

  public void setFolderSelection(int selection)
  {
    selectedTab = selection;
  }

  public AbrechnungslaufControl(AbstractView view)
  {
    super(view);
  }

  private Abrechnungslauf getAbrechnungslauf()
  {
    if (abrl != null)
    {
      return abrl;
    }
    abrl = (Abrechnungslauf) getCurrentObject();
    return abrl;
  }

  public LabelInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    datum = new LabelInput(
        new JVDateFormatTTMMJJJJ().format(getAbrechnungslauf().getDatum()));
    datum.setName("Datum");
    return datum;
  }

  public LabelInput getAbgeschlossen() throws RemoteException
  {
    if (abgeschlossen != null)
    {
      return abgeschlossen;
    }
    Boolean b = getAbrechnungslauf().getAbgeschlossen();
    abgeschlossen = new LabelInput(b ? "Ja" : "Nein");
    abgeschlossen.setName("Abgeschlossen");
    return abgeschlossen;
  }

  public LabelInput getAbrechnungsmodus() throws RemoteException
  {
    if (modus != null)
    {
      return modus;
    }
    String m = Abrechnungsmodi.get(getAbrechnungslauf().getModus());
    modus = new LabelInput(m);
    modus.setName("Abrechnungsmodus");
    return modus;
  }

  public LabelInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }
    faelligkeit = new LabelInput(new JVDateFormatTTMMJJJJ()
        .format(getAbrechnungslauf().getFaelligkeit()));
    faelligkeit.setName("Fälligkeit");
    return faelligkeit;
  }

  public LabelInput getAbrechnungStichtag() throws RemoteException
  {
    if (astichtag != null)
    {
      return astichtag;
    }
    astichtag = new LabelInput(
        new JVDateFormatTTMMJJJJ().format(getAbrechnungslauf().getStichtag()));
    astichtag.setName("Stichtag");
    return astichtag;
  }

  public LabelInput getEintrittsdatum() throws RemoteException
  {
    if (eintrittsdatum != null)
    {
      return eintrittsdatum;
    }
    Date ed = getAbrechnungslauf().getEintrittsdatum();
    if (ed.equals(Einstellungen.NODATE))
      eintrittsdatum = new LabelInput(null);
    else
      eintrittsdatum = new LabelInput(new JVDateFormatTTMMJJJJ().format(ed));
    eintrittsdatum.setName("Eintrittsdatum");
    return eintrittsdatum;
  }

  public LabelInput getAustrittsdatum() throws RemoteException
  {
    if (austrittsdatum != null)
    {
      return austrittsdatum;
    }
    Date ed = getAbrechnungslauf().getAustrittsdatum();
    if (ed.equals(Einstellungen.NODATE))
      austrittsdatum = new LabelInput(null);
    else
      austrittsdatum = new LabelInput(new JVDateFormatTTMMJJJJ().format(ed));
    austrittsdatum.setName("Austrittsdatum");
    return austrittsdatum;
  }

  public LabelInput getZahlungsgrund() throws RemoteException
  {
    if (zahlungsgrund != null)
    {
      return zahlungsgrund;
    }
    zahlungsgrund = new LabelInput(getAbrechnungslauf().getZahlungsgrund());
    zahlungsgrund.setName("Zahlungsgrund");
    return zahlungsgrund;
  }

  public LabelInput getZusatzAbrechnungen() throws RemoteException
  {
    if (zusatzabrechnungen != null)
    {
      return zusatzabrechnungen;
    }
    String zs = "";
    if (getAbrechnungslauf().getZusatzbetraege())
    {
      zs += "Zusatzbeträge ";
    }
    if (getAbrechnungslauf().getKursteilnehmer())
    {
      zs += "Kursteilnehmer ";
    }
    zusatzabrechnungen = new LabelInput(zs);
    zusatzabrechnungen.setName("Weitere Abrechnungen");
    return zusatzabrechnungen;
  }

  public Input getBemerkung() throws RemoteException
  {
    if (bemerkung != null)
    {
      return bemerkung;
    }
    bemerkung = new TextInput(getAbrechnungslauf().getBemerkung(), 80);
    bemerkung.setName("Bemerkung");
    return bemerkung;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    // Es kann nur die Bemerkung verändert werden
    Abrechnungslauf al = getAbrechnungslauf();
    al.setBemerkung((String) getBemerkung().getValue());
    return al;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      prepareStore().store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern des Abrechnungslaufs";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  @Override
  public JVereinTablePart getTablePart()
      throws RemoteException, ApplicationException
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return getBuchungList();
      case TAB_LASTSCHRIFTEN:
        return getLastschriftList();
      case TAB_SOLLBUCHUNGEN:
        return getSollbuchungList();
      case TAB_ZUSATZBETRAEGE:
        return getZusatzbetraegeList();
      case TAB_ALLEBUCHUNGEN:
        return getAlleBuchungList();
      default:
        return null;
    }
  }

  @SuppressWarnings("unchecked")
  public JVereinTablePart getBuchungList() throws RemoteException
  {
    if (buchungList != null)
    {
      return buchungList;
    }
    DBIterator<Buchung> it = Einstellungen.getDBService()
        .createList(Buchung.class);
    it.addFilter("abrechnungslauf = ?", getAbrechnungslauf().getID());

    buchungList = new BuchungListTablePart(PseudoIterator.asList(it),
        new BuchungAction(false, null));
    buchungList.setTableName("Buchungen");
    addBuchungColumns(buchungList);
    return buchungList;
  }

  @SuppressWarnings("unchecked")
  public JVereinTablePart getAlleBuchungList() throws RemoteException
  {
    if (allebuchungList != null)
    {
      return allebuchungList;
    }
    DBIterator<Buchung> it = Einstellungen.getDBService()
        .createList(Buchung.class);
    it.join("sollbuchung");
    it.addFilter("sollbuchung.id = buchung.sollbuchung");
    it.addFilter("sollbuchung.abrechnungslauf = ?",
        getAbrechnungslauf().getID());

    allebuchungList = new BuchungListTablePart(PseudoIterator.asList(it),
        new BuchungAction(false, null));
    allebuchungList.setTableName("Allebuchungen");
    addBuchungColumns(allebuchungList);
    return allebuchungList;
  }

  private void addBuchungColumns(JVereinTablePart part) throws RemoteException
  {
    part.addColumn("Nr", "id-int");
    part.addColumn("Geprüft", "geprueft", o -> (Boolean) o ? "\u2705" : "");
    if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG))
    {
      part.addColumn("D", "document");
    }
    part.addColumn("S", "splittyp",
        o -> SplitbuchungTyp.get((Integer) o).substring(0, 1));

    part.addColumn("Konto", "konto");
    part.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));

    part.addColumn("Name", "name");
    part.addColumn("IBAN oder Kontonummer", "iban", new IBANFormatter());
    part.addColumn("Verwendungszweck", "zweck", o -> {
      if (o == null)
      {
        return null;
      }
      String s = o.toString();
      s = s.replaceAll("\r\n", " ");
      s = s.replaceAll("\r", " ");
      s = s.replaceAll("\n", " ");
      return s;
    });
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      part.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }

    part.addColumn("Buchungsart", "buchungsart", new BuchungsartFormatter());
    part.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      part.addColumn("Netto", "netto",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        part.addColumn("Steuer", "steuer", o -> {
          if (o == null)
          {
            return "";
          }
          try
          {
            return ((Steuer) o).getName();
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler", e);
          }
          return "";
        }, false, Column.ALIGN_RIGHT);
      }
    }
    part.addColumn(new Column(Buchung.SOLLBUCHUNG, "Mitglied - Sollbuchung",
        new SollbuchungFormatter(), false, Column.ALIGN_AUTO,
        Column.SORT_BY_DISPLAY));
    part.setMulti(true);

    part.setContextMenu(new BuchungAbrechnugslaufMenu());
  }

  public JVereinTablePart getSollbuchungList() throws RemoteException
  {
    if (sollbuchungList != null)
    {
      return sollbuchungList;
    }

    DBIterator<Sollbuchung> it = Einstellungen.getDBService()
        .createList(Sollbuchung.class);
    it.addFilter("abrechnungslauf = ?", getAbrechnungslauf().getID());

    sollbuchungList = new BetragSummaryTablePart(it,
        new EditAction(SollbuchungDetailView.class));
    sollbuchungList.setTableName("Sollbuchungen");

    sollbuchungList.addColumn("Nr", "id-int");
    sollbuchungList.addColumn("Datum", Sollbuchung.DATUM,
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    sollbuchungList.addColumn("Abrechnungslauf", Sollbuchung.ABRECHNUNGSLAUF);
    sollbuchungList.addColumn("Mitglied", Sollbuchung.MITGLIED);
    sollbuchungList.addColumn("Zahler", Sollbuchung.ZAHLER);
    sollbuchungList.addColumn("Zweck", Sollbuchung.ZWECK1);
    sollbuchungList.addColumn("Betrag", Sollbuchung.BETRAG,
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    sollbuchungList.addColumn("Zahlungsweg", Sollbuchung.ZAHLUNGSWEG,
        o -> new Zahlungsweg((Integer) o).getText());
    sollbuchungList.addColumn("Zahlungseingang", Sollbuchung.ISTSUMME,
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
    {
      sollbuchungList.addColumn("Rechnung", "rechnung.nummer");
    }
    sollbuchungList.setContextMenu(new SollbuchungMenu(null));
    sollbuchungList.setMulti(true);

    return sollbuchungList;
  }

  public JVereinTablePart getLastschriftList() throws RemoteException
  {
    if (lastschriftList != null)
    {
      return lastschriftList;
    }
    DBIterator<Lastschrift> it = Einstellungen.getDBService()
        .createList(Lastschrift.class);
    it.addFilter("abrechnungslauf = ?", getAbrechnungslauf().getID());

    lastschriftList = new BetragSummaryTablePart(it,
        new EditAction(LastschriftDetailView.class));
    lastschriftList.setTableName("Lastschriften");

    lastschriftList.addColumn("Nr", "id-int");
    lastschriftList.addColumn("Versanddatum", "versanddatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("Abrechnungslauf", "abrechnungslauf");
    lastschriftList.addColumn("Name", "name");
    lastschriftList.addColumn("Vorname", "vorname");
    lastschriftList.addColumn("Email", "email");
    lastschriftList.addColumn("Zweck", "verwendungszweck");
    lastschriftList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    lastschriftList.addColumn("Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.addColumn("IBAN", "iban", new IBANFormatter());
    lastschriftList.addColumn("Mandat", "mandatid");
    lastschriftList.addColumn("Mandatdatum", "mandatdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    lastschriftList.setContextMenu(new LastschriftMenu(lastschriftList));
    lastschriftList.setMulti(true);
    return lastschriftList;
  }

  public JVereinTablePart getZusatzbetraegeList() throws RemoteException
  {
    if (zusatzbetraegeList != null)
    {
      return zusatzbetraegeList;
    }
    DBIterator<Zusatzbetrag> it = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    it.join("zusatzbetragabrechnungslauf");
    it.addFilter("zusatzbetragabrechnungslauf.zusatzbetrag = zusatzbetrag.id");
    it.addFilter("abrechnungslauf = ?", getAbrechnungslauf().getID());

    zusatzbetraegeList = new BetragSummaryTablePart(it,
        new EditAction(ZusatzbetragDetailView.class));
    zusatzbetraegeList.setTableName("Zusatzbeträge");

    zusatzbetraegeList.addColumn("Nr", "id-int");
    zusatzbetraegeList.addColumn("Name", "mitglied");
    zusatzbetraegeList.addColumn("Erste Fälligkeit", "startdatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Nächste Fälligkeit", "faelligkeit",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Letzte abgerechnete Fälligkeit",
        "ausfuehrung", new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Intervall", "intervalltext");
    zusatzbetraegeList.addColumn("Nicht mehr ausführen ab", "endedatum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    zusatzbetraegeList.addColumn("Buchungstext", "buchungstext");
    zusatzbetraegeList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    zusatzbetraegeList.addColumn("Zahlungsweg", "zahlungsweg", new Formatter()
    {
      @Override
      public String format(Object o)
      {
        return new Zahlungsweg((Integer) o).getText();
      }
    });
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      zusatzbetraegeList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }
    zusatzbetraegeList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      zusatzbetraegeList.addColumn("Steuer", "steuer", o -> {
        if (o == null)
        {
          return "";
        }
        try
        {
          return ((Steuer) o).getName();
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
        return "";
      }, false, Column.ALIGN_RIGHT);
    }
    zusatzbetraegeList.addColumn("Zahlt selbst", "mitgliedzahltselbst",
        new JaNeinFormatter(), false, Column.ALIGN_LEFT);
    zusatzbetraegeList
        .setContextMenu(new ZusatzbetraegeMenu(zusatzbetraegeList));
    zusatzbetraegeList.setMulti(true);

    return zusatzbetraegeList;
  }

  public PanelButton getDetailPanelButton()
  {
    return new PanelButton("document-properties.png", context -> {
      try
      {
        new TabelleSpaltenAuswahlDialog(getBuchungList(), getSollbuchungList(),
            getLastschriftList(), getZusatzbetraegeList(), getAlleBuchungList())
                .open();
      }
      catch (OperationCanceledException | ApplicationException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Spalten-Auswahl-Dialog", e);
        throw new ApplicationException("Fehler beim Spalten-Auswahl-Dialog");
      }
    }, "Spalten auswählen");
  }

  @Override
  protected String getTableTitle()
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return VorlageUtil.getName(VorlageTyp.ABRECHNUNGSLAUF_BUCHUNGEN_TITEL,
            getAbrechnungslauf());
      case TAB_LASTSCHRIFTEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_LASTSCHRIFTEN2_TITEL,
            getAbrechnungslauf());
      case TAB_SOLLBUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL,
            getAbrechnungslauf());
      case TAB_ZUSATZBETRAEGE:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_ZUSATZBETRAEGE_TITEL,
            getAbrechnungslauf());
      case TAB_ALLEBUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_ALLEBUCHUNGEN_TITEL,
            getAbrechnungslauf());
      default:
        return null;
    }
  }

  @Override
  protected String getTableSubtitle()
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_BUCHUNGEN_SUBTITEL,
            getAbrechnungslauf());
      case TAB_LASTSCHRIFTEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_LASTSCHRIFTEN2_SUBTITEL,
            getAbrechnungslauf());
      case TAB_SOLLBUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL,
            getAbrechnungslauf());
      case TAB_ZUSATZBETRAEGE:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_ZUSATZBETRAEGE_SUBTITEL,
            getAbrechnungslauf());
      case TAB_ALLEBUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_ALLEBUCHUNGEN_SUBTITEL,
            getAbrechnungslauf());
      default:
        return null;
    }
  }

  @Override
  protected String getTableDateiname()
  {
    switch (selectedTab)
    {
      case TAB_BUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_BUCHUNGEN_DATEINAME,
            getAbrechnungslauf());
      case TAB_LASTSCHRIFTEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_LASTSCHRIFTEN2_DATEINAME,
            getAbrechnungslauf());
      case TAB_SOLLBUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME,
            getAbrechnungslauf());
      case TAB_ZUSATZBETRAEGE:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_ZUSATZBETRAEGE_DATEINAME,
            getAbrechnungslauf());
      case TAB_ALLEBUCHUNGEN:
        return VorlageUtil.getName(
            VorlageTyp.ABRECHNUNGSLAUF_ALLEBUCHUNGEN_DATEINAME,
            getAbrechnungslauf());
      default:
        return null;
    }
  }

}
