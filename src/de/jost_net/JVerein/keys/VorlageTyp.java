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
      "SpendenbescheinigungDateiname",
      "Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$spendenbescheinigung_zeile2"),
  SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME(
      "spendenbescheinigung-mitglied-dateiname",
      "Spendenbescheinigung-MitgliedDateiname",
      "Spendenbescheinigung-$spendenbescheinigung_spendedatum_erstes-$mitglied_name-$mitglied_vorname"),
  RECHNUNG_DATEINAME("rechnung-dateiname", "RechnungDateiname",
      "Rechnung-$aktuellesdatum-$aktuellezeit"),
  RECHNUNG_MITGLIED_DATEINAME("rechnung-mitglied-dateiname",
      "Rechnung-MitgliedDateiname",
      "Rechnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname"),
  MAHNUNG_DATEINAME("mahnung-dateiname", "MahnungDateiname",
      "Mahnung-$aktuellesdatum-$aktuellezeit"),
  MAHNUNG_MITGLIED_DATEINAME("mahnung-mitglied-dateiname",
      "Mahnung-MitgliedDateiname",
      "Mahnung-$rechnung_nummer-$mitglied_name-$mitglied_vorname"),
  KONTOAUSZUG_DATEINAME("kontoauszug-dateiname", "KontoauszugDateiname",
      "Kontoauszug-$aktuellesdatum-$aktuellezeit"),
  KONTOAUSZUG_MITGLIED_DATEINAME("kontoauszug-mitglied-dateiname",
      "Kontoauszug-MitgliedDateiname",
      "Kontoauszug-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit"),
  FREIES_FORMULAR_DATEINAME("freies-formular-dateiname",
      "FreiesFormularDateiname",
      "$formular_name-$aktuellesdatum-$aktuellezeit"),
  FREIES_FORMULAR_MITGLIED_DATEINAME("freies-formular-mitglied-dateiname",
      "FreiesFormular-MitgliedDateiname",
      "$formular_name-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit"),
  CT1_AUSGABE_DATEINAME("1ct-ausgabe-dateiname", "1ctAusgabeDateiname",
      "1ctueberweisung-$aktuellesdatum-$aktuellezeit"),
  PRENOTIFICATION_DATEINAME("pre-notification-dateiname",
      "Pre-NotificationDateiname",
      "Prenotification-$aktuellesdatum-$aktuellezeit"),
  PRENOTIFICATION_MITGLIED_DATEINAME("pre-notification-mitglied-dateiname",
      "Pre-Notification-MitgliedDateiname",
      "Prenotification-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit"),

  // Reports aus Mitglieder
  PERSONALBOGEN_DATEINAME("personalbogen-dateiname", "PersonalbogenDateiname",
      "Personalbogen-$aktuellesdatum-$aktuellezeit"),
  PERSONALBOGEN_MITGLIED_DATEINAME("personalbogen-mitglied-dateiname",
      "Personalbogen-MitgliedDateiname",
      "Personalbogen-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit"),
  VCARD_DATEINAME("vcard-dateiname", "VCardDateiname",
      "VCards-$aktuellesdatum-$aktuellezeit"),
  VCARD_MITGLIED_DATEINAME("vcard-mitglied-dateiname",
      "VCARD-MitgliedDateiname",
      "VCard-$mitglied_name-$mitglied_vorname-$aktuellesdatum-$aktuellezeit"),
  SOLLBUCHUNGEN_DATEINAME("sollbuchungen-dateiname",
      "SollbuchungenListeDateiname",
      "Sollbuchungen-Differenz-$filter_differenz-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  SPENDENBESCHEINIGUNGEN_DATEINAME("spendenbescheinigungen-dateiname",
      "SpendenbescheinigungenListeDateiname",
      "Spendenbescheinigungen-$filter_spendeart-$aktuellesdatum"),
  ZUSATZBETRAEGE_DATEINAME("zusatzbetraege-dateiname",
      "ZusatzbeträgeListeDateiname",
      "Zusatzbetraege-$filter_ausfuehrungstag-$aktuellesdatum-$aktuellezeit"),

  // Reports aus Buchführung
  KONTENSALDO_DATEINAME("kontensaldo-dateiname", "KontensaldoDateiname",
      "Kontensdaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit"),
  BUCHUNGSJOURNAL_DATEINAME("buchungsjournal-dateiname",
      "BuchungsjournalDateiname",
      "Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  EINZELBUCHUNGEN_DATEINAME("einzelbuchungen-dateiname",
      "EinzelbuchungenDateiname",
      "Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  CSVBUCHUNGEN_DATEINAME("csvbuchungen-dateiname",
      "CSVEinzelbuchungenDateiname",
      "Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  SUMMENBUCHUNGEN_DATEINAME("summenbuchungen-dateiname",
      "SummenbuchungenDateiname",
      "Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  ANLAGEN_BUCHUNGSJOURNAL_DATEINAME("anlagen-buchungsjournal-dateiname",
      "AnlagenBuchungsjournalDateiname",
      "Anlagen-Buchungsjournal-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  ANLAGEN_EINZELBUCHUNGEN_DATEINAME("anlagen-einzelbuchungen-dateiname",
      "AnlagenEinzelbuchungenDateiname",
      "Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  ANLAGEN_CSVBUCHUNGEN_DATEINAME("anlagen-csvbuchungen-dateiname",
      "AnlagenCSVEinzelbuchungenDateiname",
      "Anlagen-Buchungsliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  ANLAGEN_SUMMENBUCHUNGEN_DATEINAME("anlagen-summenbuchungen-dateiname",
      "AnlagenSummenbuchungenDateiname",
      "Anlagen-Summenliste-$filter_datum_von_f-$filter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  BUCHUNGSKLASSENSALDO_DATEINAME("buchungsklassensaldo-dateiname",
      "BuchungsklassensaldoDateiname",
      "Buchungsklassensaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit"),
  UMSATZSTEUER_VORANMELDUNG_DATEINAME("umsatzsteuervoranmeldung-dateiname",
      "UmsatzsteuerVoranmeldungDateiname",
      "Umsatzsteuer-Voranmeldung-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit"),
  PROJEKTSALDO_DATEINAME("projektsaldo-dateiname", "ProjektsaldoDateiname",
      "Projektsaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit"),
  ANLAGENVERZEICHNIS_DATEINAME("anlagenverzeichnis-dateiname",
      "AnlagenverzeichnisDateiname",
      "Anlagenverzeichnis-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit"),
  MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME(
      "mittelverwendung-zufluss-dateiname",
      "MittelverwendungZuflussreportDateiname",
      "Mittelverwendung-Zufluss-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit"),
  MITTELVERWENDUNGSREPORT_SALDO_DATEINAME("mittelverwendung-saldo-dateiname",
      "MittelverwendungSaldoreportDateiname",
      "Mittelverwendung-Saldo-$zeitraum_jahr-$aktuellesdatum-$aktuellezeit"),
  MITTELVERWENDUNGSSALDO_DATEINAME("mittelverwendungssaldo-dateiname",
      "MittelverwendungssaldoDateiname",
      "Mittelverwendungssaldo-$zeitraum_von_f-$zeitraum_bis_f-$aktuellesdatum-$aktuellezeit"),
  WIRTSCHAFTSPLAN_DATEINAME("wirtschaftsplan-dateiname",
      "WirtschaftsplanDateiname",
      "Wirtschaftsplan-$parameter_bezeichnung-$parameter_datum_von_f-$parameter_datum_bis_f-$aktuellesdatum-$aktuellezeit"),
  WIRTSCHAFTSPLAN_MEHRERE_DATEINAME("wirtschaftsplan-mehrere-dateiname",
      "WirtschaftsplanDateiname",
      "Wirtschaftsplan-$aktuellesdatum-$aktuellezeit"),

  // Reports aus Abrechnung
  ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME(
      "abrechnungslauf-lastschriften-dateiname",
      "AbrechnungslaufLastschriftenDateiname",
      "Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit"),
  ABRECHNUNGSLAUF_SEPA_DATEINAME("abrechnungslauf-sepa-xml-dateiname",
      "AbrechnungslaufSEPAXMLLastschriftenDateiname",
      "SEPA-Lastschriften-$parameter_faelligkeit_f-$aktuellesdatum-$aktuellezeit"),
  ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME("abrechnungslaufliste-dateiname",
      "AbrechnungslaufSollbuchungenDateiname",
      "Abrechnungslaufliste-$parameter_lauf-$aktuellesdatum-$aktuellezeit"),

  // Reports aus Auswertung
  AUSWERTUNG_MITGLIED_DATEINAME("auswertung-mitglied-dateiname",
      "AuswertungMitgliedDateiname",
      "Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_NICHT_MITGLIED_DATEINAME("auswertung-nichtmitglied-dateiname",
      "AuswertungNicht-MitgliedDateiname",
      "Nicht-Mitglieder-$ausgabe_ausgabe-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_ALTERSJUBILARE_DATEINAME("auswertung-altersjubilare-dateiname",
      "AuswertungAltersjubiläenDateiname",
      "Altersjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME(
      "auswertung-mitgliedsschaftsjubilare-dateiname",
      "AuswertungMitgliedschaftsjubiläenDateiname",
      "Mitgliedschaftsjubilare-$filter_jahr-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_KURSTEILNEHMER_DATEINAME("auswertung-kursteilnehmer-dateiname",
      "AuswertungKursteilnehmerDateiname",
      "Kursteilnehmer-$filter_abbuchung_von_f-$filter_abbuchung_bis_f-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME(
      "auswertung-mitgliederstatistik-dateiname",
      "AuswertungMitgliederstatistikDateiname",
      "Mitgliederstatistik-$filter_stichtag_f-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME(
      "auswertung-jahrgangsstatistik-dateiname",
      "AuswertungJahrgangsstatistikDateiname",
      "Jahrgangsstatistik-$filter_jahr-$aktuellesdatum-$aktuellezeit"),
  AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME("auswertung-arbeitseinsaetze-dateiname",
      "AuswertungArbeitseinsätzeDateiname",
      "Arbeitseinsaetze-$filter_jahr-$filter_auswertung-$aktuellesdatum-$aktuellezeit"),

  // Reports aus Einstellungen Buchführung
  BUCHUNGSARTEN_DATEINAME("buchungsarten-dateiname", "BuchungsartenDateiname",
      "Buchungsarten-$aktuellesdatum-$aktuellezeit"),
  KONTENRAHMEN_DATEINAME_V1("kontenrahmen-v1-dateiname",
      "KontenrahmenVersion1Dateiname",
      "Kontenrahmen-v1-$aktuellesdatum-$aktuellezeit"),
  KONTENRAHMEN_DATEINAME_V2("kontenrahmen-v2-dateiname",
      "KontenrahmenVersion2Dateiname",
      "Kontenrahmen-v2-$aktuellesdatum-$aktuellezeit"),
  FORMULAR_DATEINAME("formular-dateiname", "FormularDateiname",
      "Formular-$formular_name-$aktuellesdatum-$aktuellezeit"),
  FORMULARFELDER_DATEINAME("formularfelder-dateiname",
      "FormularfelderDateiname",
      "Formularfelder-$formular_name-$aktuellesdatum-$aktuellezeit");

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
