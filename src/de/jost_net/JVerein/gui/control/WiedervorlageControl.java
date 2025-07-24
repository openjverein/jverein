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
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.gui.parts.WiedervorlageList;
import de.jost_net.JVerein.gui.view.WiedervorlageListeView;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class WiedervorlageControl extends FilterControl implements Savable
{

  private DateInput datum = null;

  private Input vermerk = null;

  private DateInput erledigung = null;

  private AbstractInput mitglied;

  private Wiedervorlage wvl = null;

  private WiedervorlageList wiedervorlageList = null;

  public WiedervorlageControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Wiedervorlage getWiedervorlage()
  {
    if (wvl != null)
    {
      return wvl;
    }
    wvl = (Wiedervorlage) getCurrentObject();
    return wvl;
  }

  public DateInput getDatum(boolean withFocus) throws RemoteException
  {
    if (datum != null)
    {
      return datum;
    }

    Date d = getWiedervorlage().getDatum();

    this.datum = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.datum.setTitle("Datum");
    this.datum.setText("Bitte Wiedervorlagedatum wählen");
    this.datum.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) datum.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    if (withFocus)
    {
      datum.focus();
    }
    datum.setMandatory(true);
    return datum;
  }

  public Input getVermerk() throws RemoteException
  {
    if (vermerk != null)
    {
      return vermerk;
    }
    vermerk = new TextInput(getWiedervorlage().getVermerk(), 50);
    vermerk.setMandatory(true);
    return vermerk;
  }

  public DateInput getErledigung() throws RemoteException
  {
    if (erledigung != null)
    {
      return erledigung;
    }

    Date d = getWiedervorlage().getErledigung();

    this.erledigung = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.erledigung.setTitle("Erledigung");
    this.erledigung.setText("Bitte Erledigungsdatum wählen");
    this.erledigung.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Date date = (Date) erledigung.getValue();
        if (date == null)
        {
          return;
        }
      }
    });
    return erledigung;
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    if (getWiedervorlage().getMitglied() != null)
    {
      Mitglied[] mitgliedArray = { getWiedervorlage().getMitglied() };
      mitglied = new SelectInput(mitgliedArray,
          getWiedervorlage().getMitglied());
      mitglied.setEnabled(false);
    }
    else
    {
      mitglied = new MitgliedInput().getMitgliedInput(mitglied, null,
          (Integer) Einstellungen.getEinstellung(Property.MITGLIEDAUSWAHL));
    }
    mitglied.setMandatory(true);
    return mitglied;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Wiedervorlage w = getWiedervorlage();
    w.setDatum((Date) getDatum(false).getValue());
    w.setVermerk((String) getVermerk().getValue());
    w.setErledigung((Date) getErledigung().getValue());
    return w;
  }

  @Override
  public void handleStore() throws ApplicationException
  {
    try
    {
      Wiedervorlage w = (Wiedervorlage) prepareStore();
      if (w.isNewObject())
      {
        if (getMitglied().getValue() != null)
        {
          Mitglied m = (Mitglied) getMitglied().getValue();
          w.setMitglied(Integer.parseInt(m.getID()));
        }
        else
        {
          throw new ApplicationException("Bitte Mitglied eingeben");
        }
      }
      w.store();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern der Wiedervorlage";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Part getWiedervorlageList() throws RemoteException
  {
    wiedervorlageList = new WiedervorlageList(
        new StartViewAction(WiedervorlageListeView.class), this);
    return wiedervorlageList.getWiedervorlageList();
  }

  @Override
  public void TabRefresh()
  {
    if (wiedervorlageList == null)
    {
      return;
    }
    wiedervorlageList.refresh();
  }

}
