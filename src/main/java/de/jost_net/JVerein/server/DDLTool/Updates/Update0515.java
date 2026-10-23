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
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0515 extends AbstractDDLUpdate
{
  public Update0515(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    execute("ALTER TABLE arbeitseinsatz MODIFY COLUMN stunden DECIMAL(15,2)");

    execute("ALTER TABLE altersstaffel MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE beitragsgruppe MODIFY COLUMN betrag DECIMAL(15,2)");
    execute(
        "ALTER TABLE beitragsgruppe MODIFY COLUMN betragmonatlich DECIMAL(15,2)");
    execute(
        "ALTER TABLE beitragsgruppe MODIFY COLUMN betragvierteljaehrlich DECIMAL(15,2)");
    execute(
        "ALTER TABLE beitragsgruppe MODIFY COLUMN betraghalbjaehrlich  DECIMAL(15,2)");
    execute(
        "ALTER TABLE beitragsgruppe MODIFY COLUMN betragjaehrlich DECIMAL(15,2)");
    execute(
        "ALTER TABLE beitragsgruppe MODIFY COLUMN arbeitseinsatzstunden  DECIMAL(15,2)");
    execute(
        "ALTER TABLE beitragsgruppe MODIFY COLUMN arbeitseinsatzbetrag  DECIMAL(15,2)");

    execute(
        "ALTER TABLE mitglied MODIFY COLUMN individuellerbeitrag DECIMAL(15,2)");

    execute("ALTER TABLE zusatzbetrag MODIFY COLUMN betrag DECIMAL(15,2)");

    execute(
        "ALTER TABLE zusatzbetragvorlage MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE anfangsbestand MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE buchung MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE konto MODIFY COLUMN betrag DECIMAL(15,2)");
    execute("ALTER TABLE konto MODIFY COLUMN afastart DECIMAL(15,2)");
    execute("ALTER TABLE konto MODIFY COLUMN afadauer DECIMAL(15,2)");
    execute("ALTER TABLE konto MODIFY COLUMN afarestwert DECIMAL(15,2)");

    execute("ALTER TABLE rechnung MODIFY COLUMN betrag DECIMAL(15,2)");
    execute(
        "ALTER TABLE rechnung MODIFY COLUMN erstattungsbetrag DECIMAL(15,2)");

    execute("ALTER TABLE kursteilnehmer MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE lastschrift MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE sollbuchung MODIFY COLUMN betrag DECIMAL(15,2)");

    execute(
        "ALTER TABLE sollbuchungposition MODIFY COLUMN betrag DECIMAL(15,2)");
    execute(
        "ALTER TABLE sollbuchungposition MODIFY COLUMN steuersatz DECIMAL(15,2)");

    execute("ALTER TABLE steuer MODIFY COLUMN satz DECIMAL(15,2)");

    execute(
        "ALTER TABLE spendenbescheinigung MODIFY COLUMN betrag DECIMAL(15,2)");

    execute(
        "ALTER TABLE jahresabschluss MODIFY COLUMN verwendungsrueckstand DECIMAL(15,2)");
    execute(
        "ALTER TABLE jahresabschluss MODIFY COLUMN zwanghafteweitergabe DECIMAL(15,2)");

    execute("ALTER TABLE wirtschaftsplanitem MODIFY COLUMN soll DECIMAL(15,2)");

    execute("ALTER TABLE buchungsart MODIFY COLUMN steuersatz DECIMAL(15,2)");

    execute("ALTER TABLE qifimportpos MODIFY COLUMN betrag DECIMAL(15,2)");

    execute("ALTER TABLE qifimporthead MODIFY COLUMN startsaldo DECIMAL(15,2)");
  }
}
