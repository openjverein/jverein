package de.jost_net.JVerein.server;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.willuhn.jameica.messaging.MessagingFactory;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.Plugin;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.services.ClassService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ApplicationCallback;
import de.willuhn.jameica.system.Config;
import de.willuhn.logging.Logger;
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;
import de.willuhn.util.Settings;

/**
 * Basisklasse für alle H2-Integrations Tests. Die Test-DB wird einmalig aus den
 * Updatescripts erstellt. Vor jedem Test wird sie auf den Ausgangszustand
 * zurückgesetzt.
 */
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("JVerein-H2-DB")
public abstract class AbstractH2Test
{
  private static MockedStatic<Application> application;

  private static MockedStatic<Einstellungen> einstellungen;

  private static MockedConstruction<Settings> settings;

  private static Path sqlFile;

  private static TestJVereinDBService db;

  @BeforeAll
  static synchronized void init() throws Exception
  {
    System.setProperty(TestJVereinDBService.class.getName() + ".uppercase",
        "true");

    settings = Mockito.mockConstruction(Settings.class);

    File pluginXml = new File("plugin.xml").getAbsoluteFile();

    if (!pluginXml.isFile())
      throw new IllegalStateException(
          "plugin.xml nicht gefunden: " + pluginXml);

    Manifest manifest = new Manifest(pluginXml);

    application = Mockito.mockStatic(Application.class);

    PluginLoader pluginLoader = mock(PluginLoader.class);
    Plugin plugin = mock(Plugin.class);
    when(pluginLoader.getPlugin(JVereinPlugin.class)).thenReturn(plugin);
    when(plugin.getManifest()).thenReturn(manifest);
    application.when(Application::getPluginLoader).thenReturn(pluginLoader);

    MessagingFactory messagingFactory = mock(MessagingFactory.class);
    MessagingQueue messagingQueue = mock(MessagingQueue.class);
    when(messagingFactory.getMessagingQueue(anyString()))
        .thenReturn(messagingQueue);
    application.when(Application::getMessagingFactory)
        .thenReturn(messagingFactory);

    ApplicationCallback callback = mock(ApplicationCallback.class);
    ProgressMonitor monitor = mock(ProgressMonitor.class);
    when(callback.getStartupMonitor()).thenReturn(monitor);
    application.when(Application::getCallback).thenReturn(callback);

    MultipleClassLoader loader = new MultipleClassLoader();
    loader.addClassloader(AbstractH2Test.class.getClassLoader());
    application.when(Application::getClassLoader).thenReturn(loader);

    Config config = mock(Config.class);
    application.when(Application::getConfig).thenReturn(config);

    new ClassService().prepareClasses(manifest);

    if (db == null)
    {
      db = new TestJVereinDBService();
      db.start();
      db.checkConsistency();

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try
        {
          db.stop(true);
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Stoppen der H2-Test-DB", e);
        }
      }, "H2-Testdatenbank-Shutdown"));
    }
    einstellungen = Mockito.mockStatic(Einstellungen.class,
        Mockito.CALLS_REAL_METHODS);

    einstellungen.when(Einstellungen::getDBService).thenReturn(db);

    if (sqlFile == null)
    {
      sqlFile = Files.createTempFile("h2-test-db", ".sql");
      sqlFile.toFile().deleteOnExit();
      Einstellungen.getDBService()
          .execute("SCRIPT TO '"
              + sqlFile.toAbsolutePath().toString().replace("'", "''") + "'",
              null, rs -> null);
    }
  }

  @BeforeEach
  void cleanDatabase() throws RemoteException
  {
    db.executeUpdate("DROP ALL OBJECTS", null);
    db.executeUpdate(
        "RUNSCRIPT FROM '"
            + sqlFile.toAbsolutePath().toString().replace("'", "''") + "'",
        null);
  }

  @AfterAll
  static void cleanUp()
  {
    try
    {
      if (einstellungen != null)
      {
        einstellungen.close();
      }
    }
    finally
    {
      try
      {
        if (application != null)
        {
          application.close();
        }
      }
      finally
      {
        if (settings != null)
        {
          settings.close();
        }
      }
    }
  }
}
