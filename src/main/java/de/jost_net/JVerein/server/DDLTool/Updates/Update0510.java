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

public class Update0510 extends AbstractDDLUpdate
{
  public Update0510(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    execute(addColumn("rechnung",
        new Column("nummer", COLTYPE.VARCHAR, 500, null, false, false)));

    // Nummer in Rechnung eintragen, dabei so viele führende Nullen wie
    // eingestellt verwenden.
    // concat ist nötig, damit es bei H2 funktioniert
    execute("UPDATE rechnung SET nummer = case when CHAR_LENGTH(id)>"
        + "(SELECT COALESCE(max(wert),5) FROM einstellungneu WHERE name = '"
        + Property.ZAEHLERLAENGE.getKey()
        + "') then concat(id,'') else lpad(id, (SELECT COALESCE(max(wert),5) FROM einstellungneu WHERE name = '"
        + Property.ZAEHLERLAENGE.getKey() + "'),0) end");

    execute("INSERT INTO einstellungneu (name,wert) SELECT '"
        + Property.RECHNUNG_ZAHLER.getKey() + "', max(id)+1 FROM rechnung");

    execute("ALTER TABLE rechnung ADD UNIQUE INDEX `nummer` (`nummer`)");

    // Fehler aus Update0509 nochmal korrigieren, falls jemand die
    // NigthlyVersion verwendet hat und es somit noch nicht in der Korrekten
    // Form ausgeführt wurde
    alterColumnDropNotNull("buchungdokument",
        new Column("uuid", COLTYPE.VARCHAR, 50, null, false, false));

    alterColumnDropNotNull("mitglieddokument",
        new Column("uuid", COLTYPE.VARCHAR, 50, null, false, false));
  }
}
