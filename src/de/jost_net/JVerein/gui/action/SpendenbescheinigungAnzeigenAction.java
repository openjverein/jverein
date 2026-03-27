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

import de.jost_net.JVerein.gui.view.SpendenbescheinigungDetailView;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class SpendenbescheinigungAnzeigenAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Spendenbescheinigung spb = null;
    try
    {
      if (context instanceof Buchung)
      {
        spb = ((Buchung) context).getSpendenbescheinigung();
      }
      else
      {
        throw new ApplicationException("Keine Buchung ausgewählt");
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      throw new ApplicationException(
          "Fehler bei der Anzeige einer Spendenbescheinigung", e);
    }
    if (spb != null)
    {
      GUI.startView(SpendenbescheinigungDetailView.class, spb);
    }
    else
    {
      throw new ApplicationException(
          "Die ausgewählte Buchung hat keine Spendenbescheinigung");
    }
  }
}
