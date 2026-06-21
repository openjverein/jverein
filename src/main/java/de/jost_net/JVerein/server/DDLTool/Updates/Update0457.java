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
import de.jost_net.JVerein.server.DDLTool.Table;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0457 extends AbstractDDLUpdate
{
  public Update0457(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("sollbuchungposition");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);

    Column sollbuchung = new Column("sollbuchung", COLTYPE.BIGINT, 10, null,
        true, false);
    t.add(sollbuchung);

    Column betrag = new Column("betrag", COLTYPE.DOUBLE, 10, null, true, false);
    t.add(betrag);

    Column steuersatz = new Column("steuersatz", COLTYPE.DOUBLE, 10, null,
        false, false);
    t.add(steuersatz);

    Column buchungsart = new Column("buchungsart", COLTYPE.BIGINT, 10, null,
        false, false);
    t.add(buchungsart);

    Column buchungsklasse = new Column("buchungsklasse", COLTYPE.BIGINT, 10,
        null, false, false);
    t.add(buchungsklasse);

    Column datum = new Column("datum", COLTYPE.DATE, 10, null, true, false);
    t.add(datum);

    Column zweck = new Column("zweck", COLTYPE.VARCHAR, 500, null, false,
        false);
    t.add(zweck);

    t.setPrimaryKey(pk);
    execute(this.createTable(t));

    Index idx = new Index("ixSollbuchungposition1", false);
    idx.add(sollbuchung);
    execute(idx.getCreateIndex("sollbuchungposition"));

    execute(
        this.createForeignKey("fk_sollbuchungposition1", "sollbuchungposition",
            "sollbuchung", "mitgliedskonto", "id", "CASCADE", "NO ACTION"));

    idx = new Index("ixSollbuchungposition2", false);
    idx.add(buchungsart);
    execute(idx.getCreateIndex("sollbuchungposition"));

    execute(
        this.createForeignKey("fk_sollbuchungposition2", "sollbuchungposition",
            "buchungsart", "buchungsart", "id", "RESTRICT", "NO ACTION"));

    idx = new Index("ixSollbuchungposition3", false);
    idx.add(zweck);
    execute(idx.getCreateIndex("sollbuchungposition"));

    execute(
        this.createForeignKey("fk_sollbuchungposition3", "sollbuchungposition",
            "buchungsklasse", "buchungsklasse", "id", "RESTRICT", "NO ACTION"));

    // Für bestehende Sollbuchungen Sollbuchungpositionen erstellen
    // Vorerst bleiben in der Tabelle Mitgliedskonto die Spalten
    // buchungsart, buchungsklasse, steuersatz, nettobetrag, steuerbetrag
    // bestehen damit eine Abwärtskompatibilität besteht
    execute("INSERT INTO sollbuchungposition"
        + " (sollbuchung, betrag, steuersatz, buchungsart, buchungsklasse, datum, zweck)"
        + " SELECT id,betrag,steuersatz,buchungsart,buchungsklasse,datum,zweck1 FROM mitgliedskonto;");

    execute(addColumn("rechnung",
        new Column("zahlungsweg", COLTYPE.INTEGER, 1, "0", true, false)));
  }
}
