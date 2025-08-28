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

public class Update0485 extends AbstractDDLUpdate
{
  public Update0485(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {
    // Mitglieder
    execute(
        "INSERT into vorlage (name, muster) VALUES ('personalbogen-dateiname', 'Personalbogen-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('personalbogen-mitglied-dateiname', 'Personalbogen-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('vcard-dateiname', 'VCards-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('vcard-mitglied-dateiname', 'VCard-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit');\n");

    execute(
        "INSERT into vorlage (name, muster) VALUES ('sollbuchungen-dateiname', 'Sollbuchungen-Differenz-$filter_differenz-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('spendenbescheinigungen-dateiname', 'Spendenbescheinigungen-$filter_spendeart-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('zusatzbetraege-dateiname', 'Zusatzbetraege-$filter_ausfuehrungstag-$aktuellesdatum-$aktuellezeit');\n");

    // Buchführung
    execute(
        "INSERT into vorlage (name, muster) VALUES ('kontensaldo-dateiname', 'Kontensdaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('buchungsjournal-dateiname', 'Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('csvbuchungen-dateiname', 'Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('einzelbuchungen-dateiname', 'Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('summenbuchungen-dateiname', 'Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('anlagen-buchungsjournal-dateiname', 'Anlagen-Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('anlagen-csvbuchungen-dateiname', 'Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('anlagen-einzelbuchungen-dateiname', 'Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('anlagen-summenbuchungen-dateiname', 'Anlagen-Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('buchungsklassensaldo-dateiname', 'Buchungsklassensaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('umsatzsteuervoranmeldung-dateiname', 'Umsatzsteuer-Voranmeldung-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('projektsaldo-dateiname', 'Projektsaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('anlagenverzeichnis-dateiname', 'Anlagenverzeichnis-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('mittelverwendung-zufluss-dateiname', 'Mittelverwendung-Zufluss-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('mittelverwendung-saldo-dateiname', 'Mittelverwendung-Saldo-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('mittelverwendungssaldo-dateiname', 'Mittelverwendungssaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit');\n");

    // Abrechnung
    execute(
        "INSERT into vorlage (name, muster) VALUES ('abrechnungslauf-lastschriften-dateiname', 'Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('abrechnungslauf-sepa-xml-dateiname', 'SEPA-Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('abrechnungslaufliste-dateiname', 'Abrechnungslaufliste-$parameter_lauf-$aktuellesdatum-$aktuellezeit');\n");

    // Auswertungen
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-mitglied-dateiname', 'Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-nichtmitglied-dateiname', 'Nicht-Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-altersjubilare-dateiname', 'Altersjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-mitgliedsschaftsjubilare-dateiname', 'Mitgliedschaftsjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-kursteilnehmer-dateiname', 'Kursteilnehmer-$filter_abbuchung_von_f-$filter_abbuchung_bis_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-mitgliederstatistik-dateiname', 'Mitgliederstatistik-$filter_stichtag_f-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-jahrgangsstatistik-dateiname', 'Jahrgangsstatistik-$filter_jahr-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('auswertung-arbeitseinsaetze-dateiname', 'Arbeitseinsaetze-$filter_jahr-$filter_auswertung-$aktuellesdatum-$aktuellezeit');\n");

    // Einstellung Mitglieder
    execute(
        "INSERT into vorlage (name, muster) VALUES ('formular-dateiname', 'Formular-$formular_name-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('formularfelder-dateiname', 'Formularfelder-$formular_name-$aktuellesdatum-$aktuellezeit');\n");

    // Einstellung Buchführung
    execute(
        "INSERT into vorlage (name, muster) VALUES ('buchungsarten-dateiname', 'Buchungsarten-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('kontenrahmen-v1-dateiname', 'Kontenrahmen-v1-$aktuellesdatum-$aktuellezeit');\n");
    execute(
        "INSERT into vorlage (name, muster) VALUES ('kontenrahmen-v2-dateiname', 'Kontenrahmen-v2-$aktuellesdatum-$aktuellezeit');\n");
  }
}
