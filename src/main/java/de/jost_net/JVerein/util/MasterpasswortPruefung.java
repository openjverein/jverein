package de.jost_net.JVerein.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.util.Arrays;

import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

public final class MasterpasswortPruefung
{
  private MasterpasswortPruefung()
  {
  }

  public static void abfragen() throws ApplicationException
  {
    try
    {
      String frage = "Bitte bestätige die Aktion mit dem Masterpasswort.";
      while (true)
      {
        // Bei Abbrechen die OperationCanceledException durchreichen.
        String eingegeben = Application.getCallback().askPassword(frage);

        // Null darf auf keinen Fall als erfolgreiche Prüfung gelten.
        if (eingegeben == null)
          throw new OperationCanceledException("Passwortabfrage abgebrochen");

        if (istMasterpasswortKorrekt(eingegeben))
          return;

        Application.getMessagingFactory()
            .sendMessage(new StatusBarMessage(
                "Masterpasswort ist falsch. Bitte erneut eingeben.",
                StatusBarMessage.TYPE_ERROR));
      }
    }
    catch (Exception e)
    {
      throw new ApplicationException(e.getMessage(), e);
    }
  }

  private static boolean istMasterpasswortKorrekt(String eingegeben)
      throws Exception
  {
    char[] passwort = eingegeben.toCharArray();

    try
    {
      KeyStore keystore = KeyStore.getInstance("JKS");

      // Fehler beim Öffnen der Datei werden nicht als falsches Passwort
      // verschluckt, sondern an den Aufrufer weitergegeben.
      try (InputStream in = Files.newInputStream(
          Application.getSSLFactory().getKeyStoreFile().toPath()))
      {
        try
        {
          keystore.load(in, passwort);
        }
        catch (IOException e)
        {
          // Ein falsches Passwort oder ein beschädigter Keystore kann beim
          // Laden als IOException gemeldet werden. In beiden Fällen wird
          // nichts ausgeführt; der Nutzer kann erneut versuchen oder abbrechen.
          return false;
        }
      }

      if (!keystore.containsAlias("jameica"))
        return false;

      try
      {
        return keystore.getKey("jameica", passwort) != null;
      }
      catch (UnrecoverableKeyException e)
      {
        return false;
      }
    }
    finally
    {
      Arrays.fill(passwort, '\0');
    }
  }
}
