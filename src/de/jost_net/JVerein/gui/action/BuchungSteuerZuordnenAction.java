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

import de.jost_net.JVerein.gui.dialogs.SteuerZuordnungDialog;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Steuer;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

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
      if (b == null || b.length == 0)
      {
        return;
      }
      final Buchung[] buchungen = b;

      SteuerZuordnungDialog dialog = new SteuerZuordnungDialog(
          SteuerZuordnungDialog.POSITION_MOUSE);
      Steuer steuer;
      try
      {
        steuer = dialog.open();
      }
      catch (Exception e)
      {
        String fehler = "Fehler beim öffnen des SteuerZuordnen Dialogs";
        Logger.error(fehler, e);
        throw new ApplicationException(fehler);
      }
      if (!dialog.getAbort())
      {
        BackgroundTask t = new BackgroundTask()
        {
          private boolean interrupted = false;

          @Override
          public void run(ProgressMonitor monitor) throws ApplicationException
          {
            int count = 0;
            int skip = 0;
            if (steuer == null)
            {
              monitor.setStatusText(
                  "Entferne Steuer aus " + buchungen.length + " Buchungen.");
              for (Buchung buchung : buchungen)
              {
                if (isInterrupted())
                {
                  throw new OperationCanceledException();
                }
                try
                {
                  buchung.setSteuer(null);
                  buchung.store();
                  count++;
                }
                catch (RemoteException e)
                {
                  skip++;
                  String fehler = "Fehler beim entfernen der Steuer";
                  try
                  {
                    fehler += " aus Buchung Nr. " + buchung.getID();
                  }
                  catch (RemoteException ingore)
                  {
                  }
                  monitor.setStatusText(fehler);
                  Logger.error(fehler, e);
                }
                monitor.setPercentComplete(100);
              }
              monitor.setStatusText("Steuer aus " + count
                  + " Buchungen entfernt, " + skip + " übersprungen.");
            }
            else
            {
              monitor.setStatusText(
                  "Ordne Steuer " + buchungen.length + " Buchungen zu.");
              for (Buchung buchung : buchungen)
              {
                if (isInterrupted())
                {
                  throw new OperationCanceledException();
                }
                try
                {
                  if (buchung.getSteuer() != null && !dialog.getOverride())
                  {
                    skip++;
                  }
                  else
                  {
                    buchung.setSteuer(steuer);
                    buchung.store();
                    count++;
                  }
                }
                catch (ApplicationException e)
                {
                  skip++;
                  try
                  {
                    monitor.setStatusText("Buchung Nr. " + buchung.getID() + " "
                        + e.getLocalizedMessage());
                  }
                  catch (RemoteException ingore)
                  {
                  }
                }
                catch (RemoteException e)
                {
                  skip++;
                  String fehler = "Fehler beim entfernen der Steuer";
                  try
                  {
                    fehler += " aus Buchung Nr. " + buchung.getID();
                  }
                  catch (RemoteException ingore)
                  {
                  }
                  monitor.setStatusText(fehler);
                  Logger.error(fehler, e);
                }
                monitor.setPercentComplete(100);
              }
              monitor.setStatusText("Steuer " + count
                  + " Buchungen zugeordnet, " + skip + " übersprungen.");
            }
          }

          @Override
          public void interrupt()
          {
            interrupted = true;
          }

          @Override
          public boolean isInterrupted()
          {
            return interrupted;
          }
        };
        Application.getController().start(t);
      }
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
    }
  }
}
