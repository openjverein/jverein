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

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0505 extends AbstractDDLUpdate
{
  public Update0505(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("rechnung",
        new Column("rechnungstext", COLTYPE.VARCHAR, 500, null, false, false)));

    execute(addColumn("rechnung", new Column("erstattungsbetrag",
        COLTYPE.DOUBLE, 17, null, false, false)));

    Column col = new Column("refrechnung", COLTYPE.BIGINT, 0, null, false,
        false);
    execute(addColumn("rechnung", col));
    Index idx = new Index("ixRechnungRefrechnung", false);
    idx.add(col);
    execute(idx.getCreateIndex("rechnung"));
    execute(createForeignKey("fkRechnungRefrechnung", "rechnung", "refrechnung",
        "rechnung", "id", "SET NULL", "NO ACTION"));
  }
}
