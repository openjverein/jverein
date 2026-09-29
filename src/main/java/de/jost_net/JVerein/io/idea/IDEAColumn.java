package de.jost_net.JVerein.io.idea;

/**
 * Beschreibung einer Spalte eines IDEA-Exports.
 *
 * @param <T>
 *          Typ des Datensatzes.
 */
public class IDEAColumn<T>
{
  public enum IDEAType
  {
    ALPHANUMERIC,
    NUMERIC,
    DATE
  }

  private final String name;

  private IDEAType type = IDEAType.ALPHANUMERIC;

  private String format;

  private Integer length;

  private Integer accuracy;

  private boolean primaryKey;

  private IDEAValueProvider<T> valueProvider;

  public IDEAColumn(String name)
  {
    this.name = name;
  }

  public IDEAColumn<T> text()
  {
    this.type = IDEAType.ALPHANUMERIC;
    return this;
  }

  public IDEAColumn<T> date(String format)
  {
    this.type = IDEAType.DATE;
    this.format = format;
    return this;
  }

  public IDEAColumn<T> numeric(int accuracy)
  {
    this.type = IDEAType.NUMERIC;
    this.accuracy = accuracy;
    return this;
  }

  public IDEAColumn<T> length(int length)
  {
    this.length = length;
    return this;
  }

  public IDEAColumn<T> value(IDEAValueProvider<T> valueProvider)
  {
    this.valueProvider = valueProvider;
    return this;
  }

  void setPrimaryKey(boolean primaryKey)
  {
    this.primaryKey = primaryKey;
  }

  public String getName()
  {
    return this.name;
  }

  public IDEAType getType()
  {
    return this.type;
  }

  public String getFormat()
  {
    return this.format;
  }

  public Integer getLength()
  {
    return this.length;
  }

  public Integer getAccuracy()
  {
    return this.accuracy;
  }

  public boolean isPrimaryKey()
  {
    return this.primaryKey;
  }

  public String getValue(T object) throws Exception
  {
    if (this.valueProvider == null)
      throw new IllegalStateException(
          "Kein Wert-Provider fuer IDEA-Spalte '" + this.name + "' definiert");

    String value = this.valueProvider.getValue(object);

    return value == null ? "" : value;
  }

  public interface IDEAValueProvider<T>
  {
    String getValue(T object) throws Exception;
  }
}
