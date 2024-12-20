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
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0454 extends AbstractDDLUpdate
{
  public Update0454(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
<<<<<<< HEAD:src/de/jost_net/JVerein/server/DDLTool/Updates/Update0453.java
    execute(addColumn("konto", new Column("kontoart",
        COLTYPE.INTEGER, 0, null, false, false)));

<<<<<<< HEAD
    execute("update konto set kontoart = 1 where anlagenkonto IS NULL");
    execute("update konto set kontoart = 1 where anlagenkonto IS FALSE");
    execute("update konto set kontoart = 2 where anlagenkonto IS TRUE");
    
    execute(dropColumn("konto", "anlagenkonto"));
=======
    execute("INSERT INTO wirtschaftsplanung(geschaeftsjahr, buchungsart, betrag) VALUES (2024, 1, 200), (2023, 1, 1000)");
>>>>>>> 517b61e9 (Added WirtschaftsplanungListView)
=======
    execute(addColumn("einstellung",
        new Column("wirtschaftsplanung", COLTYPE.BOOLEAN, 0, null, false,
            false)));

    Table wirtschaftsplanung = new Table("wirtschaftsplanung");
    Column gj = new Column("geschaeftsjahr", COLTYPE.INTEGER, 4, null, true,
        false);
    wirtschaftsplanung.add(gj);
    Column buchungsart = new Column("buchungsart", COLTYPE.BIGINT, 4, null,
        true, false);
    wirtschaftsplanung.add(buchungsart);
    wirtschaftsplanung.setPrimaryKey(gj, buchungsart);
    wirtschaftsplanung.add(
        new Column("betrag", COLTYPE.DOUBLE, 10, "0.0", true, false));

    execute(createTable(wirtschaftsplanung));

    Index idx = new Index("ix_wirtschaftsplanung", false);
    idx.add(buchungsart);
    execute(idx.getCreateIndex("wirtschaftsplanung"));

    execute(createForeignKey("fk_wirtschaftsplanung", "wirtschaftsplanung",
        "buchungsart", "buchungsart", "id", "RESTRICT", "CASCADE"));
>>>>>>> 9995d22c (UebersichtPart):src/de/jost_net/JVerein/server/DDLTool/Updates/Update0454.java
  }
}
