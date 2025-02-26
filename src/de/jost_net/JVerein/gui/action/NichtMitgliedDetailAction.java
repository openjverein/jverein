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

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.PersonenartDialog;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public class NichtMitgliedDetailAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitglied m = null;
    try
    {
      if (context != null && (context instanceof Mitglied))
      {
        m = (Mitglied) context;
      }
      else if (context != null && (context instanceof Sollbuchung))
      {
        Sollbuchung sollb = (Sollbuchung) context;
        m = sollb.getMitglied();
      }
      else
      {
        m = (Mitglied) Einstellungen.getDBService().createObject(
            Mitglied.class, null);
        if (Einstellungen.getEinstellung().getJuristischePersonen())
        {
          PersonenartDialog pad = new PersonenartDialog(
              PersonenartDialog.POSITION_CENTER);
          String pa = pad.open();
          m.setPersonenart(pa);
          if (pa == null)
          {
            return;
          }
        }
        else
        {
          m.setPersonenart("n");
        }
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      throw new ApplicationException(
          "Fehler bei der Erzeugung eines neuen Nicht-Mitglied", e);
    }
    GUI.startView(new NichtMitgliedDetailView(), m);
  }
}
