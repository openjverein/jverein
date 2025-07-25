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

public class Update0462 extends AbstractDDLUpdate
{
  public Update0462(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    try
    {
      execute(dropColumn("konto", "anlagenkonto"));
    }
    catch (Exception e)
    {
      // Es gab zwischendurch schon ein Nightly wo das Attribut gelöscht wurde
      // Das war mit der ersten Version von Update0453
      // Dann ist es schon nicht mehr da
    }

    // Diese Attribute sind jetzt in der Sollbuchungposition
    try
    {
      execute(dropForeignKey("fkMitgliedskonto3", "mitgliedskonto"));
      execute(dropColumn("mitgliedskonto", "buchungsart"));
    }
    catch (Exception e)
    {
      // Wenn bei MySQL der Key einen anderen NAmen hat damm lassen wir das
      // Attribut halt bestehen
    }
    execute(dropForeignKey("fkMitgliedkonto4", "mitgliedskonto"));
    execute(dropColumn("mitgliedskonto", "buchungsklasse"));
    execute(dropColumn("mitgliedskonto", "steuersatz"));
    execute(dropColumn("mitgliedskonto", "nettobetrag"));
    execute(dropColumn("mitgliedskonto", "steuerbetrag"));
  }
}
