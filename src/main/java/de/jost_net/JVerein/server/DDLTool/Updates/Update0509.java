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

import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0509 extends AbstractDDLUpdate
{
  public Update0509(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("buchungdokument",
        new Column("pfad", COLTYPE.VARCHAR, 500, null, false, false)));

    execute(addColumn("mitglieddokument",
        new Column("pfad", COLTYPE.VARCHAR, 500, null, false, false)));

    alterColumnDropNotNull("buchungdokument",
        new Column("uuid", COLTYPE.VARCHAR, 50, null, false, false));

    alterColumnDropNotNull("mitglieddokument",
        new Column("uuid", COLTYPE.VARCHAR, 50, null, false, false));

    // Wenn bisher Dokumentenspeicherung aktiv war, dann soll auch weiterhin per
    // Messaging gespeichert werden
    execute("INSERT INTO einstellungneu (name,wert) SELECT '"
        + Property.DOKUMENTSPEICHERUNG_MESSAGING.getKey()
        + "', wert FROM einstellungneu WHERE name LIKE '"
        + Property.DOKUMENTENSPEICHERUNG.getKey() + "'");
  }
}
