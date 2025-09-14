/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.server.EinstellungImpl;
import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class Update0479 extends AbstractDDLUpdate
{
  protected DBService service;

  public Update0479(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("einstellungneu");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);
    t.setPrimaryKey(pk);

    Column name = new Column("name", COLTYPE.VARCHAR, 50, null, true, false);
    t.add(name);

    Column wert = new Column("wert", COLTYPE.MEDIUMTEXT, 1000, null, false,
        false);
    t.add(wert);

    Index idx = new Index("ixEinstellungneu", true);
    idx.add(name);
    t.add(idx);
    execute(this.createTable(t));

    // Bestehende Einstellungen in die neue Tabelle migrieren
    try
    {
      ResultSet result = conn.createStatement()
          .executeQuery("SELECT * FROM einstellung WHERE id = 1");

      if (!result.next())
        return;

      ResultSetMetaData m = result.getMetaData();

      for (int i = 1; i <= m.getColumnCount(); i++)
      {
        String value = null;
        String col = m.getColumnName(i).toLowerCase();
        if (col == "id" || result.getObject(i) == null)
        {
          continue;
        }
        switch (m.getColumnType(i))
        {
          case Types.BIT:
          case Types.BOOLEAN:
            value = result.getBoolean(i) ? "1" : "0";
            break;
          case Types.DATE:
            value = new JVDateFormatTTMMJJJJ().format(result.getDate(i));
            break;
          case Types.CHAR:
          case Types.VARCHAR:
            value = result.getString(i).replace("\n", "\\n").replace("\\",
                "\\\\");
            break;
          case Types.INTEGER:
          case Types.TINYINT:
          case Types.BIGINT:
          case Types.DOUBLE:
            value = result.getObject(i).toString();
            break;
          case Types.BLOB:
          case Types.LONGVARBINARY:
            value = Base64.encode(result.getBytes(i));
            break;
          default:
            String fehler = "Kann Einstellung nicht lesen, Type nicht implementiert: "
                + m.getColumnType(i);
            Logger.error(fehler);
            throw new ApplicationException(fehler);
        }
        execute("INSERT INTO einstellungneu (name,wert) VALUES('" + col + "','"
            + value + "')");
      }
    }
    catch (SQLException e)
    {
      String fehler = "Fehler beim Update";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }

    // Settings an neuen Ort umziehen
    Settings settingsAlt = new Settings(EinstellungImpl.class);
    Settings settings = new Settings(Einstellungen.class);
    for (String s : settingsAlt.getAttributes())
    {
      settings.setAttribute(s, settingsAlt.getString(s, null));
    }
  }
}
