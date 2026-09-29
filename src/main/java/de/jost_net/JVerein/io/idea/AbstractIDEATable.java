package de.jost_net.JVerein.io.idea;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class AbstractIDEATable<T> implements IDEATable<T>
{
  protected final String dateFormatString = "YYYYMMDD";

  protected final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "yyyyMMdd");

  protected final NumberFormat decimalFormat = NumberFormat
      .getInstance(Locale.GERMANY);

  private final String name;

  private final String fileName;

  private final List<IDEAColumn<T>> columns = new ArrayList<IDEAColumn<T>>();

  private final List<IDEAReference> references = new ArrayList<IDEAReference>();

  protected AbstractIDEATable(String name, String fileName)
  {
    this.name = name;
    this.fileName = fileName;
  }

  protected IDEAColumn<T> primaryKey(String name)
  {
    IDEAColumn<T> column = new IDEAColumn<T>(name);
    column.setPrimaryKey(true);
    this.columns.add(column);
    return column;
  }

  protected IDEAColumn<T> column(String name)
  {
    IDEAColumn<T> column = new IDEAColumn<T>(name);
    this.columns.add(column);
    return column;
  }

  protected void reference(String fromColumn,
      Class<? extends IDEATable<?>> targetTable, String targetColumn)
  {
    this.references
        .add(new IDEAReference(fromColumn, targetTable, targetColumn));
  }

  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public String getFileName()
  {
    return this.fileName;
  }

  @Override
  public List<IDEAColumn<T>> getColumns()
  {
    return Collections.unmodifiableList(this.columns);
  }

  @Override
  public List<IDEAReference> getReferences()
  {
    return Collections.unmodifiableList(this.references);
  }
}
