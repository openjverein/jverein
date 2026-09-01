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

    execute(
        "INSERT INTO buchungsdokumentbuchung (dokument,buchung) SELECT id,referenz FROM buchungdokument"
            + " WHERE referenz IS NOT NULL");

    execute(
        "CREATE UNIQUE INDEX dokumentbuchung ON buchungsdokumentbuchung (dokument,buchung);");

    execute(addColumn("buchungdokument",
        new Column("belegnummer", COLTYPE.VARCHAR, 50, null, false, false)));

    // Bestehende Dokumente haben die gleiche refernz (=buchung_id) für mehre
    // Dokumente, durch die Migration wird dort Belegnummer = rerferenz gesetzt.
    // Daher kann es mehrfach die gleiche "Belegnummer" für bestehende Dokumente
    // geben. Ein Unique Key ist daher ohne Migration der bestehdenden Dokumente
    // nicht möglich.
    // execute(
    // "CREATE UNIQUE INDEX belegnummer ON buchungdokument (belegnummer);");

    // Damit per messaging gespeicherte Dokumente weiterhing gefunden werden,
    // ist die referenz weiter nötig, neuerdings wird dafür die Belegnummer
    // verwendet
    execute("UPDATE buchungdokument SET belegnummer = referenz");

    // TODO Drop referenz
    // execute(dropColumn("buchungdokument", "referenz"));

    execute(createForeignKey("fkBuchung", "buchungsdokumentbuchung", "buchung",
        "buchung", "id", "CASCADE", "RESTRICT"));

    execute(createForeignKey("fkDokument", "buchungsdokumentbuchung",
        "dokument", "buchungdokument", "id", "CASCADE", "RESTRICT"));
  }
}
