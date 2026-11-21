package de.jost_net.JVerein.keys;

import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.willuhn.logging.Logger;

public enum BelegTyp implements KeyEnum
{

  RECHNUNG(1, "Rechnung"),
  MAHNUNG(2, "Mahnung"),
  SPENDENBESCHEINIGUNG(3, "Spendenbescheinigung");

  private final String titel;

  private int key;

  private BelegTyp(int key, String titel)
  {
    this.key = key;
    this.titel = titel;
  }

  public static BelegTyp getByKey(int key)
  {
    for (BelegTyp a : BelegTyp.values())
    {
      if (a.getKey() == key)
      {
        return a;
      }
    }
    return null;
  }

  public static BelegTyp[] getArray()
  {
    ArrayList<BelegTyp> ret = new ArrayList<>();
    try
    {
      if ((Boolean) Einstellungen.getEinstellung(Property.RECHNUNGENANZEIGEN))
      {
        ret.add(RECHNUNG);
        ret.add(MAHNUNG);
      }
      if ((Boolean) Einstellungen
          .getEinstellung(Property.SPENDENBESCHEINIGUNGENANZEIGEN))
      {
        ret.add(SPENDENBESCHEINIGUNG);
      }
    }
    catch (Exception e1)
    {
      Logger.error("Fehler beim aufbauen der Belegtypen", e1);
    }
    return ret.toArray(new BelegTyp[0]);
  }

  @Override
  public String toString()
  {
    return titel;
  }

  @Override
  public int getKey()
  {
    return key;
  }
}
