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

import de.jost_net.JVerein.gui.dialogs.YesNoCancelDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchung.
 */
public class BuchungDeleteAction extends DeleteAction
{
  private boolean spendenbescheinigung = false;

  @Override
  protected String getText(JVereinDBObject object[])
      throws RemoteException, ApplicationException
  {
    if (object == null || object.length == 0 || !(object[0] instanceof Buchung))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }

    // Check ob einer der Buchungen
    // eine Spendenbescheinigung zugeordnet ist
    for (JVereinDBObject o : object)
    {
      Buchung bu = (Buchung) o;
      if (bu.getSpendenbescheinigung() != null)
      {
        spendenbescheinigung = true;
        break;
      }
    }

    if (!spendenbescheinigung)
    {
      return "Wollen Sie " + object.length + " Buchung"
          + (object.length > 1 ? "en" : "") + " wirklich löschen?";
    }
    else
    {
      if (object.length == 1)
      {
        return "Die Buchung gehört zu einer Spendenbescheinigung.\n"
            + "Sie können nur zusammen gelöscht werden.\n" + "Beide löschen?";
      }
      else
      {
        return "Mindestens eine Buchung gehört zu einer Spendenbescheinigung.\n"
            + "Sie können nur zusammen gelöscht werden.\n"
            + "Auch Buchungen mit Spendenbescheinigungen löschen?";
      }
    }
  }

  @Override
  protected void doDelete(JVereinDBObject object, Integer selection)
      throws RemoteException, ApplicationException
  {
    if (!(object instanceof Buchung))
    {
      return;
    }

    Buchung bu = (Buchung) object;
    Spendenbescheinigung spb = bu.getSpendenbescheinigung();

    if (bu.getSplitId() != null)
    {
      throw new ApplicationException(
          "Splitbuchungen können nicht gelöscht werden, sie müssen erst aufgelöst werden!");
    }
    if (spb != null && selection == YesNoCancelDialog.NO)
    {
      throw new ApplicationException(
          "Übersprungen, da ihr eine Spendenbescheinigung zugeordnet ist.");
    }
    if (bu.getSpendenbescheinigung() != null)
    {
      bu.getSpendenbescheinigung().delete();
    }
    bu.delete();
  }

  @Override
  protected boolean getMitNo(JVereinDBObject object[])
  {
    return spendenbescheinigung && object.length > 1;
  }
}
