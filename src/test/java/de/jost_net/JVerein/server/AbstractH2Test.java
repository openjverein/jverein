package de.jost_net.JVerein.server;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
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
@ExtendWith(AbstractH2Test.H2DatabaseExtension.class)
@ResourceLock(value = "OpenJVerein-H2-Database", mode = ResourceAccessMode.READ_WRITE)
public abstract class AbstractH2Test
{
  public static class H2DatabaseExtension
      implements BeforeAllCallback, BeforeEachCallback
  {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
        .create(H2DatabaseExtension.class);

    private static final String KEY = "database";

    private DatabaseResource getResource(ExtensionContext context)
    {
      return context.getRoot().getStore(NAMESPACE).get(KEY,
          DatabaseResource.class);
    }

    @Override
    public void beforeAll(ExtensionContext context)
    {
      context.getRoot().getStore(NAMESPACE).computeIfAbsent(KEY,
          key -> new DatabaseResource());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception
    {
      getResource(context).reset();
    }
  }

  private static class DatabaseResource implements AutoCloseable
  {
    private MockedStatic<Application> application;

    private MockedStatic<Einstellungen> einstellungen;

    private MockedConstruction<Settings> settings;

    private TestJVereinDBService db;

    private Path sqlFile;

    DatabaseResource()
    {
      try
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

        db = new TestJVereinDBService();
        db.start();
        db.checkConsistency();

        einstellungen = Mockito.mockStatic(Einstellungen.class,
            Mockito.CALLS_REAL_METHODS);

        einstellungen.when(Einstellungen::getDBService).thenReturn(db);

        sqlFile = Files.createTempFile("h2-test-db", ".sql");
        sqlFile.toFile().deleteOnExit();
        Einstellungen.getDBService().execute(
            "SCRIPT TO '" + sqlFile.toAbsolutePath() + "'", null, rs -> null);
      }
      catch (Exception e)
      {
        closeResources();
        throw new RuntimeException(
            "H2-Testdatenbank konnte nicht initialisiert werden", e);
      }
    }

    void reset() throws RemoteException
    {
      db.executeUpdate("DROP ALL OBJECTS", null);
      db.executeUpdate("RUNSCRIPT FROM '" + sqlFile.toAbsolutePath() + "'",
          null);
    }

    @Override
    public void close() throws IOException
    {
      try
      {
        closeResources();
      }
      finally
      {
        Files.deleteIfExists(sqlFile);
      }
    }

    private void closeResources()
    {
      if (einstellungen != null)
      {
        einstellungen.close();
      }

      if (db != null)
      {
        try
        {
          db.stop(true);
        }
        catch (RemoteException e)
        {
          Logger.error("Fehler beim Stoppen der DB");
        }
      }

      if (application != null)
      {
        application.close();
      }

      if (settings != null)
      {
        settings.close();
      }
    }
  }
}
