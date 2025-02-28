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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.EditAction;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.gui.formatter.BuchungsklasseFormatter;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.menu.ZusatzbetragVorlageMenu;
import de.jost_net.JVerein.gui.view.ZusatzbetragVorlageDetailView;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragVorlageControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings;

  private DateInput faelligkeit = null;

  private TextInput buchungstext;

  private DecimalInput betrag;

  private ZusatzbetragVorlage zbv;

  private DateInput startdatum;

  private SelectInput intervall;

  private DateInput endedatum;

  private AbstractInput buchungsart;

  private SelectInput buchungsklasse;

  private TablePart zusatzbetragVorlageList;

  public ZusatzbetragVorlage auswahl;

  private SelectInput zahlungsweg;

  public ZusatzbetragVorlageControl(AbstractView view)
  {
    super(view);
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public ZusatzbetragVorlage getZusatzbetragVorlage()
  {
    if (zbv != null)
    {
      return zbv;
    }
    zbv = (ZusatzbetragVorlage) getCurrentObject();
    return zbv;
  }

  public DateInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }

    Date d = getZusatzbetragVorlage().getFaelligkeit();

    this.faelligkeit = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.faelligkeit.setTitle("Fälligkeit");
    this.faelligkeit.setText("Bitte Fälligkeitsdatum wählen");
    this.faelligkeit.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) faelligkeit.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return faelligkeit;
  }

  public TextInput getBuchungstext() throws RemoteException
  {
    if (buchungstext != null)
    {
      return buchungstext;
    }
    buchungstext = new TextInput(getZusatzbetragVorlage().getBuchungstext(),
        140);
    buchungstext.setMandatory(true);
    buchungstext.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    return buchungstext;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(getZusatzbetragVorlage().getBetrag(),
        Einstellungen.DECIMALFORMAT);
    betrag.setMandatory(true);
    return betrag;
  }

  public DateInput getStartdatum(boolean withFocus) throws RemoteException
  {
    if (startdatum != null)
    {
      return startdatum;
    }

    Date d = getZusatzbetragVorlage().getStartdatum();
    this.startdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.startdatum.setTitle("Startdatum");
    this.startdatum.setText("Bitte Startdatum wählen");
    this.startdatum.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) startdatum.getValue();
        if (date == null)
        {
          return;
        }
        startdatum.setValue(date);
        if (faelligkeit.getValue() == null)
        {
          faelligkeit.setValue(startdatum.getValue());
        }
      }
    });
    if (withFocus)
    {
      startdatum.focus();
    }
    return startdatum;
  }

  public SelectInput getIntervall() throws RemoteException
  {
    if (intervall != null)
    {
      return intervall;
    }
    Integer i = getZusatzbetragVorlage().getIntervall();
    if (i == null)
    {
      i = Integer.valueOf(0);
    }
    this.intervall = new SelectInput(IntervallZusatzzahlung.getArray(),
        new IntervallZusatzzahlung(i));
    return intervall;
  }

  public AbstractInput getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(buchungsart,
        getZusatzbetragVorlage().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
        Einstellungen.getEinstellung().getBuchungBuchungsartAuswahl());
    buchungsart.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          Buchungsart bua = (Buchungsart) buchungsart.getValue();
          if (buchungsklasse != null && buchungsklasse.getValue() == null
              && bua != null)
            buchungsklasse.setValue(bua.getBuchungsklasse());
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    return buchungsart;
  }

  public SelectInput getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    buchungsklasse = new BuchungsklasseInput().getBuchungsklasseInput(
        buchungsklasse, getZusatzbetragVorlage().getBuchungsklasse());
    return buchungsklasse;
  }

  public boolean isBuchungsklasseActive()
  {
    return buchungsklasse != null;
  }

  public Long getSelectedBuchungsKlasseId() throws ApplicationException
  {
    try
    {
      if (null == buchungsklasse)
        return null;
      Buchungsklasse buchungsKlasse = (Buchungsklasse) getBuchungsklasse()
          .getValue();
      if (null == buchungsKlasse)
        return null;
      Long id = Long.valueOf(buchungsKlasse.getID());
      return id;
    }
    catch (RemoteException ex)
    {
      final String meldung = "Gewählte Buchungsklasse kann nicht ermittelt werden";
      Logger.error(meldung, ex);
      throw new ApplicationException(meldung, ex);
    }
  }

  public SelectInput getZahlungsweg() throws RemoteException
  {
    if (zahlungsweg != null)
    {
      return zahlungsweg;
    }
    zahlungsweg = new SelectInput(Zahlungsweg.getArray(false),
        getZusatzbetragVorlage().getZahlungsweg());
    zahlungsweg.setPleaseChoose("Standard");
    return zahlungsweg;
  }

  public DateInput getEndedatum() throws RemoteException
  {
    if (endedatum != null)
    {
      return endedatum;
    }

    Date d = getZusatzbetragVorlage().getEndedatum();
    this.endedatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.endedatum.setTitle("Nicht mehr ausführen ab");
    this.endedatum.setText("Bitte Endedatum wählen");
    this.endedatum.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) endedatum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return endedatum;
  }

  public void handleStore()
  {
    try
    {
      ZusatzbetragVorlage z = getZusatzbetragVorlage();
      z.setFaelligkeit((Date) getFaelligkeit().getValue());
      z.setStartdatum((Date) getStartdatum(false).getValue());
      IntervallZusatzzahlung iz = (IntervallZusatzzahlung) getIntervall()
          .getValue();
      z.setIntervall(iz.getKey());
      z.setEndedatum((Date) getEndedatum().getValue());
      z.setBuchungstext((String) getBuchungstext().getValue());
      Double d = (Double) getBetrag().getValue();
      z.setBetrag(d.doubleValue());
      z.setBuchungsart((Buchungsart) getBuchungsart().getValue());
      z.setBuchungsklasseId(getSelectedBuchungsKlasseId());
      z.setZahlungsweg((Zahlungsweg) getZahlungsweg().getValue());

      z.store();
      GUI.getStatusBar().setSuccessText("Zusatzbetrag-Vorlage gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Zusatzbetrag-Vorlage";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public Part getZusatzbetraegeVorlageList() throws RemoteException
  {
    DBIterator<ZusatzbetragVorlage> zusatzbetragsvorlagen = Einstellungen
        .getDBService().createList(ZusatzbetragVorlage.class);
    zusatzbetragsvorlagen.setOrder("ORDER BY buchungstext");

    if (zusatzbetragVorlageList == null)
    {
      zusatzbetragVorlageList = new TablePart(zusatzbetragsvorlagen,
          new EditAction(ZusatzbetragVorlageDetailView.class));
      zusatzbetragVorlageList.addColumn("Erste Fälligkeit", "startdatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetragVorlageList.addColumn("Nächste Fälligkeit", "faelligkeit",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetragVorlageList.addColumn("Intervall", "intervalltext");
      zusatzbetragVorlageList.addColumn("Nicht mehr ausführen ab", "endedatum",
          new DateFormatter(new JVDateFormatTTMMJJJJ()));
      zusatzbetragVorlageList.addColumn("Buchungstext", "buchungstext");
      zusatzbetragVorlageList.addColumn("Betrag", "betrag",
          new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
      zusatzbetragVorlageList.addColumn("Zahlungsweg", "zahlungsweg",
          new Formatter()
          {
            @Override
            public String format(Object o)
            {
              return new Zahlungsweg((Integer) o).getText();
            }
          });
      if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
      {
        zusatzbetragVorlageList.addColumn("Buchungsklasse", "buchungsklasse",
            new BuchungsklasseFormatter());
      }
      zusatzbetragVorlageList.addColumn("Buchungsart", "buchungsart",
          new BuchungsartFormatter());

      zusatzbetragVorlageList.setContextMenu(new ZusatzbetragVorlageMenu());
      zusatzbetragVorlageList.setRememberColWidths(true);
      zusatzbetragVorlageList.setRememberOrder(true);
      zusatzbetragVorlageList.addFeature(new FeatureSummary());
      zusatzbetragVorlageList.setMulti(true);
    }
    else
    {
      zusatzbetragVorlageList.removeAll();
      while (zusatzbetragsvorlagen.hasNext())
      {
        zusatzbetragVorlageList.addItem(zusatzbetragsvorlagen.next());
      }
      zusatzbetragVorlageList.sort();
    }
    return zusatzbetragVorlageList;
  }
}
