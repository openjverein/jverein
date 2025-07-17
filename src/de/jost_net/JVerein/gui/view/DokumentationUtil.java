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

  private static final String PRE = DOKU+"/";
  
  //private static final String ALLGEMEIN = "allgemein/";
  
  private static final String FUNKTIONEN = "versionen/v3.1.x/";
  
  private static final String ADMIN = "administration/";
  
  private static final String AUSWERTUNGEN = "auswertungen/";
  
  private static final String ABRECH = "abrech/";
  
  private static final String BUCHF = "buchf/";
  
  private static final String MITGLIEDER = "mitglieder/";
  
  private static final String DRUCKMAIL = "druckmail/";
  
  private static final String ADMBUCHF = "admbuchf/";

  private static final String ADMMITGLIEDER = "mitglieder/";
  
  private static final String ADMERWEITERT = "erweitert/";
  
  private static final String ADMEINSTELLUNG = "einstellungen/";
  
  
  // Mitglieder
  public static final String ARBEITSEINSATZ = PRE + FUNKTIONEN + MITGLIEDER + "arbeitseinsatz";

  public static final String FAMILIENBEITRAG = PRE + FUNKTIONEN + MITGLIEDER + "familienbeitrag";

  public static final String KURSTEILNEHMER = PRE + FUNKTIONEN + MITGLIEDER + "kursteilnehmer";

  public static final String LEHRGANG = PRE + FUNKTIONEN + MITGLIEDER  + "lehrgange";
  
  public static final String MITGLIEDSUCHE = PRE + FUNKTIONEN + MITGLIEDER + "content/mitglieder";
  
  public static final String MITGLIED = PRE + FUNKTIONEN + MITGLIEDER + "content/grunddaten";
  
  public static final String MITGLIEDIMPORT = PRE + FUNKTIONEN + MITGLIEDER + "import";

  
  public static final String MITGLIEDSKONTO_AUSWAHL = PRE + FUNKTIONEN + MITGLIEDER
      + "mitgliedskonto#mitgliedskontozuordnen";

  public static final String MITGLIEDSKONTO_UEBERSICHT = PRE + FUNKTIONEN + MITGLIEDER 
      + "mitgliedskonto#mitgliedskontouebersicht";
  
  public static final String ADRESSEN = PRE + FUNKTIONEN + MITGLIEDER + "nichtmitglieder";

  public static final String SPENDENBESCHEINIGUNG = PRE + FUNKTIONEN + MITGLIEDER 
      + "spendenbescheinigung";
  
  public static final String RECHNUNG = PRE + FUNKTIONEN + MITGLIEDER + "rechnung";
  
  public static final String SUCHPROFIL = PRE + FUNKTIONEN + MITGLIEDER + "content/suchprofil";

  public static final String WIEDERVORLAGE = PRE + FUNKTIONEN + MITGLIEDER  + "wiedervorlage";

  public static final String ZUSATZBETRAEGE = PRE + FUNKTIONEN + MITGLIEDER  + "zusatzbetrage";

  public static final String ZUSATZBETRAEGE_VORLAGE = PRE + FUNKTIONEN
      + MITGLIEDER + "zusatzbetragevorlage";

  public static final String ZUSATZBETRAEGEIMPORT = PRE + FUNKTIONEN + MITGLIEDER 
      + "zusatzbetrage-importieren";
  
  
  // Buchführung
  public static final String ANFANGSBESTAENDE = PRE + FUNKTIONEN + BUCHF + "anfangsbestand";

  public static final String BUCHUNGEN = PRE + FUNKTIONEN + BUCHF + "buchungen";
  
  public static final String ANLAGENBUCHUNGEN = PRE + FUNKTIONEN + BUCHF + "anlagenbuchungen";

  public static final String BUCHUNGSIMPORT = PRE + FUNKTIONEN + BUCHF + "buchungsimport";

  public static final String BUCHUNGSKLASSENSALDO = PRE + FUNKTIONEN + BUCHF + "buchungsklasse";

  public static final String BUCHUNGSUEBERNAHME = PRE + FUNKTIONEN + BUCHF
      + "buchungsubernahme";

  public static final String BUCHUNGSKORREKTUR = PRE + FUNKTIONEN + BUCHF + "buchungskorrektur";
  
  public static final String JAHRESABSCHLUSS = PRE + FUNKTIONEN + BUCHF + "jahresabschluss";
  
  public static final String KONTEN = PRE + FUNKTIONEN + BUCHF + "konten";

  public static final String JAHRESSALDO = PRE + FUNKTIONEN + BUCHF + "jahressaldo";

  public static final String PROJEKTSALDO = PRE + FUNKTIONEN + BUCHF + "projekte";
  
  public static final String SPLITBUCHUNG = PRE + FUNKTIONEN + BUCHF + "splittbuchungen";
  
  public static final String ANLAGENLISTE = PRE + FUNKTIONEN + BUCHF + "anlagenverzeichnis";
  
  public static final String MITTELVERWENDUNG = PRE + FUNKTIONEN + BUCHF + "mittelverwendung";

  public static final String MITTELVERWENDUNGSALDO = PRE + FUNKTIONEN + BUCHF
      + "mittelverwendungsaldo";

  public static final String UMSATZSTEUERSALDO = PRE + FUNKTIONEN + BUCHF
      + "umsatzsteuersaldo";


  // Abrechnung
  public static final String ABRECHNUNG = PRE + FUNKTIONEN + ABRECH + "abrechnung";

  public static final String ABRECHNUNGSLAUF = PRE + FUNKTIONEN + ABRECH + "abrechnungslauf";
  
  public static final String LASTSCHRIFT = PRE + FUNKTIONEN + ABRECH + "lastschrift";
  
  public static final String SEPABUGS = PRE + FUNKTIONEN + ABRECH + "sepa-bugs";
  
  
  // Auswertung
  public static final String AUSWERTUNGKURSTEILNEHMER = PRE + FUNKTIONEN + AUSWERTUNGEN
      + "auswertung-kursteilnehmer";

  public static final String AUSWERTUNGMITGLIEDER = PRE + FUNKTIONEN + AUSWERTUNGEN
      + "auswertung-mitglieder";

  public static final String JUBILAEEN = PRE + FUNKTIONEN + AUSWERTUNGEN + "jubilaen";
  
  public static final String STATISTIKMITGLIEDER = PRE + FUNKTIONEN + AUSWERTUNGEN + "statistik";

  public static final String STATISTIKJAHRGAENGE = PRE + FUNKTIONEN + AUSWERTUNGEN
      + "statistik-jahrgange";
  
  public static final String ARBEITSEINSATZPRUEFEN = PRE + FUNKTIONEN + AUSWERTUNGEN + "arbeitseinsatz";
  

  // Druck und Mail
  public static final String RECHNUNG_MAIL = PRE + FUNKTIONEN + DRUCKMAIL + "rechnungen";

  public static final String MAHNUNG = PRE + FUNKTIONEN + DRUCKMAIL+ "mahnungen";

  public static final String KONTOAUSZUG = PRE + FUNKTIONEN + DRUCKMAIL + "kontoauszug";

  public static final String FREIESFORMULAR = PRE + FUNKTIONEN + DRUCKMAIL + "freiesformular";
  
  public static final String SPENDENBESCHEINIGUNGMAIL = PRE + FUNKTIONEN + DRUCKMAIL 
      + "spendenbescheinigungen";
  
  public static final String PRENOTIFICATION = PRE + FUNKTIONEN + DRUCKMAIL + "pre-notification";
  
  public static final String MAIL = PRE + FUNKTIONEN + DRUCKMAIL + "mail";

  public static final String MAILVORLAGE = PRE + FUNKTIONEN + DRUCKMAIL + "mailvorlagen";
  
  
  // Einstellungen
  public static final String EINSTELLUNGEN_ABRECHNUNG = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "abrechnung";
  
  public static final String EINSTELLUNGEN_ALLGEMEIN = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "allgemein";
  
  public static final String EINSTELLUNGEN_ANSICHT = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "ansicht";
  
  public static final String EINSTELLUNGEN_ANZEIGE = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "anzeige";
  
  public static final String EINSTELLUNGEN_BUCHFUEHRUNG = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "buchfuehrung";
  
  public static final String EINSTELLUNGEN_DATEINAMEN = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "dateinamen";
  
  public static final String EINSTELLUNGEN_MAIL = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "mail";
  
  public static final String EINSTELLUNGEN_RECHNUNGEN = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "rechnungen";
  
  public static final String EINSTELLUNGEN_SPALTEN = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "spalten";
  
  public static final String EINSTELLUNGEN_SPENDENBESCHEINIGUNGEN = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "spendenbescheinigungen";
  
  public static final String EINSTELLUNGEN_STATISTIK = PRE + FUNKTIONEN + ADMIN + ADMEINSTELLUNG + "statistik";
  
  
  // Einstellungen Mitglieder
  public static final String BEITRAGSGRUPPEN = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER + "beitragsgruppen";

  public static final String EIGENSCHAFT = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER + "eigenschaften";

  public static final String EIGENSCHAFTGRUPPE = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER
      + "eigenschaften-gruppen";

  public static final String FELDDEFINITIONEN = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER + "felddefinition";

  public static final String FORMULARE = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER + "formulare";

  public static final String LEHRGANGARTEN = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER  + "lehrgange";

  public static final String LESEFELDER = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER + "lesefelder";

  public static final String ADRESSTYPEN = PRE + FUNKTIONEN + ADMIN + ADMMITGLIEDER + "mitgliedstypen";
  
  
  // Einstellungen Buchführung
  public static final String BUCHUNGSART = PRE + FUNKTIONEN + ADMIN + ADMBUCHF + "buchungsart.html";

  public static final String BUCHUNGSKLASSEN = PRE + FUNKTIONEN + ADMIN + ADMBUCHF + "buchungsklasse";

  public static final String KONTENRAHMEN = PRE + FUNKTIONEN + ADMIN + ADMBUCHF
      + "kontenrahmen-import-export";

  public static final String PROJEKTE = PRE + FUNKTIONEN + ADMIN + ADMBUCHF + "projekte";

  public static final String STEUER = PRE + FUNKTIONEN + ADMIN + ADMBUCHF
      + "steuer";


  // Einstellungen Erweitert
  public static final String BEREINIGEN = PRE + FUNKTIONEN + ADMIN + ADMERWEITERT + "bereinigen";  
  
  public static final String MITGRATION = PRE + FUNKTIONEN + ADMIN + ADMERWEITERT + "migration";
  
  public static final String QIFIMPORT = PRE + FUNKTIONEN + ADMIN + ADMERWEITERT + "qif-import";

  // Changelog bei Update
  public static final String CHANGELOG = PRE + FUNKTIONEN + "notes";

}
