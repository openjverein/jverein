package de.jost_net.JVerein.io.idea;

public class IDEAReference
{
  private final String fromColumn;

  private final Class<? extends IDEATable<?>> targetTable;

  private final String targetColumn;

  public IDEAReference(String fromColumn,
      Class<? extends IDEATable<?>> targetTable, String targetColumn)
  {
    this.fromColumn = fromColumn;
    this.targetTable = targetTable;
    this.targetColumn = targetColumn;
  }

  public String getFromColumn()
  {
    return this.fromColumn;
  }

  public Class<? extends IDEATable<?>> getTargetTable()
  {
    return this.targetTable;
  }

  public String getTargetColumn()
  {
    return this.targetColumn;
  }
}
