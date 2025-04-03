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

public class Update0472 extends AbstractDDLUpdate
{
  public Update0472(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    // Buchung
    execute("update buchung set verzicht = 0 where verzicht is null");
    execute("update buchung set art = '' where art is null");
    execute("update buchung set iban = '' where iban is null");
    execute("update buchung set kommentar = '' where kommentar is null");

    // Buchungsart
    execute("update buchungsart set suchbegriff = '' where suchbegriff is null");
    execute("update buchungsart set regularexp = 0 where regularexp is null");

    // Spendenbescheinigung
    execute("update spendenbeschein set ersatzaufwendungen = 0 where ersatzaufwendungen is null");
    execute("update spendenbeschein set unterlagenwertermittlung = 0 where unterlagenwertermittlung is null");

    // Konto
    execute("update konto set kommentar = '' where kommentar is null");
    
    // Mitglied
    execute("update mitglied set leitwegid = '' where leitwegid is null");
    execute("update mitglied set ktoistaat = '' where ktoistaat is null");
    execute("update mitglied set ktoipersonenart = 'J' where ktoipersonenart = 'j'");
    execute("update mitglied set ktoipersonenart = 'N' where ktoipersonenart = 'n'");
    execute("update mitglied set mandatid = '' where mandatid is null");
    execute("update mitglied set ktoistaat = '' where ktoistaat is null");
  }
}
