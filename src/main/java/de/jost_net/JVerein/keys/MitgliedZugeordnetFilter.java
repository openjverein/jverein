package de.jost_net.JVerein.keys;

public enum MitgliedZugeordnetFilter implements KeyEnum
{
  BEIDE(1, "Beide"),
  JA(2, "Ja"),
  NEIN(3, "Nein");

  private int key;

  private String text;

  private MitgliedZugeordnetFilter(int key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public String getText()
  {
    return text;
  }

  @Override
  public int getKey()
  {
    return key;
  }

  @Override
  public String toString()
  {
    return getText();
  }

  public static MitgliedZugeordnetFilter getByKey(int key)
  {
    for (MitgliedZugeordnetFilter split : MitgliedZugeordnetFilter.values())
    {
      if (split.getKey() == key)
      {
        return split;
      }
    }
    return null;
  }
}
