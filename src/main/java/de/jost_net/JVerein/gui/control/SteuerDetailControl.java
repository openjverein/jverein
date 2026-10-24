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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.input.BigDecimalInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput;
import de.jost_net.JVerein.gui.input.BuchungsartInput.buchungsarttyp;
import de.jost_net.JVerein.gui.input.BuchungsklasseInput;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SteuerDetailControl extends VorZurueckControl implements Savable
{

  private TextInput name;

  private BigDecimalInput satz;

  private AbstractInput buchungsart;

  private SelectInput buchungsklasse;

  private CheckboxInput aktiv;

  private Steuer steuer;

  public SteuerDetailControl(AbstractView view)
  {
    super(view);
  }

  private Steuer getSteuer()
  {
    if (steuer != null)
    {
      return steuer;
    }
    steuer = (Steuer) getCurrentObject();
    return steuer;
  }

  public TextInput getName() throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getSteuer().getName(), 50);
    name.setMandatory(true);
    return name;
  }

  public BigDecimalInput getSatz() throws RemoteException
  {
    if (satz != null)
    {
      return satz;
    }
    satz = new BigDecimalInput(getSteuer().getSatz(),
        Einstellungen.BIGDECIMALFORMAT);
    satz.setComment("%");
    satz.setMandatory(true);
    return satz;
  }

  public AbstractInput getBuchungsart() throws RemoteException
  {
    if (buchungsart != null)
    {
      return buchungsart;
    }
    buchungsart = new BuchungsartInput().getBuchungsartInput(
        getSteuer().getBuchungsart(), buchungsarttyp.STEUERART,
        (Integer) Einstellungen
            .getEinstellung(Property.BUCHUNGBUCHUNGSARTAUSWAHL));
    buchungsart.setMandatory(true);
    buchungsart.setComment("");
    Listener listener = new Listener()
    {
      @Override
      public void handleEvent(Event e)
      {
        String comment = "";
        if (buchungsart.getValue() != null)
        {
          try
          {
            switch (((Buchungsart) buchungsart.getValue()).getArt())
            {
              case ArtBuchungsart.AUSGABE:
                comment = "Ausgabe -> Vorsteuer";
                break;
              case ArtBuchungsart.EINNAHME:
                comment = "Einnahme -> Umsatzsteuer";
                break;
              case ArtBuchungsart.UMBUCHUNG:
                comment = "Umbuchung ist Ungültig";
                break;
            }
          }
          catch (RemoteException re)
          {
            Logger.error("Fehler", re);
          }
        }
        buchungsart.setComment(comment);
      }
    };
    buchungsart.addListener(listener);
    listener.handleEvent(null);
    buchungsart.addListener(e -> {
      if (buchungsklasse != null && buchungsart.getValue() != null)
      {
        try
        {
          buchungsklasse.setValue(
              ((Buchungsart) buchungsart.getValue()).getBuchungsklasse());
        }
        catch (RemoteException e1)
        {
          Logger.error("Fehler beim Buchungsart-Listener", e1);
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
        buchungsklasse, getSteuer().getBuchungsklasse());
    return buchungsklasse;
  }

  public CheckboxInput getAktiv() throws RemoteException
  {
    if (aktiv != null)
    {
      return aktiv;
    }
    aktiv = new CheckboxInput(getSteuer().getAktiv());
    return aktiv;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Steuer s = getSteuer();
    s.setName((String) getName().getValue());
    s.setSatz((BigDecimal) getSatz().getValue());
    if (getBuchungsart().getValue() != null)
    {
      s.setBuchungsartId(
          Long.parseLong(((Buchungsart) getBuchungsart().getValue()).getID()));
    }
    if ((Boolean) Einstellungen
        .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
    {
      s.setBuchungsklasse((Buchungsklasse) getBuchungsklasse().getValue());
    }
    s.setAktiv((Boolean) getAktiv().getValue());
    return s;
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
      String fehler = "Fehler bei speichern der Steuer";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
