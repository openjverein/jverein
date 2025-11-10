/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
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
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.keys;

public enum VorlageTyp
{
  // Dateinamen
  SPENDENBESCHEINIGUNG_DATEINAME("spendenbescheinigung-dateiname",
      "SpendenbescheinigungDateiname",
      "Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$spendenbescheinigung_zeile2",
      1),
  SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME(
      "spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-MitgliedDateiname",
      "Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$mitglied_name-$mitglied_vorname",
      1),
  RECHNUNG_DATEINAME("rechnung-dateiname", "RechnungDateiname",
      "Rechnung-$aktuellesdatum-$aktuellezeit", 1),
  RECHNUNG_MITGLIED_DATEINAME("rechnung-mitglied-dateiname",
      "Rechnung-MitgliedDateiname",
      "Rechnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname", 1),
  MAHNUNG_DATEINAME("mahnung-dateiname", "MahnungDateiname",
      "Mahnung-$aktuellesdatum-$aktuellezeit", 1),
  MAHNUNG_MITGLIED_DATEINAME("mahnung-mitglied-dateiname",
      "Mahnung-MitgliedDateiname",
      "Mahnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname", 1),
  KONTOAUSZUG_DATEINAME("kontoauszug-dateiname", "KontoauszugDateiname",
      "Kontoauszug-$aktuellesdatum-$aktuellezeit", 1),
  KONTOAUSZUG_MITGLIED_DATEINAME("kontoauszug-mitglied-dateiname",
      "Kontoauszug-MitgliedDateiname",
      "Kontoauszug-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      1),
  FREIES_FORMULAR_DATEINAME("freies-formular-dateiname",
      "FreiesFormularDateiname", "$formular_name-$aktuellesdatum-$aktuellezeit",
      1),
  FREIES_FORMULAR_MITGLIED_DATEINAME("freies-formular-mitglied-dateiname",
      "FreiesFormular-MitgliedDateiname",
      "$formular_name-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      1),
  CT1_AUSGABE_DATEINAME("1ct-ausgabe-dateiname", "1ctAusgabeDateiname",
      "1ctueberweisung-$aktuellesdatum-$aktuellezeit", 1),
  PRENOTIFICATION_DATEINAME("pre-notification-dateiname",
      "Pre-NotificationDateiname",
      "Prenotification-$aktuellesdatum-$aktuellezeit", 1),
  PRENOTIFICATION_MITGLIED_DATEINAME("pre-notification-mitglied-dateiname",
      "Pre-Notification-MitgliedDateiname",
      "Prenotification-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      1),

  // Reports aus Mitglieder
  PERSONALBOGEN_DATEINAME("personalbogen-dateiname", "PersonalbogenDateiname",
      "Personalbogen-$aktuellesdatum-$aktuellezeit", 1),
  PERSONALBOGEN_MITGLIED_DATEINAME("personalbogen-mitglied-dateiname",
      "Personalbogen-MitgliedDateiname",
      "Personalbogen-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      1),
  VCARD_DATEINAME("vcard-dateiname", "VCardDateiname",
      "VCards-$aktuellesdatum-$aktuellezeit", 1),
  VCARD_MITGLIED_DATEINAME("vcard-mitglied-dateiname",
      "VCARD-MitgliedDateiname",
      "VCard-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      1),
  SOLLBUCHUNGEN_DATEINAME("sollbuchungen-dateiname",
      "SollbuchungenListeDateiname",
      "Sollbuchungen-Differenz-$filter_differenz-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  SPENDENBESCHEINIGUNGEN_DATEINAME("spendenbescheinigungen-dateiname",
      "SpendenbescheinigungenListeDateiname",
      "Spendenbescheinigungen-$filter_spendeart-$aktuellesdatum", 1),
  ZUSATZBETRAEGE_DATEINAME("zusatzbetraege-dateiname",
      "ZusatzbeträgeListeDateiname",
      "Zusatzbetraege-$filter_ausfuehrungstag-$aktuellesdatum-$aktuellezeit",
      1),

  // Reports aus Buchführung
  KONTENSALDO_DATEINAME("kontensaldo-dateiname", "KontensaldoDateiname",
      "Kontensdaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  BUCHUNGSJOURNAL_DATEINAME("buchungsjournal-dateiname",
      "BuchungsjournalDateiname",
      "Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  EINZELBUCHUNGEN_DATEINAME("einzelbuchungen-dateiname",
      "EinzelbuchungenDateiname",
      "Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  CSVBUCHUNGEN_DATEINAME("csvbuchungen-dateiname",
      "CSVEinzelbuchungenDateiname",
      "Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  SUMMENBUCHUNGEN_DATEINAME("summenbuchungen-dateiname",
      "SummenbuchungenDateiname",
      "Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  ANLAGEN_BUCHUNGSJOURNAL_DATEINAME("anlagen-buchungsjournal-dateiname",
      "AnlagenBuchungsjournalDateiname",
      "Anlagen-Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  ANLAGEN_EINZELBUCHUNGEN_DATEINAME("anlagen-einzelbuchungen-dateiname",
      "AnlagenEinzelbuchungenDateiname",
      "Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  ANLAGEN_CSVBUCHUNGEN_DATEINAME("anlagen-csvbuchungen-dateiname",
      "AnlagenCSVEinzelbuchungenDateiname",
      "Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  ANLAGEN_SUMMENBUCHUNGEN_DATEINAME("anlagen-summenbuchungen-dateiname",
      "AnlagenSummenbuchungenDateiname",
      "Anlagen-Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  BUCHUNGSKLASSENSALDO_DATEINAME("buchungsklassensaldo-dateiname",
      "BuchungsklassensaldoDateiname",
      "Buchungsklassensaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  UMSATZSTEUER_VORANMELDUNG_DATEINAME("umsatzsteuervoranmeldung-dateiname",
      "UmsatzsteuerVoranmeldungDateiname",
      "Umsatzsteuer-Voranmeldung-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  PROJEKTSALDO_DATEINAME("projektsaldo-dateiname", "ProjektsaldoDateiname",
      "Projektsaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  ANLAGENVERZEICHNIS_DATEINAME("anlagenverzeichnis-dateiname",
      "AnlagenverzeichnisDateiname",
      "Anlagenverzeichnis-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit", 1),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME(
      "mittelverwendung-zufluss-dateiname",
      "MittelverwendungZuflussreportDateiname",
      "Mittelverwendung-Zufluss-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit",
      1),
  MITTELVERWENDUNGSREPORT_SALDO_DATEINAME("mittelverwendung-saldo-dateiname",
      "MittelverwendungSaldoreportDateiname",
      "Mittelverwendung-Saldo-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit", 1),
  MITTELVERWENDUNGSSALDO_DATEINAME("mittelverwendungssaldo-dateiname",
      "MittelverwendungssaldoDateiname",
      "Mittelverwendungssaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  WIRTSCHAFTSPLAN_DATEINAME("wirtschaftsplan-dateiname",
      "WirtschaftsplanDateiname",
      "Wirtschaftsplan-$parameter_bezeichnung-$parameter_datum_von_f-$parameter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  WIRTSCHAFTSPLAN_MEHRERE_DATEINAME("wirtschaftsplan-mehrere-dateiname",
      "WirtschaftsplanDateiname",
      "Wirtschaftsplan-$aktuellesdatum-$aktuellezeit", 1),

  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME(
      "abrechnungslauf-lastschriften-dateiname",
      "AbrechnungslaufLastschriftenDateiname",
      "Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit",
      1),
  ABRECHNUNGSLAUF_SEPA_DATEINAME("abrechnungslauf-sepa-xml-dateiname",
      "AbrechnungslaufSEPAXMLLastschriftenDateiname",
      "SEPA-Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit",
      1),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME("abrechnungslaufliste-dateiname",
      "AbrechnungslaufSollbuchungenDateiname",
      "Abrechnungslaufliste-$parameter_lauf-$aktuellesdatum-$aktuellezeit", 1),

  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_DATEINAME("auswertung-mitglied-dateiname",
      "AuswertungMitgliedDateiname",
      "Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit", 1),
  AUSWERTUNG_NICHT_MITGLIED_DATEINAME("auswertung-nichtmitglied-dateiname",
      "AuswertungNicht-MitgliedDateiname",
      "Nicht-Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit", 1),
  AUSWERTUNG_ALTERSJUBILARE_DATEINAME("auswertung-altersjubilare-dateiname",
      "AuswertungAltersjubiläenDateiname",
      "Altersjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit", 1),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME(
      "auswertung-mitgliedsschaftsjubilare-dateiname",
      "AuswertungMitgliedschaftsjubiläenDateiname",
      "Mitgliedschaftsjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit", 1),
  AUSWERTUNG_KURSTEILNEHMER_DATEINAME("auswertung-kursteilnehmer-dateiname",
      "AuswertungKursteilnehmerDateiname",
      "Kursteilnehmer-$filter_abbuchung_von_f-$filter_abbuchung_bis_f-$aktuellesdatum-$aktuellezeit",
      1),
  AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME(
      "auswertung-mitgliederstatistik-dateiname",
      "AuswertungMitgliederstatistikDateiname",
      "Mitgliederstatistik-$filter_stichtag_f-$aktuellesdatum-$aktuellezeit",
      1),
  AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME(
      "auswertung-jahrgangsstatistik-dateiname",
      "AuswertungJahrgangsstatistikDateiname",
      "Jahrgangsstatistik-$filter_jahr-$aktuellesdatum-$aktuellezeit", 1),
  AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME("auswertung-arbeitseinsaetze-dateiname",
      "AuswertungArbeitseinsätzeDateiname",
      "Arbeitseinsaetze-$filter_jahr-$filter_auswertung-$aktuellesdatum-$aktuellezeit",
      1),

  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_DATEINAME("buchungsarten-dateiname", "BuchungsartenDateiname",
      "Buchungsarten-$aktuellesdatum-$aktuellezeit", 1),
  KONTENRAHMEN_DATEINAME_V1("kontenrahmen-v1-dateiname",
      "KontenrahmenVersion1Dateiname",
      "Kontenrahmen-v1-$aktuellesdatum-$aktuellezeit", 1),
  KONTENRAHMEN_DATEINAME_V2("kontenrahmen-v2-dateiname",
      "KontenrahmenVersion2Dateiname",
      "Kontenrahmen-v2-$aktuellesdatum-$aktuellezeit", 1),
  FORMULAR_DATEINAME("formular-dateiname", "FormularDateiname",
      "Formular-$formular_name-$aktuellesdatum-$aktuellezeit", 1),
  FORMULARFELDER_DATEINAME("formularfelder-dateiname",
      "FormularfelderDateiname",
      "Formularfelder-$formular_name-$aktuellesdatum-$aktuellezeit", 1),

  // Titel
  KONTOAUSZUG_TITEL("kontoauszug-titel", "Kontoauszug Titel", "$verein_name",
      2),
  KONTOAUSZUG_SUBTITEL("kontoauszug-subtitel", "Kontoauszug Subtitel",
      "Kontoauszug $mitglied_vornamename, Stand: $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$aktuellesdatum))",
      2),
  // Reports aus Mitglieder
  PERSONALBOGEN_TITEL("personalbogen-titel", "Personalbogen Titel",
      "Personalbogen $mitglied_name $mitglied_vorname", 2),
  PERSONALBOGEN_SUBTITEL("personalbogen-subtitel", "Personalbogen Subtitel", "",
      2),
  SPENDENBESCHEINIGUNGEN_TITEL("spendenbescheinigungen-titel",
      "Spendenbescheinigungen Liste Titel", "Spendenbescheinigungen", 2),
  SPENDENBESCHEINIGUNGEN_SUBTITEL("spendenbescheinigungen-subtitel",
      "Spendenbescheinigungen Liste Subtitel", "", 2),
  ZUSATZBETRAEGE_TITEL("zusatzbetraege-titel", "Zusatzbeträge Liste Titel",
      "Zusatzbeträge", 2),
  ZUSATZBETRAEGE_SUBTITEL("zusatzbetraege-subtitel",
      "Zusatzbeträge Liste Subtitel", "", 2),
  // Reports aus Buchführung
  KONTENSALDO_TITEL("kontensaldo-titel", "Kontensaldo Titel", "Kontensaldo", 2),
  KONTENSALDO_SUBTITEL("kontensaldo-subtitel", "Kontensaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  BUCHUNGSJOURNAL_TITEL("buchungsjournal-titel", "Buchungsjournal Titel",
      "Buchungsjournal", 2),
  BUCHUNGSJOURNAL_SUBTITEL("buchungsjournal-subtitel",
      "Buchungsjournal Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      2),
  EINZELBUCHUNGEN_TITEL("einzelbuchungen-titel", "Einzelbuchungen Titel",
      "Buchungsliste", 2),
  EINZELBUCHUNGEN_SUBTITEL("einzelbuchungen-subtitel",
      "Einzelbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      2),
  SUMMENBUCHUNGEN_TITEL("summenbuchungen-titel", "Summenbuchungen Titel",
      "Summenliste", 2),
  SUMMENBUCHUNGEN_SUBTITEL("summenbuchungen-subtitel",
      "Summenbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      2),
  ANLAGEN_BUCHUNGSJOURNAL_TITEL("anlagen-buchungsjournal-titel",
      "Anlagen Buchungsjournal Titel", "Buchungsjournal", 2),
  ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL("anlagen-buchungsjournal-subtitel",
      "Anlagen Buchungsjournal Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      2),
  ANLAGEN_EINZELBUCHUNGEN_TITEL("anlagen-einzelbuchungen-titel",
      "Anlagen Einzelbuchungen Titel", "Buchungsliste", 2),
  ANLAGEN_EINZELBUCHUNGEN_SUBTITEL("anlagen-einzelbuchungen-subtitel",
      "Anlagen Einzelbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      2),
  ANLAGEN_SUMMENBUCHUNGEN_TITEL("anlagen-summenbuchungen-titel",
      "Anlagen Summenbuchungen Titel", "Summenliste", 2),
  ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL("anlagen-summenbuchungen-subtitel",
      "Anlagen Summenbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      2),
  BUCHUNGSKLASSENSALDO_TITEL("buchungsklassensaldo-titel",
      "Buchungsklassensaldo Titel", "Buchungsklassensaldo", 2),
  BUCHUNGSKLASSENSALDO_SUBTITEL("buchungsklassensaldo-subtitel",
      "Buchungsklassensaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  UMSATZSTEUER_VORANMELDUNG_TITEL("umsatzsteuervoranmeldung-titel",
      "Umsatzsteuer Voranmeldung Titel", "Umsatzsteuer Voranmeldung", 2),
  UMSATZSTEUER_VORANMELDUNG_SUBTITEL("umsatzsteuervoranmeldung-subtitel",
      "Umsatzsteuer Voranmeldung Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  PROJEKTSALDO_TITEL("projektsaldo-titel", "Projektsaldo Titel", "Projektsaldo",
      2),
  PROJEKTSALDO_SUBTITEL("projektsaldo-subtitel", "Projektsaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  ANLAGENVERZEICHNIS_TITEL("anlagenverzeichnis-titel",
      "Anlagenverzeichnis Titel", "Anlagenverzeichnis", 2),
  ANLAGENVERZEICHNIS_SUBTITEL("anlagenverzeichnis-subtitel",
      "Anlagenverzeichnis Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL("mittelverwendung-zufluss-titel",
      "Mittelverwendung Zuflussreport Titel", "Mittelverwendungsrechnung", 2),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL("mittelverwendung-zufluss-subtitel",
      "Mittelverwendung Zuflussreport Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  MITTELVERWENDUNGSREPORT_SALDO_TITEL("mittelverwendung-saldo-titel",
      "Mittelverwendung Saldoreport Titel", "Mittelverwendungsrechnung", 2),
  MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL("mittelverwendung-saldo-subtitel",
      "Mittelverwendung Saldoreport Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  MITTELVERWENDUNGSSALDO_TITEL("mittelverwendungssaldo-titel",
      "Mittelverwendungssaldo Titel", "Mittelverwendungssaldo", 2),
  MITTELVERWENDUNGSSALDO_SUBTITEL("mittelverwendungssaldo-subtitel",
      "Mittelverwendungssaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      2),
  WIRTSCHAFTSPLAN_TITEL("wirtschaftsplan-titel", "Wirtschaftsplan Titel",
      "Wirtschaftsplan", 2),
  WIRTSCHAFTSPLAN_SUBTITEL("wirtschaftsplan-subtitel",
      "Wirtschaftsplan Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_bis_f))",
      2),
  WIRTSCHAFTSPLAN_MEHRERE_TITEL("wirtschaftsplan-mehrere-titel",
      "Wirtschaftsplan Titel (Mehrere Pläne)", "Wirtschaftsplan", 2),
  WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL("wirtschaftsplan-mehrere-subtitel",
      "Wirtschaftsplan Subtitel (Mehrere Pläne)", "", 2),
  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL("abrechnungslaufliste-titel",
      "Abrechnungslauf Sollbuchungen Titel", "Abrechnungslauf", 2),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL("abrechnungslaufliste-subtitel",
      "Abrechnungslauf Sollbuchungen Subtitel",
      "Nr. $parameter_lauf zum $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_f))",
      2),
  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_TITEL("auswertung-mitglied-titel",
      "Auswertung Mitglied Titel", "Mitglieder", 2),
  AUSWERTUNG_NICHT_MITGLIED_TITEL("auswertung-nichtmitglied-titel",
      "Auswertung Nicht-Mitglied Titel", "$filter_mitgliedstyp", 2),
  AUSWERTUNG_ALTERSJUBILARE_TITEL("auswertung-altersjubilare-titel",
      "Auswertung Altersjubiläen Titel", "Altersjubilare $filter_jahr", 2),
  AUSWERTUNG_ALTERSJUBILARE_SUBTITEL("auswertung-altersjubilare-subtitel",
      "Auswertung Altersjubiläen Subtitel", "", 2),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL(
      "auswertung-mitgliedsschaftsjubilare-titel",
      "Auswertung Mitgliedschaftsjubiläen Titel",
      "Mitgliedschaftsjubilare $filter_jahr", 2),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL(
      "auswertung-mitgliedsschaftsjubilare-subtitel",
      "Auswertung Mitgliedschaftsjubiläen Subtitel", "", 2),
  AUSWERTUNG_KURSTEILNEHMER_TITEL("auswertung-kursteilnehmer-titel",
      "Auswertung Kursteilnehmer Titel", "Kursteilnehmer", 2),
  AUSWERTUNG_KURSTEILNEHMER_SUBTITEL("auswertung-kursteilnehmer-subtitel",
      "Auswertung Kursteilnehmer Subtitel", "", 2),
  AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL("auswertung-mitgliederstatistik-titel",
      "Auswertung Mitgliederstatistik Titel", "Mitgliederstatistik", 2),
  AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL(
      "auswertung-mitgliederstatistik-subtitel",
      "Auswertung Mitgliederstatistik Subtitel",
      "Stichtag: $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_stichtag_f))",
      2),
  AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL("auswertung-jahrgangsstatistik-titel",
      "Auswertung Jahrgangsstatistik Titel", "Jahrgangsstatistik", 2),
  AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL(
      "auswertung-jahrgangsstatistik-subtitel",
      "Auswertung Jahrgangsstatistik Subtitel", "Jahr: $filter_jahr", 2),
  AUSWERTUNG_ARBEITSEINSAETZE_TITEL("auswertung-arbeitseinsaetze-titel",
      "Auswertung Arbeitseinsätze Titel", "Arbeitseinsätze $filter_jahr", 2),
  AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL("auswertung-arbeitseinsaetze-subtitel",
      "Auswertung Arbeitseinsätze Subtitel", "$filter_auswertung", 2),
  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_TITEL("buchungsarten-titel", "Buchungsarten Titel",
      "Buchungsarten", 2),
  BUCHUNGSARTEN_SUBTITEL("buchungsarten-subtitel", "Buchungsarten Subtitel", "",
      2);

  private final String text;

  private final String key;

  private final String defaultValue;

  private final int artKey;

  VorlageTyp(String key, String text, String defaultValue, int artKey)
  {
    this.key = key;
    this.text = text;
    this.defaultValue = defaultValue;
    this.artKey = artKey;
  }

  public String getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public String getDefault()
  {
    return defaultValue;
  }

  public int getArtkey()
  {
    return artKey;
  }

  public static VorlageTyp getByKey(String key)
  {
    for (VorlageTyp art : VorlageTyp.values())
    {
      if (art.getKey().matches(key))
      {
        return art;
      }
    }
    return null;
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
