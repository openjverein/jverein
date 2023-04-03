/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung fuer Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2015 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import java.sql.Connection;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate.COLTYPE;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0423 extends AbstractDDLUpdate
{
  public Update0423(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    // Add tax rate
    execute(addColumn("mitgliedskonto",
        new Column("nettobetrag", COLTYPE.DOUBLE, 0, "0", false, false)));
    // Add tax rate
    execute(addColumn("mitgliedskonto",
        new Column("steuersatz", COLTYPE.DOUBLE, 0, "0", false, false)));
    // Add tax amount
    execute(addColumn("mitgliedskonto",
        new Column("steuerbetrag", COLTYPE.DOUBLE, 0, "0", false, false)));
  }
}