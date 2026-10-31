package de.jost_net.JVerein.keys;

public enum MitgliedsArt implements KeyEnum
{
  MITGLIED(0, "Mitglied"),
  NICHT_MITGLIED(1, "Nicht-Mitglied");

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

}
