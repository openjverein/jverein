package de.jost_net.JVerein.keys;

public enum Differenz implements KeyEnum
{

  FEHLBETRAG(2, "Fehlbetrag"),
  UEBERZAHLUNG(3, "Überzahlung");

  private final String titel;

  private int key;

  private Differenz(int key, String titel)
  {
    this.titel = titel;
    this.key = key;
  }

  @Override
  public String toString()
  {
    return titel;
  }

  public static Differenz fromString(final String text)
  {
    for (Differenz item : Differenz.values())
    {
      if (item.titel.equals(text))
        return item;
    }
    return null;
  }

  @Override
  public int getKey()
  {
    return key;
  }
}
