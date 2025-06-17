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

public class Update0476 extends AbstractDDLUpdate
{
  public Update0476(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(
        "update formularfeld set font = 'Courier Prime' where font = 'Courier'");
    execute(
        "update formularfeld set font = 'Courier Prime Bold' where font = 'Courier-Bold'");
    execute(
        "update formularfeld set font = 'Courier Prime Bold Italic' where font = 'Courier-Oblique'");
    execute(
        "update formularfeld set font = 'Courier Prime Italic' where font = 'Courier-BoldOblique'");

    execute(
        "update formularfeld set font = 'LiberationSans-Bold' where font = 'Helvetica-Bold'");
    execute(
        "update formularfeld set font = 'LiberationSans-BoldItalic' where font = 'Helvetica-BoldOblique'");
    execute(
        "update formularfeld set font = 'LiberationSans-Italic' where font = 'Helvetica-Oblique'");
    execute(
        "update formularfeld set font = 'LiberationSans-Regular' where font = 'Helvetica'");

    execute(
        "update formularfeld set font = 'LiberationSerif-Bold' where font = 'Times-Bold'");
    execute(
        "update formularfeld set font = 'LiberationSerif-BoldItalic' where font = 'Times-BoldItalic'");
    execute(
        "update formularfeld set font = 'LiberationSerif-Italic' where font = 'Times-Italic'");
    execute(
        "update formularfeld set font = 'LiberationSerif-Regular' where font = 'Times-Roman'");
  }
}
