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
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.SteuerInput;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Steuer;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragVorlageDetailControl extends VorZurueckControl
    implements Savable
{
  private DateInput faelligkeit = null;

  private TextInput buchungstext;

  private DecimalInput betrag;

  private ZusatzbetragVorlage zbv;

  private DateInput startdatum;

  private SelectInput intervall;

  private DateInput endedatum;

  private AbstractInput buchungsart;

  private SelectInput buchungsklasse;

  public ZusatzbetragVorlage auswahl;

  private SelectInput zahlungsweg;

  private SteuerInput steuer = null;

  private CheckboxInput mitgliedZahltSelbst;

  public ZusatzbetragVorlageDetailControl(AbstractView view)
  {
    super(view);
  }

  private ZusatzbetragVorlage getZusatzbetragVorlage()
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
    return faelligkeit;
  }

  public TextInput getBuchungstext() throws RemoteException
  {
    if (buchungstext != null)
    {
      return buchungstext;
    }
    buchungstext = new TextInput(getZusatzbetragVorlage().getBuchungstext(),
        500);
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
    buchungsart = new BuchungsartInput().getBuchungsartInput(
        getZusatzbetragVorlage().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
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
    buchungsart.addListener(e -> {
      if (steuer != null && buchungsart.getValue() != null)
      {
        try
        {
          steuer.setValue(((Buchungsart) buchungsart.getValue()).getSteuer());
        }
        catch (RemoteException e1)
        {
          Logger.error("Fehler", e1);
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

  private Long getSelectedBuchungsKlasseId() throws ApplicationException
  {
    try
    {
      if (buchungsklasse == null)
        return null;
      Buchungsklasse bukla = (Buchungsklasse) getBuchungsklasse().getValue();
      if (null == bukla)
        return null;
      Long id = Long.valueOf(bukla.getID());
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
    zahlungsweg = new SelectInput(Zahlungsweg.getArray(),
        getZusatzbetragVorlage().getZahlungsweg());
    zahlungsweg.setPleaseChoose("Standard");
    return zahlungsweg;
  }

  public SelectInput getSteuer() throws RemoteException
  {
    if (steuer != null)
    {
      return steuer;
    }
    steuer = new SteuerInput(getZusatzbetragVorlage().getSteuer());

    steuer.setAttribute("name");
    steuer.setPleaseChoose("Keine Steuer");

    return steuer;
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

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
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
    if (steuer != null)
    {
      z.setSteuer((Steuer) steuer.getValue());
    }
    z.setMitgliedzahltSelbst((Boolean) getMitgliedzahltSelbst().getValue());
    return z;
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
      String fehler = "Fehler bei speichern der Zusatzbetrag-Vorlage";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public CheckboxInput getMitgliedzahltSelbst() throws RemoteException
  {
    if (mitgliedZahltSelbst != null)
    {
      return mitgliedZahltSelbst;
    }
    mitgliedZahltSelbst = new CheckboxInput(
        getZusatzbetragVorlage().getMitgliedzahltSelbst());
    mitgliedZahltSelbst.setName(
        " *Für den Fall, dass ein abweichender Zahler konfiguriert ist.");
    return mitgliedZahltSelbst;
  }

}
