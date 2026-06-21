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

import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.Variable.SpendenbescheinigungVar;
import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0493 extends AbstractDDLUpdate
{
  public Update0493(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void run() throws ApplicationException
  {
    Column col = new Column("name", COLTYPE.VARCHAR, 1000, "''", true, false);
    execute(alterColumn("formularfeld", col));

    col = new Column("ausrichtung", COLTYPE.INTEGER, 1, "0", true, false);
    execute(addColumn("formularfeld", col));

    // Bisher waren Double Felder und der Steuersatz rechts ausgerichtet
    execute("UPDATE formularfeld SET ausrichtung = 1 WHERE name IN('"
        + RechnungVar.SUMME.getName() + "','" + RechnungVar.IST.getName()
        + "','" + RechnungVar.SUMME_OFFEN.getName() + "','"
        + RechnungVar.STAND.getName() + "','" + RechnungVar.STEUERSATZ.getName()
        + "','" + RechnungVar.MK_STEUERSATZ.getName() + "','"
        + RechnungVar.MK_SUMME_OFFEN.getName() + "','"
        + RechnungVar.MK_STAND.getName() + "','"
        + SpendenbescheinigungVar.BETRAG.getName() + "','"
        + RechnungVar.NETTOBETRAG.getName() + "','"
        + RechnungVar.STEUERBETRAG.getName() + "','"
        + RechnungVar.BETRAG.getName() + "','"
        + RechnungVar.MK_NETTOBETRAG.getName() + "','"
        + RechnungVar.MK_STEUERBETRAG.getName() + "','"
        + RechnungVar.MK_BETRAG.getName() + "')");
  }
}
