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

public class Update0511 extends AbstractDDLUpdate
{
  public Update0511(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    Table table = new Table("buchungsdokumentbuchung");

    Column id = new Column("id", COLTYPE.BIGINT, 4, null, false, true);
    table.add(id);
    table.setPrimaryKey(id);
    table.add(new Column("dokument", COLTYPE.BIGINT, 4, null, true, false));
    table.add(new Column("buchung", COLTYPE.BIGINT, 4, null, true, false));
    execute(createTable(table));

    execute("INSERT INTO buchungsdokumentbuchung (dokument,buchung) "
        + "SELECT id, referenz FROM buchungdokument WHERE referenz IS NOT NULL");

    execute(
        "CREATE UNIQUE INDEX dokumentbuchung ON buchungsdokumentbuchung (dokument,buchung);");

    execute(addColumn("buchungdokument",
        new Column("belegnummer", COLTYPE.VARCHAR, 50, null, false, false)));

    // Damit per messaging gespeicherte Dokumente weiterhing gefunden werden,
    // ist die referenz weiter nötig, neuerdings wird dafür die Belegnummer
    // verwendet.
    // Nur das erste Dokument pro Buchung wird mit einer Belegnummer versehen,
    // bei den anderen greift der Falback-Modus in
    // BuchungDokumentImpl.getNummer().

    // Temp-Tabelle mit jeweils erstem Dokument pro referenz
    execute("CREATE TEMPORARY TABLE temp_first (id BIGINT PRIMARY KEY);");
    execute("INSERT INTO temp_first (id) SELECT MIN(id) FROM buchungdokument"
        + " WHERE referenz IS NOT NULL GROUP BY referenz;");

    // Update nur für diese IDs: setze belegnummer = referenz (als String)
    execute("UPDATE buchungdokument bd JOIN temp_first tf ON bd.id = tf.id"
        + " SET bd.belegnummer = CONCAT('', bd.referenz);");

    // Temp-Tabelle entfernen
    execute("DROP TEMPORARY TABLE IF EXISTS temp_first;");

    execute(
        "CREATE UNIQUE INDEX belegnummer ON buchungdokument (belegnummer);");

    execute(createForeignKey("fkBuchung", "buchungsdokumentbuchung", "buchung",
        "buchung", "id", "CASCADE", "RESTRICT"));

    execute(createForeignKey("fkDokument", "buchungsdokumentbuchung",
        "dokument", "buchungdokument", "id", "CASCADE", "RESTRICT"));

    // Belegnummer soll erstmal von bisheriger Buchungsnummer wieterzählen,
    // solange nicht individuell in den Einstellungen angepasst wird
    execute("INSERT INTO einstellungneu (name, wert) "
        + "SELECT 'beleg_zaehler', COALESCE(MAX(referenz), 0) + 1 "
        + "FROM buchungdokument WHERE referenz IS NOT NULL;");
  }
}
