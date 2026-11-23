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
    execute(alterColumnType("arbeitseinsatz", "stunden", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("altersstaffel", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("beitragsgruppe", "betrag", COLTYPE.DECIMAL, 15));
    execute(alterColumnType("beitragsgruppe", "betragmonatlich",
        COLTYPE.DECIMAL, 15));
    execute(alterColumnType("beitragsgruppe", "betragvierteljaehrlich",
        COLTYPE.DECIMAL, 15));
    execute(alterColumnType("beitragsgruppe", "betraghalbjaehrlich",
        COLTYPE.DECIMAL, 15));
    execute(alterColumnType("beitragsgruppe", "betragjaehrlich",
        COLTYPE.DECIMAL, 15));
    execute(alterColumnType("beitragsgruppe", "arbeitseinsatzstunden",
        COLTYPE.DECIMAL, 15));
    execute(alterColumnType("beitragsgruppe", "arbeitseinsatzbetrag",
        COLTYPE.DECIMAL, 15));

    execute(alterColumnType("mitglied", "individuellerbeitrag", COLTYPE.DECIMAL,
        15));

    execute(alterColumnType("zusatzbetrag", "betrag", COLTYPE.DECIMAL, 15));

    execute(
        alterColumnType("zusatzbetragvorlage", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("anfangsbestand", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("buchung", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("konto", "betrag", COLTYPE.DECIMAL, 15));
    execute(alterColumnType("konto", "afastart", COLTYPE.DECIMAL, 15));
    execute(alterColumnType("konto", "afadauer", COLTYPE.DECIMAL, 15));
    execute(alterColumnType("konto", "afarestwert", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("rechnung", "betrag", COLTYPE.DECIMAL, 15));
    execute(
        alterColumnType("rechnung", "erstattungsbetrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("kursteilnehmer", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("lastschrift", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("sollbuchung", "betrag", COLTYPE.DECIMAL, 15));

    execute(
        alterColumnType("sollbuchungposition", "betrag", COLTYPE.DECIMAL, 15));
    execute(alterColumnType("sollbuchungposition", "steuersatz",
        COLTYPE.DECIMAL, 15));

    execute(alterColumnType("steuer", "satz", COLTYPE.DECIMAL, 15));

    execute(
        alterColumnType("spendenbescheinigung", "betrag", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("jahresabschluss", "verwendungsrueckstand",
        COLTYPE.DECIMAL, 15));
    execute(alterColumnType("jahresabschluss", "zwanghafteweitergabe",
        COLTYPE.DECIMAL, 15));

    execute(
        alterColumnType("wirtschaftsplanitem", "soll", COLTYPE.DECIMAL, 15));

    execute(alterColumnType("qifimportpos", "betrag", COLTYPE.DECIMAL, 15));

    execute(
        alterColumnType("qifimporthead", "startsaldo", COLTYPE.DECIMAL, 15));

    execute(dropColumn("buchungsart", "steuersatz"));
  }
}
