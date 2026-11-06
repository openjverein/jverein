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
    // Das wird der Lehrgangsteilnehmer
    Column lehrgangsart = new Column("lehrgangsart", COLTYPE.BIGINT, 4, null,
        false, true);
    execute(alterColumnDropNotNull("lehrgang", lehrgangsart));
    execute(dropForeignKey("fkLehrgang2", "lehrgang"));
    execute(createForeignKey("fkLehrgang2", "lehrgang", "lehrgangsart",
        "lehrgangsart", "id", "SET NULL", "RESTRICT"));
    execute(renameTable("lehrgang", "lehrgangsteilnehmer"));

    // Der neue Lehrgang
    Table t = new Table("lehrgang");
    Column pk = new Column("id", COLTYPE.BIGINT, 19, null, true, true);
    t.add(pk);
    t.setPrimaryKey(pk);
    Column col = new Column("lehrgangsart", COLTYPE.BIGINT, 19, null, true,
        false);
    t.add(col);
    t.add(new Column("von", COLTYPE.DATE, 1, null, false, false));
    t.add(new Column("bis", COLTYPE.DATE, 1, null, false, false));
    t.add(new Column("veranstalter", COLTYPE.VARCHAR, 50, null, false, false));
    t.add(new Column("bezeichnung", COLTYPE.VARCHAR, 30, null, false, false));
    t.add(new Column("betrag", COLTYPE.DECIMAL, 15, null, false, false));
    execute(this.createTable(t));
    Index idx = new Index("ixLehrgangLehrgangsart", false);
    idx.add(col);
    execute(idx.getCreateIndex("lehrgang"));
    execute(createForeignKey("fkLehrgangLehrgangsart", "lehrgang",
        "lehrgangsart", "lehrgangsart", "id", "RESTRICT", "NO ACTION"));

    // Lehrgang bei Lehrgangsteilnehmer hinzufügen
    Column col2 = new Column("lehrgang", COLTYPE.BIGINT, 19, null, false,
        false);
    execute(addColumn("lehrgangsteilnehmer", col2));
    Index idx2 = new Index("ixLehrgangsteilnehmerLehrgang", false);
    idx2.add(col2);
    execute(idx2.getCreateIndex("lehrgangsteilnehmer"));
    execute(
        createForeignKey("fkLehrgangsteilnehmerLehrgang", "lehrgangsteilnehmer",
            "lehrgang", "lehrgang", "id", "RESTRICT", "NO ACTION"));

    // Unnötige Spalten bei Lehrgangsart löschen
    execute(dropColumn("lehrgangsart", "von"));
    execute(dropColumn("lehrgangsart", "bis"));
    execute(dropColumn("lehrgangsart", "veranstalter"));
  }
}
