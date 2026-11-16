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

import de.jost_net.JVerein.gui.control.BuchungSplitDetailControl;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Splitbuchung Buchung.
 */
public class SplitBuchungDeleteAction extends DeleteAction
{
  private Buchung bu;

  private BuchungSplitDetailControl control;

  public SplitBuchungDeleteAction(BuchungSplitDetailControl control)
  {
    this.control = control;
  }

  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Buchung))
    {
      return;
    }

    bu = (Buchung) object;
    if (bu.getSplitTyp() == SplitbuchungTyp.HAUPT)
    {
      throw new ApplicationException(
          "Hauptbuchungen können nicht geloscht werden.");
    }
    if (bu.getSplitTyp() == SplitbuchungTyp.GEGEN)
    {
      throw new ApplicationException(
          "Gegenbuchungen können nicht geloscht werden.");
    }

    if (bu.isNewObject())
    {
      control.getSplitBuchung().remove(bu);
    }
    else
    {
      bu.checkDelete();
      bu.setDelete(true);
    }
  }

  @Override
  protected void doFinally() throws RemoteException
  {
    GUI.getDisplay().syncExec(() -> {
      try
      {
        control.refreshSplitbuchungen();
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler beim neuladen der Liste.", e);
      }
    });
  }

  @Override
  protected boolean isNewAllowed()
  {
    return true;
  }
}
