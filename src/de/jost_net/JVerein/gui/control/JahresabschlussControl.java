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
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.menu.JahresabschlussMenu;
import de.jost_net.JVerein.gui.util.AfaUtil;
import de.jost_net.JVerein.gui.view.JahresabschlussDetailView;
import de.jost_net.JVerein.keys.Kontoart;
import de.jost_net.JVerein.rmi.Anfangsbestand;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.server.ExtendedDBIterator;
import de.jost_net.JVerein.server.PseudoDBObject;
import de.jost_net.JVerein.util.Datum;
import de.jost_net.JVerein.util.Geschaeftsjahr;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
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

public class JahresabschlussControl extends KontensaldoControl
{

  private de.willuhn.jameica.system.Settings settings;

  private TablePart jahresabschlussList;

  private DateInput datum;

  private TextInput name;

  private Jahresabschluss jahresabschluss;

  private CheckboxInput anfangsbestaende;

  private CheckboxInput afaberechnung;

  private DecimalInput verwendungsrueckstand;

  private DecimalInput zwanghafteweitergabe;

  private DateLabel datumvon;

  private DateLabel datumbis;

  public JahresabschlussControl(AbstractView view) throws RemoteException
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    summensaldo = false;
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
      MittelverwendungControl mvcontrol = new MittelverwendungControl(null);
      mvcontrol.getMittelverwendungFlowList(getDatumvon().getDate(),
          getDatumbis().getDate());
      jahresabschluss
          .setVerwendungsrueckstand(mvcontrol.getRueckstandVorjahrNeu());
      jahresabschluss
          .setZwanghafteWeitergabe(mvcontrol.getZwanghafteWeitergabeNeu());
    }
    return jahresabschluss;
  }

  @Override
  public DateLabel getDatumvon()
  {
    if (datumvon != null)
    {
      return datumvon;
    }
    Date d = new Date();
    try
    {
      if (getJahresabschluss().isNewObject())
      {
        d = computeVonDatum();
      }
      else
      {
        d = getJahresabschluss().getVon();
      }
    }
    catch (ParseException | RemoteException e)
    {
      Logger.error("Fehler beim setzen des Startdatums", e);
    }
    datumvon = new DateLabel(d);
    datumvon.disable();
    return datumvon;
  }

  private Date computeVonDatum() throws RemoteException, ParseException
  {
    // Datum des Letzten Jahresabschlusses + 1
    DBIterator<Jahresabschluss> it = Einstellungen.getDBService()
        .createList(Jahresabschluss.class);
    it.setOrder("ORDER BY bis DESC");
    it.setLimit(1);
    if (it.hasNext())
    {
      Jahresabschluss ja = it.next();
      return DateUtils.addDays(ja.getBis(), 1);
    }

    // Geschäftsjahres-Beging aus Datum der ersten Buchung bestimmen, wenn noch
    // kein Jahresabschluss existiert.
    DBIterator<Buchung> itbu = Einstellungen.getDBService()
        .createList(Buchung.class);
    itbu.setOrder("ORDER BY datum");
    itbu.setLimit(1);
    if (itbu.hasNext())
    {
      Buchung b = itbu.next();
      Geschaeftsjahr gj = new Geschaeftsjahr(b.getDatum());
      return gj.getBeginnGeschaeftsjahr();
    }

    // Wenn es noch keine Buchungen gibt, nehmen wir den Beginn des aktuellen
    // Geschäftsjahres.
    Geschaeftsjahr gj = new Geschaeftsjahr(new Date());
    return gj.getBeginnGeschaeftsjahr();
  }

  @Override
  public DateLabel getDatumbis()
  {
    if (datumbis != null)
    {
      return datumbis;
    }
    Date d = new Date();
    try
    {
      Geschaeftsjahr gj = new Geschaeftsjahr(getDatumvon().getDate());
      d = gj.getEndeGeschaeftsjahr();
    }
    catch (ParseException | RemoteException e)
    {
      Logger.error("Fehler beim setzen des Enddatums", e);
    }
    datumbis = new DateLabel(d);
    return datumbis;
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
      ja.setVon(getDatumvon().getDate());
      ja.setBis(getDatumbis().getDate());
      ja.setDatum((Date) getDatum().getValue());
      ja.setName((String) getName().getValue());
      ja.store();
      if (afaberechnung != null && (Boolean) getAfaberechnung().getValue())
      {
        new AfaUtil(new Geschaeftsjahr(ja.getVon()), ja);
      }
      if (Einstellungen.getEinstellung().getMittelverwendung())
      {
        MittelverwendungControl mvcontrol = new MittelverwendungControl(null);
        mvcontrol.getMittelverwendungFlowList(ja.getVon(), ja.getBis());
        ja.setVerwendungsrueckstand(mvcontrol.getRueckstandVorjahrNeu());
        ja.setZwanghafteWeitergabe(mvcontrol.getZwanghafteWeitergabeNeu());
        ja.store();
      }
      if ((Boolean) getAnfangsbestaende().getValue())
      {
        ArrayList<PseudoDBObject> zeilen = getList();
        for (PseudoDBObject z : zeilen)
        {
          String ktonr = (String) z.getAttribute(KONTO_NUMMER);
          if (ktonr != null && ktonr.length() > 0)
          {
            Double endbestand = (Double) z.getAttribute(ENDBESTAND);
            Anfangsbestand anf = (Anfangsbestand) Einstellungen.getDBService()
                .createObject(Anfangsbestand.class, null);
            String konto = z.getInteger(KONTO_ID).toString();
            anf.setBetrag(endbestand);
            anf.setDatum(Datum.addTage(ja.getBis(), 1));
            anf.setKontoId(konto);
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

  /**
   * Infotext bei Fehlenden Angaben bestimmen.
   * 
   * @return
   */
  public String getInfo()
  {
    String text = "";
    try
    {
      Date vongj = getDatumvon().getDate();
      Date bisgj = getDatumbis().getDate();
      DBService service = Einstellungen.getDBService();
      DBIterator<Konto> kontenIt = service.createList(Konto.class);
      kontenIt.addFilter("kontoart = ?", Kontoart.ANLAGE.getKey());
      kontenIt.addFilter("(eroeffnung IS NULL OR eroeffnung <= ?)", bisgj);
      kontenIt.addFilter("(aufloesung IS NULL OR aufloesung >= ?)", vongj);
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
          ExtendedDBIterator<PseudoDBObject> it = new ExtendedDBIterator<>(
              "buchung");
          Double betrag = 0d;
          it.join("buchungsart", "buchungsart.id = buchung.buchungsart");
          it.addFilter("konto = ?", konto.getID());
          it.addFilter("buchungsart.abschreibung = FALSE");
          it.addFilter("datum <= ?", bisgj);
          it.addColumn("sum(buchung.betrag) as summe");
          PseudoDBObject o = it.next();
          if (o != null)
          {
            betrag = o.getDouble("summe");
          }
          if (Math.abs(betrag - konto.getBetrag()) > Double.MIN_NORMAL)
          {
            text += "Für das Anlagenkonto mit der Nummer "
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
