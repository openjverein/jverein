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

public class Update0443 extends AbstractDDLUpdate
{
  public Update0443(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    if (getDriver() == DRIVER.H2)
    {
      runh2();
    }
    else
    {
      try
      {
        runh2();
      }
      catch (Exception ex)
      {
        runmysql();
      }
    }
    
    execute(dropForeignKey("fkLastschrift2", "lastschrift"));
    execute(createForeignKey("fkLastschrift2", "lastschrift",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkLastschrift3", "lastschrift"));
    execute(createForeignKey("fkLastschrift3", "lastschrift",
        "kursteilnehmer", "kursteilnehmer", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkMitgliedDokument1", "mitglieddokument"));
    execute(createForeignKey("fkMitgliedDokument1", "mitglieddokument",
        "referenz", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkSpendenbescheinigung2", "spendenbescheinigung"));
    execute(createForeignKey("fkSpendenbescheinigung2", "spendenbescheinigung",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkWiedervorlage1", "wiedervorlage"));
    execute(createForeignKey("fkWiedervorlage1", "wiedervorlage",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkZusatzabbuchung1", "zusatzabbuchung"));
    execute(createForeignKey("fkZusatzabbuchung1", "zusatzabbuchung",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
    
    execute(dropForeignKey("fkBuchung5", "buchung"));
    execute(createForeignKey("fkBuchung5", "buchung",
        "spendenbescheinigung", "spendenbescheinigung", "id", "SET NULL", "NO ACTION"));
    
  }
  
  private void runh2() throws ApplicationException
  {
    execute(dropForeignKey("fk_sekundaerbeitragegruppe1", "sekundaerebeitragsgruppe"));
    execute(createForeignKey("fk_sekundaerbeitragegruppe1", "sekundaerebeitragsgruppe",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
  }
  
  private void runmysql() throws ApplicationException
  {
    execute(dropForeignKey("sekundaerebeitragsgruppe_ibfk_1", "sekundaerebeitragsgruppe"));
    execute(createForeignKey("fk_sekundaerbeitragegruppe1", "sekundaerebeitragsgruppe",
        "mitglied", "mitglied", "id", "CASCADE", "NO ACTION"));
  }
  
}
