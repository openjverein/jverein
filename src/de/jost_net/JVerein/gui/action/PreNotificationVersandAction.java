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
import java.util.Date;

import de.jost_net.JVerein.gui.dialogs.VersandDatumDialog;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class PreNotificationVersandAction implements Action
{
  private boolean versendet;

  public PreNotificationVersandAction(boolean versendet)
  {
    this.versendet = versendet;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Lastschrift[] lastschriften = null;
    if (context instanceof Lastschrift)
    {
      lastschriften = new Lastschrift[1];
      lastschriften[0] = (Lastschrift) context;
    }
    else if (context instanceof Lastschrift[])
    {
      lastschriften = (Lastschrift[]) context;
    }
    if (lastschriften == null)
    {
      return;
    }
    if (lastschriften.length == 0)
    {
      return;
    }

    Date datum = null;
    if (versendet)
    {
      try
      {
        VersandDatumDialog d = new VersandDatumDialog(
            VersandDatumDialog.POSITION_MOUSE);
        datum = d.open();
        // Wenn Dialog über das Schliesen Icon geschlossen wurde
        if (d.getClosed())
        {
          return;
        }
      }
      catch (OperationCanceledException oce)
      {
        throw oce;
      }
      catch (Exception e)
      {
        Logger.error("Fehler", e);
        GUI.getStatusBar().setErrorText("Fehler bei der Datums Auswahl");
        return;
      }
    }

    try
    {
      for (Lastschrift l : lastschriften)
      {
        l.setVersand(versendet);
        l.setVersanddatum(datum);
        l.store();
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
  }
}
