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
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class IstbuchungLoesenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!((context instanceof MitgliedskontoNode)
        || context instanceof Buchung))
    {
      throw new ApplicationException("Keine Istbuchung ausgewählt");
    }

    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle("Istbuchung von Sollbuchung lösen");
    d.setText("Wollen Sie die Istbuchung wirklich von der Sollbuchung lösen?");

    try
    {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return;
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      return;
    }
    MitgliedskontoNode mkn = null;
    Buchung bu = null;
    try
    {
      if (context instanceof MitgliedskontoNode)
      {
        mkn = (MitgliedskontoNode) context;
        bu = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
            mkn.getID());
      }
      else
      {
        bu = (Buchung) context;
      }
      bu.setSollbuchung(null);
      bu.store();
      GUI.getStatusBar().setSuccessText("Istbuchung von Sollbuchung gelöst.");
      Application.getMessagingFactory()
          .sendMessage(new MitgliedskontoMessage(bu));

    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler beim lösen der Istbuchung von der Sollbuchung");
    }
  }
}
