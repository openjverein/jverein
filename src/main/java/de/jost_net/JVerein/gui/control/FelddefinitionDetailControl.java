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
import de.jost_net.JVerein.keys.Datentyp;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FelddefinitionDetailControl extends VorZurueckControl
    implements Savable
{

  private Input name;

  private Input label;

  private SelectInput datentyp;

  private IntegerInput laenge;

  private Felddefinition felddefinition;

  public FelddefinitionDetailControl(AbstractView view)
  {
    super(view);
  }

  private Felddefinition getFelddefinition()
  {
    if (felddefinition != null)
    {
      return felddefinition;
    }
    felddefinition = (Felddefinition) getCurrentObject();
    return felddefinition;
  }

  public Input getName(boolean withFocus) throws RemoteException
  {
    if (name != null)
    {
      return name;
    }
    name = new TextInput(getFelddefinition().getName(), 50);
    name.setMandatory(true);
    if (withFocus)
    {
      name.focus();
    }
    return name;
  }

  public Input getLabel() throws RemoteException
  {
    if (label != null)
    {
      return label;
    }
    label = new TextInput(getFelddefinition().getLabel(), 50);
    label.setMandatory(true);
    return label;
  }

  public SelectInput getDatentyp() throws RemoteException
  {
    if (datentyp != null)
    {
      return datentyp;
    }
    datentyp = new SelectInput(Datentyp.getArray(),
        new Datentyp(getFelddefinition().getDatentyp()));
    return datentyp;
  }

  public IntegerInput getLaenge() throws RemoteException
  {
    if (laenge != null)
    {
      return laenge;
    }
    laenge = new IntegerInput(getFelddefinition().getLaenge());
    laenge.setMandatory(true);
    return laenge;
  }

  @Override
  public JVereinDBObject prepareStore()
      throws RemoteException, ApplicationException
  {
    Felddefinition f = getFelddefinition();
    Datentyp d = (Datentyp) getDatentyp().getValue();
    f.setName((String) getName(true).getValue());
    f.setLabel((String) getLabel().getValue());
    f.setDatentyp(d.getKey());
    Integer i = (Integer) getLaenge().getValue();
    f.setLaenge(i.intValue());
    return f;
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
      String fehler = "Fehler bei speichern der Felddefinition";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler, e);
    }
  }

}
