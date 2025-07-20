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

public class Update0483 extends AbstractDDLUpdate
{
  public Update0483(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    Table t = new Table("vorlage");
    Column pk = new Column("id", COLTYPE.BIGINT, 10, null, true, true);
    t.add(pk);

    Column key = new Column("key", COLTYPE.INTEGER, 10, null, false, false);
    t.add(key);

    Column muster = new Column("muster", COLTYPE.VARCHAR, 250, null, false,
        false);
    t.add(muster);


    t.setPrimaryKey(pk);
    execute(this.createTable(t));

    execute(
        "INSERT into vorlage (key, muster) VALUES (1, 'Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$spendenbescheinigung_zeile2');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (2, 'Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$mitglied_name-$mitglied_vorname');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (3, 'Rechnung-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (4, 'Rechnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (5, 'Mahnung-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (6, 'Mahnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (7, 'Kontoauszug-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (8, 'Kontoauszug-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (9, '$formular_name-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (10, '$formular_name-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (11, '1ctueberweisung-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (12, 'Prenotification-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (key, muster) VALUES (13, 'Prenotification-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");

  }
}
