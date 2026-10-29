package de.jost_net.JVerein.keys;

public enum Ausfuehrung implements KeyEnum
{

  AKTIVE(1, "Aktive"),
  NOCH_NICHT(2, "Noch nicht ausgeführt");

  private int key;

  private String text;

  private Ausfuehrung(int key, String text)
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
