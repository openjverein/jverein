package de.jost_net.JVerein.keys;

import java.rmi.RemoteException;
import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.willuhn.logging.Logger;

public enum MitgliedsArt implements KeyEnum
{
  MITGLIED(0, "Mitglied"),
  NICHT_MITGLIED(1, "Nicht-Mitglied"),
  KURSTEILNEHMER(2, "Kursteilnehmer");

  private int key;

  private String text;

  private MitgliedsArt(int key, String text)
  {
    this.key = key;
    this.text = text;
  }

  @Override
  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  @Override
  public String toString()
  {
    return text;
  }

  /**
   * Gibt die Liste der Mitgleidsarten zurück, gefiltert je nach Einstellungen.
   * 
   * @return
   * @throws RemoteException
   */
  public static MitgliedsArt[] getList()
  {
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.KURSTEILNEHMER))
      {
        return new MitgliedsArt[] { MITGLIED, NICHT_MITGLIED, KURSTEILNEHMER };
      }
      else
      {
        return new MitgliedsArt[] { MITGLIED, NICHT_MITGLIED };
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler beim holen der Mitgliedsarten", e);
      return null;
    }
  }
}
