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
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0497 extends AbstractDDLUpdate
{
  public Update0497(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Column col = new Column("zahler", COLTYPE.BIGINT, 0, null, false, false);
    execute(addColumn("rechnung", col));
    Index idx = new Index("ixRechnungZahler", false);

    idx.add(col);
    execute(idx.getCreateIndex("rechnung"));

    execute(createForeignKey("fkRechnungZahler", "rechnung", "zahler",
        "mitglied", "id", "RESTRICT", "NO ACTION"));

    execute("UPDATE rechnung set zahler = mitglied");
  }
}
