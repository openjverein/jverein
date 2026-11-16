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

import java.sql.Connection;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0519 extends AbstractDDLUpdate
{
  public Update0519(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table table = new Table("belegkonto");

    Column id = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
    table.add(id);
    table.setPrimaryKey(id);
    Column belegColumn = new Column("beleg", COLTYPE.BIGINT, 4, null, true,
        false);
    table.add(belegColumn);
    Column referenzColumn = new Column("referenz", COLTYPE.BIGINT, 4, null,
        true, false);
    table.add(referenzColumn);
    execute(createTable(table));

    Index idx = new Index("ixBelegkontoKonto", false);
    idx.add(referenzColumn);
    execute(idx.getCreateIndex("belegkonto"));
    execute(createForeignKey("fkBelegkontoKonto", "belegkonto", "referenz",
        "konto", "id", "CASCADE", "RESTRICT"));

    idx = new Index("ixBelegkontoBeleg", false);
    idx.add(belegColumn);
    execute(idx.getCreateIndex("belegkonto"));
    execute(createForeignKey("fkBelegkontoBeleg", "belegkonto", "beleg",
        "beleg", "id", "CASCADE", "RESTRICT"));
  }
}
