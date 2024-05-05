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
    execute(dropForeignKey("FK_SEKUNDAERBEITRAGEGRUPPE1", "SEKUNDAEREBEITRAGSGRUPPE"));
    execute(createForeignKey("FK_SEKUNDAERBEITRAGEGRUPPE1", "SEKUNDAEREBEITRAGSGRUPPE",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKLASTSCHRIFT2", "LASTSCHRIFT"));
    execute(createForeignKey("FKLASTSCHRIFT2", "LASTSCHRIFT",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKLASTSCHRIFT3", "LASTSCHRIFT"));
    execute(createForeignKey("FKLASTSCHRIFT3", "LASTSCHRIFT",
        "kursteilnehmer", "kursteilnehmer", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKMITGLIEDDOKUMENT1", "MITGLIEDDOKUMENT"));
    execute(createForeignKey("FKMITGLIEDDOKUMENT1", "MITGLIEDDOKUMENT",
        "referenz", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKSPENDENBESCHEINIGUNG2", "SPENDENBESCHEINIGUNG"));
    execute(createForeignKey("FKSPENDENBESCHEINIGUNG2", "SPENDENBESCHEINIGUNG",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKWIEDERVORLAGE1", "WIEDERVORLAGE"));
    execute(createForeignKey("FKWIEDERVORLAGE1", "WIEDERVORLAGE",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKZUSATZABBUCHUNG1", "ZUSATZABBUCHUNG"));
    execute(createForeignKey("FKZUSATZABBUCHUNG1", "ZUSATZABBUCHUNG",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("FKBUCHUNG3", "BUCHUNG"));
    execute(createForeignKey("FKBUCHUNG3", "BUCHUNG",
        "mitgliedskonto", "mitgliedskonto", "id", "SET NULL", "NO ACTION"));
    
    execute(dropForeignKey("FKBUCHUNG5", "BUCHUNG"));
    execute(createForeignKey("FKBUCHUNG5", "BUCHUNG",
        "spendenbescheinigung", "spendenbescheinigung", "id", "SET NULL", "NO ACTION"));
  }
}
