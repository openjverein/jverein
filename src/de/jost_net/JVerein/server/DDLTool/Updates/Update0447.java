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

public class Update0447 extends AbstractDDLUpdate
{
  public Update0447(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(alterColumnDropNotNull("mail",
        new Column("versand", COLTYPE.TIMESTAMP, 0, "NULL", false, false)));

    execute(alterColumnDropNotNull("mailempfaenger",
        new Column("versand", COLTYPE.TIMESTAMP, 0, "NULL", false, false)));

    execute(dropForeignKey("fkAnfangsbestand1", "anfangsbestand"));

    execute(createForeignKey("fkAnfangsbestand1", "anfangsbestand", "konto",
        "konto", "id", "CASCADE", "RESTRICT"));

    execute(dropForeignKey("fkLehrgang2", "lehrgang"));

    execute(createForeignKey("fkLehrgang2", "lehrgang", "lehrgangsart",
        "lehrgangsart", "id", "RESTRICT", "RESTRICT"));

    Index idx = new Index("ixBuchung9", false);
    Column col = new Column("splitid", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("buchung"));
    
    execute(createForeignKey("fkBuchung9", "buchung", "splitid", "buchung",
        "id", "CASCADE", "RESTRICT"));

    idx = new Index("ixMitgliednextbgruppe3", false);
    col = new Column("mitglied", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("mitgliednextbgruppe"));
    
    execute(createForeignKey("fkMitgliednextbgruppe2", "mitgliednextbgruppe",
        "mitglied", "mitglied", "id", "CASCADE", "RESTRICT"));

    execute(alterColumn("buchungsart", new Column(
        "steuer_buchungsart", COLTYPE.BIGINT, 10, "NULL", false, false)));

    idx = new Index("ixBuchungsart3", false);
    col = new Column("steuer_buchungsart", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("buchungsart"));
    
    execute(createForeignKey("fkBuchungsart3", "buchungsart",
        "steuer_buchungsart", "buchungsart", "id", "RESTRICT", "RESTRICT"));
  }
}
