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

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0436 extends AbstractDDLUpdate
{
  public Update0436(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("einstellung",
        new Column("qrcodesizemm", COLTYPE.INTEGER, 1, "30", false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodepdate", COLTYPE.BOOLEAN, 1, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodeptext", COLTYPE.BOOLEAN, 1, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodepmnum", COLTYPE.BOOLEAN, 1, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodeprenum", COLTYPE.BOOLEAN, 1, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodesngl", COLTYPE.BOOLEAN, 1, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodetext", COLTYPE.VARCHAR, 140, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodeinfom", COLTYPE.VARCHAR, 70, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodeintro", COLTYPE.VARCHAR, 255, null, false, false)));
    execute(addColumn("einstellung",
        new Column("qrcodekuerzen", COLTYPE.BOOLEAN, 1, null, false, false)));
  }
}
