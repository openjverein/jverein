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

import de.jost_net.JVerein.gui.dialogs.BuchungsuebernahmeDialog;
import de.jost_net.JVerein.io.Buchungsuebernahme;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsuebernahmeAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {

    try
    {
      BuchungsuebernahmeDialog d = new BuchungsuebernahmeDialog(
          BuchungsuebernahmeDialog.POSITION_CENTER);
      if (d.open())
      {
        new Buchungsuebernahme(true);
        GUI.getCurrentView().reload();
      }
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("Error while importing from Hibiscus", e);
      GUI.getStatusBar()
          .setErrorText("Fehler beim Importieren von Hibiscus Buchungen");
    }
  }

}
