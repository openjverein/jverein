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

import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.Variable.MitgliedVar;
import de.jost_net.JVerein.Variable.RechnungVar;
import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0511 extends AbstractDDLUpdate
{
  public Update0511(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    execute("INSERT INTO einstellungneu (name, wert) SELECT '"
        + Property.QRCODETEXTVELOCITY.getKey() + "', CONCAT(" +

        // QRCODEFESTERTEXT
        "CASE WHEN q.festerText = 1 THEN CONCAT(" +

        // QRCODESNGLLINE:
        // Bei genau einer Zeile $ZAHLUNGSGRUND, sonst QRCODETEXT
        "CASE WHEN q.singleLine = 1 THEN '#if($"
        + RechnungVar.ZAHLUNGSGRUND.getName()
        + ".split(\"\\\\n\").size() == 1)$"
        + RechnungVar.ZAHLUNGSGRUND.getName() + "#{else}' ELSE '' END, " +

        // Bestehendes QRCODETEXT
        "q.text, " +

        // Ende des Velocity-if
        "CASE WHEN q.singleLine = 1 THEN '#end' ELSE '' END, " +

        // Komma nach Festtext
        "CASE WHEN q.datum = 1 OR q.rechnung = 1 "
        + "OR q.mitglied = 1 THEN ', ' ELSE '' END" +

        ") ELSE '' END, " +

        // Rechnung / Re.
        "CASE WHEN q.datum = 1 OR q.rechnung = 1 THEN "
        + "CASE WHEN q.kuerzen = 1 THEN 'Re. ' ELSE 'Rechnung ' END "
        + "ELSE '' END, " +

        // Rechnungsnummer
        "CASE WHEN q.rechnung = 1 THEN '$" + RechnungVar.NUMMER.getName()
        + "' ELSE '' END, " +

        // Rechnungsdatum
        "CASE WHEN q.datum = 1 THEN CONCAT("
        + "CASE WHEN q.rechnung = 1 THEN ' ' ELSE '' END, "
        + "CASE WHEN q.kuerzen = 1 THEN 'v. ' ELSE 'vom ' END, '$"
        + RechnungVar.DATUM.getName() + "') ELSE '' END, " +

        // Komma vor Mitglied
        "CASE WHEN q.mitglied = 1 AND (q.datum = 1 OR q.rechnung = 1) "
        + "THEN ', ' ELSE '' END, " +

        // Mitglied / Mitgl.
        "CASE WHEN q.mitglied = 1 THEN "
        + "CASE WHEN q.kuerzen = 1 THEN 'Mitgl. ' ELSE 'Mitglied ' END "
        + "ELSE '' END, " +

        // Mitgliedsnummer
        "CASE WHEN q.mitglied = 1 THEN "
        + "CASE WHEN q.externeMitgliedsnummer = 1 THEN '$"
        + MitgliedVar.EXTERNE_MITGLIEDSNUMMER.getName() + "' ELSE '$"
        + MitgliedVar.ID.getName() + "' END ELSE '' END" +

        ") AS wert " +

        "FROM (SELECT " +

        // QRCODEFESTERTEXT
        "MAX(CASE WHEN name = 'qrcodeptext' THEN wert ELSE '0' END) "
        + "AS festerText, " +

        // QRCODESNGLLINE
        "MAX(CASE WHEN name = 'qrcodesngl' THEN wert ELSE '0' END) "
        + "AS singleLine, " +

        // QRCODEDATUM
        "MAX(CASE WHEN name = 'qrcodepdate' THEN wert ELSE '0' END) "
        + "AS datum, " +

        // QRCODERENU
        "MAX(CASE WHEN name = 'qrcodeprenum' THEN wert ELSE '0' END) "
        + "AS rechnung, " +

        // QRCODEMEMBER
        "MAX(CASE WHEN name = 'qrcodepmnum' THEN wert ELSE '0' END) "
        + "AS mitglied, " +

        // QRCODEKUERZEN
        "MAX(CASE WHEN name = 'qrcodekuerzen' THEN wert ELSE '0' END) "
        + "AS kuerzen, " +

        // QRCODETEXT
        "MAX(CASE WHEN name = 'qrcodetext' THEN wert ELSE '' END) "
        + "AS text, " +

        // EXTERNEMITGLIEDSNUMMER bleibt als Property erhalten
        "MAX(CASE WHEN name = '" + Property.EXTERNEMITGLIEDSNUMMER.getKey()
        + "' THEN wert ELSE 0 END) AS externeMitgliedsnummer " +

        "FROM einstellungneu) q");
  }
}
