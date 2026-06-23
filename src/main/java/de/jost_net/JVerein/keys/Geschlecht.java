package de.jost_net.JVerein.keys;

public enum Geschlecht implements KeyEnum
{
  OHNEANGABE(0, "o", "Ohne Angabe"),
  MAENNLICH(1, "m", "Männlich"),
  WEIBLICH(2, "w", "Weiblich");

  private int key;

  private String stringKey;

  private String text;

  private Geschlecht(int key, String stringKey, String text)
  {
    this.key = key;
    this.stringKey = stringKey;
    this.text = text;
  }

  public String getStringKey()
  {
    return stringKey;
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
}
