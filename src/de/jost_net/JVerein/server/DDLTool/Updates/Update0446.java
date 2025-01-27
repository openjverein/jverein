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

public class Update0446 extends AbstractDDLUpdate
{
  public Update0446(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("einstellung", new Column("bkinbuchung",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    
    execute(addColumn("buchung", new Column("buchungsklasse",
        COLTYPE.BIGINT, 0, null, false, false)));
    Index idx = new Index("ixBuchung7", false);
    Column col = new Column("buchungsklasse", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("buchung"));
    execute(createForeignKey("fkBuchung7", "buchung",
        "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "NO ACTION"));
    
    execute(addColumn("mitgliedskonto", new Column("buchungsklasse",
        COLTYPE.BIGINT, 0, null, false, false)));
    idx = new Index("ixMitgliedkonto4", false);
    col = new Column("buchungsklasse", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("mitgliedskonto"));
    execute(createForeignKey("fkMitgliedkonto4", "mitgliedskonto",
        "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "NO ACTION"));
    
    execute(addColumn("zusatzabbuchung", new Column("buchungsklasse",
        COLTYPE.BIGINT, 0, null, false, false)));
    idx = new Index("ixZusatzabbuchung3", false);
    col = new Column("buchungsklasse", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("zusatzabbuchung"));
    execute(createForeignKey("fkZusatzabbuchung3", "zusatzabbuchung",
        "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "NO ACTION"));
    
    execute(addColumn("zusatzbetragvorlage", new Column("buchungsklasse",
        COLTYPE.BIGINT, 0, null, false, false)));
    idx = new Index("ixZusatzbetragvorlage3", false);
    col = new Column("buchungsklasse", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("zusatzbetragvorlage"));
    execute(createForeignKey("fkZusatzbetragvorlage3", "zusatzbetragvorlage",
        "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "NO ACTION"));
    
    execute(addColumn("beitragsgruppe", new Column("buchungsklasse",
        COLTYPE.BIGINT, 0, null, false, false)));
    idx = new Index("ixBeitragsgruppe2", false);
    col = new Column("buchungsklasse", COLTYPE.BIGINT, 0, null, false,
        false);
    idx.add(col);
    execute(idx.getCreateIndex("beitragsgruppe"));
    execute(createForeignKey("fkBeitragsgruppe2", "beitragsgruppe",
        "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "NO ACTION"));
    
    execute(addColumn("buchungsart", new Column("status",
        COLTYPE.INTEGER, 0, "2", false, false)));
  }
}
