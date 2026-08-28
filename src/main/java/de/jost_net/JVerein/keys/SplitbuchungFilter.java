package de.jost_net.JVerein.keys;

public enum SplitbuchungFilter implements KeyEnum
{
  SPLIT(1, "Nur Splitbuchungen"),
  HAUPT(2, "Nur Hauptbuchungen");

  private int key;

  private String text;

  private SplitbuchungFilter(int key, String text)
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

  public static SplitbuchungFilter getByKey(int key)
  {
    for (SplitbuchungFilter split : SplitbuchungFilter.values())
    {
      if (split.getKey() == key)
      {
        return split;
      }
    }
    return null;
  }
}
