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

public class Update0516 extends AbstractDDLUpdate
{
  public Update0516(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    execute(dropForeignKey("FKBUCHUNGDOKUMENT1", "beleg"));

    Table table = new Table("belegmitglied");

    Column id = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
    table.add(id);
    table.setPrimaryKey(id);
    table.add(new Column("beleg", COLTYPE.BIGINT, 4, null, true, false));
    table.add(new Column("referenz", COLTYPE.BIGINT, 4, null, true, false));
    execute(createTable(table));

    execute(
        "INSERT INTO beleg (referenz,datum,bemerkung,uuid,pfad,hash,belegart) "
            + "SELECT referenz,datum,bemerkung,uuid,pfad,hash,3 FROM mitglieddokument "
            + "	WHERE mitglieddokument.referenz IS NOT NULL");

    execute("INSERT INTO belegmitglied (beleg,referenz) "
        + "SELECT id, referenz FROM beleg WHERE belegart=3 AND beleg.referenz IS NOT NULL");

    execute(
        "CREATE UNIQUE INDEX belegreferenzmitglied ON belegmitglied (beleg,referenz);");

    // Damit per messaging gespeicherte Belege weiterhing gefunden werden,
    // ist die referenz weiter nötig, neuerdings wird dafür die Belegnummer
    // verwendet.
    // Nur der erste Beleg pro Mitglied wird mit einer Belegnummer versehen,
    // bei den anderen greift der Falback-Modus in
    // BelegImpl.getNummer().

    // Temp-Tabelle mit jeweils erstem Beleg pro referenz
    execute("CREATE TEMPORARY TABLE temp_first (id BIGINT PRIMARY KEY);");
    execute("INSERT INTO temp_first (id) SELECT MIN(id) FROM beleg"
        + " WHERE referenz IS NOT NULL and belegart = 3 GROUP BY referenz;");

    // Update nur für diese IDs: setze belegnummer = referenz (als String)
    execute("UPDATE beleg SET belegnummer = CONCAT('M-', referenz) "
        + "WHERE id IN (SELECT id  FROM temp_first) and belegart = 3");

    // Temp-Tabelle entfernen
    execute("DROP TABLE IF EXISTS temp_first;");

    execute(createForeignKey("fkBelegMitgliedMitglied", "belegmitglied",
        "referenz", "mitglied", "id", "CASCADE", "RESTRICT"));

    execute(createForeignKey("fkBelegMitglied", "belegmitglied", "beleg",
        "beleg", "id", "CASCADE", "RESTRICT"));

    execute(dropTable("mitglieddokument"));

    // Belegnummer soll erstmal von bisheriger Mitgliedsnummer weiterzählen,
    // solange nicht individuell in den Einstellungen angepasst wird
    execute("INSERT INTO einstellungneu (name, wert) "
        + "SELECT 'beleg_zaehler_mitglied', COALESCE(MAX(referenz), 0) + 1 "
        + "FROM beleg WHERE referenz IS NOT NULL AND belegart = 3;");
  }
}
