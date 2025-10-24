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
  KONTOAUSZUG_TITEL("kontoauszug-titel", "Kontoauszug Titel"),
  KONTOAUSZUG_SUBTITEL("kontoauszug-subtitel", "Kontoauszug Subtitel"),
  // Reports aus Mitglieder
  PERSONALBOGEN_TITEL("personalbogen-titel", "Personalbogen Titel"),
  PERSONALBOGEN_SUBTITEL("personalbogen-subtitel", "Personalbogen Subtitel"),
  SPENDENBESCHEINIGUNGEN_TITEL("spendenbescheinigungen-titel",
      "Spendenbescheinigungen Liste Titel"),
  SPENDENBESCHEINIGUNGEN_SUBTITEL("spendenbescheinigungen-subtitel",
      "Spendenbescheinigungen Liste Subtitel"),
  ZUSATZBETRAEGE_TITEL("zusatzbetraege-titel", "Zusatzbeträge Liste Titel"),
  ZUSATZBETRAEGE_SUBTITEL("zusatzbetraege-subtitel",
      "Zusatzbeträge Liste Subtitel"),
  // Reports aus Buchführung
  KONTENSALDO_TITEL("kontensaldo-titel", "Kontensaldo Titel"),
  KONTENSALDO_SUBTITEL("kontensaldo-subtitel", "Kontensaldo Subtitel"),
  BUCHUNGSJOURNAL_TITEL("buchungsjournal-titel", "Buchungsjournal Titel"),
  BUCHUNGSJOURNAL_SUBTITEL("buchungsjournal-subtitel",
      "Buchungsjournal Subtitel"),
  EINZELBUCHUNGEN_TITEL("einzelbuchungen-titel", "Einzelbuchungen Titel"),
  EINZELBUCHUNGEN_SUBTITEL("einzelbuchungen-subtitel",
      "Einzelbuchungen Subtitel"),
  SUMMENBUCHUNGEN_TITEL("summenbuchungen-titel", "Summenbuchungen Titel"),
  SUMMENBUCHUNGEN_SUBTITEL("summenbuchungen-subtitel",
      "Summenbuchungen Subtitel"),
  ANLAGEN_BUCHUNGSJOURNAL_TITEL("anlagen-buchungsjournal-titel",
      "Anlagen Buchungsjournal Titel"),
  ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL("anlagen-buchungsjournal-subtitel",
      "Anlagen Buchungsjournal Subtitel"),
  ANLAGEN_EINZELBUCHUNGEN_TITEL("anlagen-einzelbuchungen-titel",
      "Anlagen Einzelbuchungen Titel"),
  ANLAGEN_EINZELBUCHUNGEN_SUBTITEL("anlagen-einzelbuchungen-subtitel",
      "Anlagen Einzelbuchungen Subtitel"),
  ANLAGEN_SUMMENBUCHUNGEN_TITEL("anlagen-summenbuchungen-titel",
      "Anlagen Summenbuchungen Titel"),
  ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL("anlagen-summenbuchungen-subtitel",
      "Anlagen Summenbuchungen Subtitel"),
  BUCHUNGSKLASSENSALDO_TITEL("buchungsklassensaldo-titel",
      "Buchungsklassensaldo Titel"),
  BUCHUNGSKLASSENSALDO_SUBTITEL("buchungsklassensaldo-subtitel",
      "Buchungsklassensaldo Subtitel"),
  UMSATZSTEUER_VORANMELDUNG_TITEL("umsatzsteuervoranmeldung-titel",
      "Umsatzsteuer Voranmeldung Titel"),
  UMSATZSTEUER_VORANMELDUNG_SUBTITEL("umsatzsteuervoranmeldung-subtitel",
      "Umsatzsteuer Voranmeldung Subtitel"),
  PROJEKTSALDO_TITEL("projektsaldo-titel", "Projektsaldo Titel"),
  PROJEKTSALDO_SUBTITEL("projektsaldo-subtitel", "Projektsaldo Subtitel"),
  ANLAGENVERZEICHNIS_TITEL("anlagenverzeichnis-titel",
      "Anlagenverzeichnis Titel"),
  ANLAGENVERZEICHNIS_SUBTITEL("anlagenverzeichnis-subtitel",
      "Anlagenverzeichnis Subtitel"),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL("mittelverwendung-zufluss-titel",
      "Mittelverwendung Zuflussreport Titel"),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL("mittelverwendung-zufluss-subtitel",
      "Mittelverwendung Zuflussreport Subtitel"),
  MITTELVERWENDUNGSREPORT_SALDO_TITEL("mittelverwendung-saldo-titel",
      "Mittelverwendung Saldoreport Titel"),
  MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL("mittelverwendung-saldo-subtitel",
      "Mittelverwendung Saldoreport Subtitel"),
  MITTELVERWENDUNGSSALDO_TITEL("mittelverwendungssaldo-titel",
      "Mittelverwendungssaldo Titel"),
  MITTELVERWENDUNGSSALDO_SUBTITEL("mittelverwendungssaldo-subtitel",
      "Mittelverwendungssaldo Subtitel"),
  WIRTSCHAFTSPLAN_TITEL("wirtschaftsplan-titel", "Wirtschaftsplan Titel"),
  WIRTSCHAFTSPLAN_SUBTITEL("wirtschaftsplan-subtitel",
      "Wirtschaftsplan Subtitel"),
  WIRTSCHAFTSPLAN_MEHRERE_TITEL("wirtschaftsplan-mehrere-titel",
      "Wirtschaftsplan Titel (Mehrere Pläne)"),
  WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL("wirtschaftsplan-mehrere-subtitel",
      "Wirtschaftsplan Subtitel (Mehrere Pläne)"),
  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL("abrechnungslaufliste-titel",
      "Abrechnungslauf Sollbuchungen Titel"),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL("abrechnungslaufliste-subtitel",
      "Abrechnungslauf Sollbuchungen Subtitel"),
  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_TITEL("auswertung-mitglied-titel",
      "Auswertung Mitglied Titel"),
  AUSWERTUNG_NICHT_MITGLIED_TITEL("auswertung-nichtmitglied-titel",
      "Auswertung Nicht-Mitglied Titel"),
  AUSWERTUNG_ALTERSJUBILARE_TITEL("auswertung-altersjubilare-titel",
      "Auswertung Altersjubiläen Titel"),
  AUSWERTUNG_ALTERSJUBILARE_SUBTITEL("auswertung-altersjubilare-subtitel",
      "Auswertung Altersjubiläen Subtitel"),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL(
      "auswertung-mitgliedsschaftsjubilare-titel",
      "Auswertung Mitgliedschaftsjubiläen Titel"),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL(
      "auswertung-mitgliedsschaftsjubilare-subtitel",
      "Auswertung Mitgliedschaftsjubiläen Subtitel"),
  AUSWERTUNG_KURSTEILNEHMER_TITEL("auswertung-kursteilnehmer-titel",
      "Auswertung Kursteilnehmer Titel"),
  AUSWERTUNG_KURSTEILNEHMER_SUBTITEL("auswertung-kursteilnehmer-subtitel",
      "Auswertung Kursteilnehmer Subtitel"),
  AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL("auswertung-mitgliederstatistik-titel",
      "Auswertung Mitgliederstatistik Titel"),
  AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL(
      "auswertung-mitgliederstatistik-subtitel",
      "Auswertung Mitgliederstatistik Subtitel"),
  AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL("auswertung-jahrgangsstatistik-titel",
      "Auswertung Jahrgangsstatistik Titel"),
  AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL(
      "auswertung-jahrgangsstatistik-subtitel",
      "Auswertung Jahrgangsstatistik Subtitel"),
  AUSWERTUNG_ARBEITSEINSAETZE_TITEL("auswertung-arbeitseinsaetze-titel",
      "Auswertung Arbeitseinsätze Titel"),
  AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL("auswertung-arbeitseinsaetze-subtitel",
      "Auswertung Arbeitseinsätze Subtitel"),
  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_TITEL("buchungsarten-titel", "Buchungsarten Titel"),
  BUCHUNGSARTEN_SUBTITEL("buchungsarten-subtitel", "Buchungsarten Subtitel");

  private final String text;

  private final String key;

  VorlageTyp(String key, String text)
  {
    this.key = key;
    this.text = text;
  }

  public String getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
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
