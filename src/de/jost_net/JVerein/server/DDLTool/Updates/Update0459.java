/**********************************************************************
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not,
 * see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import java.sql.Connection;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0459 extends AbstractDDLUpdate
{
  public Update0459(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    {
      // Autoincrement von Rechnung auf den maximalen Zähler aus Formular setzen
      // so gibt es eine fortlaufende Rechnungsnummer bei verwendung der
      // Rechnung-ID
      if (getDriver() == DRIVER.H2)
      {
        execute(
            "SET @max_id = (SELECT case when MAX(zaehler) is null then 1 else MAX(zaehler)+1 end FROM formular WHERE art = 2);"
                + "ALTER TABLE rechnung ALTER COLUMN id RESTART WITH @max_id;");
      }
      if (getDriver() == DRIVER.MYSQL)
      {
        execute(
            "SET @max_id = (SELECT if(MAX(zaehler),MAX(zaehler)+1,1) FROM formular WHERE art = 2);"
                + "SET @sql = CONCAT('ALTER TABLE rechnung AUTO_INCREMENT = ', @max_id);"
                + "PREPARE st FROM @sql; EXECUTE st;");
      }
    }
  }
}
