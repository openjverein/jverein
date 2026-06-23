package de.jost_net.JVerein.keys;

public enum MailAuswahl implements KeyEnum
{
  MIT(1, "Nur mit Mailadresse"),
  OHNE(2, "Nur ohne Mailadresse");

  private final String text;

  private final int key;

  private MailAuswahl(int key, String text)
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
    return getText();
  }
}
