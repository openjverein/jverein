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
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.server.SplitBuchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class SplitbuchungBulkAufloesenAction implements Action
{
  private BuchungsControl control;

  public SplitbuchungBulkAufloesenAction(BuchungsControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Buchung) && !(context instanceof Buchung[]))
    {
      throw new ApplicationException("Keine Buchung ausgewählt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      else if (context instanceof Buchung[])
      {
        b = (Buchung[]) context;
      }
      if (b == null || b.length == 0 || b[0].isNewObject())
      {
        return;
      }

      Map<Long, Buchung> splitbuchungIds = new HashMap<>();
      for (Buchung bu : b)
      {
        // Wir überschreiben, da wir nur eine Buchung der Splitbuchung brauchen
        splitbuchungIds.put(bu.getSplitId(), bu);
      }
      int anzahl = splitbuchungIds.size();

      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Splitbuchung" + (anzahl > 1 ? "en" : "") + " auflösen");
      d.setText(String.format("Wollen Sie %d %s wirklich auflösen?", anzahl,
          (anzahl == 1 ? "Splitbuchung" : "Splitbuchungen")));

      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          return;
        }
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Auflösen der Splitbuchung", e);
        return;
      }

      BackgroundTask t = new BackgroundTask()
      {
        private boolean interrupted = false;

        @Override
        public void run(ProgressMonitor monitor) throws ApplicationException
        {
          int count = 0;
          int skip = 0;
          monitor.setStatus(ProgressMonitor.STATUS_RUNNING);
          monitor.setStatusText(
              "Löse " + splitbuchungIds.size() + " Splitbuchen auf.");
          for (Long id : splitbuchungIds.keySet())
          {
            if (isInterrupted())
            {
              throw new OperationCanceledException();
            }
            try
            {
              new SplitBuchung(splitbuchungIds.get(id)).aufloesen(false);
              count++;
              monitor.setStatusText("Splitbuchung " + id + " aufgelöst.");
            }
            catch (ApplicationException e2)
            {
              skip++;
              String fehler = "Fehler beim Auflösen der Splitbuchung " + id
                  + ": ";
              monitor.setStatusText(fehler + e2.getMessage());
            }
            catch (RemoteException e3)
            {
              skip++;
              String fehler = "Fehler beim Auflösen der Splitbuchung " + id
                  + ": " + "Es wird eventuell von anderen Objekten benutzt.";
              monitor.setStatusText(fehler);
              Logger.error(fehler, e3);
            }
            monitor.setPercentComplete(
                100 * (count + skip) / splitbuchungIds.size());
          }
          GUI.getDisplay().syncExec(() -> {
            try
            {
              control.refreshBuchungsList();
            }
            catch (ApplicationException e)
            {
              Logger.error("Fehler beim neuladen der Liste.", e);
            }
          });
          if (count > 0)
          {
            String text = String.format(
                "%d Splitbuchung" + (count != 1 ? "en" : "") + " aufgelöst.",
                count);
            monitor.setStatusText(text);
            GUI.getStatusBar().setSuccessText(text);
          }
          else
          {
            String text = "Keine Splitbuchung aufgelöst";
            monitor.setStatusText(text);
            GUI.getStatusBar().setSuccessText(text);
          }
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
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
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Auflösen einer Splitbuchung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
