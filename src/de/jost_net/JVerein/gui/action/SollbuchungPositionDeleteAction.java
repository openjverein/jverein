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
import java.util.ArrayList;

import de.jost_net.JVerein.Messaging.SollbuchungMessage;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.rmi.SollbuchungPosition;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Löschen eine Sollbuchung Position
 */
public class SollbuchungPositionDeleteAction extends DeleteAction
{
  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof SollbuchungPosition))
    {
      return;
    }

    SollbuchungPosition position = (SollbuchungPosition) object;
    position.delete();
    // Betrag in Sollbuchung neu berechnen
    Double betrag = 0.0;
    Sollbuchung sollb = position.getSollbuchung();
    ArrayList<SollbuchungPosition> sollbpList = sollb
        .getSollbuchungPositionList();
    for (SollbuchungPosition sollp : sollbpList)
    {
      betrag += sollp.getBetrag();
    }
    sollb.setBetrag(betrag);
    sollb.store();
    Application.getMessagingFactory()
        .sendMessage(new SollbuchungMessage(sollb));
  }
}
