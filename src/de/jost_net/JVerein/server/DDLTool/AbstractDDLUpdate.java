package de.jost_net.JVerein.server.DDLTool;

import java.io.StringReader;
import java.sql.Connection;
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
    H2, MYSQL
  }

  public enum COLTYPE
  {
    BIGINT, INTEGER, VARCHAR, CHAR, DATE, TIMESTAMP, BOOLEAN, DOUBLE, LONGBLOB, MEDIUMTEXT
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
    execute(statement, true);
  }

  public void execute(String statement, boolean setVersion)
      throws ApplicationException
  {
    if (statement == null)
    {
      throw new ApplicationException("Leeres Statement");
    }
    try
    {
      Logger.info(statement);
      ScriptExecutor.execute(new StringReader(statement), conn, null);
      if (setVersion)
      {
        setNewVersion(nr);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to execute update", e);
      throw new ApplicationException("Fehler beim Ausführen des Updates", e);
    }
  }

  public void setNewVersion(int newVersion) throws ApplicationException
  {
    try
    {
      String msg = "JVerein-DB-Update: " + newVersion;
      monitor.setStatusText(msg);
      Logger.info(msg);
      Statement stmt = conn.createStatement();
      int anzahl = stmt.executeUpdate(
          "UPDATE version SET version = " + newVersion + " WHERE id = 1");
      if (anzahl == 0)
      {
        stmt.executeUpdate(
            "INSERT INTO version VALUES (1, " + newVersion + ")");
      }
      stmt.close();
    }
    catch (SQLException e)
    {
      Logger.error("Versionsnummer kann nicht eingefügt werden.", e);
      throw new ApplicationException(
          "Versionsnummer kann nicht gespeichert werden.");
    }
  }

  public String createTable(Table table)
  {
    StringBuffer sb = new StringBuffer();
    switch (drv)
    {
      case H2:
        sb.append("CREATE TABLE " + table.getName() + "(");
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
        sb.append("CREATE TABLE " + table.getName() + "(");
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

  public String addColumn(String table, Column col)
  {
    String ret = "ALTER TABLE " + table + " ADD " + col.getName() + " ";
    ret += getType(col);
    ret += ";\n";
    return ret;
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

  public String renameColumn(String table, String columnold, Column colnew)
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
        return "ALTER TABLE " + table + " CHANGE " + columnold + " "
            + colnew.getName() + " " + getType(colnew) + ";\n";
      }
    }
    return "";
  }

  public String dropColumn(String table, String column)
  {
    return "ALTER TABLE " + table + " DROP COLUMN " + column + ";\n";
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
    else if(drv == DRIVER.MYSQL && !col.isAutoincrement())
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
  
  public void createForeignKeyIfNotExistsNocheck(String constraintname, String table,
      String column, String reftable, String refcolumn, String ondelete,
      String onupdate) throws ApplicationException
  {
    switch (drv)
    {
      case H2:
      {
        execute( "ALTER TABLE " + table + " ADD CONSTRAINT IF NOT EXISTS " + constraintname
            + " FOREIGN KEY (" + column + ") REFERENCES " + reftable + "("
            + refcolumn + ") ON DELETE " + ondelete + " ON UPDATE " + onupdate
            + " NOCHECK;\n", true);
      }
      break;
      case MYSQL:
      {
        String statement =  "ALTER TABLE " + table + " ADD CONSTRAINT " + constraintname
            + " FOREIGN KEY (" + column + ") REFERENCES " + reftable + " ("
            + refcolumn + ") ON DELETE " + ondelete + " ON UPDATE " + onupdate
            + ";\n";
        try
        {
          Logger.debug(statement);
          ScriptExecutor.execute(new StringReader(statement), conn, null);
        }
        catch (Exception e)
        {
          // Wenn Foreign Key schon existiert ist es auch ok;
        }
        setNewVersion(nr);
      }
    }
  }

  public String dropTable(String table)
  {
    return "drop table " + table + ";\n";
  }
  
  public String dropForeignKey(String constraintname, String table)
  {
    switch (drv)
    {
      case H2:
      {
        return "ALTER TABLE " + table + " DROP CONSTRAINT " + constraintname + ";\n";
      }
      case MYSQL:
      {
        return "ALTER TABLE " + table + " DROP FOREIGN KEY " + constraintname + ";\n";
      }
    }
    return "";
  }

}
