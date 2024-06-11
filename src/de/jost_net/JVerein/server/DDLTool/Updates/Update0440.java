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

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0440 extends AbstractDDLUpdate
{
  public Update0440(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(dropForeignKey("fk_sekundaerbeitragsgruppe1", "sekundaerebeitragsgruppe"));
    execute(createForeignKey("fk_sekundaerbeitragsgruppe1", "sekundaerebeitragsgruppe",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fklastschrift2", "lastschrift"));
    execute(createForeignKey("fklastschrift2", "lastschrift",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fklastschrift3", "lastschrift"));
    execute(createForeignKey("fklastschrift3", "lastschrift",
        "kursteilnehmer", "kursteilnehmer", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkmitglieddokument1", "mitglieddokument"));
    execute(createForeignKey("fkmitglieddokument1", "mitglieddokument",
        "referenz", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkspendenbescheinigung2", "spendenbescheinigung"));
    execute(createForeignKey("fkspendenbescheinigung2", "spendenbescheinigung",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkwiedervorlage1", "wiedervorlage"));
    execute(createForeignKey("fkwiedervorlage1", "wiedervorlage",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkzusatzabbuchung1", "zusatzabbuchung"));
    execute(createForeignKey("fkzusatzabbuchung1", "zusatzabbuchung",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkbuchung3", "buchung"));
    execute(createForeignKey("fkbuchung3", "buchung",
        "mitgliedskonto", "mitgliedskonto", "id", "SET NULL", "NO ACTION"));
    
    execute(dropForeignKey("fkbuchung5", "buchung"));
    execute(createForeignKey("fkbuchung5", "buchung",
        "spendenbescheinigung", "spendenbescheinigung", "id", "SET NULL", "NO ACTION"));
  }
}
