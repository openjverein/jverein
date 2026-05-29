package de.jost_net.JVerein.keys;

public enum MitgliedStatus implements KeyEnum
{
  ANGEMELDET(0, "Angemeldet"),
  ABGEMELDET(1, "Abgemeldet");

  private int key;

  private String text;

  private MitgliedStatus(int key, String text)
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

}
