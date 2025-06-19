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

public class Update0476 extends AbstractDDLUpdate
{
  public Update0476(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    // Beitragsgruppe
    Column colBeitragsgruppe = new Column("steuer", COLTYPE.BIGINT, 10, "null",
        false, false);
    execute(addColumn("beitragsgruppe", colBeitragsgruppe));

    Index idxBeitragsgruppe = new Index("ixBeitragsgruppeSteuer", false);
    idxBeitragsgruppe.add(colBeitragsgruppe);
    execute(idxBeitragsgruppe.getCreateIndex("beitragsgruppe"));

    execute(createForeignKey("fkBeitragsgruppeSteuer", "beitragsgruppe",
        "steuer", "steuer", "id", "RESTRICT", "NO ACTION"));

    // Zusatzbetrag
    Column colZusatzbetrag = new Column("steuer", COLTYPE.BIGINT, 10, "null",
        false, false);
    execute(addColumn("zusatzabbuchung", colZusatzbetrag));

    Index idxZusatzbetrag = new Index("ixZusatzbetragSteuer", false);
    idxZusatzbetrag.add(colZusatzbetrag);
    execute(idxZusatzbetrag.getCreateIndex("zusatzabbuchung"));

    execute(createForeignKey("fkZusatzbetragSteuer", "zusatzabbuchung",
        "steuer", "steuer", "id", "RESTRICT", "NO ACTION"));

    // Zusatzbetrag Vorlage
    Column colZusatzvorlage = new Column("steuer", COLTYPE.BIGINT, 10, "null",
        false, false);
    execute(addColumn("zusatzbetragvorlage", colZusatzvorlage));

    Index idxZusatzvorlage = new Index("ixZusatzbetragvorlageSteuer", false);
    idxZusatzvorlage.add(colZusatzvorlage);
    execute(idxZusatzvorlage.getCreateIndex("zusatzbetragvorlage"));

    execute(
        createForeignKey("fkZusatzbetragvorlageSteuer", "zusatzbetragvorlage",
        "steuer", "steuer", "id", "RESTRICT", "NO ACTION"));
  }
}
