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

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

import java.sql.Connection;

public class Update0465 extends AbstractDDLUpdate
{
  public Update0465(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    // Abrechnungslauf
    execute(alterColumn("abrechnungslauf",
        new Column("dtausdruck", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("abrechnungslauf",
        new Column("zusatzbetraege", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("abrechnungslauf",
        new Column("kursteilnehmer", COLTYPE.BOOLEAN, 0, null, false, false)));
    // Buchungsart
    execute(alterColumn("buchungsart",
        new Column("spende", COLTYPE.BOOLEAN, 0, null, false, false)));
    // Eigenschaftengruppe
    execute(alterColumn("eigenschaftgruppe",
        new Column("pflicht", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("eigenschaftgruppe",
        new Column("max1", COLTYPE.BOOLEAN, 0, null, false, false)));
    // Einstellung
    execute(alterColumn("einstellung",
        new Column("vorlaeufig", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("mitgliedsbeitraege",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("geburtsdatumpflicht",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("eintrittsdatumpflicht",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("sterbedatum", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("kommunikationsdaten",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("zusatzabbuchung", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("vermerke", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("wiedervorlage", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("kursteilnehmer", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("lehrgaenge", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("juristischepersonen",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("mitgliedfoto", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("auslandsadressen",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("arbeitseinsatz", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("dokumentenspeicherung",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("individuellebeitraege",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung", new Column("externemitgliedsnummer",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("smtp_ssl", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("zusatzadressen", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("smtp_starttls", COLTYPE.BOOLEAN, 0, null, false, false)));
    // Spendenbescheinigung
    execute(alterColumn("spendenbescheinigung", new Column("ersatzaufwendungen",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("spendenbescheinigung", new Column(
        "unterlagenwertermittlung", COLTYPE.BOOLEAN, 0, null, false, false)));
    // Zusatzfelder
    execute(alterColumn("zusatzfelder",
        new Column("feldjanein", COLTYPE.BOOLEAN, 0, null, false, false)));

    // Attribute die nur bei der MySQL falsch waren
    execute(alterColumn("buchung",
        new Column("verzicht", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("spendenbescheinigung",
        new Column("autocreate", COLTYPE.BOOLEAN, 0, "0", false, false)));
    execute(alterColumn("einstellung",
        new Column("spendenbescheinigungprintbuchungsart", COLTYPE.BOOLEAN, 0,
            "0", false, false)));
    execute(alterColumn("einstellung", new Column("copy_to_imap_folder",
        COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("imap_ssl", COLTYPE.BOOLEAN, 0, null, false, false)));
    execute(alterColumn("einstellung",
        new Column("imap_starttls", COLTYPE.BOOLEAN, 0, null, false, false)));
  }

}
