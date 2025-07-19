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
package de.jost_net.JVerein.gui.view;

public class DokumentationUtil
{
  public static final String DOKU = "https://openjverein.gitbook.io/doku";

  private static final String PRE = DOKU + "/";

  // private static final String ALLGEMEIN = "allgemein/";

  private static final String FUNKTIONEN = PRE + "v/3.1/";
  

  private static final String AUSWERTUNGEN = FUNKTIONEN + "auswertungen/";
  
  private static final String ABRECH = FUNKTIONEN + "abrech/";
  
  private static final String BUCHF = FUNKTIONEN + "buchf/";
  
  private static final String MITGLIEDER = FUNKTIONEN + "mitglieder/";
  
  private static final String DRUCKMAIL = FUNKTIONEN + "druckmail/";
  

  private static final String ADMIN = FUNKTIONEN + "administration/";

  private static final String ADMBUCHF = ADMIN + "admbuchf/";

  private static final String ADMMITGLIEDER = ADMIN + "mitglieder/";
  
  private static final String ADMERWEITERT = ADMIN + "erweitert/";
  
  private static final String ADMEINSTELLUNG = ADMIN + "einstellungen/";
  
  
  // Mitglieder
  public static final String ARBEITSEINSATZ = MITGLIEDER + "arbeitseinsatz";

  public static final String FAMILIENBEITRAG = MITGLIEDER + "familienbeitrag";

  public static final String KURSTEILNEHMER = MITGLIEDER + "kursteilnehmer";

  public static final String LEHRGANG = MITGLIEDER + "lehrgange";
  
  public static final String MITGLIEDSUCHE = MITGLIEDER + "content/mitglieder";
  
  public static final String MITGLIED = MITGLIEDER + "content/grunddaten";
  
  public static final String MITGLIEDIMPORT = MITGLIEDER + "import";

  
  public static final String MITGLIEDSKONTO_AUSWAHL = MITGLIEDER
      + "mitgliedskonto#mitgliedskontozuordnen";

  public static final String MITGLIEDSKONTO_UEBERSICHT = MITGLIEDER
      + "mitgliedskonto#mitgliedskontouebersicht";
  
  public static final String ADRESSEN = MITGLIEDER + "nichtmitglieder";

  public static final String SPENDENBESCHEINIGUNG = MITGLIEDER
      + "spendenbescheinigung";
  
  public static final String RECHNUNG = MITGLIEDER + "rechnung";
  
  public static final String SUCHPROFIL = MITGLIEDER + "content/suchprofil";

  public static final String WIEDERVORLAGE = MITGLIEDER + "wiedervorlage";

  public static final String ZUSATZBETRAEGE = MITGLIEDER + "zusatzbetrage";

  public static final String ZUSATZBETRAEGE_VORLAGE = MITGLIEDER
      + "zusatzbetragevorlage";

  public static final String ZUSATZBETRAEGEIMPORT = MITGLIEDER
      + "zusatzbetrage-importieren";
  
  
  // Buchführung
  public static final String ANFANGSBESTAENDE = BUCHF + "anfangsbestand";

  public static final String BUCHUNGEN = BUCHF + "buchungen";
  
  public static final String ANLAGENBUCHUNGEN = BUCHF + "anlagenbuchungen";

  public static final String BUCHUNGSIMPORT = BUCHF + "buchungsimport";

  public static final String BUCHUNGSKLASSENSALDO = BUCHF + "buchungsklasse";

  public static final String BUCHUNGSUEBERNAHME = BUCHF
      + "buchungsubernahme";

  public static final String BUCHUNGSKORREKTUR = BUCHF + "buchungskorrektur";
  
  public static final String JAHRESABSCHLUSS = BUCHF + "jahresabschluss";
  
  public static final String KONTEN = BUCHF + "konten";

  public static final String JAHRESSALDO = BUCHF + "kontensaldo";

  public static final String PROJEKTSALDO = BUCHF + "projekte";
  
  public static final String SPLITBUCHUNG = BUCHF + "splittbuchungen";
  
  public static final String ANLAGENLISTE = BUCHF + "anlagenverzeichnis";
  
  public static final String MITTELVERWENDUNG = BUCHF + "mittelverwendung";

  public static final String MITTELVERWENDUNGSALDO = BUCHF
      + "mittelverwendungsaldo";

  public static final String UMSATZSTEUERSALDO = BUCHF + "umsatzsteuersaldo";


  // Abrechnung
  public static final String ABRECHNUNG = ABRECH + "abrechnung";

  public static final String ABRECHNUNGSLAUF = ABRECH + "abrechnungslauf";
  
  public static final String LASTSCHRIFT = ABRECH + "lastschrift";
  
  public static final String SEPABUGS = ABRECH + "sepa-bugs";
  
  
  // Auswertung
  public static final String AUSWERTUNGKURSTEILNEHMER = AUSWERTUNGEN
      + "auswertung-kursteilnehmer";

  public static final String AUSWERTUNGMITGLIEDER = AUSWERTUNGEN
      + "auswertung-mitglieder";

  public static final String JUBILAEEN = AUSWERTUNGEN + "jubilaen";
  
  public static final String STATISTIKMITGLIEDER = AUSWERTUNGEN + "statistik";

  public static final String STATISTIKJAHRGAENGE = AUSWERTUNGEN
      + "statistik-jahrgange";
  
  public static final String ARBEITSEINSATZPRUEFEN = AUSWERTUNGEN
      + "arbeitseinsatz";
  

  // Druck und Mail
  public static final String RECHNUNG_MAIL = DRUCKMAIL + "rechnungen";

  public static final String MAHNUNG = DRUCKMAIL + "mahnungen";

  public static final String KONTOAUSZUG = DRUCKMAIL + "kontoauszug";

  public static final String FREIESFORMULAR = DRUCKMAIL + "freiesformular";
  
  public static final String SPENDENBESCHEINIGUNGMAIL = DRUCKMAIL
      + "spendenbescheinigungen";
  
  public static final String PRENOTIFICATION = DRUCKMAIL + "pre-notification";
  
  public static final String MAIL = DRUCKMAIL + "mail";

  public static final String MAILVORLAGE = DRUCKMAIL + "mailvorlagen";
  
  
  // Einstellungen
  public static final String EINSTELLUNGEN_ABRECHNUNG = ADMEINSTELLUNG
      + "abrechnung";
  
  public static final String EINSTELLUNGEN_ALLGEMEIN = ADMEINSTELLUNG
      + "allgemein";
  
  public static final String EINSTELLUNGEN_ANSICHT = ADMEINSTELLUNG + "ansicht";
  
  public static final String EINSTELLUNGEN_ANZEIGE = ADMEINSTELLUNG + "anzeige";
  
  public static final String EINSTELLUNGEN_BUCHFUEHRUNG = ADMEINSTELLUNG
      + "buchfuehrung";
  
  public static final String EINSTELLUNGEN_DATEINAMEN = ADMEINSTELLUNG
      + "dateinamen";
  
  public static final String EINSTELLUNGEN_MAIL = ADMEINSTELLUNG + "mail";
  
  public static final String EINSTELLUNGEN_RECHNUNGEN = ADMEINSTELLUNG
      + "rechnungen";
  
  public static final String EINSTELLUNGEN_SPALTEN = ADMEINSTELLUNG + "spalten";
  
  public static final String EINSTELLUNGEN_SPENDENBESCHEINIGUNGEN = ADMEINSTELLUNG
      + "spendenbescheinigungen";
  
  public static final String EINSTELLUNGEN_STATISTIK = ADMEINSTELLUNG
      + "statistik";
  
  
  // Einstellungen Mitglieder
  public static final String BEITRAGSGRUPPEN = ADMMITGLIEDER
      + "beitragsgruppen";

  public static final String EIGENSCHAFT = ADMMITGLIEDER + "eigenschaften";

  public static final String EIGENSCHAFTGRUPPE = ADMMITGLIEDER
      + "eigenschaften-gruppen";

  public static final String FELDDEFINITIONEN = ADMMITGLIEDER
      + "felddefinition";

  public static final String FORMULARE = ADMMITGLIEDER + "formulare";

  public static final String LEHRGANGARTEN = ADMMITGLIEDER + "lehrgange";

  public static final String LESEFELDER = ADMMITGLIEDER + "lesefelder";

  public static final String ADRESSTYPEN = ADMMITGLIEDER + "mitgliedstypen";
  
  
  // Einstellungen Buchführung
  public static final String BUCHUNGSART = ADMBUCHF + "buchungsart";

  public static final String BUCHUNGSKLASSEN = ADMBUCHF + "buchungsklasse";

  public static final String KONTENRAHMEN = ADMBUCHF
      + "kontenrahmen-import-export";

  public static final String PROJEKTE = ADMBUCHF + "projekte";

  public static final String STEUER = ADMBUCHF
      + "steuer";


  // Einstellungen Erweitert
  public static final String BEREINIGEN = ADMERWEITERT + "bereinigen";
  
  public static final String MITGRATION = ADMERWEITERT + "migration";
  
  public static final String QIFIMPORT = ADMERWEITERT + "qif-import";

  public static final String DATEI_NAME = PRE + FUNKTIONEN + ADMIN
      + ADMERWEITERT + "dateinamen";

  // Changelog bei Update
  public static final String CHANGELOG = FUNKTIONEN + "notes";

}
