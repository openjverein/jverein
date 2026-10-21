package de.jost_net.JVerein.keys;

public enum Auswertung implements KeyEnum
{
  MINDERLEISTUNG(1, "Minderleistung"),
  PASSENDELEISTUNG(2, "Passende Leistung"),
  MEHRLEISTUNG(3, "Mehrleistung"),
  ALLE(4, "Alle");

  private int key;

  private String text;

  private Auswertung(int key, String text)
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
