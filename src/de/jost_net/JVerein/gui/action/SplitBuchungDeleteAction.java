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

import de.jost_net.JVerein.Messaging.SplitbuchungMessage;
import de.jost_net.JVerein.gui.dialogs.YesNoCancelDialog;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Splitbuchung Buchung.
 */
public class SplitBuchungDeleteAction extends BuchungDeleteAction
{
  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Buchung))
    {
      return;
    }

    Buchung bu = (Buchung) object;
    if (bu.isNewObject())
    {
      SplitbuchungsContainer.get().remove(bu);
    }
    else
    {
      Spendenbescheinigung spb = bu.getSpendenbescheinigung();
      if (spb != null && selection == YesNoCancelDialog.NO)
      {
        throw new ApplicationException(
            "Übersprungen, da ihr eine Spendenbescheinigung zugeordnet ist.");
      }
      bu.setDelete(true);
    }
    Application.getMessagingFactory().sendMessage(new SplitbuchungMessage(bu));
  }

  @Override
  protected boolean isNewAllowed()
  {
    return true;
  }
}
