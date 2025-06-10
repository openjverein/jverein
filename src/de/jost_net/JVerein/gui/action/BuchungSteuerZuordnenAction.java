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

import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.gui.dialogs.SteuerZuordnungDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Steuer zuordnen.
 */
public class BuchungSteuerZuordnenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Buchung) && !(context instanceof Buchung[])))
    {
      throw new ApplicationException("Keine Buchung(en) ausgew�hlt");
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
      if (b == null || b.length == 0 || b[0].isNewObject())
      {
        return;
      }

      SteuerZuordnungDialog dialog = new SteuerZuordnungDialog(
          SteuerZuordnungDialog.POSITION_MOUSE);
      Steuer steuer = dialog.open();
      if (!dialog.getAbort())
      {
        DBTransaction.starten();
        int counter = 0;
        if (steuer == null)
        {
          for (Buchung buchung : b)
          {
            buchung.setSteuer(null);
            buchung.store();
          }
        }
        else
        {
          for (Buchung buchung : b)
          {
            if (buchung.getSteuer() != null && !dialog.getOverride())
            {
              counter++;
            }
            else
            {
              buchung.setSteuer(steuer);
              buchung.store();
            }
          }
        }
        if (steuer == null)
        {
          GUI.getStatusBar().setSuccessText("Steuer gel�scht");
        }
        else
        {
          String protecttext = "";
          if (counter > 0)
          {
            protecttext = String
                .format(", %d Buchungen wurden nicht �berschrieben. ", counter);
          }
          GUI.getStatusBar()
              .setSuccessText("Steuer zugeordnet" + protecttext);
        }
        DBTransaction.commit();
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException e)
    {
      DBTransaction.rollback();
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
    catch (Exception e)
    {
      DBTransaction.rollback();
      Logger.error("Fehler", e);
      GUI.getStatusBar()
          .setErrorText("Fehler bei der Zuordnung der Steuer: "
              + e.getLocalizedMessage());
    }
  }
}
