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
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchung.
 */
public class SplitBuchungDeleteAction implements Action
{
  private BuchungsControl control;

  public SplitBuchungDeleteAction(BuchungsControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Buchung))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    try
    {
      Buchung bu = (Buchung) context;
      if (((Buchung) context).isNewObject())
      {
        SplitbuchungsContainer.get().remove(bu);
      }
      else
      {
        BuchungDeleteAction action = new BuchungDeleteAction(true);
        action.handleAction(context);
      }
      control.refreshSplitbuchungen();
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Löschen der Buchung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
