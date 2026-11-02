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
import de.jost_net.JVerein.rmi.Lehrgangsteilnehmer;
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

public class LehrgangsteilnehmerDetailControl extends VorZurueckControl
    implements Savable
{

  private SelectInput lehrgangsart;

  private SelectInput lehrgang;

  private DateInput von = null;

  private DateInput bis = null;

  private TextInput veranstalter = null;

  private TextInput ergebnis = null;

  private Lehrgangsteilnehmer teilnehmer = null;

  private AbstractInput mitglied;

  private TextInput bezeichnung;

  public LehrgangsteilnehmerDetailControl(AbstractView view)
  {
    super(view);
  }

  public Lehrgangsteilnehmer getLehrgangsteilnehmer()
  {
    if (teilnehmer != null)
    {
      return teilnehmer;
    }
    teilnehmer = (Lehrgangsteilnehmer) getCurrentObject();
    return teilnehmer;
  }

  public Input getMitglied() throws RemoteException
  {
    if (mitglied != null)
    {
      return mitglied;
    }

    if (getLehrgangsteilnehmer().getMitglied() != null)
    {
      Mitglied[] mitgliedArray = { getLehrgangsteilnehmer().getMitglied() };
      mitglied = new SelectInput(mitgliedArray,
          getLehrgangsteilnehmer().getMitglied());
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

  public SelectInput getLehrgang() throws RemoteException
  {
    if (lehrgang != null)
    {
      return lehrgang;
    }
    DBIterator<Lehrgang> it = Einstellungen.getDBService()
        .createList(Lehrgang.class);
    it.setOrder("order by bezeichnung");
    lehrgang = new SelectInput(it != null ? PseudoIterator.asList(it) : null,
        getLehrgangsteilnehmer().getLehrgang());
    lehrgang.setPleaseChoose("Bitte auswählen");
    lehrgang.setMandatory(true);
    lehrgang.addListener(new Listener()
    {

      @Override
      public void handleEvent(Event event)
      {
        Lehrgang l = (Lehrgang) lehrgang.getValue();
        try
        {
          if (l != null)
          {
            getLehrgangsart().setValue(l.getLehrgangsart());
            getBezeichnung().setValue(l.getBezeichnung());
            getVon().setValue(l.getVon());
            getBis().setValue(l.getBis());
            getVeranstalter().setValue(l.getVeranstalter());
          }
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler", e);
        }
      }
    });
    return lehrgang;
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
        getLehrgangsteilnehmer().getLehrgangsart());
    lehrgangsart.disable();
    return lehrgangsart;
  }

  public DateInput getVon() throws RemoteException
  {
    if (von != null)
    {
      return von;
    }

    Date d = getLehrgangsteilnehmer().getVon();
    von = new DateInput(d, new JVDateFormatTTMMJJJJ());
    von.setTitle("Datum");
    von.disable();
    return von;
  }

  public DateInput getBis() throws RemoteException
  {
    if (bis != null)
    {
      return bis;
    }

    Date d = getLehrgangsteilnehmer().getBis();
    bis = new DateInput(d, new JVDateFormatTTMMJJJJ());
    bis.setTitle("Datum");
    bis.disable();
    return bis;
  }

  public Input getVeranstalter() throws RemoteException
  {
    if (veranstalter != null)
    {
      return veranstalter;
    }
    veranstalter = new TextInput(getLehrgangsteilnehmer().getVeranstalter(),
        50);
    veranstalter.disable();
    return veranstalter;
  }

  public Input getErgebnis() throws RemoteException
  {
    if (ergebnis != null)
    {
      return ergebnis;
    }
    ergebnis = new TextInput(getLehrgangsteilnehmer().getErgebnis(), 50);
    return ergebnis;
  }

  public TextInput getBezeichnung() throws RemoteException
  {
    if (bezeichnung != null)
    {
      return bezeichnung;
    }
    bezeichnung = new TextInput(getLehrgangsteilnehmer().getBezeichnung(), 30);
    bezeichnung.disable();
    return bezeichnung;
  }

  @Override
  public JVereinDBObject prepareStore() throws RemoteException
  {
    Lehrgangsteilnehmer teilnehmer = getLehrgangsteilnehmer();

    Lehrgang l = (Lehrgang) getLehrgang().getValue();
    if (l != null)
    {
      teilnehmer.setLehrgang(Long.valueOf(l.getID()));
      teilnehmer.setLehrgangsart((Long.valueOf(l.getLehrgangsart().getID())));
      teilnehmer.setVeranstalter(l.getVeranstalter());
      teilnehmer.setVon(l.getVon());
      teilnehmer.setBis(l.getBis());
    }
    else
    {
      teilnehmer.setLehrgang(null);
    }
    teilnehmer.setErgebnis((String) getErgebnis().getValue());
    if (teilnehmer.isNewObject())
    {
      if (getMitglied().getValue() != null)
      {
        teilnehmer.setMitglied(
            Integer.valueOf(((Mitglied) getMitglied().getValue()).getID()));
      }
      else
      {
        teilnehmer.setMitglied(null);
      }
    }
    return teilnehmer;
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
      String fehler = "Fehler bei speichern des Lehrgangsteilnehmers";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
