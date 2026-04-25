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
import de.jost_net.JVerein.gui.formatter.AbrechnungsmodusFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.formatter.IBANFormatter;
import de.jost_net.JVerein.gui.formatter.JaNeinFormatter;
import de.jost_net.JVerein.gui.formatter.SollbuchungFormatter;
import de.jost_net.JVerein.gui.menu.AbrechnungslaufMenu;
import de.jost_net.JVerein.gui.menu.BuchungAbrechnugslaufMenu;
import de.jost_net.JVerein.gui.menu.LastschriftMenu;
import de.jost_net.JVerein.gui.menu.SollbuchungMenu;
import de.jost_net.JVerein.gui.menu.ZusatzbetraegeMenu;
import de.jost_net.JVerein.gui.parts.BetragSummaryTablePart;
import de.jost_net.JVerein.gui.parts.BuchungListTablePart;
import de.jost_net.JVerein.gui.parts.ButtonRtoL;
import de.jost_net.JVerein.gui.parts.JVereinTablePart;
import de.jost_net.JVerein.gui.parts.JVereinTablePart.ExportArt;
import de.jost_net.JVerein.gui.view.AbrechnungslaufDetailView;
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
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbrechnungslaufControl extends FilterControl implements Savable
{

  private Abrechnungslauf abrl;

  private JVereinTablePart abrechnungslaufList;

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

  public AbrechnungslaufControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Abrechnungslauf getAbrechnungslauf()
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

  final class StatData
  {
    Double summe;

    Integer anzahl;
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

  public Part getAbrechnungslaeufeList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Abrechnungslauf> abrechnungslaeufe = service
        .createList(Abrechnungslauf.class);
    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      abrechnungslaeufe.addFilter("datum >= ?",
          new Object[] { (Date) getDatumvon().getValue() });
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      abrechnungslaeufe.addFilter("datum <= ?",
          new Object[] { (Date) getDatumbis().getValue() });
    }
    abrechnungslaeufe.setOrder("ORDER BY datum DESC");

    if (abrechnungslaufList == null)
    {
      abrechnungslaufList = new JVereinTablePart(abrechnungslaeufe,
          new EditAction(AbrechnungslaufDetailView.class));
      abrechnungslaufList.addColumn("Nr", "nr");
      abrechnungslaufList.addColumn("Datum", "datum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Modus", "modus",
          new AbrechnungsmodusFormatter(), false, Column.ALIGN_LEFT);
      abrechnungslaufList.addColumn("Fälligkeit", "faelligkeit",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Stichtag", "stichtag",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Eintrittsdatum", "eingabedatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Austrittsdatum", "austrittsdatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      abrechnungslaufList.addColumn("Zahlungsgrund", "zahlungsgrund");
      if ((Boolean) Einstellungen.getEinstellung(Property.ZUSATZBETRAG))
      {
        abrechnungslaufList.addColumn("Zusatzbeträge", "zusatzbetraege",
            new JaNeinFormatter());
      }
      if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
      {
        abrechnungslaufList.addColumn("Kursteilnehmer", "kursteilnehmer",
            new JaNeinFormatter());
      }
      abrechnungslaufList
          .setContextMenu(new AbrechnungslaufMenu(abrechnungslaufList));
      abrechnungslaufList.setRememberColWidths(true);
      abrechnungslaufList.setRememberOrder(true);
      abrechnungslaufList.setAction(
          new EditAction(AbrechnungslaufDetailView.class, abrechnungslaufList));
      VorZurueckControl.setObjektListe(null, null);
    }
    else
    {
      abrechnungslaufList.removeAll();
      while (abrechnungslaeufe.hasNext())
      {
        abrechnungslaufList.addItem(abrechnungslaeufe.next());
      }
      abrechnungslaufList.sort();
    }
    return abrechnungslaufList;
  }

  @Override
  public void TabRefresh()
  {
    if (abrechnungslaufList == null)
    {
      return;
    }
    try
    {
      getAbrechnungslaeufeList();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }

  @SuppressWarnings("unchecked")
  public Part getBuchungList() throws RemoteException
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
    buchungList.addColumn("Nr", "id-int");
    buchungList.addColumn("Geprüft", "geprueft",
        o -> (Boolean) o ? "\u2705" : "");
    if ((Boolean) Einstellungen.getEinstellung(Property.DOKUMENTENSPEICHERUNG))
    {
      buchungList.addColumn("D", "document");
    }
    buchungList.addColumn("S", "splittyp",
        o -> SplitbuchungTyp.get((Integer) o).substring(0, 1));

    buchungList.addColumn("Konto", "konto");
    buchungList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));

    buchungList.addColumn("Name", "name");
    buchungList.addColumn("IBAN oder Kontonummer", "iban", new IBANFormatter());
    buchungList.addColumn("Verwendungszweck", "zweck", o -> {
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
      buchungList.addColumn("Buchungsklasse", "buchungsklasse",
          new BuchungsklasseFormatter());
    }

    buchungList.addColumn("Buchungsart", "buchungsart",
        new BuchungsartFormatter());
    buchungList.addColumn("Betrag", "betrag",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    if ((Boolean) Einstellungen.getEinstellung(Property.OPTIERT))
    {
      buchungList.addColumn("Netto", "netto",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
      {
        buchungList.addColumn("Steuer", "steuer", o -> {
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
    buchungList.addColumn(new Column(Buchung.SOLLBUCHUNG,
        "Mitglied - Sollbuchung", new SollbuchungFormatter(), false,
        Column.ALIGN_AUTO, Column.SORT_BY_DISPLAY));
    buchungList.setMulti(true);

    buchungList.setContextMenu(new BuchungAbrechnugslaufMenu());
    buchungList.setRememberColWidths(true);
    buchungList.setRememberOrder(true);

    return buchungList;
  }

  public Part getSollbuchungList() throws RemoteException
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
      sollbuchungList.addColumn("Rechnung", Sollbuchung.RECHNUNG);
    }
    sollbuchungList.setContextMenu(new SollbuchungMenu(null));
    sollbuchungList.setRememberColWidths(true);
    sollbuchungList.setRememberOrder(true);
    sollbuchungList.setMulti(true);

    return sollbuchungList;
  }

  public Part getLastschriftList() throws RemoteException
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
    lastschriftList.setRememberColWidths(true);
    lastschriftList.setContextMenu(new LastschriftMenu(lastschriftList));
    lastschriftList.setRememberOrder(true);
    lastschriftList.setMulti(true);
    return lastschriftList;
  }

  public Part getZusatzbetraegeList() throws RemoteException
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
    zusatzbetraegeList.setRememberColWidths(true);
    zusatzbetraegeList.setRememberOrder(true);
    zusatzbetraegeList.setMulti(true);

    return zusatzbetraegeList;
  }

  public ButtonRtoL exportLastschriftButton(ExportArt art)
      throws ApplicationException
  {
    return new ButtonRtoL(art.equals(ExportArt.PDF) ? "PDF" : "CSV",
        context -> {
          doExport(lastschriftList,
              VorlageTyp.ABRECHNUNGSLAUF_LASTSCHRIFTEN2_TITEL,
              VorlageTyp.ABRECHNUNGSLAUF_LASTSCHRIFTEN2_SUBTITEL,
              VorlageTyp.ABRECHNUNGSLAUF_LASTSCHRIFTEN2_DATEINAME,
              "abrechnungslauf.lastschrift", art);
        }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  public ButtonRtoL exportZusatzbetragButton(ExportArt art)
      throws ApplicationException
  {
    return new ButtonRtoL(art.equals(ExportArt.PDF) ? "PDF" : "CSV",
        context -> {
          doExport(zusatzbetraegeList,
              VorlageTyp.ABRECHNUNGSLAUF_ZUSATZBETRAEGE_TITEL,
              VorlageTyp.ABRECHNUNGSLAUF_ZUSATZBETRAEGE_SUBTITEL,
              VorlageTyp.ABRECHNUNGSLAUF_ZUSATZBETRAEGE_DATEINAME,
              "abrechnungslauf.zusatzbetrag", art);
        }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  public ButtonRtoL exportBuchungButton(ExportArt art)
      throws ApplicationException
  {
    return new ButtonRtoL(art.equals(ExportArt.PDF) ? "PDF" : "CSV",
        context -> {
          doExport(buchungList, VorlageTyp.ABRECHNUNGSLAUF_BUCHUNGEN_TITEL,
              VorlageTyp.ABRECHNUNGSLAUF_BUCHUNGEN_SUBTITEL,
              VorlageTyp.ABRECHNUNGSLAUF_BUCHUNGEN_DATEINAME,
              "abrechnungslauf.buchung", art);
        }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  public ButtonRtoL exportSollbuchungButton(ExportArt art)
      throws ApplicationException
  {
    return new ButtonRtoL(art.equals(ExportArt.PDF) ? "PDF" : "CSV",
        context -> {
          doExport(sollbuchungList,
              VorlageTyp.ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL,
              VorlageTyp.ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL,
              VorlageTyp.ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME,
              "abrechnungslauf.sollbuchung", art);
        }, null, false, art.equals(ExportArt.PDF) ? "file-pdf.png" : "xsd.png");
  }

  private void doExport(JVereinTablePart tabelle, VorlageTyp titel,
      VorlageTyp subtitel, VorlageTyp dateiname, String settingPrefix,
      ExportArt art) throws ApplicationException
  {
    if (tabelle == null)
    {
      throw new ApplicationException(
          "Der Export kann nicht durchgeführt werden, Tabelle ist nicht geladen.");
    }
    tabelle.export(VorlageUtil.getName(titel, getAbrechnungslauf()),
        VorlageUtil.getName(subtitel, getAbrechnungslauf()),
        VorlageUtil.getName(dateiname, getAbrechnungslauf()), settingPrefix,
        art);
    GUI.getStatusBar().setSuccessText("Auswertung fertig.");
  }
}
