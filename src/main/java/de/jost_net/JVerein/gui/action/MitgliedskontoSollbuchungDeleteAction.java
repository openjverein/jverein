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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Messaging.MitgliedskontoMessage;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

public class MitgliedskontoSollbuchungDeleteAction implements Action
{
  private Mitglied mitglied;

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    MitgliedskontoNode[] nodes = null;

    if (context instanceof MitgliedskontoNode)
    {
      nodes = new MitgliedskontoNode[] { (MitgliedskontoNode) context };
    }
    else if (context instanceof MitgliedskontoNode[])
    {
      nodes = (MitgliedskontoNode[]) context;
    }
    else
    {
      throw new ApplicationException("Kein Objekt ausgewählt.");
    }

    try
    {
      mitglied = nodes[0].getMitglied();
      Sollbuchung[] sollbuchungen = new Sollbuchung[nodes.length];
      for (int i = 0; i < nodes.length; i++)
      {
        if (nodes[i].getType() == MitgliedskontoNode.SOLL)
        {
          sollbuchungen[i] = (Sollbuchung) Einstellungen.getDBService()
              .createObject(Sollbuchung.class, nodes[i].getID());
        }
        else
        {
          throw new ApplicationException(
              "Ein anderer Eintrag als Sollbuchung ausgewählt.");
        }
      }
      new SollbuchungDeleteAction().handleAction(sollbuchungen);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Löschen einer Sollbuchung.");
    }
  }

  private class SollbuchungDeleteAction extends DeleteAction
  {
    @Override
    protected void doFinally()
    {
      Application.getMessagingFactory()
          .sendMessage(new MitgliedskontoMessage(mitglied));
    }
  }

}
