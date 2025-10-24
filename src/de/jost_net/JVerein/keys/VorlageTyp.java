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
      "Spendenbescheinigung Dateiname"),
  SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME(
      "spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-Mitglied Dateiname"),
  RECHNUNG_DATEINAME("rechnung-dateiname", "Rechnung Dateiname"),
  RECHNUNG_MITGLIED_DATEINAME("rechnung-mitglied-dateiname",
      "Rechnung-Mitglied Dateiname"),
  MAHNUNG_DATEINAME("mahnung-dateiname", "Mahnung Dateiname"),
  MAHNUNG_MITGLIED_DATEINAME("mahnung-mitglied-dateiname",
      "Mahnung-Mitglied Dateiname"),
  KONTOAUSZUG_DATEINAME("kontoauszug-dateiname", "Kontoauszug Dateiname"),
  KONTOAUSZUG_MITGLIED_DATEINAME("kontoauszug-mitglied-dateiname",
      "Kontoauszug-Mitglied Dateiname"),
  FREIES_FORMULAR_DATEINAME("freies-formular-dateiname",
      "Freies Formular Dateiname"),
  FREIES_FORMULAR_MITGLIED_DATEINAME("freies-formular-mitglied-dateiname",
      "Freies Formular-Mitglied Dateiname"),
  CT1_AUSGABE_DATEINAME("1ct-ausgabe-dateiname", "1ct Ausgabe Dateiname"),
  PRENOTIFICATION_DATEINAME("pre-notification-dateiname",
      "Pre-Notification Dateiname"),
  PRENOTIFICATION_MITGLIED_DATEINAME("pre-notification-mitglied-dateiname",
      "Pre-Notification-Mitglied Dateiname"),
  // Reports aus Mitglieder
  PERSONALBOGEN_DATEINAME("personalbogen-dateiname", "Personalbogen Dateiname"),
  PERSONALBOGEN_MITGLIED_DATEINAME("personalbogen-mitglied-dateiname",
      "Personalbogen-Mitglied Dateiname"),
  VCARD_DATEINAME("vcard-dateiname", "VCard Dateiname"),
  VCARD_MITGLIED_DATEINAME("vcard-mitglied-dateiname",
      "VCARD-Mitglied Dateiname"),
  SOLLBUCHUNGEN_DATEINAME("sollbuchungen-dateiname",
      "Sollbuchungen Liste Dateiname"),
  SPENDENBESCHEINIGUNGEN_DATEINAME("spendenbescheinigungen-dateiname",
      "Spendenbescheinigungen Liste Dateiname"),
  ZUSATZBETRAEGE_DATEINAME("zusatzbetraege-dateiname",
      "Zusatzbeträge Liste Dateiname"),
  // Reports aus Buchführung
  KONTENSALDO_DATEINAME("kontensaldo-dateiname", "Kontensaldo Dateiname"),
  BUCHUNGSJOURNAL_DATEINAME("buchungsjournal-dateiname",
      "Buchungsjournal Dateiname"),
  EINZELBUCHUNGEN_DATEINAME("einzelbuchungen-dateiname",
      "Einzelbuchungen Dateiname"),
  CSVBUCHUNGEN_DATEINAME("csvbuchungen-dateiname",
      "CSV Einzelbuchungen Dateiname"),
  SUMMENBUCHUNGEN_DATEINAME("summenbuchungen-dateiname",
      "Summenbuchungen Dateiname"),
  ANLAGEN_BUCHUNGSJOURNAL_DATEINAME("anlagen-buchungsjournal-dateiname",
      "Anlagen Buchungsjournal Dateiname"),
  ANLAGEN_EINZELBUCHUNGEN_DATEINAME("anlagen-einzelbuchungen-dateiname",
      "Anlagen Einzelbuchungen Dateiname"),
  ANLAGEN_CSVBUCHUNGEN_DATEINAME("anlagen-csvbuchungen-dateiname",
      "Anlagen CSV Einzelbuchungen Dateiname"),
  ANLAGEN_SUMMENBUCHUNGEN_DATEINAME("anlagen-summenbuchungen-dateiname",
      "Anlagen Summenbuchungen Dateiname"),
  BUCHUNGSKLASSENSALDO_DATEINAME("buchungsklassensaldo-dateiname",
      "Buchungsklassensaldo Dateiname"),
  UMSATZSTEUER_VORANMELDUNG_DATEINAME("umsatzsteuervoranmeldung-dateiname",
      "Umsatzsteuer Voranmeldung Dateiname"),
  PROJEKTSALDO_DATEINAME("projektsaldo-dateiname", "Projektsaldo Dateiname"),
  ANLAGENVERZEICHNIS_DATEINAME("anlagenverzeichnis-dateiname",
      "Anlagenverzeichnis Dateiname"),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME(
      "mittelverwendung-zufluss-dateiname",
      "Mittelverwendung Zuflussreport Dateiname"),
  MITTELVERWENDUNGSREPORT_SALDO_DATEINAME("mittelverwendung-saldo-dateiname",
      "Mittelverwendung Saldoreport Dateiname"),
  MITTELVERWENDUNGSSALDO_DATEINAME("mittelverwendungssaldo-dateiname",
      "Mittelverwendungssaldo Dateiname"),
  WIRTSCHAFTSPLAN_DATEINAME("wirtschaftsplan-dateiname",
      "Wirtschaftsplan Dateiname"),
  WIRTSCHAFTSPLAN_MEHRERE_DATEINAME("wirtschaftsplan-mehrere-dateiname",
      "Wirtschaftsplan Dateiname (Mehrere Pläne)"),
  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME(
      "abrechnungslauf-lastschriften-dateiname",
      "Abrechnungslauf Lastschriften Dateiname"),
  ABRECHNUNGSLAUF_SEPA_DATEINAME("abrechnungslauf-sepa-xml-dateiname",
      "Abrechnungslauf SEPA XML Lastschriften Dateiname"),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME("abrechnungslaufliste-dateiname",
      "Abrechnungslauf Sollbuchungen Dateiname"),
  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_DATEINAME("auswertung-mitglied-dateiname",
      "Auswertung Mitglied Dateiname"),
  AUSWERTUNG_NICHT_MITGLIED_DATEINAME("auswertung-nichtmitglied-dateiname",
      "Auswertung Nicht-Mitglied Dateiname"),
  AUSWERTUNG_ALTERSJUBILARE_DATEINAME("auswertung-altersjubilare-dateiname",
      "Auswertung Altersjubiläen Dateiname"),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME(
      "auswertung-mitgliedsschaftsjubilare-dateiname",
      "Auswertung Mitgliedschaftsjubiläen Dateiname"),
  AUSWERTUNG_KURSTEILNEHMER_DATEINAME("auswertung-kursteilnehmer-dateiname",
      "Auswertung Kursteilnehmer Dateiname"),
  AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME(
      "auswertung-mitgliederstatistik-dateiname",
      "Auswertung Mitgliederstatistik Dateiname"),
  AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME(
      "auswertung-jahrgangsstatistik-dateiname",
      "Auswertung Jahrgangsstatistik Dateiname"),
  AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME("auswertung-arbeitseinsaetze-dateiname",
      "Auswertung Arbeitseinsätze Dateiname"),
  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_DATEINAME("buchungsarten-dateiname", "Buchungsarten Dateiname"),
  KONTENRAHMEN_DATEINAME_V1("kontenrahmen-v1-dateiname",
      "Kontenrahmen Version1 Dateiname"),
  KONTENRAHMEN_DATEINAME_V2("kontenrahmen-v2-dateiname",
      "Kontenrahmen Version2 Dateiname"),
  FORMULAR_DATEINAME("formular-dateiname", "Formular Dateiname"),
  FORMULARFELDER_DATEINAME("formularfelder-dateiname",
      "Formularfelder Dateiname"),

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
