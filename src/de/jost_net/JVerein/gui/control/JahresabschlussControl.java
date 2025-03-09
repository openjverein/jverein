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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.JahresabschlussMenu;
import de.jost_net.JVerein.gui.parts.KontensaldoList;
import de.jost_net.JVerein.gui.parts.MittelverwendungFlowList;
import de.jost_net.JVerein.gui.util.AfaUtil;
import de.jost_net.JVerein.gui.view.JahresabschlussDetailView;
import de.jost_net.JVerein.io.SaldoZeile;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class JahresabschlussControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings;

  private TablePart jahresabschlussList;

  private Part jahresabschlusssaldoList;

  private DateInput von;

  private DateInput bis;

  private DateInput datum;

  private TextInput name;

  private Jahresabschluss jahresabschluss;

  private CheckboxInput anfangsbestaende;

  private CheckboxInput afaberechnung;

  private DecimalInput verwendungsrueckstand;

  private DecimalInput zwanghafteweitergabe;

  public JahresabschlussControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Jahresabschluss getJahresabschluss()
      throws RemoteException, ParseException
  {
    if (jahresabschluss != null)
    {
      return jahresabschluss;
    }
    jahresabschluss = (Jahresabschluss) getCurrentObject();
    if (Einstellungen.getEinstellung().getMittelverwendung()
        && jahresabschluss.isNewObject())
    {
      MittelverwendungFlowList list = new MittelverwendungFlowList(
          (Date) getVon().getValue(), (Date) getBis().getValue());
      list.getInfo();
      jahresabschluss.setVerwendungsrueckstand(list.getRueckstandVorjahrNeu());
      jahresabschluss
          .setZwanghafteWeitergabe(list.getZwanghafteWeitergabeNeu());
    }
    return jahresabschluss;
  }

  public DateInput getVon() throws RemoteException, ParseException
  {
    if (von != null)
    {
      return von;
    }
    von = new DateInput(getJahresabschluss().getVon());
    von.setEnabled(false);
    if (getJahresabschluss().isNewObject())
    {
      von.setValue(computeVonDatum());
    }
    return von;
  }

  private Date computeVonDatum() throws RemoteException, ParseException
  {
    DBIterator<Jahresabschluss> it = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    it.setOrder("ORDER BY bis DESC");
    if (it.hasNext())
    {
      Jahresabschluss ja = it.next();
      Calendar cal = Calendar.getInstance();
      cal.setTime(ja.getBis());
      cal.add(Calendar.DAY_OF_MONTH, 1);
      return cal.getTime();
    }
    DBIterator<Buchung> itbu = Einstellungen.getDBService()
        .createList(Buchung.class);
    itbu.setOrder("ORDER BY datum");
    if (itbu.hasNext())
    {
      Buchung b = itbu.next();
      Geschaeftsjahr gj = new Geschaeftsjahr(b.getDatum());
      return gj.getBeginnGeschaeftsjahr();
    }
    Geschaeftsjahr gj = new Geschaeftsjahr(new Date());
    return gj.getBeginnGeschaeftsjahr();
  }

  public DateInput getBis() throws RemoteException, ParseException
  {
    if (bis != null)
    {
      return bis;
    }
    Geschaeftsjahr gj = new Geschaeftsjahr((Date) von.getValue());
    bis = new DateInput(gj.getEndeGeschaeftsjahr());
    bis.setEnabled(false);
    return bis;
  }

  public DateInput getDatum() throws RemoteException, ParseException
  {
    if (datum != null)
    {
      return datum;
    }
    Date date = new Date();
    if (!getJahresabschluss().isNewObject())
    {
      date = getJahresabschluss().getDatum();
    }
    datum = new DateInput(date);
    datum.setEnabled(false);
    return datum;
  }

  public TextInput getName() throws RemoteException, ParseException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getJahresabschluss().getName(), 50);
    name.setEnabled(getJahresabschluss().isNewObject());
    return name;
  }

  public CheckboxInput getAnfangsbestaende()
      throws RemoteException, ParseException
  {
    if (anfangsbestaende != null)
    {
      return anfangsbestaende;
    }
    anfangsbestaende = new CheckboxInput(getJahresabschluss().isNewObject());
    anfangsbestaende.setName("Anfangsbestände Folgejahr");
    anfangsbestaende.setEnabled(getJahresabschluss().isNewObject());
    return anfangsbestaende;
  }

  public CheckboxInput getAfaberechnung() throws RemoteException, ParseException
  {
    if (afaberechnung != null)
    {
      return afaberechnung;
    }
    afaberechnung = new CheckboxInput(getJahresabschluss().isNewObject());
    afaberechnung.setName("Erzeuge Abschreibungen");
    afaberechnung.setEnabled(getJahresabschluss().isNewObject());
    return afaberechnung;
  }

  public Part getJahresabschlussSaldo() throws RemoteException
  {
    if (jahresabschlusssaldoList != null)
    {
      return jahresabschlusssaldoList;
    }
    try
    {
      jahresabschlusssaldoList = new KontensaldoList(null,
          new Geschaeftsjahr((Date) getVon().getValue())).getSaldoList();
    }
    catch (ApplicationException e)
    {
      throw new RemoteException(e.getMessage());
    }
    catch (ParseException e)
    {
      throw new RemoteException(e.getMessage());
    }
    return jahresabschlusssaldoList;
  }

  public DecimalInput getVerwendungsrueckstand()
      throws RemoteException, ParseException
  {
    if (verwendungsrueckstand != null)
    {
      return verwendungsrueckstand;
    }

    if (getJahresabschluss().getVerwendungsrueckstand() == null)
    {
      verwendungsrueckstand = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    else
    {
      verwendungsrueckstand = new DecimalInput(
          getJahresabschluss().getVerwendungsrueckstand(),
          Einstellungen.DECIMALFORMAT);
    }
    verwendungsrueckstand.setEnabled(false);
    return verwendungsrueckstand;
  }

  public DecimalInput getZwanghafteWeitergabe()
      throws RemoteException, ParseException
  {
    if (zwanghafteweitergabe != null)
    {
      return zwanghafteweitergabe;
    }

    if (getJahresabschluss().getZwanghafteWeitergabe() == null)
    {
      zwanghafteweitergabe = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    else
    {
      zwanghafteweitergabe = new DecimalInput(
          getJahresabschluss().getZwanghafteWeitergabe(),
          Einstellungen.DECIMALFORMAT);
    }
    zwanghafteweitergabe.setEnabled(false);
    return zwanghafteweitergabe;
  }

  public boolean isSaveEnabled() throws RemoteException, ParseException
  {
    return getJahresabschluss().isNewObject();
  }

  /**
   * This method stores the project using the current values.
   */
  public void handleStore()
  {
    try
    {
      Jahresabschluss ja = getJahresabschluss();
      ja.setVon((Date) getVon().getValue());
      ja.setBis((Date) getBis().getValue());
      ja.setDatum((Date) getDatum().getValue());
      ja.setName((String) getName().getValue());
      ja.store();
      if (afaberechnung != null && (Boolean) getAfaberechnung().getValue())
      {
        new AfaUtil(new Geschaeftsjahr(ja.getVon()), ja);
      }
      if (Einstellungen.getEinstellung().getMittelverwendung())
      {
        MittelverwendungFlowList list = new MittelverwendungFlowList(
            ja.getVon(), ja.getBis());
        list.getInfo();
        ja.setVerwendungsrueckstand(list.getRueckstandVorjahrNeu());
        ja.setZwanghafteWeitergabe(list.getZwanghafteWeitergabeNeu());
        ja.store();
      }
      if ((Boolean) getAnfangsbestaende().getValue())
      {
        KontensaldoList jsl = new KontensaldoList(null,
            new Geschaeftsjahr(ja.getVon()));
        ArrayList<SaldoZeile> zeilen = jsl.getInfo(false);
        for (SaldoZeile z : zeilen)
        {
          String ktonr = (String) z.getAttribute("kontonummer");
          if (ktonr.length() > 0)
          {
            Double endbestand = (Double) z.getAttribute("endbestand");
            Anfangsbestand anf = (Anfangsbestand) Einstellungen.getDBService()
                .createObject(Anfangsbestand.class, null);
            Konto konto = (Konto) z.getAttribute("konto");
            anf.setBetrag(endbestand);
            anf.setDatum(Datum.addTage(ja.getBis(), 1));
            anf.setKonto(konto);
            anf.store();
          }
        }
      }
      GUI.getStatusBar().setSuccessText("Jahresabschluss gespeichert");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim speichern des Jahresabschlusses";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    catch (ParseException e)
    {

      String fehler = "Fehler beim speichern des Jahresabschlusses";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

  public Part getJahresabschlussList() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Jahresabschluss> jahresabschluesse = service
        .createList(Jahresabschluss.class);
    jahresabschluesse.setOrder("ORDER BY von desc");

    jahresabschlussList = new TablePart(jahresabschluesse,
        new EditAction(JahresabschlussDetailView.class));
    jahresabschlussList.addColumn("Nr", "id-int");
    jahresabschlussList.addColumn("Von", "von",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    jahresabschlussList.addColumn("Bis", "bis",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    jahresabschlussList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    jahresabschlussList.addColumn("Name", "name");
    jahresabschlussList.setRememberColWidths(true);
    jahresabschlussList.setContextMenu(new JahresabschlussMenu());
    jahresabschlussList.setRememberOrder(true);
    jahresabschlussList.removeFeature(FeatureSummary.class);
    return jahresabschlussList;
  }

  public void refreshTable() throws RemoteException
  {
    jahresabschlussList.removeAll();
    DBIterator<Jahresabschluss> jahresabschluesse = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    jahresabschluesse.setOrder("ORDER BY von desc");
    while (jahresabschluesse.hasNext())
    {
      jahresabschlussList.addItem(jahresabschluesse.next());
    }
    jahresabschlussList.sort();
  }

  public String getInfo()
  {
    String text = "";
    try
    {
      Date vongj = (Date) getVon().getValue();
      Date bisgj = (Date) getBis().getValue();
      DBService service = Einstellungen.getDBService();
      DBIterator<Konto> kontenIt = service.createList(Konto.class);
      kontenIt.addFilter("kontoart = ?",
          new Object[] { Kontoart.ANLAGE.getKey() });
      kontenIt.addFilter("(eroeffnung IS NULL OR eroeffnung <= ?)",
          new Object[] { new java.sql.Date(bisgj.getTime()) });
      kontenIt.addFilter("(aufloesung IS NULL OR aufloesung >= ?)",
          new Object[] { new java.sql.Date(vongj.getTime()) });
      while (kontenIt.hasNext())
      {
        Konto konto = (Konto) kontenIt.next();
        if (konto.getEroeffnung() == null)
        {
          text = text + "Das Anlagenkonto mit Nummer " + konto.getNummer()
              + " hat kein Eröffnungsdatum\n";
        }
        if (konto.getAnschaffung() == null)
        {
          text = text + "Das Anlagenkonto mit Nummer " + konto.getNummer()
              + " hat kein Anschaffungsdatum. Bitte auf Plausibilität prüfen!\n";
        }
        else if (konto.getAnschaffung().after(Datum.addTage(vongj, -1))
            && konto.getAnschaffung().before(Datum.addTage(bisgj, 1)))
        {
          Double betrag = 0d;
          DBService service2 = Einstellungen.getDBService();
          DBIterator<Buchung> buchungenIt = service2.createList(Buchung.class);
          buchungenIt.join("buchungsart");
          buchungenIt.addFilter("buchungsart.id = buchung.buchungsart");
          buchungenIt.addFilter("konto = ?", new Object[] { konto.getID() });
          buchungenIt.addFilter("buchungsart.abschreibung = FALSE");
          buchungenIt.addFilter("datum <= ?",
              new Object[] { new java.sql.Date(bisgj.getTime()) });
          while (buchungenIt.hasNext())
          {
            betrag += ((Buchung) buchungenIt.next()).getBetrag();
          }
          if (Math.abs(betrag - konto.getBetrag()) > Double.MIN_NORMAL)
          {
            text = text + "Für das Anlagenkonto mit der Nummer "
                + konto.getNummer() + " stimmt die Summe der Buchungen ("
                + betrag + ") nicht mit den Anschaffungskosten ("
                + konto.getBetrag()
                + ") überein. Bitte auf Plausibilität prüfen!\n";
          }
        }
      }
    }
    catch (Exception e)
    {
      String fehler = "Fehler beim Initialisieren der Anzeige";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    return text;
  }

}
