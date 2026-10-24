package de.jost_net.JVerein.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class MigratH2Database
{
  public static void start() throws ApplicationException
  {
    String source = DBSupportH2Impl.class.getName();
    String target = DBSupportH2v2Impl.class.getName();

    Application.getController().start(new BackgroundTask()
    {
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        JVereinDBServiceImpl sourceService = null;
        JVereinDBServiceImpl targetService = null;
        try
        {
          sourceService = new JVereinDBServiceImpl(source);
          sourceService.start();

          targetService = new JVereinDBServiceImpl(target);
          targetService.start();

          // Macht das Update der DB, erstellt alle Tabellen etc.
          // TODO (Warum auch immer diese Funktion...)
          Logger.info("Erstelle neu Datenbank");
          targetService.checkConsistency();

          Logger.info("Migriere Datenbank");
          Class<?> c = Class.forName("de.willuhn.jameica.system.MigrateDB");
          Method method = c.getMethod("migrate", Connection.class,
              Connection.class, ProgressMonitor.class);

          method.invoke(null, sourceService.getConnection(),
              targetService.getConnection(), monitor);
          Logger.info("Migration erfolgreich abgeschlossen");

          Logger.info("Neue Datenbankverbindung in Settings speichern");
          new Settings(JVereinDBService.class).setAttribute("database.driver",
              target);

          try
          {
            // ALte Datenbank beenden, neue starten
            Einstellungen.getDBService().stop(true);
            Einstellungen.getDBService().setDriver(target);
            Einstellungen.getDBService().start();

            Logger.info("Server mit nuer Datenbank neu gestarte");

            Application.getMessagingFactory()
                .sendMessage(new StatusBarMessage(
                    "Migration erfolgreich beendet, neue Datenbank gestartet.",
                    StatusBarMessage.TYPE_SUCCESS));
          }
          catch (RemoteException e)
          {
            Logger.error("Fehler beim Neustarts des Services", e);
          }

        }
        catch (ClassNotFoundException | NoSuchMethodException
            | IllegalAccessException | InvocationTargetException e)
        {
          throw new ApplicationException(
              "Die aktuelle Jameica Version unterstützt die Migration nicht");
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler bei der Migration", e);
          throw new ApplicationException(
              "Fehler bei der Migration: " + e.getMessage());
        }
        finally
        {
          if (sourceService != null)
          {
            try
            {
              sourceService.stop(true);
            }
            catch (Exception e)
            {
              Logger.error("error while closing old db service", e);
            }
          }
          if (targetService != null)
          {
            try
            {
              targetService.stop(true);
            }
            catch (Exception e)
            {
              Logger.error("error while closing new db service", e);
            }
          }
        }
      }

      @Override
      public void interrupt()
      {
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    });
  }
}
