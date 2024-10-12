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
package de.jost_net.JVerein.gui.parts;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.LabelGroup;

public class ZusatzbetragPart implements Part
{
  private Zusatzbetrag zusatzbetrag;

  private DateInput faelligkeit = null;

  private TextInput buchungstext;

  private DecimalInput betrag;

  private DateInput startdatum;

  private SelectInput intervall;

  private DateInput endedatum;

  private DateInput ausfuehrung = null;

  private AbstractInput buchungsart;

  public ZusatzbetragPart(Zusatzbetrag zusatzbetrag)
  {
    this.zusatzbetrag = zusatzbetrag;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent, "Zusatzbetrag");
    group.addLabelPair("Startdatum", getStartdatum(true));
    group.addLabelPair("N�chste F�lligkeit", getFaelligkeit());
    group.addLabelPair("Intervall", getIntervall());
    group.addLabelPair("Endedatum", getEndedatum());
    group.addLabelPair("Buchungstext", getBuchungstext());
    group.addLabelPair("Betrag", getBetrag());
    group.addLabelPair("Buchungsart", getBuchungsart());
  }

  public DateInput getFaelligkeit() throws RemoteException
  {
    if (faelligkeit != null)
    {
      return faelligkeit;
    }

    Date d = zusatzbetrag.getFaelligkeit();

    this.faelligkeit = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.faelligkeit.setTitle("F�lligkeit");
    this.faelligkeit.setText("Bitte F�lligkeitsdatum w�hlen");
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
    buchungstext = new TextInput(zusatzbetrag.getBuchungstext(), 140);
    buchungstext.setMandatory(true);
    return buchungstext;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }
    betrag = new DecimalInput(zusatzbetrag.getBetrag(),
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

    Date d = zusatzbetrag.getStartdatum();
    this.startdatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.startdatum.setTitle("Startdatum");
    this.startdatum.setText("Bitte Startdatum w�hlen");
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
    Integer i = zusatzbetrag.getIntervall();
    if (i == null)
    {
      i = Integer.valueOf(0);
    }
    this.intervall = new SelectInput(IntervallZusatzzahlung.getArray(),
        new IntervallZusatzzahlung(i));
    return intervall;
  }

  public DateInput getEndedatum() throws RemoteException
  {
    if (endedatum != null)
    {
      return endedatum;
    }

    Date d = zusatzbetrag.getEndedatum();
    this.endedatum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.endedatum.setTitle("Endedatum");
    this.endedatum.setText("Bitte Endedatum w�hlen");
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

  public DateInput getAusfuehrung() throws RemoteException
  {
    if (ausfuehrung != null)
    {
      return ausfuehrung;
    }

    Date d = zusatzbetrag.getAusfuehrung();

    this.ausfuehrung = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.ausfuehrung.setTitle("Ausf�hrung");
    this.ausfuehrung.setText("Bitte Ausf�hrungsdatum w�hlen");
    this.ausfuehrung.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) ausfuehrung.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    ausfuehrung.setEnabled(false);
    return ausfuehrung;
  }

  public AbstractInput getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(buchungsart,
        zusatzbetrag.getBuchungsart(), buchungsarttyp.BUCHUNGSART);
    return buchungsart;
  }
}
