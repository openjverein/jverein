/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe, Leonardo Mörlein
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
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.view.SollbuchungDetailView;
import de.jost_net.JVerein.keys.SteuersatzBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.logging.Logger;

public class SollbuchungPositionControl extends AbstractControl
{

  private DateInput datum;

  private TextAreaInput zweck;

  private DecimalInput betrag;

  private AbstractInput buchungsart;

  private SelectInput buchungsklasse;

  private SelectInput steuersatz;

  private SollbuchungPosition position = null;

  public SollbuchungPositionControl(AbstractView view)
  {
    super(view);
  }

  public SollbuchungPosition getPosition()
  {
    if (position != null)
    {
      return position;
    }
    position = (SollbuchungPosition) getCurrentObject();
    return position;
  }

  public DecimalInput getBetrag() throws RemoteException
  {
    if (betrag != null)
    {
      return betrag;
    }

    if (getPosition().isNewObject() && getPosition().getBetrag() == null)
    {
      betrag = new DecimalInput(Einstellungen.DECIMALFORMAT);
    }
    else
    {
      betrag = new DecimalInput(getPosition().getBetrag(),
          Einstellungen.DECIMALFORMAT);
    }
    betrag.setMandatory(true);
    return betrag;
  }

  public Input getZweck() throws RemoteException
  {
    if (zweck != null)
    {
      return zweck;
    }
    zweck = new TextAreaInput(getPosition().getZweck(), 500);
    zweck.setHeight(50);
    zweck.setMandatory(true);
    return zweck;
  }

  public DateInput getDatum() throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }
    Date d = getPosition().getDatum();
    this.datum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.datum.setTitle("Datum");
    this.datum.setText("Bitte Datum wählen");
    datum.setMandatory(true);
    return datum;
  }

  public Input getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(buchungsart,
        getPosition().getBuchungsart(), buchungsarttyp.BUCHUNGSART,
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

  public Input getBuchungsklasse() throws RemoteException
  {
    if (buchungsklasse != null)
    {
      return buchungsklasse;
    }
    buchungsklasse = new BuchungsklasseInput().getBuchungsklasseInput(
        buchungsklasse, getPosition().getBuchungsklasse());
    return buchungsklasse;
  }

  public SelectInput getSteuersatz() throws RemoteException
  {
    if (steuersatz != null)
    {
      return steuersatz;
    }
    if (getPosition().getSteuersatz() == null)
    {
      steuersatz = new SelectInput(SteuersatzBuchungsart.getArray(),
          new SteuersatzBuchungsart(0));
    }
    else
    {
      steuersatz = new SelectInput(SteuersatzBuchungsart.getArray(),
          new SteuersatzBuchungsart(getPosition().getSteuersatz()));
    }
    return steuersatz;
  }

  public void handleStore()
  {
    try
    {
      SollbuchungPosition pos = getPosition();
      pos.setDatum((Date) getDatum().getValue());
      pos.setZweck((String) getZweck().getValue());
      pos.setBetrag((Double) getBetrag().getValue());
      if (getBuchungsart().getValue() != null)
      {
        Buchungsart ba = (Buchungsart) getBuchungsart().getValue();
        pos.setBuchungsartId(Long.parseLong(ba.getID()));
        pos.setSteuersatz(ba.getSteuersatz());
      }
      else
      {
        pos.setBuchungsartId(null);
        pos.setSteuersatz(0.0);
      }
      if (getBuchungsklasse().getValue() != null)
      {
        pos.setBuchungsklasseId(Long.parseLong(
            ((Buchungsklasse) getBuchungsklasse().getValue()).getID()));
      }
      else
      {
        pos.setBuchungsklasseId(null);
      }
      pos.store();
      // Betrag in Sollbuchung neu berechnen
      Double betrag = 0.0;
      Mitgliedskonto sollb = pos.getSollbuchung();
      ArrayList<SollbuchungPosition> sollbpList = sollb
          .getSollbuchungPositionList();
      for (SollbuchungPosition sollp : sollbpList)
      {
        betrag += sollp.getBetrag();
      }
      sollb.setBetrag(betrag);
      sollb.store();

      GUI.startView(SollbuchungDetailView.class.getName(), sollb);
      GUI.getStatusBar().setSuccessText("Sollbuchungsposition gespeichert");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Speichern der Sollbuchungsposition";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
    catch (Exception e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
  }

}
