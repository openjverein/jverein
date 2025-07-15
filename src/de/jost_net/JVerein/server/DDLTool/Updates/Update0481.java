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
import java.sql.ResultSet;
import java.sql.SQLException;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0481 extends AbstractDDLUpdate
{
  public Update0481(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    try
    {
      ResultSet check = conn.createStatement().executeQuery(
          "SELECT * FROM einstellungneu WHERE name = 'optiertpflicht'");

      if (!check.next())
      {
        ResultSet result = conn.createStatement().executeQuery(
            "SELECT * FROM einstellungneu WHERE name = 'optiert'");

        if (result.next())
        {
          String value = result.getBoolean(3) ? "1" : "0";
          execute(
              "INSERT INTO einstellungneu (name,wert) VALUES('optiertpflicht','"
                  + value + "')");
        }
      }
      else
      {
        setNewVersion(481);
      }
    }
    catch (SQLException e)
    {
      String fehler = "Fehler beim Update";
      Logger.error(fehler, e);
      throw new ApplicationException(fehler);
    }
  }
}
