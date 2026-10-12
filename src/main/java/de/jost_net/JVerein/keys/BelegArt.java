package de.jost_net.JVerein.keys;

public enum BelegArt implements KeyEnum
{

  SONSTIGES(1, "Allgemein"),
  BUCHUNG(2, "Buchung");

  private final String titel;

  private int key;

  private BelegArt(int key, String titel)
  {
    this.key = key;
    this.titel = titel;
  }

  public static BelegArt getByKey(int key)
  {
    for (BelegArt a : BelegArt.values())
    {
      if (a.getKey() == key)
      {
        return a;
      }
    }
    return null;
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
