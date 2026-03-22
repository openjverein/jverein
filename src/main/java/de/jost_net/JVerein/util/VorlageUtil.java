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
package de.jost_net.JVerein.util;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.AbrechnungSollbuchungenParameterMap;
import de.jost_net.JVerein.Variable.AbrechnungslaufParameterMap;
import de.jost_net.JVerein.Variable.AllgemeineMap;
import de.jost_net.JVerein.Variable.AnlagenbuchungListeFilterMap;
import de.jost_net.JVerein.Variable.AuswertungArbeitseinsatzFilterMap;
import de.jost_net.JVerein.Variable.AuswertungJubilareFilterMap;
import de.jost_net.JVerein.Variable.AuswertungMitgliedFilterMap;
import de.jost_net.JVerein.Variable.AuswertungNichtMitgliedFilterMap;
import de.jost_net.JVerein.Variable.BuchungListeFilterMap;
import de.jost_net.JVerein.Variable.BuchungMap;
import de.jost_net.JVerein.Variable.FilterMap;
import de.jost_net.JVerein.Variable.JahresabschlussListeFilterMap;
import de.jost_net.JVerein.Variable.LastschriftMap;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.RechnungMap;
import de.jost_net.JVerein.Variable.SaldoFilterMap;
import de.jost_net.JVerein.Variable.SpendenbescheinigungMap;
import de.jost_net.JVerein.Variable.WirtschaftsplanParameterMap;
import de.jost_net.JVerein.Variable.ZusatzbetragListeFilterMap;
import de.jost_net.JVerein.gui.control.AbstractSaldoControl;
import de.jost_net.JVerein.gui.control.ArbeitseinsatzAbrechnungControl;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.control.FilterControl;
import de.jost_net.JVerein.gui.control.JahresabschlussControl;
import de.jost_net.JVerein.gui.control.MitgliedControl;
import de.jost_net.JVerein.gui.control.WirtschaftsplanControl;
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.jost_net.JVerein.io.AbrechnungSEPAParam;
import de.jost_net.JVerein.io.VelocityTool;
import de.jost_net.JVerein.io.Zeichen;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.keys.Vorlageart;
import de.jost_net.JVerein.rmi.Abrechnungslauf;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Lastschrift;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Rechnung;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.rmi.Vorlage;
import de.jost_net.JVerein.server.IMitglied;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class VorlageUtil
{

  // Namen Generierung aus Vorlagen Muster
  public static String getName(VorlageTyp typ)
  {
    return getName(typ, null);
  }

  public static String getName(VorlageTyp typ, Object obj)
  {
    return getName(typ, obj, null);
  }

  public static String getName(VorlageTyp typ, Object obj, String formularName)
  {
    Map<String, Object> map = null;
    String muster = "";
    try
    {
      map = new AllgemeineMap().getMap(null);
      muster = getVorlageMuster(typ);

      if (obj instanceof IMitglied && ((IMitglied) obj).getMitglied() != null)
      {
        map = new MitgliedMap().getMap(((IMitglied) obj).getMitglied(), map);
      }
      switch (typ)
      {
        case ABRECHNUNGSLAEUFE_DATEINAME:
        case ABRECHNUNGSLAEUFE_SUBTITEL:
        case ABRECHNUNGSLAEUFE_TITEL:
        case ANFANGSBESTAENDE_DATEINAME:
        case ANFANGSBESTAENDE_SUBTITEL:
        case ANFANGSBESTAENDE_TITEL:
        case KONTEN_DATEINAME:
        case KONTEN_SUBTITEL:
        case KONTEN_TITEL:
        case KURSTEILNEHMER_DATEINAME:
        case KURSTEILNEHMER_SUBTITEL:
        case KURSTEILNEHMER_TITEL:
        case LASTSCHRIFTEN_DATEINAME:
        case LASTSCHRIFTEN_SUBTITEL:
        case LASTSCHRIFTEN_TITEL:
        case LEHRGAENGE_DATEINAME:
        case LEHRGAENGE_SUBTITEL:
        case LEHRGAENGE_TITEL:
        case MAILS_DATEINAME:
        case MAILS_SUBTITEL:
        case MAILS_TITEL:
        case RECHNUNGEN_DATEINAME:
        case RECHNUNGEN_SUBTITEL:
        case RECHNUNGEN_TITEL:
        case WIEDERVORLAGEN_DATEINAME:
        case WIEDERVORLAGEN_SUBTITEL:
        case WIEDERVORLAGEN_TITEL:
        case MITGLIEDER_DATEINAME:
        case MITGLIEDER_TITEL:
        case MITGLIEDER_SUBTITEL:
        case NICHT_MITGLIEDER_DATEINAME:
        case NICHT_MITGLIEDER_TITEL:
        case NICHT_MITGLIEDER_SUBTITEL:
        case SOLLBUCHUNGEN_DATEINAME:
        case SOLLBUCHUNGEN_TITEL:
        case SOLLBUCHUNGEN_SUBTITEL:
        case ARBEITSEINSAETZE_DATEINAME:
        case ARBEITSEINSAETZE_TITEL:
        case ARBEITSEINSAETZE_SUBTITEL:
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
        case SPENDENBESCHEINIGUNGEN_TITEL:
        case SPENDENBESCHEINIGUNGEN_SUBTITEL:
        case BUCHUNGSARTEN_DATEINAME:
        case BUCHUNGSARTEN_TITEL:
        case BUCHUNGSARTEN_SUBTITEL:
        case PROJEKTE_DATEINAME:
        case PROJEKTE_TITEL:
        case PROJEKTE_SUBTITEL:
        case AUSWERTUNG_KURSTEILNEHMER_DATEINAME:
        case AUSWERTUNG_KURSTEILNEHMER_TITEL:
        case AUSWERTUNG_KURSTEILNEHMER_SUBTITEL:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL:
        case ABWEICHENDE_ZAHLER_DATEINAME:
        case ABWEICHENDE_ZAHLER_SUBTITEL:
        case ABWEICHENDE_ZAHLER_TITEL:
        case FAMILIENVERBAND_DATEINAME:
        case FAMILIENVERBAND_SUBTITEL:
        case FAMILIENVERBAND_TITEL:
          map = new FilterMap().getMap((FilterControl) obj, map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = new SpendenbescheinigungMap().getMap((Spendenbescheinigung) obj,
              map);
          break;
        case RECHNUNG_MITGLIED_DATEINAME:
        case MAHNUNG_MITGLIED_DATEINAME:
          map = new RechnungMap().getMap((Rechnung) obj, map);
          break;
        case PRENOTIFICATION_MITGLIED_DATEINAME:
        case PRENOTIFICATION_KURSTEILNEHMER_DATEINAME:
          map = new LastschriftMap().getMap((Lastschrift) obj, map);
          break;
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case PERSONALBOGEN_TITEL:
        case PERSONALBOGEN_SUBTITEL:
        case VCARD_MITGLIED_DATEINAME:
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case KONTOAUSZUG_TITEL:
        case KONTOAUSZUG_SUBTITEL:
        case MITGLIED_ZUSATZBETRAEGE_DATEINAME:
        case MITGLIED_ZUSATZBETRAEGE_TITEL:
        case MITGLIED_ZUSATZBETRAEGE_SUBTITEL:
        case MITGLIED_WIEDERVORLAGEN_DATEINAME:
        case MITGLIED_WIEDERVORLAGEN_TITEL:
        case MITGLIED_WIEDERVORLAGEN_SUBTITEL:
        case MITGLIED_MAILS_DATEINAME:
        case MITGLIED_MAILS_TITEL:
        case MITGLIED_MAILS_SUBTITEL:
        case MITGLIED_LEHRGAENGE_DATEINAME:
        case MITGLIED_LEHRGAENGE_TITEL:
        case MITGLIED_LEHRGAENGE_SUBTITEL:
        case MITGLIED_LESEFELDER_DATEINAME:
        case MITGLIED_LESEFELDER_TITEL:
        case MITGLIED_LESEFELDER_SUBTITEL:
        case MITGLIED_ARBEITSEINSAETZE_DATEINAME:
        case MITGLIED_ARBEITSEINSAETZE_TITEL:
        case MITGLIED_ARBEITSEINSAETZE_SUBTITEL:
        case MITGLIED_DOKUMENTE_DATEINAME:
        case MITGLIED_DOKUMENTE_TITEL:
        case MITGLIED_DOKUMENTE_SUBTITEL:
        case MITGLIED_DOKUMENT_PFAD:
          map = new MitgliedMap().getMap((Mitglied) obj, map);
          break;
        case FREIES_FORMULAR_DATEINAME:
        case FORMULAR_DATEINAME:
        case FORMULARFELDER_DATEINAME:
        case FORMULARFELDER_TITEL:
        case FORMULARFELDER_SUBTITEL:
          map.put("formular_name", (String) obj);
          break;
        case FREIES_FORMULAR_MITGLIED_DATEINAME:
          map = new MitgliedMap().getMap((Mitglied) obj, map);
          map.put("formular_name", formularName);
          break;
        case KONTENSALDO_DATEINAME:
        case KONTENSALDO_TITEL:
        case KONTENSALDO_SUBTITEL:
        case BUCHUNGSKLASSENSALDO_DATEINAME:
        case BUCHUNGSKLASSENSALDO_TITEL:
        case BUCHUNGSKLASSENSALDO_SUBTITEL:
        case UMSATZSTEUER_VORANMELDUNG_DATEINAME:
        case UMSATZSTEUER_VORANMELDUNG_TITEL:
        case UMSATZSTEUER_VORANMELDUNG_SUBTITEL:
        case PROJEKTSALDO_DATEINAME:
        case PROJEKTSALDO_TITEL:
        case PROJEKTSALDO_SUBTITEL:
        case ANLAGENVERZEICHNIS_DATEINAME:
        case ANLAGENVERZEICHNIS_TITEL:
        case ANLAGENVERZEICHNIS_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_DATEINAME:
        case MITTELVERWENDUNGSREPORT_SALDO_TITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL:
        case MITTELVERWENDUNGSSALDO_DATEINAME:
        case MITTELVERWENDUNGSSALDO_TITEL:
        case MITTELVERWENDUNGSSALDO_SUBTITEL:
          map = new SaldoFilterMap().getMap((AbstractSaldoControl) obj, map);
          break;
        case ABRECHNUNGSLAUF_SEPA_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME:
          map = new AbrechnungslaufParameterMap()
              .getMap((AbrechnungSEPAParam) obj, map);
          break;
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_TITEL:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_SUBTITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_DATEINAME:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_TITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_SUBTITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_BUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_SUBTITEL:
          map = new AbrechnungSollbuchungenParameterMap()
              .getMap((Abrechnungslauf) obj, map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
        case ZUSATZBETRAEGE_TITEL:
        case ZUSATZBETRAEGE_SUBTITEL:
          map = new ZusatzbetragListeFilterMap()
              .getMap((ZusatzbetragControl) obj, map);
          break;
        case BUCHUNGEN_DATEINAME:
        case BUCHUNGEN_TITEL:
        case BUCHUNGEN_SUBTITEL:
        case BUCHUNGSJOURNAL_DATEINAME:
        case BUCHUNGSJOURNAL_TITEL:
        case BUCHUNGSJOURNAL_SUBTITEL:
        case EINZELBUCHUNGEN_DATEINAME:
        case EINZELBUCHUNGEN_TITEL:
        case EINZELBUCHUNGEN_SUBTITEL:
        case SUMMENBUCHUNGEN_DATEINAME:
        case SUMMENBUCHUNGEN_TITEL:
        case SUMMENBUCHUNGEN_SUBTITEL:
        case CSVBUCHUNGEN_DATEINAME:
          map = new BuchungListeFilterMap().getMap((BuchungsControl) obj, map);
          break;
        case ANLAGEN_BUCHUNGEN_DATEINAME:
        case ANLAGEN_BUCHUNGEN_TITEL:
        case ANLAGEN_BUCHUNGEN_SUBTITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_DATEINAME:
        case ANLAGEN_BUCHUNGSJOURNAL_TITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL:
        case ANLAGEN_EINZELBUCHUNGEN_DATEINAME:
        case ANLAGEN_EINZELBUCHUNGEN_TITEL:
        case ANLAGEN_EINZELBUCHUNGEN_SUBTITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_DATEINAME:
        case ANLAGEN_SUMMENBUCHUNGEN_TITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL:
        case ANLAGEN_CSVBUCHUNGEN_DATEINAME:
          map = new AnlagenbuchungListeFilterMap().getMap((BuchungsControl) obj,
              map);
          break;
        case JAHRESABSCHLUSS_DATEINAME:
        case JAHRESABSCHLUSS_TITEL:
        case JAHRESABSCHLUSS_SUBTITEL:
          map = new JahresabschlussListeFilterMap()
              .getMap((JahresabschlussControl) obj, map);
          break;
        case AUSWERTUNG_MITGLIED_DATEINAME:
        case AUSWERTUNG_MITGLIED_TITEL:
          map = new AuswertungMitgliedFilterMap().getMap((MitgliedControl) obj,
              map);
          break;
        case AUSWERTUNG_NICHT_MITGLIED_DATEINAME:
        case AUSWERTUNG_NICHT_MITGLIED_TITEL:
          map = new AuswertungNichtMitgliedFilterMap()
              .getMap((MitgliedControl) obj, map);
          break;
        case AUSWERTUNG_ALTERSJUBILARE_DATEINAME:
        case AUSWERTUNG_ALTERSJUBILARE_TITEL:
        case AUSWERTUNG_ALTERSJUBILARE_SUBTITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL:
          map = new AuswertungJubilareFilterMap().getMap((MitgliedControl) obj,
              map);
          break;
        case AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME:
        case AUSWERTUNG_ARBEITSEINSAETZE_TITEL:
        case AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL:
          map = new AuswertungArbeitseinsatzFilterMap()
              .getMap((ArbeitseinsatzAbrechnungControl) obj, map);
          break;
        case WIRTSCHAFTSPLAN_DATEINAME:
        case WIRTSCHAFTSPLAN_TITEL:
        case WIRTSCHAFTSPLAN_SUBTITEL:
          map = new WirtschaftsplanParameterMap()
              .getMap((WirtschaftsplanControl) obj, map);
          break;
        case SPENDENBESCHEINIGUNG_DATEINAME:
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case GUTSCHRIFT_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_TITEL:
        case WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL:
        case BUCHUNGSKLASSEN_DATEINAME:
        case BUCHUNGSKLASSEN_TITEL:
        case BUCHUNGSKLASSEN_SUBTITEL:
        case STEUERN_DATEINAME:
        case STEUERN_TITEL:
        case STEUERN_SUBTITEL:
        case BEITRAGSGRUPPEN_DATEINAME:
        case BEITRAGSGRUPPEN_TITEL:
        case BEITRAGSGRUPPEN_SUBTITEL:
        case EIGENSCHAFTENGRUPPEN_DATEINAME:
        case EIGENSCHAFTENGRUPPEN_TITEL:
        case EIGENSCHAFTENGRUPPEN_SUBTITEL:
        case EIGENSCHAFTEN_DATEINAME:
        case EIGENSCHAFTEN_TITEL:
        case EIGENSCHAFTEN_SUBTITEL:
        case ZUSATZFELDER_DATEINAME:
        case ZUSATZFELDER_TITEL:
        case ZUSATZFELDER_SUBTITEL:
        case LESEFELDER_DATEINAME:
        case LESEFELDER_TITEL:
        case LESEFELDER_SUBTITEL:
        case FORMULARE_DATEINAME:
        case FORMULARE_TITEL:
        case FORMULARE_SUBTITEL:
        case LEHRGANGSARTEN_DATEINAME:
        case LEHRGANGSARTEN_TITEL:
        case LEHRGANGSARTEN_SUBTITEL:
        case MITGLIEDSTYPEN_DATEINAME:
        case MITGLIEDSTYPEN_TITEL:
        case MITGLIEDSTYPEN_SUBTITEL:
        case WIRTSCHAFTSPLAENE_DATEINAME:
        case WIRTSCHAFTSPLAENE_TITEL:
        case WIRTSCHAFTSPLAENE_SUBTITEL:
        case JAHRESABSCHLUESSE_DATEINAME:
        case JAHRESABSCHLUESSE_TITEL:
        case JAHRESABSCHLUESSE_SUBTITEL:
        case ZUSATZBETRAEGE_VORLAGEN_DATEINAME:
        case ZUSATZBETRAEGE_VORLAGEN_TITEL:
        case ZUSATZBETRAEGE_VORLAGEN_SUBTITEL:
        case MAILVORLAGEN_DATEINAME:
        case MAILVORLAGEN_TITEL:
        case MAILVORLAGEN_SUBTITEL:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        case BUCHUNG_DOKUMENT_PFAD:
          map = new BuchungMap().getMap((Buchung) obj, map);
          break;
      }
    }
    catch (RemoteException | ApplicationException e)
    {
      Logger.error("Fehler bei Dateinamen Ersetzung: " + e.getMessage());
      return "";
    }
    return translate(map, muster,
        typ.getArtkey() == Vorlageart.DATEINAME.getKey());
  }

  // Dummy Namen Generierung aus Vorlagen Muster
  public static String getDummyName(VorlageTyp typ, String muster)
  {
    return translate(getDummyMap(typ), muster,
        typ.getArtkey() == Vorlageart.DATEINAME.getKey());
  }

  public static Map<String, Object> getDummyMap(VorlageTyp typ)
  {
    Map<String, Object> map = null;
    try
    {
      Set<Filter> set = new HashSet<>();
      map = new AllgemeineMap().getMap(null);
      switch (typ)
      {
        case ABRECHNUNGSLAEUFE_DATEINAME:
        case ABRECHNUNGSLAEUFE_SUBTITEL:
        case ABRECHNUNGSLAEUFE_TITEL:
          set = new HashSet<>();
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case ANFANGSBESTAENDE_DATEINAME:
        case ANFANGSBESTAENDE_SUBTITEL:
        case ANFANGSBESTAENDE_TITEL:
          set = new HashSet<>();
          set.add(Filter.BEZEICHNUNG);
          set.add(Filter.NUMMER);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case KONTEN_DATEINAME:
        case KONTEN_SUBTITEL:
        case KONTEN_TITEL:
          set.add(Filter.BEZEICHNUNG);
          set.add(Filter.NUMMER);
          set.add(Filter.KONTOART);
          set.add(Filter.STATUS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case KURSTEILNEHMER_DATEINAME:
        case KURSTEILNEHMER_SUBTITEL:
        case KURSTEILNEHMER_TITEL:
          set.add(Filter.NAME);
          set.add(Filter.VERWENDUNGSZWECK);
          set.add(Filter.EINGABEDATUM_VON);
          set.add(Filter.EINGABEDATUM_BIS);
          set.add(Filter.ABBUCHUNGSDATUM_VON);
          set.add(Filter.ABBUCHUNGSDATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case LASTSCHRIFTEN_DATEINAME:
        case LASTSCHRIFTEN_SUBTITEL:
        case LASTSCHRIFTEN_TITEL:
          set.add(Filter.MITGLIEDART);
          set.add(Filter.NAME);
          set.add(Filter.ZWECK);
          set.add(Filter.DATUM_FAELLIGKEI_VON);
          set.add(Filter.DATUM_FAELLIGKEI_BIS);
          set.add(Filter.ABRECHNUNGSLAUF_AB);
          set.add(Filter.VERSAND);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case LEHRGAENGE_DATEINAME:
        case LEHRGAENGE_SUBTITEL:
        case LEHRGAENGE_TITEL:
          set.add(Filter.NAME);
          set.add(Filter.VERANSTALTER);
          set.add(Filter.BEZEICHNUNG);
          set.add(Filter.LEHRGANGSART);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case MAILS_DATEINAME:
        case MAILS_SUBTITEL:
        case MAILS_TITEL:
          set.add(Filter.MAIL_EMPFAENGER);
          set.add(Filter.BETREFF);
          set.add(Filter.DATUM_BEARBEITUNG_BIS);
          set.add(Filter.DATUM_BEARBEITUNG_VON);
          set.add(Filter.DATUM_VERSAND_VON);
          set.add(Filter.DATUM_VERSAND_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case RECHNUNGEN_DATEINAME:
        case RECHNUNGEN_SUBTITEL:
        case RECHNUNGEN_TITEL:
          set.add(Filter.VERSAND);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          set.add(Filter.NAME);
          set.add(Filter.MAIL);
          set.add(Filter.OHNE_ABBUCHER);
          set.add(Filter.DIFFERENZ);
          set.add(Filter.DIFFERENZ_LIMIT);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case WIEDERVORLAGEN_DATEINAME:
        case WIEDERVORLAGEN_SUBTITEL:
        case WIEDERVORLAGEN_TITEL:
          set.add(Filter.NAME);
          set.add(Filter.VERMERK);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          set.add(Filter.OHNE_ERLEDIGUNG);
          set.add(Filter.DATUM_ERLEDIGUNG_VON);
          set.add(Filter.DATUM_ERLEDIGUNG_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case SPENDENBESCHEINIGUNG_MITGLIED_DATEINAME:
          map = SpendenbescheinigungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case MITGLIEDER_DATEINAME:
        case MITGLIEDER_TITEL:
        case MITGLIEDER_SUBTITEL:
          set.add(Filter.ZUSATZFELD);
          set.add(Filter.BEITRAGSGRUPPE);
          set.add(Filter.MITGLIEDSCHAFT_STATUS);
          set.add(Filter.STICHTAG);
          set.add(Filter.MAIL);
          set.add(Filter.NAME);
          set.add(Filter.GEBURTSDATUM_VON);
          set.add(Filter.GEBURTSDATUM_BIS);
          set.add(Filter.GESCHLECHT);
          set.add(Filter.EINTRITT_VON);
          set.add(Filter.EINTRITT_BIS);
          set.add(Filter.AUSTRITT_VON);
          set.add(Filter.AUSTRITT_BIS);
          set.add(Filter.EXTERNEMITGLIEDSNUMMER);
          set.add(Filter.MITGLIEDSNUMMER);
          set.add(Filter.DIFFERENZ);
          set.add(Filter.DIFFERENZ_LIMIT);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          set.add(Filter.EIGENSCHAFTEN);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case NICHT_MITGLIEDER_DATEINAME:
        case NICHT_MITGLIEDER_TITEL:
        case NICHT_MITGLIEDER_SUBTITEL:
          set.add(Filter.ZUSATZFELD);
          set.add(Filter.MITGLIEDSTYP);
          set.add(Filter.MAIL);
          set.add(Filter.NAME);
          set.add(Filter.GEBURTSDATUM_VON);
          set.add(Filter.GEBURTSDATUM_BIS);
          set.add(Filter.GESCHLECHT);
          set.add(Filter.DIFFERENZ);
          set.add(Filter.DIFFERENZ_LIMIT);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          set.add(Filter.EIGENSCHAFTEN);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case RECHNUNG_MITGLIED_DATEINAME:
        case MAHNUNG_MITGLIED_DATEINAME:
          map = RechnungMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case PRENOTIFICATION_MITGLIED_DATEINAME:
          map = LastschriftMap.getDummyMap(map);
          map = MitgliedMap.getDummyMap(map);
          break;
        case PRENOTIFICATION_KURSTEILNEHMER_DATEINAME:
          map = LastschriftMap.getDummyMap(map);
          break;
        case PERSONALBOGEN_MITGLIED_DATEINAME:
        case PERSONALBOGEN_TITEL:
        case PERSONALBOGEN_SUBTITEL:
        case VCARD_MITGLIED_DATEINAME:
        case KONTOAUSZUG_MITGLIED_DATEINAME:
        case KONTOAUSZUG_TITEL:
        case KONTOAUSZUG_SUBTITEL:
        case MITGLIED_ZUSATZBETRAEGE_DATEINAME:
        case MITGLIED_ZUSATZBETRAEGE_TITEL:
        case MITGLIED_ZUSATZBETRAEGE_SUBTITEL:
        case MITGLIED_WIEDERVORLAGEN_DATEINAME:
        case MITGLIED_WIEDERVORLAGEN_TITEL:
        case MITGLIED_WIEDERVORLAGEN_SUBTITEL:
        case MITGLIED_MAILS_DATEINAME:
        case MITGLIED_MAILS_TITEL:
        case MITGLIED_MAILS_SUBTITEL:
        case MITGLIED_LEHRGAENGE_DATEINAME:
        case MITGLIED_LEHRGAENGE_TITEL:
        case MITGLIED_LEHRGAENGE_SUBTITEL:
        case MITGLIED_LESEFELDER_DATEINAME:
        case MITGLIED_LESEFELDER_TITEL:
        case MITGLIED_LESEFELDER_SUBTITEL:
        case MITGLIED_ARBEITSEINSAETZE_DATEINAME:
        case MITGLIED_ARBEITSEINSAETZE_TITEL:
        case MITGLIED_ARBEITSEINSAETZE_SUBTITEL:
        case MITGLIED_DOKUMENTE_DATEINAME:
        case MITGLIED_DOKUMENTE_TITEL:
        case MITGLIED_DOKUMENTE_SUBTITEL:
        case MITGLIED_DOKUMENT_PFAD:
          map = MitgliedMap.getDummyMap(map);
          break;
        case FREIES_FORMULAR_DATEINAME:
          map.put("formular_name", "Freies Formular");
          break;
        case FORMULAR_DATEINAME:
          map.put("formular_name", "Rechnung");
          break;
        case FORMULARFELDER_DATEINAME:
        case FORMULARFELDER_TITEL:
        case FORMULARFELDER_SUBTITEL:
          map.put("formular_name", "Rechnung");
          break;
        case FREIES_FORMULAR_MITGLIED_DATEINAME:
          map = MitgliedMap.getDummyMap(map);
          map.put("formular_name", "Freies Formular");
          break;
        case KONTENSALDO_DATEINAME:
        case KONTENSALDO_TITEL:
        case KONTENSALDO_SUBTITEL:
        case BUCHUNGSKLASSENSALDO_DATEINAME:
        case BUCHUNGSKLASSENSALDO_TITEL:
        case BUCHUNGSKLASSENSALDO_SUBTITEL:
        case UMSATZSTEUER_VORANMELDUNG_DATEINAME:
        case UMSATZSTEUER_VORANMELDUNG_TITEL:
        case UMSATZSTEUER_VORANMELDUNG_SUBTITEL:
        case PROJEKTSALDO_DATEINAME:
        case PROJEKTSALDO_TITEL:
        case PROJEKTSALDO_SUBTITEL:
        case ANLAGENVERZEICHNIS_DATEINAME:
        case ANLAGENVERZEICHNIS_TITEL:
        case ANLAGENVERZEICHNIS_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_DATEINAME:
        case MITTELVERWENDUNGSREPORT_SALDO_TITEL:
        case MITTELVERWENDUNGSREPORT_SALDO_SUBTITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_DATEINAME:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_TITEL:
        case MITTELVERWENDUNGSREPORT_ZUFLUSS_SUBTITEL:
        case MITTELVERWENDUNGSSALDO_DATEINAME:
        case MITTELVERWENDUNGSSALDO_TITEL:
        case MITTELVERWENDUNGSSALDO_SUBTITEL:
          map = SaldoFilterMap.getDummyMap(map);
          break;
        case ABRECHNUNGSLAUF_SEPA_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN_DATEINAME:
          map = AbrechnungslaufParameterMap.getDummyMap(map);
          break;
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_DATEINAME:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_TITEL:
        case ABRECHNUNGSLAUF_LASTSCHRIFTEN2_SUBTITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_DATEINAME:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_TITEL:
        case ABRECHNUNGSLAUF_ZUSATZBETRAEGE_SUBTITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_SOLLBUCHUNGEN_SUBTITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_DATEINAME:
        case ABRECHNUNGSLAUF_BUCHUNGEN_TITEL:
        case ABRECHNUNGSLAUF_BUCHUNGEN_SUBTITEL:
          map = AbrechnungSollbuchungenParameterMap.getDummyMap(map);
          break;
        case SOLLBUCHUNGEN_DATEINAME:
        case SOLLBUCHUNGEN_TITEL:
        case SOLLBUCHUNGEN_SUBTITEL:
          set.add(Filter.MITGLIED);
          set.add(Filter.ZAHLER);
          set.add(Filter.MAIL);
          set.add(Filter.DIFFERENZ);
          set.add(Filter.DIFFERENZ_LIMIT);
          set.add(Filter.OHNE_ABBUCHER);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case ZUSATZBETRAEGE_DATEINAME:
        case ZUSATZBETRAEGE_TITEL:
        case ZUSATZBETRAEGE_SUBTITEL:
          map = ZusatzbetragListeFilterMap.getDummyMap(map);
          break;
        case ARBEITSEINSAETZE_DATEINAME:
        case ARBEITSEINSAETZE_TITEL:
        case ARBEITSEINSAETZE_SUBTITEL:
          set.add(Filter.NAME);
          set.add(Filter.BEMERKUNG);
          set.add(Filter.DATUM_VON);
          set.add(Filter.DATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case SPENDENBESCHEINIGUNGEN_DATEINAME:
        case SPENDENBESCHEINIGUNGEN_TITEL:
        case SPENDENBESCHEINIGUNGEN_SUBTITEL:
          set.add(Filter.DATUM_SPENDE_VON);
          set.add(Filter.DATUM_SPENDE_BIS);
          set.add(Filter.DATUM_BESCHEINIGUNG_VON);
          set.add(Filter.DATUM_BESCHEINIGUNG_BIS);
          set.add(Filter.ZEILE2);
          set.add(Filter.MAIL);
          set.add(Filter.SPENDENART);
          set.add(Filter.VERSAND);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case BUCHUNGEN_DATEINAME:
        case BUCHUNGEN_TITEL:
        case BUCHUNGEN_SUBTITEL:
        case BUCHUNGSJOURNAL_DATEINAME:
        case BUCHUNGSJOURNAL_TITEL:
        case BUCHUNGSJOURNAL_SUBTITEL:
        case EINZELBUCHUNGEN_DATEINAME:
        case EINZELBUCHUNGEN_TITEL:
        case EINZELBUCHUNGEN_SUBTITEL:
        case SUMMENBUCHUNGEN_DATEINAME:
        case SUMMENBUCHUNGEN_TITEL:
        case SUMMENBUCHUNGEN_SUBTITEL:
        case CSVBUCHUNGEN_DATEINAME:
          map = BuchungListeFilterMap.getDummyMap(map);
          break;
        case ANLAGEN_BUCHUNGEN_DATEINAME:
        case ANLAGEN_BUCHUNGEN_TITEL:
        case ANLAGEN_BUCHUNGEN_SUBTITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_DATEINAME:
        case ANLAGEN_BUCHUNGSJOURNAL_TITEL:
        case ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL:
        case ANLAGEN_EINZELBUCHUNGEN_DATEINAME:
        case ANLAGEN_EINZELBUCHUNGEN_TITEL:
        case ANLAGEN_EINZELBUCHUNGEN_SUBTITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_DATEINAME:
        case ANLAGEN_SUMMENBUCHUNGEN_TITEL:
        case ANLAGEN_SUMMENBUCHUNGEN_SUBTITEL:
        case ANLAGEN_CSVBUCHUNGEN_DATEINAME:
          map = AnlagenbuchungListeFilterMap.getDummyMap(map);
          break;
        case JAHRESABSCHLUSS_DATEINAME:
        case JAHRESABSCHLUSS_TITEL:
        case JAHRESABSCHLUSS_SUBTITEL:
          map = JahresabschlussListeFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_MITGLIED_DATEINAME:
        case AUSWERTUNG_MITGLIED_TITEL:
          map = AuswertungMitgliedFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_NICHT_MITGLIED_DATEINAME:
        case AUSWERTUNG_NICHT_MITGLIED_TITEL:
          map = AuswertungNichtMitgliedFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_KURSTEILNEHMER_DATEINAME:
        case AUSWERTUNG_KURSTEILNEHMER_TITEL:
        case AUSWERTUNG_KURSTEILNEHMER_SUBTITEL:
          set.add(Filter.ABBUCHUNGSDATUM_VON);
          set.add(Filter.ABBUCHUNGSDATUM_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case AUSWERTUNG_MITGLIEDER_STATISTIK_DATEINAME:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_TITEL:
        case AUSWERTUNG_MITGLIEDER_STATISTIK_SUBTITEL:
          set.add(Filter.STICHTAG);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case AUSWERTUNG_ALTERSJUBILARE_DATEINAME:
        case AUSWERTUNG_ALTERSJUBILARE_TITEL:
        case AUSWERTUNG_ALTERSJUBILARE_SUBTITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_DATEINAME:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_TITEL:
        case AUSWERTUNG_MITGLIEDSCHAFTSJUBILARE_SUBTITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_DATEINAME:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_TITEL:
        case AUSWERTUNG_JAHRGANGS_STATISTIK_SUBTITEL:
          map = AuswertungJubilareFilterMap.getDummyMap(map);
          break;
        case AUSWERTUNG_ARBEITSEINSAETZE_DATEINAME:
        case AUSWERTUNG_ARBEITSEINSAETZE_TITEL:
        case AUSWERTUNG_ARBEITSEINSAETZE_SUBTITEL:
          map = AuswertungArbeitseinsatzFilterMap.getDummyMap(map);
          break;
        case BUCHUNGSARTEN_DATEINAME:
        case BUCHUNGSARTEN_TITEL:
        case BUCHUNGSARTEN_SUBTITEL:
          set.add(Filter.BEZEICHNUNG);
          set.add(Filter.NUMMER);
          set.add(Filter.BUCHUNGSKLASSE);
          set.add(Filter.BUCHUNGSARTART);
          set.add(Filter.STATUS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case WIRTSCHAFTSPLAN_DATEINAME:
        case WIRTSCHAFTSPLAN_TITEL:
        case WIRTSCHAFTSPLAN_SUBTITEL:
          map = WirtschaftsplanParameterMap.getDummyMap(map);
          break;
        case PROJEKTE_DATEINAME:
        case PROJEKTE_TITEL:
        case PROJEKTE_SUBTITEL:
          set.add(Filter.BEZEICHNUNG);
          set.add(Filter.DATUM_ENDE_VON);
          set.add(Filter.DATUM_ENDE_BIS);
          set.add(Filter.DATUM_START_VON);
          set.add(Filter.DATUM_START_BIS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case ABWEICHENDE_ZAHLER_DATEINAME:
        case ABWEICHENDE_ZAHLER_SUBTITEL:
        case ABWEICHENDE_ZAHLER_TITEL:
          set.add(Filter.MITGLIEDSCHAFT_STATUS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case FAMILIENVERBAND_DATEINAME:
        case FAMILIENVERBAND_SUBTITEL:
        case FAMILIENVERBAND_TITEL:
          set.add(Filter.MITGLIEDSCHAFT_STATUS);
          map = new FilterMap().getDummyMap(set, map);
          break;
        case SPENDENBESCHEINIGUNG_DATEINAME:
        case RECHNUNG_DATEINAME:
        case MAHNUNG_DATEINAME:
        case KONTOAUSZUG_DATEINAME:
        case CT1_AUSGABE_DATEINAME:
        case GUTSCHRIFT_DATEINAME:
        case PRENOTIFICATION_DATEINAME:
        case PERSONALBOGEN_DATEINAME:
        case KONTENRAHMEN_DATEINAME_V1:
        case KONTENRAHMEN_DATEINAME_V2:
        case VCARD_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_DATEINAME:
        case WIRTSCHAFTSPLAN_MEHRERE_TITEL:
        case WIRTSCHAFTSPLAN_MEHRERE_SUBTITEL:
        case BUCHUNGSKLASSEN_DATEINAME:
        case BUCHUNGSKLASSEN_TITEL:
        case BUCHUNGSKLASSEN_SUBTITEL:
        case STEUERN_DATEINAME:
        case STEUERN_TITEL:
        case STEUERN_SUBTITEL:
        case BEITRAGSGRUPPEN_DATEINAME:
        case BEITRAGSGRUPPEN_TITEL:
        case BEITRAGSGRUPPEN_SUBTITEL:
        case EIGENSCHAFTENGRUPPEN_DATEINAME:
        case EIGENSCHAFTENGRUPPEN_TITEL:
        case EIGENSCHAFTENGRUPPEN_SUBTITEL:
        case EIGENSCHAFTEN_DATEINAME:
        case EIGENSCHAFTEN_TITEL:
        case EIGENSCHAFTEN_SUBTITEL:
        case ZUSATZFELDER_DATEINAME:
        case ZUSATZFELDER_TITEL:
        case ZUSATZFELDER_SUBTITEL:
        case LESEFELDER_DATEINAME:
        case LESEFELDER_TITEL:
        case LESEFELDER_SUBTITEL:
        case FORMULARE_DATEINAME:
        case FORMULARE_TITEL:
        case FORMULARE_SUBTITEL:
        case LEHRGANGSARTEN_DATEINAME:
        case LEHRGANGSARTEN_TITEL:
        case LEHRGANGSARTEN_SUBTITEL:
        case MITGLIEDSTYPEN_DATEINAME:
        case MITGLIEDSTYPEN_TITEL:
        case MITGLIEDSTYPEN_SUBTITEL:
        case WIRTSCHAFTSPLAENE_DATEINAME:
        case WIRTSCHAFTSPLAENE_TITEL:
        case WIRTSCHAFTSPLAENE_SUBTITEL:
        case JAHRESABSCHLUESSE_DATEINAME:
        case JAHRESABSCHLUESSE_TITEL:
        case JAHRESABSCHLUESSE_SUBTITEL:
        case ZUSATZBETRAEGE_VORLAGEN_DATEINAME:
        case ZUSATZBETRAEGE_VORLAGEN_TITEL:
        case ZUSATZBETRAEGE_VORLAGEN_SUBTITEL:
        case MAILVORLAGEN_DATEINAME:
        case MAILVORLAGEN_TITEL:
        case MAILVORLAGEN_SUBTITEL:
          // Bei zip oder einzelnes Dokument für mehrere Einträge
          // Nur die allgemeine Map
          break;
        case BUCHUNG_DOKUMENT_PFAD:
          map = new BuchungMap().getMap(null, map);
          break;
      }
    }
    catch (RemoteException e)
    {
      //
    }
    return map;
  }

  public static String translate(Map<String, Object> map, String inString,
      boolean dateiname)
  {
    try
    {

      String in = inString.replaceAll("-\\$", "\\'\\#\\'\\$");
      String str = VelocityTool.eval(map, in);
      str = str.replaceAll("\\'\\#\\'", "-");
      if (dateiname)
      {
        str = Zeichen.convert(str);
        str = str.replaceAll("[^a-zA-Z0-9_\\-\\. ]", "_");
      }
      return str;
    }
    catch (Exception e)
    {
      Logger.error("Format Fehler bei der Dateinamen/Titel Ersetzung: "
          + e.getMessage());
      return "Format Fehler bei der Dateinamen/Titel Ersetzung.";

    }
  }

  private static String getVorlageMuster(VorlageTyp typ) throws RemoteException
  {
    DBIterator<Vorlage> vorlagen = Einstellungen.getDBService()
        .createList(Vorlage.class);
    vorlagen.addFilter(Vorlage.KEY + " = ?", typ.getKey());
    if (vorlagen.hasNext())
    {
      return vorlagen.next().getMuster();
    }
    return typ.getDefault();
  }
}
