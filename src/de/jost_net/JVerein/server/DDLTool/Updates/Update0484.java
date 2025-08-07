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

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

@SuppressWarnings("unused")
public class Update0484 extends AbstractDDLUpdate
{
    public Update0484(String driver, ProgressMonitor monitor, Connection conn)
    {
        super(driver, monitor, conn);
    }

    @Override
    public void run() throws ApplicationException
    {
        execute(addColumn("einstellung",
                new Column("wirtschaftsplanung", COLTYPE.BOOLEAN, 0, null, false,
                        false)));

        Table wirtschaftsplan = new Table("wirtschaftsplan");
        Column id = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
        wirtschaftsplan.add(id);
        wirtschaftsplan.setPrimaryKey(id);
        wirtschaftsplan.add(
                new Column("bezeichnung", COLTYPE.VARCHAR, 200, null, false, false));
        wirtschaftsplan.add(
                new Column("datum_von", COLTYPE.DATE, 10, null, true, false));
        wirtschaftsplan.add(
                new Column("datum_bis", COLTYPE.DATE, 10, null, true, false));

        execute(createTable(wirtschaftsplan));

        Table wirtschaftsplanItem = new Table("wirtschaftsplanitem");
        Column itemId = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
        wirtschaftsplanItem.add(itemId);
        wirtschaftsplanItem.setPrimaryKey(itemId);
        Column wirtschaftsplanCol = new Column("wirtschaftsplan", COLTYPE.INTEGER, 4,
                null, true, false);
        wirtschaftsplanItem.add(wirtschaftsplanCol);
        Column buchungsart = new Column("buchungsart", COLTYPE.INTEGER, 4, null,
                true, false);
        wirtschaftsplanItem.add(buchungsart);
        Column buchungsklasse = new Column("buchungsklasse", COLTYPE.INTEGER, 4,
                null, true, false);
        wirtschaftsplanItem.add(buchungsklasse);
        wirtschaftsplanItem.add(
                new Column("posten", COLTYPE.VARCHAR, 200, null, true, false));
        wirtschaftsplanItem.add(
                new Column("soll", COLTYPE.DOUBLE, 10, null, true, false));

        execute(createTable(wirtschaftsplanItem));

        Index idx = new Index("ix_wirtschaftsplanitem", false);
        idx.add(wirtschaftsplanCol);
        execute(idx.getCreateIndex("wirtschaftsplanitem"));

        execute(createForeignKey("fK_wirtschaftsplanitem", "wirtschaftsplanitem",
                "wirtschaftsplan", "wirtschaftsplan", "id", "CASCADE", "CASCADE"));

        Index idx1 = new Index("ix_wirtschaftsplanitem1", false);
        idx1.add(buchungsart);
        execute(idx1.getCreateIndex("wirtschaftsplanitem"));

        execute(createForeignKey("fk_wirtschaftsplanitem1", "wirtschaftsplanitem",
                "buchungsart", "buchungsart", "id", "RESTRICT", "CASCADE"));

        Index idx2 = new Index("ix_wirtschaftsplanitem2", false);
        idx2.add(buchungsklasse);
        execute(idx2.getCreateIndex("wirtschaftsplanitem"));

        execute(createForeignKey("fk_wirtschaftsplanitem2", "wirtschaftsplanitem",
                "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "CASCADE"));
    }
}
