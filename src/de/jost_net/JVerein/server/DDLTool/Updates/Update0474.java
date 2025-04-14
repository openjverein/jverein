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

public class Update0474 extends AbstractDDLUpdate
{
  public Update0474(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("steuer");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);
    t.setPrimaryKey(pk);

    t.add(new Column("name", COLTYPE.VARCHAR, 50, null, true, false));
    t.add(new Column("satz", COLTYPE.DOUBLE, 10, null, true, false));

    Column buchungsart = new Column("buchungsart", COLTYPE.BIGINT, 10, null,
        true, false);
    t.add(new Column("aktiv", COLTYPE.BOOLEAN, 1, "1", true, false));
    t.add(buchungsart);

    execute(createTable(t));

    // Indexes und ForeignKeys
    Index idx = new Index("ixSteuerBuchungsart", false);
    idx.add(buchungsart);
    execute(idx.getCreateIndex("steuer"));

    execute(this.createForeignKey("fk_steuerBuchungsart", "steuer",
        "buchungsart", "buchungsart", "id", "RESTRICT", "RESTRICT"));

    // Spalte steuer in buchungsart
    Column steuer = new Column("steuer", COLTYPE.BIGINT, 0, null, false, false);
    execute(addColumn("buchungsart", steuer));

    // Index und ForeignKey in buchungsart
    idx = new Index("ixBuchungsartSteuer", false);
    idx.add(steuer);
    execute(idx.getCreateIndex("buchungsart"));

    execute(this.createForeignKey("fkBuchungsartSteuer", "buchungsart",
        "steuer", "steuer", "id", "RESTRICT", "RESTRICT"));

    // Spalte steuer in buchung
    steuer = new Column("steuer", COLTYPE.BIGINT, 0, null, false, false);
    execute(addColumn("buchung", steuer));

    // Index und ForeignKey in buchung
    idx = new Index("ixBuchungSteuer", false);
    idx.add(steuer);
    execute(idx.getCreateIndex("buchung"));

    execute(this.createForeignKey("fkBuchungSteuer", "buchung", "steuer",
        "steuer", "id", "RESTRICT", "RESTRICT"));

    // Spalte steuer in sollbuchungposition
    steuer = new Column("steuer", COLTYPE.BIGINT, 0, null, false, false);
    execute(addColumn("sollbuchungposition", steuer));

    // Index und ForeignKey in sollbuchungposition
    idx = new Index("ixSollbuchungpositionSteuer", false);
    idx.add(steuer);
    execute(idx.getCreateIndex("sollbuchungposition"));

    execute(this.createForeignKey("fkSollbuchungpositionSteuer",
        "sollbuchungposition", "steuer", "steuer", "id", "RESTRICT",
        "RESTRICT"));

    // Spalte in einstellung
    steuer = new Column("steuerinbuchung", COLTYPE.BOOLEAN, 1, "0", true,
        false);
    execute(addColumn("einstellung", steuer));
  }
}
