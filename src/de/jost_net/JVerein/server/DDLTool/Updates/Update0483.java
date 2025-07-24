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

    Column name = new Column("name", COLTYPE.VARCHAR, 100, null, false, false);
    t.add(name);

    Column muster = new Column("muster", COLTYPE.VARCHAR, 1000, null, false,
        false);
    t.add(muster);

    t.setPrimaryKey(pk);
    execute(this.createTable(t));

    execute(
        "INSERT into vorlage (name, muster) VALUES ('spendenbescheinigung-dateiname', 'Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$spendenbescheinigung_zeile2');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('spendenbescheinigung-mitglied-dateiname', 'Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$mitglied_name-$mitglied_vorname');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('rechnung-dateiname', 'Rechnung-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('rechnung-mitglied-dateiname', 'Rechnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('mahnung-dateiname', 'Mahnung-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('mahnung-mitglied-dateiname', 'Mahnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('kontoauszug-dateiname', 'Kontoauszug-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('kontoauszug-mitglied-dateiname', 'Kontoauszug-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('freies-formular-dateiname', '$formular_name-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('freies-formular-mitglied-dateiname', '$formular_name-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('1ct-ausgabe-dateiname', '1ctueberweisung-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('pre-notification-dateiname', 'Prenotification-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('pre-notification-mitglied-dateiname', 'Prenotification-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");

  }
}
