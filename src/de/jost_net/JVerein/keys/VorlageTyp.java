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
      "Spendenbescheinigung Dateiname",
      "Spendenbescheinigung-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME(
      "spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-Mitglied Dateiname",
      "Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$spendenbescheinigung_zeile2",
      Vorlageart.DATEINAME.getKey()),
  RECHNUNG_DATEINAME("rechnung-dateiname", "Rechnung Dateiname",
      "Rechnung-$aktuellesdatum-$aktuellezeit", Vorlageart.DATEINAME.getKey()),
  RECHNUNG_MITGLIED_DATEINAME("rechnung-mitglied-dateiname",
      "Rechnung-Mitglied Dateiname",
      "Rechnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname",
      Vorlageart.DATEINAME.getKey()),
  MAHNUNG_DATEINAME("mahnung-dateiname", "Mahnung Dateiname",
      "Mahnung-$aktuellesdatum-$aktuellezeit", Vorlageart.DATEINAME.getKey()),
  MAHNUNG_MITGLIED_DATEINAME("mahnung-mitglied-dateiname",
      "Mahnung-Mitglied Dateiname",
      "Mahnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname",
      Vorlageart.DATEINAME.getKey()),
  KONTOAUSZUG_DATEINAME("kontoauszug-dateiname", "Kontoauszug Dateiname",
      "Kontoauszug-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  KONTOAUSZUG_MITGLIED_DATEINAME("kontoauszug-mitglied-dateiname",
      "Kontoauszug-Mitglied Dateiname",
      "Kontoauszug-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  FREIES_FORMULAR_DATEINAME("freies-formular-dateiname",
      "FreiesFormular Dateiname",
      "$formular_name-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  FREIES_FORMULAR_MITGLIED_DATEINAME("freies-formular-mitglied-dateiname",
      "FreiesFormular-Mitglied Dateiname",
      "$formular_name-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  CT1_AUSGABE_DATEINAME("1ct-ausgabe-dateiname", "1ctAusgabe Dateiname",
      "1ctueberweisung-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  GUTSCHRIFT_DATEINAME("gutschrift-dateiname", "Gutschrift Dateiname",
      "Gutschrift-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  PRENOTIFICATION_DATEINAME("pre-notification-dateiname",
      "Pre-Notification Dateiname",
      "Prenotification-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  PRENOTIFICATION_MITGLIED_DATEINAME("pre-notification-mitglied-dateiname",
      "Pre-Notification-Mitglied Dateiname",
      "Prenotification-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  PRENOTIFICATION_KURSTEILNEHMER_DATEINAME(
      "pre-notification-kursteilnehmer-dateiname",
      "Pre-Notification-Kursteilnehmer Dateiname",
      "Prenotification-$lastschrift_name-$lastschrift_vorname-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),

  // Reports aus Mitglieder
  PERSONALBOGEN_DATEINAME("personalbogen-dateiname", "Personalbogen Dateiname",
      "Personalbogen-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  PERSONALBOGEN_MITGLIED_DATEINAME("personalbogen-mitglied-dateiname",
      "Personalbogen-Mitglied Dateiname",
      "Personalbogen-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  VCARD_DATEINAME("vcard-dateiname", "VCard Dateiname",
      "VCards-$aktuellesdatum-$aktuellezeit", Vorlageart.DATEINAME.getKey()),
  VCARD_MITGLIED_DATEINAME("vcard-mitglied-dateiname",
      "VCARD-Mitglied Dateiname",
      "VCard-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  SOLLBUCHUNGEN_DATEINAME("sollbuchungen-dateiname",
      "Sollbuchungen Liste Dateiname",
      "Sollbuchungen-Differenz-$filter_differenz-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  SPENDENBESCHEINIGUNGEN_DATEINAME("spendenbescheinigungen-dateiname",
      "Spendenbescheinigungen Liste Dateiname",
      "Spendenbescheinigungen-$filter_spendeart-$aktuellesdatum",
      Vorlageart.DATEINAME.getKey()),
  ZUSATZBETRAEGE_DATEINAME("zusatzbetraege-dateiname",
      "Zusatzbeträge Liste Dateiname",
      "Zusatzbetraege-$filter_ausfuehrungstag-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),

  // Reports aus Buchführung
  KONTENSALDO_DATEINAME("kontensaldo-dateiname", "Kontensaldo Dateiname",
      "Kontensdaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  BUCHUNGSJOURNAL_DATEINAME("buchungsjournal-dateiname",
      "Buchungsjournal Dateiname",
      "Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  EINZELBUCHUNGEN_DATEINAME("einzelbuchungen-dateiname",
      "Einzelbuchungen Dateiname",
      "Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  CSVBUCHUNGEN_DATEINAME("csvbuchungen-dateiname",
      "CSVEinzelbuchungen Dateiname",
      "Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  SUMMENBUCHUNGEN_DATEINAME("summenbuchungen-dateiname",
      "Summenbuchungen Dateiname",
      "Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ANLAGEN_BUCHUNGSJOURNAL_DATEINAME("anlagen-buchungsjournal-dateiname",
      "Anlagen Buchungsjournal Dateiname",
      "Anlagen-Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ANLAGEN_EINZELBUCHUNGEN_DATEINAME("anlagen-einzelbuchungen-dateiname",
      "Anlagen Einzelbuchungen Dateiname",
      "Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ANLAGEN_CSVBUCHUNGEN_DATEINAME("anlagen-csvbuchungen-dateiname",
      "AnlagenCSVEinzelbuchungen Dateiname",
      "Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ANLAGEN_SUMMENBUCHUNGEN_DATEINAME("anlagen-summenbuchungen-dateiname",
      "Anlagen Summenbuchungen Dateiname",
      "Anlagen-Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  BUCHUNGSKLASSENSALDO_DATEINAME("buchungsklassensaldo-dateiname",
      "Buchungsklassensaldo Dateiname",
      "Buchungsklassensaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  UMSATZSTEUER_VORANMELDUNG_DATEINAME("umsatzsteuervoranmeldung-dateiname",
      "Umsatzsteuer Voranmeldung Dateiname",
      "Umsatzsteuer-Voranmeldung-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  PROJEKTSALDO_DATEINAME("projektsaldo-dateiname", "Projektsaldo Dateiname",
      "Projektsaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ANLAGENVERZEICHNIS_DATEINAME("anlagenverzeichnis-dateiname",
      "Anlagenverzeichnis Dateiname",
      "Anlagenverzeichnis-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME(
      "mittelverwendung-zufluss-dateiname",
      "Mittelverwendung Zuflussreport Dateiname",
      "Mittelverwendung-Zufluss-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  MITTELVERWENDUNGSREPORT_SALDO_DATEINAME("mittelverwendung-saldo-dateiname",
      "Mittelverwendung Saldoreport Dateiname",
      "Mittelverwendung-Saldo-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  MITTELVERWENDUNGSSALDO_DATEINAME("mittelverwendungssaldo-dateiname",
      "Mittelverwendungssaldo Dateiname",
      "Mittelverwendungssaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  WIRTSCHAFTSPLAN_DATEINAME("wirtschaftsplan-dateiname",
      "Wirtschaftsplan Dateiname",
      "Wirtschaftsplan-$parameter_bezeichnung-$parameter_datum_von_f-$parameter_datum_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  WIRTSCHAFTSPLAN_MEHRERE_DATEINAME("wirtschaftsplan-mehrere-dateiname",
      "Wirtschaftsplan Dateiname (Mehrere Pläne)",
      "Wirtschaftsplan-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),

  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME(
      "abrechnungslauf-lastschriften-dateiname",
      "Abrechnungslauf Lastschriften Dateiname",
      "Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ABRECHNUNGSLAUF_SEPA_DATEINAME("abrechnungslauf-sepa-xml-dateiname",
      "Abrechnungslauf SEPAXMLLastschriften Dateiname",
      "SEPA-Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME("abrechnungslaufliste-dateiname",
      "Abrechnungslauf Sollbuchungen Dateiname",
      "Abrechnungslaufliste-$parameter_lauf-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),

  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_DATEINAME("auswertung-mitglied-dateiname",
      "Auswertung Mitglied Dateiname",
      "Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_NICHT_MITGLIED_DATEINAME("auswertung-nichtmitglied-dateiname",
      "Auswertung Nicht-Mitglied Dateiname",
      "Nicht-Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_ALTERSJUBILARE_DATEINAME("auswertung-altersjubilare-dateiname",
      "Auswertung Altersjubiläen Dateiname",
      "Altersjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME(
      "auswertung-mitgliedsschaftsjubilare-dateiname",
      "Auswertung Mitgliedschaftsjubiläen Dateiname",
      "Mitgliedschaftsjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_KURSTEILNEHMER_DATEINAME("auswertung-kursteilnehmer-dateiname",
      "Auswertung Kursteilnehmer Dateiname",
      "Kursteilnehmer-$filter_abbuchung_von_f-$filter_abbuchung_bis_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME(
      "auswertung-mitgliederstatistik-dateiname",
      "Auswertung Mitgliederstatistik Dateiname",
      "Mitgliederstatistik-$filter_stichtag_f-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME(
      "auswertung-jahrgangsstatistik-dateiname",
      "Auswertung Jahrgangsstatistik Dateiname",
      "Jahrgangsstatistik-$filter_jahr-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME("auswertung-arbeitseinsaetze-dateiname",
      "Auswertung Arbeitseinsätze Dateiname",
      "Arbeitseinsaetze-$filter_jahr-$filter_auswertung-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),

  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_DATEINAME("buchungsarten-dateiname", "Buchungsarten Dateiname",
      "Buchungsarten-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  KONTENRAHMEN_DATEINAME_V1("kontenrahmen-v1-dateiname",
      "Kontenrahmen Version1 Dateiname",
      "Kontenrahmen-v1-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  KONTENRAHMEN_DATEINAME_V2("kontenrahmen-v2-dateiname",
      "Kontenrahmen Version2 Dateiname",
      "Kontenrahmen-v2-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  FORMULAR_DATEINAME("formular-dateiname", "Formular Dateiname",
      "Formular-$formular_name-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),
  FORMULARFELDER_DATEINAME("formularfelder-dateiname",
      "Formularfelder Dateiname",
      "Formularfelder-$formular_name-$aktuellesdatum-$aktuellezeit",
      Vorlageart.DATEINAME.getKey()),

  // Titel
  KONTOAUSZUG_TITEL("kontoauszug-titel", "Kontoauszug Titel", "$verein_name",
      Vorlageart.TITEL.getKey()),
  KONTOAUSZUG_SUBTITEL("kontoauszug-subtitel", "Kontoauszug Subtitel",
      "Kontoauszug $mitglied_vornamename, Stand: $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$aktuellesdatum))",
      Vorlageart.TITEL.getKey()),
  // Reports aus Mitglieder
  PERSONALBOGEN_TITEL("personalbogen-titel", "Personalbogen Titel",
      "Personalbogen $mitglied_name $mitglied_vorname",
      Vorlageart.TITEL.getKey()),
  PERSONALBOGEN_SUBTITEL("personalbogen-subtitel", "Personalbogen Subtitel", "",
      Vorlageart.TITEL.getKey()),
  SPENDENBESCHEINIGUNGEN_TITEL("spendenbescheinigungen-titel",
      "Spendenbescheinigungen Liste Titel", "Spendenbescheinigungen",
      Vorlageart.TITEL.getKey()),
  SPENDENBESCHEINIGUNGEN_SUBTITEL("spendenbescheinigungen-subtitel",
      "Spendenbescheinigungen Liste Subtitel", "", Vorlageart.TITEL.getKey()),
  ZUSATZBETRAEGE_TITEL("zusatzbetraege-titel", "Zusatzbeträge Liste Titel",
      "Zusatzbeträge", Vorlageart.TITEL.getKey()),
  ZUSATZBETRAEGE_SUBTITEL("zusatzbetraege-subtitel",
      "Zusatzbeträge Liste Subtitel", "", Vorlageart.TITEL.getKey()),
  // Reports aus Buchführung
  KONTENSALDO_TITEL("kontensaldo-titel", "Kontensaldo Titel", "Kontensaldo",
      Vorlageart.TITEL.getKey()),
  KONTENSALDO_SUBTITEL("kontensaldo-subtitel", "Kontensaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  BUCHUNGSJOURNAL_TITEL("buchungsjournal-titel", "Buchungsjournal Titel",
      "Buchungsjournal", Vorlageart.TITEL.getKey()),
  BUCHUNGSJOURNAL_SUBTITEL("buchungsjournal-subtitel",
      "Buchungsjournal Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  EINZELBUCHUNGEN_TITEL("einzelbuchungen-titel", "Einzelbuchungen Titel",
      "Buchungsliste", Vorlageart.TITEL.getKey()),
  EINZELBUCHUNGEN_SUBTITEL("einzelbuchungen-subtitel",
      "Einzelbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  SUMMENBUCHUNGEN_TITEL("summenbuchungen-titel", "Summenbuchungen Titel",
      "Summenliste", Vorlageart.TITEL.getKey()),
  SUMMENBUCHUNGEN_SUBTITEL("summenbuchungen-subtitel",
      "Summenbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  ANLAGEN_BUCHUNGSJOURNAL_TITEL("anlagen-buchungsjournal-titel",
      "Anlagen Buchungsjournal Titel", "Buchungsjournal",
      Vorlageart.TITEL.getKey()),
  ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL("anlagen-buchungsjournal-subtitel",
      "Anlagen Buchungsjournal Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  ANLAGEN_EINZELBUCHUNGEN_TITEL("anlagen-einzelbuchungen-titel",
      "Anlagen Einzelbuchungen Titel", "Buchungsliste",
      Vorlageart.TITEL.getKey()),
  ANLAGEN_EINZELBUCHUNGEN_SUBTITEL("anlagen-einzelbuchungen-subtitel",
      "Anlagen Einzelbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  ANLAGEN_SUMMENBUCHUNGEN_TITEL("anlagen-summenbuchungen-titel",
      "Anlagen Summenbuchungen Titel", "Summenliste",
      Vorlageart.TITEL.getKey()),
  ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL("anlagen-summenbuchungen-subtitel",
      "Anlagen Summenbuchungen Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  BUCHUNGSKLASSENSALDO_TITEL("buchungsklassensaldo-titel",
      "Buchungsklassensaldo Titel", "Buchungsklassensaldo",
      Vorlageart.TITEL.getKey()),
  BUCHUNGSKLASSENSALDO_SUBTITEL("buchungsklassensaldo-subtitel",
      "Buchungsklassensaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  UMSATZSTEUER_VORANMELDUNG_TITEL("umsatzsteuervoranmeldung-titel",
      "Umsatzsteuer Voranmeldung Titel", "Umsatzsteuer Voranmeldung",
      Vorlageart.TITEL.getKey()),
  UMSATZSTEUER_VORANMELDUNG_SUBTITEL("umsatzsteuervoranmeldung-subtitel",
      "Umsatzsteuer Voranmeldung Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  PROJEKTSALDO_TITEL("projektsaldo-titel", "Projektsaldo Titel", "Projektsaldo",
      Vorlageart.TITEL.getKey()),
  PROJEKTSALDO_SUBTITEL("projektsaldo-subtitel", "Projektsaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  ANLAGENVERZEICHNIS_TITEL("anlagenverzeichnis-titel",
      "Anlagenverzeichnis Titel", "Anlagenverzeichnis",
      Vorlageart.TITEL.getKey()),
  ANLAGENVERZEICHNIS_SUBTITEL("anlagenverzeichnis-subtitel",
      "Anlagenverzeichnis Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL("mittelverwendung-zufluss-titel",
      "Mittelverwendung Zuflussreport Titel", "Mittelverwendungsrechnung",
      Vorlageart.TITEL.getKey()),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL("mittelverwendung-zufluss-subtitel",
      "Mittelverwendung Zuflussreport Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  MITTELVERWENDUNGSREPORT_SALDO_TITEL("mittelverwendung-saldo-titel",
      "Mittelverwendung Saldoreport Titel", "Mittelverwendungsrechnung",
      Vorlageart.TITEL.getKey()),
  MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL("mittelverwendung-saldo-subtitel",
      "Mittelverwendung Saldoreport Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  MITTELVERWENDUNGSSALDO_TITEL("mittelverwendungssaldo-titel",
      "Mittelverwendungssaldo Titel", "Mittelverwendungssaldo",
      Vorlageart.TITEL.getKey()),
  MITTELVERWENDUNGSSALDO_SUBTITEL("mittelverwendungssaldo-subtitel",
      "Mittelverwendungssaldo Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$zeitraum_bis_f))",
      Vorlageart.TITEL.getKey()),
  WIRTSCHAFTSPLAN_TITEL("wirtschaftsplan-titel", "Wirtschaftsplan Titel",
      "Wirtschaftsplan", Vorlageart.TITEL.getKey()),
  WIRTSCHAFTSPLAN_SUBTITEL("wirtschaftsplan-subtitel",
      "Wirtschaftsplan Subtitel",
      "$udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_von_f)) - $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_bis_f))",
      Vorlageart.TITEL.getKey()),
  WIRTSCHAFTSPLAN_MEHRERE_TITEL("wirtschaftsplan-mehrere-titel",
      "Wirtschaftsplan Titel (Mehrere Pläne)", "Wirtschaftsplan",
      Vorlageart.TITEL.getKey()),
  WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL("wirtschaftsplan-mehrere-subtitel",
      "Wirtschaftsplan Subtitel (Mehrere Pläne)", "",
      Vorlageart.TITEL.getKey()),
  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL("abrechnungslaufliste-titel",
      "Abrechnungslauf Sollbuchungen Titel", "Abrechnungslauf",
      Vorlageart.TITEL.getKey()),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL("abrechnungslaufliste-subtitel",
      "Abrechnungslauf Sollbuchungen Subtitel",
      "Nr. $parameter_lauf zum $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$parameter_datum_f))",
      Vorlageart.TITEL.getKey()),
  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_TITEL("auswertung-mitglied-titel",
      "Auswertung Mitglied Titel", "Mitglieder", Vorlageart.TITEL.getKey()),
  AUSWERTUNG_NICHT_MITGLIED_TITEL("auswertung-nichtmitglied-titel",
      "Auswertung Nicht-Mitglied Titel", "$filter_mitgliedstyp",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_ALTERSJUBILARE_TITEL("auswertung-altersjubilare-titel",
      "Auswertung Altersjubiläen Titel", "Altersjubilare $filter_jahr",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_ALTERSJUBILARE_SUBTITEL("auswertung-altersjubilare-subtitel",
      "Auswertung Altersjubiläen Subtitel", "", Vorlageart.TITEL.getKey()),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL(
      "auswertung-mitgliedsschaftsjubilare-titel",
      "Auswertung Mitgliedschaftsjubiläen Titel",
      "Mitgliedschaftsjubilare $filter_jahr", Vorlageart.TITEL.getKey()),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL(
      "auswertung-mitgliedsschaftsjubilare-subtitel",
      "Auswertung Mitgliedschaftsjubiläen Subtitel", "",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_KURSTEILNEHMER_TITEL("auswertung-kursteilnehmer-titel",
      "Auswertung Kursteilnehmer Titel", "Kursteilnehmer",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_KURSTEILNEHMER_SUBTITEL("auswertung-kursteilnehmer-subtitel",
      "Auswertung Kursteilnehmer Subtitel", "", Vorlageart.TITEL.getKey()),
  AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL("auswertung-mitgliederstatistik-titel",
      "Auswertung Mitgliederstatistik Titel", "Mitgliederstatistik",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL(
      "auswertung-mitgliederstatistik-subtitel",
      "Auswertung Mitgliederstatistik Subtitel",
      "Stichtag: $udateformat.format(\"dd.MM.yyyy\",$udateformat.parse(\"yyyyMMdd\",$filter_stichtag_f))",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL("auswertung-jahrgangsstatistik-titel",
      "Auswertung Jahrgangsstatistik Titel", "Jahrgangsstatistik",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL(
      "auswertung-jahrgangsstatistik-subtitel",
      "Auswertung Jahrgangsstatistik Subtitel", "Jahr: $filter_jahr",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_ARBEITSEINSAETZE_TITEL("auswertung-arbeitseinsaetze-titel",
      "Auswertung Arbeitseinsätze Titel", "Arbeitseinsätze $filter_jahr",
      Vorlageart.TITEL.getKey()),
  AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL("auswertung-arbeitseinsaetze-subtitel",
      "Auswertung Arbeitseinsätze Subtitel", "$filter_auswertung",
      Vorlageart.TITEL.getKey()),
  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_TITEL("buchungsarten-titel", "Buchungsarten Titel",
      "Buchungsarten", Vorlageart.TITEL.getKey()),
  BUCHUNGSARTEN_SUBTITEL("buchungsarten-subtitel", "Buchungsarten Subtitel", "",
      Vorlageart.TITEL.getKey());

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
