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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.control.Savable;
import de.jost_net.JVerein.rmi.JVereinDBObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public abstract class AbstractDetailView extends AbstractView
{

  /**
   * Diese Funktion muss implementiert werden und den Controller zurückliefern
   * 
   * @return Savable das Control
   */
  protected abstract Savable getControl();

  @Override
  public void unbind() throws OperationCanceledException
  {
    try
    {
      boolean error = false;
      try
      {
        getControl().prepareStore();
      }
      catch (RemoteException | ApplicationException e)
      {
        error = true;
        Logger.error("Fehler bei unbind prepareStore", e);
      }
      JVereinDBObject o = (JVereinDBObject) getCurrentObject();

      // Wenn beim prepareStore() eine Exception geworfen wird, ist es
      // warscheinlich, dass etwas ungültiges eingegeben wurde. Also wurde etwas
      // verändert und wir fragen auch nach.
      if (o.isChanged() || error)
      {
        YesNoDialog dialog = new YesNoDialog(AbstractDialog.POSITION_CENTER);
        dialog.setTitle("Nicht gespeichert");
        dialog.setText("Der Eintrag wurde nicht gespeichert,\n"
            + " soll die View wirklich verlassen werden?");
        if (!(Boolean) dialog.open())
        {
          throw new OperationCanceledException();
        }
      }
    }
    catch (OperationCanceledException oce)
    {
      throw new OperationCanceledException(oce);
    }
    catch (Exception e)
    {
      String fehler = "Feler beim testen auf Änderungen: ";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler + e.getMessage());
    }
  }
}
