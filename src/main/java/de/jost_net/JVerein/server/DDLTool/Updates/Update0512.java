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
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0512 extends AbstractDDLUpdate
{
  public Update0512(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    execute(renameTable("buchungdokument", "beleg"));

    Table table = new Table("belegbuchung");

    Column id = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
    table.add(id);
    table.setPrimaryKey(id);
    table.add(new Column("beleg", COLTYPE.BIGINT, 4, null, true, false));
    table.add(new Column("referenz", COLTYPE.BIGINT, 4, null, true, false));
    execute(createTable(table));

    execute("INSERT INTO belegbuchung (beleg,referenz) "
        + "SELECT id, referenz FROM beleg WHERE beleg.referenz IS NOT NULL");

    execute(
        "CREATE UNIQUE INDEX belegreferenz ON belegbuchung (beleg,referenz);");

    execute(addColumn("beleg",
        new Column("belegnummer", COLTYPE.VARCHAR, 50, null, false, false)));

    // Damit per messaging gespeicherte Belege weiterhing gefunden werden,
    // ist die referenz weiter nötig, neuerdings wird dafür die Belegnummer
    // verwendet.
    // Nur der erste Beleg pro Buchung wird mit einer Belegnummer versehen,
    // bei den anderen greift der Falback-Modus in
    // BelegImpl.getNummer().

    // Temp-Tabelle mit jeweils erstem Beleg pro referenz
    execute("CREATE TEMPORARY TABLE temp_first (id BIGINT PRIMARY KEY);");
    execute("INSERT INTO temp_first (id) SELECT MIN(id) FROM beleg"
        + " WHERE referenz IS NOT NULL GROUP BY referenz;");

    // Update nur für diese IDs: setze belegnummer = referenz (als String)
    execute("UPDATE beleg SET belegnummer = CONCAT('', referenz) "
        + "WHERE id IN (SELECT id  FROM temp_first)");

    // Temp-Tabelle entfernen
    execute("DROP TABLE IF EXISTS temp_first;");

    execute("CREATE UNIQUE INDEX belegnummer ON beleg (belegnummer);");

    execute(createForeignKey("fkBuchung", "belegbuchung", "referenz", "buchung",
        "id", "CASCADE", "RESTRICT"));

    execute(createForeignKey("fkBeleg", "belegbuchung", "beleg", "beleg", "id",
        "CASCADE", "RESTRICT"));

    execute(alterColumnDropNotNull("beleg",
        new Column("referenz", COLTYPE.BIGINT, 11, null, false, false)));

    // Belegnummer soll erstmal von bisheriger Buchungsnummer wieterzählen,
    // solange nicht individuell in den Einstellungen angepasst wird
    execute("INSERT INTO einstellungneu (name, wert) "
        + "SELECT 'beleg_zaehler', COALESCE(MAX(referenz), 0) + 1 "
        + "FROM beleg WHERE referenz IS NOT NULL;");
  }
}
