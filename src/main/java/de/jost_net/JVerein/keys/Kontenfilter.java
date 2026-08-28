package de.jost_net.JVerein.keys;

public enum Kontenfilter implements KeyEnum
{
  GELDKONTO(0, "Geldkonto"), // Beinhaltet Rückstellungen
  ANLAGEKONTO(1, "Anlagekonto"),
  ALLE(2, "Alle Konten");

  private int key;

  private String text;

  private Kontenfilter(int key, String text)
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
