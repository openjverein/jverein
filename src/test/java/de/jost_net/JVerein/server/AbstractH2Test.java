package de.jost_net.JVerein.server;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;
import de.willuhn.util.Settings;

public abstract class AbstractH2Test
{
  protected static TestJVereinDBService db;

  private static MockedStatic<Application> application;

  private static MockedStatic<Einstellungen> einstellungen;

  private static MockedConstruction<Settings> settings;

  private static Path sqlFile;

  @BeforeAll
  static void initDatabase() throws Exception
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

  @AfterEach
  final void resetDatabase() throws Exception
  {
    Einstellungen.getDBService().executeUpdate("DROP ALL OBJECTS", null);
    Einstellungen.getDBService().executeUpdate(
        "RUNSCRIPT FROM '" + sqlFile.toAbsolutePath() + "'", null);
  }

  @AfterAll
  static void shutdownDatabase() throws Exception
  {
    if (einstellungen != null)
      einstellungen.close();

    if (db != null)
      db.stop(true);

    if (application != null)
      application.close();

    if (settings != null)
      settings.close();
  }

}
