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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.rmi.Rechnung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

public class RechnungVersandAction implements Action
{
  private boolean versendet;

  public RechnungVersandAction(boolean versendet)
  {
    this.versendet = versendet;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Rechnung[] rechnungen = null;
    if (context instanceof Rechnung)
    {
      rechnungen = new Rechnung[1];
      rechnungen[0] = (Rechnung) context;
    }
    else if (context instanceof Rechnung[])
    {
      rechnungen = (Rechnung[]) context;
    }
    if (rechnungen == null)
    {
      return;
    }
    if (rechnungen.length == 0)
    {
      return;
    }

    try
    {
      for (Rechnung r : rechnungen)
      {
        r.setVersand(versendet);
        r.store();
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
  }
}
