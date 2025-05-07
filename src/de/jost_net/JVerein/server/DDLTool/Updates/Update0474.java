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

public class Update0474 extends AbstractDDLUpdate
{
  public Update0474(String driver, ProgressMonitor monitor, Connection conn)
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
    execute(
        "update spendenbescheinigung set unterlagenwertermittlung = 0 where unterlagenwertermittlung is null");
    execute(
        "update spendenbescheinigung set bezeichnungsachzuwendung = '' where bezeichnungsachzuwendung is null");
    execute(
        "update spendenbescheinigung set ersatzaufwendungen = 0 where ersatzaufwendungen is null");
    execute(
        "update spendenbescheinigung set herkunftspende = 3 where herkunftspende is null");
    execute("update spendenbescheinigung set zeile1 = '' where zeile1 is null");
    execute("update spendenbescheinigung set zeile2 = '' where zeile2 is null");
    execute("update spendenbescheinigung set zeile3 = '' where zeile3 is null");
    execute("update spendenbescheinigung set zeile4 = '' where zeile4 is null");
    execute("update spendenbescheinigung set zeile5 = '' where zeile5 is null");
    execute("update spendenbescheinigung set zeile6 = '' where zeile6 is null");
    execute("update spendenbescheinigung set zeile7 = '' where zeile7 is null");

    // Konto
    execute("update konto set kommentar = '' where kommentar is null");
    
    // Mitglied
    execute("update mitglied set leitwegid = '' where leitwegid is null");
    execute("update mitglied set ktoipersonenart = 'J' where ktoipersonenart = 'j'");
    execute("update mitglied set ktoipersonenart = 'N' where ktoipersonenart = 'n'");
    execute("update mitglied set mandatid = '' where mandatid is null");
    execute("update mitglied set staat = '' where staat is null");
    execute(
        "update mitglied set adressierungszusatz = '' where adressierungszusatz is null");
    execute("update mitglied set anrede = '' where anrede is null");
    execute("update mitglied set email = '' where email is null");
    execute(
        "update mitglied set ktoiadressierungszusatz = '' where ktoiadressierungszusatz is null");
    execute("update mitglied set ktoianrede = '' where ktoianrede is null");
    execute("update mitglied set ktoiemail = '' where ktoiemail is null");
    execute("update mitglied set ktoiname = '' where ktoiname is null");
    execute("update mitglied set ktoiort = '' where ktoiort is null");
    execute("update mitglied set ktoiplz = '' where ktoiplz is null");
    execute("update mitglied set ktoistaat = '' where ktoistaat is null");
    execute("update mitglied set ktoistrasse = '' where ktoistrasse is null");
    execute("update mitglied set ktoititel = '' where ktoititel is null");
    execute("update mitglied set ktoivorname = '' where ktoivorname is null");
    execute(
        "update mitglied set ktoigeschlecht = '' where ktoigeschlecht is null");
    execute(
        "update mitglied set telefondienstlich = '' where telefondienstlich is null");
    execute(
        "update mitglied set telefonprivat = '' where telefonprivat is null");
    execute("update mitglied set handy = '' where handy is null");
    execute("update mitglied set titel = '' where titel is null");
    execute("update mitglied set vermerk1 = '' where vermerk1 is null");
    execute("update mitglied set vermerk2 = '' where vermerk2 is null");
  }
}
