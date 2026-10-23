package de.jost_net.JVerein.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.rmi.JVereinDBService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class MigratH2Database
{
  public static void start() throws ApplicationException
  {
    try
    {
      final JVereinDBServiceImpl serviceOld = new JVereinDBServiceImpl(
          DBSupportH2Impl.class.getName());
      serviceOld.start();

      final JVereinDBServiceImpl serviceNew = new JVereinDBServiceImpl(
          DBSupportH2v2Impl.class.getName());
      serviceNew.start();

      // Macht das Update der DB, erstellt alle Tabellen etc.
      // TODO (Warum auch immer diese Funktion...)
      Logger.info("Erstelle neu Datenbank");
      serviceNew.checkConsistency();

      Logger.info("Migriere Datenbank");
      Class<?> c = Class.forName("de.willuhn.jameica.system.MigrateDB");
      Method method = c.getMethod("migrate", Connection.class, Connection.class,
          Runnable.class, Runnable.class);

      method.invoke(null, serviceOld.getConnection(),
          serviceNew.getConnection(), (Runnable) () -> {
            if (serviceOld != null)
            {
              try
              {
                serviceOld.stop(true);
              }
              catch (Exception e)
              {
                Logger.error("error while closing old db service", e);
              }
            }
            if (serviceNew != null)
            {
              try
              {
                serviceNew.stop(true);
              }
              catch (Exception e)
              {
                Logger.error("error while closing new db service", e);
              }
            }
          }, (Runnable) () -> {
            Logger.info("Migration erfolgreich abgeschlossen");

            Logger.info("Neue Datenbankverbindung in Settings speichern");
            new Settings(JVereinDBService.class).setAttribute("database.driver",
                DBSupportH2v2Impl.class.getName());

            try
            {
              // ALte Datenbank beenden, neue starten
              Einstellungen.getDBService().stop(true);
              Einstellungen.getDBService()
                  .setDriver(DBSupportH2v2Impl.class.getName());
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
          });

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
  }
}
