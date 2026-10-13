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
import de.jost_net.JVerein.gui.input.MitgliedInput;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class LehrgangDetailControl extends VorZurueckControl implements Savable
{

  private SelectInput lehrgangsart;

  private DateInput von = null;

  private DateInput bis = null;

  private TextInput veranstalter = null;

  private TextInput ergebnis = null;

  private Lehrgang lehrg = null;

  private AbstractInput mitglied;

  private TextInput bezeichnung;

  public LehrgangDetailControl(AbstractView view)
  {
    super(view);
  }

  public Lehrgang getLehrgang()
  {
    if (lehrg != null)
    {
      return lehrg;
    }
    lehrg = (Lehrgang) getCurrentObject();
    return lehrg;
  }

  public SelectInput getLehrgangsart() throws RemoteException
  {
    if (lehrgangsart != null)
    {
      return lehrgangsart;
    }
    DBIterator<Lehrgangsart> it = Einstellungen.getDBService()
        .createList(Lehrgangsart.class);
    it.setOrder("order by bezeichnung");
    lehrgangsart = new SelectInput(
        it != null ? PseudoIterator.asList(it) : null,
        getLehrgang().getLehrgangsart());
    lehrgangsart.setPleaseChoose("Bitte auswählen");
    lehrgangsart.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Lehrgangsart la = (Lehrgangsart) lehrgangsart.getValue();
        try
        {
          if (la != null)
          {
            getVon().setValue(la.getVon());
            getBis().setValue(la.getBis());
            getVeranstalter().setValue(la.getVeranstalter());
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    lehrgangsart.setMandatory(true);
    return lehrgangsart;
  }

  public DateInput getVon() throws RemoteException
  {
    if (von != null)
    {
      return von;
    }

    Date d = getLehrgang().getVon();

    this.von = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.von.setTitle("Datum");
    this.von.setText("Bitte (Beginn-)Datum wählen");
    von.setMandatory(true);
    return von;
  }

  public DateInput getBis() throws RemoteException
  {
    if (bis != null)
    {
      return bis;
    }

    Date d = getLehrgang().getBis();

    this.bis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    this.bis.setTitle("Datum");
    this.bis.setText("Bitte Ende-Datum wählen");
    return bis;
  }

  public Input getVeranstalter() throws RemoteException
  {
    if (veranstalter != null)
    {
      return veranstalter;
    }
    veranstalter = new TextInput(getLehrgang().getVeranstalter(), 50);
    return veranstalter;
  }

  public Input getErgebnis() throws RemoteException
  {
    if (ergebnis != null)
    {
      return ergebnis;
    }
    ergebnis = new TextInput(getLehrgang().getErgebnis(), 50);
    return ergebnis;
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getLehrgang().getBezeichnung(), 30);
    return bezeichnung;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Lehrgang l = getLehrgang();

    Lehrgangsart la = (Lehrgangsart) getLehrgangsart().getValue();
    if (la != null)
    {
      l.setLehrgangsart(Long.valueOf(la.getID()));
    }
    else
    {
      l.setLehrgangsart(null);
    }
    l.setBezeichnung((String) getBezeichnung().getValue());
    l.setVon((Date) getVon().getValue());
    l.setBis((Date) getBis().getValue());
    l.setVeranstalter((String) getVeranstalter().getValue());
    l.setErgebnis((String) getErgebnis().getValue());
    if (l.isNewObject())
    {
      if (getMitglied().getValue() != null)
      {
        l.setMitglied(
            Integer.valueOf(((Mitglied) getMitglied().getValue()).getID()));
      }
      else
      {
        l.setMitglied(null);
      }
    }
    return l;
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
      String fehler = "Fehler bei speichern des Lehrgangs";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    if (getLehrgang().getMitglied() != null)
    {
      Mitglied[] mitgliedArray = { getLehrgang().getMitglied() };
      mitglied = new SelectInput(mitgliedArray, getLehrgang().getMitglied());
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

}
