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

public class Update0484 extends AbstractDDLUpdate
{
  public Update0484(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute("INSERT INTO einstellungneu (name,wert)"
        + " SELECT 'kursteilnehmergebpflicht',wert FROM einstellungneu WHERE name = 'kursteilnehmergebgespflicht' ");
    execute("INSERT INTO einstellungneu (name,wert)"
        + " SELECT 'kursteilnehmergespflicht',wert FROM einstellungneu WHERE name = 'kursteilnehmergebgespflicht' ");
    execute("INSERT INTO einstellungneu (name,wert)"
        + " VALUES ('anlagenkonten', EXISTS (SELECT 1 FROM konto WHERE kontoart = 2))");
    execute("INSERT INTO einstellungneu (name,wert)"
        + " VALUES ('ruecklagenkonten', EXISTS (SELECT 1 FROM konto WHERE kontoart > 100 AND kontoart < 200))");
    execute("INSERT INTO einstellungneu (name,wert)"
        + " VALUES ('verbindlichkeitenforderungen', EXISTS (SELECT 1 FROM konto WHERE kontoart > 200))");
    execute("INSERT INTO einstellungneu (name,wert)"
        + " VALUES ('familienbeitrag', EXISTS (SELECT 1 FROM beitragsgruppe WHERE beitragsart = 2))");
    execute("INSERT INTO einstellungneu (name,wert)"
        + " VALUES ('usezusatzfelder', EXISTS (SELECT 1 FROM felddefinition))");
  }
}
