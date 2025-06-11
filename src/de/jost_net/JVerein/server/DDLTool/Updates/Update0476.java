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
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0476 extends AbstractDDLUpdate
{
  public Update0476(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("dateinamenvorlage");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);

    Column name = new Column("dateiname", COLTYPE.VARCHAR, 250, null, false,
        false);
    t.add(name);

    t.setPrimaryKey(pk);
    execute(this.createTable(t));

    execute(
        "INSERT into dateinamenvorlage VALUES (1, 'Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$spendenbescheinigung_zeile2');\n");
    execute(
        "INSERT into dateinamenvorlage VALUES (2, 'Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$mitglied_name-$mitglied_vorname');\n");
  }
}
