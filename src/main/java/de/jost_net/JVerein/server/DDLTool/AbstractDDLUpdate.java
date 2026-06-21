package de.jost_net.JVerein.server.DDLTool;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.jost_net.JVerein.server.DBSupportH2Impl;
import de.jost_net.JVerein.server.DBSupportMySqlImpl;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public abstract class AbstractDDLUpdate implements IDDLUpdate
{
  protected int nr;

  protected Connection conn;

  protected ProgressMonitor monitor;

  public static final String MYSQL = DBSupportMySqlImpl.class.getName();

  public static final String H2 = DBSupportH2Impl.class.getName();

  public enum DRIVER
  {
    H2,
    MYSQL
  }

  public enum COLTYPE
  {
    BIGINT,
    INTEGER,
    VARCHAR,
    CHAR,
    DATE,
    TIMESTAMP,
    BOOLEAN,
    DOUBLE,
    LONGBLOB,
    MEDIUMTEXT
  }

  private DRIVER drv;

  public AbstractDDLUpdate(String driver, ProgressMonitor monitor,
      Connection conn)
  {
    String name = getClass().getName();
    int pos = name.lastIndexOf(".") + 1;
    name = name.substring(pos);

    if (name.length() != 10)
    {
      throw new RuntimeException(
          "Ungültiger Name für eine Update-Klasse (Updatennnn)");
    }
    if (!name.startsWith("Update"))
    {
      throw new RuntimeException(
          "Ungültiger Name für eine Update-Klasse (Updatennnn)");
    }
    nr = Integer.parseInt(name.substring(6));
    this.conn = conn;
    if (driver.endsWith("DBSupportH2Impl"))
    {
      drv = DRIVER.H2;
    }
    if (driver.endsWith("DBSupportMySqlImpl"))
    {
      drv = DRIVER.MYSQL;
    }
    this.monitor = monitor;
  }

  protected DRIVER getDriver()
  {
    return drv;
  }

  public void execute(String statement) throws ApplicationException
  {
    if (statement == null)
    {
      throw new ApplicationException("Leeres Statement");
    }
    if (statement.isBlank())
    {
      Logger.debug("ignore empty Statement");
      return;
    }
    try (Statement stmt = conn.createStatement())
    {
      Logger.info(statement);
      stmt.executeUpdate(statement);
    }
    catch (Exception e)
    {
      Logger.error("unable to execute update", e);
      throw new ApplicationException("Fehler beim Ausführen des Updates " + nr,
          e);
    }
  }

  public String createTable(Table table)
  {
    StringBuffer sb = new StringBuffer();
    switch (drv)
    {
      case H2:
        sb.append("CREATE TABLE IF NOT EXISTS " + table.getName() + "(");
        for (Column c : table.getColumns())
        {
          if (c.isAutoincrement())
          {
            sb.append(c.getName() + " IDENTITY(1), ");
          }
          else
          {
            sb.append(c.getName() + " " + getType(c) + ", ");
          }
        }
        for (Index i : table.getIndices())
        {
          sb.append(i.getCreateString());
        }
        sb.append(
            "PRIMARY KEY (" + table.getPrimaryKey()[0].getName() + "));\n");
        break;
      case MYSQL:
        sb.append("CREATE TABLE IF NOT EXISTS " + table.getName() + "(");
        for (Column c : table.getColumns())
        {
          sb.append(c.getName() + " " + getType(c));
          if (c.isAutoincrement())
          {
            sb.append(" AUTO_INCREMENT ");
          }
          sb.append(",");
        }
        for (Index i : table.getIndices())
        {
          sb.append(i.getCreateString());
        }
        sb.append("PRIMARY KEY (" + table.getPrimaryKey()[0].getName()
            + ")) ENGINE=InnoDB;\n");
        break;
    }
    return sb.toString();
  }

  public String addColumn(String table, Column col) throws ApplicationException
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " ADD IF NOT EXISTS " + col.getName()
            + " " + getType(col) + ";\n";
      }
      case MYSQL:
      {
        // Prüfen, ob die Spalte bereits existeiert, ist bei MySQL leider nicht
        // mehr per SQL möglich
        try
        {
          ResultSet meta = conn.getMetaData().getColumns(conn.getCatalog(),
              null, table, col.getName());
          if (meta.next())
          {
            return "";
          }
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Fehler beim Abfragen der Metadaten",
              e);
        }
        return "ALTER TABLE " + table + " ADD " + col.getName() + " "
            + getType(col);
      }
    }
    return "";
  }

  public String alterColumn(String table, Column col)
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " ALTER COLUMN " + col.getName() + " "
            + getType(col) + ";\n";
      }
      case MYSQL:
      {
        return "ALTER TABLE " + table + " MODIFY COLUMN " + col.getName() + " "
            + getType(col) + ";\n";
      }
    }
    return "";
  }

  public String alterColumnSetNotNull(String table, Column col)
  {
    switch (drv)
    {
      case H2:
        return "ALTER TABLE " + table + " ALTER COLUMN " + col.getName()
            + " SET NOT NULL;\n";
      case MYSQL:
        return "ALTER TABLE " + table + " MODIFY COLUMN " + col.getName() + " "
            + getType(col) + ";\n";
    }
    return "";
  }

  public String alterColumnDropNotNull(String table, Column col)
  {
    switch (drv)
    {
      case H2:
        return "ALTER TABLE " + table + " ALTER COLUMN " + col.getName()
            + " DROP NOT NULL;\n";
      case MYSQL:
        return "ALTER TABLE " + table + " MODIFY COLUMN " + col.getName() + " "
            + getType(col) + ";\n";
    }
    return "";
  }

  public String renameTable(String tableold, String tablenew)
      throws ApplicationException
  {
    switch (drv)
    {
      case H2:
        return "ALTER TABLE IF EXISTS " + tableold + " RENAME TO " + tablenew
            + ";\n";
      case MYSQL:
      {
        // Prüfen, ob die Tabelle bereits existeiert, ist bei MySQL leider nicht
        // mehr per SQL möglich
        try
        {
          ResultSet meta = conn.getMetaData().getTables(conn.getCatalog(), null,
              tablenew, new String[] { "TABLE" });
          if (meta.next())
          {
            return "";
          }
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Fehler beim Abfragen der Metadaten",
              e);
        }
        return "ALTER TABLE " + tableold + " RENAME " + tablenew + ";\n";
      }
    }
    return "";
  }

  public String renameColumn(String table, String columnold, Column colnew)
      throws ApplicationException
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " ALTER COLUMN " + columnold
            + " RENAME TO " + colnew.getName() + ";\n";
      }
      case MYSQL:
      {
        // Prüfen, ob die Spalte bereits existeiert, ist bei MySQL leider nicht
        // mehr per SQL möglich
        try
        {
          ResultSet meta = conn.getMetaData().getColumns(conn.getCatalog(),
              null, table, colnew.getName());
          if (meta.next())
          {
            return "";
          }
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Fehler beim Abfragen der Metadaten",
              e);
        }
        return "ALTER TABLE " + table + " CHANGE " + columnold + " "
            + colnew.getName() + " " + getType(colnew) + ";\n";
      }
    }
    return "";
  }

  public String dropColumn(String table, String column)
      throws ApplicationException
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " DROP COLUMN IF EXISTS " + column
            + ";\n";
      }
      case MYSQL:
      {
        // Prüfen, ob die Spalte noch existeiert, ist bei MySQL leider nicht
        // mehr per SQL möglich
        try
        {
          ResultSet meta = conn.getMetaData().getColumns(conn.getCatalog(),
              null, table, column);
          if (!meta.next())
          {
            return "";
          }
        }
        catch (SQLException e)
        {
          throw new ApplicationException("Fehler beim Abfragen der Metadaten",
              e);
        }
        return "ALTER TABLE " + table + " DROP COLUMN " + column + ";\n";
      }
    }
    return "";
  }

  private String getType(Column col)
  {
    String ret = "";
    switch (col.getType())
    {
      case INTEGER:
        ret += "INTEGER";
        break;
      case BIGINT:
        ret += "BIGINT(" + col.getLen() + ")";
        break;
      case BOOLEAN:
        switch (drv)
        {
          case H2:
            ret += "BOOLEAN";
            break;
          case MYSQL:
            ret += "BIT(1)";
            break;
        }
        break;
      case CHAR:
        ret += "CHAR(" + col.getLen() + ")";
        break;
      case DATE:
        ret += "DATE";
        break;
      case TIMESTAMP:
        ret += "TIMESTAMP";
        break;
      case DOUBLE:
        ret += "DOUBLE";
        break;
      case VARCHAR:
        ret += "VARCHAR(" + col.getLen() + ")";
        break;
      case MEDIUMTEXT:
        ret += "MEDIUMTEXT";
        break;
      case LONGBLOB:
        ret += "LONGBLOB";
        break;
    }
    ret += " ";
    if (col.isNotnull())
    {
      ret += "NOT NULL ";
    }
    else if (drv == DRIVER.MYSQL && !col.isAutoincrement())
    {
      ret += "NULL ";
    }
    if (col.getDefaultvalue() != null)
    {
      ret += "DEFAULT " + col.getDefaultvalue();
    }
    return ret;
  }

  public String createForeignKey(String constraintname, String table,
      String column, String reftable, String refcolumn, String ondelete,
      String onupdate)
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " ADD CONSTRAINT " + constraintname
            + " FOREIGN KEY (" + column + ") REFERENCES " + reftable + "("
            + refcolumn + ") ON DELETE " + ondelete + " ON UPDATE " + onupdate
            + ";\n";
      }
      case MYSQL:
      {
        return "ALTER TABLE " + table + " ADD CONSTRAINT " + constraintname
            + " FOREIGN KEY (" + column + ") REFERENCES " + reftable + "("
            + refcolumn + ") ON DELETE " + ondelete + " ON UPDATE " + onupdate
            + ";\n";
      }
    }
    return "";

  }

  public void createForeignKeyIfNotExistsNocheck(String constraintname,
      String table, String column, String reftable, String refcolumn,
      String ondelete, String onupdate) throws ApplicationException
  {
    switch (drv)
    {
      case H2:
      {
        execute("ALTER TABLE " + table + " ADD CONSTRAINT IF NOT EXISTS "
            + constraintname + " FOREIGN KEY (" + column + ") REFERENCES "
            + reftable + "(" + refcolumn + ") ON DELETE " + ondelete
            + " ON UPDATE " + onupdate + " NOCHECK;\n");
      }
        break;
      case MYSQL:
      {
        String statement = "ALTER TABLE " + table + " ADD CONSTRAINT "
            + constraintname + " FOREIGN KEY (" + column + ") REFERENCES "
            + reftable + " (" + refcolumn + ") ON DELETE " + ondelete
            + " ON UPDATE " + onupdate + ";\n";
        try
        {
          Logger.debug(statement);
          ScriptExecutor.execute(new StringReader(statement), conn, null);
        }
        catch (Exception e)
        {
          // Wenn Foreign Key schon existiert ist es auch ok;
        }
      }
    }
  }

  public String dropTable(String table)
  {
    return "drop table IF EXISTS " + table + ";\n";
  }

  public String dropForeignKey(String constraintname, String table)
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " DROP CONSTRAINT " + constraintname
            + ";\n";
      }
      case MYSQL:
      {
        return "ALTER TABLE " + table + " DROP FOREIGN KEY " + constraintname
            + ";\n";
      }
    }
    return "";
  }

}
