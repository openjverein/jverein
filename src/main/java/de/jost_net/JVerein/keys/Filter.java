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

import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.datasource.rmi.DBObject;

/**
 * enum mit allen Filtern die in Views verwendet werden können.
 */
public enum Filter
{
  ABBUCHUNGSDATUM_BIS("filter_abbuchung_bis_f", "Abbuchungsdatum bis",
      "20241231", FilterArt.DATE),
  ABBUCHUNGSDATUM_VON("filter_abbuchung_von_f", "Abbuchungsdatum von",
      "20240101", FilterArt.DATE),
  ABRECHNUNGSLAUF("filter_abrechnungslauf", "Abrechnungslauf", "44",
      FilterArt.SELECT, Abrechnungslauf.class),
  ABRECHNUNGSLAUF_AB("filter_abrechnungslauf_ab", "Abrechnungslauf ab", "4",
      FilterArt.INTEGER),
  AUSTRITT_BIS("filter_austrittsdatum_bis_f", "Austritt bis", "20241231",
      FilterArt.DATE),
  AUSTRITT_VON("filter_austrittsdatum_von_f", "Austritt von", "20240101",
      FilterArt.DATE),
  BEITRAGSGRUPPE("filter_beitragsgruppe", "Beitragsgruppe", "Alle",
      FilterArt.SELECT, Beitragsgruppe.class),
  BEMERKUNG("filter_bemerkung", "Bemerkung", "Bemerkung", FilterArt.TEXT),
  BETREFF("filter_betreff", "Betreff", "Betreff", FilterArt.TEXT),
  BEZEICHNUNG("filter_bezeichnung", "Bezeichnung", "Bezeichnung",
      FilterArt.TEXT),
  BUCHUNGSARTART("filter_art", "Art", "Beitrag", FilterArt.SELECT,
      ArtBuchungsart.getArray()),
  BUCHUNGSKLASSE("filter_buchungsklasse", "Buchungsklasse", "Alle",
      FilterArt.SELECT, Buchungsklasse.class),
  DATUM_BEARBEITUNG_BIS("filter_bearbeitung_bis_f", "Bearbeitung bis",
      "20241231", FilterArt.DATE),
  DATUM_BEARBEITUNG_VON("filter_bearbeitung_von_f", "Bearbeitung von",
      "20240101", FilterArt.DATE),
  DATUM_BESCHEINIGUNG_BIS("filter_bescheinigungdatum_bis_f",
      "Bescheinigungsdatum bis", "20241231", FilterArt.DATE),
  DATUM_BESCHEINIGUNG_VON("filter_bescheinigungdatum_von_f",
      "Bescheinigungsdatum von", "20240101", FilterArt.DATE),
  DATUM_BIS("filter_datum_bis_f", "Datum bis", "20241231", FilterArt.DATE),
  DATUM_ENDE_BIS("filter_endedatum_bis_f", "Endedatum bis", "20241231",
      FilterArt.DATE),
  DATUM_ENDE_VON("filter_endedatum_von_f", "Endedatum von", "20240101",
      FilterArt.DATE),
  DATUM_ERLEDIGUNG_BIS("filter_erledigung_bis_f", "Erledigung bis", "20241231",
      FilterArt.DATE),
  DATUM_ERLEDIGUNG_VON("filter_erledigung_von_f", "Erledigung von", "20240101",
      FilterArt.DATE),
  DATUM_FAELLIGKEI_BIS("filter_datum_faelligkeit_bis_f", "Fälligkeit bis",
      "20241231", FilterArt.DATE),
  DATUM_FAELLIGKEI_VON("filter_datum_faelligkeit_von_f", "Fälligkeit von",
      "20240101", FilterArt.DATE),
  DATUM_SPENDE_BIS("filter_spendedatum_bis_f", "Spendedatum bis", "20241231",
      FilterArt.DATE),
  DATUM_SPENDE_VON("filter_spendedatum_von_f", "Spendedatum von", "20240101",
      FilterArt.DATE),
  DATUM_START_BIS("filter_startdatum_bis_f", "Startdatum bis", "20241231",
      FilterArt.DATE),
  DATUM_START_VON("filter_startdatum_von_f", "Startdatum von", "20240101",
      FilterArt.DATE),
  DATUM_VERSAND_BIS("filter_versand_bis_f", "Versand bis", "20241231",
      FilterArt.DATE),
  DATUM_VERSAND_VON("filter_versand_von_f", "Versand von", "20240101",
      FilterArt.DATE),
  DATUM_VON("filter_datum_von_f", "Datum von", "20240101", FilterArt.DATE),
  DIFFERENZ("filter_differenz", "Differenz", "Fehlbetrag", FilterArt.SELECT,
      Differenz.values()),
  DIFFERENZ_LIMIT("filter_differenz_limit", "Differenz Limit", "4.00",
      FilterArt.DOUBLE),
  EIGENSCHAFTEN("filter_eigenschaften", "Eigenschaften", "+Eigenschaft",
      FilterArt.EIGENSCHAFTEN),
  EINGABEDATUM_BIS("filter_eingabedatum_bis_f", "Eingabedatum bis", "20241231",
      FilterArt.DATE),
  EINGABEDATUM_VON("filter_eingabedatum_von_f", "Eingabedatum von", "20240101",
      FilterArt.DATE),
  EINTRITT_BIS("filter_eintrittsdatum_bis_f", "Eintritt bis", "20241231",
      FilterArt.DATE),
  EINTRITT_VON("filter_eintrittsdatum_von_f", "Eintritt von", "20240101",
      FilterArt.DATE),
  ENTHALTENER_TEXT("filter_enthaltener_text", "Enthaltener Text", "Text",
      FilterArt.TEXT),
  EXTERNEMITGLIEDSNUMMER("filter_externe_mitgliedsnummer",
      "Externe Mitgliedsnummer", "Ext45", FilterArt.TEXT),
  GEBURTSDATUM_BIS("filter_geburtsdatum_bis_f", "Geburtsdatum bis", "20241231",
      FilterArt.DATE),
  GEBURTSDATUM_VON("filter_geburtsdatum_von_f", "Geburtsdatum von", "20240101",
      FilterArt.DATE),
  GESCHLECHT("filter_geschlecht", "Geschlecht", "Alle", FilterArt.SELECT,
      Geschlecht.values()),
  KONTOART("filter_kontoart", "Kontoart", "Alle", FilterArt.SELECT,
      Kontoart.getList()),
  LEHRGANGSART("filter_lehrgangsart", "Lehrgangsart", "Grundkurs",
      FilterArt.SELECT, Lehrgangsart.class),
  MAIL("filter_mail", "Mail", "Alle", FilterArt.SELECT, MailAuswahl.values()),
  MAIL_EMPFAENGER("filter_mail_empfaenger", "Mail Empfänger", "Text",
      FilterArt.TEXT),
  MITGLIED("filter_mitglied", "Mitglied", "Meier", FilterArt.TEXT),
  MITGLIEDART("filter_mitgliedsart", "Mitgliedsart", "Alle", FilterArt.SELECT,
      MitgliedsArt.getList()),
  MITGLIEDSCHAFT_STATUS("filter_mitgliedschaft", "Mitgliedschaft", "Alle",
      FilterArt.SELECT, MitgliedStatus.values()),
  MITGLIEDSNUMMER("filter_mitgliedsnummer", "Mitgliedsnummer", "45",
      FilterArt.INTEGER),
  MITGLIEDSTYP("filter_mitgliedstyp", "Adresstyp", "Spender/in",
      FilterArt.SELECT, Mitgliedstyp.class),
  NAME("filter_name", "Name", "Meier", FilterArt.TEXT),
  NUMMER("filter_nummer", "Nummer", "44", FilterArt.TEXT),

  OHNE_ABBUCHER("filter_ohne_abbucher", "Ohne Abbucher", "Ja",
      FilterArt.CHECKBOX),
  OHNE_ERLEDIGUNG("filter_ohne_erledigung", "Ohne Erledigung", "Ja",
      FilterArt.CHECKBOX),
  SPENDENART("filter_spendeart", "Spendenart", "Alle", FilterArt.SELECT,
      SuchSpendenart.values()),
  STATUS("filter_status", "Status", "Alle", FilterArt.CHECKBOX),
  STERBEDATUM_BIS("filter_sterbedatum_bis_f", "Sterbetag bis", "20241231",
      FilterArt.DATE),
  STERBEDATUM_VON("filter_sterbedatum_von_f", "Sterbetag von", "20240101",
      FilterArt.DATE),
  STICHTAG("filter_stichtag_f", "Stichtag", "20240101", FilterArt.DATE),
  VERANSTALTER("filter_veranstalter", "Veranstalter", "Kursleiter",
      FilterArt.TEXT),
  VERMERK("filter_vermerk", "Vermerk", "Vermerk", FilterArt.TEXT),
  VERSAND("filter_versand", "Versand", "Alle", FilterArt.SELECT,
      SuchVersand.values()),
  VERWENDUNGSZWECK("filter_verwendungszweck", "Verwendungszweck", "Beitrag",
      FilterArt.TEXT),
  VORLAGEART("filter_vorlagenart", "Vorlagenart", "Titel", FilterArt.SELECT,
      Vorlageart.values()),
  ZAHLER("filter_zahler", "Zahler", "Text", FilterArt.TEXT),
  ZEILE2("filter_zeile2", "Zeile 2", "Meier", FilterArt.TEXT),
  ZUSATZFELD("filter_zusatzfelder", "Zusatzfelder", "Zusatzfeld",
      FilterArt.ZUSATZFELD),
  ZWECK("filter_zweck", "Zweck", "Zweck", FilterArt.TEXT);

  private String setting;

  private String anzeigeText;

  private String defaultValue;

  private FilterArt art;

  private KeyEnum[] array;

  private Class<? extends DBObject> dbObject;

  public enum FilterArt
  {
    SELECT,
    TEXT,
    DATE,
    ZUSATZFELD,
    EIGENSCHAFTEN,
    CHECKBOX,
    INTEGER,
    DOUBLE
  }

  Filter(String setting, String anzeigeText, String defalutValue, FilterArt art)
  {
    this.setting = setting;
    this.anzeigeText = anzeigeText;
    this.defaultValue = defalutValue;
    this.art = art;
  }

  Filter(String setting, String anzeigeText, String defalutValue, FilterArt art,
      KeyEnum[] array)
  {
    this.setting = setting;
    this.anzeigeText = anzeigeText;
    this.defaultValue = defalutValue;
    this.art = art;
    this.array = array;
  }

  Filter(String setting, String anzeigeText, String defalutValue, FilterArt art,
      Class<? extends DBObject> dbObject)
  {
    this.setting = setting;
    this.anzeigeText = anzeigeText;
    this.defaultValue = defalutValue;
    this.art = art;
    this.dbObject = dbObject;
  }

  public String getSetting()
  {
    return setting;
  }

  public String getAnzeigeText()
  {
    return anzeigeText;
  }

  public String getDefault()
  {
    return defaultValue;
  }

  public FilterArt getArt()
  {
    return art;
  }

  public KeyEnum[] getArray()
  {
    return array;
  }

  public Class<? extends DBObject> getDbObject()
  {
    return dbObject;
  }
}
