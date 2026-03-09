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

import de.jost_net.JVerein.Messaging.AbweichenderZahlerMessage;
import de.jost_net.JVerein.gui.control.AbweichenderZahlerNode;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AbweichenderZahlerEntfernenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof AbweichenderZahlerNode))
    {
      throw new ApplicationException("Kein Eintrag ausgewählt");
    }
    AbweichenderZahlerNode azn = (AbweichenderZahlerNode) context;
    if (azn.getType() != AbweichenderZahlerNode.ANGEHOERIGER)
    {
      throw new ApplicationException(
          "Kein Mitglied mit abweichendem Zahler ausgewählt");
    }
    Mitglied m = azn.getMitglied();
    try
    {
      m.setAbweichenderZahlerID(null);
      m.store();
      Application.getMessagingFactory()
          .sendMessage(new AbweichenderZahlerMessage(m));
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(
          "Interner Fehler beim Speichern des Mitglieds!");
    }
  }
}
