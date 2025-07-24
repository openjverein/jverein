/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import java.sql.Connection;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.Index;
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0444 extends AbstractDDLUpdate
{
  public Update0444(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("altersstaffel");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);
    Column beitragsgruppe = new Column("beitragsgruppe", COLTYPE.BIGINT, 10,
        null, true, false);
    t.add(beitragsgruppe);
    Column nummer = new Column("nummer", COLTYPE.BIGINT, 10, null, true, false);
    t.add(nummer);
    Column betrag = new Column("betrag", COLTYPE.DOUBLE, 10, null, true, false);
    t.add(betrag);
    t.setPrimaryKey(pk);
    execute(this.createTable(t));

    execute(addColumn("beitragsgruppe",
        new Column("altersstaffel", COLTYPE.BOOLEAN, 0, "FALSE", true, false)));

    Index idx = new Index("ixAltersstaffel1", false);
    idx.add(beitragsgruppe);
    execute(idx.getCreateIndex("altersstaffel"));

    execute(this.createForeignKey("fk_altersstaffel", "altersstaffel",
        "beitragsgruppe", "beitragsgruppe", "id", "CASCADE", "NO ACTION"));

    execute(addColumn("einstellung", new Column("beitragaltersstufen",
        COLTYPE.VARCHAR, 200, "'0-6,7-12,13-18,19-99'", false, false)));
  }
}
