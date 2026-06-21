/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * <p>
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

public class Update0488 extends AbstractDDLUpdate
{
  public Update0488(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Column klasse = new Column("buchungsklasse", COLTYPE.BIGINT, 4, null, false,
        false);
    execute(addColumn("steuer", klasse));

    Index idx = new Index("ix_steuer_buchungsklasse", false);
    idx.add(klasse);
    execute(idx.getCreateIndex("steuer"));

    execute(createForeignKey("fk_steuer_buchungsklasse", "steuer",
        "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "CASCADE"));
  }
}
