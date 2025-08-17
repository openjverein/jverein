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
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0484 extends AbstractDDLUpdate
{
  public Update0484(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    // Verzicht von Spendenbescheinigung nach Buchung kopieren wenn es nicht
    // dort schon gesetzt ist
    execute("UPDATE buchung SET verzicht = "
        + "(SELECT ersatzaufwendungen FROM spendenbescheinigung WHERE spendenbescheinigung.id = buchung.spendenbescheinigung) "
        + "WHERE verzicht IS NOT TRUE");
    // Der Befehl vorher setzt bei allen Buchungen ohne Spendenbescheinigung
    // verzicht auf null
    execute("UPDATE buchung SET verzicht = FALSE WHERE verzicht IS NULL");

    // Attribute für Sachspenden
    Column unterlagenwertermittlung = new Column("unterlagenwertermittlung",
        COLTYPE.BOOLEAN, 0, null, false, false);
    execute(addColumn("buchung", unterlagenwertermittlung));
    execute("UPDATE buchung SET unterlagenwertermittlung = FALSE");
    Column herkunftspende = new Column("herkunftspende", COLTYPE.INTEGER, 0,
        null, false, false);
    execute(addColumn("buchung", herkunftspende));
    execute("UPDATE buchung SET herkunftspende = 3");
    Column bezeichnungsachzuwendung = new Column("bezeichnungsachzuwendung",
        COLTYPE.VARCHAR, 100, null, false, false);
    execute(addColumn("buchung", bezeichnungsachzuwendung));
    execute("UPDATE buchung SET bezeichnungsachzuwendung = ''");
  }
}
