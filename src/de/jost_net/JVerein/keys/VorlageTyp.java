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
  SPENDENBESCHEINIGUNG_DATEINAME("spendenbescheinigung-dateiname",
      "Spendenbescheinigung Dateiname", ""),
  SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME(
      "spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-Mitglied Dateiname", ""),
  RECHNUNG_DATEINAME("rechnung-dateiname", "Rechnung Dateiname", ""),
  RECHNUNG_MITGLIED_DATEINAME("rechnung-mitglied-dateiname",
      "Rechnung-Mitglied Dateiname", ""),
  MAHNUNG_DATEINAME("mahnung-dateiname", "Mahnung Dateiname", ""),
  MAHNUNG_MITGLIED_DATEINAME("mahnung-mitglied-dateiname",
      "Mahnung-Mitglied Dateiname", ""),
  KONTOAUSZUG_DATEINAME("kontoauszug-dateiname", "Kontoauszug Dateiname", ""),
  KONTOAUSZUG_MITGLIED_DATEINAME("kontoauszug-mitglied-dateiname",
      "Kontoauszug-Mitglied Dateiname", ""),
  FREIES_FORMULAR_DATEINAME("freies-formular-dateiname",
      "Freies Formular Dateiname", ""),
  FREIES_FORMULAR_MITGLIED_DATEINAME("freies-formular-mitglied-dateiname",
      "Freies Formular-Mitglied Dateiname", ""),
  CT1_AUSGABE_DATEINAME("1ct-ausgabe-dateiname", "1ct Ausgabe Dateiname", ""),
  PRENOTIFICATION_DATEINAME("pre-notification-dateiname",
      "Pre-Notification Dateiname", ""),
  PRENOTIFICATION_MITGLIED_DATEINAME("pre-notification-mitglied-dateiname",
      "Pre-Notification-Mitglied Dateiname", ""),
  // Reports aus Mitglieder
  PERSONALBOGEN_DATEINAME("personalbogen-dateiname", "Personalbogen Dateiname",
      ""),
  PERSONALBOGEN_MITGLIED_DATEINAME("personalbogen-mitglied-dateiname",
      "Personalbogen-Mitglied Dateiname", ""),
  VCARD_DATEINAME("vcard-dateiname", "VCard Dateiname", ""),
  VCARD_MITGLIED_DATEINAME("vcard-mitglied-dateiname",
      "VCARD-Mitglied Dateiname", ""),
  SOLLBUCHUNGEN_DATEINAME("sollbuchungen-dateiname",
      "Sollbuchungen Liste Dateiname", ""),
  SPENDENBESCHEINIGUNGEN_DATEINAME("spendenbescheinigungen-dateiname",
      "Spendenbescheinigungen Liste Dateiname", ""),
  ZUSATZBETRAEGE_DATEINAME("zusatzbetraege-dateiname",
      "Zusatzbeträge Liste Dateiname", ""),
  // Reports aus Buchführung
  KONTENSALDO_DATEINAME("kontensaldo-dateiname", "Kontensaldo Dateiname", ""),
  BUCHUNGSJOURNAL_DATEINAME("buchungsjournal-dateiname",
      "Buchungsjournal Dateiname", ""),
  EINZELBUCHUNGEN_DATEINAME("einzelbuchungen-dateiname",
      "Einzelbuchungen Dateiname", ""),
  CSVBUCHUNGEN_DATEINAME("csvbuchungen-dateiname",
      "CSV Einzelbuchungen Dateiname", ""),
  SUMMENBUCHUNGEN_DATEINAME("summenbuchungen-dateiname",
      "Summenbuchungen Dateiname", ""),
  ANLAGEN_BUCHUNGSJOURNAL_DATEINAME("anlagen-buchungsjournal-dateiname",
      "Anlagen Buchungsjournal Dateiname", ""),
  ANLAGEN_EINZELBUCHUNGEN_DATEINAME("anlagen-einzelbuchungen-dateiname",
      "Anlagen Einzelbuchungen Dateiname", ""),
  ANLAGEN_CSVBUCHUNGEN_DATEINAME("anlagen-csvbuchungen-dateiname",
      "Anlagen CSV Einzelbuchungen Dateiname", ""),
  ANLAGEN_SUMMENBUCHUNGEN_DATEINAME("anlagen-summenbuchungen-dateiname",
      "Anlagen Summenbuchungen Dateiname", ""),
  BUCHUNGSKLASSENSALDO_DATEINAME("buchungsklassensaldo-dateiname",
      "Buchungsklassensaldo Dateiname", ""),
  UMSATZSTEUER_VORANMELDUNG_DATEINAME("umsatzsteuervoranmeldung-dateiname",
      "Umsatzsteuer Voranmeldung Dateiname", ""),
  PROJEKTSALDO_DATEINAME("projektsaldo-dateiname", "Projektsaldo Dateiname",
      ""),
  ANLAGENVERZEICHNIS_DATEINAME("anlagenverzeichnis-dateiname",
      "Anlagenverzeichnis Dateiname", ""),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME(
      "mittelverwendung-zufluss-dateiname",
      "Mittelverwendung Zuflussreport Dateiname", ""),
  MITTELVERWENDUNGSREPORT_SALDO_DATEINAME("mittelverwendung-saldo-dateiname",
      "Mittelverwendung Saldoreport Dateiname", ""),
  MITTELVERWENDUNGSSALDO_DATEINAME("mittelverwendungssaldo-dateiname",
      "Mittelverwendungssaldo Dateiname", ""),
  WIRTSCHAFTSPLAN_DATEINAME("wirtschaftsplan-dateiname",
      "Wirtschaftsplan Dateiname", ""),
  WIRTSCHAFTSPLAN_MEHRERE_DATEINAME("wirtschaftsplan-mehrere-dateiname",
      "Wirtschaftsplan Dateiname (Mehrere Pläne)", ""),
  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME(
      "abrechnungslauf-lastschriften-dateiname",
      "Abrechnungslauf Lastschriften Dateiname", ""),
  ABRECHNUNGSLAUF_SEPA_DATEINAME("abrechnungslauf-sepa-xml-dateiname",
      "Abrechnungslauf SEPA XML Lastschriften Dateiname", ""),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME("abrechnungslaufliste-dateiname",
      "Abrechnungslauf Sollbuchungen Dateiname", ""),
  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_DATEINAME("auswertung-mitglied-dateiname",
      "Auswertung Mitglied Dateiname", ""),
  AUSWERTUNG_NICHT_MITGLIED_DATEINAME("auswertung-nichtmitglied-dateiname",
      "Auswertung Nicht-Mitglied Dateiname", ""),
  AUSWERTUNG_ALTERSJUBILARE_DATEINAME("auswertung-altersjubilare-dateiname",
      "Auswertung Altersjubiläen Dateiname", ""),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME(
      "auswertung-mitgliedsschaftsjubilare-dateiname",
      "Auswertung Mitgliedschaftsjubiläen Dateiname", ""),
  AUSWERTUNG_KURSTEILNEHMER_DATEINAME("auswertung-kursteilnehmer-dateiname",
      "Auswertung Kursteilnehmer Dateiname", ""),
  AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME(
      "auswertung-mitgliederstatistik-dateiname",
      "Auswertung Mitgliederstatistik Dateiname", ""),
  AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME(
      "auswertung-jahrgangsstatistik-dateiname",
      "Auswertung Jahrgangsstatistik Dateiname", ""),
  AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME("auswertung-arbeitseinsaetze-dateiname",
      "Auswertung Arbeitseinsätze Dateiname", ""),
  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_DATEINAME("buchungsarten-dateiname", "Buchungsarten Dateiname",
      ""),
  KONTENRAHMEN_DATEINAME_V1("kontenrahmen-v1-dateiname",
      "Kontenrahmen Version1 Dateiname", ""),
  KONTENRAHMEN_DATEINAME_V2("kontenrahmen-v2-dateiname",
      "Kontenrahmen Version2 Dateiname", ""),
  FORMULAR_DATEINAME("formular-dateiname", "Formular Dateiname", ""),
  FORMULARFELDER_DATEINAME("formularfelder-dateiname",
      "Formularfelder Dateiname", "DEFAULT");

  private final String text;

  private final String key;

  private String defaultValue;

  VorlageTyp(String key, String text, String defaultValue)
  {
    this.key = key;
    this.text = text;
    this.defaultValue = defaultValue;
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
