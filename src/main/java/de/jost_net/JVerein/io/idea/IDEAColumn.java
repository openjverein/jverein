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
    type = IDEAType.ALPHANUMERIC;
    return this;
  }

  public IDEAColumn<T> date(String format)
  {
    type = IDEAType.DATE;
    this.format = format;
    return this;
  }

  public IDEAColumn<T> numeric(int accuracy)
  {
    type = IDEAType.NUMERIC;
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
    return name;
  }

  public IDEAType getType()
  {
    return type;
  }

  public String getFormat()
  {
    return format;
  }

  public Integer getLength()
  {
    return length;
  }

  public Integer getAccuracy()
  {
    return accuracy;
  }

  public boolean isPrimaryKey()
  {
    return primaryKey;
  }

  public String getValue(T object) throws Exception
  {
    if (valueProvider == null)
    {
      throw new IllegalStateException(
          "Kein Wert-Provider fuer IDEA-Spalte '" + name + "' definiert");
    }

    String value = valueProvider.getValue(object);

    return value == null ? "" : value;
  }

  public interface IDEAValueProvider<T>
  {
    String getValue(T object) throws Exception;
  }
}
