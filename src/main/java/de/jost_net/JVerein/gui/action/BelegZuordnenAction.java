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
import de.jost_net.JVerein.gui.dialogs.BelegAuswahlDialog;
import de.jost_net.JVerein.rmi.AbstractBelegDBObject;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BelegZuordnenAction implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if (!(context instanceof AbstractBelegDBObject)
          && !(context instanceof AbstractBelegDBObject[]))
      {
        throw new ApplicationException(
            "Keine Objekt ausgewählt welches Belege unterstützt.");
      }

      AbstractBelegDBObject[] object = null;
      if (context instanceof AbstractBelegDBObject)
      {
        object = new AbstractBelegDBObject[1];
        object[0] = (AbstractBelegDBObject) context;
      }
      if (context instanceof AbstractBelegDBObject[])
      {
        object = (AbstractBelegDBObject[]) context;
      }

      final AbstractBelegDBObject[] objects = object;

      BelegAuswahlDialog d = new BelegAuswahlDialog();
      Beleg[] belege = d.open();
      if (belege != null)
      {
        // Bei nur einem Beleg und einem Objekt ohne BackgroundTask
        if (objects.length == 1 && belege.length == 1)
        {
          ((AbstractBelegDBObject) objects[0]).addBeleg(belege[0]);
          GUI.getCurrentView().reload();
          GUI.getStatusBar().setSuccessText("Beleg zugeordnet");
        }
        else
        {
          BackgroundTask t = new BackgroundTask()
          {
            @Override
            public void run(ProgressMonitor monitor) throws ApplicationException
            {
              try
              {
                DBTransaction.starten();
                monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
                monitor.setStatusText("Starte Beleg(e) zugeordnen.");
                int anzahl = 0;
                for (Beleg b : belege)
                {
                  for (int i = 0; i < objects.length; i++)
                  {
                    try
                    {
                      ((AbstractBelegDBObject) objects[i]).addBeleg(b);
                      monitor.setStatusText("Beleg " + b.getBelegnummer()
                          + ": Bei " + objects[i].getObjektName() + " "
                          + objects[i].getID() + " zugeordnet.");
                      anzahl++;
                    }
                    catch (ApplicationException ex)
                    {
                      monitor.setStatusText("Beleg " + b.getBelegnummer() + ": "
                          + objects[i].getObjektName() + " "
                          + objects[i].getID() + ": " + ex.getMessage());
                    }
                  }
                }
                DBTransaction.commit();
                monitor.setPercentComplete(100);
                monitor.setStatus(ProgressMonitor.STATUS_DONE);
                monitor.setStatusText(anzahl + " Belege zugeordnet.");
              }
              catch (OperationCanceledException oce)
              {
                DBTransaction.rollback();
                throw oce;
              }
              catch (Exception oce)
              {
                DBTransaction.rollback();
                throw new ApplicationException(oce.getMessage());
              }

              GUI.getCurrentView().reload();
              GUI.getStatusBar().setSuccessText("Beleg(e) zugeordnet");
            }

            @Override
            public void interrupt()
            {
              //
            }

            @Override
            public boolean isInterrupted()
            {
              return false;
            }
          };
          Application.getController().start(t);
        }
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
      Logger.error("Fehler beim Öffnen des Dialogs", e);
      GUI.getStatusBar().setErrorText("Fehler beim Öffnen des Dialogs");
    }
  }

}
