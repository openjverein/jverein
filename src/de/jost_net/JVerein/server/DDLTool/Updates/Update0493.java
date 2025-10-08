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
import de.jost_net.JVerein.server.DDLTool.Column;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0493 extends AbstractDDLUpdate
{
  public Update0493(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    execute(addColumn("vorlage",
        new Column("art", COLTYPE.INTEGER, 10, "0", false, false)));
    execute("update vorlage set art = 1");

    // Mitglieder
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('kontoauszug-titel', '$verein_name', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('kontoauszug-subtitel', 'Kontoauszug $mitglied_vornamename, Stand: $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$aktuellesdatum))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('personalbogen-titel', 'Personalbogen $mitglied_name $mitglied_vorname', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('personalbogen-subtitel', '', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('spendenbescheinigungen-titel', 'Spendenbescheinigungen', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('spendenbescheinigungen-subtitel', '', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('zusatzbetraege-titel', 'Zusatzbeträge', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('zusatzbetraege-subtitel', '', 2);\n");

    // Buchführung
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('kontensaldo-titel', 'Kontensaldo', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('kontensaldo-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('buchungsjournal-titel', 'Buchungsjournal', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('buchungsjournal-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('einzelbuchungen-titel', 'Buchungsliste', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('einzelbuchungen-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('summenbuchungen-titel', 'Summenliste', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('summenbuchungen-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))', 2);\n");

    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagen-buchungsjournal-titel', 'Buchungsjournal', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagen-buchungsjournal-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagen-einzelbuchungen-titel', 'Buchungsliste', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagen-einzelbuchungen-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagen-summenbuchungen-titel', 'Summenliste', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagen-summenbuchungen-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))', 2);\n");

    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('buchungsklassensaldo-titel', 'Buchungsklassensaldo', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('buchungsklassensaldo-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('umsatzsteuervoranmeldung-titel', 'Umsatzsteuer Voranmeldung', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('umsatzsteuervoranmeldung-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('projektsaldo-titel', 'Projektsaldo', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('projektsaldo-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");

    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagenverzeichnis-titel', 'Anlagenverzeichnis', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('anlagenverzeichnis-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('mittelverwendung-zufluss-titel', 'Mittelverwendungsrechnung', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('mittelverwendung-zufluss-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('mittelverwendung-saldo-titel', 'Mittelverwendungsrechnung', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('mittelverwendung-saldo-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('mittelverwendungssaldo-titel', 'Mittelverwendungssaldo', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('mittelverwendungssaldo-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('wirtschaftsplan-titel', 'Wirtschaftsplan', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('wirtschaftsplan-subtitel', '$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_bis_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('wirtschaftsplan-mehrere-titel', 'Wirtschaftsplan', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('wirtschaftsplan-mehrere-subtitel', '', 2);\n");

    // Abrechnung
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('abrechnungslaufliste-titel', 'Abrechnungslauf', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('abrechnungslaufliste-subtitel', 'Nr. $parameter_lauf zum $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_f))', 2);\n");

    // Auswertungen
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-mitglied-titel', 'Mitglieder', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-nichtmitglied-titel', '$filter_mitgliedstyp', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-altersjubilare-titel', 'Altersjubilare $filter_jahr', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-altersjubilare-subtitel', '', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-mitgliedsschaftsjubilare-titel', 'Mitgliedschaftsjubilare $filter_jahr', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-mitgliedsschaftsjubilare-subtitel', '', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-kursteilnehmer-titel', 'Kursteilnehmer', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-kursteilnehmer-subtitel', '', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-mitgliederstatistik-titel', 'Mitgliederstatistik', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-mitgliederstatistik-subtitel', 'Stichtag: $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_stichtag_f))', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-jahrgangsstatistik-titel', 'Jahrgangsstatistik', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-jahrgangsstatistik-subtitel', 'Jahr: $filter_jahr', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-arbeitseinsaetze-titel', 'Arbeitseinsätze $filter_jahr', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('auswertung-arbeitseinsaetze-subtitel', '$filter_auswertung', 2);\n");

    // Einstellung Mitglieder

    // Einstellung Buchführung
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('buchungsarten-titel', 'Buchungsarten', 2);\n");
    execute(
        "INSERT into vorlage (name, muster, art) VALUES ('buchungsarten-subtitel', '', 2);\n");
  }
}
