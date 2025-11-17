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

public class Update0494 extends AbstractDDLUpdate
{
  public Update0494(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("mitglied",
        new Column("kontoinhaber", COLTYPE.VARCHAR, 70, null, false, false)));

    Column altZahler = new Column("altzahler", COLTYPE.BIGINT, 4, null, false,
        false);
    execute(addColumn("mitglied", altZahler));

    Index idx = new Index("ix_mitglied_altzahler", false);
    idx.add(altZahler);
    execute(idx.getCreateIndex("mitglied"));

    execute(createForeignKey("fk_mitglied_altzahler", "mitglied", "altzahler",
        "mitglied", "id", "RESTRICT", "CASCADE"));

    execute("UPDATE mitglied SET altzahler = zahlerid WHERE zahlungsweg = 4");

    execute("UPDATE mitglied SET zahlungsweg = 2 WHERE zahlungsweg = 4");

    execute(
        "UPDATE mitglied SET kontoinhaber = SUBSTR(CONCAT(ktoiname,' ',ktoivorname),1,70) WHERE ktoiname <> '' OR ktoivorname <> ''");

    execute(addColumn("zusatzabbuchung", new Column("mitgliedzahltselbst",
        COLTYPE.BOOLEAN, 1, "0", true, false)));

    execute(addColumn("zusatzbetragvorlage", new Column("mitgliedzahltselbst",
        COLTYPE.BOOLEAN, 1, "0", true, false)));
  }
}
