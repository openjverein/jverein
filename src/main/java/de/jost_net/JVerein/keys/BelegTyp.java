package de.jost_net.JVerein.keys;

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
