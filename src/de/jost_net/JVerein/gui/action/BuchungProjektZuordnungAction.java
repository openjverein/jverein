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

import de.jost_net.JVerein.gui.dialogs.ProjektAuswahlDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Projekt;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Projekt zuordnen.
 */
public class BuchungProjektZuordnungAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Buchung)
        && !(context instanceof Buchung[]))
    {
      throw new ApplicationException("Keine Buchung(en) ausgewählt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      if (context instanceof Buchung[])
      {
        b = (Buchung[]) context;
      }
      if (b != null && b.length > 0 && b[0].isNewObject())
      {
        return;
      }

      ProjektAuswahlDialog pad = new ProjektAuswahlDialog(
          AbstractDialog.POSITION_CENTER, b);
      Projekt open = pad.open();

      if (!pad.getAbort())
      {
        int counter = 0;
        if (open == null)
        {
          for (Buchung buchung : b)
          {
            buchung.setProjekt(null);
            buchung.store();
          }
        }
        else
        {
          for (Buchung buchung : b)
          {
            boolean protect = buchung.getProjekt() != null
                && !pad.getOverride();
            if (protect)
            {
              counter++;
            }
            else
            {
              buchung.setProjekt(open);
              buchung.store();
            }
          }
        }
        String protecttext = "";
        if (open == null)
        {
          GUI.getStatusBar().setSuccessText("Projekte gelöscht");
        }
        else
        {
          if (counter > 0)
          {
            protecttext = String
                .format(", %d Projekte wurden nicht überschrieben. ", counter);
          }
          GUI.getStatusBar()
              .setSuccessText("Projekte zugeordnet" + protecttext);
        }
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar().setErrorText(
          "Fehler bei der Zuordnung des Projektes");
    }
  }
}
