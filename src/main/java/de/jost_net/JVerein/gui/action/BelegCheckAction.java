package de.jost_net.JVerein.gui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.dialogs.BelegCheckDialog;
import de.jost_net.JVerein.rmi.Beleg;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BelegCheckAction implements Action
{

  public enum FehlerArt
  {
    DATEI_FEHLT,
    PREUEFSUMME_FEHLT,
    PRUEFSUMME_FALSCH
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    BackgroundTask t = new BackgroundTask()
    {
      private boolean interrupted;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          DBIterator<Beleg> it = Einstellungen.getDBService()
              .createList(Beleg.class);

          Map<Beleg, FehlerArt> fehlerMap = new HashMap<>();

          monitor.log("Starte Prüfung");
          int i = 0;
          while (it.hasNext())
          {
            monitor.setPercentComplete(i++ * 100 / it.size());

            if (isInterrupted())
            {
              throw new OperationCanceledException();
            }
            Beleg beleg = it.next();
            try
            {
              beleg.getFile();
            }
            catch (IOException | ApplicationException e)
            {
              fehlerMap.put(beleg, FehlerArt.DATEI_FEHLT);
              continue;
            }
            Boolean ergebnis = null;

            ergebnis = beleg.checkChecksum();

            if (ergebnis == null)
            {
              fehlerMap.put(beleg, FehlerArt.PREUEFSUMME_FEHLT);
            }
            else if (!ergebnis)
            {
              fehlerMap.put(beleg, FehlerArt.PRUEFSUMME_FALSCH);
            }
          }
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);
          monitor.setStatusText("Prüfung abgeschlossen");
          if (fehlerMap.isEmpty())
          {
            SimpleDialog dialog = new SimpleDialog(
                SimpleDialog.POSITION_CENTER);
            dialog.setText("Die Dateien aller " + it.size()
                + " Belege sind vorhanden und haben korrekte Prüfsummen.");
            dialog.setTitle("Belege geprüft");
            dialog.open();
          }
          else
          {
            new BelegCheckDialog(fehlerMap).open();
          }
        }
        catch (OperationCanceledException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new ApplicationException(
              "Fehler beim prüfen der Prüfsummen: " + e.getMessage());
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
