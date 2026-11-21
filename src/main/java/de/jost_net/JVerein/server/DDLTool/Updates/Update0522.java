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

public class Update0522 extends AbstractDDLUpdate
{
  public Update0522(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    execute(alterColumn("beleg",
        new Column("bemerkung", COLTYPE.VARCHAR, 250, null, false, false)));

    // Belegtyp Rechnung, Mahnung, Spendenbescheinigung für interne Zwecke
    execute(addColumn("beleg",
        new Column("belegtyp", COLTYPE.INTEGER, 10, null, false, false)));

    Table table = new Table("belegrechnung");

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

    Index idx = new Index("ixBelegrechnungRechnung", false);
    idx.add(referenzColumn);
    execute(idx.getCreateIndex("belegrechnung"));
    execute(createForeignKey("fkBelegrechnungRechnung", "belegrechnung",
        "referenz", "rechnung", "id", "CASCADE", "RESTRICT"));

    idx = new Index("ixBelegrechnungBeleg", false);
    idx.add(belegColumn);
    execute(idx.getCreateIndex("belegrechnung"));
    execute(createForeignKey("fkBelegrechnungBeleg", "belegrechnung", "beleg",
        "beleg", "id", "CASCADE", "RESTRICT"));

    Table table2 = new Table("belegspendenbescheinigung");

    Column id2 = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
    table2.add(id2);
    table2.setPrimaryKey(id2);
    Column belegColumn2 = new Column("beleg", COLTYPE.BIGINT, 4, null, true,
        false);
    table2.add(belegColumn2);
    Column referenzColumn2 = new Column("referenz", COLTYPE.BIGINT, 4, null,
        true, false);
    table2.add(referenzColumn2);
    execute(createTable(table2));

    idx = new Index("ixBelegspendenbescheinigungSpendenbescheinigung", false);
    idx.add(referenzColumn);
    execute(idx.getCreateIndex("belegspendenbescheinigung"));
    execute(createForeignKey("fkBelegspendenbescheinigungSpendenbescheinigung",
        "belegspendenbescheinigung", "referenz", "spendenbescheinigung", "id",
        "CASCADE", "RESTRICT"));

    idx = new Index("ixBelegspendenbescheinigungBeleg", false);
    idx.add(belegColumn);
    execute(idx.getCreateIndex("belegspendenbescheinigung"));
    execute(createForeignKey("fkBelegspendenbescheinigungBeleg",
        "belegspendenbescheinigung", "beleg", "beleg", "id", "CASCADE",
        "RESTRICT"));
  }
}
